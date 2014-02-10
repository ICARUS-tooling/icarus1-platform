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
package de.ims.icarus.language.treebank;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.language.SentenceData;
import de.ims.icarus.util.Counter;


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

	private Counter counter = new Counter();

	public TreebankMetaDataBuilder() {
		// no-op
	}

	public void process(SentenceData data) {
		int len = data.length();

		dataCount++;
		totalLength += len;
		maxLength = Math.max(maxLength, len);
		minLength = Math.min(minLength, len);

		counter.increment(len);
	}

	public TreebankMetaData buildMetaData() {
		Map<String, Object> metaData = new HashMap<>();

		metaData.put(TreebankMetaData.ITEM_COUNT, dataCount);
		metaData.put(TreebankMetaData.TOTAL_LENGTH, totalLength);
		metaData.put(TreebankMetaData.MAX_LENGTH, maxLength);
		metaData.put(TreebankMetaData.MIN_LENGTH, minLength);

		double avgLength = totalLength/(double)dataCount;
		avgLength = Math.floor(avgLength*100)/100;
		metaData.put(TreebankMetaData.AVERAGE_LENGTH, avgLength);

		for(int i=minLength; i<=maxLength; i++) {
			int count = counter.getCount(i);
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
		 * @see de.ims.icarus.language.treebank.TreebankMetaData#getValue(java.lang.String)
		 */
		@Override
		public Object getValue(String key) {
			return metaData==null ? null : metaData.get(key);
		}

	}
}
