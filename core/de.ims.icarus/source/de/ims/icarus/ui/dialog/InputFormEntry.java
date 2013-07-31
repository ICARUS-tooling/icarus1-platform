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

import javax.swing.JTextField;
import javax.swing.text.Document;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class InputFormEntry extends LabeledFormEntry<InputFormEntry> {
	
	public static final int DEFAULT_COLUMNS = 30; 
	
	protected final JTextField input;
	
	public InputFormEntry() {
		this(null, (JTextField)null);
	}

	public InputFormEntry(String label) {
		this(label, (JTextField)null);
	}

	public InputFormEntry(String label, String text) {
		this(label, new JTextField(text, DEFAULT_COLUMNS));
	}

	public InputFormEntry(String label, int columns) {
		this(label, new JTextField(columns));
	}

	public InputFormEntry(String label, JTextField input) {
		super(label);
		
		if(input==null) {
			input = new JTextField(DEFAULT_COLUMNS);
		}
		
		this.input = input;
		
		setResizeMode(FormBuilder.RESIZE_HORIZONTAL);
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormEntry#addComponents(de.ims.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	protected void addComponents(FormBuilder builder) {
		builder.feedComponent(input, null, getResizeMode());
	}

	public JTextField getInput() {
		return input;
	}
	
	public InputFormEntry setDocument(Document doc) {
		getInput().setDocument(doc);
		return this;
	}
	
	public InputFormEntry setValue(Object value) {
		input.setText((String) value);
		return this;
	}
	
	public InputFormEntry setColumns(int columns) {
		input.setColumns(columns);
		return this;
	}
	
	public InputFormEntry clear() {
		input.setText(null);
		return this;
	}
	
	public Object getValue() {
		return input.getText();
	}
}
