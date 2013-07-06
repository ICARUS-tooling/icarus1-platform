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
public class DependencyPosConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "pos"; //$NON-NLS-1$

	public DependencyPosConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.pos.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.pos.description"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, net.ikarus_systems.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new DependencyPosConstraint(value, operator);
		else
			return new DependencyPosCIConstraint(value, operator);
	}

	private static class DependencyPosConstraint extends DefaultConstraint {

		private static final long serialVersionUID = 18977116270797226L;

		public DependencyPosConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).getPos();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyPosConstraint(getValue(), getOperator());
		}
	}

	private static class DependencyPosCIConstraint extends DefaultCaseInsensitiveConstraint {

		private static final long serialVersionUID = 4933479883479834272L;

		public DependencyPosCIConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).getPos().toLowerCase();
		}

		@Override
		public DependencyPosCIConstraint clone() {
			return new DependencyPosCIConstraint(getValue(), getOperator());
		}
	}
}
