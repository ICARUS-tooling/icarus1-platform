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
package de.ims.icarus.ui.view;

import java.awt.Rectangle;

import javax.swing.JTextPane;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class BasicTextPaneRenderer extends JTextPane {
	
	private static final long serialVersionUID = 8933915223970221240L;

	@Override
	public boolean isShowing() {
		return true;
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
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		if (propertyName == "document" || propertyName == "font" //$NON-NLS-1$ //$NON-NLS-2$
				|| propertyName == "highlighter") { //$NON-NLS-1$
			super.firePropertyChange(propertyName, oldValue, newValue);
		}
	}

	@Override
	public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, char oldValue, char newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, short oldValue,	short newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, int oldValue, int newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, long oldValue, long newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, float oldValue,	float newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, double oldValue, double newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		// no-op
	}
}
