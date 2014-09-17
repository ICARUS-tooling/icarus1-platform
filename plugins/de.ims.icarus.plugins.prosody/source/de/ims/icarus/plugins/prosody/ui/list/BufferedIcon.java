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
package de.ims.icarus.plugins.prosody.ui.list;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

public class BufferedIcon implements Icon {
	private BufferedImage image;
	private int width = 0, height = 0;

	public BufferedIcon() {
		this(500, 9);
	}

	public BufferedIcon(int width, int height) {
		setSize(width, height);
	}

	public void setSize(int newWidth, int newHeight) {
		if(newWidth>width || newHeight>height) {
			image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		}

		height = newHeight;
		width = newWidth;
	}

	public Graphics2D getGraphics() {
		if(image==null)
			throw new IllegalStateException("Cannot create graphics context if image size is still unspecified"); //$NON-NLS-1$

		return image.createGraphics();
	}

	/**
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if(image!=null) {
			g.drawImage(image, x, y, width, height, c);
		}
	}

	/**
	 * @see javax.swing.Icon#getIconWidth()
	 */
	@Override
	public int getIconWidth() {
		return width;
	}

	/**
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
	public int getIconHeight() {
		return height;
	}
}