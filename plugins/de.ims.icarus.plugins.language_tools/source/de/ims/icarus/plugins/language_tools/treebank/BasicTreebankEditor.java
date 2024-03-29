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
package de.ims.icarus.plugins.language_tools.treebank;

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankRegistry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.dialog.InvalidFormDataException;
import de.ims.icarus.ui.dialog.LocationFormEntry;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.Locations;


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
		formBuilder.addPropertiesFormEntry("properties") //$NON-NLS-1$
			.setLabel("labels.properties").setPropertyFilter(handler); //$NON-NLS-1$
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
		formBuilder.setValue("location", location); //$NON-NLS-1$

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
				LoggerFactory.log(this, Level.SEVERE, "Failed to resolve location for treebank: "+treebank.getName(), e); //$NON-NLS-1$
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
		int size = properties==null ? 0 : properties.size();
		if(size!=treebank.getProperties().size()) {
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
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			if(!isIgnoreTreebankEvents()) {
				resetEdit();
			}
		}

		/**
		 * @see de.ims.icarus.util.Filter#accepts(java.lang.Object)
		 */
		@Override
		public boolean accepts(Object obj) {
			return isPropertyKeyAllowed((String)obj);
		}

	}
}
