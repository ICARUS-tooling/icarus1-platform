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

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.ListCellRenderer;

import de.ims.icarus.ui.list.TooltipListCellRenderer;



/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ChoiceFormEntry extends LabeledFormEntry<ChoiceFormEntry> {

	protected final JComboBox<?> comboBox;

	public ChoiceFormEntry(String label, ComboBoxModel<?> model, boolean editable) {
		super(label);
		if(model==null) {
			model = new DefaultComboBoxModel<>();
		}
		
		comboBox = new JComboBox<>(model);
		comboBox.setEditable(editable);
		comboBox.setSelectedItem(null);
		comboBox.setRenderer(TooltipListCellRenderer.getSharedInstance());
		
		setResizeMode(FormBuilder.RESIZE_HORIZONTAL);
	}
	
	public ChoiceFormEntry(String label, ComboBoxModel<?> model) {
		this(label, model, false);
	}
	
	public ChoiceFormEntry(String label, boolean editable) {
		this(label, (ComboBoxModel<?>) null, editable);
	}

	public ChoiceFormEntry(String label, Object[] items) {
		this(label, items, false);
	}
	
	public ChoiceFormEntry(String label, Object[] items, boolean editable) {
		this(label, new DefaultComboBoxModel<>(items), editable);
	}

	public ChoiceFormEntry() {
		this(null, (ComboBoxModel<Object>)null);
	}
	
	public JComboBox<?> getComboBox() {
		return comboBox;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.LabeledFormEntry#addComponents(de.ims.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	public void addComponents(FormBuilder builder) {
		builder.feedComponent(comboBox, null, getResizeMode());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ChoiceFormEntry setRenderer(ListCellRenderer renderer) {
		getComboBox().setRenderer(renderer);
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
	 */
	@Override
	public ChoiceFormEntry setValue(Object value) {
		comboBox.setSelectedItem(value);
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {
		return comboBox.getSelectedItem(); 
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public ChoiceFormEntry clear() {
		comboBox.setSelectedItem(null);
		return this;
	}

}
