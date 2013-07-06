/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.coref.helper;

import de.ims.icarus.language.coref.Span;
import de.ims.icarus.util.Filter;

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
		 * @see de.ims.icarus.util.Filter#accepts(java.lang.Object)
		 */
		@Override
		public boolean accepts(Object obj) {
			return obj==null ? false : ((Span)obj).getClusterId()==clusterId;
		}
		
	}
	
	// TODO add more fitlers!
}
