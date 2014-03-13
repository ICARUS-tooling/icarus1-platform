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
package de.ims.icarus.language.model.standard.layer;

import javax.swing.Icon;

import de.ims.icarus.language.model.api.layer.LayerType;
import de.ims.icarus.language.model.api.manifest.LayerManifest;
import de.ims.icarus.language.model.registry.CorpusRegistry;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LazyLayerType implements LayerType {

	private final String id;
	private String name;
	private String description;
	private Icon icon;

	private String layerId;
	private LayerManifest sharedManifest;

	public LazyLayerType(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		this.id = id;
	}

	public LazyLayerType(Identity identity, String layerId) {
		if (identity == null)
			throw new NullPointerException("Invalid identity"); //$NON-NLS-1$
		if (layerId == null)
			throw new NullPointerException("Invalid layerId"); //$NON-NLS-1$
		if(identity.getId()==null)
			throw new IllegalArgumentException("Missing 'id' calue from identity"); //$NON-NLS-1$

		id = identity.getId();
		name = identity.getName();
		description = identity.getDescription();
		icon = identity.getIcon();

		this.layerId = layerId;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return icon;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.LayerType#getSharedManifest()
	 */
	@Override
	public LayerManifest getSharedManifest() {
		if(sharedManifest==null && layerId!=null) {
			sharedManifest = (LayerManifest) CorpusRegistry.getInstance().getTemplate(layerId);
		}

		return sharedManifest;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name");  //$NON-NLS-1$

		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		if (description == null)
			throw new NullPointerException("Invalid description");  //$NON-NLS-1$

		this.description = description;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(Icon icon) {
		if (icon == null)
			throw new NullPointerException("Invalid icon");  //$NON-NLS-1$

		this.icon = icon;
	}

	/**
	 * @param layerId the layerId to set
	 */
	public void setLayerId(String layerId) {
		if (layerId == null)
			throw new NullPointerException("Invalid layerId");  //$NON-NLS-1$

		this.layerId = layerId;
	}

}
