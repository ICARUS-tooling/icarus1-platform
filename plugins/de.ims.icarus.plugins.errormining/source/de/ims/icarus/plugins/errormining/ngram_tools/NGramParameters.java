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
	
	//TODO maybe with heuristics? 
	public static final String NGRAM_MODE = "ngramMode"; //$NON-NLS-1$ 

	public static final String NGRAM_RESULT_LIMIT = "ngramResultLimit"; //$NON-NLS-1$	
	
	public static final boolean USE_FRINGE_HEURISTIC = false;
	
	public static final String FRINGE_START = "fringeStart";  //$NON-NLS-1$
	
	public static final String FRINGE_END = "fringeEnd"; //$NON-NLS-1$

}
