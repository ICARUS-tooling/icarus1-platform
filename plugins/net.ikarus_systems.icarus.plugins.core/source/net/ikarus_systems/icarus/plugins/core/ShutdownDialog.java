/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core;

import javax.swing.JProgressBar;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ShutdownDialog {
	
	private JProgressBar progressBar;
	
	private static ShutdownDialog instance;
	
	public static ShutdownDialog getDialog() {
		if(instance==null) {
			synchronized (ShutdownDialog.class) {
				if(instance==null) {
					instance = new ShutdownDialog();
				}
			}
		}
		
		return instance;
	}

	private ShutdownDialog() {
		// no-op
	}

	public synchronized void shutdown() {
		// TODO create dialog and invoke shutdown calls on various frameworks
	}
}
