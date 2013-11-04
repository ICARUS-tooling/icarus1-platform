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
package de.ims.icarus.plugins.language_tools.treebank;


import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.treebank.AbstractTreebank;
import de.ims.icarus.language.treebank.DerivedTreebank;
import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankDescriptor;
import de.ims.icarus.language.treebank.TreebankEvents;
import de.ims.icarus.language.treebank.TreebankMetaData;
import de.ims.icarus.language.treebank.TreebankRegistry;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.data.ContentType;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FilteredTreebank extends AbstractTreebank implements DerivedTreebank {
	
	public static final String BASE_ID_PROPERTY = "FilteredTreebank::base_id"; //$NON-NLS-1$
	
	protected transient Treebank base;
	protected String baseTreebankId;
	
	protected int[] filter;
	
	public FilteredTreebank() {
		// no-op
	}
	
	public Treebank getBase() {
		if(base==null) {
			if(baseTreebankId==null) {
				base = TreebankRegistry.DUMMY_TREEBANK;
			} else {
				base = TreebankRegistry.getInstance().getTreebank(baseTreebankId);
			}
		}
		
		return base;
	}
	
	public void setBase(Treebank base) {
		if(base==null)
			throw new NullPointerException("Invalid base"); //$NON-NLS-1$
		
		if(base.equals(this.base)) {
			return;
		}
		
		if(base!=TreebankRegistry.DUMMY_TREEBANK) {
			baseTreebankId = TreebankRegistry.getInstance().getDescriptor(base).getId();
		} else {
			baseTreebankId = null;
		}
		
		this.base = base;
		
		eventSource.fireEvent(new EventObject(Events.CHANGED));
	}

	@Override
	public ContentType getContentType() {
		return getBase().getContentType();
	}

	@Override
	public boolean isLoaded() {
		return getBase().isLoaded() && filter !=null;
	}

	@Override
	public void load() throws Exception {
		getBase().load();
		
		// TODO load filter from our location
	}

	@Override
	public int size() {
		return filter==null ? 0 : filter.length;
	}
	
	/**
	 * @see de.ims.icarus.language.treebank.AbstractTreebank#saveState(de.ims.icarus.language.treebank.TreebankDescriptor)
	 */
	@Override
	public void saveState(TreebankDescriptor descriptor) {
		super.saveState(descriptor);
		
		// Save base treebank id in addition to default stuff
		descriptor.getProperties().put(BASE_ID_PROPERTY, baseTreebankId);
	}

	/**
	 * @see de.ims.icarus.language.treebank.AbstractTreebank#loadState(de.ims.icarus.language.treebank.TreebankDescriptor)
	 */
	@Override
	public void loadState(TreebankDescriptor descriptor) {
		super.loadState(descriptor);
		
		// Load base treebank id and remove it from the properties map
		baseTreebankId = (String) getProperty(BASE_ID_PROPERTY);
		properties.remove(BASE_ID_PROPERTY);
		
		// Let te next getBase() call load the base treebank
		base = null;
	}

	/**
	 * @return the filter
	 */
	public int[] getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(int[] filter) {
		this.filter = filter;
	}

	public FilteredTreebank derive(int[] filter) {
		Exceptions.testNullArgument(filter, "filter"); //$NON-NLS-1$
		if(this.filter==null || filter.length>this.filter.length)
			throw new IllegalArgumentException(
					String.format("Provided filter is too large: %d - only %d legal",  //$NON-NLS-1$
							filter.length, this.filter.length));
		
		int[] newFilter = new int[filter.length];
		for(int i=0; i<filter.length; i++) {
			newFilter[i] = this.filter[filter[i]];
		}
		
		FilteredTreebank newTreebank = new FilteredTreebank();
		newTreebank.setFilter(newFilter);
		newTreebank.setBase(getBase());
		
		return newTreebank;
	}
	
	public static FilteredTreebank filterTreebank(Treebank base, int[] filter) {
		Exceptions.testNullArgument(base, "base"); //$NON-NLS-1$
		Exceptions.testNullArgument(filter, "filter"); //$NON-NLS-1$
		
		if(base instanceof FilteredTreebank)
			return ((FilteredTreebank)base).derive(filter);
		else {
			FilteredTreebank newTreebank = new FilteredTreebank();
			newTreebank.setFilter(filter);
			newTreebank.setBase(base);
			
			return newTreebank;
		}
	}

	@Override
	public void free() {
		eventSource.fireEvent(new EventObject(TreebankEvents.FREEING));
		
		filter = null;
		
		eventSource.fireEvent(new EventObject(TreebankEvents.FREED));
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#getMetaData()
	 */
	@Override
	public TreebankMetaData getMetaData() {
		return getBase().getMetaData();
	}
	
	public void addListener(String eventName, EventListener listener) {
		getBase().addListener(eventName, listener);
	}

	public void removeListener(EventListener listener) {
		getBase().removeListener(listener);
	}

	public void removeListener(EventListener listener, String eventName) {
		getBase().removeListener(listener, eventName);
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#set(de.ims.icarus.language.SentenceData, int, de.ims.icarus.language.DataType)
	 */
	@Override
	public void set(SentenceData item, int index, DataType type) {
		getBase().set(item, index, type);
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#remove(int, de.ims.icarus.language.DataType)
	 */
	@Override
	public void remove(int index, DataType type) {
		getBase().remove(index, type);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#supportsType(de.ims.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		return getBase().supportsType(type);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType)
	 */
	@Override
	public SentenceData get(int index, DataType type) {
		return getBase().get(filter==null ? index : filter[index], type);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType, de.ims.icarus.language.AvailabilityObserver)
	 */
	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		return getBase().get(filter==null ? index : filter[index], type, observer);
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoading()
	 */
	@Override
	public boolean isLoading() {
		return getBase().isLoading();
	}
}
