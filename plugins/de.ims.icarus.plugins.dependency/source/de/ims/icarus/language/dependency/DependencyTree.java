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
package de.ims.icarus.language.dependency;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.util.Exceptions;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyTree {

	protected Object data;
	
	protected DependencyTree parent;
	
	protected List<DependencyTree> children;
	
	protected int depth = -1;
	
	public DependencyTree() {
		this(null, null);
	}
	
	public DependencyTree(DependencyTree parent) {
		this(parent, null);
	}
	
	public DependencyTree(DependencyTree parent, Object data) {
		setParent(parent);
		setData(data);
	}
	
	public static DependencyTree[] createTree(DependencyData data) {
		if(data.isEmpty())
			return null;
		
		DependencyTree[] buffer = new DependencyTree[data.length()];
		
		for(short i=0; i<data.length(); i++)
			buffer[i] = new DependencyTree(null,
					new DependencyNodeData(data, i));

		ArrayList<DependencyTree> roots = new ArrayList<DependencyTree>();
		
		for(short i=0; i<data.length(); i++) {
			if(data.getHead(i)==LanguageUtils.DATA_HEAD_ROOT
					|| data.getHead(i)==LanguageUtils.DATA_UNDEFINED_VALUE)
				roots.add(buffer[i]);
			else
				buffer[data.getHead(i)].addChild(buffer[i]);
		}
		
		return roots.toArray(new DependencyTree[roots.size()]);
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * @return the parent
	 */
	public DependencyTree getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(DependencyTree parent) {
		this.parent = parent;
	}

	/**
	 * @return the children
	 */
	public List<DependencyTree> getChildren() {
		return children;
	}
	
	public int indexOf(DependencyTree child) {
		return children==null ? -1 : children.indexOf(child);
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(List<DependencyTree> children) {
		this.children = children;
	}
	
	public void addChild(DependencyTree child) {
		Exceptions.testNullArgument(child, "child"); //$NON-NLS-1$
		
		if(children==null)
			children = new Vector<DependencyTree>();
		
		child.setParent(this);
		
		children.add(child);
	}
	
	public void insertChild(DependencyTree child, int index) {
		Exceptions.testNullArgument(child, "child"); //$NON-NLS-1$
		
		if(children==null)
			children = new Vector<DependencyTree>();
		
		child.setParent(this);
		
		children.add(index, child);
	}
	
	public void removeChild(DependencyTree child) {
		Exceptions.testNullArgument(child, "child"); //$NON-NLS-1$
		
		if(children!=null) {
			children.remove(child);
			child.setParent(null);
		}
	}
	
	public void removeChildAt(int index) {		
		if(children!=null) {
			DependencyTree child = children.remove(index);
			child.setParent(null);
		}
	}
	
	public DependencyTree append(Object data) {
		DependencyTree child = new DependencyTree(this, data);
		addChild(child);
		
		return child;
	}
	
	public int getChildCount() {
		return children==null ? 0 : children.size();
	}
	
	public DependencyTree getChildAt(int index) {
		return children==null ? null : children.get(index);
	}
	
	public boolean isRoot() {
		return parent==null;
	}
	
	public boolean isLeaf() {
		return children==null || children.isEmpty();
	}
	
	public DependencyTree getRoot() {
		return parent==null ? this : parent.getRoot();
	}
	
	public void invalidateDepth() {
		if(depth!=-1) {
			depth = -1;
			if(children!=null && !children.isEmpty())
				for(DependencyTree child : children)
					child.invalidateDepth();
		}
	}
	
	public void validateDepth() {
		if(depth==-1) {
			depth = parent==null ? 1 : parent.getDepth()+1;
		}
	}
	
	public int getDepth() {
		validateDepth();
		return depth;
	}
	
	public int getHeight() {
		int height = 1;
		
		if(children!=null)
			for(DependencyTree child : children)
				height = Math.max(height, child.getHeight()+1);
		
		return height;
	}
	
	private static void collect(DependencyTree tree, List<DependencyTree> buffer, int depth) {
		if(tree.getDepth()>depth) {
			buffer.add(tree);
		}
		
		for(int i=0; i<tree.getChildCount(); i++)
			collect(tree.getChildAt(i), buffer, depth);
	}
	
	public static void compressTree(DependencyTree tree, MergeFunction merger, int maxDepth) {
		LinkedList<DependencyTree> shrinkQueue = new LinkedList<DependencyTree>();
		collect(tree, shrinkQueue, maxDepth);
		
		// no nodes featuring the desired depth -> return
		if(shrinkQueue.isEmpty())
			return;
		
		DependencyTree node;
		while(!shrinkQueue.isEmpty()) {
			node = shrinkQueue.pollLast();
			
			// if we have merged this node previously
			// we do not need to remove it from the queue
			// what would be a costly operation
			if(node.getData()==null)
				continue;
			
			// maybe the node's path got shrinked already
			if(node.getDepth()<=maxDepth)
				continue;
			
			// estimate the number of merges we need
			// to minimize overhead when searching for merges
			int requiredMerges = node.getDepth()-maxDepth;
			
			DependencyTree parent;
			while(!node.isRoot()) {
				parent = node.getParent();
				
				if(node.getData()!=null && 
						merger.mergeInto(node, parent)) {
					
					// invalidate node's depth and erase its data
					node.invalidateDepth();
					node.setData(null);
					
					// remove node from parent's child list
					// and shift all of node's children into
					// parent, starting with the node's previous
					// index (keeps the 'look' clean)
					int offset = parent.indexOf(node);
					parent.removeChildAt(offset);
					for(int i=0; i<node.getChildCount(); i++)
						parent.insertChild(node.getChildAt(i), offset+i);
					
					requiredMerges--;
				}
				
				if(requiredMerges==0)
					break;
				
				node = parent;
			}
		}
	}
	
	/**
	 * 
	 * @author Markus G�rtner
	 *
	 */
	public interface MergeFunction {
		
		/**
		 * Merges the content of object {@code source}
		 * into object {@code target} and returns {@code true}
		 * if and only if the operation succeeded;
		 * @param source
		 * @param target
		 * @return
		 */
		boolean mergeInto(DependencyTree source, DependencyTree target);
	}
}
