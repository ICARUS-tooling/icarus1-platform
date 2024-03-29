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
package de.ims.icarus.ui.helper;

import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractRendererPanel extends JPanel {

	private static final long serialVersionUID = 2232765458226156033L;
	
	public AbstractRendererPanel() {
		super();
	}

	public AbstractRendererPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public AbstractRendererPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public AbstractRendererPanel(LayoutManager layout) {
		super(layout);
	}

	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
}
