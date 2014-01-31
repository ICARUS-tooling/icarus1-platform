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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.language.model.manifest.AnnotationLayerManifest;
import de.ims.icarus.language.model.manifest.ContextManifest;
import de.ims.icarus.language.model.manifest.ContextReaderManifest;
import de.ims.icarus.language.model.manifest.ContextWriterManifest;
import de.ims.icarus.language.model.manifest.CorpusManifest;
import de.ims.icarus.language.model.manifest.LayerManifest;
import de.ims.icarus.language.model.manifest.LocationManifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.language.model.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.manifest.StructureLayerManifest;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.language.model.xml.XmlWriter;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContextManifestImpl extends AbstractManifest<ContextManifest> implements ContextManifest {

	private final List<LayerManifest> layerManifests = new ArrayList<>();
	private final Map<String, LayerManifest> layerManifestLookup = new HashMap<>();

	private ContextReaderManifest readerManifest;
	private ContextWriterManifest writerManifest;

	private LocationManifest locationManifest;

	private boolean independent;
	private String baseId;
	private ContextManifest baseContextManifest;
	private final CorpusManifest corpusManifest;

	public ContextManifestImpl(CorpusManifest corpusManifest) {
//		if (corpusManifest == null)
//			throw new NullPointerException("Invalid corpusManifest"); //$NON-NLS-1$

		this.corpusManifest = corpusManifest;
	}

	public ContextManifestImpl(CorpusManifest corpusManifest, ContextManifest template) {
		super(template);

//		if (corpusManifest == null)
//			throw new NullPointerException("Invalid corpusManifest"); //$NON-NLS-1$

		this.corpusManifest = corpusManifest;

		ContextManifest baseContextManifest = template.getBaseContext();

		for(LayerManifest layerManifest : template.getLayerManifests()) {
			addLayerManifest(wrap(layerManifest));
		}
	}

	private LayerManifest wrap(LayerManifest template) {
		switch (template.getManifestType()) {
		case ANNOTATION_LAYER_MANIFEST:
			return new AnnotationLayerManifestImpl(this, (AnnotationLayerManifest) template);
		case MARKABLE_LAYER_MANIFEST:
			return new MarkableLayerManifestImpl(this, (MarkableLayerManifest) template);
		case STRUCTURE_LAYER_MANIFEST:
			return new StructureLayerManifestImpl(this, (StructureLayerManifest) template);

		default:
			throw new IllegalArgumentException("Not a valid manifest type definition on layer template: "+template.getId()); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getLayerManifests()
	 */
	@Override
	public List<LayerManifest> getLayerManifests() {
		return CollectionUtils.getListProxy(layerManifests);
	}

	public void addLayerManifest(LayerManifest layerManifest) {
		if(layerManifest==null)
			throw new NullPointerException("Invalid layer manifest"); //$NON-NLS-1$
//		if(layerManifests.contains(layerManifest))
//			throw new IllegalArgumentException("Layer manifest already registered: "+layerManifest.getId()); //$NON-NLS-1$

		LayerManifest current = layerManifestLookup.get(layerManifest.getId());
		if(current!=null && !current.equals(layerManifest))
			throw new IllegalArgumentException("Duplicate layer manifests for id: "+layerManifest.getId()); //$NON-NLS-1$

		layerManifests.add(layerManifest);
		layerManifestLookup.put(layerManifest.getId(), layerManifest);
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getLayerManifest(java.lang.String)
	 */
	@Override
	public LayerManifest getLayerManifest(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		LayerManifest layerManifest = layerManifestLookup.get(id);
		if(layerManifest==null)
			throw new IllegalArgumentException("No such layer: "+id); //$NON-NLS-1$

		return layerManifest;
	}

//	/**
//	 * @see de.ims.icarus.language.model.manifest.ContextManifest#setName(java.lang.String)
//	 */
//	@Override
//	public void setName(String newName) {
//		throw new UnsupportedOperationException("Renaming not supported"); //$NON-NLS-1$
//	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getCorpusManifest()
	 */
	@Override
	public CorpusManifest getCorpusManifest() {
		return corpusManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getLocationManifest()
	 */
	@Override
	public LocationManifest getLocationManifest() {
		return locationManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#setLocationManifest(de.ims.icarus.language.model.manifest.LocationManifest)
	 */
	@Override
	public void setLocationManifest(LocationManifest manifest) {
		this.locationManifest = manifest;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#isIndependentContext()
	 */
	@Override
	public boolean isIndependentContext() {
		return independent;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getReaderManifest()
	 */
	@Override
	public ContextReaderManifest getReaderManifest() {
		return readerManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getWriterManifest()
	 */
	@Override
	public ContextWriterManifest getWriterManifest() {
		return writerManifest;
	}

	/**
	 * @param readerManifest the readerManifest to set
	 */
	public void setReaderManifest(ContextReaderManifest readerManifest) {
		if (readerManifest == null)
			throw new NullPointerException("Invalid readerManifest"); //$NON-NLS-1$

		this.readerManifest = readerManifest;
	}

	/**
	 * @param writerManifest the writerManifest to set
	 */
	public void setWriterManifest(ContextWriterManifest writerManifest) {
		if (writerManifest == null)
			throw new NullPointerException("Invalid writerManifest");  //$NON-NLS-1$

		this.writerManifest = writerManifest;
	}

	/**
	 * @param independent the independent to set
	 */
	public void setIndependent(boolean independent) {
		this.independent = independent;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getBaseContext()
	 */
	@Override
	public ContextManifest getBaseContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#isDefaultContext()
	 */
	@Override
	public boolean isDefaultContext() {
		return corpusManifest!=null && corpusManifest.getDefaultContextManifest()==this;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Manifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CONTEXT_MANIFEST;
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeTemplateXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		writeXmlAttribute(serializer, "independent", independent, getTemplate().isIndependentContext()); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeFullXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		serializer.writeAttribute("independent", independent); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeTemplateXmlElements(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		ContextManifest template = getTemplate();

		if(locationManifest!=null && !locationManifest.equals(template.getLocationManifest())) {
			XmlWriter.writeLocationManifestElement(serializer, locationManifest);
		}

		if(readerManifest!=null && !readerManifest.equals(template.getReaderManifest())) {
			XmlWriter.writeContextReaderManifestElement(serializer, readerManifest);
		}

		if(writerManifest!=null && !writerManifest.equals(template.getWriterManifest())) {
			XmlWriter.writeContextWriterManifestElement(serializer, writerManifest);
		}

		Set<LayerManifest> derived = new HashSet<>(template.getLayerManifests());

		for(LayerManifest layerManifest : layerManifests) {
			if(derived.contains(layerManifest)) {
				continue;
			}

			XmlWriter.writeLayerManifestElement(serializer, layerManifest);
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

		XmlWriter.writeLocationManifestElement(serializer, locationManifest);
		XmlWriter.writeContextReaderManifestElement(serializer, readerManifest);
		XmlWriter.writeContextWriterManifestElement(serializer, writerManifest);

		for(LayerManifest layerManifest : layerManifests) {
			XmlWriter.writeLayerManifestElement(serializer, layerManifest);
		}
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.DerivedObject#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return isDefaultContext() ? "default-context" : "context"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
