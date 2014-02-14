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
package de.ims.icarus.ui.dialog;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.ims.icarus.ui.dialog.FormBuilder.FormEntry;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class SpinnerFormEntry extends LabeledFormEntry<SpinnerFormEntry> {
	
	protected final JSpinner spinner;
	
	public SpinnerFormEntry(String label, JSpinner spinner) {
		super(label);
		if(spinner==null) {
			spinner = new JSpinner();
		}
		
		this.spinner = spinner;
		setResizeMode(FormBuilder.RESIZE_HORIZONTAL);
	}
	
	
	public SpinnerFormEntry(String label, SpinnerNumberModel model) {
		super(label);
		if(model==null) {
			model = new SpinnerNumberModel();
		}

		spinner = new JSpinner();
		spinner.setModel(model);

		setResizeMode(FormBuilder.RESIZE_HORIZONTAL);
	}

		
	
	public SpinnerFormEntry(String label) {
		this(label, (SpinnerNumberModel)null);
	}
	
	public SpinnerFormEntry() {
		this(null, (SpinnerNumberModel)null);
	}
	
	
	public SpinnerFormEntry setSpinnerNumberModel(SpinnerNumberModel model){
		if(model==null) {
			model = new SpinnerNumberModel();
		}
		
		spinner.setModel(model);
		return this;
	}


	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
	 */
	@Override
	public FormEntry setValue(Object value) {
		spinner.setValue(Integer.valueOf((String) value));
		
		return this;
	}

	public JSpinner getSpinner() {
		return spinner;
	}
	
	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {		
		return spinner.getValue();
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public FormEntry clear() {

		
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.LabeledFormEntry#addComponents(de.ims.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	protected void addComponents(FormBuilder builder) {
		builder.feedComponent(spinner, null, getResizeMode());		
	}

}
