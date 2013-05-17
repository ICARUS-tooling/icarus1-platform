/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools.treebank;

import net.ikarus_systems.icarus.language.DataType;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.AvailabilityObserver;
import net.ikarus_systems.icarus.language.treebank.AbstractTreebank;
import net.ikarus_systems.icarus.language.treebank.Treebank;
import net.ikarus_systems.icarus.language.treebank.TreebankDescriptor;
import net.ikarus_systems.icarus.language.treebank.TreebankEvents;
import net.ikarus_systems.icarus.language.treebank.TreebankMetaData;
import net.ikarus_systems.icarus.language.treebank.TreebankRegistry;
import net.ikarus_systems.icarus.language.treebank.DerivedTreebank;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.util.Exceptions;
import net.ikarus_systems.icarus.util.data.ContentType;

import com.sun.xml.internal.txw2.IllegalSignatureException;

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
			throw new IllegalSignatureException("Invalid base"); //$NON-NLS-1$
		
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
	 * @see net.ikarus_systems.icarus.language.treebank.AbstractTreebank#saveState(net.ikarus_systems.icarus.language.treebank.TreebankDescriptor)
	 */
	@Override
	public void saveState(TreebankDescriptor descriptor) {
		super.saveState(descriptor);
		
		// Save base treebank id in addition to default stuff
		descriptor.getProperties().put(BASE_ID_PROPERTY, baseTreebankId);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.AbstractTreebank#loadState(net.ikarus_systems.icarus.language.treebank.TreebankDescriptor)
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
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#getMetaData()
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
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#set(net.ikarus_systems.icarus.language.SentenceData, int, net.ikarus_systems.icarus.language.DataType)
	 */
	@Override
	public void set(SentenceData item, int index, DataType type) {
		getBase().set(item, index, type);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#remove(int, net.ikarus_systems.icarus.language.DataType)
	 */
	@Override
	public void remove(int index, DataType type) {
		getBase().remove(index, type);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#supportsType(net.ikarus_systems.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		return getBase().supportsType(type);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType)
	 */
	@Override
	public SentenceData get(int index, DataType type) {
		return getBase().get(filter==null ? index : filter[index], type);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType, net.ikarus_systems.icarus.language.AvailabilityObserver)
	 */
	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		return getBase().get(filter==null ? index : filter[index], type, observer);
	}
}
