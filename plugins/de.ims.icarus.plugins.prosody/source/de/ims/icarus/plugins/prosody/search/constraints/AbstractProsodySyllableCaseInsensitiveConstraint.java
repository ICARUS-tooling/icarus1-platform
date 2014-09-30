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
package de.ims.icarus.plugins.prosody.search.constraints;

import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.DefaultCaseInsensitiveConstraint;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractProsodySyllableCaseInsensitiveConstraint extends DefaultCaseInsensitiveConstraint implements SyllableConstraint {

	private static final long serialVersionUID = 8100173492561489663L;


	public AbstractProsodySyllableCaseInsensitiveConstraint(String token, Object value,
			SearchOperator operator, Object specifier) {
		super(token, value, operator, specifier);
	}


	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#matches(java.lang.Object)
	 */
	@Override
	public boolean matches(Object value) {
		ProsodyTargetTree tree = (ProsodyTargetTree) value;

		if(tree.hasSyllables()) {

			SearchOperator operator = getOperator();
			Object constraint = getConstraint();

			for(int i=0; i<tree.getSyllableCount(); i++) {
				Object instance = getInstance(tree, i);
				if(instance instanceof String) {
					instance = ((String)instance).toLowerCase();
				}

				if(operator.apply(instance, constraint)) {
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

			SearchOperator operator = getOperator();
			Object constraint = getConstraint();
			Object instance = getInstance(tree, syllable);
			if(instance instanceof String) {
				instance = ((String)instance).toLowerCase();
			}

			return operator.apply(instance, constraint);
		}

		return false;
	}

	@Override
	public Object getInstance(Object value) {
		throw new UnsupportedOperationException("Syllable constraints do not operate on word level"); //$NON-NLS-1$
	}


	protected abstract Object getInstance(ProsodyTargetTree tree, int syllable);
}
