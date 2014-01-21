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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.language.model.manifest.AnnotationLayerManifest;
import de.ims.icarus.language.model.manifest.AnnotationManifest;
import de.ims.icarus.language.model.manifest.ContextManifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationLayerManifestImpl extends AbstractLayerManifest<AnnotationLayerManifest> implements AnnotationLayerManifest {

	private Map<String, AnnotationManifest> annotationManifests;
	private AnnotationManifest defaultAnnotationManifest;
	private Boolean deepAnnotation, allowUnknownKeys;

	public AnnotationLayerManifestImpl(ContextManifest contextManifest) {
		super(contextManifest);
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Manifest#getManifestType()
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

		checkTemplate();
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

		checkTemplate();
		if(annotationManifests==null)
			throw new UnsupportedOperationException();

		AnnotationManifest manifest = annotationManifests.get(key);
		if(manifest==null)
			throw new IllegalArgumentException("Unknown annotation key: "+key); //$NON-NLS-1$

		return manifest;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest#templateLoaded(de.ims.icarus.language.model.manifest.LayerManifest)
	 */
	@Override
	protected void templateLoaded(AnnotationLayerManifest template) {
		super.templateLoaded(template);

		// Copy over all annotation manifests
		for(String key : template.getAvailableKeys()) {
			addAnnotationManifest(key, template.getAnnotationManifest(key));
		}
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
		AnnotationManifest defaultAnnotationManifest = this.defaultAnnotationManifest;

		if(defaultAnnotationManifest==null && hasTemplate()) {
			defaultAnnotationManifest = getTemplate().getDefaultAnnotationManifest();
		}

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
		Boolean deepAnnotation = this.deepAnnotation;

		if(deepAnnotation==null && hasTemplate()) {
			deepAnnotation = Boolean.valueOf(getTemplate().isDeepAnnotation());
		}

		return deepAnnotation==null ? false : deepAnnotation.booleanValue();
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationLayerManifest#allowUnknownKeys()
	 */
	@Override
	public boolean allowUnknownKeys() {
		Boolean allowUnknownKeys = this.allowUnknownKeys;

		if(allowUnknownKeys==null && hasTemplate()) {
			allowUnknownKeys = Boolean.valueOf(getTemplate().allowUnknownKeys());
		}

		return allowUnknownKeys==null ? false : allowUnknownKeys.booleanValue();
	}

	/**
	 * @param deepAnnotation the deepAnnotation to set
	 */
	public void setDeepAnnotation(boolean deepAnnotation) {
		this.deepAnnotation = Boolean.valueOf(deepAnnotation);
	}

	/**
	 * @param allowUnknownKeys the allowUnknownKeys to set
	 */
	public void setAllowUnknownKeys(boolean allowUnknownKeys) {
		this.allowUnknownKeys = Boolean.valueOf(allowUnknownKeys);
	}

}
