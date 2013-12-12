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

import java.util.List;

import de.ims.icarus.language.model.events.EventManager;
import de.ims.icarus.language.model.io.ContextReader;
import de.ims.icarus.language.model.io.ContextWriter;
import de.ims.icarus.util.location.Location;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ContextManifest extends Manifest {

	/**
	 * Allows for changes of the layer's name at runtime. This is one of the
	 * few situations where a direct modification of a manifest by the user is
	 * possible.
	 * <p>
	 * Checks if the {@code newName} parameter is valid within the corpus
	 * and assigns it as current name. Does nothing if the {@code newName}
	 * argument is equal to the current name. Otherwise it will forward
	 * notification of the change to the {@link EventManager} of the corpus.
	 * 
	 * @param newName The desired new name of the layer
	 * @throws UnsupportedOperationException if the manifest does not support renaming
	 * @throws NullPointerException if the {@code newName} parameter is {@code null}
	 * @throws IllegalArgumentException if the {@code newName} parameter is either empty or
	 * another layer already has that name assigned to it
	 */
	void setName(String newName);

	/**
	 * Returns the list of manifests that describe the layers in this context
	 * 
	 * @return
	 */
	List<LayerManifest> getLayerManifests();

	/**
	 * Returns the reader that is used to build the actual content
	 * of this context. If the layers in this context are generated
	 * programmatically then this method might return {@code null}.
	 * 
	 * @return The {@link ContextReader} that is used for this context
	 * or {@code null} if this context does not derive from a physical
	 * data location.
	 */
	Class<? extends ContextReader> getReaderClass();

	Class<? extends ContextWriter> getWriterClass();

	/**
	 * Returns the physical location the data in this context is originating
	 * from. If the layers in this context are generated
	 * programmatically then this method might return {@code null}.
	 * 
	 * @return
	 */
	Location getLocation();

	/**
	 * Changes
	 * @param location
	 */
	void setLocation(Location location);
}
