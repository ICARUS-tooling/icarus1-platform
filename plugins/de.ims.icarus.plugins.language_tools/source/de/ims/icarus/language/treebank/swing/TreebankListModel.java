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
package de.ims.icarus.language.treebank.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankRegistry;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.collections.CollectionUtils;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankListModel extends AbstractListModel<Treebank> 
		implements ComboBoxModel<Treebank>, EventListener {

	private static final long serialVersionUID = -2738466365490012327L;
	
	private List<Treebank> treebanks;
		
	private Filter filter;
	
	private Treebank selectedTreebank;
	
	private boolean dummyTreebankAllowed = true;
	
	public TreebankListModel(Treebank...excludes) {
		setExcludes(excludes);
		
		TreebankRegistry.getInstance().addListener(null, this);
	}
	
	public TreebankListModel(Filter filter) {
		setFilter(filter);
		
		TreebankRegistry.getInstance().addListener(null, this);
	}
	
	public void setExcludes(Treebank...excludes) {
		setFilter(new ExclusionFilter((Object[]) excludes));
	}
	
	public void setExcludes(Collection<Treebank> excludes) {
		setFilter(new ExclusionFilter(excludes));
	}
	
	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		if(this.filter==filter) {
			return;
		}
		
		this.filter = filter;
		
		reload();
	}

	public void reload() {
		if(treebanks==null) {
			treebanks = new ArrayList<>();
		} else {
			treebanks.clear();
		}
		
		for(Treebank treebank : TreebankRegistry.getInstance().availableTreebanks()) {
			if(filter==null || filter.accepts(treebank)) {
				treebanks.add(treebank);
			}
		}
		
		if(dummyTreebankAllowed) {
			treebanks.add(TreebankRegistry.DUMMY_TREEBANK);
		}
		
		Collections.sort(treebanks, TreebankRegistry.TREEBANK_NAME_COMPARATOR);
		
		if(!treebanks.isEmpty()) {
			fireContentsChanged(this, 0, getSize()-1);
		}
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return treebanks.size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Treebank getElementAt(int index) {
		return treebanks.get(index);
	}

	/**
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object anItem) {
		if(anItem!=null && !(anItem instanceof Treebank))
			throw new IllegalArgumentException("Unsupported item: "+anItem); //$NON-NLS-1$
		
		if((selectedTreebank!=null && !selectedTreebank.equals(anItem))
				|| (selectedTreebank==null && anItem!=null)) {
			selectedTreebank = (Treebank) anItem;
			
			fireContentsChanged(this, -1, -1);
		}
	}

	/**
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		return selectedTreebank;
	}

	/**
	 * @return the dummyTreebankAllowed
	 */
	public boolean isDummyTreebankAllowed() {
		return dummyTreebankAllowed;
	}

	/**
	 * @param dummyTreebankAllowed the dummyTreebankAllowed to set
	 */
	public void setDummyTreebankAllowed(boolean dummyTreebankAllowed) {
		if(this.dummyTreebankAllowed!=dummyTreebankAllowed) {
			this.dummyTreebankAllowed = dummyTreebankAllowed;
			
			reload();
		}
	}

	/**
	 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
	 */
	@Override
	public void invoke(Object sender, EventObject event) {
		reload();
	}
	
	private static class ExclusionFilter implements Filter {
		private final Set<Object> exclusions;
		
		private ExclusionFilter(Collection<?> items) {
			exclusions = items==null ? null : new HashSet<>(items);
		}
		
		private ExclusionFilter(Object...items) {
			exclusions = items==null ? null : new HashSet<>();
			if(exclusions!=null) {
				CollectionUtils.feedItems(exclusions, items);
			}
		}

		/**
		 * @see de.ims.icarus.util.Filter#accepts(java.lang.Object)
		 */
		@Override
		public boolean accepts(Object obj) {
			return exclusions==null || !exclusions.contains(obj);
		}
	}
}
