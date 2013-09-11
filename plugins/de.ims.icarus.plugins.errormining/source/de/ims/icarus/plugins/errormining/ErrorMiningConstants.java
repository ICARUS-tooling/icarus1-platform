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
package de.ims.icarus.plugins.errormining;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public interface ErrorMiningConstants {
	
	// Plugin ID
	public static final String ERRORMINING_PLUGIN_ID = 
			"de.ims.icarus.errormining"; //$NON-NLS-1$
		
	// Perspective IDs
	public static final String ERRORMINING_PERSPECTIVE_ID = 
			ERRORMINING_PLUGIN_ID+"@ErrorMiningPerspective"; //$NON-NLS-1$
	
	// Event constants
	public static final String ERRORMINING_RESULT_VIEW_CHANGED = 
			"errorMiningTools:explorerSelectionChanged"; //$NON-NLS-1$
	
	
	// View IDs
	public static final String NGRAM_RESULT_VIEW_ID = 
			ERRORMINING_PLUGIN_ID+"@NGramResultView"; //$NON-NLS-1$
	
	public static final String NGRAM_RESULT_SENTENCE_VIEW_ID = 
			ERRORMINING_PLUGIN_ID+"@NGramResultSentenceView"; //$NON-NLS-1$
	
	public static final String NGRAM_QUERY_VIEW_ID = 
			ERRORMINING_PLUGIN_ID+"@NGramQueryView"; //$NON-NLS-1$


}
