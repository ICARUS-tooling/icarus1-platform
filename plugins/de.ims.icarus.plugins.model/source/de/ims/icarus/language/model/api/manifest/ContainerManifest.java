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
package de.ims.icarus.language.model.api.manifest;

import de.ims.icarus.language.model.api.ContainerType;
import de.ims.icarus.language.model.api.Structure;

/**
 * A manifest that describes a container and its content.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ContainerManifest extends MemberManifest {

	/**
	 * Returns the manifest of the {@code MarkableLayer} the container
	 * is hosted in.
	 * @return
	 */
	MarkableLayerManifest getLayerManifest();

	/**
	 * If this is a nested container this will return the manifest of
	 * the parent container. On a top-level manifest the returned
	 * value will be {@code null}.
	 * @return
	 */
	ContainerManifest getParentManifest();

	/**
	 * If the elements of this container are themselves containers and this
	 * container is their actual parent than this method returns the manifest
	 * for those nested containers. Otherwise {@code null} will be returned.
	 * @return
	 */
	ContainerManifest getElementManifest();

	/**
	 * Returns the type of this container. This provides
	 * information about how contained {@code Markable}s are ordered and
	 * if they represent a continuous subset of the corpus. Note that the
	 * container type of a {@link Structure} object is undefined and therefore
	 * this method is optional when this container is a structure.
	 *
	 * @return The {@code ContainerType} of this {@code Container}
	 * @see ContainerType
	 */
	ContainerType getContainerType();


	/**
	 * Changes the type of this container
	 * @param containerType The new type of this container
	 * @throws NullPointerException if the {@code containerType} argument is {@code null}
	 */
	void setContainerType(ContainerType containerType);

	/**
	 * Returns the manifest describing the <i>boundary containers</i> used for
	 * {@code Container} instances of this manifest or {@code null} if those
	 * containers are not restricted by boundaries.
	 * <p>
	 * Note that <i>boundary containers</i> are only valid when a container actually hosts
	 * other containers!
	 */
	ContainerManifest getBoundaryContainerManifest();

	ContainerManifest getBaseContainerManifest();
}
