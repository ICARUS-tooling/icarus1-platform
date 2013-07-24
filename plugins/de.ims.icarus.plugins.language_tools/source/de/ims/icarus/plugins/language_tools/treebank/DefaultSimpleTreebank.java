/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.language_tools.treebank;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;


import org.java.plugin.registry.Extension;

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.Grammar;
import de.ims.icarus.language.LanguageManager;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataReader;
import de.ims.icarus.language.UnsupportedSentenceDataException;
import de.ims.icarus.language.treebank.AbstractTreebank;
import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankDescriptor;
import de.ims.icarus.language.treebank.TreebankEvents;
import de.ims.icarus.language.treebank.TreebankMetaData;
import de.ims.icarus.language.treebank.TreebankMetaDataBuilder;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.location.Location;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultSimpleTreebank extends AbstractTreebank implements Treebank {

	public static final String READER_EXTENSION_PROPERTY = "DefaultSimpleTreebank::reader_extension"; //$NON-NLS-1$
	
	public static final String READER_PROPERTY_PREFIX = "DefaultSimpleTreebank::reader::"; //$NON-NLS-1$
	
	protected List<SentenceData> buffer;
	
	protected boolean editable = false;
	
	protected Grammar grammar;
	
	protected final AtomicBoolean loading = new AtomicBoolean();
	protected boolean loaded = false;
	
	protected transient SentenceDataReader reader;
	
	protected Extension readerExtension;
	
	protected TreebankMetaData metaData; 
	
	public DefaultSimpleTreebank() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#saveState(de.ims.icarus.language.treebank.TreebankDescriptor)
	 */
	public void saveState(TreebankDescriptor descriptor) {
		super.saveState(descriptor);
		
		if(readerExtension!=null) {
			descriptor.getProperties().put(READER_EXTENSION_PROPERTY, readerExtension.getUniqueId());
		}
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#loadState(de.ims.icarus.language.treebank.TreebankDescriptor)
	 */
	public void loadState(TreebankDescriptor descriptor) {		
		super.loadState(descriptor);
		
		String uid = (String) properties.get(READER_EXTENSION_PROPERTY);
		if(uid!=null) {
			readerExtension = PluginUtil.getExtension(uid);
		}
		
		reader = null;
	}
	
	@Override
	public void setLocation(Location location) {
		super.setLocation(location);
		free();
	}

	public void setReader(Extension readerExtension) {
		if(readerExtension!=null && readerExtension.equals(this.readerExtension)) {
			return;
		}
		
		this.readerExtension = readerExtension;
		reader = null;
		free();
	}
	
	public Extension getReader() {
		return readerExtension;
	}
	
	public SentenceDataReader getSentenceDataReader() {
		if(reader!=null) {
			return reader;
		}
		
		if(readerExtension==null)
			throw new IllegalStateException("No reader defined"); //$NON-NLS-1$
				
		try {
			reader = (SentenceDataReader) PluginUtil.instantiate(readerExtension);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			LoggerFactory.log(this, Level.SEVERE, "Failed to instantiate reader: "+readerExtension.getUniqueId(), e); //$NON-NLS-1$
		}
		
		return reader;
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
		
		eventSource.fireEvent(new EventObject(TreebankEvents.EDITABLE));
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#set(de.ims.icarus.language.SentenceData, int, de.ims.icarus.language.DataType)
	 */
	public void set(SentenceData item, int index, DataType type) {
		if(!isEditable())
			throw new UnsupportedOperationException();
		if(!ContentTypeRegistry.isCompatible(getContentType(), item))
			throw new UnsupportedSentenceDataException("Unsupported data: "+item); //$NON-NLS-1$
		
		if(!supportsType(type)) {
			return;
		}
		
		if(buffer==null) {
			buffer = new ArrayList<>();
		}
		
		if(index==buffer.size()) {
			buffer.add(item);
		} else {
			buffer.set(index, item);
		}
		
		eventSource.fireEvent(new EventObject(TreebankEvents.ADDED, 
				"item", item, "index", index)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#remove(int, de.ims.icarus.language.DataType)
	 */
	public void remove(int index, DataType type) {
		if(!isEditable())
			throw new UnsupportedOperationException();
		
		if(buffer==null) {
			return;
		}
		
		SentenceData item = buffer.remove(index);
		eventSource.fireEvent(new EventObject(TreebankEvents.REMOVED, 
				"item", item, "index", index)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#isLoaded()
	 */
	@Override
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoading()
	 */
	@Override
	public boolean isLoading() {
		return loading.get();
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#load()
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

		eventSource.fireEvent(new EventObject(TreebankEvents.LOADING));
		try {
			reader.init(location, new Options(getProperties()));
			List<SentenceData> buffer = new ArrayList<>(200);
			TreebankMetaDataBuilder metaDataBuilder = new TreebankMetaDataBuilder();
			SentenceData item;
			
			while((item = reader.next())!=null) {
				if(Thread.currentThread().isInterrupted())
					throw new InterruptedException();
				
				buffer.add(item);
				metaDataBuilder.process(item);
				
				//eventSource.fireEvent(new EventObject(TreebankEvents.ADDED, "item", item)); //$NON-NLS-1$
			}
			synchronized (this) {
				this.buffer = buffer;
				loaded = true;
				metaData = metaDataBuilder.buildMetaData();
			}
		} finally {			
			loading.set(false);	
			try {
				reader.close();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, "Failed to close reader for treebank: "+getName(), e); //$NON-NLS-1$
			}
			eventSource.fireEvent(new EventObject(TreebankEvents.LOADED));
		}
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#size()
	 */
	@Override
	public int size() {
		return buffer==null ? 0 : buffer.size();
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#free()
	 */
	@Override
	public void free() {
		if(loading.get())
			throw new IllegalStateException("Cannot free treebank while loading is still in progress!"); //$NON-NLS-1$
		
		synchronized (this) {
			eventSource.fireEvent(new EventObject(TreebankEvents.FREEING));
			
			loaded = false;
			int size = size(); 
			buffer = null;
			
			eventSource.fireEvent(new EventObject(TreebankEvents.FREED));
			
			if(size>0) {
				fireChangeEvent();
			}
		}
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#getEntryClass()
	 */
	@Override
	public ContentType getContentType() {
		return readerExtension==null ? LanguageManager.getInstance().getSentenceDataContentType() 
				: getSentenceDataReader().getContentType();
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#getMetaData()
	 */
	@Override
	public TreebankMetaData getMetaData() {
		return metaData;
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#supportsType(de.ims.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		return type==DataType.SYSTEM;
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType)
	 */
	@Override
	public SentenceData get(int index, DataType type) {
		return get(index, type, null);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType, de.ims.icarus.language.AvailabilityObserver)
	 */
	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		SentenceData item = buffer==null ? null : buffer.get(index);
		return type==DataType.SYSTEM ? item : null;
	}
}
