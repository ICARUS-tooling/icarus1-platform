/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.core;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import de.ims.icarus.Core;
import de.ims.icarus.Core.NamedRunnable;
import de.ims.icarus.logging.LoggerFactory;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ShutdownDialog {
	
	private JProgressBar progressBar;
	private JDialog dialog;
	private JLabel label;
	
	private static ShutdownDialog instance;
	
	private boolean shutdownStarted = false;
	
	public static ShutdownDialog getDialog() {
		if(instance==null) {
			synchronized (ShutdownDialog.class) {
				if(instance==null) {
					instance = new ShutdownDialog();
				}
			}
		}
		
		return instance;
	}

	private ShutdownDialog() {
		// no-op
	}

	public synchronized void shutdown() {
		
		if(!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					shutdown();
				}
			});
			return;
		}
		
		if(shutdownStarted) {
			return;
		}
		shutdownStarted = true;
		
		// Build panel
		
		dialog = new JDialog();
		dialog.setIconImages(Core.getIconImages());
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(false);
		progressBar.setBorder(new EmptyBorder(5, 15, 10, 15));
		progressBar.setPreferredSize(new Dimension(250, 20));
		
		label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setVerticalAlignment(SwingConstants.BOTTOM);
		label.setBorder(new EmptyBorder(10, 15, 1, 15));
		label.setPreferredSize(new Dimension(250, 100));
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label, BorderLayout.CENTER);
		panel.add(progressBar, BorderLayout.SOUTH);
		
		dialog.add(progressBar);
		dialog.pack();
		dialog.setLocationRelativeTo(null);

		NamedRunnable[] hooks = Core.getCore().getShutdownHooks();
		progressBar.setMaximum(hooks.length);
		
		// Dispatch worker
		new ShutdownWorker(hooks).execute();
		
		dialog.setVisible(true);
	}
	
	private class ShutdownWorker extends SwingWorker<Object, String> {
		
		private final NamedRunnable[] hooks;
		
		ShutdownWorker(NamedRunnable[] hooks) {
			this.hooks = hooks;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Object doInBackground() throws Exception {
			
			for(NamedRunnable hook : hooks) {
				try {
					publish(hook.getName());
					hook.run();
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to execute shutdown hook: "+hook.getName(), e); //$NON-NLS-1$
				}
			}
			
			return null;
		}

		@Override
		protected void process(List<String> chunks) {
			if(chunks==null || chunks.isEmpty()) {
				return;
			}
			
			String text = chunks.get(chunks.size()-1);
			int steps = chunks.size();
			
			label.setText(text);
			progressBar.setValue(progressBar.getValue()+steps);
		}

		@Override
		protected void done() {
			boolean success = true;
			try {
				get();
			} catch(Exception e) {
				success = false;
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to execute all shutdown hooks", e); //$NON-NLS-1$
			}
			
			try {
				FrameManager.getInstance().shutdown();
			} catch(Exception e) {
				success = false;
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to shutdown frame manager", e); //$NON-NLS-1$
			}
			
			System.exit(success ? 0 : 1);
		}
		
	}
}
