/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core.output;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.core.ManagementConstants;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.helper.UIHelperRegistry;
import net.ikarus_systems.icarus.ui.text.Console;
import net.ikarus_systems.icarus.ui.view.AWTPresenter;
import net.ikarus_systems.icarus.ui.view.PresenterUtils;
import net.ikarus_systems.icarus.ui.view.UnsupportedPresentationDataException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.Wrapper;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;
import net.ikarus_systems.icarus.util.mpi.ResultMessage;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultOutputView extends View implements ManagementConstants {
	
	public static final String VIEW_ID = ManagementConstants.DEFAULT_OUTPUT_VIEW_ID;
	
	private JTabbedPane presenterPane;
	
	private Handler handler;

	public DefaultOutputView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		handler = new Handler();
		
		addBroadcastListener(ManagementConstants.EXPLORER_SELECTION_CHANGED, handler);
		addBroadcastListener(ManagementConstants.LOG_SELECTION_CHANGED, handler);
		
		if(ConfigRegistry.getGlobalRegistry().getBoolean(
				"general.appearance.showSystemStreams")) { //$NON-NLS-1$
			Console console = Console.getInstance();
			
			// Display sys.out redirect
			Options options = new Options();			
			options.put(Options.TITLE, "System.out"); //$NON-NLS-1$
			displayData(console.getOutputDocument(), console, options);
			
			if(!console.isMergeStreams()) {
				// Display sys.err redirect
				options = new Options();
				options.put(Options.TITLE, "System.err"); //$NON-NLS-1$
				displayData(console.getErrorDocument(), console, options);
			}
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#close()
	 */
	@Override
	public void close() {
		reset();
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		if(presenterPane==null) {
			return;
		}
		
		for(int i=presenterPane.getTabCount()-1; i>-1; i--) {
			TabComponent tabComponent = (TabComponent) presenterPane.getTabComponentAt(i);
			if(tabComponent.presenter!=null) {
				tabComponent.presenter.close();
			}
		}
		presenterPane.removeAll();
	}
	
	private TabComponent findTabForOwner(Object owner) {
		if(owner==null) {
			return null;
		}
		
		TabComponent comp = null;
		
		for(int i=0; i<presenterPane.getTabCount(); i++) {
			comp = (TabComponent)presenterPane.getTabComponentAt(i);
			if(comp.getOwner()==owner) {
				return comp;
			}
		}
		
		return null;
	}
	
	private boolean canDisplay(Object data) {
		if(data==null) {
			return false;
		}
		return UIHelperRegistry.globalRegistry().hasHelper(
				AWTPresenter.class, data);
	}
	
	public void displayData(Object data, Object owner, Options options) {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		if(owner==null)
			throw new IllegalArgumentException("Invalid owner"); //$NON-NLS-1$
		
		selectViewTab();
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		// Lazily create presenter pane
		if(presenterPane==null) {
			presenterPane = new JTabbedPane();
			UIUtil.defaultHideTabbedPaneDecoration(presenterPane);
			presenterPane.setBorder(null);
			JComponent container = getContainer();
			container.setLayout(new BorderLayout());
			container.add(presenterPane, BorderLayout.CENTER);
		}
		
		TabComponent tabComponent = null;
		
		// Recycle tabs if allowed
		if(options.get(ManagementConstants.REUSE_TAB_OPTION, false)) {
			tabComponent = findTabForOwner(owner);
		}

		int tabIndex = -1;
		if(tabComponent==null) {
			tabComponent = new TabComponent(owner);
			tabIndex = presenterPane.getTabCount();
			presenterPane.add(new JLabel(), tabIndex);
			presenterPane.setTabComponentAt(tabIndex, tabComponent);
		} else {
			tabIndex = presenterPane.indexOfTabComponent(tabComponent);
		}
		
		tabComponent.setPresentedObject(data, options);
		
		presenterPane.setSelectedIndex(tabIndex);
	}

	/**
	 * Accepted commands:
	 * <ul>
	 * <li>{@link Commands#DISPLAY}</li>
	 * <li>{@link Commands#PRESENT}</li>
	 * <li>{@link Commands#CLEAR}</li>
	 * </ul>
	 * 
	 * @see net.ikarus_systems.icarus.plugins.core.View#handleRequest(net.ikarus_systems.icarus.util.mpi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		
		Object data = message.getData();
		
		// Unwrap wrapped data
		if(data instanceof Wrapper) {
			data = ((Wrapper<?>)data).get();
		}
		
		Object owner = message.getOption(Options.OWNER);
		
		if(Commands.DISPLAY.equals(message.getCommand())
				|| Commands.PRESENT.equals(message.getCommand())) {
			
			if(owner==null) {
				return message.errorResult(this, new IllegalArgumentException("Missing owner")); //$NON-NLS-1$
			}
			
			// Check if there is a presenter available for the supplied object
			if(!canDisplay(data)) {
				return message.unsupportedDataResult(this);
			}
			
			displayData(data, owner, message.getOptions());
			
			return message.successResult(this, null);
		} else if(Commands.CLEAR.equals(message.getCommand())) {
			reset();
			return message.successResult(this, null);
		} else {
			return message.unknownRequestResult(this);
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private class TabComponent extends JLabel implements MouseListener {
		
		private static final long serialVersionUID = -9157665763570448381L;
		
		private AWTPresenter presenter;
		@SuppressWarnings("unused")
		private Object presentedObject;
		private Options options;
		private final Object owner;
		
		TabComponent(Object owner) {
			this.owner = owner;
			addMouseListener(this);
		}
		
		@SuppressWarnings("unused")
		private void close() {
			if(presenter!=null) {
				presenter.clear();
			}
			
			presenter = null;
			presentedObject = null;
			options = null;
		}
		
		@Override
		public String getText() {
			int tabIndex = presenterPane.indexOfTabComponent(this);
			return tabIndex==-1 ? null : presenterPane.getTitleAt(tabIndex);
		}
		
		@Override
		public Icon getIcon() {
			int tabIndex = presenterPane.indexOfTabComponent(this);
			return tabIndex==-1 ? null : presenterPane.getIconAt(tabIndex);
		}
		
		@Override
		public String getToolTipText() {
			int tabIndex = presenterPane.indexOfTabComponent(this);
			return tabIndex==-1 ? null : presenterPane.getToolTipTextAt(tabIndex);
		}

		/**
		 * @return the owner
		 */
		public Object getOwner() {
			return owner;
		}
		
		private void showInfoLabel(String infoText) {
			int tabIndex = presenterPane.indexOfTabComponent(this);
			if(tabIndex==-1) {
				return;
			}
			JLabel infoLabel = new JLabel(infoText);
			infoLabel.setBorder(new EmptyBorder(50, 100, 100, 100));
			infoLabel.setVerticalAlignment(SwingConstants.TOP);
			infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			presenterPane.setComponentAt(tabIndex, infoLabel);
		}

		/**
		 * @param presentedObject the presentedObject to set
		 */
		public void setPresentedObject(Object presentedObject, Options options) {

			// Show info for empty content data
			if(presentedObject==null) {
				String infoText = ResourceManager.getInstance().get(
						"plugins.core.outputView.emptyContent"); //$NON-NLS-1$
				showInfoLabel(infoText);
				return;
			}
			
			AWTPresenter presenter = this.presenter;
			
			// Try to find a suitable presenter if no presenter
			// is present or the current presenter is not able to
			// present the given data
			if(presenter==null || !PresenterUtils.presenterSupports(presenter, presentedObject)) {
				presenter = UIHelperRegistry.globalRegistry().findHelper(
						AWTPresenter.class, presentedObject);
			}
			
			// Only with a suitable presenter show the content
			if(presenter!=null && PresenterUtils.presenterSupports(presenter, presentedObject)) {
				this.presentedObject = presentedObject;
				this.options = options;
				this.presenter = presenter;
				
				if(this.options==null) {
					this.options = Options.emptyOptions;
				}
				
				try {
					presenter.present(presentedObject, options);
					
					// Display presenter component
					int tabIndex = presenterPane.indexOfTabComponent(this);
					Component comp = presenter.getPresentingComponent();
					if(comp instanceof JComponent) {
						((JComponent)comp).setBorder(UIUtil.topLineBorder);
					}
					presenterPane.setComponentAt(tabIndex, comp);
					String title = options.get(Options.TITLE, String.valueOf(presentedObject));
					presenterPane.setTitleAt(tabIndex, title);
					presenterPane.setToolTipTextAt(tabIndex, title);
				} catch (UnsupportedPresentationDataException e) {
					LoggerFactory.log(this, Level.SEVERE, "Failed to present data: "+presentedObject, e); //$NON-NLS-1$
					
					// Show info if data not supported
					String infoText = ResourceManager.getInstance().get(
							"plugins.core.outputView.presentationFailed"); //$NON-NLS-1$
					showInfoLabel(infoText);
					return;
				}
			} else {
				// Inform user about unsupported data
				String infoText = ResourceManager.getInstance().get(
						"plugins.core.outputView.unsupportedContent"); //$NON-NLS-1$
				showInfoLabel(infoText);
			}
		}

		/**
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
				DefaultOutputView.this.toggleContainer();
			}
		}

		/**
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			// no-op
		}

		/**
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			// no-op
		}

		/**
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
			// no-op
		}

		/**
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseExited(MouseEvent e) {
			// no-op
		}
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public final class CallbackHandler {
		
		private CallbackHandler() {
			// no-op
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private class Handler implements EventListener {

		/**
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			Object item = event.getProperty("item"); //$NON-NLS-1$
			
			if(!canDisplay(item)) {
				return;
			}
			
			Options options = (Options)event.getProperty("options"); //$NON-NLS-1$
			if(options==null) {
				options = Options.emptyOptions;
			}
			
			Object owner = options.get(Options.OWNER);
			if(owner==null) {
				LoggerFactory.log(this, Level.INFO, "No owner set for item to display: "+item); //$NON-NLS-1$
				return;
			}
			
			displayData(item, owner, options);
		}
	}
}
