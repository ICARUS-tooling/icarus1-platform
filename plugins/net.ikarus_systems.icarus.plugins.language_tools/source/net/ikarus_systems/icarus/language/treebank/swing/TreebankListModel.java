/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import net.ikarus_systems.icarus.language.treebank.Treebank;
import net.ikarus_systems.icarus.language.treebank.TreebankRegistry;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.util.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankListModel extends AbstractListModel<Treebank> 
		implements ComboBoxModel<Treebank>, EventListener {

	private static final long serialVersionUID = -2738466365490012327L;
	
	private List<Treebank> treebanks;
	
	private Set<Treebank> excludes;
	
	private Treebank selectedTreebank;
	
	private boolean dummyTreebankAllowed = true;
	
	public TreebankListModel(Treebank...excludes) {
		setExcludes(excludes);
		
		TreebankRegistry.getInstance().addListener(null, this);
	}
	
	public void setExcludes(Treebank...excludes) {
		setExcludes(CollectionUtils.asList(excludes));
	}
	
	public void setExcludes(Collection<Treebank> excludes) {
		if(this.excludes==null) {
			this.excludes = new HashSet<>();
		} else {
			this.excludes.clear();
		}
		
		this.excludes.addAll(excludes);
		
		reload();
	}
	
	public void reload() {
		if(treebanks==null) {
			treebanks = new ArrayList<>();
		} else {
			treebanks.clear();
		}
		
		for(Treebank treebank : TreebankRegistry.getInstance().availableTreebanks()) {
			if(!this.excludes.contains(treebank)) {
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
	 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
	 */
	@Override
	public void invoke(Object sender, EventObject event) {
		reload();
	}
}
