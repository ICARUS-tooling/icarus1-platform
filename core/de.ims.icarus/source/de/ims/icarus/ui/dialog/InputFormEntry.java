/*
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
