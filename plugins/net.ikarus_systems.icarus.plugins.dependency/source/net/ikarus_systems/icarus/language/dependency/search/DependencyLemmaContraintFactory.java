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
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyLemmaContraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "lemma"; //$NON-NLS-1$

	public DependencyLemmaContraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.lemma.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.lemma.description"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, net.ikarus_systems.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, false))
			return new DependencyLemmaCIConstraint(value, operator);
		else
			return new DependencyLemmaConstraint(value, operator);
	}

	private static class DependencyLemmaConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -2816057046153547371L;

		public DependencyLemmaConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		protected Object prepareValue(Object value) {
			return ((DependencyTargetTree)value).getLemma();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyLemmaConstraint(getValue(), getOperator());
		}
	}

	private static class DependencyLemmaCIConstraint extends DefaultCaseInsensitiveConstraint {

		private static final long serialVersionUID = -8582367322352411091L;

		public DependencyLemmaCIConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		protected Object prepareValue(Object value) {
			return ((DependencyTargetTree)value).getLemma().toLowerCase();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyLemmaCIConstraint(getValue(), getOperator());
		}
	}
}
