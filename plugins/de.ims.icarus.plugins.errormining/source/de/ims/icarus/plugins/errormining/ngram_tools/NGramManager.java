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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;

import org.java.plugin.registry.Extension;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramManager {
	
	private static NGramManager instance;
	
	private Map<Extension, NGramFactory> factoryInstances;


	public static NGramManager getInstance() {
		if(instance==null) {
			synchronized (NGramManager.class) {
				if(instance==null) {
					instance = new NGramManager();
				}
			}
		}
		
		return instance;
	}


	/**
	 * @param factoryExtension
	 * @return
	 */
	public NGramFactory getFactory(Extension extension) {
		if(extension==null)
			throw new IllegalArgumentException("Invalid extension"); //$NON-NLS-1$
		
		if(factoryInstances==null) {
			factoryInstances = new HashMap<>();
		}
		
		NGramFactory factory = factoryInstances.get(extension);
		if(factory==null) {
			try {
				factory = (NGramFactory) PluginUtil.instantiate(extension);
			} catch (Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to instantiate ngram factory: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}
		
		return factory;
	}

}
