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

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import de.ims.icarus.util.Exceptions;


/**
 * An {@code ActionList} encapsulates a collection of action related
 * objects that can be used to construct menus, tool-bars, pop-ups and
 * the like. It basically holds a list of identifier {@code Strings} or
 * {@code null} values that are each associated with a certain {@code EntryType}.
 * Each {@code ActionList} created by the {@code ActionManager} or other
 * framework elements is immutable (i.e. it was created using a {@code null}
 * owner). All attempts to modify those lists by client code outside the
 * framework will cause {@code IllegalArgumentException} being thrown.
 * If an application wants to create their own {@code ActionList} instance
 * and modify it at runtime it can use the {@link #ActionList(String, Object)}
 * constructor to pass an {@code owner} object to the list that will serve
 * as a kind of {@code key} to access methods that structurally modify the list
 * or some of its properties.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ActionList {

	private List<ListEntry> list;
	private final String id;
	private final WeakReference<Object> owner;
	private String actionId;
	private Map<String, String> groupMap;

	/**
	 * Creates an {@code ActionList} that will be immutable
	 * for code outside the framework. That means all public methods
	 * that take an {@code owner} argument will throw {@code IllegalArgumentException}
	 * on every attempt to call them regardless of the {@code Object} passed
	 * as {@code owner} to the specific call.
	 * @param id the global identifier used to address this list
	 */
	public ActionList(String id) {
		this(id, null);
	}

	/**
	 * Creates an {@code ActionList} that will be immutable
	 * for all code besides the framework and the holder of the {@code owner}
	 * object. Calls to restricted methods like {@link #add(Object, EntryType, String)}
	 * will throw {@code IllegalArgumentException} if the given {@code owner}
	 * argument does not match the initial value set in this constructor or
	 * the initial {@code owner} is {@code null}. Note that framework members
	 * can still bypass those restriction by using the package private methods!
	 *
	 * @param id the global identifier used to address this list
	 * @param owner {@code "key"} to access restricted methods on this list or
	 * {@code null} if this {@code ActionList} is meant to be immutable
	 */
	public ActionList(String id, Object owner) {
		Exceptions.testNullArgument(id, "id"); //$NON-NLS-1$

		this.id = id;

		this.owner = owner==null ? null : new WeakReference<Object>(owner);
	}

	/**
	 * Returns the global identifier assigned to this list
	 */
	public String getId() {
		return id;
	}

	/**
	 * Fetches the {@code value} this list stores at the given {@code index}.
	 * Note that the meaning of this value is depending on the {@code EntryType}
	 * defined for that {@code index} and might even be {@code null}.
	 */
	public String getValueAt(int index) {
		if(list!=null) {
			ListEntry entry = list.get(index);
			return entry.getValue();
		}
		return null;
	}

	/**
	 * Fetches the {@code EntryType} that describes the actual
	 * 'content' of data stored at the given {@code index}.
	 */
	public EntryType getTypeAt(int index) {
		if(list!=null) {
			ListEntry entry = list.get(index);
			return entry.getType();
		}
		return null;
	}

	/**
	 * Returns the number of elements in this list or {@code 0}
	 * if it is empty
	 */
	public int size() {
		return list==null ? 0 : list.size();
	}

	private void checkOwner(Object owner) {
		Object realOwner = this.owner==null ? null : this.owner.get();
		if(owner==null || (realOwner!=null && realOwner!=owner))
			throw new IllegalArgumentException("Illegal access attempt - supplied owner object is invalid: "+String.valueOf(owner)); //$NON-NLS-1$
	}

	/**
	 * Replaces the {@code type} and {@code value} objects at the specified
	 * {@code index} by the given arguments.
	 * <p>
	 * Before actual modifications take place the supplied {@code owner}
	 * will be checked against the one that was set at constructor time.
	 * If this internal {@code owner} is {@code null} or the given one
	 * differs from it an {@code IllegalArgumentException} will be thrown.
	 * @param index the index the modifications will take place
	 * @param owner {@code key} to access this restricted method
	 * @param type the new {@code EntryType} or {@code null} if the type
	 * should not be changed
	 * @param value object to replace the old {@code value} or {@code null}
	 * if no changes are intended
	 */
	public void set(int index, Object owner, EntryType type, String value) {
		checkOwner(owner);
		if(list!=null) {
			ListEntry entry = list.get(index);
			entry.type = type==null ? entry.type : type;
			entry.value = value==null ? entry.value : value;
		}
	}

	void add(EntryType type, String value) {
		Exceptions.testNullArgument(type, "type"); //$NON-NLS-1$

		if(list==null) {
			list = new ArrayList<>();
		}

		ListEntry entry = new ListEntry(type, value);
		list.add(entry);
	}

	/**
	 * Adds the {@code type} and {@code value} objects to the end
	 * of this list.
	 * <p>
	 * Before actual modifications take place the supplied {@code owner}
	 * will be checked against the one that was set at constructor time.
	 * If this internal {@code owner} is {@code null} or the given one
	 * differs from it an {@code IllegalArgumentException} will be thrown.
	 * @param owner {@code key} to access this restricted method
	 * @param type the {@code EntryType} to be added, must not be {@code null}
	 * @param value {@code String} to be added, may be {@code null}
	 */
	public void add(Object owner, EntryType type, String value) {
		checkOwner(owner);
		add(type, value);
	}

	/**
	 * Returns the identifier of an {@code Action} that is meant to
	 * be used to {@code activate} this list or {@code null} if this list
	 * is not associated with any action.
	 */
	public String getActionId() {
		return actionId;
	}

	void setActionId(String actionId) {
		this.actionId = actionId;
	}

	/**
	 *
	 * @param owner
	 * @param actionId
	 */
	public void setActionId(Object owner, String actionId) {
		checkOwner(owner);
		setActionId(actionId);
	}

	public String getGroupId(String actionId) {
		return groupMap==null ? null : groupMap.get(actionId);
	}

	void mapGroup(String actionId, String groupId) {
		if(groupMap==null)
			groupMap = new HashMap<>();

		if(groupId==null)
			groupMap.remove(actionId);
		else
			groupMap.put(actionId, groupId);
	}

	public void mapGroup(Object owner, String actionId, String groupId) {
		checkOwner(owner);
		Exceptions.testNullArgument(actionId, "actionId"); //$NON-NLS-1$

		mapGroup(actionId, groupId);
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private static class ListEntry implements Cloneable {
		private EntryType type;
		private String value;

		/**
		 * @param type
		 * @param value
		 */
		ListEntry(EntryType type, String value) {
			this.type = type;
			this.value = value;
		}

		@Override
		public ListEntry clone() {
			return new ListEntry(type, value);
		}

		/**
		 * @return the type
		 */
		public EntryType getType() {
			return type;
		}

		/**
		 * @return the value
		 */
		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.format("type: %s value: %s", type, value); //$NON-NLS-1$
		}
	}

	/**
	 * Type definitions used for entries in an {@link ActionList}.
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public enum EntryType {

		/**
		 * Marks the separation of two elements in the list. Members of the
		 * framework will honor this type by using one of the various
		 * {@code addSeparator()} methods in the {@code Swing} classes or
		 * add a new instance of {@link JSeparator} depending on the {@code value}
		 * of the entry.
		 */
		SEPARATOR,

		/**
		 * Links an entry to another {@code ActionList} instance. An implementation
		 * specific component (typically a button) will be placed at the
		 * corresponding index that expands the referenced list when clicked.
		 */
		ACTION_LIST_ID,

		/**
		 * Points to a collection of {@code Action}s encapsulated in
		 * an {@code ActionSet}. Each element of this collection will be
		 * added in sequential order.
		 */
		ACTION_SET_ID,

		/**
		 * References a single action to be added.
		 */
		ACTION_ID,

		/**
		 * Makes the framework insert a {@code JLabel} that will be
		 * localized using the corresponding {@code value} as key to obtain
		 * the localized {@code String} for {@link JLabel#setText(String)}.
		 */
		LABEL,

		/**
		 * Inserts an implementation specific placeholder that typically
		 * is roughly the same size as a regular action component for the
		 * current container. It is possible to assign a specific size value
		 * that determines either the width or height of the inserted component
		 * depending on the type of action component the containing list
		 * is converted into.
		 */
		EMPTY,

		/**
		 * Inserts an implementation specific 'glue' component that consumes
		 * free space when available. Note that typically only tool-bar
		 * components support such behavior.
		 */
		GLUE,

		/**
		 * Mightiest type to assign to an entry.
		 * <p>
		 * When asked to build action based components the framework can
		 * be supplied a {@code Map} of properties. For each {@code placeholder}
		 * encountered this map will be queried with the actual {@code value}
		 * of the entry as key. When not {@code null} the result will be
		 * handled in the following way:
		 * <ul>
		 * 	<li>if it is a {@code String} then it will be handled as {@link EntryType#LABEL}</li>
		 * 	<li>if it is an {@link Action} then it will be added directly</li>
		 * 	<li>if it is an {@link ActionSet} all its elements will be added sequentially</li>
		 * 	<li>if it is an {@link ActionList} the framework will either wrap the
		 * 		list into a new implementation specific {@code Component} and add
		 * 		an {@code Action} responsible for showing this component or it will
		 * 		"inline" the list into the current construction process</li>
		 * 	<li>if it is a {@link Component} it will be added directly (some members
		 * 		of the framework might resize the component to fit their requirements</li>
		 *  <li>all other types are ignored</li>
		 * </ul>
		 */
		CUSTOM;


		public static EntryType parse(String text) {
			Exceptions.testNullArgument(text, "text"); //$NON-NLS-1$
			switch(text) {
			case "separator": return SEPARATOR; //$NON-NLS-1$
			case "action-list": return ACTION_LIST_ID; //$NON-NLS-1$
			case "action-set": return ACTION_SET_ID; //$NON-NLS-1$
			case "action": return ACTION_ID; //$NON-NLS-1$
			case "label": return LABEL; //$NON-NLS-1$
			case "custom": return CUSTOM; //$NON-NLS-1$
			case "empty": return SEPARATOR; //$NON-NLS-1$
			case "glue": return GLUE; //$NON-NLS-1$
			default:
				throw new IllegalArgumentException("Unknown entry-type: "+text); //$NON-NLS-1$
			}
		}
	}
}
