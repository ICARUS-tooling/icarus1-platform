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
import de.ims.icarus.search_tools.SearchOperator;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractParameterizedPaIntEConstraint extends BoundedSyllableConstraint implements PaIntEConstraint {

	private static final long serialVersionUID = -8403821633243469371L;

	protected final PaIntEConstraintParams valueParams = new PaIntEConstraintParams();
	protected PaIntEConstraintParams specifierParams;

	public AbstractParameterizedPaIntEConstraint(String token, Object value,
			SearchOperator operator, Object specifier) {
		super(token, value, operator, specifier);
	}

	@Override
	public PaIntEConstraintParams[] getPaIntEConstraints() {
		return new PaIntEConstraintParams[]{specifierParams};
	}

	@Override
	public void setSpecifier(Object specifier) {
		super.setSpecifier(specifier);

		String s = (String)specifier;
		if(s!=null && !LanguageConstants.DATA_UNDEFINED_LABEL.equals(s)) {
			if(specifierParams==null) {
				specifierParams = new PaIntEConstraintParams();
			}

			parseConstraint(s, specifierParams);

			specifierParams.checkNonEmpty();
		}
	}

	protected void checkConstraint(PaIntEConstraintParams constraint) {
		constraint.checkNonEmpty(getToken()+" constraint"); //$NON-NLS-1$
	}
}
