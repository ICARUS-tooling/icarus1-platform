/*
 * $Revision: 26 $
 * $Date: 2013-04-21 19:52:49 +0200 (So, 21 Apr 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.language_tools/source/net/ikarus_systems/icarus/plugins/language_tools/treebank/DerivedTreebankEditor.java $
 *
 * $LastChangedDate: 2013-04-21 19:52:49 +0200 (So, 21 Apr 2013) $ 
 * $LastChangedRevision: 26 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.language_tools.treebank;

import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankRegistry;
import de.ims.icarus.language.treebank.swing.TreebankListCellRenderer;
import de.ims.icarus.language.treebank.swing.TreebankListModel;
import de.ims.icarus.ui.dialog.ChoiceFormEntry;

/**
 * @author Markus Gärtner
 * @version $Id: DerivedTreebankEditor.java 26 2013-04-21 17:52:49Z mcgaerty $
 *
 */
public class DerivedTreebankEditor extends BasicTreebankEditor {

	public DerivedTreebankEditor() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#setEditingItem(de.ims.icarus.language.treebank.Treebank)
	 */
	@Override
	public void setEditingItem(Treebank treebank) {
		if(!(treebank instanceof FilteredTreebank))
			throw new IllegalArgumentException("Unsupported treebank class: "+treebank.getClass()); //$NON-NLS-1$
		
		super.setEditingItem(treebank);
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#initForm()
	 */
	@Override
	protected void initForm() {
		super.initForm();
		
		TreebankListModel model = new TreebankListModel();
		ChoiceFormEntry entry = new ChoiceFormEntry(
				"plugins.languageTools.labels.base", model); //$NON-NLS-1$
		entry.getComboBox().setRenderer(new TreebankListCellRenderer());
		formBuilder.insertEntry("base", entry, "location"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#hasChanges()
	 */
	@Override
	public boolean hasChanges() {
		if(super.hasChanges()) {
			return true;
		}
		
		FilteredTreebank filteredTreebank = getEditingItem();
		
		if(formBuilder.getValue("base")!=filteredTreebank.getBase()) { //$NON-NLS-1$
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
		
		FilteredTreebank treebank = getEditingItem();
		ChoiceFormEntry entry = (ChoiceFormEntry) formBuilder.getEntry("base"); //$NON-NLS-1$
		TreebankListModel model = (TreebankListModel) entry.getComboBox().getModel();
		model.setExcludes(treebank);
		model.setSelectedItem(treebank.getBase());
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#applyEdit()
	 */
	@Override
	protected void doApplyEdit() {

		FilteredTreebank filteredTreebank = getEditingItem();
		setIgnoreTreebankEvents(true);
		try {
			super.doApplyEdit();
			
			filteredTreebank.setBase((Treebank) formBuilder.getValue("base")); //$NON-NLS-1$
			TreebankRegistry.getInstance().treebankChanged(filteredTreebank);
		} finally {
			setIgnoreTreebankEvents(false);
		}
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#getEditingItem()
	 */
	@Override
	public FilteredTreebank getEditingItem() {
		return (FilteredTreebank) super.getEditingItem();
	}

	/**
	 * @see de.ims.icarus.plugins.language_tools.treebank.BasicTreebankEditor#isPropertyKeyAllowed(java.lang.String)
	 */
	@Override
	protected boolean isPropertyKeyAllowed(String key) {
		return !FilteredTreebank.BASE_ID_PROPERTY.equals(key);
	}

}
