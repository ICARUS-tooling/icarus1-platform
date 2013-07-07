/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.helper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;

import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.ui.actions.ActionList.EntryType;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.data.DataListModel;
import de.ims.icarus.util.data.DataListPresenter;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SentenceDataListPresenter extends DataListPresenter<SentenceData> {
	
	protected DataTypeButton[] dataTypeButtons;
	
	public SentenceDataListPresenter() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible(
				"SentenceDataListContentType", type); //$NON-NLS-1$
	}

	@Override
	protected JList<SentenceData> createList() {
		JList<SentenceData> list = super.createList();
		list.setCellRenderer(new SentenceDataListCellRenderer());
		
		return list;
	}

	@Override
	protected DataListModel<SentenceData> createListModel() {
		return new SentenceDataListModel();
	}
	
	@Override
	public SentenceDataListModel getDataListModel() {
		if(dataListModel==null) {
			dataListModel = createListModel();
		}
		return (SentenceDataListModel) dataListModel;
	}
	
	@Override
	protected Object leftNavigationContent() {
		return new Object[] {
				getOutlineToggleButton(),
				EntryType.SEPARATOR,
				getDataTypeButton(DataType.SYSTEM),
				getDataTypeButton(DataType.USER),
				getDataTypeButton(DataType.GOLD),
		};
	}
	
	protected DataTypeButton getDataTypeButton(DataType dataType) {
		int index = dataType.ordinal();
		if(dataTypeButtons==null) {
			dataTypeButtons = new DataTypeButton[DataType.values().length];
		}
		
		DataTypeButton button = dataTypeButtons[index];
		if(button==null) {
			button = new DataTypeButton(dataType);
			dataTypeButtons[index] = button;
		}
		
		return button;
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class DataTypeButton extends JButton implements ActionListener {

		private static final long serialVersionUID = 2032467669223802186L;
		
		private final DataType dataType;
		
		public DataTypeButton(DataType dataType) {
			if(dataType==null)
				throw new IllegalArgumentException("Invalid data type"); //$NON-NLS-1$
			
			this.dataType = dataType;
			
			// TODO set tool-tips and icons
			setText(dataType.name().substring(0, 1));
		}
		
		public DataType getDataType() {
			return dataType;
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			getDataListModel().setDataType(getDataType());
		}
		
	}
}
