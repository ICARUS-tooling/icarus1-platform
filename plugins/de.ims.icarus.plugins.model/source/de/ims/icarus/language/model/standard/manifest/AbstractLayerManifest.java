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
package de.ims.icarus.language.model.standard.manifest;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.model.manifest.ContextManifest;
import de.ims.icarus.language.model.manifest.LayerManifest;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractLayerManifest<L extends LayerManifest> extends AbstractManifest<L> implements LayerManifest {

	private List<Prerequisite> prerequisites = new ArrayList<>(3);
	private Boolean indexable, searchable;
	private final ContextManifest contextManifest;

	protected AbstractLayerManifest(ContextManifest contextManifest) {
		if (contextManifest == null)
			throw new NullPointerException("Invalid contextManifest"); //$NON-NLS-1$
		this.contextManifest = contextManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LayerManifest#getContextManifest()
	 */
	@Override
	public ContextManifest getContextManifest() {
		return contextManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#templateLoaded(de.ims.icarus.language.model.manifest.Manifest)
	 */
	@Override
	protected void templateLoaded(L template) {
		super.templateLoaded(template);

		// Copy over all prerequisites
		for(Prerequisite prerequisite : template.getPrerequisites()) {
			if(!prerequisites.contains(prerequisite)) {
				prerequisites.add(prerequisite);
			}
		}
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LayerManifest#getPrerequisites()
	 */
	@Override
	public List<Prerequisite> getPrerequisites() {
		checkTemplate();
		return CollectionUtils.getListProxy(prerequisites);
	}

	public void addPrerequiriste(Prerequisite prerequisite) {
		if(prerequisite==null)
			throw new NullPointerException("Invalid prerequisite"); //$NON-NLS-1$

		if(prerequisites.contains(prerequisite))
			throw new IllegalArgumentException("Duplicate prerequisite: "+CorpusUtils.getName(prerequisite)); //$NON-NLS-1$

		prerequisites.add(prerequisite);
	}

	public void removePrerequisite(Prerequisite prerequisite) {
		if(prerequisite==null)
			throw new NullPointerException("Invalid prerequisite"); //$NON-NLS-1$

		if(!prerequisites.remove(prerequisite))
			throw new IllegalArgumentException("Unknown prerequisite: "+CorpusUtils.getName(prerequisite)); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LayerManifest#isIndexable()
	 */
	@Override
	public boolean isIndexable() {
		Boolean indexable = this.indexable;

		if(indexable==null && hasTemplate()) {
			indexable = Boolean.valueOf(getTemplate().isIndexable());
		}

		return indexable==null ? false : indexable.booleanValue();
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LayerManifest#isSearchable()
	 */
	@Override
	public boolean isSearchable() {
		Boolean searchable = this.searchable;

		if(searchable==null && hasTemplate()) {
			searchable = Boolean.valueOf(getTemplate().isSearchable());
		}

		return searchable==null ? false : searchable.booleanValue();
	}

	/**
	 * @param indexable the indexable to set
	 */
	public void setIndexable(boolean indexable) {
		this.indexable = Boolean.valueOf(indexable);
	}

	/**
	 * @param searchable the searchable to set
	 */
	public void setSearchable(boolean searchable) {
		this.searchable = Boolean.valueOf(searchable);
	}
}
