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
package de.ims.icarus.model.xml.sax;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.ims.icarus.logging.LogReport;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.Manifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.AnnotationLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.AnnotationManifestImpl;
import de.ims.icarus.model.standard.manifest.ContainerManifestImpl;
import de.ims.icarus.model.standard.manifest.ContextManifestImpl;
import de.ims.icarus.model.standard.manifest.CorpusManifestImpl;
import de.ims.icarus.model.standard.manifest.DriverManifestImpl;
import de.ims.icarus.model.standard.manifest.ExpressionXmlHandler;
import de.ims.icarus.model.standard.manifest.FragmentLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.HighlightLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.MarkableLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.OptionsManifestImpl;
import de.ims.icarus.model.standard.manifest.PathResolverManifestImpl;
import de.ims.icarus.model.standard.manifest.RasterizerManifestImpl;
import de.ims.icarus.model.standard.manifest.StructureLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.StructureManifestImpl;
import de.ims.icarus.model.xml.ModelXmlAttributes;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlTags;
import de.ims.icarus.util.id.Identity;

/**
 * Important constraints:
 * <ul>
 * <li>Templates will inherit all data unchanged from their ancestor template if they declare one</li>
 * <li>Templates will overwrite all data they explicitly declare</li>
 * <li>Only top-level manifests in a &lt;manifests&gt; context are considered manifests</li>
 * <li>A live corpus will clone <b>all</b> data from its inherited manifests and re-link them
 * to the new instances</li>
 * <li>A template must be completely loaded and fully resolved before it can be used for further inheritance</li>
 * </ul>
 *
 * Reading is done in 4 steps:
 * <ol>
 * <li>Parsing all template sources into intermediate builder states</li>
 * <li>Creating from every top-level builder a new template object (this is done recursively to ensure that
 * referenced manifests get fully resolved before being further processed)</li>
 * <li>Parsing all live corpora into intermediate builder states</li>
 * <li>Creating fully cloned manifest instances for each corpus, preserving template informations</li>
 * </ol>
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestXmlReader implements ModelXmlTags, ModelXmlAttributes {

	private final Set<ManifestLocation> templateSources = new HashSet<>();
	private final Set<ManifestLocation> corpusSources = new HashSet<>();
	private final AtomicBoolean reading = new AtomicBoolean(false);

	private final ParseState state = new ParseState();

	private final CorpusRegistry registry;

	public ManifestXmlReader(CorpusRegistry registry) {
		if (registry == null)
			throw new NullPointerException("Invalid registry"); //$NON-NLS-1$

		this.registry = registry;
	}

	public void addSource(ManifestLocation source) {
		if (source == null)
			throw new NullPointerException("Invalid source");  //$NON-NLS-1$

		if(reading.get())
			throw new IllegalStateException("Reading in progress, cannot add new sources"); //$NON-NLS-1$

		Set<ManifestLocation> sources = source.isTemplate() ? templateSources : corpusSources;

		if(!sources.add(source))
			throw new IllegalArgumentException("Source already registered: "+source.getUrl()); //$NON-NLS-1$
	}

	public LogReport readAll() throws ModelException, IOException, SAXException {

		if(!reading.compareAndSet(false, true))
			throw new IllegalStateException("Reading already in progress"); //$NON-NLS-1$

		XMLReader reader = newReader();

		List<Manifest> templates = new ArrayList<>();

		// Read in manifests
		for(ManifestLocation source : templateSources) {
			try (InputStream in = source.getInputStream()) {
				RootHandler handler = new RootHandler(source);

				reader.setContentHandler(handler);
				reader.setErrorHandler(handler);
				reader.setEntityResolver(handler);
				reader.setDTDHandler(handler);

				reader.parse(new InputSource(in));

				templates.addAll(handler.getTopLevelManifests());
			}
		}

		// Register all manifests
		for(Manifest template : templates) {
			registry.registerTemplate(template);
		}

		// Read in live corpora
		List<Manifest> corpora = new ArrayList<>();
		for(ManifestLocation source : corpusSources) {
			try (InputStream in = source.getInputStream()) {
				RootHandler handler = new RootHandler(source);

				reader.setContentHandler(handler);
				reader.setErrorHandler(handler);
				reader.setEntityResolver(handler);
				reader.setDTDHandler(handler);

				reader.parse(new InputSource(in));

				corpora.addAll(handler.getTopLevelManifests());
			}
		}

		// Now build and register all live corpora
		for(Manifest manifest : corpora) {
			CorpusManifest corpusManifest = (CorpusManifest) manifest;

			//TODO instantiate a fresh new corpus manifest with proper linking!

			registry.addCorpus(corpusManifest);
		}

		return state.report;
	}

	protected XMLReader newReader() throws SAXException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();

		parserFactory.setNamespaceAware(true);
//		parserFactory.setValidating(true);
		//FIXME

		try {
			parserFactory.setFeature("http://xml.org/sax/features/use-entity-resolver2", true); //$NON-NLS-1$
		} catch (SAXNotRecognizedException | SAXNotSupportedException
				| ParserConfigurationException e) {
			LoggerFactory.error(this, "Failed to activate advanced entity-resolver feature", e); //$NON-NLS-1$
		}


		SAXParser parser = null;
		try {
			parser = parserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			LoggerFactory.error(this, "Failed to create parser instance", e); //$NON-NLS-1$
		}
		return parser.getXMLReader();
	}

	@SuppressWarnings("unused")
	protected class ParseState {

		private final Stack<Object> stack = new Stack<>();

		private final LogReport report = new LogReport(ManifestXmlReader.this);

		private boolean isRoot() {
			return stack.isEmpty();
		}

		public Object push(Object item) {
			return stack.push(item);
		}

		public Object pop() {
			return stack.pop();
		}

		private String trace(String msg) {
			StringBuilder sb = new StringBuilder("<root>"); //$NON-NLS-1$

			for(int i=stack.size()-1; i>-1; i--) {
				String id = null;
				Object item = stack.get(i);

				if(item instanceof Manifest) {
					id = ((Manifest)item).getId();
				} else if(item instanceof Identity) {
					id = ((Identity)item).getId();
				}

				if(id==null) {
					id = item.getClass().getSimpleName();
//					id = "<unknown>"; //$NON-NLS-1$
				}

				sb.append('.');

				sb.append(id);
			}

			if(sb.length()>0) {
				sb.append(": "); //$NON-NLS-1$
			}

			sb.append(msg);

			return sb.toString();
		}

		private void debug(String msg) {
			report.debug(trace(msg));
		}

		private void info(String msg) {
			report.info(trace(msg));
		}

		private void warning(String msg) {
			report.warning(trace(msg));
		}

		private void error(String msg) {
			report.error(trace(msg));
		}

		private void error(String msg, Throwable t) {
			report.error(trace(msg), t);
		}
	}

	protected class RootHandler extends DefaultHandler {

		private final StringBuilder buffer = new StringBuilder();

		private final Stack<ModelXmlHandler> handlers = new Stack<>();

		// List of all top-level handlers. Used to preserve order of appearance
		private final List<Manifest> topLevelManifests = new ArrayList<>();

		private final ManifestLocation manifestLocation;

		RootHandler(ManifestLocation manifestLocation) {
			this.manifestLocation = manifestLocation;
		}

		/**
		 * @return the topLevelManifests
		 */
		public List<Manifest> getTopLevelManifests() {
			return topLevelManifests;
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			buffer.append(ch, start, length);
		}

		private void push(ModelXmlHandler handler) {
			handlers.push(handler);
			state.push(handler);
		}

		private ModelXmlHandler pop() {
			ModelXmlHandler handler = handlers.pop();
			state.pop();
			return handler;
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if(handlers.isEmpty()) {
				push(new ManifestCollector());
			}

			ModelXmlHandler current = handlers.peek();

			ModelXmlHandler future = current.startElement(manifestLocation, uri, localName, qName, attributes);

			// Delegate initial element handling to next builder
			if(future!=null && future!=current) {
				push(future);

				future.startElement(manifestLocation, uri, localName, qName, attributes);
			}
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			String text = getText();

			ModelXmlHandler current = handlers.peek();
			ModelXmlHandler future = current.endElement(manifestLocation, uri, localName, qName, text);

			// Discard current builder and switch to ancestor
			if(future==null) {
				pop();

				// Root level means we just add the manifests from the collector
				if(handlers.isEmpty()) {
					topLevelManifests.addAll(((ManifestCollector)current).getManifests());
				} else {
					// Allow ancestor to collect nested entries
					ModelXmlHandler ancestor = handlers.peek();

					ancestor.endNestedHandler(manifestLocation, uri, localName, qName, current);
				}
			}
		}

		private String logMsg(SAXParseException ex) {
			StringBuilder sb = new StringBuilder();
			sb.append(ex.getMessage()).append(":\n"); //$NON-NLS-1$
			sb.append("Message: ").append(ex.getMessage()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("Public ID: ").append(String.valueOf(ex.getPublicId())).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("System ID: ").append(String.valueOf(ex.getSystemId())).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("Line: ").append(ex.getLineNumber()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("Column: ").append(ex.getColumnNumber()); //$NON-NLS-1$
//			if(ex.getException()!=null)
//				sb.append("\nEmbedded: ").append(ex.getException()); //$NON-NLS-1$

//			report.log(level, sb.toString(), ex);

			return sb.toString();
		}

		@Override
		public void error(SAXParseException ex) throws SAXException {
			state.error(logMsg(ex));
		}

		@Override
		public void warning(SAXParseException ex) throws SAXException {
			state.warning(logMsg(ex));
		}

		@Override
		public void fatalError(SAXParseException ex) throws SAXException {
			state.error(logMsg(ex));
		}

		private String getText() {
			String text = buffer.length()==0 ? null : buffer.toString().trim();
			buffer.setLength(0);

			return text.length()==0 ? null : text;
		}
	}

	private final static Map<String, Object> templateHandlers = new HashMap<>();

	private final static Map<String, Object> liveHandlers = new HashMap<>();

	static {
		// Live manifests
		liveHandlers.put(TAG_CORPUS, CorpusManifestImpl.class);

		// Templates
		templateHandlers.put(TAG_ANNOTATION_LAYER, AnnotationLayerManifestImpl.class);
		templateHandlers.put(TAG_ANNOTATION, AnnotationManifestImpl.class);
		templateHandlers.put(TAG_CONTAINER, ContainerManifestImpl.class);
		templateHandlers.put(TAG_CONTEXT, ContextManifestImpl.class);
		templateHandlers.put(TAG_DRIVER, DriverManifestImpl.class);
		templateHandlers.put(TAG_EVAL, ExpressionXmlHandler.class);
		templateHandlers.put(TAG_FRAGMENT_LAYER, FragmentLayerManifestImpl.class);
		templateHandlers.put(TAG_HIGHLIGHT_LAYER, HighlightLayerManifestImpl.class);
		templateHandlers.put(TAG_MARKABLE_LAYER, MarkableLayerManifestImpl.class);
		templateHandlers.put(TAG_OPTIONS, OptionsManifestImpl.class);
		templateHandlers.put(TAG_PATH_RESOLVER, PathResolverManifestImpl.class);
		templateHandlers.put(TAG_RASTERIZER, RasterizerManifestImpl.class);
		templateHandlers.put(TAG_STRUCTURE_LAYER, StructureLayerManifestImpl.class);
		templateHandlers.put(TAG_STRUCTURE, StructureManifestImpl.class);
	}

	@SuppressWarnings("rawtypes")
	private final static Class[] CONSTRUCTOR_TYPES = {
		ManifestLocation.class,
		CorpusRegistry.class,
	};

	private static final Object[] constructorParams = new Object[2];

	protected ModelXmlHandler newInstance(String tag, ManifestLocation manifestLocation) throws SAXException {
		synchronized (CONSTRUCTOR_TYPES) {
			Map<String, Object> handlerLut = manifestLocation.isTemplate() ? templateHandlers : liveHandlers;
			Object current = handlerLut.get(tag);

			if(current==null)
				throw new SAXException("No handler for tag: "+tag); //$NON-NLS-1$

			Constructor<?> constructor;

			if(current instanceof Constructor) {
				constructor = (Constructor<?>) current;
			} else {
				Class<?> clazz = (Class<?>) current;
				try {
					constructor = clazz.getConstructor(CONSTRUCTOR_TYPES);
				} catch (NoSuchMethodException | SecurityException e) {
					throw new SAXException("Failed to access handler constructur", e); //$NON-NLS-1$
				}
			}

			constructorParams[0] = manifestLocation;
			constructorParams[1] = registry;

			try {
				return (ModelXmlHandler) constructor.newInstance(constructorParams);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new SAXException("Failed to invoke handler constructur", e); //$NON-NLS-1$
			}
		}
	}

	protected class ManifestCollector implements ModelXmlHandler {

		// List of all top-level handlers. Used to preserve order of appearance
		private final List<Manifest> manifests = new ArrayList<>();

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlHandler#startElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public ModelXmlHandler startElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			switch (qName) {
			case TAG_CORPORA: {
				if(manifestLocation.isTemplate())
					throw new SAXException("Illegal "+TAG_CORPORA+" tag in template manifest source"); //$NON-NLS-1$ //$NON-NLS-2$
				return this;
			}

			case TAG_TEMPLATES: {
				if(!manifestLocation.isTemplate())
					throw new SAXException("Illegal "+TAG_TEMPLATES+" tag in live corpus manifest source"); //$NON-NLS-1$ //$NON-NLS-2$
				return this;
			}

			default:
				// no-op
				break;
			}

			return newInstance(qName, manifestLocation);
		}

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlHandler#endElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public ModelXmlHandler endElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName, String text)
				throws SAXException {
			return null;
		}

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
		 */
		@Override
		public void endNestedHandler(ManifestLocation manifestLocation, String uri,
				String localName, String qName, ModelXmlHandler handler)
				throws SAXException {
			manifests.add((Manifest) handler);
		}

		/**
		 * @return the manifests
		 */
		public List<Manifest> getManifests() {
			return manifests;
		}
	}
}
