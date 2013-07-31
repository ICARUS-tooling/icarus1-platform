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
package de.ims.icarus.ui.dialog;

import java.awt.Component;

import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public abstract class DialogBuilder {
	
	public static final String RESIZABLE_OPTION = "resizable"; //$NON-NLS-1$
	
	protected ResourceDomain resourceDomain;

	/**
	 * 
	 */
	public DialogBuilder(ResourceDomain resourceDomain) {
		this.resourceDomain = resourceDomain;
	}

	/**
	 * 
	 */
	public DialogBuilder() {
		this(null);
	}
	
	public abstract void showDialog(Component parent, Options options);
	
	/**
	 * @return the resourceDomain
	 */
	public ResourceDomain getResourceDomain() {
		return resourceDomain;
	}

	/**
	 * @param resourceDomain the resourceDomain to set
	 */
	public void setResourceDomain(ResourceDomain resourceDomain) {
		this.resourceDomain = resourceDomain;
	}
}