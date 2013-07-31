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

import de.ims.icarus.ui.dialog.FormBuilder.AbstractFormEntry;
import de.ims.icarus.ui.dialog.FormBuilder.FormEntry;


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
