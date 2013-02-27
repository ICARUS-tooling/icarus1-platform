/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TextRenderer {
	
	private Font font;
	private Rectangle clip;
	private Rectangle bounds;
	private int lineSpacing;
	private boolean antiAliasing = true;
	
	private static Component dummyComponent;
	
	@SuppressWarnings("serial")
	private static Component getDummyComponent() {
		if(dummyComponent==null) {
			synchronized (TextRenderer.class) {
				if(dummyComponent==null) {
					dummyComponent = new Component(){};
				}
			}
		}
		return dummyComponent;
	}

	public TextRenderer() {
	}
	
	public void paint(Graphics g, int x, int y) {
		
	}

	private interface Block {
		int getWidth();
		int getHeight();
		void paint(Graphics g, int x, int y, int width, int height);
	}
	
	private class TextBlock implements Block {
		
		private String text;
		private Font font;
		private int width, height;
		private Color color;

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public void paint(Graphics g, int x, int y, int width, int height) {
			Font f = null;
			Color c = null;
			
			if(font!=null) {
				f = g.getFont();
				g.setFont(font);
			}
			if(color!=null) {
				c = g.getColor();
				g.setColor(color);
			}
			
			g.drawString(text, x, y);
			
			
			if(f!=null) {
				g.setFont(f);
			}
			if(c!=null) {
				g.setColor(c);
			}
		}		
	}
	
	private class ImageBlock implements Block {
		
		private Icon icon;

		@Override
		public int getWidth() {
			return icon.getIconWidth();
		}

		@Override
		public int getHeight() {
			return icon.getIconHeight();
		}

		@Override
		public void paint(Graphics g, int x, int y, int width, int height) {
			int iwidth = icon.getIconWidth();
			int iheight = icon.getIconHeight();
			
			if(iwidth<width) {
				x+= (width-iwidth)*0.5;
			}

			if(iheight<height) {
				y+= (height-iheight)*0.5;
			}
			
			icon.paintIcon(getDummyComponent(), g, x, y);
		}
		
	}
}
