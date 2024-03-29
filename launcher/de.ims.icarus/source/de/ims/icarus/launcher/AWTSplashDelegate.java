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
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Window;
import java.net.URL;

import de.ims.icarus.launcher.SplashWindow.SplashDelegate;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
class AWTSplashDelegate extends Window implements SplashDelegate {

	private static final long serialVersionUID = 8769476579291666994L;

	private Label label;
	private ProgressBar progressBar;
	private boolean paintCalled = false;
	private Image image;

	/**
	 *
	 */
	public AWTSplashDelegate(URL url) throws Exception {
		super(new Frame());

		image = Toolkit.getDefaultToolkit().createImage(url);

		// Wait until our image is loaded
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(image, 0);
		try {
			mt.waitForID(0);
		} catch (InterruptedException e) {
			// no-op
		}

		setSize(image.getWidth(this), image.getHeight(this));
		setAlwaysOnTop(true);
		setLocationRelativeTo(null);

		label = new Label();
		label.setAlignment(Label.LEFT);

		progressBar = new ProgressBar();
		progressBar.setSize(getWidth(), 18);

		setLayout(null);
		add(label);
		add(progressBar);

		int y = getHeight()-progressBar.getHeight();

		progressBar.setLocation(0, y);
		y -= label.getHeight()-2;
		label.setLocation(0, y);

		setVisible(true);

		if (!EventQueue.isDispatchThread()
				&& Runtime.getRuntime().availableProcessors() == 1) {

			synchronized (this) {
				while (!paintCalled) {
					try {
						wait();
					} catch (InterruptedException e) {
						// no-op
					}
				}
			}
		}
	}

	@Override
	public boolean isOpaque() {
		return true;
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		g.drawImage(image, 0, 0, this);

		super.paintComponents(g);

		if (!paintCalled) {
			synchronized (this) {
				paintCalled = true;
				notifyAll();
			}
		}
	}

	/**
	 * @see de.ims.icarus.launcher.SplashWindow.SplashDelegate#dispose()
	 */
	@Override
	public void dispose() {
		setVisible(false);
		super.dispose();
	}

	/**
	 * @see de.ims.icarus.launcher.SplashWindow.SplashDelegate#setProgress(int)
	 */
	@Override
	public void setProgress(int value) {
		progressBar.setValue(value);
	}

	/**
	 * @see de.ims.icarus.launcher.SplashWindow.SplashDelegate#setMaxProgress(int)
	 */
	@Override
	public void setMaxProgress(int maxValue) {
		progressBar.setMaxValue(maxValue);
	}

	/**
	 * @see de.ims.icarus.launcher.SplashWindow.SplashDelegate#setText(java.lang.String)
	 */
	@Override
	public void setText(String text) {
		label.setText(text);
	}

	/**
	 * @see de.ims.icarus.launcher.SplashWindow.SplashDelegate#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return super.isVisible();
	}

	/**
	 * @see de.ims.icarus.launcher.SplashWindow.SplashDelegate#step()
	 */
	@Override
	public void step() {
		progressBar.step();
	}

	private class ProgressBar extends Component {

		private static final long serialVersionUID = -1661482562081031389L;

		private int minValue = 0;
		private int maxValue = 100;
		private int value = 0;

		private double progress = 0d;

		private Color barColor = Color.BLUE;
		private Color borderColor = Color.DARK_GRAY;

		@Override
		public void paint(Graphics g) {
			int width = getWidth();
			int height = getHeight();

			g.setColor(borderColor);
			g.fillRect(0, 0, width, height);

			g.setColor(getBackground());
			g.fillRect(1, 1, width-2, height-2);

			int fill = (int) ((width-2) * progress);

			if(fill>0) {
				g.setColor(barColor);
				g.fillRect(1, 1, fill, height-2);
			}
		}

		private void recalcProgress() {
			double min = minValue;
			double progress = (value-min) / (maxValue-min);

			progress = Math.min(progress, 1d);

			// restrict precision to 3 digits
			progress = Math.floor(1000*progress) * 0.001;

			if(this.progress<progress) {
				this.progress = progress;
				repaint();
			}
		}

		public void setMaxValue(int value) {
			if(value<=minValue)
				throw new IllegalArgumentException("maxValue too small"); //$NON-NLS-1$

			int oldValue = maxValue;
			maxValue = value;
			firePropertyChange("maxValue", oldValue, value); //$NON-NLS-1$

			recalcProgress();
		}

		public void setValue(int value) {
			if(value>maxValue || value<minValue)
				throw new IllegalArgumentException("value out of range"); //$NON-NLS-1$

			int oldValue = this.value;
			this.value = value;
			firePropertyChange("value", oldValue, value); //$NON-NLS-1$

			recalcProgress();
		}

		public void step() {
			if(value<=maxValue)
				throw new IllegalStateException();

			int oldValue = value;
			value++;
			firePropertyChange("value", oldValue, value); //$NON-NLS-1$

			recalcProgress();
		}
	}
}
