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

import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.EventSource;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.util.BiDiMap;
import net.ikarus_systems.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class TaskManager { 
	
	private static final int MAX_WORKER_THREADS = 10;

	private static ExecutorService executorService;
	
	private static RejectedTaskExecutionHandler rejectedTaskExecutionHandler;
	
	private BiDiMap<Object, SwingWorker<?, ?>> wrapperMap = new BiDiMap<>();
	
	private Map<SwingWorker<?, ?>, TaskState> states = Collections.synchronizedMap(
			new IdentityHashMap<SwingWorker<?,?>, TaskState>());
	
	private TaskQueue taskQueue = new TaskQueue();
	
	private EventSource eventSource = new EventSource(this);
	
	private volatile SwingWorker<?, ?> currentWorker;
	
	private TaskObserver taskObserver = new TaskObserver();
	
	private static TaskManager instance;
	
	private static final boolean DEFAULT_UNIQUE = true;
	
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
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#addListener(java.lang.String, net.ikarus_systems.icarus.ui.events.EventListener)
	 */
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#removeListener(net.ikarus_systems.icarus.ui.events.EventListener)
	 */
	public void removeListener(EventListener listener) {
		eventSource.removeListener(listener);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#removeListener(net.ikarus_systems.icarus.ui.events.EventListener, java.lang.String)
	 */
	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}

	private TaskState getTaskState(Object task) {
		task = getWorker(task);
		
		if(task==null)
			throw new IllegalArgumentException("Invalid task"); //$NON-NLS-1$
		
		return states.get(task);
	}
	
	private SwingWorker<?, ?> getWorker(Object task) {
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
		
		if(task==null) {
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
		
		if(task==null) {
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
		
		if(task==null) {
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
					Events.CHANGED, "task", task)); //$NON-NLS-1$
		}
	}
	
	public void setInfo(Object task, String info) {
		TaskState state = getTaskState(task);
		if(state!=null) {
			state.setInfo(info);
			eventSource.fireEvent(new EventObject(
					Events.CHANGED, "task", task)); //$NON-NLS-1$
		}
	}
	
	public void setIcon(Object task, Icon icon) {
		TaskState state = getTaskState(task);
		if(state!=null) {
			state.setIcon(icon);
			eventSource.fireEvent(new EventObject(
					Events.CHANGED, "task", task)); //$NON-NLS-1$
		}
	}
	
	public boolean isIndeterminate(Object task) {
		TaskState state = getTaskState(task);
		
		return state==null ? null : state.isIndeterminate();
	}
	
	public void setIndeterminate(Object task, boolean indeterminate) {
		TaskState state = getTaskState(task);
		if(state!=null) {
			state.setIndeterminate(indeterminate);
			eventSource.fireEvent(new EventObject(
					Events.CHANGED, "task", task)); //$NON-NLS-1$
		}
	}
	
	public SwingWorker.StateValue getState(Object task) {
		if(!(task instanceof SwingWorker)) {
			task = wrapperMap.get(task);
		}
		
		if(task==null)
			throw new IllegalArgumentException("Invalid task"); //$NON-NLS-1$
		
		return ((SwingWorker<?, ?>)task).getState();
	}
	
	public int getProgress(Object task) {
		if(!(task instanceof SwingWorker)) {
			task = wrapperMap.get(task);
		}
		
		if(task==null)
			throw new IllegalArgumentException("Invalid task"); //$NON-NLS-1$
		
		return ((SwingWorker<?, ?>)task).getProgress();
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
		
		if(task instanceof Runnable) {
			worker = Tasks.createWorker((Runnable)task);
			wrapperMap.put(task, worker);
		} else if(task instanceof Callable) {
			worker = Tasks.createWorker((Callable<?>)task);
			wrapperMap.put(task, worker);
		} else if(task instanceof SwingWorker) {
			worker = (SwingWorker<?, ?>) task;
		}
		
		if(!(worker instanceof SwingWorker))
			throw new IllegalArgumentException("Invalid task"); //$NON-NLS-1$
				
		boolean result = true;
		
		// Save task identity in state object
		if(identity!=null) {
			TaskState state = new TaskState(priority, identity);		
			states.put(worker, state);
		}
		
		// Try to add task to queue
		if(unique) {
			result = taskQueue.insertIfAbsent(task, priority);
		} else {
			taskQueue.insert(task, priority);
		}
		
		// Clear lookup tables if adding failed
		if(!result) {
			states.remove(worker);
			wrapperMap.remove(task);
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
			throw new IllegalArgumentException("Invalid task"); //$NON-NLS-1$
		
		return task==currentWorker;
	}
	
	public synchronized Object getActiveTask() {
		SwingWorker<?, ?> worker = currentWorker;
		return worker==null ? null : getTask(worker);
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

			executorService = new ThreadPoolExecutor(MAX_WORKER_THREADS,
					MAX_WORKER_THREADS, 10L, TimeUnit.MINUTES,
					new LinkedBlockingQueue<Runnable>(), threadFactory,
					rejectedTaskExecutionHandler);
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
		 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return null;
		}
		
		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return title;
		}
		
		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return info;
		}
		
		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return icon;
		}
		
		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getOwner()
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
				
			case "state": //$NON-NLS-1$
				if(worker.isDone()) {
					worker.removePropertyChangeListener(this);
					
					// Integrity check
					if(worker!=currentWorker)
						throw new IllegalStateException();
					
					// Remove current worker
					currentWorker = null;
					eventSource.fireEvent(new EventObject(
							TaskConstants.ACTIVE_TASK_CHANGED));
					
					// Schedule to start next worker
					dispatch();
					break;
				}

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
				worker.addPropertyChangeListener(this);
				
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
		
		/**
		 * Fetches the first task in any priority queue starting with
		 * the highest priority.
		 */
		Object getNext() {
			lock.lock();
			Object task = null;
			try {
				if(queue.size()==0) {
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
}
