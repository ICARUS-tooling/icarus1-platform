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
package de.ims.icarus.ui.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import de.ims.icarus.Core;
import de.ims.icarus.Core.NamedRunnable;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.EventSource;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.events.WeakEventSource;
import de.ims.icarus.util.BiDiMap;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class TaskManager {

	private static ExecutorService executorService;
	
	private static RejectedTaskExecutionHandler rejectedTaskExecutionHandler;
	
	private BiDiMap<Object, SwingWorker<?, ?>> wrapperMap = new BiDiMap<>();
	
	private Map<SwingWorker<?, ?>, TaskState> states = Collections.synchronizedMap(
			new IdentityHashMap<SwingWorker<?,?>, TaskState>());
	
	private TaskQueue taskQueue = new TaskQueue();
	
	private EventSource eventSource = new WeakEventSource(this);
	
	private volatile SwingWorker<?, ?> currentWorker;
	
	private TaskObserver taskObserver = new TaskObserver();
	
	private static TaskManager instance;
	
	public static TaskManager getInstance() {
		if(instance==null) {
			synchronized (TaskManager.class) {
				if(instance==null) {
					instance = new TaskManager();
				}
			}
		}
		
		return instance;
	}

	public TaskManager() {
		// no-op
	}
	
	/**
	 * @see de.ims.icarus.ui.events.EventSource#addListener(java.lang.String, de.ims.icarus.ui.events.EventListener)
	 */
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#removeListener(de.ims.icarus.ui.events.EventListener)
	 */
	public void removeListener(EventListener listener) {
		eventSource.removeListener(listener);
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#removeListener(de.ims.icarus.ui.events.EventListener, java.lang.String)
	 */
	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}

	private TaskState getTaskState(Object task) {
		task = getWorker(task);
		
		if(task==null)
			throw new NullPointerException("Invalid task"); //$NON-NLS-1$
		
		return states.get(task);
	}
	
	SwingWorker<?, ?> getWorker(Object task) {
		if(task instanceof SwingWorker) {
			return (SwingWorker<?, ?>)task;
		}
		
		return wrapperMap.get(task);
	}
	
	private Object getTask(SwingWorker<?, ?> worker) {
		Object task = wrapperMap.getKey(worker);
		return task==null ? worker : task;
	}
	
	public Icon getIcon(Object task) {
		TaskState state = getTaskState(task);
		
		if(state==null) {
			return null;
		}
		
		Identity identity = state.getIdentity();
		if(identity==null && task instanceof Identity) {
			identity = (Identity) task;
		}
		return identity==null ? null : identity.getIcon();
	}
	
	public String getTitle(Object task) {
		TaskState state = getTaskState(task);
		
		if(state==null) {
			return null;
		}
		
		Identity identity = state.getIdentity();
		if(identity==null && task instanceof Identity) {
			identity = (Identity) task;
		}
		return identity==null ? null : identity.getName();
	}
	
	public String getInfo(Object task) {
		TaskState state = getTaskState(task);
		
		if(state==null) {
			return null;
		}
		
		Identity identity = state.getIdentity();
		if(identity==null && task instanceof Identity) {
			identity = (Identity) task;
		}
		return identity==null ? null : identity.getDescription();
	}
	
	public void setTitle(Object task, String title) {
		TaskState state = getTaskState(task);
		if(state!=null) {
			state.setTitle(title);
			eventSource.fireEvent(new EventObject(
					Events.CHANGED, "task", task, "property", "title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	public void setInfo(Object task, String info) {
		TaskState state = getTaskState(task);
		if(state!=null) {
			state.setInfo(info);
			eventSource.fireEvent(new EventObject(
					Events.CHANGED, "task", task, "property", "info")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	public void setIcon(Object task, Icon icon) {
		TaskState state = getTaskState(task);
		if(state!=null) {
			state.setIcon(icon);
			eventSource.fireEvent(new EventObject(
					Events.CHANGED, "task", task, "property", "icon")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	public boolean isIndeterminate(Object task) {
		TaskState state = getTaskState(task);
		
		return state==null ? false : state.isIndeterminate();
	}
	
	public void setIndeterminate(Object task, boolean indeterminate) {
		TaskState state = getTaskState(task);
		if(state!=null) {
			state.setIndeterminate(indeterminate);
			eventSource.fireEvent(new EventObject(
					Events.CHANGED, "task", task, "property", "indeterminate")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	public SwingWorker.StateValue getState(Object task) {
		if(!(task instanceof SwingWorker)) {
			task = wrapperMap.get(task);
		}
		
		if(task==null)
			throw new NullPointerException("Invalid task"); //$NON-NLS-1$
		
		return ((SwingWorker<?, ?>)task).getState();
	}
	
	public int getProgress(Object task) {
		if(!(task instanceof SwingWorker)) {
			task = wrapperMap.get(task);
		}
		
		if(task==null)
			throw new NullPointerException("Invalid task"); //$NON-NLS-1$
		
		return ((SwingWorker<?, ?>)task).getProgress();
	}
	
	public synchronized void cancelTask(Object task) {
		if(task==null)
			throw new NullPointerException("Invalid task"); //$NON-NLS-1$
		// TODO
		SwingWorker<?, ?> worker = getWorker(task);
		
		// Already executed and removed 
		if(worker==null) {
			return;
		}
		
		wrapperMap.removeValue(worker);
		states.remove(worker);
		
		if(currentWorker==worker) {
			worker.cancel(true);
		} else {
			taskQueue.remove(task);
		}
	}
	
	TaskQueue getQueue() {
		return taskQueue;
	}
	
	public synchronized boolean schedule(Object task, TaskPriority priority,
			boolean unique) {
		return schedule(task, null, priority, unique);
	}
	
	public synchronized boolean schedule(Object task, String title, 
			String info, Icon icon, TaskPriority priority, boolean unique) {
		TaskIdentity identity = new TaskIdentity(title, info, icon);
		
		return schedule(task, identity, priority, unique);
	}

	public synchronized boolean schedule(Object task, Identity identity, 
			TaskPriority priority, boolean unique) {
		
		SwingWorker<?, ?> worker = null;
		
		if(task instanceof SwingWorker) {
			worker = (SwingWorker<?, ?>) task;
		} else if(task instanceof Runnable) {
			worker = Tasks.createWorker((Runnable)task);
			wrapperMap.put(task, worker);
		} else if(task instanceof Callable) {
			worker = Tasks.createWorker((Callable<?>)task);
			wrapperMap.put(task, worker);
		}
		
		if(!(worker instanceof SwingWorker))
			throw new NullPointerException("Invalid task: "+task); //$NON-NLS-1$
				
		// Save task identity and priority in state object
		TaskState state = new TaskState(priority, identity);		
		states.put(worker, state);
		
		boolean result = true;
		
		// Try to add task to queue
		if(unique) {
			result = taskQueue.insertIfAbsent(task, priority);
		} else {
			taskQueue.insert(task, priority);
		}
		
		// Clear lookup tables if adding failed
		if(!result) {
			states.remove(worker);
			wrapperMap.removeValue(worker);
		}
		
		if(result && currentWorker==null) {
			taskObserver.dispatch();
		}
		
		return result;
	}
	
	public synchronized boolean isActiveTask(Object task) {
		if(!(task instanceof SwingWorker)) {
			task = wrapperMap.get(task);
		}
		
		if(task==null)
			throw new NullPointerException("Invalid task"); //$NON-NLS-1$
		
		return task==currentWorker;
	}
	
	public synchronized Object getActiveTask() {
		SwingWorker<?, ?> worker = currentWorker;
		return worker==null ? null : getTask(worker);
	}
	
	public synchronized boolean isEmpty() {
		return currentWorker==null && taskQueue.isEmpty();
	}
	
	public void close() {
		if(executorService!=null) {
			// TODO add handling to await termination and notify user
			executorService.shutdown();
		}
	}
	
	public void execute(Runnable r) {
		getExecutorService().execute(r);
	}
	
	public Future<?> submit(Runnable task) {
		return getExecutorService().submit(task);
	}
	
	public <T extends Object> Future<T> submit(Callable<T> task) {
		return getExecutorService().submit(task);
	}
	
	private static synchronized ExecutorService getExecutorService() {

		if (executorService == null) {
			// this creates daemon threads.
			ThreadFactory threadFactory = new ThreadFactory() {
				final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

				@Override
				public Thread newThread(final Runnable r) {
					Thread thread = defaultFactory.newThread(r);
					thread.setName("TaskManager-" + thread.getName()); //$NON-NLS-1$
					thread.setDaemon(true);
					return thread;
				}
			};
			
			if(rejectedTaskExecutionHandler==null) {
				rejectedTaskExecutionHandler = new RejectedTaskExecutionHandler();
			}
			
			int maxThreadCount = Math.max(1, Runtime.getRuntime().availableProcessors()-1); 

			executorService = new ThreadPoolExecutor(1,
					maxThreadCount, 10L, TimeUnit.MINUTES,
					new LinkedBlockingQueue<Runnable>(), threadFactory,
					rejectedTaskExecutionHandler);
			
			Core.getCore().addShutdownHook(new ShutdownHook());
		}
		return executorService;
	}
	
	private static class RejectedTaskExecutionHandler implements RejectedExecutionHandler {

		/**
		 * @see java.util.concurrent.RejectedExecutionHandler#rejectedExecution(java.lang.Runnable, java.util.concurrent.ThreadPoolExecutor)
		 */
		@Override
		public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
			if(!SwingUtilities.isEventDispatchThread()) {
				UIUtil.invokeLater(new Runnable() {					
					@Override
					public void run() {
						rejectedExecution(r, executor);
					}
				});
				
				return;
			}
			
			String id = null;

			if(r instanceof Identity) {
				id = ((Identity)r).getId();
			}
			if(id==null) {
				id=r.toString();
			}
			
			DialogFactory.getGlobalFactory().showWarning(null, 
					"taskManager.dialogs.title",  //$NON-NLS-1$
					"taskManager.dialogs.taskRejected",  //$NON-NLS-1$
					id);
		}
		
	}
	
	private class TaskIdentity implements Identity {
		private String title;
		private String info;
		private Icon icon;

		public TaskIdentity(String title, String info, Icon icon) {
			this.title = title;
			this.info = info;
			this.icon = icon;
		}
		
		/**
		 * @see de.ims.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return null;
		}
		
		/**
		 * @see de.ims.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return title;
		}
		
		/**
		 * @see de.ims.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return info;
		}
		
		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return icon;
		}
		
		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return null;
		}
	}
	
	private class TaskState {
		boolean indeterminate = false;
		Identity identity = null;
		final TaskPriority priority;
		
		TaskState(TaskPriority priority, Identity identity) {
			this.priority = priority;
			this.identity = identity;
		}
		
		public TaskPriority getPriority() {
			return priority;
		}

		public Identity getIdentity() {
			return identity;
		}

		public synchronized boolean isIndeterminate() {
			return indeterminate;
		}
		
		public synchronized void setInfo(String info) {
			if(identity!=null && identity instanceof TaskIdentity) {
				TaskIdentity ti = (TaskIdentity) identity;
				ti.info = info;
			}
		}
		
		public synchronized void setTitle(String title) {
			if(identity!=null && identity instanceof TaskIdentity) {
				TaskIdentity ti = (TaskIdentity) identity;
				ti.title = title;
			}
		}
		
		public synchronized void setIcon(Icon icon) {
			if(identity!=null && identity instanceof TaskIdentity) {
				TaskIdentity ti = (TaskIdentity) identity;
				ti.icon = icon;
			}
		}

		public synchronized void setIndeterminate(boolean indeterminate) {
			if(indeterminate!=this.indeterminate) {
				this.indeterminate = indeterminate;
			}
		}
	}
	
	private class TaskObserver implements PropertyChangeListener, Runnable {
		
		private boolean isScheduled = false;

		/**
		 * Schedules this observer's {@code #run()} method to be executed
		 * on the <i>Event Dispatch thread</i>.
		 */
		private synchronized void dispatch() {
			if(!isScheduled) {
				isScheduled = true;
				
				// TODO verify that there is no event race condition
				// when we immediately call our run() method
				if(SwingUtilities.isEventDispatchThread()) {
					run();
				} else {
					UIUtil.invokeLater(this);
				}
			}
		}
		
		/**
		 * Due to the nature of {@link SwingWorker}'s implementation
		 * this method will always be called on the <i>Event Dispatch Thread</i>.
		 * 
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public synchronized void propertyChange(PropertyChangeEvent evt) {
			//System.out.printf("name=%s value=%s\n", evt.getPropertyName(), evt.getNewValue());
			
			SwingWorker<?, ?> worker = (SwingWorker<?, ?>) evt.getSource();
			Object task = wrapperMap.getKey(worker);
			if(task==null) {
				task = worker;
			}
			
			switch (evt.getPropertyName()) {
			case TaskConstants.INFO_PROPERTY:
				setInfo(task, (String)evt.getNewValue());
				break;

			case TaskConstants.TITLE_PROPERTY:
				setTitle(task, (String)evt.getNewValue());
				break;

			case TaskConstants.ICON_PROPERTY:
				setIcon(task, (Icon)evt.getNewValue());
				break;

			case TaskConstants.INDETERMINATE_PROPERTY:
				setIndeterminate(task, (Boolean)evt.getNewValue());
				break;
				
			case TaskConstants.PROGRESS_PROPERTY:
				eventSource.fireEvent(new EventObject(
						Events.CHANGED, "task", task, "property", "progress")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;
				
			case TaskConstants.STATE_PROPERTY:
				if(worker.isDone()) {
					worker.removePropertyChangeListener(this);
					
					// Integrity check
					if(worker!=currentWorker)
						throw new IllegalStateException();

					wrapperMap.removeValue(worker);
					states.remove(worker);
					
					// Remove current worker
					currentWorker = null;
					eventSource.fireEvent(new EventObject(
							TaskConstants.ACTIVE_TASK_CHANGED));
					
					// Schedule to start next worker
					dispatch();
					break;
				}
				eventSource.fireEvent(new EventObject(
						Events.CHANGED, "task", task, "property", "state")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				break;

			default:
				eventSource.fireEvent(new EventObject(
						Events.CHANGED, "task", task)); //$NON-NLS-1$
				break;
			}
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public synchronized void run() {
			isScheduled = false;
			if(currentWorker!=null) {
				return;
			}
			
			Object task = taskQueue.getNext();
			if(task!=null) {
				
				SwingWorker<?, ?> worker = getWorker(task);
				
				currentWorker = worker;
				
				if(worker!=null) {
					worker.addPropertyChangeListener(this);
					
					worker.execute();
				}
				
				eventSource.fireEvent(new EventObject(
						TaskConstants.ACTIVE_TASK_CHANGED));
			}
		}
		
	}
	
	class TaskQueue {
		
		private Lock lock;
		
		private Map<TaskPriority, LinkedList<Object>> queue
			= new EnumMap<>(TaskPriority.class);
			
		private Map<Object, List<TaskPriority>> taskPriorities = new HashMap<>();
		
		private int size = 0;
		
		TaskQueue() {
			lock = new ReentrantLock();
		}
		
		int size() {
			return size;
		}
		
		boolean isEmpty() {
			return size==0;
		}
		
		Object taskAt(int index) {
			lock.lock();
			try {
				if(queue.size()==0) {
					return null;
				}
				
				for(TaskPriority priority : TaskPriority.values()) {
					List<Object> list = queue.get(priority);
					if(list==null || list.isEmpty()) {
						continue;
					}
					
					if(index>=list.size()) {
						index -= list.size();
					} else {
						return list.get(index);
					}
				}
			} finally {
				lock.unlock();
			}
			
			return null;
		}
		
		/**
		 * Inserts a task into the appropriate priority
		 * queue.
		 */
		void insert(Object task, TaskPriority priority) {
			lock.lock();
			try {
				// Append task to priority queue
				LinkedList<Object> list = queue.get(priority);
				if(list==null) {
					list = new LinkedList<>();
					queue.put(priority, list);
				}
				list.offer(task);
				
				// Add priority to task lookup list
				List<TaskPriority> priorities = taskPriorities.get(task);
				if(priorities==null) {
					priorities = new LinkedList<>();
					taskPriorities.put(task, priorities);
				}
				priorities.add(priority);
				
				// Increment total counter
				size++;
				
				eventSource.fireEvent(new EventObject(Events.ADDED, 
						"task", task, "priority", priority)); //$NON-NLS-1$ //$NON-NLS-2$
			} finally {
				lock.unlock();
			}
		}
		
		boolean insertIfAbsent(Object task, TaskPriority priority) {
			lock.lock();
			try {
				// TODO case diff: if there are only tasks with lower
				// priorities scheduled then the given task will be
				// queued as well.
				if(taskPriorities.containsKey(task)) {
					return false;
				}
				
				insert(task, priority);
			} finally {
				lock.unlock();
			}
			
			return true;
		}
		
		void remove(Object task) {
			lock.lock();
			try {
				if(size==0) {
					return;
				}
				
				for(TaskPriority priority : TaskPriority.values()) {
					Queue<Object> list = queue.get(priority);
					if(list==null || list.isEmpty()) {
						continue;
					}
						
					// Remove task and remove list if empty
					if(!list.remove(task)) {
						continue;
					}
					if(list.isEmpty()) {
						queue.remove(priority);
					}
					
					// Remove priority from task lookup list
					List<TaskPriority> priorities = taskPriorities.get(task);
					priorities.remove(priority);
					if(priorities.isEmpty()) {
						taskPriorities.remove(task);
					}
					
					// Decrement total counter
					size--;

					eventSource.fireEvent(new EventObject(Events.REMOVED, 
							"task", task, "priority", priority)); //$NON-NLS-1$ //$NON-NLS-2$
					
					break;
				}
			} finally {
				lock.unlock();
			}
		}
		
		/**
		 * Fetches the first task in any priority queue starting with
		 * the highest priority.
		 */
		Object getNext() {
			lock.lock();
			Object task = null;
			try {
				if(size==0) {
					return null;
				}
				
				for(TaskPriority priority : TaskPriority.values()) {
					Queue<Object> list = queue.get(priority);
					if(list==null || list.isEmpty()) {
						continue;
					}
						
					// Fetch task and remove list if empty
					task = list.poll();							
					if(list.isEmpty()) {
						queue.remove(priority);
					}
					
					// Remove priority from task lookup list
					List<TaskPriority> priorities = taskPriorities.get(task);
					priorities.remove(priority);
					if(priorities.isEmpty()) {
						taskPriorities.remove(task);
					}
					
					// Decrement total counter
					size--;

					eventSource.fireEvent(new EventObject(Events.REMOVED, 
							"task", task, "priority", priority)); //$NON-NLS-1$ //$NON-NLS-2$
					
					break;
				}
			} finally {
				lock.unlock();
			}
			
			// TODO notify listeners
			
			return task;
		}
	}
	
	private static class ShutdownHook implements NamedRunnable {

		/**
		 * @see de.ims.icarus.Core.NamedRunnable#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"taskManager.shutdownHook.title", "Closing task manager"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * @see de.ims.icarus.Core.NamedRunnable#run()
		 */
		@Override
		public void run() throws Exception {
			if(executorService==null) {
				return;
			}
			
			executorService.shutdownNow();
			
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		}
		
	}
}
