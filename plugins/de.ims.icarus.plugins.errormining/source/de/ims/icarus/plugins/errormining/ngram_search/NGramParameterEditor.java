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
package de.ims.icarus.plugins.errormining.ngram_search;

import java.awt.Component;

import de.ims.icarus.plugins.errormining.ngram_tools.NGramParameters;
import de.ims.icarus.ui.NumberDocument;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.dialog.FormBuilder.FormEntry;
import de.ims.icarus.ui.dialog.InputFormEntry;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.util.Options;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramParameterEditor implements Editor<Options>, NGramParameters {
	
	protected Options data;
	
	protected FormBuilder formBuilder;
	
	
	public NGramParameterEditor(){
		//noop
	}

	
	protected FormBuilder createForm() {
		FormBuilder formBuilder = FormBuilder.newLocalizingBuilder();
		
		formBuilder.addToggleFormEntry(USE_NUMBER_WILDCARD,
				"plugins.errormining.labels.numberWildcard"); //$NON-NLS-1$
				
		formBuilder.addToggleFormEntry(USE_FRINGE_HEURISTIC,
				"plugins.errormining.labels.fringe"); //$NON-NLS-1$
		
		FormEntry entry = new InputFormEntry(
				"plugins.errormining.labels.fringeStart") //$NON-NLS-1$
			.setDocument(new NumberDocument());
		formBuilder.addEntry(FRINGE_START, entry);
		
		entry = new InputFormEntry(
				"plugins.errormining.labels.fringeEnd") //$NON-NLS-1$
			.setDocument(new NumberDocument());
		formBuilder.addEntry(FRINGE_END, entry);
		
		formBuilder.addSeperator();
		
		entry = new InputFormEntry(
				"plugins.errormining.labels.resultLimit") //$NON-NLS-1$
			.setDocument(new NumberDocument());
		formBuilder.addEntry(NGRAM_RESULT_LIMIT, entry);		
		
		entry = new InputFormEntry(
				"plugins.errormining.labels.sentenceLimit") //$NON-NLS-1$
			.setDocument(new NumberDocument());
		formBuilder.addEntry(SENTENCE_LIMIT, entry);
		
		//formBuilder.addSeperator();

		
		entry = new InputFormEntry(
				"plugins.errormining.labels.onlyGramsGreaterX") //$NON-NLS-1$
			.setDocument(new NumberDocument());
		formBuilder.addEntry(GRAMS_GREATERX, entry);
		
		formBuilder.addToggleFormEntry(CREATE_XML_OUTPUT,
				"plugins.errormining.labels.createxmlOutput"); //$NON-NLS-1$
			
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
		
		formBuilder.setValue(USE_NUMBER_WILDCARD,
				options.get(USE_NUMBER_WILDCARD,
							DEFAULT_USE_NUMBER_WILDCARD));
		
		formBuilder.setValue(USE_FRINGE_HEURISTIC,
							options.get(USE_FRINGE_HEURISTIC,
										DEFAULT_USE_FRINGE_HEURISTIC));
		formBuilder.setValue(FRINGE_START,
				String.valueOf(options.get(FRINGE_START,
										DEFAULT_FRINGE_START)));
		formBuilder.setValue(FRINGE_END,
				String.valueOf(options.get(FRINGE_END,
										DEFAULT_FRINGE_END)));		
		
		formBuilder.setValue(NGRAM_RESULT_LIMIT,
				String.valueOf(options.get(NGRAM_RESULT_LIMIT,
										DEFAULT_NGRAM_RESULT_LIMIT)));
		
		formBuilder.setValue(SENTENCE_LIMIT,
				String.valueOf(options.get(SENTENCE_LIMIT,
										DEFAULT_SENTENCE_LIMIT))); 
		
		formBuilder.setValue(GRAMS_GREATERX,
				String.valueOf(options.get(GRAMS_GREATERX,
										DEFAULT_GRAMS_GREATERX)));
		
		formBuilder.setValue(CREATE_XML_OUTPUT,
							options.get(CREATE_XML_OUTPUT,
										DEFAULT_CREATE_XML_OUTPUT));

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
		// noop
		
	}

}
