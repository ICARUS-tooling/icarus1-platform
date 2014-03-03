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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.ui.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import de.ims.icarus.logging.LoggerFactory;

/**
 * Allows a buffered dispatching of arbitrary events on the <i>event-dispatch thread</i>.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EventDispatchBridge {

	private final Object lock = new Object();

	private List<EventWrapper> queue = new ArrayList<>();
	private AtomicBoolean taskDispatched = new AtomicBoolean();

	private EventDispatcher dispatcher;

	/**
	 * Creates a initially deactivated bridge.
	 */
	public EventDispatchBridge() {
		// no-op
	}

	/**
	 * Creates a new bridge that uses the given {@code dispatcher} to
	 * perform the final event handling on the <i>event-dispatch thread</i>
	 *
	 * @param dispatcher
	 */
	public EventDispatchBridge(EventDispatcher dispatcher) {
		if (dispatcher == null)
			throw new NullPointerException("Invalid dispatcher"); //$NON-NLS-1$

		setDispatcher(dispatcher);
	}

	private Runnable task = new Runnable() {

		@Override
		public void run() {
			processQueue();
		}
	};

	/**
	 * @return the dispatcher
	 */
	public EventDispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * @param dispatcher the dispatcher to set
	 */
	public void setDispatcher(EventDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public void queueEvent(Object type, Object...args) {
		if (type == null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$

		doQueue(new EventWrapper(type, args));
	}

	public void queueEvent() {
		doQueue(new EventWrapper());
	}

	private void doQueue(EventWrapper wrapper) {
		if(dispatcher==null) {
			return;
		}

		if(SwingUtilities.isEventDispatchThread()) {
			dispatch(wrapper);
		} else {
			synchronized (lock) {
				queue.add(wrapper);

				if(taskDispatched.compareAndSet(false, true)) {
					SwingUtilities.invokeLater(task);
				}
			}
		}
	}

	private void processQueue() {
		List<EventWrapper> items;

		synchronized (lock) {
			if(queue.isEmpty()) {
				return;
			}

			items = new ArrayList<>(queue);
			queue.clear();

			taskDispatched.set(false);
		}

		for(EventWrapper wrapper : items) {
			dispatch(wrapper);
		}
	}

	private void dispatch(EventWrapper wrapper) {
		if(dispatcher==null) {
			return;
		}

		try {
			dispatcher.dispatch(wrapper);
		} catch(Exception e) {
			LoggerFactory.error(this, "Unexpected error while dispatching event: "+wrapper, e); //$NON-NLS-1$
		}
	}

	public interface EventDispatcher {

		void dispatch(EventWrapper wrapper);
	}

	public static final class EventWrapper {

		private final Object type;
		private final Object[] args;

		private EventWrapper() {
			this(null, null);
		}

		private EventWrapper(Object type, Object[] args) {
			this.type = type;
			this.args = args;
		}

		public Object getType() {
			return type;
		}

		public int argCount() {
			return args==null ? 0 : args.length;
		}

		public Object arg(int index) {
			return args[index];
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append('{');
			if(type==null) {
				sb.append("Typeless Event"); //$NON-NLS-1$
			} else {
				sb.append(type);
			}

			if(args!=null && args.length>0) {
				sb.append(' ');
				sb.append(Arrays.toString(args));
			}

			sb.append('}');

			return sb.toString();
		}
	}
}
