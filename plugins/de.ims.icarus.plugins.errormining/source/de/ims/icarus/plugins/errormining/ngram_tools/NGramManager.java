/* 
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
