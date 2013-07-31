/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.coref.registry;

import java.io.File;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;

import de.ims.icarus.Core;
import de.ims.icarus.io.Loadable;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.coref.CorefConstants;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.EventSource;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.events.WeakEventSource;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.xml.jaxb.JAXBUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CoreferenceRegistry {
	
	private static CoreferenceRegistry instance;
	
	public static CoreferenceRegistry getInstance() {
		if(instance==null) {
			synchronized (CoreferenceRegistry.class) {
				if(instance==null) {
					JAXBUtils.registerClass(DocumentSetBuffer.class);
					
					instance = new CoreferenceRegistry();
				}
			}
		}
		return instance;
	}

	private EventSource eventSource;
	
	private Map<String, DocumentSetDescriptor> documentSetMap;
	private Map<CoreferenceDocumentSet, DocumentSetDescriptor> documentLookup;
	private List<DocumentSetDescriptor> documentSetList;

	private Map<String, AllocationDescriptor> allocationMap;
	private Map<CoreferenceAllocation, AllocationDescriptor> allocationLookup;
	
	private DocumentSetListModel documentListModel;

	private CoreferenceRegistry() {
		eventSource = new WeakEventSource(this);
		
		documentSetList = new ArrayList<>();
		
		documentSetMap = new HashMap<>();
		documentSetMap = Collections.synchronizedMap(documentSetMap);
		
		documentLookup = new HashMap<>();
		
		allocationMap = new HashMap<>();
		allocationMap = Collections.synchronizedMap(allocationMap);
		
		allocationLookup = new HashMap<>();

		// Attempt to load document-set list
		try {
			load();
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to load document-set list", e); //$NON-NLS-1$
		}
	}
	
	// prevent multiple deserialization
	private Object readResolve() throws ObjectStreamException {
		throw new NotSerializableException();
	}
	
	// prevent cloning
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	public ListModel<DocumentSetDescriptor> getDocumentSetListModel() {
		if(documentListModel==null) {
			synchronized (this) {
				if(documentListModel==null) {
					documentListModel = new DocumentSetListModel();
				}
			}
		}
		return documentListModel;
	}
	
	// BEGIN document set related methods
	
	public int getDocumentSetCount() {
		return documentSetList.size();
	}
	
	public DocumentSetDescriptor getDocumentSet(int index) {
		return documentSetList.get(index);
	}
	
	public int indexOfDocumentSet(DocumentSetDescriptor descriptor) {
		return documentSetList.indexOf(descriptor);
	}
	
	public DocumentSetDescriptor getDocumentSet(String id) {
		return documentSetMap.get(id);
	}
	
	public DocumentSetDescriptor getDescriptor(CoreferenceDocumentSet documentSet) {
		return documentLookup.get(documentSet);
	}
	
	public DocumentSetDescriptor newDocumentSet(String name) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		
		DocumentSetDescriptor descriptor = new DocumentSetDescriptor();
		descriptor.setId(UUID.randomUUID().toString());
		descriptor.setName(name);
		
		addDocumentSet(descriptor);
		
		return descriptor;
	}
	
	public void addDocumentSet(DocumentSetDescriptor descriptor) {
		addDocumentSet0(descriptor);
		
		eventSource.fireEvent(new EventObject(Events.ADDED, 
				"documentSet", descriptor)); //$NON-NLS-1$
		
		saveBackground();
	}
	
	private void addDocumentSet0(DocumentSetDescriptor descriptor) {
		if(descriptor==null)
			throw new IllegalArgumentException("Invalid descriptor"); //$NON-NLS-1$

		// Ensure uniqueness of ids
		DocumentSetDescriptor presentDescriptor = documentSetMap.get(descriptor.getId());
		if(presentDescriptor==descriptor) {
			return;
		}
		if(presentDescriptor!=null) {
			descriptor.setId(UUID.randomUUID().toString());
		}
		
		documentSetMap.put(descriptor.getId(), descriptor);
		documentLookup.put(descriptor.getDocumentSet(), descriptor);
		documentSetList.add(descriptor);
		
		for(int i=0; i<descriptor.size(); i++) {
			addAllocation0(descriptor.get(i));
		}
	}
	
	public void deleteDocumentSet(DocumentSetDescriptor descriptor) {
		if(descriptor==null)
			throw new IllegalArgumentException("Invalid document set descriptor"); //$NON-NLS-1$
		
		documentSetMap.remove(descriptor.getId());
		documentLookup.remove(descriptor.getDocumentSet());

		int index = documentSetList.indexOf(descriptor);
		if(index==-1)
			throw new IllegalArgumentException("Unknown document set: "+descriptor.getName()); //$NON-NLS-1$
		documentSetList.remove(index);
		
		// Release document set resources
		try {
			descriptor.free();
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to free document set: "+descriptor.getName(), e); //$NON-NLS-1$
		}
		
		// Finally notify listeners
		eventSource.fireEvent(new EventObject(Events.REMOVED, 
				"documentSet", descriptor, //$NON-NLS-1$
				"index", index)); //$NON-NLS-1$
		
		saveBackground();
	}
	
	public void deleteDocumentSet(CoreferenceDocumentSet documentSet) {
		if(documentSet==null)
			throw new IllegalArgumentException("Invalid document set"); //$NON-NLS-1$
			
		DocumentSetDescriptor descriptor = documentLookup.remove(documentSet);
		if(descriptor!=null) {	
			deleteDocumentSet(descriptor);
		}
	}
	
	public void setName(DocumentSetDescriptor descriptor, String name) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		if(name.equals(descriptor.getName())) {
			return;
		}
		
		descriptor.setName(name);
		
		documentSetChanged(descriptor.getDocumentSet());
	}
	
	public void setLocation(DocumentSetDescriptor descriptor, Location location) {
		if(location!=null && location.equals(descriptor.getLocation())) {
			return;
		}
		
		descriptor.setLocation(location);
		
		documentSetChanged(descriptor.getDocumentSet());
	}
	
	public void setReaderExtension(DocumentSetDescriptor descriptor, Extension extension) {
		if(extension!=null && extension.equals(descriptor.getReaderExtension())) {
			return;
		}
		
		descriptor.setReaderExtension(extension);
		
		documentSetChanged(descriptor.getDocumentSet());
	}
	
	public void setProperties(DocumentSetDescriptor descriptor, Map<String, Object> properties) {		
		descriptor.setProperties(properties);
		
		documentSetChanged(descriptor.getDocumentSet());
	}
	
	// END document set related methods
	
	// BEGIN allocation related methods
	
	public AllocationDescriptor getAllocation(String id) {
		return allocationMap.get(id);
	}
	
	public AllocationDescriptor getDescriptor(CoreferenceAllocation allocation) {
		return allocationLookup.get(allocation);
	}
	
	public AllocationDescriptor newAllocation(String name, DocumentSetDescriptor parent) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		if(parent==null)
			throw new IllegalArgumentException("Invalid parent"); //$NON-NLS-1$
		
		AllocationDescriptor descriptor = new AllocationDescriptor(parent);
		descriptor.setId(UUID.randomUUID().toString());
		descriptor.setName(name);
		
		parent.addAllocation(descriptor);
		
		addAllocation(descriptor);
		
		return descriptor;
	}
	
	public void addAllocation(AllocationDescriptor descriptor) {
		addAllocation0(descriptor);
		
		eventSource.fireEvent(new EventObject(Events.ADDED, 
				"allocation", descriptor)); //$NON-NLS-1$
		
		saveBackground();
	}
	
	private void addAllocation0(AllocationDescriptor descriptor) {
		if(descriptor==null)
			throw new IllegalArgumentException("Invalid descriptor"); //$NON-NLS-1$

		// Ensure uniqueness of ids
		AllocationDescriptor presentDescriptor = allocationMap.get(descriptor.getId());
		if(presentDescriptor==descriptor) {
			return;
		}
		if(presentDescriptor!=null) {
			descriptor.setId(UUID.randomUUID().toString());
		}
		
		allocationMap.put(descriptor.getId(), descriptor);
		allocationLookup.put(descriptor.getAllocation(), descriptor);
	}
	
	public void deleteAllocation(AllocationDescriptor descriptor) {
		if(descriptor==null)
			throw new IllegalArgumentException("Invalid descriptor"); //$NON-NLS-1$
		
		allocationMap.remove(descriptor.getId());
		allocationLookup.remove(descriptor.getAllocation());
		
		// Release allocation resources
		try {
			descriptor.free();
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to free allocation: "+descriptor.getName(), e); //$NON-NLS-1$
		}
		
		// Remove allocation from document set
		DocumentSetDescriptor parent = descriptor.getParent();
		int index = -1;
		if(parent!=null) {
			index = parent.removeAllocation(descriptor);
		}
		
		// Finally notify listeners
		eventSource.fireEvent(new EventObject(Events.REMOVED, 
				"allocation", descriptor, //$NON-NLS-1$
				"parent", parent, //$NON-NLS-1$
				"index", index)); //$NON-NLS-1$
		
		saveBackground();
	}
	
	public void deleteAllocation(CoreferenceAllocation allocation) {
		if(allocation==null)
			throw new IllegalArgumentException("Invalid allocation"); //$NON-NLS-1$
			
		AllocationDescriptor descriptor = allocationLookup.remove(allocation);
		if(descriptor!=null) {	
			deleteAllocation(descriptor);
		}
	}
	
	public void setName(AllocationDescriptor descriptor, String name) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		if(name.equals(descriptor.getName())) {
			return;
		}
		
		descriptor.setName(name);
		
		allocationChanged(descriptor.getAllocation());
	}
	
	public void setLocation(AllocationDescriptor descriptor, Location location) {
		if(location!=null && location.equals(descriptor.getLocation())) {
			return;
		}
		
		descriptor.setLocation(location);
		
		allocationChanged(descriptor.getAllocation());
	}
	
	public void setReaderExtension(AllocationDescriptor descriptor, Extension extension) {
		if(extension!=null && extension.equals(descriptor.getReaderExtension())) {
			return;
		}
		
		descriptor.setReaderExtension(extension);
		
		allocationChanged(descriptor.getAllocation());
	}
	
	public void setProperties(AllocationDescriptor descriptor, Map<String, Object> properties) {		
		descriptor.setProperties(properties);

		allocationChanged(descriptor.getAllocation());
	}
	
	// END allocation related methods

	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	public void removeListener(EventListener listener) {
		eventSource.removeListener(listener);
	}

	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}
	
	public void documentSetChanged(CoreferenceDocumentSet documentSet) {
		eventSource.fireEvent(new EventObject(Events.CHANGED, 
				"documentSet", getDescriptor(documentSet))); //$NON-NLS-1$

		saveBackground();
	}
	
	public void allocationChanged(CoreferenceAllocation allocation) {
		eventSource.fireEvent(new EventObject(Events.CHANGED, 
				"allocation", getDescriptor(allocation))); //$NON-NLS-1$

		saveBackground();
	}
	
	public String getUniqueDocumentSetName(String baseName) {
		Set<String> usedNames = new HashSet<>(documentSetMap.size());
		for(DocumentSetDescriptor descriptor : documentSetMap.values()) {
			usedNames.add(descriptor.getName());
		}

		return StringUtil.getUniqueName(baseName, usedNames);
	}
	
	public String getUniqueAllocationName(String baseName) {
		Set<String> usedNames = new HashSet<>(allocationMap.size());
		for(AllocationDescriptor descriptor : allocationMap.values()) {
			usedNames.add(descriptor.getName());
		}

		return StringUtil.getUniqueName(baseName, usedNames);
	}
	
	private AtomicBoolean saveCheck = new AtomicBoolean();
	private Runnable saveTask;
	
	private void saveBackground() {
		if(saveCheck.compareAndSet(false, true)) {			
			if(saveTask==null) {
				saveTask = new Runnable() {
					
					@Override
					public void run() {
						try {
							save();
						} catch (Exception e) {
							LoggerFactory.log(this, Level.SEVERE, 
									"Failed to save document-set descriptor list", e); //$NON-NLS-1$
						} finally {
							saveCheck.set(false);
						}
					}
				};
			}
			
			String title = ResourceManager.getInstance().get(
					"plugins.coref.documentSetSaveTask.title"); //$NON-NLS-1$
			String info = ResourceManager.getInstance().get(
					"plugins.coref.documentSetSaveTask.description", documentSetList.size()); //$NON-NLS-1$
			
			TaskManager.getInstance().schedule(saveTask, title, 
					info, null, TaskPriority.DEFAULT, true);
		}
	}
	
	private static final String list_file = "corefDocuments.xml"; //$NON-NLS-1$
	
	private void load() throws Exception {
		File file = new File(Core.getCore().getDataFolder(), list_file);
		if(!file.exists() || file.length()==0) {
			return;
		}

		JAXBContext context = JAXBUtils.getSharedJAXBContext();
		Unmarshaller unmarshaller = context.createUnmarshaller();
		DocumentSetBuffer buffer = (DocumentSetBuffer) unmarshaller.unmarshal(file);
		
		documentSetList.clear();
		documentSetMap.clear();
		documentLookup.clear();
		allocationMap.clear();
		allocationLookup.clear();
		
		for(int i=0; i<buffer.getItemCount(); i++) {
			DocumentSetDescriptor descriptor = buffer.getItem(i);
			try {
				addDocumentSet0(descriptor);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add document-set: "+descriptor, e); //$NON-NLS-1$
			}
		}
	}
	
	private void save() throws Exception {
		File file = new File(Core.getCore().getDataFolder(), list_file);
		if(!file.exists()) {
			file.createNewFile();
		}

		DocumentSetBuffer buffer = new DocumentSetBuffer(documentSetList);

		JAXBContext context = JAXBUtils.getSharedJAXBContext();
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(buffer, file);
	}
	
	public static Collection<Extension> getDocumentReaderExtensions() {
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				CorefConstants.COREFERENCE_PLUGIN_ID, "DocumentReader"); //$NON-NLS-1$
		return extensionPoint.getConnectedExtensions();
	}
	
	public static Collection<Extension> getAllocationReaderExtensions() {
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				CorefConstants.COREFERENCE_PLUGIN_ID, "AllocationReader"); //$NON-NLS-1$
		return extensionPoint.getConnectedExtensions();
	}
	@XmlRootElement(name="documentSets")
	@XmlAccessorType(XmlAccessType.FIELD)
	static class DocumentSetBuffer {
		@XmlElement(name="documentSet")
		private List<DocumentSetDescriptor> items = new ArrayList<>();
		
		public DocumentSetBuffer() {
			// no-op
		}
		
		public DocumentSetBuffer(Collection<DocumentSetDescriptor> descriptors) {
			items.addAll(descriptors);
		}
		
		int getItemCount() {
			return items.size();
		}
		
		DocumentSetDescriptor getItem(int index) {
			return items.get(index);
		}
	}
	
	private class DocumentSetListModel extends AbstractListModel<DocumentSetDescriptor> implements EventListener {

		private static final long serialVersionUID = 6444435276610886335L;

		public DocumentSetListModel() {
			addListener(null, this);
		}
		
		/**
		 * @see javax.swing.ListModel#getSize()
		 */
		@Override
		public int getSize() {
			return documentSetList.size();
		}

		/**
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		@Override
		public DocumentSetDescriptor getElementAt(int index) {
			return documentSetList.get(index);
		}

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			if(event.getProperty("documentSet")==null) { //$NON-NLS-1$
				return;
			}
			
			if(Events.REMOVED.equals(event.getName())) {
				int index = (int) event.getProperty("index"); //$NON-NLS-1$
				fireIntervalRemoved(this, index, index);
			} else if(Events.ADDED.equals(event.getName())) {
				int index = getSize()-1;
				fireIntervalAdded(this, index, index);
			} else if(Events.CHANGED.equals(event.getName())) {
				int index = indexOfDocumentSet(
						(DocumentSetDescriptor) event.getProperty("documentSet")); //$NON-NLS-1$
				fireContentsChanged(this, index, index);
			}
		}
	}

	
	public static class LoadJob extends SwingWorker<Loadable, Object> {
		
		private final Loadable loadable;
		
		public LoadJob(Loadable loadable) {
			if(loadable==null) 
				throw new IllegalArgumentException("Invalid loadable"); //$NON-NLS-1$
			
			this.loadable = loadable;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof LoadJob) {
				return ((LoadJob)obj).loadable==loadable;
			}
			return false;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Loadable doInBackground() throws Exception {
			// Wait while target is loading
			while(loadable.isLoading());
			
			if(loadable.isLoaded()) {
				return null;
			}
			
			loadable.load();
			
			return loadable;
		}		
	}
}
