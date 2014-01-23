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

import org.java.plugin.registry.Extension;

import de.ims.icarus.language.model.io.ContextReader;
import de.ims.icarus.language.model.io.ContextWriter;
import de.ims.icarus.language.model.manifest.ContextManifest;
import de.ims.icarus.language.model.manifest.CorpusManifest;
import de.ims.icarus.language.model.manifest.LayerManifest;
import de.ims.icarus.language.model.manifest.LocationManifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.language.model.xml.XmlWriter;
import de.ims.icarus.util.ClassProxy;
import de.ims.icarus.util.ClassUtils;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContextManifestImpl extends AbstractManifest<ContextManifest> implements ContextManifest {

	private final List<LayerManifest> layerManifests = new ArrayList<>();

	private Object readerSource;
	private Object writerSource;

	private ContextReader reader;
	private ContextWriter writer;

	private LocationManifest locationManifest;

	private boolean root;
	private final CorpusManifest corpusManifest;

	public ContextManifestImpl(CorpusManifest corpusManifest) {
		if (corpusManifest == null)
			throw new NullPointerException("Invalid corpusManifest"); //$NON-NLS-1$

		this.corpusManifest = corpusManifest;
	}

	public ContextManifestImpl(CorpusManifest corpusManifest, ContextManifest template) {
		super(template);

		if (corpusManifest == null)
			throw new NullPointerException("Invalid corpusManifest"); //$NON-NLS-1$

		this.corpusManifest = corpusManifest;
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

		layerManifests.add(layerManifest);
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#setName(java.lang.String)
	 */
	@Override
	public void setName(String newName) {
		throw new UnsupportedOperationException("Renaming not supported"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getCorpusManifest()
	 */
	@Override
	public CorpusManifest getCorpusManifest() {
		return corpusManifest;
	}

	private void setReader0(Object source) {
		if(reader!=null && reader.isReading())
			throw new IllegalStateException("Cannot change reader while loading context"); //$NON-NLS-1$

		if(source.equals(readerSource)) {
			return;
		}

		readerSource = source;
		reader = null;
	}

	public void setReader(String className) {
		if (className == null)
			throw new NullPointerException("Invalid reader classname"); //$NON-NLS-1$

		setReader0(className);
	}

	public void setReader(Class<? extends ContextReader> readerClass) {
		if (readerClass == null)
			throw new NullPointerException("Invalid reader class"); //$NON-NLS-1$

		setReader0(readerClass);
	}

	public void setReader(ClassProxy proxy) {
		if (proxy == null)
			throw new NullPointerException("Invalid proxy"); //$NON-NLS-1$

		setReader0(proxy);
	}

	public void setReader(Extension extension) {
		if (extension == null)
			throw new NullPointerException("Invalid extension"); //$NON-NLS-1$

		setReader0(extension);
	}

	private void loadReader() {
		if(reader!=null) {
			return;
		}

		if(readerSource==null)
			throw new IllegalStateException("No reader set for context: "+getName()); //$NON-NLS-1$

		try {
			reader = (ContextReader) ClassUtils.instantiate(readerSource);
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			throw new IllegalStateException("Failed to instantiate reader object: "+readerSource); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getReader()
	 */
	@Override
	public ContextReader getReader() {
		loadReader();

		return reader;
	}

	private void setWriter0(Object source) {
		if(writer!=null && writer.isWriting())
			throw new IllegalStateException("Cannot change writer while writing context"); //$NON-NLS-1$

		if(source.equals(writerSource)) {
			return;
		}

		writerSource = source;
		writer = null;
	}

	public void setWriter(String className) {
		if (className == null)
			throw new NullPointerException("Invalid writer classname"); //$NON-NLS-1$

		setWriter0(className);
	}

	public void setWriter(Class<? extends ContextWriter> writerClass) {
		if (writerClass == null)
			throw new NullPointerException("Invalid writer class"); //$NON-NLS-1$

		setWriter0(writerClass);
	}

	public void setWriter(ClassProxy proxy) {
		if (proxy == null)
			throw new NullPointerException("Invalid proxy"); //$NON-NLS-1$

		setWriter0(proxy);
	}

	public void setWriter(Extension extension) {
		if (extension == null)
			throw new NullPointerException("Invalid extension"); //$NON-NLS-1$

		setWriter0(extension);
	}

	private void loadWriter() {
		if(writer!=null) {
			return;
		}

		if(writerSource==null) {
			return;
		}

		try {
			writer = (ContextWriter) ClassUtils.instantiate(writerSource);
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			throw new IllegalStateException("Failed to instantiate writer object: "+writerSource); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getWriter()
	 */
	@Override
	public ContextWriter getWriter() {
		loadWriter();

		return writer;
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
		return root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(boolean root) {
		this.root = root;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#isDefaultContext()
	 */
	@Override
	public boolean isDefaultContext() {
		return corpusManifest.getDefaultContextManifest()==this;
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

		writeXmlAttribute(serializer, "independent", root, getTemplate().isIndependentContext()); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeFullXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		serializer.writeAttribute("independent", root); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeTemplateXmlElements(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		Set<LayerManifest> derived = new HashSet<>(getTemplate().getLayerManifests());

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
