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
package de.ims.icarus.search_tools.tree;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import de.ims.icarus.search_tools.EdgeType;
import de.ims.icarus.search_tools.NodeType;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchMode;
import de.ims.icarus.search_tools.SearchNode;
import de.ims.icarus.search_tools.result.EntryBuilder;
import de.ims.icarus.search_tools.standard.GroupCache;
import de.ims.icarus.util.CorruptedStateException;


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
	protected Matcher next, previous;
	protected Matcher alternate;
	
	protected Matcher[] before;
	protected Matcher[] after;
	
	protected Matcher[] options;
	
	protected TargetTree targetTree;
	protected GroupCache cache;
	protected EntryBuilder entryBuilder;
	
	protected int allocation = -1;
	
	protected int height;
	protected int descendantCount;
	protected int childCount;
	protected NodeType type;
			
	protected boolean exhaustive = false;
	protected SearchMode searchMode = SearchMode.MATCHES;
	protected boolean leftToRight = true;
	
	protected IndexIterator indexIterator = new LTRIterator();
	
	public Matcher(SearchNode node, SearchEdge edge) {
		if(node==null)
			throw new IllegalArgumentException("Invalid node"); //$NON-NLS-1$
		
		this.node = node;
		this.edge = edge;
		
		// Refresh childCount
		if(node!=null) {
			for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
				EdgeType type = node.getOutgoingEdgeAt(i).getEdgeType();
				if(type==EdgeType.PRECEDENCE || type==EdgeType.LINK) {
					continue;
				}
				
				childCount++;
			}
			
			type = node.getNodeType();
		}
	}
	
	public boolean matches() {		
		int parentAllocation = parent.getAllocation();
		targetTree.viewNode(parentAllocation);
		indexIterator.setMax(targetTree.getEdgeCount()-1);
		
		int minIndex = getMinIndex();
		int maxIndex = getMaxIndex();
		
		boolean matched = false;
		
		if(minIndex<=maxIndex) {
			while(indexIterator.hasNext()) {
				targetTree.viewNode(parentAllocation);
				targetTree.viewChild(indexIterator.next());
				
				if(entryBuilder.getIndex()==49 && targetTree.getNodeIndex()==15) {
					System.out.println();
				}

				// Check for precedence constraints
				if(targetTree.getNodeIndex()<minIndex
						|| targetTree.getNodeIndex()>maxIndex) {
					continue;
				}
				
				// Honor locked nodes that are allocated to other matchers!
				if(targetTree.isNodeLocked()) {
					continue;
				}

				// Check for type constraints
				if(!matchesType()) {
					continue;
				}
				
				// Check for structural constraints 
				if(targetTree.getDescendantCount()<descendantCount
						|| targetTree.getHeight()<height) {
					continue;
				}
				
				// Check for required number of children
				if(targetTree.getEdgeCount()<childCount) {
					continue;
				}
				
				// Check if the current node is a potential match
				if(!matchesConstraints()) {
					continue;
				}

				// Lock allocation
				allocate();

				// Search for child matchers that serve as exclusions			
				if(!matchesExclusions()) {
					// Delegate further search to the next matcher
					// or otherwise commit current match
					matched |= matchesNext();
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
		}
		
		// Return scope to parent node
		targetTree.viewNode(parentAllocation);
		
		// If unsuccessful and part of a disjunction let the 
		// alternate matcher have a try.
		if((!matched || exhaustive) && alternate!=null) {
			matched |= alternate.matches();
		}
		
		return matched;
	}
	
	/**
	 * Returns {@code true} if at least on of the
	 * {@code Matcher} instances registered as exclusions
	 * returns a successful match.
	 */
	protected boolean matchesExclusions() {
		if(exclusions!=null) {
			for(Matcher matcher : exclusions) {
				if(matcher.matches()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	protected boolean matchesType() {
		switch (type) {
		case LEAF:
			return targetTree.getEdgeCount()==0;
		case ROOT:
			return targetTree.isRoot();
		case INTERMEDIATE:
			return targetTree.getEdgeCount()>0;

		default:
			return true;
		}
	}
	
	protected boolean matchesNext() {
		if(next!=null) {
			return next.matches();
		} else if(!exclusionMember) {
			// ONLY cache here if this matcher is not a
			// member of a sub-tree that serves as exclusion
			cacheHits();
			
			// Commit if every hit should be reported independently
			if(searchMode==SearchMode.INDEPENDENT_HITS) {
				commit();
			}
			
			//return true;
		}
		
		// return false
		return true;
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
		int min = -1;
		
		if(before!=null) {
			for(Matcher matcher : before) {
				int alloc = matcher.getAllocation();
				if(alloc!=-1) {
					min = Math.max(min, alloc);
				}
			}
		}
		min++;
		
		return min;
	}

	/**
	 * Finds the maximum allowed index for this matcher
	 * considering the allocation of all previous matchers.
	 */
	public int getMaxIndex() {
		int max = targetTree.size();
		
		if(after!=null) {
			for(Matcher matcher : after) {
				int alloc = matcher.getAllocation();
				if(alloc!=-1) {
					max = Math.min(max, alloc);
				}
			}
		}
		max--;
		
		return max;
	}
	
	public int getChildCount() {
		return childCount;
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
		if(options!=null) {
			for(Matcher option : options) {
				option.setLeftToRight(leftToRight);
			}
		}
	}

	public void setId(int id) {
		this.id = id;
	}

	public Matcher getPrevious() {
		return previous;
	}

	public void setPrevious(Matcher previous) {
		this.previous = previous;
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

	public Matcher[] getOptions() {
		return options;
	}

	public void setOptions(Matcher[] options) {
		this.options = options;
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
		
		Arrays.sort(constraints, CONSTRAINT_PRIORITY_SORTER);
		
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
	
	public static Comparator<SearchConstraint> CONSTRAINT_PRIORITY_SORTER = new Comparator<SearchConstraint>() {

		@Override
		public int compare(SearchConstraint c1, SearchConstraint c2) {
			boolean isGrp1 = SearchManager.isGroupingOperator(c1.getOperator());
			boolean isGrp2 = SearchManager.isGroupingOperator(c2.getOperator());
			
			return isGrp1==isGrp2 ? 0 : isGrp1 ? 1 : -1;
		}
		
	};

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
		if(options!=null) {
			for(Matcher option : options) {
				option.setTargetTree(targetTree);
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
		if(options!=null) {
			for(Matcher option : options) {
				option.setCache(cache);
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
		if(options!=null) {
			for(Matcher option : options) {
				option.setEntryBuilder(entryBuilder);
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
		if(options!=null) {
			for(Matcher option : options) {
				option.setSearchMode(searchMode);
			}
		}
	}
	
	protected void innerClose() {
		// for subclasses
	}

	public void close() {
		if(next!=null) {
			next.close();
		}
		if(alternate!=null) {
			alternate.close();
		}
		if(exclusions!=null) {
			for(Matcher matcher : exclusions) {
				matcher.close();
			}
		}
		if(options!=null) {
			for(Matcher option : options) {
				option.close();
			}
		}
	}
	
	public void link() {
		if(next!=null) {
			next.setPrevious(this);
			next.link();
		}
		if(alternate!=null) {
			alternate.link();
		}
		if(exclusions!=null) {
			for(Matcher matcher : exclusions) {
				matcher.link();
			}
		}
		if(options!=null) {
			for(Matcher option : options) {
				option.link();
			}
		}
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Matcher other) {
		return id-other.id;
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
		
		public abstract int getRange();

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			// no-op
		}
		
		public abstract IndexIterator clone();
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

		/**
		 * @see de.ims.icarus.search_tools.tree.Matcher.IndexIterator#clone()
		 */
		@Override
		public IndexIterator clone() {
			return new LTRIterator();
		}

		/**
		 * @see de.ims.icarus.search_tools.tree.Matcher.IndexIterator#getRange()
		 */
		@Override
		public int getRange() {
			return Math.max(max, 0);
		}
	}
	
	protected static class RTLIterator extends IndexIterator {
		
		private int current = -1;
		private int max = -1;
		
		public void setMax(int max) {
			this.max = max;
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

		/**
		 * @see de.ims.icarus.search_tools.tree.Matcher.IndexIterator#clone()
		 */
		@Override
		public IndexIterator clone() {
			return new RTLIterator();
		}

		/**
		 * @see de.ims.icarus.search_tools.tree.Matcher.IndexIterator#getRange()
		 */
		@Override
		public int getRange() {
			return Math.max(max, 0);
		}
	}
}