/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.matetools;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.logging.Level;


import org.java.plugin.Plugin;

import de.ims.icarus.logging.LoggerFactory;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MatetoolsPlugin extends Plugin {
	
	public static final String PLUGIN_ID = MatetoolsConstants.MATETOOLS_PLUGIN_ID;

	public MatetoolsPlugin() {
		// no-op
	}
	
	/**
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		checkHeap();
	}
	
	private void checkHeap() {
		// Check for heap space settings
		long mb = 1024*1024;
		long maxMemory = Runtime.getRuntime().maxMemory()/mb;
		long requiredMemory = 2000;
		
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		for(String argument : bean.getInputArguments()) {
			if(argument.startsWith("-Xmx")) { //$NON-NLS-1$
				maxMemory = Long.parseLong(argument.replaceAll("\\D+", "")); //$NON-NLS-1$ //$NON-NLS-2$
				if(argument.endsWith("g")) //$NON-NLS-1$
					maxMemory *= 1024;
			}
		}

		// Just notify the user of the potential problem
		if(maxMemory<requiredMemory) {
			String msg = String.format( 
					"Insufficient heap-space for MateTools-Adapter\n" + //$NON-NLS-1$
					"The maximum amount of heap space for this JVM is set to ~ %d MB\n" + //$NON-NLS-1$
					"To operate properly the dependency parser requires at least %d MB\n" + //$NON-NLS-1$
					"\n" + //$NON-NLS-1$
					"Increase the available heap space by using the -Xmx command line argument:\n" + //$NON-NLS-1$
					"        java -Xmx2g -jar icarus.jar\n" + //$NON-NLS-1$
					"will make a maximum of 2 GB heap space avaialble to the JVM.\n" + //$NON-NLS-1$
					"\n" + //$NON-NLS-1$
					"If you do not intent to use the parser adapter at all you might just " + //$NON-NLS-1$
					"leave the heap space settings as they are, since the core implementation " + //$NON-NLS-1$
					"does not require a lot of memory.",  //$NON-NLS-1$
					maxMemory, requiredMemory);
			
			// TODO keep jar name in outline above consistent with our packaged jar!!!
			
			LoggerFactory.log(this, Level.WARNING, msg);
		}
	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub

	}
}
