/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.corpus;

import java.util.HashMap;
import java.util.Map;

import net.ikarus_systems.icarus.language.SentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusMetaDataBuilder {
	
	private int dataCount = 0;
	private long totalLength = 0;
	private int maxLength = 0;
	private int minLength = Integer.MAX_VALUE;
	
	private int[] counters = new int[200];

	public CorpusMetaDataBuilder() {
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

	public CorpusMetaData buildMetaData() {
		Map<String, Object> metaData = new HashMap<>();
		
		metaData.put(CorpusMetaData.ITEM_COUNT, dataCount);
		metaData.put(CorpusMetaData.TOTAL_LENGTH, totalLength);
		metaData.put(CorpusMetaData.MAX_LENGTH, maxLength);
		metaData.put(CorpusMetaData.MIN_LENGTH, minLength);

		metaData.put(CorpusMetaData.AVERAGE_LENGTH, totalLength/(double)dataCount);
		
		for(int i=minLength; i<=maxLength; i++) {
			int count = counters[i];
			if(count>0) {
				metaData.put("itemCount_"+i, count); //$NON-NLS-1$
			}
		}
		
		return new DefaultCorpusMetaData(metaData);
	}
	

	private static class DefaultCorpusMetaData implements CorpusMetaData {
		
		private final Map<String, Object> metaData;
		
		DefaultCorpusMetaData(Map<String, Object> metaData) {
			this.metaData = metaData;
		}

		/**
		 * @see net.ikarus_systems.icarus.language.corpus.CorpusMetaData#getValue(java.lang.String)
		 */
		@Override
		public Object getValue(String key) {
			return metaData==null ? null : metaData.get(key);
		}
		
	}
}
