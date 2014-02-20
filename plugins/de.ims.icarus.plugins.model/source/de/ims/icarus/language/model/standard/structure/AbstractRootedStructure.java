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
package de.ims.icarus.language.model.standard.structure;

import java.util.List;

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.CorpusMember;
import de.ims.icarus.language.model.Edge;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.language.model.standard.CorpusMemberUtils;
import de.ims.icarus.language.model.standard.LookupList;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;
import de.ims.icarus.util.mem.ReferenceType;

/**
 * Implements a rooted structure of arbitrary type (chain, tree or graph).
 * <p>
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public abstract class AbstractRootedStructure extends EmptyStructure {

	@Link
	private final LookupList<Edge> edges = new LookupList<>();

	@Link(type=ReferenceType.DOWNLINK)
	private final Markable root;

	public AbstractRootedStructure(long id, Container parent) {
		super(id, parent);

		root = createRoot();
	}

	public AbstractRootedStructure(long id, Container parent, Container boundary,
			boolean augment, boolean boundaryAsBase) {
		super(id, parent, boundary, augment, boundaryAsBase);

		root = createRoot();
	}

	protected void addAllEdges0(List<? extends Edge> edges) {
		this.edges.addAll(edges);

		invalidate();
	}

	protected void addAllEdges0(Edge...edges) {
		this.edges.addAll(edges);

		invalidate();
	}

	protected void addEdge0(Edge edge) {
		edges.add(edge);

		invalidate();
	}

	/**
	 * Creates the single {@code root} node for this structure. Note that
	 * each structure must declare its own root object. Otherwise it would be
	 * impossible for annotations to be assigned to the correct root node!
	 *
	 * @return
	 */
	protected Markable createRoot() {
		return new Root(getCorpus().getGlobalIdDomain().nextId(), this);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#getRoot()
	 */
	@Override
	public Markable getRoot() {
		return root;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#isRoot(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public boolean isRoot(Markable node) {
		if (node == null)
			throw new NullPointerException("Invalid node"); //$NON-NLS-1$

		if(node==root) {
			return true;
		} else if(!containsMarkable(node))
			throw new IllegalArgumentException("Unknown node: "+node); //$NON-NLS-1$

		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#isMultiRoot()
	 */
	@Override
	public boolean isMultiRoot() {
		return true;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#getEdgeCount()
	 */
	@Override
	public int getEdgeCount() {
		return edges.size();
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#getEdgeAt(int)
	 */
	@Override
	public Edge getEdgeAt(int index) {
		return edges.get(index);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#indexOfEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public int indexOfEdge(Edge edge) {
		return edges.indexOf(edge);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#containsEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public boolean containsEdge(Edge edge) {
		return edges.contains(edge);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#removeAllEdges()
	 */
	@Override
	public void removeAllEdges() {
		execute(new ClearEdgesChange());
	}

	/**
	 * Verifies that the given {@code edge} is allowed to be added to this structure.
	 * THe default implementation only checks the source and target of the edge using the
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

		checkMarkable(edge.getSource());
		checkMarkable(edge.getTarget());
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#addEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public Edge addEdge(Edge edge) {
		return addEdge(edge, getEdgeCount()-1);
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#addEdge(de.ims.icarus.language.model.Edge, int)
	 */
	@Override
	public Edge addEdge(Edge edge, int index) {
		checkEdge(edge);

		execute(new EdgeChange(index, edge.getId(), edge));

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
		long id = getCorpus().getGlobalIdDomain().nextId();
		return new DefaultEdge(id, this, source, target);
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
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#removeEdge(int)
	 */
	@Override
	public Edge removeEdge(int index) {
		Edge edge = getEdgeAt(index);

		execute(new EdgeChange(index, edge.getId(), null));

		return edge;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#removeEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public Edge removeEdge(Edge edge) {
		int index = indexOfEdge(edge);

		execute(new EdgeChange(index, edge.getId(), null));

		return edge;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#moveEdge(int, int)
	 */
	@Override
	public void moveEdge(int index0, int index1) {
		execute(new MoveEdgeChange(index0, index1));
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#moveEdge(de.ims.icarus.language.model.Edge, int)
	 */
	@Override
	public void moveEdge(Edge edge, int index) {
		int index0 = indexOfEdge(edge);

		execute(new MoveEdgeChange(index0, index));
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#setTerminal(de.ims.icarus.language.model.Edge, de.ims.icarus.language.model.Markable, boolean)
	 */
	@Override
	public void setTerminal(Edge edge, Markable markable, boolean isSource) {
		execute(new TerminalChange(edge, isSource, markable));
	}

	/**
	 * Allows subclasses to intercept execution of an {@code add} change.
	 * This method is intended to be both a notification for the structure
	 * to refresh its internal data and an opportunity to verify the correctness
	 * of the change.
	 * <p>
	 * If this method throws an exception no changes will be carried out by
	 * the model!
	 *
	 * @param edge The {@code Edge} that will be added to the structure
	 * @param index The position in the list the edge should be placed at
	 */
	protected void edgeAdded(Edge edge, int index) {
		// hook for subclasses to refresh structure data
	}


	/**
	 * Allows subclasses to intercept execution of an {@code remove} change.
	 * This method is intended to be both a notification for the structure
	 * to refresh its internal data and an opportunity to verify the correctness
	 * of the change.
	 * <p>
	 * If this method throws an exception no changes will be carried out by
	 * the model!
	 *
	 * @param edge The {@code Edge} that will be added to the structure
	 * @param index The position in the list the edge should be placed at
	 */
	protected void edgeRemoved(Edge edge, int index) {
		// hook for subclasses to refresh structure data
	}

	/**
	 * Allows subclasses to intercept execution of an {@code terminal} change.
	 * This method is intended to be both a notification for the structure
	 * to refresh its internal data and an opportunity to verify the correctness
	 * of the change.
	 * <p>
	 * If this method throws an exception no changes will be carried out by
	 * the model!
	 *
	 * @param edge The {@code Edge} whose terminal will be changed
	 * @param isSource Specifies whether or not the source terminal is affected
	 * @param oldTerminal The old terminal (this is essentially the same as {@link Edge#getSource()}
	 * @param newTerminal The markable to which the terminal should be changed
	 */
	protected void terminalChanged(Edge edge, boolean isSource, Markable oldTerminal, Markable newTerminal) {
		// hook for subclasses to refresh structure data
	}

	private class EdgeChange implements AtomicChange {

		private Edge edge;
		private final int index;
		private final long id;
		private int expectedSize;

		public EdgeChange(int index, long id, Edge edge) {
			this.index = index;
			this.id = id;
			this.edge = edge;
			expectedSize = edges.size();
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(edges.size()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Removing failed", expectedSize, edges.size())); //$NON-NLS-1$

			if(edge==null) {
				if(edges.get(index).getId()!=id)
					throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
							"Removing failed", id, edges.get(index).getId())); //$NON-NLS-1$

				edgeRemoved(edges.get(index), index);

				edge = edges.remove(index);
				expectedSize--;
			} else {
				edgeAdded(edge, index);

				edges.add(index, edge);
				expectedSize++;
				edge = null;
			}
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return AbstractRootedStructure.this;
		}

	}

	private class MoveEdgeChange implements AtomicChange {

		private int indexFrom, indexTo;
		private long idFrom, idTo;
		private int expectedSize;

		public MoveEdgeChange(int indexFrom, int indexTo) {
			this.indexFrom = indexFrom;
			this.indexTo = indexTo;

			idFrom = edges.get(indexFrom).getId();
			idTo = edges.get(indexTo).getId();

			expectedSize = edges.size();
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(edges.size()!=expectedSize)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Removing failed", expectedSize, edges.size())); //$NON-NLS-1$

			Edge e1 = edges.get(indexFrom);
			Edge e2 = edges.get(indexTo);

			if(e1.getId()!=idFrom)
				throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
						"Moving failed (origin)", idFrom, e1.getId())); //$NON-NLS-1$
			if(e2.getId()!=idTo)
				throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
						"Moving failed (destination)", idTo, e2.getId())); //$NON-NLS-1$

			edges.set(e2, indexFrom);
			edges.set(e1, indexTo);

			int tmp = indexFrom;
			indexFrom = indexTo;
			indexTo = tmp;

			long idTmp = idFrom;
			idFrom = idTo;
			idTo = idTmp;

			//TODO should we add some hook to react on edge movement?
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return AbstractRootedStructure.this;
		}

	}

	private class ClearEdgesChange implements AtomicChange {

		private Object[] items;
		private int expectedSize = edges.size();

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
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
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return AbstractRootedStructure.this;
		}

	}

	private class TerminalChange implements AtomicChange {

		private final Edge edge;
		private final boolean isSource;
		private Markable terminal;
		private long expectedId;

		public TerminalChange(Edge edge, boolean isSource, Markable terminal) {
			if (edge == null)
				throw new NullPointerException("Invalid edge"); //$NON-NLS-1$
			if (terminal == null)
				throw new NullPointerException("Invalid terminal");  //$NON-NLS-1$

			this.edge = edge;
			this.isSource = isSource;
			this.terminal = terminal;

			expectedId = isSource ? edge.getSource().getId() : edge.getTarget().getId();
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			Markable oldTerminal = isSource ? edge.getSource() : edge.getTarget();
			if(expectedId!=oldTerminal.getId())
				throw new CorruptedStateException(CorpusMemberUtils.idMismatchMessage(
						"Terminal change failed", expectedId, oldTerminal.getId())); //$NON-NLS-1$

			expectedId = terminal.getId();

			terminalChanged(edge, isSource, oldTerminal, terminal);

			if(isSource) {
				edge.setSource(terminal);
			} else {
				edge.setTarget(terminal);
			}

			terminal = oldTerminal;
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return edge;
		}

	}
}
