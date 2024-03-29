/* 
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus G�rtner and Gregor Thiele
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

import java.util.ArrayList;
import java.util.Map;

import de.ims.icarus.util.UnsupportedFormatException;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramsExecution {
	
	private static NGramsExecution instance;
	
	public static NGramsExecution getInstance() {
		if (instance == null) {
			synchronized (NGramsExecution.class) {
				if (instance == null) {
					instance = new NGramsExecution();
				}
			}
		}
		return instance;
	}
	
	public Map<String,ArrayList<ItemInNuclei>> runNGrams() throws UnsupportedFormatException{
		//return NGrams.getInstance().main();
		
		return null;
	}

}
