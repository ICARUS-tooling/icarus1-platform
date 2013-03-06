/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.corpus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.ikarus_systems.icarus.Core;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.language_tools.LanguageToolsConstants;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.EventSource;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.ui.events.WeakEventSource;
import net.ikarus_systems.icarus.util.Location;
import net.ikarus_systems.icarus.util.UnknownIdentifierException;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;



/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusRegistry {

	private static CorpusRegistry instance;
	
	public static CorpusRegistry getInstance() {
		if(instance==null) {
			synchronized (CorpusRegistry.class) {
				if(instance==null) {
					instance = new CorpusRegistry();
				}
			}
		}
		return instance;
	}
	
	private EventSource eventSource;
	
	private Map<String, CorpusDescriptor> descriptorMap = new HashMap<>();
	private Map<Corpus, CorpusDescriptor> corporaMap = new HashMap<>();
	
	private Map<String, Extension> corpusTypes = new HashMap<>();
	
	private static Logger logger;
	
	private static Logger getLogger() {
		if(logger==null) {
			logger = LoggerFactory.getLogger(CorpusRegistry.class);
		}
		return logger;
	}
	
	private CorpusRegistry() {
		eventSource = new WeakEventSource(this);
		
		// Load all connected corpus extensions
		PluginDescriptor descriptor = PluginUtil.getPluginRegistry().getPluginDescriptor(
				LanguageToolsConstants.LANGUAGE_TOOLS_PLUGIN_ID);
		for(Extension extension : descriptor.getExtensionPoint("Corpus").getConnectedExtensions()) { //$NON-NLS-1$
			corpusTypes.put(extension.getId(), extension);
		}
		
		// Attempt to load corpus list
		try {
			load();
		} catch (Exception e) {
			getLogger().log(LoggerFactory.record(Level.SEVERE, 
					"Failed to load corpora list", e)); //$NON-NLS-1$
		}
	}
	
	private void load() throws Exception {
		File file = new File(Core.getCore().getDataFolder(), "corpora.xml"); //$NON-NLS-1$
		if(!file.exists() || file.length()==0) {
			return;
		}
		
		JAXBContext context = JAXBContext.newInstance(CorpusBuffer.class);
		
		CorpusBuffer buffer = (CorpusBuffer) context.createUnmarshaller().unmarshal(file);
		descriptorMap.clear();
		for(CorpusDescriptor descriptor : buffer.corpora) {
			try {
				descriptor.instantiateCorpus();
			} catch(Exception e) {
				getLogger().log(LoggerFactory.record(Level.SEVERE, 
						"Failed to instantiate corpus: "+descriptor, e)); //$NON-NLS-1$
			}
		}
	}
	
	private void save() throws Exception {
		File file = new File(Core.getCore().getDataFolder(), "corpora.xml"); //$NON-NLS-1$
		if(!file.exists()) {
			file.createNewFile();
		}
		
		CorpusBuffer buffer = new CorpusBuffer();
		for(CorpusDescriptor descriptor : descriptorMap.values()) {
			descriptor.syncFromCorpus();
			buffer.corpora.add(descriptor);
		}

		JAXBContext context = JAXBContext.newInstance(CorpusBuffer.class);
		context.createMarshaller().marshal(descriptorMap, file);
	}
	
	public Set<Extension> availableCorpusTypes() {
		return new HashSet<>(corpusTypes.values());
	}
	
	public Collection<Corpus> availableCorpora() {
		return Collections.unmodifiableCollection(corporaMap.keySet());
	}
	
	public Set<Extension> compatibleCorpusTypes(String grammar) {
		if(grammar==null)
			throw new IllegalArgumentException("Invalid grammar"); //$NON-NLS-1$
		
		Set<Extension> compatibleTypes = new HashSet<>();
		
		loop_extensions : for(Extension extension : corpusTypes.values()) {
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
	
	public List<Corpus> getCorporaForType(Extension type) {
		List<Corpus> corpora = new ArrayList<>();
		
		for(Entry<Corpus, CorpusDescriptor> entry : corporaMap.entrySet()) {
			if(entry.getValue().getExtension()==type) {
				corpora.add(entry.getKey());
			}
		}
		
		return corpora;
	}
	
	public List<DerivedCorpus> getDerivedCorpora(Corpus base) {
		List<DerivedCorpus> derivedCorpora = new ArrayList<>();
		
		for(Corpus corpus : corporaMap.keySet()) {
			if(!(corpus instanceof DerivedCorpus)) {
				continue;
			}
			
			DerivedCorpus derivedCorpus = (DerivedCorpus) corpus;
			if(derivedCorpus.getBase()==base) {
				derivedCorpora.add(derivedCorpus);
			}
		}
		
		return derivedCorpora;
	}
	
	public CorpusDescriptor getDescriptor(String id) {
		CorpusDescriptor descriptor = descriptorMap.get(id);
		if(descriptor==null)
			throw new UnknownIdentifierException("No such corpus: "+id); //$NON-NLS-1$
		
		return descriptor;
	}
	
	public CorpusDescriptor getDescriptorByName(String name) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		
		for(Corpus corpus : corporaMap.keySet()) {
			if(name.equals(corpus.getName())) {
				return corporaMap.get(corpus);
			}
		}
		
		return null;
	}
	
	public CorpusDescriptor getDescriptor(Corpus corpus) {
		CorpusDescriptor descriptor = corporaMap.get(corpus);
		if(descriptor==null)
			throw new IllegalArgumentException("Corpus not managed by this registry: "+corpus.getName()); //$NON-NLS-1$
		
		return descriptor;
	}
	
	public Extension getExtension(Corpus corpus) {
		CorpusDescriptor descriptor = corporaMap.get(corpus);
		if(descriptor==null)
			throw new IllegalArgumentException("Corpus not managed by this registry: "+corpus.getName()); //$NON-NLS-1$
		
		return descriptor.getExtension();
	}
	
	public Corpus getCorpus(String id) {
		CorpusDescriptor descriptor = descriptorMap.get(id);
		if(descriptor==null)
			throw new UnknownIdentifierException("No such corpus: "+id); //$NON-NLS-1$
		
		return descriptor.getCorpus();
	}
	
	public Corpus getCorpusByName(String name) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		
		CorpusDescriptor descriptor = getDescriptorByName(name);
		return descriptor==null ? null : descriptor.getCorpus();
	}
	
	private static Pattern indexPattern;
	
	public String getUniqueName(String baseName) {
		Set<String> usedNames = new HashSet<>(corporaMap.size());
		for(Corpus corpus : corporaMap.keySet()) {
			usedNames.add(corpus.getName());
		}

		String name = baseName;
		int count = 2;
		
		if(indexPattern==null) {
			indexPattern = Pattern.compile("\\((\\d+)\\)$"); //$NON-NLS-1$
		}
		
		Matcher matcher = indexPattern.matcher(baseName);
		if(matcher.find()) {
			int currentCount = 0;
			try {
				currentCount = Integer.parseInt(matcher.group(1));
			} catch(NumberFormatException e) {
				getLogger().log(LoggerFactory.record(Level.SEVERE, 
						"Failed to parse existing base name index suffix: "+baseName, e)); //$NON-NLS-1$
			}
			
			count = Math.max(count, currentCount+1);
			baseName = baseName.substring(0, baseName.length()-matcher.group().length()).trim();
		}
		
		if(usedNames.contains(name)) {
			while(usedNames.contains((name = baseName+" ("+count+")"))) { //$NON-NLS-1$ //$NON-NLS-2$
				count++;
			}
		}
		
		return name;
	}
	
	public void deleteCorpus(Corpus corpus) {
		CorpusDescriptor descriptor = corporaMap.remove(corpus);
		if(descriptor!=null) {			
			descriptorMap.remove(descriptor.getId());
			
			// Reset all the derived corpora pointing to this one
			List<DerivedCorpus> derivedCorpora = getDerivedCorpora(corpus);
			for(DerivedCorpus derivedCorpus : derivedCorpora) {
				derivedCorpus.setBase(DUMMY_CORPUS);
			}
			
			// Release corpus resources
			try {
				corpus.free();
			} catch(Exception e) {
				logger.log(LoggerFactory.record(Level.SEVERE, 
						"Failed to free corpus: "+corpus.getName(), e)); //$NON-NLS-1$
			}
			
			// Finally notify listeners
			eventSource.fireEvent(new EventObject(Events.REMOVED, 
					"corpus", corpus, //$NON-NLS-1$
					"extension", descriptor.getExtension())); //$NON-NLS-1$
		}
	}

	public CorpusDescriptor newCorpus(String type, String name) throws Exception {
		return newCorpus(corpusTypes.get(type), name);
	}
	
	public CorpusDescriptor newCorpus(Extension type, String name) throws Exception {
		if(type==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		
		CorpusDescriptor descriptor = new CorpusDescriptor();
		descriptor.setId(UUID.randomUUID().toString());
		descriptor.setExtension(type);
		descriptor.setName(name);
		descriptor.instantiateCorpus();
		
		addCorpus(descriptor);
		
		return descriptor;
	}
	
	void addCorpus(CorpusDescriptor descriptor) {
		descriptorMap.put(descriptor.getId(), descriptor);
		corporaMap.put(descriptor.getCorpus(), descriptor);
		
		eventSource.fireEvent(new EventObject(Events.ADDED, 
				"corpus", descriptor.getCorpus(),  //$NON-NLS-1$
				"extension", descriptor.getExtension())); //$NON-NLS-1$
	}
	
	public void corpusChanged(Corpus corpus) {
		eventSource.fireEvent(new EventObject(Events.CHANGED, 
				"corpus", corpus)); //$NON-NLS-1$
	}
	
	public static String getTempName(Corpus corpus) {
		return corpus.hashCode()+"@"+System.currentTimeMillis(); //$NON-NLS-1$
	}
	
	public void setProperties(Corpus corpus, Map<String, Object> properties) {		
		Map<String, Object> oldProperties = corpus.getProperties();
		oldProperties.clear();
		
		if(properties!=null) {
			oldProperties.putAll(properties);
		}
		
		corpusChanged(corpus);
	}
	
	public void setProperty(Corpus corpus, String key, Object value) {
		if(key==null)
			throw new IllegalArgumentException("Invalid key"); //$NON-NLS-1$
		
		Object oldValue = corpus.getProperty(key);
		if(oldValue==value || (value!=null && value.equals(oldValue))) {
			return;
		}
		
		corpus.setProperty(key, value);
		
		corpusChanged(corpus);
	}
	
	public void setName(Corpus corpus, String name) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		if(name.equals(corpus.getName())) {
			return;
		}
		
		corpus.setName(name);
		
		corpusChanged(corpus);
	}
	
	public void setLocation(Corpus corpus, Location location) {
		if(location==null)
			throw new IllegalArgumentException("Invalid location"); //$NON-NLS-1$
		if(location.equals(corpus.getLocation())) {
			return;
		}
		
		corpus.setLocation(location);
		
		corpusChanged(corpus);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#addListener(java.lang.String, net.ikarus_systems.icarus.ui.events.EventListener)
	 */
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#removeListener(net.ikarus_systems.icarus.ui.events.EventListener)
	 */
	public void removeListener(EventListener listener) {
		eventSource.removeListener(listener);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#removeListener(net.ikarus_systems.icarus.ui.events.EventListener, java.lang.String)
	 */
	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}
	
	public static final Corpus DUMMY_CORPUS = new Corpus() {
		
		private Map<String, Object> properties = Collections.emptyMap();
		
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
		public void saveState(CorpusDescriptor descriptor) {
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
		public void remove(int index) {
			// no-op
		}
		
		@Override
		public void remove(SentenceData item) {
			// no-op
		}
		
		@Override
		public void loadState(CorpusDescriptor descriptor) {
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
			return "DummyCorpus"; //$NON-NLS-1$
		}
		
		@Override
		public CorpusMetaData getMetaData() {
			return null;
		}
		
		@Override
		public Location getLocation() {
			return null;
		}
		
		@Override
		public Class<? extends SentenceData> getEntryClass() {
			return SentenceData.class;
		}
		
		@Override
		public SentenceData get(int index, CorpusObserver observer) {
			return null;
		}
		
		@Override
		public SentenceData get(int index) {
			return null;
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
		public void add(SentenceData item, int index) {
			// no-op
		}
		
		@Override
		public void add(SentenceData item) {
			// no-op
		}

		@Override
		public boolean hasGold() {
			return false;
		}

		@Override
		public SentenceData getGold(int index) {
			return null;
		}

		@Override
		public SentenceData getGold(int index, CorpusObserver observer) {
			return null;
		}
	};
	
	public static final Comparator<Corpus> CORPUS_NAME_COMPARATOR = new Comparator<Corpus>() {

		@Override
		public int compare(Corpus c1, Corpus c2) {
			return c1.getName().compareTo(c2.getName());
		}
		
	};
	
	@XmlRootElement(name="corpora")
	private class CorpusBuffer {
		
		@XmlAttribute(name="timestamp")
		private String timestamp = new Date().toString();
		
		@XmlElement(name="corpus")
		private List<CorpusDescriptor> corpora = new ArrayList<>();
	}
}
