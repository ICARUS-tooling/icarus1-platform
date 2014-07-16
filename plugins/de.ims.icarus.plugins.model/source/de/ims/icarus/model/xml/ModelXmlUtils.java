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

import javax.swing.Icon;

import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.eval.Expression;
import de.ims.icarus.eval.Variable;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest.Note;
import de.ims.icarus.model.api.manifest.IndexManifest;
import de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest;
import de.ims.icarus.model.api.manifest.LocationManifest;
import de.ims.icarus.model.api.manifest.OptionsManifest.Option;
import de.ims.icarus.model.api.manifest.PathResolverManifest;
import de.ims.icarus.model.api.manifest.ValueManifest;
import de.ims.icarus.model.api.manifest.ValueRange;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.date.DateUtils;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ModelXmlUtils implements ModelXmlAttributes, ModelXmlTags {

	public static String getSerializedForm(ValueType type) {
		return type==ValueType.STRING ? null : type.getXmlValue();
	}

	public static void writePropertyElement(XmlSerializer serializer, String name, Object value, ValueType type) throws Exception {
		if(value==null) {
			return;
		}

		serializer.startElement(TAG_PROPERTY);

		switch (type) {
		case UNKNOWN:
			throw new IllegalArgumentException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		case CUSTOM:
			throw new IllegalArgumentException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		default:
			serializer.writeAttribute(ATTR_NAME, name);
			serializer.writeAttribute(ATTR_TYPE, getSerializedForm(type));
			serializer.writeText(type.toString(value));
			break;
		}
		serializer.endElement(TAG_PROPERTY);
	}

	public static void writeIdentityAttributes(XmlSerializer serializer, Identity identity) throws Exception {
		if(identity==null) {
			return;
		}

		writeIdentityAttributes(serializer, identity.getId(), identity.getName(), identity.getDescription(), identity.getIcon());
	}

	public static void writeIdentityAttributes(XmlSerializer serializer, String id, String name, String description, Icon icon) throws Exception {
		if(icon==null) {
			return;
		}

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

		//FIXME empty element
		serializer.startElement(name);

		// ATTRIBUTES
		serializer.writeAttribute(ATTR_LAYER_ID, getSerializedForm(manifest));

		serializer.endElement(name);
	}

	public static void writeAliasElement(XmlSerializer serializer, String alias) throws Exception {
		//FIXME empty element
		serializer.startElement(TAG_ALIAS);
		serializer.writeAttribute(ATTR_NAME, alias);
		serializer.endElement(TAG_ALIAS);
	}

	public static void writeValueRangeElement(XmlSerializer serializer, ValueRange range, ValueType type) throws Exception {
		if(range==null) {
			return;
		}

		serializer.startElement(TAG_RANGE);
		serializer.writeAttribute(ATTR_INCLUDE_MIN, range.isLowerBoundInclusive());
		serializer.writeAttribute(ATTR_INCLUDE_MAX, range.isUpperBoundInclusive());
		writeValueElement(serializer, TAG_MIN, range.getLowerBound(), type);
		writeValueElement(serializer, TAG_MAX, range.getUpperBound(), type);
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

		serializer.startElement(TAG_VALUE);

		//ATTRIBUTES
		serializer.writeAttribute(ATTR_NAME, manifest.getName());
		serializer.writeAttribute(ATTR_DESCRIPTION, manifest.getDescription());

		// CONTENT

		Object value = manifest.getValue();

		switch (type) {
		case UNKNOWN:
			throw new IllegalArgumentException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		case CUSTOM:
			throw new IllegalArgumentException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		default:
			serializer.writeText(type.toString(value));
			break;
		}

		serializer.endElement(TAG_VALUE);
	}

	public static void writeValueElement(XmlSerializer serializer, String name, Object value, ValueType type) throws Exception {
		if(value==null) {
			return;
		}

		serializer.startElement(name);

		// CONTENT

		switch (type) {
		case UNKNOWN:
			throw new IllegalArgumentException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		case CUSTOM:
			throw new IllegalArgumentException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		default:
			if(value instanceof Expression) {
				writeEvalElement(serializer, (Expression)value);
			} else {
				serializer.writeText(type.toString(value));
			}
			break;
		}

		serializer.endElement(name);
	}

	public static void writeEvalElement(XmlSerializer serializer, Expression expression) throws Exception {
		serializer.startElement(TAG_EVAL);

		for(Variable variable : expression.getVariables()) {
			serializer.startElement(TAG_VARIABLE);
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
		serializer.writeText(expression.getCode());
		serializer.endElement(TAG_CODE);

		serializer.endElement(TAG_EVAL);
	}

	public static void writeOptionElement(XmlSerializer serializer, Option option) throws Exception {

		ValueType type = option.getValueType();

		serializer.startElement(TAG_OPTION);

		// Attributes

		serializer.writeAttribute(ATTR_ID, option.getId());
		serializer.writeAttribute(ATTR_TYPE, getSerializedForm(type));
		serializer.writeAttribute(ATTR_NAME, option.getName());
		serializer.writeAttribute(ATTR_DESCRIPTION, option.getDescription());
		if(option.isPublished()!=Option.DEFAULT_PUBLISHED_VALUE) {
			serializer.writeAttribute(ATTR_PUBLISHED, option.isPublished());
		}
		if(option.isMultiValue()!=Option.DEFAULT_MULTIVALUE_VALUE) {
			serializer.writeAttribute(ATTR_MULTI_VALUE, option.isMultiValue());
		}
		serializer.writeAttribute(ATTR_GROUP, option.getOptionGroup());

		// Elements

		writeValueElement(serializer, TAG_DEFAULT_VALUE, option.getDefaultValue(), type);
		writeValueSetElement(serializer, option.getSupportedValues(), type);
		writeValueRangeElement(serializer, option.getSupportedRange(), type);

		serializer.endElement(TAG_OPTION);
	}

	public static void writeLocationElement(XmlSerializer serializer, LocationManifest manifest) throws Exception {

		serializer.startElement(TAG_LOCATION);

		// ATTRIBUTES

		serializer.writeAttribute(ATTR_PATH, manifest.getPath());

		// ELEMENTS

		PathResolverManifest pathResolverManifest = manifest.getPathResolverManifest();
		if(pathResolverManifest!=null) {
			pathResolverManifest.writeXml(serializer);
		}

		serializer.endElement(TAG_LOCATION);
	}

	public static void writePrerequisiteElement(XmlSerializer serializer, PrerequisiteManifest manifest) throws Exception {

		serializer.startElement(TAG_PREREQUISITE);

		// ATTRIBUTES

		serializer.writeAttribute(ATTR_CONTEXT_ID, manifest.getContextId());
		serializer.writeAttribute(ATTR_LAYER_ID, manifest.getLayerId());

		// Only write the layer type attribute for unresolved prerequisites!
		if(manifest.getUnresolvedForm()==null) {
			serializer.writeAttribute(ATTR_LAYER_TYPE, manifest.getTypeId());
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

		// Never serialize inverted manifests, they are created by the parser dynamically!
		if(manifest.getOriginal()!=null) {
			return;
		}

		serializer.startElement(TAG_INDEX);

		serializer.writeAttribute(ATTR_SOURCE_LAYER, getSerializedForm(manifest.getSourceLayerManifest()));
		serializer.writeAttribute(ATTR_TARGET_LAYER, getSerializedForm(manifest.getTargetLayerManifest()));
		serializer.writeAttribute(ATTR_RELATION, manifest.getRelation().getXmlValue());
		serializer.writeAttribute(ATTR_COVERAGE, manifest.getCoverage().getXmlValue());
		if(manifest.getInverse()!=null) {
			serializer.writeAttribute(ATTR_INCLUDE_REVERSE, true);
		}

		serializer.endElement(TAG_INDEX);
	}
}
