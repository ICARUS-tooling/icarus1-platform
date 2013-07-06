/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.dialog;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.ikarus_systems.icarus.ui.UIUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DialogDispatcher implements Runnable {
	
	private final Component parent;
	private final String title;
	private final String message;
	private final Object[] params;
	
	private final Throwable throwable;
	
	private int messageType = JOptionPane.PLAIN_MESSAGE;
	
	private boolean dispatched = false;

	public DialogDispatcher(Component parent, String title, String message, Object...params) {
		this(parent, title, message, null, params);
	}

	public DialogDispatcher(Component parent, String title, String message, Throwable throwable, Object...params) {
		this.parent = parent;
		this.title = title;
		this.message = message;
		this.throwable = throwable;
		this.params = params;
	}
	
	public void showAsError() {
		messageType = JOptionPane.ERROR_MESSAGE;
		dispatch();
	}
	
	public void showAsWarning() {
		messageType = JOptionPane.WARNING_MESSAGE;
		dispatch();
	}
	
	public void showAsInfo() {
		messageType = JOptionPane.INFORMATION_MESSAGE;
		dispatch();
	}
	
	public void showPlain() {
		dispatch();
	}
	
	private synchronized void dispatch() {
		if(dispatched)
			throw new IllegalStateException("Already dispatched"); //$NON-NLS-1$

		dispatched = true;
		
		if(SwingUtilities.isEventDispatchThread()) {
			run();
		} else {
			UIUtil.invokeLater(this);
		}
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		switch (messageType) {
		case JOptionPane.ERROR_MESSAGE:
			if(throwable!=null) {
				DialogFactory.getGlobalFactory().showDetailedError(parent, title, message, throwable, params);
			} else {
				DialogFactory.getGlobalFactory().showError(parent, title, message, params);
			}
			break;

		case JOptionPane.WARNING_MESSAGE:
			DialogFactory.getGlobalFactory().showWarning(parent, title, message, params);
			break;

		case JOptionPane.INFORMATION_MESSAGE:
			DialogFactory.getGlobalFactory().showInfo(parent, title, message, params);
			break;

		default:
			DialogFactory.getGlobalFactory().showPlain(parent, title, message, params);
			break;
		}
	}

}
