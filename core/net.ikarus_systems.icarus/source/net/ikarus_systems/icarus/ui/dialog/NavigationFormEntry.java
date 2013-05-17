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

	public NavigationFormEntry(String label, SpinnerModel spinnerModel) {
		super(label);
		
		if(spinnerModel==null) {
			spinnerModel = new SpinnerNumberModel();
		}
		
		spinner = new JSpinner(spinnerModel);
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
			snm.setMinimum(value);
		}
		
		return this;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
	 */
	@Override
	public NavigationFormEntry setValue(Object value) {
		spinner.setValue(value);
		
		return this;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {
		return spinner.getValue();
	}

	/**
	 * Resets the spinner to its minimum value if the used {@code SpinnerModel}
	 * is of type {@link SpinnerNumberModel}.
	 * 
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#clear()
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
	 * @see net.ikarus_systems.icarus.ui.dialog.LabeledFormEntry#addComponents(net.ikarus_systems.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	public void addComponents(FormBuilder builder) {
		builder.feedComponent(spinner, null, getResizeMode());
	}

}
