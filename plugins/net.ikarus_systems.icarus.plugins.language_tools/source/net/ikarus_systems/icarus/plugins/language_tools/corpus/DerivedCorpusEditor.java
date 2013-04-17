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

import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.language.corpus.CorpusRegistry;
import net.ikarus_systems.icarus.language.corpus.swing.CorpusListCellRenderer;
import net.ikarus_systems.icarus.language.corpus.swing.CorpusListModel;
import net.ikarus_systems.icarus.ui.dialog.ChoiceFormEntry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DerivedCorpusEditor extends BasicCorpusEditor {

	public DerivedCorpusEditor() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#setEditingItem(net.ikarus_systems.icarus.language.corpus.Corpus)
	 */
	@Override
	public void setEditingItem(Corpus corpus) {
		if(!(corpus instanceof FilteredCorpus))
			throw new IllegalArgumentException("Unsupported corpus class: "+corpus.getClass()); //$NON-NLS-1$
		
		super.setEditingItem(corpus);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#initForm()
	 */
	@Override
	protected void initForm() {
		super.initForm();
		
		CorpusListModel model = new CorpusListModel();
		ChoiceFormEntry entry = new ChoiceFormEntry(
				"plugins.languageTools.labels.base", model); //$NON-NLS-1$
		entry.getComboBox().setRenderer(new CorpusListCellRenderer());
		formBuilder.insertEntry("base", entry, "location"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#hasChanges()
	 */
	@Override
	public boolean hasChanges() {
		if(super.hasChanges()) {
			return true;
		}
		
		FilteredCorpus filteredCorpus = getEditingItem();
		
		if(formBuilder.getValue("base")!=filteredCorpus.getBase()) { //$NON-NLS-1$
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
		
		FilteredCorpus corpus = getEditingItem();
		ChoiceFormEntry entry = (ChoiceFormEntry) formBuilder.getEntry("base"); //$NON-NLS-1$
		CorpusListModel model = (CorpusListModel) entry.getComboBox().getModel();
		model.setExcludes(corpus);
		model.setSelectedItem(corpus.getBase());
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#applyEdit()
	 */
	@Override
	protected void doApplyEdit() {

		FilteredCorpus filteredCorpus = getEditingItem();
		setIgnoreCorpusEvents(true);
		try {
			super.doApplyEdit();
			
			filteredCorpus.setBase((Corpus) formBuilder.getValue("base")); //$NON-NLS-1$
			CorpusRegistry.getInstance().corpusChanged(filteredCorpus);
		} finally {
			setIgnoreCorpusEvents(false);
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#getEditingItem()
	 */
	@Override
	public FilteredCorpus getEditingItem() {
		return (FilteredCorpus) super.getEditingItem();
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#isPropertyKeyAllowed(java.lang.String)
	 */
	@Override
	protected boolean isPropertyKeyAllowed(String key) {
		return !FilteredCorpus.BASE_ID_PROPERTY.equals(key);
	}

}
