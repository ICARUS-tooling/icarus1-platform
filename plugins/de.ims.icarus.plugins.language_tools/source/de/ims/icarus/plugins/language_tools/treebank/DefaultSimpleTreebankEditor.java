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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;

import de.ims.icarus.language.SentenceDataReader;
import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankRegistry;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.ExtensionListModel;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.language_tools.LanguageToolsConstants;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.dialog.ChoiceFormEntry;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.helper.Configurable;
import de.ims.icarus.util.PropertyOwner;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultSimpleTreebankEditor extends BasicTreebankEditor {

	public DefaultSimpleTreebankEditor() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#setEditingItem(de.ims.icarus.language.treebank.Treebank)
	 */
	@Override
	public void setEditingItem(Treebank treebank) {
		if(!(treebank instanceof DefaultSimpleTreebank))
			throw new IllegalArgumentException("Unsupported treebank class: "+treebank.getClass()); //$NON-NLS-1$

		super.setEditingItem(treebank);
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#initForm()
	 */
	@Override
	protected void initForm() {
		super.initForm();

		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				LanguageToolsConstants.LANGUAGE_TOOLS_PLUGIN_ID, "SentenceDataReader"); //$NON-NLS-1$
		ComboBoxModel<Extension> model = new ExtensionListModel(
				extensionPoint.getConnectedExtensions(), true);

		ChoiceFormEntry entry = new ReaderChoiceFormEntry(
				"plugins.languageTools.labels.reader", model); //$NON-NLS-1$
		formBuilder.insertEntry("reader", entry, "location"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#hasChanges()
	 */
	@Override
	public boolean hasChanges() {
		if(super.hasChanges()) {
			return true;
		}

		DefaultSimpleTreebank simpleTreebank = getEditingItem();

		if(formBuilder.getValue("reader")!=simpleTreebank.getReader()) { //$NON-NLS-1$
			return true;
		}

		return false;
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#resetEdit()
	 */
	@Override
	protected void doResetEdit() {
		super.doResetEdit();

		DefaultSimpleTreebank simpleTreebank = getEditingItem();
		formBuilder.setValue("reader", simpleTreebank.getReader()); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#applyEdit()
	 */
	@Override
	protected void doApplyEdit() {

		DefaultSimpleTreebank simpleTreebank = getEditingItem();
		setIgnoreTreebankEvents(true);
		try {
			super.doApplyEdit();

			simpleTreebank.setReader((Extension) formBuilder.getValue("reader")); //$NON-NLS-1$
			TreebankRegistry.getInstance().treebankChanged(simpleTreebank);
		} finally {
			setIgnoreTreebankEvents(false);
		}
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#getEditingItem()
	 */
	@Override
	public DefaultSimpleTreebank getEditingItem() {
		return (DefaultSimpleTreebank) super.getEditingItem();
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#isPropertyKeyAllowed(java.lang.String)
	 */
	@Override
	protected boolean isPropertyKeyAllowed(String key) {
		return !DefaultSimpleTreebank.READER_EXTENSION_PROPERTY.equals(key);
	}

	protected static boolean isConfigurablePropertyOwner(Object obj) {
		return obj instanceof Configurable && obj instanceof PropertyOwner;
	}

	protected Map<String, Object> extractReaderProperties(Map<String, Object> source) {
		Map<String, Object> result = new HashMap<>();

		String prefix = DefaultSimpleTreebank.READER_PROPERTY_PREFIX;

		if(source!=null) {
			for(Entry<String, Object> entry : source.entrySet()) {
				if(entry.getKey().startsWith(prefix)) {
					String key = entry.getKey().substring(prefix.length());
					result.put(key, entry.getValue());
				}
			}
		}

		return result;
	}

	protected void addReaderProperties(Map<String, Object> source,
			Map<String, Object> readerProperties) {
		if(source==null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$

		if(readerProperties==null || readerProperties.isEmpty()) {
			return;
		}

		String prefix = DefaultSimpleTreebank.READER_PROPERTY_PREFIX;

		for(Entry<String, Object> entry : readerProperties.entrySet()) {
			String key = prefix+entry.getKey();
			source.put(key, entry.getValue());
		}
	}

	protected void removeReaderProperties(Map<String, Object> properties,
			Map<String, Object> readerProperties) {
		if(properties==null)
			throw new NullPointerException("Invalid properties"); //$NON-NLS-1$

		String prefix = DefaultSimpleTreebank.READER_PROPERTY_PREFIX;

		for(Iterator<Entry<String, Object>> i = properties.entrySet().iterator(); i.hasNext();) {
			if(i.next().getKey().startsWith(prefix)) {
				i.remove();
			}
		}
	}


	protected class ReaderChoiceFormEntry extends ChoiceFormEntry implements ActionListener {

		protected JButton openConfigButton;

		public ReaderChoiceFormEntry(String label, ComboBoxModel<?> model) {
			super(label, model);

			setRenderer(new ExtensionListCellRenderer());

			openConfigButton = new JButton();
			openConfigButton.setFocusable(false);
			openConfigButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("settings.gif")); //$NON-NLS-1$
			openConfigButton.addActionListener(this);
			openConfigButton.setPreferredSize(new Dimension(20, 20));

			comboBox.addActionListener(this);
		}

		protected void refreshButtonEnabled() {
			DefaultSimpleTreebank treebank = getEditingItem();
			boolean enabled = treebank!=null;
			if(enabled) {
				enabled = treebank.getReader()!=null;
			}
			if(enabled) {
				enabled = isConfigurablePropertyOwner(treebank.getSentenceDataReader());
			}
			openConfigButton.setEnabled(enabled);
		}

		public JButton getOpenConfigButton() {
			return openConfigButton;
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() instanceof JComboBox) {
				refreshButtonEnabled();
				return;
			}

			DefaultSimpleTreebank treebank = getEditingItem();
			if(treebank==null) {
				return;
			}
			if(treebank.getReader()==null) {
				return;
			}

			SentenceDataReader reader = treebank.getSentenceDataReader();
			if(reader==null) {
				return;
			}

			if(reader instanceof Configurable) {
				try {
					((Configurable)reader).openConfig();
				} catch(Exception ex) {
					// TODO
				}
			}
		}

		@Override
		public void addComponents(FormBuilder builder) {
			super.addComponents(builder);
			builder.feedComponent(openConfigButton, new Insets(0, 5, 0, 0));
		}

		@Override
		public ReaderChoiceFormEntry setValue(Object value) {
			super.setValue(value);

			refreshButtonEnabled();

			return this;
		}

		@Override
		public ReaderChoiceFormEntry clear() {
			super.clear();

			refreshButtonEnabled();

			return this;
		}

	}
}
