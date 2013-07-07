/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.helper;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import net.ikarus_systems.icarus.language.AvailabilityObserver;
import net.ikarus_systems.icarus.language.DataType;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.util.data.DataListModel;

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
		SentenceDataList dataList = (SentenceDataList)this.dataList;
		return dataList==null ? null : dataList.get(index, dataType, this);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.AvailabilityObserver#dataAvailable(int, net.ikarus_systems.icarus.language.SentenceData)
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
	 * Only ever runs on the Event-Dispatch-Thread
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
