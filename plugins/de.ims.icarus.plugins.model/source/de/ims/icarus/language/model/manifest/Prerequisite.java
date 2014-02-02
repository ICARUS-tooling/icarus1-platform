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

/**
 * Abstract description of a layer object this layer depends on.
 * <p>
 * Note that prerequisites are only used in templates. When a template
 * is being instantiated, all the prerequisites will be resolved to actual
 * layers and linked accordingly.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Prerequisite {

	/**
	 * Returns the id of the target layer or {@code null} if an exact id match
	 * is not required.
	 *
	 * @return
	 */
	String getLayerId();

	/**
	 * Returns the id of the context which should be used to resolve the required
	 * layer (specified by the {@link #getLayerId() method}) or {@code null} if no
	 * exact match is required.
	 * @return
	 */
	String getContextId();

	/**
	 * If this layer only requires <i>some</i> layer of a certain type to be present
	 * this method provides the mechanics to tell this. When the returned value is
	 * {@code non-null} it is considered to be the exact name of a previously
	 * defined layer type.
	 *
	 * @return
	 */
	String getTypeId();

	/**
	 * Returns the id the required layer should be assigned once resolved. This links
	 * the result of an abstract prerequisite declaration to a boundary or base definition
	 * in a template.
	 * @return
	 */
	String getAlias();
}