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
package de.ims.icarus.language.helper;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.data.DataList;
import de.ims.icarus.util.data.DataListModel;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SentenceDataListModel extends DataListModel<SentenceData>
		implements AvailabilityObserver, Runnable {
	
	private static final long serialVersionUID = -3840947824371821481L;
	
	protected DataType dataType = DataType.SYSTEM;
	
	protected Queue<Integer> pendingIndices = new LinkedList<>();
	protected AtomicBoolean dispatchCheck = new AtomicBoolean();
	
	protected static final int MAX_PROCESSED_INDICES = 500;

	public SentenceDataListModel() {
		// no-op
	}
	
	public SentenceDataListModel(SentenceDataList sentenceDataList) {
		super(sentenceDataList);
	}

	public DataType getDataType() {
		return dataType;
	}
	
	public boolean isDataTypeSupported(DataType dataType) {
		SentenceDataList dataList = getDataList();
		return dataList!=null && dataList.supportsType(dataType);
	}

	@Override
	public SentenceDataList getDataList() {
		return (SentenceDataList) super.getDataList();
	}

	@Override
	public void setDataList(DataList<SentenceData> dataList) {
		if(dataList!=null && !(dataList instanceof SentenceDataList))
			throw new IllegalArgumentException("Unsupported data-list class: "+dataList.getClass()); //$NON-NLS-1$
		
		super.setDataList(dataList);
	}

	public void setDataType(DataType dataType) {
		if(this.dataType==dataType) {
			return;
		}
		
		this.dataType = dataType;
		
		fireContentsChanged();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public SentenceData getElementAt(int index) {
		SentenceDataList dataList = getDataList();
		return dataList==null ? null : dataList.get(index, dataType, this);
	}

	/**
	 * @see de.ims.icarus.language.AvailabilityObserver#dataAvailable(int, de.ims.icarus.language.SentenceData)
	 */
	@Override
	public synchronized void dataAvailable(final int index, final SentenceData item) {
		if(!pendingIndices.offer(index)) {
			return;
		}
		
		if(SwingUtilities.isEventDispatchThread()) {
			run();
		} else {
			// Dispatch update if not already running
			if(dispatchCheck.compareAndSet(false, true)) {
				UIUtil.invokeLater(this);
			}
		}
	}

	/**
	 * Only ever runs on the Event-Dispatch-Thread so no additional
	 * synchronizations are performed!
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		int stepCount = Math.min(MAX_PROCESSED_INDICES, pendingIndices.size());
		
		// Allow for early scheduling of next update, since there cannot
		// be complications as long as all updates are dispatched on the EDT!
		dispatchCheck.set(false);
		
		if(stepCount==0) {
			return;
		}
		
		int minIndex = Integer.MAX_VALUE;
		int maxIndex = 0;
		while(stepCount>0) {
			stepCount--;
			Integer index = pendingIndices.poll();
			if(index==null) {
				break;
			}
			minIndex = Math.min(minIndex, index);
			maxIndex = Math.max(maxIndex, index);
		}
		
		fireContentsChanged(this, minIndex, maxIndex);
		
		if(!pendingIndices.isEmpty() && dispatchCheck.compareAndSet(false, true)) {
			UIUtil.invokeLater(this);
		}
	}
}
