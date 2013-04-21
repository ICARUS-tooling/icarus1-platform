/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank;

import java.util.HashMap;
import java.util.Map;

import net.ikarus_systems.icarus.language.SentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankMetaDataBuilder {
	
	private int dataCount = 0;
	private long totalLength = 0;
	private int maxLength = 0;
	private int minLength = Integer.MAX_VALUE;
	
	private int[] counters = new int[200];

	public TreebankMetaDataBuilder() {
		// no-op
	}
	
	public void process(SentenceData data) {
		int len = data.length();
		
		dataCount++;
		totalLength += len;
		maxLength = Math.max(maxLength, len);
		minLength = Math.min(minLength, len);
		
		if(len>=counters.length) {
			int newSize = Math.max(len+1, counters.length+50);
			int[] newCounters = new int[newSize];
			System.arraycopy(counters, 0, newCounters, 0, counters.length);
		}
		
		counters[len]++;
	}

	public TreebankMetaData buildMetaData() {
		Map<String, Object> metaData = new HashMap<>();
		
		metaData.put(TreebankMetaData.ITEM_COUNT, dataCount);
		metaData.put(TreebankMetaData.TOTAL_LENGTH, totalLength);
		metaData.put(TreebankMetaData.MAX_LENGTH, maxLength);
		metaData.put(TreebankMetaData.MIN_LENGTH, minLength);

		metaData.put(TreebankMetaData.AVERAGE_LENGTH, totalLength/(double)dataCount);
		
		for(int i=minLength; i<=maxLength; i++) {
			int count = counters[i];
			if(count>0) {
				metaData.put("itemCount_"+i, count); //$NON-NLS-1$
			}
		}
		
		return new DefaultTreebankMetaData(metaData);
	}
	

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private static class DefaultTreebankMetaData implements TreebankMetaData {
		
		private final Map<String, Object> metaData;
		
		DefaultTreebankMetaData(Map<String, Object> metaData) {
			this.metaData = metaData;
		}

		/**
		 * @see net.ikarus_systems.icarus.language.treebank.TreebankMetaData#getValue(java.lang.String)
		 */
		@Override
		public Object getValue(String key) {
			return metaData==null ? null : metaData.get(key);
		}
		
	}
}
