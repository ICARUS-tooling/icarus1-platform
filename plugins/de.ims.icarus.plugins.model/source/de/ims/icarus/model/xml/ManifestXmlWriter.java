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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.rowset.spi.XmlWriter;
import javax.swing.Icon;

import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.eval.Expression;
import de.ims.icarus.eval.Variable;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.model.api.layer.LayerType;
import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.AnnotationManifest;
import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.Derivable;
import de.ims.icarus.model.api.manifest.Documentation;
import de.ims.icarus.model.api.manifest.Documentation.Resource;
import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.manifest.FragmentLayerManifest;
import de.ims.icarus.model.api.manifest.HighlightLayerManifest;
import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.api.manifest.ImplementationManifest.SourceType;
import de.ims.icarus.model.api.manifest.IndexManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest;
import de.ims.icarus.model.api.manifest.LocationManifest;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.model.api.manifest.ModifiableManifest;
import de.ims.icarus.model.api.manifest.OptionsManifest;
import de.ims.icarus.model.api.manifest.OptionsManifest.Option;
import de.ims.icarus.model.api.manifest.PathResolverManifest;
import de.ims.icarus.model.api.manifest.RasterizerManifest;
import de.ims.icarus.model.api.manifest.StructureLayerManifest;
import de.ims.icarus.model.api.manifest.StructureManifest;
import de.ims.icarus.model.api.manifest.ValueManifest;
import de.ims.icarus.model.api.manifest.ValueRange;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.io.LocationType;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.ClassUtils;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestXmlWriter implements ModelXmlTags, ModelXmlAttributes {

	private final XmlSerializer serializer;
	private final boolean templateMode;
	private final String rootTag;

	public ManifestXmlWriter(XmlSerializer serializer, boolean templateMode) {
		if (serializer == null)
			throw new NullPointerException("Invalid serializer"); //$NON-NLS-1$

		this.serializer = serializer;
		this.templateMode = templateMode;

		rootTag = templateMode ? TAG_TEMPLATES : TAG_CORPORA;
	}

	private int depth = 0;

	// Called for all manifests except the top level corpus manifests!
	private void startManifest(String tag) throws Exception {
		if(depth==0 && !templateMode && !TAG_CORPUS.equals(tag))
			throw new IllegalStateException("Only '"+TAG_CORPUS+"' elements allowed on top level outside template mode: "+tag); //$NON-NLS-1$ //$NON-NLS-2$

		depth++;

		serializer.startElement(tag);
	}

	private void endManifest(String tag) throws Exception {
		depth--;

		serializer.endElement(tag);
	}

	private void checkTemplate(Derivable derivable) {
		if (derivable == null)
			throw new NullPointerException("Invalid derivable"); //$NON-NLS-1$

		if(depth==0 && derivable.isTemplate()!=templateMode)
			throw new IllegalArgumentException("Illegal element (template mode mismatch): "+derivable.getId()); //$NON-NLS-1$
	}

	public void beginDocument() throws Exception {
		serializer.startDocument();
		serializer.startElement(rootTag);
	}

	public void endDocument() throws Exception {
		serializer.endElement(rootTag);
		serializer.endDocument();
		serializer.close();
	}

//	private boolean tryWrite(Object object) throws Exception {
//		if(object==null) {
//			return true;
//		}
//		if(object instanceof ModelXmlElement) {
//			((ModelXmlElement)object).writeXml(serializer);
//			return true;
//		}
//
//		return false;
//	}

	/**
	 * Returns the value to be serialized if a template value is given.
	 */
	private static <T extends Object> T diff(T value, T template) {
		return (value!=null && !value.equals(template)) ? value : null;
	}


	//**************************************************
	//		MANIFEST PARTS USABLE FOR TEMPLATING
	//**************************************************

	/**
	 * Write id, name, description and icon from the given identity after
	 * making a diff check against the optional template identity.
	 */
	private void writeIdentityAttributes(Identity identity) throws Exception {

		String id = identity.getId();
		String name = identity.getName();
		String description = identity.getDescription();
		Icon icon = identity.getIcon();

		Identity template = null;
		if(identity instanceof Derivable) {
			template = (Identity) ((Derivable)identity).getTemplate();
		}

		if(template!=null) {
			id = diff(id, template.getId());
			name = diff(name, template.getName());
			description = diff(description, template.getDescription());
			icon = diff(icon, template.getIcon());
		}

		serializer.writeAttribute(ATTR_ID, id);
		serializer.writeAttribute(ATTR_NAME, name);
		serializer.writeAttribute(ATTR_DESCRIPTION, description);

		if(icon instanceof XmlResource) {
			serializer.writeAttribute(ATTR_ICON, ((XmlResource)icon).getXmlValue());
		} else if(icon != null) {
			LoggerFactory.warning(XmlWriter.class, "Skipping serialization of icon for identity: "+identity); //$NON-NLS-1$
		}
	}

//	/**
//	 * Write template id if the given derivable is depending on a template
//	 */
//	private void writeTemplateAttribute(Derivable derivable) throws Exception {
//		Derivable template = derivable.getTemplate();
//
//		if(template!=null) {
//			serializer.writeAttribute(ATTR_TEMPLATE_ID, template.getId());
//		}
//	}

	/**
	 * Write options manifest and properties, after making diff check against template.
	 */
	private void writeModifiableManifest(ModifiableManifest manifest) throws Exception {
		ModifiableManifest template = (ModifiableManifest) manifest.getTemplate();

		OptionsManifest optionsManifest = manifest.getOptionsManifest();
		Documentation documentation = manifest.getDocumentation();

		Set<String> properties = manifest.getPropertyNames();

		if(template!=null) {
			serializer.writeAttribute(ATTR_TEMPLATE_ID, template.getId());

			optionsManifest = diff(optionsManifest, template.getOptionsManifest());
			documentation = diff(documentation, template.getDocumentation());

			properties = new HashSet<>(properties);

			for(Iterator<String> it = properties.iterator(); it.hasNext();) {
				String property = it.next();

				Object value = diff(manifest.getProperty(property), template.getProperty(property));
				if(value==null) {
					it.remove();
				}
			}
		}

		// Elements

		if(documentation!=null) {
			writeDocumentationElement(documentation);
		}

		if(optionsManifest!=null) {
			writeOptionsManifest(optionsManifest);
		}

		if(!properties.isEmpty()) {
			List<String> names = CollectionUtils.asSortedList(properties);

			for(String name : names) {
				ValueType type = optionsManifest==null ?
						ValueType.STRING : optionsManifest.getOption(name).getValueType();

				writePropertyElement(name, manifest.getProperty(name), type);
			}
		}
	}

	private String getSerializedForm(TargetLayerManifest manifest) {
		return manifest.getPrerequisite()!=null ?
				manifest.getPrerequisite().getAlias()
				: manifest.getResolvedLayerManifest().getId();
	}

	/**
	 * Writes the given target layer. Uses as layer id the alias of the prerequisite of the
	 * link if present, or the resolved layer's id otherwise.
	 */
	private void writeTargetLayerManifestElement(String name, TargetLayerManifest manifest) throws Exception {
		if(manifest==null) {
			return;
		}

		//FIXME empty element
		serializer.startElement(name);

		// ATTRIBUTES
		serializer.writeAttribute(ATTR_LAYER_ID, getSerializedForm(manifest));

		serializer.endElement(name);
	}

	private void writeLayerTypeAttribute(LayerManifest manifest) throws Exception {
		LayerType layerType = manifest.getLayerType();
		if(layerType!=null && manifest.getTemplate()!=null) {
			layerType = diff(layerType, ((LayerManifest)manifest.getTemplate()).getLayerType());
		}

		// ATTRIBUTES
		if(layerType!=null) {
			serializer.writeAttribute(ATTR_LAYER_TYPE, layerType.getId());
		}
	}

	private void writeAnnotationKeyAttribute(FragmentLayerManifest manifest) throws Exception {
		String annotationKey = manifest.getAnnotationKey();
		if(annotationKey!=null && manifest.getTemplate()!=null) {
			annotationKey = diff(annotationKey, ((FragmentLayerManifest)manifest.getTemplate()).getAnnotationKey());
		}

		// ATTRIBUTES
		if(annotationKey!=null) {
			serializer.writeAttribute(ATTR_ANNOTATION_KEY, annotationKey);
		}
	}

	private void writeLocationAttribute(DriverManifest manifest) throws Exception {
		LocationType locationType = manifest.getLocationType();
		if(locationType!=null && manifest.getTemplate()!=null) {
			locationType = diff(locationType, ((DriverManifest)manifest.getTemplate()).getLocationType());
		}

		// ATTRIBUTES
		if(locationType!=null) {
			serializer.writeAttribute(ATTR_LOCATION_TYPE, locationType.getXmlValue());
		}
	}

	private void writeBaseLayerElements(LayerManifest manifest) throws Exception {
		List<TargetLayerManifest> layers = manifest.getBaseLayerManifests();
		LayerManifest template = (LayerManifest) manifest.getTemplate();

		if(template!=null && !template.getBaseLayerManifests().isEmpty()) {
			layers = new ArrayList<>();
			layers.removeAll(template.getBaseLayerManifests());
		}

		// ELEMENTS
		for(TargetLayerManifest targetLayerManifest : layers) {
			writeTargetLayerManifestElement(TAG_BASE_LAYER, targetLayerManifest);
		}
	}

	private void writeContainerManifestElement(ContainerManifest manifest) throws Exception {
		if(manifest==null) {
			return;
		}

		serializer.startElement(TAG_CONTAINER);

		// ATTRIBUTES
		writeIdentityAttributes(manifest);
		serializer.writeAttribute(ATTR_CONTAINER_TYPE, manifest.getContainerType().getXmlValue());

		// ELEMENTS
		writeModifiableManifest(manifest);

		serializer.endElement(TAG_CONTAINER);
	}

	private void writeStructureManifestElement(StructureManifest manifest) throws Exception {
		if(manifest==null) {
			return;
		}

		serializer.startElement(TAG_STRUCTURE);

		// ATTRIBUTES
		writeIdentityAttributes(manifest);
		serializer.writeAttribute(ATTR_CONTAINER_TYPE, manifest.getContainerType().getXmlValue());
		serializer.writeAttribute(ATTR_STRUCTURE_TYPE, manifest.getStructureType().getXmlValue());

		StructureManifest template = (StructureManifest) manifest.getTemplate();
		boolean multiRoot = manifest.isMultiRootAllowed();
		if(multiRoot && template!=null && !template.isMultiRootAllowed()) {
			serializer.writeAttribute(ATTR_MULTI_ROOT, true);
		}

		// ELEMENTS
		writeModifiableManifest(manifest);

		serializer.endElement(TAG_STRUCTURE);
	}

	private void writeBoundaryLayerElement(MarkableLayerManifest manifest) throws Exception {

		TargetLayerManifest boundaryLayer = manifest.getBoundaryLayerManifest();
		if(boundaryLayer!=null && manifest.getTemplate()!=null) {
			boundaryLayer = diff(boundaryLayer, ((MarkableLayerManifest)manifest.getTemplate()).getBoundaryLayerManifest());
		}

		writeTargetLayerManifestElement(TAG_BOUNDARY_LAYER, boundaryLayer);
	}

	private void writeValueLayerElement(FragmentLayerManifest manifest) throws Exception {

		TargetLayerManifest valueLayer = manifest.getBoundaryLayerManifest();
		if(valueLayer!=null && manifest.getTemplate()!=null) {
			valueLayer = diff(valueLayer, ((FragmentLayerManifest)manifest.getTemplate()).getValueLayerManifest());
		}

		writeTargetLayerManifestElement(TAG_VALUE_LAYER, valueLayer);
	}

	private void writeNestedContainerElements(MarkableLayerManifest manifest) throws Exception {
		if(manifest==null) {
			return;
		}

		for(int i=0; i<manifest.getContainerDepth(); i++) {
			ContainerManifest container = manifest.getContainerManifest(i);

			if(container.getManifestType()==ManifestType.STRUCTURE_MANIFEST) {
				writeStructureManifestElement((StructureManifest) container);
			} else {
				writeContainerManifestElement(container);
			}
		}
	}

	private void writeMarkableLayerManifest0(String name, MarkableLayerManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(name);

		// ATTRIBUTES

		// Write default stuff
		writeIdentityAttributes(manifest);
		writeLayerTypeAttribute(manifest);

		// ELEMENTS
		writeModifiableManifest(manifest);

		// Write base layers
		writeBaseLayerElements(manifest);

		// Write boundary layer
		writeBoundaryLayerElement(manifest);

		// Write containers
		writeNestedContainerElements(manifest);

		endManifest(name);
	}


	//**************************************************
	//		STATIC MANIFEST PARTS
	//**************************************************

	private void writeLayerGroupElement(LayerGroupManifest manifest) throws Exception {

		serializer.startElement(TAG_LAYER_GROUP);

		// ATTRIBUTES
		serializer.writeAttribute(ATTR_NAME, manifest.getName());
		if(manifest.isIndependent()) {
			serializer.writeAttribute(ATTR_INDEPENDENT, true);
		}
		serializer.writeAttribute(ATTR_PRIMARY_LAYER, manifest.getPrimaryLayerManifest().getId());

		// ELEMENTS

		// Write layers
		for(LayerManifest layerManifest : manifest.getLayerManifests()) {
			switch (layerManifest.getManifestType()) {
			case MARKABLE_LAYER_MANIFEST:
				writeMarkableLayerManifest((MarkableLayerManifest) layerManifest);
				break;
			case STRUCTURE_LAYER_MANIFEST:
				writeStructureLayerManifest((StructureLayerManifest) layerManifest);
				break;
			case ANNOTATION_LAYER_MANIFEST:
				writeAnnotationLayerManifest((AnnotationLayerManifest) layerManifest);
				break;
			case FRAGMENT_LAYER_MANIFEST:
				writeFragmentLayerManifest((FragmentLayerManifest) layerManifest);
				break;
			case HIGHLIGHT_LAYER_MANIFEST:
				writeHighlightLayerManifest((HighlightLayerManifest) layerManifest);
				break;

			default:
				throw new CorruptedStateException("Illegal manifest type for layer ' " //$NON-NLS-1$
							+layerManifest.getId()+"': "+layerManifest.getManifestType()); //$NON-NLS-1$
			}
		}

		serializer.endElement(TAG_LAYER_GROUP);
	}

	private void writeDocumentationElement(Documentation documentation) throws Exception {

		serializer.startElement(TAG_DOCUMENTATION);

		writeIdentityAttributes(documentation);

		serializer.startElement(TAG_CONTENT);
		serializer.writeText(documentation.getContent());
		serializer.endElement(TAG_CONTENT);

		for(Resource resource : documentation.getResources()) {
			serializer.startElement(TAG_RESOURCE);
			writeIdentityAttributes(resource);
			serializer.writeText(resource.getURL().toExternalForm());
			serializer.endElement(TAG_RESOURCE);
		}

		serializer.endElement(TAG_DOCUMENTATION);
	}

	private void writeLocationElement(LocationManifest manifest) throws Exception {

		serializer.startElement(TAG_LOCATION);

		// ATTRIBUTES

		serializer.writeAttribute(ATTR_PATH, manifest.getPath());

		// ELEMENTS

		PathResolverManifest pathResolverManifest = manifest.getPathResolverManifest();
		if(pathResolverManifest!=null) {
			writePathResolverManifest(pathResolverManifest);
		}

		serializer.endElement(TAG_LOCATION);
	}

	private void writePrerequisiteElement(PrerequisiteManifest manifest) throws Exception {

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

	private void writeIndexElement(IndexManifest manifest) throws Exception {

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

	private void writeOptionElement(Option option) throws Exception {

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

		writeValueElement(TAG_DEFAULT_VALUE, option.getDefaultValue(), type);
		writeValueSetElement(option.getSupportedValues(), type);
		writeValueRangeElement(option.getSupportedRange(), type);

		serializer.endElement(TAG_OPTION);
	}

	private void writeValueElement(String name, Object value, ValueType type) throws Exception {
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
				writeEvalElement((Expression)value);
			} else {
				serializer.writeText(type.toString(value));
			}
			break;
		}

		serializer.endElement(name);
	}

	private void writeEvalElement(Expression expression) throws Exception {
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

	private static String getSerializedForm(ValueType type) {
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

	private void writeValueRangeElement(ValueRange range, ValueType type) throws Exception {
		if(range==null) {
			return;
		}

		serializer.startElement(TAG_RANGE);
		serializer.writeAttribute(ATTR_INCLUDE_MIN, range.isLowerBoundInclusive());
		serializer.writeAttribute(ATTR_INCLUDE_MAX, range.isUpperBoundInclusive());
		writeValueElement(TAG_MIN, range.getLowerBound(), type);
		writeValueElement(TAG_MAX, range.getUpperBound(), type);
		serializer.endElement(TAG_RANGE);
	}

	private void writeValueSetElement(ValueSet values, ValueType type) throws Exception {
		if(values==null) {
			return;
		}

		serializer.startElement(TAG_VALUES);
		for(int i=0; i<values.valueCount(); i++) {
			Object value = values.getValueAt(i);

			if(value instanceof ValueManifest) {
				writeValueManifestElement((ValueManifest) value, type);
			} else {
				writeValueElement(TAG_VALUE, value, type);
			}
		}
		serializer.endElement(TAG_VALUES);
	}

	private void writeValueManifestElement(ValueManifest manifest, ValueType type) throws Exception {

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


	//**************************************************
	//		PUBLIC MANIFEST SERIALIZATION
	//**************************************************

	public void writeCorpusManifest(CorpusManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_CORPUS);

		// Write default stuff
		writeIdentityAttributes(manifest);
		writeModifiableManifest(manifest);

		// Root context and editable flag
		serializer.writeAttribute(ATTR_ROOT_CONTEXT, manifest.getRootContextManifest().getId());
		serializer.writeAttribute(ATTR_EDITABLE, manifest.isEditable());

		// Now write all context manifests
//		writeContextManifest(manifest.getRootContextManifest());
		for(ContextManifest contextManifest : manifest.getContextManifests()) {
			writeContextManifest(contextManifest);
		}

		endManifest(TAG_CORPUS);
	}

	public void writeContextManifest(ContextManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_CONTEXT);

		ContextManifest template = (ContextManifest) manifest.getTemplate();

		MarkableLayerManifest primaryLayer = manifest.getPrimaryLayerManifest();
		MarkableLayerManifest baseLayer = manifest.getBaseLayerManifest();

		if(template!=null) {
			primaryLayer = diff(primaryLayer, template.getPrimaryLayerManifest());
			baseLayer = diff(baseLayer, template.getBaseLayerManifest());
		}

		// ATTRIBUTES

		// Write default stuff
		writeIdentityAttributes(manifest);

		// Write independent flag
		if (manifest.isIndependentContext() && template!=null
				&& !template.isIndependentContext()) {
			serializer.writeAttribute(ATTR_INDEPENDENT, true);
		}

		// ELEMENTS

		writeModifiableManifest(manifest);

		// Write location
		LocationManifest locationManifest = manifest.getLocationManifest();
		if(locationManifest!=null) {
			writeLocationElement(locationManifest);
		}

		// Write prerequisites
		if(!manifest.getPrerequisites().isEmpty()) {
			serializer.startElement(TAG_PREREQUISITES);

			for(PrerequisiteManifest prerequisiteManifest : manifest.getPrerequisites()) {
				if(prerequisiteManifest.getContextManifest()==manifest) {
					writePrerequisiteElement(prerequisiteManifest);
				}
			}

			serializer.endElement(TAG_PREREQUISITES);
		}

		// Write groups
		List<LayerGroupManifest> groupManifests = manifest.getGroupManifests();

		if(template!=null) {
			groupManifests = new ArrayList<>(groupManifests);

			groupManifests.removeAll(template.getGroupManifests());
		}

		for(LayerGroupManifest groupManifest : groupManifests) {
			writeLayerGroupElement(groupManifest);
		}

		// Write driver manifest
		DriverManifest driverManifest = manifest.getDriverManifest();
		if(template!=null) {
			driverManifest = diff(driverManifest, template.getDriverManifest());
		}

		if(driverManifest!=null) {
			writeDriverManifest(driverManifest);
		}

		endManifest(TAG_CONTEXT);
	}

	public void writeOptionsManifest(OptionsManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_OPTIONS);

		// ATTRIBUTES
		serializer.writeAttribute(ATTR_ID, manifest.getId());

		OptionsManifest template = (OptionsManifest) manifest.getTemplate();

		Set<String> options = manifest.getOptionNames();
		Set<Identity> groups = manifest.getGroupIdentifiers();

		// Make diff between current manifest and template
		if(template!=null) {
			serializer.writeAttribute(ATTR_TEMPLATE_ID, template.getId());

			options = new HashSet<>(options);
			groups = new HashSet<>(groups);

			for(String option : template.getOptionNames()) {
				if(options.contains(option)
						&& ClassUtils.equals(manifest.getName(option), template.getName(option))
						&& ClassUtils.equals(manifest.getDescription(option), template.getDescription(option))
						&& ClassUtils.equals(manifest.getDefaultValue(option), template.getDefaultValue(option))
						&& ClassUtils.equals(manifest.getValueType(option), template.getValueType(option))
						&& ClassUtils.equals(manifest.getSupportedValues(option), template.getSupportedValues(option))
						&& ClassUtils.equals(manifest.getSupportedRange(option), template.getSupportedRange(option))
						&& ClassUtils.equals(manifest.getOptionGroup(option), template.getOptionGroup(option))
						&& manifest.isPublished(option)==template.isPublished(option)
						&& manifest.isMultiValue(option)==template.isMultiValue(option)) {

					options.remove(option);
				}
			}

			groups.removeAll(template.getGroupIdentifiers());
		}

		// Write options in alphabetic order
		if(!options.isEmpty()) {
			List<String> names = CollectionUtils.asSortedList(options);

			for(String option : names) {
				writeOptionElement(manifest.getOption(option));
			}
		}

		// Write groups in alphabetic order
		if(!groups.isEmpty()) {
			List<Identity> idents = CollectionUtils.asSortedList(groups, Identity.COMPARATOR);

			for(Identity group : idents) {
				//FIXME empty element
				serializer.startElement(TAG_GROUP);

				// ATTRIBUTES
				writeIdentityAttributes(group);
				serializer.endElement(TAG_GROUP);
			}
		}

		endManifest(TAG_OPTIONS);
	}

	public void writeMarkableLayerManifest(MarkableLayerManifest manifest) throws Exception {
		writeMarkableLayerManifest0(TAG_MARKABLE_LAYER, manifest);
	}

	public void writeStructureLayerManifest(StructureLayerManifest manifest) throws Exception {
		writeMarkableLayerManifest0(TAG_STRUCTURE_LAYER, manifest);
	}

	private void writeAliasElement(String alias) throws Exception {
		//FIXME empty element
		serializer.startElement(TAG_ALIAS);
		serializer.writeAttribute(ATTR_NAME, alias);
		serializer.endElement(TAG_ALIAS);
	}

	public void writeAnnotationManifestElement(AnnotationManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_ANNOTATION);

		// ATTRIBUTES
		writeIdentityAttributes(manifest);
		serializer.writeAttribute(ATTR_KEY, manifest.getKey());

		// ELEMENTS
		writeModifiableManifest(manifest);

		for(String alias : manifest.getAliases()) {
			writeAliasElement(alias);
		}

		writeValueSetElement(manifest.getSupportedValues(), manifest.getValueType());
		writeValueRangeElement(manifest.getSupportedRange(), manifest.getValueType());

		serializer.endElement(TAG_ANNOTATION);
	}

	public void writeAnnotationLayerManifest(AnnotationLayerManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_ANNOTATION_LAYER);

		AnnotationLayerManifest template = (AnnotationLayerManifest) manifest.getTemplate();

		String defaultKey = manifest.getDefaultKey();
		if(template!=null) {
			defaultKey = diff(defaultKey, template.getDefaultKey());
		}

		// ATTRIBUTES

		// Write default stuff
		writeIdentityAttributes(manifest);
		writeLayerTypeAttribute(manifest);

		// Deep annotation
		if(manifest.isDeepAnnotation() && template!=null
				&& !template.isDeepAnnotation()) {
			serializer.writeAttribute(ATTR_DEEP_ANNOTATION, true);
		}

		// Unknown keys
		if(manifest.allowUnknownKeys() && template!=null
				&& !template.allowUnknownKeys()) {
			serializer.writeAttribute(ATTR_UNKNOWN_KEYS, true);
		}

		// Searchable
		if(!manifest.isSearchable() && template!=null
				&& template.isSearchable()) {
			serializer.writeAttribute(ATTR_SERCH, false);
		}

		// Indexable
		if(!manifest.isIndexable() && template!=null
				&& template.isIndexable()) {
			serializer.writeAttribute(ATTR_INDEX, false);
		}

		// Default key
		if(defaultKey!=null) {
			serializer.writeAttribute(ATTR_DEFAULT_KEY, defaultKey);
		}

		// ELEMENTS

		// Options and properties
		writeModifiableManifest(manifest);

		// Write base layers
		writeBaseLayerElements(manifest);

		// Write annotations
		Set<String> keys = manifest.getAvailableKeys();
		if(template!=null && !template.getAvailableKeys().isEmpty()) {
			keys = new HashSet<>(keys);

			keys.removeAll(template.getAvailableKeys());
		}

		if(!keys.isEmpty()) {

			List<String> names = CollectionUtils.asSortedList(keys);

			for(String name : names) {
				writeAnnotationManifestElement(manifest.getAnnotationManifest(name));
			}
		}

		endManifest(TAG_ANNOTATION_LAYER);
	}

	public void writeFragmentLayerManifest(FragmentLayerManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_FRAGMENT_LAYER);

		FragmentLayerManifest template = (FragmentLayerManifest) manifest.getTemplate();

		// ATTRIBUTES

		// Write default stuff
		writeIdentityAttributes(manifest);
		writeLayerTypeAttribute(manifest);

		// Write annotation key
		writeAnnotationKeyAttribute(manifest);

		// ELEMENTS

		writeModifiableManifest(manifest);

		// Write base layers
		writeBaseLayerElements(manifest);

		// Write boundary layer
		writeBoundaryLayerElement(manifest);

		// Write value layer
		writeValueLayerElement(manifest);

		// Write containers
		writeNestedContainerElements(manifest);

		// Write rasterizer
		RasterizerManifest rasterizerManifest = manifest.getRasterizerManifest();
		if(template!=null) {
			rasterizerManifest = diff(rasterizerManifest, template.getRasterizerManifest());
		}

		if(rasterizerManifest!=null) {
			writeRasterizerManifest(rasterizerManifest);
		}

		endManifest(TAG_FRAGMENT_LAYER);
	}

	public void writeHighlightLayerManifest(HighlightLayerManifest manifest) throws Exception {
		//TODO
	}

	public void writeImplementationManifest(ImplementationManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_IMPLEMENTATION);

		ImplementationManifest template = (ImplementationManifest) manifest.getTemplate();

		String source = manifest.getSource();
		String classname = manifest.getClassname();
		SourceType sourceType = manifest.getSourceType();

		if(template!=null) {
			source = diff(source, template.getSource());
			classname = diff(classname, template.getClassname());
			sourceType = diff(sourceType, template.getSourceType());
		}

		serializer.writeAttribute(ATTR_SOURCE, source);
		serializer.writeAttribute(ATTR_CLASSNAME, classname);
		if(sourceType!=null && sourceType!=SourceType.DEFAULT) {
			serializer.writeAttribute(ATTR_SOURCE_TYPE, sourceType.getXmlValue());
		}

		endManifest(TAG_IMPLEMENTATION);
	}

	public void writeRasterizerManifest(RasterizerManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_RASTERIZER);

		RasterizerManifest template = (RasterizerManifest) manifest.getTemplate();

		// ATTRIBUTES

		// Write default stuff
		writeIdentityAttributes(manifest);

		// ELEMENTS

		writeModifiableManifest(manifest);

		// Write implementation manifest
		ImplementationManifest implementationManifest = manifest.getImplementationManifest();
		if(template!=null) {
			implementationManifest = diff(implementationManifest, template.getImplementationManifest());
		}

		if(implementationManifest!=null) {
			writeImplementationManifest(implementationManifest);
		}

		endManifest(TAG_RASTERIZER);
	}

	//TODO write connector?
	public void writeDriverManifest(DriverManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_DRIVER);

		DriverManifest template = (DriverManifest) manifest.getTemplate();

		// ATTRIBUTES

		// Write default stuff
		writeIdentityAttributes(manifest);
		writeLocationAttribute(manifest);

		// ELEMENTS

		writeModifiableManifest(manifest);

		// Write implementation manifest
		ImplementationManifest implementationManifest = manifest.getImplementationManifest();
		if(template!=null) {
			implementationManifest = diff(implementationManifest, template.getImplementationManifest());
		}

		if(implementationManifest!=null) {
			writeImplementationManifest(implementationManifest);
		}

		// Write index manifests
		List<IndexManifest> indices = manifest.getIndexManifests();
		if(template!=null && !template.getIndexManifests().isEmpty()) {
			indices = new ArrayList<>(indices);

			indices.removeAll(template.getIndexManifests());
		}

		for(IndexManifest indexManifest : indices) {
			writeIndexElement(indexManifest);
		}

		endManifest(TAG_DRIVER);
	}

	public void writePathResolverManifest(PathResolverManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_PATH_RESOLVER);

		PathResolverManifest template = (PathResolverManifest) manifest.getTemplate();

		// ATTRIBUTES

		// Write default stuff
		writeIdentityAttributes(manifest);

		// ELEMENTS

		writeModifiableManifest(manifest);

		// Write implementation manifest
		ImplementationManifest implementationManifest = manifest.getImplementationManifest();
		if(template!=null) {
			implementationManifest = diff(implementationManifest, template.getImplementationManifest());
		}

		if(implementationManifest!=null) {
			writeImplementationManifest(implementationManifest);
		}

		endManifest(TAG_PATH_RESOLVER);
	}
}
