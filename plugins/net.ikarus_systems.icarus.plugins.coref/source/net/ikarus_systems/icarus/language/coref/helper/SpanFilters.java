/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.coref.helper;

import net.ikarus_systems.icarus.language.coref.Span;
import net.ikarus_systems.icarus.util.Filter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class SpanFilters {

	private SpanFilters() {
		// no-op
	}

	public static class ClusterIdFilter implements Filter {
		
		private final int clusterId;
		
		public ClusterIdFilter(int clusterId) {
			this.clusterId = clusterId;
		}

		/**
		 * @see net.ikarus_systems.icarus.util.Filter#accepts(java.lang.Object)
		 */
		@Override
		public boolean accepts(Object obj) {
			return obj==null ? false : ((Span)obj).getClusterId()==clusterId;
		}
		
	}
	
	// TODO add more fitlers!
}
