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
package de.ims.icarus.plugins.errormining.ngram_tools;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public interface NGramParameters {

	public static final String USE_NUMBER_WILDCARD = "numberWildcard"; //$NON-NLS-1$
	
	public static final String NGRAM_RESULT_LIMIT = "resultLimit"; //$NON-NLS-1$	
	
	public static final String USE_FRINGE_HEURISTIC = "fringe"; //$NON-NLS-1$
	
	public static final String FRINGE_SIZE = "fringeSize";  //$NON-NLS-1$	
	
	public static final String GRAMS_GREATERX = "gramsGreaterX"; //$NON-NLS-1$
	
	public static final String SENTENCE_LIMIT = "sentenceLimit"; //$NON-NLS-1$
	
	public static final String CREATE_XML_OUTPUT = "createxmlOutput"; //$NON-NLS-1$
	

	//default values
	public static final boolean DEFAULT_USE_NUMBER_WILDCARD = true;
	public static final int DEFAULT_NGRAM_RESULT_LIMIT = 0;
	public static final boolean DEFAULT_USE_FRINGE_HEURISTIC = true;
	public static final int DEFAULT_FRINGE_SIZE = 1;
	public static final int DEFAULT_GRAMS_GREATERX = 0;
	public static final int DEFAULT_SENTENCE_LIMIT = 0;
	public static final boolean DEFAULT_CREATE_XML_OUTPUT = false; 
}
