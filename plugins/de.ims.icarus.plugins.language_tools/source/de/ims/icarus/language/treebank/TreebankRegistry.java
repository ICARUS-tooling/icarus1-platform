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
package de.ims.icarus.language.treebank;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.Version;

import de.ims.icarus.Core;
import de.ims.icarus.Core.NamedRunnable;
import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.LanguageManager;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.IcarusCorePlugin;
import de.ims.icarus.plugins.language_tools.LanguageToolsConstants;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.EventSource;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.events.WeakEventSource;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.id.UnknownIdentifierException;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.strings.StringUtil;
import de.ims.icarus.xml.jaxb.JAXBUtils;



/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankRegistry {

	private static volatile TreebankRegistry instance;

	public static TreebankRegistry getInstance() {
		if(instance==null) {
			synchronized (TreebankRegistry.class) {
				if(instance==null) {
					JAXBUtils.registerClass(TreebankInfoSet.class);
					JAXBUtils.registerClass(TreebankSet.class);

					instance = new TreebankRegistry();
				}
			}
		}
		return instance;
	}

	private EventSource eventSource;

	// maps uuid to descriptor
	private Map<String, TreebankDescriptor> descriptorMap =
			Collections.synchronizedMap(new HashMap<String, TreebankDescriptor>());

	private Extension defaultReaderExtension;

	// maps instantiated treebank to its descriptor
	private Map<Treebank, TreebankDescriptor> treebankMap = new HashMap<>();

	private Map<String, Extension> treebankTypes = new HashMap<>();

	private Map<String, Reference<TreebankListDelegate>> delegateMap;
	private TreebankRegistry() {
		eventSource = new WeakEventSource(this);

		// Load all connected treebank extensions
		PluginDescriptor descriptor = PluginUtil.getPluginRegistry().getPluginDescriptor(
				LanguageToolsConstants.LANGUAGE_TOOLS_PLUGIN_ID);


		ExtensionPoint extensionPoint = descriptor.getExtensionPoint("SentenceDataReader"); //$NON-NLS-1$
		List<Extension> readerExtensions = new ArrayList<>(extensionPoint.getConnectedExtensions());
		Collections.sort(readerExtensions, PluginUtil.EXTENSION_COMPARATOR);
		defaultReaderExtension = readerExtensions.isEmpty() ? null : readerExtensions.get(0);

		for(Extension extension : descriptor.getExtensionPoint("Treebank").getConnectedExtensions()) { //$NON-NLS-1$
			treebankTypes.put(extension.getId(), extension);
		}

		// Attempt to load treebank list
		try {
			load();
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE, "Failed to load treebank list", e); //$NON-NLS-1$
		}

		Core.getCore().addShutdownHook(new ShutdownHook());

		// Show example data if required
		if(!IcarusCorePlugin.isShowExampleData()) {
			return;
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

	public Extension getDefaultReaderExtension() {
		return defaultReaderExtension;
	}

	public Set<Extension> availableTypes() {
		return new HashSet<>(treebankTypes.values());
	}

	public int availableTypeCount() {
		return treebankTypes.size();
	}

	public Collection<Treebank> availableTreebanks() {
		return Collections.unmodifiableCollection(treebankMap.keySet());
	}

	public int availableTreebankCount() {
		return treebankMap.size();
	}

	public Set<Extension> compatibleTypes(String grammar) {
		if(grammar==null)
			throw new NullPointerException("Invalid grammar"); //$NON-NLS-1$

		Set<Extension> compatibleTypes = new HashSet<>();

		loop_extensions : for(Extension extension : treebankTypes.values()) {
			Collection<Extension.Parameter> params = extension.getParameters("grammar"); //$NON-NLS-1$
			if(params==null || params.isEmpty()) {
				continue loop_extensions;
			}
			for(Extension.Parameter param : params) {
				if(grammar.equals(param.valueAsString())) {
					compatibleTypes.add(extension);
					continue loop_extensions;
				}
			}
		}

		return compatibleTypes;
	}

	public List<Treebank> getInstances(Extension type) {
		List<Treebank> treebanks = new ArrayList<>();

		for(Entry<Treebank, TreebankDescriptor> entry : treebankMap.entrySet()) {
			if(entry.getValue().getExtension()==type) {
				treebanks.add(entry.getKey());
			}
		}

		return treebanks;
	}

	public List<DerivedTreebank> getDerived(Treebank base) {
		List<DerivedTreebank> treebanks = new ArrayList<>();

		for(Treebank treebank : treebankMap.keySet()) {
			if(!(treebank instanceof DerivedTreebank)) {
				continue;
			}

			DerivedTreebank derivedTreebank = (DerivedTreebank) treebank;
			if(derivedTreebank.getBase()==base) {
				treebanks.add(derivedTreebank);
			}
		}

		return treebanks;
	}

	public TreebankDescriptor getDescriptor(String id) {
		TreebankDescriptor descriptor = descriptorMap.get(id);
		if(descriptor==null)
			throw new UnknownIdentifierException("No such treebank: "+id); //$NON-NLS-1$

		return descriptor;
	}

	public TreebankDescriptor getDescriptorByName(String name) {
		if(name==null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		for(Treebank treebank : treebankMap.keySet()) {
			if(name.equals(treebank.getName())) {
				return treebankMap.get(treebank);
			}
		}

		return null;
	}

	public TreebankDescriptor getDescriptor(Treebank treebank) {
		TreebankDescriptor descriptor = treebankMap.get(treebank);
		if(descriptor==null)
			throw new IllegalArgumentException("Treebank not managed by this registry: "+treebank.getName()); //$NON-NLS-1$

		return descriptor;
	}

	public Extension getExtension(Treebank treebank) {
		TreebankDescriptor descriptor = treebankMap.get(treebank);
		if(descriptor==null)
			throw new IllegalArgumentException("Treebank not managed by this registry: "+treebank.getName()); //$NON-NLS-1$

		return descriptor.getExtension();
	}

	public Treebank getTreebank(String id) {
		TreebankDescriptor descriptor = descriptorMap.get(id);
		if(descriptor==null)
			throw new UnknownIdentifierException("No such treebank: "+id); //$NON-NLS-1$

		return descriptor.getTreebank();
	}

	public Treebank getTreebankByName(String name) {
		if(name==null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		TreebankDescriptor descriptor = getDescriptorByName(name);
		return descriptor==null ? null : descriptor.getTreebank();
	}

	public String getUniqueName(String baseName) {
		Set<String> usedNames = new HashSet<>(treebankMap.size());
		for(Treebank treebank : treebankMap.keySet()) {
			usedNames.add(treebank.getName());
		}

		return StringUtil.getUniqueName(baseName, usedNames);
	}

	public TreebankListDelegate getListDelegate(Treebank treebank) {
		TreebankDescriptor descriptor = getDescriptor(treebank);
		if(delegateMap==null) {
			delegateMap = new WeakHashMap<>();
		}
		TreebankListDelegate delegate = null;
		Reference<TreebankListDelegate> ref = delegateMap.get(descriptor.getId());
		if(ref!=null) {
			delegate = ref.get();
		}

		if(delegate==null) {
			delegate = new TreebankListDelegate(treebank);
			ref = new WeakReference<>(delegate);
			delegateMap.put(descriptor.getId(), ref);
		}

		if(!delegate.hasTreebank()) {
			delegate.setTreebank(treebank);
		}

		return delegate;
	}

	public void deleteTreebank(Treebank treebank) {
		TreebankDescriptor descriptor = treebankMap.get(treebank);
		if(descriptor!=null) {

			// Release treebank resources
			try {
				treebank.destroy();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to free treebank: "+treebank.getName(), e); //$NON-NLS-1$
			}

			treebankMap.remove(treebank);
			descriptorMap.remove(descriptor.getId());
			if(delegateMap!=null) {
				delegateMap.remove(descriptor.getId());
			}

			// Reset all the derived treebanks pointing to this one
			List<DerivedTreebank> derivedTreebanks = getDerived(treebank);
			for(DerivedTreebank derivedTreebank : derivedTreebanks) {
				derivedTreebank.setBase(DUMMY_TREEBANK);
			}

			// Finally notify listeners
			eventSource.fireEvent(new EventObject(Events.REMOVED,
					"treebank", treebank, //$NON-NLS-1$
					"extension", descriptor.getExtension())); //$NON-NLS-1$

			saveBackground();
		}
	}

	public TreebankDescriptor newTreebank(String type, String name) throws Exception {
		return newTreebank(treebankTypes.get(type), name);
	}

	public TreebankDescriptor newTreebank(Extension type, String name) throws Exception {
		if(type==null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$
		if(name==null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		TreebankDescriptor descriptor = new TreebankDescriptor();
		descriptor.setId(UUID.randomUUID().toString());
		descriptor.setExtension(type);
		descriptor.setName(name);
		descriptor.instantiateTreebank();

		addTreebank(descriptor);

		return descriptor;
	}

	public void addTreebank(TreebankDescriptor descriptor) throws Exception {

		addTreebank0(descriptor);

		eventSource.fireEvent(new EventObject(Events.ADDED,
				"treebank", descriptor.getTreebank(),  //$NON-NLS-1$
				"extension", descriptor.getExtension())); //$NON-NLS-1$

		saveBackground();
	}

	private void addTreebank0(TreebankDescriptor descriptor) throws Exception {

		// Ensure uniqueness of ids
		TreebankDescriptor presentDescriptor = descriptorMap.get(descriptor.getId());
		if(presentDescriptor==descriptor) {
			return;
		}
		if(presentDescriptor!=null) {
			descriptor.setId(UUID.randomUUID().toString());
		}

		if(!descriptor.hasTreebank()) {
			descriptor.instantiateTreebank();
		}

		descriptorMap.put(descriptor.getId(), descriptor);
		treebankMap.put(descriptor.getTreebank(), descriptor);
	}

	public void treebankChanged(Treebank treebank) {
		eventSource.fireEvent(new EventObject(Events.CHANGED,
				"treebank", treebank)); //$NON-NLS-1$

		saveBackground();
	}

	public static String getTempName(Treebank treebank) {
		return treebank.hashCode()+"@"+System.currentTimeMillis(); //$NON-NLS-1$
	}

	public void setProperties(Treebank treebank, Map<String, Object> properties) {
		Map<String, Object> oldProperties = treebank.getProperties();
		oldProperties.clear();

		if(properties!=null) {
			oldProperties.putAll(properties);
		}

		treebankChanged(treebank);
	}

	public void setProperty(Treebank treebank, String key, Object value) {
		if(key==null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$

		Object oldValue = treebank.getProperty(key);
		if(oldValue==value || (value!=null && value.equals(oldValue))) {
			return;
		}

		treebank.setProperty(key, value);

		treebankChanged(treebank);
	}

	public void setName(Treebank treebank, String name) {
		if(name==null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$
		if(name.equals(treebank.getName())) {
			return;
		}

		treebank.setName(name);

		treebankChanged(treebank);
	}

	/**
	 * Allows {@code null} location
	 */
	public void setLocation(Treebank treebank, Location location) {
		if(location!=null && location.equals(treebank.getLocation())) {
			return;
		}

		treebank.setLocation(location);

		treebankChanged(treebank);
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#addListener(java.lang.String, de.ims.icarus.ui.events.EventListener)
	 */
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#removeEventListener(de.ims.icarus.ui.events.EventListener)
	 */
	public void removeListener(EventListener listener) {
		eventSource.removeEventListener(listener);
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#removeEventListener(de.ims.icarus.ui.events.EventListener, java.lang.String)
	 */
	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeEventListener(listener, eventName);
	}

	public static final Treebank DUMMY_TREEBANK = new Treebank() {

		private Map<String, Object> properties = Collections.emptyMap();

		/**
		 * @see de.ims.icarus.language.treebank.Treebank#destroy()
		 */
		@Override
		public void destroy() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.language.treebank.Treebank#isDestroyed()
		 */
		@Override
		public boolean isDestroyed() {
			return false;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public void setProperty(String key, Object value) {
			// no-op
		}

		@Override
		public void setName(String name) {
			// no-op
		}

		@Override
		public void setLocation(Location location) {
			// no-op
		}

		@Override
		public void saveState(TreebankDescriptor descriptor) {
			// no-op
		}

		@Override
		public void removeListener(EventListener listener, String eventName) {
			// no-op
		}

		@Override
		public void removeListener(EventListener listener) {
			// no-op
		}

		@Override
		public void loadState(TreebankDescriptor descriptor) {
			// no-op
		}

		@Override
		public void load() throws Exception {
			// no-op
		}

		@Override
		public boolean isLoaded() {
			return true;
		}

		@Override
		public boolean isEditable() {
			return false;
		}

		@Override
		public Object getProperty(String key) {
			return null;
		}

		@Override
		public Map<String, Object> getProperties() {
			return properties;
		}

		@Override
		public String getName() {
			return "DummyTreebank"; //$NON-NLS-1$
		}

		@Override
		public TreebankMetaData getMetaData() {
			return null;
		}

		@Override
		public Location getLocation() {
			return null;
		}

		@Override

		public ContentType getContentType() {
			return LanguageManager.getInstance().getSentenceDataContentType();
		}

		@Override
		public void free() {
			// no-op
		}

		@Override
		public void addListener(String eventName, EventListener listener) {
			// no-op
		}

		@Override
		public boolean supportsType(DataType type) {
			return false;
		}

		@Override
		public SentenceData get(int index, DataType type) {
			return null;
		}

		@Override
		public SentenceData get(int index, DataType type,
				AvailabilityObserver observer) {
			return null;
		}

		@Override
		public void set(SentenceData item, int index, DataType type) {
			// no-op
		}

		@Override
		public void remove(int index, DataType type) {
			// no-op
		}

		@Override
		public void addChangeListener(ChangeListener listener) {
			// no-op
		}

		@Override
		public void removeChangeListener(ChangeListener listener) {
			// no-op
		}

		@Override
		public String getId() {
			return getName();
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public Icon getIcon() {
			return null;
		}

		@Override
		public Object getOwner() {
			return this;
		}

		@Override
		public SentenceData get(int index) {
			return null;
		}

		@Override
		public boolean isLoading() {
			return false;
		}
	};

	public static final Comparator<Treebank> TREEBANK_NAME_COMPARATOR = new Comparator<Treebank>() {

		@Override
		public int compare(Treebank c1, Treebank c2) {
			return c1.getName().compareTo(c2.getName());
		}

	};

	private synchronized void saveBackground() {
		final TreebankSet treebankSet = new TreebankSet(descriptorMap.values());

		Runnable saveTask = new Runnable() {

			@Override
			public void run() {
				try {
					save(treebankSet);
				} catch (Exception e) {
					LoggerFactory.log(this, Level.SEVERE, "Failed to save treebank descriptor list", e); //$NON-NLS-1$
				}
			}
		};

		String title = ResourceManager.getInstance().get(
				"plugins.languageTools.treebankSaveTask.title"); //$NON-NLS-1$
		String info = ResourceManager.getInstance().get(
				"plugins.languageTools.treebankSaveTask.description", availableTreebankCount()); //$NON-NLS-1$
		Icon icon = IconRegistry.getGlobalRegistry().getIcon("treebank_saveas_edit.gif"); //$NON-NLS-1$

		TaskManager.getInstance().schedule(saveTask, title,
				info, icon, TaskPriority.DEFAULT, true);
	}

	private static final String list_file = "treebanks.xml"; //$NON-NLS-1$

	private void load() throws Exception {
		Path file = Core.getCore().getDataFolder().resolve(list_file);
		if(Files.notExists(file) || Files.size(file)==0) {
			return;
		}

		JAXBContext context = JAXBUtils.getSharedJAXBContext();
		Unmarshaller unmarshaller = context.createUnmarshaller();
		TreebankSet treebankSet = (TreebankSet) unmarshaller.unmarshal(Files.newInputStream(file));

		descriptorMap.clear();
		for(int i=0; i<treebankSet.getItemCount(); i++) {
			TreebankDescriptor descriptor = treebankSet.getItem(i);
			try {
				addTreebank0(descriptor);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to add treebank: "+descriptor, e); //$NON-NLS-1$
			}
		}
	}

	private final Object saveLock = new Object();

	private void save(TreebankSet treebankSet) throws Exception {
		synchronized (saveLock) {
			Path file = Core.getCore().getDataFolder().resolve(list_file);
			if(Files.notExists(file)) {
				Files.createFile(file);
			}

			for(int i=0; i<treebankSet.getItemCount(); i++) {
				treebankSet.getItem(i).syncFromTreebank();
			}

			JAXBContext context = JAXBUtils.getSharedJAXBContext();
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(treebankSet, Files.newOutputStream(file));
		}
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	static class TreebankInfoSet {
		@XmlElement(name="info")
		private List<TreebankInfo> items = new ArrayList<>();

		@XmlElement(name="treebank")
		private List<TreebankDescriptor> descriptors = new ArrayList<>();

		public TreebankInfoSet() {
			// no-op
		}

		public TreebankInfoSet(List<Treebank> treebanks) {
			for(Treebank treebank : treebanks) {
				TreebankDescriptor descriptor = getInstance().getDescriptor(treebank);
				TreebankInfo info = new TreebankInfo(descriptor);

				items.add(info);
				descriptors.add(descriptor);
			}
		}

		public int getItemCount() {
			if(items.size()!=descriptors.size())
				throw new IllegalStateException();

			return items.size();
		}

		public TreebankInfo getInfo(int index) {
			return items.get(index);
		}

		public TreebankDescriptor getDescriptor(int index) {
			return descriptors.get(index);
		}
	}

	@XmlRootElement(name="treebanks")
	@XmlAccessorType(XmlAccessType.FIELD)
	static class TreebankSet {
		@XmlElement(name="treebank")
		private List<TreebankDescriptor> items = new ArrayList<>();

		public TreebankSet() {
			// no-op
		}

		public TreebankSet(Collection<TreebankDescriptor> treebanks) {
			items.addAll(treebanks);
		}

		int getItemCount() {
			return items.size();
		}

		TreebankDescriptor getItem(int index) {
			return items.get(index);
		}
	}

	public void exportTreebanks(Path file, List<Treebank> treebanks) throws IOException, Exception {
		if(file==null)
			throw new NullPointerException("Invalid file"); //$NON-NLS-1$
		if(treebanks==null)
			throw new NullPointerException("Invalid treebanks"); //$NON-NLS-1$

		if(treebanks.isEmpty()) {
			return;
		}

		JAXBContext context = JAXBUtils.getSharedJAXBContext();
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		TreebankInfoSet infoSet = new TreebankInfoSet(treebanks);

		marshaller.marshal(infoSet, Files.newOutputStream(file));
	}

	private boolean isTreebankAvailable(TreebankInfo info) {
		if(!PluginUtil.getPluginRegistry().isPluginDescriptorAvailable(info.getPluginId())) {
			return false;
		}
		PluginDescriptor pluginDescriptor = PluginUtil.getPluginRegistry().getPluginDescriptor(info.getPluginId());
		Version currentVersion = pluginDescriptor.getVersion();
		Version requiredVersion = Version.parse(info.getPluginVersion());

		return currentVersion.isCompatibleWith(requiredVersion);
	}

	public TreebankImportResult importTreebanks(Path file) throws IOException, Exception {
		if(file==null)
			throw new NullPointerException("Invalid file"); //$NON-NLS-1$

		if(Files.size(file)==0) {
			return null;
		}

		JAXBContext context = JAXBUtils.getSharedJAXBContext();
		Unmarshaller unmarshaller = context.createUnmarshaller();

		TreebankInfoSet infoSet = (TreebankInfoSet) unmarshaller.unmarshal(Files.newInputStream(file));

		TreebankImportResult result = new TreebankImportResult();

		for(int i = 0; i<infoSet.getItemCount(); i++) {
			TreebankInfo info = infoSet.getInfo(i);
			TreebankDescriptor descriptor = infoSet.getDescriptor(i);

			if(!isTreebankAvailable(info)) {
				result.addUnavailable(info);
			} else {
				result.addAvailable(info, descriptor);
			}
		}

		return result;
	}

	private class ShutdownHook implements NamedRunnable {

		/**
		 * @see de.ims.icarus.Core.NamedRunnable#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"plugins.languageTools.treebankSaveTask.title"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.Core.NamedRunnable#run()
		 */
		@Override
		public void run() throws Exception {
			TreebankSet treebankSet = new TreebankSet(treebankMap.values());
			save(treebankSet);
		}
	}
}
