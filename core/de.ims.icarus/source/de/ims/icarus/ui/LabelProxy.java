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
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui;

import javax.swing.Icon;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.Exceptions;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LabelProxy {

	private final String key;
	private final Object icon;
	private final Object[] params;

	public LabelProxy(String key, String iconName) {
		this(key, iconName, (Object[])null);
	}
	
	public LabelProxy(String key, Object icon, Object...params) {
		Exceptions.testNullArgument(key, "key"); //$NON-NLS-1$
		
		this.key = key;
		this.icon = icon;
		this.params = params;
	}
	
	@Override
	public String toString() {
		return ResourceManager.getInstance().get(key, params);
	}

	public Icon getIcon() {
		if(icon instanceof String) {
			return IconRegistry.getGlobalRegistry().getIcon((String)icon);
		}
		
		if(icon instanceof Icon) {
			return (Icon) icon;
		}
		
		return null;
	}
	
	public static String limit(String s) {
		return s.length()>25 ? s.substring(0, 25) : s;
	}
}
