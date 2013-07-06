/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.helper;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import de.ims.icarus.util.Filter;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FilteredListModel<E extends Object> extends AbstractListModel<E> {
	
	private static final long serialVersionUID = 8371348205390135107L;

	protected ListModel<E> baseModel;
	
	protected Filter filter;
	
	protected List<Integer> mask;
	
	protected OwnedListDataListener ownedListDataListener;
	
	public FilteredListModel() {
		// no-op
	}
	
	public FilteredListModel(ListModel<E> baseModel) {
		setBaseModel(baseModel);
	}
	
	public FilteredListModel(ListModel<E> baseModel, Filter filter) {
		setBaseModel(baseModel);
		setFilter(filter);
	}

	public ListModel<E> getBaseModel() {
		return baseModel;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setBaseModel(ListModel<E> baseModel) {
		if(this.baseModel==baseModel) {
			return;
		}
		
		if(this.baseModel!=null && ownedListDataListener!=null) {
			this.baseModel.removeListDataListener(ownedListDataListener);
		}
		
		this.baseModel = baseModel;
		
		if(this.baseModel!=null) {
			if(ownedListDataListener==null) {
				ownedListDataListener = new OwnedListDataListener(this);
			}
			this.baseModel.addListDataListener(ownedListDataListener);
		}
		
		recomputeMask();
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
		recomputeMask();
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		if(baseModel==null) {
			return 0;
		}
		return mask==null ? baseModel.getSize() : mask.size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public E getElementAt(int index) {
		if(baseModel==null) {
			return null;
		}
		
		if(mask!=null) {
			index = mask.get(index);
		}
		
		return baseModel.getElementAt(index);
	}
	
	protected void fireContentsChanged() {
		int index1 = Math.max(0, getSize()-1);
		fireContentsChanged(this, 0, index1);
	}
	
	protected void recomputeMask() {
		List<Integer> mask = null;
				
		if(filter!=null) {
			// TODO filter indices
		}
		
		this.mask = mask;
		
		fireContentsChanged();
	}
	
	protected void intervalAdded(int index0, int index1) {
		// TODO validate range of change by finding masking indices
		
		// For now we use costly recomputation of entire mask -> needs to be changed
		recomputeMask();
	}
	
	protected void intervalRemoved(int index0, int index1) {
		// TODO validate range of change by finding masking indices
		
		// For now we use costly recomputation of entire mask -> needs to be changed
		recomputeMask();
	}
	
	protected void intervalChanged(int index0, int index1) {
		// TODO validate range of change by finding masking indices
		
		// For now we use costly recomputation of entire mask -> needs to be changed
		recomputeMask();
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@SuppressWarnings("rawtypes")
	private static class OwnedListDataListener implements ListDataListener {
		
		private final Reference<FilteredListModel> owner;
		
		public OwnedListDataListener(FilteredListModel model) {
			owner = new WeakReference<>(model);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof OwnedListDataListener) {
				OwnedListDataListener other = (OwnedListDataListener)obj;
				return other.owner.get()==owner.get();
			}
			return false;
		}

		/**
		 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void intervalAdded(ListDataEvent e) {
			FilteredListModel model = owner.get();
			if(model==null) {
				ListModel<?> target = (ListModel<?>)e.getSource();
				target.removeListDataListener(this);
			} else {
				model.intervalAdded(e.getIndex0(), e.getIndex1());
			}
		}

		/**
		 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void intervalRemoved(ListDataEvent e) {
			FilteredListModel model = owner.get();
			if(model==null) {
				ListModel<?> target = (ListModel<?>)e.getSource();
				target.removeListDataListener(this);
			} else {
				model.intervalRemoved(e.getIndex0(), e.getIndex1());
			}
		}

		/**
		 * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void contentsChanged(ListDataEvent e) {
			FilteredListModel model = owner.get();
			if(model==null) {
				ListModel<?> target = (ListModel<?>)e.getSource();
				target.removeListDataListener(this);
			} else {
				model.intervalChanged(e.getIndex0(), e.getIndex1());
			}
		}
	}
}
