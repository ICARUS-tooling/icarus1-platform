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

import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.search_tools.standard.AbstractConstraintFactory;
import net.ikarus_systems.icarus.search_tools.standard.DefaultCaseInsensitiveConstraint;
import net.ikarus_systems.icarus.search_tools.standard.DefaultConstraint;
import net.ikarus_systems.icarus.search_tools.standard.DefaultSearchOperator;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyFeaturesContraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "features"; //$NON-NLS-1$

	public DependencyFeaturesContraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.features.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.features.description"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, net.ikarus_systems.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, int flags) {
		if(isFlagSet(flags, IGNORE_CASE))
			return new DependencyFeaturesCIConstraint(value, operator);
		else
			return new DependencyFeaturesConstraint(value, operator);
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return new SearchOperator[] {
			DefaultSearchOperator.EQUALS,
			DefaultSearchOperator.EQUALS_NOT,
			DefaultSearchOperator.CONTAINS,
			DefaultSearchOperator.CONTAINS_NOT,
			DefaultSearchOperator.MATCHES,
			DefaultSearchOperator.MATCHES_NOT,
			DefaultSearchOperator.GROUPING,
		};
	}

	private static class DependencyFeaturesConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -3346450454270312183L;

		public DependencyFeaturesConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		protected Object prepareValue(Object value) {
			return ((DependencyTargetTree)value).getFeatures();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyFeaturesConstraint(getValue(), getOperator());
		}
	}

	private static class DependencyFeaturesCIConstraint extends DefaultCaseInsensitiveConstraint {

		private static final long serialVersionUID = -3346450454270312183L;

		public DependencyFeaturesCIConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		protected Object prepareValue(Object value) {
			return ((DependencyTargetTree)value).getFeatures().toLowerCase();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyFeaturesCIConstraint(getValue(), getOperator());
		}
	}
}