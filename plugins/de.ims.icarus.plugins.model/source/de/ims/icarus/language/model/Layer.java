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
package de.ims.icarus.language.model;

import de.ims.icarus.language.model.manifest.LayerManifest;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Layer extends NamedCorpusMember {

	/**
	 * Returns the 'type' of this layer. This is an object
	 * previously defined via a plugin.xml declaration. The corpus
	 * uses this type information to group top-level members for
	 * easier access.
	 * 
	 * @return the shared type of the group the member belongs to
	 * (e.g. 'Lemma', 'Part-of-Speech', etc.)
	 */
	LayerType getType();
	
	/**
	 * Returns the {@code Context} object that defines the physical 
	 * source of this layer and provides information about other 
	 * layers sharing the same origin.
	 */
	Context getContext();
	
	/**
	 * Returns the {@code MarkableLayer} that this layer
	 * depends on.
	 * 
	 * @return
	 */
	MarkableLayer getBaseLayer();
	
	/**
	 * Returns the manifest object that describes the content of 
	 * this layer. Subclasses should override this method to return
	 * a specific type of {@code LayerManifest}!
	 * 
	 * @return The manifest describing the content of this layer
	 */
	LayerManifest getManifest();
	
	/**
	 * Called by a corpus to signal a layer that it has been added.
	 * 
	 * @param corpus The corpus this layer has been added to
	 */
	void addNotify(Corpus corpus);

	/**
	 * Called by a corpus to signal a layer that it has been removed.
	 * 
	 * @param corpus The corpus this layer has been removed from
	 */
	void removeNotify(Corpus corpus);
}
