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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.java.plugin.registry.Extension;

import net.ikarus_systems.icarus.language.Grammar;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataReader;
import net.ikarus_systems.icarus.language.UnsupportedSentenceDataException;
import net.ikarus_systems.icarus.language.corpus.AbstractCorpus;
import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.language.corpus.CorpusDescriptor;
import net.ikarus_systems.icarus.language.corpus.CorpusEvents;
import net.ikarus_systems.icarus.language.corpus.CorpusMetaData;
import net.ikarus_systems.icarus.language.corpus.CorpusMetaDataBuilder;
import net.ikarus_systems.icarus.language.corpus.CorpusObserver;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.ui.events.EventObject;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultSimpleCorpus extends AbstractCorpus implements Corpus {

	public static final String READER_EXTENSION_PROPERTY = "DefaultSimpleCorpus::reader_extension"; //$NON-NLS-1$
	
	protected List<SentenceData> buffer;
	
	protected boolean editable = false;
	
	protected Grammar grammar;
	
	protected AtomicBoolean loading = new AtomicBoolean();
	protected boolean loaded = false;
	
	protected transient SentenceDataReader reader;
	
	protected Extension readerExtension;
	
	protected CorpusMetaData metaData;
	
	public DefaultSimpleCorpus() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#saveState(net.ikarus_systems.icarus.language.corpus.CorpusDescriptor)
	 */
	public void saveState(CorpusDescriptor descriptor) {
		super.saveState(descriptor);
		
		if(readerExtension!=null) {
			descriptor.getProperties().put(READER_EXTENSION_PROPERTY, readerExtension.getUniqueId());
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#loadState(net.ikarus_systems.icarus.language.corpus.CorpusDescriptor)
	 */
	public void loadState(CorpusDescriptor descriptor) {		
		super.loadState(descriptor);
		
		String uid = (String) properties.get(READER_EXTENSION_PROPERTY);
		if(uid!=null) {
			readerExtension = PluginUtil.getExtension(uid);
		}
		
		reader = null;
	}
	
	public void setReader(Extension readerExtension) {
		if(readerExtension!=null && readerExtension.equals(this.readerExtension)) {
			return;
		}
		
		this.readerExtension = readerExtension;
		reader = null;
	}
	
	public Extension getReader() {
		return readerExtension;
	}
	
	protected SentenceDataReader getSentenceDataReader() {
		if(reader!=null) {
			return reader;
		}
		
		if(readerExtension==null)
			throw new IllegalStateException("No reader defined"); //$NON-NLS-1$
				
		try {
			reader = (SentenceDataReader) PluginUtil.instantiate(readerExtension);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			LoggerFactory.getLogger(DefaultSimpleCorpus.class).log(LoggerFactory.record(
					Level.SEVERE, "Failed to instantiate reader: "+readerExtension.getUniqueId(), e)); //$NON-NLS-1$
		}
		
		return reader;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
		
		eventSource.fireEvent(new EventObject(CorpusEvents.EDITABLE));
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#add(net.ikarus_systems.icarus.language.SentenceData)
	 */
	@Override
	public void add(SentenceData item) {
		int index = buffer==null ? 0 : buffer.size();
		add(item, index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#add(net.ikarus_systems.icarus.language.SentenceData, int)
	 */
	@Override
	public void add(SentenceData item, int index) {
		if(!editable)
			throw new UnsupportedOperationException("Cannot add sentence data when not editable"); //$NON-NLS-1$
		if(!getEntryClass().isAssignableFrom(item.getClass()))
			throw new UnsupportedSentenceDataException("Unsupported data: "+item); //$NON-NLS-1$
		
		if(buffer==null) {
			buffer = new ArrayList<>();
		}
		
		buffer.add(index, item);
		
		eventSource.fireEvent(new EventObject(CorpusEvents.ADDED, 
				"item", item, "index", index)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#remove(net.ikarus_systems.icarus.language.SentenceData)
	 */
	@Override
	public void remove(SentenceData item) {
		if(!editable)
			throw new UnsupportedOperationException("Cannot remove sentence data when not editable"); //$NON-NLS-1$
		if(!getEntryClass().isAssignableFrom(item.getClass()))
			throw new UnsupportedSentenceDataException("Unsupported data: "+item); //$NON-NLS-1$
		
		if(buffer==null) {
			return;
		}
		
		if(buffer.remove(item)) {
			eventSource.fireEvent(new EventObject(CorpusEvents.REMOVED, 
					"item", item)); //$NON-NLS-1$
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#remove(int)
	 */
	@Override
	public void remove(int index) {
		if(!editable)
			throw new UnsupportedOperationException("Cannot remove sentence data when not editable"); //$NON-NLS-1$
		
		if(buffer==null) {
			return;
		}
		
		SentenceData item = buffer.remove(index);
		eventSource.fireEvent(new EventObject(CorpusEvents.REMOVED, 
				"item", item, "index", index)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#isLoaded()
	 */
	@Override
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#load()
	 */
	@Override
	public void load() throws Exception {
		SentenceDataReader reader = getSentenceDataReader();
		
		if(location==null)
			throw new IllegalStateException("Invalid location"); //$NON-NLS-1$
		if(reader==null)
			throw new IllegalStateException("Invalid reader"); //$NON-NLS-1$
		if(!loading.compareAndSet(false, true))
			throw new IllegalStateException("Loading already in progress"); //$NON-NLS-1$

		eventSource.fireEvent(new EventObject(CorpusEvents.LOADING));
		try {
			reader.init(location, null);
			List<SentenceData> buffer = new ArrayList<>(200);
			CorpusMetaDataBuilder metaDataBuilder = new CorpusMetaDataBuilder();
			SentenceData item;
			
			while((item = reader.next())!=null) {
				buffer.add(item);
				metaDataBuilder.process(item);
			}
			synchronized (this) {
				this.buffer = buffer;
				loaded = true;
				metaData = metaDataBuilder.buildMetaData();
			}
		} finally {			
			loading.set(false);			
			reader.close();
		}
		
		eventSource.fireEvent(new EventObject(CorpusEvents.LOADED));
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#size()
	 */
	@Override
	public int size() {
		return buffer==null ? 0 : buffer.size();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#free()
	 */
	@Override
	public void free() {
		if(loading.get())
			throw new IllegalStateException("Cannot free corpus while loading is still in progress!"); //$NON-NLS-1$
		
		synchronized (this) {
			loaded = false;
			int size = size(); 
			buffer = null;
			
			if(size>0) {
				eventSource.fireEvent(new EventObject(CorpusEvents.CHANGED));
			}
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#get(int)
	 */
	@Override
	public SentenceData get(int index) {
		return buffer==null ? null : buffer.get(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#get(int, net.ikarus_systems.icarus.language.corpus.CorpusObserver)
	 */
	@Override
	public SentenceData get(int index, CorpusObserver observer) {
		return get(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#getEntryClass()
	 */
	@Override
	public Class<? extends SentenceData> getEntryClass() {
		return reader==null ? SentenceData.class : getSentenceDataReader().getDataClass();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#getMetaData()
	 */
	@Override
	public CorpusMetaData getMetaData() {
		return metaData;
	}
}
