/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/launcher/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/launcher/NativeSplashDelegate.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.launcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.net.URL;

import net.ikarus_systems.icarus.launcher.SplashWindow.SplashDelegate;

/**
 * @author Markus Gärtner
 * @version $Id: NativeSplashDelegate.java 7 2013-02-27 13:18:56Z mcgaerty $
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
	 * @see net.ikarus_systems.icarus.launcher.SplashWindow.SplashDelegate#dispose()
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
				int barWidth = progress * progressArea.width / maxProgress;
				barWidth = Math.min(barWidth, progressArea.width-2);

				screenGraphics.setPaint(barColor);
				screenGraphics.fillRect(progressArea.x+1, progressArea.y+1, 
						barWidth, progressArea.height-2);
			}

			screen.update();
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.launcher.SplashWindow.SplashDelegate#setProgress(int)
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
	 * @see net.ikarus_systems.icarus.launcher.SplashWindow.SplashDelegate#setMaxProgress(int)
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
	 * @see net.ikarus_systems.icarus.launcher.SplashWindow.SplashDelegate#setText(java.lang.String)
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
	 * @see net.ikarus_systems.icarus.launcher.SplashWindow.SplashDelegate#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return screen.isVisible();
	}

}
