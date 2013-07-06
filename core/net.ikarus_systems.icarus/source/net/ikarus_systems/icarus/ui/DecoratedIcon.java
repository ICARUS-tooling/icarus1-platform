/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/ui/DecoratedIcon.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

import net.ikarus_systems.icarus.util.Exceptions;

/**
 * Implements an {@code Icon} that can have arbitrary decorative
 * icons attached to it. The decorations are grouped into 3 layers
 * that are painted on top of each other so that icons in the higher
 * layers obscure those in lower ones.
 * <p>
 * Note that this class is not thread-safe since all modifications should
 * occur on the event dispatch thread only!
 * 
 * @author Markus Gärtner
 * @version $Id: DecoratedIcon.java 7 2013-02-27 13:18:56Z mcgaerty $
 *
 */
public class DecoratedIcon implements Icon {
	
	public static final int BACKGROUND_LAYER = 0;
	public static final int DEFAULT_LAYER = 1;
	public static final int OVERLAY_LAYER = 2;
	
	private Icon baseIcon;
	private Dimension maxSize;
	
	private Decoration[][] decorations;
	private int[] decorationCounts;
	
	private BufferedImage buffer;
	
	public DecoratedIcon(Icon baseIcon) {
		this(baseIcon, 64, 32);
	}

	public DecoratedIcon(Icon baseIcon, int width, int height) {
		Exceptions.testNullArgument(baseIcon, "baseIcon"); //$NON-NLS-1$
		
		this.baseIcon = baseIcon;
		this.maxSize = new Dimension(width, height);
		buffer = null;
	}
	
	public void setBaseIcon(Icon baseIcon) {
		Exceptions.testNullArgument(baseIcon, "baseIcon"); //$NON-NLS-1$
		
		this.baseIcon = baseIcon;
		
		rebuildBuffer();
	}
	
	public void setMaxSize(int width, int height) {
		if(maxSize==null) {
			maxSize = new Dimension(width, height);
		} else {
			maxSize.setSize(width, height);
		}
	}
	
	private void rebuildBuffer() {
		if(decorations==null || decorationCounts==null) {
			return;
		}
		
		int width = baseIcon.getIconWidth();
		int height = baseIcon.getIconHeight();
		BufferedImage image = new BufferedImage(maxSize.width, maxSize.height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics g = image.createGraphics();
		
		loop_layers : for(int layer=0; layer<decorationCounts.length; layer++) {
			if(decorations[layer]==null) {
				continue loop_layers;
			}
			
			int count = decorationCounts[layer];
			loop_decorations : for(int i=0; i<count; i++) {
				Decoration deco = decorations[layer][i];
				if(deco==null) {
					continue loop_decorations;
				}
				
				deco.icon.paintIcon(null, g, deco.x, deco.y);
				
				width = Math.max(width, deco.x+deco.icon.getIconWidth());
				height = Math.max(height, deco.y+deco.icon.getIconHeight());
			}
		}
		
		buffer = image.getSubimage(0, 0, width, height);
	}

	public void addBackgroundDecoration(Icon icon, int x, int y) {
		addDecoration(BACKGROUND_LAYER, icon, x, y);
	}

	public void addOverlayDecoration(Icon icon, int x, int y) {
		addDecoration(OVERLAY_LAYER, icon, x, y);
	}

	public void addDecoration(Icon icon, int x, int y) {
		addDecoration(DEFAULT_LAYER, icon, x, y);
	}
	
	public void addDecoration(int layer, Icon icon, int x, int y) {
		Exceptions.testNullArgument(icon, "icon"); //$NON-NLS-1$
		if(layer<0 || layer>2)
			throw new IllegalArgumentException("Invalid layer: "+layer); //$NON-NLS-1$
		
		if(decorations==null) {
			decorations = new Decoration[3][];
		}
		
		if(decorationCounts==null) {
			decorationCounts = new int[]{0, 0, 0};
		}
		
		if(decorations[layer]==null) {
			decorations[layer] = new Decoration[5];
		}
		
		Decoration[] layerItems = decorations[layer];
		int index = decorationCounts[layer];
		if(index>=layerItems.length) {
			Decoration[] newList = new Decoration[index*2];
			System.arraycopy(layerItems, 0, newList, 0, layerItems.length);
			decorations[layer] = newList;
			layerItems = newList;
		}
		
		layerItems[index] = new Decoration(icon, x, y);
		decorationCounts[layer]++;
		
		rebuildBuffer();
	}
	
	public void removeDecoration(int layer, Icon icon, int x, int y) {
		Exceptions.testNullArgument(icon, "icon"); //$NON-NLS-1$
		if(layer<0 || layer>2)
			throw new IllegalArgumentException("Invalid layer: "+layer); //$NON-NLS-1$
		
		if(decorations==null || decorations[layer]==null || decorationCounts==null) {
			return;
		}
		
		int count = decorationCounts[layer];
		Decoration[] layerItems = decorations[layer];
		for(int i=0; i<count; i++) {
			if(layerItems[i].icon==icon && layerItems[i].x==x && layerItems[i].y==y) {
				count--;
				for(int j=i; j<count; j++)
					layerItems[j] = layerItems[j+1];
			}
		}
		decorationCounts[layer] = count;
		
		rebuildBuffer();
	}
	
	public void removeDecorations(int layer) {
		if(layer<0 || layer>2)
			throw new IllegalArgumentException("Invalid layer: "+layer); //$NON-NLS-1$
		
		if(decorations==null || decorations[layer]==null || decorationCounts==null) {
			return;
		}
		
		decorations[layer] = null;
		decorationCounts[layer] = 0;
		
		rebuildBuffer();
	}
	
	public void removeDecorations() {
		decorations = null;
		decorationCounts = null;
		buffer = null;
	}

	/**
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if(buffer==null) {
			baseIcon.paintIcon(c, g, x, y);
		} else {
			g.drawImage(buffer, x, y, buffer.getWidth(), buffer.getHeight(), c);
		}
	}

	/**
	 * @see javax.swing.Icon#getIconWidth()
	 */
	@Override
	public int getIconWidth() {
		return buffer==null ? baseIcon.getIconWidth() : buffer.getWidth();
	}

	/**
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
	public int getIconHeight() {
		return buffer==null ? baseIcon.getIconHeight() : buffer.getHeight();
	}

	private static class Decoration {
		private final Icon icon;
		private final int x;
		private final int y;

		Decoration(Icon icon, int x, int y) {
			this.icon = icon;
			this.x = x;
			this.y = y;
		}
	}
}
