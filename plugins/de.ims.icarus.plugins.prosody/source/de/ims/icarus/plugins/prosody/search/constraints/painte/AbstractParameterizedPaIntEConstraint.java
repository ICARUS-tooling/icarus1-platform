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

import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEOperator;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.plugins.prosody.search.constraints.AbstractProsodySyllableConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.GroupCache;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractParameterizedPaIntEConstraint extends AbstractProsodySyllableConstraint {

	private static final long serialVersionUID = -8403821633243469371L;

	protected final PaIntEConstraintParams valueParams = new PaIntEConstraintParams();

	protected PaIntEOperator painteOperator;

	public AbstractParameterizedPaIntEConstraint(String token, Object value,
			SearchOperator operator, Object specifier) {
		super(token, value, operator, specifier);
	}

	public PaIntEOperator getPainteOperator() {
		return painteOperator;
	}

	public void setPainteOperator(PaIntEOperator painteOperator) {
		if (painteOperator == null)
			throw new NullPointerException("Invalid painteOperator"); //$NON-NLS-1$

		this.painteOperator = painteOperator;

		Object value = getValue();
		if(value!=null) {
			String params = (String) value;
			painteOperator.setParams(params);
		}
	}

	@Override
	public void setValue(Object value) {
		super.setValue(value);

		if(painteOperator!=null) {
			String params = (String) value;
			painteOperator.setParams(params);
		}
	}

	@Override
	public void setOperator(SearchOperator operator) {
		super.setOperator(operator);

		setPainteOperator(createPaIntEOperator(operator));
	}

	protected abstract PaIntEOperator createPaIntEOperator(SearchOperator operator);

	@Override
	public void setSpecifier(Object specifier) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#matches(java.lang.Object)
	 */
	@Override
	public boolean matches(Object value) {
		ProsodyTargetTree tree = (ProsodyTargetTree) value;

		if(tree.hasSyllables()) {

			PaIntEOperator operator = getPainteOperator();

			for(int i=0; i<tree.getSyllableCount(); i++) {
				if(operator.apply(getInstance(tree, i))) {
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

			PaIntEOperator operator = getPainteOperator();

			return operator.apply(getInstance(tree, syllable));
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
