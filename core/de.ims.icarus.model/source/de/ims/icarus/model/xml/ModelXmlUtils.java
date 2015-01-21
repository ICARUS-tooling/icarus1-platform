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
package de.ims.icarus.model.xml;

import java.util.List;

import javax.swing.Icon;

import org.java.plugin.registry.PluginDescriptor;
import org.xml.sax.Attributes;

import de.ims.icarus.eval.Expression;
import de.ims.icarus.eval.Variable;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest.Note;
import de.ims.icarus.model.api.manifest.Documentation.Resource;
import de.ims.icarus.model.api.manifest.DriverManifest.ModuleSpec;
import de.ims.icarus.model.api.manifest.IndexManifest;
import de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest;
import de.ims.icarus.model.api.manifest.LocationManifest;
import de.ims.icarus.model.api.manifest.LocationManifest.PathEntry;
import de.ims.icarus.model.api.manifest.ModifiableIdentity;
import de.ims.icarus.model.api.manifest.OptionsManifest.Option;
import de.ims.icarus.model.api.manifest.PathResolverManifest;
import de.ims.icarus.model.api.manifest.ValueManifest;
import de.ims.icarus.model.api.manifest.ValueRange;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.api.manifest.VersionManifest;
import de.ims.icarus.model.types.ValueType;
import de.ims.icarus.model.xml.sax.IconWrapper;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.date.DateUtils;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ModelXmlUtils implements ModelXmlAttributes, ModelXmlTags {

	//*******************************************
	//               WRITE METHODS
	//*******************************************

	public static String getSerializedForm(ValueType type) {
//		return type==ValueType.STRING ? null : type.getXmlValue();
		return type.getXmlValue();
	}

	public static void writePropertyElement(XmlSerializer serializer, String name, Object value, ValueType type) throws Exception {
		if(value==null) {
			return;
		}

		if(type==ValueType.UNKNOWN)
			throw new UnsupportedOperationException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		if(type==ValueType.CUSTOM)
			throw new UnsupportedOperationException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		serializer.startElement(TAG_PROPERTY);
		serializer.writeAttribute(ATTR_NAME, name);
		serializer.writeAttribute(ATTR_VALUE_TYPE, getSerializedForm(type));
		serializer.writeText(type.toString(value));
		serializer.endElement(TAG_PROPERTY);
	}

	public static void writeIdentityAttributes(XmlSerializer serializer, Identity identity) throws Exception {
		if(identity==null) {
			return;
		}

		writeIdentityAttributes(serializer, identity.getId(), identity.getName(), identity.getDescription(), identity.getIcon());
	}

	public static void writeIdentityAttributes(XmlSerializer serializer, String id, String name, String description, Icon icon) throws Exception {
		serializer.writeAttribute(ATTR_ID, id);
		serializer.writeAttribute(ATTR_NAME, name);
		serializer.writeAttribute(ATTR_DESCRIPTION, description);

		if(icon instanceof XmlResource) {
			serializer.writeAttribute(ATTR_ICON, ((XmlResource)icon).getXmlValue());
		} else if(icon != null) {
			LoggerFactory.warning(ModelXmlUtils.class, "Skipping serialization of icon for identity: "+(id==null ? id : "<unnamed>")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static String getSerializedForm(TargetLayerManifest manifest) {
		return manifest.getPrerequisite()!=null ?
				manifest.getPrerequisite().getAlias()
				: manifest.getResolvedLayerManifest().getId();
	}

	/**
	 * Writes the given target layer. Uses as layer id the alias of the prerequisite of the
	 * link if present, or the resolved layer's id otherwise.
	 */
	public static void writeTargetLayerManifestElement(XmlSerializer serializer, String name, TargetLayerManifest manifest) throws Exception {
		if(manifest==null) {
			return;
		}

		serializer.startEmptyElement(name);

		// ATTRIBUTES
		serializer.writeAttribute(ATTR_LAYER_ID, getSerializedForm(manifest));

		serializer.endElement(name);
	}

	public static void writeAliasElement(XmlSerializer serializer, String alias) throws Exception {
		serializer.startEmptyElement(TAG_ALIAS);
		serializer.writeAttribute(ATTR_NAME, alias);
		serializer.endElement(TAG_ALIAS);
	}

	public static void writeValueRangeElement(XmlSerializer serializer, ValueRange range, ValueType type) throws Exception {
		if(range==null) {
			return;
		}

		serializer.startElement(TAG_RANGE);

		// ATTRIBUTES
		if(range.isLowerBoundInclusive()!=ValueRange.DEFAULT_LOWER_INCLUSIVE_VALUE) {
			serializer.writeAttribute(ATTR_INCLUDE_MIN, range.isLowerBoundInclusive());
		}
		if(range.isUpperBoundInclusive()!=ValueRange.DEFAULT_UPPER_INCLUSIVE_VALUE) {
			serializer.writeAttribute(ATTR_INCLUDE_MAX, range.isUpperBoundInclusive());
		}

		// ELEMENTS

		writeValueElement(serializer, TAG_MIN, range.getLowerBound(), type);
		writeValueElement(serializer, TAG_MAX, range.getUpperBound(), type);
		writeValueElement(serializer, TAG_STEP_SIZE, range.getStepSize(), type);
		serializer.endElement(TAG_RANGE);
	}

	public static void writeValueSetElement(XmlSerializer serializer, ValueSet values, ValueType type) throws Exception {
		if(values==null) {
			return;
		}

		serializer.startElement(TAG_VALUES);
		for(int i=0; i<values.valueCount(); i++) {
			Object value = values.getValueAt(i);

			if(value instanceof ValueManifest) {
				writeValueManifestElement(serializer, (ValueManifest) value, type);
			} else {
				writeValueElement(serializer, TAG_VALUE, value, type);
			}
		}
		serializer.endElement(TAG_VALUES);
	}

	public static void writeValueManifestElement(XmlSerializer serializer, ValueManifest manifest, ValueType type) throws Exception {

		Object value = manifest.getValue();

		if(type==ValueType.UNKNOWN)
			throw new UnsupportedOperationException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		if(type==ValueType.CUSTOM)
			throw new UnsupportedOperationException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		serializer.startElement(TAG_VALUE);

		//ATTRIBUTES
		serializer.writeAttribute(ATTR_NAME, manifest.getName());
		serializer.writeAttribute(ATTR_DESCRIPTION, manifest.getDescription());

		// CONTENT

		writeValue(serializer, value, type);

		serializer.endElement(TAG_VALUE);
	}

	public static void writeValueElement(XmlSerializer serializer, String name, Object value, ValueType type) throws Exception {
		if(value==null) {
			return;
		}

		if(type==ValueType.UNKNOWN)
			throw new UnsupportedOperationException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		if(type==ValueType.CUSTOM)
			throw new UnsupportedOperationException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		serializer.startElement(name);

		// CONTENT

		writeValue(serializer, value, type);

		serializer.endElement(name);
	}

	private static void writeValue(XmlSerializer serializer, Object value, ValueType type) throws Exception {
		if(value instanceof Expression) {
			writeEvalElement(serializer, (Expression)value);
		} else {
			serializer.writeText(type.toString(value));
		}
	}

	public static void writeEvalElement(XmlSerializer serializer, Expression expression) throws Exception {
		serializer.startElement(TAG_EVAL);

		for(Variable variable : expression.getVariables()) {
			serializer.startEmptyElement(TAG_VARIABLE);
			serializer.writeAttribute(ATTR_NAME, variable.getName());
			serializer.writeAttribute(ATTR_NAMESPACE, variable.getNamespaceClass().getName());

			ClassLoader loader = variable.getNamespaceClass().getClassLoader();
			if(PluginUtil.isPluginClassLoader(loader)) {
				PluginDescriptor descriptor = PluginUtil.getDescriptor(loader);
				serializer.writeAttribute(ATTR_PLUGIN_ID, descriptor.getId());
			}

			serializer.endElement(TAG_VARIABLE);
		}

		serializer.startElement(TAG_CODE);
		serializer.writeCData(expression.getCode());
		serializer.endElement(TAG_CODE);

		serializer.endElement(TAG_EVAL);
	}

	public static void writeVersionElement(XmlSerializer serializer, VersionManifest version) throws Exception {
		if(version.getVersionString()==null)
			throw new IllegalArgumentException("Invalid version string in manifest"); //$NON-NLS-1$

		serializer.startElement(TAG_VERSION);

		// ATTRIBUTES
		serializer.writeAttribute(ATTR_VERSION_FORMAT, version.getFormatId());

		// CONTENT
		serializer.writeCData(version.getVersionString());

		serializer.endElement(TAG_VERSION);
	}

	public static void writeOptionElement(XmlSerializer serializer, Option option) throws Exception {

		ValueType type = option.getValueType();

		Object defaultValue = option.getDefaultValue();
		ValueSet valueSet = option.getSupportedValues();
		ValueRange valueRange = option.getSupportedRange();
		String extensionPointUid = option.getExtensionPointUid();

		if(defaultValue==null && valueSet==null && valueRange==null && extensionPointUid==null) {
			serializer.startEmptyElement(TAG_OPTION);
		} else {
			serializer.startElement(TAG_OPTION);
		}

		// Attributes

		writeIdentityAttributes(serializer, option);
		serializer.writeAttribute(ATTR_VALUE_TYPE, getSerializedForm(type));
		if(option.isPublished()!=Option.DEFAULT_PUBLISHED_VALUE) {
			serializer.writeAttribute(ATTR_PUBLISHED, option.isPublished());
		}
		if(option.isMultiValue()!=Option.DEFAULT_MULTIVALUE_VALUE) {
			serializer.writeAttribute(ATTR_MULTI_VALUE, option.isMultiValue());
		}
		serializer.writeAttribute(ATTR_GROUP, option.getOptionGroup());

		// Elements

		if(extensionPointUid!=null) {
			serializer.startElement(TAG_EXTENSION_POINT);
			serializer.writeText(extensionPointUid);
			serializer.endElement(TAG_EXTENSION_POINT);
		}

		if(defaultValue!=null) {
			writeValueElement(serializer, TAG_DEFAULT_VALUE, defaultValue, type);
		}

		if(valueSet!=null) {
			writeValueSetElement(serializer, valueSet, type);
		}

		if(valueRange!=null) {
			writeValueRangeElement(serializer, valueRange, type);
		}

		serializer.endElement(TAG_OPTION);
	}

	public static void writeLocationElement(XmlSerializer serializer, LocationManifest manifest) throws Exception {

		if(manifest.getPath()==null)
			throw new IllegalStateException("Location manifest is missing path"); //$NON-NLS-1$

		List<PathEntry> entries = manifest.getPathEntries();
		PathResolverManifest pathResolverManifest = manifest.getPathResolverManifest();

		serializer.startElement(TAG_LOCATION);

		// ATTRIBUTES

		serializer.startElement(TAG_PATH);
		serializer.writeCData(manifest.getPath());
		serializer.endElement(TAG_PATH);

		// ELEMENTS

		// Write path entries
		for(PathEntry pathEntry : entries) {
			writePathEntryElement(serializer, pathEntry);
		}

		// Write path resolver
		if(pathResolverManifest!=null) {
			pathResolverManifest.writeXml(serializer);
		}

		serializer.endElement(TAG_LOCATION);
	}

	public static void writePathEntryElement(XmlSerializer serializer, PathEntry pathEntry) throws Exception {
		if(pathEntry.getType()==null)
			throw new IllegalStateException("Path entry is missing type"); //$NON-NLS-1$
		if(pathEntry.getValue()==null)
			throw new IllegalStateException("Path entry is missing value"); //$NON-NLS-1$

		serializer.startElement(TAG_PATH_ENTRY);
		serializer.writeAttribute(ATTR_TYPE, pathEntry.getType().getXmlValue());
		serializer.writeCData(pathEntry.getValue());
		serializer.endElement(TAG_PATH_ENTRY);
	}

	public static void writeResourceElement(XmlSerializer serializer, Resource resource) throws Exception {
		if(resource.getUrl()==null)
			throw new IllegalStateException("Resource is missing url"); //$NON-NLS-1$

		serializer.startElement(TAG_RESOURCE);
		ModelXmlUtils.writeIdentityAttributes(serializer, resource);
		serializer.writeText(resource.getUrl().getURL().toExternalForm());
		serializer.endElement(TAG_RESOURCE);
	}

	public static void writePrerequisiteElement(XmlSerializer serializer, PrerequisiteManifest manifest) throws Exception {

		serializer.startEmptyElement(TAG_PREREQUISITE);

		// ATTRIBUTES

		serializer.writeAttribute(ATTR_CONTEXT_ID, manifest.getContextId());
		serializer.writeAttribute(ATTR_LAYER_ID, manifest.getLayerId());

		// Only write the layer type attribute for unresolved prerequisites!
		if(manifest.getUnresolvedForm()==null) {
			serializer.writeAttribute(ATTR_TYPE_ID, manifest.getTypeId());
		}

		serializer.writeAttribute(ATTR_ALIAS, manifest.getAlias());

		serializer.endElement(TAG_PREREQUISITE);
	}

	public static void writeNoteElement(XmlSerializer serializer, Note note) throws Exception {
		if(note==null) {
			return;
		}

		serializer.startElement(TAG_NOTE);

		// Attributes

		serializer.writeAttribute(ATTR_NAME, note.getName());
		serializer.writeAttribute(ATTR_DATE, DateUtils.formatDate(note.getModificationDate()));

		// Content

		serializer.writeText(note.getContent());

		serializer.endElement(TAG_NOTE);
	}

	public static void writeIndexElement(XmlSerializer serializer, IndexManifest manifest) throws Exception {

		if(manifest.getSourceLayerId()==null)
			throw new IllegalStateException("Index manifest is missing source layer id"); //$NON-NLS-1$
		if(manifest.getTargetLayerId()==null)
			throw new IllegalStateException("Index manifest is missing target layer id"); //$NON-NLS-1$
		if(manifest.getRelation()==null)
			throw new IllegalStateException("Index manifest is missing relation"); //$NON-NLS-1$
		if(manifest.getCoverage()==null)
			throw new IllegalStateException("Index manifest is missing coverage"); //$NON-NLS-1$

		serializer.startEmptyElement(TAG_INDEX);

		serializer.writeAttribute(ATTR_SOURCE_LAYER, manifest.getSourceLayerId());
		serializer.writeAttribute(ATTR_TARGET_LAYER, manifest.getTargetLayerId());
		serializer.writeAttribute(ATTR_RELATION, manifest.getRelation().getXmlValue());
		serializer.writeAttribute(ATTR_COVERAGE, manifest.getCoverage().getXmlValue());
		if(manifest.isIncludeReverse()!=IndexManifest.DEFAULT_INCLUDE_REVERSE_VALUE) {
			serializer.writeAttribute(ATTR_INCLUDE_REVERSE, manifest.isIncludeReverse());
		}

		serializer.endElement(TAG_INDEX);
	}

	public static void writeModuleSpecElement(XmlSerializer serializer, ModuleSpec spec) throws Exception {

		String extensionPointUid = spec.getExtensionPointUid();

		if(extensionPointUid==null) {
			serializer.startEmptyElement(TAG_MODULE_SPEC);
		} else {
			serializer.startElement(TAG_MODULE_SPEC);
		}

		// ATTRIBUTES

		writeIdentityAttributes(serializer, spec);

		if(spec.isCustomizable()!=ModuleSpec.DEFAULT_IS_CUSTOMIZABLE) {
			serializer.writeAttribute(ATTR_CUSTOMIZABLE, spec.isCustomizable());
		}

		if(spec.isOptional()!=ModuleSpec.DEFAULT_IS_OPTIONAL) {
			serializer.writeAttribute(ATTR_OPTIONAL, spec.isOptional());
		}

		// ELEMENTS

		if(extensionPointUid!=null) {
			serializer.startElement(TAG_EXTENSION_POINT);
			serializer.writeText(extensionPointUid);
			serializer.endElement(TAG_EXTENSION_POINT);
		}

		serializer.endElement(TAG_MODULE_SPEC);
	}

	//*******************************************
	//               READ METHOD
	//*******************************************

	public static void readIdentity(Attributes attr, ModifiableIdentity identity) {
		String id = normalize(attr, ATTR_ID);
		if(id!=null) {
			identity.setId(id);
		}

		String name = normalize(attr, ATTR_NAME);
		if(name!=null) {
			identity.setName(name);
		}

		String description = normalize(attr, ATTR_DESCRIPTION);
		if(description!=null) {
			identity.setDescription(description);
		}

		String icon = normalize(attr, ATTR_ICON);
		if(icon!=null) {
			identity.setIcon(iconValue(icon));
		}
	}

	public static Icon iconValue(String iconName) {
		return new IconWrapper(iconName);
	}

	public static Icon iconValue(Attributes attr, String key) {
		String icon = normalize(attr, key);
		return icon==null ? null : iconValue(icon);
	}

	public static String stringValue(Attributes attr, String key) {
		return normalize(attr, key);
	}

	public static long longValue(String s) {
		return Long.parseLong(s);
	}

	public static long longValue(Attributes attr, String key) {
		return longValue(normalize(attr, key));
	}

	public static double doubleValue(String s) {
		return Double.parseDouble(s);
	}

	public static double doubleValue(Attributes attr, String key) {
		return doubleValue(normalize(attr, key));
	}

	public static float floatValue(String s) {
		return Float.parseFloat(s);
	}

	public static float floatValue(Attributes attr, String key) {
		return floatValue(normalize(attr, key));
	}

	public static int intValue(String s) {
		return Integer.parseInt(s);
	}

	public static int intValue(Attributes attr, String key) {
		return intValue(normalize(attr, key));
	}

	public static boolean booleanValue(String s) {
		return s!=null && ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean booleanValue(Attributes attr, String key) {
		return booleanValue(normalize(attr, key));
	}

	public static boolean booleanValue(Attributes attr, String key, boolean defaultValue) {
		String s = normalize(attr, key);
		return s==null ? defaultValue : booleanValue(s);
	}

	public static ValueType typeValue(Attributes attr) {
		String s = normalize(attr, ATTR_VALUE_TYPE);
		return typeValue(s);
	}

	public static ValueType typeValue(String s) {
		return s==null ? ValueType.STRING : ValueType.parseValueType(s);
	}

	public static Boolean boolValue(Attributes attr, String key) {
		String s = normalize(attr, key);
		return s==null ? null : booleanValue(s);
	}

	public static String normalize(Attributes attr, String name) {
		String value = attr.getValue(name);

		return (value==null || value.isEmpty()) ? null : value;
	}
}
