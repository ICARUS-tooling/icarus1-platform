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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ims.icarus.util.Exceptions;


/**
 * A logical grouping of actions identified by their 'ids'.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ActionSet {
	
	private final String id;	
	private final WeakReference<Object> owner;
	private List<String> actionIds;
	private Map<String, String> groupMap;
	
	public ActionSet(String id) {
		this(id, null);
	}
	
	public ActionSet(String id, Object owner) {
		Exceptions.testNullArgument(id, "id"); //$NON-NLS-1$
		
		this.id = id;
		
		this.owner = owner==null ? null : new WeakReference<Object>(owner);
	}
	
	private void checkOwner(Object owner) {
		Object realOwner = this.owner==null ? null : this.owner.get();
		if(owner==null || (realOwner!=null && realOwner!=owner))
			throw new IllegalArgumentException("Illegal access attempt - supplied owner object is invalid: "+String.valueOf(owner)); //$NON-NLS-1$
	}

	public String getId() {
		return id;
	}
	
	public String[] getActionIds() {
		if(actionIds==null) {
			return new String[0];
		}
		return actionIds.toArray(new String[actionIds.size()]);
	}
	
	public String getActionIdAt(int index) {
		return actionIds==null ? null : actionIds.get(index);
	}
	
	public boolean isEmpty() {
		return actionIds==null ? true : actionIds.isEmpty();
	}
	
	public int size() {
		return actionIds==null ? 0 : actionIds.size();
	}
	
	public boolean contains(String actionId) {
		return actionIds==null ? false : actionIds.contains(actionId);
	}
	
	void add(String actionId, String groupId) {
		Exceptions.testNullArgument(actionId, "actionId"); //$NON-NLS-1$
		if(actionIds==null) {
			actionIds = new ArrayList<>();
		}
		
		actionIds.add(actionId);
		if(groupId!=null) {
			mapGroup(actionId, groupId);
		}
	}
	
	public void add(Object owner, String actionId, String groupId) {
		checkOwner(owner);
		add(actionId, groupId);
	}
	
	public String getGroupId(String actionId) {
		return groupMap==null ? null : groupMap.get(actionId);
	}
	
	void mapGroup(String actionId, String groupId) {
		if(groupMap==null) {
			groupMap = new HashMap<>();
		}
			
		if(groupId==null) {
			groupMap.remove(actionId);
		} else {
			groupMap.put(actionId, groupId);
		}
	}
	
	public void mapGroup(Object owner, String actionId, String groupId) {
		checkOwner(owner);
		Exceptions.testNullArgument(actionId, "actionId"); //$NON-NLS-1$
		
		mapGroup(actionId, groupId);
	} 
}
