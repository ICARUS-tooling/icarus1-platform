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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Icon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.java.plugin.registry.PluginDescriptor;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.ims.icarus.eval.Expression;
import de.ims.icarus.eval.ExpressionFactory;
import de.ims.icarus.logging.LogReport;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.Derivable;
import de.ims.icarus.model.api.manifest.Documentation;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.ManifestSource;
import de.ims.icarus.model.api.manifest.MemberManifest;
import de.ims.icarus.model.api.manifest.ModifiableIdentity;
import de.ims.icarus.model.api.manifest.ModifiableManifest;
import de.ims.icarus.model.api.manifest.OptionsManifest;
import de.ims.icarus.model.api.manifest.ValueRange;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.DocumentationImpl;
import de.ims.icarus.model.standard.manifest.DocumentationImpl.ResourceImpl;
import de.ims.icarus.model.standard.manifest.OptionsManifestImpl;
import de.ims.icarus.model.standard.manifest.ValueManifestImpl;
import de.ims.icarus.model.standard.manifest.ValueRangeImpl;
import de.ims.icarus.model.standard.manifest.ValueSetImpl;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.model.xml.ModelXmlAttributes;
import de.ims.icarus.model.xml.ModelXmlTags;
import de.ims.icarus.model.xml.sax.ManifestXmlReader.Buffers.OptionBuffer;
import de.ims.icarus.model.xml.sax.ManifestXmlReader.Buffers.PrerequisiteBuffer;
import de.ims.icarus.model.xml.sax.ManifestXmlReader.Buffers.PropertyBuffer;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.util.id.StaticIdentity;

/**
 * Important constraints:
 * <ul>
 * <li>Templates will inherit all data unchanged from their ancestor template if they declare one</li>
 * <li>Templates will overwrite all data they explicitly declare</li>
 * <li>Only top-level manifests in a &lt;templates&gt; context are considered templates</li>
 * <li>A live corpus will clone <b>all</b> data from its inherited templates and re-link them
 * to the new instances</li>
 * <li>A template must be completely loaded and fully resolved before it can be used for further inheritance</li>
 * </ul>
 *
 * Reading is done in 4 steps:
 * <ol>
 * <li>Parsing all template sources into intermediate builder states</li>
 * <li>Creating from every top-level builder a new template object (this is done recursively to ensure that
 * referenced templates get fully resolved before being further processed)</li>
 * <li>Parsing all live corpora into intermediate builder states</li>
 * <li>Creating fully cloned manifest instances for each corpus, preserving template informations</li>
 * </ol>
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestXmlReader implements ModelXmlTags, ModelXmlAttributes {

	private final Set<ManifestSource> templateSources = new HashSet<>();
	private final Set<ManifestSource> corpusSources = new HashSet<>();
	private final AtomicBoolean reading = new AtomicBoolean(false);

	private final CorpusRegistry registry;

	private final Map<String, TemplateState> templateStates = new HashMap<>();

	private ParseState state;

	public ManifestXmlReader(CorpusRegistry registry) {
		if (registry == null)
			throw new NullPointerException("Invalid registry"); //$NON-NLS-1$

		this.registry = registry;
	}

	public void addSource(ManifestSource source) {
		if (source == null)
			throw new NullPointerException("Invalid source");  //$NON-NLS-1$

		if(reading.get())
			throw new IllegalStateException("Reading in progress, cannot add new sources"); //$NON-NLS-1$

		Set<ManifestSource> sources = source.isTemplate() ? templateSources : corpusSources;

		if(!sources.add(source))
			throw new IllegalArgumentException("Source already registered: "+source.getUrl()); //$NON-NLS-1$
	}

	public void readAll() throws ModelException, IOException, SAXException {

		if(!reading.compareAndSet(false, true))
			throw new IllegalStateException("Reading already in progress"); //$NON-NLS-1$

		XMLReader reader = newReader();

		// Read in templates
		for(ManifestSource source : templateSources) {
			try (InputStream in = source.getUrl().openStream()) {
				RootHandler handler = new RootHandler(source);

				reader.setContentHandler(handler);
				reader.setErrorHandler(handler);
				reader.setEntityResolver(handler);
				reader.setDTDHandler(handler);

				reader.parse(new InputSource(in));
			}
		}

		// Resolve all templates
		for(String templateId : templateStates.keySet()) {
			readTemplate(templateId);
		}

		// Register all templates once resolution process is done
		for(TemplateState state : templateStates.values()) {

			// Make sure our templates are marked as such!
			state.template.setIsTemplate(true);

			// Register fully resolved template to the registry
			registry.registerTemplate(state.template);
		}

		// Read in live corpora
		List<Builder<?>> topLevelBuilders = new ArrayList<>();
		for(ManifestSource source : templateSources) {
			try (InputStream in = source.getUrl().openStream()) {
				RootHandler handler = new RootHandler(source);

				reader.setContentHandler(handler);
				reader.setErrorHandler(handler);
				reader.setEntityResolver(handler);
				reader.setDTDHandler(handler);

				reader.parse(new InputSource(in));

				topLevelBuilders.addAll(handler.getTopLevelBuilders());
			}
		}

		// Now build and register all live corpora
		for(Builder<?> builder : topLevelBuilders) {
			CorpusManifest corpusManifest = (CorpusManifest) builder.build();

			registry.addCorpus(corpusManifest);
		}
	}

	private XMLReader newReader() throws SAXException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();

		parserFactory.setNamespaceAware(true);
		parserFactory.setValidating(true);

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

	/**
	 * Attempts to read an element containing a top level template
	 */
	private Derivable readTemplate(String id) {
		if(id==null) {
			return null;
		}

		TemplateState state = templateStates.get(id);

		if(state==null)
			throw new ModelException(ModelError.MANIFEST_UNKNOWN_ID, "No template definition found for id: "+id); //$NON-NLS-1$

		if(state.building)
			throw new ModelException(ModelError.MANIFEST_CYCLIC_TEMPLATE, "Template id is part of a cyclic dependency: "+id); //$NON-NLS-1$

		// Create new template if required
		if(state.template!=null) {
			state.building = true;

			state.template = (Derivable) state.builder.build();

			state.building = false;
		}

		return state.template;
	}

	/**
	 * Performs a prioritized template lookup in that it first checks the
	 * {@code CorpusRegistry} for a template of the given {@code id} and only if
	 * that fails will the method attempt to load a template from the current
	 * parser cache.
	 */
	private Derivable lookupTemplate(String id) {
		if(registry.hasTemplate(id)) {
			return registry.getTemplate(id);
		} else {
			return readTemplate(id);
		}
	}

	private void addTemplateState(String id, Builder<?> builder, ManifestSource manifestSource) {
		if(templateStates.containsKey(id) || registry.hasTemplate(id))
			throw new ModelException(ModelError.MANIFEST_DUPLICATE_ID, "Template id already in use: "+id); //$NON-NLS-1$

		templateStates.put(id, new TemplateState(builder, manifestSource));
	}

	static class TemplateState {
		// Builder responsible for the template
		final Builder<?> builder;
		// The physical source of the template
		final ManifestSource manifestSource;
		// FLag for cyclic dependency checks
		boolean building;
		// Constructed template
		Derivable template;

		public TemplateState(Builder<?> builder, ManifestSource manifestSource) {
			this.builder = builder;
			this.manifestSource = manifestSource;
		}
	}

	@SuppressWarnings("unused")
	private class ParseState {

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
	}

	private class RootHandler extends DefaultHandler {

		private final StringBuilder buffer = new StringBuilder();

		private final Stack<Builder<?>> builders = new Stack<>();

		// List of all top-level builders. Used to preserve order of appearance
		private final List<Builder<?>> topLevelBuilders = new ArrayList<>();

		private final ManifestSource manifestSource;

		RootHandler(ManifestSource manifestSource) {
			this.manifestSource = manifestSource;
		}

		/**
		 * @return the topLevelBuilders
		 */
		public List<Builder<?>> getTopLevelBuilders() {
			return topLevelBuilders;
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			buffer.append(ch, start, length);
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if(builders.isEmpty()) {
				Builder<?> root;
				if(manifestSource.isTemplate()) {
					root = new TemplatesBuilder();
				} else {
					root = new CorporaBuilder();
				}

				builders.add(root);
			}

			Builder<?> current = builders.peek();

			Builder<?> future = current.startElement(manifestSource, uri, localName, qName, attributes);

			// Delegate initial element handling to next builder
			if(future!=null && future!=current) {
				builders.push(future);
				state.push(future);

				future.startElement(manifestSource, uri, localName, qName, attributes);
			}
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			String text = getText();

			Builder<?> current = builders.peek();
			Builder<?> future = current.endElement(manifestSource, uri, localName, qName, text);

			// Discard current builder and switch to ancestor
			if(future==null) {
				builders.pop();
				state.pop();

				// If we are at the <templates> level again the builder was considered top-level
				if(builders.size()==1) {
					topLevelBuilders.add(current);
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

			return text;
		}
	}

	static class ParseUtils {

		public static void readIdentity(Attributes attr, ModifiableIdentity identity) {
			identity.setId(normalize(attr, ATTR_ID));
			identity.setName(normalize(attr, ATTR_NAME));
			identity.setDescription(normalize(attr, ATTR_DESCRIPTION));
			identity.setIcon(iconValue(attr, ATTR_ICON));
		}

		public static Icon iconValue(Attributes attr, String key) {
			String icon = normalize(attr, key);
			return icon==null ? null : new IconWrapper(icon);
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

		public static boolean booleanValue(Attributes attr, String key, boolean defaultValue) {
			String s = normalize(attr, key);
			return s==null ? defaultValue : booleanValue(s);
		}

		public static ValueType typeValue(Attributes attr) {
			String s = normalize(attr, ATTR_TYPE);
			return typeValue(s);
		}

		public static ValueType typeValue(String s) {
			return s==null ? null : ValueType.parseValueType(s);
		}

		public static Boolean boolValue(Attributes attr, String key) {
			String s = normalize(attr, key);
			return s==null ? null : booleanValue(s);
		}

		public static String normalize(Attributes attr, String name) {
			String value = attr.getValue(name);
			if(value.isEmpty()) {
				value = null;
			}
			return value;
		}
	}

	static class Buffers {
		static class OptionBuffer {
			String name, id, description, group;
			ValueType type;
			ValueSet valueSet;
			ValueRange valueRange;
			Object defaultValue;
			boolean published = true, multiValue = false;
		}

		static class PropertyBuffer {
			String name;
			ValueType type = ValueType.STRING;
		}

		static class PrerequisiteBuffer {
			String layerId, contextId, layerType, alias;
		}
	}

	abstract class Builder<O extends Object> {

		abstract O build();

		abstract Builder<?> startElement(ManifestSource manifestSource, String uri, String localName, String qName,
				Attributes attributes) throws SAXException;

		abstract Builder<?> endElement(ManifestSource manifestSource, String uri, String localName, String qName, String text)
				throws SAXException;

		<B extends Builder<?>> B add(Collection<B> buffer, B builder) {
			buffer.add(builder);

			return builder;
		}
	}

	class TemplatesBuilder extends Builder<Object> {

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#build()
		 */
		@Override
		Object build() {
			return null;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		Builder<?> startElement(ManifestSource manifestSource, String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			Builder<?> builder = null;
			// TODO Auto-generated method stub


			addTemplateState(attributes.getValue(ATTR_ID), builder, manifestSource);

			return builder;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#endElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		Builder<?> endElement(ManifestSource manifestSource, String uri, String localName, String qName,
				String text) throws SAXException {
			return null;
		}

	}

	class CorporaBuilder extends Builder<Object> {

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#build()
		 */
		@Override
		Object build() {
			return null;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		Builder<?> startElement(ManifestSource manifestSource, String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			switch (qName) {
			case TAG_CORPUS:
				return new CorpusBuilder();

			default:
				throw new SAXException("Only 'corpus' elements allowed on top level in live corpus file: "+qName); //$NON-NLS-1$
			}
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#endElement(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		Builder<?> endElement(ManifestSource manifestSource, String uri, String localName, String qName,
				String text) throws SAXException {
			switch (qName) {
			case TAG_CORPUS:
				return null;

			default:
				throw new SAXException("Only 'corpus' elements allowed on top level in live corpus file: "+qName); //$NON-NLS-1$
			}
		}

	}

	abstract class DerivableBuilder<D extends Derivable> extends Builder<D> {
		private String templateId;
		private String id;

		void readAttributes(Attributes attributes) {
			setId(ParseUtils.normalize(attributes, ATTR_ID));
			setTemplateId(ParseUtils.normalize(attributes, ATTR_TEMPLATE_ID));
		}

		String getTemplateId() {
			return templateId;
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @param templateId the templateId to set
		 */
		public void setTemplateId(String templateId) {
			this.templateId = templateId;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}

		boolean hasTemplate() {
			return templateId!=null;
		}

		void init(D instance) {
			instance.setId(id);

			if(templateId!=null) {
				Derivable template = lookupTemplate(templateId);

				instance.setTemplate(template);
			}
		}
	}

	class EvalBuilder extends Builder<Expression> {
		private final ExpressionFactory factory = new ExpressionFactory();

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#build()
		 */
		@Override
		Expression build() {
			return factory.build();
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#startElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@SuppressWarnings("resource")
		@Override
		Builder<?> startElement(ManifestSource manifestSource, String uri,
				String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (qName) {
			case TAG_EVAL: {
				// no-op
			} break;

			case TAG_VARIABLE: {
				String name = ParseUtils.normalize(attributes, ATTR_NAME);
				String namespace = ParseUtils.normalize(attributes, ATTR_NAMESPACE);
				String pluginId = ParseUtils.normalize(attributes, ATTR_PLUGIN_ID);

				Class<?> clazz;

				ClassLoader classLoader = getClass().getClassLoader();
				if(pluginId!=null) {
					PluginDescriptor descriptor = PluginUtil.getPluginRegistry().getPluginDescriptor(pluginId);
					classLoader = PluginUtil.getPluginManager().getPluginClassLoader(descriptor);
				}

				try {
					clazz = classLoader.loadClass(namespace);
				} catch (ClassNotFoundException e) {
					throw new SAXException("Unable to laod namespace class for variable: "+name, e); //$NON-NLS-1$
				}

				factory.addVariable(name, clazz);
			} break;

			case TAG_CODE: {
				// no-op
			} break;

			default:
				throw new SAXException("Unexpected tag in eval scope: "+qName); //$NON-NLS-1$
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#endElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		Builder<?> endElement(ManifestSource manifestSource, String uri,
				String localName, String qName, String text)
				throws SAXException {
			switch (qName) {
			case TAG_EVAL: {
				return null;
			}

			case TAG_VARIABLE: {
				// no-op
			} break;

			case TAG_CODE: {
				factory.setCode(text);
			} break;

			default:
				throw new SAXException("Unexpected tag in eval scope: "+qName); //$NON-NLS-1$
			}

			return this;
		}
	}

	class ValueSetBuilder extends Builder<ValueSet> {

		private final ValueSetImpl valueSet;

		private String name, description;

		ValueSetBuilder(ValueType type) {
			valueSet = new ValueSetImpl();
			valueSet.setValueType(type);
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#build()
		 */
		@Override
		ValueSet build() {
			return valueSet;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#startElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		Builder<?> startElement(ManifestSource manifestSource, String uri,
				String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (qName) {
			case TAG_VALUES: {
				// no-op
			} break;

			case TAG_VALUE: {
				name = ParseUtils.normalize(attributes, ATTR_NAME);
				description = ParseUtils.normalize(attributes, ATTR_DESCRIPTION);
			} break;

			default:
				throw new SAXException("Unexpected tag in value-set scope: "+qName); //$NON-NLS-1$
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#endElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		Builder<?> endElement(ManifestSource manifestSource, String uri,
				String localName, String qName, String text)
				throws SAXException {
			switch (qName) {
			case TAG_VALUES: {
				return null;
			}

			case TAG_VALUE: {
				Object value = valueSet.getValueType().parse(text, manifestSource.getClassLoader());
				if(name!=null) {
					ValueManifestImpl manifst = new ValueManifestImpl();
					manifst.setDescription(description);
					manifst.setName(name);
					manifst.setValue(value);

					value = manifst;
				}

				valueSet.addValue(value);
			} break;

			default:
				throw new SAXException("Unexpected tag in value-set scope: "+qName); //$NON-NLS-1$
			}

			return this;
		}

	}

	class ValueRangeBuilder extends Builder<ValueRange> {

		private final ValueType type;
		private boolean includeLower = true, includeUpper = true;
		private Object lower, upper;

		private EvalBuilder evalBuilder;

		ValueRangeBuilder(ValueType type) {
			this.type = type;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#build()
		 */
		@Override
		ValueRange build() {
			return new ValueRangeImpl(lower, upper, includeLower, includeUpper);
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#startElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		Builder<?> startElement(ManifestSource manifestSource, String uri,
				String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (qName) {
			case TAG_RANGE: {
				// no-op
			} break;

			case TAG_MIN: {
				// no-op
			} break;

			case TAG_MAX: {
				// no-op
			} break;

			case TAG_EVAL: {
				return (evalBuilder = new EvalBuilder());
			}

			default:
				throw new SAXException("Unexpected tag in value-range scope: "+qName); //$NON-NLS-1$
			}

			return this;
		}

		private Object getValue(String text, ManifestSource manifestSource) {
			Object value;
			if(evalBuilder!=null) {
				value = evalBuilder.build();
				evalBuilder = null;
			} else {
				value = type.parse(text, manifestSource.getClassLoader());
			}

			return value;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#endElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		Builder<?> endElement(ManifestSource manifestSource, String uri,
				String localName, String qName, String text)
				throws SAXException {
			switch (qName) {
			case TAG_RANGE: {
				return null;
			}

			case TAG_MIN: {
				lower = getValue(text, manifestSource);
			} break;

			case TAG_MAX: {
				upper = getValue(text, manifestSource);
			} break;

			case TAG_EVAL: {
				// no-op
			} break;

			default:
				throw new SAXException("Unexpected tag in value-range scope: "+qName); //$NON-NLS-1$
			}

			return this;
		}
	}

	class OptionsBuilder extends DerivableBuilder<OptionsManifest> {

		private List<OptionBuffer> options = new ArrayList<>();
		private List<Identity> groups = new ArrayList<>();

		private ValueRangeBuilder rangeBuilder;
		private ValueSetBuilder setBuilder;

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#build()
		 */
		@Override
		OptionsManifest build() {
			OptionsManifestImpl optionsManifest = new OptionsManifestImpl();

			init(optionsManifest);

			for(Identity group : groups) {
				optionsManifest.addGroupIdentifier(group);
			}

			Set<String> names = optionsManifest.getOptionNames();

			for(OptionBuffer option : options) {
				String key = option.id;

				if(names.contains(key)) {
					optionsManifest.removeOption(key);
				}

				optionsManifest.addOption(key);
				optionsManifest.setValueType(key, option.type);
				optionsManifest.setName(key, option.name);
				optionsManifest.setDescription(key, option.description);
				optionsManifest.setOptionGroup(key, option.group);
				optionsManifest.setSupportedValues(key, option.valueSet);
				optionsManifest.setSupportedRange(key, option.valueRange);
				optionsManifest.setMultiValue(key, option.multiValue);
				optionsManifest.setPublished(key, option.published);
				optionsManifest.setDefaultValue(key, option.defaultValue);
			}

			return optionsManifest;
		}

		private OptionBuffer current() {
			return options.get(options.size()-1);
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#startElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		Builder<?> startElement(ManifestSource manifestSource, String uri,
				String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (qName) {
			case TAG_OPTIONS: {
				readAttributes(attributes);
			} break;

			case TAG_OPTION: {
				OptionBuffer option = new OptionBuffer();
				option.id = ParseUtils.normalize(attributes, ATTR_ID);
				option.name = ParseUtils.normalize(attributes, ATTR_NAME);
				option.description = ParseUtils.normalize(attributes, ATTR_DESCRIPTION);
				option.group = ParseUtils.normalize(attributes, ATTR_GROUP);
				String type = ParseUtils.normalize(attributes, ATTR_TYPE);
				if(type!=null) {
					option.type = ValueType.parseValueType(type);
				}
				option.published = ParseUtils.booleanValue(attributes, ATTR_PUBLISHED, true);
				option.multiValue = ParseUtils.booleanValue(attributes, ATTR_MULTI_VALUE, false);

				options.add(option);
			} break;

			case TAG_GROUP: {
				String id = ParseUtils.normalize(attributes, ATTR_ID);
				String name = ParseUtils.normalize(attributes, ATTR_NAME);
				String description = ParseUtils.normalize(attributes, ATTR_DESCRIPTION);
				Icon icon = ParseUtils.iconValue(attributes, ATTR_ICON);

				StaticIdentity identity = new StaticIdentity(id);
				identity.setName(name);
				identity.setDescription(description);
				identity.setIcon(icon);

				groups.add(identity);
			} break;

			case TAG_RANGE: {
				return (rangeBuilder = new ValueRangeBuilder(current().type));
			}

			case TAG_VALUES: {
				return (setBuilder = new ValueSetBuilder(current().type));
			}

			case TAG_DEFAULT_VALUE: {
				// no-op
			} break;

			default:
				throw new SAXException("Unexpected tag in options scope: "+qName); //$NON-NLS-1$
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#endElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		Builder<?> endElement(ManifestSource manifestSource, String uri,
				String localName, String qName, String text)
				throws SAXException {
			switch (qName) {
			case TAG_OPTIONS:
				return null;

			case TAG_OPTION: {
				// no-op
			} break;

			case TAG_GROUP: {
				// no-op
			} break;

			case TAG_VALUES: {
				current().valueSet = setBuilder.build();
				setBuilder = null;
			} break;

			case TAG_RANGE: {
				current().valueRange = rangeBuilder.build();
				rangeBuilder = null;
			} break;

			case TAG_DEFAULT_VALUE: {
				OptionBuffer option = current();

				option.defaultValue = option.type.parse(text, manifestSource.getClassLoader());
			} break;

			default:
				throw new SAXException("Unexpected tag in options scope: "+qName); //$NON-NLS-1$
			}

			return this;
		}

	}

	class DocumentationBuilder extends Builder<Documentation> {

		private final DocumentationImpl documentation = new DocumentationImpl();

		private ResourceImpl resource;

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#build()
		 */
		@Override
		Documentation build() {
			return documentation;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#startElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		Builder<?> startElement(ManifestSource manifestSource, String uri,
				String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (qName) {
			case TAG_DOCUMENTATION: {
				ParseUtils.readIdentity(attributes, documentation);
			} break;

			case TAG_CONTENT: {
				// no-op
			} break;

			case TAG_RESOURCE: {
				resource = new ResourceImpl();

				ParseUtils.readIdentity(attributes, resource);
			} break;

			default:
				throw new SAXException("Unexpected tag in documentation scope: "+qName); //$NON-NLS-1$
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#endElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		Builder<?> endElement(ManifestSource manifestSource, String uri,
				String localName, String qName, String text)
				throws SAXException {
			switch (qName) {
			case TAG_DOCUMENTATION:
				return null;

			case TAG_CONTENT: {
				documentation.setContent(text);
			} break;

			case TAG_RESOURCE: {
				try {
					resource.setURL(new URL(text));
				} catch (MalformedURLException e) {
					throw new SAXException("Invalid url for resource", e); //$NON-NLS-1$
				}

				documentation.addResource(resource);
			} break;

			default:
				throw new SAXException("Unexpected tag in documentation scope: "+qName); //$NON-NLS-1$
			}

			return this;
		}

	}

	abstract class ModifiableBuilder<M extends ModifiableManifest> extends DerivableBuilder<M> {

		private Map<String, Object> properties;
		private OptionsBuilder optionsBuilder;
		private DocumentationBuilder documentationBuilder;
		private PropertyBuffer property;

		abstract String tag();

		@Override
		void init(M instance) {
			super.init(instance);

			if(optionsBuilder!=null) {
				instance.setOptionsManifest(optionsBuilder.build());
			}

			if(documentationBuilder!=null) {
				instance.setDocumentation(documentationBuilder.build());
			}

			if(properties!=null) {
				for(Entry<String, Object> entry : properties.entrySet()) {
					instance.setProperty(entry.getKey(), entry.getValue());
				}
			}
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#startElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		Builder<?> startElement(ManifestSource manifestSource, String uri,
				String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (qName) {
			case TAG_OPTIONS: {
				return (optionsBuilder = new OptionsBuilder());
			}

			case TAG_DOCUMENTATION: {
				return (documentationBuilder = new DocumentationBuilder());
			}

			case TAG_PROPERTY: {
				property = new PropertyBuffer();
				property.name = ParseUtils.normalize(attributes, ATTR_NAME);

				if(properties.containsKey(property.name))
					throw new SAXException("Duplicate property definition: "+property.name); //$NON-NLS-1$

				String type = ParseUtils.normalize(attributes, ATTR_TYPE);
				if(type!=null) {
					property.type = ValueType.parseValueType(type);
				}
			} break;

			default:
				throw new SAXException("Unexpected tag in "+tag()+" scope: "+qName); //$NON-NLS-1$ //$NON-NLS-2$
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#endElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		Builder<?> endElement(ManifestSource manifestSource, String uri,
				String localName, String qName, String text)
				throws SAXException {
			switch (qName) {

			case TAG_OPTIONS: {
				// no-op
			} break;

			case TAG_DOCUMENTATION: {
				// no-op
			} break;

			case TAG_PROPERTY: {
				Object value = property.type.parse(text, manifestSource.getClassLoader());

				properties.put(property.name, value);
			} break;

			default:
				throw new SAXException("Unexpected tag in "+tag()+" scope: "+qName); //$NON-NLS-1$ //$NON-NLS-2$
			}

			return this;
		}
	}

	abstract class MemberBuilder<M extends MemberManifest> extends ModifiableBuilder<M> {
		String name, description;
		Icon icon;
		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.ModifiableBuilder#init(de.ims.icarus.model.api.manifest.ModifiableManifest)
		 */
		@Override
		void init(M instance) {
			super.init(instance);

			if(name!=null) {
				instance.setName(name);
			}
			if(description!=null) {
				instance.setDescription(description);
			}
			if(icon!=null) {
				instance.setIcon(icon);
			}
		}
		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.DerivableBuilder#readAttributes(org.xml.sax.Attributes)
		 */
		@Override
		void readAttributes(Attributes attributes) {
			super.readAttributes(attributes);

			name = ParseUtils.normalize(attributes, ATTR_NAME);
			description = ParseUtils.normalize(attributes, ATTR_DESCRIPTION);
			icon = ParseUtils.iconValue(attributes, ATTR_ICON);
		}
	}

	abstract class LayerBuilder<L extends LayerManifest> extends MemberBuilder<L> {

		private final List<String> baseLayerIds = new ArrayList<>();
		private String layerTypeId;

		void link(ContextManifest contextManifest) {

		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.MemberBuilder#init(de.ims.icarus.model.api.manifest.MemberManifest)
		 */
		@Override
		void init(L instance) {
			super.init(instance);

			if(layerTypeId!=null) {
				instance.setLayerType(registry.getLayerType(layerTypeId));
			}
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.MemberBuilder#readAttributes(org.xml.sax.Attributes)
		 */
		@Override
		void readAttributes(Attributes attributes) {
			super.readAttributes(attributes);

			layerTypeId = ParseUtils.normalize(attributes, ATTR_LAYER_TYPE);
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#startElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		Builder<?> startElement(ManifestSource manifestSource, String uri,
				String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (qName) {

			case TAG_BASE_LAYER: {
				baseLayerIds.add(ParseUtils.normalize(attributes, ATTR_LAYER_ID));
			} break;

			default:
				return super.startElement(manifestSource, uri, localName, qName, attributes);
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#endElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		Builder<?> endElement(ManifestSource manifestSource, String uri,
				String localName, String qName, String text)
				throws SAXException {
			switch (qName) {

			case TAG_BASE_LAYER: {

			} break;

			default:
				return super.endElement(manifestSource, uri, localName, qName, text);
			}

			return this;
		}
	}

	class LayerGroupBuilder extends Builder<LayerGroupManifest> {

		private String name;
		private Boolean independent;
		private String primaryLayerId;
		private final List<LayerBuilder<?>> layerBuilders = new ArrayList<>();

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#build()
		 */
		@Override
		LayerGroupManifest build() {
			// TODO Auto-generated method stub
			return null;
		}

		void link(ContextManifest contextManifest) {
			for(LayerBuilder<?> layerBuilder : layerBuilders) {
				layerBuilder.link(contextManifest);
			}
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#startElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		Builder<?> startElement(ManifestSource manifestSource, String uri,
				String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (qName) {
			case TAG_LAYER_GROUP: {
				name = ParseUtils.normalize(attributes, ATTR_ID);
				primaryLayerId = ParseUtils.normalize(attributes, ATTR_PRIMARY_LAYER);
				independent = ParseUtils.boolValue(attributes, ATTR_INDEPENDENT);
			} break;

			case TAG_MARKABLE_LAYER: {
				return add(layerBuilders, new MarkableLayerBuilder());
			}

			case TAG_STRUCTURE_LAYER: {
				return add(layerBuilders, new StructureLayerBuilder());
			}

			case TAG_FRAGMENT_LAYER: {
				return add(layerBuilders, new FragmentLayerBuilder());
			}

			case TAG_ANNOTATION_LAYER: {
				return add(layerBuilders, new AnnotationLayerBuilder());
			}

			case TAG_HIGHLIGHT_LAYER: {
				return add(layerBuilders, new HighlightLayerBuilder());
			}

			default:
				throw new SAXException("Unexpected tag in layer-group scope: "+qName); //$NON-NLS-1$
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#endElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		Builder<?> endElement(ManifestSource manifestSource, String uri,
				String localName, String qName, String text)
				throws SAXException {
			switch (qName) {

			case TAG_LAYER_GROUP: {
				return null;
			}

			case TAG_MARKABLE_LAYER: {
				// no-op
			} break;

			case TAG_STRUCTURE_LAYER: {
				// no-op
			} break;

			case TAG_FRAGMENT_LAYER: {
				// no-op
			} break;

			case TAG_ANNOTATION_LAYER: {
				// no-op
			} break;

			case TAG_HIGHLIGHT_LAYER: {
				// no-op
			} break;

			default:
				throw new SAXException("Unexpected tag in layer-group scope: "+qName); //$NON-NLS-1$
			}

			return this;
		}

	}

	class ContextBuiler extends MemberBuilder<ContextManifest> {

		private final List<PrerequisiteBuffer> prerequisites =  new ArrayList<>();
		private final List<LayerGroupBuilder> groupBuilders = new ArrayList<>();

		private Boolean independent;

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.ModifiableBuilder#tag()
		 */
		@Override
		String tag() {
			return TAG_CONTEXT;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#build()
		 */
		@Override
		ContextManifest build() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.MemberBuilder#init(de.ims.icarus.model.api.manifest.MemberManifest)
		 */
		@Override
		void init(ContextManifest instance) {
			// TODO Auto-generated method stub
			super.init(instance);
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.MemberBuilder#readAttributes(org.xml.sax.Attributes)
		 */
		@Override
		void readAttributes(Attributes attributes) {
			super.readAttributes(attributes);

			independent = ParseUtils.boolValue(attributes, ATTR_INDEPENDENT);
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.ModifiableBuilder#startElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		Builder<?> startElement(ManifestSource manifestSource, String uri,
				String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (qName) {

			case TAG_CONTEXT: {
				readAttributes(attributes);
			} break;

			case TAG_PREREQUISITES: {
				// no-op
			} break;

			case TAG_PREREQUISITE: {
				PrerequisiteBuffer prerequisite = new PrerequisiteBuffer();
				prerequisite.layerId = ParseUtils.normalize(attributes, ATTR_LAYER_ID);
				prerequisite.contextId = ParseUtils.normalize(attributes, ATTR_CONTEXT_ID);
				prerequisite.layerType = ParseUtils.normalize(attributes, ATTR_LAYER_TYPE);
				prerequisite.alias = ParseUtils.normalize(attributes, ATTR_ALIAS);

				prerequisites.add(prerequisite);
			} break;

			case TAG_LAYER_GROUP: {
				return add(groupBuilders, new LayerGroupBuilder());
			}

			default:
				return super.startElement(manifestSource, uri, localName, qName, attributes);
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.ModifiableBuilder#endElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		Builder<?> endElement(ManifestSource manifestSource, String uri,
				String localName, String qName, String text)
				throws SAXException {
			switch (qName) {

			case TAG_CONTEXT: {
				return null;
			}

			case TAG_PREREQUISITES: {
				// no-op
			} break;

			case TAG_PREREQUISITE: {
				// no-op
			} break;

			case TAG_LAYER_GROUP: {
				// no-op
			} break;

			default:
				return super.endElement(manifestSource, uri, localName, qName, text);
			}

			return this;
		}

	}

	class CorpusBuilder extends MemberBuilder<CorpusManifest> {

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.Builder#build()
		 */
		@Override
		CorpusManifest build() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.ModifiableBuilder#tag()
		 */
		@Override
		String tag() {
			return TAG_CORPUS;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.ModifiableBuilder#startElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		Builder<?> startElement(ManifestSource manifestSource, String uri,
				String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (qName) {

			case TAG_BASE_LAYER: {

			} break;

			default:
				return super.startElement(manifestSource, uri, localName, qName, attributes);
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.sax.ManifestXmlReader.ModifiableBuilder#endElement(de.ims.icarus.model.api.manifest.ManifestSource, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		Builder<?> endElement(ManifestSource manifestSource, String uri,
				String localName, String qName, String text)
				throws SAXException {
			switch (qName) {

			case TAG_BASE_LAYER: {

			} break;

			default:
				return super.endElement(manifestSource, uri, localName, qName, text);
			}

			return this;
		}

	}
}
