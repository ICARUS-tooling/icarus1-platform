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
package de.ims.icarus.language.coref.registry;

import java.awt.Component;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.ComboBoxModel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.java.plugin.registry.Extension;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.ExtensionListModel;
import de.ims.icarus.plugins.coref.CoreferencePlugin;
import de.ims.icarus.ui.dialog.ChoiceFormEntry;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.dialog.InvalidFormDataException;
import de.ims.icarus.ui.dialog.LocationFormEntry;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.Locations;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AllocationEditor implements Editor<AllocationDescriptor> {
	
	private JPanel contentPanel;
	private FormBuilder formBuilder;
	private AllocationDescriptor descriptor;
	
	private boolean ignoreEvents = false;
	private Handler handler;

	public AllocationEditor() {
		// no-op
	}
	
	private void initForm() {		
		// NAME
		formBuilder.addInputFormEntry("name", "labels.name"); //$NON-NLS-1$ //$NON-NLS-2$
		// LOCATION
		formBuilder.addLocationFormEntry("location", "labels.location"); //$NON-NLS-1$ //$NON-NLS-2$
		// READER
		ComboBoxModel<Extension> model = new ExtensionListModel(
				CoreferencePlugin.getAllocationReaderExtensions(), true);		
		ChoiceFormEntry entry = new ChoiceFormEntry("labels.reader", model); //$NON-NLS-1$
		entry.getComboBox().setRenderer(ExtensionListCellRenderer.getSharedInstance());
		formBuilder.addEntry("reader", entry); //$NON-NLS-1$
		// PROPERTIES
		formBuilder.addPropertiesFormEntry("properties"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#getEditorComponent()
	 */
	@Override
	public Component getEditorComponent() {
		if(contentPanel==null) {
			// Create form
			contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			formBuilder = FormBuilder.newLocalizingBuilder(contentPanel);

			// Connect to registry
			handler = new Handler();
			CoreferenceRegistry.getInstance().addListener(Events.REMOVED, handler);
			CoreferenceRegistry.getInstance().addListener(Events.CHANGED, handler);
			
			initForm();
			
			formBuilder.buildForm();
			
			resetEdit();
		}
		return contentPanel;
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
	 */
	@Override
	public void setEditingItem(AllocationDescriptor item) {
		if(item==descriptor) {
			return;
		}
		
		descriptor = item;
		
		resetEdit();
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#getEditingItem()
	 */
	@Override
	public AllocationDescriptor getEditingItem() {
		return descriptor;
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#resetEdit()
	 */
	@Override
	public void resetEdit() {
		if(contentPanel==null) {
			return;
		}
		
		if(descriptor==null) {
			formBuilder.clear();
			return;
		} 
		
		formBuilder.setValue("name", descriptor.getName()); //$NON-NLS-1$
		formBuilder.setValue("location", descriptor.getLocation()); //$NON-NLS-1$
		formBuilder.setValue("reader", descriptor.getReaderExtension()); //$NON-NLS-1$
		formBuilder.setValue("properties", descriptor.getProperties()); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#applyEdit()
	 */
	@Override
	public void applyEdit() {
		ignoreEvents = true;
		try {
			
			// Name
			String newName = (String)formBuilder.getValue("name"); //$NON-NLS-1$
			if(!newName.equals(descriptor.getName())) {
				String uniqueName = CoreferenceRegistry.getInstance().getUniqueAllocationName(newName);
				if(!uniqueName.equals(newName)) {
					DialogFactory.getGlobalFactory().showInfo(null, 
							"plugins.coref.coreferenceManagerView.dialogs.allocation.title",  //$NON-NLS-1$
							"plugins.coref.coreferenceManagerView.dialogs.allocation.duplicateName",  //$NON-NLS-1$
							newName, uniqueName);
				}
				formBuilder.setValue("name", uniqueName); //$NON-NLS-1$
				CoreferenceRegistry.getInstance().setName(descriptor, uniqueName);
			}
			
			// Location
			Location location = null;
			try {
				location = (Location)formBuilder.getValue("location"); //$NON-NLS-1$
			} catch (InvalidFormDataException e) {
				LoggerFactory.log(this, Level.SEVERE, "Failed to resolve location for document-set: "+descriptor.getName(), e); //$NON-NLS-1$
				DialogFactory.getGlobalFactory().showError(null, 
						"plugins.coref.coreferenceManagerView.dialogs.allocation.title",  //$NON-NLS-1$
						"plugins.coref.coreferenceManagerView.dialogs.allocation.invalidLocation",  //$NON-NLS-1$
						((LocationFormEntry)formBuilder.getEntry("location")).getLocationString()); //$NON-NLS-1$
			}
			if(!Locations.equals(location, descriptor.getLocation())) {
				CoreferenceRegistry.getInstance().setLocation(descriptor, location);
			}
			
			// Reader
			Extension extension = (Extension) formBuilder.getValue("reader"); //$NON-NLS-1$
			CoreferenceRegistry.getInstance().setReaderExtension(descriptor, extension);
	
	
			// Properties
			// Replace the old set of properties
			@SuppressWarnings("unchecked")
			Map<String, Object> properties = (Map<String, Object>) formBuilder.getValue("properties"); //$NON-NLS-1$
			CoreferenceRegistry.getInstance().setProperties(descriptor, properties);
		} finally {
			ignoreEvents = false;
		}
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#hasChanges()
	 */
	@Override
	public boolean hasChanges() {
		if(contentPanel==null) {
			return false;
		}
		if(descriptor==null) {
			return false;
		}
		
		// Compare name
		if(!CollectionUtils.equals(formBuilder.getValue("name"), descriptor.getName())) { //$NON-NLS-1$
			return true;
		}
		
		// Compare location
		Location location = null;
		try {
			location = (Location) formBuilder.getValue("location"); //$NON-NLS-1$
		} catch(InvalidFormDataException e) {
			// ignore
		}
		if(!Locations.equals(location, descriptor.getLocation())) {
			return true;
		}
		
		// Compare reader
		if(!CollectionUtils.equals(formBuilder.getValue("reader"), descriptor.getReaderExtension())) { //$NON-NLS-1$
			return true;
		}
		
		// Compare complete set of properties
		@SuppressWarnings("unchecked")
		Map<String, Object> properties = (Map<String, Object>) formBuilder.getValue("properties"); //$NON-NLS-1$
		Map<String, Object> oldProperties = descriptor.getProperties();
		if((properties==null || properties.isEmpty()) !=
				(oldProperties==null || oldProperties.isEmpty())) {
			return true;
		}
		if(properties.size()!= (oldProperties==null ? 0 : oldProperties.size())) {
			return true;
		}
		if(properties!=null) {
			for(Entry<String, Object> entry : properties.entrySet()) {
				Object oldValue = oldProperties.get(entry.getKey());
				if(oldValue==null && entry.getValue()==null) {
					continue;
				}
				if((oldValue==null) != (entry.getValue()==null)) {
					return true;
				}
				if(!oldValue.equals(entry.getValue())) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#close()
	 */
	@Override
	public void close() {
		descriptor = null;
		
		if(contentPanel!=null) {
			formBuilder.clear();
		}
		if(handler!=null) {
			CoreferenceRegistry.getInstance().removeListener(handler);
		}
	}
	
	protected class Handler implements EventListener {

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			if(!ignoreEvents) {
				resetEdit();
			}
		}
		
	}
}
