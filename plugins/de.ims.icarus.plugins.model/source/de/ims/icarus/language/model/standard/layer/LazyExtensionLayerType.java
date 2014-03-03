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

import org.java.plugin.registry.Extension;

import de.ims.icarus.language.model.api.LayerType;
import de.ims.icarus.language.model.api.manifest.LayerManifest;
import de.ims.icarus.language.model.registry.CorpusRegistry;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.ClassUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LazyExtensionLayerType implements LayerType {

	private final Extension extension;

	private LayerManifest sharedManifest;

	public LazyExtensionLayerType(Extension extension) {
		if (extension == null)
			throw new NullPointerException("Invalid extension"); //$NON-NLS-1$

		this.extension = extension;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return extension.getId();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return PluginUtil.getIdentity(extension).getName();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return PluginUtil.getIdentity(extension).getDescription();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return PluginUtil.getIdentity(extension).getIcon();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	/**
	 * @see de.ims.icarus.language.model.api.LayerType#getSharedManifest()
	 */
	@Override
	public LayerManifest getSharedManifest() {
		if(sharedManifest==null) {
			Extension.Parameter param = extension.getParameter("template"); //$NON-NLS-1$
			if(param!=null) {
				String layerId = param.valueAsString();
				sharedManifest = (LayerManifest) CorpusRegistry.getInstance().getTemplate(layerId);
			}
		}

		return sharedManifest;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return extension.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof LayerType) {
			LayerType other = (LayerType) obj;
			return ClassUtils.equals(getId(), other.getId());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Layer-Type:"+extension.getId(); //$NON-NLS-1$
	}
}
