/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.dependency.search;

import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.search_tools.standard.AbstractConstraintFactory;
import net.ikarus_systems.icarus.search_tools.standard.DefaultConstraint;
import net.ikarus_systems.icarus.search_tools.standard.DefaultSearchOperator;
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyDistanceContraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "distance"; //$NON-NLS-1$

	public DependencyDistanceContraintFactory() {
		super(TOKEN, EDGE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.distance.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.distance.description"); //$NON-NLS-1$
	}

	/**
	 * 
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, net.ikarus_systems.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Options options) {
		return new DependencyDistanceConstraint(value, operator);
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return new SearchOperator[] {
			DefaultSearchOperator.EQUALS,
			DefaultSearchOperator.EQUALS_NOT,
			DefaultSearchOperator.LESS_THAN,
			DefaultSearchOperator.LESS_OR_EQUAL,
			DefaultSearchOperator.GREATER_THAN,
			DefaultSearchOperator.GREATER_OR_EQUAL,
			DefaultSearchOperator.GROUPING,
		};
	}

	@Override
	public Class<?> getValueClass() {
		return Integer.class;
	}

	@Override
	public Object getDefaultValue() {
		return LanguageUtils.DATA_UNDEFINED_VALUE;
	}

	@Override
	public Object labelToValue(Object label) {
		return LanguageUtils.parseLabel((String) label);
	}

	@Override
	public Object valueToLabel(Object value) {
		return LanguageUtils.getLabel((int)value);
	}

	private static class DependencyDistanceConstraint extends DefaultConstraint {

		private static final long serialVersionUID = 4020431284510729498L;

		public DependencyDistanceConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		protected Object prepareValue(Object value) {
			return ((DependencyTargetTree)value).getDistance();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyDistanceConstraint(getValue(), getOperator());
		}
	}
}
