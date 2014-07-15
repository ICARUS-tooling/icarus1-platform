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
package de.ims.icarus.model.standard.manifest;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.model.api.layer.LayerType;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractLayerManifest<L extends LayerManifest> extends AbstractMemberManifest<L> implements LayerManifest {

	private LayerGroupManifest layerGroupManifest;
	private List<TargetLayerManifest> baseLayerManifests = new ArrayList<>(3);
	private LayerType layerType;

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerManifest#getContextManifest()
	 */
	@Override
	public ContextManifest getContextManifest() {
		return layerGroupManifest.getContextManifest();
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerManifest#getGroupManifest()
	 */
	@Override
	public LayerGroupManifest getGroupManifest() {
		return layerGroupManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerManifest#setGroupManifest(de.ims.icarus.model.api.manifest.LayerGroupManifest)
	 */
	@Override
	public void setGroupManifest(LayerGroupManifest layerGroupManifest) {
		if (layerGroupManifest == null)
			throw new NullPointerException("Invalid layerGroupManifest"); //$NON-NLS-1$

		this.layerGroupManifest = layerGroupManifest;
	}

	/**
	 * @param layerType the layerType to set
	 */
	@Override
	public void setLayerType(LayerType layerType) {
		this.layerType = layerType;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerManifest#getLayerType()
	 */
	@Override
	public LayerType getLayerType() {
		return layerType;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerManifest#getBaseLayerManifests()
	 */
	@Override
	public List<TargetLayerManifest> getBaseLayerManifests() {
		return CollectionUtils.getListProxy(baseLayerManifests);
	}

	@Override
	public void addBaseLayerManifest(TargetLayerManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		baseLayerManifests.remove(manifest);
		baseLayerManifests.add(manifest);
	}

	@Override
	public void removeBaseLayerManifest(TargetLayerManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		baseLayerManifests.remove(manifest);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#copyFrom(de.ims.icarus.model.api.manifest.MemberManifest)
	 */
	@Override
	protected void copyFrom(L template) {
		super.copyFrom(template);

		layerType = template.getLayerType();

		for(TargetLayerManifest baseLayerManifest : template.getBaseLayerManifests()) {
			addBaseLayerManifest(baseLayerManifest);
		}
	}

	public static class TargetLayerManifestImpl implements TargetLayerManifest {

		private final LayerManifest layerManifest;

		private PrerequisiteManifest prerequisiteManifest;
		private LayerManifest resolvedLayerManifest;

		public TargetLayerManifestImpl(LayerManifest layerManifest) {
			if (layerManifest == null)
				throw new NullPointerException("Invalid layerManifest"); //$NON-NLS-1$

			this.layerManifest = layerManifest;
		}

		public TargetLayerManifestImpl(LayerManifest layerManifest, TargetLayerManifest template) {
			this(layerManifest);

			if (template == null)
				throw new NullPointerException("Invalid template"); //$NON-NLS-1$

			prerequisiteManifest = template.getPrerequisite();
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest#getLayerManifest()
		 */
		@Override
		public LayerManifest getLayerManifest() {
			return layerManifest;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest#getPrerequisite()
		 */
		@Override
		public PrerequisiteManifest getPrerequisite() {
			return prerequisiteManifest;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest#getResolvedLayerManifest()
		 */
		@Override
		public LayerManifest getResolvedLayerManifest() {
			return resolvedLayerManifest;
		}

		/**
		 * @param prerequisiteManifest the prerequisiteManifest to set
		 */
		public void setPrerequisiteManifest(PrerequisiteManifest prerequisiteManifest) {
			this.prerequisiteManifest = prerequisiteManifest;
		}

		/**
		 * @param resolvedLayerManifest the resolvedLayerManifest to set
		 */
		public void setResolvedLayerManifest(LayerManifest resolvedLayerManifest) {
			if (resolvedLayerManifest == null)
				throw new NullPointerException("Invalid resolvedLayerManifest"); //$NON-NLS-1$

			this.resolvedLayerManifest = resolvedLayerManifest;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return super.hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof TargetLayerManifest) {
				TargetLayerManifest other = (TargetLayerManifest) obj;
				return (resolvedLayerManifest==null || resolvedLayerManifest==other.getResolvedLayerManifest())
						&& (prerequisiteManifest==null || prerequisiteManifest==other.getPrerequisite());
			}

			return false;
		}

	}
}
