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
import net.ikarus_systems.icarus.search_tools.standard.DefaultConstraint;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyFeaturesContraintFactory extends AbstractConstraintFactory {

	public static final String ID = "dependency_features"; //$NON-NLS-1$

	public DependencyFeaturesContraintFactory() {
		super(ID, NODE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.features.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.features.description"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, net.ikarus_systems.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator) {
		return new DependencyFeaturesConstraint(value, operator);
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return new SearchOperator[] {
			SearchOperator.EQUALS,
			SearchOperator.EQUALS_NOT,
			SearchOperator.CONTAINS,
			SearchOperator.CONTAINS_NOT,
			SearchOperator.MATCHES,
			SearchOperator.MATCHES_NOT,
			SearchOperator.GROUPING,
		};
	}

	private static class DependencyFeaturesConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -3346450454270312183L;

		public DependencyFeaturesConstraint(Object value, SearchOperator operator) {
			super(ID, value, operator);
		}

		@Override
		public boolean matches(Object value) {
			value = ((DependencyTargetTree)value).getFeatures();
			return super.matches(value);
		}
	}
}
