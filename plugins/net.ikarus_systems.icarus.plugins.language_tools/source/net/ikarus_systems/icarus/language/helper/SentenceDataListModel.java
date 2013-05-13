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
		implements AvailabilityObserver {
	
	private static final long serialVersionUID = -3840947824371821481L;
	
	protected DataType dataType = DataType.SYSTEM;

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
	public void dataAvailable(final int index, final SentenceData item) {
		if(!SwingUtilities.isEventDispatchThread()) {
			UIUtil.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					fireContentsChanged(SentenceDataListModel.this, index, index);
				}
			});
			return;
		}
		
		fireContentsChanged(this, index, index);
	}
}
