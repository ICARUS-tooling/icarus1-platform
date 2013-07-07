/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.data;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.AbstractListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DataListModel<E extends Object> extends AbstractListModel<E> {
	
	private static final long serialVersionUID = -3970689300461058008L;
	
	protected DataList<E> dataList;
	
	protected OwnedChangeListener ownedChangeListener;
	
	public DataListModel() {
		// no-op
	}
	
	public DataListModel(DataList<E> dataList) {
		setDataList(dataList);
	}

	public DataList<E> getDataList() {
		return dataList;
	}

	public void setDataList(DataList<E> dataList) {
		if(this.dataList==dataList) {
			return;
		}
		
		if(this.dataList!=null && ownedChangeListener!=null) {
			this.dataList.removeChangeListener(ownedChangeListener);
		}
		
		this.dataList = dataList;
		
		if(this.dataList!=null) {
			if(ownedChangeListener==null) {
				ownedChangeListener = new OwnedChangeListener(this);
			}
			
			this.dataList.addChangeListener(ownedChangeListener);
		}
		
		fireContentsChanged();
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return dataList==null ? 0 : dataList.size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public E getElementAt(int index) {
		return dataList==null ? null : dataList.get(index);
	}
	
	public void clear() {
		setDataList(null);
	}
	
	/**
	 * Helper method for the {@code OwnedChangeListener} to delegate
	 * notification
	 */
	protected void fireContentsChanged() {
		int index1 = Math.max(0, getSize()-1);
		fireContentsChanged(this, 0, index1);
	}

	@SuppressWarnings("rawtypes")
	private static class OwnedChangeListener implements ChangeListener {
		
		private final Reference<DataListModel> owner;
		
		public OwnedChangeListener(DataListModel model) {
			owner = new WeakReference<>(model);
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			DataListModel<?> model = owner.get();
			if(model==null) {
				DataList<?> dataList = (DataList<?>)e.getSource();
				dataList.removeChangeListener(this);
			} else {
				model.fireContentsChanged();
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof OwnedChangeListener) {
				OwnedChangeListener other = (OwnedChangeListener)obj;
				return other.owner.get()==owner.get();
			}
			return false;
		}
	}

}
