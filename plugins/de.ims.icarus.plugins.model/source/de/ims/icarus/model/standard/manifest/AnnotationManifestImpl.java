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
import java.util.Collections;
import java.util.List;

import de.ims.icarus.model.api.manifest.AnnotationManifest;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.ValueRange;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationManifestImpl extends AbstractMemberManifest<AnnotationManifest> implements AnnotationManifest {

	private String key;
	private List<String> aliases;
	private ValueType valueType = ValueType.UNKNOWN;
	private ValueSet values;
	private ValueRange valueRange;
	private ContentType contentType;

	/**
	 * @return the key
	 */
	@Override
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	@Override
	public void setKey(String key) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$

		this.key = key;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.ANNOTATION_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationManifest#getAliases()
	 */
	@Override
	public List<String> getAliases() {
		List<String> result = aliases;

		if(result==null) {
			result = Collections.emptyList();
		} else {
			result = CollectionUtils.getListProxy(result);
		}

		return result;
	}

	@Override
	public void addAlias(String alias) {
		if (alias == null)
			throw new NullPointerException("Invalid alias"); //$NON-NLS-1$

		if(aliases==null) {
			aliases = new ArrayList<>(3);
		}

		if(aliases.contains(alias))
			throw new IllegalArgumentException("Alias already registered: "+alias); //$NON-NLS-1$

		aliases.add(alias);
	}

	@Override
	public void removeAlias(String alias) {
		if (alias == null)
			throw new NullPointerException("Invalid alias"); //$NON-NLS-1$

		if(aliases==null || !aliases.remove(alias))
			throw new IllegalArgumentException("Unknown alias: "+alias); //$NON-NLS-1$
	}

	/**
	 * This implementation returns {@code true} when at least one of
	 * {@link ValueRange} or {@link ValueSet} previously
	 * assigned to this manifest is non-null.
	 *
	 * @see de.ims.icarus.model.api.manifest.AnnotationManifest#isBounded()
	 */
	@Override
	public boolean isBounded() {
		return valueRange!=null || values!=null;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationManifest#getSupportedRange()
	 */
	@Override
	public ValueRange getSupportedRange() {
		return valueRange;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationManifest#getSupportedValues()
	 */
	@Override
	public ValueSet getSupportedValues() {
		return values;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationManifest#getValueType()
	 */
	@Override
	public ValueType getValueType() {
		return valueType;
	}

	/**
	 * @return the contentType
	 */
	@Override
	public ContentType getContentType() {
		return contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	@Override
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * @param valueType the valueType to set
	 */
	@Override
	public void setValueType(ValueType valueType) {
		if (valueType == null)
			throw new NullPointerException("Invalid valueType"); //$NON-NLS-1$

		this.valueType = valueType;
	}

	@Override
	public void setSupportedValues(ValueSet values) {
		if (values == null)
			throw new NullPointerException("Invalid values"); //$NON-NLS-1$

		this.values = values;
	}

	/**
	 * @param valueRange the valueRange to set
	 */
	@Override
	public void setSupportedRange(ValueRange valueRange) {
		if (valueRange == null)
			throw new NullPointerException("Invalid valueRange"); //$NON-NLS-1$

		this.valueRange = valueRange;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractDerivable#copyFrom(de.ims.icarus.model.api.manifest.Derivable)
	 */
	@Override
	protected void copyFrom(AnnotationManifest template) {
		super.copyFrom(template);

		key = template.getKey();
		for(String alias : template.getAliases()) {
			addAlias(alias);
		}
		valueType = template.getValueType();
		values = template.getSupportedValues();
		valueRange = template.getSupportedRange();
		contentType = template.getContentType();
	}
}
