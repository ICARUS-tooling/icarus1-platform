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
package de.ims.icarus.ui.table;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import de.ims.icarus.resources.ResourceManager;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ColumnInfo {
	
	@XmlAttribute(name="active")
	private boolean active = true;
	
	@XmlAttribute(name="key")
	private String key;

	@XmlAttribute(name="resizable")
	private boolean resizable = false;

	@XmlAttribute(name="minWidth")
	private int minWidth;

	@XmlAttribute(name="maxWidth")
	private int maxWidth;

	@XmlAttribute(name="preferredWidth")
	private int preferredWidth;

	@XmlAttribute(name="required")
	private boolean required = false;
	
	public ColumnInfo() {
		// no-op
	}
	
	public ColumnInfo(String key) {
		this(key, true);
	}
	
	public ColumnInfo(String key, boolean active) {
		setKey(key);
		setActive(active);
	}
	
	public ColumnInfo(String key, boolean active, int min, int max, int pref, boolean resizable, boolean required) {
		this(key, active);
		
		minWidth = min;
		maxWidth = max;
		preferredWidth = pref;
		this.resizable = resizable;
		this.required = required;
	}
	
	@Override
	public String toString() {
		return key==null ? super.toString() : ResourceManager.getInstance().get(key);
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		if(key==null)
			throw new IllegalArgumentException("Invalid key"); //$NON-NLS-1$
		
		this.key = key;
	}

	/**
	 * @return the resizable
	 */
	public boolean isResizable() {
		return resizable;
	}

	/**
	 * @return the minWidth
	 */
	public int getMinWidth() {
		return minWidth;
	}

	/**
	 * @return the maxWidth
	 */
	public int getMaxWidth() {
		return maxWidth;
	}

	/**
	 * @return the preferredWidth
	 */
	public int getPreferredWidth() {
		return preferredWidth;
	}

	/**
	 * @param resizable the resizable to set
	 */
	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	/**
	 * @param minWidth the minWidth to set
	 */
	public void setMinWidth(int minWidth) {
		this.minWidth = minWidth;
	}

	/**
	 * @param maxWidth the maxWidth to set
	 */
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	/**
	 * @param preferredWidth the preferredWidth to set
	 */
	public void setPreferredWidth(int preferredWidth) {
		this.preferredWidth = preferredWidth;
	}

	/**
	 * @return the required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param required the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

}
