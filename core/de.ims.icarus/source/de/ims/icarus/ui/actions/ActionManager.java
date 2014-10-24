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
package de.ims.icarus.ui.actions;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.WeakHandler;
import de.ims.icarus.ui.actions.ActionList.EntryType;
import de.ims.icarus.ui.helper.ModifiedFlowLayout;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.id.DuplicateIdentifierException;
import de.ims.icarus.util.id.UnknownIdentifierException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ActionManager {

	public static final String DIRECTION_PARAMETER = "direction"; //$NON-NLS-1$
	public static final String FILL_TOOLBAR = "fillToolBar"; //$NON-NLS-1$

	public static final String RIGHT_TO_LEFT = "rightToLeft"; //$NON-NLS-1$
	public static final String LEFT_TO_RIGHT = "leftToRight"; //$NON-NLS-1$

	public static final String SEPARATOR_SMALL = "small"; //$NON-NLS-1$
	public static final String SEPARATOR_MEDIUM = "medium"; //$NON-NLS-1$
	public static final String SEPARATOR_WIDE = "wide"; //$NON-NLS-1$

	public static final String SMALL_SELECTED_ICON_KEY = "IcarusSmallSelectedIcon"; //$NON-NLS-1$
	public static final String LARGE_SELECTED_ICON_KEY = "IcarusLargeSelectedIcon"; //$NON-NLS-1$

	private ResourceDomain resourceDomain;
	private IconRegistry iconRegistry;

	private final ActionManager parent;

	private boolean silent = false;

	protected Map<String, Action> actionMap;
	protected Map<String, ActionSet> actionSetMap;
	protected Map<String, ActionList> actionListMap;
	protected TIntObjectMap<ButtonGroup> groupMap;
	protected Map<String, ActionAttributes> attributeMap;

	private static ActionManager instance;

	public static ActionManager globalManager() {
		if(instance==null) {
			synchronized (ActionManager.class) {
				if(instance==null) {
					instance = new ActionManager(null, null, null);

					URL actionLocation = ActionManager.class.getResource(
							"default-actions.xml"); //$NON-NLS-1$
					if(actionLocation==null)
						throw new CorruptedStateException("Missing resources: default-actions.xml"); //$NON-NLS-1$

					try {
						instance.loadActions(actionLocation);
					} catch (IOException e) {
						LoggerFactory.error(ActionManager.class, "Failed to load actions from file: "+actionLocation, e); //$NON-NLS-1$
					}
				}
			}
		}

		return instance;
	}

	protected ComponentHandler toolBarHandler;
	protected ComponentHandler menuHandler;
	protected ComponentHandler popupMenuHandler;
	protected ComponentHandler menuBarHandler;

	protected Set<String> loadedResources;

	/**
	 *
	 */
	public ActionManager(ActionManager parent, ResourceDomain resourceDomain, IconRegistry iconRegistry) {
		this.parent = parent;
		this.resourceDomain = resourceDomain;
		this.iconRegistry = iconRegistry;

		// Inherit silent flag from parent
		if(parent!=null) {
			setSilent(parent.isSilent());
		}
	}

	public ResourceDomain getResourceDomain() {
		if(resourceDomain==null) {
			synchronized (this) {
				if(resourceDomain==null && parent!=null)
					resourceDomain = parent.getResourceDomain();

				if(resourceDomain==null)
					resourceDomain = ResourceManager.getInstance().getGlobalDomain();
			}
		}

		return resourceDomain;
	}

	public IconRegistry getIconRegistry() {
		if(iconRegistry==null) {
			synchronized (this) {
				if(iconRegistry==null && parent!=null)
					iconRegistry = parent.getIconRegistry();

				if(iconRegistry==null)
					iconRegistry = IconRegistry.getGlobalRegistry();
			}
		}

		return iconRegistry;
	}

	public ActionManager derive() {
		return new ActionManager(this, null, null);
	}

	protected boolean isSilent() {
		return silent;
	}

	protected void setSilent(boolean silent) {
		this.silent = silent;
	}

	public ActionManager getParent() {
		return parent;
	}

	protected DelegateAction getDelegateAction(String id) {
		Action a = getAction(id);
		if(a instanceof DelegateAction) {
			return (DelegateAction)a;
		}
		return null;
	}

	protected StateChangeAction getStateChangeAction(String id) {
		Action a = getAction(id);
		if(a instanceof StateChangeAction) {
			return (StateChangeAction)a;
		}
		return null;
	}

	protected void addAttributes(Attributes attrs) {
		if (attributeMap == null) {
			attributeMap = new HashMap<>();
		}
		attributeMap.put(attrs.getValue(ID_ATTRIBUTE), new ActionAttributes(attrs));
	}

	protected ActionAttributes getAttributes(String key) {
		ActionAttributes attributes = null;

		if (attributeMap != null) {
			attributes = attributeMap.get(key);
		}

		if(attributes==null && parent!=null) {
			attributes = parent.getAttributes(key);
		}

		return attributes;
	}

	public Action deriveAction(String id, String templateId) {
		Exceptions.testNullArgument(id, "id"); //$NON-NLS-1$
		Exceptions.testNullArgument(templateId, "templateId"); //$NON-NLS-1$
		if(actionMap==null) {
			actionMap = new HashMap<>();
		}

		ActionAttributes attr = getAttributes(templateId);
		if(attr==null)
			throw new UnknownIdentifierException("Unknown template id: "+templateId); //$NON-NLS-1$

		return createAction(attr, id);
	}

	protected Action findAction(String id) {
		Action action = null;
		if(actionMap!=null) {
			action = actionMap.get(id);
		}

		if(action==null && parent!=null) {
			action = parent.findAction(id);
		}

		return action;
	}

	public Action getAction(String id) {
		Exceptions.testNullArgument(id, "id"); //$NON-NLS-1$

		// Search for action all along the parent line
		Action action = findAction(id);

		// Action already found -> return it
		if(action!=null) {
			return action;
		}

		// Fetch attributes to create action from
		ActionAttributes attr = getAttributes(id);

		if(attr==null && !isSilent())
			throw new UnknownIdentifierException("Unknown action id: "+id); //$NON-NLS-1$

		// Virtual actions are not supposed to be instantiated directly
		if(attr!=null && Boolean.parseBoolean(attr.getValue(VIRTUAL_INDEX)))
			throw new IllegalArgumentException("Cannot instantiate virtual action: "+id); //$NON-NLS-1$

		// Create new action
		return createAction(attr, null);
	}

	public void addAction(String id, Action action) {
		Exceptions.testNullArgument(id, "id"); //$NON-NLS-1$
		Exceptions.testNullArgument(action, "action"); //$NON-NLS-1$

		if(actionMap==null) {
			actionMap = new HashMap<>();
		}

		synchronized (actionMap) {
			if(actionMap.containsKey(id) && !isSilent())
				throw new DuplicateIdentifierException("Duplicate action id: "+id); //$NON-NLS-1$

			actionMap.put(id, action);
		}
	}

	protected Action createAction(ActionAttributes attr, String id) {
		Action action = null;
		if (attr != null) {
			// For deriving actions from a template we have to use
			// the given id instead of the template defined one
			if(id==null) {
				id = attr.getValue(ID_INDEX);
			}

			String type = attr.getValue(TYPE_INDEX);
			if ("toggle".equals(type)) { //$NON-NLS-1$
				action = new StateChangeAction();
			} else {
				action = new DelegateAction();
			}
			configureAction(action, attr, null, id);

			addAction(id, action);
		}
		return action;
	}

	private void configureAction(Action action, ActionAttributes attr, ActionAttributes orig, String id) {
		if(orig==null) {
			orig = attr;
		}

		if(attr.hasValue(TEMPLATE_INDEX)) {
			String templateId = attr.getValue(TEMPLATE_INDEX);
			ActionAttributes tplAttr = getAttributes(templateId);
			if(tplAttr==null && !silent)
				throw new UnknownIdentifierException("Unknown template id: "+templateId); //$NON-NLS-1$

			if(tplAttr!=null) {
				configureAction(action, tplAttr, orig, id);
			}
		}

		action.putValue(Action.NAME, attr.getValue(NAME_INDEX));
		if(attr.hasValue(SMALL_SELECTED_ICON_INDEX)) {
			action.putValue(SMALL_SELECTED_ICON_KEY, getIconRegistry().getIcon(
					attr.getValue(SMALL_SELECTED_ICON_INDEX)));
		}
		if(attr.hasValue(SMALL_ICON_INDEX)) {
			action.putValue(Action.SMALL_ICON, getIconRegistry().getIcon(
					attr.getValue(SMALL_ICON_INDEX)));
		}
		if(attr.hasValue(LARGE_SELECTED_ICON_INDEX)) {
			action.putValue(LARGE_SELECTED_ICON_KEY, getIconRegistry().getIcon(
					attr.getValue(LARGE_SELECTED_ICON_INDEX)));
		}
		if(attr.hasValue(LARGE_ICON_INDEX)) {
			action.putValue(Action.LARGE_ICON_KEY, getIconRegistry().getIcon(
					attr.getValue(LARGE_ICON_INDEX)));
		}
		String command = attr.getValue(COMMAND_INDEX);
		if(command==null) {
			command = id;
		}
		if(attr.hasValue(COMMAND_INDEX)) {
			action.putValue(Action.ACTION_COMMAND_KEY, command);
		}
		if(attr.hasValue(DESC_INDEX)) {
			action.putValue(Action.SHORT_DESCRIPTION, attr.getValue(DESC_INDEX));
			action.putValue(Action.LONG_DESCRIPTION, attr.getValue(DESC_INDEX));
		}

		String mnemonic = attr.getValue(MNEMONIC_INDEX);
		if (mnemonic != null && !mnemonic.equals("")) { //$NON-NLS-1$
			action.putValue(Action.MNEMONIC_KEY,
					new Integer(mnemonic.charAt(0)));
		}
		String accel = attr.getValue(ACCEL_INDEX);
		if (accel != null && !accel.equals("")) { //$NON-NLS-1$
			action.putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(accel));
		}

		// Finally apply localization
		if(orig==attr) {
			getResourceDomain().prepareAction(action,
					(String)action.getValue(Action.NAME),
					(String)action.getValue(Action.SHORT_DESCRIPTION));
			getResourceDomain().addAction(action);
		}
	}

	public ActionSet getActionSet(String id) {
		Exceptions.testNullArgument(id, "id"); //$NON-NLS-1$
		ActionSet actionSet = null;

		if(actionSetMap!=null) {
			actionSet = actionSetMap.get(id);
		}

		if(actionSet==null && parent!=null) {
			try {
				actionSet = parent.getActionSet(id);
			} catch(UnknownIdentifierException e) {
				// ignore the 'silent' setting of parent managers
			}
		}

		if(actionSet==null && !isSilent())
			throw new UnknownIdentifierException("Unknown action-set id: "+id); //$NON-NLS-1$

		return actionSet;
	}

	public void addActionSet(String id, ActionSet actionSet) {
		Exceptions.testNullArgument(id, "id"); //$NON-NLS-1$
		Exceptions.testNullArgument(actionSet, "actionSet"); //$NON-NLS-1$

		if(actionSetMap==null) {
			actionSetMap = new HashMap<>();
		}

		synchronized (actionSetMap) {
			if(actionSetMap.containsKey(id) && !isSilent())
				throw new DuplicateIdentifierException("Duplicate action-set id: "+id); //$NON-NLS-1$

			actionSetMap.put(id, actionSet);
		}
	}

	public ActionList getActionList(String id) {
		Exceptions.testNullArgument(id, "id"); //$NON-NLS-1$
		ActionList actionList = null;
		if(actionListMap!=null) {
			actionList = actionListMap.get(id);
		}

		if(actionList==null && parent!=null) {
			try {
				actionList = parent.getActionList(id);
			} catch(UnknownIdentifierException e) {
				// ignore the 'silent' setting of parent managers
			}
		}

		if(actionList==null && !isSilent())
			throw new UnknownIdentifierException("Unknown action-list id: "+id); //$NON-NLS-1$

		return actionList;
	}

	public void addActionList(String id, ActionList actionList) {
		Exceptions.testNullArgument(id, "id"); //$NON-NLS-1$
		Exceptions.testNullArgument(actionList, "actionList"); //$NON-NLS-1$

		if(actionListMap==null) {
			actionListMap = new HashMap<>();
		}

		synchronized (actionListMap) {
			if(actionListMap.containsKey(id) && !isSilent())
				throw new DuplicateIdentifierException("Duplicate action-list id: "+id); //$NON-NLS-1$

			actionListMap.put(id, actionList);
		}
	}

	public ButtonGroup getGroup(String groupId, Component comp) {
		Exceptions.testNullArgument(groupId, "id"); //$NON-NLS-1$
		Exceptions.testNullArgument(comp, "comp"); //$NON-NLS-1$

		if(groupMap==null) {
			groupMap = new TIntObjectHashMap<>();
		}

		int key = groupId.hashCode() ^ comp.hashCode();

		ButtonGroup group = groupMap.get(key);
		if(group==null) {
			if(group==null) {
				group = new ButtonGroup();
				groupMap.put(key, group);
			}
		}

		return group;
	}

	public void setEnabled(boolean enabled, String...ids) {
		for(String id : ids) {
			Action action = getAction(id);
			if (action != null) {
				action.setEnabled(enabled);
			}
		}
	}

	public boolean isEnabled(String id) {
		Action action = getAction(id);
		if (action != null) {
			return action.isEnabled();
		}
		return false;
	}

	public void setSelected(boolean selected, String...ids) {
		for(String id : ids) {
			StateChangeAction action = getStateChangeAction(id);
			if (action != null) {
				action.setSelected(selected);
			}
		}
	}

	public boolean isSelected(String id) {
		StateChangeAction action = getStateChangeAction(id);
		if (action != null) {
			return action.isSelected();
		}
		return false;
	}

	public boolean isStateChangeAction(String id) {
		return (getStateChangeAction(id) != null);
	}

	/**
	 * Registers a {@code handler} object to receive notifications
	 * about events on the specified action. All invocations are
	 * targeted at the method named {@code method} with the matching
	 * signature for the invocation at hand:
	 * <p>
	 * Calls made through {@link ActionListener} interfaces will forward
	 * the provided {@link ActionEvent}.
	 * Calls coming from {@link ItemListener}s on the other hand provide
	 * the received {@code ItemEvent}.
	 * <p>
	 * It is up to the {@code handler} object to distinguish between those
	 * two cases by providing methods with different signatures or merge
	 * the handling by providing only one method with a sole parameter of
	 * type {@code Object}.
	 * <p>
	 * Note that all internal handlers store weak references to the {@code handler}
	 * objects registered as callbacks. When the target of such a reference
	 * gets garbage collected the next invocation attempt will cause the
	 * listener to be unregistered from the action. So it is strongly recommended
	 * to store a strong reference to all handler objects!
	 *
	 * @param id the unique identifier of the {@code Action} the handler should
	 * be attached to
	 * @param handler the object defining the callback method
	 * @param method method name used to find an appropriate {@code Method} when
	 * forwarding events
	 * @throws IllegalArgumentException if any of the arguments is {@code null} or
	 * if the referenced {@code Action} is not able to forward events (i.e. it is
	 * not derived of type {@link DelegateAction})
	 * @throws UnknownIdentifierException if the given {@code id} is not mapped to
	 * an {@code Action} and this manager is not configured to be silent
	 */
	public void addHandler(String id, Object handler, String method) {
		Exceptions.testNullArgument(handler, "handler"); //$NON-NLS-1$
		Exceptions.testNullArgument(method, "method"); //$NON-NLS-1$
		Action a = getAction(id);

		if(a instanceof StateChangeAction) {
			ItemListener listener = new BooleanInvocationHandler(a, handler, method);
			((StateChangeAction)a).addItemListener(listener);
		}

		if(a instanceof DelegateAction) {
			ActionListener listener = new ActionInvocationHandler(a, handler, method);
			((DelegateAction)a).addActionListener(listener);
		} else
			throw new IllegalArgumentException("Cannot attach handler to non-delegating action: "+id); //$NON-NLS-1$
	}

	public void removeHandler(String id, Object handler, String method) {
		Exceptions.testNullArgument(handler, "handler"); //$NON-NLS-1$
		Exceptions.testNullArgument(method, "method"); //$NON-NLS-1$
		Action a = getAction(id);

		if(a instanceof StateChangeAction) {
			StateChangeAction sa = (StateChangeAction) a;
			ItemListener[] listeners = sa.getItemListeners();
			for(ItemListener listener : listeners) {
				if(listener instanceof WeakHandler) {
					WeakHandler wh = (WeakHandler) listener;
					if(wh.getTarget()==handler && wh.getMethodName().equals(method)) {
						sa.removeItemListener(listener);
					}
				}
			}
		}

		if(a instanceof DelegateAction) {
			DelegateAction da = (DelegateAction) a;
			ActionListener[] listeners = da.getActionListeners();
			for(ActionListener listener : listeners) {
				if(listener instanceof WeakHandler) {
					WeakHandler wh = (WeakHandler) listener;
					if(wh.getTarget()==handler && wh.getMethodName().equals(method)) {
						da.removeActionListener(listener);
					}
				}
			}
		}
	}

	protected void feedActionSet(final Component container,
			final ComponentHandler handler, final ActionSet actionSet, final Map<String, Object> properties) {

		int size = actionSet.size();
		int index;
		boolean rightToLeft = RIGHT_TO_LEFT.equals(properties.get(DIRECTION_PARAMETER));
		for(int i=0; i<size; i++) {
			index = rightToLeft ? size-i-1 : i;
			String actionId = actionSet.getActionIdAt(index);
			Action action = getAction(actionId);
			if(action!=null)
				handler.feedAction(container, action, actionSet.getGroupId(actionId));
			else
				LoggerFactory.log(this, Level.WARNING, String.format(
						"Unknown action id in set '%s': '%s'",  //$NON-NLS-1$
						actionSet.getId(), actionSet.getActionIdAt(i)));
		}
	}

	protected void feedActionList(final Component container,
			final ComponentHandler handler, final ActionList list, final Map<String, Object> properties) {

		Action action;
		ActionSet actionSet;

		int size = list.size();
		int index;
		boolean rightToLeft = RIGHT_TO_LEFT.equals(properties.get(DIRECTION_PARAMETER));
		boolean separatorAllowed = false;
		for(int i=0; i<size; i++) {
			index = rightToLeft ? size-i-1 : i;
			String value = list.getValueAt(index);
			switch (list.getTypeAt(index)) {
			case LABEL:
				handler.feedComponent(container, createLabel(value));
				break;

			case SEPARATOR:
				// Prevent two separators beside each other
				if(separatorAllowed) {
					handler.feedSeparator(container, value);
					separatorAllowed = false;
				}
				break;

			case EMPTY:
				handler.feedEmpty(container, value);
				break;

			case GLUE:
				handler.feedGlue(container);
				break;

			case ACTION_ID:
				action = getAction(value);
				if(action!=null)
					handler.feedAction(container, action, null);
				else
					LoggerFactory.log(this, Level.WARNING, "Unknown action id: "+value); //$NON-NLS-1$
				break;

			case ACTION_SET_ID:
				actionSet = getActionSet(value);
				if(actionSet==null) {
					LoggerFactory.log(this, Level.WARNING, "Unknown action-set id: "+value); //$NON-NLS-1$
					break;
				}
				feedActionSet(container, handler, actionSet, properties);
				break;

			case ACTION_LIST_ID:
				ActionList subList = getActionList(value);
				if(subList!=null)
					handler.feedList(container, subList, properties);
				else
					LoggerFactory.log(this, Level.WARNING, "Unknown action-list id: "+value); //$NON-NLS-1$
				break;

			case CUSTOM:
				Object replacement = properties.get(value);
				if(replacement==null) {
					value = null;
				} else {
					Object[] items = replacement instanceof Object[] ?
							(Object[]) replacement : new Object[]{replacement};
					for(Object item : items) {
						if(item instanceof String) {
							handler.feedComponent(container, createLabel((String)item));
						} else if(item instanceof Action) {
							handler.feedAction(container, (Action)item, null);
						} else if(item instanceof ActionSet) {
							feedActionSet(container, handler, (ActionSet)item, properties);
						} else if(item instanceof ActionList) {
							handler.feedList(container, (ActionList)item, properties);
						} else if(item instanceof Component) {
							handler.feedComponent(container, (Component)item);
						} else if(item==EntryType.SEPARATOR) {
							// No checking for subsequent separators!
							handler.feedSeparator(container, null);
						} else if(item!=null) {
							// Null replacements are legal, unrecognizable objects are not
							LoggerFactory.log(this, Level.WARNING, "Not a valid action-list element: "+String.valueOf(item)); //$NON-NLS-1$
						} else {
							LoggerFactory.log(this, Level.FINE, "No replacement defined for item: "+list.getTypeAt(index)); //$NON-NLS-1$
						}
					}
				}
				break;
			}
			separatorAllowed = list.getTypeAt(i)!=EntryType.SEPARATOR && value!=null;
		}
	}

	private static Border DEFAULT_LABEL_BORDER;

	protected Component createLabel(String value) {
		JLabel label = new JLabel();

		if(value!=null && !value.isEmpty()) {
			getResourceDomain().prepareComponent(label, value, null);
			getResourceDomain().addComponent(label);
		} else {
			label.setText("<undefined>"); //$NON-NLS-1$
		}

		if(DEFAULT_LABEL_BORDER==null) {
			DEFAULT_LABEL_BORDER = new EmptyBorder(0, 4, 0, 3);
		}

		label.setBorder(DEFAULT_LABEL_BORDER);

		return label;
	}

	protected ComponentHandler getHandler(Class<?> containerClass) {
		if(containerClass==JMenu.class) {
			if(menuHandler==null)
				menuHandler = new MenuHandler();
			return menuHandler;
		} else if(containerClass==JPopupMenu.class) {
			if(popupMenuHandler==null)
				popupMenuHandler = new PopupMenuHandler();
			return popupMenuHandler;
		} else if(containerClass==JToolBar.class) {
			if(toolBarHandler==null)
				toolBarHandler = new ToolBarHandler();
			return toolBarHandler;
		} else if(containerClass==JMenuBar.class) {
			if(menuBarHandler==null)
				menuBarHandler = new MenuBarHandler();
			return menuBarHandler;
		} else
			throw new IllegalArgumentException("Unsupported container class: "+containerClass); //$NON-NLS-1$
	}

	protected static final Map<String, Object> EMPTY_PROPERTIES = Collections.emptyMap();

	public JMenu createMenu(String id, Map<String, Object> properties) {
		ActionList actionList = getActionList(id);

		if(actionList==null) {
			LoggerFactory.log(this, Level.WARNING, "Unknown action-list id: "+id); //$NON-NLS-1$
			return null;
		}

		return createMenu(actionList, properties);
	}

	public JMenu createMenu(ActionList actionList, Map<String, Object> properties) {
		Exceptions.testNullArgument(actionList, "actionList"); //$NON-NLS-1$
		if(properties==null) {
			properties = EMPTY_PROPERTIES;
		}

		Action action = null;
		if(actionList.getActionId()!=null) {
			action = getAction(actionList.getActionId());
		}

		JMenu menu = new JMenu(action);

		feedActionList(menu, getHandler(JMenu.class), actionList, properties);
		configureMenu(menu, action, properties);

		return menu;
	}

	public JMenuBar createMenuBar(String id, Map<String, Object> properties) {
		ActionList actionList = getActionList(id);

		if(actionList==null) {
			LoggerFactory.log(this, Level.WARNING, "Unknown action-list id: "+id); //$NON-NLS-1$
			return null;
		}

		return createMenuBar(actionList, properties);
	}

	public JMenuBar createMenuBar(ActionList actionList, Map<String, Object> properties) {
		Exceptions.testNullArgument(actionList, "actionList"); //$NON-NLS-1$
		if(properties==null) {
			properties = EMPTY_PROPERTIES;
		}

		JMenuBar menuBar = new JMenuBar();

		feedActionList(menuBar, getHandler(JMenuBar.class), actionList, properties);
		configureMenuBar(menuBar, properties);

		return menuBar;
	}

	public JToolBar createEmptyToolBar() {
		JToolBar toolBar = new JToolBar();
		configureToolBar(toolBar, EMPTY_PROPERTIES);

		return toolBar;
	}

	public JToolBar createToolBar(String id, Map<String, Object> properties) {
		ActionList actionList = getActionList(id);

		if(actionList==null) {
			LoggerFactory.log(this, Level.WARNING, "Unknown action-list id: "+id); //$NON-NLS-1$
			return null;
		}

		return createToolBar(actionList, properties);
	}

	public JToolBar createToolBar(ActionList actionList, Map<String, Object> properties) {
		Exceptions.testNullArgument(actionList, "actionList"); //$NON-NLS-1$
		if(properties==null) {
			properties = EMPTY_PROPERTIES;
		}

		JToolBar toolBar = new JToolBar();

		feedActionList(toolBar, getHandler(JToolBar.class), actionList, properties);

		configureToolBar(toolBar, properties);

		return toolBar;
	}

	public void feedToolBar(String id, JToolBar toolBar, Map<String, Object> properties) {
		ActionList actionList = getActionList(id);

		if(actionList==null) {
			LoggerFactory.log(this, Level.WARNING, "Unknown action-list id: "+id); //$NON-NLS-1$
			return;
		}

		feedToolBar(actionList, toolBar, properties);
	}

	public void feedToolBar(ActionList actionList, JToolBar toolBar, Map<String, Object> properties) {
		Exceptions.testNullArgument(actionList, "actionList"); //$NON-NLS-1$
		if(properties==null) {
			properties = EMPTY_PROPERTIES;
		}

		feedActionList(toolBar, getHandler(JToolBar.class), actionList, properties);

		configureToolBar(toolBar, properties);
	}

	public JPopupMenu createPopupMenu(String id, Map<String, Object> properties) {
		ActionList actionList = getActionList(id);

		if(actionList==null) {
			LoggerFactory.log(this, Level.WARNING, "Unknown action-list id: "+id); //$NON-NLS-1$
			return null;
		}

		return createPopupMenu(actionList, properties);
	}

	public JPopupMenu createPopupMenu(ActionList actionList, Map<String, Object> properties) {
		Exceptions.testNullArgument(actionList, "actionList"); //$NON-NLS-1$
		if(properties==null) {
			properties = EMPTY_PROPERTIES;
		}

		JPopupMenu popupMenu = new JPopupMenu();

		feedActionList(popupMenu, getHandler(JPopupMenu.class), actionList, properties);
		configurePopupMenu(popupMenu, properties);

		return popupMenu;
	}

	protected final JMenuItem createMenuItem(Action action, String groupId,	Component container) {
		JMenuItem menuItem = null;
		if (action instanceof StateChangeAction) {
			StateChangeAction sca = (StateChangeAction)action;

			menuItem = new JCheckBoxMenuItem(sca);
			menuItem.addItemListener(sca);
			menuItem.setSelected(sca.isSelected());
			if (groupId != null) {
				ButtonGroup group = getGroup(groupId, container);
				group.add(menuItem);

				action.addPropertyChangeListener(new ToggleActionPropertyChangeListener(menuItem));
			}

			configureToggleMenuItem(menuItem, sca);

			sca.addPropertyChangeListener(new ToggleActionPropertyChangeListener(menuItem));
		} else if(action!=null) {
			menuItem = new JMenuItem(action);
			configureMenuItem(menuItem, action);
		}
		return menuItem;
	}

	/**
	 * Creates a {@link JButton} or {@link JToggleButton} depending
	 * on the type of the given {@code Action} (a {@code StateChangeAction}
	 * will result in a {@code JToggleButton}). If the supplied {@code Action}
	 * is {@code null} then this will silently fail by returning {@code null}.
	 */
	protected final AbstractButton createButton(Action action, String groupId, Component container) {
		AbstractButton button = null;
		if (action instanceof StateChangeAction) {
			StateChangeAction sca = (StateChangeAction)action;
			ButtonGroup group = groupId==null ? null : getGroup(groupId, container);

			button = new JToggleButton(sca);
			button.addItemListener(sca);
			button.setSelected(sca.isSelected());
			if (group != null) {
				group.add(button);
			}
			configureToggleButton((JToggleButton)button, sca);
			sca.addPropertyChangeListener(new ToggleActionPropertyChangeListener(button));
		} else if(action!=null) {
			button = new JButton(action);
			configureButton(button, action);
		}
		return button;
	}

	// configuration callbacks

	protected void configureMenu(JMenu menu, Action action, Map<String, Object> properties) {
		// no-op
	}

	protected void configurePopupMenu(JPopupMenu popupMenu, Map<String, Object> properties) {
		// no-op
	}

	protected void configureToolBar(JToolBar toolBar, Map<String, Object> properties) {
		toolBar.setFloatable(false);
		toolBar.setRollover(true);

		if(Boolean.parseBoolean(String.valueOf(properties.get("multiline")))) { //$NON-NLS-1$
			// FIXME ModifiedFlowLayout needs rework to support glue objects
			toolBar.setLayout(new ModifiedFlowLayout(FlowLayout.LEFT, 1, 3));
		}
	}

	protected void configureMenuBar(JMenuBar menuBar, Map<String, Object> properties) {
		// no-op
	}

	protected void configureToggleButton(JToggleButton button, Action action) {
		configureButton(button, action);

		Icon selectedIcon = (Icon) action.getValue(SMALL_SELECTED_ICON_KEY);
		if(selectedIcon==null) {
			selectedIcon = (Icon) action.getValue(LARGE_SELECTED_ICON_KEY);
		}
		button.setSelectedIcon(selectedIcon);
	}

	protected void configureButton(AbstractButton button, Action action) {
		button.setHideActionText(true);
		button.setFocusable(false);

		// TODO check for requirements of a default preferred size for buttons!
		/*Icon icon = button.getIcon();
		if(icon!=null) {
			int width = Math.max(24, icon.getIconWidth()+6);
			int height = Math.max(24, icon.getIconHeight()+6);
			button.setPreferredSize(new Dimension(width, height));
		}*/
	}

	protected void configureToggleMenuItem(JMenuItem menuItem, Action action) {
		configureMenuItem(menuItem, action);
	}

	protected void configureMenuItem(JMenuItem menuItem, Action action) {
		// no-op
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected static class ToggleActionPropertyChangeListener implements PropertyChangeListener {

		private transient WeakReference<AbstractButton> target;

		public ToggleActionPropertyChangeListener(AbstractButton button) {
			this.target = new WeakReference<AbstractButton>(button);
		}

	    public AbstractButton getTarget() {
	        if (target == null) {
	            return null;
	        }
	        return this.target.get();
	    }

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String propertyName = evt.getPropertyName();

			if (propertyName.equals("selected")) { //$NON-NLS-1$
				AbstractButton button = target.get();
				if(button==null) {
					Action action = (Action) evt.getSource();
					action.removePropertyChangeListener(this);
				} else {
					Boolean selected = (Boolean) evt.getNewValue();
					button.setSelected(selected.booleanValue());
				}
			}
		}

	    private void writeObject(ObjectOutputStream s) throws IOException {
	        s.defaultWriteObject();
	        s.writeObject(getTarget());
	    }

	    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
	        s.defaultReadObject();
	        AbstractButton target = (AbstractButton)s.readObject();
	        if (target != null) {
	        	this.target = new WeakReference<AbstractButton>(target);
	        }
	    }
	}

	protected static class WeakActionHandler extends WeakHandler {

		private final Action action;

		public WeakActionHandler(Action action, Object target, String methodName) {
			super(target, methodName);
			this.action = action;
		}

		public Action getAction() {
			return action;
		}
	}

	protected static class ActionInvocationHandler extends WeakActionHandler implements ActionListener {

		public ActionInvocationHandler(Action action, Object target, String methodName) {
			super(action, target, methodName);
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(isObsolete()) {
				DelegateAction action = (DelegateAction) getAction();
				action.removeActionListener(this);
			} else {
				dispatch(e);
			}
		}
	}

	protected static class BooleanInvocationHandler extends WeakActionHandler implements ItemListener {

		public BooleanInvocationHandler(Action action, Object target, String methodName) {
			super(action, target, methodName);
		}

		@Override
		public void itemStateChanged(ItemEvent evt) {
			Boolean value = Boolean.TRUE;
			if (evt.getStateChange() == ItemEvent.DESELECTED) {
				value = Boolean.FALSE;
			}

			if(isObsolete()) {
				StateChangeAction action = (StateChangeAction) getAction();
				action.removeItemListener(this);
			} else {
				dispatch(value);
			}
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	class ActionAttributes {

		private String[] array;

		public ActionAttributes(Attributes attrs) {
			array = new String[13];
			setValue(ID_INDEX, attrs.getValue(ID_ATTRIBUTE));
			setAttributes(attrs);
		}

		public String getValue(int index) {
			return array[index];
		}

		private String substitute(String s) {
			String id = getValue(ID_INDEX);
			if(id!=null) {
				s = s.replaceFirst("\\$\\{id\\}", id); //$NON-NLS-1$
			}
			return s;
		}

		public boolean hasValue(int index) {
			return array[index]!=null;
		}

		public void setValue(int index, String value) {
			// Do not allow 'clearing' of fields
			if(value!=null) {
				if(index!=ID_INDEX)
					value = substitute(value);

				array[index] = value;
			}
		}

		public void setAttributes(Attributes attrs) {
			setValue(TEMPLATE_INDEX, attrs.getValue(TEMPLATE_ATTRIBUTE));
			setValue(ACCEL_INDEX, attrs.getValue(ACCEL_ATTRIBUTE));
			setValue(DESC_INDEX, attrs.getValue(DESC_ATTRIBUTE));
			setValue(LARGE_ICON_INDEX, attrs.getValue(LARGE_ICON_ATTRIBUTE));
			setValue(LARGE_SELECTED_ICON_INDEX, attrs.getValue(LARGE_SELECTED_ICON_ATTRIBUTE));
			setValue(MNEMONIC_INDEX, attrs.getValue(MNEMONIC_ATTRIBUTE));
			setValue(NAME_INDEX, attrs.getValue(NAME_ATTRIBUTE));
			setValue(SMALL_ICON_INDEX, attrs.getValue(SMALL_ICON_ATTRIBUTE));
			setValue(SMALL_SELECTED_ICON_INDEX, attrs.getValue(SMALL_SELECTED_ICON_ATTRIBUTE));
			setValue(TYPE_INDEX, attrs.getValue(TYPE_ATTRIBUTE));
			setValue(VIRTUAL_INDEX, attrs.getValue(VIRTUAL_ATTRIBUTE));
			setValue(COMMAND_INDEX, attrs.getValue(COMMAND_ATTRIBUTE));
		}
	}

    private final static String ACCEL_ATTRIBUTE = "accel"; //$NON-NLS-1$
    private final static String DESC_ATTRIBUTE = "desc"; //$NON-NLS-1$
    private final static String LARGE_ICON_ATTRIBUTE = "licon"; //$NON-NLS-1$
    private final static String LARGE_SELECTED_ICON_ATTRIBUTE = "slicon"; //$NON-NLS-1$
    private final static String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
    private final static String IDREF_ATTRIBUTE = "idref"; //$NON-NLS-1$
    private final static String MNEMONIC_ATTRIBUTE = "mnemonic"; //$NON-NLS-1$
    private final static String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
    private final static String SMALL_ICON_ATTRIBUTE = "icon"; //$NON-NLS-1$
    private final static String SMALL_SELECTED_ICON_ATTRIBUTE = "sicon"; //$NON-NLS-1$
    private final static String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$
    private final static String VALUE_ATTRIBUTE = "value"; //$NON-NLS-1$
    private final static String VIRTUAL_ATTRIBUTE = "virtual"; //$NON-NLS-1$
    private final static String COMMAND_ATTRIBUTE = "command"; //$NON-NLS-1$
    private final static String TEMPLATE_ATTRIBUTE = "template"; //$NON-NLS-1$

    private final static int ACCEL_INDEX = 0;
    private final static int DESC_INDEX = 1;
    private final static int SMALL_ICON_INDEX = 2;
    private final static int SMALL_SELECTED_ICON_INDEX = 3;
    private final static int ID_INDEX = 4;
    private final static int MNEMONIC_INDEX = 5;
    private final static int NAME_INDEX = 6;
    private final static int LARGE_ICON_INDEX = 7;
    private final static int LARGE_SELECTED_ICON_INDEX = 8;
    private final static int TYPE_INDEX = 9;
    private final static int VIRTUAL_INDEX = 10;
    private final static int COMMAND_INDEX = 11;
    private final static int TEMPLATE_INDEX = 12;

    private static SAXParserFactory parserFactory;
    private XmlActionHandler xmlHandler;

	private void parseActions(InputStream stream) throws IOException {
		if (parserFactory == null) {
			parserFactory = SAXParserFactory.newInstance();
			parserFactory.setValidating(true);
		}

		if (xmlHandler == null) {
			xmlHandler = new XmlActionHandler();
		}

		try {
			SAXParser parser = parserFactory.newSAXParser();
			String dtdResource = getClass().getResource("").toString(); //$NON-NLS-1$

			parser.parse(stream, xmlHandler, dtdResource);
		} catch (SAXException e) {
			throw new IOException("Error parsing: " + e.getMessage()); //$NON-NLS-1$
		} catch (IOException e) {
			throw e;
		} catch (ParserConfigurationException e) {
			throw new IOException("Error configuring parser: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/**
	 * Hook for subclasses to bypass the optimization regarding
	 * redundant loading of action resources.
	 * <p>
	 * The default implementation returns {@code true}.
	 */
	protected boolean isPreventRedundantLoading() {
		return true;
	}

	/**
	 *
	 */
	public void loadActions(URL location) throws IOException {
		Exceptions.testNullArgument(location, "location"); //$NON-NLS-1$

		// Fix for umlauts and such causing problems with the xerces internal URL scheme
		//FIXME spaces break the encoding when resolved back to files
//		location = IOUtil.encodeURL(location);

		if(loadedResources==null) {
			loadedResources = new HashSet<>();
		}

		// Skip redundant loading of resources
		if(isPreventRedundantLoading() && loadedResources.contains(location.toExternalForm())) {
			return;
		}

		InputStream stream = location.openStream();
		try {
			parseActions(stream);
		} finally {
			try {
				stream.close();
			} catch(IOException e) {
				// ignore
			}
		}

		loadedResources.add(location.toExternalForm());
	}

    /**
     *
     * @author Markus Gärtner
     * @version $Id$
     *
     */
	protected class XmlActionHandler extends DefaultHandler {

	    private final static String ACTION_ELEMENT="action"; //$NON-NLS-1$
	    private final static String ACTION_SET_ELEMENT="action-set"; //$NON-NLS-1$
	    private final static String ACTION_LIST_ELEMENT="action-list"; //$NON-NLS-1$
	    private final static String ITEM_ELEMENT="item"; //$NON-NLS-1$
	    private final static String EMPTY_ELEMENT="empty"; //$NON-NLS-1$
	    private final static String GROUP_ELEMENT="group"; //$NON-NLS-1$
	    private final static String SEPARATOR_ELEMENT="separator"; //$NON-NLS-1$

		private Stack<ActionList> actionListStack;
		private String groupId;
		private ActionList actionList;
		private ActionSet actionSet;

		@Override
		public void startDocument() {
			actionListStack = new Stack<>();
			groupId = null;
			actionList = null;
			actionSet = null;
		}

		@Override
		public void startElement(String nameSpace, String localName,
				String name, Attributes attributes) {

			if (ACTION_SET_ELEMENT.equals(name)) {
				String id = attributes.getValue(ID_ATTRIBUTE);
				actionSet = new ActionSet(id);
				if (actionList != null) {
					actionList.add(EntryType.ACTION_SET_ID, id);
				}
				addActionSet(id, actionSet);
			} else if (ACTION_LIST_ELEMENT.equals(name)) {
				String id = attributes.getValue(ID_ATTRIBUTE);
				String idref = attributes.getValue(IDREF_ATTRIBUTE);

				// Idref pointer overrides id
				if (idref == null) {
					idref = id;
				}
				ActionAttributes actionAtts = getAttributes(idref);
				if (actionAtts == null) {
					// Create new action for this list
					addAttributes(attributes);
				} else if(Boolean.parseBoolean(actionAtts.getValue(VIRTUAL_INDEX))) {
					// Not allowed to use a template within a list definition
					throw new RuntimeException("Cannot use template action id within list definition: "+idref); //$NON-NLS-1$
				} else {
					// Override fields
					actionAtts.setAttributes(attributes);
				}

				// Instantiate and add new list
				ActionList newList = new ActionList(id);
				newList.setActionId(idref);
				if (actionList != null) {
					actionList.add(EntryType.ACTION_LIST_ID, id);
					actionListStack.push(actionList);
				}
				addActionList(id, newList);

				actionList = newList;
			} else if (ACTION_ELEMENT.equals(name)) {
				String id = attributes.getValue(IDREF_ATTRIBUTE);
				if (id == null) {
					id = attributes.getValue(ID_ATTRIBUTE);
				}
				ActionAttributes actionAtts = getAttributes(id);
				if (actionAtts == null) {
					// Create new action
					addAttributes(attributes);
				} else if(!Boolean.parseBoolean(actionAtts.getValue(VIRTUAL_INDEX))) {
					// Override fields only if target is not a template!
					actionAtts.setAttributes(attributes);
				}

				if(actionSet!=null) {
					actionSet.add(id, groupId);
				} else if(actionList!=null) {
					actionList.add(EntryType.ACTION_ID, id);
					if(groupId!=null) {
						actionList.mapGroup(id, groupId);
					}
				}
			} else if (GROUP_ELEMENT.equals(name)) {
				groupId = attributes.getValue(ID_ATTRIBUTE);
			} else if (EMPTY_ELEMENT.equals(name)) {
				if (actionList != null) {
					actionList.add(EntryType.EMPTY, null);
				}
			} else if (SEPARATOR_ELEMENT.equals(name)) {
				if (actionList != null) {
					actionList.add(EntryType.SEPARATOR, null);
				}
			} else if(ITEM_ELEMENT.equals(name)) {
				if(actionList!=null) {
					String type = attributes.getValue(TYPE_ATTRIBUTE);
					String value = attributes.getValue(VALUE_ATTRIBUTE);

					actionList.add(EntryType.parse(type), value);
				}
			}
		}

		@Override
		public void endElement(String nameSpace, String localName, String name) {

			if (ACTION_SET_ELEMENT.equals(name)) {
				actionSet = null;
			} else if (ACTION_LIST_ELEMENT.equals(name)) {
				if(!actionListStack.isEmpty())
					actionList = actionListStack.pop();
				else
					actionList = null;
			} else if (GROUP_ELEMENT.equals(name)) {
				groupId = null;
			}
		}

		@Override
		public void endDocument() {
			actionListStack = null;
			groupId = null;
			actionList = null;
			actionSet = null;
		}

		@Override
		public void error(SAXParseException ex) throws SAXException {
			logException(Level.SEVERE, ex);
		}

		@Override
		public void warning(SAXParseException ex) throws SAXException {
			logException(Level.WARNING, ex);
		}

		@Override
		public void fatalError(SAXParseException ex) throws SAXException {
			logException(Level.SEVERE, ex);
		}

		private void logException(Level level, SAXParseException ex) {
			StringBuilder sb = new StringBuilder();
			sb.append(ex.getMessage()).append(":\n"); //$NON-NLS-1$
			sb.append("Message: ").append(ex.getMessage()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("Public ID: ").append(String.valueOf(ex.getPublicId())).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("System ID: ").append(String.valueOf(ex.getSystemId())).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("Line: ").append(ex.getLineNumber()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("Column: ").append(ex.getColumnNumber()); //$NON-NLS-1$
			if(ex.getException()!=null)
				sb.append("\nEmbedded: ").append(ex.getException()); //$NON-NLS-1$

			LoggerFactory.log(this, level, sb.toString(), ex);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected interface ComponentHandler {
		void feedLabel(Component container, String label);
		void feedList(Component container, ActionList list, Map<String, Object> properties);
		void feedAction(Component container, Action a, String groupId);
		void feedSeparator(Component container, String value);
		void feedComponent(Component container, Component comp);
		void feedEmpty(Component container, String value);
		void feedGlue(Component container);
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class MenuHandler implements ComponentHandler {

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedList(java.awt.Component, de.ims.icarus.ui.actions.ActionList, java.util.Map)
		 */
		@Override
		public void feedList(Component container, ActionList list,
				Map<String, Object> properties) {
			JMenu menu = (JMenu) container;
			JMenu subMenu = createMenu(list, properties);
			if(subMenu!=null)
				menu.add(subMenu);
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedAction(java.awt.Component, javax.swing.Action)
		 */
		@Override
		public void feedAction(Component container, Action a, String groupId) {
			JMenu menu = (JMenu) container;
			menu.add(createMenuItem(a, groupId, container));
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedSeparator(java.awt.Component, java.lang.String)
		 */
		@Override
		public void feedSeparator(Component container, String value) {
			JMenu menu = (JMenu) container;
			menu.addSeparator();
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedComponent(java.awt.Component, java.awt.Component)
		 */
		@Override
		public void feedComponent(Component container, Component comp) {
			JMenu menu = (JMenu) container;
			menu.add(comp);
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedLabel(java.awt.Component, java.lang.String)
		 */
		@Override
		public void feedLabel(Component container, String label) {
			JMenu menu = (JMenu) container;
			menu.add(createLabel(label));
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedEmpty(java.awt.Component)
		 */
		@Override
		public void feedEmpty(Component container, String value) {
			JMenu menu = (JMenu) container;
			menu.add(" "); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedGlue(java.awt.Component)
		 */
		@Override
		public void feedGlue(Component container) {
			// not supported
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class PopupMenuHandler implements ComponentHandler {

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedList(java.awt.Component, de.ims.icarus.ui.actions.ActionList, java.util.Map)
		 */
		@Override
		public void feedList(Component container, ActionList list,
				Map<String, Object> properties) {
			JPopupMenu menu = (JPopupMenu) container;
			JMenu subMenu = createMenu(list, properties);
			if(subMenu!=null) {
				menu.add(subMenu);
			}
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedAction(java.awt.Component, javax.swing.Action)
		 */
		@Override
		public void feedAction(Component container, Action a, String groupId) {
			JPopupMenu menu = (JPopupMenu) container;
			menu.add(createMenuItem(a, groupId, container));
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedSeparator(java.awt.Component, java.lang.String)
		 */
		@Override
		public void feedSeparator(Component container, String value) {
			JPopupMenu menu = (JPopupMenu) container;
			menu.addSeparator();
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedComponent(java.awt.Component, java.awt.Component)
		 */
		@Override
		public void feedComponent(Component container, Component comp) {
			JPopupMenu menu = (JPopupMenu) container;
			menu.add(comp);
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedLabel(java.awt.Component, java.lang.String)
		 */
		@Override
		public void feedLabel(Component container, String label) {
			JPopupMenu menu = (JPopupMenu) container;
			menu.add(createLabel(label));
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedEmpty(java.awt.Component)
		 */
		@Override
		public void feedEmpty(Component container, String value) {
			JPopupMenu menu = (JPopupMenu) container;
			menu.add(" "); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedGlue(java.awt.Component)
		 */
		@Override
		public void feedGlue(Component container) {
			// not supported
		}

	}

	protected class MenuBarHandler implements ComponentHandler {

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedLabel(java.awt.Component, java.lang.String)
		 */
		@Override
		public void feedLabel(Component container, String label) {
			// not supported by menu bars
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedList(java.awt.Component, de.ims.icarus.ui.actions.ActionList, java.util.Map)
		 */
		@Override
		public void feedList(Component container, ActionList list,
				Map<String, Object> properties) {
			JMenuBar menuBar = (JMenuBar)container;

			JMenu menu = createMenu(list, properties);
			if(menu!=null) {
				menuBar.add(menu);
			}
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedAction(java.awt.Component, javax.swing.Action, java.lang.String)
		 */
		@Override
		public void feedAction(Component container, Action a, String groupId) {
			// not supported by menu bars
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedSeparator(java.awt.Component, java.lang.String)
		 */
		@Override
		public void feedSeparator(Component container, String value) {
			// not supported by menu bars
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedComponent(java.awt.Component, java.awt.Component)
		 */
		@Override
		public void feedComponent(Component container, Component comp) {
			// not supported by menu bars
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedEmpty(java.awt.Component)
		 */
		@Override
		public void feedEmpty(Component container, String value) {
			// not supported by menu bars
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedGlue(java.awt.Component)
		 */
		@Override
		public void feedGlue(Component container) {
			// not supported
		}

	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class ToolBarHandler implements ComponentHandler {

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedList(java.awt.Component, de.ims.icarus.ui.actions.ActionList, java.util.Map)
		 */
		@Override
		public void feedList(Component container, ActionList list,
				Map<String, Object> properties) {
			//feedActionList(container, this, list, properties);
			JToolBar toolBar = (JToolBar) container;
			JMenu menu = createMenu(list, properties);
			if(menu!=null) {
				toolBar.add(menu);
			}
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedAction(java.awt.Component, javax.swing.Action)
		 */
		@Override
		public void feedAction(Component container, Action a, String groupId) {
			JToolBar toolBar = (JToolBar) container;

			AbstractButton button = createButton(a, groupId, container);
			button.setHorizontalTextPosition(SwingConstants.CENTER);
	        button.setVerticalTextPosition(SwingConstants.BOTTOM);
	        //button.setFocusable(false);

			toolBar.add(button);
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedSeparator(java.awt.Component, java.lang.String)
		 */
		@Override
		public void feedSeparator(Component container, String value) {
			JToolBar toolBar = (JToolBar) container;
			int width = -1;

			if(value!=null) {
				switch (value) {
				case SEPARATOR_WIDE:
					width = 30;
					break;

				case SEPARATOR_MEDIUM:
					width = 20;
					break;

				case SEPARATOR_SMALL:
					width = 10;
					break;

				default:
					try {
						width = Integer.parseInt(value);
					} catch(NumberFormatException e) {
						width = -1;
					}
					break;
				}
			}

			if(width<0) {
				width = 4;
			}

			toolBar.addSeparator(new Dimension(width, 24));

			// TODO
			/*if(width>-1)
				toolBar.addSeparator(new Dimension(width, 24));
			else
				toolBar.addSeparator();*/
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedComponent(java.awt.Component, java.awt.Component)
		 */
		@Override
		public void feedComponent(Component container, Component comp) {
			JToolBar toolBar = (JToolBar) container;
			toolBar.add(comp);
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedLabel(java.awt.Component, java.lang.String)
		 */
		@Override
		public void feedLabel(Component container, String label) {
			JToolBar toolBar = (JToolBar) container;
			toolBar.add(createLabel(label));
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedEmpty(java.awt.Component)
		 */
		@Override
		public void feedEmpty(Component container, String value) {
			JToolBar toolBar = (JToolBar) container;
			int width = -1;

			if(value!=null) {
				switch (value) {
				case SEPARATOR_WIDE:
					width = 50;
					break;

				case SEPARATOR_MEDIUM:
					width = 25;
					break;

				case SEPARATOR_SMALL:
					width = 10;
					break;

				default:
					try {
						width = Integer.parseInt(value);
					} catch(NumberFormatException e) {
						width = -1;
					}
					break;
				}
			}

			if(width==-1) {
				width = 25;
			}

			toolBar.add(Box.createHorizontalStrut(width));
		}

		/**
		 * @see de.ims.icarus.ui.actions.ActionManager.ComponentHandler#feedGlue(java.awt.Component)
		 */
		@Override
		public void feedGlue(Component container) {
			JToolBar toolBar = (JToolBar) container;
			toolBar.add(Box.createHorizontalGlue());
		}

	}
}
