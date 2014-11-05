/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision: 269 $
 * $Date: 2014-07-08 00:09:53 +0200 (Di, 08 Jul 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.dependency/source/de/ims/icarus/language/dependency/search/constraints/DependencyRelationConstraintFactory.java $
 *
 * $LastChangedDate: 2014-07-08 00:09:53 +0200 (Di, 08 Jul 2014) $
 * $LastChangedRevision: 269 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.prosody.search.constraints.painte;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.plugins.prosody.search.constraints.SyllableConstraint;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: DependencyRelationConstraintFactory.java 269 2014-07-07 22:09:53Z mcgaerty $
 *
 */
public class PaIntERangeConstraintFactory extends AbstractConstraintFactory implements ProsodyConstants {

	public static final String TOKEN = "painteRange"; //$NON-NLS-1$

	public PaIntERangeConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.painteRange.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.painteRange.description"); //$NON-NLS-1$
	}

	@Override
	public Object[] getSupportedSpecifiers() {
		return DEFAULT_UNDEFINED_VALUESET;
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return DefaultSearchOperator.range();
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new ProsodyPaIntERangeConstraint(value, operator, specifier);
	}

	private static class ProsodyPaIntERangeConstraint extends DefaultConstraint implements SyllableConstraint {

		private static final long serialVersionUID = 7309790344228778387L;

		// lower <> upper
		private transient PaIntEConstraintParams fromParams, toParams;
		private final transient PaIntEConstraintParams targetParams = new PaIntEConstraintParams();

		public ProsodyPaIntERangeConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		@Override
		public SearchConstraint clone() {
			return new ProsodyPaIntERangeConstraint(getValue(), getOperator(), getSpecifier());
		}

		@Override
		public void setSpecifier(Object specifier) {
			super.setSpecifier(specifier);

			String s = (String)specifier;
			if(s!=null && !LanguageConstants.DATA_UNDEFINED_LABEL.equals(s)) {
				if(fromParams==null) {
					fromParams = new PaIntEConstraintParams();
				}

				BoundedSyllableConstraint.parseParams(s, fromParams);
			}
		}

		@Override
		public void setValue(Object specifier) {
			super.setValue(specifier);

			String s = (String)specifier;
			if(s!=null && !LanguageConstants.DATA_UNDEFINED_LABEL.equals(s)) {
				if(toParams==null) {
					toParams = new PaIntEConstraintParams();
				}

				BoundedSyllableConstraint.parseParams(s, toParams);
			}
		}

		@Override
		public boolean isUndefined() {
			return super.isUndefined()
					|| (toParams!=null && toParams.isUndefined()
							&& fromParams!=null && fromParams.isUndefined());
		}

		/**
		 * @see de.ims.icarus.search_tools.SearchConstraint#matches(java.lang.Object)
		 */
		@Override
		public boolean matches(Object value) {
			ProsodyTargetTree tree = (ProsodyTargetTree) value;

			if(tree.hasSyllables()) {

				for(int i=0; i<tree.getSyllableCount(); i++) {
					if(matches(tree, i)) {
						return true;
					}
				}
			}

			return false;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.search.constraints.SyllableConstraint#matches(java.lang.Object, int)
		 */
		@Override
		public boolean matches(Object value, int syllable) {
			ProsodyTargetTree tree = (ProsodyTargetTree) value;

			targetParams.setParams(tree.getSource(), tree.getNodeIndex(), syllable);

			if(fromParams!=null && !fromParams.isUndefined()) {
				if(!fromParams.isCompact()) {
					if(fromParams.isA1Active() && targetParams.getA1()<fromParams.getA1()) {
						return false;
					}
					if(fromParams.isA2Active() && targetParams.getA2()<fromParams.getA2()) {
						return false;
					}
					if(fromParams.isAlignmentActive() && targetParams.getAlignment()<fromParams.getAlignment()) {
						return false;
					}
				}

				if(fromParams.isBActive() && targetParams.getB()<fromParams.getB()) {
					return false;
				}
				if(fromParams.isC1Active() && targetParams.getC1()<fromParams.getC1()) {
					return false;
				}
				if(fromParams.isC2Active() && targetParams.getC2()<fromParams.getC2()) {
					return false;
				}
				if(fromParams.isDActive() && targetParams.getD()<fromParams.getD()) {
					return false;
				}
			}

			if(toParams!=null && !toParams.isUndefined()) {
				if(!toParams.isCompact()) {
					if(toParams.isA1Active() && targetParams.getA1()>toParams.getA1()) {
						return false;
					}
					if(toParams.isA2Active() && targetParams.getA2()>toParams.getA2()) {
						return false;
					}
					if(toParams.isAlignmentActive() && targetParams.getAlignment()>toParams.getAlignment()) {
						return false;
					}
				}

				if(toParams.isBActive() && targetParams.getB()>toParams.getB()) {
					return false;
				}
				if(toParams.isC1Active() && targetParams.getC1()>toParams.getC1()) {
					return false;
				}
				if(toParams.isC2Active() && targetParams.getC2()>toParams.getC2()) {
					return false;
				}
				if(toParams.isDActive() && targetParams.getD()>toParams.getD()) {
					return false;
				}
			}

			// TODO Auto-generated method stub
			return true;
		}
	}
}
