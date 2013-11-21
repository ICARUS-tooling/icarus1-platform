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
package de.ims.icarus.language.model.manifest;

import java.util.Set;

import de.ims.icarus.language.model.NamedCorpusMember;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Manifest extends Identity {

	
	/**
	 * Allows for localization of corpus related components. 
	 * The returned name is not required to be equal to the 
	 * result of a {@link NamedCorpusMember#getTypeName()} call.
	 * 
	 * @return The (optionally) localized name of the task 
	 * this manifest describes.
	 */
	String getName();
	
	/**
	 * Returns a more detailed description of the task performed
	 * by instances of this manifest. This is an optional method.
	 * 
	 * @return Returns the optional description of this manifest
	 */
	String getDescription();
	
	/**
	 * Returns the property assigned to this manifest for the given
	 * name. If their is no property with the given name available
	 * this method should return {@code null}.
	 * 
	 * @param name The name of the property in question
	 * @return The value of the property with the given name or {@code null}
	 * if no such property exists.
	 */
	Object getProperty(String name);
	
	/**
	 * Returns a {@link Set} view of all the available property names
	 * in this manifest. If there are no properties in the manifest
	 * available then this method should return an empty {@code Set}!
	 * <p>
	 * The returned {@code Set} should be immutable.
	 *  
	 * @return A {@code Set} view on all the available property names
	 * for this manifest or the empty {@code Set} if this manifest does
	 * not contain any properties.
	 */
	Set<String> getPropertyNames();
}
