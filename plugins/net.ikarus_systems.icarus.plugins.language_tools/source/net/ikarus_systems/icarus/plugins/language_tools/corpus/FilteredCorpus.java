/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools.corpus;

import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.corpus.AbstractCorpus;
import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.language.corpus.CorpusDescriptor;
import net.ikarus_systems.icarus.language.corpus.CorpusMetaData;
import net.ikarus_systems.icarus.language.corpus.CorpusObserver;
import net.ikarus_systems.icarus.language.corpus.CorpusRegistry;
import net.ikarus_systems.icarus.language.corpus.DerivedCorpus;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.util.Exceptions;

import com.sun.xml.internal.txw2.IllegalSignatureException;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FilteredCorpus extends AbstractCorpus implements DerivedCorpus {
	
	public static final String BASE_ID_PROPERTY = "FilteredCorpus::base_id"; //$NON-NLS-1$
	
	protected transient Corpus base;
	protected String baseCorpusId;
	
	protected int[] filter;
	
	public FilteredCorpus() {
		// no-op
	}
	
	public Corpus getBase() {
		if(base==null) {
			if(baseCorpusId==null) {
				base = CorpusRegistry.DUMMY_CORPUS;
			} else {
				base = CorpusRegistry.getInstance().getCorpus(baseCorpusId);
			}
		}
		
		return base;
	}
	
	public void setBase(Corpus base) {
		if(base==null)
			throw new IllegalSignatureException("Invalid base"); //$NON-NLS-1$
		
		if(base.equals(this.base)) {
			return;
		}
		
		if(base!=CorpusRegistry.DUMMY_CORPUS) {
			baseCorpusId = CorpusRegistry.getInstance().getDescriptor(base).getId();
		} else {
			baseCorpusId = null;
		}
		
		this.base = base;
		
		eventSource.fireEvent(new EventObject(Events.CHANGED));
	}

	@Override
	public SentenceData get(int index) {
		return getBase().get(filter==null ? index : filter[index]);
	}

	@Override
	public SentenceData get(int index, CorpusObserver observer) {
		return getBase().get(filter==null ? index : filter[index], observer);
	}

	@Override
	public Class<? extends SentenceData> getEntryClass() {
		return getBase().getEntryClass();
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
	 * @see net.ikarus_systems.icarus.language.corpus.AbstractCorpus#saveState(net.ikarus_systems.icarus.language.corpus.CorpusDescriptor)
	 */
	@Override
	public void saveState(CorpusDescriptor descriptor) {
		super.saveState(descriptor);
		
		// Save base corpus id in addition to default stuff
		descriptor.getProperties().put(BASE_ID_PROPERTY, baseCorpusId);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.AbstractCorpus#loadState(net.ikarus_systems.icarus.language.corpus.CorpusDescriptor)
	 */
	@Override
	public void loadState(CorpusDescriptor descriptor) {
		super.loadState(descriptor);
		
		// Load base corpus id and remove it from the properties map
		baseCorpusId = (String) getProperty(BASE_ID_PROPERTY);
		properties.remove(BASE_ID_PROPERTY);
		
		// Let te next getBase() call load the base corpus
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

	public FilteredCorpus derive(int[] filter) {
		Exceptions.testNullArgument(filter, "filter"); //$NON-NLS-1$
		if(this.filter==null || filter.length>this.filter.length)
			throw new IllegalArgumentException(
					String.format("Provided filter is too large: %d - only %d legal",  //$NON-NLS-1$
							filter.length, this.filter.length));
		
		int[] newFilter = new int[filter.length];
		for(int i=0; i<filter.length; i++)
			newFilter[i] = this.filter[filter[i]];
		
		FilteredCorpus newCorpus = new FilteredCorpus();
		newCorpus.setFilter(newFilter);
		newCorpus.setBase(getBase());
		
		return newCorpus;
	}
	
	public static FilteredCorpus filterCorpus(Corpus base, int[] filter) {
		Exceptions.testNullArgument(base, "base"); //$NON-NLS-1$
		Exceptions.testNullArgument(filter, "filter"); //$NON-NLS-1$
		
		if(base instanceof FilteredCorpus)
			return ((FilteredCorpus)base).derive(filter);
		else {
			FilteredCorpus newCorpus = new FilteredCorpus();
			newCorpus.setFilter(filter);
			newCorpus.setBase(base);
			
			return newCorpus;
		}
	}

	@Override
	public void free() {
		filter = null;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#getMetaData()
	 */
	@Override
	public CorpusMetaData getMetaData() {
		return base.getMetaData();
	}
	
	public void addListener(String eventName, EventListener listener) {
		base.addListener(eventName, listener);
	}

	public void removeListener(EventListener listener) {
		base.removeListener(listener);
	}

	public void removeListener(EventListener listener, String eventName) {
		base.removeListener(listener, eventName);
	}
}
