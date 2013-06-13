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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyProjectivityContraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "projectivity"; //$NON-NLS-1$

	public DependencyProjectivityContraintFactory() {
		super(TOKEN, EDGE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.projectivity.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.projectivity.description"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, net.ikarus_systems.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, int flags) {
		return new DependencyProjectivityConstraint(value, operator);
	}

	@Override
	public Class<?> getValueClass() {
		return null;
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return new SearchOperator[]{
				DefaultSearchOperator.EQUALS,
				DefaultSearchOperator.GROUPING,
		};
	}

	@Override
	public Object getDefaultValue() {
		return LanguageUtils.DATA_UNDEFINED_VALUE;
	}

	@Override
	public Object labelToValue(Object label) {
		return LanguageUtils.parseBooleanLabel((String)label);
	}

	@Override
	public Object valueToLabel(Object value) {
		return LanguageUtils.getBooleanLabel((int)value);
	}

	@Override
	public Object[] getLabelSet() {
		return new Object[]{
				LanguageUtils.DATA_UNDEFINED_LABEL,
				LanguageUtils.getBooleanLabel(LanguageUtils.DATA_YES_VALUE),
				LanguageUtils.getBooleanLabel(LanguageUtils.DATA_NO_VALUE),
		};
	}

	private static class DependencyProjectivityConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -8096178398923755732L;

		public DependencyProjectivityConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		protected Object prepareValue(Object value) {
			return ((DependencyTargetTree)value).isFlagSet(LanguageUtils.FLAG_PROJECTIVE);
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyProjectivityConstraint(getValue(), getOperator());
		}
	}
}
