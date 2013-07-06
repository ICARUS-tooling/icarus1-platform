/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.corpus;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import net.ikarus_systems.icarus.language.treebank.Treebank;
import net.ikarus_systems.icarus.language.treebank.TreebankListDelegate;
import net.ikarus_systems.icarus.language.treebank.TreebankRegistry;
import net.ikarus_systems.icarus.language.treebank.swing.TreebankListCellRenderer;
import net.ikarus_systems.icarus.language.treebank.swing.TreebankListModel;
import net.ikarus_systems.icarus.search_tools.SearchTargetSelector;
import net.ikarus_systems.icarus.util.Filter;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankTargetSelector implements SearchTargetSelector, Filter {

	protected JList<Treebank> list;
	protected JScrollPane scrollPane;
	protected ContentType contentType;
	
	public TreebankTargetSelector() {
		// no-op
	}
	
	protected void ensureUI() {
		if(list==null) {
			TreebankListModel model = new TreebankListModel(this);
			model.setDummyTreebankAllowed(false);
			list = new JList<>(model);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setCellRenderer(new TreebankListCellRenderer());
			list.setBorder(null);
			list.setSelectedIndex(0);
			
			scrollPane = new JScrollPane(list);
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchTargetSelector#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		ensureUI();
		Treebank treebank = list.getSelectedValue();
		
		return treebank==null ? null : TreebankRegistry.getInstance().getListDelegate(treebank);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchTargetSelector#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object item) {
		ensureUI();
		
		if(item instanceof TreebankListDelegate) {
			item = ((TreebankListDelegate)item).getTreebank();
		}
		
		list.setSelectedValue(item, true);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchTargetSelector#clear()
	 */
	@Override
	public void clear() {
		ensureUI();
		list.clearSelection();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchTargetSelector#getSelectorComponent()
	 */
	@Override
	public Component getSelectorComponent() {
		ensureUI();
		return scrollPane;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchTargetSelector#setAllowedContentType(net.ikarus_systems.icarus.util.data.ContentType)
	 */
	@Override
	public void setAllowedContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Filter#accepts(java.lang.Object)
	 */
	@Override
	public boolean accepts(Object obj) {
		Treebank treebank = (Treebank) obj;
		
		ContentType targetType = treebank.getContentType();
		if(targetType==null) {
			return false;
		}
		
		return ContentTypeRegistry.isCompatible(contentType, targetType);
	}

}
