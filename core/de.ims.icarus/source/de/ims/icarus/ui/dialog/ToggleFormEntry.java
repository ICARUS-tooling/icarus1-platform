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

import javax.swing.JCheckBox;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ToggleFormEntry extends LabeledFormEntry<ToggleFormEntry> {
	
	protected final JCheckBox checkBox;
	
	public ToggleFormEntry(String label, JCheckBox checkBox) {
		super(label);
		if(checkBox==null) {
			checkBox = new JCheckBox();
		}
		
		this.checkBox = checkBox;
	}
	
	public ToggleFormEntry(String label, boolean selected) {
		this(label, new JCheckBox((String)null, selected));
	}
	
	public ToggleFormEntry(String label) {
		this(label, null);
	}
	
	public ToggleFormEntry() {
		this(null, null);
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
	 */
	@Override
	public ToggleFormEntry setValue(Object value) {
		checkBox.setSelected((Boolean)value);
		
		return this;
	}
	
	public JCheckBox getCheckBox() {
		return checkBox;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {
		return checkBox.isSelected();
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public ToggleFormEntry clear() {
		checkBox.setSelected(false);
		
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.LabeledFormEntry#addComponents(de.ims.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	protected void addComponents(FormBuilder builder) {
		builder.feedComponent(checkBox, null, getResizeMode());
	}

}
