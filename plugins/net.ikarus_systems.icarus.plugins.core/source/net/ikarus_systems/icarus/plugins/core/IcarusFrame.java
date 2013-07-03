/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.Core;
import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.EventSource;
import net.ikarus_systems.icarus.ui.tasks.TaskManager;
import net.ikarus_systems.icarus.ui.tasks.TaskPanel;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.id.Identity;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class IcarusFrame extends JFrame {

	private static final long serialVersionUID = 8408785238638475020L;
	
	private Perspective currentPerspective;
	
	private ActionManager actionManager;
	
	private Map<Perspective, PerspectiveContainer> containers;
	private JPanel rootPanel;
	
	private InfoPanel infoPanel;
	private TaskPanel taskPanel;
	private PerspectivePanel perspectivePanel;
	private JPanel footerPanel;
	
	private IcarusFrameDelegate frameDelegate;
	private MenuDelegate menuDelegate;
	private ToolBarDelegate toolBarDelegate;
	private CallbackHandler callbackHandler;
	private Handler handler;
	
	private EventSource eventSource;

	/**
	 * 
	 */
	public IcarusFrame(Options options) {
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		setIconImage(Core.getSmallIcon().getImage());
		
		currentPerspective = readPerspectiveOption(options);
		
		refreshTitle();
	}
	
	private Perspective readPerspectiveOption(Options options) {

		Extension perspectiveExtension = null;
		
		Object perspective = options.get("perspective"); //$NON-NLS-1$
		
		// Interpret a string as guid for some Perspective extension
		if(perspective instanceof String) {
			perspective = PluginUtil.findExtension(IcarusCorePlugin.PLUGIN_ID, 
					"Perspective", (String)perspective); //$NON-NLS-1$
		}
		if(perspective instanceof Extension) {
			Extension extension = (Extension) perspective;
			perspectiveExtension = extension;
			Extension.Parameter param = extension.getParameter("class"); //$NON-NLS-1$
			if(param==null)
				throw new IllegalArgumentException("Provided extension does not declare a class"); //$NON-NLS-1$
			ClassLoader loader = PluginUtil.getClassLoader(extension);
			try {
				perspective = loader.loadClass(param.valueAsString());
			} catch(ClassNotFoundException e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to load class for perspective extension: "+extension.getUniqueId(), e); //$NON-NLS-1$
				throw new IllegalArgumentException("Not a valid perspective class: "+perspective, e); //$NON-NLS-1$
			}
		}
		if(perspective instanceof Class<?>) {
			Class<?> clazz = (Class<?>) perspective;
			if(!Perspective.class.isAssignableFrom(clazz))
				throw new IllegalArgumentException(
						"Supplied perspective class is not compatible with "+Perspective.class.getName() //$NON-NLS-1$
						+" : "+perspective); //$NON-NLS-1$
			
			if(perspectiveExtension==null) {
				perspectiveExtension = findDeclaringExtension(clazz);
			}
			
			try {
				Perspective newPerspective = (Perspective) clazz.newInstance();
				newPerspective.setExtension(perspectiveExtension);
				perspective = newPerspective;
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to instantiate perspective class: "+clazz, e); //$NON-NLS-1$
				throw new IllegalArgumentException("Not a valid perspective class: "+perspective, e); //$NON-NLS-1$
			}
		}
		
		// Make sure we only accept Perspective objects
		if(perspective!=null && !(perspective instanceof Perspective))
			throw new IllegalArgumentException("Provided object is not a perspective: "+perspective); //$NON-NLS-1$
		
		return (Perspective) perspective;
	}
	
	void init() throws Exception {
		
		// DEBUG
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		// Load actions
		URL actionLocation = IcarusFrame.class.getResource(
				"icarus-frame-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: icarus-frame-actions.xml"); //$NON-NLS-1$
		
		try {
			getActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this,	Level.SEVERE, "Failed to load actions from file", e); //$NON-NLS-1$
			JPanel container = new JPanel();
			getContentPane().add(container);
			UIDummies.createDefaultErrorOutput(container, e);
			pack();
			return;
		}
		
		rootPanel = new JPanel();
		menuDelegate = new MenuDelegate("plugins.core.icarusFrame.menuBarList"); //$NON-NLS-1$
		menuDelegate.setActionManager(getActionManager());
		
		toolBarDelegate = new ToolBarDelegate();

		// TODO read config to get default perspective?
		
		Object defaultPerspective = ConfigRegistry.getGlobalRegistry().getValue(
				"general.appearance.defaultPerspective"); //$NON-NLS-1$
		
		if("NONE".equals(defaultPerspective)) { //$NON-NLS-1$
			defaultPerspective = null;
		}
		
		if(defaultPerspective instanceof String) {
			try {
				defaultPerspective = PluginUtil.getExtension((String) defaultPerspective);
			} catch(Exception e) {
				LoggerFactory.log(this,	Level.SEVERE, "Failed to fetch perspective-extension: "+defaultPerspective, e); //$NON-NLS-1$
			}
		}
		
		if(defaultPerspective instanceof Extension) {
			try {
				Extension extension = (Extension) defaultPerspective;
				currentPerspective = (Perspective) PluginUtil.instantiate(extension);
				currentPerspective.setExtension(extension);
			} catch(Exception e) {
				LoggerFactory.log(this,	Level.SEVERE, "Failed to instantiate default perspective: "+defaultPerspective, e); //$NON-NLS-1$
			}
		}
		
		if(currentPerspective!=null){
			openPerspective(currentPerspective, true);
		} else {
			PerspectiveChooser chooser = new PerspectiveChooser(getHandler());
			chooser.init(rootPanel);
		}
		
		getContentPane().add(rootPanel);
		pack();
		
		setMinimumSize(new Dimension(500, 400));
		setSize(new Dimension(1000, 650));
		// DEBUG DUMMY
		//setSize(new Dimension(800, 461));
		
		// TODO find out why setLocationByPlatform moves the frame southwards
		setLocationRelativeTo(null);
		//setLocationByPlatform(true);
		
		registerActionCallbacks();
	}
	
	private TaskPanel getTaskPanel() {
		if(taskPanel==null) {
			taskPanel = new TaskPanel(TaskManager.getInstance());
		}
		return taskPanel;
	}
	
	private InfoPanel getInfoPanel() {
		if(infoPanel==null) {
			infoPanel = new InfoPanel();
		}
		return infoPanel;
	}
	
	private JPanel getFooterPanel() {
		if(footerPanel==null) {
			footerPanel = new JPanel(new BorderLayout());
			footerPanel.add(getInfoPanel().getContentPanel(), BorderLayout.CENTER);
			footerPanel.add(getTaskPanel(), BorderLayout.EAST);
		}
		return footerPanel;
	}
	
	InfoPanel getInfoPanel(Perspective perspective) {
		if(perspective!=currentPerspective) {
			return null;
		}
		
		return infoPanel;
	}
	
	private IcarusFrameDelegate getFrameDelegate() {
		if(frameDelegate==null) {
			frameDelegate = new IcarusFrameDelegate();
		}
		return frameDelegate;
	}
	
	private Handler getHandler() {
		if(handler == null) {
			handler = new Handler();
		}
		return handler;
	}
	
	private CallbackHandler getCallbackHandler() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		return callbackHandler;
	}
	
	private EventSource getEventSource() {
		if(eventSource==null) {
			eventSource = new EventSource(this);
			// From now on listen to window events
			addWindowListener(getHandler());
		}
		return eventSource;
	}
	
	private ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = ActionManager.globalManager().derive();
		}
		return actionManager;
	}
	
	/**
	 * @see #openPerspective(Object, boolean)
	 */
	Perspective ensurePerspective(Object perspective) {
		try {
			openPerspective(perspective, false);
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Unable to open perspective: "+perspective, e); //$NON-NLS-1$
			return null;
		}
		
		return currentPerspective;
	}
	
	private Object getOpenedPerspective(Object data) {
		if(data==null || containers==null || containers.isEmpty()) {
			return data;
		}
		
		for(Perspective perspective : containers.keySet()) {
			if(data instanceof Extension && perspective.getExtension()==data) {
				data = perspective;
				break;
			} else if(data instanceof Class && perspective.getClass()==data) {
				data = perspective;
				break;
			} else if(data instanceof Perspective && perspective.getClass()==data.getClass()) {
				data = perspective;
				break;
			} else if(data instanceof String && perspective.getExtension().getUniqueId().equals(data)) {
				data = perspective;
				break;
			}
		}
		
		return data;
	}
	
	/**
	 * Opens a {@code Perspective} specified by the {@code data} parameter.
	 * It may be of the following types:
	 * <ul>
	 * <li>String - interpreted as the globally unique id of an 
	 * {@link Extension} extending the {@code Perspective} extension-point
	 * of the icarus core plug-in</li>
	 * <li>Extension - as described above required to extend the {@code Perspective}</li>
	 * extension-point
	 * <li>Class - extending the {@link Perspective} class</li>
	 * <li>Perspective - actual instance</li>
	 * </ul>
	 * 
	 * Note that there is only one instance allowed per {@code Perspective} class
	 * and window! Attempting to display e.g. a new instance of an already active
	 * perspective will ignore that new instance and instead open the existing one!
	 */
	void openPerspective(Object data, boolean clearPerspective) throws Exception {
		Perspective currentPerspective = this.currentPerspective;
		if(clearPerspective) {
			currentPerspective = null;
		}
		
		data = getOpenedPerspective(data);
		
		// Abort if perspective is already open!
		if(data==currentPerspective) {
			return;
		}
		
		Extension perspectiveExtension = null;
		
		// Interpret a string as guid for some Perspective extension
		if(data instanceof String) {
			data = PluginUtil.findExtension(IcarusCorePlugin.PLUGIN_ID, 
					"Perspective", (String)data); //$NON-NLS-1$
		}
		if(data instanceof Extension) {
			Extension extension = (Extension) data;
			/*if(currentPerspective!=null && currentPerspective.getExtension()==extension) {
				// Already showing -> no need to load class
				return;
			}*/
			perspectiveExtension = extension;
			
			PluginUtil.activatePlugin(perspectiveExtension);
			
			Extension.Parameter param = extension.getParameter("class"); //$NON-NLS-1$
			if(param==null)
				throw new IllegalArgumentException("Provided extension does not declare a class"); //$NON-NLS-1$
			ClassLoader loader = PluginUtil.getClassLoader(extension);
			try {				
				data = loader.loadClass(param.valueAsString());
			} catch(ClassNotFoundException e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to load class for perspective extension: "+extension.getUniqueId(), e); //$NON-NLS-1$
				throw e;
			}
		}
		if(data instanceof Class<?>) {
			Class<?> clazz = (Class<?>) data;
			if(!Perspective.class.isAssignableFrom(clazz))
				throw new IllegalArgumentException(
						"Supplied perspective class is not compatible with "+Perspective.class.getName() //$NON-NLS-1$
						+" : "+data); //$NON-NLS-1$
			
			/*if(currentPerspective!=null && currentPerspective.getClass().equals(clazz)) {
				// Already showing -> no need to instantiate object
				return;
			}*/
			
			if(perspectiveExtension==null) {
				perspectiveExtension = findDeclaringExtension(clazz);
			}
			
			try {
				Perspective perspective = (Perspective) clazz.newInstance();
				data = perspective;
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to instantiate perspective class: "+clazz, e); //$NON-NLS-1$
				throw e;
			}
		}
		
		// Make sure we only accept Perspective objects
		if(!(data instanceof Perspective))
			throw new IllegalArgumentException("Provided object is not a perspective: "+data); //$NON-NLS-1$
		
		// Already showing
		/*if(currentPerspective!=null && data==currentPerspective) {
			return;
		}*/
		
		// Clear previous components
		// Ensures we have no fragments of failed perspectives 
		// remaining on the root panel
		rootPanel.setVisible(false);
		rootPanel.removeAll();
		
		// Data can only be of type Perspective now
		currentPerspective = (Perspective) data;
		currentPerspective.setFrameDelegate(getFrameDelegate());
		if(perspectiveExtension!=null) {
			currentPerspective.setExtension(perspectiveExtension);
		}
		
		String id = currentPerspective.getIdentity().getId();

		// Load container
		PerspectiveContainer container = getContainers().get(currentPerspective);
		
		// Show perspective
		try {
			if(container==null) {
				container = new PerspectiveContainer(currentPerspective);
				container.init();
				getContainers().put(currentPerspective, container);
			}
			rootPanel.setLayout(new BorderLayout());
			rootPanel.add(container, BorderLayout.CENTER);
			rootPanel.add(getFooterPanel(), BorderLayout.SOUTH);
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to init perspective: "+id, e); //$NON-NLS-1$
			throw e;
		} finally {
			rootPanel.setVisible(true);
		}
		
		this.currentPerspective = currentPerspective;
		
		// Refresh menu bar
		refreshMenuBar();
		
		// Refresh tool-bar
		refreshToolBar();
		
		refreshTitle();
		
		LoggerFactory.log(this, Level.FINE, "Opened perspective: "+id); //$NON-NLS-1$
		
		getEventSource().fireEvent(new EventObject(IcarusFrameEvents.PERSPECTIVE_OPENED, 
				"perspective", currentPerspective)); //$NON-NLS-1$
	}
	
	void refreshMenuBar() {
		
		Perspective perspective = currentPerspective;
		
		// Refresh menu bar
		menuDelegate.clear();
		menuDelegate.setPerspectives(createPerspectiveMenuItems());
		if(perspective!=null) {
			perspective.buildMenuBar(menuDelegate);
		}
		setJMenuBar(menuDelegate.createMenuBar());
	}
	
	void refreshToolBar() {
		
		Perspective perspective = currentPerspective;
		
		// Refresh tool-bar
		toolBarDelegate.clear();
		if(perspective!=null) {
			perspective.buildToolBar(toolBarDelegate);
		}
		
		if(perspectivePanel==null) {
			perspectivePanel = new PerspectivePanel();
		}
		perspectivePanel.feedToolBar(toolBarDelegate);
		
		rootPanel.add(toolBarDelegate.getToolbar(), BorderLayout.NORTH);
	}
	
	void closePerspective(Perspective perspective) {
		if(perspective==null)
			throw new IllegalArgumentException("Invalid perspective to close"); //$NON-NLS-1$
		
		Perspective currentPerspective = this.currentPerspective;

		String id = perspective.getIdentity().getId();
		try {
			perspective.close();
			
			if(containers!=null) {
				containers.remove(perspective);
			}
		} catch(CorruptedStateException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Perspective entered corrputed state: "+id, e); //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showDetailedError(this, 
					"plugins.core.icarusFrame.dialogs.errorTitle",  //$NON-NLS-1$
					"plugins.core.icarusFrame.dialogs.corruptedState", e, id); //$NON-NLS-1$
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Perspective failed to close properly: "+id, e); //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showDetailedError(this, 
					"plugins.core.icarusFrame.dialogs.errorTitle",  //$NON-NLS-1$
					"plugins.core.icarusFrame.dialogs.closingFailed", e, id); //$NON-NLS-1$
		} catch(Error e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Error while attempting to close perspective: "+id, e); //$NON-NLS-1$
			String operation = "Perspective.close() on "+id; //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showDetailedError(this, 
					"plugins.core.icarusFrame.dialogs.errorTitle",  //$NON-NLS-1$
					"plugins.core.icarusFrame.dialogs.seriousError", e, operation); //$NON-NLS-1$
		}
		
		if(perspective==currentPerspective) {
			this.currentPerspective = null;
		}
		
		getEventSource().fireEvent(new EventObject(IcarusFrameEvents.PERSPECTIVE_CLOSED, 
				"perspective", perspective)); //$NON-NLS-1$
	}
	
	void refreshContent() throws Exception {
		Perspective perspective = null;
		if(containers!=null && !containers.isEmpty()) {
			perspective = containers.keySet().iterator().next();
		}

		
		if(perspective!=null){
			openPerspective(perspective, false);
		} else {
			rootPanel.removeAll();
			PerspectiveChooser chooser = new PerspectiveChooser(getHandler());
			chooser.init(rootPanel);
		}
		
		getContentPane().revalidate();
		getContentPane().repaint();
	}
	
	private Map<Perspective, PerspectiveContainer> getContainers() {
		if(containers==null) {
			containers = new LinkedHashMap<>();
		}
		return containers;
	}
	
	void mapContainer(Perspective perspective, PerspectiveContainer container) {
		getContainers().put(perspective, container);
	}
	
	JComponent getContainer(Perspective perspective) {
		return containers==null ? null : containers.get(perspective);
	}
	
	void close() {
		if(containers==null || containers.isEmpty()) {
			return;
		}

		try {
			for(Perspective perspective : containers.keySet()) {
				String id = perspective.getIdentity().getId();
				try {
					perspective.close();
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Perspective failed to close properly: "+id, e); //$NON-NLS-1$
				}
			}
		} finally {
			containers.clear();
			currentPerspective = null;
		}
	}
	
	boolean isClosable() {
		if(containers==null || containers.isEmpty()) {
			return true;
		}
		
		for(Perspective perspective : containers.keySet()) {
			if(!perspective.isClosable()) {
				return false;
			}
		}
		
		return true;
	}
	
	private void refreshTitle() {
		String title = "ICARUS"; //$NON-NLS-1$
		Perspective perspective = currentPerspective;
		if(perspective!=null) {
			title += " - "+perspective.getIdentity().getName(); //$NON-NLS-1$
		}
		
		setTitle(title);
	}
	
	private Extension findDeclaringExtension(Class<?> clazz) {
		String className = clazz.getName();
		for(Extension extension : PluginUtil.getPluginRegistry()
				.getPluginDescriptor(IcarusCorePlugin.PLUGIN_ID)
				.getExtensionPoint("Perspective").getConnectedExtensions()) { //$NON-NLS-1$
			Extension.Parameter param = extension.getParameter("class"); //$NON-NLS-1$
			if(param!=null && param.valueAsString().equals(className)) {
				return extension;
			}
		}
		return null;
	}
	
	private Object[] createPerspectiveMenuItems() {
		List<Extension> extensions = new ArrayList<>(PluginUtil.getPluginRegistry()
				.getPluginDescriptor(IcarusCorePlugin.PLUGIN_ID)
				.getExtensionPoint("Perspective").getConnectedExtensions()); //$NON-NLS-1$
		
		// TODO sort extensions?
		
		List<Object> menuItems = new ArrayList<>(extensions.size());
		
		Perspective currentPerspective = this.currentPerspective;
		
		for(int i=0, len=extensions.size(); i<len; i++) {
			Extension extension = extensions.get(i);
			
			if(currentPerspective!=null && currentPerspective.getExtension()==extension) {
				continue;
			}
			
			Identity identity = PluginUtil.getIdentity(extension);
			
			JMenuItem item = new JMenuItem(identity.getName(), identity.getIcon());
			item.setActionCommand(extension.getUniqueId());
			item.addActionListener(getCallbackHandler());
			
			menuItems.add(item);
		}
		
		return menuItems.toArray();
	}
	

	private void registerActionCallbacks() {
		CallbackHandler callbackHandler = getCallbackHandler();
		
		ActionManager actionManager = getActionManager();
		
		actionManager.addHandler("plugins.core.icarusFrame.closeFrameAction",  //$NON-NLS-1$
				callbackHandler, "closeFrame"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.core.icarusFrame.newFrameAction",  //$NON-NLS-1$
				callbackHandler, "newFrame"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.core.icarusFrame.copyFrameAction",  //$NON-NLS-1$
				callbackHandler, "copyFrame"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.core.icarusFrame.closePerspectiveAction",  //$NON-NLS-1$
				callbackHandler, "closePerspective"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.core.icarusFrame.closeAllPerspectivesAction",  //$NON-NLS-1$
				callbackHandler, "closeAllPerspectives"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.core.icarusFrame.resetPerspectiveAction",  //$NON-NLS-1$
				callbackHandler, "resetPerspective"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.core.icarusFrame.openPerspectiveDialogAction",  //$NON-NLS-1$
				callbackHandler, "openPerspectiveDialog"); //$NON-NLS-1$
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	class Handler implements WindowListener, ChangeListener {

		/**
		 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowOpened(WindowEvent e) {
			// no-op
		}

		/**
		 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowClosing(WindowEvent e) {
			getEventSource().fireEvent(new EventObject(
					IcarusFrameEvents.CLOSING, "event", e)); //$NON-NLS-1$
		}

		/**
		 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowClosed(WindowEvent e) {
			getEventSource().fireEvent(new EventObject(
					IcarusFrameEvents.CLOSED, "event", e)); //$NON-NLS-1$
		}

		/**
		 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowIconified(WindowEvent e) {
			getEventSource().fireEvent(new EventObject(
					IcarusFrameEvents.MINIMIZED, "event", e)); //$NON-NLS-1$
		}

		/**
		 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowDeiconified(WindowEvent e) {
			getEventSource().fireEvent(new EventObject(
					IcarusFrameEvents.RESTORED, "event", e)); //$NON-NLS-1$
		}

		/**
		 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowActivated(WindowEvent e) {
			getEventSource().fireEvent(new EventObject(
					IcarusFrameEvents.ACTIVATED, "event", e)); //$NON-NLS-1$
		}

		/**
		 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowDeactivated(WindowEvent e) {
			getEventSource().fireEvent(new EventObject(
					IcarusFrameEvents.DEACTIVATED, "event", e)); //$NON-NLS-1$
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			
			if(e.getSource() instanceof PerspectiveChooser) {
				PerspectiveChooser chooser = (PerspectiveChooser)e.getSource();
				Extension perspective = chooser.getSelectedPerspective();
				if(perspective==null) {
					return;
				}
				try {
					openPerspective(perspective, false);
				} catch (Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to open perspective: "+perspective.getUniqueId(), ex); //$NON-NLS-1$
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	class IcarusFrameDelegate extends FrameDelegate {

		/**
		 * @see net.ikarus_systems.icarus.plugins.core.FrameDelegate#getActionManager()
		 */
		@Override
		public ActionManager getActionManager() {
			return IcarusFrame.this.getActionManager();
		}

		/**
		 * @see net.ikarus_systems.icarus.plugins.core.FrameDelegate#addListener(java.lang.String, net.ikarus_systems.icarus.ui.events.EventListener)
		 */
		@Override
		public void addListener(String eventName, EventListener listener) {
			getEventSource().addListener(eventName, listener);
		}

		/**
		 * @see net.ikarus_systems.icarus.plugins.core.FrameDelegate#removeListener(net.ikarus_systems.icarus.ui.events.EventListener)
		 */
		@Override
		public void removeListener(EventListener listener) {
			getEventSource().removeListener(listener);
		}

		/**
		 * @see net.ikarus_systems.icarus.plugins.core.FrameDelegate#removeListener(net.ikarus_systems.icarus.ui.events.EventListener, java.lang.String)
		 */
		@Override
		public void removeListener(EventListener listener, String eventName) {
			getEventSource().removeListener(listener, eventName);
		}

		/**
		 * @see net.ikarus_systems.icarus.plugins.core.FrameDelegate#getFrame()
		 */
		@Override
		IcarusFrame getFrame() {
			return IcarusFrame.this;
		}
		
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public class PerspectiveContainer extends JPanel {

		private static final long serialVersionUID = -2841133385131475740L;
		
		private final Perspective perspective;
		
		private PerspectiveContainer(Perspective perspective) {
			if(perspective==null)
				throw new IllegalArgumentException("Invalid perspective"); //$NON-NLS-1$
			
			this.perspective = perspective;
		}
		
		void init() {
			perspective.init(this);
		}
		
		public Perspective getPerspective() {
			return perspective;
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public final class CallbackHandler implements ActionListener {
		
		private CallbackHandler() {
			// no-op
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			// Called when the user selects a perspective to open
			String extensionId = e.getActionCommand();
			try {
				openPerspective(extensionId, false);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to open perspective: "+extensionId, ex); //$NON-NLS-1$
			}
		}
		
		public void closeFrame(ActionEvent e) {
			try {
				FrameManager.getInstance().closeFrame(IcarusFrame.this);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to close frame", ex); //$NON-NLS-1$
			}
		}
		
		public void newFrame(ActionEvent e) {
			try {
				FrameManager.getInstance().newFrame();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to create new frame", ex); //$NON-NLS-1$
			}
		}
		
		public void copyFrame(ActionEvent e) {
			try {
				Options options = null;
				Perspective currentPerspective = IcarusFrame.this.currentPerspective;
				if(currentPerspective!=null) {
					options = new Options(
							"perspective", currentPerspective.getClass()); //$NON-NLS-1$
				}
				
				FrameManager.getInstance().newFrame(options);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to copy frame: "+currentPerspective, ex); //$NON-NLS-1$
			}
		}
		
		public void closePerspective(ActionEvent e) {
			try {
				Perspective currentPerspective = IcarusFrame.this.currentPerspective;
				if(currentPerspective!=null && currentPerspective.isClosable()) {
					IcarusFrame.this.closePerspective(currentPerspective);
					refreshContent();
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to close perspective: "+currentPerspective, ex); //$NON-NLS-1$
			}
		}
		
		public void closeAllPerspectives(ActionEvent e) {
			try {
				if(currentPerspective==null) {
					return;
				}
				if(containers==null || containers.isEmpty()) {
					return;
				}
				
				List<Perspective> perspectives = new ArrayList<>(containers.keySet());
				for(Perspective perspective : perspectives) {
					if(perspective.isClosable()) {
						IcarusFrame.this.closePerspective(perspective);
					}
				}
				
				refreshContent();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to copy frame: "+currentPerspective, ex); //$NON-NLS-1$
			}
		}
		
		public void resetPerspective(ActionEvent e) {
			Perspective perspective = currentPerspective;
			if(perspective!=null) {
				String id = perspective.getIdentity().getId();
				try {
					perspective.reset();
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to reset perspective: "+id, ex); //$NON-NLS-1$
				}
			}
		}
		
		public void openPerspectiveDialog(ActionEvent e) {
			ExtensionPoint extensionPoint = PluginUtil.getCorePlugin().getExtensionPoint("Perspective"); //$NON-NLS-1$
			// TODO Sort shown extensions in dialog?
			boolean doSort = false;
			Object perspective = PluginUtil.showExtensionDialog(
					IcarusFrame.this, 
					"plugins.core.icarusFrame.dialogs.openPerspective",  //$NON-NLS-1$
					extensionPoint, true, doSort);
			
			if(perspective==null) {
				return;
			}
			
			try {
				openPerspective(perspective, false);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to open perspective: "+perspective, ex); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private class PerspectivePanel implements EventListener, ActionListener {
		
		private JComboBox<Object> comboBox;
		
		PerspectivePanel() {
			IcarusFrame.this.getEventSource().addListener(IcarusFrameEvents.PERSPECTIVE_OPENED, this);
			IcarusFrame.this.getEventSource().addListener(IcarusFrameEvents.PERSPECTIVE_CLOSED, this);
		}
		
		void feedToolBar(ToolBarDelegate toolBarDelegate) {
			toolBarDelegate.add(Box.createGlue());
			if(!toolBarDelegate.isEmpty()) {
				toolBarDelegate.addSeparator();
			}
			
			if(comboBox==null) {
				init();
			}
			
			toolBarDelegate.add(comboBox);
			//toolBarDelegate.addSeparator();
			toolBarDelegate.addAction(getActionManager(), 
					"plugins.core.icarusFrame.openPerspectiveDialogAction"); //$NON-NLS-1$
		}
		
		private void init() {
			comboBox = new JComboBox<>();
			comboBox.setEditable(false);
			comboBox.setRenderer(new PerspectiveListCellRenderer());
			comboBox.addActionListener(this);

			Dimension size = new Dimension(200, 25);
			comboBox.setPreferredSize(size);
			comboBox.setMinimumSize(size);
			comboBox.setMaximumSize(size);
			
			for(Perspective perspective : getContainers().keySet()) {
				comboBox.addItem(perspective);
			}
			comboBox.setSelectedItem(currentPerspective);
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			if(comboBox==null) {
				return;
			}
			
			Perspective perspective = (Perspective) event.getProperty("perspective"); //$NON-NLS-1$
						
			if(IcarusFrameEvents.PERSPECTIVE_OPENED.equals(event.getName())) {
				// Only add opened perspective if it is not already present
				boolean doAdd = true;
				for(int i=comboBox.getItemCount()-1; i>-1; i--) {
					if(comboBox.getItemAt(i).equals(perspective)) {
						doAdd = false;
						break;
					}
				}
				
				if(doAdd) {
					comboBox.addItem(perspective);
				}
				
				comboBox.setSelectedItem(perspective);
			} else {
				comboBox.removeItem(perspective);
				comboBox.setSelectedItem(currentPerspective);
			}
			
			comboBox.setVisible(comboBox.getItemCount()>1);
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			Object perspective = comboBox.getSelectedItem();

			if(perspective==null) {
				return;
			}
			
			try {
				openPerspective(perspective, false);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to open perspective: "+perspective, ex); //$NON-NLS-1$
			}
		}
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private static class PerspectiveListCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -250478882345416648L;

		/**
		 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
		 */
		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			
			Perspective perspective = null;
			if(value instanceof Perspective) {
				perspective = (Perspective) value;
				value = perspective.getIdentity().getName();
			}
			
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			
			if(perspective!=null) {
				setIcon(perspective.getIdentity().getIcon());
			}
			
			return this;
		}
		
	}
}
