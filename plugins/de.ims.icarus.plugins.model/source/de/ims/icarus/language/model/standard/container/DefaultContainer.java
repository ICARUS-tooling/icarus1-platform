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

import java.util.List;

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.MarkableLayer;
import de.ims.icarus.language.model.manifest.ContainerManifest;
import de.ims.icarus.language.model.manifest.MarkableLayerManifest;

/**
 * A list based container implementation without restrictions to its
 * elements. Note that this implementation is not suitable as root
 * container since it assumes a parent container!
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultContainer extends AbstractListContainer {

	private final Container parent;

	public DefaultContainer(Container parent) {
		this(parent, null);
	}

	public DefaultContainer(Container parent, List<? extends Markable> markables) {
		if (parent == null)
			throw new NullPointerException("Invalid parent");  //$NON-NLS-1$

		this.parent = parent;

		if(markables!=null) {
			addAll(markables);
		}
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getContainer()
	 */
	@Override
	public Container getContainer() {
		return parent;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return parent.getLayer();
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getCorpus()
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
	 * @see de.ims.icarus.language.model.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		// Fetch the container level and ask the
		// hosting markable layer manifest for the container
		// manifest at the specific level

		// We assume that this container is nested at least one level
		// below a root container
		int level = 2;

		Container parent = getContainer();
		while(parent.getContainer()!=null) {
			level++;
			parent = parent.getContainer();
		}

		MarkableLayerManifest manifest = getLayer().getManifest();

		return manifest.getContainerManifest(level);
	}

}
