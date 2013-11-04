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

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.id.DuplicateIdentifierException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FormBuilder {

	public static final int RESIZE_NONE = 0;
	public static final int RESIZE_REMAINDER = 1;
	public static final int RESIZE_HORIZONTAL = 2;
	public static final int RESIZE_FILL = 3;
	
	public static final Insets DEFAULT_LABEL_INSETS = new Insets(0, 2, 0, 5);
	
	public static final Insets DEFAULT_CONTENT_INSETS = new Insets(1, 1, 1, 1);
	
	private List<String> ids;
	private Map<String, FormEntry> entryMap;
	
	private final Container container;
	
	private ResourceDomain resourceDomain;
	
	public static FormBuilder newBuilder() {
		return new FormBuilder(null);
	}
	
	public static FormBuilder newBuilder(Container container) {
		return new FormBuilder(container);
	}

	public static FormBuilder newLocalizingBuilder() {
		return newLocalizingBuilder(null, ResourceManager.getInstance().getGlobalDomain());
	}

	public static FormBuilder newLocalizingBuilder(Container container) {
		return newLocalizingBuilder(container, ResourceManager.getInstance().getGlobalDomain());
	}

	public static FormBuilder newLocalizingBuilder(Container container,
			ResourceDomain resourceDomain) {
		FormBuilder builder = new FormBuilder(container);
		builder.setResourceDomain(resourceDomain);
		
		return builder;
	}

	private FormBuilder(Container container) {
		if(container==null) {
			container = new JPanel();
		}
		
		this.container = container;
		container.setLayout(new GridBagLayout());
	}
	
	public ResourceDomain getResourceDomain() {
		return resourceDomain;
	}

	public void setResourceDomain(ResourceDomain resourceDomain) {
		this.resourceDomain = resourceDomain;
	}

	private List<String> getIds() {
		if(ids==null) {
			ids = new ArrayList<>();
		}
		return ids;
	}
	
	private Map<String, FormEntry> getEntryMap() {
		if(entryMap==null) {
			entryMap = new HashMap<>();
		}
		return entryMap;
	}

	public <E extends FormEntry> E addEntry(String id, E entry) {
		insertEntry(id, entry, -1);
		return entry;
	}
	
	public int getEntryCount() {
		return ids==null ? 0 : ids.size();
	}
	
	public int indexOf(FormEntry entry) {
		for(int i=0; i<getIds().size(); i++) {
			String id = getIds().get(i);
			if(getEntryMap().get(id)==entry) {
				return i;
			}
		}
		
		return -1;
	}
	
	public int indexOf(String id) {
		return getIds().indexOf(id);
	}
	
	public String getIdAt(int index) {
		return getIds().get(index);
	}
	
	public FormEntry removeEntry(int index) {
		String id = getIds().remove(index);
		return getEntryMap().remove(id);
	}
	
	public FormEntry getEntry(int index) {
		String id = getIds().get(index);
		return getEntryMap().get(id);
	}
	
	public FormEntry getEntry(String id) {
		return getEntryMap().get(id);
	}

	public FormEntry removeEntry(String id) {
		FormEntry entry = getEntryMap().remove(id);
		
		if(entry!=null) {
			entry.setId(null);
			entry.setBuilder(null);
		}
		
		return entry;
	}
	
	public FormEntry removeEntry(FormEntry entry) {
		for(int i=0; i<getIds().size(); i++) {
			String id = getIds().get(i);
			if(getEntryMap().get(id)==entry) {
				getEntryMap().remove(id);
				getIds().remove(i);

				entry.setId(null);
				entry.setBuilder(null);
				
				break;
			}
		}
		
		return entry;
	}
	
	public void removeAllEntries() {
		getIds().clear();
		getEntryMap().clear();
	}
	
	public <E extends FormEntry> E insertEntry(String id, E entry, String afterId) {
		int index = indexOf(afterId);
		if(index!=-1) {
			index++;
		}
		return insertEntry(id, entry, index);
	}
	
	public <E extends FormEntry> E insertEntry(String id, E entry, int index) {
		if(entry==null)
			throw new NullPointerException("Invalid entry"); //$NON-NLS-1$
		if(id!=null && getEntryMap().containsKey(id))
			throw new DuplicateIdentifierException("Duplicate id: "+id); //$NON-NLS-1$
		if(getEntryMap().containsValue(entry))
			throw new IllegalArgumentException("Entry already added for id: "+id); //$NON-NLS-1$
		
		if(index==-1) {
			getIds().add(id);
		} else {
			getIds().add(index, id);
		}
		
		if(id!=null) {
			getEntryMap().put(id, entry);
		}
		
		entry.setId(id);
		entry.setBuilder(this);
		
		return entry;
	}
	
	public Container getContainer() {
		return container;
	}
	
	private int row;
	private int column;
	
	public void buildForm() {
		container.removeAll();
		
		if(ids==null || ids.isEmpty()) {
			return;
		}
		
		row = 0;
		column = 0;
		for(String id : ids) {
			FormEntry entry = entryMap.get(id);
			entry.addToForm(this);
			row++;
		}
	}
	
	public void pack() {
		container.setSize(container.getPreferredSize());
	}
	
	public InputFormEntry addInputFormEntry(String id) {
		return addEntry(id, new InputFormEntry());
	}
	
	public InputFormEntry addInputFormEntry(String id, String label) {
		return addInputFormEntry(id).setLabel(label);
	}
	
	public InputFormEntry addInputFormEntry(String id, String label, int columns) {
		return addInputFormEntry(id).setLabel(label).setColumns(columns);
	}
	
	public LocationFormEntry addLocationFormEntry(String id) {
		return addEntry(id, new LocationFormEntry());
	}
	
	public LocationFormEntry addLocationFormEntry(String id, String label) {
		return addLocationFormEntry(id).setLabel(label);
	}
	
	public PropertiesFormEntry addPropertiesFormEntry(String id) {
		return addEntry(id, new PropertiesFormEntry());
	}
	
	public PropertiesFormEntry addPropertiesFormEntry(String id, String keyLabel, String valueLabel) {
		return addPropertiesFormEntry(id).setKeyLabel(keyLabel).setValueLabel(valueLabel);
	}
	
	public ChoiceFormEntry addChoiceFormEntry(String id) {
		return addEntry(id, new ChoiceFormEntry());
	}
	
	public ChoiceFormEntry addChoiceFormEntry(String id, String label) {
		return addChoiceFormEntry(id).setLabel(label);
	}
	
	public ToggleFormEntry addToggleFormEntry(String id) {
		return addEntry(id, new ToggleFormEntry());
	}
	
	public ToggleFormEntry addToggleFormEntry(String id, String label) {
		return addEntry(id, new ToggleFormEntry(label));
	}
	
	public void addSeperator() {
		addEntry("sep_"+System.currentTimeMillis(), new SeparatorFormEntry()); //$NON-NLS-1$
	}

	public void feedComponent(Component comp) {
		feedComponent(comp, null, -1, -1);
	}

	public void feedComponent(Component comp, Insets insets) {
		feedComponent(comp, insets, -1, -1);
	}

	public void feedComponent(Component comp, Insets insets, int mode) {
		feedComponent(comp, insets, -1, mode);
	}
	
	public void feedComponent(Component comp, Insets insets, int anchor, int mode) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = column;
		gbc.gridy = row;
		
		if(insets==null) {
			insets = DEFAULT_CONTENT_INSETS;
		}

		gbc.insets = insets;
		
		if(anchor==-1) {
			anchor = GridBagConstraints.NORTHWEST;
		}
		gbc.anchor = anchor;
		
		switch(mode) {
		// Make component fill its column
		case RESIZE_HORIZONTAL:
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 100;
			break;
			
		// Make component fill entire remaining row
		case RESIZE_REMAINDER:
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			break;
			
		// Make component occupy all remaning space
		case RESIZE_FILL:
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 100;
			gbc.weighty = 100;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			break;
		}
		
		container.add(comp, gbc);
		
		column++;
	}
	
	public void feedLabel(String title) {
		if(resourceDomain!=null) {
			title = resourceDomain.get(title);
		}
		
		JLabel label = new JLabel(title);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		
		feedComponent(label, DEFAULT_LABEL_INSETS);
	}
	
	public void newLine() {
		if(column>0) {
			row++;
			column = 0;
		}
	}
	
	public void feedSeparator() {
		feedComponent(new JSeparator(SwingConstants.HORIZONTAL), null, RESIZE_REMAINDER);
		newLine();
	}
	
	public void feedRow(String label, Component comp) {
		feedLabel(label);
		feedComponent(comp);
		newLine();
	}
	
	public int getRow() {
		return row;
	}
	
	public int getColumn() {
		return column;
	}
	
	public Object getValue(int index) {
		return getEntry(index).getValue();
	}
	
	public Object getValue(String id) {
		return getEntry(id).getValue();
	}
	
	public void setValue(int index, Object value) {
		getEntry(index).setValue(value);
	}
	
	public void setValue(String id, Object value) {
		getEntry(id).setValue(value);
	}
	
	public void clear() {
		for(FormEntry entry : getEntryMap().values()) {
			entry.clear();
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static abstract class FormEntry {
		
		private String id;
		private FormBuilder builder;

		public String getId() {
			return id;
		}

		void setId(String id) {
			this.id = id;
		}

		public FormBuilder getBuilder() {
			return builder;
		}

		void setBuilder(FormBuilder builder) {
			this.builder = builder;
		}

		public abstract FormEntry addToForm(FormBuilder builder);
		
		public abstract FormEntry setValue(Object value);
		
		public abstract Object getValue();
		
		public abstract FormEntry clear();
	}
	
	public static class SeparatorFormEntry extends FormEntry {

		/**
		 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#addToForm(de.ims.icarus.ui.dialog.FormBuilder)
		 */
		@Override
		public FormEntry addToForm(FormBuilder builder) {
			builder.feedSeparator();
			return this;
		}

		/**
		 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
		 */
		@Override
		public FormEntry setValue(Object value) {
			return this;
		}

		/**
		 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
		 */
		@Override
		public Object getValue() {
			return null;
		}

		/**
		 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#clear()
		 */
		@Override
		public FormEntry clear() {
			return this;
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static abstract class AbstractFormEntry<E extends FormEntry> extends FormEntry {
		
		protected int resizeMode = RESIZE_NONE;

		public int getResizeMode() {
			return resizeMode;
		}

		@SuppressWarnings("unchecked")
		public E setResizeMode(int resizeMode) {
			this.resizeMode = resizeMode;
			
			return (E) this;
		}
	}
}
