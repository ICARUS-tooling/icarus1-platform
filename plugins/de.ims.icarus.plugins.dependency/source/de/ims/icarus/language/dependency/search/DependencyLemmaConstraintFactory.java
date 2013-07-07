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
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyLemmaConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "lemma"; //$NON-NLS-1$

	public DependencyLemmaConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.lemma.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.lemma.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new DependencyLemmaConstraint(value, operator);
		else
			return new DependencyLemmaCIConstraint(value, operator);
	}

	private static class DependencyLemmaConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -2816057046153547371L;

		public DependencyLemmaConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
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
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).getLemma().toLowerCase();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyLemmaCIConstraint(getValue(), getOperator());
		}
	}
}