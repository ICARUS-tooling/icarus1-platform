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

import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JToggleButton;

import de.ims.icarus.language.DataType;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.actions.ActionList.EntryType;
import de.ims.icarus.ui.list.DynamicWidthList;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.data.DataListModel;
import de.ims.icarus.util.data.DataListPresenter;
import de.ims.icarus.util.strings.StringUtil;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SentenceDataListPresenter extends DataListPresenter<SentenceData> {

	protected DataTypeButton[] dataTypeButtons;
	protected ButtonGroup buttonGroup;

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
	protected DynamicWidthList<SentenceData> createList() {
		DynamicWidthList<SentenceData> list = super.createList();
		list.setCellRenderer(new SentenceDataListCellRenderer());
		list.setTrackViewportWidth(false);

		// Crucial performance settings:
		// First let the list compute the fixed cell height based
		// on some dummy input, then limit the width to some arbitrary
		// 200px and finally clear the prototype value since later
		// changes made to the cell renderer would cause errors when
		// a new renderer is unable to handle the dummy object!
		//
		// DO NOT deactivate this settings or lists with dozens
		// of thousands of rows will cause the UI to freeze for ages!
		list.setPrototypeCellValue(LanguageUtils.dummySentenceData);
		//list.setFixedCellWidth(200);
		list.setPrototypeCellValue(null);

		return list;
	}

	@Override
	protected DataListModel<SentenceData> createListModel() {
		SentenceDataListModel model = new SentenceDataListModel();

		DataType dataType = model.getDataType();
		getDataTypeButton(dataType).setSelected(true);

		return model;
	}

	@Override
	public SentenceDataListModel getDataListModel() {
		if(dataListModel==null) {
			dataListModel = createListModel();
		}
		return (SentenceDataListModel) dataListModel;
	}

	@Override
	protected void refreshUtilities() {
		SentenceDataListModel model = getDataListModel();
		for(DataType dataType : DataType.values()) {
			getDataTypeButton(dataType).setEnabled(
					model.isDataTypeSupported(dataType));
		}

		getDataTypeButton(model.getDataType()).setSelected(true);
	}

	@Override
	protected Object leftNavigationContent() {
		return new Object[] {
				getOutlineToggleButton(),
				EntryType.SEPARATOR,
				getWidthToggleButton(),
				EntryType.SEPARATOR,
				getDataTypeButton(DataType.SYSTEM),
				getDataTypeButton(DataType.USER),
				getDataTypeButton(DataType.GOLD),
		};
	}

	protected ButtonGroup getButtonGroup() {
		if(buttonGroup==null) {
			buttonGroup = new ButtonGroup();
		}
		return buttonGroup;
	}

	protected DataTypeButton getDataTypeButton(DataType dataType) {
		int index = dataType.ordinal();
		if(dataTypeButtons==null) {
			dataTypeButtons = new DataTypeButton[DataType.values().length];
		}

		DataTypeButton button = dataTypeButtons[index];
		if(button==null) {
			button = new DataTypeButton(dataType);
			getButtonGroup().add(button);
			dataTypeButtons[index] = button;
		}

		return button;
	}

	@Override
	protected int getEstimatedWidth(FontMetrics fm, SentenceData item) {
		int width = 0;
		int size = item.length();

		for(int i=0; i<size; i++) {
			if(i>0) {
				width += fm.charWidth(' ');
			}
			width += fm.stringWidth(item.getForm(i));
		}

		return width;
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class DataTypeButton extends JToggleButton implements ActionListener {

		private static final long serialVersionUID = 2032467669223802186L;

		private final DataType dataType;

		public DataTypeButton(DataType dataType) {
			if(dataType==null)
				throw new NullPointerException("Invalid data type"); //$NON-NLS-1$

			this.dataType = dataType;

			Icon icon = null;
			switch (dataType) {
			case GOLD:
				icon = IconRegistry.getGlobalRegistry().getIcon("datatype_gold.png"); //$NON-NLS-1$
				break;

			case USER:
				icon = IconRegistry.getGlobalRegistry().getIcon("datatype_user.png"); //$NON-NLS-1$
				break;

			case SYSTEM:
				icon = IconRegistry.getGlobalRegistry().getIcon("datatype_system.png"); //$NON-NLS-1$
				break;
			}

			setIcon(icon);
			setFocusable(false);
			setFocusPainted(false);
			setRolloverEnabled(false);

			String key = StringUtil.capitalize(dataType.name().toLowerCase());
			ResourceManager.getInstance().getGlobalDomain().prepareComponent(this,
					null,
					"plugins.languageTools.selectDataType"+key+"Action.description"); //$NON-NLS-1$ //$NON-NLS-2$
			ResourceManager.getInstance().getGlobalDomain().addComponent(this);
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
