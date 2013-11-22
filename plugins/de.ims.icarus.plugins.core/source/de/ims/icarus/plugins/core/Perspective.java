/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginRegistry;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.resources.Localizer;
import de.ims.icarus.ui.Alignment;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.EventSource;
import de.ims.icarus.ui.layout.AreaLayout;
import de.ims.icarus.ui.layout.DefaultAreaLayout;
import de.ims.icarus.util.Capability;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.Priority;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.id.ExtensionIdentity;
import de.ims.icarus.util.id.Identifiable;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.MultiResultMessage;
import de.ims.icarus.util.mpi.ResultMessage;
import de.ims.icarus.util.mpi.ResultMessage.ResultType;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public abstract class Perspective implements Identifiable {
	
	public static final String CLIENT_PROPERTY_ALIGNMENT = "view:alignment"; //$NON-NLS-1$
	
	ActionManager actionManager;
	FrameDelegate frameDelegate;
	
	protected final Set<Extension> connectedViews = new HashSet<>();
	
	protected final Map<Extension, View> activatedViews = new HashMap<>();
	
	protected final Map<Extension, ViewContainer> containers = new HashMap<>();
	
	protected final Set<View> views = new HashSet<>();
	
	private View activeView = null;
	
	private Extension extension;
	
	private Identity identity;
	
	protected AreaLayout areaLayout;
	
	/**
	 * For security reasons we do not extend {@code EventSource} but rather
	 * store an internal instance and forward methods we want to expose.
	 * Since {@code EventSource} does not offer access control to methods 
	 * like {@link EventSource#setEventsEnabled(boolean)} this is necessary
	 * to prevent external sources from disabling event handling for us.
	 * Implementations might reconsider about that and expose more 
	 * functionality of the {@code EventSource} via new forwarding methods.
	 */
	protected final EventSource eventSource = new EventSource(this);
	
	private final EventSource broadcastEventSource = new EventSource(this);

	/**
	 * 
	 */
	protected Perspective() {
		// no-op
	}
	
	final void setExtension(Extension extension) {
		if(this.extension!=null && this.extension!=extension)
			throw new IllegalStateException("Extension already defined"); //$NON-NLS-1$
		
		this.extension = extension;
		identity = null;
	}
	
	public Identity getIdentity() {
		if(identity==null) {
			identity = new ExtensionIdentity(getExtension());
		}
		return identity;
	}
	
	public final Extension getExtension() {
		if(extension==null)
			throw new IllegalStateException("Extension not available yet"); //$NON-NLS-1$
		
		return extension;
	}
	
	@Override
	public String toString() {
		return getExtension().getId();
	}
	
	public final ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = getFrameDelegate().getActionManager();
			if(usePrivateActionNamespace()) {
				actionManager = actionManager.derive();
			}
		}
		
		return actionManager;
	}
	
	/**
	 * Hook for subclasses to disable private action-namespace.
	 * If this method returns {@code false} then the {@code ActionManager}
	 * provided by the present {@code FrameDelegate} will be used {@code as-is}
	 * otherwise a new manager will be instantiated by calling 
	 * {@link ActionManager#derive()} on the one from the delegate. This way
	 * all actions loaded by this {@code Perspective} or any of its containing
	 * {@code View}s using the perspective's {@code ActionManager} will be private.
	 * The default implementation returns {@code true}. 
	 */
	protected boolean usePrivateActionNamespace() {
		return true;
	}

	/**
	 * Called from the framework when a {@code Perspective} is made
	 * visible for the first time after if was created. The provided
	 * {@code container} is the {@code JComponent} that will serve as
	 * {@code root} for this {@code Perspective}. It is up to the
	 * implementation to add components or use the {@link #defaultDoLayout(JComponent)}
	 * method to layout all {@code View}s registered to this perspective
	 * so far.
	 * @param container the {@code root} component of this {@code Perspective}
	 */
	public abstract void init(JComponent container);
	
	public boolean isClosable() {
		for(View view : views) {
			if(!view.isClosable()) {
				return false;
			}
		}
		
		return true;
	}

    private static final String PERMANENT_FOCUS_OWNER_PROPERTY 
    		= "permanentFocusOwner";  //$NON-NLS-1$
    
	public void close() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.removePropertyChangeListener(PERMANENT_FOCUS_OWNER_PROPERTY, focusTracker);
		
		for(View view : getViews()) {
			try {
				view.close();
			} catch(Exception e) {
				Identity id = view.getIdentity();
				LoggerFactory.log(this, Level.SEVERE, "Error while closing view: "+id.getName()+" ("+id.getId()+")", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
	}
	
	/**
	 * Returns the {@code View} currently available within this
	 * {@code Perspective} that uses the given {@code id} or {@code null}
	 * if there is no matching {@code View} registered or it has not
	 * yet been activated.
	 * @param id the id to be matched against the ids of all active {@code View}s
	 * @return the active {@code View} within this {@code Perspective} that
	 * is assigned the given {@code id} or {@code null} if no such {@code View}
	 * could be found
	 */
	public final View getView(String id) {
		Exceptions.testNullArgument(id, "id"); //$NON-NLS-1$
		
		for(View view : views)
			if(id.equals(view.getIdentity().getId()))
				return view;
		
		return null;
	}
	
	/**
	 * Searches the list of registered {@code View}s within this
	 * {@code Perspective} for instances of the given {@code Class}
	 * and returns all such matches as a {@code Collection}. The
	 * check is performed by calling {@link Class#isAssignableFrom(Class)}.
	 * @param clazz the superinterface or superclass of the desired {@code View}
	 * instances
	 * @return all {@code View}s within this {@code Perspective} that either
	 * implement or extend the class or interface defined by the {@code clazz}
	 * parameter
	 */
	public final Collection<View> getViews(Class<?> clazz) {
		Collection<View> result = new ArrayList<>();

		for(View view : views)
			if(clazz.isAssignableFrom(view.getClass()))
				result.add(view);
			
		return result;
	}
	
	/**
	 * Returns all the active {@code View} instances within this
	 * {@code Perspective} as an unmodifiable {@code Collection}
	 * @return all the active {@code View} instances within this
	 * {@code Perspective} as an unmodifiable {@code Collection}
	 */
	public final Collection<View> getViews() {
		return Collections.unmodifiableCollection(views);
	}
	
	public final Collection<View> getViews(Filter filter) {
		return CollectionUtils.filter(views, filter);
	}
	
	/**
	 * Returns all the {@code Extension}s that represent {@code View}s
	 * and are currently connected to this {@code Perspective}. The
	 * returned {@code Collection} is unmodifiable. Note that an
	 * {@code Extension} being connected does {@code not} necessarily
	 * mean that the corresponding {@code View} is actually loaded or
	 * active!
	 * @return
	 */
	public final Collection<Extension> getConnectedViewExtensions() {
		return Collections.unmodifiableCollection(connectedViews);
	}
	
	/**
	 * Resets all the {@code View} objects currently associated
	 * with this {@code Perspective}. All catched exceptions are forwarded
	 * to the default UI-logger save for {@code CorruptedStateException}s
	 * which will be re-thrown once the logging is done and all views had
	 * their chance to properly reset.
	 * <p>
	 * If a subclass is overriding this method it is recommended that it
	 * calls the {@code super.reset()} after managing internal reset so that
	 * forwarding of  {@code CorruptedStateException}s is ensured.
	 */
	public void reset() {
		CorruptedStateException ex = null;
		
		for(View view : getViews()) {
			try {
				view.reset();
			} catch(Exception e) {
				if(e instanceof CorruptedStateException)
					ex = (CorruptedStateException)e;
				
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to reset view: "+view.getIdentity().getId(), e); //$NON-NLS-1$
			}
		}
		
		if(ex!=null)
			throw ex;
	}

	/**
	 * Registers the given {@code listener} for events of the
	 * specified {@code eventName} or as a listener for all
	 * events in the case the {@code eventName} parameter is {@code null}
	 * @param eventName name of events to listen for or {@code null} if
	 * the listener is meant to receive all fired events
	 * @param listener the {@code EventListener} to be registered
	 */
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * Removes the given {@code EventListener} from all events
	 * it was previously registered for.
	 * @param listener the {@code EventListener} to be removed
	 */
	public void removeListener(EventListener listener) {
		eventSource.removeEventListener(listener);
	}

	/**
	 * Removes from the list of registered listeners all pairs
	 * matching the given combination of {@code EventListener}
	 * and {@code eventName}. If {@code eventName} is {@code null}
	 * then all occurrences of the given {@code listener} will be
	 * removed.
	 * @param listener
	 * @param eventName
	 */
	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeEventListener(listener, eventName);
	}
	
	public final void openPerspective(Extension extension) throws Exception {
		getFrameDelegate().getFrame().openPerspective(extension, false);
	}
	
	protected Frame getFrame() {
		return getFrameDelegate().getFrame();
	}
	
	/**
	 * Collects all {@code Extension}s that should be used for this perspective.
	 * The default implementation simply adds all extensions defined for the
	 * {@code de.ims.icarus.core} plugin's {@code 'View'} extension-point.
	 * In addition all extensions for the {@code 'View'} extension-point in the
	 * declaring plugin will be added if present.
	 */
	protected void collectViewExtensions() {
		PluginRegistry registry = PluginUtil.getPluginRegistry();
		
		ExtensionPoint coreViewExtPoint = registry.getExtensionPoint("de.ims.icarus.core", "View"); //$NON-NLS-1$ //$NON-NLS-2$
		connectedViews.addAll(coreViewExtPoint.getConnectedExtensions());
		
		PluginDescriptor declaringPluginDesc = getExtension().getDeclaringPluginDescriptor();
		
		// nothing to do here if plugin does not define its own 'View' extension point
		if(declaringPluginDesc.getExtensionPoint("View")==null) //$NON-NLS-1$
			return;
		// fetch connected extensions
		ExtensionPoint declaringViewExtPoint = registry.getExtensionPoint(declaringPluginDesc.getId(), "View"); //$NON-NLS-1$
		connectedViews.addAll(declaringViewExtPoint.getConnectedExtensions());
		
		eventSource.fireEvent(new EventObject(PerspectiveEvents.VIEWS_ADDED, 
				"extensions", connectedViews.toArray())); //$NON-NLS-1$
	}
	
	protected void defaultDoLayout(JComponent root) {
		DefaultAreaLayout layout = null;
		if(this.areaLayout instanceof DefaultAreaLayout) {
			layout = (DefaultAreaLayout) this.areaLayout;
		}
		if(layout==null) {
			layout = new DefaultAreaLayout();
		}
		
		layout.init(root);
		
		for(Extension extension : connectedViews) {
			Alignment alignment = getViewAlignment(extension);
			ViewContainer container = new ViewContainer(extension, alignment);
			containers.put(extension, container);
			
			Extension.Parameter param = extension.getParameter("activateEarly");  //$NON-NLS-1$
			if(param!=null && param.valueAsBoolean()) {
				activateView(container);
			}
			
			layout.add(container, alignment);
		}
		
		layout.setComponentSorter(COMPONENT_SORTER);
		layout.setContainerWatcher(viewActivater);
		layout.setTabLocalizer(tabLocalizer);
		
		layout.doLayout();
		
		this.areaLayout = layout;

		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.addPropertyChangeListener(PERMANENT_FOCUS_OWNER_PROPERTY, focusTracker);
	}
	
	protected Alignment getViewAlignment(Extension extension) {
		return Alignment.parse(extension.getParameter("alignment").valueAsString()); //$NON-NLS-1$
	}
	
	public void toggleView(View view) {
		Extension extension = view.getExtension();
		ViewContainer container = containers.get(extension);
		if(container==null)
			throw new IllegalArgumentException("View not registered to this perspective: "+view); //$NON-NLS-1$
		
		if(areaLayout==null) {
			return;
		}
		
		areaLayout.toggle(container);
	}
	
	protected void focusView(final Object view) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				View v = null;
				
				if(view instanceof View) {
					v = (View)view;
				} else if(view instanceof String) {
					v = getView((String) view);
				} else if(view instanceof Extension) {
					v = activatedViews.get((Extension) view);
				}
				
				if(v!=null && views.contains(v)) {
					v.focusView();
				}
			}
		});
	}
	
	protected Localizer tabLocalizer = new Localizer() {
		
		@Override
		public void localize(Object item) {
			JTabbedPane tabbedPane = (JTabbedPane) item;
			
			for(int i=0; i<tabbedPane.getTabCount(); i++) {
				ViewContainer container = (ViewContainer) tabbedPane.getComponentAt(i);
				try {
					Identity identity = container.getIdentity();
					
					tabbedPane.setTitleAt(i, identity.getName());
					tabbedPane.setToolTipTextAt(i, identity.getDescription());
				} catch(Throwable t) {
					LoggerFactory.log(this, Level.SEVERE, "Failed to localize container: "+container, t); //$NON-NLS-1$
				}
			}
		}
	};
	
	protected ChangeListener viewActivater = new ChangeListener() {
		
		@Override
		public void stateChanged(ChangeEvent e) {
			ViewContainer container = null;
			if(e.getSource() instanceof ViewContainer) {
				container = (ViewContainer) e.getSource();
			} else if(e.getSource() instanceof JTabbedPane) {
				JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
				container = (ViewContainer) tabbedPane.getSelectedComponent();
			}
			if(container!=null) {
				activateView(container);
			}
			
			View view = container.getView();
			if(view!=null) {
				view.focusView();
			}
		}
	};
	
	protected InfoPanel getInfoPanel() {
		return getFrameDelegate().getFrame().getInfoPanel(this);
	}
	
	protected View getFocusedView() {
		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
		if(focusOwner==null) {
			return null;
		}
		
		ViewContainer container = getContainer(focusOwner);
		if(container==null) {
			return null;
		}
		
		return container.getView();
	}
	
	/**
	 * Checks whether the given {@code View} is the currently active
	 * view in this {@code Perspective}. 
	 */
	public boolean isActiveView(View view) {
		if(view==null)
			throw new NullPointerException();
		
		View focusedView = getFocusedView();
		return view==focusedView;
	}
	
	protected void refreshInfoPanelForView(View view) {
		InfoPanel infoPanel = getInfoPanel();
		if(infoPanel==null) {
			return;
		}
		
		infoPanel.clear();

		if(view==null) {
			return;
		}
		
		view.refreshInfoPanel(infoPanel);
	}
	
	public InfoPanel getInfoPanel(View view) {
		if(!isActiveView(view)) {
			return null;
		}
		return getInfoPanel();
	}
	
	public final View getActiveView() {
		return activeView;
	}
	
	protected final void setActiveView(View view) {
		if(view!=null && view==activeView) {
			return;
		}
		
		activeView = view;
		refreshInfoPanelForView(view);
	}
	
	/**
	 * Returns the ViewContainer for the given component. Returns null
	 * if the component is part of a component hierarchy that resides
	 * outside of this perspective's root container.
	 */
	protected final ViewContainer getContainer(Component comp) {
		if(comp==null) {
			return null;
		}
		if(comp instanceof ViewContainer) {
			return (ViewContainer) comp;
		}
		ViewContainer ancestor = (ViewContainer)SwingUtilities.getAncestorOfClass(ViewContainer.class, comp);
		
		// Expensive call to containsValue, but should be ok since we never have 
		// more than 10 to 20 views
		if(ancestor!=null && !containers.containsValue(ancestor)) {
			ancestor = null;
		}
		return ancestor;
	}
	
	protected PropertyChangeListener focusTracker = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			ViewContainer oldFocusOwner = getContainer((Component) evt.getOldValue());
			ViewContainer newFocusOwner = getContainer((Component) evt.getNewValue());
			
			if(oldFocusOwner==newFocusOwner) {
				return;
			}
			
			if(oldFocusOwner!=null) {
				oldFocusOwner.setFocused(false);
			}
			
			if(newFocusOwner!=null) {
				newFocusOwner.setFocused(true);
			}
			
			// Tell perspective to refresh info panel
			View view = newFocusOwner==null ? null : newFocusOwner.getView();			
			setActiveView(view);
		}
	};
	
	protected static final Comparator<JComponent> COMPONENT_SORTER = new Comparator<JComponent>() {

		@Override
		public int compare(JComponent c1, JComponent c2) {
			ViewContainer vc1 = (ViewContainer) c1;
			ViewContainer vc2 = (ViewContainer) c2;
			int result = -vc1.getPriority().compareTo(vc2.getPriority());
			if(result==0) {
				result = -vc1.getIdentity().getId().compareTo(vc2.getIdentity().getId());
			}
			return result;
		}
		
	};
	
	protected final void closeView(View view, boolean forceClose, boolean removeContainer) throws Exception {
		Extension extension = view.getExtension();
		JComponent container = containers.get(extension);
		String viewId = view.getIdentity().getId();
		
		if(container==null)
			throw new IllegalArgumentException("View already closed and not showing: "+view); //$NON-NLS-1$
		
		// If we are not forced to close the view
		// let it decide for itself
		if(!forceClose && !view.isClosable()) {
			return;
		}
		
		eventSource.fireEvent(new EventObject(PerspectiveEvents.CLOSE_VIEW, "id", viewId)); //$NON-NLS-1$
		
		// Save-point for exception thrown by the call
		// to view.close() to allow for cleanup of
		// the view's root container component.
		// Only exceptions are handled this way, errors
		// will just pass uncatched.
		Exception thrown = null;
		
		// Close the view object
		try {
			view.close();
		} catch(Exception e) {
			thrown = e;
		}
		
		if(removeContainer) {
			// TODO remove the view's container and rearrange components if necessary
		}
		
		eventSource.fireEvent(new EventObject(PerspectiveEvents.VIEW_CLOSED, "id", viewId)); //$NON-NLS-1$
		
		// If closing the view yielded an error re-throw it
		if(thrown!=null)
			throw thrown;
	}
	
	void reloadViewTab(View view) {
		JComponent container = getViewContainer(view);
		Component parent = container.getParent();
		
		if(parent instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) parent;
			int index = tabbedPane.indexOfComponent(container);
			Identity identity = view.getIdentity();

			Icon currentIcon = tabbedPane.getIconAt(index);
			Icon icon = identity.getIcon();
			if(icon==null)
				icon = currentIcon;

			// To support compound icons we have to first set the icon
			// to null and then re-set it so the tabbed pane does not
			// dicard our call due to old and new icon being the same
			// object
			tabbedPane.setIconAt(index, null);
			tabbedPane.setIconAt(index, icon);
			tabbedPane.setTitleAt(index, identity.getName());
			tabbedPane.setToolTipTextAt(index, identity.getDescription());
			
			Component tabComponent = tabbedPane.getTabComponentAt(index);
			if(tabComponent!=null) {
				tabComponent.repaint();
			}
		}
	}
	
	void selectViewTab(View view) {
		JComponent container = getViewContainer(view);
		Component parent = container.getParent();
		
		if(parent instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) parent;
			tabbedPane.setSelectedComponent(container);
		}
	}
	
	JComponent getViewContainer(View view) {
		Extension extension = view.getExtension();
		return containers.get(extension);
	}
	
	protected final JComponent getContainer() {
		return getFrameDelegate().getFrame().getContainer(this);
	}
	
	void addBroadcastListener(String eventName, EventListener listener) {
		broadcastEventSource.addListener(eventName, listener);
	}
	
	void removeBroadcastListener(EventListener listener) {
		broadcastEventSource.removeEventListener(listener);
	}
	
	void removeBroadcastListener(EventListener listener, String eventName) {
		broadcastEventSource.removeEventListener(listener, eventName);
	}
	
	void fireBroadcastEvent(View source, EventObject event) {
		broadcastEventSource.fireEvent(event, source);
	}
	
	void setFrameDelegate(FrameDelegate frameDelegate) {
		if(this.frameDelegate!=null && this.frameDelegate!=frameDelegate)
			throw new IllegalStateException("Already assigned to a frame!"); //$NON-NLS-1$
		
		this.frameDelegate = frameDelegate;
	}
	
	protected final FrameDelegate getFrameDelegate() {
		if(frameDelegate==null)
			throw new IllegalArgumentException("No frame delegate available yet"); //$NON-NLS-1$
		
		return frameDelegate;
	}
	
	protected void buildMenuBar(MenuDelegate delegate) {
		// no-op
	}
	
	protected void buildToolBar(ToolBarDelegate delegate) {
		List<View> views = new ArrayList<>(activatedViews.values());
		if(views.isEmpty()) {
			return;
		}
		
		Collections.sort(views, Identifiable.COMPARATOR);
		
		for(View view : views) {
			try {
				view.buildToolBar(delegate);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to build tool-bar elements for view: "+view.getIdentity().getId(), e); //$NON-NLS-1$
			}
		}
	}
	
	protected final void activateView(ViewContainer container) {
		Extension extension = container.getViewExtension(); 
		View view = container.getView(); 
		
		if(view==null && !container.isInvalid()) {
			try {
				eventSource.fireEvent(new EventObject(PerspectiveEvents.ACTIVATE_VIEW, "extension", extension)); //$NON-NLS-1$
				
				PluginManager pluginManager = PluginUtil.getPluginManager();
	            // Activate plug-in that declares extension
				pluginManager.activatePlugin(extension.getDeclaringPluginDescriptor().getId());
	            // Get plug-in class loader
	            @SuppressWarnings("resource")
				ClassLoader classLoader = pluginManager.getPluginClassLoader(
	                    extension.getDeclaringPluginDescriptor());
	            // Load View class
	            Class<?> viewCls = classLoader.loadClass(extension.getParameter("class").valueAsString()); //$NON-NLS-1$
	            // Create View instance
	            view = (View) viewCls.newInstance();
	            view.setExtension(extension);
	            // Notify view about new enclosing perspective
	            view.addNotify(this);
	            // Initialize class instance according to interface specification
	            initView(view, container);
	            views.add(view);
	            activatedViews.put(extension, view);
	            
	            reloadViewTab(view);
	            
	            LoggerFactory.log(this, Level.FINE, 
	        			"Activated view: "+extension.getId()); //$NON-NLS-1$
	            
	            eventSource.fireEvent(new EventObject(PerspectiveEvents.VIEW_ACTIVATED, "view", view)); //$NON-NLS-1$
	        } catch (Throwable t) {
	        	// Present the user some feedback both at logging level
	        	// and on the container itself
	        	LoggerFactory.log(this, Level.SEVERE, 
	        			"Failed to activate view: "+extension.getId(), t); //$NON-NLS-1$
	        	UIDummies.createDefaultErrorOutput(container, t);
	        	container.setToolTipText("View: "+extension.getId()); //$NON-NLS-1$
	        	container.setInvalid();
	            return;
	        }
			
	        container.setView(view); 
		}
	}
	
	/**
	 * Called after a {@code View} instance has been activated and before
	 * notifying listeners to allow subclasses to perform implementation
	 * specific configuration of views. Note that it is mandatory to call
	 * {@link View#init(JComponent)} during this initialization process!
	 * Subclasses should either make that call themselves or simply call
	 * {@code super.initView(View, ViewContainer)} since the default
	 * implementation only calls the {@link View#init(JComponent)} method
	 * without additional configuration.
	 */
	protected void initView(View view, ViewContainer container) {
		view.init(container);
	}
	
	protected final ResultMessage sendRequest(Object sender, Object perspective, Object receiver, Message message) {
		Perspective target = getFrameDelegate().getFrame().ensurePerspective(perspective);
		
		if(target==null) {
			return message.unknownReceiver(this);
		}
		
		try {
			return target.handleRequest(sender, receiver, message);
		} catch (Exception e) {
			return message.errorResult(this, e);
		}
	}

	protected ResultMessage handleRequest(Object sender, Object receiver, Message message) throws Exception {
		return sendRequest(sender, receiver, message);
	}
	
	final ResultMessage sendRequest(Object sender, Object receiver, Message message) {
		if(message==null)
			throw new NullPointerException("Invalid message"); //$NON-NLS-1$
		
		ResultMessage result = null;
		
		if(SwingUtilities.isEventDispatchThread()) {
			result = dispatchRequest(sender, receiver, message);
		} else {
			RequestDispatcher dispatcher = new RequestDispatcher(sender, receiver, message);
			try {
				SwingUtilities.invokeAndWait(dispatcher);
				result = dispatcher.getResult();
			} catch (InvocationTargetException | InterruptedException e) {
				result = new ResultMessage(this, message, e);
			}
		}
		
		return result;
	}
	
	private ResultMessage dispatchRequest(Object sender, Object receiver, Message message) {		
		Collection<View> receivers = new LinkedList<>();
		
		ViewFilter filter = ViewFilter.emptyFilter;
		
		// Translate receiver argument into a ViewFilter
		if(receiver instanceof ViewFilter) {
			filter = (ViewFilter) receiver;
		} else if(receiver instanceof String) {
			filter = new ViewFilter.ViewIdFilter((String)receiver);
		} else if(receiver instanceof Capability) {
			filter = new ViewFilter.ViewCapabilityFilter((Capability)receiver);
		} else if(receiver instanceof Capability[]) {
			filter = new ViewFilter.ViewCapabilityFilter((Capability[])receiver);
		} else if(receiver instanceof Class) {
			filter = new ViewFilter.ViewClassFilter((Class<?>) receiver);
		} else if(receiver != null) {
			throw new NullPointerException("Invalid receiver: "+receiver); //$NON-NLS-1$
		}
		
		for(Extension extension : connectedViews) {
			View view = activatedViews.get(extension);
			
			if(view==sender) {
				continue;
			}
			
			if(filter.filter(extension, view)) {
				// If view is to be included but not yet activated
				// perform activation now
				if(view==null) {
					activateView(containers.get(extension));
					view = activatedViews.get(extension);
				}
				
				// Include only loaded and valid view instances
				if(view!=null) {
					receivers.add(view);
				}
			}
		}
		
		// Tell the sender if we couldn't find suitable receivers
		if(receivers.isEmpty()) {
			return new ResultMessage(this, ResultType.UNKNOWN_RECEIVER, message, null, null);
		}
		
		List<ResultMessage> results = new ArrayList<>();
		for(View view : receivers) {
			try {
				results.add(view.handleRequest(message));
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to dispatch message to view: "+view.getIdentity().getId(), e); //$NON-NLS-1$
				results.add(new ResultMessage(this, message, e));
			}
		}
		
		// Depending on the number of results either return the one
		// existing result or wrap them all in a multi-result
		if(results.size()==1) {
			return results.get(0);
		} else {
			
			// Find the 'best' result type among all results
			ResultType dominatingType = ResultType.REQUEST_FAILED;
			for(ResultMessage result : results) {
				if(result.getType().compareTo(dominatingType)<0) {
					dominatingType = result.getType();
				}
				
				// Best possible result type encountered 
				// -> no need to search further
				if(dominatingType==ResultType.REQUEST_SUCCESSFUL) {
					break;
				}
			}
			
			return new MultiResultMessage(this, dominatingType, message, results);
		}
	}
	
	// Eclipse view highlight color: r=160, g=191, b=244
	private static Color defaultHighlightColor = new Color(160, 191, 244);

	
	/**
	 * Searches the component hierarchy of the given
	 * {@code JComponent} for one that serves as container
	 * for a {@code View} object and returns that view.
	 */
	public static View findView(Component comp) {
		Exceptions.testNullArgument(comp, "comp"); //$NON-NLS-1$
		
		while(!(comp instanceof ViewContainer)) {
			Component parent = comp.getParent();
			if(parent instanceof JComponent)
				comp = (JComponent) parent;
			else
				return null;
		}
		
		return (comp instanceof ViewContainer) ? ((ViewContainer)comp).getView() : null;
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class ViewContainer extends JPanel implements Identifiable, Border {

		private static final long serialVersionUID = -2139304858671288495L;
		
		private final Extension viewExtension;
		private Alignment alignment;
		private View view;
		private boolean invalid = false;
		private Identity identity;
		private boolean focused = false;

		public ViewContainer(Extension viewExtension, Alignment alignment) {
			if(viewExtension==null)
				throw new NullPointerException("Invalid view extension"); //$NON-NLS-1$
			
			this.viewExtension = viewExtension;
			this.alignment = alignment;
			
			setBorder(this);
			
			init();
		}
		
		public ViewContainer(Extension viewExtension) {
			this(viewExtension, null);
		}
		
		protected void init() {
			Extension.Parameter param = viewExtension.getParameter("requiresTab"); //$NON-NLS-1$
			if(param!=null) {
				putClientProperty(DefaultAreaLayout.REQUIRES_TAB_PROPERTY, 
						param.valueAsBoolean());
			}
		}
		
		public Priority getPriority() {
			Extension.Parameter param = viewExtension.getParameter("priority");  //$NON-NLS-1$
			return param==null ? Priority.STANDARD : Priority.parse(param.valueAsString());
		}

		/**
		 * @return the viewExtension
		 */
		public Extension getViewExtension() {
			return viewExtension;
		}

		/**
		 * @return the alignment
		 */
		public Alignment getAlignment() {
			return alignment;
		}

		/**
		 * @return the view
		 */
		public View getView() {
			return view;
		}

		/**
		 * @param alignment the alignment to set
		 */
		public void setAlignment(Alignment alignment) {
			if(this.alignment!=null && this.alignment!=alignment)
				throw new IllegalArgumentException("Alignment already defined"); //$NON-NLS-1$
			
			this.alignment = alignment;
		}

		/**
		 * @param view the view to set
		 */
		public void setView(View view) {
			if(this.view!=null && this.view!=view)
				throw new IllegalArgumentException("View already defined"); //$NON-NLS-1$
			
			this.view = view;
		}

		/**
		 * @return the invalid
		 */
		public boolean isInvalid() {
			return invalid;
		}

		public void setInvalid() {
			this.invalid = true;
		}

		/**
		 * @see de.ims.icarus.util.id.Identifiable#getIdentity()
		 */
		@Override
		public Identity getIdentity() {
			if(view!=null) {
				identity = null;
				return view.getIdentity();
			}
			if(identity==null) {
				identity = PluginUtil.getIdentity(viewExtension);
			}
			return identity;
		}

		/**
		 * @return the focused
		 */
		public boolean isFocused() {
			return focused;
		}

		/**
		 * @param focused the focused to set
		 */
		public void setFocused(boolean focused) {
			if(focused!=this.focused) {
				this.focused = focused;
				repaint();
			}
		}
		
		/**
		 * @see java.awt.Component#getName()
		 */
		@Override
		public String getName() {
			return viewExtension==null ? super.getName() : viewExtension.getId();
		}
		
		/**
		 * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
		 */
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y,
				int w, int h) {
			
			Color col = isFocused() ? UIManager.getColor("TabbedPane.focus") : getBackground(); //$NON-NLS-1$
			if(Color.black.equals(col)) {
				col = defaultHighlightColor;
			}
			
			g.setColor(col);
			
			int b = y+h-1;
			int r = x+w-1;
			
			// top
			g.drawLine(x, y, r, y);
			g.drawLine(x, y+1, r, y+1);
			// left
			g.drawLine(x, y, x, b);
			g.drawLine(x+1, y, x+1, b);
			// right
			g.drawLine(r, y, r, b);
			g.drawLine(r-1, y, r-1, b);
			// bottom
			g.drawLine(x, b, r, b);
			g.drawLine(x, b-1, r, b-1);
			
			// Only separate from header area if we are inside of a tabbed pane
			if(!isFocused() && getParent() instanceof JTabbedPane) {
				g.setColor(Color.black);
				g.drawLine(x+1, y, r-1, y);
			}
		}

		/**
		 * @see javax.swing.border.Border#getBorderInsets(java.awt.Component)
		 */
		@Override
		public Insets getBorderInsets(Component c) {
			return new Insets(2, 2, 2, 2);
		}

		/**
		 * @see javax.swing.border.Border#isBorderOpaque()
		 */
		@Override
		public boolean isBorderOpaque() {
			return true;
		}
		
		@Override
		public String toString() {
			return "ViewContainer: "+getName(); //$NON-NLS-1$
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private class RequestDispatcher implements Runnable {
		
		private ResultMessage result;

		private final Object receiver;
		private final Object sender;
		private final Message message;

		public RequestDispatcher(Object sender, Object receiver, Message message) {
			this.sender = sender;
			this.receiver = receiver;
			this.message = message;
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				result = dispatchRequest(sender, receiver, message);
			} catch(Exception e) {
				result = new ResultMessage(this, message, e);
			}
		}
		
		ResultMessage getResult() {
			return result;
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface PerspectiveEvents {

		public static final String VIEWS_ADDED = "viewsAdded"; //$NON-NLS-1$
		public static final String ACTIVATE_VIEW = "activateView"; //$NON-NLS-1$
		public static final String VIEW_ACTIVATED = "viewActivated"; //$NON-NLS-1$
		public static final String CLOSE_VIEW = "closeView"; //$NON-NLS-1$
		public static final String VIEW_CLOSED = "viewClosed"; //$NON-NLS-1$
		public static final String CLOSING = "closing"; //$NON-NLS-1$
		public static final String CLOSED = "closed"; //$NON-NLS-1$
		public static final String INITIALIZED = "initialized"; //$NON-NLS-1$

	}
}
