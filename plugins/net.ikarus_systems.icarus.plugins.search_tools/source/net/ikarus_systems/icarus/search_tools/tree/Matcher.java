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

import java.util.Iterator;

import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.SearchMode;
import net.ikarus_systems.icarus.search_tools.SearchNode;
import net.ikarus_systems.icarus.search_tools.result.EntryBuilder;
import net.ikarus_systems.icarus.search_tools.standard.GroupCache;
import net.ikarus_systems.icarus.util.CorruptedStateException;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Matcher implements Cloneable, Comparable<Matcher> {
	protected final SearchNode node;
	protected final SearchEdge edge;
	
	// Flag to indicate that this matcher is part of
	// a sub-tree that serves as exclusion and therefore
	// no successful match should ever be committed
	protected boolean exclusionMember;
	
	protected int id;
	
	protected SearchConstraint[] constraints;
	
	protected Matcher parent;
	protected Matcher[] exclusions;
	protected Matcher next;
	protected Matcher alternate;
	
	protected Matcher[] before;
	protected Matcher[] after;
	
	protected TargetTree targetTree;
	protected GroupCache cache;
	protected EntryBuilder entryBuilder;
	
	protected int allocation;
	
	protected int height;
	protected int descendantCount;
			
	protected boolean exhaustive = false;
	protected SearchMode searchMode = SearchMode.MATCHES;
	protected boolean leftToRight = true;
	
	protected IndexIterator indexIterator = new LTRIterator();
	
	public Matcher(SearchNode node, SearchEdge edge) {
		if(node==null)
			throw new IllegalArgumentException("Invalid node"); //$NON-NLS-1$
		
		this.node = node;
		this.edge = edge;
	}
	
	public boolean matches() {					
		int parentAllocation = parent.getAllocation();
		targetTree.viewNode(parentAllocation);
		indexIterator.setMax(targetTree.getEdgeCount()-1);
		
		boolean matched = false;
		boolean excluded = false;
		
		int minIndex = getMinIndex();
		int maxIndex = getMaxIndex();
				
		while(indexIterator.hasNext()) {
			targetTree.viewNode(parentAllocation);
			targetTree.viewChild(indexIterator.next());
			
			// Honor locked nodes that are allocated to other matchers!
			if(targetTree.isNodeLocked()) {
				continue;
			}
			
			// Check for structural constraints 
			if(targetTree.getDescendantCount()<descendantCount
					|| targetTree.getHeight()<height) {
				continue;
			}
			
			// Check for required number of children
			if(targetTree.getEdgeCount()<node.getChildCount()) {
				continue;
			}
			
			// Check for precedence constraints
			if(targetTree.getNodeIndex()<minIndex
					|| targetTree.getNodeIndex()>maxIndex) {
				continue;
			}
			
			// Check if the current node is a potential match
			if(!matchesConstraints()) {
				continue;
			}
			
			// Lock allocation
			allocate();
			
			// Search for child matchers that serve as exclusions
			excluded = false;
			if(exclusions!=null) {
				for(Matcher matcher : exclusions) {
					if(matcher.matches()) {
						excluded = true; 
						break;
					}
				}
			}
			
			if(!excluded) {
				// Delegate further search to the next matcher
				// or otherwise commit current match
				if(next!=null) {
					matched |= next.matches();
				} else if(!exclusionMember) {
					// ONLY cache here if this matcher is not a
					// member of a sub-tree that serves as exclusion
					cacheHits();
					
					// Commit if every hit should be reported independently
					if(searchMode==SearchMode.INDEPENDENT_HITS) {
						commit();
					}
					
					matched = true;
				}
			}

			// Release lock
			deallocate();
			
			// Stop search if only one successful hit is required
			// This is the case when either a non-exhaustive search
			// takes place or the matcher is a part of a sub-tree
			// serving as exclusion
			if(matched && (exclusionMember || !exhaustive)) {
				break;
			}
		}
		
		// Return scope to parent node
		targetTree.viewNode(parentAllocation);
		
		// If unsuccessful and part of a disjunction let the 
		// alternate matcher have a try.
		if(!matched && alternate!=null) {
			matched = alternate.matches();
		}
		
		return matched;
	}
	
	protected boolean matchesConstraints() {
		if(constraints==null) {
			return true;
		}
		
		for(SearchConstraint constraint : constraints) {
			if(!constraint.matches(getTargetTree())) {
				return false;
			}
		}
		
		return true;
	}
	
	protected void commit() {
		cache.commit(entryBuilder.toEntry());
	}
	
	protected void cacheHits() {
		entryBuilder.commitAllocation();
	}
	
	public int getAllocation() {
		return allocation;
	}
	
	public void deallocate() {
		targetTree.unlockNode(allocation);
		allocation = -1;
		entryBuilder.deallocate(id);
	}
	
	protected void allocate() {
		targetTree.lockNode();
		allocation = targetTree.getNodeIndex();
		entryBuilder.allocate(id, allocation);
	}
	
	/**
	 * Finds the minimum allowed index for this matcher
	 * considering the allocation of all previous matchers.
	 */
	public int getMinIndex() {
		int min = 0;
		
		if(before!=null) {
			for(Matcher matcher : before) {
				min = Math.max(min, matcher.getAllocation());
			}
		}
		
		return min;
	}

	/**
	 * Finds the maximum allowed index for this matcher
	 * considering the allocation of all previous matchers.
	 */
	public int getMaxIndex() {
		int max = targetTree.size()-1;
		
		if(after!=null) {
			for(Matcher matcher :after) {
				max = Math.min(max, matcher.getAllocation());
			}
		}
		
		return max;
	}

	public SearchNode getNode() {
		return node;
	}

	public SearchEdge getEdge() {
		return edge;
	}

	public int getId() {
		return id;
	}

	public SearchConstraint[] getConstraints() {
		return constraints;
	}

	public Matcher getParent() {
		return parent;
	}

	public Matcher getNext() {
		return next;
	}

	public TargetTree getTargetTree() {
		return targetTree;
	}

	public GroupCache getCache() {
		return cache;
	}

	public EntryBuilder getEntryBuilder() {
		return entryBuilder;
	}

	public boolean isExclusionMember() {
		return exclusionMember;
	}

	public Matcher[] getExclusions() {
		return exclusions;
	}

	public Matcher getAlternate() {
		return alternate;
	}

	public Matcher[] getBefore() {
		return before;
	}

	public Matcher[] getAfter() {
		return after;
	}

	public int getHeight() {
		return height;
	}

	public int getDescendantCount() {
		return descendantCount;
	}

	public void setExclusionMember(boolean exclusionMember) {
		this.exclusionMember = exclusionMember;
	}

	public boolean isLeftToRight() {
		return leftToRight;
	}

	public void setLeftToRight(boolean leftToRight) {
		this.leftToRight = leftToRight;
		indexIterator = leftToRight ? new LTRIterator() : new RTLIterator();
		
		if(next!=null) {
			next.setLeftToRight(leftToRight);
		}
		if(alternate!=null) {
			alternate.setLeftToRight(leftToRight);
		}
		if(exclusions!=null) {
			for(Matcher matcher : exclusions) {
				matcher.setLeftToRight(leftToRight);
			}
		}
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setParent(Matcher parent) {
		this.parent = parent;
	}

	public void setExclusions(Matcher[] exclusions) {
		this.exclusions = exclusions;
	}

	public void setNext(Matcher next) {
		this.next = next;
	}

	public void setAlternate(Matcher alternate) {
		this.alternate = alternate;
	}

	public void setBefore(Matcher[] before) {
		this.before = before;
	}

	public void setAfter(Matcher[] after) {
		this.after = after;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setDescendantCount(int descendantCount) {
		this.descendantCount = descendantCount;
	}

	public boolean isExhaustive() {
		return exhaustive;
	}

	public SearchMode getSearchMode() {
		return searchMode;
	}
	
	protected void prepareGroupConstraints() {
		if(constraints==null || cache==null) {
			return;
		}
		
		for(int i=0; i<constraints.length; i++) {
			SearchConstraint constraint = constraints[i];
			if(constraint instanceof DummyGroupConstraint) {
				continue;
			}
			
			if(SearchManager.isGroupingOperator(constraint.getOperator())) {
				constraints[i] = new DummyGroupConstraint(constraint, getCache());
			}
		}
	}

	public void setConstraints(SearchConstraint[] constraints) {
		this.constraints = constraints;
		
		prepareGroupConstraints();
	}
	
	// RECURSIVE TREE OPERATIONS

	public void setTargetTree(TargetTree targetTree) {
		if(targetTree==null)
			throw new IllegalArgumentException("Invalid target-tree"); //$NON-NLS-1$
		
		this.targetTree = targetTree;
		if(next!=null) {
			next.setTargetTree(targetTree);
		}
		if(alternate!=null) {
			alternate.setTargetTree(targetTree);
		}
		if(exclusions!=null) {
			for(Matcher matcher : exclusions) {
				matcher.setTargetTree(targetTree);
			}
		}
	}

	public void setCache(GroupCache cache) {
		if(cache==null)
			throw new IllegalArgumentException("Invalid cache"); //$NON-NLS-1$
		
		this.cache = cache;
		prepareGroupConstraints();
		
		if(next!=null) {
			next.setCache(cache);
		}
		if(alternate!=null) {
			alternate.setCache(cache);
		}
		if(exclusions!=null) {
			for(Matcher matcher : exclusions) {
				matcher.setCache(cache);
			}
		}
	}

	public void setEntryBuilder(EntryBuilder entryBuilder) {
		if(entryBuilder==null)
			throw new IllegalArgumentException("Invalid entry-builder"); //$NON-NLS-1$
		
		this.entryBuilder = entryBuilder;
		if(next!=null) {
			next.setEntryBuilder(entryBuilder);
		}
		if(alternate!=null) {
			alternate.setEntryBuilder(entryBuilder);
		}
		if(exclusions!=null) {
			for(Matcher matcher : exclusions) {
				matcher.setEntryBuilder(entryBuilder);
			}
		}
	}

	public void setSearchMode(SearchMode searchMode) {
		if(searchMode==null)
			throw new IllegalArgumentException("Invalid search mode"); //$NON-NLS-1$
		
		this.searchMode = searchMode;
		exhaustive = searchMode.isExhaustive();
		
		if(next!=null) {
			next.setSearchMode(searchMode);
		}
		if(alternate!=null) {
			alternate.setSearchMode(searchMode);
		}
		if(exclusions!=null) {
			for(Matcher matcher : exclusions) {
				matcher.setSearchMode(searchMode);
			}
		}
	}
	
	public Matcher clone() {
		Matcher clone = null;
		
		try {
			clone = (Matcher) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new CorruptedStateException("Cannot clone cloneable super type: "+getClass(), e); //$NON-NLS-1$
		}
		
		return clone;
	}
	
	protected static abstract class IndexIterator implements Iterator<Integer> {

		public abstract void setMax(int max);

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			// no-op
		}
	}
	
	protected static class LTRIterator extends IndexIterator {
		
		private int max = -1;
		private int current = -1;
		
		public void setMax(int max) {
			this.max = max;
			
			current = -1;
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return current<max;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Integer next() {
			return ++current;
		}
	}
	
	protected static class RTLIterator extends IndexIterator {
		
		private int current = -1;
		
		public void setMax(int max) {
			current = max+1;
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return current>0;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Integer next() {
			return --current;
		}
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Matcher other) {
		return id-other.id;
	}
}