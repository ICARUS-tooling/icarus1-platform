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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.ims.icarus.language.model.manifest.ContextManifest;
import de.ims.icarus.language.model.manifest.LayerManifest;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.language.model.xml.XmlWriter;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractLayerManifest<L extends LayerManifest> extends AbstractManifest<L> implements LayerManifest {

	private List<Prerequisite> prerequisites = new ArrayList<>(3);
	private boolean indexable, searchable;
	private final ContextManifest contextManifest;

	protected AbstractLayerManifest(ContextManifest contextManifest) {
		if (contextManifest == null)
			throw new NullPointerException("Invalid contextManifest"); //$NON-NLS-1$
		this.contextManifest = contextManifest;
	}

	protected AbstractLayerManifest(ContextManifest contextManifest, L template) {
		super(template);

		if (contextManifest == null)
			throw new NullPointerException("Invalid contextManifest"); //$NON-NLS-1$
		this.contextManifest = contextManifest;

		for(Prerequisite prerequisite : template.getPrerequisites()) {
			if(!prerequisites.contains(prerequisite)) {
				prerequisites.add(prerequisite);
			}
		}

		indexable = template.isIndexable();
		searchable = template.isSearchable();
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LayerManifest#getContextManifest()
	 */
	@Override
	public ContextManifest getContextManifest() {
		return contextManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LayerManifest#getPrerequisites()
	 */
	@Override
	public List<Prerequisite> getPrerequisites() {
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
		return indexable;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LayerManifest#isSearchable()
	 */
	@Override
	public boolean isSearchable() {
		return searchable;
	}

	/**
	 * @param indexable the indexable to set
	 */
	public void setIndexable(boolean indexable) {
		this.indexable = indexable;
	}

	/**
	 * @param searchable the searchable to set
	 */
	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeTemplateXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		writeXmlAttribute(serializer, "indexable", indexable, getTemplate().isIndexable()); //$NON-NLS-1$
		writeXmlAttribute(serializer, "searchable", searchable, getTemplate().isSearchable()); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeFullXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		serializer.writeAttribute("indexable", indexable); //$NON-NLS-1$
		serializer.writeAttribute("searchable", searchable); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeTemplateXmlElements(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		Set<Prerequisite> tmp = new HashSet<>(prerequisites);
		tmp.removeAll(getTemplate().getPrerequisites());

		for(Prerequisite prerequisite : tmp) {
			XmlWriter.writePrerequisiteElement(serializer, prerequisite);
		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeFullXmlElements(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlElements(serializer);

		for(Prerequisite prerequisite : prerequisites) {
			XmlWriter.writePrerequisiteElement(serializer, prerequisite);
		}
	}
}
