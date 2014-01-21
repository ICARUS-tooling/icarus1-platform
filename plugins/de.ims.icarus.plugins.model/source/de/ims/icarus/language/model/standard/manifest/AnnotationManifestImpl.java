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

import de.ims.icarus.language.model.manifest.AnnotationManifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.language.model.manifest.ValueIterator;
import de.ims.icarus.language.model.manifest.ValueRange;
import de.ims.icarus.language.model.meta.ValueType;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationManifestImpl extends AbstractManifest<AnnotationManifest> implements AnnotationManifest {

	private List<String> aliases;
	private ValueType valueType = ValueType.UNKNOWN;
	private Values.ValueIteratorFactory valueIteratorFactory;
	private ValueRange valueRange;

	/**
	 * @see de.ims.icarus.language.model.manifest.Manifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.ANNOTATION_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationManifest#getAliases()
	 */
	@Override
	public List<String> getAliases() {
		checkTemplate();

		List<String> result = aliases;

		if(result==null) {
			result = Collections.emptyList();
		} else {
			result = CollectionUtils.getListProxy(result);
		}

		return result;
	}

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

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#templateLoaded(de.ims.icarus.language.model.manifest.Manifest)
	 */
	@Override
	protected void templateLoaded(AnnotationManifest template) {
		super.templateLoaded(template);

		for(String alias : template.getAliases()) {
			addAlias(alias);
		}
	}

	/**
	 * This implementation returns {@code true} when at least one of
	 * {@link ValueRange} or {@link Values.ValueIteratorFactory} previously
	 * assigned to this manifest is non-null.
	 *
	 * @see de.ims.icarus.language.model.manifest.AnnotationManifest#isBounded()
	 */
	@Override
	public boolean isBounded() {
		boolean bounded = valueRange!=null || valueIteratorFactory!=null;

		if(bounded && hasTemplate()) {
			bounded = getTemplate().isBounded();
		}

		return bounded;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationManifest#getSupportedRange()
	 */
	@Override
	public ValueRange getSupportedRange() {
		ValueRange valueRange = this.valueRange;

		if(valueRange==null && hasTemplate()) {
			valueRange = getTemplate().getSupportedRange();
		}

		return valueRange;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationManifest#getSupportedValues()
	 */
	@Override
	public ValueIterator getSupportedValues() {
		ValueIterator valueIterator = null;

		if(valueIteratorFactory!=null) {
			valueIterator = valueIteratorFactory.newIterator();
		}

		if(valueIterator==null && hasTemplate()) {
			valueIterator = getTemplate().getSupportedValues();
		}

		return valueIterator;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationManifest#getValueType()
	 */
	@Override
	public ValueType getValueType() {
		ValueType valueType = this.valueType;

		if(valueType==null && hasTemplate()) {
			valueType = getTemplate().getValueType();
		}

		return valueType;
	}

	/**
	 * @param valueType the valueType to set
	 */
	public void setValueType(ValueType valueType) {
		if (valueType == null)
			throw new NullPointerException("Invalid valueType"); //$NON-NLS-1$

		this.valueType = valueType;
	}

	/**
	 * @param valueIteratorFactory the valueIteratorFactory to set
	 */
	public void setValueIteratorFactory(
			Values.ValueIteratorFactory valueIteratorFactory) {
		if (valueIteratorFactory == null)
			throw new NullPointerException("Invalid valueIteratorFactory"); //$NON-NLS-1$

		this.valueIteratorFactory = valueIteratorFactory;
	}

	/**
	 * @param valueRange the valueRange to set
	 */
	public void setValueRange(ValueRange valueRange) {
		if (valueRange == null)
			throw new NullPointerException("Invalid valueRange"); //$NON-NLS-1$

		this.valueRange = valueRange;
	}

}
