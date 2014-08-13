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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ModelXmlTags {

	public static final String TAG_TEMPLATES = "templates"; //$NON-NLS-1$
	public static final String TAG_CORPORA = "corpora"; //$NON-NLS-1$

	public static final String TAG_CORPUS = "corpus"; //$NON-NLS-1$
	public static final String TAG_CONTEXT = "context"; //$NON-NLS-1$
	public static final String TAG_LAYER_GROUP = "layer-group"; //$NON-NLS-1$
	public static final String TAG_MARKABLE_LAYER = "markable-layer"; //$NON-NLS-1$
	public static final String TAG_STRUCTURE_LAYER = "structure-layer"; //$NON-NLS-1$
	public static final String TAG_FRAGMENT_LAYER = "fragment-layer"; //$NON-NLS-1$
	public static final String TAG_ANNOTATION_LAYER = "annotation-layer"; //$NON-NLS-1$
	public static final String TAG_HIGHLIGHT_LAYER = "highlight-layer"; //$NON-NLS-1$
	public static final String TAG_PREREQUISITES = "prerequisites"; //$NON-NLS-1$
	public static final String TAG_PREREQUISITE = "prerequisite"; //$NON-NLS-1$
	public static final String TAG_RASTERIZER = "rasterizer"; //$NON-NLS-1$

	public static final String TAG_BASE_LAYER = "base-layer"; //$NON-NLS-1$
	public static final String TAG_BOUNDARY_LAYER = "boundary-layer"; //$NON-NLS-1$
	public static final String TAG_VALUE_LAYER = "value-layer"; //$NON-NLS-1$

	public static final String TAG_CONTAINER = "container"; //$NON-NLS-1$
	public static final String TAG_STRUCTURE = "structure"; //$NON-NLS-1$
	public static final String TAG_ANNOTATION = "annotation"; //$NON-NLS-1$
	public static final String TAG_ALIAS = "alias"; //$NON-NLS-1$

	public static final String TAG_VALUES = "values"; //$NON-NLS-1$
	public static final String TAG_VALUE = "value"; //$NON-NLS-1$
	public static final String TAG_OPTIONS = "options"; //$NON-NLS-1$
	public static final String TAG_OPTION = "option"; //$NON-NLS-1$
	public static final String TAG_EXTENSION_POINT = "extension-point"; //$NON-NLS-1$
	public static final String TAG_DEFAULT_VALUE = "default-value"; //$NON-NLS-1$
	public static final String TAG_IMPLEMENTATION = "implementation"; //$NON-NLS-1$
	public static final String TAG_DOCUMENTATION = "documentation"; //$NON-NLS-1$
	public static final String TAG_CONTENT = "content"; //$NON-NLS-1$
	public static final String TAG_RESOURCE = "resource"; //$NON-NLS-1$

	public static final String TAG_DRIVER = "driver"; //$NON-NLS-1$
	public static final String TAG_INDEX = "index"; //$NON-NLS-1$
	public static final String TAG_MODULE = "module"; //$NON-NLS-1$
	public static final String TAG_MODULE_SPEC = "module-spec"; //$NON-NLS-1$

	public static final String TAG_LOCATION = "location"; //$NON-NLS-1$
	public static final String TAG_PATH = "path"; //$NON-NLS-1$
	public static final String TAG_PATH_ENTRY = "path-entry"; //$NON-NLS-1$
	public static final String TAG_PATH_RESOLVER = "path-resolver"; //$NON-NLS-1$

	public static final String TAG_RANGE = "range"; //$NON-NLS-1$
	public static final String TAG_MIN = "min"; //$NON-NLS-1$
	public static final String TAG_MAX = "max"; //$NON-NLS-1$
	public static final String TAG_STEP_SIZE = "step-size"; //$NON-NLS-1$

	public static final String TAG_EVAL = "eval"; //$NON-NLS-1$
	public static final String TAG_CODE = "code"; //$NON-NLS-1$
	public static final String TAG_VARIABLE = "variable"; //$NON-NLS-1$

	public static final String TAG_PROPERTIES = "properties"; //$NON-NLS-1$
	public static final String TAG_PROPERTY = "property"; //$NON-NLS-1$
	public static final String TAG_GROUP = "group"; //$NON-NLS-1$

	public static final String TAG_NOTE = "note"; //$NON-NLS-1$
}
