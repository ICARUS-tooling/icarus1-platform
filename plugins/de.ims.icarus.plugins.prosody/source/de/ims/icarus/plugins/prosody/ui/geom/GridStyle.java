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
package de.ims.icarus.plugins.prosody.ui.geom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.xml.bind.annotation.XmlEnum;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlEnum
public enum GridStyle {

	SOLID_FULL {
		@Override
		public void paintGrid(Graphics graphics, Axis xAxis, Axis yAxis, Color color, int x, int y, int w, int h) {
			Graphics2D g = (Graphics2D)graphics;

			final Color c = g.getColor();
			g.translate(x, y);
			g.setColor(color);

			// Horizontal and vertical lines
			paintLines(graphics, xAxis, yAxis, w, h, true, true);

			g.setColor(c);
			g.translate(-x, -y);
		}
	},

	SOLID_HORIZONTAL {
		@Override
		public void paintGrid(Graphics graphics, Axis xAxis, Axis yAxis, Color color, int x, int y, int w, int h) {
			Graphics2D g = (Graphics2D)graphics;

			final Color c = g.getColor();
			g.translate(x, y);
			g.setColor(color);

			// Horizontal lines
			paintLines(graphics, xAxis, yAxis, w, h, true, false);

			g.setColor(c);
			g.translate(-x, -y);
		}
	},

	SOLID_VERTICAL {
		@Override
		public void paintGrid(Graphics graphics, Axis xAxis, Axis yAxis, Color color, int x, int y, int w, int h) {
			Graphics2D g = (Graphics2D)graphics;

			final Color c = g.getColor();
			g.translate(x, y);
			g.setColor(color);

			// Vertical lines
			paintLines(graphics, xAxis, yAxis, w, h, false, true);

			g.setColor(c);
			g.translate(-x, -y);
		}
	},

	DOTS {
		@Override
		public void paintGrid(Graphics graphics, Axis xAxis, Axis yAxis, Color color, int x, int y, int w, int h) {
			Graphics2D g = (Graphics2D)graphics;

			final Color c = g.getColor();
			g.translate(x, y);
			g.setColor(color);

			double scaleY = h/yAxis.getRange();
			double scaleX = w/xAxis.getRange();

			// Horizontal parts

			int cursorY = h;

			for(;;) {

				cursorY -= (int)(yAxis.getMarkerStepSize() * scaleY);

				if(cursorY<1) {
					break;
				}

				// Vertical parts

				int cursorX = 0;

				for(;;) {

					cursorX += (int)(xAxis.getMarkerStepSize() * scaleX);

					if(cursorX>w-1) {
						break;
					}

					graphics.drawLine(cursorX-1, cursorY, cursorX+1, cursorY);
					graphics.drawLine(cursorX, cursorY-1, cursorX, cursorY+1);
				}
			}

			g.setColor(c);
			g.translate(-x, -y);
		}
	},

	CROSSES {
		@Override
		public void paintGrid(Graphics graphics, Axis xAxis, Axis yAxis, Color color, int x, int y, int w, int h) {
			Graphics2D g = (Graphics2D)graphics;

			final Color c = g.getColor();
			g.translate(x, y);
			g.setColor(color);

			double scaleY = h/yAxis.getRange();
			double scaleX = w/xAxis.getRange();

			// Horizontal parts

			int cursorY = h;

			for(;;) {

				cursorY -= (int)(yAxis.getMarkerStepSize() * scaleY);

				if(cursorY<2) {
					break;
				}

				// Vertical parts

				int cursorX = 0;

				for(;;) {

					cursorX += (int)(xAxis.getMarkerStepSize() * scaleX);

					if(cursorX>w-2) {
						break;
					}

					graphics.drawLine(cursorX-2, cursorY, cursorX+2, cursorY);
					graphics.drawLine(cursorX, cursorY-2, cursorX, cursorY+2);
				}
			}

			g.setColor(c);
			g.translate(-x, -y);
		}
	},

	;

	public abstract void paintGrid(Graphics graphics, Axis xAxis, Axis yAxis, Color color, int x, int y, int w, int h);

	protected void paintLines(Graphics graphics, Axis xAxis, Axis yAxis, int w, int h, boolean horizontal, boolean vertical) {

		if(horizontal) {
			double scaleY = h/yAxis.getRange();

			// Horizontal lines

			int cursorY = h;

			for(;;) {

				cursorY -= (int)(yAxis.getMarkerStepSize() * scaleY);

				if(cursorY<0) {
					break;
				}

				graphics.drawLine(0, cursorY, w, cursorY);
			}
		}

		if(vertical) {
			double scaleX = w/xAxis.getRange();

			// Vertical lines

			int cursorX = 0;

			for(;;) {

				cursorX += (int)(xAxis.getMarkerStepSize() * scaleX);

				if(cursorX>w) {
					break;
				}

				graphics.drawLine(cursorX, 0, cursorX, h);
			}
		}
	}
}
