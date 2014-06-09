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

import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;

import de.ims.icarus.language.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.language.model.api.manifest.AnnotationManifest;
import de.ims.icarus.language.model.api.manifest.ContainerManifest;
import de.ims.icarus.language.model.api.manifest.ContextManifest;
import de.ims.icarus.language.model.api.manifest.ContextReaderManifest;
import de.ims.icarus.language.model.api.manifest.ContextWriterManifest;
import de.ims.icarus.language.model.api.manifest.CorpusManifest;
import de.ims.icarus.language.model.api.manifest.Implementation;
import de.ims.icarus.language.model.api.manifest.LayerManifest;
import de.ims.icarus.language.model.api.manifest.LocationManifest;
import de.ims.icarus.language.model.api.manifest.ManifestType;
import de.ims.icarus.language.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.api.manifest.MemberManifest;
import de.ims.icarus.language.model.api.manifest.OptionsManifest;
import de.ims.icarus.language.model.api.manifest.PathResolverManifest;
import de.ims.icarus.language.model.api.manifest.Prerequisite;
import de.ims.icarus.language.model.api.manifest.StructureLayerManifest;
import de.ims.icarus.language.model.api.manifest.StructureManifest;
import de.ims.icarus.language.model.api.manifest.ValueIterator;
import de.ims.icarus.language.model.api.manifest.ValueManifest;
import de.ims.icarus.language.model.api.manifest.ValueRange;
import de.ims.icarus.language.model.api.manifest.ValueSet;
import de.ims.icarus.language.model.util.ValueType;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class XmlWriter {

	public static void writeValueAttribute(XmlSerializer serializer, String name, Object value, ValueType type) throws Exception {
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

	public static void writeValueElement(XmlSerializer serializer, String name, Object value, ValueType type) throws Exception {
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

	public static void writePropertyElement(XmlSerializer serializer, String name, Object value, ValueType type) throws Exception {
		serializer.startElement("property"); //$NON-NLS-1$

		switch (type) {
		case UNKNOWN:
			throw new IllegalArgumentException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		case CUSTOM:
			throw new IllegalArgumentException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		default:
			serializer.writeAttribute("name", name); //$NON-NLS-1$
			serializer.writeAttribute("type", type.getValue()); //$NON-NLS-1$
			serializer.writeText(String.valueOf(value));
			break;
		}
		serializer.endElement("property"); //$NON-NLS-1$
	}

	public static void writePrerequisiteElement(XmlSerializer serializer, PrerequisiteManifest prerequisite) throws Exception {
		if(prerequisite==null) {
			return;
		}

		if(prerequisite.getLayerId()!=null) {
			serializer.startElement("layer-prerequisite"); //$NON-NLS-1$
			serializer.writeAttribute("id", prerequisite.getLayerId()); //$NON-NLS-1$
			serializer.endElement("layer-prerequisite"); //$NON-NLS-1$
		} else if(prerequisite.getTypeId()!=null) {
			serializer.startElement("type-prerequisite"); //$NON-NLS-1$
			serializer.writeAttribute("type", prerequisite.getTypeId()); //$NON-NLS-1$
			serializer.endElement("type-prerequisite"); //$NON-NLS-1$
		} else
			throw new IllegalArgumentException("Not a valid prerequisite: "+prerequisite); //$NON-NLS-1$
	}

	private static boolean tryWrite(XmlSerializer serializer, Object object) throws Exception {
		if(object==null) {
			return true;
		}
		if(object instanceof XmlElement) {
			((XmlElement)object).writeXml(serializer);
			return true;
		}

		return false;
	}

	public static void writeObjectElement(XmlSerializer serializer, Object object) throws Exception {
		if(object==null) {
			return;
		}

		if(object instanceof XmlElement) {
			((XmlElement)object).writeXml(serializer);
		} else {
			LoggerFactory.warning(XmlWriter.class, "Unable to serialize object to xml: "+object.getClass()); //$NON-NLS-1$
		}
	}

	public static void writeObjectElement(XmlSerializer serializer, String name, Object object) throws Exception {
		if(object==null) {
			return;
		}

		if(object instanceof XmlElement) {
			((XmlElement)object).writeXml(serializer);
		} else {
			serializer.startElement(name);
			serializer.writeAttribute("class", object.getClass().getName()); //$NON-NLS-1$
			serializer.endElement(name);
		}
	}

	public static void writeOptionsManifestElement(XmlSerializer serializer, OptionsManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("options"); //$NON-NLS-1$

		for(String option : manifest.getOptionNames()) {
			writeOptionElement(serializer, option, manifest);
		}

		serializer.endElement("options"); //$NON-NLS-1$
	}

	public static void writeAliasElement(XmlSerializer serializer, String alias) throws Exception {
		serializer.startEmptyElement("alias"); //$NON-NLS-1$
		serializer.writeAttribute("name", alias); //$NON-NLS-1$
		serializer.endElement("alias"); //$NON-NLS-1$
	}

	public static void writeOptionElement(XmlSerializer serializer, String option, OptionsManifest manifest) throws Exception {
		ValueType type = manifest.getValueType(option);

		serializer.startElement("option"); //$NON-NLS-1$
		serializer.writeAttribute("id", option); //$NON-NLS-1$
		serializer.writeAttribute("type", type.getValue()); //$NON-NLS-1$
		serializer.writeAttribute("name", manifest.getName(option)); //$NON-NLS-1$
		serializer.writeAttribute("description", manifest.getDescription(option)); //$NON-NLS-1$
		if(!manifest.isPublished(option)) {
			serializer.writeAttribute("published", false); //$NON-NLS-1$
		}
		writeValueElement(serializer, "default-value", manifest.getDefaultValue(option), type); //$NON-NLS-1$
		writeValuesElement(serializer, manifest.getSupportedValues(option), type);
		writeValueRangeElement(serializer, manifest.getSupportedRange(option), type);

		serializer.endElement("option"); //$NON-NLS-1$
	}

	public static void writeValueRangeElement(XmlSerializer serializer, ValueRange range, ValueType type) throws Exception {
		if(tryWrite(serializer, range)) {
			return;
		}

		serializer.startEmptyElement("range"); //$NON-NLS-1$
		serializer.writeAttribute("include-min", range.isLowerBoundInclusive()); //$NON-NLS-1$
		serializer.writeAttribute("include-max", range.isUpperBoundInclusive()); //$NON-NLS-1$
		writeValueElement(serializer, "min", range.getLowerBound(), type); //$NON-NLS-1$
		writeValueElement(serializer, "max", range.getUpperBound(), type); //$NON-NLS-1$
		serializer.endElement("range"); //$NON-NLS-1$
	}

	public static void writeValueIteratorElement(XmlSerializer serializer, ValueIterator iterator, ValueType type) throws Exception {
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

	public static void writeValuesElement(XmlSerializer serializer, ValueSet values, ValueType type) throws Exception {
		if(tryWrite(serializer, values)) {
			return;
		}

		serializer.startElement("values"); //$NON-NLS-1$
		serializer.writeAttribute("id", values.getId()); //$NON-NLS-1$
		for(int i=0; i<values.valueCount(); i++) {
			Object value = values.getValueAt(i);

			if(value instanceof ValueManifest) {
				writeValueManifestElement(serializer, (ValueManifest) value, type);
			} else {
				writeValueElement(serializer, "value", value, type); //$NON-NLS-1$
			}
		}
		serializer.endElement("values"); //$NON-NLS-1$
	}

	public static void writeValueManifestElement(XmlSerializer serializer, ValueManifest manifest, ValueType type) throws Exception {
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

	public static void writeAnnotationManifestElement(XmlSerializer serializer, AnnotationManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("annotation"); //$NON-NLS-1$
		writeIdentityAttributes(serializer, manifest);
		serializer.writeAttribute("key", manifest.getKey()); //$NON-NLS-1$

		for(String alias : manifest.getAliases()) {
			writeAliasElement(serializer, alias);
		}

		writeValuesElement(serializer, manifest.getSupportedValues(), manifest.getValueType());
		writeValueRangeElement(serializer, manifest.getSupportedRange(), manifest.getValueType());

		serializer.endElement("annotation"); //$NON-NLS-1$
	}

	public static void writeContainerManifestElement(XmlSerializer serializer, ContainerManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("container"); //$NON-NLS-1$
		writeIdentityAttributes(serializer, manifest);
		serializer.writeAttribute("container-type", manifest.getContainerType().getValue()); //$NON-NLS-1$

		writeContainerManifestElement(serializer, manifest.getElementManifest());

		serializer.endElement("container"); //$NON-NLS-1$
	}

	public static void writeStructureManifestElement(XmlSerializer serializer, StructureManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("structure"); //$NON-NLS-1$
		writeIdentityAttributes(serializer, manifest);
		serializer.writeAttribute("container-type", manifest.getContainerType().getValue()); //$NON-NLS-1$
		serializer.writeAttribute("structure-type", manifest.getStructureType().getValue()); //$NON-NLS-1$

		ContainerManifest boundary = manifest.getBoundaryContainerManifest();
		if(boundary!=null) {
			serializer.writeAttribute("boundary", boundary.getId()); //$NON-NLS-1$
		}

		writeContainerManifestElement(serializer, manifest.getElementManifest());

		serializer.endElement("structure"); //$NON-NLS-1$
	}

	private static void writeDefaultManifestElements(XmlSerializer serializer, MemberManifest manifest) throws Exception {
		OptionsManifest optionsManifest = manifest.getOptionsManifest();
		if(optionsManifest!=null) {
			for(String name : manifest.getPropertyNames()) {
				writePropertyElement(serializer, name,
						manifest.getProperty(name), optionsManifest.getValueType(name));
			}

			writeOptionsManifestElement(serializer, optionsManifest);
		}

		Implementation implementation = manifest.getImplementation();
		if(implementation!=null) {
			implementation.writeXml(serializer);
		}
	}

	private static void writeDefaultLayerAttributes(XmlSerializer serializer, LayerManifest manifest) throws Exception {

		writeIdentityAttributes(serializer, manifest);

		serializer.writeAttribute("index", manifest.isIndexable()); //$NON-NLS-1$
		serializer.writeAttribute("search", manifest.isSearchable()); //$NON-NLS-1$
	}

	public static void writeAnnotationLayerManifestElement(XmlSerializer serializer, AnnotationLayerManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("annotation-layer"); //$NON-NLS-1$
		writeDefaultLayerAttributes(serializer, manifest);
		serializer.writeAttribute("deep-annotation", manifest.isDeepAnnotation()); //$NON-NLS-1$
		serializer.writeAttribute("unknown-keys", manifest.allowUnknownKeys()); //$NON-NLS-1$

		writeDefaultManifestElements(serializer, manifest);

		writeAnnotationManifestElement(serializer, manifest.getDefaultAnnotationManifest());
		for(String key : manifest.getAvailableKeys()) {
			writeAnnotationManifestElement(serializer, manifest.getAnnotationManifest(key));
		}

		serializer.endElement("annotation-layer"); //$NON-NLS-1$
	}

	public static void writeMarkableLayerManifestElement(XmlSerializer serializer, MarkableLayerManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("markable-layer"); //$NON-NLS-1$
		writeDefaultLayerAttributes(serializer, manifest);

		writeDefaultManifestElements(serializer, manifest);

		for(int i=0; i<manifest.getContainerDepth(); i++) {
			writeContainerManifestElement(serializer, manifest.getContainerManifest(i));
		}

		serializer.endElement("markable-layer"); //$NON-NLS-1$
	}

	public static void writeStructureLayerManifestElement(XmlSerializer serializer, StructureLayerManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("structure-layer"); //$NON-NLS-1$
		writeDefaultLayerAttributes(serializer, manifest);

		MarkableLayerManifest boundary = manifest.getBoundaryLayerManifest();
		if(boundary!=null) {
			serializer.writeAttribute("boundary", boundary.getId()); //$NON-NLS-1$
		}

		writeDefaultManifestElements(serializer, manifest);

		for(int i=0; i<manifest.getContainerDepth(); i++) {
			ContainerManifest containerManifest = manifest.getContainerManifest(i);

			if(containerManifest.getManifestType()==ManifestType.STRUCTURE_MANIFEST) {
				writeStructureManifestElement(serializer, (StructureManifest) containerManifest);
			} else {
				writeContainerManifestElement(serializer, containerManifest);
			}
		}

		serializer.endElement("structure-layer"); //$NON-NLS-1$
	}

	public static void writeLocationManifestElement(XmlSerializer serializer, LocationManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("location"); //$NON-NLS-1$

		serializer.writeAttribute("location-type", manifest.getType().getValue()); //$NON-NLS-1$

		writeValueElement(serializer, "path", manifest.getPath(), ValueType.STRING); //$NON-NLS-1$

		PathResolverManifest pathResolverManifest = manifest.getPathResolverManifest();
		if(pathResolverManifest!=null) {
			writePathResolverManifestElement(serializer, pathResolverManifest);
		}

		serializer.endElement("location"); //$NON-NLS-1$
	}


	public static void writePathResolverManifestElement(XmlSerializer serializer, PathResolverManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("path-resolver"); //$NON-NLS-1$

		writeDefaultManifestElements(serializer, manifest);

		serializer.endElement("path-resolver"); //$NON-NLS-1$
	}

	public static void writeLayerManifestElement(XmlSerializer serializer, LayerManifest manifest) throws Exception {

		switch (manifest.getManifestType()) {
		case MARKABLE_LAYER_MANIFEST:
			writeMarkableLayerManifestElement(serializer, (MarkableLayerManifest) manifest);
			break;

		case STRUCTURE_LAYER_MANIFEST:
			writeStructureLayerManifestElement(serializer, (StructureLayerManifest) manifest);
			break;

		case ANNOTATION_LAYER_MANIFEST:
			writeAnnotationLayerManifestElement(serializer, (AnnotationLayerManifest) manifest);
			break;

		default:
			return;
		}
	}

	public static void writeContextReaderManifestElement(XmlSerializer serializer, ContextReaderManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("context-reader"); //$NON-NLS-1$

		writeIdentityAttributes(serializer, manifest);
		serializer.writeAttribute("format", manifest.getFormatId()); //$NON-NLS-1$

		writeDefaultManifestElements(serializer, manifest);

		serializer.endElement("context-reader"); //$NON-NLS-1$
	}

	public static void writeContextWriterManifestElement(XmlSerializer serializer, ContextWriterManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("context-writer"); //$NON-NLS-1$

		writeIdentityAttributes(serializer, manifest);
		serializer.writeAttribute("format", manifest.getFormatId()); //$NON-NLS-1$

		writeDefaultManifestElements(serializer, manifest);

		serializer.endElement("context-writer"); //$NON-NLS-1$
	}

	public static void writeContextManifestElement(XmlSerializer serializer, ContextManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		String tag = manifest.isDefaultContext() ? "default-context" : "context"; //$NON-NLS-1$ //$NON-NLS-2$

		serializer.startElement(tag);

		writeIdentityAttributes(serializer, manifest);
		serializer.writeAttribute("independent", manifest.isIndependentContext()); //$NON-NLS-1$

		writeDefaultManifestElements(serializer, manifest);
		writeLocationManifestElement(serializer, manifest.getLocationManifest());
		writeContextReaderManifestElement(serializer, manifest.getReaderManifest());
		writeContextWriterManifestElement(serializer, manifest.getWriterManifest());

		for(LayerManifest layerManifest : manifest.getLayerManifests()) {
			writeLayerManifestElement(serializer, layerManifest);
		}

		serializer.endElement(tag);
	}

	public static void writeCorpusManifestElement(XmlSerializer serializer, CorpusManifest manifest) throws Exception {
		if(tryWrite(serializer, manifest)) {
			return;
		}

		serializer.startElement("corpus"); //$NON-NLS-1$

		writeIdentityAttributes(serializer, manifest);
		serializer.writeAttribute("editable", manifest.isEditable()); //$NON-NLS-1$

		writeDefaultManifestElements(serializer, manifest);

		writeContextManifestElement(serializer, manifest.getDefaultContextManifest());

		for(ContextManifest contextManifest : manifest.getCustomContextManifests()) {
			writeContextManifestElement(serializer, contextManifest);
		}

		serializer.endElement("corpus"); //$NON-NLS-1$
	}

	public static void writeProperties(XmlSerializer serializer,
			Map<String, Object> properties, OptionsManifest manifest) throws Exception {
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
			writePropertyElement(serializer, name, value, type);
		}
	}

	public static void writeIdentityAttributes(XmlSerializer serializer, Identity identity) throws Exception {
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
