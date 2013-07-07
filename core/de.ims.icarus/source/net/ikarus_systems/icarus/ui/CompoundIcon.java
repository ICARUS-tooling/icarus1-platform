/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/ui/CompoundIcon.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.ui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import net.ikarus_systems.icarus.util.Exceptions;

/**
 * @author Markus Gärtner
 * @version $Id: CompoundIcon.java 7 2013-02-27 13:18:56Z mcgaerty $
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
			throw new IllegalArgumentException("Invalid corner for overlay icon: "+corner); //$NON-NLS-1$
		
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
