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
package de.ims.icarus.model.standard.elements;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.ims.icarus.model.api.Container;
import de.ims.icarus.model.api.CorpusMember;
import de.ims.icarus.model.api.Edge;
import de.ims.icarus.model.api.Markable;
import de.ims.icarus.model.api.Structure;
import de.ims.icarus.model.api.StructureType;
import de.ims.icarus.model.api.edit.EditOperation;
import de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.model.api.manifest.StructureManifest;
import de.ims.icarus.model.util.CorpusMemberUtils;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.collections.LookupList;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;
import de.ims.icarus.util.mem.Primitive;
import de.ims.icarus.util.mem.Reference;
import de.ims.icarus.util.mem.ReferenceType;

/**
 * Implements an empty structure.
 * <p>
 * This implementation supports adding and removing of {@code Markable} objects
 * but throws an {@code UnsupportedOperationException} for any attempt to
 * add or remove an {@code Edge}.
 * <p>
 * As defined in the {@link Container} specification an optional <i>boundary container</i>
 * can be set at construction time. When a non-null container is set as boundary then all
 * {@code Markable} objects to be added will first be checked for possible boundary violation.
 * Note that no boundary checks are performed for <i>virtual</i> markables
 * (see {@link CorpusUtils#isVirtual(Markable)}).
 * <p>
 * To reduce memory footprint an additional flag can be set to use the members of the <i>boundary
 * container</i> as base markables for this structure, obsoleting the need to maintain a separate
 * list. If the structure needs to define additional markables (as is the case for phrase structure trees)
 * it has to set the {@code augment} flag at construction time. This will enable the internal buffer
 * for new markables. Note that this implementation will fail for every attempt to add or remove markables
 * when augmentation is disabled, since it is not able to modify the underlying <i>boundary container</i>!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class DefaultStructure extends ListContainer implements Structure {

	// Internal edge storage, will be created lazily, but never destroyed!
	@Reference
	private LookupList<Edge> edges;
	// Internal graph storage, will be created lazily and destroyed each
	// time the structure gets invalidated
	@Reference(ReferenceType.DOWNLINK)
	private Graph graph;
	@Primitive
	private boolean boundaryAsBase;
	@Primitive
	private boolean augment;
	@Link(type=ReferenceType.DOWNLINK)
	private Markable root;

	private LookupList<Edge> edges() {
		if(edges==null) {
			synchronized (this) {
				if(edges==null) {
					edges = new LookupList<>();
				}
			}
		}

		return edges;
	}

	// Ensures a valid graph buffer and fills it with the edges in
	// this structure if it has to be freshly created.
	private Graph graph() {
		if(graph==null) {
			synchronized (this) {
				if(graph==null) {
					graph = new Graph();
					graph.rebuild(this);
				}
			}
		}

		return graph;
	}

	public void reset(boolean augment, boolean boundaryAsBase) {
		if(!augment && !boundaryAsBase)
			throw new IllegalArgumentException("Must declare either boundary as base or activate augmentation"); //$NON-NLS-1$
		if(boundaryAsBase && getBoundaryContainer()==null)
			throw new IllegalArgumentException("Cannot declare null boundary as base"); //$NON-NLS-1$

		this.boundaryAsBase = boundaryAsBase;
		this.augment = augment;

		root = createRoot();
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.ListContainer#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();

		boundaryAsBase = augment = false;

		if(edges!=null) {
			edges.clear();
		}

		graph = null;
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.AbstractContainer#revive()
	 */
	@Override
	public boolean revive() {
		if(!super.revive()) {
			return false;
		}

		if(root==null) {
			root = createRoot();
		}

		return true;
	}

	public void appendAllEdges(List<? extends Edge> edges) {
		edges().addAll(edges);
		invalidate();
	}

	public void appendAllEdges0(Edge...edges) {
		edges().addAll(edges);
		invalidate();
	}

	public void appendEdge(Edge edge) {
		edges().add(edge);

		invalidate();
	}

	protected void checkStructureAction(EditOperation operation) {
		StructureType type = getStructureType();
		if(!type.supportsOperation(operation))
			throw new UnsupportedOperationException(operation.name()+" not supported on structure-type "+type.name()); //$NON-NLS-1$
	}

	protected boolean isRandomAccessEdgeIndex(int index) {
		return index>0 && index<getEdgeCount()-1;
	}

	/**
	 * @see de.ims.icarus.model.api.Structure#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return getManifest().getStructureType();
	}

	/**
	 * @see de.ims.icarus.model.api.standard.container.AbstractNestedContainer#getManifest()
	 */
	@Override
	public StructureManifest getManifest() {
		return (StructureManifest) super.getManifest();
	}

	/**
	 * @see de.ims.icarus.model.api.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		int size = 0;
		if(boundaryAsBase) {
			size += getBoundaryContainer().getMarkableCount();
		}
		if(augment) {
			size += super.getMarkableCount();
		}
		return size;
	}

	/**
	 * @see de.ims.icarus.model.api.Container#getMarkableAt(int)
	 */
	@Override
	public Markable getMarkableAt(int index) {
		if(boundaryAsBase) {
			Container boundary = getBoundaryContainer();
			if(index<boundary.getMarkableCount()) {
				return boundary.getMarkableAt(index);
			} else {
				index -= boundary.getMarkableCount();
			}
		}
		if(augment) {
			return super.getMarkableAt(index);
		}

		throw new IndexOutOfBoundsException();
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.standard.container.AbstractContainer#indexOfMarkable(de.ims.icarus.model.api.Markable)
	 */
	@Override
	public int indexOfMarkable(Markable markable) {
		int result = -1;
		int offset = 0;

		if(boundaryAsBase) {
			offset = getBoundaryContainer().getMarkableCount();
			result = getBoundaryContainer().indexOfMarkable(markable);
		}
		if(result==-1 && augment) {
			result = super.indexOfMarkable(markable);
		}

		if(result!=-1) {
			result += offset;
		}

		return result;
	}

//	/**
//	 * Checks whether or not the given markable is allowed to be added
//	 * to this structure. If either the <i>boundary-container</i> is {@code null}
//	 * or the given {@code markable} is virtual as determined by
//	 * {@link CorpusUtils#isVirtual(Markable)} then this method does nothing.
//	 * Otherwise it compares the begin and end offset of the markable with those
//	 * of the <i>boundary-container</i>. If one of those indices lays outside the
//	 * boundary it will throw an {@code IllegalArgumentException}.
//	 * <p>
//	 * Subclasses are encouraged to implement their own verification logic as
//	 * they see fit.
//	 *
//	 * @param markable
//	 * @throws NullPointerException if the {@code markable} argument is {@code null}
//	 * @throws IllegalArgumentException if the {@code markable} violates the boundary
//	 */
//	@Override
//	protected void checkMarkable(Markable markable) {
//		if (markable == null)
//			throw new NullPointerException("Invalid markable");  //$NON-NLS-1$
//
//		if(getBoundaryContainer()==null || CorpusUtils.isVirtual(markable)) {
//			return;
//		}
//
//		if(markable.getBeginOffset()<getBoundaryContainer().getBeginOffset()
//				|| markable.getEndOffset()>getBoundaryContainer().getEndOffset())
//			throw new IllegalArgumentException("Markable not within boundary: "+markable); //$NON-NLS-1$
//	}

	/**
	 * Creates the single {@code root} node for this structure. Note that
	 * each structure must declare its own root object. Otherwise it would be
	 * impossible for annotations to be assigned to the correct root node!
	 *
	 * @return
	 */
	protected Markable createRoot() {
		return new RootMarkable(this);
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#getRoot()
	 */
	@Override
	public Markable getRoot() {
		return root;
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#isRoot(de.ims.icarus.model.api.Markable)
	 */
	@Override
	public boolean isRoot(Markable node) {
		if (node == null)
			throw new NullPointerException("Invalid node"); //$NON-NLS-1$

		int countIn = graph().edgeCount(node, true);
		return countIn==1 && graph().edgeAt(node, true, 0).getSource()==root;
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#getEdgeCount()
	 */
	@Override
	public int getEdgeCount() {
		return edges().size();
	}

	/**
	 * @see de.ims.icarus.model.api.Structure#getEdgeAt(de.ims.icarus.model.api.Markable, int, boolean)
	 */
	@Override
	public Edge getEdgeAt(Markable node, int index, boolean isSource) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#getEdgeAt(int)
	 */
	@Override
	public Edge getEdgeAt(int index) {
		return edges().get(index);
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#indexOfEdge(de.ims.icarus.model.api.Edge)
	 */
	@Override
	public int indexOfEdge(Edge edge) {
		return edges().indexOf(edge);
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#containsEdge(de.ims.icarus.model.api.Edge)
	 */
	@Override
	public boolean containsEdge(Edge edge) {
		return edges().contains(edge);
	}

	@Override
	public int getEdgeCount(Markable node) {
		return graph().edgeCount(node);
	}

	/**
	 * @see de.ims.icarus.model.api.Structure#getEdgeCount(de.ims.icarus.model.api.Markable, boolean)
	 */
	@Override
	public int getEdgeCount(Markable node, boolean isSource) {
		return graph().edgeCount(node, !isSource);
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#removeAllEdges()
	 */
	@Override
	public void removeAllEdges() {
		checkStructureAction(EditOperation.CLEAR);
		execute(new ClearEdgesChange());
	}

	/**
	 * Verifies that the given {@code edge} is allowed to be added to this structure.
	 * The default implementation only checks the source and target of the edge using the
	 * {@link #checkMarkable(Markable)} method. Subclasses should override this method to
	 * perform structure type specific checks to ensure that certain constraints are not
	 * violated (like having at most one incoming edge in the case of a tree structure).
	 * When overriding a subclass should still call this method via {@code super#checkEdge(Edge)}
	 * to make sure that the terminals of the edge are valid.
	 *
	 * @param edge The {@code edge} to verify
	 */
	protected void checkEdge(Edge edge) {
		if (edge == null)
			throw new NullPointerException("Invalid edge"); //$NON-NLS-1$

		Markable source = edge.getSource();
		Markable target = edge.getTarget();

		checkMarkable(source);
		checkMarkable(target);

		// ROOT not allowed as target
		if(edge.getTarget()==root)
			throw new IllegalArgumentException("Cannot add edge that points to virtual ROOT node"); //$NON-NLS-1$

		StructureManifest manifest = getManifest();
		StructureType type = manifest.getStructureType();

		if(type.isDirected() && !edge.isDirected())
			throw new IllegalArgumentException("Structure type "+type+" requires directed edges"); //$NON-NLS-1$ //$NON-NLS-2$

		Graph graph = graph();

		// Ensure that the multi-root constraint remains intact
		if(source==root && !manifest.isMultiRootAllowed() && graph.edgeCount(root)>0)
			throw new IllegalArgumentException("Unsupported attempt to install multi-root structure"); //$NON-NLS-1$

		checkEdgeConstraints(source, target, 1, 1);
	}

	protected void checkEdgeConstraints(Markable source, Markable target, int deltaSource, int deltaTarget) {

		// Fetch edge counts as they would be AFTER the desired operation
		int countIn = graph.edgeCount(target, true)+deltaTarget;
		int countOut = graph.edgeCount(source, false)+deltaSource;
		int countSource = graph.edgeCount(source)+deltaSource;
		int countTarget = graph.edgeCount(target)+deltaTarget;

		StructureType type = getStructureType();

		// Only check source when it is not the ROOT node
		if(source!=root) {
			if(type.isLegalEdgeCount(countSource))
				throw new IllegalArgumentException(CorpusMemberUtils.insufficientEdgesMessage(
						"Source node", source, type.getMinEdgeCount(), countSource)); //$NON-NLS-1$
			if(type.isLegalOutgoingEdgeCount(countOut))
				throw new IllegalArgumentException(CorpusMemberUtils.edgesOverflowMessage(
						"Outgoing edges", target, type.getOutgoingEdgeLimit(), countTarget)); //$NON-NLS-1$
		}

		// In any case check target node
		if(type.isLegalEdgeCount(countTarget))
			throw new IllegalArgumentException(CorpusMemberUtils.insufficientEdgesMessage(
					"Target node", target, type.getMinEdgeCount(), countTarget)); //$NON-NLS-1$
		if(type.isLegalIncomingEdgeCount(countIn))
			throw new IllegalArgumentException(CorpusMemberUtils.edgesOverflowMessage(
					"Incoming edges", target, type.getIncomingEdgeLimit(), countIn)); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.model.api.Structure#addEdge(de.ims.icarus.model.api.Edge)
	 */
	@Override
	public Edge addEdge(Edge edge) {
		return addEdge(edge, getEdgeCount()-1);
	}

	/**
	 * @see de.ims.icarus.model.api.Structure#addEdge(de.ims.icarus.model.api.Edge, int)
	 */
	@Override
	public Edge addEdge(Edge edge, int index) {
		checkEdge(edge);

		EditOperation operation = isRandomAccessEdgeIndex(index) ?
				EditOperation.ADD_RANDOM : EditOperation.ADD;
		checkStructureAction(operation);

		execute(new EdgeChange(index, true, edge));

		return edge;
	}

	/**
	 * Creates a new {@code Edge} that can be added to this structure.
	 * The default implementation generates a simple directed edge.
	 * Note that no verification is performed in this method, since
	 * an edge should only be checked once an attempt is made to actually
	 * add it to the structure!
	 *
	 * @param source
	 * @param target
	 * @return
	 */
	protected Edge createEdge(Markable source, Markable target) {
		if (source == null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$
		if (target == null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$
		if (target == root)
			throw new IllegalArgumentException("Illegal target - ROOT node can only be used as source node"); //$NON-NLS-1$

		return new DefaultEdge(this, source, target);
	}

	/**
	 * Creates a new {@link DefaultEdge} object using the given
	 * {@code isSource} and {@code target} markables and attempts
	 * to add it using {@link #addEdge(Edge)}
	 *
	 * @see #addEdge(Edge)
	 */
	@Override
	public Edge addEdge(Markable source, Markable target) {
		return addEdge(createEdge(source, target));
	}

	/**
	 * Creates a new {@link DefaultEdge} object using the given
	 * {@code isSource} and {@code target} markables and attempts
	 * to add it using {@link #addEdge(Edge)}
	 *
	 * @see #addEdge(Edge, int)
	 */
	@Override
	public Edge addEdge(Markable source, Markable target, int index) {
		return addEdge(createEdge(source, target), index);
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#removeEdge(int)
	 */
	@Override
	public Edge removeEdge(int index) {
		Edge edge = getEdgeAt(index);

		EditOperation operation = isRandomAccessEdgeIndex(index) ?
				EditOperation.REMOVE_RANDOM : EditOperation.REMOVE;
		checkStructureAction(operation);

		checkEdgeConstraints(edge.getSource(), edge.getTarget(), -1, -1);

		execute(new EdgeChange(index, false, edge));

		return edge;
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#removeEdge(de.ims.icarus.model.api.Edge)
	 */
	@Override
	public Edge removeEdge(Edge edge) {
		int index = indexOfEdge(edge);

		if(index==-1)
			throw new IllegalArgumentException("Unknown edge: "+edge); //$NON-NLS-1$

		EditOperation operation = isRandomAccessEdgeIndex(index) ?
				EditOperation.REMOVE_RANDOM : EditOperation.REMOVE;
		checkStructureAction(operation);

		execute(new EdgeChange(index, false, edge));

		return edge;
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#moveEdge(int, int)
	 */
	@Override
	public void moveEdge(int index0, int index1) {
		checkStructureAction(EditOperation.MOVE);
		execute(new MoveEdgeChange(index0, index1));
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#moveEdge(de.ims.icarus.model.api.Edge, int)
	 */
	@Override
	public void moveEdge(Edge edge, int index) {
		int index0 = indexOfEdge(edge);

		checkStructureAction(EditOperation.MOVE);
		execute(new MoveEdgeChange(index0, index));
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#setTerminal(de.ims.icarus.model.api.Edge, de.ims.icarus.model.api.Markable, boolean)
	 */
	@Override
	public void setTerminal(Edge edge, Markable markable, boolean isSource) {
		if (edge == null)
			throw new NullPointerException("Invalid edge"); //$NON-NLS-1$
		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$

		if (!isSource && markable==root)
			throw new IllegalArgumentException("Cannot change target terminal to ROOT node"); //$NON-NLS-1$

		checkStructureAction(EditOperation.LINK);

		// Run two checks, one for the removal of the edge from one node
		// and another for adding it to the next node
		if(isSource) {
			checkEdgeConstraints(edge.getSource(), edge.getTarget(), -1, 0);
			checkEdgeConstraints(markable, edge.getTarget(), 1, 0);
		} else {
			checkEdgeConstraints(edge.getSource(), edge.getTarget(), 0, -1);
			checkEdgeConstraints(edge.getSource(), markable, 0, 1);
		}

		execute(new TerminalChange(edge, isSource, markable));
	}

	/**
	 * @see de.ims.icarus.model.api.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		if(!augment)
			throw new UnsupportedOperationException();

		// Make sure all augmentation nodes are "edge free"
		for(int i=0; i<super.getMarkableCount(); i++) {
			Markable markable = super.getMarkableAt(i);
			if(getEdgeCount(markable)!=0)
				throw new IllegalStateException("Cannot clear markables while some of them are still tied to edges"); //$NON-NLS-1$
		}

		super.removeAllMarkables();
	}

	/**
	 * @see de.ims.icarus.model.api.Container#addMarkable(int, de.ims.icarus.model.api.Markable)
	 */
	@Override
	public void addMarkable(int index, Markable markable) {
		if(!augment)
			throw new UnsupportedOperationException();

		if(boundaryAsBase) {
			index -= getBoundaryContainer().getMarkableCount();
		}

		super.addMarkable(index, markable);
	}

	/**
	 * @see de.ims.icarus.model.api.Container#removeMarkable(int)
	 */
	@Override
	public Markable removeMarkable(int index) {
		if(!augment)
			throw new UnsupportedOperationException();

		if(boundaryAsBase) {
			index -= getBoundaryContainer().getMarkableCount();
		}

		// Make sure the markable in question is not tied by an edge!
		Markable markable = super.getMarkableAt(index);
		if(getEdgeCount(markable)!=0)
			throw new IllegalStateException("Cannot remove markable "+markable+" while it is still required for edges"); //$NON-NLS-1$ //$NON-NLS-2$

		return super.removeMarkable(index);
	}

	/**
	 * @see de.ims.icarus.model.api.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		// We cannot move markables belonging to the boundary container!
		if(!augment)
			throw new UnsupportedOperationException();

		if(boundaryAsBase) {
			index0 -= getBoundaryContainer().getMarkableCount();
			index1 -= getBoundaryContainer().getMarkableCount();
		}

		super.moveMarkable(index0, index1);
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Markable> iterator() {
		return new ComboItr();
	}

	private Iterator<Markable> augIterator() {
		return super.iterator();
	}

	/**
	 * Refreshes the internal {@link GraphStruct} object according to the
	 * change. Note that subclasses should always call this method via
	 * {@code super#edgeAdded(Edge, int)} after verifying the given change
	 * to make sure that changes are correctly reflected in the internal
	 * graph structure!
	 *
	 * @see de.ims.icarus.model.api.standard.structure.AbstractRootedStructure#edgeAdded(de.ims.icarus.model.api.Edge, int)
	 */
	protected void edgeAdded(Edge edge, int index) {
		graph().add(edge.getSource(), edge, false);
		graph().add(edge.getTarget(), edge, true);
	}

	/**
	 * Refreshes the internal {@link GraphStruct} object according to the
	 * change. Note that subclasses should always call this method via
	 * {@code super#edgeAdded(Edge, int)} after verifying the given change
	 * to make sure that changes are correctly reflected in the internal
	 * graph structure!
	 *
	 * @see de.ims.icarus.model.api.standard.structure.AbstractRootedStructure#edgeRemoved(de.ims.icarus.model.api.Edge, int)
	 */
	protected void edgeRemoved(Edge edge, int index) {
		graph().remove(edge.getSource(), edge, false);
		graph().remove(edge.getTarget(), edge, true);
	}

	protected void edgeMoved(Edge edge, int from, int to) {
		// for subclasses
	}

	/**
	 * Refreshes the internal {@link GraphStruct} object according to the
	 * change. Note that subclasses should always call this method via
	 * {@code super#edgeAdded(Edge, int)} after verifying the given change
	 * to make sure that changes are correctly reflected in the internal
	 * graph structure!
	 *
	 * @see de.ims.icarus.model.api.standard.structure.AbstractRootedStructure#terminalChanged(de.ims.icarus.model.api.Edge, boolean, de.ims.icarus.model.api.Markable, de.ims.icarus.model.api.Markable)
	 */
	protected void terminalChanged(Edge edge, boolean isSource,
			Markable oldTerminal, Markable newTerminal) {

		Graph graph = graph();
		graph.remove(oldTerminal, edge, !isSource);
		graph.add(newTerminal, edge, !isSource);
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.ListContainer#invalidate()
	 */
	@Override
	protected void invalidate() {
		super.invalidate();

		graph = null;
	}

	/**
	 * @see de.ims.icarus.model.standard.elements.DefaultStructure.structure.EmptyStructure#getParent(de.ims.icarus.model.api.Markable)
	 */
	@Override
	public Markable getParent(Markable node) {
		if (node == null)
			throw new NullPointerException("Invalid node"); //$NON-NLS-1$
		if(node==root)
			throw new IllegalArgumentException("Cannot fetch parent of ROOT node"); //$NON-NLS-1$

		// TODO
		// Quick way of getting the parent, needs to be adjusted to report
		// case of more than one incoming edge!
		return graph().edgeAt(node, true, 0);
	}

	private class EdgeChange implements AtomicChange {

		private final Edge edge;
		private final int index;
		private final boolean add;
		private int expectedSize;

		public EdgeChange(int index, boolean add, Edge edge) {
			this.index = index;
			this.add = add;
			this.edge = edge;
			expectedSize = edges.size();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(edges().size()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Removing failed", expectedSize, edges().size())); //$NON-NLS-1$

			if(add) {
				edgeAdded(edge, index);

				edges().add(index, edge);
				expectedSize++;
			} else {
				if(edges().get(index)!=edge)
					throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
							"Removing failed", edge, edges().get(index))); //$NON-NLS-1$

				edgeRemoved(edges().get(index), index);

				edges().remove(index);
				expectedSize--;
			}
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return DefaultStructure.this;
		}

	}

	private class MoveEdgeChange implements AtomicChange {

		private int indexFrom, indexTo;
		private Edge edge;
		private int expectedSize;

		public MoveEdgeChange(int indexFrom, int indexTo) {
			this.indexFrom = indexFrom;
			this.indexTo = indexTo;

			edge = edges().get(indexFrom);

			expectedSize = edges().size();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			LookupList<Edge> edges = edges();

			if(edges.size()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Removing failed", expectedSize, edges.size())); //$NON-NLS-1$

			if(edges.get(indexFrom)!=edge)
				throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
						"Moving failed (origin)", edge, edges.get(indexFrom))); //$NON-NLS-1$

			edgeMoved(edge, indexFrom, indexTo);

			edges.remove(indexFrom);
			edges.add(indexTo, edge);

			int tmp = indexFrom;
			indexFrom = indexTo;
			indexTo = tmp;
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return DefaultStructure.this;
		}

	}

	private class ClearEdgesChange implements AtomicChange {

		private Object[] items;
		private int expectedSize = edges().size();

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			LookupList<Edge> edges = edges();

			if(edges.size()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Clear failed", expectedSize, edges.size())); //$NON-NLS-1$

			if(items==null) {
				items = edges.toArray();
				edges.clear();
				expectedSize = 0;
			} else {
				edges.set(items);
				expectedSize = items.length;
				items = null;
			}

			invalidate();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return DefaultStructure.this;
		}

	}

	private class TerminalChange implements AtomicChange {

		private final Edge edge;
		private final boolean isSource;
		private Markable terminal;
		private Markable expected;

		public TerminalChange(Edge edge, boolean isSource, Markable terminal) {
			if (edge == null)
				throw new NullPointerException("Invalid edge"); //$NON-NLS-1$
			if (terminal == null)
				throw new NullPointerException("Invalid terminal");  //$NON-NLS-1$

			this.edge = edge;
			this.isSource = isSource;
			this.terminal = terminal;

			expected = isSource ? edge.getSource() : edge.getTarget();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			Markable oldTerminal = isSource ? edge.getSource() : edge.getTarget();
			if(expected!=oldTerminal)
				throw new CorruptedStateException(CorpusMemberUtils.mismatchMessage(
						"Terminal change failed", expected, oldTerminal)); //$NON-NLS-1$

			terminalChanged(edge, isSource, oldTerminal, terminal);

			expected = terminal;

			if(isSource) {
				edge.setSource(terminal);
			} else {
				edge.setTarget(terminal);
			}

			terminal = oldTerminal;
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return edge;
		}

	}

	private class ComboItr implements Iterator<Markable> {

		private final int expectedSize = getMarkableCount();
		private final Iterator<Markable> baseItr = boundaryAsBase ? getBoundaryContainer().iterator() : null;
		private final Iterator<Markable> augmentItr = augment ? augIterator() : null;

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			checkModification();

			return (baseItr!=null && baseItr.hasNext())
					|| (augmentItr!=null && augmentItr.hasNext());
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Markable next() {
			checkModification();

			if(baseItr!=null && baseItr.hasNext()) {
				return baseItr.next();
			} else if(augmentItr!=null && augmentItr.hasNext()) {
				return augmentItr.next();
			} else
				throw new NoSuchElementException();
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported"); //$NON-NLS-1$
		}

		private void checkModification() {
			if(getMarkableCount()!=expectedSize)
				throw new ConcurrentModificationException();
		}
	}
}
