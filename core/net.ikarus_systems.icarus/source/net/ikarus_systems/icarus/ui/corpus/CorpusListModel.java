/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.corpus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.language.corpus.CorpusRegistry;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.util.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusListModel extends AbstractListModel<Corpus> 
		implements ComboBoxModel<Corpus>, EventListener {

	private static final long serialVersionUID = -2738466365490012327L;
	
	private List<Corpus> corpora;
	
	private Set<Corpus> excludes;
	
	private Corpus selectedCorpus;
	
	private boolean dummyCorpusAllowed = true;
	
	public CorpusListModel(Corpus...excludes) {
		setExcludes(excludes);
		
		CorpusRegistry.getInstance().addListener(null, this);
	}
	
	public void setExcludes(Corpus...excludes) {
		setExcludes(CollectionUtils.asList(excludes));
	}
	
	public void setExcludes(Collection<Corpus> excludes) {
		if(this.excludes==null) {
			this.excludes = new HashSet<>();
		} else {
			this.excludes.clear();
		}
		
		this.excludes.addAll(excludes);
		
		reload();
	}
	
	public void reload() {
		if(corpora==null) {
			corpora = new ArrayList<>();
		} else {
			corpora.clear();
		}
		
		for(Corpus corpus : CorpusRegistry.getInstance().availableCorpora()) {
			if(!this.excludes.contains(corpus)) {
				corpora.add(corpus);
			}
		}
		
		if(dummyCorpusAllowed) {
			corpora.add(CorpusRegistry.DUMMY_CORPUS);
		}
		
		Collections.sort(corpora, CorpusRegistry.CORPUS_NAME_COMPARATOR);
		
		if(!corpora.isEmpty()) {
			fireContentsChanged(this, 0, getSize()-1);
		}
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return corpora.size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Corpus getElementAt(int index) {
		return corpora.get(index);
	}

	/**
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object anItem) {
		if(anItem!=null && !(anItem instanceof Corpus))
			throw new IllegalArgumentException("Unsupported item: "+anItem); //$NON-NLS-1$
		
		if((selectedCorpus!=null && !selectedCorpus.equals(anItem))
				|| (selectedCorpus==null && anItem!=null)) {
			selectedCorpus = (Corpus) anItem;
			
			fireContentsChanged(this, -1, -1);
		}
	}

	/**
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		return selectedCorpus;
	}

	/**
	 * @return the dummyCorpusAllowed
	 */
	public boolean isDummyCorpusAllowed() {
		return dummyCorpusAllowed;
	}

	/**
	 * @param dummyCorpusAllowed the dummyCorpusAllowed to set
	 */
	public void setDummyCorpusAllowed(boolean dummyCorpusAllowed) {
		if(this.dummyCorpusAllowed!=dummyCorpusAllowed) {
			this.dummyCorpusAllowed = dummyCorpusAllowed;
			
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
