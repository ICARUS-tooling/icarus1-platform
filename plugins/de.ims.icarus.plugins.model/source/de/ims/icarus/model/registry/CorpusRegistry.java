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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.model.registry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.rowset.spi.XmlWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.Core;
import de.ims.icarus.logging.LogReport;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.model.ModelPlugin;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.layer.LayerType;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.Derivable;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.MemberManifest;
import de.ims.icarus.model.api.manifest.OptionsManifest;
import de.ims.icarus.model.io.ContextReader;
import de.ims.icarus.model.io.ContextWriter;
import de.ims.icarus.model.standard.layer.LayerTypeWrapper;
import de.ims.icarus.model.standard.layer.LazyExtensionLayerType;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.model.xml.sax.ManifestParser;
import de.ims.icarus.model.xml.stream.XmlStreamSerializer;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.EventSource;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.events.WeakEventSource;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.id.DuplicateIdentifierException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CorpusRegistry {

	private final Map<String, Derivable> templates = new HashMap<>();
	private final Map<String, LayerType> layerTypes = new HashMap<>();
	private final Map<String, ContextReader> contextReaders = new HashMap<>();
	private final Map<String, ContextWriter> contextWriters = new HashMap<>();

	private final List<CorpusManifest> corpora = new ArrayList<>();
	private final Map<String, CorpusManifest> corpusLookup = new HashMap<>();

	private final Map<CorpusManifest, Corpus> corpusIncstances = new HashMap<>();

	private final EventSource eventSource = new WeakEventSource(this);

	public CorpusRegistry() {

	}

	//TODO externalize init call
	private void init() {

		registerTemplates();

		registerLayerTypes();

		loadCorpora();
	}

	private static final Filter<Path> xmlPathFilter = new Filter<Path>() {

		@Override
		public boolean accept(Path entry) throws IOException {
			return Files.exists(entry) && entry.endsWith(".xml"); //$NON-NLS-1$
		}
	};

	private void registerTemplates() {
		synchronized (templates) {
			// Load default template file
			loadTemplates(CorpusRegistry.class.getResource("model-templates.xml")); //$NON-NLS-1$

			PluginDescriptor descriptor = PluginUtil.getPluginRegistry()
					.getPluginDescriptor(ModelPlugin.PLUGIN_ID);

			// Load explicit template files
			for(Extension extension : descriptor.getExtensionPoint("ModelTemplates").getConnectedExtensions()) { //$NON-NLS-1$
				for(Extension.Parameter parameter : extension.getParameters("path")) { //$NON-NLS-1$
					try {
						URL url = parameter.valueAsUrl();
						loadTemplates(url);
					} catch (Exception e) {
						LoggerFactory.error(this, "Failed to load templates for path: " //$NON-NLS-1$
								+extension.getUniqueId()+" ("+parameter.rawValue()+")", e); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}

			// Process template folders
			for(Extension extension : descriptor.getExtensionPoint("ModelFolder").getConnectedExtensions()) { //$NON-NLS-1$

				Path pluginFolder = PluginUtil.getPluginFolder(extension);

				for(Extension.Parameter parameter : extension.getParameters("folder")) { //$NON-NLS-1$
					Path folder = pluginFolder.resolve(parameter.valueAsString());

					if(Files.notExists(folder)) {
						LoggerFactory.warning(this, "Missing template folder: "+folder); //$NON-NLS-1$
						continue;
					}

			        // Recursively traverse content of folder till we find plug-ins
			        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder, xmlPathFilter)) {

			        	int numEntries = 0;

						for(Path file : stream) {
							try {
								loadTemplates(file.toUri().toURL());
							} catch (Exception e) {
								LoggerFactory.error(this, "Failed to load file from template folder: " //$NON-NLS-1$
										+extension.getUniqueId()+" ("+file+")", e); //$NON-NLS-1$ //$NON-NLS-2$
							} finally {
								numEntries++;
							}
						}

						if(numEntries==0) {
							LoggerFactory.warning(this, "Template folder is empty: "+folder); //$NON-NLS-1$
							continue;
						}
			        } catch (IOException e) {
			            LoggerFactory.error(this, "Failed traversing files in plug-in folder " + folder, e); //$NON-NLS-1$
					}
				}
			}
		}
	}

	private void loadTemplates(URL url) {
		LogReport report = null;
		try {
			report = ManifestParser.getInstance().loadTemplates(url);
		} catch(Exception e) {
			LoggerFactory.error(this, "Unexpected error while loading templates: "+url, e); //$NON-NLS-1$
		} finally {
			if(report!=null) {
				report.publish();
			}
		}
	}

	private void registerLayerTypes() {

		PluginDescriptor descriptor = PluginUtil.getPluginRegistry()
				.getPluginDescriptor(ModelPlugin.PLUGIN_ID);

		// Load explicit template files
		for(Extension extension : descriptor.getExtensionPoint("LayerType").getConnectedExtensions()) { //$NON-NLS-1$

			LayerType layerType = null;

			Extension.Parameter classParam = extension.getParameter("class"); //$NON-NLS-1$
			if(classParam!=null) {
				layerType = new LayerTypeWrapper(extension.getId(), extension);
			} else {
				layerType = new LazyExtensionLayerType(extension);
			}

			try {
				registerLayerType(layerType);
			} catch(Exception e) {
				LoggerFactory.error(this, "Failed to register layer type: "+extension, e); //$NON-NLS-1$
			}
		}
	}

	private void loadCorpora() {
		synchronized (corpora) {
			corpora.clear();

			Path file = getCorporaFile();
			URL url = null;
			try {
				url = file.toUri().toURL();
			} catch (MalformedURLException e) {
				LoggerFactory.error(this, "Failed to resolve corpora.xml url", e); //$NON-NLS-1$
			}

			if(url==null) {
				return;
			}

			// Note: the parser will handle all the registration of corpora
			// that it can find in the xml file. We hold the lock on the
			// corpora list so that no interference is possible.
			LogReport report = ManifestParser.getInstance().loadCorpora(url);

			//TODO extract errors from report and display dialog to user
			report.publish();
		}
	}

	public void saveCorpora() {
		generation.incrementAndGet();

		scheduleUpdate();
	}

	private Path getCorporaFile() {
		return Core.getCore().getDataFolder().resolve("corpora.xml"); //$NON-NLS-1$
	}

	private static final Object writeLock = new Object();
	private final AtomicInteger generation = new AtomicInteger();

	private final AtomicBoolean updatePending = new AtomicBoolean(false);

	private void scheduleUpdate() {
		if(updatePending.compareAndSet(false, true)) {
			TaskManager.getInstance().execute(new SaveTask());
		}
	}

	private void save() {
		Path file = getCorporaFile();
		XMLOutputFactory factory = XMLOutputFactory.newFactory();

		XmlSerializer serializer = null;
		try {
			@SuppressWarnings("resource")
			XMLStreamWriter writer = factory.createXMLStreamWriter(Files.newOutputStream(file), "UTF-8"); //$NON-NLS-1$
			serializer = new XmlStreamSerializer(writer);

			serializer.startDocument();

			synchronized (corpora) {
				for(CorpusManifest manifest : corpora) {
					XmlWriter.writeCorpusManifestElement(serializer, manifest);
				}
			}

			serializer.endDocument();

		} catch (FileNotFoundException e) {
			LoggerFactory.error(this, "Failed to access corpora file: "+file, e); //$NON-NLS-1$
		} catch (XMLStreamException e) {
			LoggerFactory.error(this, "Xml-Stream error while writing corpora file: "+file, e); //$NON-NLS-1$
		} catch (Exception e) {
			LoggerFactory.error(this, "Failed to write corpora file: "+file, e); //$NON-NLS-1$
		} finally {
			if(serializer!=null) {
				try {
					serializer.close();
				} catch (Exception e) {
					LoggerFactory.error(this, "Failed to close xml stream writer", e); //$NON-NLS-1$
				}
			}
		}
	}

	private class SaveTask implements Runnable {

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {

			int savedGeneration;

			synchronized (writeLock) {
				if(!updatePending.compareAndSet(true, false))
					throw new CorruptedStateException();
				savedGeneration = generation.get();

				try {
					save();
				} catch (Exception e) {
					LoggerFactory.error(this, "Failed to save corpora", e); //$NON-NLS-1$
				}
			}

			if(savedGeneration!=generation.get()) {
				scheduleUpdate();
			}
		}
	}

	private static final Pattern idPattern = Pattern.compile(
			"^\\p{Alpha}[\\w_-]{2,}$"); //$NON-NLS-1$

	private static Matcher idMatcher;

	/**
	 * Verifies the validity of the given {@code id} string.
	 * <p>
	 * Valid ids are defined as follows:
	 * <ul>
	 * <li>they have a minimum length of 3 characters</li>
	 * <li>they start with an alphabetic character (lower and upper case are allowed)</li>
	 * <li>subsequent characters may be alphabetic or digits</li>
	 * <li>no whitespaces, control characters or code points with 2 or more bytes are allowed</li>
	 * <li>no special characters are allowed besides the following 2: _- (underscore, hyphen)</li>
	 * </ul>
	 *
	 * Attempting to use any other string as an identifier for arbitrary members of a corpus will
	 * result in them being rejected by the registry.
	 */
	public static boolean isValidId(String id) {
		synchronized (idPattern) {
			if(idMatcher==null) {
				idMatcher = idPattern.matcher(id);
			} else {
				idMatcher.reset(id);
			}

			return idMatcher.matches();
		}
	}

	public LayerType getLayerType(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		synchronized (layerTypes) {
			LayerType layerType = layerTypes.get(name);

			if(layerType==null)
				throw new IllegalArgumentException("No such layer-type: "+name); //$NON-NLS-1$

			return layerType;
		}
	}

	public void registerLayerType(LayerType layerType) {
		if (layerType == null)
			throw new NullPointerException("Invalid layerType"); //$NON-NLS-1$

		String id = layerType.getId();
		if(id==null)
			throw new IllegalArgumentException("Missing id on layer type"); //$NON-NLS-1$
		if(!isValidId(id))
			throw new IllegalArgumentException("Invaid layer id: "+id); //$NON-NLS-1$

		synchronized (layerTypes) {
			LayerType currentType = layerTypes.get(id);
			if(currentType!=null && currentType!=layerType)
				throw new DuplicateIdentifierException("Type id already in use: "+id); //$NON-NLS-1$

			layerTypes.put(id, layerType);
		}
	}

	public void addCorpus(CorpusManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		String id = manifest.getId();
		if(id==null)
			throw new IllegalArgumentException("Missing corpus id"); //$NON-NLS-1$
		if(!isValidId(id))
			throw new IllegalArgumentException("Invaid corpus id: "+id); //$NON-NLS-1$

		synchronized (corpora) {
			if(corpusLookup.containsKey(id))
				throw new DuplicateIdentifierException("Corpus id already in use: "+id); //$NON-NLS-1$

			corpora.add(manifest);
			corpusLookup.put(id, manifest);
		}

		eventSource.fireEventEDT(new EventObject(Events.ADDED, "corpus", manifest)); //$NON-NLS-1$
	}

	public void removeCorpus(CorpusManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		String id = manifest.getId();
		if(id==null)
			throw new IllegalArgumentException("Missing corpus id"); //$NON-NLS-1$

		synchronized (corpora) {
			if(!corpusLookup.containsKey(id))
				throw new IllegalArgumentException("Unknown corpus id: "+id); //$NON-NLS-1$

			corpusLookup.remove(id);
			corpora.remove(manifest);
		}

		// Clear up
		Corpus corpus = corpusIncstances.remove(manifest);
		if(corpus!=null) {
			destroy(corpus);
		}

		eventSource.fireEventEDT(new EventObject(Events.REMOVED, "corpus", manifest)); //$NON-NLS-1$
	}

	private Corpus instantiate(CorpusManifest manifest) {
		return null;
		//FIXME
	}

	private void destroy(Corpus corpus) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus");  //$NON-NLS-1$

		try {
			corpus.free();
		} catch(Exception e) {
			LoggerFactory.error(this, "Failed to free corpus resources: "+corpus.getManifest().getId(), e); //$NON-NLS-1$
		}
	}

	public CorpusManifest getCorpus(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		CorpusManifest manifest = corpusLookup.get(id);
		if(manifest==null)
			throw new IllegalArgumentException("No such corpus: "+id); //$NON-NLS-1$

		return manifest;
	}

	public Set<String> getCorpusIds() {
		return CollectionUtils.getSetProxy(corpusLookup.keySet());
	}

	public List<CorpusManifest> getCorpora() {
		return CollectionUtils.getListProxy(corpora);
	}

	public Corpus getCorpusInstance(CorpusManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		String id = manifest.getId();
		if(id==null)
			throw new IllegalArgumentException("Missing corpus id"); //$NON-NLS-1$

		if(!corpusLookup.containsKey(id))
			throw new IllegalArgumentException("Unknown corpus id: "+id); //$NON-NLS-1$

		Corpus instance = corpusIncstances.get(manifest);
		if(instance==null) {
			instance = instantiate(manifest);
			corpusIncstances.put(manifest, instance);
		} else if(instance.getManifest()!=manifest)
			throw new CorruptedStateException("Illegal modification of corpus menifest detected: "+manifest); //$NON-NLS-1$

		return instance;
	}

	public boolean hasTemplate(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		synchronized (templates) {
			return templates.containsKey(id);
		}
	}

	public Derivable getTemplate(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		synchronized (templates) {
			Derivable template = templates.get(id);

			if(template==null)
				throw new IllegalArgumentException("No template registered for id: "+id); //$NON-NLS-1$
			if(!id.equals(template.getId()))
				throw new CorruptedStateException("Illegal modification of template id detected. Expected "+id+" - got "+template.getId()); //$NON-NLS-1$ //$NON-NLS-2$

			return template;
		}
	}


	public void addContext(CorpusManifest corpus, ContextManifest context) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		if(context.isTemplate())
			throw new IllegalArgumentException("Cannot add a context template to a live corpus: "+context); //$NON-NLS-1$

		corpus.addCustomContextManifest(context);

		eventSource.fireEvent(new EventObject(Events.ADDED,
				"corpus", corpus, "context", context)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void removeContext(CorpusManifest corpus, ContextManifest context) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		if(context.isTemplate())
			throw new IllegalArgumentException("Attempting to remove a context template: "+context); //$NON-NLS-1$

		corpus.removeCustomContextManifest(context);

		eventSource.fireEvent(new EventObject(Events.REMOVED,
				"corpus", corpus, "context", context)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void corpusChanged(CorpusManifest corpus) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		eventSource.fireEvent(new EventObject(Events.CHANGED,
				"corpus", corpus)); //$NON-NLS-1$

		saveCorpora();
	}

	public void contextChanged(ContextManifest context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		eventSource.fireEvent(new EventObject(Events.CHANGED,
				"corpus", context.getCorpusManifest(), //$NON-NLS-1$
				"context", context)); //$NON-NLS-1$
	}

	/**
	 * Returns all the {@code ContextManifest} templates added to this registry.
	 * @return
	 *
	 * @see #getRootContextTemplates()
	 */
	public List<ContextManifest> getContextTemplates() {
		@SuppressWarnings("unchecked")
		List<ContextManifest> result = (List<ContextManifest>)
				getTemplatesOfType(ManifestType.CONTEXT_MANIFEST);

		return result;
	}

	private static boolean isManifestOfType(Derivable template, ManifestType type) {
		if(template instanceof MemberManifest) {
			return ((MemberManifest)template).getManifestType()==type;
		}

		return false;
	}

	/**
	 * Returns all previously registered templates that are of the given
	 * {@code ManifestType}. Note that this method only returns templates
	 * implementing the {@link MemberManifest} interface! So for example
	 * it is not possible to collect templates for the {@link OptionsManifest}
	 * interface this way. Use the {@link #getTemplatesOfClass(Class)} for
	 * such cases.
	 *
	 * @throws NullPointerException if the {@code type} argument is {@code null}
	 * @see #getTemplatesOfClass(Class)
	 */
	public List<? extends MemberManifest> getTemplatesOfType(ManifestType type) {
		if (type == null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$

		List<MemberManifest> result = new ArrayList<>();

		for(Derivable template : templates.values()) {
			if(isManifestOfType(template, type)) {
				result.add((MemberManifest) template);
			}
		}

		return result;
	}

	/**
	 * Returns all previously templates that derive from the given {@code Class}.
	 *
	 * @throws NullPointerException if the {@code clazz} argument is {@code null}
	 */
	public <E extends Derivable> List<E> getTemplatesOfClass(Class<E> clazz) {
		if (clazz == null)
			throw new NullPointerException("Invalid clazz"); //$NON-NLS-1$

		List<E> result = new ArrayList<>();

		for(Derivable template : templates.values()) {
			if(clazz.isAssignableFrom(template.getClass())) {
				result.add(clazz.cast(template));
			}
		}

		return result;
	}

	/**
	 * Returns a list of {@code ContextManifest} objects that can be used to
	 * create a new corpus by serving as the default context of that corpus.
	 * Suitability is checked by means of the {@link ContextManifest#isIndependentContext()}
	 * method returning {@code true}.
	 *
	 * @return
	 */
	public List<ContextManifest> getRootContextTemplates() {
		List<ContextManifest> result = new ArrayList<>();

		for(Derivable template : templates.values()) {
			if(isManifestOfType(template, ManifestType.CONTEXT_MANIFEST)) {
				ContextManifest manifest = (ContextManifest) template;
				if(manifest.isIndependentContext()) {
					result.add(manifest);
				}
			}
		}

		return result;
	}

	public void registerTemplate(Derivable template) {
		if (template == null)
			throw new NullPointerException("Invalid template"); //$NON-NLS-1$

		if(!template.isTemplate())
			throw new IllegalArgumentException("Provided derivable object is not a proper template"); //$NON-NLS-1$

		String id = template.getId();
		if(id==null)
			throw new IllegalArgumentException("Template does not declare valid identifier"); //$NON-NLS-1$
		if(!isValidId(id))
			throw new IllegalArgumentException("Invalid template id: "+id); //$NON-NLS-1$

		synchronized (templates) {
			Derivable current = templates.get(id);
			if(current==null) {
				templates.put(id, template);
			} else if(current!=template)
				throw new DuplicateIdentifierException("Template id already in use: "+id); //$NON-NLS-1$
		}
	}

	public void registerContextReader(String formatId, ContextReader reader) {
		if (formatId == null)
			throw new NullPointerException("Invalid formatId"); //$NON-NLS-1$
		if (reader == null)
			throw new NullPointerException("Invalid reader"); //$NON-NLS-1$

		synchronized (contextReaders) {
			ContextReader currentReader = contextReaders.get(formatId);
			if(currentReader==null) {
				contextReaders.put(formatId, reader);
			} else if(currentReader!=reader)
				throw new DuplicateIdentifierException("Format-id for context reader already in use: "+formatId); //$NON-NLS-1$
		}
	}

	public ContextReader getContextReader(String formatId) {
		if (formatId == null)
			throw new NullPointerException("Invalid formatId");  //$NON-NLS-1$

		synchronized (contextReaders) {
			ContextReader reader = contextReaders.get(formatId);

			if(reader==null)
				throw new IllegalArgumentException("No reader registered for format-id: "+formatId); //$NON-NLS-1$

			return reader;
		}
	}

	public void registerContextWriter(String formatId, ContextWriter writer) {
		if (formatId == null)
			throw new NullPointerException("Invalid formatId"); //$NON-NLS-1$
		if (writer == null)
			throw new NullPointerException("Invalid writer"); //$NON-NLS-1$

		synchronized (contextWriters) {
			ContextWriter currentWriter = contextWriters.get(formatId);
			if(currentWriter==null) {
				contextWriters.put(formatId, writer);
			} else if(currentWriter!=writer)
				throw new DuplicateIdentifierException("Format-id for context writer already in use: "+formatId); //$NON-NLS-1$
		}
	}

	public ContextWriter getContextWriter(String formatId) {
		if (formatId == null)
			throw new NullPointerException("Invalid formatId");  //$NON-NLS-1$

		synchronized (contextWriters) {
			ContextWriter writer = contextWriters.get(formatId);

			if(writer==null)
				throw new IllegalArgumentException("No writer registered for format-id: "+formatId); //$NON-NLS-1$

			return writer;
		}
	}

	/**
	 * @param eventName
	 * @param listener
	 * @see de.ims.icarus.ui.events.EventSource#addListener(java.lang.String, de.ims.icarus.ui.events.EventListener)
	 */
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * @param listener
	 * @see de.ims.icarus.ui.events.EventSource#removeEventListener(de.ims.icarus.ui.events.EventListener)
	 */
	public void removeEventListener(EventListener listener) {
		eventSource.removeEventListener(listener);
	}

	/**
	 * @param listener
	 * @param eventName
	 * @see de.ims.icarus.ui.events.EventSource#removeEventListener(de.ims.icarus.ui.events.EventListener, java.lang.String)
	 */
	public void removeEventListener(EventListener listener, String eventName) {
		eventSource.removeEventListener(listener, eventName);
	}

	public LayerType getOverlayLayerType() {
		return getLayerType(DefaultLayerTypes.MARK_LAYER_OVERLAY);
	}
}
