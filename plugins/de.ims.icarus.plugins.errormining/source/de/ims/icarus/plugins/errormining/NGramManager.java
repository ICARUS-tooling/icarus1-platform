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
package de.ims.icarus.plugins.errormining;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.SwingWorker;

import de.ims.icarus.io.Loadable;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.ConstraintContext;
import de.ims.icarus.search_tools.ConstraintFactory;
import de.ims.icarus.search_tools.InvalidSearchGraphException;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.id.Identity;

/**
 * @author Gregor Thiele
 * @version $Id$
 * 
 */
public class NGramManager {

	public static final Comparator<ConstraintFactory> FACTORY_SORTER = new Comparator<ConstraintFactory>() {

		@Override
		public int compare(ConstraintFactory o1, ConstraintFactory o2) {
			return o1.getToken().compareTo(o2.getToken());
		}

	};

	private Map<ContentType, ConstraintContext> contexts;

	// Maps strings to their compiled Pattern instance.
	// We use a weak hash map here since we only need the Pattern
	// as long as the respective string is used in some constraint
	private static Map<String, Pattern> patternCache = Collections
			.synchronizedMap(new WeakHashMap<String, Pattern>());

	private static NGramManager instance;

	public static NGramManager getInstance() {
		if (instance == null) {
			synchronized (SearchManager.class) {
				if (instance == null) {
					instance = new NGramManager();
				}
			}
		}

		return instance;
	}

	private NGramManager() {
		// no-op
	}

	private static Map<Search, ExecuteSearchJob> searchJobMap = new WeakHashMap<>();

	public void executeSearch(Search search) {
		if (search == null)
			throw new NullPointerException("Invalid search"); //$NON-NLS-1$
		if (search.isDone())
			throw new IllegalArgumentException("Search already finished"); //$NON-NLS-1$
		if (search.isCancelled())
			throw new IllegalArgumentException("Search already cancelled"); //$NON-NLS-1$
		if (search.getTarget() == null)
			throw new IllegalArgumentException("No target specified for search"); //$NON-NLS-1$
		if (search.getQuery() == null)
			throw new IllegalArgumentException(
					"Search not properly initialized - query is missing"); //$NON-NLS-1$

		if (searchJobMap.containsKey(search)) {
			return;
		}

		// If target is not loaded delay search execution and attempt to load
		Object target = search.getTarget();
		if (target instanceof Loadable && !((Loadable) target).isLoaded()) {
			TaskManager.getInstance().schedule(new LoadTargetJob(search),
					TaskPriority.HIGH, true);
			return;
		}

		ExecuteSearchJob task = new ExecuteSearchJob(search);
		searchJobMap.put(search, task);

		TaskManager.getInstance().schedule(task, TaskPriority.DEFAULT, true);
	}

	public void cancelSearch(Search search) {
		if (search == null)
			throw new NullPointerException("Invalid search"); //$NON-NLS-1$

		ExecuteSearchJob task = searchJobMap.get(search);

		if (task != null) {
			TaskManager.getInstance().cancelTask(task);
		}

		if (search.isRunning()) {
			search.cancel();
		}
	}

	private static class ExecuteSearchJob extends SwingWorker<Object, Object>
			implements Identity, PropertyChangeListener {

		private final Search search;
		private final long timeout;
		private long startMillis;

		private ExecuteSearchJob(Search search) {
			if (search == null)
				throw new NullPointerException("Invalid search"); //$NON-NLS-1$

			this.search = search;

			/*
			 * timeout = ConfigRegistry.getGlobalRegistry().getLong(
			 * "plugins.searchTools.searchTimeout") * 1000; //$NON-NLS-1$
			 */

			// TODO DEBUG
			timeout = 0;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ExecuteSearchJob) {
				return search == ((ExecuteSearchJob) obj).search;
			}
			return false;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return getClass().getSimpleName();
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"plugins.searchTools.searchManager.executeSearchJob.name"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return ResourceManager
					.getInstance()
					.get("plugins.searchTools.searchManager.executeSearchJob.description", //$NON-NLS-1$
					search.getProgress());
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Object doInBackground() throws Exception {
			// search.addPropertyChangeListener(this);

			firePropertyChange("indeterminate", false, true); //$NON-NLS-1$

			search.execute();

			firePropertyChange("indeterminate", true, false); //$NON-NLS-1$

			// We start counting time only after the search returned from
			// its execution call to allow for loading of target data which
			// should not be counted against the timeout!
			startMillis = System.currentTimeMillis();

			// Wait for the search to finish
			while (!search.isDone()) {
				// Forward cancellation or interruption to the search object
				if (isCancelled() || Thread.currentThread().isInterrupted()) {
					cancelSearch();
					break;
				}

				setProgress(search.getProgress());

				// Check for timeout
				long duration = System.currentTimeMillis() - startMillis;
				if (timeout != 0 && duration > timeout) {
					cancelSearch();
					throw new TimeoutException(
							String.format(
									"Search execution timed out: current executon time %d ms - timeout set to %d ms", //$NON-NLS-1$
									duration, timeout));
				}

				// Sleep for another period
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					cancelSearch();
				}
			}

			return null;
		}

		@Override
		protected void done() {
			try {
				get();
			} catch (InterruptedException | CancellationException e) {
				cancelSearch();
			} catch (Throwable e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to execute search", e); //$NON-NLS-1$
				cancelSearch();
				UIUtil.beep();

				showErrorDialog(e);
			} finally {
				searchJobMap.remove(search);
			}
		}

		private void showErrorDialog(Throwable e) {
			String title = "plugins.searchTools.searchManager.loadTargetJob.title"; //$NON-NLS-1$
			String message = null;

			// Set message based on error type
			if (e instanceof OutOfMemoryError) {
				message = "plugins.searchTools.searchManager.loadTargetJob.outOfMemoryError"; //$NON-NLS-1$
			} else if (e instanceof InvalidSearchGraphException) {
				message = "plugins.searchTools.searchManager.loadTargetJob.invalidSearchGraph"; //$NON-NLS-1$
			}

			// Ensure valid message string
			if (message == null) {
				message = "plugins.searchTools.searchManager.loadTargetJob.generalError"; //$NON-NLS-1$
			}

			DialogFactory.getGlobalFactory().showError(null, title, message);
		}

		private void cancelSearch() {
			try {
				search.cancel();
			} catch (Exception ex) {
				// ignore
			}
		}

		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
			// evt.getNewValue());
		}
	}

	private static class LoadTargetJob extends SwingWorker<Loadable, Object>
			implements Identity {

		private final Search search;

		private LoadTargetJob(Search search) {
			this.search = search;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LoadTargetJob) {
				return search == ((LoadTargetJob) obj).search;
			}
			return false;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return getClass().getSimpleName();
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"plugins.searchTools.searchManager.loadTargetJob.name"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return ResourceManager
					.getInstance()
					.get("plugins.searchTools.searchManager.loadTargetJob.description", //$NON-NLS-1$
					search.getTarget());
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}

		private void cancelSearch() {
			try {
				search.cancel();
			} catch (Exception ex) {
				// ignore
			}
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Loadable doInBackground() throws Exception {

			firePropertyChange("indeterminate", false, true); //$NON-NLS-1$

			Loadable loadable = (Loadable) search.getTarget();
			if (!loadable.isLoaded()) {
				loadable.load();
			}

			firePropertyChange("indeterminate", true, false); //$NON-NLS-1$

			return loadable;
		}

		@Override
		protected void done() {
			Loadable loadable = null;
			try {
				loadable = get();

				SearchManager.getInstance().executeSearch(search);
			} catch (InterruptedException | CancellationException e) {
				cancelSearch();
			} catch (Exception e) {
				LoggerFactory
						.log(this,
								Level.SEVERE,
								"Failed to load search target: " + String.valueOf(loadable), e); //$NON-NLS-1$
				cancelSearch();
				UIUtil.beep();
			}
		}
	}
}
