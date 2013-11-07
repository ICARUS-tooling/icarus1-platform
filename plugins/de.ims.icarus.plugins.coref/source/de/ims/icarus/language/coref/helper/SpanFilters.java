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
package de.ims.icarus.language.coref.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.ims.icarus.language.coref.Span;
import de.ims.icarus.util.CollectionUtils;
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
	
	public static class SpanFilter implements Filter {
		
		private final Set<Span> spans;
		
		public SpanFilter(Span...spans) {
			this.spans = CollectionUtils.asSet(spans);
		}
		
		
		public SpanFilter(Collection<Span> spans) {
			if(spans==null)
				throw new NullPointerException("Invalid spans"); //$NON-NLS-1$
			
			this.spans = new HashSet<>(spans);
		}


		/**
		 * @see de.ims.icarus.util.Filter#accepts(java.lang.Object)
		 */
		@Override
		public boolean accepts(Object obj) {
			return spans==null ? true : spans.contains(obj);
		}


		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return spans==null ? 0 : spans.hashCode();
		}


		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof SpanFilter) {
				return CollectionUtils.equals(spans, ((SpanFilter)obj).spans);
			}
			return false;
		}


		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "{SpanFilter: "+spans.toString()+"}"; //$NON-NLS-1$ //$NON-NLS-2$
		}
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

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return clusterId;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ClusterIdFilter) {
				return clusterId==((ClusterIdFilter)obj).clusterId;
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "{Cluster-Id Filter: "+clusterId+"}"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
	}
	
	// TODO add more fitlers!
}
