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
package de.ims.icarus.model.standard.driver.file.indexing;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.driver.IndexSet;
import de.ims.icarus.model.api.driver.IndexUtils;
import de.ims.icarus.model.api.driver.indexing.Index;
import de.ims.icarus.model.api.driver.indexing.IndexCollector;
import de.ims.icarus.model.api.driver.indexing.IndexReader;
import de.ims.icarus.model.iql.expr.ExpressionUtils;
import de.ims.icarus.model.iql.expr.func.Function;
import de.ims.icarus.model.iql.expr.func.FunctionUtils;
import de.ims.icarus.model.util.CorpusUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class IndexImplFunction extends AbstractIndex {

	private final Function function;
	private final Function batchFunction;

	public IndexImplFunction(Function function, Function batchFunction) {
		if (function == null)
			throw new NullPointerException("Invalid function");  //$NON-NLS-1$

		// Check function
		if(function.getGrade()!=1)
			throw new IllegalArgumentException("Supplied function must expect exactly 1 parameter: "+function.getClass()); //$NON-NLS-1$
		if(!ExpressionUtils.isLongResult(function))
			throw new IllegalArgumentException("Supplied function does not yield results of type 'long': "+function.getClass()); //$NON-NLS-1$
		if(!FunctionUtils.isLongParamFunction(function))
			throw new IllegalArgumentException("Supplied function uses other parameters than 'long': "+function.getClass()); //$NON-NLS-1$

		// Check optional batch function
		if(batchFunction!=null) {
			if(batchFunction.getGrade()!=1)
				throw new IllegalArgumentException("Supplied batch function must expect exactly 1 parameter: "+batchFunction.getClass()); //$NON-NLS-1$
			if(!IndexSet.class.isAssignableFrom(batchFunction.getResultType()))
				throw new IllegalArgumentException("Supplied batch function does not yield results of type 'IndexSet': "+batchFunction.getClass()); //$NON-NLS-1$
			if(!batchFunction.getParamType(0).isAssignableFrom(IndexSet.class))
				throw new IllegalArgumentException("Supplied batch function uses other parameters than 'IndexSet': "+batchFunction.getClass()); //$NON-NLS-1$
		}

		this.function = function;
		this.batchFunction = batchFunction;
	}

	/**
	 * @see de.ims.icarus.model.api.driver.indexing.Index#newReader()
	 */
	@Override
	public IndexReader newReader() {
		return this.new Reader();
	}

	public class Reader implements IndexReader {

		private Function func = (Function) function.clone();
		private Function batchFunc = batchFunction==null ? null : (Function) batchFunction.clone();

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#getSource()
		 */
		@Override
		public Index getSource() {
			return IndexImplFunction.this;
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#begin()
		 */
		@Override
		public void begin() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#end()
		 */
		@Override
		public void end() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.model.io.SynchronizedAccessor#close()
		 */
		@Override
		public void close() throws ModelException {
			func = null;
		}

		private long lookup0(long sourceIndex) {
			return func.setParam(0, sourceIndex).longValue();
		}

		private IndexSet lookup0(IndexSet indices) {
			return (IndexSet) func.setParam(0, indices).value();
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(long, de.ims.icarus.model.api.driver.indexing.IndexCollector)
		 */
		@Override
		public boolean lookup(long sourceIndex, IndexCollector collector)
				throws ModelException, InterruptedException {
			long index = lookup0(sourceIndex);

			if(index==INVALID) {
				return false;
			} else {
				collector.add(index);
				return true;
			}
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(long)
		 */
		@Override
		public IndexSet[] lookup(long sourceIndex) throws ModelException,
				InterruptedException {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getBeginIndex(long)
		 */
		@Override
		public long getBeginIndex(long sourceIndex) throws ModelException,
				InterruptedException {
			return lookup0(sourceIndex);
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getEndIndex(long)
		 */
		@Override
		public long getEndIndex(long sourceIndex) throws ModelException,
				InterruptedException {
			return lookup0(sourceIndex);
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public IndexSet[] lookup(IndexSet[] sourceIndices)
				throws ModelException, InterruptedException {
			// TODO Auto-generated method stub
			return IndexUtils.wrap(lo)
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#lookup(de.ims.icarus.model.api.driver.IndexSet[], de.ims.icarus.model.api.driver.indexing.IndexCollector)
		 */
		@Override
		public boolean lookup(IndexSet[] sourceIndices, IndexCollector collector)
				throws ModelException, InterruptedException {
			// TODO Auto-generated method stub
			return false;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getBeginIndex(de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public long getBeginIndex(IndexSet[] sourceIndices)
				throws ModelException, InterruptedException {
			// TODO Auto-generated method stub
			return 0;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#getEndIndex(de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public long getEndIndex(IndexSet[] sourceIndices)
				throws ModelException, InterruptedException {
			// TODO Auto-generated method stub
			return 0;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#find(long, long, long)
		 */
		@Override
		public long find(long fromSource, long toSource, long targetIndex)
				throws ModelException, InterruptedException {
			// TODO Auto-generated method stub
			return 0;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#find(long, long, de.ims.icarus.model.api.driver.IndexSet[])
		 */
		@Override
		public IndexSet[] find(long fromSource, long toSource,
				IndexSet[] targetIndices) throws ModelException,
				InterruptedException {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.model.api.driver.indexing.IndexReader#find(long, long, de.ims.icarus.model.api.driver.IndexSet[], de.ims.icarus.model.api.driver.indexing.IndexCollector)
		 */
		@Override
		public boolean find(long fromSource, long toSource,
				IndexSet[] targetIndices, IndexCollector collector)
				throws ModelException, InterruptedException {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
