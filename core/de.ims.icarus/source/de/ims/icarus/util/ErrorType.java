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
package de.ims.icarus.util;

import javax.swing.Icon;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum ErrorType implements Identity {
	TRUE_NEGATIVE("trueNegative"), //$NON-NLS-1$
	TRUE_POSITIVE("truePositive"), //$NON-NLS-1$
	FALSE_POSITIVE("falsePositive"), //$NON-NLS-1$
	FALSE_NEGATIVE("falseNegative"); //$NON-NLS-1$

	
	private final String key;
	
	private ErrorType(String key) {
		this.key = key;
	}

	@Override
	public String getId() {
		return getName();
	}

	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"errorType."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"errorType."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public Object getOwner() {
		return ErrorType.class;
	}
}
