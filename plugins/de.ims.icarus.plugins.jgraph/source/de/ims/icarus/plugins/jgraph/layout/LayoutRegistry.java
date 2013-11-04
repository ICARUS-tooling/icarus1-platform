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
package de.ims.icarus.plugins.jgraph.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.jgraph.JGraphConstants;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.id.UnknownIdentifierException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class LayoutRegistry {
	
	private static LayoutRegistry instance;
	
	private Map<String, Extension> layouts = new LinkedHashMap<>();	
	private Map<ContentType, Collection<Extension>> layoutCache;
	
	private Map<String, Extension> styles = new LinkedHashMap<>();	
	private Map<ContentType, Collection<Extension>> styleCache;
	
	private Map<String, Extension> renderers = new LinkedHashMap<>();	
	private Map<ContentType, Collection<Extension>> rendererCache;

	private LayoutRegistry() {
		loadLayouts();
		loadStyles();
		loadRenderers();
	}

	public static LayoutRegistry getInstance() {
		if(instance==null) {
			synchronized (LayoutRegistry.class) {
				if(instance==null) {
					instance = new LayoutRegistry();
				}
			}
		}
		
		return instance;
	}
	
	private void loadLayouts() {
		PluginDescriptor descriptor = PluginUtil.getPluginRegistry()
				.getPluginDescriptor(JGraphConstants.JGRAPH_PLUGIN_ID);
		for(Extension extension : descriptor.getExtensionPoint("GraphLayout").getConnectedExtensions()) { //$NON-NLS-1$
			layouts.put(extension.getId(), extension);
		}
	}
	
	private void loadStyles() {
		PluginDescriptor descriptor = PluginUtil.getPluginRegistry()
				.getPluginDescriptor(JGraphConstants.JGRAPH_PLUGIN_ID);
		for(Extension extension : descriptor.getExtensionPoint("GraphStyle").getConnectedExtensions()) { //$NON-NLS-1$
			styles.put(extension.getId(), extension);
		}
	}
	
	private void loadRenderers() {
		PluginDescriptor descriptor = PluginUtil.getPluginRegistry()
				.getPluginDescriptor(JGraphConstants.JGRAPH_PLUGIN_ID);
		for(Extension extension : descriptor.getExtensionPoint("GraphRenderer").getConnectedExtensions()) { //$NON-NLS-1$
			renderers.put(extension.getId(), extension);
		}
	}
	
	public int availableLayoutsCount() {
		return layouts.size();
	}
	
	public Collection<Extension> availableLayouts() {
		return Collections.unmodifiableCollection(layouts.values());
	}
	
	public int availableStylesCount() {
		return styles.size();
	}
	
	public Collection<Extension> availableStyles() {
		return Collections.unmodifiableCollection(styles.values());
	}
	
	public int availableRenderersCount() {
		return renderers.size();
	}
	
	public Collection<Extension> availableRenderers() {
		return Collections.unmodifiableCollection(renderers.values());
	}
	
	public Extension getLayout(String id) {
		Extension layout = layouts.get(id);
		if(layout==null)
			throw new UnknownIdentifierException("No such layout: "+id); //$NON-NLS-1$
		
		return layout;
	}
	
	public Extension getStyle(String id) {
		Extension style = styles.get(id);
		if(style==null)
			throw new UnknownIdentifierException("No such style: "+id); //$NON-NLS-1$
		
		return style;
	}
	
	public Extension getRenderer(String id) {
		Extension renderer = renderers.get(id);
		if(renderer==null)
			throw new UnknownIdentifierException("No such renderer: "+id); //$NON-NLS-1$
		
		return renderer;
	}
	
	private Collection<Extension> findCompatible(ContentType type, Collection<Extension> available) {
		Collection<Extension> compatible = new LinkedList<>();
		
		for(Extension extension : available) {
			Collection<Extension.Parameter> params = extension.getParameters("contentType"); //$NON-NLS-1$
			if(params.isEmpty()) {
				compatible.add(extension);
			} else {
				for(Extension.Parameter param : params) {
					ContentType compatibleType = ContentTypeRegistry.getInstance().getType(param.valueAsString());
					// Need only one compatible definition
					if(ContentTypeRegistry.isCompatible(type, compatibleType)) {
						compatible.add(extension);
						break;
					}
				}
			}
		}
		
		return compatible;
	}
	
	private Collection<Extension> getCached(ContentType type,
			Map<ContentType, Collection<Extension>> cache,
			Collection<Extension> available) {
		
		Collection<Extension> compatible = cache.get(type);
		if(compatible==null) {
			compatible = findCompatible(type, available);
			cache.put(type, compatible);
		}
		
		return compatible;
	}
	
	/**
	 * Searches the collection of registered {@code GraphLayout} extensions
	 * for such that either declare the given {@code ContentType} as compatible
	 * or are considered "general" layouts that do not rely on content types.
	 * If the {@code includeCompatibleTypes} parameter is {@code true} then
	 * all content types that are compatible to {@code type} as specified by
	 * {@link ContentTypeRegistry#isCompatible(ContentType, ContentType)} are used
	 * to check for compatibility when searching layouts.
	 */
	// TODO validate cost of repeatedly performed searches?
	public Collection<Extension> getCompatibleLayouts(ContentType type, boolean includeCompatibleTypes) {
		if(type==null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$
		
		List<ContentType> contentTypes = new ArrayList<>();
		contentTypes.add(type);
		if(includeCompatibleTypes) {
			contentTypes.addAll(ContentTypeRegistry.getInstance()
					.getCompatibleTypes(type));
		}
		
		Set<Extension> compatibleLayouts = new LinkedHashSet<>();
		if(layoutCache==null) {
			layoutCache = new HashMap<>();
		}
		
		for(ContentType contentType : contentTypes) {
			compatibleLayouts.addAll(getCached(
					contentType, layoutCache, layouts.values()));
		}
		
		return compatibleLayouts;
	}
	
	/**
	 * Searches the collection of registered {@code GraphStyle} extensions
	 * for such that either declare the given {@code ContentType} as compatible
	 * or are considered "general" styles that do not rely on content types.
	 * If the {@code includeCompatibleTypes} parameter is {@code true} then
	 * all content types that are compatible to {@code type} as specified by
	 * {@link ContentTypeRegistry#isCompatible(ContentType, ContentType)} are used
	 * to check for compatibility when searching styles.
	 */
	// TODO validate cost of repeatedly performed searches?
	public Collection<Extension> getCompatibleStyles(ContentType type, boolean includeCompatibleTypes) {
		if(type==null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$
		
		List<ContentType> contentTypes = new ArrayList<>();
		contentTypes.add(type);
		if(includeCompatibleTypes) {
			contentTypes.addAll(ContentTypeRegistry.getInstance()
					.getCompatibleTypes(type));
		}
		
		Set<Extension> compatibleStyles = new LinkedHashSet<>();
		if(styleCache==null) {
			styleCache = new HashMap<>();
		}
		
		for(ContentType contentType : contentTypes) {
			compatibleStyles.addAll(getCached(
					contentType, styleCache, styles.values()));
		}
		
		return compatibleStyles;
	}
	
	/**
	 * Searches the collection of registered {@code GraphRenderer} extensions
	 * for such that either declare the given {@code ContentType} as compatible
	 * or are considered "general" renderers that do not rely on content types.
	 * If the {@code includeCompatibleTypes} parameter is {@code true} then
	 * all content types that are compatible to {@code type} as specified by
	 * {@link ContentTypeRegistry#isCompatible(ContentType, ContentType)} are used
	 * to check for compatibility when searching renderers.
	 */
	// TODO validate cost of repeatedly performed searches?
	public Collection<Extension> getCompatibleRenderers(ContentType type, boolean includeCompatibleTypes) {
		if(type==null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$
		
		List<ContentType> contentTypes = new ArrayList<>();
		contentTypes.add(type);
		if(includeCompatibleTypes) {
			contentTypes.addAll(ContentTypeRegistry.getInstance()
					.getCompatibleTypes(type));
		}
		
		Set<Extension> compatibleRenderers = new LinkedHashSet<>();
		if(rendererCache==null) {
			rendererCache = new HashMap<>();
		}
		
		for(ContentType contentType : contentTypes) {
			compatibleRenderers.addAll(getCached(
					contentType, rendererCache, renderers.values()));
		}
		
		return compatibleRenderers;
	}
}
