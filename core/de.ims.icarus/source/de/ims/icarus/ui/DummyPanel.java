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

import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DummyPanel extends JPanel {

	private static final long serialVersionUID = 8101383182871821635L;

	public DummyPanel() {
		// no-op
	}

	public DummyPanel(LayoutManager layout) {
		super(layout);
	}

	@Override
	public void validate() {
		// no-op
	}

	@Override
	public void invalidate() {
		// no-op
	}

	@Override
	public void revalidate() {
		// no-op
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
		// no-op
	}

	@Override
	public void repaint(Rectangle r) {
		// no-op
	}

	@Override
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, byte oldValue,
			byte newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, char oldValue,
			char newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, short oldValue,
			short newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, int oldValue,
			int newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, long oldValue,
			long newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, float oldValue,
			float newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, double oldValue,
			double newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
		// no-op
	}
}
