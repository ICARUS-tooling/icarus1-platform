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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.language.model.manifest.AnnotationLayerManifest;
import de.ims.icarus.language.model.manifest.AnnotationManifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.language.model.xml.XmlWriter;
import de.ims.icarus.util.ClassUtils;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationLayerManifestImpl extends AbstractLayerManifest<AnnotationLayerManifest> implements AnnotationLayerManifest {

	private Map<String, AnnotationManifest> annotationManifests;
	private AnnotationManifest defaultAnnotationManifest;
	private boolean deepAnnotation, allowUnknownKeys;

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest#readTemplate(de.ims.icarus.language.model.manifest.LayerManifest)
	 */
	@Override
	protected void readTemplate(AnnotationLayerManifest template) {
		super.readTemplate(template);

		AnnotationManifest defaultAnnotationManifest = template.getDefaultAnnotationManifest();
		if(defaultAnnotationManifest!=null) {
			if(this.defaultAnnotationManifest==null) {
				this.defaultAnnotationManifest = new AnnotationManifestImpl();
			}
			this.defaultAnnotationManifest.setTemplate(defaultAnnotationManifest);
		}

		// Copy over all annotation manifests
		for(String key : template.getAvailableKeys()) {
			if(annotationManifests==null) {
				annotationManifests = new LinkedHashMap<>();
			}

			AnnotationManifest annotationManifest = annotationManifests.get(key);
			if(annotationManifest==null) {
				annotationManifest = new AnnotationManifestImpl();
				annotationManifests.put(key, annotationManifest);
			}

			annotationManifest.setTemplate(template.getAnnotationManifest(key));
		}

		deepAnnotation |= template.isDeepAnnotation();
		allowUnknownKeys |= template.allowUnknownKeys();
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.ANNOTATION_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationLayerManifest#getAvailableKeys()
	 */
	@Override
	public Set<String> getAvailableKeys() {
		Set<String> keys = null;

		if(annotationManifests!=null) {
			keys = annotationManifests.keySet();
		}

		if(keys==null) {
			keys = Collections.emptySet();
		} else {
			keys = CollectionUtils.getSetProxy(keys);
		}

		return keys;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationLayerManifest#getAnnotationManifest(java.lang.String)
	 */
	@Override
	public AnnotationManifest getAnnotationManifest(String key) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$

		if(annotationManifests==null)
			throw new UnsupportedOperationException();

		AnnotationManifest manifest = annotationManifests.get(key);
		if(manifest==null)
			throw new IllegalArgumentException("Unknown annotation key: "+key); //$NON-NLS-1$

		return manifest;
	}

	public void addAnnotationManifest(String key, AnnotationManifest manifest) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		if(annotationManifests==null) {
			annotationManifests = new LinkedHashMap<>();
		}

		if(annotationManifests.containsKey(key))
			throw new IllegalArgumentException("Duplicate manifest for annotation key: "+key); //$NON-NLS-1$

		annotationManifests.put(key, manifest);
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationLayerManifest#getDefaultAnnotationManifest()
	 */
	@Override
	public AnnotationManifest getDefaultAnnotationManifest() {
		return defaultAnnotationManifest;
	}

	/**
	 * @param defaultAnnotationManifest the defaultAnnotationManifest to set
	 */
	public void setDefaultAnnotationManifest(
			AnnotationManifest defaultAnnotationManifest) {
		if (defaultAnnotationManifest == null)
			throw new NullPointerException("Invalid defaultAnnotationManifest"); //$NON-NLS-1$

		this.defaultAnnotationManifest = defaultAnnotationManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationLayerManifest#isDeepAnnotation()
	 */
	@Override
	public boolean isDeepAnnotation() {
		return deepAnnotation;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationLayerManifest#allowUnknownKeys()
	 */
	@Override
	public boolean allowUnknownKeys() {
		return allowUnknownKeys;
	}

	/**
	 * @param deepAnnotation the deepAnnotation to set
	 */
	public void setDeepAnnotation(boolean deepAnnotation) {
		this.deepAnnotation = deepAnnotation;
	}

	/**
	 * @param allowUnknownKeys the allowUnknownKeys to set
	 */
	public void setAllowUnknownKeys(boolean allowUnknownKeys) {
		this.allowUnknownKeys = allowUnknownKeys;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractDerivable#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return "annotation-layer"; //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest#writeTemplateXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		writeXmlAttribute(serializer, "deep-annotation", deepAnnotation, getTemplate().isDeepAnnotation()); //$NON-NLS-1$
		writeXmlAttribute(serializer, "unknown-keys", allowUnknownKeys, getTemplate().allowUnknownKeys()); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest#writeFullXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		serializer.writeAttribute("deep-annotation", deepAnnotation); //$NON-NLS-1$
		serializer.writeAttribute("unknown-keys", allowUnknownKeys); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest#writeTemplateXmlElements(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		if(!ClassUtils.equals(defaultAnnotationManifest, getTemplate().getDefaultAnnotationManifest())) {
			XmlWriter.writeAnnotationManifestElement(serializer, defaultAnnotationManifest);
		}

		Set<String> derived = new HashSet<>(getTemplate().getAvailableKeys());
		for(String key : getAvailableKeys()) {
			if(derived.contains(key)
					&& ClassUtils.equals(getAnnotationManifest(key),
							getTemplate().getAnnotationManifest(key))) {
				continue;
			}

			XmlWriter.writeAnnotationManifestElement(serializer, getAnnotationManifest(key));
		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest#writeFullXmlElements(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlElements(serializer);

		XmlWriter.writeAnnotationManifestElement(serializer, defaultAnnotationManifest);

		for(String key : getAvailableKeys()) {
			XmlWriter.writeAnnotationManifestElement(serializer, getAnnotationManifest(key));
		}
	}

}
