/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core;

import java.util.Collection;
import java.util.logging.Level;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class ViewFilter {
	
	/**
	 * Returns {@code true} if the supplied {@code View} instance
	 * should be included. Note that only the {@code extension}
	 * parameter is guaranteed to be {@code non-null}! Only if
	 * the {@code View} defined by the given {@code Extension} has
	 * already been activated the second parameter {@code view} will be
	 * set as well. This allows for the filtering of all connected view
	 * extensions on a {@code Perspective} and if a filter allows a
	 * not yet activated view to be included it will be activated.
	 */
	public abstract boolean filter(Extension extension, View view);
	
	public static final ViewFilter emptyFilter = new ViewFilter() {
		
		@Override
		public boolean filter(Extension extension, View view) {
			return true;
		}
	};

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class ViewIdFilter extends ViewFilter {
		
		private final String id;

		/**
		 * @param id
		 */
		public ViewIdFilter(String id) {
			if(id==null)
				throw new IllegalArgumentException("Invalid id"); //$NON-NLS-1$
			
			this.id = id;
		}

		/**
		 * @see net.ikarus_systems.icarus.plugins.core.ViewFilter#filter(net.ikarus_systems.icarus.plugins.core.View)
		 */
		@Override
		public boolean filter(Extension extension, View view) {
			return id.equals(extension.getId());
		}		
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class ViewCategoryFilter extends ViewFilter {
		
		private final String category;

		/**
		 * @param category
		 */
		public ViewCategoryFilter(String category) {
			if(category==null)
				throw new IllegalArgumentException("Invalid category"); //$NON-NLS-1$
			
			this.category = category;
		}

		/**
		 * @see net.ikarus_systems.icarus.plugins.core.ViewFilter#filter(net.ikarus_systems.icarus.plugins.core.View)
		 */
		@Override
		public boolean filter(Extension extension, View view) {
			Collection<Extension.Parameter> params = extension.getParameters("category"); //$NON-NLS-1$
			for(Extension.Parameter param : params) {
				if(category.equals(param.valueAsString())) {
					return true;
				}
			}
			
			return false;
		}		
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class ViewClassFilter extends ViewFilter {
		
		private final Class<?> clazz;
		private final boolean useEquals;

		public ViewClassFilter(Class<?> clazz) {
			this(clazz, false);
		}

		public ViewClassFilter(Class<?> clazz, boolean useEquals) {
			if(clazz==null)
				throw new IllegalArgumentException("Invalid class"); //$NON-NLS-1$
			
			this.clazz = clazz;
			this.useEquals = useEquals;
		}

		/**
		 * @see net.ikarus_systems.icarus.plugins.core.ViewFilter#filter(net.ikarus_systems.icarus.plugins.core.View)
		 */
		@Override
		public boolean filter(Extension extension, View view) {
			Class<?> viewClazz = null;
			
			// Directly access the view's class if possible
			if(view!=null) {
				viewClazz = view.getClass();
			}
			// Supplied view was null, so we have to access the class via
			// the extension.
			if(viewClazz==null) {
				try {
					String className = extension.getParameter("class").valueAsString(); //$NON-NLS-1$
					ClassLoader loader = PluginUtil.getPluginManager().getPluginClassLoader(
							extension.getDeclaringPluginDescriptor());
					viewClazz = loader.loadClass(className);
				} catch(Exception e) {
					LoggerFactory.getLogger(ViewFilter.class).log(LoggerFactory.record(Level.SEVERE, 
							"Unable to load extension view class: "+extension, e)); //$NON-NLS-1$
				}
			}
			
			if(useEquals) {
				return clazz.equals(viewClazz);
			} else {
				return clazz.isAssignableFrom(viewClazz);
			}
		}		
	}
}
