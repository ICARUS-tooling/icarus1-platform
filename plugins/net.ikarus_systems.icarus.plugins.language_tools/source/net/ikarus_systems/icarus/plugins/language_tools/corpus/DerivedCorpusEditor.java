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

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.language.corpus.CorpusRegistry;
import net.ikarus_systems.icarus.language.corpus.swing.CorpusListCellRenderer;
import net.ikarus_systems.icarus.language.corpus.swing.CorpusListModel;
import net.ikarus_systems.icarus.resources.ResourceDomain;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.GridBagUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DerivedCorpusEditor extends BasicCorpusEditor {
	
	protected JComboBox<Corpus> baseCorpusSelect;
	protected CorpusListModel baseListModel;

	public DerivedCorpusEditor() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#feedBasicComponents(javax.swing.JPanel)
	 */
	@Override
	protected int feedBasicComponents(JPanel panel) {
		int row = super.feedBasicComponents(panel);
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		JLabel label;
		
		GridBagConstraints gbc = GridBagUtil.makeGbc(0, row);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(1, 2, 1, 2);
		
		// Base 
		label = new JLabel();
		resourceDomain.prepareComponent(label, 
				"plugins.languageTools.labels.base", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(baseCorpusSelect, gbc);

		return ++gbc.gridy;
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
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#init()
	 */
	@Override
	protected void init() {
		super.init();
		
		baseListModel = new CorpusListModel();
		baseCorpusSelect = new JComboBox<>(baseListModel);
		baseCorpusSelect.setRenderer(new CorpusListCellRenderer());
		baseCorpusSelect.setEditable(false);
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
		
		if(baseCorpusSelect.getSelectedItem()!=filteredCorpus.getBase()) {
			return true;
		}
		
		return false;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#resetEdit()
	 */
	@Override
	public void resetEdit() {
		if(contentPanel==null) {
			return;
		}
		super.resetEdit();
		
		if(corpus==null) {
			baseListModel.setSelectedItem(null);
			return;
		}
		
		FilteredCorpus corpus = getEditingItem();
		baseListModel.setExcludes(corpus);
		baseListModel.setSelectedItem(corpus.getBase());
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.language_tools.corpus.BasicCorpusEditor#applyEdit()
	 */
	@Override
	public void applyEdit() {
		if(contentPanel==null) {
			return;
		}
		if(corpus==null) {
			return;
		}

		FilteredCorpus filteredCorpus = getEditingItem();
		setIgnoreCorpusEvents(true);
		try {
			super.applyEdit();
			
			filteredCorpus.setBase((Corpus) baseCorpusSelect.getSelectedItem());
		} finally {
			setIgnoreCorpusEvents(false);
		}
		CorpusRegistry.getInstance().corpusChanged(filteredCorpus);
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
