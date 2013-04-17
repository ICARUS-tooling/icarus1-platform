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

import javax.swing.ComboBoxModel;

import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.language.corpus.CorpusRegistry;
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
public class DefaultSimpleCorpusEditor extends BasicCorpusEditor {

	public DefaultSimpleCorpusEditor() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#setEditingItem(net.ikarus_systems.icarus.language.corpus.Corpus)
	 */
	@Override
	public void setEditingItem(Corpus corpus) {
		if(!(corpus instanceof DefaultSimpleCorpus))
			throw new IllegalArgumentException("Unsupported corpus class: "+corpus.getClass()); //$NON-NLS-1$
		
		super.setEditingItem(corpus);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#initForm()
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
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#hasChanges()
	 */
	@Override
	public boolean hasChanges() {
		if(super.hasChanges()) {
			return true;
		}
		
		DefaultSimpleCorpus simpleCorpus = getEditingItem();
		
		if(formBuilder.getValue("reader")!=simpleCorpus.getReader()) { //$NON-NLS-1$
			return true;
		}
		
		return false;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#resetEdit()
	 */
	@Override
	protected void doResetEdit() {
		super.doResetEdit();
		
		DefaultSimpleCorpus simpleCorpus = getEditingItem();
		formBuilder.setValue("reader", simpleCorpus.getReader()); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#applyEdit()
	 */
	@Override
	protected void doApplyEdit() {

		DefaultSimpleCorpus simpleCorpus = getEditingItem();
		setIgnoreCorpusEvents(true);
		try {
			super.doApplyEdit();
			
			simpleCorpus.setReader((Extension) formBuilder.getValue("reader")); //$NON-NLS-1$
			CorpusRegistry.getInstance().corpusChanged(simpleCorpus);
		} finally {
			setIgnoreCorpusEvents(false);
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#getEditingItem()
	 */
	@Override
	public DefaultSimpleCorpus getEditingItem() {
		return (DefaultSimpleCorpus) super.getEditingItem();
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#isPropertyKeyAllowed(java.lang.String)
	 */
	@Override
	protected boolean isPropertyKeyAllowed(String key) {
		return !DefaultSimpleCorpus.READER_EXTENSION_PROPERTY.equals(key);
	}
}
