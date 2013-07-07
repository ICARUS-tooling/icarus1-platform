/*
 * $Revision: 29 $
 * $Date: 2013-05-03 20:03:21 +0200 (Fr, 03 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/view/TextRenderer.java $
 *
 * $LastChangedDate: 2013-05-03 20:03:21 +0200 (Fr, 03 Mai 2013) $ 
 * $LastChangedRevision: 29 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.SwingConstants;

/**
 * @author Markus Gärtner
 * @version $Id: TextRenderer.java 29 2013-05-03 18:03:21Z mcgaerty $
 *
 */
public class TextRenderer {
	
	private Font font;
	private Rectangle bounds;
	private int lineSpacing = 3;
	private boolean antiAliasing = true;
	private boolean verticalStretch = false;
	
	private List<Line> lines;
	
	private LineBuffer buffer;
	
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
		// no-op
	}
	
	public Rectangle getBounds() {
		recalc();
		return bounds;
	}
	
	private void recalc() {
		if(bounds!=null) {
			Rectangle r = new Rectangle();
			
			if(lines!=null && !lines.isEmpty()) {
				for(Line line : lines) {
					r.height += line.getTotalHeight();
					r.width = Math.max(r.width, line.getTotalWidth());
				}
			}
						
			bounds = r;
		}
	}
	
	public void paint(Graphics gr, int x, int y, int width, int height) {
		if(lines==null || lines.isEmpty()) {
			return;
		}		
	
		Graphics2D g = (Graphics2D) gr.create();
		if(font!=null) {
			g.setFont(font);
		}
		if(antiAliasing) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		
		Rectangle clip = g.getClipBounds();
		Rectangle lineBounds = new Rectangle(getBounds());
		
		int lineHeight = -1;
		if(verticalStretch) {
			int totalHeight = lineBounds.height - (lines.size()-1)*lineSpacing; 
			lineHeight = totalHeight / lines.size();
		}
		
		for(int i=0; i<lines.size(); i++) {
			Line line = lines.get(i);
			
			// Shift to current line origin
			lineBounds.x = x;
			lineBounds.y = y;
			
			// Apply spacing
			if(i>0) {
				lineBounds.y += lineSpacing;
			}
			
			// Determine current line height
			lineBounds.height = Math.max(lineHeight, line.getTotalHeight());
			
			// Not in clipping area -> end rendering
			// FIXME there seemed to be a bug in the clipping area used by
			// jgraph canvas renderers, needs further investigation
			if(clip!=null && !clip.intersects(lineBounds)) {
				break;
			}
			
			line.paint(g, lineBounds.x, lineBounds.y, lineBounds.width, lineBounds.height);
			
			// Increment line origin (ONLY y)
			y += lineBounds.height;
		}
		
		g.dispose();
	}

	public boolean isAntiAliasing() {
		return antiAliasing;
	}

	public void setAntiAliasing(boolean antiAliasing) {
		this.antiAliasing = antiAliasing;
	}

	public boolean isVerticalStretch() {
		return verticalStretch;
	}

	public void setVerticalStretch(boolean verticalStretch) {
		this.verticalStretch = verticalStretch;
	}

	public Font getFont() {
		return font;
	}

	public int getLineSpacing() {
		return lineSpacing;
	}

	public void setFont(Font font) {
		this.font = font;
		bounds = null;
	}

	public void setLineSpacing(int lineSpacing) {
		this.lineSpacing = lineSpacing;
		bounds = null;
	}
	
	public void addBlock(Block block) {
		feedBlock(block, false);
	}
	
	public void newLine() {
		feedBlock(null, true);
	}
	
	public void addText(String text) {
		feedBlock(new TextBlock(text, null, null, -1, -1), false);
	}
	
	public void addText(String text, Color color) {
		feedBlock(new TextBlock(text, null, color, -1, -1), false);
	}
	
	public void addText(String text, Font font) {
		feedBlock(new TextBlock(text, font, null, -1, -1), false);
	}
	
	public void addText(String text, Font font, Color color) {
		feedBlock(new TextBlock(text, font, color, -1, -1), false);
	}
	
	public void addText(String text, Font font, Color color, int width, int height) {
		feedBlock(new TextBlock(text, font, color, width, height), false);
	}
	
	public void addSpace(int width, int height) {
		feedBlock(new EmptyBlock(width, height), false);
	}
	
	public void addIcon(Icon icon) {
		feedBlock(new ImageBlock(icon, -1, -1), false);
	}
	
	public void addIcon(Icon icon, int width, int height) {
		feedBlock(new ImageBlock(icon, width, height), false);
	}
	
	private void addLine(Line line) {
		if(line==null) {
			return;
		}
		if(lines==null) {
			lines = new ArrayList<>();
		}
		lines.add(line);
		bounds = null;
	}
	
	private void feedBlock(Block block, boolean wrap) {
		if(buffer==null) {
			buffer = new LineBuffer();
		}
		
		if(block!=null) {
			buffer.addBlock(block);
		}
		
		if(wrap) {
			addLine(buffer.toLine());
		}
	}
	
	private class LineBuffer {
		int width = 0;
		int height = 0;
		
		List<Block> blocks;
		
		void addBlock(Block block) {
			if(blocks==null) {
				blocks = new LinkedList<>();
			}
			
			blocks.add(block);
			
			width += block.getWidth();
			height = Math.max(height, block.getHeight());
		}
		
		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		void reset() {
			width = height = 0;
			if(blocks!=null) {
				blocks.clear();
			}
		}
		
		Line toLine() {
			Line line = null;
			if(blocks!=null && !blocks.isEmpty()) {
				line = new Line(blocks.toArray(new Block[blocks.size()]));
			}
			
			reset();
			
			return line;
		}
	}
	
	private class Line {
		final Block[] blocks;
		int[] widths;
		int[] heights;
		
		int totalWidth;
		int totalHeight;
		
		int alignment;
		
		Line(Block[] blocks) {
			this.blocks = blocks;
		}
		
		private void recalc() {
			if(widths==null) {
				totalHeight = 0;
				totalWidth = 0;
				
				widths = new int[blocks.length];
				heights = new int[blocks.length];
				// Calculate required space for our line
				for(int i=blocks.length-1; i>-1; i--) {
					Block b = blocks[i];
					widths[i] = b.getWidth();
					heights[i] = b.getHeight();
					
					totalWidth += widths[i];
					totalHeight = Math.max(totalHeight, heights[i]);
				}
			}
		}
		
		int getTotalHeight() {
			recalc();
			
			return totalHeight;
		}
		
		int getTotalWidth() {
			recalc();
			
			return totalWidth;
		}

		void paint(Graphics g, int x, int y, int width, int height) {
			recalc();
			
			// Skip rendering if we have no vertical space left
			if(totalHeight>height) {
				return;
			}
			
			// Only bother with alignment if we fit into the provided 'bounds'
			if(totalWidth < width) {
				// Centered
				if(alignment==SwingConstants.CENTER 
						|| alignment==SwingConstants.NORTH 
						|| alignment==SwingConstants.SOUTH) {
					// Shift everything to the center
					x += (width-totalWidth)*0.5;
				}
				// Right aligned
				else if(alignment==SwingConstants.EAST 
						|| alignment==SwingConstants.NORTH_EAST
						|| alignment==SwingConstants.SOUTH_EAST) {
					x = x+width-totalWidth;
				}
			}
			
			if(totalHeight<height) {
				// Centered
				if(alignment==SwingConstants.CENTER 
						|| alignment==SwingConstants.EAST 
						|| alignment==SwingConstants.WEST) {
					// Shift everything to the center
					y += (height-totalHeight)*0.5;
				}
				// Bottom aligned
				else if(alignment==SwingConstants.SOUTH 
						|| alignment==SwingConstants.SOUTH_EAST
						|| alignment==SwingConstants.SOUTH_WEST) {
					y = y+height-totalHeight;
				}
			}
			
			// Render all blocks
			for(int i=0; i<blocks.length; i++) {
				Block b = blocks[i];
				b.paint(g, x, y, widths[i], heights[i]);
				
				x += widths[i];
			}
		}
	}

	private interface Block {
		int getWidth();
		int getHeight();
		void paint(Graphics g, int x, int y, int width, int height);
	}
	
	private class EmptyBlock implements Block {
		
		private final int height, width;

		/**
		 * @param width
		 * @param height
		 */
		public EmptyBlock(int width, int height) {
			this.width = width;
			this.height = height;
		}

		/**
		 * @see de.ims.icarus.ui.view.TextRenderer.Block#getWidth()
		 */
		@Override
		public int getWidth() {
			return width;
		}

		/**
		 * @see de.ims.icarus.ui.view.TextRenderer.Block#getHeight()
		 */
		@Override
		public int getHeight() {
			return height;
		}

		/**
		 * @see de.ims.icarus.ui.view.TextRenderer.Block#paint(java.awt.Graphics, int, int, int, int)
		 */
		@Override
		public void paint(Graphics g, int x, int y, int width, int height) {
			// no-op
		}
		
	}
	
	private class TextBlock implements Block {
		
		private String text;
		private Font font;
		private Color color;
		private int width, height;
		
		TextBlock(String text, Font font, Color color, int width, int height) {
			this.text = text;
			this.font = font;
			this.color = color;
			this.width = width;
			this.height = height;
		}

		private Font getFont() {
			if(font!=null) {
				return font;
			}
			return TextRenderer.this.font;
		}

		@Override
		public int getWidth() {
			if(text==null || text.isEmpty()) {
				return 0;
			}
			if(width!=-1) {
				return width;
			}
			Font font = getFont();
			if(font==null) {
				return -1;
			}
			return getDummyComponent().getFontMetrics(font).stringWidth(text);
		}

		@Override
		public int getHeight() {
			if(text==null || text.isEmpty()) {
				return 0;
			}
			if(height!=-1) {
				return height;
			}
			Font font = getFont();
			if(font==null) {
				return -1;
			}
			return getDummyComponent().getFontMetrics(font).getHeight();
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
		private int width, height;

		ImageBlock(Icon icon, int width, int height) {
			this.icon = icon;
			this.width = width;
			this.height = height;
		}

		@Override
		public int getWidth() {
			return width==-1 ? icon.getIconWidth() : width;
		}

		@Override
		public int getHeight() {
			return height==-1 ? icon.getIconHeight() : height;
		}

		@Override
		public void paint(Graphics g, int x, int y, int w, int h) {
			int iwidth = icon.getIconWidth();
			int iheight = icon.getIconHeight();
			
			if(iwidth<w) {
				x+= (w-iwidth)*0.5;
			}

			if(iheight<h) {
				y+= (h-iheight)*0.5;
			}
			
			icon.paintIcon(getDummyComponent(), g, x, y);
		}
		
	}
}
