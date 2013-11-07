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

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.id.DuplicateIdentifierException;


/**
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class Actions {

	private final TreeMap<String, Action> actions = new TreeMap<String, Action>();
	private Actions parent;
	
	public Actions() {
		this(globalActions);
	}
	
	public Actions(Actions parent) {
		this.parent = parent;
	}

	public void addActions(Actions a) {
		actions.putAll(a.actions);
	}

	public void addAction(String id, Action action) {
		if (actions.containsKey(id))
			throw new DuplicateIdentifierException(id);
		actions.put(id, action);
	}

	/**
	 * Adds the provided {@code action} and uses the global {@code ResourceDomain}
	 * of the {@code ResourceManager} to prepare and finally add it to localization
	 * control using the given {@code nameKey} and {@code descKey}
	 * @param id
	 * @param action
	 * @param nameKey
	 * @param descKey
	 */
	public void addAction(String id, Action action, String nameKey,
			String descKey) {
		addAction(id, action);
		ResourceManager.getInstance().getGlobalDomain().prepareAction(action, nameKey, descKey);
		ResourceManager.getInstance().getGlobalDomain().addAction(action);
	}

	public Action getAction(String id) {
		Action action = actions.get(id);
		if(action==null && parent!=null)
			action = parent.getAction(id);
		
		return action;
	}

	public Map<String, Action> getActionGroup(String prefix) {
		if (prefix != null && !prefix.isEmpty()) {
			return actions.subMap(prefix + ".a", prefix + ".z"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return actions;
	}

	public String[] getActionIDs() {
		return actions.entrySet().toArray(new String[0]);
	}

	public void remove(String... ids) {
		for (String id : ids)
			actions.remove(id);
	}

	public void enable(String... ids) {
		setEnabled(true, ids);
	}

	public void disable(String... ids) {
		setEnabled(false, ids);
	}

	public void setEnabled(boolean enabled, String... ids) {
		Action a;
		for (String id : ids) {
			a = getAction(id);
			if (a != null && a.isEnabled() != enabled)
				a.setEnabled(enabled);
		}
	}

	public void setAllEnabled(boolean enabled) {
		for (Iterator<Entry<String, Action>> i = actions.entrySet().iterator(); i
				.hasNext();) {
			i.next().getValue().setEnabled(enabled);
		}
	}

	public void copyTo(ActionMap map) {
		Entry<String, Action> entry;
		for (Iterator<Entry<String, Action>> i = actions.entrySet().iterator(); i
				.hasNext();) {
			entry = i.next();
			map.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * @return the parent
	 */
	public Actions getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Actions parent) {
		this.parent = parent;
	}

	public static final Actions globalActions = new Actions(null);

	public static final Actions emptyActions = new Actions(null);

	public static final Action dummyAction = new AbstractAction() {
		private static final long serialVersionUID = -4725674994952709073L;

		@Override
		public void actionPerformed(ActionEvent e) {
			// no-op
		}

	};
}
