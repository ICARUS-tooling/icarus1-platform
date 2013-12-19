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

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.CorpusMember;
import de.ims.icarus.language.model.Edge;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.language.model.standard.CorpusMemberUtils;
import de.ims.icarus.language.model.standard.LookupList;
import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AbstractRootedStructure extends EmptyStructure {

	private final LookupList<Edge> edges = new LookupList<>();

	public AbstractRootedStructure(Container parent, Container boundary) {
		super(parent, boundary);
	}

	public AbstractRootedStructure(Container parent) {
		super(parent);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#getEdgeCount()
	 */
	@Override
	public int getEdgeCount() {
		// TODO Auto-generated method stub
		return super.getEdgeCount();
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#getEdgeAt(int)
	 */
	@Override
	public Edge getEdgeAt(int index) {
		// TODO Auto-generated method stub
		return super.getEdgeAt(index);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#indexOfEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public int indexOfEdge(Edge edge) {
		// TODO Auto-generated method stub
		return super.indexOfEdge(edge);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#containsEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public boolean containsEdge(Edge edge) {
		// TODO Auto-generated method stub
		return super.containsEdge(edge);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#removeAllEdges()
	 */
	@Override
	public void removeAllEdges() {
		execute(new ClearChange());
	}

	protected void checkEdge(Edge edge) {
		if (edge == null)
			throw new NullPointerException("Invalid edge");

		checkMarkable(edge.getSource());
		checkMarkable(edge.getTarget());
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#addEdge(de.ims.icarus.language.model.Markable, de.ims.icarus.language.model.Markable)
	 */
	@Override
	public Markable addEdge(Markable source, Markable target) {
		Edge edge = new
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#addEdge(de.ims.icarus.language.model.Markable, de.ims.icarus.language.model.Markable, int)
	 */
	@Override
	public Edge addEdge(Markable source, Markable target, int index) {
		// TODO Auto-generated method stub
		return super.addEdge(source, target, index);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#removeEdge(int)
	 */
	@Override
	public Edge removeEdge(int index) {
		Edge edge = getEdgeAt(index);

		execute(new ElementChange(index, edge.getId(), edge));

		return edge;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#removeEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public Edge removeEdge(Edge edge) {
		int index = indexOfEdge(edge);

		execute(new ElementChange(index, edge.getId(), null));

		return edge;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#moveEdge(int, int)
	 */
	@Override
	public void moveEdge(int index0, int index1) {
		execute(new MoveChange(index0, index1));
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#moveEdge(de.ims.icarus.language.model.Edge, int)
	 */
	@Override
	public void moveEdge(Edge edge, int index) {
		int index0 = indexOfEdge(edge);

		execute(new MoveChange(index0, index));
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#setTerminal(de.ims.icarus.language.model.Edge, de.ims.icarus.language.model.Markable, boolean)
	 */
	@Override
	public void setTerminal(Edge edge, Markable markable, boolean isSource) {
		// TODO Auto-generated method stub
		super.setTerminal(edge, markable, isSource);
	}

	private class ElementChange implements AtomicChange {

		private Edge edge;
		private final int index;
		private final long id;
		private int expectedSize;

		public ElementChange(int index, long id, Edge edge) {
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

				edge = edges.remove(index);
				expectedSize--;
			} else {
				edges.add(index, edge);
				edge = null;
				expectedSize++;
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

	private class MoveChange implements AtomicChange {

		private int indexFrom, indexTo;
		private long idFrom, idTo;
		private int expectedSize;

		public MoveChange(int indexFrom, int indexTo) {
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
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return AbstractRootedStructure.this;
		}

	}

	private class ClearChange implements AtomicChange {

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
		}

		/**
		 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return AbstractRootedStructure.this;
		}

	}
}
