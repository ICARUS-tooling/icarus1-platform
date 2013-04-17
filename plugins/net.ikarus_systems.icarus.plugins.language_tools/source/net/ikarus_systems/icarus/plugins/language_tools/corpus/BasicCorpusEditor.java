/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools.corpus;

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.language.corpus.CorpusRegistry;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.dialog.FormBuilder;
import net.ikarus_systems.icarus.ui.dialog.InvalidFormDataException;
import net.ikarus_systems.icarus.ui.dialog.LocationFormEntry;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.ui.helper.Editor;
import net.ikarus_systems.icarus.util.Filter;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.location.Locations;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class BasicCorpusEditor implements Editor<Corpus> {

	protected JPanel contentPanel;
	
	protected FormBuilder formBuilder;
	
	protected Corpus corpus;
	
	protected Handler handler;
	
	private int ignoreCorpusEvents = 0;
	
	protected BasicCorpusEditor() {
		// no-op
	}
	
	protected final void setIgnoreCorpusEvents(boolean ignore) {
		if(ignore)
			ignoreCorpusEvents++;
		else
			ignoreCorpusEvents--;
	}
	
	protected final boolean isIgnoreCorpusEvents() {
		return ignoreCorpusEvents>0;
	}
	
	protected final void resetIgnoreCorpusEvents() {
		ignoreCorpusEvents = 0;
	}
	
	protected Handler createHandler() {
		return new Handler();
	}
	
	protected void initForm() {
		formBuilder.addInputFormEntry("name", "labels.name"); //$NON-NLS-1$ //$NON-NLS-2$
		formBuilder.addLocationFormEntry("location", "labels.location"); //$NON-NLS-1$ //$NON-NLS-2$
		formBuilder.addPropertiesFormEntry("properties").setPropertyFilter(handler); //$NON-NLS-1$
	}
	
	protected String getCorpusName() {
		return corpus==null ? "<undefined>" : corpus.getName(); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.CorpusEditor#getEditorComponent()
	 */
	@Override
	public Component getEditorComponent() {
		if(contentPanel==null) {
			// Create form
			contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			formBuilder = FormBuilder.newLocalizingBuilder(contentPanel);

			// Connect to corpus registry
			handler = createHandler();
			CorpusRegistry.getInstance().addListener(Events.REMOVED, handler);
			CorpusRegistry.getInstance().addListener(Events.CHANGED, handler);
			
			initForm();
			
			formBuilder.buildForm();
			
			resetEdit();
		}
		return contentPanel;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.CorpusEditor#setCorpus(net.ikarus_systems.icarus.language.corpus.Corpus)
	 */
	@Override
	public void setEditingItem(Corpus corpus) {
		if(this.corpus==corpus) {
			return;
		}
		
		this.corpus = corpus;
		
		resetIgnoreCorpusEvents();
		resetEdit();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.CorpusEditor#resetEdit()
	 */
	@Override
	public void resetEdit() {
		if(contentPanel==null) {
			return;
		}
		if(corpus==null) {
			formBuilder.clear();
			
			return;
		}
		
		doResetEdit();
	}
	
	protected void doResetEdit() {
		
		// Name
		formBuilder.setValue("name", corpus.getName()); //$NON-NLS-1$
		
		// Location
		Location location = corpus.getLocation();
		formBuilder.setValue("location", Locations.getPath(location)); //$NON-NLS-1$
		
		// Properties
		Map<String, Object> properties = new HashMap<>(corpus.getProperties());
		filterProperties(properties);
		formBuilder.setValue("properties", properties); //$NON-NLS-1$
		
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.CorpusEditor#applyEdit()
	 */
	@Override
	public void applyEdit() {
		if(contentPanel==null) {
			return;
		}
		if(corpus==null) {
			return;
		}
		
		doApplyEdit();
	}
	
	protected void doApplyEdit() {
		
		setIgnoreCorpusEvents(true);
		try {
			
			// Name
			String newName = (String)formBuilder.getValue("name"); //$NON-NLS-1$
			if(!newName.equals(corpus.getName())) {
				String uniqueName = CorpusRegistry.getInstance().getUniqueName(newName);
				if(!uniqueName.equals(newName)) {
					DialogFactory.getGlobalFactory().showInfo(null, 
							"plugins.languageTools.corpusEditView.dialogs.title",  //$NON-NLS-1$
							"plugins.languageTools.corpusExplorerView.dialogs.duplicateName",  //$NON-NLS-1$
							newName, uniqueName);
				}
				formBuilder.setValue("name", uniqueName); //$NON-NLS-1$
				CorpusRegistry.getInstance().setName(corpus, uniqueName);
			}
			
			// Location
			Location location = null;
			try {
				location = (Location)formBuilder.getValue("location"); //$NON-NLS-1$
			} catch (InvalidFormDataException e) {
				LoggerFactory.getLogger(BasicCorpusEditor.class).log(LoggerFactory.record(
						Level.SEVERE, "Failed to resolve location for corpus: "+corpus.getName(), e)); //$NON-NLS-1$
				DialogFactory.getGlobalFactory().showError(null, 
						"plugins.languageTools.corpusEditView.dialogs.title",  //$NON-NLS-1$
						"plugins.languageTools.corpusEditView.dialogs.invalidLocation",  //$NON-NLS-1$
						((LocationFormEntry)formBuilder.getEntry("location")).getLocationString()); //$NON-NLS-1$
			}
			if(!Locations.equals(location, corpus.getLocation())) {
				CorpusRegistry.getInstance().setLocation(corpus, location);
			}
	
			// Properties
			// Replace the old set of properties
			@SuppressWarnings("unchecked")
			Map<String, Object> properties = (Map<String, Object>) formBuilder.getValue("properties"); //$NON-NLS-1$
			CorpusRegistry.getInstance().setProperties(corpus, properties);
		} finally {
			setIgnoreCorpusEvents(true);
		}
	}
	
	protected static boolean equals(Object o1, Object o2) {
		if(o1==null || o2==null) {
			return false;
		}
		
		return o1.equals(o2);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.CorpusEditor#hasUnsavedChanges()
	 */
	@Override
	public boolean hasChanges() {
		if(contentPanel==null) {
			return false;
		}
		if(corpus==null) {
			return false;
		}
		
		// Compare name
		if(!equals(formBuilder.getValue("name"), corpus.getName())) { //$NON-NLS-1$
			return true;
		}
		
		// Compare location
		Location location = null;
		try {
			location = (Location) formBuilder.getValue("location"); //$NON-NLS-1$
		} catch(InvalidFormDataException e) {
			// ignore
		}
		if(!Locations.equals(location, corpus.getLocation())) {
			return true;
		}
		
		// Compare complete set of properties
		@SuppressWarnings("unchecked")
		Map<String, Object> properties = (Map<String, Object>) formBuilder.getValue("properties"); //$NON-NLS-1$
		if((properties==null || properties.isEmpty()) !=
				corpus.getProperties().isEmpty()) {
			return true;
		}
		if(properties.size()!= corpus.getProperties().size()) {
			return true;
		}
		if(properties!=null) {
			for(Entry<String, Object> entry : properties.entrySet()) {
				Object value = corpus.getProperty(entry.getKey());
				if(value==null && entry.getValue()==null) {
					continue;
				}
				if((value==null) != (entry.getValue()==null)) {
					return true;
				}
				if(!value.equals(entry.getValue())) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.CorpusEditor#close()
	 */
	@Override
	public void close() {
		if(handler!=null) {
			CorpusRegistry.getInstance().removeListener(handler);
		}
		
		corpus = null;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.CorpusEditor#getCorpus()
	 */
	@Override
	public Corpus getEditingItem() {
		return corpus;
	}
	
	protected void filterProperties(Map<String, Object> properties) {
		for(Iterator<Entry<String, Object>> i = properties.entrySet().iterator(); 
				i.hasNext(); ) {
			Entry<String, Object> entry = i.next();
			if(!isPropertyKeyAllowed(entry.getKey())) {
				i.remove();
			}
		}
	}
	
	protected boolean isPropertyKeyAllowed(String key) {
		return true;
	}
	
	protected class Handler implements EventListener, Filter {

		/**
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			if(!isIgnoreCorpusEvents()) {
				resetEdit();
			}
		}

		/**
		 * @see net.ikarus_systems.icarus.util.Filter#accepts(java.lang.Object)
		 */
		@Override
		public boolean accepts(Object obj) {
			return isPropertyKeyAllowed((String)obj);
		}
		
	}
}
