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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.LocationManifest;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContextManifestImpl extends AbstractMemberManifest<ContextManifest> implements ContextManifest {

	private final List<LayerManifest> layerManifests = new ArrayList<>();
	private final Map<String, LayerManifest> layerManifestLookup = new HashMap<>();

	private LocationManifest locationManifest;

	private boolean independent = false;
	private final CorpusManifest corpusManifest;
	private DriverManifest driverManifest;

	public ContextManifestImpl(CorpusManifest corpusManifest) {
		if (corpusManifest == null)
			throw new NullPointerException("Invalid corpusManifest"); //$NON-NLS-1$

		this.corpusManifest = corpusManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getLayerManifests()
	 */
	@Override
	public List<LayerManifest> getLayerManifests() {
		return CollectionUtils.getListProxy(layerManifests);
	}

	public void addLayerManifest(LayerManifest layerManifest) {
		if(layerManifest==null)
			throw new NullPointerException("Invalid layer manifest"); //$NON-NLS-1$
		if(layerManifests.contains(layerManifest))
			throw new IllegalArgumentException("Layer manifest already registered: "+layerManifest.getId()); //$NON-NLS-1$

		LayerManifest current = layerManifestLookup.get(layerManifest.getId());
		if(current!=null && !current.equals(layerManifest))
			throw new IllegalArgumentException("Duplicate layer manifests for rawId: "+layerManifest.getId()); //$NON-NLS-1$

		layerManifests.add(layerManifest);
		layerManifestLookup.put(layerManifest.getId(), layerManifest);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getLayerManifest(java.lang.String)
	 */
	@Override
	public LayerManifest getLayerManifest(String id) {
		if (id == null)
			throw new NullPointerException("Invalid rawId"); //$NON-NLS-1$

		LayerManifest layerManifest = layerManifestLookup.get(id);
		if(layerManifest==null)
			throw new IllegalArgumentException("No such layer: "+id); //$NON-NLS-1$

		return layerManifest;
	}

//	/**
//	 * @see de.ims.icarus.model.api.manifest.ContextManifest#setName(java.lang.String)
//	 */
//	@Override
//	public void setName(String newName) {
//		throw new UnsupportedOperationException("Renaming not supported"); //$NON-NLS-1$
//	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getCorpusManifest()
	 */
	@Override
	public CorpusManifest getCorpusManifest() {
		return corpusManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getLocationManifest()
	 */
	@Override
	public LocationManifest getLocationManifest() {
		return locationManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#setLocationManifest(de.ims.icarus.model.api.manifest.LocationManifest)
	 */
	@Override
	public void setLocationManifest(LocationManifest manifest) {
		if(isTemplate())
			throw new UnsupportedOperationException("Cannot assign location manifest to template"); //$NON-NLS-1$

		this.locationManifest = manifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#isIndependentContext()
	 */
	@Override
	public boolean isIndependentContext() {
		return independent;
	}

	/**
	 * @param independent the independent to set
	 */
	public void setIndependent(boolean independent) {
		this.independent = independent;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#isRootContext()
	 */
	@Override
	public boolean isRootContext() {
		return corpusManifest!=null && corpusManifest.getRootContextManifest()==this;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CONTEXT_MANIFEST;
	}

	public static class PrerequisiteManifestImpl implements PrerequisiteManifest {

		private final ContextManifest contextManifest;

		private String layerId;
		private String typeId;
		private String contextId;
		private final String alias;

		private PrerequisiteManifest unresolvedForm;

		public PrerequisiteManifestImpl(ContextManifest contextManifest, String alias) {
			if (contextManifest == null)
				throw new NullPointerException("Invalid contextManifest"); //$NON-NLS-1$
			if (alias == null)
				throw new NullPointerException("Invalid alias");  //$NON-NLS-1$

			this.contextManifest = contextManifest;
			this.alias = alias;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest#getContextManifest()
		 */
		@Override
		public ContextManifest getContextManifest() {
			return contextManifest;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest#getLayerId()
		 */
		@Override
		public String getLayerId() {
			return layerId;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest#getContextId()
		 */
		@Override
		public String getContextId() {
			return contextId;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest#getTypeId()
		 */
		@Override
		public String getTypeId() {
			return typeId;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest#getAlias()
		 */
		@Override
		public String getAlias() {
			return alias;
		}

		/**
		 * @return the unresolvedForm
		 */
		@Override
		public PrerequisiteManifest getUnresolvedForm() {
			return unresolvedForm;
		}

		/**
		 * @param unresolvedForm the unresolvedForm to set
		 */
		public void setUnresolvedForm(PrerequisiteManifest unresolvedForm) {
			this.unresolvedForm = unresolvedForm;
		}

		/**
		 * @param layerId the layerId to set
		 */
		public void setLayerId(String layerId) {
			this.layerId = layerId;
		}

		/**
		 * @param typeId the typeId to set
		 */
		public void setTypeId(String typeId) {
			this.typeId = typeId;
		}

		/**
		 * @param contextId the contextId to set
		 */
		public void setContextId(String contextId) {
			this.contextId = contextId;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return alias.hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof PrerequisiteManifest) {
				return alias.equals(((PrerequisiteManifest)obj).getAlias());
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Prerequisite:"+alias; //$NON-NLS-1$
		}

	}
}
