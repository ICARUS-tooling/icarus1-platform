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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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

import de.ims.icarus.eval.Expression;
import de.ims.icarus.logging.LogReport;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.model.api.ContainerType;
import de.ims.icarus.model.api.StructureType;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.Derivable;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.model.api.manifest.OptionsManifest;
import de.ims.icarus.model.io.LocationType;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.AbstractDerivable;
import de.ims.icarus.model.standard.manifest.AbstractLayerManifest;
import de.ims.icarus.model.standard.manifest.AbstractMemberManifest;
import de.ims.icarus.model.standard.manifest.AnnotationLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.AnnotationManifestImpl;
import de.ims.icarus.model.standard.manifest.ContainerManifestImpl;
import de.ims.icarus.model.standard.manifest.ContextManifestImpl;
import de.ims.icarus.model.standard.manifest.CorpusManifestImpl;
import de.ims.icarus.model.standard.manifest.LocationManifestImpl;
import de.ims.icarus.model.standard.manifest.MarkableLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.OptionsManifestImpl;
import de.ims.icarus.model.standard.manifest.PathResolverManifestImpl;
import de.ims.icarus.model.standard.manifest.StructureLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.StructureManifestImpl;
import de.ims.icarus.model.standard.manifest.ValueRangeImpl;
import de.ims.icarus.model.standard.manifest.ValueSetImpl;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestParser {

	private final Set<ParseSource> sources = new HashSet<>();

	public void addSource(URL url, ParseMode mode) {
		if (url == null)
			throw new NullPointerException("Invalid url");
		if (mode == null)
			throw new NullPointerException("Invalid mode");

		if(!sources.add(new ParseSource(mode, url)))
			throw new IllegalArgumentException("Source already registered: "+url);
	}

	public void parseAll() throws SAXException {
		XMLReader reader = newReader();


	}

	private XMLReader newReader() throws ParserConfigurationException, SAXException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setNamespaceAware(true);
		parserFactory.setValidating(true);

		try {
			parserFactory.setFeature("http://xml.org/sax/features/use-entity-resolver2", true); //$NON-NLS-1$
		} catch (SAXNotRecognizedException | SAXNotSupportedException
				| ParserConfigurationException e) {
			LoggerFactory.error(this, "Failed to activate advanced entity-resolver feature", e); //$NON-NLS-1$
		}


		SAXParser parser = parserFactory.newSAXParser();
		return parser.getXMLReader();
	}

	private interface Linker<O extends Object> {
		void link();
	}

	public LogReport loadCorpora(URL url) {

		LogReport report = new LogReport(this);
		ModelHandler handler = new ModelHandler(ParseMode.CORPORA, report);

		loadResource(url, handler);

		if(!report.hasErrorRecords()) {
			for(CorpusManifest manifest : handler.getCorpora()) {
				try {
					CorpusRegistry.getInstance().addCorpus(manifest);
				} catch(Exception e) {
					report.error("Failed to register corpus: "+manifest.getId(), e); //$NON-NLS-1$
				}
			}
		}

		return report;
	}

	public LogReport loadTemplates(URL url) {

		LogReport report = new LogReport(this);
		ModelHandler handler = new ModelHandler(ParseMode.TEMPLATES, report);

		loadResource(url, handler);

		if(!report.hasErrorRecords()) {
			for(Derivable template : handler.getTemplates()) {
				try {
					CorpusRegistry.getInstance().registerTemplate(template);
				} catch(Exception e) {
					report.error("Failed to register template: "+template.getId(), e); //$NON-NLS-1$
				}
			}
		}

		return report;
	}

	private void loadResource(URL url, ModelHandler handler) {
		if (url == null)
			throw new NullPointerException("Invalid url");  //$NON-NLS-1$
		if (handler == null)
			throw new NullPointerException("Invalid handler"); //$NON-NLS-1$

		try {
			XMLReader reader = newReader();

			reader.setEntityResolver(handler);
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.setDTDHandler(handler);

			reader.parse(new InputSource(url.openStream()));
		} catch (IOException e) {
			handler.error("Failed to load model data", e); //$NON-NLS-1$
		} catch (SAXException e) {
			handler.error("Parsing error while loading model data", e); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			handler.error("Unable to configure model parser", e); //$NON-NLS-1$
		}
	}

	public enum ParseMode {
		TEMPLATES,
		CORPORA;
	}

	private class ParseSource {

		private final ParseMode mode;
		private final URL url;

		ParseSource(ParseMode mode, URL url) {
			this.mode = mode;
			this.url = url;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return url.hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ParseSource) {
				return url.equals(((ParseSource)obj).url);
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "{"+mode.name()+": "+url.toExternalForm()+"}";
		}
	}

	public static class ModelHandler extends DefaultHandler implements EntityResolver2 {

		private final LogReport report;

		private final StringBuilder buffer = new StringBuilder();

		private final Stack<Object> stack = new Stack<>();

		private final Map<String, Derivable> templates = new LinkedHashMap<>();
		private final List<CorpusManifest> corpora = new ArrayList<>();

		private final ParseMode parseMode;
		private final Stack<String> ignores = new Stack<>();

		private String key;
		private ValueType valueType;
		private Expression expression;

		public ModelHandler() {
			report = new LogReport(this);
			this.parseMode = parseMode;
		}

		public ModelHandler(ParseMode parseMode, LogReport report) {
			if (parseMode == null)
				throw new NullPointerException("Invalid parseMode"); //$NON-NLS-1$
			if (report == null)
				throw new NullPointerException("Invalid report"); //$NON-NLS-1$

			this.report = report;
			this.parseMode = parseMode;
		}

		public Collection<CorpusManifest> getCorpora() {
			return CollectionUtils.getListProxy(corpora);
		}

		public Collection<Derivable> getTemplates() {
			return CollectionUtils.getCollectionProxy(templates.values());
		}

		public LogReport getReport() {
			return report;
		}

		private void checkId(String id) {
			if(!CorpusRegistry.isValidId(id)) {
				error("Invalid identifier: "+id); //$NON-NLS-1$
			}
		}

		private boolean isTemplateMode() {
			return parseMode==ParseMode.TEMPLATES;
		}

		private void push(Object item) {
			stack.push(item);
		}

		private Object peek() {
			return stack.isEmpty() ? null : stack.peek();
		}

		private <T extends Object> T pop(Class<T> clazz) {
			return clazz.cast(stack.pop());
		}

		private <T extends Object> T current(Class<T> clazz) {
			return clazz.cast(stack.peek());
		}

		private boolean isRoot() {
			return stack.isEmpty();
		}

		private String trace(String msg) {
			StringBuilder sb = new StringBuilder("<root>"); //$NON-NLS-1$

			for(int i=stack.size()-1; i>-1; i--) {
				String id = null;
				Object item = stack.get(i);

				if(item instanceof Derivable) {
					id = ((Derivable)item).getId();
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

		private void ignore(String localName) {
			ignores.push(localName);
		}

		private void unignore(String localName) {
			if(!ignores.isEmpty() && ignores.peek().equals(localName)) {
				ignores.pop();
			}
		}

		private boolean isIgnoring() {
			return !ignores.isEmpty();
		}

		private Derivable resolveTemplate(String id) {
			Derivable template = templates.get(id);
			if(template==null) {
				try {
					template = CorpusRegistry.getInstance().getTemplate(id);
				} catch(IllegalArgumentException e) {
					// no-op
				}
			}

			return template;
		}

		private void addTemplate(Derivable template) {
			if(templates.containsKey(template.getId())) {
//				System.out.println(trace("ignoring "+template)); //$NON-NLS-1$

				error("Duplicate template id: "+template.getId()); //$NON-NLS-1$
			} else {
//				System.out.println(trace("added "+template)); //$NON-NLS-1$
				templates.put(template.getId(), template);
			}
		}

		private void readLayerAttributes(Attributes attributes, AbstractLayerManifest<?> manifest) {
			readManifestAttributes(attributes, manifest);

			String indexable = normalize(attributes, "index"); //$NON-NLS-1$
			if(indexable!=null) {
				manifest.setIndexable(booleanValue(indexable));
			}

			String searchable = normalize(attributes, "search"); //$NON-NLS-1$
			if(searchable!=null) {
				manifest.setSearchable(booleanValue(searchable));
			}

			String baseLayer = normalize(attributes, "base-layer"); //$NON-NLS-1$
			String baseContext = normalize(attributes, "base-context"); //$NON-NLS-1$

			if(baseLayer==null) {
				return;
			}

			if(isTemplateMode()) {
				manifest.setBaseLayer(baseLayer);
				if(baseContext!=null) {
					warning("Base context declaration not supported in template mode"); //$NON-NLS-1$
				}
			} else {
				ContextManifest contextManifest = current(ContextManifest.class);
				CorpusManifest corpusManifest = contextManifest.getCorpusManifest();

				if(baseContext!=null) {
					try {
						contextManifest = corpusManifest.getContextManifest(baseContext);
					} catch(IllegalArgumentException e) {
						error("No such base context in scope: "+baseContext); //$NON-NLS-1$
					}
				}

				MarkableLayerManifest layerManifest = null;

				try {
					layerManifest = (MarkableLayerManifest) contextManifest.getLayerManifest(baseLayer);
				} catch(IllegalArgumentException e) {
					error("No such base layer in scope: "+baseLayer); //$NON-NLS-1$
				}

				if(layerManifest!=null) {
					manifest.setBaseLayerManifest(layerManifest);
				}
			}
		}

		private void readMarkableLayerAttributes(Attributes attributes, MarkableLayerManifestImpl manifest) {
			readLayerAttributes(attributes, manifest);

			String boundaryLayer = normalize(attributes, "boundary-layer"); //$NON-NLS-1$
			String boundaryContext = normalize(attributes, "boundary-context"); //$NON-NLS-1$

			if(boundaryLayer==null) {
				return;
			}

			if(isTemplateMode()) {
				manifest.setBoundaryLayer(boundaryLayer);
				if(boundaryContext!=null) {
					warning("Boundary context declaration not supported in template mode"); //$NON-NLS-1$
				}
			} else {
				ContextManifest contextManifest = current(ContextManifest.class);
				CorpusManifest corpusManifest = contextManifest.getCorpusManifest();

				if(boundaryContext!=null) {
					try {
						contextManifest = corpusManifest.getContextManifest(boundaryContext);
					} catch(IllegalArgumentException e) {
						error("No such boundary context in scope: "+boundaryContext); //$NON-NLS-1$
					}
				}

				MarkableLayerManifest layerManifest = null;

				try {
					layerManifest = (MarkableLayerManifest) contextManifest.getLayerManifest(boundaryLayer);
				} catch(IllegalArgumentException e) {
					error("No such boundary layer in scope: "+boundaryLayer); //$NON-NLS-1$
				}

				if(layerManifest!=null) {
					manifest.setBoundaryLayerManifest(layerManifest);
				}
			}
		}

		private void readManifestAttributes(Attributes attributes, AbstractMemberManifest<?> manifest) {
			readTemplateAttributes(attributes, manifest);

			String id = normalize(attributes, "id"); //$NON-NLS-1$
			String name = normalize(attributes, "name"); //$NON-NLS-1$
			String description = normalize(attributes, "description"); //$NON-NLS-1$
			String icon = normalize(attributes, "icon"); //$NON-NLS-1$

			if(id!=null) {
				manifest.setId(id);
			}
			if(name!=null) {
				manifest.setName(name);
			}
			if(description!=null) {
				manifest.setDescription(description);
			}
			if(icon!=null) {
				manifest.setIcon(new IconWrapper(icon));
			}
		}

		private void appendLayer(AbstractLayerManifest<?> layerManifest) {
			ContextManifestImpl contextManifest = current(ContextManifestImpl.class);

			layerManifest.setContextManifest(contextManifest);
			contextManifest.addLayerManifest(layerManifest);
		}

		private void appendContainer(ContainerManifestImpl containerManifest) {
			MarkableLayerManifestImpl layerManifest = current(MarkableLayerManifestImpl.class);

			int depth = layerManifest.getContainerDepth();
			if(depth>0) {
				ContainerManifestImpl lastContainer = (ContainerManifestImpl) layerManifest.getContainerManifest(depth-1);
				lastContainer.setElementManifest(containerManifest);
				containerManifest.setParentManifest(lastContainer);
			}

			containerManifest.setLayerManifest(layerManifest);
			layerManifest.addContainerManifest(containerManifest);
		}

		private void readTemplateAttributes(Attributes attributes, AbstractDerivable<?> derivable) {
			derivable.setIsTemplate(isTemplateMode());

			String templateId = normalize(attributes, "template-id"); //$NON-NLS-1$
			if(templateId==null) {
				return;
			}

			Derivable template = resolveTemplate(templateId);

			if(template==null) {
				error("Missing template: "+templateId); //$NON-NLS-1$
				return;
			}

			if(!template.isTemplate()) {
				error("Referenced object is not a valid template: "+templateId); //$NON-NLS-1$
				return;
			}

			try {
				derivable.setTemplate(template);
			} catch(ClassCastException e) {
				error("Incompatible template", e); //$NON-NLS-1$
			}
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if(isIgnoring()) {
				return;
			}

//			System.out.println(trace(" - START: "+localName)); //$NON-NLS-1$

			switch (localName) {
			case "corpora": //$NON-NLS-1$
				break;
			case "templates": //$NON-NLS-1$
				break;

			case "implementation": { //$NON-NLS-1$
				Implementation implementation = null;

				String extensionId = normalize(attributes, "extension-id"); //$NON-NLS-1$
				String pluginId = normalize(attributes, "plugin-id"); //$NON-NLS-1$
				String className = normalize(attributes, "class"); //$NON-NLS-1$

				if(extensionId!=null) {
					implementation = Implementations.foreignImplementation(extensionId);
				} else if(className!=null) {
					if(pluginId!=null) {
						implementation = Implementations.foreignImplementation(pluginId, className);
					} else {
						implementation = Implementations.fixedImplementation(className);
					}
				}

				if(implementation==null) {
					error("Empty implementation declaration in manifest"); //$NON-NLS-1$
				} else {
					current(AbstractMemberManifest.class).setImplementation(implementation);
				}
			} break;

			case "property": { //$NON-NLS-1$
				AbstractMemberManifest<?> manifest = current(AbstractMemberManifest.class);

				key = normalize(attributes, "key"); //$NON-NLS-1$
				valueType = typeValue(normalize(attributes, "type")); //$NON-NLS-1$

				if(valueType==null) {
					OptionsManifest optionsManifest = manifest.getOptionsManifest();
					if(optionsManifest!=null) {
						valueType = optionsManifest.getValueType(key);
					} else {
						error("Unable to resolve type of property '"+key+"': No options manifest defined"); //$NON-NLS-1$ //$NON-NLS-2$
						valueType = ValueType.STRING;
					}
				}

				String valueString = normalize(attributes, "value"); //$NON-NLS-1$
				if(valueString!=null) {
					Object value = value(valueString, valueType);

					manifest.setProperty(key, value);
				}
			} break;

			case "options": { //$NON-NLS-1$
				OptionsManifestImpl manifest = new OptionsManifestImpl();

				if(isRoot()) {
					addTemplate(manifest);
				} else {
					current(AbstractMemberManifest.class).setOptionsManifest(manifest);
				}

				push(manifest);
			} break;

			case "option": { //$NON-NLS-1$
				OptionsManifestImpl manifest = current(OptionsManifestImpl.class);

				key = normalize(attributes, "id"); //$NON-NLS-1$
				manifest.addOption(key);

				String name = normalize(attributes, "name"); //$NON-NLS-1$
				if(name!=null) {
					manifest.setName(key, name);
				}

				String description = normalize(attributes, "description"); //$NON-NLS-1$
				if(description!=null) {
					manifest.setDescription(key, description);
				}

				String type = normalize(attributes, "type"); //$NON-NLS-1$
				if(type!=null) {
					valueType = typeValue(type);
				}

				if(valueType==null) {
					valueType = ValueType.STRING;
				}

				manifest.setValueType(key, valueType);

				manifest.setPublished(key, boolValue(attributes, "published", true)); //$NON-NLS-1$
			} break;

			case "default-value": //$NON-NLS-1$
				break;

			case "range": { //$NON-NLS-1$
				boolean includeMin = boolValue(attributes, "include-min", true); //$NON-NLS-1$
				boolean includeMax = boolValue(attributes, "include-max", true); //$NON-NLS-1$
				ValueRangeImpl range = new ValueRangeImpl(includeMin, includeMax);
				Object owner = peek();
				if(owner instanceof OptionsManifestImpl) {
					OptionsManifestImpl manifest = (OptionsManifestImpl) owner;
					manifest.setSupportedRange(key, range);

					valueType = manifest.getValueType(key);
				} else {
					AnnotationManifestImpl manifest = (AnnotationManifestImpl) owner;
					manifest.setSupportedRange(range);

					valueType = manifest.getValueType();
				}
				push(range);
			} break;

			case "eval": //$NON-NLS-1$
				break;

			case "min": //$NON-NLS-1$
			case "max": //$NON-NLS-1$
				break;

			case "values": { //$NON-NLS-1$
				ValueSetImpl valueSet = new ValueSetImpl();

				readTemplateAttributes(attributes, valueSet);

				String id = normalize(attributes, "id"); //$NON-NLS-1$
				String name = normalize(attributes, "name"); //$NON-NLS-1$
				String description = normalize(attributes, "description"); //$NON-NLS-1$
				String icon = normalize(attributes, "icon"); //$NON-NLS-1$

				if(id!=null) {
					valueSet.setId(id);
				}
				if(name!=null) {
					valueSet.setName(name);
				}
				if(description!=null) {
					valueSet.setDescription(description);
				}
				if(icon!=null) {
					valueSet.setIcon(new IconWrapper(icon));
				}

				Object owner = peek();
				if(owner instanceof OptionsManifestImpl) {
					OptionsManifestImpl manifest = (OptionsManifestImpl) owner;
					manifest.setSupportedValues(key, valueSet);

					valueType = manifest.getValueType(key);
				} else if(owner instanceof AnnotationManifestImpl) {
					AnnotationManifestImpl manifest = (AnnotationManifestImpl) owner;
					manifest.setSupportedValues(valueSet);

					valueType = manifest.getValueType();
				} else {
					addTemplate(valueSet);
				}

				String type = normalize(attributes, "type"); //$NON-NLS-1$
				if(type!=null) {
					valueType = typeValue(type);
				}

				if(valueType==null) {
					valueType = ValueType.STRING;
				}

				push(valueSet);
			} break;

			case "value": //$NON-NLS-1$
				break;

			case "corpus": { //$NON-NLS-1$
				CorpusManifestImpl manifest = new CorpusManifestImpl();

				readManifestAttributes(attributes, manifest);

				String editable = normalize(attributes, "editable"); //$NON-NLS-1$
				if(editable!=null) {
					manifest.setEditable(booleanValue(editable));
				}

				if(isTemplateMode()) {
					addTemplate(manifest);
				}

				push(manifest);
			} break;

			case "context": { //$NON-NLS-1$
				ContextManifestImpl manifest = new ContextManifestImpl();

				readManifestAttributes(attributes, manifest);

				String independent = normalize(attributes, "independent"); //$NON-NLS-1$
				if(independent!=null) {
					manifest.setIndependent(booleanValue(independent));
				}

				if(isRoot()) {
					addTemplate(manifest);
				} else {
					CorpusManifestImpl corpusManifest = current(CorpusManifestImpl.class);
					corpusManifest.addCustomContextManifest(manifest);
					manifest.setCorpusManifest(corpusManifest);
				}

				push(manifest);
			} break;

			case "default-context": { //$NON-NLS-1$
				ContextManifestImpl manifest = new ContextManifestImpl();

				readManifestAttributes(attributes, manifest);

				if(isRoot()) {
					error("Cannot define default context as template"); //$NON-NLS-1$
				} else {
					CorpusManifestImpl corpusManifest = current(CorpusManifestImpl.class);
					corpusManifest.setDefaultContextManifest(manifest);;
					manifest.setCorpusManifest(corpusManifest);
				}

				push(manifest);
			} break;

			case "documentation": //$NON-NLS-1$
				break;

			case "location": { //$NON-NLS-1$
				LocationManifestImpl manifest = new LocationManifestImpl();

				String path = normalize(attributes, "path"); //$NON-NLS-1$
				if(path!=null) {
					manifest.setPath(path);
				}

				manifest.setType(LocationType.parseLocationType(
						normalize(attributes, "location-type"))); //$NON-NLS-1$

				current(ContextManifestImpl.class).setLocationManifest(manifest);
				push(manifest);
			} break;

			case "path": //$NON-NLS-1$
				break;

			case "path-resolver": { //$NON-NLS-1$
				PathResolverManifestImpl manifest = new PathResolverManifestImpl();

				readManifestAttributes(attributes, manifest);

				if(isRoot()) {
					addTemplate(manifest);
				} else {
					current(LocationManifestImpl.class).setPathResolverManifest(manifest);
				}

				push(manifest);
			} break;

			case "context-reader": { //$NON-NLS-1$
				ContextReaderManifestImpl manifest = new ContextReaderManifestImpl();

				readManifestAttributes(attributes, manifest);

				String format = normalize(attributes, "format"); //$NON-NLS-1$
				if(format!=null) {
					manifest.setFormatId(format);
				}

				if(isRoot()) {
					addTemplate(manifest);
				} else {
					current(ContextManifestImpl.class).setReaderManifest(manifest);
				}

				push(manifest);
			} break;

			case "context-writer": { //$NON-NLS-1$
				ContextWriterManifestImpl manifest = new ContextWriterManifestImpl();

				readManifestAttributes(attributes, manifest);

				String format = normalize(attributes, "format"); //$NON-NLS-1$
				if(format!=null) {
					manifest.setFormatId(format);
				}

				if(isRoot()) {
					addTemplate(manifest);
				} else {
					current(ContextManifestImpl.class).setWriterManifest(manifest);
				}

				push(manifest);
			} break;

			case "markable-layer": { //$NON-NLS-1$
				MarkableLayerManifestImpl manifest = new MarkableLayerManifestImpl();

				readMarkableLayerAttributes(attributes, manifest);

				if(isRoot()) {
					addTemplate(manifest);
				} else {
					appendLayer(manifest);
				}

				push(manifest);
			} break;

			case "structure-layer": { //$NON-NLS-1$
				StructureLayerManifestImpl manifest = new StructureLayerManifestImpl();

				readMarkableLayerAttributes(attributes, manifest);

				if(isRoot()) {
					addTemplate(manifest);
				} else {
					appendLayer(manifest);
				}

				push(manifest);
			} break;

			case "annotation-layer": { //$NON-NLS-1$
				AnnotationLayerManifestImpl manifest = new AnnotationLayerManifestImpl();

				readLayerAttributes(attributes, manifest);

				String deepAnnotation = normalize(attributes, "deep-annotation"); //$NON-NLS-1$
				if(deepAnnotation!=null) {
					manifest.setDeepAnnotation(booleanValue(deepAnnotation));
				}

				String unknownKeys = normalize(attributes, "unknown-keys"); //$NON-NLS-1$
				if(unknownKeys!=null) {
					manifest.setAllowUnknownKeys(booleanValue(unknownKeys));
				}

				if(isRoot()) {
					addTemplate(manifest);
				} else {
					appendLayer(manifest);
				}

				push(manifest);
			} break;

			case "annotation": { //$NON-NLS-1$
				AnnotationManifestImpl manifest = new AnnotationManifestImpl();

				readTemplateAttributes(attributes, manifest);

				manifest.setKey(normalize(attributes, "key")); //$NON-NLS-1$

				String name = normalize(attributes, "name"); //$NON-NLS-1$
				if(name!=null) {
					manifest.setName(name);
				}

				String description = normalize(attributes, "description"); //$NON-NLS-1$
				if(description!=null) {
					manifest.setDescription(description);
				}

				String type = normalize(attributes, "type"); //$NON-NLS-1$
				if(type!=null) {
					valueType = typeValue(type);
				}

				if(valueType==null) {
					valueType = ValueType.STRING;
				}

				manifest.setValueType(valueType);

				boolean isDefault = booleanValue(normalize(attributes, "default")); //$NON-NLS-1$

				if(isRoot()) {
					addTemplate(manifest);
				} else {
					AnnotationLayerManifestImpl layerManifest = current(AnnotationLayerManifestImpl.class);
					if(isDefault) {
						layerManifest.setDefaultAnnotationManifest(manifest);
					} else {
						layerManifest.addAnnotationManifest(manifest.getKey(), manifest);
					}
				}

				push(manifest);
			} break;

			case "alias": { //$NON-NLS-1$
				String alias = normalize(attributes, "name"); //$NON-NLS-1$
				if(alias!=null) {
					current(AnnotationManifestImpl.class).addAlias(alias);
				}
			} break;

			case "container": { //$NON-NLS-1$
				ContainerManifestImpl manifest = new ContainerManifestImpl();

				String containerType = normalize(attributes, "container-type"); //$NON-NLS-1$
				if(containerType!=null) {
					manifest.setContainerType(ContainerType.parseContainerType(containerType));
				}

				if(isRoot()) {
					addTemplate(manifest);
				} else {
					appendContainer(manifest);
				}

				push(manifest);
			} break;

			case "structure": { //$NON-NLS-1$
				StructureManifestImpl manifest = new StructureManifestImpl();

				String containerType = normalize(attributes, "container-type"); //$NON-NLS-1$
				if(containerType!=null) {
					manifest.setContainerType(ContainerType.parseContainerType(containerType));
				}

				String structureType = normalize(attributes, "structure-type"); //$NON-NLS-1$
				if(structureType!=null) {
					manifest.setStructureType(StructureType.parseStructureType(structureType));
				}

				if(isRoot()) {
					addTemplate(manifest);
				} else {
					appendContainer(manifest);
				}

				push(manifest);
			} break;

			case "prerequisites": //$NON-NLS-1$
				break;

			case "prerequisite": { //$NON-NLS-1$
				String layerId = normalize(attributes, "layer-id"); //$NON-NLS-1$
				String typeId = normalize(attributes, "type-id"); //$NON-NLS-1$
				String contextId = normalize(attributes, "context-id"); //$NON-NLS-1$
				String alias = normalize(attributes, "alias"); //$NON-NLS-1$

				PrerequisiteManifest prerequisite = new PrerequisiteImpl(layerId, typeId, contextId, alias);

				if(isTemplateMode()) {
					current(AbstractLayerManifest.class).addPrerequisite(prerequisite);
				} else {
					error("Unresolved prerequisite declarations not allowed in instance mode: "+prerequisite); //$NON-NLS-1$
				}
			} break;

			default:
				error("Unexpected start tag: "+localName); //$NON-NLS-1$
				break;
			}
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			boolean ignored = isIgnoring();
			unignore(localName);

			if(ignored) {
				return;
			}

//			System.out.println(trace(" - END: "+localName)); //$NON-NLS-1$

			switch (localName) {
			case "corpora": //$NON-NLS-1$
				break;
			case "templates": //$NON-NLS-1$
				break;

			case "implementation": //$NON-NLS-1$
				break;

			case "property": { //$NON-NLS-1$
				String text = getText();

				if(text!=null) {
					Object value = value(text, valueType);
					current(AbstractMemberManifest.class).setProperty(key, value);
				}
				key = null;
				valueType = null;
			} break;

			case "options": { //$NON-NLS-1$
				pop(OptionsManifestImpl.class);
			} break;

			case "option": //$NON-NLS-1$
				valueType = null;
				break;

			case "default-value":  { //$NON-NLS-1$
				OptionsManifestImpl manifest = current(OptionsManifestImpl.class);
				ValueType valueType = manifest.getValueType(key);

				Object value = expression;

				if(value==null) {
					value = value(getText(), valueType);
				}

				manifest.setDefaultValue(key, value);
				expression = null;
			} break;

			case "range": { //$NON-NLS-1$
				pop(ValueRangeImpl.class);
			} break;

			case "eval": { //$NON-NLS-1$
				String text = getText();
				if(text!=null) {
					try {
						expression = Expression.compile(text);
					} catch (UnsupportedFormatException e) {
						error("Unable to compile expression: "+text); //$NON-NLS-1$
					}
				} else {
					error("Empty expression"); //$NON-NLS-1$
				}
			} break;

			case "min": { //$NON-NLS-1$
				ValueRangeImpl range = current(ValueRangeImpl.class);

				Object value = expression;
				if(value==null) {
					value = value(getText(), valueType);
				}

				range.setLowerBound(value);
				expression = null;
			} break;

			case "max": { //$NON-NLS-1$
				ValueRangeImpl range = current(ValueRangeImpl.class);

				Object value = expression;
				if(value==null) {
					value = value(getText(), valueType);
				}

				range.setLowerBound(value);
				expression = null;
			} break;

			case "values": { //$NON-NLS-1$
				pop(ValueSetImpl.class);
			} break;

			case "value": { //$NON-NLS-1$
				ValueSetImpl valueSet = current(ValueSetImpl.class);

				Object value = expression;
				if(value==null) {
					String text = getText();
					if(text!=null) {
						value = value(text, valueType);
					} else {
						error("Empty value declaration"); //$NON-NLS-1$
						break;
					}
				}

				valueSet.addValue(value);
				expression = null;
			} break;

			case "corpus": { //$NON-NLS-1$
				CorpusManifestImpl manifest = pop(CorpusManifestImpl.class);

				corpora.add(manifest);
			} break;

			case "context": { //$NON-NLS-1$
				pop(ContextManifestImpl.class);
			} break;

			case "default-context": { //$NON-NLS-1$
				pop(ContextManifestImpl.class);
			} break;

			case "documentation": //$NON-NLS-1$
				break;

			case "location": { //$NON-NLS-1$
				LocationManifestImpl manifest = pop(LocationManifestImpl.class);
				if(manifest.getPath()==null) {
					error("Missing path for location"); //$NON-NLS-1$
				}
			} break;

			case "path": { //$NON-NLS-1$
				String path = getText();
				if(path!=null) {
					current(LocationManifestImpl.class).setPath(path);
				}
			} break;

			case "path-resolver": { //$NON-NLS-1$
				pop(PathResolverManifestImpl.class);
			} break;

			case "context-reader": { //$NON-NLS-1$
				pop(ContextReaderManifestImpl.class);
			} break;

			case "context-writer": { //$NON-NLS-1$
				pop(ContextWriterManifestImpl.class);
			} break;

			case "markable-layer": { //$NON-NLS-1$
				pop(MarkableLayerManifestImpl.class);
			} break;

			case "structure-layer": { //$NON-NLS-1$
				pop(StructureLayerManifestImpl.class);
			} break;

			case "annotation-layer": { //$NON-NLS-1$
				pop(AnnotationLayerManifestImpl.class);
			} break;

			case "annotation": { //$NON-NLS-1$
				pop(AnnotationManifestImpl.class);
				valueType = null;
			} break;

			case "alias": { //$NON-NLS-1$
				String alias = getText();
				if(alias!=null && !alias.isEmpty()) {
					current(AnnotationManifestImpl.class).addAlias(alias);
				}
			} break;

			case "container": { //$NON-NLS-1$
				pop(ContainerManifestImpl.class);
			} break;

			case "structure": { //$NON-NLS-1$
				pop(StructureManifestImpl.class);
			} break;

			case "prerequisites": //$NON-NLS-1$
				break;

			case "prerequisite": //$NON-NLS-1$
				break;

			default:
				error("Unexpected end tag: "+localName); //$NON-NLS-1$
				break;
			}

		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			buffer.append(ch, start, length);
		}

		@Override
		public void error(SAXParseException ex) throws SAXException {
			error(logMsg(ex));
		}

		@Override
		public void warning(SAXParseException ex) throws SAXException {
			warning(logMsg(ex));
		}

		@Override
		public void fatalError(SAXParseException ex) throws SAXException {
			error(logMsg(ex));
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

		protected String getText() {
			String text = buffer.length()==0 ? null : buffer.toString();
			buffer.setLength(0);

			return text;
		}
	}

	public static String stringValue(Attributes attr, String key) {
		return normalize(attr, key);
	}

	public static long longValue(String s) {
		return Long.parseLong(s);
	}

	public static long longValue(Attributes attr, String key) {
		return longValue(normalize(attr, key));
	}

	public static double doubleValue(String s) {
		return Double.parseDouble(s);
	}

	public static double doubleValue(Attributes attr, String key) {
		return doubleValue(normalize(attr, key));
	}

	public static float floatValue(String s) {
		return Float.parseFloat(s);
	}

	public static float floatValue(Attributes attr, String key) {
		return floatValue(normalize(attr, key));
	}

	public static int intValue(String s) {
		return Integer.parseInt(s);
	}

	public static int intValue(Attributes attr, String key) {
		return intValue(normalize(attr, key));
	}

	public static boolean booleanValue(String s) {
		return s!=null && ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean booleanValue(Attributes attr, String key) {
		return booleanValue(normalize(attr, key));
	}

	public static boolean bitValue(String s) {
		return "1".equals(s); //$NON-NLS-1$
	}

	public static boolean bitValue(Attributes attr, String key) {
		return bitValue(normalize(attr, key));
	}

	public static ValueType typeValue(Attributes attr) {
		String s = normalize(attr, "type"); //$NON-NLS-1$
		return typeValue(s);
	}

	public static ValueType typeValue(String s) {
		return s==null ? null : ValueType.parseValueType(s);
	}

	public static Object value(Attributes attr, String key, ValueType valueType) {
		return value(normalize(attr, key), valueType);
	}

	public static Object value(String s, ValueType valueType) {
		switch (valueType) {
		case BOOLEAN:
			return booleanValue(s);
		case INTEGER:
			return intValue(s);
		case DOUBLE:
			return doubleValue(s);
		case STRING:
			return s;

		default:
			throw new IllegalArgumentException("Cannot parse custom or default type: "+s); //$NON-NLS-1$
		}
	}

	public static boolean boolValue(Attributes attr, String key, boolean defaultValue) {
		String s = normalize(attr, key);
		return s==null ? defaultValue : booleanValue(s);
	}

	public static String normalize(Attributes attr, String name) {
		String value = attr.getValue(name);
		if(value.isEmpty()) {
			value = null;
		}
		return value;
	}
}
