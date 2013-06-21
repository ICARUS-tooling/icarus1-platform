/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.standard;

import java.awt.Component;

import net.ikarus_systems.icarus.search_tools.SearchMode;
import net.ikarus_systems.icarus.search_tools.SearchParameters;
import net.ikarus_systems.icarus.ui.NumberDocument;
import net.ikarus_systems.icarus.ui.dialog.ChoiceFormEntry;
import net.ikarus_systems.icarus.ui.dialog.FormBuilder;
import net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry;
import net.ikarus_systems.icarus.ui.dialog.InputFormEntry;
import net.ikarus_systems.icarus.ui.helper.Editor;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.Orientation;

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
				SearchMode.values());
		formBuilder.addEntry(SEARCH_MODE, entry); 
		
		entry = new ChoiceFormEntry(
				"plugins.searchTools.labels.orientation",  //$NON-NLS-1$
				Orientation.values());
		formBuilder.addEntry(SEARCH_ORIENTATION, entry); 
		
		formBuilder.addToggleFormEntry(SEARCH_CASESENSITIVE,  
				"plugins.searchTools.labels.caseSensitive"); //$NON-NLS-1$
		formBuilder.addToggleFormEntry(OPTIMIZE_SEARCH,  
				"plugins.searchTools.labels.optimize"); //$NON-NLS-1$
		
		entry = new InputFormEntry(
				"plugins.searchTools.labels.resultLimit").setDocument(new NumberDocument()); //$NON-NLS-1$
		formBuilder.addEntry(SEARCH_RESULT_LIMIT, entry);
			
		return formBuilder;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#getEditorComponent()
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
		
		formBuilder.setValue(SEARCH_MODE, options.get(SEARCH_MODE, SearchMode.MATCHES)); 
		formBuilder.setValue(SEARCH_ORIENTATION, options.get(SEARCH_ORIENTATION, Orientation.LEFT_TO_RIGHT)); 
		formBuilder.setValue(SEARCH_CASESENSITIVE, options.get(SEARCH_CASESENSITIVE, false)); 
		formBuilder.setValue(OPTIMIZE_SEARCH, options.get(OPTIMIZE_SEARCH, false)); 
		formBuilder.setValue(SEARCH_RESULT_LIMIT, String.valueOf(options.get(SEARCH_RESULT_LIMIT, 0))); 
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
	 */
	@Override
	public void setEditingItem(Options item) {
		data = item;
		
		if(formBuilder!=null) {
			refresh();
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#getEditingItem()
	 */
	@Override
	public Options getEditingItem() {
		return data;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#resetEdit()
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
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#applyEdit()
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
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#hasChanges()
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
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#close()
	 */
	@Override
	public void close() {
		// no-op
	}
}
