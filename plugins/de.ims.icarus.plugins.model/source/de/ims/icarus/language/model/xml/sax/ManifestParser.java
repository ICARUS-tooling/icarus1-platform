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
package de.ims.icarus.language.model.xml.sax;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

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
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.helpers.DefaultHandler;

import de.ims.icarus.language.model.manifest.CorpusManifest;
import de.ims.icarus.language.model.manifest.Manifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.language.model.manifest.Template;
import de.ims.icarus.language.model.registry.CorpusRegistry;
import de.ims.icarus.language.model.xml.sax.handlers.AnnotationElementHandler;
import de.ims.icarus.language.model.xml.sax.handlers.AnnotationLayerElementHandler;
import de.ims.icarus.language.model.xml.sax.handlers.ContainerElementHandler;
import de.ims.icarus.language.model.xml.sax.handlers.ContextElementHandler;
import de.ims.icarus.language.model.xml.sax.handlers.ContextReaderElementHandler;
import de.ims.icarus.language.model.xml.sax.handlers.ContextWriterElementHandler;
import de.ims.icarus.language.model.xml.sax.handlers.CorpusElementHandler;
import de.ims.icarus.language.model.xml.sax.handlers.MarkableLayerElementHandler;
import de.ims.icarus.language.model.xml.sax.handlers.OptionsElementHandler;
import de.ims.icarus.language.model.xml.sax.handlers.PathResolverElementHandler;
import de.ims.icarus.language.model.xml.sax.handlers.StructureElementHandler;
import de.ims.icarus.language.model.xml.sax.handlers.StructureLayerElementHandler;
import de.ims.icarus.language.model.xml.sax.handlers.ValuesElementHandler;
import de.ims.icarus.logging.LogReport;
import de.ims.icarus.logging.LoggerFactory;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestParser {

	private volatile static ManifestParser instance;

	public static ManifestParser getInstance() {
		ManifestParser result = instance;

		if (result == null) {
			synchronized (ManifestParser.class) {
				result = instance;

				if (result == null) {
					instance = new ManifestParser();
					result = instance;
				}
			}
		}

		return result;
	}

	private SAXParserFactory parserFactory;

	private synchronized SAXParserFactory getParserFactory() {
		if(parserFactory==null) {
			parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			parserFactory.setValidating(true);

			try {
				parserFactory.setFeature("http://xml.org/sax/features/use-entity-resolver2", true); //$NON-NLS-1$
			} catch (SAXNotRecognizedException | SAXNotSupportedException
					| ParserConfigurationException e) {
				LoggerFactory.error(this, "Failed to activate advanced entity-resolver feature", e); //$NON-NLS-1$
			}
		}
		return parserFactory;
	}

	private synchronized XMLReader getReader() throws ParserConfigurationException, SAXException {
		SAXParserFactory parserFactory = getParserFactory();
		SAXParser parser = parserFactory.newSAXParser();
		return parser.getXMLReader();
	}

	public LogReport loadCorpora(URL url) {
		return loadResource(url, false);
	}

	public LogReport loadTemplates(URL url) {
		return loadResource(url, true);
	}

	private LogReport loadResource(URL url, boolean allowCorpora) {
		if (url == null)
			throw new NullPointerException("Invalid url");  //$NON-NLS-1$

		LogReport report = new LogReport(this);

		try {
			XMLReader reader = getReader();

			ModelHandler handler = new ModelHandler(report, allowCorpora);

			reader.setEntityResolver(handler);
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.setDTDHandler(handler);

			reader.parse(new InputSource(url.openStream()));

			List<CorpusManifest> corpora = handler.getCorpora();
			if(allowCorpora && !corpora.isEmpty()) {
				for(CorpusManifest manifest : corpora) {
					CorpusRegistry.getInstance().addCorpus(manifest);
				}
			}
		} catch (IOException e) {
			report.error("Failed to load model data", e); //$NON-NLS-1$
		} catch (SAXException e) {
			report.error("Parsing error while loading model data", e); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			report.error("Unable to configure model parser", e); //$NON-NLS-1$
		}

		return report;
	}

	private static Map<String, Class<?>> handlerLookup = new HashMap<>();
	static {
		handlerLookup.put("corpus", CorpusElementHandler.class); //$NON-NLS-1$
		handlerLookup.put("context", ContextElementHandler.class); //$NON-NLS-1$
		handlerLookup.put("context-reader", ContextReaderElementHandler.class); //$NON-NLS-1$
		handlerLookup.put("context-writer", ContextWriterElementHandler.class); //$NON-NLS-1$
		handlerLookup.put("path-resolver", PathResolverElementHandler.class); //$NON-NLS-1$
		handlerLookup.put("markable-layer", MarkableLayerElementHandler.class); //$NON-NLS-1$
		handlerLookup.put("structure-layer", StructureLayerElementHandler.class); //$NON-NLS-1$
		handlerLookup.put("annotation-layer", AnnotationLayerElementHandler.class); //$NON-NLS-1$
		handlerLookup.put("options", OptionsElementHandler.class); //$NON-NLS-1$
		handlerLookup.put("annotation", AnnotationElementHandler.class); //$NON-NLS-1$
		handlerLookup.put("container", ContainerElementHandler.class); //$NON-NLS-1$
		handlerLookup.put("structure", StructureElementHandler.class); //$NON-NLS-1$
		handlerLookup.put("values", ValuesElementHandler.class); //$NON-NLS-1$
	}

	@SuppressWarnings("rawtypes")
	private static Class<ModelElementHandler> defaultGetHandlerClass(String localName) {
		@SuppressWarnings("unchecked")
		Class<ModelElementHandler> clazz = (Class<ModelElementHandler>) handlerLookup.get(localName);

		return clazz;
	}

	private static class RootHandler extends ModelElementHandler<Void> {

		private final LogReport report;
		private final HandlerPool pool;

		private List<CorpusManifest> corpora = new ArrayList<>();

		private Map<String, Object> cache = new HashMap<>();

		public RootHandler(LogReport report) {
			super("model"); //$NON-NLS-1$

			if (report == null)
				throw new NullPointerException("Invalid report"); //$NON-NLS-1$


			this.report = report;
			this.pool = new HandlerPool(report);
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#getReport()
		 */
		@Override
		public LogReport getReport() {
			return report;
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#getPool()
		 */
		@Override
		public HandlerPool getPool() {
			return pool;
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#registerTemplate(java.lang.String, java.lang.Object)
		 */
		@Override
		protected void registerTemplate(Template template) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#feedCharacters(char[], int, int)
		 */
		@Override
		void feedCharacters(char[] ch, int start, int length) {
			// do nothing
		}

		public void resetCache() {
			cache.clear();
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#lookup(java.lang.String, java.lang.Class)
		 */
		@Override
		public <T> T lookup(String id, Class<T> clazz) {
			if (id == null)
				throw new NullPointerException("Invalid id");  //$NON-NLS-1$

			Object cachedItem = cache.get(id);

			if(cachedItem==null) {
				getReport().error("No item for id available in scope: "+id); //$NON-NLS-1$
				return null;
			}

			if(!clazz.isAssignableFrom(cachedItem.getClass())) {
				getReport().error("Incompatible cached item '"+id+"'. Expected "+clazz); //$NON-NLS-1$ //$NON-NLS-2$
				return null;
			}

			return clazz.cast(cachedItem);
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#register(java.lang.String, java.lang.Object)
		 */
		@Override
		protected void register(String id, Object item) {
			if (id == null)
				throw new NullPointerException("Invalid id"); //$NON-NLS-1$
			if (item == null)
				throw new NullPointerException("Invalid item"); //$NON-NLS-1$

			if(cache.containsKey(id)) {
//				getReport().error("Duplicate item in scope for id: "+id); //$NON-NLS-1$
				//FIXME
			}

			cache.put(id, item);

			if(item instanceof Manifest
					&& ((Manifest)item).getManifestType()==ManifestType.CORPUS_MANIFEST) {
				corpora.add((CorpusManifest) item);
			}
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public ModelElementHandler<?> startElement(String uri,
				String localName, String qName, Attributes attributes)
				throws SAXException {

			switch (localName) {
			case "model": //$NON-NLS-1$
				break;

			case "templates": { //$NON-NLS-1$
				return new TemplateHandler();
			}

			case "corpus": { //$NON-NLS-1$
				resetCache();
				return getPool().getHandler(CorpusElementHandler.class);
			}

			default:
				super.startElement(uri, localName, qName, attributes);
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public ModelElementHandler<?> endElement(String uri, String localName,
				String qName) throws SAXException {

			switch (localName) {
			case "model": { //$NON-NLS-1$
				return null;
			}

			case "templates": //$NON-NLS-1$
				break;

			case "corpus": //$NON-NLS-1$

			default:
				super.endElement(uri, localName, qName);
			}

			return this;
		}

	}

	private static class TemplateHandler extends ModelElementHandler<Void> {

		private TemplateHandler() {
			super("templates"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#getParent()
		 */
		@Override
		public RootHandler getParent() {
			return (RootHandler) super.getParent();
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#registerTemplate(java.lang.String, java.lang.Object)
		 */
		@Override
		protected void registerTemplate(Template template) {
			if(!template.isTemplate()) {
				getReport().error("Cannot register a non-template as template: "+template); //$NON-NLS-1$
				return;
			}
			CorpusRegistry.getInstance().registerTemplate(template);
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#feedCharacters(char[], int, int)
		 */
		@Override
		void feedCharacters(char[] ch, int start, int length) {
			// do nothing
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public ModelElementHandler<?> startElement(String uri,
				String localName, String qName, Attributes attributes)
				throws SAXException {

			getParent().resetCache();

			if("templates".equals(localName)) { //$NON-NLS-1$
				return this;
			}

			@SuppressWarnings("rawtypes")
			Class<ModelElementHandler> clazz = defaultGetHandlerClass(localName);

			if(clazz!=null) {
				ModelElementHandler<?> handler = getPool().getHandler(clazz);
				handler.setTemplateMode(true);

				return handler;
			}

			return super.startElement(uri, localName, qName, attributes);
		}

		/**
		 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public ModelElementHandler<?> endElement(String uri, String localName,
				String qName) throws SAXException {

			if("templates".equals(localName)) { //$NON-NLS-1$
				return null;
			}

			@SuppressWarnings("rawtypes")
			Class<ModelElementHandler> clazz = defaultGetHandlerClass(localName);

			if(clazz!=null) {
				return this;
			}

			return super.endElement(uri, localName, qName);
		}
	}

	private class ModelHandler extends DefaultHandler implements EntityResolver2 {

		private final LogReport report;
		private final RootHandler rootHandler;

		private Stack<ModelElementHandler<?>> stack = new Stack<>();

		private final boolean allowCorpora;
		private boolean ignore = false;

		public ModelHandler(LogReport report, boolean allowCorpora) {
			if (report == null)
				throw new NullPointerException("Invalid report"); //$NON-NLS-1$

			this.report = report;
			this.allowCorpora = allowCorpora;

			rootHandler = new RootHandler(report);

			stack.push(rootHandler);
		}

		public List<CorpusManifest> getCorpora() {
			return rootHandler.corpora;
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			switch (localName) {
			case "model": //$NON-NLS-1$
				break;

			case "corpus": //$NON-NLS-1$
				if(!allowCorpora) {
					ignore = true;
				}

				//$FALL-THROUGH$
			default:
				if(!ignore) {
					startElement0(activeHandler(), uri, localName, qName, attributes);
				}
				break;
			}

		}

		private void startElement0(ModelElementHandler<?> handler, String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			System.out.printf("START tag=%s handler=%s\n",localName,handler.getClass().getSimpleName());

			ModelElementHandler<?> replacement = handler.startElement(uri, localName, qName, attributes);

			if(replacement!=null && replacement!=handler) {
				pushHandler(replacement, handler);

				startElement0(replacement, uri, localName, qName, attributes);
			}
		}

		private void pushHandler(ModelElementHandler<?> handler, ModelElementHandler<?> parent) {
			handler.setParent(parent);
			stack.push(handler);
		}

		private ModelElementHandler<?> activeHandler() {
			return stack.peek();
		}

		private void popHandler() {
			stack.pop();
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			switch (localName) {
			case "model": //$NON-NLS-1$
				break;

			case "corpus": //$NON-NLS-1$
				ignore = false;
				break;

			default:
				if(!ignore) {
					endElement0(activeHandler(), uri, localName, qName);
				}
				break;
			}

		}

		private void endElement0(ModelElementHandler<?> handler, String uri, String localName, String qName)
				throws SAXException {
			System.out.printf("END tag=%s handler=%s\n",localName,handler.getClass().getSimpleName());

			ModelElementHandler<?> replacement = handler.endElement(uri, localName, qName);

			if(replacement!=handler) {
				popHandler();

				if(replacement!=null) {
					pushHandler(replacement, activeHandler());
					endElement0(replacement, uri, localName, qName);
				}
			}
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			activeHandler().feedCharacters(ch, start, length);
		}

		@Override
		public void error(SAXParseException ex) throws SAXException {
			logException(Level.SEVERE, ex);
		}

		@Override
		public void warning(SAXParseException ex) throws SAXException {
			logException(Level.WARNING, ex);
		}

		@Override
		public void fatalError(SAXParseException ex) throws SAXException {
			logException(Level.SEVERE, ex);
		}

		private InputSource getDTD() {
			return new InputSource(ManifestParser.class.getResourceAsStream("corpus.dtd")); //$NON-NLS-1$
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String, java.lang.String)
		 */
		@Override
		public InputSource resolveEntity(String publicId, String systemId)
				throws IOException, SAXException {

			if("corpus.dtd".equals(systemId)) { //$NON-NLS-1$
				return getDTD();
			}

			return null;
		}

		private void logException(Level level, SAXParseException ex) {
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

			report.log(level, sb.toString());
		}

		/**
		 * @see org.xml.sax.ext.EntityResolver2#getExternalSubset(java.lang.String, java.lang.String)
		 */
		@Override
		public InputSource getExternalSubset(String name, String baseURI)
				throws SAXException, IOException {
			return getDTD();
		}

		/**
		 * @see org.xml.sax.ext.EntityResolver2#resolveEntity(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public InputSource resolveEntity(String name, String publicId,
				String baseURI, String systemId) throws SAXException,
				IOException {

			if("corpus.dtd".equals(systemId)) { //$NON-NLS-1$
				return getDTD();
			}

			return null;
		}
	}
}
