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
package de.ims.icarus.search_tools.standard;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum IntOperator {

	EQUALS {
		@Override
		public boolean apply(int value, int constraint) {
			return value==constraint;
		}
	},

	EQUALS_NOT {
		@Override
		public boolean apply(int value, int constraint) {
			return value!=constraint;
		}
	},

	LESS_THAN {
		@Override
		public boolean apply(int value, int constraint) {
			return value<constraint;
		}
	},

	LESS_OR_EQUALS {
		@Override
		public boolean apply(int value, int constraint) {
			return value<=constraint;
		}
	},

	GREATER_THAN {
		@Override
		public boolean apply(int value, int constraint) {
			return value>constraint;
		}
	},

	GREATER_OR_EQUALS {
		@Override
		public boolean apply(int value, int constraint) {
			return value>=constraint;
		}
	},

	;

	public abstract boolean apply(int value, int constraint);

	public static IntOperator fromSymbol(String s) {
		switch (s) {
		case "=": return EQUALS; //$NON-NLS-1$
		case "!=": return EQUALS_NOT; //$NON-NLS-1$
		case ">": return GREATER_THAN; //$NON-NLS-1$
		case ">=": return GREATER_OR_EQUALS; //$NON-NLS-1$
		case "<": return LESS_THAN; //$NON-NLS-1$
		case "<=": return LESS_OR_EQUALS; //$NON-NLS-1$

		default:
			return null;
		}
	}
}
