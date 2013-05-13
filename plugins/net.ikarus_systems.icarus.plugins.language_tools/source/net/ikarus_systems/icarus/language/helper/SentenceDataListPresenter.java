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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;

import net.ikarus_systems.icarus.language.DataType;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.ui.NavigationControl;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.data.DataListModel;
import net.ikarus_systems.icarus.util.data.DataListPresenter;

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
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#supports(net.ikarus_systems.icarus.util.data.ContentType)
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
	protected NavigationControl createNavigationControl() {	
		
		Options options = new Options();
		Object[] rightContent = {
				getFilterSelect(),
				getFilterEditButton(),
		};
		options.put(NavigationControl.RIGHT_CONTENT_OPTION, rightContent);
		
		Object[] leftContent = {
				getDataTypeButton(DataType.SYSTEM),
				getDataTypeButton(DataType.USER),
				getDataTypeButton(DataType.GOLD),
		};
		options.put(NavigationControl.LEFT_CONTENT_OPTION, leftContent);
		
		return new NavigationControl(list, options);
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
