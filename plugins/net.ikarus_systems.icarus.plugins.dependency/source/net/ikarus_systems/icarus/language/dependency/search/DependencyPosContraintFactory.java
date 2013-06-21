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
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyPosContraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "pos"; //$NON-NLS-1$

	public DependencyPosContraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.pos.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.pos.description"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, net.ikarus_systems.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, false))
			return new DependencyPosCIConstraint(value, operator);
		else
			return new DependencyPosConstraint(value, operator);
	}

	private static class DependencyPosConstraint extends DefaultConstraint {

		private static final long serialVersionUID = 18977116270797226L;

		public DependencyPosConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		protected Object prepareValue(Object value) {
			return ((DependencyTargetTree)value).getPos();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyPosConstraint(getValue(), getOperator());
		}
	}

	private static class DependencyPosCIConstraint extends DefaultConstraint {

		private static final long serialVersionUID = 4933479883479834272L;

		public DependencyPosCIConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		protected Object prepareValue(Object value) {
			return ((DependencyTargetTree)value).getPos().toLowerCase();
		}
	}
}
