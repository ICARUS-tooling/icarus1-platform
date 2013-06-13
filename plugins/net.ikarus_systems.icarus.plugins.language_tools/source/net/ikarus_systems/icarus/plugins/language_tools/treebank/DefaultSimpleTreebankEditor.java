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

import javax.swing.ComboBoxModel;

import net.ikarus_systems.icarus.language.treebank.Treebank;
import net.ikarus_systems.icarus.language.treebank.TreebankRegistry;
import net.ikarus_systems.icarus.plugins.ExtensionListCellRenderer;
import net.ikarus_systems.icarus.plugins.ExtensionListModel;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.language_tools.LanguageToolsConstants;
import net.ikarus_systems.icarus.ui.dialog.ChoiceFormEntry;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;

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
	 * @see net.ikarus_systems.icarus.plugins.language_tools.treebank.BasicTreebankEditor#setEditingItem(net.ikarus_systems.icarus.language.treebank.Treebank)
	 */
	@Override
	public void setEditingItem(Treebank treebank) {
		if(!(treebank instanceof DefaultSimpleTreebank))
			throw new IllegalArgumentException("Unsupported treebank class: "+treebank.getClass()); //$NON-NLS-1$
		
		super.setEditingItem(treebank);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.treebank.BasicTreebankEditor#initForm()
	 */
	@Override
	protected void initForm() {
		super.initForm();

		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				LanguageToolsConstants.LANGUAGE_TOOLS_PLUGIN_ID, "SentenceDataReader"); //$NON-NLS-1$
		ComboBoxModel<Extension> model = new ExtensionListModel(
				extensionPoint.getConnectedExtensions(), true);
		
		ChoiceFormEntry entry = new ChoiceFormEntry(
				"plugins.languageTools.labels.reader", model); //$NON-NLS-1$
		entry.getComboBox().setRenderer(new ExtensionListCellRenderer());
		formBuilder.insertEntry("reader", entry, "location"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.treebank.BasicTreebankEditor#hasChanges()
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
	 * @see net.ikarus_systems.icarus.plugins.language_tools.treebank.BasicTreebankEditor#resetEdit()
	 */
	@Override
	protected void doResetEdit() {
		super.doResetEdit();
		
		DefaultSimpleTreebank simpleTreebank = getEditingItem();
		formBuilder.setValue("reader", simpleTreebank.getReader()); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.treebank.BasicTreebankEditor#applyEdit()
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
	 * @see net.ikarus_systems.icarus.plugins.language_tools.treebank.BasicTreebankEditor#getEditingItem()
	 */
	@Override
	public DefaultSimpleTreebank getEditingItem() {
		return (DefaultSimpleTreebank) super.getEditingItem();
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.treebank.BasicTreebankEditor#isPropertyKeyAllowed(java.lang.String)
	 */
	@Override
	protected boolean isPropertyKeyAllowed(String key) {
		return !DefaultSimpleTreebank.READER_EXTENSION_PROPERTY.equals(key);
	}
}