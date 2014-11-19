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
package de.ims.icarus.search_tools;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.SwingWorker;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;

import de.ims.icarus.Core;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.io.Loadable;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.search_tools.SearchToolsConstants;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.Wrapper;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class SearchManager {

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
	private static Map<String, Matcher> matcherCache = Collections.synchronizedMap(
			new WeakHashMap<String, Matcher>());

	private static SearchManager instance;

	public static SearchManager getInstance() {
		if(instance==null) {
			synchronized (SearchManager.class) {
				if(instance==null) {
					instance = new SearchManager();
				}
			}
		}

		return instance;
	}

	private SearchManager() {
		// no-op
	}

	public synchronized ConstraintContext getConstraintContext(ContentType contentType) {
		if(contentType==null)
			throw new NullPointerException("Invalid content-type"); //$NON-NLS-1$

		if(contexts==null) {
			contexts = new HashMap<>();
		}

		ConstraintContext context = contexts.get(contentType);
		if(context==null) {
			context = new ConstraintContext(contentType);
			contexts.put(contentType, context);
		}

		return context;
	}

	public static Matcher getMatcher(String s, String input) {
		if(s==null || s.isEmpty()) {
			return null;
		}

		Matcher matcher = matcherCache.remove(s);
		if(matcher==null) {
			// Do not catch PatternSyntaxException!
			// We want whatever operation the pattern request was originated
			// from to be terminated by the exception.
			matcher = Pattern.compile(s).matcher(input);

			// Do not bother with 'duplicates' since all Pattern
			// compiled from the same string are in fact identical in
			// terms of functionality
//			matcherCache.put(s, matcher);
		} else {
			matcher.reset(input);
		}

		return matcher;
	}

	public static void recycleMatcher(Matcher matcher) {
		if (matcher == null)
			throw new NullPointerException("Invalid matcher"); //$NON-NLS-1$

		matcher.reset();

		matcherCache.put(matcher.pattern().pattern(), matcher);
	}

	public static ConstraintFactory[] getEdgeConstraintFactories(ConstraintFactory[] items) {
		if(items==null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$

		List<ConstraintFactory> result = new ArrayList<>();

		for(ConstraintFactory factory : items) {
			if(factory.getConstraintType()==ConstraintFactory.EDGE_CONSTRAINT_TYPE) {
				result.add(factory);
			}
		}

		return result.toArray(new ConstraintFactory[0]);
	}

	public static ConstraintFactory[] getNodeConstraintFactories(ConstraintFactory[] items) {
		if(items==null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$

		List<ConstraintFactory> result = new ArrayList<>();

		for(ConstraintFactory factory : items) {
			if(factory.getConstraintType()==ConstraintFactory.NODE_CONSTRAINT_TYPE) {
				result.add(factory);
			}
		}

		return result.toArray(new ConstraintFactory[0]);
	}

	public static Collection<Extension> getSearchFactoryExtensions() {
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				SearchToolsConstants.SEARCH_TOOLS_PLUGIN_ID, "SearchFactory"); //$NON-NLS-1$
		return extensionPoint.getConnectedExtensions();
	}

	public static List<Extension> getResultPresenterExtensions(ContentType contentType, int dimension) {
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				SearchToolsConstants.SEARCH_TOOLS_PLUGIN_ID, "SearchResultPresenter"); //$NON-NLS-1$

		List<Extension> result = new ArrayList<>();

		for(Extension extension : extensionPoint.getConnectedExtensions()) {
			if(extension.getParameter("dimension").valueAsNumber().intValue()!=dimension) { //$NON-NLS-1$
				continue;
			}

			Collection<Extension.Parameter> params = extension.getParameters("contentType"); //$NON-NLS-1$
			if(contentType==null && !params.isEmpty()) {
				continue;
			}

			if(!params.isEmpty()) {
				boolean compatible = false;

				for(Extension.Parameter param : params) {
					ContentType entryType = ContentTypeRegistry.getInstance().getType(param.valueAsExtension());
					if(ContentTypeRegistry.isCompatible(contentType, entryType)) {
						compatible = true;
						break;
					}
				}

				if(!compatible) {
					continue;
				}
			}

			result.add(extension);
		}

		return result;
	}

	public static boolean isGroupingOperator(SearchOperator operator) {
		return operator==DefaultSearchOperator.GROUPING;
	}

	private static Collection<Extension> availableResultExportHandlers;

	public static Collection<Extension> getResultExportHandlers(SearchResult searchResult) {
		if (searchResult == null)
			throw new NullPointerException("Invalid searchResult"); //$NON-NLS-1$

		if(availableResultExportHandlers==null) {
			ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
					SearchToolsConstants.SEARCH_TOOLS_PLUGIN_ID, "SearchResultExportHandler"); //$NON-NLS-1$
			availableResultExportHandlers = Collections.unmodifiableCollection(extensionPoint.getConnectedExtensions());
		}

		ContentType targetType = searchResult.getContentType();

		List<Extension> result = new ArrayList<>();

		for(Extension extension : availableResultExportHandlers) {
			Extension.Parameter param = extension.getParameter("contentType"); //$NON-NLS-1$
			ContentType handlerType = ContentTypeRegistry.getInstance().getType(param.valueAsExtension());
			if(ContentTypeRegistry.isCompatible(handlerType, targetType)) {
				result.add(extension);
			}
		}

		return result;
	}

	private Map<Extension, SearchFactory> factoryInstances;

	public Collection<Extension> availableSearchFactories() {
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				SearchToolsConstants.SEARCH_TOOLS_PLUGIN_ID, "SearchFactory"); //$NON-NLS-1$
		return Collections.unmodifiableCollection(extensionPoint.getConnectedExtensions());
	}

	public SearchFactory getFactory(Extension extension) {
		if(extension==null)
			throw new NullPointerException("Invalid extension"); //$NON-NLS-1$

		if(factoryInstances==null) {
			factoryInstances = new HashMap<>();
		}


		SearchFactory factory = factoryInstances.get(extension);
		if(factory==null) {
			try {
				factory = (SearchFactory) PluginUtil.instantiate(extension);
			} catch (Exception e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to instantiate search factory: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}

		return factory;
	}

	public Collection<Extension> availableTargetSelectors() {
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				SearchToolsConstants.SEARCH_TOOLS_PLUGIN_ID, "SearchTargetSelector"); //$NON-NLS-1$
		return Collections.unmodifiableCollection(extensionPoint.getConnectedExtensions());
	}

	public static Object getTarget(Search search) {
		if(search==null)
			throw new NullPointerException("Invalid search"); //$NON-NLS-1$

		Object target = search.getTarget();
		if(target instanceof Wrapper) {
			target = ((Wrapper<?>) target).get();
		}
		return target;
	}

	private static Map<Search, ExecuteSearchJob> searchJobMap = new WeakHashMap<>();

	public void executeSearch(Search search) {
		if(search==null)
			throw new NullPointerException("Invalid search"); //$NON-NLS-1$
		if(search.isDone())
			throw new IllegalArgumentException("Search already finished"); //$NON-NLS-1$
		if(search.isCancelled())
			throw new IllegalArgumentException("Search already cancelled"); //$NON-NLS-1$
		if(search.getTarget()==null)
			throw new IllegalArgumentException("No target specified for search"); //$NON-NLS-1$
		if(search.getQuery()==null)
			throw new IllegalArgumentException("Search not properly initialized - query is missing"); //$NON-NLS-1$

		if(searchJobMap.containsKey(search)) {
			return;
		}

		// If target is not loaded delay search execution and attempt to load
		Object target = search.getTarget();
		if(target instanceof Loadable && !((Loadable)target).isLoaded()) {
			TaskManager.getInstance().schedule(new LoadTargetJob(search), TaskPriority.HIGH, true);
			return;
		}

		ExecuteSearchJob task = new ExecuteSearchJob(search);
		searchJobMap.put(search, task);

		TaskManager.getInstance().schedule(task, TaskPriority.DEFAULT, true);
	}

	public void cancelSearch(Search search) {
		if(search==null)
			throw new NullPointerException("Invalid search"); //$NON-NLS-1$

		ExecuteSearchJob task = searchJobMap.get(search);

		if(task!=null) {
			TaskManager.getInstance().cancelTask(task);
		}

		if(search.isRunning()) {
			search.cancel();
		}
	}

	private static class ExecuteSearchJob extends SwingWorker<Object, Object>
			implements Identity, PropertyChangeListener {

		private final Search search;
		private final long timeout;
		private long startMillis;

		private ExecuteSearchJob(Search search) {
			if(search==null)
				throw new NullPointerException("Invalid search"); //$NON-NLS-1$

			this.search = search;

			timeout = ConfigRegistry.getGlobalRegistry().getLong(
					"plugins.searchTools.searchTimeout") * 1000; //$NON-NLS-1$*/

			// TODO DEBUG
//			timeout = 0;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ExecuteSearchJob) {
				return search == ((ExecuteSearchJob)obj).search;
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
			return ResourceManager.getInstance().get(
					"plugins.searchTools.searchManager.executeSearchJob.description", //$NON-NLS-1$
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
			//search.addPropertyChangeListener(this);

			firePropertyChange("indeterminate", false, true); //$NON-NLS-1$

			search.execute();

			firePropertyChange("indeterminate", true, false); //$NON-NLS-1$

			// We start counting time only after the search returned from
			// its execution call to allow for loading of target data which
			// should not be counted against the timeout!
			startMillis = System.currentTimeMillis();

			// Wait for the search to finish
			while(!search.isDone()) {
				// Forward cancellation or interruption to the search object
				if(isCancelled() || Thread.currentThread().isInterrupted()) {
					cancelSearch();
					break;
				}

				setProgress(search.getProgress());

				// Check for timeout
				long duration = System.currentTimeMillis()-startMillis;
				if(timeout!=0 && duration>timeout) {
					cancelSearch();
					throw new TimeoutException(String.format(
							"Search execution timed out: current executon time %d ms - timeout set to %d ms",  //$NON-NLS-1$
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
			} catch(InterruptedException | CancellationException e) {
				cancelSearch();
			} catch(Throwable e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to execute search", e); //$NON-NLS-1$
				cancelSearch();
				UIUtil.beep();

				if(!Core.getCore().handleThrowable(e)) {
					showErrorDialog(e);
				}
			} finally {
				searchJobMap.remove(search);
			}
		}

		private void showErrorDialog(Throwable e) {
			String title = "plugins.searchTools.searchManager.loadTargetJob.errorTitle"; //$NON-NLS-1$
			String message = null;

			// Set message based on error type
			if(e instanceof OutOfMemoryError) {
				message = "plugins.searchTools.searchManager.loadTargetJob.outOfMemoryError"; //$NON-NLS-1$
			} else if(e instanceof InvalidSearchGraphException) {
				message = "plugins.searchTools.searchManager.loadTargetJob.invalidSearchGraph"; //$NON-NLS-1$
			}

			// Ensure valid message string
			if(message==null) {
				message = "plugins.searchTools.searchManager.loadTargetJob.generalError"; //$NON-NLS-1$
			}

			DialogFactory.getGlobalFactory().showError(null, title, message);
		}

		private void cancelSearch() {
			try {
				search.cancel();
			} catch(Exception ex) {
				// ignore
			}
		}

		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			//firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
		}
	}

	public static class LoadTargetJob extends SwingWorker<Loadable, Object>
			implements Identity {

		private final Search search;

		public LoadTargetJob(Search search) {
			if(search==null)
				throw new NullPointerException("Invalid search"); //$NON-NLS-1$

			this.search = search;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof LoadTargetJob) {
				return search == ((LoadTargetJob)obj).search;
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
			return ResourceManager.getInstance().get(
					"plugins.searchTools.searchManager.loadTargetJob.description",  //$NON-NLS-1$
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
			} catch(Exception ex) {
				// ignore
			}
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Loadable doInBackground() throws Exception {

			firePropertyChange("indeterminate", false, true); //$NON-NLS-1$

			Loadable loadable = (Loadable)search.getTarget();
			if(!loadable.isLoaded()) {
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
			} catch(InterruptedException | CancellationException e) {
				cancelSearch();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to load search target: "+String.valueOf(loadable), e); //$NON-NLS-1$
				cancelSearch();
				UIUtil.beep();

				Core.getCore().handleThrowable(e);
			}
		}
	}
}
