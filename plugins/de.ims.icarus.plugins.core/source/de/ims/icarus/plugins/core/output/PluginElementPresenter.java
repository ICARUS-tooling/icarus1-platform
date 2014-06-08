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
package de.ims.icarus.plugins.core.output;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.java.plugin.registry.Documentable;
import org.java.plugin.registry.Documentation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.Library;
import org.java.plugin.registry.PluginAttribute;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginElement;
import org.java.plugin.registry.PluginFragment;
import org.java.plugin.registry.PluginPrerequisite;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.view.AbstractEditorPanePresenter;
import de.ims.icarus.ui.view.MalformedTemplateException;
import de.ims.icarus.ui.view.Template;
import de.ims.icarus.ui.view.Template.SubTemplateCache;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.cache.LRUCache;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.id.UnknownIdentifierException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PluginElementPresenter extends AbstractEditorPanePresenter<Object> {
	
	private String templateName;
	
	private static Map<String, Template> templates;
	private static Map<String, TemplateFiller> fillers;
	
	// We can use strong references to the data objects being
	// cached since all members of the plugin framework are
	// alive during the entire runtime of the application
	private static LRUCache<Object, String> textCache;
	
	private static final String emptyContent = "<html>{1}</html>"; //$NON-NLS-1$
	private static final String htmlBegin = "<html>"; //$NON-NLS-1$
	private static final String htmlEnd = "</html>"; //$NON-NLS-1$
	private static final String headBegin = "<head>"; //$NON-NLS-1$
	private static final String headEnd = "</head>"; //$NON-NLS-1$
	private static final String baseDef = "<base href='{1}' />"; //$NON-NLS-1$
	
	private static URL baseURL;

	/**
	 * 
	 */
	public PluginElementPresenter() {
	}
	
	private Template getTemplate(String name) {
		if(name==null) {
			return null;
		}
		if(templates==null) {
			templates = new HashMap<>();
		}
		
		Template template = templates.get(name);
		
		if(template==null) {
			template = loadTemplate(name);
			templates.put(name, template);
		}
		
		return template;
	}
	
	public static boolean supportsData(Object data) {
		return getTemplateName(data)!=null;
	}
	
	private static String getTemplateName(Object data) {
		if(data==null) {
			return null;
		}
		
		if(data instanceof PluginDescriptor) {
			return "plugin-descriptor"; //$NON-NLS-1$
		} else if(data instanceof PluginFragment) {
			return "plugin-fragment"; //$NON-NLS-1$
		} else if(data instanceof Extension) {
			return "extension"; //$NON-NLS-1$
		} else if(data instanceof ExtensionPoint) {
			return "extension-point"; //$NON-NLS-1$
		} else if(data instanceof Extension.Parameter) {
			return "parameter"; //$NON-NLS-1$
		} else if(data instanceof ExtensionPoint.ParameterDefinition) {
			return "parameter-definition"; //$NON-NLS-1$
		} else if(data instanceof Library) {
			return "library"; //$NON-NLS-1$
		} else if(data instanceof Documentable) {
			return "documentable"; //$NON-NLS-1$
		}
		
		return null;
	}
	
	private Template loadTemplate(String name) {
		if(name==null)
			throw new NullPointerException("Invalid template file name"); //$NON-NLS-1$
		
		name = name+".tpl"; //$NON-NLS-1$
		
		URL url = PluginElementPresenter.class.getResource(name);
		if(url==null) {
			LoggerFactory.log(this, Level.SEVERE, "Cannot find template file: "+name); //$NON-NLS-1$
			return null;
		}
		String templateData = null;
		Template template = null;
		
		try {
			templateData = IOUtil.readStream(url.openStream(), IOUtil.UTF8_ENCODING);
			template = Template.compile(templateData, null);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to read template data from resource: "+url, e); //$NON-NLS-1$
		} catch (MalformedTemplateException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Malformed template data in resource: "+url, e); //$NON-NLS-1$
		}
		
		return template;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible("DocumentableContentType", type); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if(data==null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
		
		if(data==presentedData) {
			return;
		}
		
		String templateName = getTemplateName(data);
		if(templateName==null) {
			throw new UnsupportedPresentationDataException("Cannot present data: "+data); //$NON-NLS-1$
		}
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		presentedData = data;
		this.options = options;
		this.templateName = templateName;
		
		if(contentPane!=null) {
			refresh();
		}
	}
	
	@Override
	protected String getDefaultText() {
		return ResourceManager.format(emptyContent, 
				ResourceManager.getInstance().get("plugins.core.outputView.emptyContent")); //$NON-NLS-1$;
	}
	
	private String getUnsupportedText() {
		return ResourceManager.format(emptyContent, 
				ResourceManager.getInstance().get("plugins.core.outputView.unsupportedContent")); //$NON-NLS-1$;
	}
	
	private String getCachedText(Object data) {
		if(textCache==null) {
			return null;
		}
		
		return textCache.get(data);
	}
	
	private void cacheText(Object data, String text) {
		if(textCache==null) {
			// Make cache size affordable
			// TODO leave size at 10 or increase to 20 or 50?
			textCache = new LRUCache<>(10);
		}
		
		textCache.put(data, text);
	}
	
	private static URL getBaseURL() {
		if(baseURL==null) {
			URL jarLocation = PluginElementPresenter.class.getProtectionDomain()
					.getCodeSource().getLocation();
			
			try {
				baseURL = new URL(jarLocation, "de/ims/icarus/plugins/core/icons/"); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				LoggerFactory.log(PluginElementPresenter.class, Level.SEVERE, "Failed to create base URL for icons folder at jar: "+jarLocation, e); //$NON-NLS-1$
			}
			
			LoggerFactory.log(PluginElementPresenter.class, Level.FINE, "New base url for plugin element presenter html templates:\n"+baseURL); //$NON-NLS-1$
		}
		return baseURL;
	}
	
	private TemplateFiller getFiller(String name) {
		if(name==null) {
			return null;
		}
		if(fillers==null) {
			fillers = new HashMap<>();
			
			// Add default fillers
			fillers.put("plugin-descriptor", new PluginDescriptorFiller()); //$NON-NLS-1$
			fillers.put("plugin-fragment", new PluginFragmentFiller()); //$NON-NLS-1$
			fillers.put("extension", new ExtensionFiller()); //$NON-NLS-1$
			fillers.put("extension-point", new ExtensionPointFiller()); //$NON-NLS-1$
			fillers.put("library", new LibraryFiller()); //$NON-NLS-1$
			fillers.put("parameter", new ParameterFiller()); //$NON-NLS-1$
			fillers.put("parameter-definition", new ParameterDefinitionFiller()); //$NON-NLS-1$
			fillers.put("documentable", new DocumentableFiller()); //$NON-NLS-1$
		}
		
		return fillers.get(name);
	}
	
	@Override
	protected void refresh() {
		if(contentPane==null) {
			return;
		}
		
		Object data = presentedData;
		
		// Just show default message if nothing is there to display
		if(data==null) {
			contentPane.setText(getDefaultText());
			return;
		}
		
		// Look for cached text first
		String cachedText = getCachedText(data);
		if(cachedText!=null) {
			contentPane.setText(cachedText);
			return;
		}
		
		Template template = getTemplate(templateName);
		TemplateFiller filler = getFiller(templateName);
		if(template==null || filler==null) {
			contentPane.setText(getUnsupportedText());
			return;
		}
		
		template.clear();
		
		StringBuilder sb = new StringBuilder(1000);
		
		sb.append(htmlBegin);
		sb.append(headBegin).append(ResourceManager.format(baseDef, getBaseURL())).append(headEnd);
		filler.fillTemplate(template, data, options);
		template.appendText(sb);
		sb.append(htmlEnd);
		
		String text = sb.toString();
		contentPane.setText(text);
		// FIXME sometimes scrollPane jumps to the bottom after setText() with new content
		
		// Do not cache every little stuff
		if(text.length()>1000) {
			cacheText(data, text);
		}
	}
	
	private static String getId(Identity obj) {
		return obj==null ? NONE : obj.getId();
	}
	
	private static void fillCacheEmpty(SubTemplateCache cache, String...fields) {
		if(cache==null) {
			return;
		}
		for(String field : fields) {
			cache.setValue(field, NONE);
		}
		cache.commit();
	}
	
	private static void fillDocumentable(Template template, Documentable<?> documentable) {
		template.setValue("captionDocumentation", ResourceManager.getInstance().get("plugins.core.outputView.captions.documentation")); //$NON-NLS-1$ //$NON-NLS-2$
		template.setValue("captionReferences", ResourceManager.getInstance().get("plugins.core.outputView.captions.references")); //$NON-NLS-1$ //$NON-NLS-2$
		template.setValue("captionIndex", ResourceManager.getInstance().get("plugins.core.outputView.captions.index")); //$NON-NLS-1$ //$NON-NLS-2$
		template.setValue("captionReference", ResourceManager.getInstance().get("plugins.core.outputView.captions.reference")); //$NON-NLS-1$ //$NON-NLS-2$
		SubTemplateCache cache = null;
		try {
			cache = template.getSubTemplate("references"); //$NON-NLS-1$
		} catch(UnknownIdentifierException e) {
			// no-op
		}
		Documentation<?> doc = documentable.getDocumentation();
		int index = 0;
		if(doc!=null) {
			template.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
			if(cache!=null) {
				for(Documentation.Reference<?> reference : doc.getReferences()) {
					index++;
					cache.setValue("index", index); //$NON-NLS-1$
					cache.setValue("reference", reference.getCaption()); //$NON-NLS-1$
					cache.commit();
				}
			}
		}
		
		if(doc==null || index==0) {
			template.setValue("documentation", NONE);	 //$NON-NLS-1$
			fillCacheEmpty(cache, "index", "reference");		 //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private static String listIds(Collection<? extends Identity> items) {
		if(items==null || items.isEmpty()) {
			return NONE;
		}
		StringBuilder sb = new StringBuilder(items.size()*20);
		for(Iterator<? extends Identity> i = items.iterator(); i.hasNext();) {
			sb.append(getId(i.next()));
			if(i.hasNext()) {
				sb.append(BR);
			}
		}
		return sb.toString();
	}
	
	private static void fillPluginElement(Template template, PluginElement<?> element) {		
		// General stuff
		template.setValue("captionId", ResourceManager.getInstance().get("plugins.core.outputView.captions.id")); //$NON-NLS-1$ //$NON-NLS-2$
		template.setValue("id", element.getId()); //$NON-NLS-1$
		template.setValue("captionDeclaringPlugin", ResourceManager.getInstance().get("plugins.core.outputView.captions.declaringPlugin")); //$NON-NLS-1$ //$NON-NLS-2$
		template.setValue("declaringPlugin", getId(element.getDeclaringPluginDescriptor())); //$NON-NLS-1$
		template.setValue("captionDeclaringFragment", ResourceManager.getInstance().get("plugins.core.outputView.captions.declaringFragment")); //$NON-NLS-1$ //$NON-NLS-2$
		template.setValue("declaringFragment", getId(element.getDeclaringPluginFragment())); //$NON-NLS-1$
		
		// Documentation
		if(element instanceof Documentable) {
			fillDocumentable(template, (Documentable<?>)element);
		}
	}
	
	private interface TemplateFiller {
		void fillTemplate(Template template, Object data, Options options);
	}
	
	private static class ExtensionPointFiller implements TemplateFiller {
		
		private static String declaringPointTxt = 
				"<img src='ext_point_obj.gif' />&nbsp;{1}<br><img src='plugin_obj.gif' />&nbsp;{2}"; //$NON-NLS-1$
		
		/**
		 * @see de.ims.icarus.plugins.core.output.PluginElementPresenter.TemplateFiller#fillTemplate(de.ims.icarus.ui.view.Template, java.lang.Object, de.ims.icarus.util.Options)
		 */
		@Override
		public void fillTemplate(Template template, Object data, Options options) {
			ExtensionPoint extensionPoint = (ExtensionPoint) data;
			// Caption
			template.setValue("caption", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.extensionPoint")); //$NON-NLS-1$
					
			// Defaults
			fillPluginElement(template, extensionPoint);
			
			// Relation
			template.setValue("captionMultiplicity", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.multiplicity")); //$NON-NLS-1$
			template.setValue("multiplicity", noneOrNonempty(extensionPoint.getMultiplicity())); //$NON-NLS-1$

			// Parameter definitions
			// captions set so far: captionId, captionMultiplicity, captionDocumentation
			template.setValue("captionParameterDefinitions", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.parameterDefinitions")); //$NON-NLS-1$
			template.setValue("captionId", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.id")); //$NON-NLS-1$
			SubTemplateCache cache = template.getSubTemplate("paramaterDefinitions"); //$NON-NLS-1$
			Collection<ExtensionPoint.ParameterDefinition> paramDefs = extensionPoint.getParameterDefinitions();
			int count = 0;
			for(ExtensionPoint.ParameterDefinition def : paramDefs) {
				if(def.getDeclaringExtensionPoint()!=extensionPoint) {
					continue;
				}
				count++;
				cache.setValue("id", getId(def)); //$NON-NLS-1$
				cache.setValue("multiplicity", noneOrNonempty(def.getMultiplicity())); //$NON-NLS-1$
				Documentation<?> doc = def.getDocumentation();
				if(doc==null) {
					cache.setValue("documentation", NONE); //$NON-NLS-1$
				} else {
					cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
				}
				cache.commit();
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "multiplicity", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			
			// Inherited Parameter definitions
			// captions set so far: captionId, captionMultiplicity, captionDocumentation
			template.setValue("captionInheritedParameterDefinitions", ResourceManager.getInstance().get( //$NON-NLS-1$
							"plugins.core.outputView.captions.inheritedParameterDefinitions")); //$NON-NLS-1$
			template.setValue("captionDeclaringPoint", ResourceManager.getInstance().get( //$NON-NLS-1$
							"plugins.core.outputView.captions.declaringPoint")); //$NON-NLS-1$
			cache = template.getSubTemplate("inheritedParamaterDefinitions"); //$NON-NLS-1$
			count = 0;
			for(ExtensionPoint.ParameterDefinition def : paramDefs) {
				if(def.getDeclaringExtensionPoint()==extensionPoint) {
					continue;
				}
				count++;
				cache.setValue("id", getId(def)); //$NON-NLS-1$
				cache.setValue("multiplicity", noneOrNonempty(def.getMultiplicity())); //$NON-NLS-1$
				String declaringPoint = ResourceManager.format(declaringPointTxt, 
						getId(def.getDeclaringExtensionPoint()),
						getId(def.getDeclaringPluginDescriptor()));
				cache.setRawValue("declaringPoint", declaringPoint); //$NON-NLS-1$
				Documentation<?> doc = def.getDocumentation();
				if(doc==null) {
					cache.setValue("documentation", NONE); //$NON-NLS-1$
				} else {
					cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
				}
				cache.commit();
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "multiplicity", "declaringPoint", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			
			// Connected extensions
			template.setValue("captionConnectedExtensions", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.connectedExtensions")); //$NON-NLS-1$
			cache = template.getSubTemplate("connectedExtensions"); //$NON-NLS-1$
			count = 0;
			for(Extension extension : extensionPoint.getConnectedExtensions()) {
				count++;
				cache.setValue("id", getId(extension)); //$NON-NLS-1$
				cache.setValue("declaringPlugin", getId(extension.getDeclaringPluginDescriptor())); //$NON-NLS-1$
				Documentation<?> doc = extension.getDocumentation();
				if(doc==null) {
					cache.setValue("documentation", NONE); //$NON-NLS-1$
				} else {
					cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
				}
				cache.commit();
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "declaringPlugin", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}			
		}
		
	}
	
	private static class PluginDescriptorFiller implements TemplateFiller {

		private static String extPointAncestryText = 
				"<img src='ext_point_obj.gif' />&nbsp;{1}<br><img src='plugin_obj.gif' />&nbsp;{2}"; //$NON-NLS-1$
		private static String extensionTargetText = 
				"<img src='ext_point_obj.gif' />&nbsp;{1}<br><img src='plugin_obj.gif' />&nbsp;{2}"; //$NON-NLS-1$

		/**
		 * @see de.ims.icarus.plugins.core.output.PluginElementPresenter.TemplateFiller#fillTemplate(de.ims.icarus.ui.view.Template, java.lang.Object, de.ims.icarus.util.Options)
		 */
		@Override
		public void fillTemplate(Template template, Object data, Options options) {
			PluginDescriptor descriptor = (PluginDescriptor) data;
			// Caption
			template.setValue("caption", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.pluginDescriptor")); //$NON-NLS-1$
			
			fillDocumentable(template, descriptor);

			// General
			template.setValue("captionId", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.id")); //$NON-NLS-1$ 
			template.setValue("id", descriptor.getId()); //$NON-NLS-1$
			template.setValue("captionVersion", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.version")); //$NON-NLS-1$ 
			template.setValue("version", noneOrNonempty(descriptor.getVersion())); //$NON-NLS-1$
			template.setValue("captionVendor", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.vendor")); //$NON-NLS-1$  
			template.setValue("vendor", noneOrNonempty(descriptor.getVendor())); //$NON-NLS-1$
			template.setValue("captionLocation", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.location")); //$NON-NLS-1$
			template.setValue("location", noneOrNonempty(descriptor.getLocation())); //$NON-NLS-1$
			
			// Attributes
			// captions set so far: captionId, captionDocumentation
			template.setValue("captionAttributes", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.attributes")); //$NON-NLS-1$
			template.setValue("captionValue", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.value")); //$NON-NLS-1$
			SubTemplateCache cache = template.getSubTemplate("attributes"); //$NON-NLS-1$
			int count = 0;
			for(PluginAttribute attribute : descriptor.getAttributes()) {
				count++;
				cache.setValue("id", getId(attribute)); //$NON-NLS-1$
				cache.setValue("value", noneOrNonempty(attribute.getValue())); //$NON-NLS-1$
				Documentation<?> doc = attribute.getDocumentation();
				if(doc==null) {
					cache.setValue("documentation", NONE); //$NON-NLS-1$
				} else {
					cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
				}
				cache.commit();
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "value", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			
			// Prerequisites
			// captions set so far: captionId, captionVersion, captionDocumentation
			template.setValue("captionPrerequisites", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.prerequisites")); //$NON-NLS-1$
			template.setValue("captionPluginVersion", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.pluginVersion")); //$NON-NLS-1$
			template.setValue("captionMatchingRule", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.matchingRule")); //$NON-NLS-1$
			template.setValue("captionOptional", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.optional")); //$NON-NLS-1$
			cache = template.getSubTemplate("prerequisites"); //$NON-NLS-1$
			count = 0;
			for(PluginPrerequisite prerequisite : descriptor.getPrerequisites()) {
				count++;
				cache.setValue("id", noneOrNonempty(prerequisite.getPluginId())); //$NON-NLS-1$
				cache.setValue("version", noneOrNonempty(prerequisite.getPluginVersion())); //$NON-NLS-1$
				cache.setValue("matchingRule", noneOrNonempty(prerequisite.getMatchingRule())); //$NON-NLS-1$
				cache.setValue("optional", ResourceManager.getInstance().get( //$NON-NLS-1$
						prerequisite.isOptional() ? "yes" : "no")); //$NON-NLS-1$ //$NON-NLS-2$
				Documentation<?> doc = prerequisite.getDocumentation();
				if(doc==null) {
					cache.setValue("documentation", NONE); //$NON-NLS-1$
				} else {
					cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
				}
				cache.commit();
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "version", "matchingRule", "optional", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
			
			// Fragments
			// captions set so far: captionId, captionPluginVersion, captionMatchingRule, captionDocumentation
			template.setValue("captionFragments", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.fragments")); //$NON-NLS-1$
			cache = template.getSubTemplate("fragments"); //$NON-NLS-1$
			count = 0;
			for(PluginFragment fragment : descriptor.getFragments()) {
				count++;
				cache.setValue("id", getId(fragment)); //$NON-NLS-1$
				cache.setValue("version", noneOrNonempty(fragment.getVersion())); //$NON-NLS-1$
				cache.setValue("pluginVersion", noneOrNonempty(fragment.getPluginVersion())); //$NON-NLS-1$
				cache.setValue("matchingRule", noneOrNonempty(fragment.getMatchingRule())); //$NON-NLS-1$
				Documentation<?> doc = fragment.getDocumentation();
				if(doc==null) {
					cache.setValue("documentation", NONE); //$NON-NLS-1$
				} else {
					cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
				}
				cache.commit();
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "version", "pluginVersion",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"matchingRule", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ 
			}
			
			// Extensions
			// captions set so far: captionId, captionDocumentation
			template.setValue("captionExtensions", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.extensions")); //$NON-NLS-1$
			template.setValue("captionTarget", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.target")); //$NON-NLS-1$
			cache = template.getSubTemplate("extensions"); //$NON-NLS-1$
			count = 0;
			for(Extension extension : descriptor.getExtensions()) {
				count++;
				cache.setValue("id", getId(extension)); //$NON-NLS-1$
				String target = ResourceManager.format(extensionTargetText, 
						noneOrNonempty(extension.getExtendedPointId()),
						noneOrNonempty(extension.getExtendedPluginId()));
				cache.setRawValue("target", target); //$NON-NLS-1$
				Documentation<?> doc = extension.getDocumentation();
				if(doc==null) {
					cache.setValue("documentation", NONE); //$NON-NLS-1$
				} else {
					cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
				}
				cache.commit();
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "target", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			}
			
			// Extension-points
			// captions set so far: captionId, captionDocumentation
			template.setValue("captionExtensionPoints", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.extensionPoints")); //$NON-NLS-1$
			template.setValue("captionAncestry", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.ancestry")); //$NON-NLS-1$
			template.setValue("captionMultiplicity", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.multiplicity")); //$NON-NLS-1$
			template.setValue("captionConnectedExtensions", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.connectedExtensions")); //$NON-NLS-1$
			cache = template.getSubTemplate("extensionPoints"); //$NON-NLS-1$
			count = 0;
			for(ExtensionPoint extensionPoint : descriptor.getExtensionPoints()) {
				count++;
				cache.setValue("id", getId(extensionPoint)); //$NON-NLS-1$
				String ancestry = NONE;
				if(extensionPoint.getParentExtensionPointId()!=null) {
					ancestry = ResourceManager.format(extPointAncestryText, 
							noneOrNonempty(extensionPoint.getParentExtensionPointId()),
							noneOrNonempty(extensionPoint.getParentPluginId()));
				}
				cache.setRawValue("ancestry", ancestry); //$NON-NLS-1$
				cache.setValue("multiplicity", noneOrNonempty(extensionPoint.getMultiplicity())); //$NON-NLS-1$
				cache.setRawValue("connectedExtensions", listIds(extensionPoint.getConnectedExtensions())); //$NON-NLS-1$
				Documentation<?> doc = extensionPoint.getDocumentation();
				if(doc==null) {
					cache.setValue("documentation", NONE); //$NON-NLS-1$
				} else {
					cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
				}
				cache.commit();
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "ancestry", "multiplicity", "connectedExtensions", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
			}
			
		}
		
	}
	
	private static class ExtensionFiller implements TemplateFiller {
		
		private void feedParameter(Extension.Parameter param, SubTemplateCache cache) {
			cache.setValue("id", getId(param)); //$NON-NLS-1$
			cache.setValue("parentParameter", getId(param.getSuperParameter())); //$NON-NLS-1$
			cache.setValue("value", noneOrNonempty(param.rawValue())); //$NON-NLS-1$
			
			Documentation<?> doc = param.getDocumentation();
			if(doc==null) {
				cache.setValue("documentation", NONE); //$NON-NLS-1$
			} else {
				cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
			}
			cache.commit();
			
			for(Extension.Parameter subParam : param.getSubParameters()) {
				feedParameter(subParam, cache);
			}
		}

		/**
		 * @see de.ims.icarus.plugins.core.output.PluginElementPresenter.TemplateFiller#fillTemplate(de.ims.icarus.ui.view.Template, java.lang.Object, de.ims.icarus.util.Options)
		 */
		@Override
		public void fillTemplate(Template template, Object data, Options options) {
			Extension extension = (Extension) data;
			// Caption
			template.setValue("caption", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.extension")); //$NON-NLS-1$
					
			// Defaults
			fillPluginElement(template, extension);
			
			// Target stuff
			template.setValue("captionTargetPoint", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.targetPoint")); //$NON-NLS-1$
			template.setValue("targetPoint", noneOrNonempty(extension.getExtendedPointId())); //$NON-NLS-1$
			template.setValue("captionTargetPlugin", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.targetPlugin")); //$NON-NLS-1$
			template.setValue("targetPlugin", noneOrNonempty(extension.getExtendedPluginId())); //$NON-NLS-1$
			
			// Parameters
			// captions set so far: captionId, captionDocumentation
			template.setValue("captionParameters", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.parameterDefinitions")); //$NON-NLS-1$
			template.setValue("captionParentParameter", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.parentParameter")); //$NON-NLS-1$
			template.setValue("captionValue", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.value")); //$NON-NLS-1$
			SubTemplateCache cache = template.getSubTemplate("paramaters"); //$NON-NLS-1$
			int count = 0;
			for(Extension.Parameter param : extension.getParameters()) {
				count++;
				feedParameter(param, cache);
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "multiplicity", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		
	}
	
	private static class ParameterFiller implements TemplateFiller {

		/**
		 * @see de.ims.icarus.plugins.core.output.PluginElementPresenter.TemplateFiller#fillTemplate(de.ims.icarus.ui.view.Template, java.lang.Object, de.ims.icarus.util.Options)
		 */
		@Override
		public void fillTemplate(Template template, Object data, Options options) {
			Extension.Parameter param = (Extension.Parameter) data;
			// Caption
			template.setValue("caption", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.parameter")); //$NON-NLS-1$
					
			// Defaults
			fillPluginElement(template, param);
			
			// Declaring extension
			template.setValue("captionDeclaringExtension", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.declaringExtension")); //$NON-NLS-1$
			template.setValue("declaringExtension", getId(param.getDeclaringExtension())); //$NON-NLS-1$
			
			// Declaring extension-point
			template.setValue("captionDeclaringExtensionPoint", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.declaringExtensionPoint")); //$NON-NLS-1$
			template.setValue("declaringExtensionPoint", getId(param.getDefinition().getDeclaringExtensionPoint())); //$NON-NLS-1$
			
			// Value
			template.setValue("captionValue", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.value")); //$NON-NLS-1$
			template.setValue("value", noneOrNonempty(param.rawValue())); //$NON-NLS-1$
			
			// Type
			template.setValue("captionType", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.type")); //$NON-NLS-1$
			template.setValue("type", noneOrNonempty(param.getDefinition().getType())); //$NON-NLS-1$
		}
		
	}
	
	private static class ParameterDefinitionFiller implements TemplateFiller {

		/**
		 * @see de.ims.icarus.plugins.core.output.PluginElementPresenter.TemplateFiller#fillTemplate(de.ims.icarus.ui.view.Template, java.lang.Object, de.ims.icarus.util.Options)
		 */
		@Override
		public void fillTemplate(Template template, Object data, Options options) {
			ExtensionPoint.ParameterDefinition def = (ExtensionPoint.ParameterDefinition) data;
			// Caption
			template.setValue("caption", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.parameterDefinition")); //$NON-NLS-1$
					
			// Defaults
			fillPluginElement(template, def);
			
			// Declaring extension-point
			template.setValue("captionDeclaringExtensionPoint", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.declaringExtensionPoint")); //$NON-NLS-1$
			template.setValue("declaringExtensionPoint", getId(def.getDeclaringExtensionPoint())); //$NON-NLS-1$
			
			// Relation
			template.setValue("captionMultiplicity", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.multiplicity")); //$NON-NLS-1$
			template.setValue("multiplicity", noneOrNonempty(def.getMultiplicity())); //$NON-NLS-1$
		}
		
	}
	
	private static class LibraryFiller implements TemplateFiller {

		/**
		 * @see de.ims.icarus.plugins.core.output.PluginElementPresenter.TemplateFiller#fillTemplate(de.ims.icarus.ui.view.Template, java.lang.Object, de.ims.icarus.util.Options)
		 */
		@Override
		public void fillTemplate(Template template, Object data, Options options) {
			Library library = (Library) data;
			// Caption
			template.setValue("caption", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.library")); //$NON-NLS-1$
			
			// Defaults
			fillPluginElement(template, library);
			
			// Version
			template.setValue("captionVersion", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.version")); //$NON-NLS-1$
			template.setValue("version", noneOrNonempty(library.getVersion())); //$NON-NLS-1$

			// Exports
			template.setValue("captionExports", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.exports")); //$NON-NLS-1$
			template.setValue("captionIndex", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.index")); //$NON-NLS-1$
			template.setValue("captionPath", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.path")); //$NON-NLS-1$
			SubTemplateCache cache = template.getSubTemplate("exports"); //$NON-NLS-1$
			int index = 0;
			for(String path : library.getExports()) {
				index++;
				cache.setValue("index", index); //$NON-NLS-1$
				cache.setValue("path", path); //$NON-NLS-1$
				cache.commit();
			}
			if(index==0) {
				fillCacheEmpty(cache, "index", "path"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
	}
	
	private static class PluginFragmentFiller implements TemplateFiller {

		private static String extPointAncestryText = 
				"<img src='ext_point_obj.gif' />&nbsp;{1}<br><img src='plugin_obj.gif' />&nbsp;{2}"; //$NON-NLS-1$
		private static String extensionTargetText = 
				"<img src='ext_point_obj.gif' />&nbsp;{1}<br><img src='plugin_obj.gif' />&nbsp;{2}"; //$NON-NLS-1$

		/**
		 * @see de.ims.icarus.plugins.core.output.PluginElementPresenter.TemplateFiller#fillTemplate(de.ims.icarus.ui.view.Template, java.lang.Object, de.ims.icarus.util.Options)
		 */
		@Override
		public void fillTemplate(Template template, Object data, Options options) {
			PluginFragment fragment = (PluginFragment) data;
			PluginDescriptor descriptor = PluginUtil.getPluginRegistry().getPluginDescriptor(fragment.getPluginId());
			// Caption
			template.setValue("caption", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.pluginFragment")); //$NON-NLS-1$
			
			fillDocumentable(template, fragment);

			// General
			template.setValue("captionId", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.id")); //$NON-NLS-1$ 
			template.setValue("id", fragment.getId()); //$NON-NLS-1$
			template.setValue("captionVersion", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.version")); //$NON-NLS-1$ 
			template.setValue("version", noneOrNonempty(fragment.getVersion())); //$NON-NLS-1$
			template.setValue("captionVendor", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.vendor")); //$NON-NLS-1$  
			template.setValue("vendor", noneOrNonempty(fragment.getVendor())); //$NON-NLS-1$
			template.setValue("captionPlugin", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.plugin")); //$NON-NLS-1$  
			template.setValue("plugin", noneOrNonempty(fragment.getPluginId())); //$NON-NLS-1$
			template.setValue("captionPluginVersion", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.pluginVersion")); //$NON-NLS-1$ 
			template.setValue("pluginVersion", noneOrNonempty(fragment.getPluginVersion())); //$NON-NLS-1$
			template.setValue("captionmatchingRule", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.matchingRule")); //$NON-NLS-1$  
			template.setValue("matchingRule", noneOrNonempty(fragment.getMatchingRule())); //$NON-NLS-1$
			template.setValue("captionLocation", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.location")); //$NON-NLS-1$
			template.setValue("location", noneOrNonempty(fragment.getLocation())); //$NON-NLS-1$

			// Attributes
			// captions set so far: captionId, captionDocumentation
			template.setValue("captionAttributes", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.attributes")); //$NON-NLS-1$
			template.setValue("captionValue", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.value")); //$NON-NLS-1$
			SubTemplateCache cache = template.getSubTemplate("attributes"); //$NON-NLS-1$
			int count = 0;
			for(PluginAttribute attribute : descriptor.getAttributes()) {
				if(attribute.getDeclaringPluginFragment()!=fragment) {
					continue;
				}
				count++;
				cache.setValue("id", getId(attribute)); //$NON-NLS-1$
				cache.setValue("value", noneOrNonempty(attribute.getValue())); //$NON-NLS-1$
				Documentation<?> doc = attribute.getDocumentation();
				if(doc==null) {
					cache.setValue("documentation", NONE); //$NON-NLS-1$
				} else {
					cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
				}
				cache.commit();
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "value", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			
			// Prerequisites
			// captions set so far: captionId, captionVersion, captionDocumentation
			template.setValue("captionPrerequisites", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.prerequisites")); //$NON-NLS-1$
			template.setValue("captionPluginVersion", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.pluginVersion")); //$NON-NLS-1$
			template.setValue("captionMatchingRule", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.matchingRule")); //$NON-NLS-1$
			template.setValue("captionOptional", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.optional")); //$NON-NLS-1$
			cache = template.getSubTemplate("prerequisites"); //$NON-NLS-1$
			count = 0;
			for(PluginPrerequisite prerequisite : descriptor.getPrerequisites()) {
				if(prerequisite.getDeclaringPluginFragment()!=fragment) {
					continue;
				}
				count++;
				cache.setValue("id", noneOrNonempty(prerequisite.getPluginId())); //$NON-NLS-1$
				cache.setValue("version", noneOrNonempty(prerequisite.getPluginVersion())); //$NON-NLS-1$
				cache.setValue("matchingRule", noneOrNonempty(prerequisite.getMatchingRule())); //$NON-NLS-1$
				cache.setValue("optional", ResourceManager.getInstance().get( //$NON-NLS-1$
						prerequisite.isOptional() ? "yes" : "no")); //$NON-NLS-1$ //$NON-NLS-2$
				Documentation<?> doc = prerequisite.getDocumentation();
				if(doc==null) {
					cache.setValue("documentation", NONE); //$NON-NLS-1$
				} else {
					cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
				}
				cache.commit();
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "version", "matchingRule", "optional", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
			
			// Extensions
			// captions set so far: captionId, captionDocumentation
			template.setValue("captionExtensions", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.extensions")); //$NON-NLS-1$
			template.setValue("captionTarget", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.target")); //$NON-NLS-1$
			cache = template.getSubTemplate("extensions"); //$NON-NLS-1$
			count = 0;
			for(Extension extension : descriptor.getExtensions()) {
				if(extension.getDeclaringPluginFragment()!=fragment) {
					continue;
				}
				count++;
				cache.setValue("id", getId(extension)); //$NON-NLS-1$
				String target = ResourceManager.format(extensionTargetText, 
						noneOrNonempty(extension.getExtendedPointId()),
						noneOrNonempty(extension.getExtendedPluginId()));
				cache.setRawValue("target", target); //$NON-NLS-1$
				Documentation<?> doc = extension.getDocumentation();
				if(doc==null) {
					cache.setValue("documentation", NONE); //$NON-NLS-1$
				} else {
					cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
				}
				cache.commit();
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "target", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			}
			
			// Extension-points
			// captions set so far: captionId, captionDocumentation
			template.setValue("captionExtensionPoints", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.extensionPoints")); //$NON-NLS-1$
			template.setValue("captionAncestry", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.ancestry")); //$NON-NLS-1$
			template.setValue("captionMultiplicity", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.multiplicity")); //$NON-NLS-1$
			template.setValue("captionConnectedExtensions", ResourceManager.getInstance().get( //$NON-NLS-1$
					"plugins.core.outputView.captions.connectedExtensions")); //$NON-NLS-1$
			cache = template.getSubTemplate("extensionPoints"); //$NON-NLS-1$
			count = 0;
			for(ExtensionPoint extensionPoint : descriptor.getExtensionPoints()) {
				if(extensionPoint.getDeclaringPluginFragment()!=fragment) {
					continue;
				}
				count++;
				cache.setValue("id", getId(extensionPoint)); //$NON-NLS-1$
				String ancestry = NONE;
				if(extensionPoint.getParentExtensionPointId()!=null) {
					ancestry = ResourceManager.format(extPointAncestryText, 
							noneOrNonempty(extensionPoint.getParentExtensionPointId()),
							noneOrNonempty(extensionPoint.getParentPluginId()));
				}
				cache.setRawValue("ancestry", ancestry); //$NON-NLS-1$
				cache.setValue("multiplicity", noneOrNonempty(extensionPoint.getMultiplicity())); //$NON-NLS-1$
				cache.setRawValue("connectedExtensions", listIds(extensionPoint.getConnectedExtensions())); //$NON-NLS-1$
				Documentation<?> doc = extensionPoint.getDocumentation();
				if(doc==null) {
					cache.setValue("documentation", NONE); //$NON-NLS-1$
				} else {
					cache.setValue("documentation", noneOrNonempty(doc.getText())); //$NON-NLS-1$
				}
				cache.commit();
			}
			if(count==0) {
				fillCacheEmpty(cache, "id", "ancestry", "multiplicity", "connectedExtensions", "documentation"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
			}
		}
		
	}
	
	private static class DocumentableFiller implements TemplateFiller {

		/**
		 * @see de.ims.icarus.plugins.core.output.PluginElementPresenter.TemplateFiller#fillTemplate(de.ims.icarus.ui.view.Template, java.lang.Object, de.ims.icarus.util.Options)
		 */
		@Override
		public void fillTemplate(Template template, Object data, Options options) {
			Documentable<?> documentable = (Documentable<?>) data;
			fillDocumentable(template, documentable);
		}
		
	}
}
