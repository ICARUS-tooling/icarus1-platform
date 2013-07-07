/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.standard;

import java.util.regex.Pattern;

import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.SearchOperator;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class DefaultSearchOperator extends SearchOperator {
	
	private static final long serialVersionUID = -710533898266463677L;

	private String key;
	
	private DefaultSearchOperator(String symbol, String key) {
		super(symbol);
		this.key = key;
	}
	
	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.operator."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.operator."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
		
	}
	
	private static boolean equals(Object value, Object constraint) {
		return value.equals(constraint);
	}
	
	private static boolean contains(Object value, Object constraint) {
		return ((String)value).contains((String)constraint);
	}
	
	private static boolean matches(Object value, Object constraint) {
		Pattern pattern = SearchManager.getPattern((String)constraint);
		return pattern==null ? false : pattern.matcher((String)value).find();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static int compare(Object value, Object constraint) {
		return ((Comparable)value).compareTo((Comparable)constraint);
	}
	
	public static final SearchOperator GROUPING = new DefaultSearchOperator("<*>", "grouping") { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -1321881172167842710L;

		/**
		 * @see net.ikarus_systems.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return true;
		}		
	};
	
	public static final SearchOperator EQUALS = new DefaultSearchOperator("=", "equals") {  //$NON-NLS-1$//$NON-NLS-2$
	
		private static final long serialVersionUID = -3692306391485959449L;

		/**
		 * @see net.ikarus_systems.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return equals(value, constraint);
		}		
	};
	
	public static final SearchOperator EQUALS_NOT = new DefaultSearchOperator("!=", "equalsNot") { //$NON-NLS-1$ //$NON-NLS-2$
	
		private static final long serialVersionUID = -4730832928170697565L;

		/**
		 * @see net.ikarus_systems.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return !equals(value, constraint);
		}		
	};
	
	public static final SearchOperator MATCHES = new DefaultSearchOperator("~", "matches") { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -548739311862178925L;

		/**
		 * @see net.ikarus_systems.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return matches(value, constraint);
		}		
	};
	
	public static final SearchOperator MATCHES_NOT = new DefaultSearchOperator("!~", "matchesNot") { //$NON-NLS-1$ //$NON-NLS-2$
	
		private static final long serialVersionUID = -370237882408639045L;

		/**
		 * @see net.ikarus_systems.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return !matches(value, constraint);
		}		
	};
	
	public static final SearchOperator CONTAINS = new DefaultSearchOperator("#", "contains") { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -8935758538857689576L;

		/**
		 * @see net.ikarus_systems.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return contains(value, constraint);
		}		
	};
	
	public static final SearchOperator CONTAINS_NOT = new DefaultSearchOperator("!#", "containsNot") { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = 2110261744483750112L;

		/**
		 * @see net.ikarus_systems.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return !contains(value, constraint);
		}		
	};
	
	public static final SearchOperator LESS_THAN = new DefaultSearchOperator("<", "lessThan") { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -8353909321259706543L;

		/**
		 * @see net.ikarus_systems.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return compare(value, constraint)<0;
		}		
	};
	
	public static final SearchOperator LESS_OR_EQUAL = new DefaultSearchOperator("<=", "lessOrEqual") { //$NON-NLS-1$ //$NON-NLS-2$
	
		private static final long serialVersionUID = 6982415206383632031L;

		/**
		 * @see net.ikarus_systems.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return compare(value, constraint)<=0;
		}		
	};
	
	public static final SearchOperator GREATER_THAN = new DefaultSearchOperator(">", "greaterThan") { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -3748593349088379755L;

		/**
		 * @see net.ikarus_systems.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return compare(value, constraint)>0;
		}		
	};
	
	public static final SearchOperator GREATER_OR_EQUAL = new DefaultSearchOperator(">=", "greaterOrEqual") { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = 5164052048370243973L;

		/**
		 * @see net.ikarus_systems.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return compare(value, constraint)>=0;
		}		
	};
	
	private static final SearchOperator[] operators = {
		EQUALS,
		EQUALS_NOT,
		MATCHES,
		MATCHES_NOT,
		CONTAINS,
		CONTAINS_NOT,
		LESS_THAN,
		LESS_OR_EQUAL,
		GREATER_THAN,
		GREATER_OR_EQUAL,
		GROUPING,
	};
	
	public static SearchOperator[] values() {
		return operators.clone();
	}
}
