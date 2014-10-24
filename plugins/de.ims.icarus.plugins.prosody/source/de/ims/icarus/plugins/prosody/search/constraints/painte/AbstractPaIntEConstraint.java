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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.search.constraints.painte;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.GroupCache;

/**
 * Constraint using a set of painte parameters as constraint instance. Internally translates
 * string constraints and search operators to implementations suitable for painte sets.
 * Does <b>not</b> support grouping, specifiers or transformation of value instances to string labels!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractPaIntEConstraint extends BoundedSyllableConstraint implements PaIntEConstraint {

	private static final long serialVersionUID = -361927812276915216L;

	protected final PaIntEConstraintParams valueParams = new PaIntEConstraintParams();
	protected PaIntEConstraintParams constraintParams;

	public AbstractPaIntEConstraint(String token, Object value,
			SearchOperator operator, Object specifier) {
		super(token, value, operator, specifier);
	}

	@Override
	public void setValue(Object value) {
		super.setValue(value);

		String s = (String)value;
		if(s!=null && !LanguageConstants.DATA_UNDEFINED_LABEL.equals(s)) {
			if(constraintParams==null) {
				constraintParams = new PaIntEConstraintParams();
			}

			parseConstraint(s, constraintParams);

			constraintParams.checkNonEmpty();
		}
	}

	@Override
	public boolean hasBounds() {
		return false;
	}

	@Override
	public PaIntEConstraintParams[] getPaIntEConstraints() {
		return new PaIntEConstraintParams[]{constraintParams};
	}

	protected abstract boolean applyOperator(PaIntEConstraintParams target);

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#matches(java.lang.Object)
	 */
	@Override
	public boolean matches(Object value) {
		ProsodyTargetTree tree = (ProsodyTargetTree) value;

		if(tree.hasSyllables()) {

			for(int i=0; i<tree.getSyllableCount(); i++) {
				if(applyOperator(getInstance(tree, i))) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean matches(Object value, int syllable) {
		ProsodyTargetTree tree = (ProsodyTargetTree) value;

		if(tree.hasSyllables()) {
			return applyOperator(getInstance(tree, syllable));
		}

		return false;
	}

	@Override
	public void group(GroupCache cache, int groupId, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getLabel(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected PaIntEConstraintParams getInstance(ProsodyTargetTree tree, int syllable) {

		valueParams.setParams(tree.getSource(), tree.getNodeIndex(), syllable);

		return valueParams;
	}

}
