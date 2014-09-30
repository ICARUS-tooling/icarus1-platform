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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.ui.view;

import de.ims.icarus.util.CompactProperties;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class PartInfo implements Comparable<PartInfo> {

	private CompactProperties properties;
	private int width, x;

	private String label;

	public String getLabel() {
		return label;
	}

	protected void setLabel(String label) {
		this.label = label;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public boolean contains(int offset) {
		return offset>=x && offset<=x+width;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PartInfo o) {
		if(o.x>=x && o.x+o.width<=x+width) {
			return 0;
		}
		return x-o.x;
	}

	public CompactProperties getProperties() {
		return properties;
	}

	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new CompactProperties();
		}
		properties.put(key, value);
	}

	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}
}
