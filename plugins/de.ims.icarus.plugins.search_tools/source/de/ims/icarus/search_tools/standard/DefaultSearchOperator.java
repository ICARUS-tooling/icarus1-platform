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
package de.ims.icarus.search_tools.standard;

import java.util.regex.Matcher;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchOperator;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class DefaultSearchOperator extends SearchOperator {

	private static final long serialVersionUID = -710533898266463677L;

	private final String key;
	private final boolean supportsNumbers;

	private DefaultSearchOperator(String symbol, String key, boolean supportsNumbers) {
		super(symbol);
		this.key = key;
		this.supportsNumbers = supportsNumbers;
	}
	
	@Override
	public boolean supportsNumbers() { return supportsNumbers; }

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

	private static boolean equals0(Object value, Object constraint) {
		return value==null ? constraint==null : value.equals(constraint);
	}

	private static boolean contains0(Object value, Object constraint) {
		return value==null ? constraint==null : (value.toString()).contains(constraint.toString());
	}

	private static boolean matches0(Object value, Object constraint) {
		if(value==null) {
			return constraint==null;
		}

		Matcher matcher = SearchManager.getMatcher(constraint.toString(), value.toString());
		boolean result = matcher==null ? false : matcher.find();

		if(matcher!=null) {
			SearchManager.recycleMatcher(matcher);
		}

		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static int compare0(Object value, Object constraint) {
		return ((Comparable)value).compareTo((Comparable)constraint);
	}

	public static final SearchOperator GROUPING = new DefaultSearchOperator("<*>", "grouping", false) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -1321881172167842710L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return true;
		}

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#isGrouping()
		 */
		@Override
		public boolean isGrouping() {
			return true;
		}
	};

	public static final SearchOperator RANGE = new DefaultSearchOperator("<>", "range", false) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = 2640098895375772516L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return true;
		}
	};

	public static final SearchOperator EQUALS = new DefaultSearchOperator("=", "equals", true) {  //$NON-NLS-1$//$NON-NLS-2$

		private static final long serialVersionUID = -3692306391485959449L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return equals0(value, constraint);
		}
	};

	public static final SearchOperator EQUALS_NOT = new DefaultSearchOperator("!=", "equalsNot", true) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -4730832928170697565L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return !equals0(value, constraint);
		}
	};

	public static final SearchOperator MATCHES = new DefaultSearchOperator("~", "matches", false) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -548739311862178925L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return matches0(value, constraint);
		}
	};

	public static final SearchOperator MATCHES_NOT = new DefaultSearchOperator("!~", "matchesNot", false) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -370237882408639045L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return !matches0(value, constraint);
		}
	};

	public static final SearchOperator CONTAINS = new DefaultSearchOperator("#", "contains", false) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -8935758538857689576L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return contains0(value, constraint);
		}
	};

	public static final SearchOperator CONTAINS_NOT = new DefaultSearchOperator("!#", "containsNot", false) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = 2110261744483750112L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return !contains0(value, constraint);
		}
	};

	public static final SearchOperator LESS_THAN = new DefaultSearchOperator("<", "lessThan", true) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -8353909321259706543L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return compare0(value, constraint)<0;
		}
	};

	public static final SearchOperator LESS_OR_EQUAL = new DefaultSearchOperator("<=", "lessOrEqual", true) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = 6982415206383632031L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return compare0(value, constraint)<=0;
		}
	};

	public static final SearchOperator GREATER_THAN = new DefaultSearchOperator(">", "greaterThan", true) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -3748593349088379755L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return compare0(value, constraint)>0;
		}
	};

	public static final SearchOperator GREATER_OR_EQUAL = new DefaultSearchOperator(">=", "greaterOrEqual", true) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = 5164052048370243973L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return compare0(value, constraint)>=0;
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

	private static final SearchOperator[] operatorsNoGrouping = {
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
	};

	private static final SearchOperator[] numericalOperators = {
		EQUALS,
		EQUALS_NOT,
		LESS_THAN,
		LESS_OR_EQUAL,
		GREATER_THAN,
		GREATER_OR_EQUAL,
		GROUPING,
	};

	private static final SearchOperator[] booleanOperators = {
		EQUALS,
		EQUALS_NOT,
		GROUPING,
	};

	private static final SearchOperator[] comparingOperators = {
		LESS_THAN,
		LESS_OR_EQUAL,
		GREATER_THAN,
		GREATER_OR_EQUAL,
	};

	private static final SearchOperator[] numericalOperatorsNoGrouping = {
		EQUALS,
		EQUALS_NOT,
		LESS_THAN,
		LESS_OR_EQUAL,
		GREATER_THAN,
		GREATER_OR_EQUAL,
	};

	public static SearchOperator[] values() {
		return operators.clone();
	}

	public static SearchOperator[] valuesNoGrouping() {
		return operatorsNoGrouping.clone();
	}

	public static SearchOperator[] numerical() {
		return numericalOperators.clone();
	}

	public static SearchOperator[] binary() {
		return booleanOperators.clone();
	}

	public static SearchOperator[] comparing() {
		return comparingOperators.clone();
	}

	public static SearchOperator[] numericalNoGrouping() {
		return numericalOperatorsNoGrouping.clone();
	}

	public static SearchOperator[] range() {
		return new SearchOperator[]{RANGE};
	}
}
