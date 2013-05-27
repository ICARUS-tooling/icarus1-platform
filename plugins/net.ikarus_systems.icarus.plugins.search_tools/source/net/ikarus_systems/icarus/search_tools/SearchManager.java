/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.search_tools.SearchToolsConstants;
import net.ikarus_systems.icarus.util.data.ContentType;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class SearchManager {
	
	public static final Comparator<ConstraintFactory> FACTORY_SORTER = new Comparator<ConstraintFactory>() {

		@Override
		public int compare(ConstraintFactory o1, ConstraintFactory o2) {
			return o1.getToken().compareTo(o2.getToken());
		}
		
	};
	
	private Map<ContentType, ConstraintContext> contexts;
	
	// Maps strings to their compiled Pattern instance.
	// We use a weak hash map here since we only need the Pattern
	// as long as the respective string is used in some constraint
	private static Map<String, Pattern> patternCache = Collections.synchronizedMap(
			new WeakHashMap<String, Pattern>());
	
	private static SearchManager instance;
	
	public static SearchManager getInstance() {
		if(instance==null) {
			synchronized (SearchManager.class) {
				if(instance==null) {
					instance = new SearchManager();
				}
			}
		}
		
		return instance;
	}

	private SearchManager() {
		// no-op
	}
	
	public synchronized ConstraintContext getConstraintContext(ContentType contentType) {
		if(contentType==null)
			throw new IllegalArgumentException("Invalid content-type"); //$NON-NLS-1$
		
		if(contexts==null) {
			contexts = new HashMap<>();
		}
		
		ConstraintContext context = contexts.get(contentType);
		if(context==null) {
			context = new ConstraintContext(contentType);
			contexts.put(contentType, context);
		}
		
		return context;
	}

	public static Pattern getPattern(String s) {
		if(s==null || s.isEmpty()) {
			return null;
		}
		
		Pattern pattern = patternCache.get(s);
		if(pattern==null) {
			// Do not catch PatternSyntaxException!
			// We want whatever operation the pattern request was originated 
			// from to be terminated by the exception.
			pattern = Pattern.compile(s);
			
			// Do not bother with 'duplicates' since all Pattern
			// compiled from the same string are in fact identical in
			// terms of functionality
			patternCache.put(s, pattern);
		}
		
		return pattern;
	}
		
	public static ConstraintFactory[] getEdgeConstraintFactories(ConstraintFactory[] items) {
		if(items==null)
			throw new IllegalArgumentException("Invalid items"); //$NON-NLS-1$
		
		List<ConstraintFactory> result = new ArrayList<>();
		
		for(ConstraintFactory factory : items) {
			if(factory.getConstraintType()==ConstraintFactory.EDGE_CONSTRAINT_TYPE) {
				result.add(factory);
			}
		}
		
		return result.toArray(new ConstraintFactory[0]);
	}
		
	public static ConstraintFactory[] getNodeConstraintFactories(ConstraintFactory[] items) {
		if(items==null)
			throw new IllegalArgumentException("Invalid items"); //$NON-NLS-1$
		
		List<ConstraintFactory> result = new ArrayList<>();
		
		for(ConstraintFactory factory : items) {
			if(factory.getConstraintType()==ConstraintFactory.NODE_CONSTRAINT_TYPE) {
				result.add(factory);
			}
		}
		
		return result.toArray(new ConstraintFactory[0]);
	}
	
	public static Collection<Extension> getSearchFactoryExtensions() {
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				SearchToolsConstants.SEARCH_TOOLS_PLUGIN_ID, "SearchFactory"); //$NON-NLS-1$
		return extensionPoint.getConnectedExtensions();
	}
	
	public static List<Extension> getResultPresenterExtensions(int dimension) {
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				SearchToolsConstants.SEARCH_TOOLS_PLUGIN_ID, "SearchResultPresenter"); //$NON-NLS-1$
		
		List<Extension> result = new ArrayList<>();
		
		for(Extension extension : extensionPoint.getConnectedExtensions()) {
			if(extension.getParameter("dimension").valueAsNumber().intValue()==dimension) { //$NON-NLS-1$
				result.add(extension);
			}
		}
		
		return result;
	}

	public void executeSearch(Search search) {
		
	}
}
