/* 
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

import javax.swing.SwingWorker;

import de.ims.icarus.plugins.weblicht.webservice.WebExecutionService;
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
