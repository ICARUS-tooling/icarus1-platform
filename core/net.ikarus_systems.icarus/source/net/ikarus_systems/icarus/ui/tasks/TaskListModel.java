/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.tasks;


import javax.swing.AbstractListModel;

import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.ui.tasks.TaskManager.TaskQueue;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TaskListModel extends AbstractListModel<Object> implements EventListener {

	private static final long serialVersionUID = -7537887751568303846L;
	
	/**
	 * Caches tasks at their respective index to save expensive
	 * calls to the {@link TaskQueue#taskAt(int)} method. This cache
	 * will only grow in size when required, but never shrink. 
	 */
	private Object[] cache;
	
	private final Object cacheLock = new Object();
	
	// Stores the largest index ever encountered
	private int cacheSize = 0;
	
	private final TaskManager manager;
	
	public TaskListModel(TaskManager manager) {
		this.manager = manager;
		
		manager.addListener(Events.CHANGED, this);
		manager.addListener(Events.REMOVED, this);
		manager.addListener(Events.ADDED, this);
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return manager.getQueue().size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Object getElementAt(int index) {
		int size = getSize();
		if(index>=size) {
			return null;
		}
		
		Object task = null;
		
		synchronized (cacheLock) {
			// Create cache if necessary
			if(cache==null) {
				cache = new Object[10];
			}
			// Expand cache if necessary
			if(cache.length<index) {
				Object[] newCache = new Object[index*2];
				System.arraycopy(cache, 0, newCache, 0, cache.length);
				cache = newCache;
			}
			
			task = cache[index];
			if(task==null) {
				task = manager.getQueue().taskAt(index);
				cache[index] = task;
			}
			
			cacheSize = Math.max(index, cacheSize);
		}
		
		return task;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
	 */
	@Override
	public void invoke(Object sender, EventObject event) {
		// If no cache present yet do not bother with handling events
		if(cache==null) {
			return;
		}
		
		Object task = event.getProperty("task"); //$NON-NLS-1$
		
		// We are not interested in changes to the active task
		if(manager.isActiveTask(task)) {
			return;
		}
		
		synchronized (cacheLock) {	
			int size = getSize();
			if(Events.CHANGED.equals(event.getName())) {
				// Just notify listeners about the one index
				// that change occurred at
				for(int i=0; i<size; i++) {
					if(task==cache[i]) {
						fireContentsChanged(this, i, i);
						break;
					}
				}
			} else {
				// Clear cache starting from the last entry until
				// the task provided via event is found
				for(int i=cacheSize; i>-1; i--) {
					Object cachedTask = cache[i];
					cache[i] = null;
					
					if(task==cachedTask) {
						// Report a change to the cleared region
						fireContentsChanged(this, i, size);
						return;
					}
				}
				
				// In case we traversed the entire cache we have
				// to 'invalidate' the entire list
				fireContentsChanged(this, 0, size);
			}
		}
	}

}
