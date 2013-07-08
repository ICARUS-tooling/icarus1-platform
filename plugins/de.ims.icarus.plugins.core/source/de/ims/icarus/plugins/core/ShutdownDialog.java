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

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ShutdownDialog {
	
	private JProgressBar progressBar;
	
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
		// TODO create dialog and invoke shutdown calls on various frameworks
	
		if(true)
		throw new IllegalArgumentException();
		
		System.exit(0);
	}
}
