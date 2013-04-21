/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools.treebank;

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.ikarus_systems.icarus.language.treebank.Treebank;
import net.ikarus_systems.icarus.language.treebank.TreebankRegistry;
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
public abstract class BasicTreebankEditor implements Editor<Treebank> {

	protected JPanel contentPanel;
	
	protected FormBuilder formBuilder;
	
	protected Treebank treebank;
	
	protected Handler handler;
	
	private int ignoreTreebankEvents = 0;
	
	protected BasicTreebankEditor() {
		// no-op
	}
	
	protected final void setIgnoreTreebankEvents(boolean ignore) {
		if(ignore)
			ignoreTreebankEvents++;
		else
			ignoreTreebankEvents--;
	}
	
	protected final boolean isIgnoreTreebankEvents() {
		return ignoreTreebankEvents>0;
	}
	
	protected final void resetIgnoreTreebankEvents() {
		ignoreTreebankEvents = 0;
	}
	
	protected Handler createHandler() {
		return new Handler();
	}
	
	protected void initForm() {
		formBuilder.addInputFormEntry("name", "labels.name"); //$NON-NLS-1$ //$NON-NLS-2$
		formBuilder.addLocationFormEntry("location", "labels.location"); //$NON-NLS-1$ //$NON-NLS-2$
		formBuilder.addPropertiesFormEntry("properties").setPropertyFilter(handler); //$NON-NLS-1$
	}
	
	protected String getTreebankName() {
		return treebank==null ? "<undefined>" : treebank.getName(); //$NON-NLS-1$
	}

	/**
	 * @see Editor#getEditorComponent()
	 */
	@Override
	public Component getEditorComponent() {
		if(contentPanel==null) {
			// Create form
			contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			formBuilder = FormBuilder.newLocalizingBuilder(contentPanel);

			// Connect to treebank registry
			handler = createHandler();
			TreebankRegistry.getInstance().addListener(Events.REMOVED, handler);
			TreebankRegistry.getInstance().addListener(Events.CHANGED, handler);
			
			initForm();
			
			formBuilder.buildForm();
			
			resetEdit();
		}
		return contentPanel;
	}

	/**
	 * @see Editor#setEditingItem(Object)
	 */
	@Override
	public void setEditingItem(Treebank treebank) {
		if(this.treebank==treebank) {
			return;
		}
		
		this.treebank = treebank;
		
		resetIgnoreTreebankEvents();
		resetEdit();
	}

	/**
	 * @see Editor#resetEdit()
	 */
	@Override
	public void resetEdit() {
		if(contentPanel==null) {
			return;
		}
		if(treebank==null) {
			formBuilder.clear();
			
			return;
		}
		
		doResetEdit();
	}
	
	protected void doResetEdit() {
		
		// Name
		formBuilder.setValue("name", treebank.getName()); //$NON-NLS-1$
		
		// Location
		Location location = treebank.getLocation();
		formBuilder.setValue("location", Locations.getPath(location)); //$NON-NLS-1$
		
		// Properties
		Map<String, Object> properties = new HashMap<>(treebank.getProperties());
		filterProperties(properties);
		formBuilder.setValue("properties", properties); //$NON-NLS-1$
		
	}

	/**
	 * @see Editor#applyEdit()
	 */
	@Override
	public void applyEdit() {
		if(contentPanel==null) {
			return;
		}
		if(treebank==null) {
			return;
		}
		
		doApplyEdit();
	}
	
	protected void doApplyEdit() {
		
		setIgnoreTreebankEvents(true);
		try {
			
			// Name
			String newName = (String)formBuilder.getValue("name"); //$NON-NLS-1$
			if(!newName.equals(treebank.getName())) {
				String uniqueName = TreebankRegistry.getInstance().getUniqueName(newName);
				if(!uniqueName.equals(newName)) {
					DialogFactory.getGlobalFactory().showInfo(null, 
							"plugins.languageTools.treebankEditView.dialogs.title",  //$NON-NLS-1$
							"plugins.languageTools.treebankExplorerView.dialogs.duplicateName",  //$NON-NLS-1$
							newName, uniqueName);
				}
				formBuilder.setValue("name", uniqueName); //$NON-NLS-1$
				TreebankRegistry.getInstance().setName(treebank, uniqueName);
			}
			
			// Location
			Location location = null;
			try {
				location = (Location)formBuilder.getValue("location"); //$NON-NLS-1$
			} catch (InvalidFormDataException e) {
				LoggerFactory.getLogger(BasicTreebankEditor.class).log(LoggerFactory.record(
						Level.SEVERE, "Failed to resolve location for treebank: "+treebank.getName(), e)); //$NON-NLS-1$
				DialogFactory.getGlobalFactory().showError(null, 
						"plugins.languageTools.treebankEditView.dialogs.title",  //$NON-NLS-1$
						"plugins.languageTools.treebankEditView.dialogs.invalidLocation",  //$NON-NLS-1$
						((LocationFormEntry)formBuilder.getEntry("location")).getLocationString()); //$NON-NLS-1$
			}
			if(!Locations.equals(location, treebank.getLocation())) {
				TreebankRegistry.getInstance().setLocation(treebank, location);
			}
	
			// Properties
			// Replace the old set of properties
			@SuppressWarnings("unchecked")
			Map<String, Object> properties = (Map<String, Object>) formBuilder.getValue("properties"); //$NON-NLS-1$
			TreebankRegistry.getInstance().setProperties(treebank, properties);
		} finally {
			setIgnoreTreebankEvents(true);
		}
	}
	
	protected static boolean equals(Object o1, Object o2) {
		if(o1==null || o2==null) {
			return false;
		}
		
		return o1.equals(o2);
	}

	/**
	 * @see Editor#hasChanges()
	 */
	@Override
	public boolean hasChanges() {
		if(contentPanel==null) {
			return false;
		}
		if(treebank==null) {
			return false;
		}
		
		// Compare name
		if(!equals(formBuilder.getValue("name"), treebank.getName())) { //$NON-NLS-1$
			return true;
		}
		
		// Compare location
		Location location = null;
		try {
			location = (Location) formBuilder.getValue("location"); //$NON-NLS-1$
		} catch(InvalidFormDataException e) {
			// ignore
		}
		if(!Locations.equals(location, treebank.getLocation())) {
			return true;
		}
		
		// Compare complete set of properties
		@SuppressWarnings("unchecked")
		Map<String, Object> properties = (Map<String, Object>) formBuilder.getValue("properties"); //$NON-NLS-1$
		if((properties==null || properties.isEmpty()) !=
				treebank.getProperties().isEmpty()) {
			return true;
		}
		if(properties.size()!= treebank.getProperties().size()) {
			return true;
		}
		if(properties!=null) {
			for(Entry<String, Object> entry : properties.entrySet()) {
				Object value = treebank.getProperty(entry.getKey());
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
	 * @see Editor#close()
	 */
	@Override
	public void close() {
		if(handler!=null) {
			TreebankRegistry.getInstance().removeListener(handler);
		}
		
		treebank = null;
	}

	/**
	 * @see Editor#getEditingItem()
	 */
	@Override
	public Treebank getEditingItem() {
		return treebank;
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
			if(!isIgnoreTreebankEvents()) {
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
