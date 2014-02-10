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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class RootContainer extends AbstractListContainer {

	private final MarkableLayer layer;
	private final ContainerManifest manifest;

	public RootContainer(long id, MarkableLayer layer, ContainerManifest manifest) {
		this(id, layer, manifest, null);
	}

	public RootContainer(long id, MarkableLayer layer, ContainerManifest manifest, List<? extends Markable> markables) {
		super(id);

		if (layer == null)
			throw new NullPointerException("Invalid layer");  //$NON-NLS-1$
		if (manifest == null)
			throw new NullPointerException("Invalid manifest");  //$NON-NLS-1$

		this.layer = layer;
		this.manifest = manifest;

		if(markables!=null) {
			addAllMarkables0(markables);
		}
	}

	/**
	 * Since this implementation represents the top-level container
	 * of a layer there is no enclosing container. Therefore this
	 * method always returns {@code null}.
	 *
	 * @see de.ims.icarus.language.model.Markable#getContainer()
	 */
	@Override
	public Container getContainer() {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return layer;
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return layer.getCorpus();
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		return manifest;
	}

	public static class RootContainerBuilder extends ListContainerBuilder<RootContainer> {

		/**
		 * @see de.ims.icarus.language.model.standard.builder.AbstractContainerBuilder#createContainer()
		 */
		@Override
		protected RootContainer createContainer() {
			return new RootContainer(newId(), layer, manifest);
		}
	}
}
