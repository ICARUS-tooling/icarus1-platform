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
package de.ims.icarus.search_tools.standard;

import java.awt.Component;

import de.ims.icarus.search_tools.SearchMode;
import de.ims.icarus.search_tools.SearchParameters;
import de.ims.icarus.ui.NumberDocument;
import de.ims.icarus.ui.dialog.ChoiceFormEntry;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.dialog.FormBuilder.FormEntry;
import de.ims.icarus.ui.dialog.InputFormEntry;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.ui.list.TooltipListCellRenderer;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.Orientation;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultParameterEditor implements Editor<Options>, SearchParameters {

	protected Options data;

	protected FormBuilder formBuilder;

	public DefaultParameterEditor() {
		// no-op
	}

	protected FormBuilder createForm() {
		FormBuilder formBuilder = FormBuilder.newLocalizingBuilder();

		FormEntry entry = new ChoiceFormEntry(
				"plugins.searchTools.labels.searchMode",  //$NON-NLS-1$
				SearchMode.supportedModes()).setRenderer(new TooltipListCellRenderer());
		formBuilder.addEntry(SEARCH_MODE, entry);

		entry = new ChoiceFormEntry(
				"plugins.searchTools.labels.orientation",  //$NON-NLS-1$
				Orientation.values()).setRenderer(new TooltipListCellRenderer());
		formBuilder.addEntry(SEARCH_ORIENTATION, entry);

		formBuilder.addToggleFormEntry(SEARCH_CASESENSITIVE,
				"plugins.searchTools.labels.caseSensitive"); //$NON-NLS-1$
//		formBuilder.addToggleFormEntry(OPTIMIZE_SEARCH,
//				"plugins.searchTools.labels.optimize"); //$NON-NLS-1$

		entry = new InputFormEntry(
				"plugins.searchTools.labels.resultLimit") //$NON-NLS-1$
			.setDocument(new NumberDocument());
		formBuilder.addEntry(SEARCH_RESULT_LIMIT, entry);

		entry = new InputFormEntry(
				"plugins.searchTools.labels.minLength") //$NON-NLS-1$
			.setDocument(new NumberDocument());
		formBuilder.addEntry(SEARCH_MIN_LENGTH, entry);

		entry = new InputFormEntry(
				"plugins.searchTools.labels.maxLength") //$NON-NLS-1$
			.setDocument(new NumberDocument());
		formBuilder.addEntry(SEARCH_MAX_LENGTH, entry);

		formBuilder.addToggleFormEntry(SEARCH_NON_PROJECTIVE,
				"plugins.searchTools.labels.nonProjective"); //$NON-NLS-1$

		return formBuilder;
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#getEditorComponent()
	 */
	@Override
	public Component getEditorComponent() {
		if(formBuilder==null) {
			formBuilder = createForm();
			formBuilder.buildForm();

			refresh();
		}

		return formBuilder.getContainer();
	}

	protected void refresh() {
		if(formBuilder==null) {
			return;
		}

		Options options = data==null ? Options.emptyOptions : data;

		formBuilder.setValue(SEARCH_MODE, options.get(SEARCH_MODE, DEFAULT_SEARCH_MODE));
		formBuilder.setValue(SEARCH_ORIENTATION, options.get(SEARCH_ORIENTATION, DEFAULT_SEARCH_ORIENTATION));
		formBuilder.setValue(SEARCH_CASESENSITIVE, options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE));
//		formBuilder.setValue(OPTIMIZE_SEARCH, options.get(OPTIMIZE_SEARCH, DEFAULT_OPTIMIZE_SEARCH));
		formBuilder.setValue(SEARCH_RESULT_LIMIT, String.valueOf(options.get(SEARCH_RESULT_LIMIT, DEFAULT_SEARCH_RESULT_LIMIT)));
		formBuilder.setValue(SEARCH_MIN_LENGTH, String.valueOf(options.get(SEARCH_MIN_LENGTH, DEFAULT_SEARCH_MIN_LENGTH)));
		formBuilder.setValue(SEARCH_MAX_LENGTH, String.valueOf(options.get(SEARCH_MAX_LENGTH, DEFAULT_SEARCH_MAX_LENGTH)));
		formBuilder.setValue(SEARCH_NON_PROJECTIVE, options.get(SEARCH_NON_PROJECTIVE, DEFAULT_SEARCH_NON_PROJECTIVE));
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
	 */
	@Override
	public void setEditingItem(Options item) {
		data = item;

		if(formBuilder!=null) {
			refresh();
		}
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#getEditingItem()
	 */
	@Override
	public Options getEditingItem() {
		return data;
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#resetEdit()
	 */
	@Override
	public void resetEdit() {
		if(formBuilder==null) {
			formBuilder = createForm();
			formBuilder.buildForm();
		}
		refresh();
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#applyEdit()
	 */
	@Override
	public void applyEdit() {
		if(formBuilder==null) {
			return;
		}

		if(data==null) {
			data = new Options();
		}

		for(int i=0; i<formBuilder.getEntryCount(); i++) {
			String id = formBuilder.getIdAt(i);

			data.put(id, formBuilder.getValue(id));
		}
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#hasChanges()
	 */
	@Override
	public boolean hasChanges() {
		if(formBuilder==null) {
			return false;
		}
		if(data==null) {
			data = new Options();
		}

		for(int i=0; i<formBuilder.getEntryCount(); i++) {
			String id = formBuilder.getIdAt(i);

			Object oldValue = data.get(id);
			Object newValue = formBuilder.getValue(id);

			if(oldValue==null || !oldValue.equals(newValue)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#close()
	 */
	@Override
	public void close() {
		// no-op
	}
}
