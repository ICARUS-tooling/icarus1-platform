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
package de.ims.icarus.search_tools.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;

import org.java.plugin.registry.Extension;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.search_tools.InvalidSearchGraphException;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchGraph;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchMode;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.SearchState;
import de.ims.icarus.search_tools.annotation.AnnotationBuffer;
import de.ims.icarus.search_tools.annotation.ResultAnnotator;
import de.ims.icarus.search_tools.corpus.ConstraintUnifier;
import de.ims.icarus.search_tools.io.SearchResolver;
import de.ims.icarus.search_tools.io.SearchWriter;
import de.ims.icarus.search_tools.result.AbstractSearchResult;
import de.ims.icarus.search_tools.result.DefaultSearchResult0D;
import de.ims.icarus.search_tools.result.DefaultSearchResultND;
import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.ui.dialog.DialogDispatcher;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.Orientation;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.data.DataList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractParallelSearch extends Search {

	public static final int ANNOTATION_BUFFER_SIZE = 300;

	protected SearchResult result;

	protected DataList<?> source;
	protected int processed;

	protected int pendingWorkers;
	protected List<Worker> workers = Collections.synchronizedList(new ArrayList<Worker>());
	protected Set<Integer> pendingIndices = Collections.synchronizedSet(new HashSet<Integer>());
	protected Queue<ItemBuffer> pendingItems = new LinkedList<>();

	protected final Object notifer = new Object();

	protected int nextItemIndex = 0;

	protected final int resultLimit;
	protected final SearchMode searchMode;
	protected final Orientation orientation;

	public AbstractParallelSearch(SearchFactory factory, SearchQuery query,
			Options parameters, Object target) {
		super(factory, query, parameters, target);

		resultLimit = getParameters().getInteger(SEARCH_RESULT_LIMIT, DEFAULT_SEARCH_RESULT_LIMIT);
		searchMode = getParameters().get(SEARCH_MODE, DEFAULT_SEARCH_MODE);
		orientation = getParameters().get(SEARCH_ORIENTATION, DEFAULT_SEARCH_ORIENTATION);
	}

	@Override
	public boolean init() {
		if(SearchUtils.isEmpty(getSearchGraph()))
			throw new InvalidSearchGraphException("Graph is empty"); //$NON-NLS-1$
		if(!validateGraph()) {
			// Validation already shows a dialog
			return false;
		}

		initEngine();

		result = createResult();
		if(result==null) {
			return false;
		}

		source = createSource(SearchManager.getTarget(this));
		if(source==null)
			throw new IllegalStateException("Invalid source created"); //$NON-NLS-1$

		return true;
	}

	@Override
	protected void innerCancel() {

		for(Worker worker : workers) {
			worker.cancel();
		}

		// Allow all workers to properly finish their last cycle
		synchronized (notifer) {
			notifer.notifyAll();
		}

		workers.clear();
	}

	public SearchGraph getSearchGraph() {
		return getQuery().getSearchGraph();
	}

	protected ItemRequestResult nextItem(ItemBuffer buffer) {
		if(isCancelled()) {
			return ItemRequestResult.SEARCH_FINISHED;
		}

		synchronized (result) {
			if(resultLimit>0 && result.getTotalMatchCount()>=resultLimit) {
				return ItemRequestResult.RESULT_FILLED;
			}
		}

		// Check cached items
		synchronized (pendingItems) {
			ItemBuffer cached = pendingItems.poll();
			if(cached!=null) {
				buffer.copy(cached);
				return ItemRequestResult.ITEM_AVAILABLE;
			}
		}

		synchronized (this) {
			int sourceSize = source.size();
			int index = nextItemIndex++;

			if(index<sourceSize) {
				Object data = getTargetItem(index);

				if(data!=null) {
					buffer.set(index, data);
					return ItemRequestResult.ITEM_AVAILABLE;
				} else {
					pendingIndices.add(index);
					return ItemRequestResult.ITEM_PENDING;
				}
			}
		}

		return ItemRequestResult.NO_MORE_ITEMS;
	}

	protected boolean hasUnprocessedItems() {
		if(isCancelled()) {
			return false;
		}

		synchronized (pendingItems) {
			if(!pendingItems.isEmpty()) {
				return true;
			}
		}

		synchronized (this) {
			if(nextItemIndex<source.size()) {
				return true;
			}
		}

		return !pendingIndices.isEmpty();
	}

	protected void awaitItem() throws InterruptedException {
		synchronized (notifer) {
			notifer.wait();
		}
	}

	protected void itemProcessed(ItemBuffer buffer) {
		synchronized (this) {
			processed++;
			double total = source.size();
			setProgress((int)(processed/total * 100d));
		}
	}

	/**
	 * Tries to fetch an item from the target list. If the
	 * item is currently not available (indicated by a return
	 * value of {@code null}) the index will be stored as pending.
	 */
	protected Object getTargetItem(int index) {
		return source.get(index);
	}

	protected void offerItem(int index, Object data) {
		synchronized (pendingItems) {
			pendingItems.add(new ItemBuffer(index, data));
		}

		pendingIndices.remove(index);

		synchronized (notifer) {
			// TODO should we notify more than one worker?
			notifer.notify();
		}
	}

	protected synchronized void finalizeResult(boolean broken) {
		if(result.isFinal()) {
			return;
		}

		if(broken) {
			result.clear();
		}

		result.finish();
	}

	protected abstract boolean validateGraph();

	protected abstract void initEngine();

	protected abstract DataList<?> createSource(Object target);

	protected SearchResult createResult() {
		List<SearchConstraint> groupConstraints = null;

		try {
			// Try to unify group constraints
			groupConstraints = new ConstraintUnifier(getSearchGraph()).getGroupConstraints();
		} catch(Exception e) {
			LoggerFactory.log(this, Level.WARNING,
					"Aggregation of group-constraints failed", e); //$NON-NLS-1$
		}

		/* If unifying the group constraints failed allow user
		 * to manually override and switch to raw collection of
		 * all existing group constraints (ignoring duplicates)
		 *
		 * 'ok' will cause collection of all group constraints
		 * without aggregation check
		 */
		if(groupConstraints==null) {
			boolean doPlainUnify = ConfigRegistry.getGlobalRegistry().getBoolean(
					"plugins.searchTools.alwaysUnifyNonAggregatedConstraints"); //$NON-NLS-1$

			if(!doPlainUnify) {
				MutableBoolean check = new MutableBoolean(false);
				doPlainUnify = DialogFactory.getGlobalFactory().showCheckedConfirm(
						null, DialogFactory.CONTINUE_CANCEL_OPTION, check,
						"plugins.searchTools.graphValidation.title",  //$NON-NLS-1$
						"config.alwaysUnifyNonAggregatedConstraints", //$NON-NLS-1$
						"plugins.searchTools.graphValidation.ununifiedGroups"); //$NON-NLS-1$

				if(check.getValue()) {
					ConfigRegistry.getGlobalRegistry().setValue(
							"plugins.searchTools.alwaysUnifyNonAggregatedConstraints",  //$NON-NLS-1$
							true);
				}
			}

			if(doPlainUnify) {
				groupConstraints = ConstraintUnifier.collectUnunifiedGroupConstraints(getSearchGraph());
			} else {
				return null;
			}
		}

		if(groupConstraints==null) {
			groupConstraints = Collections.emptyList();
		}

		ContentType entryType = ContentTypeRegistry.getEntryType(getTarget());

		/* Allow user to run search with a dimension that is not
		 * covered by a specialized result presenter.
		 *
		 * 'ok' will cause search to ignore group count limits
		 */
		boolean forceFallback = false;
		int dimension = groupConstraints.size();
		List<Extension> presenters = SearchManager.getResultPresenterExtensions(
				entryType, dimension);
		if(presenters==null || presenters.isEmpty()) {
			if(!DialogFactory.getGlobalFactory().showConfirm(null, DialogFactory.CONTINUE_CANCEL_OPTION,
					"plugins.searchTools.graphValidation.title",  //$NON-NLS-1$
					"plugins.searchTools.graphValidation.groupLimitExceeded",  //$NON-NLS-1$
					dimension)) {
				return null;
			}
			forceFallback = true;
		}

		SearchResult result = createResult(groupConstraints);

		if(forceFallback) {
			result.setProperty(SearchResult.FORCE_SIMPLE_OUTLINE_PROPERTY, forceFallback);
		}

		ResultAnnotator annotator = createAnnotator();
		if(annotator!=null && result instanceof AbstractSearchResult) {
			AnnotationBuffer annotationBuffer = new AnnotationBuffer(
					result, annotator, ANNOTATION_BUFFER_SIZE);
			((AbstractSearchResult)result).setAnnotationBuffer(annotationBuffer);
		}

		return result;
	}

	protected SearchResult createResult(List<SearchConstraint> groupConstraints) {

		/* Only distinguish between 0D and ND where N>0 since 0D
		 * can be implemented efficiently by using a simple list storage.
		 */
		if(groupConstraints.isEmpty()) {
			return new DefaultSearchResult0D(this);
		} else {
			return new DefaultSearchResultND(this,
					groupConstraints.toArray(new SearchConstraint[0]));
		}

	}

	protected abstract ResultAnnotator createAnnotator();

	protected abstract Worker createWorker(int id);

	protected synchronized void workerFinished(Worker worker) {
		pendingWorkers--;
		if(pendingWorkers>0) {
			return;
		}

		// Properly finish search
		if(!isDone()) {
			finish();
		}
		finalizeResult(false);
	}

	protected int getMaxWorkerCount() {
		int cores = ConfigRegistry.getGlobalRegistry().getInteger("plugins.searchTools.maxCores"); //$NON-NLS-1$
		int availableCores = Math.max(1, Runtime.getRuntime().availableProcessors()-1);
		if(cores>0) {
			availableCores = Math.min(cores, availableCores);
		}
		cores = Math.max(availableCores, 1);

		return cores;
	}

	/**
	 * @see de.ims.icarus.search_tools.Search#execute()
	 */
	@Override
	public boolean innerExecute() throws Exception {

		if(result==null) {
			return false;
		}
		if(source.size()==0) {
			return false;
		}

		// Obtain number of possible concurrent workers
		int cores = getMaxWorkerCount();

		LoggerFactory.log(this, Level.INFO, "Executing search "+getClass().getSimpleName()+" on "+cores+" cores"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		pendingWorkers = cores;

		for(int i=0; i<cores; i++) {
			Worker worker = createWorker(i);
			workers.add(worker);
			TaskManager.getInstance().execute(worker);
		}

		return true;
	}

	/**
	 * @see de.ims.icarus.search_tools.Search#getPerformanceInfo()
	 */
	@Override
	public SearchPerformanceInfo getPerformanceInfo() {
		// TODO
		return null;
	}

	/**
	 * @see de.ims.icarus.search_tools.Search#getResult()
	 */
	@Override
	public SearchResult getResult() {
		return result;
	}

	/**
	 * @see de.ims.icarus.search_tools.Search#getSearchResolver()
	 */
	@Override
	public SearchResolver getSearchResolver() {
		return new DefaultSearchResolver();
	}

	public class DefaultSearchResolver implements SearchResolver {

//		/**
//		 * @see de.ims.icarus.search_tools.io.SearchResolver#initSearch()
//		 */
//		@Override
//		public void initSearch() {
//			init();
//		}

		/**
		 * @see de.ims.icarus.search_tools.io.SearchResolver#writeResultEntries(de.ims.icarus.search_tools.io.SearchWriter)
		 */
		@Override
		public void writeResultEntries(SearchWriter writer)
				throws XMLStreamException {
			AbstractSearchResult result = (AbstractSearchResult) getResult();

			result.writeEntries(writer);
		}

		/**
		 * @see de.ims.icarus.search_tools.io.SearchResolver#addResultEntry(de.ims.icarus.search_tools.result.ResultEntry, int[])
		 */
		@Override
		public void addResultEntry(ResultEntry entry, int... indices) {
			AbstractSearchResult result = (AbstractSearchResult) getResult();
			result.addEntry(entry, indices);
		}

		/**
		 * @see de.ims.icarus.search_tools.io.SearchResolver#finalizeSearch()
		 */
		@Override
		public void finalizeSearch() {
			finalizeResult(false);
			setState(SearchState.DONE);
		}

		/**
		 * @see de.ims.icarus.search_tools.io.SearchResolver#setGroupLabels(int, java.lang.String[])
		 */
		@Override
		public void setGroupLabels(int dimension, String[] labels) {
			AbstractSearchResult result = (AbstractSearchResult) getResult();
			result.setGroupInstances(dimension, labels);
		}

		/**
		 * @see de.ims.icarus.search_tools.io.SearchResolver#prepareWrite(de.ims.icarus.util.Options)
		 */
		@Override
		public Options prepareWrite(Options options) {
			return options;
		}

	}

	protected static enum ItemRequestResult {
		ITEM_AVAILABLE,
		ITEM_PENDING,
		NO_MORE_ITEMS,
		RESULT_FILLED,
		SEARCH_FINISHED;
	}

	protected abstract class Worker implements Runnable {

		protected ItemBuffer buffer;

		protected boolean cancelled = false;

		private final int id;

		private Thread thread;

		protected Worker(int id) {
			this.id = id;

			init();

			buffer = new ItemBuffer();
		}

		protected abstract void init();

		public String getId() {
			return "SearchWorker-"+id; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return getId();
		}

		public void cancel() {
			cancelled = true;
			// TODO maybe a bit redundant?
			getThread().interrupt();
		}

		public boolean isCancelled() {
			return cancelled || AbstractParallelSearch.this.isCancelled() || Thread.currentThread().isInterrupted();
		}

		public final Thread getThread() {
			if(thread==null)
				throw new IllegalStateException("Worker still pending for execution"); //$NON-NLS-1$

			return thread;
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			// Save reference to current thread
			thread = Thread.currentThread();

			try {
				search_loop : while(!isCancelled() && hasUnprocessedItems()) {
					switch (nextItem(buffer)) {
					case ITEM_AVAILABLE: {
							// Perform search operation on item
							process();

							// Notify search of processed item
							itemProcessed(buffer);
						}
						break;
					case ITEM_PENDING:
						try {
							awaitItem();
						} catch (InterruptedException e) {
							break search_loop;
						}
						break;

					default:
						break search_loop;
					}
				}

			} catch(Throwable t) {
				LoggerFactory.log(this, Level.SEVERE,
						"Unexpected error during search", t); //$NON-NLS-1$
				AbstractParallelSearch.this.cancel();

				String message = "plugins.searchTools.tools.dialogs.generalError"; //$NON-NLS-1$
				if(t instanceof OutOfMemoryError) {
					message = "plugins.searchTools.tools.dialogs.outOfMemoryError"; //$NON-NLS-1$
				}
				DialogDispatcher dispatcher = new DialogDispatcher(null,
						"plugins.searchTools.tools.dialogs.errorTitle",  //$NON-NLS-1$
						message, t);
				dispatcher.showAsError();
			} finally {
				cleanup();
			}

			workerFinished(this);
		}

		protected abstract void process();

		protected abstract void cleanup();
	}

	protected static class ItemBuffer {
		private int index;
		private Object data;

		public ItemBuffer() {
			// no-op
		}

		public ItemBuffer(int index, Object data) {
			set(index, data);
		}

		public void set(int index, Object data) {
			this.index = index;
			this.data = data;
		}

		public int getIndex() {
			return index;
		}

		public Object getData() {
			return data;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public void setData(Object data) {
			this.data = data;
		}

		public void copy(ItemBuffer source) {
			index = source.index;
			data = source.data;
		}
	}
}
