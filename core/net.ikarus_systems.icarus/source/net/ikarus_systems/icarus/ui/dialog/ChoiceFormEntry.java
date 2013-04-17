/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.dialog;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ChoiceFormEntry extends LabeledFormEntry<ChoiceFormEntry> {

	/**
	 * By default not editable!
	 */
	protected final JComboBox<?> comboBox;
	
	public ChoiceFormEntry(String label, ComboBoxModel<?> model) {
		super(label);
		
		if(model==null) {
			model = new DefaultComboBoxModel<>();
		}
		
		comboBox = new JComboBox<>(model);
		comboBox.setEditable(false);
		
		setResizeMode(FormBuilder.RESIZE_HORIZONTAL);
	}

	public ChoiceFormEntry(String label, Object[] items) {
		this(label, new DefaultComboBoxModel<>(items));
	}

	public ChoiceFormEntry() {
		this(null, (ComboBoxModel<Object>)null);
	}
	
	public JComboBox<?> getComboBox() {
		return comboBox;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.LabeledFormEntry#addComponents(net.ikarus_systems.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	public void addComponents(FormBuilder builder) {
		builder.feedComponent(comboBox, null, getResizeMode());
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
	 */
	@Override
	public ChoiceFormEntry setValue(Object value) {
		comboBox.setSelectedItem(value);
		return this;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {
		return comboBox.getSelectedItem(); 
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public ChoiceFormEntry clear() {
		comboBox.setSelectedItem(null);
		return this;
	}

}
