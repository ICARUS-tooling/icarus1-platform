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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.java.plugin.registry.Extension;

import net.ikarus_systems.icarus.language.Grammar;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataReader;
import net.ikarus_systems.icarus.language.UnsupportedSentenceDataException;
import net.ikarus_systems.icarus.language.treebank.AbstractTreebank;
import net.ikarus_systems.icarus.language.treebank.Treebank;
import net.ikarus_systems.icarus.language.treebank.TreebankDescriptor;
import net.ikarus_systems.icarus.language.treebank.TreebankEvents;
import net.ikarus_systems.icarus.language.treebank.TreebankMetaData;
import net.ikarus_systems.icarus.language.treebank.TreebankMetaDataBuilder;
import net.ikarus_systems.icarus.language.treebank.TreebankObserver;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.ui.events.EventObject;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultSimpleTreebank extends AbstractTreebank implements Treebank {

	public static final String READER_EXTENSION_PROPERTY = "DefaultSimpleTreebank::reader_extension"; //$NON-NLS-1$
	
	protected List<SentenceData> buffer;
	
	protected boolean editable = false;
	
	protected Grammar grammar;
	
	protected AtomicBoolean loading = new AtomicBoolean();
	protected boolean loaded = false;
	
	protected transient SentenceDataReader reader;
	
	protected Extension readerExtension;
	
	protected TreebankMetaData metaData;
	
	public DefaultSimpleTreebank() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#saveState(net.ikarus_systems.icarus.language.treebank.TreebankDescriptor)
	 */
	public void saveState(TreebankDescriptor descriptor) {
		super.saveState(descriptor);
		
		if(readerExtension!=null) {
			descriptor.getProperties().put(READER_EXTENSION_PROPERTY, readerExtension.getUniqueId());
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#loadState(net.ikarus_systems.icarus.language.treebank.TreebankDescriptor)
	 */
	public void loadState(TreebankDescriptor descriptor) {		
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
			LoggerFactory.getLogger(DefaultSimpleTreebank.class).log(LoggerFactory.record(
					Level.SEVERE, "Failed to instantiate reader: "+readerExtension.getUniqueId(), e)); //$NON-NLS-1$
		}
		
		return reader;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#isEditable()
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
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#add(net.ikarus_systems.icarus.language.SentenceData)
	 */
	@Override
	public void add(SentenceData item) {
		int index = buffer==null ? 0 : buffer.size();
		add(item, index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#add(net.ikarus_systems.icarus.language.SentenceData, int)
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
		
		eventSource.fireEvent(new EventObject(TreebankEvents.ADDED, 
				"item", item, "index", index)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#remove(net.ikarus_systems.icarus.language.SentenceData)
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
			eventSource.fireEvent(new EventObject(TreebankEvents.REMOVED, 
					"item", item)); //$NON-NLS-1$
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#remove(int)
	 */
	@Override
	public void remove(int index) {
		if(!editable)
			throw new UnsupportedOperationException("Cannot remove sentence data when not editable"); //$NON-NLS-1$
		
		if(buffer==null) {
			return;
		}
		
		SentenceData item = buffer.remove(index);
		eventSource.fireEvent(new EventObject(TreebankEvents.REMOVED, 
				"item", item, "index", index)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#isLoaded()
	 */
	@Override
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#load()
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
			reader.init(location, null);
			List<SentenceData> buffer = new ArrayList<>(200);
			TreebankMetaDataBuilder metaDataBuilder = new TreebankMetaDataBuilder();
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
		
		eventSource.fireEvent(new EventObject(TreebankEvents.LOADED));
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#size()
	 */
	@Override
	public int size() {
		return buffer==null ? 0 : buffer.size();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#free()
	 */
	@Override
	public void free() {
		if(loading.get())
			throw new IllegalStateException("Cannot free treebank while loading is still in progress!"); //$NON-NLS-1$
		
		synchronized (this) {
			loaded = false;
			int size = size(); 
			buffer = null;
			
			if(size>0) {
				eventSource.fireEvent(new EventObject(TreebankEvents.CHANGED));
			}
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#get(int)
	 */
	@Override
	public SentenceData get(int index) {
		return buffer==null ? null : buffer.get(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#get(int, net.ikarus_systems.icarus.language.treebank.TreebankObserver)
	 */
	@Override
	public SentenceData get(int index, TreebankObserver observer) {
		return get(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#getEntryClass()
	 */
	@Override
	public Class<? extends SentenceData> getEntryClass() {
		return reader==null ? SentenceData.class : getSentenceDataReader().getDataClass();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#getMetaData()
	 */
	@Override
	public TreebankMetaData getMetaData() {
		return metaData;
	}
}
