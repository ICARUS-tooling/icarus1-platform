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

import java.util.Locale;

import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.GroupCache;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractProsodySyllableConstraint extends DefaultConstraint implements SyllableConstraint, ProsodyConstants {

	private static final long serialVersionUID = 8333873086091026549L;
	protected  boolean ignoreUnstressed = false;

	protected static final double UNDEFINED_B = Double.NEGATIVE_INFINITY;

	protected double minB = UNDEFINED_B, maxB = UNDEFINED_B;

	public AbstractProsodySyllableConstraint(String token, Object value,
			SearchOperator operator, Object specifier) {
		super(token, value, operator, specifier);
	}

	public double getMinB() {
		return minB;
	}

	public double getMaxB() {
		return maxB;
	}

	public void setMinB(double minB) {
		this.minB = minB;
	}

	public void setMaxB(double maxB) {
		this.maxB = maxB;
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
				if((ignoreUnstressed && !tree.isSyllableStressed(i))) {
					continue;
				}

				if(maxB!=UNDEFINED_B
						&& tree.getPainteB(i)<minB) {
					continue;
				}

				if(maxB!=UNDEFINED_B
						&& tree.getPainteB(i)>maxB) {
					continue;
				}

				Object instance = getInstance(tree, i);

				if(instance!=null && operator.apply(instance, constraint)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean matches(Object value, int syllable) {
		ProsodyTargetTree tree = (ProsodyTargetTree) value;

		if(tree.hasSyllables() && (!ignoreUnstressed || tree.isSyllableStressed(syllable))
				&& (minB==UNDEFINED_B || tree.getPainteB(syllable)>=minB)
				&& (maxB==UNDEFINED_B || tree.getPainteB(syllable)<=maxB)) {

			SearchOperator operator = getOperator();
			Object constraint = getConstraint();
			Object instance = getInstance(tree, syllable);

			return instance!=null && operator.apply(instance, constraint);
		}

		return false;
	}


	@Override
	public boolean isMultiplexing() {
		return true;
	}

	@Override
	public void group(GroupCache cache, int groupId, Object value) {
		ProsodyTargetTree tree = (ProsodyTargetTree) value;

		// Remember to handle the "empty" cache in the result set implementations!
		if(tree.hasSyllables()) {
			for(int i=0; i<tree.getSyllableCount(); i++) {
				float b = tree.getPainteB(i);

				if(minB!=UNDEFINED_B && b<minB) {
					continue;
				}
				if(maxB!=UNDEFINED_B && b>maxB) {
					continue;
				}

				Object instance = getInstance(tree, i);

				if(instance==null) {
					continue;
				}

				if(instance instanceof Float) {
					instance = (float)Math.floor((float)instance*100F)*0.01F;
				} else if(instance instanceof Double) {
					instance = Math.floor((double)instance*100D)*0.01D;
				}

				cache.cacheGroupInstance(groupId, getLabel(instance), false);
			}
		}
	}

	@Override
	public Object getLabel(Object value) {
		return (value instanceof Float || value instanceof Double) ?
				String.format(Locale.ENGLISH, "%.02f", value) : super.getLabel(value); //$NON-NLS-1$
	}


	@Override
	public Object getInstance(Object value) {
		throw new UnsupportedOperationException("Syllable constraints do not operate on word level"); //$NON-NLS-1$
	}


	protected abstract Object getInstance(ProsodyTargetTree tree, int syllable);
}
