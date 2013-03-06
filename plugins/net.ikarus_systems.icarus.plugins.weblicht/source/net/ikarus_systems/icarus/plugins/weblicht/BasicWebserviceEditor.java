package net.ikarus_systems.icarus.plugins.weblicht;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.resources.ResourceDomain;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.GridBagUtil;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.helper.Editor;
import net.ikarus_systems.icarus.util.KeyValuePair;
import net.ikarus_systems.icarus.util.Location;

public class BasicWebserviceEditor implements Editor<Webservice> {

	protected JPanel contentPanel;

	protected Collection<JComponent> localizedComponents;

	protected JTextField nameInput;
	protected JButton propertyAddButton;
	protected JButton propertyEditButton;
	protected JButton propertyRemoveButton;

	protected JTable propertiesTable;
	protected PropertiesTableModel propertiesTableModel;

	protected Webservice webservice;

	protected Handler handler;

	public BasicWebserviceEditor() {
		// no-op
	}

	protected int feedBasicComponents(JPanel panel) {
		ResourceDomain resourceDomain = ResourceManager.getInstance()
				.getGlobalDomain();
		JLabel label;

		GridBagConstraints gbc = GridBagUtil.makeGbc(0, 0);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(1, 2, 1, 2);

		// Name
		label = new JLabel();
		resourceDomain.prepareComponent(label,
				"plugins.languageTools.labels.name", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbc);
		gbc.gridx++;
		panel.add(nameInput, gbc);

		panel.add(Box.createGlue(), GridBagUtil.makeGbcH(gbc.gridx + 1,
				gbc.gridy, GridBagConstraints.REMAINDER, 1));

		return ++gbc.gridy;
	}

	protected void feedEditorComponent(JPanel panel) {
		panel.setLayout(new GridBagLayout());

		int row = feedBasicComponents(panel);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = row;
		panel.add(Box.createVerticalStrut(10), gbc);

		gbc.gridy++;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.NORTH;
		JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
		buttonPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
		buttonPanel.add(propertyAddButton);
		buttonPanel.add(propertyRemoveButton);
		buttonPanel.add(propertyEditButton);
		panel.add(buttonPanel, gbc);

		gbc.gridx++;
		gbc.gridheight = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(propertiesTable.getTableHeader(), gbc);

		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 100;
		gbc.weightx = 100;
		panel.add(propertiesTable, gbc);
	}

	protected Handler createHandler() {
		return new Handler();
	}

	protected void init() {
		localizedComponents = new ArrayList<>();

		handler = createHandler();

		nameInput = new JTextField(30);

		ResourceDomain resourceDomain = ResourceManager.getInstance()
				.getGlobalDomain();

		propertyAddButton = new JButton();
		propertyAddButton.setIcon(IconRegistry.getGlobalRegistry().getIcon(
				"add_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(propertyAddButton, "add", null); //$NON-NLS-1$
		resourceDomain.addComponent(propertyAddButton);
		localizedComponents.add(propertyAddButton);
		propertyAddButton.addActionListener(handler);

		propertyRemoveButton = new JButton();
		propertyRemoveButton.setIcon(IconRegistry.getGlobalRegistry().getIcon(
				"delete_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(propertyRemoveButton, "delete", null); //$NON-NLS-1$
		resourceDomain.addComponent(propertyRemoveButton);
		localizedComponents.add(propertyRemoveButton);
		propertyRemoveButton.addActionListener(handler);

		propertyEditButton = new JButton();
		propertyEditButton.setIcon(IconRegistry.getGlobalRegistry().getIcon(
				"write_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(propertyEditButton, "edit", null); //$NON-NLS-1$
		resourceDomain.addComponent(propertyEditButton);
		localizedComponents.add(propertyEditButton);
		propertyEditButton.addActionListener(handler);

		propertiesTableModel = new PropertiesTableModel();
		propertiesTable = new JTable(propertiesTableModel);
		propertiesTable.setBorder(UIUtil.defaultContentBorder);
		propertiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		propertiesTable.getTableHeader().setReorderingAllowed(false);
		propertiesTable.getSelectionModel().addListSelectionListener(handler);
		propertiesTable.addMouseListener(handler);
		propertiesTable.setIntercellSpacing(new Dimension(4, 4));

		// CorpusRegistry.getInstance().addListener(Events.REMOVED, handler);
	}

	protected String getWebserviceName() {
		return webservice == null ? "<undefined>" : webservice.getName(); //$NON-NLS-1$
	}

	@Override
	public Component getEditorComponent() {
		if (contentPanel == null) {
			contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			init();
			feedEditorComponent(contentPanel);

			resetEdit();
		}
		return contentPanel;
	}

	@Override
	public void setEditingItem(Webservice webservice) {
		if (this.webservice == webservice) {
			return;
		}

		this.webservice = webservice;

		resetEdit();
	}

	@Override
	public Webservice getEditingItem() {
		return webservice;
	}

	@Override
	public void resetEdit() {
		if (contentPanel == null) {
			return;
		}
		if (webservice == null) {
			nameInput.setText(null);

			return;
		}

		// Name
		nameInput.setText(webservice.getName());

		// Properties
		propertiesTableModel.reload();

	}

	@Override
	public void applyEdit() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasChanges() {
		if (contentPanel == null) {
			return false;
		}
		if (webservice == null) {
			return false;
		}

		// Compare name
		if (!nameInput.getText().equals(webservice.getName())) {
			return true;
		}

		// Compare complete set of properties
		Map<String, Object> properties = propertiesTableModel.properties;
		if ((properties == null || properties.isEmpty()) != webservice
				.getProperties().isEmpty()) {
			return true;
		}
		if (properties.size() != webservice.getProperties().size()) {
			return true;
		}
		if (properties != null) {
			for (Entry<String, Object> entry : properties.entrySet()) {
				Object value = webservice.getProperty(entry.getKey());
				if (value == null && entry.getValue() == null) {
					continue;
				}
				if ((value == null) != (entry.getValue() == null)) {
					return true;
				}
				if (!value.equals(entry.getValue())) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void close() {
		// CorpusRegistry.getInstance().removeListener(handler);

		if (localizedComponents == null) {
			return;
		}
		for (JComponent component : localizedComponents) {
			ResourceManager.getInstance().removeLocalizableItem(component);
		}

		webservice = null;

	}

	protected void addProperty() {
		editProperty(null);
	}

	protected void removeProperty(String key) {
		if (DialogFactory
				.getGlobalFactory()
				.showConfirm(
						null,
						"plugins.weblicht.weblichtEditView.dialogs.deleteProperty.title", //$NON-NLS-1$
						"plugins.weblicht.weblichtEditView.dialogs.deleteProperty.message", //$NON-NLS-1$
						key)) {
			propertiesTableModel.setValue(key, null);
		}
	}

	protected void editProperty(String key) {
		String originalKey = key;

		String value = key == null ? null : propertiesTableModel.getValue(key);

		KeyValuePair<String, String> entry = DialogFactory
				.getGlobalFactory()
				.showPropertyEditDialog(
						null,
						"plugins.weblicht.weblichtEditView.addProperty.title", //$NON-NLS-1$
						"plugins.weblicht.weblichtEditView.addProperty.message", //$NON-NLS-1$
						key, value, null);

		// Cancelled by user
		if (entry == null) {
			return;
		}

		key = entry.getKey();
		value = entry.getValue();

		// Only non-empty strings are allowed for keys and values!
		if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
			return;
		}

		// Check for illegal property keys that are reserved by subclasses
		if (!isPropertyKeyAllowed(key)) {
			String baseKey = key;
			key = getValidKey(baseKey);

			DialogFactory.getGlobalFactory().showInfo(null,
					"plugins.weblicht.weblichtEditView.dialogs.title", //$NON-NLS-1$
					"plugins.weblicht.weblichtEditView.dialogs.invalidKey", //$NON-NLS-1$
					baseKey, key);
		}

		// Check for duplicate property keys
		if (!key.equals(originalKey) && propertiesTableModel.containsKey(key)) {
			String baseKey = key;
			key = getValidKey(baseKey);

			DialogFactory.getGlobalFactory().showInfo(null,
					"plugins.weblicht.weblichtEditView.dialogs.title", //$NON-NLS-1$
					"plugins.weblicht.weblichtEditView.dialogs.duplicateKey", //$NON-NLS-1$
					baseKey, key);
		}

		// Set new property value
		propertiesTableModel.setValue(key, value);
		// Remove all property value if key has changed
		if (originalKey != null && !originalKey.equals(key)) {
			propertiesTableModel.setValue(originalKey, null);
		}
	}

	protected void filterProperties(Map<String, Object> properties) {
		for (Iterator<Entry<String, Object>> i = properties.entrySet()
				.iterator(); i.hasNext();) {
			Entry<String, Object> entry = i.next();
			if (!isPropertyKeyAllowed(entry.getKey())) {
				i.remove();
			}
		}
	}

	protected boolean isPropertyKeyAllowed(String key) {
		return true;
	}

	protected static Pattern indexPattern;

	private String getValidKey(String baseKey) {
		int count = 1;

		if (indexPattern == null) {
			indexPattern = Pattern.compile("\\((\\d+)\\)$"); //$NON-NLS-1$
		}
		Matcher matcher = indexPattern.matcher(baseKey);
		if (matcher.find()) {
			int currentCount = 0;
			try {
				currentCount = Integer.parseInt(matcher.group(1));
			} catch (NumberFormatException e) {
				LoggerFactory
						.getLogger(BasicWebserviceEditor.class)
						.log(LoggerFactory
								.record(Level.SEVERE,
										"Failed to parse existing base key index suffix: " + baseKey, e)); //$NON-NLS-1$
			}

			count = Math.max(count, currentCount + 1);
			baseKey = baseKey.substring(0,
					baseKey.length() - matcher.group().length()).trim();
		}

		String key = null;
		while (!isPropertyKeyAllowed((key = baseKey + " (" + count + ")")) //$NON-NLS-1$ //$NON-NLS-2$
				|| propertiesTableModel.containsKey(key)) {
			count++;
		}
		return key;
	}

	// --
	protected class Handler extends MouseAdapter implements ActionListener,
			ListSelectionListener, EventListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {

			// Add Webservice
			if (e.getSource() == propertyAddButton) {

				try {
					addProperty();
				} catch (Exception ex) {
					LoggerFactory
							.getLogger(BasicWebserviceEditor.class)
							.log(LoggerFactory
									.record(Level.SEVERE,
											"Failed to add webservice to webservicechain " + getWebserviceName(), ex)); //$NON-NLS-1$
				}
				return;
			}

			// Remove Webservice
			if (e.getSource() == propertyRemoveButton) {
				int row = propertiesTable.getSelectedRow();
				if (row == -1) {
					return;
				}
				String key = propertiesTableModel.getKey(row);
				if (key == null) {
					return;
				}

				try {
					removeProperty(key);
				} catch (Exception ex) {
					LoggerFactory
							.getLogger(BasicWebserviceEditor.class)
							.log(LoggerFactory
									.record(Level.SEVERE,
											"Failed to remove webservice '" + key + "' of " + getWebserviceName(), ex)); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return;
			}

			// Edit property
			if (e.getSource() == propertyEditButton) {
				int row = propertiesTable.getSelectedRow();
				if (row == -1) {
					return;
				}
				String key = propertiesTableModel.getKey(row);
				if (key == null) {
					return;
				}

				try {
					editProperty(key);
				} catch (Exception ex) {
					LoggerFactory
							.getLogger(BasicWebserviceEditor.class)
							.log(LoggerFactory
									.record(Level.SEVERE,
											"Failed to edit corpus property '" + key + "' of " + getWebserviceName(), ex)); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return;
			}
		}

		@Override
		public void invoke(Object sender, EventObject event) {
			resetEdit();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			boolean enabled = e.getFirstIndex()>-1;
			propertyEditButton.setEnabled(enabled);
			propertyRemoveButton.setEnabled(enabled);
			
		}

	}

	protected class PropertiesTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -2831414296483390759L;

		protected List<String> keys;
		protected Map<String, Object> properties;

		protected void reload() {
			if (webservice == null) {
				keys = null;
				properties = null;
			} else {
				properties = new HashMap<>(webservice.getProperties());
				filterProperties(properties);
				keys = new ArrayList<>(properties.keySet());
				Collections.sort(keys);
			}
			fireTableDataChanged();
		}

		protected String getValue(String key) {
			return properties == null ? null : String.valueOf(properties
					.get(key));
		}

		protected void setValue(String key, String value) {
			if (properties == null) {
				properties = new HashMap<>();
			}

			if (value == null) {
				properties.remove(key);
			} else {
				properties.put(key, value);
			}
			keys = new ArrayList<>(properties.keySet());
			Collections.sort(keys);
			fireTableDataChanged();
		}

		protected String getKey(int rowIndex) {
			return keys == null ? null : keys.get(rowIndex);
		}

		protected boolean containsKey(String key) {
			return properties == null ? false : properties.containsKey(key);
		}

		/**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return keys == null ? 0 : keys.size();
		}

		/**
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			String key = column == 0 ? "plugins.weblicht.labels.property" //$NON-NLS-1$
					: "plugins.weblicht.labels.value"; //$NON-NLS-1$
			return ResourceManager.getInstance().get(key);
		}

		/**
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return 2;
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (keys == null) {
				return null;
			}

			String key = keys.get(rowIndex);

			if (columnIndex == 0) {
				return key;
			} else {
				return properties.get(key);
			}
		}

	}
}
