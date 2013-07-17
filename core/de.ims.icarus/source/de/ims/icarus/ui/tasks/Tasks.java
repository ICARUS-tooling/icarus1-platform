/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.tasks;

import java.util.concurrent.Callable;

import javax.swing.SwingWorker;

import de.ims.icarus.util.WrapperException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class Tasks {

	private Tasks() {
		// no-op
	}
	
	public static SwingWorker<?, ?> createWorker(Runnable runnable) {
		if(runnable==null)
			throw new IllegalArgumentException("Invalid runnable"); //$NON-NLS-1$
		
		return new RunnableWorker(runnable);
	}
	
	public static SwingWorker<?, ?> createWorker(Callable<?> callable) {
		if(callable==null)
			throw new IllegalArgumentException("Invalid callable"); //$NON-NLS-1$
		
		return new CallableWorker(callable);
	}
	
	static class RunnableWorker extends SwingWorker<Object, Object> {
		
		private final Runnable runnable;

		public RunnableWorker(Runnable runnable) {
			this.runnable = runnable;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Object doInBackground() throws Exception {
			
			try {
				runnable.run();
			} catch(WrapperException e) {
				throw e.getWrappedException();
			}
			
			return null;
		}		
	}
	
	static class CallableWorker extends SwingWorker<Object, Object> {
		
		private final Callable<?> callable;

		public CallableWorker(Callable<?> callable) {
			this.callable = callable;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Object doInBackground() throws Exception {
			
			return callable.call();
		}		
	}
}
