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
import net.ikarus_systems.icarus.search_tools.result.ResultAnnotator;
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
public abstract class AbstractTreeSearch<D extends DataList<?>> extends Search {
	
	protected Matcher baseRootMatcher;
	
	protected SearchResult result;
	
	protected D source;
	
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
		
		resultLimit = getParameters().getInteger(SEARCH_RESULT_LIMIT, 0);
		searchMode = getParameters().get(SEARCH_MODE, SearchMode.MATCHES);
		orientation = getParameters().get(SEARCH_ORIENTATION, Orientation.LEFT_TO_RIGHT);
		
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
			lastBatch = new Batch(start, sourceSize-start);
			return lastBatch;
		} else {
			return null;
		}
	}
	
	protected synchronized void batchProcessed(Batch batch) {
		processed += batch.getSize();
		double total = source.size();
		setProgress((int)(processed/total * 100d));
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
	
	protected abstract D createSource(Object target);

	protected abstract SearchResult createResult();
	
	protected abstract ResultAnnotator createAnnotator();
	
	protected SearchWorker createWorker(int id) {
		return new SearchWorker(id);
	}
	
	protected Matcher createRootMatcher() {
		if(baseRootMatcher==null) {
			baseRootMatcher = new MatcherBuilder(this).createRootMatcher();
		}
		
		return new MatcherBuilder(this).cloneMatcher(baseRootMatcher);
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
	}
	
	protected class ResultFinalizer implements Runnable {

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			finalizeResult(false);
		}
	}

	protected class SearchWorker implements Runnable {
		
		protected final TargetTree targetTree;
		protected final GroupCache cache;
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
			
			rootMatcher.setCache(cache);
			rootMatcher.setTargetTree(targetTree);
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
				
				int size = currentBatch.getSize();
				int startIndex = currentBatch.getStart();
				for(int i=0; i<size; i++) {
					// Limited cancel check
					if(cancelled) {
						break;
					}
					if(result.getTotalMatchCount()>=resultLimit) {
						break;
					}
					
					// Prepare target tree
					targetTree.reload(getTargetItem(startIndex+i));
					
					// Let matcher do its part
					rootMatcher.matches();
				}
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
