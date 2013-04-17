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

import net.ikarus_systems.icarus.resources.ResourceDomain;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.util.id.DuplicateIdentifierException;

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
	
	public static FormBuilder newBuilder(Container container) {
		return new FormBuilder(container);
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
	
	public FormEntry removeEntry(FormEntry entry) {
		for(int i=0; i<getIds().size(); i++) {
			String id = getIds().get(i);
			if(getEntryMap().get(id)==entry) {
				getEntryMap().remove(id);
				getIds().remove(i);
				break;
			}
		}
		
		return entry;
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
			throw new IllegalArgumentException("Invalid entry"); //$NON-NLS-1$
		if(getEntryMap().containsKey(id))
			throw new DuplicateIdentifierException("Duplicate id: "+id); //$NON-NLS-1$
		if(getEntryMap().containsValue(entry))
			throw new IllegalArgumentException("Entry already added for id: "+id); //$NON-NLS-1$
		
		if(index==-1) {
			getIds().add(id);
		} else {
			getIds().add(index, id);
		}
		
		getEntryMap().put(id, entry);
		
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
	
	public InputFormEntry addInputFormEntry(String id) {
		return addEntry(id, new InputFormEntry());
	}
	
	public InputFormEntry addInputFormEntry(String id, String label) {
		return addInputFormEntry(id).setLabel(label);
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
		feedComponent(new JSeparator(SwingConstants.HORIZONTAL), null, RESIZE_HORIZONTAL);
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
	public interface FormEntry {

		FormEntry addToForm(FormBuilder builder);
		
		FormEntry setValue(Object value);
		
		Object getValue();
		
		FormEntry clear();
	}
	
	public static abstract class AbstractFormEntry implements FormEntry {
		
		protected int resizeMode = RESIZE_NONE;

		public int getResizeMode() {
			return resizeMode;
		}

		public void setResizeMode(int resizeMode) {
			this.resizeMode = resizeMode;
		}
	}
}
