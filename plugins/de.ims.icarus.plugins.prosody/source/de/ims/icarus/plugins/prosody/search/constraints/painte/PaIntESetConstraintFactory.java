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

import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.plugins.prosody.search.constraints.AbstractProsodySyllableConstraint;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: DependencyRelationConstraintFactory.java 269 2014-07-07 22:09:53Z mcgaerty $
 *
 */
public class PaIntESetConstraintFactory extends AbstractConstraintFactory implements ProsodyConstants {

	public static final String TOKEN = "painteSet"; //$NON-NLS-1$

	public PaIntESetConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.painteSet.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.painteSet.description"); //$NON-NLS-1$
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return new SearchOperator[]{
				DefaultSearchOperator.EQUALS,
				DefaultSearchOperator.EQUALS_NOT,
				DefaultSearchOperator.LESS_THAN,
				DefaultSearchOperator.LESS_OR_EQUAL,
				DefaultSearchOperator.GREATER_THAN,
				DefaultSearchOperator.GREATER_OR_EQUAL,
		};
	}

	@Override
	public Class<?> getValueClass(Object specifier) {
		return String.class;
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new ProsodyPaIntESetConstraint(value, operator);
	}

	private static class ProsodyPaIntESetConstraint extends AbstractProsodySyllableConstraint {

		private static final long serialVersionUID = 7309790344228778387L;

		private final transient PaIntEConstraintParams painteConstraints;

		public ProsodyPaIntESetConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator, null);

			if(!DATA_UNDEFINED_LABEL.equals(value)
					&& !DATA_GROUP_LABEL.equals(value)) {
				painteConstraints = new PaIntEConstraintParams((String) value);
			} else {
				painteConstraints = null;
			}
		}

		private transient PaIntEConstraintParams instance = new PaIntEConstraintParams();

		@Override
		public Object getInstance(ProsodyTargetTree tree, int sylIndex) {
			instance.setParams(tree.getSource(), tree.getNodeIndex(), sylIndex);
			return instance;
		}

		@Override
		public Object getLabel(Object value) {
			return (value instanceof PaIntEConstraintParams) ? value.toString() : value;
		}
	}
}
