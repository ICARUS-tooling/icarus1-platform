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
package de.ims.icarus.ui.dialog;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

import de.ims.icarus.ui.dialog.FormBuilder.FormEntry;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ControlFormEntry extends FormEntry {
	
	private final JButton[] buttons;

	public ControlFormEntry(Action...actions) {
		if(actions==null || actions.length==0)
			throw new IllegalArgumentException("Invalid actions list"); //$NON-NLS-1$
		
		buttons = new JButton[actions.length];
		
		for(int i=0; i<actions.length; i++) {
			buttons[i] = new JButton(actions[i]);
		}
	}
	
	public int getButtonCount() {
		return buttons.length;
	}
	
	public JButton getButton(int index) {
		return buttons[index];
	}
	
	public Action getAction(int index) {
		return getButton(index).getAction();
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#addToForm(de.ims.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	public ControlFormEntry addToForm(FormBuilder builder) {
		
		JPanel panel = new JPanel(new FlowLayout());
		
		for(JButton button : buttons) {
			panel.add(button);
		}
		
		builder.feedSeparator();
		builder.feedComponent(panel, null, GridBagConstraints.CENTER, FormBuilder.RESIZE_REMAINDER);
		
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
	 */
	@Override
	public ControlFormEntry setValue(Object value) {
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {
		return null;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public ControlFormEntry clear() {
		return this;
	}

}
