/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.dependency.search;

import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultCaseInsensitiveConstraint;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyFeaturesConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "features"; //$NON-NLS-1$

	public DependencyFeaturesConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.features.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.features.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new DependencyFeaturesConstraint(value, operator);
		else
			return new DependencyFeaturesCIConstraint(value, operator);
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
		public Object getInstance(Object value) {
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
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).getFeatures().toLowerCase();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyFeaturesCIConstraint(getValue(), getOperator());
		}
	}
}
