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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.AnnotationManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.ManifestSource;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationLayerManifestImpl extends AbstractLayerManifest<AnnotationLayerManifest> implements AnnotationLayerManifest {

	private Map<String, AnnotationManifest> annotationManifests = new LinkedHashMap<>();
	private String defaultKey;
	private Boolean deepAnnotation;
	private Boolean allowUnknownKeys;
	private Boolean searchable;
	private Boolean indexable;

	/**
	 * @param manifestSource
	 * @param registry
	 * @param layerGroupManifest
	 */
	public AnnotationLayerManifestImpl(ManifestSource manifestSource,
			CorpusRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestSource, registry, layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractLayerManifest#writeAttributes(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		// Write default key
		serializer.writeAttribute(ATTR_DEFAULT_KEY, defaultKey);

		// Write flags
		writeFlag(serializer, ATTR_DEEP_ANNOTATION, deepAnnotation, DEFAULT_DEEP_ANNOTATION_VALUE);
		writeFlag(serializer, ATTR_UNKNOWN_KEYS, allowUnknownKeys, DEFAULT_ALLOW_UNKNOWN_KEYS_VALUE);
		writeFlag(serializer, ATTR_SERCH, searchable, DEFAULT_SEARCHABLE_VALUE);
		writeFlag(serializer, ATTR_INDEX, indexable, DEFAULT_INDEXABLE_VALUE);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractLayerManifest#writeElements(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		// Write annotation manifests
		if(annotationManifests!=null && !annotationManifests.isEmpty()) {
			List<String> keys = CollectionUtils.asSortedList(annotationManifests.keySet());

			for(String key : keys) {
				annotationManifests.get(key).writeXml(serializer);
			}
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractDerivable#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_ANNOTATION_LAYER;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.ANNOTATION_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#getAvailableKeys()
	 */
	@Override
	public Set<String> getAvailableKeys() {
		Set<String> keys = new HashSet<>();

		if(annotationManifests!=null) {
			keys.addAll(annotationManifests.keySet());
		}

		if(hasTemplate()) {
			keys.addAll(getTemplate().getAvailableKeys());
		}

		return keys;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#getAnnotationManifest(java.lang.String)
	 */
	@Override
	public AnnotationManifest getAnnotationManifest(String key) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$


		AnnotationManifest manifest = annotationManifests.get(key);

		if(manifest==null && hasTemplate()) {
			manifest = getTemplate().getAnnotationManifest(key);
		}

		if(manifest==null)
			throw new IllegalArgumentException("Unknown annotation key: "+key); //$NON-NLS-1$

		return manifest;
	}

//	@Override
	public void addAnnotationManifest(AnnotationManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		String key = manifest.getKey();

		if(annotationManifests.containsKey(key))
			throw new IllegalArgumentException("Duplicate manifest for annotation key: "+key); //$NON-NLS-1$

		annotationManifests.put(key, manifest);
	}

//	@Override
	public void removeAnnotationManifest(AnnotationManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		String key = manifest.getKey();

		if(annotationManifests==null || annotationManifests.remove(key)==null)
			throw new IllegalArgumentException("Unknown annotation manifest: "+key); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#getDefaultKey()
	 */
	@Override
	public String getDefaultKey() {
		String result = defaultKey;
		if(result==null && hasTemplate()) {
			result = getTemplate().getDefaultKey();
		}
		return result;
	}

	/**
	 * @param defaultAnnotationManifest the defaultAnnotationManifest to set
	 */
//	@Override
	public void setDefaultKey(String defaultKey) {
		this.defaultKey = defaultKey;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#isDeepAnnotation()
	 */
	@Override
	public boolean isDeepAnnotation() {
		if(deepAnnotation==null) {
			return hasTemplate() ? getTemplate().isDeepAnnotation() : DEFAULT_DEEP_ANNOTATION_VALUE;
		} else {
			return deepAnnotation.booleanValue();
		}
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#allowUnknownKeys()
	 */
	@Override
	public boolean allowUnknownKeys() {
		if(allowUnknownKeys==null) {
			return hasTemplate() ? getTemplate().allowUnknownKeys() : DEFAULT_ALLOW_UNKNOWN_KEYS_VALUE;
		} else {
			return allowUnknownKeys.booleanValue();
		}
	}

	/**
	 * @param deepAnnotation the deepAnnotation to set
	 */
//	@Override
	public void setDeepAnnotation(boolean deepAnnotation) {
		this.deepAnnotation = deepAnnotation;
	}

	/**
	 * @param allowUnknownKeys the allowUnknownKeys to set
	 */
//	@Override
	public void setAllowUnknownKeys(boolean allowUnknownKeys) {
		this.allowUnknownKeys = allowUnknownKeys;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#isIndexable()
	 */
	@Override
	public boolean isIndexable() {
		if(indexable==null) {
			return hasTemplate() ? getTemplate().isIndexable() : DEFAULT_INDEXABLE_VALUE;
		} else {
			return indexable.booleanValue();
		}
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#isSearchable()
	 */
	@Override
	public boolean isSearchable() {
		if(searchable==null) {
			return hasTemplate() ? getTemplate().isSearchable() : DEFAULT_SEARCHABLE_VALUE;
		} else {
			return searchable.booleanValue();
		}
	}

	/**
	 * @param searchable the searchable to set
	 */
//	@Override
	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	/**
	 * @param indexable the indexable to set
	 */
//	@Override
	public void setIndexable(boolean indexable) {
		this.indexable = indexable;
	}
}
