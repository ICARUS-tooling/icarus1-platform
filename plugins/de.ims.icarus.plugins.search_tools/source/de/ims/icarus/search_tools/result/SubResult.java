package de.ims.icarus.search_tools.result;

import java.util.Arrays;
import java.util.List;

import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.annotation.AnnotationBuffer;
import de.ims.icarus.search_tools.corpus.CorpusSearchResult0D;
import de.ims.icarus.search_tools.standard.GroupCache;
import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.CompactProperties;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataList;



public class SubResult implements SearchResult {

	protected final SearchResult base;
	protected final int fixedDimensions;

	protected final int[] indexBuffer;
	
	protected int baseSize = 0;
	protected int size = 0;
	
	protected int[][] groupTranslate;
	
	protected int[][] groupMatchCounts;
	
	protected CompactProperties properties;

	public SubResult(SearchResult base, int... groupInstances) {
		if(base==null)
			throw new NullPointerException("Invalid base"); //$NON-NLS-1$
		if (base.getDimension()-groupInstances.length < 1)
			throw new IllegalArgumentException(
					"Dimension of base not high enough"); //$NON-NLS-1$

		this.base = base;
		fixedDimensions = groupInstances.length;
		indexBuffer = new int[base.getDimension()];
		System.arraycopy(groupInstances, 0, indexBuffer, 0, fixedDimensions);
		
		groupTranslate = new int[getDimension()][];
		groupMatchCounts = new int[getDimension()][];
		
		getTotalMatchCount();
	}

	@Override
	public int getDimension() {
		return base.getDimension() - fixedDimensions;
	}

	@Override
	public Object getGroupLabel(int groupId) {
		return base.getGroupLabel(groupId + fixedDimensions);
	}

	@Override
	public int getInstanceCount(int groupId) {
		return groupTranslate[groupId]==null ? 
				base.getInstanceCount(groupId + fixedDimensions)
				: groupTranslate[groupId].length;
	}

	@Override
	public Object getInstanceLabel(int groupId, int index) {
		return base.getInstanceLabel(groupId + fixedDimensions, 
				groupTranslate[groupId]==null ? index :
					groupTranslate[groupId][index]);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getGroupMatchCount(int, int)
	 */
	@Override
	public int getGroupMatchCount(int groupId, int index) {
		checkSize();
		
		if(groupTranslate[groupId]==null) {
			return base.getGroupMatchCount(groupId + fixedDimensions, index); 
		} else {
			index = groupTranslate[groupId][index];
			return groupMatchCounts[groupId][index];
		}
	}

	/**
	 * We would have to traverse our translate array for
	 * the given dimension. This is rather costly and in
	 * general this method is never used in the
	 * dependency view?
	 */
	@Override
	public int getIndexOf(int dimension, Object label) {
		//return base.getCaseDiffIndex(dimension + fixedDimensions, label);
		return -1;
	}
	
	protected void prepareIndices(int[] indices) {
		checkIndices(indices);
		
		for(int i=0; i<indices.length; i++) {
			if(groupTranslate[i]!=null)
				// FIXME IndexOutOfBoundsException on indices array
				indices[i] = groupTranslate[i][indices[i]];
		}
		
		System.arraycopy(indices, 0, indexBuffer, fixedDimensions,
				indexBuffer.length - fixedDimensions);
	}

	protected void checkIndices(int... indices) {
		if (indices.length != groupTranslate.length)
			throw new IllegalArgumentException(String.format(
					"Invalid number of index arguments: %d - expected %d", //$NON-NLS-1$
					indices.length, groupTranslate.length));
	}

	@Override
	public Object getEntryAt(int index, int... groupIndices) {
		prepareIndices(groupIndices);
		return base.getEntryAt(index, indexBuffer);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getRawEntryAt(int, int[])
	 */
	@Override
	public ResultEntry getRawEntryAt(int index, int... groupIndices) {
		prepareIndices(groupIndices);
		return base.getRawEntryAt(index, indexBuffer);
	}

	@Override
	public int getMatchCount(int... indices) {
		prepareIndices(indices);
		return base.getMatchCount(indexBuffer);
	}
	
	public AnnotationBuffer getAnnotationBuffer() {
		if(base instanceof AbstractSearchResult) {
			return ((AbstractSearchResult)base).getAnnotationBuffer();
		} else {
			return null;
		}
	}

	@Override
	public SearchResult getSubResult(int... groupInstances) {
		if(getDimension()-groupInstances.length==0){
			// Avoid creating a "real" sub-result for dimension of 0
			// since the default list-based implementation is far more
			// efficient!
			prepareIndices(groupInstances);
			List<ResultEntry> list = base.getRawEntryList(indexBuffer);
			if(list!=null) {
				CorpusSearchResult0D subResult = new CorpusSearchResult0D(getSource(), list);
				subResult.setAnnotationBuffer(getAnnotationBuffer());
				
				return subResult;
			}
		} else if (getDimension() >= groupInstances.length) {
			// Combine group-instance arrays and let base create new
			// SubResult
			int[] combinedInstances = new int[fixedDimensions+groupInstances.length];
			System.arraycopy(indexBuffer, 0, combinedInstances, 0, fixedDimensions);
			System.arraycopy(groupInstances, 0, combinedInstances, fixedDimensions, groupInstances.length);
			
			return base.getSubResult(combinedInstances);
		}
		return null;
	}

	@Override
	public DataList<? extends Object> getEntryList(int... indices) {
		prepareIndices(indices);
		return base.getEntryList(indexBuffer);
	}

	@Override
	public List<ResultEntry> getRawEntryList(int... indices) {
		prepareIndices(indices);
		return base.getRawEntryList(indexBuffer);
	}
	
	private void addGroupMatchCount(int groupId, int index, int amount) {
		if(amount==0) {
			return;
		}
		
		int[] list = groupMatchCounts[groupId];
		
		if(list==null) {
			list = new int[Math.max(10, index*2)];
			groupMatchCounts[groupId] = list;
		} else if(list.length<=index) {
			list = Arrays.copyOf(list, Math.max(10, index*2));
			groupMatchCounts[groupId] = list;
		}
		
		list[index] += amount;
	}
	
	private void refreshSize0D() {
		int[] indices = new int[fixedDimensions];
		System.arraycopy(indexBuffer, 0, indices, 0, fixedDimensions);
		size = base.getMatchCount(indices);
	}
	
	private void refreshSize1D(boolean[][] groupOccurencies) {
		int[] indices = new int[fixedDimensions+1];
		System.arraycopy(indexBuffer, 0, indices, 0, fixedDimensions);
		
		int groupCount = getInstanceCount(0);
		int entryCount;
		for(int i=0; i<groupCount; i++) {
			indices[fixedDimensions] = i;
			entryCount = base.getMatchCount(indices);
			if(entryCount>0) {
				groupOccurencies[0][i] = true;
				addGroupMatchCount(0, i, entryCount);
			}
			size += entryCount;
		}
	}
	
	private void refreshSize2D(boolean[][] groupOccurencies) {
		int[] indices = new int[fixedDimensions+2];
		System.arraycopy(indexBuffer, 0, indices, 0, fixedDimensions);
		
		int groupCount1 = getInstanceCount(0);
		int groupCount2 = getInstanceCount(1);
		int count;
		for(int i=0; i<groupCount1; i++) {
			indices[fixedDimensions] = i;
			for(int j=0; j<groupCount2; j++) {
				indices[fixedDimensions+1] = j;
				count = base.getMatchCount(indices);
				if(count>0) {
					groupOccurencies[0][i] = true;
					groupOccurencies[1][j] = true;
					
					addGroupMatchCount(0, i, count);
					addGroupMatchCount(1, j, count);
				}
				size += count;
			}
		}
	}
	
	private void refreshSizeND(boolean[][] groupOccurencies, 
			int dimension, int[] indices) {
		int groupCount = getInstanceCount(dimension);
		int count;
		if(dimension==indices.length-1) {			
			for(int i=0; i<groupCount; i++) {
				indices[dimension] = i;
				count = base.getMatchCount(indices);
				if(count>0) {
					for(int j=0; j<indices.length; j++) {
						groupOccurencies[j][indices[j]] = true;
						addGroupMatchCount(j, indices[j], count);
					}
				}
				size += count;
			}				
		} else {		
			for(int i=0; i<groupCount; i++) {
				indices[dimension] = i;
				refreshSizeND(groupOccurencies, dimension+1, indices);
			}				
		}
	}
	
	private void refreshSize() {
		size = 0;
		
		boolean[][] groupOccurencies = new boolean[groupTranslate.length][];
		
		for(int i=0; i< groupTranslate.length; i++) {
			groupTranslate[i] = null;
			groupOccurencies[i] = new boolean[base.getInstanceCount(fixedDimensions+i)];
		}			
		
		switch (getDimension()) {
		case 0:
			refreshSize0D();
			// should never occure?
			break;
			
		case 1:
			refreshSize1D(groupOccurencies);
			break;
			
		case 2:
			refreshSize2D(groupOccurencies);
			break;

		default:
			// NOTE: very expensive strategy, but
			// seems the only way for arbitrary dimensional
			// results
			int[] indices = new int[indexBuffer.length];
			System.arraycopy(indexBuffer, 0, indices, 0, fixedDimensions);
			refreshSizeND(groupOccurencies, 0, indices);
			break;
		}
		
		int idx;
		for(int i=0; i<groupTranslate.length; i++) {
			idx = 0;
			
			int[] matchCounts = groupMatchCounts[i];
			
			// Shrink instance label array via translate
			int count = CollectionUtils.count(groupOccurencies[i], true);
			groupTranslate[i] = new int[count];
			groupMatchCounts[i] = new int[count];
			
			for(int j=0; j<groupOccurencies[i].length; j++) {
				if(groupOccurencies[i][j]) {
					groupTranslate[i][idx] = j;
					groupMatchCounts[i][idx] = matchCounts[j];
					idx++;
				}
			}
		}
		
		
		baseSize = base.getTotalMatchCount();
	}
	
	private void checkSize() {
		if(baseSize!=base.getTotalMatchCount()) {
			synchronized (this) {
				refreshSize();
			}
		}
	}

	@Override
	public int getTotalMatchCount() {
		checkSize();
		
		return size;
	}
	
	private int feedFilter(int dimension, int index, int[] filter, int[] indices) {
		int caseDiffCount = getInstanceCount(dimension);
		int startIndex = index;
		if(dimension==indices.length-1) {	
			List<ResultEntry> entries;
			for(int i=0; i<caseDiffCount; i++) {
				indices[dimension] = i;
				entries = base.getRawEntryList(indices);
				for(ResultEntry entry : entries) {
					filter[index] = entry.getIndex();

					index++;
				}
			}				
		} else {		
			for(int i=0; i<caseDiffCount; i++) {
				indices[dimension] = i;
				index += feedFilter(dimension+1, index, filter, indices);
			}				
		}
		
		return index-startIndex;
	}

	//@Override
	public int[] asFilter() {
		int[] filter = new int[getTotalMatchCount()];			
		int[] indices = new int[indexBuffer.length];
		
		// NOTE: again not the most optimal strategy i think
		feedFilter(0, 0, filter, indices);
		
		return filter;
	}

	@Override
	public boolean isFinal() {
		return base.isFinal();
	}

	@Override
	public boolean reorder(int[] permutation) {
		// no reordering of sub results!
		return false;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#canReorder()
	 */
	@Override
	public boolean canReorder() {
		// no reordering of sub results!
		return false;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getSource()
	 */
	@Override
	public Search getSource() {
		return base.getSource();
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getGroupConstraint(int)
	 */
	@Override
	public SearchConstraint getGroupConstraint(int groupId) {
		return base.getGroupConstraint(groupId + fixedDimensions);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return base.getContentType();
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getEntry(int)
	 */
	@Override
	public Object getEntry(int index) {
		return null;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getRawEntry(int)
	 */
	@Override
	public ResultEntry getRawEntry(int index) {
		return null;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#createCache()
	 */
	@Override
	public GroupCache createCache() {
		return null;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#clear()
	 */
	@Override
	public void clear() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#finish()
	 */
	@Override
	public void finish() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getPlainEntry(de.ims.icarus.search_tools.result.ResultEntry)
	 */
	@Override
	public Object getPlainEntry(ResultEntry entry) {
		return base.getPlainEntry(entry);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getAnnotatedEntry(de.ims.icarus.search_tools.result.ResultEntry)
	 */
	@Override
	public AnnotatedData getAnnotatedEntry(ResultEntry entry) {
		return base.getAnnotatedEntry(entry);
	}

	/**
	 * @see de.ims.icarus.util.annotation.AnnotationContainer#getAnnotationType()
	 */
	@Override
	public ContentType getAnnotationType() {
		return base.getAnnotationType();
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getTotalHitCount()
	 */
	@Override
	public int getTotalHitCount() {
		return 0;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#setProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new CompactProperties();
		}
		properties.put(key, value);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		return properties==null ? base.getProperty(key) : properties.get(key);
	}
}