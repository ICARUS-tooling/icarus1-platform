/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.table.PropertiesTableModel;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.KeyValuePair;
import de.ims.icarus.util.StringUtil;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PropertiesFormEntry extends LabeledFormEntry<PropertiesFormEntry> implements ActionListener, 
		ListSelectionListener, MouseListener, TableModelListener {

	protected final JTable propertiesTable;
	protected final PropertiesTableModel propertiesTableModel;
	
	protected final JButton propertyAddButton;
	protected final JButton propertyEditButton;
	protected final JButton propertyRemoveButton;
	
	protected Filter propertyFilter;
	
	protected final JPanel buttonPanel;
	protected final JPanel tablePanel;

	public PropertiesFormEntry() {
		this(null);
	}

	public PropertiesFormEntry(Map<String, Object> properties) {
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		
		propertyAddButton = new JButton();
		propertyAddButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("add_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(propertyAddButton, "add", null); //$NON-NLS-1$
		resourceDomain.addComponent(propertyAddButton);
		propertyAddButton.addActionListener(this);
		
		propertyRemoveButton = new JButton();
		propertyRemoveButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("delete_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(propertyRemoveButton, "delete", null); //$NON-NLS-1$
		resourceDomain.addComponent(propertyRemoveButton);
		propertyRemoveButton.addActionListener(this);
		
		propertyEditButton = new JButton();
		propertyEditButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("write_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(propertyEditButton, "edit", null); //$NON-NLS-1$
		resourceDomain.addComponent(propertyEditButton);
		propertyEditButton.addActionListener(this);
		
		propertiesTableModel = new PropertiesTableModel();
		propertiesTable = new JTable(propertiesTableModel);
		UIUtil.disableHtml(propertiesTable.getDefaultRenderer(Object.class));
		propertiesTable.setBorder(UIUtil.defaultContentBorder);
		propertiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		propertiesTable.getTableHeader().setReorderingAllowed(false);
		propertiesTable.getSelectionModel().addListSelectionListener(this);
		propertiesTable.addMouseListener(this);
		propertiesTable.setIntercellSpacing(new Dimension(4, 4));
		
		buttonPanel = new JPanel(new GridLayout(3, 1));
		buttonPanel.add(propertyAddButton);
		buttonPanel.add(propertyRemoveButton);
		buttonPanel.add(propertyEditButton);
		
		tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(propertiesTable.getTableHeader(), BorderLayout.NORTH);
		tablePanel.add(propertiesTable, BorderLayout.CENTER);
		
		propertiesTableModel.setProperties(properties);
		
		refreshButtons();
		
		setResizeMode(FormBuilder.RESIZE_FILL);
	}

	public Filter getPropertyFilter() {
		return propertyFilter;
	}

	public PropertiesFormEntry setPropertyFilter(Filter propertyFilter) {
		this.propertyFilter = propertyFilter;
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public PropertiesFormEntry setValue(Object value) {
		propertiesTableModel.setProperties((Map<String, Object>) value);
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.LabeledFormEntry#addComponents(de.ims.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	protected void addComponents(FormBuilder builder) {
		if(getLabel()==null) {
			// "Normal" layout - buttonPanel aligned with the
			// general label column.
			builder.feedComponent(buttonPanel, FormBuilder.DEFAULT_LABEL_INSETS);
			builder.feedComponent(tablePanel, null, getResizeMode());
		} else {
			// Allows the entire properties form including buttonPanel
			// to be inlined in the general "content" column to the right.
			// We therefore wrap another form layout via a JPanel instance 
			// and add our stuff there before forwarding to the original builder.
			JPanel panel = new JPanel();
			FormBuilder fb = FormBuilder.newBuilder(panel);
			fb.feedComponent(buttonPanel, FormBuilder.DEFAULT_LABEL_INSETS);
			fb.feedComponent(tablePanel, null, getResizeMode());
			builder.feedComponent(panel, null, getResizeMode());
		}
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {
		return propertiesTableModel.getProperties();
	}
	
	public String getKeyLabel() {
		return propertiesTableModel.getKeyLabel();
	}

	public String getValueLabel() {
		return propertiesTableModel.getValueLabel();
	}

	public PropertiesFormEntry setKeyLabel(String keyLabel) {
		propertiesTableModel.setKeyLabel(keyLabel);
		return this;
	}

	public PropertiesFormEntry setValueLabel(String valueLabel) {
		propertiesTableModel.setValueLabel(valueLabel);
		return this;
	}

	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
			int row = propertiesTable.rowAtPoint(e.getPoint());
			if(row==-1) {
				return;
			}
			String key = propertiesTableModel.getKey(row);
			if(key==null) {
				return;
			}
			
			try {
				editProperty(key);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to edit property '"+key+"'", ex); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// no-op
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// no-op
	}

	/**
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// no-op
	}

	/**
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// no-op
	}
	
	protected void refreshButtons() {
		boolean enabled = propertiesTable.getSelectedRow()!=-1;
		propertyEditButton.setEnabled(enabled);
		propertyRemoveButton.setEnabled(enabled);
	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		refreshButtons();
	}
	
	protected boolean isPropertyKeyAllowed(String key) {
		return propertyFilter==null ? true : propertyFilter.accepts(key);
	}
	
	protected String getStringValue(String key) {
		Object value = key==null ? null : propertiesTableModel.getValue(key);
		return value==null ? null : String.valueOf(value);
	}
	
	protected String getValidKey(String baseKey) {
		int count = Math.max(1, StringUtil.getCurrentCount(baseKey));
		baseKey = StringUtil.getBaseName(baseKey);
		
		String key = null;
		while(!isPropertyKeyAllowed((key = baseKey+" ("+count+")")) //$NON-NLS-1$ //$NON-NLS-2$
				|| propertiesTableModel.containsKey(key)) {
			count++;
		}
		return key;
	}
	
	protected void addProperty() {
		editProperty(null);
	}
	
	protected void removeProperty(String key) {
		if(DialogFactory.getGlobalFactory().showConfirm(null, 
				"dialogs.deleteProperty.title",  //$NON-NLS-1$
				"dialogs.deleteProperty.message",  //$NON-NLS-1$
				key)) {
			propertiesTableModel.setValue(key, null);
		}
	}
	
	protected void editProperty(String key) {
		String originalKey = key;
		
		String value = getStringValue(key);
		
		KeyValuePair<String, String> entry = DialogFactory.getGlobalFactory().showPropertyEditDialog(null, 
				"dialogs.addProperty.title",  //$NON-NLS-1$
				"dialogs.addProperty.message",  //$NON-NLS-1$
				key, value, null);
		
		// Cancelled by user
		if(entry==null) {
			return;
		}
		
		key = entry.getKey();
		value = entry.getValue();
		
		// Only non-empty strings are allowed for keys and values!
		if(key==null || key.isEmpty() || value==null || value.isEmpty()) {
			return;
		}
		
		// Check for illegal property keys that are reserved
		if(!isPropertyKeyAllowed(key)) {
			String baseKey = key;
			key = getValidKey(baseKey);
			
			DialogFactory.getGlobalFactory().showInfo(null, 
					"dialogs.info",  //$NON-NLS-1$
					"dialogs.invalidKey",  //$NON-NLS-1$
					baseKey, key);
		}
		
		// Check for duplicate property keys
		if(!key.equals(originalKey) && propertiesTableModel.containsKey(key)) {
			String baseKey = key;
			key = getValidKey(baseKey);
			
			DialogFactory.getGlobalFactory().showInfo(null, 
					"dialogs.info",  //$NON-NLS-1$
					"dialogs.duplicateKey",  //$NON-NLS-1$
					baseKey, key);			
		}
		
		// Set new property value
		propertiesTableModel.setValue(key, value);
		// Remove old property value if key has changed
		if(originalKey!=null && !originalKey.equals(key)) {
			propertiesTableModel.setValue(originalKey, null);
		}
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// Add property
		if(e.getSource()==propertyAddButton) {
			
			try {
				addProperty();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add property", ex); //$NON-NLS-1$
			}
			return;
		}
		
		// Remove property
		if(e.getSource()==propertyRemoveButton) {
			int row = propertiesTable.getSelectedRow();
			if(row==-1) {
				return;
			}
			String key = propertiesTableModel.getKey(row);
			if(key==null) {
				return;
			}
			
			try {
				removeProperty(key);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to remove property '"+key+"'", ex); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return;
		}
		
		// Edit property
		if(e.getSource()==propertyEditButton) {
			int row = propertiesTable.getSelectedRow();
			if(row==-1) {
				return;
			}
			String key = propertiesTableModel.getKey(row);
			if(key==null) {
				return;
			}
			
			try {
				editProperty(key);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to edit property '"+key+"'", ex); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return;
		}
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public PropertiesFormEntry clear() {
		propertiesTableModel.clear();
		propertiesTable.getSelectionModel().clearSelection();
		return this;
	}

	/**
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	@Override
	public void tableChanged(TableModelEvent e) {
		refreshButtons();
	}
}
