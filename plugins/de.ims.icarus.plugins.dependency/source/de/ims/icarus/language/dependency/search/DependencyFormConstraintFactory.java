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
public class DependencyFormConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "form"; //$NON-NLS-1$

	public DependencyFormConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.form.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.form.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new DependencyFormConstraint(value, operator);
		else
			return new DependencyFormCIConstraint(value, operator);
	}

	private static class DependencyFormConstraint extends DefaultConstraint {

		private static final long serialVersionUID = 2843300705315175039L;

		public DependencyFormConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).getForm();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyFormConstraint(getValue(), getOperator());
		}
	}

	private static class DependencyFormCIConstraint extends DefaultCaseInsensitiveConstraint {

		private static final long serialVersionUID = -7737708296328734303L;

		public DependencyFormCIConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).getForm().toLowerCase();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyFormCIConstraint(getValue(), getOperator());
		}
	}
}
