/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.io.Loadable;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchFactory;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.SearchMode;
import net.ikarus_systems.icarus.search_tools.SearchQuery;
import net.ikarus_systems.icarus.search_tools.annotation.ResultAnnotator;
import net.ikarus_systems.icarus.search_tools.result.EntryBuilder;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.search_tools.standard.GroupCache;
import net.ikarus_systems.icarus.ui.tasks.TaskManager;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.Orientation;
import net.ikarus_systems.icarus.util.data.DataList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractTreeSearch extends Search {
	
	protected Matcher baseRootMatcher;
	
	protected SearchResult result;
	
	protected DataList<?> source;
	
	protected Batch lastBatch;
	protected int processed;

	protected int batchSize = 100;
	
	protected List<SearchWorker> workers = new ArrayList<>();
	
	protected final int resultLimit;
	protected final SearchMode searchMode;
	protected final Orientation orientation;
	
	protected CyclicBarrier barrier;
	
	protected static final int DEFAULT_BARRIER_TIMEOUT_SECONDS = 60;
	
	protected AbstractTreeSearch(SearchFactory factory, SearchQuery query,
			Options parameters, Object target) {
		super(factory, query, parameters, target);
		
		resultLimit = getParameters().getInteger(SEARCH_RESULT_LIMIT, DEFAULT_SEARCH_RESULT_LIMIT);
		searchMode = getParameters().get(SEARCH_MODE, DEFAULT_SEARCH_MODE);
		orientation = getParameters().get(SEARCH_ORIENTATION, DEFAULT_SEARCH_ORIENTATION);
	}
	
	@Override
	public void init() {
		result = createResult();
		baseRootMatcher = new MatcherBuilder(this).createRootMatcher();
		source = createSource(getTarget());
	}

	@Override
	protected void innerCancel() {
		for(SearchWorker worker : workers) {
			worker.cancel();
		}
		workers.clear();
	}
	
	public SearchGraph getSearchGraph() {
		return getQuery().getSearchGraph();
	}
	
	protected synchronized Batch nextBatch() {
		if(isCancelled()) {
			return null;
		}
		
		int start = 0;
		if(lastBatch!=null) {
			start = lastBatch.getStart()+lastBatch.getSize();
		}
		
		int sourceSize = source.size();
		
		if(start<sourceSize) {
			int size = Math.min(batchSize, sourceSize-start);
			lastBatch = new Batch(start, size);
			return lastBatch;
		} else {
			return null;
		}
	}
	
	protected synchronized void batchProcessed(Batch batch) {
		processed += batch.getSize();
		double total = source.size();
		setProgress((int)(processed/total * 100d));
		
		//System.out.println("batch processed: "+batch+" progress="+getProgress());
	}
	
	protected synchronized Object getTargetItem(int index) {
		return source.get(index);
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
	
	protected final CyclicBarrier getBarrier() {
		return barrier;
	}
	
	protected GroupCache createCache() {
		return result.createCache();
	}
	
	protected abstract TargetTree createTargetTree();
	
	protected abstract DataList<?> createSource(Object target);

	protected abstract SearchResult createResult();
	
	protected abstract ResultAnnotator createAnnotator();
	
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

	/**
	 * @see net.ikarus_systems.icarus.search_tools.Search#execute()
	 */
	@Override
	public boolean innerExecute() throws Exception {
		
		// TODO DEBUG
		/*TaskManager.getInstance().execute(new Runnable() {
			
			@Override
			public void run() {
				for(int i=0; i<100; i++) {
					progress = i;
					
					if(isDone()) {
						return;
					}
					
					try {
						Thread.sleep(350);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				finish();
			}
		});*/
		
		if(result==null) {
			return false;
		}
		if(source.size()==0) {
			return false;
		}
		
		Object target = getTarget();
		if(target instanceof Loadable && !((Loadable)target).isLoaded()) {
			try {
				((Loadable)target).load();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to load search target: "+target, e); //$NON-NLS-1$
				throw new IOException("Could not load traget - aborting", e); //$NON-NLS-1$
			}
		}

		// Obtain number of possible concurrent workers
		int cores = ConfigRegistry.getGlobalRegistry().getInteger("plugins.searchTools.maxCores"); //$NON-NLS-1$
		int availableCores = Math.max(1, Runtime.getRuntime().availableProcessors()/2);
		if(cores>0) {
			cores = Math.min(cores, availableCores);
		}
		cores = Math.max(cores, 1);
		
		barrier = new CyclicBarrier(cores, new ResultFinalizer());
		
		for(int i=0; i<cores; i++) {
			TaskManager.getInstance().execute(createWorker(i));
		}
		
		return true;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.Search#getPerformanceInfo()
	 */
	@Override
	public SearchPerformanceInfo getPerformanceInfo() {
		// TODO
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.Search#getResult()
	 */
	@Override
	public SearchResult getResult() {
		return result;
	}
	
	protected static class Batch {
		final int start;
		final int size;
		
		public Batch(int start, int size) {
			this.start = start;
			this.size = size;
		}

		public int getStart() {
			return start;
		}

		public int getSize() {
			return size;
		}
		
		@Override
		public String toString() {
			return String.format("Batch [%d:%d]", start, start+size); //$NON-NLS-1$
		}
	}
	
	protected class ResultFinalizer implements Runnable {

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			finish();
			finalizeResult(false);
		}
	}

	protected class SearchWorker implements Runnable {
		
		protected final TargetTree targetTree;
		protected final GroupCache cache;
		protected final EntryBuilder entryBuilder;
		protected final Matcher rootMatcher;

		protected Batch currentBatch;
		
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
			
			while((currentBatch=nextBatch()) != null) {
				if(isCancelled()) {
					break;
				}
				
				//System.out.println("processing batch: "+currentBatch);
				
				int size = currentBatch.getSize();
				int startIndex = currentBatch.getStart();
				for(int i=0; i<size; i++) {
					// Limited cancel check
					if(cancelled) {
						break;
					}
					if(resultLimit>0 && result.getTotalMatchCount()>=resultLimit) {
						break;
					}
					
					int index = startIndex+i;
					
					// Prepare target tree
					targetTree.reload(getTargetItem(index));
					entryBuilder.setIndex(index);
					
					// Let matcher do its part
					rootMatcher.matches();
				}
				
				batchProcessed(currentBatch);
			}
			
			targetTree.close();
			
			try {
				getBarrier().await(DEFAULT_BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			} catch (InterruptedException | TimeoutException e) {
				// ignore
			} catch (BrokenBarrierException e) {
				finalizeResult(true);
			}
		}
	}
}
