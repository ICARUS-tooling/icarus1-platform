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
package de.ims.icarus.model.api;

import java.util.List;

import de.ims.icarus.model.api.driver.Driver;
import de.ims.icarus.model.api.layer.Layer;
import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.ManifestOwner;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Context extends ManifestOwner<ContextManifest> {

	Corpus getCorpus();

	List<Layer> getLayers();

	/**
	 * If this context contains one or more markable layers it has to define
	 * one primary layer among them.
	 * @return
	 */
	MarkableLayer getPrimaryLayer();

	List<LayerGroup> getLayerGroups();

	@Override
	ContextManifest getManifest();

	Layer getLayer(String id);

	/**
	 * Returns the shared {@code Driver} instance that is used to access
	 * and manage the content represented by this context.
	 */
	Driver getDriver();

	/**
	 * Called by a corpus to signal a context that it has been added.
	 * <p>
	 * Note that this method will <b>not</b> be called when a context is
	 * assigned default context for a corpus!
	 *
	 * @param corpus The corpus this context has been added to
	 */
	void addNotify(Corpus corpus);

	/**
	 * Called by a corpus to signal a context that it has been removed.
	 * <p>
	 * Note that this method will <b>not</b> be called for default contexts
	 * since they cannot be removed without completely invalidating their
	 * respective host corpus!
	 *
	 * @param corpus The corpus this context has been removed from
	 */
	void removeNotify(Corpus corpus);
}
