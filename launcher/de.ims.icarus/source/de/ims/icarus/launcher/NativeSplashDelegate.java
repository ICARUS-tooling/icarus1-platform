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
package de.ims.icarus.launcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.net.URL;

import de.ims.icarus.launcher.SplashWindow.SplashDelegate;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
class NativeSplashDelegate implements SplashDelegate {
	
	private SplashScreen screen;
	private Rectangle textArea;
	private Rectangle progressArea;
	private Graphics2D screenGraphics;
	private Font statusFont;
	private int maxProgress = -1;
	private int progress = 0;
	
	private Color fontColor = Color.black;
	private Color barColor = new Color(24, 24, 151);
	private Color barBgColor = Color.LIGHT_GRAY;
	private Color bgColor = Color.white;
	
	public NativeSplashDelegate(URL url) throws Exception {
		// Do not catch exceptions but throw one in case
		// we cannot use the native splash screen
		screen = SplashScreen.getSplashScreen();
		if(screen==null)
			throw new UnsupportedOperationException();
		
		// TODO leave the original image?
		//screen.setImageURL(url);
		
		Dimension dim = screen.getSize();
		int height = dim.height;
		int width = dim.width;
		
		textArea = new Rectangle(20, height-30, width/2, 18);
		progressArea = new Rectangle(1, height-13, width-2, 12);
		
		screenGraphics = screen.createGraphics();
		
		statusFont = new Font("TimesRoman", Font.BOLD, 12); //$NON-NLS-1$
		screenGraphics.setFont(statusFont);
		
		screen.update();
	}

	/**
	 * @see de.ims.icarus.launcher.SplashWindow.SplashDelegate#dispose()
	 */
	@Override
	public void dispose() {
		if(screen.isVisible()) {
			screen.close();
		}
	}
	
	private void refresh() {
		if (screen.isVisible()) {
			
			screenGraphics.setPaint(maxProgress>-1 ? barBgColor : bgColor);
			screenGraphics.fill(progressArea);
			
			if(maxProgress>-1) {
				double ratio = (double) progress / (double) maxProgress;
				int barWidth = (int) (ratio * progressArea.width);
				barWidth = Math.min(barWidth, progressArea.width-2);

				screenGraphics.setPaint(barColor);
				screenGraphics.fillRect(progressArea.x+1, progressArea.y+1, 
						barWidth, progressArea.height-2);
			}

			screen.update();
		}
	}

	/**
	 * @see de.ims.icarus.launcher.SplashWindow.SplashDelegate#setProgress(int)
	 */
	@Override
	public void setProgress(int value) {
		if(value<0 || value>maxProgress)
			throw new IllegalArgumentException(
					"Cannot set value outside progress range: "+value); //$NON-NLS-1$
		
		progress = value;
		refresh();
	}

	/**
	 * @see de.ims.icarus.launcher.SplashWindow.SplashDelegate#setMaxProgress(int)
	 */
	@Override
	public void setMaxProgress(int maxValue) {
		if(maxValue<progress && maxValue!=-1)
			throw new IllegalArgumentException(
					"Cannot set max progress below current progress: "+maxValue); //$NON-NLS-1$
		
		maxProgress = maxValue;
		refresh();
	}

	/**
	 * @see de.ims.icarus.launcher.SplashWindow.SplashDelegate#setText(java.lang.String)
	 */
	@Override
	public void setText(String text) {
		if (screen.isVisible()) {
			screenGraphics.setPaint(bgColor);
			screenGraphics.fill(textArea);

			screenGraphics.setPaint(fontColor);
			screenGraphics.drawString(text,
					(int) textArea.getX() + 10, 
					(int) textArea.getY() + 10);
			
			screen.update();
		}
	}

	/**
	 * @see de.ims.icarus.launcher.SplashWindow.SplashDelegate#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return screen.isVisible();
	}

	/**
	 * @see de.ims.icarus.launcher.SplashWindow.SplashDelegate#step()
	 */
	@Override
	public void step() {
		if(progress>=maxProgress)
			throw new IllegalStateException();

		progress++;
		refresh();
	}

}
