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
package de.ims.icarus.plugins.prosody.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TextArea implements SwingConstants {

	public static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12); //$NON-NLS-1$
	public static final Color DEFAULT_TEXT_COLOR = Color.black;
	public static final int DEFAULT_HORIZONTAL_ALIGNMENT = CENTER;
	public static final int DEFAULT_VERTICAL_ALIGNMENT = CENTER;
	public static final boolean DEFAULT_VERTICAL = false;

	private int horizontalAlignment = DEFAULT_HORIZONTAL_ALIGNMENT;
	private int verticalAlignment = DEFAULT_VERTICAL_ALIGNMENT;
	private boolean vertical = DEFAULT_VERTICAL;
	private Insets insets;

	private Font font = DEFAULT_FONT;
	private Font font90;
	private Color textColor = DEFAULT_TEXT_COLOR;

	private static final Insets NO_INSETS = new Insets(0, 0, 0, 0);

	private static final AffineTransform ROTATE_90 = new AffineTransform();
	static {
		ROTATE_90.rotate(-Math.PI / 2);
	}

	private Font getRotatedFont() {
		if(font90==null) {
			font90 = font.deriveFont(ROTATE_90);
		}
		return font90;
	}

	public Dimension getSize(JComponent c, String[] lines) {
		return getSize(c, lines, new Dimension());
	}

	public Dimension getSize(JComponent c, String[] lines, Dimension d) {
		if(d==null) {
			d = new Dimension();
		}

		// Make sure to reset size
		d.height = d.width = 0;

		if(lines==null || lines.length==0) {
			return d;
		}

		FontMetrics fm = c.getFontMetrics(font);

		d.height = lines.length * fm.getHeight();

		for(String s : lines) {
			int sw = fm.stringWidth(s);
			d.width = Math.max(d.width, sw);
		}

		if(vertical) {
			int w = d.width;
			d.width = d.height;
			d.height = w;
		}

		if(insets!=null) {
			d.height += insets.top + insets.bottom;
			d.width += insets.left + insets.right;
		}

		return d;
	}

	public void paint(Graphics g, String[] lines, Rectangle area) {
		if(lines==null || lines.length==0) {
			return;
		}

		Color c = g.getColor();
		Font f = g.getFont();

		g.translate(area.x, area.y);
		g.setColor(textColor);
		g.setFont(font);

		FontMetrics fm = g.getFontMetrics();
		Insets insets = this.insets;
		if(insets==null) {
			insets = NO_INSETS;
		}

		if(vertical) {
			//TODO
		} else {

			int xr = insets.left;
			int yr = insets.top;
			int wr = area.width - insets.left - insets.right;
			int hr = area.height - insets.top - insets.bottom;

			int hl = lines.length * fm.getHeight();

			if(verticalAlignment==CENTER) {
				yr += (hr-hl)/2;
			} else if(verticalAlignment==BOTTOM) {
				yr += hr-hl;
			}

			int y = yr;
			for(String line : lines) {
				if(line!=null && !line.isEmpty()) {
					int sw = fm.stringWidth(line);

					int x = xr;

					if(horizontalAlignment==CENTER) {
						x += (wr-sw) / 2;
					} else if(horizontalAlignment==RIGHT) {
						x += wr-sw;
					}

					g.drawString(line, x, y+fm.getAscent());
				}

				y += fm.getHeight();
			}
		}

		g.translate(-area.x, -area.y);
		g.setFont(f);
		g.setColor(c);
	}

	/**
	 * @return the horizontalAlignment
	 */
	public int getHorizontalAlignment() {
		return horizontalAlignment;
	}

	/**
	 * @return the verticalAlignment
	 */
	public int getVerticalAlignment() {
		return verticalAlignment;
	}

	/**
	 * @return the vertical
	 */
	public boolean isVertical() {
		return vertical;
	}

	/**
	 * @return the insets
	 */
	public Insets getInsets() {
		return insets;
	}

	public int getTopInsets() {
		return insets==null ? 0 : insets.top;
	}

	public int getLeftInsets() {
		return insets==null ? 0 : insets.left;
	}

	public int getRightInsets() {
		return insets==null ? 0 : insets.right;
	}

	public int getBottomInsets() {
		return insets==null ? 0 : insets.bottom;
	}

	/**
	 * @return the font
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * @return the textColor
	 */
	public Color getTextColor() {
		return textColor;
	}

	/**
	 * @param horizontalAlignment the horizontalAlignment to set
	 */
	public void setHorizontalAlignment(int horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	/**
	 * @param verticalAlignment the verticalAlignment to set
	 */
	public void setVerticalAlignment(int verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}

	/**
	 * @param vertical the vertical to set
	 */
	public void setVertical(boolean vertical) {
		this.vertical = vertical;
	}

	/**
	 * @param insets the insets to set
	 */
	public void setInsets(Insets insets) {
		this.insets = insets;
	}

	/**
	 * @param font the font to set
	 */
	public void setFont(Font font) {
		if (font == null)
			throw new NullPointerException("Invalid font"); //$NON-NLS-1$

		this.font = font;
		font90 = null;
	}

	/**
	 * @param textColor the textColor to set
	 */
	public void setTextColor(Color textColor) {
		if (textColor == null)
			throw new NullPointerException("Invalid textColor");  //$NON-NLS-1$

		this.textColor = textColor;
	}
}
