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
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
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
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {
		return checkBox.isSelected();
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public ToggleFormEntry clear() {
		checkBox.setSelected(false);
		
		return this;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.LabeledFormEntry#addComponents(net.ikarus_systems.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	protected void addComponents(FormBuilder builder) {
		builder.feedComponent(checkBox, null, getResizeMode());
	}

}
