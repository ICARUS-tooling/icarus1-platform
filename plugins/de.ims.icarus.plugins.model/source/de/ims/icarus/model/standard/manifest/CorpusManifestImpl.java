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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.model.xml.XmlWriter;
import de.ims.icarus.util.ClassUtils;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusManifestImpl extends AbstractManifest<CorpusManifest> implements CorpusManifest {

	private ContextManifest defaultContextManifest;
	private List<ContextManifest> contextManifests = new ArrayList<>(3);
	private Map<String, ContextManifest> contextManifestLookup = new HashMap<>();
	private boolean editable;

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CORPUS_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#getDefaultContextManifest()
	 */
	@Override
	public ContextManifest getDefaultContextManifest() {
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
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#getCustomContextManifests()
	 */
	@Override
	public List<ContextManifest> getCustomContextManifests() {
		return CollectionUtils.getListProxy(contextManifests);
	}

	/**
	 *
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#addCustomContextManifest(de.ims.icarus.model.api.manifest.ContextManifest)
	 */
	@Override
	public void addCustomContextManifest(ContextManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		contextManifests.add(manifest);
		contextManifestLookup.put(manifest.getId(), manifest);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#removeCustomContextManifest(de.ims.icarus.model.api.manifest.ContextManifest)
	 */
	@Override
	public void removeCustomContextManifest(ContextManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		if(!contextManifests.remove(manifest))
			throw new IllegalArgumentException("Unknown context manifest: "+manifest); //$NON-NLS-1$
		contextManifestLookup.remove(manifest.getId());
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#getContextManifest(java.lang.String)
	 */
	@Override
	public ContextManifest getContextManifest(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		ContextManifest contextManifest = contextManifestLookup.get(id);
		if(contextManifest==null)
			throw new IllegalArgumentException("No such context: "+id); //$NON-NLS-1$

		return contextManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#setEditable(boolean)
	 */
	@Override
	public void setEditable(boolean value) {
		this.editable = value;
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.model.api.standard.manifest.AbstractManifest#writeTemplateXmlAttributes(de.ims.icarus.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		writeXmlAttribute(serializer, "editable", editable, getTemplate().isEditable()); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.model.api.standard.manifest.AbstractManifest#writeFullXmlAttributes(de.ims.icarus.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		serializer.writeAttribute("editable", editable); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.model.api.standard.manifest.AbstractManifest#writeFullXmlElements(de.ims.icarus.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlElements(serializer);

		XmlWriter.writeContextManifestElement(serializer, defaultContextManifest);

		for(ContextManifest contextManifest : contextManifests) {
			XmlWriter.writeContextManifestElement(serializer, contextManifest);
		}
	}

	/**
	 * @see de.ims.icarus.model.api.standard.manifest.AbstractManifest#writeTemplateXmlElements(de.ims.icarus.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		if(!ClassUtils.equals(defaultContextManifest, getTemplate().getDefaultContextManifest())) {
			XmlWriter.writeContextManifestElement(serializer, defaultContextManifest);
		}

		Set<ContextManifest> derived = new HashSet<>(getTemplate().getCustomContextManifests());

		for(ContextManifest contextManifest : contextManifests) {
			if(derived.contains(contextManifest)) {
				continue;
			}

			XmlWriter.writeContextManifestElement(serializer, contextManifest);
		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.model.api.standard.manifest.AbstractDerivable#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return "corpus"; //$NON-NLS-1$
	}
}
