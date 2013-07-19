/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.search_tools.InvalidSearchGraphException;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchGraph;
import de.ims.icarus.search_tools.SearchMode;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.result.EntryBuilder;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.standard.GroupCache;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.ui.dialog.DialogDispatcher;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.Orientation;
import de.ims.icarus.util.data.DataList;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractTreeSearch extends Search {
	
	protected Matcher baseRootMatcher;
	
	protected SearchResult result;
	
	protected DataList<?> source;
	protected int processed;
	
	protected int pendingWorkers;
	protected List<SearchWorker> workers = Collections.synchronizedList(new ArrayList<SearchWorker>());
	protected Set<Integer> pendingIndices = Collections.synchronizedSet(new HashSet<Integer>());
	protected Queue<ItemBuffer> pendingItems = new LinkedList<>();
	
	protected final Object notifer = new Object();
	
	protected int nextItemIndex = 0;
	
	protected final int resultLimit;
	protected final SearchMode searchMode;
	protected final Orientation orientation;
	
	protected static final int DEFAULT_BARRIER_TIMEOUT_SECONDS = 60;
	
	protected AbstractTreeSearch(SearchFactory factory, SearchQuery query,
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
		if(!validateTree()) {
			// Validation already shows a dialog
			return false;
		}
		
		baseRootMatcher = new MatcherBuilder(this).createRootMatcher();
		if(baseRootMatcher==null)
			throw new IllegalStateException("Invalid root matcher created"); //$NON-NLS-1$
		
		result = createResult();
		if(result==null) {
			return false;
		}
		
		source = createSource(getTarget());
		if(source==null)
			throw new IllegalStateException("Invalid source created"); //$NON-NLS-1$
		
		// Now init those objects
		baseRootMatcher.setLeftToRight(SearchUtils.isLeftToRightSearch(this));
		
		return true;
	}

	@Override
	protected void innerCancel() {
		
		for(SearchWorker worker : workers) {
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
	
	protected GroupCache createCache() {
		return result.createCache();
	}
	
	protected boolean validateTree() {
		return TreeUtils.validateTree(getSearchGraph());
	}
	
	protected abstract TargetTree createTargetTree();
	
	protected abstract DataList<?> createSource(Object target);

	protected abstract SearchResult createResult();
	
	protected SearchWorker createWorker(int id) {
		return new SearchWorker(id);
	}
	
	protected Matcher createRootMatcher() {
		if(baseRootMatcher==null)
			throw new IllegalStateException("No root matcher available!"); //$NON-NLS-1$
		
		return new MatcherBuilder(this).cloneMatcher(baseRootMatcher);
	}
	
	protected EntryBuilder createEntryBuilder() {
		return new EntryBuilder(TreeUtils.getMaxId(baseRootMatcher)+1);
	}
	
	protected synchronized void workerFinished(SearchWorker worker) {
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
		int availableCores = Math.max(1, Runtime.getRuntime().availableProcessors()/2);
		if(cores>0) {
			cores = Math.min(cores, availableCores);
		}
		cores = Math.max(cores, 1);
		
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
		
		LoggerFactory.log(this, Level.FINE, "Executing search "+getClass().getSimpleName()+" on "+cores+" cores"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		pendingWorkers = cores;
		
		for(int i=0; i<cores; i++) {
			TaskManager.getInstance().execute(createWorker(i));
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
	
	protected static enum ItemRequestResult {
		ITEM_AVAILABLE,
		ITEM_PENDING,
		NO_MORE_ITEMS,
		RESULT_FILLED,
		SEARCH_FINISHED;
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

	protected class SearchWorker implements Runnable {
		
		protected final TargetTree targetTree;
		protected final GroupCache cache;
		protected final EntryBuilder entryBuilder;
		protected final Matcher rootMatcher;

		protected ItemBuffer buffer;
		
		protected boolean cancelled = false;
		
		private final int id;
		
		private Thread thread;
		
		protected SearchWorker(int id) {
			this.id = id;
			
			targetTree = createTargetTree();
			cache = createCache();
			rootMatcher = createRootMatcher();
			entryBuilder = createEntryBuilder();
			
			rootMatcher.setSearchMode(searchMode);
			rootMatcher.setCache(cache);
			rootMatcher.setTargetTree(targetTree);
			rootMatcher.setEntryBuilder(entryBuilder);
			
			buffer = new ItemBuffer();
		}
		
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
			return cancelled || AbstractTreeSearch.this.isCancelled() || Thread.currentThread().isInterrupted();
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
							// Init utilities
							targetTree.reload(buffer.getData());
							entryBuilder.setIndex(buffer.getIndex());
							
							// Let matcher do its part
							rootMatcher.matches();
							
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
				AbstractTreeSearch.this.cancel();
				
				String message = "plugins.searchTools.tools.dialogs.generalError"; //$NON-NLS-1$
				if(t instanceof OutOfMemoryError) {
					message = "plugins.searchTools.tools.dialogs.outOfMemoryError"; //$NON-NLS-1$
				}
				DialogDispatcher dispatcher = new DialogDispatcher(null, 
						"plugins.searchTools.tools.dialogs.errorTitle",  //$NON-NLS-1$
						message, t);
				dispatcher.showAsError();
			} finally {
				rootMatcher.close();
				targetTree.close();
			}
			
			workerFinished(this);
		}
	}
}
