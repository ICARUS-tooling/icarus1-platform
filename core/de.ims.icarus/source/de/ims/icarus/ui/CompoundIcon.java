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
package de.ims.icarus.ui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import de.ims.icarus.util.Exceptions;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CompoundIcon implements Icon {
	
	public static final int TOP_LEFT = 0;
	public static final int TOP_RIGHT = 1;
	public static final int BOTTOM_LEFT = 2;
	public static final int BOTTOM_RIGHT = 3;
	
	private final Icon baseIcon;
	
	private Icon[] overlayIcons;

	/**
	 * @param baseIcon
	 */
	public CompoundIcon(Icon baseIcon) {
		Exceptions.testNullArgument(baseIcon, "baseIcon"); //$NON-NLS-1$
		
		this.baseIcon = baseIcon;
	}

	/**
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		baseIcon.paintIcon(c, g, x, y);
		
		if(overlayIcons==null)
			return;
		
		Icon overlay;
		
		if((overlay=overlayIcons[TOP_LEFT])!=null) {
			overlay.paintIcon(c, g, x, y);
		}
		
		if((overlay=overlayIcons[TOP_RIGHT])!=null) {
			overlay.paintIcon(c, g, x+getIconWidth()-overlay.getIconWidth(), y);
		}
		
		if((overlay=overlayIcons[BOTTOM_LEFT])!=null) {
			overlay.paintIcon(c, g, x, y+getIconHeight()-overlay.getIconHeight());
		}
		
		if((overlay=overlayIcons[BOTTOM_RIGHT])!=null) {
			overlay.paintIcon(c, g, x+getIconWidth()-overlay.getIconWidth(), 
					y+getIconHeight()-overlay.getIconHeight());
		}
	}
	
	public void setTopLeftOverlay(Icon icon) {
		setOverlay(TOP_LEFT, icon);
	}
	
	public void setTopRightOverlay(Icon icon) {
		setOverlay(TOP_RIGHT, icon);
	}
	
	public void setBottomLeftOverlay(Icon icon) {
		setOverlay(BOTTOM_LEFT, icon);
	}
	
	public void setBottomRightOverlay(Icon icon) {
		setOverlay(BOTTOM_RIGHT, icon);
	}
	
	public void setOverlay(int corner, Icon icon) {
		if(corner<0 || corner>3)
			throw new NullPointerException("Invalid corner for overlay icon: "+corner); //$NON-NLS-1$
		
		if(overlayIcons==null)
			overlayIcons = new Icon[4];
		
		overlayIcons[corner] = icon;
	}

	/**
	 * Returns the {@code width} of the {@code baseIcon}
	 * @see javax.swing.Icon#getIconWidth()
	 */
	@Override
	public int getIconWidth() {
		return baseIcon.getIconWidth();
	}

	/**
	 * Returns the {@code height} of the {@code baseIcon}
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
	public int getIconHeight() {
		return baseIcon.getIconHeight();
	}
}
