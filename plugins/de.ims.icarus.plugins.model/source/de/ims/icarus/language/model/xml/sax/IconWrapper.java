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
package de.ims.icarus.language.model.xml.sax;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import de.ims.icarus.language.model.xml.XmlResource;
import de.ims.icarus.ui.IconRegistry;

public class IconWrapper implements Icon, XmlResource {
	private Icon source;
	private final String name;

	public IconWrapper(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		this.name = name;
	}

	/**
	 * @see de.ims.icarus.language.model.xml.XmlResource#getValue()
	 */
	@Override
	public String getValue() {
		return name;
	}

	private Icon getSource() {
		if(source==null) {
			source = IconRegistry.getGlobalRegistry().getIcon(name);
		}
		return source;
	}

	/**
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if(getSource()!=null) {
			source.paintIcon(c, g, x, y);
		}
	}

	/**
	 * @see javax.swing.Icon#getIconWidth()
	 */
	@Override
	public int getIconWidth() {
		return getSource()==null ? 0 : source.getIconWidth();
	}

	/**
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
	public int getIconHeight() {
		return getSource()==null ? 0 : source.getIconHeight();
	}
}