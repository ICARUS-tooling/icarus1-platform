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
package de.ims.icarus.search_tools.corpus;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankListDelegate;
import de.ims.icarus.language.treebank.TreebankRegistry;
import de.ims.icarus.language.treebank.swing.TreebankListCellRenderer;
import de.ims.icarus.language.treebank.swing.TreebankListModel;
import de.ims.icarus.search_tools.SearchTargetSelector;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;


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
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		ensureUI();
		Treebank treebank = list.getSelectedValue();
		
		return treebank==null ? null : TreebankRegistry.getInstance().getListDelegate(treebank);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#setSelectedItem(java.lang.Object)
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
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#clear()
	 */
	@Override
	public void clear() {
		ensureUI();
		list.clearSelection();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#getSelectorComponent()
	 */
	@Override
	public Component getSelectorComponent() {
		ensureUI();
		return scrollPane;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#setAllowedContentType(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public void setAllowedContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * @see de.ims.icarus.util.Filter#accepts(java.lang.Object)
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
