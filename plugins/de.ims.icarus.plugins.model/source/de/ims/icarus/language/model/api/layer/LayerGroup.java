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
package de.ims.icarus.language.model.api.layer;

import java.util.Set;

import de.ims.icarus.language.model.api.Context;
import de.ims.icarus.language.model.api.manifest.LayerGroupManifest;

/**
 * Groups several layers, so that they are guaranteed to be loaded together.
 * The idea behind layer groups is to bundle layers that are so closely tight together
 * in the respective physical storage, that it is practically impossible to load them
 * separately. For example in a text based data format like the format of the
 * <i>CoNLL 2012 Shared Task</i> which falls into the <i>markable centric</i> category,
 * loading sentences without moving the content of underlying tokens into memory is not
 * feasible. While for the purpose of inspecting or visualizing data there might be the
 * motivation to provide a finer granularity than layers grouped by format design,
 * drivers and member caches of a corpus cannot efficiently handle such fine grained
 * data chunks.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface LayerGroup {

	Context getContext();

	Set<Layer> getLayers();

	LayerGroupManifest getManifest();

	MarkableLayer getPrimaryLayer();

	Set<Dependency<LayerGroup>> getDependencies();
}