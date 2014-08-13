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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.manifest.IndexManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.classes.ClassUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class IndexManifestImpl implements IndexManifest, ModelXmlHandler {

	private Coverage coverage;
	private Relation relation;

	private boolean includeReverse;

	private String sourceLayerId;
	private String targetLayerId;

	private final DriverManifest driverManifest;

	public IndexManifestImpl(DriverManifest driverManifest) {
		if (driverManifest == null)
			throw new NullPointerException("Invalid driverManifest");  //$NON-NLS-1$

		this.driverManifest = driverManifest;
	}


	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 1;

		if(coverage!=null) {
			hash *= coverage.hashCode();
		}

		if(relation!=null) {
			hash *= relation.hashCode();
		}

		if(sourceLayerId!=null) {
			hash *= sourceLayerId.hashCode();
		}

		if(targetLayerId!=null) {
			hash *= targetLayerId.hashCode();
		}

		return hash;
	}


	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof IndexManifest) {
			IndexManifest other = (IndexManifest) obj;
			return ClassUtils.equals(coverage, other.getCoverage())
					&& ClassUtils.equals(relation, other.getRelation())
					&& ClassUtils.equals(sourceLayerId, other.getSourceLayerId())
					&& ClassUtils.equals(targetLayerId, other.getTargetLayerId());
		}

		return false;
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("IndexManifest@[") //$NON-NLS-1$
		.append(coverage).append(',')
		.append(relation).append(',')
		.append(sourceLayerId).append(',')
		.append(targetLayerId).append(']');

		return sb.toString();
	}


	/**
	 * @see de.ims.icarus.model.xml.ModelXmlElement#writeXml(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		ModelXmlUtils.writeIndexElement(serializer, this);
	}

	/**
	 * @param attributes
	 */
	protected void readAttributes(Attributes attributes) {
		setCoverage(Coverage.parseCoverage(ModelXmlUtils.normalize(attributes, ATTR_COVERAGE)));
		setRelation(Relation.parseRelation(ModelXmlUtils.normalize(attributes, ATTR_RELATION)));

		String includeReverse = ModelXmlUtils.normalize(attributes, ATTR_INCLUDE_REVERSE);
		if(includeReverse!=null) {
			this.includeReverse = Boolean.parseBoolean(includeReverse);
		} else {
			this.includeReverse = DEFAULT_INCLUDE_REVERSE_VALUE;
		}

		setSourceLayerId(ModelXmlUtils.normalize(attributes, ATTR_SOURCE_LAYER));
		setTargetLayerId(ModelXmlUtils.normalize(attributes, ATTR_TARGET_LAYER));
	}


	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_INDEX: {
			readAttributes(attributes);
		} break;

		default:
			throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+TAG_INDEX+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return this;
	}

	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (qName) {
		case TAG_INDEX: {
			return null;
		}

		default:
			throw new SAXException("Unrecognized end tag  '"+qName+"' in "+TAG_INDEX+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {
		throw new SAXException("Unexpected nested element "+qName+" in "+TAG_INDEX+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.IndexManifest#getDriverManifest()
	 */
	@Override
	public DriverManifest getDriverManifest() {
		return driverManifest;
	}


	/**
	 * @see de.ims.icarus.model.api.manifest.IndexManifest#getSourceLayerId()
	 */
	@Override
	public String getSourceLayerId() {
		return sourceLayerId;
	}


	/**
	 * @see de.ims.icarus.model.api.manifest.IndexManifest#getTargetLayerId()
	 */
	@Override
	public String getTargetLayerId() {
		return targetLayerId;
	}


	/**
	 * @see de.ims.icarus.model.api.manifest.IndexManifest#getRelation()
	 */
	@Override
	public Relation getRelation() {
		return relation;
	}


	/**
	 * @see de.ims.icarus.model.api.manifest.IndexManifest#getCoverage()
	 */
	@Override
	public Coverage getCoverage() {
		return coverage;
	}


	/**
	 * @see de.ims.icarus.model.api.manifest.IndexManifest#isIncludeReverse()
	 */
	@Override
	public boolean isIncludeReverse() {
		return includeReverse;
	}


	/**
	 * @param coverage the coverage to set
	 */
	public void setCoverage(Coverage coverage) {
		if (coverage == null)
			throw new NullPointerException("Invalid coverage"); //$NON-NLS-1$

		this.coverage = coverage;
	}


	/**
	 * @param relation the relation to set
	 */
	public void setRelation(Relation relation) {
		if (relation == null)
			throw new NullPointerException("Invalid relation"); //$NON-NLS-1$

		this.relation = relation;
	}


	/**
	 * @param includeReverse the includeReverse to set
	 */
	public void setIncludeReverse(boolean includeReverse) {
		this.includeReverse = includeReverse;
	}


	/**
	 * @param sourceLayerId the sourceLayerId to set
	 */
	public void setSourceLayerId(String sourceLayerId) {
		if (sourceLayerId == null)
			throw new NullPointerException("Invalid sourceLayerId"); //$NON-NLS-1$

		this.sourceLayerId = sourceLayerId;
	}


	/**
	 * @param targetLayerId the targetLayerId to set
	 */
	public void setTargetLayerId(String targetLayerId) {
		if (targetLayerId == null)
			throw new NullPointerException("Invalid targetLayerId"); //$NON-NLS-1$

		this.targetLayerId = targetLayerId;
	}
}
