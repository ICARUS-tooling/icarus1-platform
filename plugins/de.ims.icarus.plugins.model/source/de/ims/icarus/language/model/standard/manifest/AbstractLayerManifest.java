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

import de.ims.icarus.language.model.api.manifest.ContextManifest;
import de.ims.icarus.language.model.api.manifest.ContextManifest.PrerequisiteManifest;
import de.ims.icarus.language.model.api.manifest.LayerManifest;
import de.ims.icarus.language.model.api.manifest.MarkableLayerManifest;
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

	private List<PrerequisiteManifest> prerequisites = new ArrayList<>(3);
	private boolean indexable = true, searchable = true;
	private ContextManifest contextManifest;
	private MarkableLayerManifest baseLayerManifest;

	private String baseLayer;
	private String baseContext;

	/**
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractManifest#readTemplate(de.ims.icarus.language.model.api.manifest.MemberManifest)
	 */
	@Override
	protected void readTemplate(L template) {
		super.readTemplate(template);

		for(PrerequisiteManifest prerequisite : template.getPrerequisites()) {
			if(!prerequisites.contains(prerequisite)) {
				prerequisites.add(prerequisite);
			}
		}

		indexable |= template.isIndexable();
		searchable |= template.isSearchable();
	}

	/**
	 * @param contextManifest the contextManifest to set
	 */
	public void setContextManifest(ContextManifest contextManifest) {
		if (contextManifest == null)
			throw new NullPointerException("Invalid contextManifest"); //$NON-NLS-1$

		this.contextManifest = contextManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.LayerManifest#getContextManifest()
	 */
	@Override
	public ContextManifest getContextManifest() {
		return contextManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.LayerManifest#getPrerequisites()
	 */
	@Override
	public List<PrerequisiteManifest> getPrerequisites() {
		return CollectionUtils.getListProxy(prerequisites);
	}

	public void addPrerequisite(PrerequisiteManifest prerequisite) {
		if(prerequisite==null)
			throw new NullPointerException("Invalid prerequisite"); //$NON-NLS-1$

		if(prerequisites.contains(prerequisite))
			throw new IllegalArgumentException("Duplicate prerequisite: "+CorpusUtils.getName(prerequisite)); //$NON-NLS-1$

		prerequisites.add(prerequisite);
	}

	public void removePrerequisite(PrerequisiteManifest prerequisite) {
		if(prerequisite==null)
			throw new NullPointerException("Invalid prerequisite"); //$NON-NLS-1$

		if(!prerequisites.remove(prerequisite))
			throw new IllegalArgumentException("Unknown prerequisite: "+CorpusUtils.getName(prerequisite)); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.LayerManifest#isIndexable()
	 */
	@Override
	public boolean isIndexable() {
		return indexable;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.LayerManifest#isSearchable()
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
	 * @see de.ims.icarus.language.model.api.manifest.LayerManifest#getBaseLayerManifest()
	 */
	@Override
	public MarkableLayerManifest getBaseLayerManifest() {
		return baseLayerManifest;
	}

	/**
	 * @param baseLayerManifest the baseLayerManifest to set
	 */
	public void setBaseLayerManifest(MarkableLayerManifest baseLayerManifest) {
		if (baseLayerManifest == null)
			throw new NullPointerException("Invalid baseLayerManifest"); //$NON-NLS-1$

		this.baseLayerManifest = baseLayerManifest;
	}

	/**
	 * @return the baseLayer
	 */
	public String getBaseLayer() {
		return baseLayer;
	}

	/**
	 * @return the baseContext
	 */
	public String getBaseContext() {
		return baseContext;
	}

	/**
	 * @param baseLayer the baseLayer to set
	 */
	public void setBaseLayer(String baseLayer) {
		if (baseLayer == null)
			throw new NullPointerException("Invalid baseLayer");  //$NON-NLS-1$
		if(!isTemplate())
			throw new UnsupportedOperationException("Cannot define lazy base layer link"); //$NON-NLS-1$

		this.baseLayer = baseLayer;
	}

	/**
	 * @param baseContext the baseContext to set
	 */
	public void setBaseContext(String baseContext) {
		if (baseContext == null)
			throw new NullPointerException("Invalid baseContext");  //$NON-NLS-1$
		if(!isTemplate())
			throw new UnsupportedOperationException("Cannot define lazy base context link"); //$NON-NLS-1$

		this.baseContext = baseContext;
	}

	private void writeBaseXmlAttributes(XmlSerializer serializer) throws Exception {
		if(isTemplate()) {
			serializer.writeAttribute("base-layer", baseLayer); //$NON-NLS-1$
			serializer.writeAttribute("base-context", baseContext); //$NON-NLS-1$
		} else if(baseLayerManifest!=null) {
			ContextManifest baseContext = baseLayerManifest.getContextManifest();

			serializer.writeAttribute("base-layer", baseLayerManifest.getId()); //$NON-NLS-1$

			if(baseContext!=contextManifest) {
				serializer.writeAttribute("base-context", baseContext.getId()); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractManifest#writeTemplateXmlAttributes(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		writeXmlAttribute(serializer, "index", indexable, getTemplate().isIndexable()); //$NON-NLS-1$
		writeXmlAttribute(serializer, "search", searchable, getTemplate().isSearchable()); //$NON-NLS-1$

		writeBaseXmlAttributes(serializer);
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractManifest#writeFullXmlAttributes(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		serializer.writeAttribute("index", indexable); //$NON-NLS-1$
		serializer.writeAttribute("search", searchable); //$NON-NLS-1$

		writeBaseXmlAttributes(serializer);
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractManifest#writeTemplateXmlElements(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		Set<PrerequisiteManifest> tmp = new HashSet<>(prerequisites);
		tmp.removeAll(getTemplate().getPrerequisites());

		for(PrerequisiteManifest prerequisite : tmp) {
			XmlWriter.writePrerequisiteElement(serializer, prerequisite);
		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractManifest#writeFullXmlElements(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlElements(serializer);

		for(PrerequisiteManifest prerequisite : prerequisites) {
			XmlWriter.writePrerequisiteElement(serializer, prerequisite);
		}
	}
}
