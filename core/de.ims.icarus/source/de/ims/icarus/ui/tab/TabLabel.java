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
package de.ims.icarus.ui.tab;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import de.ims.icarus.util.Exceptions;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TabLabel extends JLabel {

	private static final long serialVersionUID = 8922903169312874278L;
	
	private final JTabbedPane tabbedPane;
	
	public TabLabel(JTabbedPane tabbedPane) {			
		Exceptions.testNullArgument(tabbedPane, "tabbedPane"); //$NON-NLS-1$
		
		this.tabbedPane = tabbedPane;
		
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	}
	
	@Override
	public String getText() {
		int index = tabbedPane == null ? -1 : tabbedPane.indexOfTabComponent(this);
		return index==-1 ? null : tabbedPane.getTitleAt(index);
	}
	
	@Override
	public Icon getIcon() {
		int index = tabbedPane==null ? -1 : tabbedPane.indexOfTabComponent(this);
		return index==-1 ? null : tabbedPane.getIconAt(index);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.height = 21;
		return dim;
	}
}