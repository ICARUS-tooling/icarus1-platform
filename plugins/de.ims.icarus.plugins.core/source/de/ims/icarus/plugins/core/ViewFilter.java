/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.core;

import java.util.Collection;
import java.util.logging.Level;

import org.java.plugin.registry.Extension;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.Capability;

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
	
	/**
	 * Empty default filter, accepts all {@code View} instances.
	 * This implementation always returns {@code true}.
	 */
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
		 * @see de.ims.icarus.plugins.core.ViewFilter#filter(de.ims.icarus.plugins.core.View)
		 */
		@Override
		public boolean filter(Extension extension, View view) {
			return id.equals(extension.getId()) || id.equals(extension.getUniqueId());
		}		
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class ViewCapabilityFilter extends ViewFilter {
		
		private final Capability[] capabilities;
		private final boolean generalize;
		
		public ViewCapabilityFilter(Capability...capabilities) {
			if(capabilities==null || capabilities.length==0)
				throw new IllegalArgumentException("Invalid capabilities list"); //$NON-NLS-1$
			
			this.capabilities = capabilities;
			this.generalize = false;
		}
		
		public ViewCapabilityFilter(Capability capability, boolean generalize) {
			if(capability==null)
				throw new IllegalArgumentException("Invalid capability"); //$NON-NLS-1$
			
			this.capabilities = new Capability[]{ capability };
			this.generalize = generalize;
		}

		/**
		 * @see de.ims.icarus.plugins.core.ViewFilter#filter(org.java.plugin.registry.Extension, de.ims.icarus.plugins.core.View)
		 */
		@Override
		public boolean filter(Extension extension, View view) {
			if(capabilities.length==1) {
				return PluginUtil.hasCapability(extension, capabilities[0], generalize);
			} else {
				return PluginUtil.hasCapability(extension, capabilities);
			}
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
		 * @see de.ims.icarus.plugins.core.ViewFilter#filter(de.ims.icarus.plugins.core.View)
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
		 * @see de.ims.icarus.plugins.core.ViewFilter#filter(de.ims.icarus.plugins.core.View)
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
					viewClazz = PluginUtil.loadClass(extension);
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Unable to load extension view class: "+extension, e); //$NON-NLS-1$
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
