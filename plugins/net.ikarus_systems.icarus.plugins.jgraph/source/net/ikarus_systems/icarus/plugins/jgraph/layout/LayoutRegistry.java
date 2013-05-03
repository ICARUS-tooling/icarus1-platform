/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.layout;

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

import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.jgraph.JGraphConstants;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.id.UnknownIdentifierException;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class LayoutRegistry {
	
	private static LayoutRegistry instance;
	
	private Map<String, Extension> layouts = new LinkedHashMap<>();
	
	private Map<ContentType, Collection<Extension>> cache;

	private LayoutRegistry() {
		loadLayouts();
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
	
	public int availableLayoutsCount() {
		return layouts.size();
	}
	
	public Collection<Extension> availableLayouts() {
		return Collections.unmodifiableCollection(layouts.values());
	}
	
	public Extension getLayout(String id) {
		Extension layout = layouts.get(id);
		if(layout==null)
			throw new UnknownIdentifierException("No such layout: "+id); //$NON-NLS-1$
		
		return layout;
	}
	
	private Collection<Extension> findCompatibleLayouts(ContentType type) {
		Collection<Extension> compatibleLayouts = new LinkedList<>();
		
		for(Extension extension : layouts.values()) {
			Collection<Extension.Parameter> params = extension.getParameters("contentType"); //$NON-NLS-1$
			if(params.isEmpty()) {
				compatibleLayouts.add(extension);
			} else {
				for(Extension.Parameter param : params) {
					ContentType compatibleType = ContentTypeRegistry.getInstance().getType(param.valueAsString());
					// Need only one compatible definition
					if(ContentTypeRegistry.isCompatible(type, compatibleType)) {
						compatibleLayouts.add(extension);
						break;
					}
				}
			}
		}
		
		return compatibleLayouts;
	}
	
	private Collection<Extension> getCachedLayouts(ContentType type) {
		if(cache==null) {
			cache = new HashMap<>();
		}
		
		Collection<Extension> compatibleLayouts = cache.get(type);
		if(compatibleLayouts==null) {
			compatibleLayouts = findCompatibleLayouts(type);
			cache.put(type, compatibleLayouts);
		}
		
		return compatibleLayouts;
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
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		
		List<ContentType> contentTypes = new ArrayList<>();
		contentTypes.add(type);
		if(includeCompatibleTypes) {
			contentTypes.addAll(ContentTypeRegistry.getInstance()
					.getCompatibleTypes(type));
		}
		
		Set<Extension> compatibleLayouts = new LinkedHashSet<>();
		
		for(ContentType contentType : contentTypes) {
			compatibleLayouts.addAll(getCachedLayouts(contentType));
		}
		
		return compatibleLayouts;
	}
}
