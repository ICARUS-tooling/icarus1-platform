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
package de.ims.icarus.language.model.standard.builder;

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.MarkableLayer;
import de.ims.icarus.language.model.manifest.ContainerManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractContainerBuilder<C extends Container> extends AbstractBuilder implements ContainerBuilder {

	protected Container parent;
	protected Container base;
	protected MarkableLayer layer;
	protected ContainerManifest manifest;

	protected C container;

	/**
	 * @return the parent
	 */
	public Container getParent() {
		return parent;
	}

	/**
	 * @return the base
	 */
	public Container getBase() {
		return base;
	}

	/**
	 * @param base the base to set
	 */
	public void setBase(Container base) {
		this.base = base;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Container parent) {
		this.parent = parent;
	}

	/**
	 * @return the layer
	 */
	public MarkableLayer getLayer() {
		return layer;
	}

	/**
	 * @return the manifest
	 */
	public ContainerManifest getManifest() {
		return manifest;
	}

	/**
	 * @param layer the layer to set
	 */
	public void setLayer(MarkableLayer layer) {
		this.layer = layer;
	}

	/**
	 * @param manifest the manifest to set
	 */
	public void setManifest(ContainerManifest manifest) {
		this.manifest = manifest;
	}

	@Override
	public C build() {
		return container;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.builder.ContainerBuilder#reset()
	 */
	@Override
	public void reset() {
		container = createContainer();
	}

	protected abstract C createContainer();
}
