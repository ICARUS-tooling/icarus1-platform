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
import java.util.Collections;
import java.util.List;

import de.ims.icarus.language.model.manifest.ContextManifest;
import de.ims.icarus.language.model.manifest.CorpusManifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusManifestImpl extends AbstractManifest<CorpusManifest> implements CorpusManifest {

	private ContextManifest defaultContextManifest;
	private List<ContextManifest> contextManifests;
	private Boolean editable;

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#templateLoaded(de.ims.icarus.language.model.manifest.Manifest)
	 */
	@Override
	protected void templateLoaded(CorpusManifest template) {
		super.templateLoaded(template);

		for(ContextManifest contextManifest : template.getCustomContextManifests()) {
			addCustomContextManifest(contextManifest);
		}
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Manifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CORPUS_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.CorpusManifest#getDefaultContextManifest()
	 */
	@Override
	public ContextManifest getDefaultContextManifest() {
		ContextManifest defaultContextManifest = this.defaultContextManifest;

		if(defaultContextManifest==null && hasTemplate()) {
			defaultContextManifest = getTemplate().getDefaultContextManifest();
		}

		return defaultContextManifest;
	}

	/**
	 * @param defaultContextManifest the defaultContextManifest to set
	 */
	public void setDefaultContextManifest(ContextManifest defaultContextManifest) {
		if (defaultContextManifest == null)
			throw new NullPointerException("Invalid defaultContextManifest"); //$NON-NLS-1$

		this.defaultContextManifest = defaultContextManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.CorpusManifest#getCustomContextManifests()
	 */
	@Override
	public List<ContextManifest> getCustomContextManifests() {
		checkTemplate();
		List<ContextManifest> result = contextManifests;

		if(result==null) {
			result = Collections.emptyList();
		} else {
			result = CollectionUtils.getListProxy(result);
		}

		return result;
	}

	public void addCustomContextManifest(ContextManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		if(contextManifests==null) {
			contextManifests = new ArrayList<>(3);
		}

		contextManifests.add(manifest);
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.CorpusManifest#isEditable()
	 */
	@Override
	public boolean isEditable() {
		Boolean editable = this.editable;

		if(editable==null && hasTemplate()) {
			editable = Boolean.valueOf(getTemplate().isEditable());
		}

		return editable==null ? false : editable.booleanValue();
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.CorpusManifest#setEditable(boolean)
	 */
	@Override
	public void setEditable(boolean value) {
		this.editable = Boolean.valueOf(value);
	}
}
