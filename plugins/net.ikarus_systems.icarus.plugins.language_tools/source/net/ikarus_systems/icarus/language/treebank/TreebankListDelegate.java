/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.language.AvailabilityObserver;
import net.ikarus_systems.icarus.language.DataType;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankListDelegate implements SentenceDataList {

	private Reference<Treebank> ref;
	
	private List<ChangeListener> changeListeners;
	
	private OwnedChangeListener ownedChangeListener;
	
	public TreebankListDelegate(Treebank treebank) {
		if(treebank==null)
			throw new IllegalArgumentException("Invalid treebank"); //$NON-NLS-1$
		
		setTreebank(treebank);
	}
	
	void setTreebank(Treebank treebank) {
		if(ref!=null && ref.get()==treebank) {
			return;
		}
		ref = new WeakReference<>(treebank);
		
		if(ownedChangeListener!=null) {
			ownedChangeListener.destroy();
		}
		
		ownedChangeListener = new OwnedChangeListener(this);
		treebank.addChangeListener(ownedChangeListener);
		
		fireChangeEvent();
	}
	
	private Treebank getTreebank() {
		Treebank treebank = ref.get();
		if(treebank==null) {
			return null;
		}
		
		// TODO present dialog to ask user if he wants to load treebank if not loaded already
		
		return treebank;
	}
	
	public boolean hasTreebank() {
		return ref.get()!=null;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		Treebank treebank = getTreebank();
		return treebank==null ? 0 : treebank.size();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#get(int)
	 */
	@Override
	public SentenceData get(int index) {
		Treebank treebank = getTreebank();
		return treebank==null ? null : treebank.get(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		Treebank treebank = getTreebank();
		return treebank==null ? null : treebank.getContentType();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		if(changeListeners==null) {
			changeListeners = new ArrayList<>();
		}
		
		changeListeners.add(listener);
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener listener) {
		if(changeListeners==null) {
			return;
		}
		
		changeListeners.remove(listener);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#supportsType(net.ikarus_systems.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		Treebank treebank = getTreebank();
		return treebank==null ? false : treebank.supportsType(type);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType)
	 */
	@Override
	public SentenceData get(int index, DataType type) {
		Treebank treebank = getTreebank();
		return treebank==null ? null : treebank.get(index, type);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType, net.ikarus_systems.icarus.language.AvailabilityObserver)
	 */
	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		Treebank treebank = getTreebank();
		return treebank==null ? null : treebank.get(index, type, observer);
	}
	
	private void fireChangeEvent() {
		if(changeListeners==null || changeListeners.isEmpty()) {
			return;
		}
		ChangeEvent event = new ChangeEvent(this);
		Object[] listeners = changeListeners.toArray();
		for(Object listener : listeners) {
			((ChangeListener)listener).stateChanged(event);
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private static class OwnedChangeListener implements ChangeListener {
		
		private final Reference<TreebankListDelegate> ref;
		
		OwnedChangeListener(TreebankListDelegate delegate) {
			if(delegate==null)
				throw new IllegalArgumentException("Invalid delegate"); //$NON-NLS-1$
			
			ref = new WeakReference<>(delegate);
		}
		
		void destroy() {
			ref.clear();
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			TreebankListDelegate delegate = ref.get();
			if(delegate==null) {
				Treebank target = (Treebank)e.getSource();
				target.removeChangeListener(this);
			} else {
				delegate.fireChangeEvent();
			}
		}		
	}
}
