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
package de.ims.icarus.language.model.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.LayerType;
import de.ims.icarus.language.model.io.ContextReader;
import de.ims.icarus.language.model.io.ContextWriter;
import de.ims.icarus.language.model.manifest.ContextManifest;
import de.ims.icarus.language.model.manifest.CorpusManifest;
import de.ims.icarus.language.model.manifest.Derivable;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.EventSource;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.events.WeakEventSource;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.id.DuplicateIdentifierException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CorpusRegistry {

	private static volatile CorpusRegistry instance;

	public static CorpusRegistry getInstance() {
		CorpusRegistry tmp = instance;
		if(tmp==null) {
			synchronized (CorpusRegistry.class) {
				tmp = instance;
				if(tmp==null) {
					tmp = new CorpusRegistry();
					tmp.init();

					instance = tmp;
				}
			}
		}

		return tmp;
	}

	private final Map<String, Derivable> templates = new HashMap<>();
	private final Map<String, LayerType> layerTypes = new HashMap<>();
	private final Map<String, ContextReader> contextReaders = new HashMap<>();
	private final Map<String, ContextWriter> contextWriters = new HashMap<>();

	private final List<CorpusManifest> corpora = new ArrayList<>();
	private final Map<String, CorpusManifest> corpusLookup = new HashMap<>();

	private final Map<CorpusManifest, Corpus> corpusIncstances = new HashMap<>();

	private final EventSource eventSource = new WeakEventSource(this);

	private CorpusRegistry() {
		if(instance!=null)
			throw new IllegalStateException("Cannot instantiate additional registry"); //$NON-NLS-1$
	}

	private void init() {
		// TODO
	}

	private static final Pattern idPattern = Pattern.compile(
			"^\\p{Alpha}[\\w_-:]{2,}$"); //$NON-NLS-1$

	/**
	 * Verifies the validity of the given {@code id} string.
	 * <p>
	 * Valid ids are defined as follows:
	 * <ul>
	 * <li>they have a minimum length of 3 characters</li>
	 * <li>they start with an alphabetic character (lower and upper case are allowed)</li>
	 * <li>subsequent characters may be alphabetic or digits</li>
	 * <li>no special characters are allowed besides the following 3: _-: (underscore, hyphen, colon)</li>
	 * </ul>
	 *
	 * Attempting to use any other string as an identifier for arbitrary members of a corpus will
	 * result in them being rejected by the registry.
	 */
	public static boolean isValidId(String id) {
		return idPattern.matcher(id).matches();
	}

	public LayerType getLayerType(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		LayerType layerType = layerTypes.get(name);

		if(layerType==null)
			throw new IllegalArgumentException("No such layer-type: "+name); //$NON-NLS-1$

		return layerType;
	}

	public void addCorpus(CorpusManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		String id = manifest.getId();
		if(id==null)
			throw new IllegalArgumentException("Missing corpus id"); //$NON-NLS-1$
		if(!isValidId(id))
			throw new IllegalArgumentException("Invaid corpus id: "+id); //$NON-NLS-1$

		if(corpusLookup.containsKey(id))
			throw new DuplicateIdentifierException("Corpus id already in use: "+id); //$NON-NLS-1$

		corpora.add(manifest);
		corpusLookup.put(id, manifest);

		eventSource.fireEventEDT(new EventObject(Events.ADDED, "corpus", manifest)); //$NON-NLS-1$
	}

	public void removeCorpus(CorpusManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		String id = manifest.getId();
		if(id==null)
			throw new IllegalArgumentException("Missing corpus id"); //$NON-NLS-1$

		if(!corpusLookup.containsKey(id))
			throw new IllegalArgumentException("Unknown corpus id: "+id); //$NON-NLS-1$

		corpusLookup.remove(id);
		corpora.remove(manifest);

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
		}

		return instance;
	}

	public Derivable getTemplate(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		Derivable template = templates.get(id);

		if(template==null)
			throw new IllegalArgumentException("No template registered for id: "+id); //$NON-NLS-1$

		return template;
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
		List<ContextManifest> result = new ArrayList<>();

		for(Derivable template : templates.values()) {
			if(template instanceof ContextManifest) {
				result.add((ContextManifest) template);
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
			if(template instanceof ContextManifest) {
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

		Derivable current = templates.get(id);
		if(current==null) {
			templates.put(id, template);
		} else if(current!=template)
			throw new DuplicateIdentifierException("Template id already in use: "+id); //$NON-NLS-1$
	}

	public void registerContextReader(String formatId, ContextReader reader) {
		if (formatId == null)
			throw new NullPointerException("Invalid formatId"); //$NON-NLS-1$
		if (reader == null)
			throw new NullPointerException("Invalid reader"); //$NON-NLS-1$

		ContextReader currentReader = contextReaders.get(formatId);
		if(currentReader==null) {
			contextReaders.put(formatId, reader);
		} else if(currentReader!=reader)
			throw new DuplicateIdentifierException("Format-id for context reader already in use: "+formatId); //$NON-NLS-1$
	}

	public ContextReader getContextReader(String formatId) {
		if (formatId == null)
			throw new NullPointerException("Invalid formatId");  //$NON-NLS-1$

		ContextReader reader = contextReaders.get(formatId);

		if(reader==null)
			throw new IllegalArgumentException("No reader registered for format-id: "+formatId); //$NON-NLS-1$

		return reader;
	}

	public void registerContextWriter(String formatId, ContextWriter writer) {
		if (formatId == null)
			throw new NullPointerException("Invalid formatId"); //$NON-NLS-1$
		if (writer == null)
			throw new NullPointerException("Invalid writer"); //$NON-NLS-1$

		ContextWriter currentWriter = contextWriters.get(formatId);
		if(currentWriter==null) {
			contextWriters.put(formatId, writer);
		} else if(currentWriter!=writer)
			throw new DuplicateIdentifierException("Format-id for context writer already in use: "+formatId); //$NON-NLS-1$
	}

	public ContextWriter getContextWriter(String formatId) {
		if (formatId == null)
			throw new NullPointerException("Invalid formatId");  //$NON-NLS-1$

		ContextWriter writer = contextWriters.get(formatId);

		if(writer==null)
			throw new IllegalArgumentException("No writer registered for format-id: "+formatId); //$NON-NLS-1$

		return writer;
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
}
