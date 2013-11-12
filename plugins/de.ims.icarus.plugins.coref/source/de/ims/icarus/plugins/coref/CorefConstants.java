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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface CorefConstants {

	// Plugin ID
	public static final String COREFERENCE_PLUGIN_ID = 
			"de.ims.icarus.coref"; //$NON-NLS-1$
		
	// Perspective IDs
	public static final String COREFERENCE_PERSPECTIVE_ID = 
			COREFERENCE_PLUGIN_ID+"@CoreferencePerspective"; //$NON-NLS-1$
	
	// View IDs
	public static final String COREFERENCE_MANAGER_VIEW_ID = 
			COREFERENCE_PLUGIN_ID+"@CoreferenceManagerView"; //$NON-NLS-1$
	public static final String COREFERENCE_EXPLORER_VIEW_ID = 
			COREFERENCE_PLUGIN_ID+"@CoreferenceExplorerView"; //$NON-NLS-1$
	public static final String COREFERENCE_DOCUMENT_VIEW_ID = 
			COREFERENCE_PLUGIN_ID+"@CoreferenceDocumentView"; //$NON-NLS-1$
	public static final String ERROR_ANALYSIS_VIEW_ID = 
			COREFERENCE_PLUGIN_ID+"@ErrorAnalysisView"; //$NON-NLS-1$

	// Event constants
	public static final String DOCUMENT_EXPLORER_SELECTION_CHANGED = 
			"corefTools:explorerSelectionChanged"; //$NON-NLS-1$

}
