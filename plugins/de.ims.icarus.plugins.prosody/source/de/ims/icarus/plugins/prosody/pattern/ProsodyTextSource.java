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
package de.ims.icarus.plugins.prosody.pattern;

import de.ims.icarus.util.Options;
import de.ims.icarus.util.strings.pattern.TextSource.AggregatedTextSource;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class ProsodyTextSource extends AggregatedTextSource {

	private final ProsodyAccessor accessor;
	private final ProsodyData proxy = new ProsodyData();
	private IndexIterator indexIterator;

	protected ProsodyTextSource(ProsodyAccessor accessor) {
		if (accessor == null)
			throw new NullPointerException("Invalid accessor"); //$NON-NLS-1$

		this.accessor = accessor;
	}

	@Override
	protected boolean aggregateText(Object data, Options env) {

		ProsodyData rawData = (ProsodyData) data;

		proxy.set(rawData);

		if(indexIterator!=null) {

			prepareIndexIterator(indexIterator, rawData);

			if(indexIterator.size()==0) {
				return false;
			}

			for(int i=0; i<indexIterator.size(); i++) {
				if(i>0) {
					buffer.append(accessor.getSeparator());
				}

				int index = indexIterator.indexAt(i);

				// Wait till we move into valid space
				if(index<0) {
					continue;
				}

				// Once again outside valid space -> stop processing
				if(!indexValid(index, rawData)) {
					break;
				}

				setIndex(proxy, index, rawData);
				String text = accessor.getText(proxy, env);

				if(text==null || text.isEmpty()) {
					text = accessor.getDefaultText();
				}

				buffer.append(text);
			}

		} else {
			String text = accessor.getText(proxy, env);

			if(text==null || text.isEmpty()) {
				return false;
			}

			buffer.append(text);
		}


		return true;
	}

	protected abstract void setIndex(ProsodyData proxy, int index, ProsodyData rawData);

	protected abstract void prepareIndexIterator(IndexIterator indexIterator, ProsodyData rawData);

	protected abstract boolean indexValid(int index, ProsodyData rawData);

	public ProsodyAccessor getAccessor() {
		return accessor;
	}

	public IndexIterator getIndexIterator() {
		return indexIterator;
	}

	public void setIndexIterator(IndexIterator indexIterator) {
		this.indexIterator = indexIterator;
	}

	public static abstract class IndexIterator {

		protected int center = -1;

		public abstract int size();
		public abstract int indexAt(int pos);

		public void refresh(int space) {
			// for subclasses
		}

		public int getCenter() {
			return center;
		}

		public void setCenter(int center) {
			this.center = center;
		}
	}

	public static class ScopeIndexIterator extends IndexIterator {

		private final int scope;

		public ScopeIndexIterator(int scope) {
			if(scope<=0)
				throw new IllegalArgumentException("Scope must be positive: "+scope); //$NON-NLS-1$

			this.scope = scope;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator#size()
		 */
		@Override
		public int size() {
			return 2*scope + 1;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator#indexAt(int)
		 */
		@Override
		public int indexAt(int pos) {
			return center-scope+pos;
		}

	}

	public static class OffsetIndexIterator extends IndexIterator {
		private final int leftOffset, rightOffset;

		public OffsetIndexIterator(int leftOffset, int rightOffset) {
			if(leftOffset<0)
				throw new IllegalArgumentException("Left offset must be positive or 0: "+leftOffset); //$NON-NLS-1$
			if(rightOffset<0)
				throw new IllegalArgumentException("Right offset must be positive or 0: "+rightOffset); //$NON-NLS-1$
			if(leftOffset==0 && rightOffset==0)
				throw new IllegalArgumentException("At least one offset must be greater than 0"); //$NON-NLS-1$

			this.leftOffset = leftOffset;
			this.rightOffset = rightOffset;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator#size()
		 */
		@Override
		public int size() {
			return leftOffset+rightOffset+1;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator#indexAt(int)
		 */
		@Override
		public int indexAt(int pos) {
			return center-leftOffset+pos;
		}
	}

	public static class FixedIndexIterator extends IndexIterator {
		private final int[] positions;
		private final int[] indices;

		public FixedIndexIterator(int[] positions) {
			if (positions == null)
				throw new NullPointerException("Invalid positions"); //$NON-NLS-1$

			this.positions = positions;
			this.indices = new int[positions.length];
		}

		@Override
		public void refresh(int space) {
			for(int i=0; i<positions.length; i++) {
				int val = positions[i];
				indices[i] = val<0 ? space+val-1 : val;
			}
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator#size()
		 */
		@Override
		public int size() {
			return positions.length;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator#indexAt(int)
		 */
		@Override
		public int indexAt(int pos) {
			return indices[pos];
		}
	}

	public static class CompleteIndexIterator extends IndexIterator {
		private int space = -1;

		@Override
		public void refresh(int space) {
			this.space = space;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator#size()
		 */
		@Override
		public int size() {
			return space;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator#indexAt(int)
		 */
		@Override
		public int indexAt(int pos) {
			return pos;
		}
	}

	public static class DirectProsodyTextSource extends ProsodyTextSource {

		public DirectProsodyTextSource(ProsodyAccessor accessor) {
			super(accessor);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#setIndex(int)
		 */
		@Override
		protected void setIndex(ProsodyData proxy, int index, ProsodyData rawData) {
			throw new UnsupportedOperationException("Not designed to aggregate data!"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#prepareIndexIterator(de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator, de.ims.icarus.plugins.prosody.pattern.ProsodyData)
		 */
		@Override
		protected void prepareIndexIterator(IndexIterator indexIterator,
				ProsodyData rawData) {
			throw new UnsupportedOperationException("Not designed to aggregate data!"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#indexValid(int)
		 */
		@Override
		protected boolean indexValid(int index, ProsodyData rawData) {
			throw new UnsupportedOperationException("Not designed to aggregate data!"); //$NON-NLS-1$
		}

	}

	public static class SyllableTextSource extends ProsodyTextSource {

		public SyllableTextSource(ProsodyAccessor accessor) {
			super(accessor);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#setIndex(de.ims.icarus.plugins.prosody.pattern.ProsodyData, int, de.ims.icarus.plugins.prosody.pattern.ProsodyData)
		 */
		@Override
		protected void setIndex(ProsodyData proxy, int index,
				ProsodyData rawData) {
			proxy.setSyllableIndex(index);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#prepareIndexIterator(de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator, de.ims.icarus.plugins.prosody.pattern.ProsodyData)
		 */
		@Override
		protected void prepareIndexIterator(IndexIterator indexIterator,
				ProsodyData rawData) {

			indexIterator.setCenter(rawData.getSyllableIndex());
			indexIterator.refresh(rawData.getSentence().getSyllableCount(rawData.getWordIndex()));
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#indexValid(int)
		 */
		@Override
		protected boolean indexValid(int index, ProsodyData rawData) {
			return index<rawData.getSentence().getSyllableCount(rawData.getWordIndex());
		}

	}

	public static class WordTextSource extends ProsodyTextSource {

		public WordTextSource(ProsodyAccessor accessor) {
			super(accessor);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#setIndex(de.ims.icarus.plugins.prosody.pattern.ProsodyData, int, de.ims.icarus.plugins.prosody.pattern.ProsodyData)
		 */
		@Override
		protected void setIndex(ProsodyData proxy, int index,
				ProsodyData rawData) {
			proxy.setWordIndex(index);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#prepareIndexIterator(de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator, de.ims.icarus.plugins.prosody.pattern.ProsodyData)
		 */
		@Override
		protected void prepareIndexIterator(IndexIterator indexIterator,
				ProsodyData rawData) {

			indexIterator.setCenter(rawData.getWordIndex());
			indexIterator.refresh(rawData.getSentence().length());
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#indexValid(int)
		 */
		@Override
		protected boolean indexValid(int index, ProsodyData rawData) {
			return index<rawData.getSentence().length();
		}

	}

	public static class SentenceTextSource extends ProsodyTextSource {

		public SentenceTextSource(ProsodyAccessor accessor) {
			super(accessor);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#setIndex(de.ims.icarus.plugins.prosody.pattern.ProsodyData, int, de.ims.icarus.plugins.prosody.pattern.ProsodyData)
		 */
		@Override
		protected void setIndex(ProsodyData proxy, int index,
				ProsodyData rawData) {
			proxy.setSentenceIndex(index);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#prepareIndexIterator(de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator, de.ims.icarus.plugins.prosody.pattern.ProsodyData)
		 */
		@Override
		protected void prepareIndexIterator(IndexIterator indexIterator,
				ProsodyData rawData) {

			indexIterator.setCenter(rawData.getSentenceIndex());
			indexIterator.refresh(rawData.getDocument().size());
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#indexValid(int)
		 */
		@Override
		protected boolean indexValid(int index, ProsodyData rawData) {
			return index<rawData.getDocument().size();
		}

	}

	public static class DocumentTextSource extends ProsodyTextSource {

		public DocumentTextSource(ProsodyAccessor accessor) {
			super(accessor);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#setIndex(de.ims.icarus.plugins.prosody.pattern.ProsodyData, int, de.ims.icarus.plugins.prosody.pattern.ProsodyData)
		 */
		@Override
		protected void setIndex(ProsodyData proxy, int index,
				ProsodyData rawData) {
			proxy.setDocumentIndex(index);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#prepareIndexIterator(de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource.IndexIterator, de.ims.icarus.plugins.prosody.pattern.ProsodyData)
		 */
		@Override
		protected void prepareIndexIterator(IndexIterator indexIterator,
				ProsodyData rawData) {

			indexIterator.setCenter(rawData.getDocumentIndex());
			indexIterator.refresh(rawData.getDocumentSet().size());
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyTextSource#indexValid(int)
		 */
		@Override
		protected boolean indexValid(int index, ProsodyData rawData) {
			return index<rawData.getDocumentSet().size();
		}

	}
}
