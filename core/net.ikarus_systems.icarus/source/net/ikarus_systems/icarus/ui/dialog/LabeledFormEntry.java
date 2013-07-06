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

import net.ikarus_systems.icarus.ui.dialog.FormBuilder.AbstractFormEntry;
import net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class LabeledFormEntry<E extends FormEntry> 
		extends AbstractFormEntry<E> {
	
	protected String label;

	public LabeledFormEntry(String label) {
		this.label = label;
	}
	
	public LabeledFormEntry() {
		// no-op
	}
	
	public String getLabel() {
		return label;
	}

	@SuppressWarnings("unchecked")
	public E setLabel(String label) {
		this.label = label;
		return (E) this;
	}

	@SuppressWarnings("unchecked")
	public E addToForm(FormBuilder builder) {
		// ONLY feed label if it is non-null!
		// This allows subclasses to occupy the label area if they desire
		if(label!=null) {
			builder.feedLabel(label);
		}
		addComponents(builder);
		builder.newLine();
		return (E) this;
	}

	protected abstract void addComponents(FormBuilder builder);
}
