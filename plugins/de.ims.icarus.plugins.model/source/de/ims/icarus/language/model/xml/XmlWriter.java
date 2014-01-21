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
package de.ims.icarus.language.model.xml;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;

import de.ims.icarus.language.model.manifest.OptionsManifest;
import de.ims.icarus.language.model.manifest.ValueIterator;
import de.ims.icarus.language.model.manifest.ValueManifest;
import de.ims.icarus.language.model.manifest.ValueRange;
import de.ims.icarus.language.model.meta.ValueType;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class XmlWriter {

	public static void writeValueAttribute(XmlSerializer serializer, String name, Object value, ValueType type) throws IOException {
		switch (type) {
		case BOOLEAN:
			serializer.writeAttribute(name, (boolean)value);
			break;
		case DOUBLE:
			serializer.writeAttribute(name, (double)value);
			break;
		case INTEGER:
			serializer.writeAttribute(name, (int)value);
			break;
		case STRING:
			serializer.writeAttribute(name, (String)value);
			break;

		case UNKNOWN:
			throw new IllegalArgumentException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		case CUSTOM:
			throw new IllegalArgumentException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		}
	}

	public static void writeValueElement(XmlSerializer serializer, String name, Object value, ValueType type) throws IOException {
		serializer.startElement(name);

		switch (type) {
		case UNKNOWN:
			throw new IllegalArgumentException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		case CUSTOM:
			throw new IllegalArgumentException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		default:
			serializer.writeText(String.valueOf(value));
			break;
		}
		serializer.endElement(name);
	}

	private static boolean tryWrite(XmlSerializer serializer, Object object) throws IOException {
		if(object==null) {
			return true;
		}
		if(object instanceof XmlElement) {
			((XmlElement)object).writeXml(serializer);
			return true;
		}

		return false;
	}

	public static void writeObjectElement(XmlSerializer serializer, Object object) throws IOException {
		if(object==null) {
			return;
		}

		if(object instanceof XmlElement) {
			((XmlElement)object).writeXml(serializer);
		} else {
			LoggerFactory.warning(XmlWriter.class, "Unable to serialize object to xml: "+object.getClass()); //$NON-NLS-1$
		}
	}

	public static void writeOptionsManifestElement(XmlSerializer serializer, OptionsManifest manifest) throws IOException {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("options"); //$NON-NLS-1$

		for(String option : manifest.getOptionNames()) {
			ValueType type = manifest.getValueType(option);

			serializer.startElement("option"); //$NON-NLS-1$
			serializer.writeAttribute("id", option); //$NON-NLS-1$
			serializer.writeAttribute("type", type.name()); //$NON-NLS-1$
			serializer.writeAttribute("name", manifest.getName(option)); //$NON-NLS-1$
			serializer.writeAttribute("description", manifest.getDescription(option)); //$NON-NLS-1$
			writeValueElement(serializer, "default-value", manifest.getDefaultValue(option), type); //$NON-NLS-1$
			writeValueIteratorElement(serializer, manifest.getSupportedValues(option), type);
			writeValueRangeElement(serializer, manifest.getSupportedRange(option), type);

			serializer.endElement("option"); //$NON-NLS-1$
		}

		serializer.endElement("options"); //$NON-NLS-1$
	}

	public static void writeValueRangeElement(XmlSerializer serializer, ValueRange range, ValueType type) throws IOException {
		if(tryWrite(serializer, range)) {
			return;
		}

		serializer.startElement("range"); //$NON-NLS-1$
		writeValueAttribute(serializer, "min", range.getLowerBound(), type); //$NON-NLS-1$
		writeValueAttribute(serializer, "max", range.getUpperBound(), type); //$NON-NLS-1$
		serializer.writeAttribute("include-min", range.isLowerBoundInclusive()); //$NON-NLS-1$
		serializer.writeAttribute("include-max", range.isUpperBoundInclusive()); //$NON-NLS-1$
		serializer.endElement("range"); //$NON-NLS-1$
	}

	public static void writeValueIteratorElement(XmlSerializer serializer, ValueIterator iterator, ValueType type) throws IOException {
		if(tryWrite(serializer, iterator)) {
			return;
		}

		serializer.startElement("values"); //$NON-NLS-1$
		while(iterator.hasMoreValues()) {
			Object value = iterator.nextValue();

			if(value instanceof ValueManifest) {
				writeValueManifestElement(serializer, (ValueManifest) value, type);
			} else {
				writeValueElement(serializer, "value", value, type); //$NON-NLS-1$
			}
		}
		serializer.endElement("values"); //$NON-NLS-1$
	}

	public static void writeValueManifestElement(XmlSerializer serializer, ValueManifest manifest, ValueType type) throws IOException {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("value"); //$NON-NLS-1$
		serializer.writeAttribute("name", manifest.getName()); //$NON-NLS-1$
		serializer.writeAttribute("description", manifest.getDescription()); //$NON-NLS-1$

		Object value = manifest.getValue();

		switch (type) {
		case UNKNOWN:
			throw new IllegalArgumentException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		case CUSTOM:
			throw new IllegalArgumentException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		default:
			serializer.writeText(String.valueOf(value));
			break;
		}

		serializer.endElement("value"); //$NON-NLS-1$
	}

	public static void writeProperties(XmlSerializer serializer, Map<String, Object> properties, OptionsManifest manifest) throws IOException {
		if(properties==null || properties.isEmpty()) {
			return;
		}

		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		for(Entry<String, Object> entry : properties.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();

			if(value==null) {
				continue;
			}

			ValueType type = manifest.getValueType(name);
			writeValueElement(serializer, "property", value, type); //$NON-NLS-1$
		}
	}

	public static void writeIdentityAttributes(XmlSerializer serializer, Identity identity) throws IOException {
		if(tryWrite(serializer, identity)) {
			return;
		}

		serializer.writeAttribute("id", identity.getId()); //$NON-NLS-1$
		serializer.writeAttribute("name", identity.getName()); //$NON-NLS-1$
		serializer.writeAttribute("description", identity.getDescription()); //$NON-NLS-1$

		Icon icon = identity.getIcon();
		if(icon instanceof XmlResource) {
			serializer.writeAttribute("icon", ((XmlResource)icon).getValue()); //$NON-NLS-1$
		} else if(icon != null) {
			LoggerFactory.warning(XmlWriter.class, "Skipping serialization of icon for identity: "+identity); //$NON-NLS-1$
		}
	}
}
