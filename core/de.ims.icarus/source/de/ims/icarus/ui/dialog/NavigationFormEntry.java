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

import java.text.ParseException;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class NavigationFormEntry extends LabeledFormEntry<NavigationFormEntry> {
	
	protected final JSpinner spinner;

	public NavigationFormEntry(String label) {
		this(label, null);
	}

	public NavigationFormEntry(String label, SpinnerModel spinnerModel) {
		super(label);
		
		if(spinnerModel==null) {
			spinnerModel = new SpinnerNumberModel(0, 0, 1, 1);
		}
		
		spinner = new JSpinner(spinnerModel);
	}
	
	public NavigationFormEntry(String label, int min, int max, int step, int value) {
		this(label, new SpinnerNumberModel(value, min, max, step));
	}
	
	public JSpinner getSpinner() {
		return spinner;
	}
	
	public NavigationFormEntry setMinimumValue(Comparable<?> value) {
		SpinnerModel model = spinner.getModel();
		if(model instanceof SpinnerNumberModel) {
			SpinnerNumberModel snm = (SpinnerNumberModel) model;
			snm.setMinimum(value);
		}
		
		return this;
	}
	
	public NavigationFormEntry setMaximumValue(Comparable<?> value) {
		SpinnerModel model = spinner.getModel();
		if(model instanceof SpinnerNumberModel) {
			SpinnerNumberModel snm = (SpinnerNumberModel) model;
			snm.setMaximum(value);
		}
		
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
	 */
	@Override
	public NavigationFormEntry setValue(Object value) {
		spinner.setValue(value);
		
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {
		return spinner.getValue();
	}
	
	public NavigationFormEntry commit() {
		try {
			spinner.commitEdit();
		} catch (ParseException e) {
			// TODO output to log?
			spinner.setValue(spinner.getValue());
		}
		
		return this;
	}

	/**
	 * Resets the spinner to its minimum value if the used {@code SpinnerModel}
	 * is of type {@link SpinnerNumberModel}.
	 * 
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public NavigationFormEntry clear() {
		SpinnerModel model = spinner.getModel();
		if(model instanceof SpinnerNumberModel) {
			SpinnerNumberModel snm = (SpinnerNumberModel) model;
			snm.setValue(snm.getMinimum());
		}
		
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.LabeledFormEntry#addComponents(de.ims.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	public void addComponents(FormBuilder builder) {
		builder.feedComponent(spinner, null, getResizeMode());
	}

}
