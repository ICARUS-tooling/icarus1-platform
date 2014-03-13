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
package de.ims.icarus.language.model.standard.container;

import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.Corpus;
import de.ims.icarus.language.model.api.layer.MarkableLayer;
import de.ims.icarus.language.model.api.manifest.ContainerManifest;
import de.ims.icarus.language.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Reference;
import de.ims.icarus.util.mem.ReferenceType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public abstract class AbstractNestedContainer extends AbstractContainer {

	@Reference(ReferenceType.UPLINK)
	private final Container parent;

	public AbstractNestedContainer(long id, Container parent) {
		super(id);

		this.parent = parent;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getContainer()
	 */
	@Override
	public Container getContainer() {
		return parent;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return parent.getLayer();
	}

	/**
	 * @see de.ims.icarus.language.model.api.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return parent.getCorpus();
	}

	/**
	 * To decrease memory footprint this implementation does not
	 * store a reference to the assigned manifest itself, but rather
	 * checks the depth of nesting and forwards the call to the
	 * {@link MarkableLayerManifest} that describes this
	 * container's root.
	 *
	 * @see de.ims.icarus.language.model.api.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		return CorpusUtils.getContainerManifest(this);
	}
}
