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

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyProjectivityConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "projectivity"; //$NON-NLS-1$

	public DependencyProjectivityConstraintFactory() {
		super(TOKEN, EDGE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.projectivity.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.projectivity.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Options options) {
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
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).isFlagSet(LanguageUtils.FLAG_PROJECTIVE);
		}

		@Override
		protected Object getConstraint() {
			return getValue().equals(LanguageConstants.DATA_YES_VALUE);
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyProjectivityConstraint(getValue(), getOperator());
		}
	}
}
