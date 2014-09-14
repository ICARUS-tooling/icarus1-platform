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
package de.ims.icarus.plugins.prosody.ui.view.outline;

import java.awt.FontMetrics;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum PreviewSize {

	SMALL {
		@Override
		public int getHeight(FontMetrics fm) {
			return (int) (fm.getHeight()*0.80);
		}
	},

	MEDIUM {
		@Override
		public int getHeight(FontMetrics fm) {
			return (int) (fm.getHeight()*1.5);
		}
	},

	LARGE {
		@Override
		public int getHeight(FontMetrics fm) {
			return (int) (fm.getHeight()*2.4);
		}
	},
	;

	public abstract int getHeight(FontMetrics fm);
}
