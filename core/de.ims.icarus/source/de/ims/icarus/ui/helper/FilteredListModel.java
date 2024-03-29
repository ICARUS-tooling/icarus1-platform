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
package de.ims.icarus.ui.helper;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import de.ims.icarus.util.Filter;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FilteredListModel<E extends Object> extends AbstractListModel<E> {

	private static final long serialVersionUID = 8371348205390135107L;

	protected ListModel<E> baseModel;

	protected Filter filter;

	protected TIntList mask;

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
		TIntList mask = null;

		if(filter!=null) {
			mask = new TIntArrayList();

			for(int i=0; i<baseModel.getSize(); i++) {
				E item = baseModel.getElementAt(i);
				if(filter.accepts(item)) {
					mask.add(i);
				}
			}
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
		private final int hash;

		public OwnedListDataListener(FilteredListModel model) {
			owner = new WeakReference<>(model);
			hash = model.hashCode();
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

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return hash;
		}
	}
}
