/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.treebank;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.io.Loadable;
import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.util.NamedObject;
import de.ims.icarus.util.data.ContentType;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankListDelegate implements SentenceDataList, NamedObject, Loadable {

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
	
	public Treebank getTreebank() {
		Treebank treebank = ref.get();
		if(treebank==null) {
			return null;
		}
		
		return treebank;
	}
	
	public boolean hasTreebank() {
		return ref.get()!=null;
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		Treebank treebank = getTreebank();
		return treebank==null ? 0 : treebank.size();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#get(int)
	 */
	@Override
	public SentenceData get(int index) {
		Treebank treebank = getTreebank();
		return treebank==null ? null : treebank.get(index);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		Treebank treebank = getTreebank();
		return treebank==null ? null : treebank.getContentType();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		if(changeListeners==null) {
			changeListeners = new ArrayList<>();
		}
		
		changeListeners.add(listener);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener listener) {
		if(changeListeners==null) {
			return;
		}
		
		changeListeners.remove(listener);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#supportsType(de.ims.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		Treebank treebank = getTreebank();
		return treebank==null ? false : treebank.supportsType(type);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType)
	 */
	@Override
	public SentenceData get(int index, DataType type) {
		Treebank treebank = getTreebank();
		return treebank==null ? null : treebank.get(index, type);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType, de.ims.icarus.language.AvailabilityObserver)
	 */
	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		Treebank treebank = getTreebank();
		return treebank==null ? null : treebank.get(index, type, observer);
	}

	/**
	 * @see de.ims.icarus.util.NamedObject#getName()
	 */
	@Override
	public String getName() {
		Treebank treebank = getTreebank();
		return treebank==null ? null : treebank.getName();
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoaded()
	 */
	@Override
	public boolean isLoaded() {
		Treebank treebank = getTreebank();
		return treebank==null ? false : treebank.isLoaded();
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoading()
	 */
	@Override
	public boolean isLoading() {
		Treebank treebank = getTreebank();
		return treebank==null ? false : treebank.isLoading();
	}

	/**
	 * @see de.ims.icarus.io.Loadable#load()
	 */
	@Override
	public void load() throws Exception {
		Treebank treebank = getTreebank();
		if(treebank!=null) {
			treebank.load();
		}
	}
	
	@Override
	public String toString() {
		Treebank treebank = getTreebank();
		return treebank==null ? "<empty>" : treebank.getName(); //$NON-NLS-1$
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
