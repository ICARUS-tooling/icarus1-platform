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
package de.ims.icarus.plugins.dependency.table;

import java.awt.Dimension;
import java.util.logging.Level;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.MutableSentenceData;
import de.ims.icarus.language.SentenceDataEvent;
import de.ims.icarus.language.SentenceDataListener;
import de.ims.icarus.language.dependency.DependencyConstants;
import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.language.dependency.MutableDependencyData;
import de.ims.icarus.language.dependency.MutableDependencyData.DependencyDataEntry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.Localizable;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.table.TablePresenter;
import de.ims.icarus.ui.table.TooltipTableCellRenderer;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.data.ContentType;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyTablePresenter extends TablePresenter implements SentenceDataListener {

	protected MutableDependencyData data;
		
	public ContentType getContentType() {
		return DependencyUtils.getDependencyContentType();
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataListener#dataChanged(de.ims.icarus.language.SentenceDataEvent)
	 */
	@Override
	public void dataChanged(SentenceDataEvent event) {
		DependencyTableModel model = (DependencyTableModel) table.getModel();
		model.refresh();
	}

	/**
	 * @see de.ims.icarus.ui.table.TablePresenter#createTable()
	 */
	@Override
	protected JTable createTable() {
		JTable table = new JTable(new DependencyTableModel(), new DependencyTableColumnModel());
		table.getTableHeader().setReorderingAllowed(true);
		table.setIntercellSpacing(new Dimension(4, 3));
		table.setFillsViewportHeight(true);
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setDefaultRenderer(String.class, new TooltipTableCellRenderer());
		UIUtil.enableToolTip(table);
		return table;
	}
	
	/**
	 * @see de.ims.icarus.ui.table.TablePresenter#setData(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	protected void setData(Object data, Options options) {	
		MutableDependencyData newData = null;
		if(data instanceof MutableDependencyData) {
			newData = (MutableDependencyData)data;
		} else {
			newData = new MutableDependencyData();
			if(data!=null) {
				newData.copyFrom((DependencyData)data);
			}
		}
		
		MutableDependencyData oldData = getData();
		if(newData==oldData) {
			return;
		}
		
		if(oldData!=null) {
			oldData.removeSentenceDataListener(this);
		}
		
		this.data = newData;
		
		if(newData!=null) {
			newData.addSentenceDataListener(this);
		}
		
		getTable().setRowSelectionAllowed(isDataMutable());
	}
	
	public MutableDependencyData getData() {
		return data;
	}
	
	protected boolean isDataMutable() {
		// obsolete check
		return data instanceof MutableDependencyData;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		if(data==null) {
			return;
		}
		
		if(data instanceof MutableSentenceData) {
			((MutableSentenceData)data).removeSentenceDataListener(this);
		}
		
		data = null;
		table.repaint();
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return data;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return data!=null;
	}


	protected static String[] columnKeys = { 
		"plugins.dependency.captions.index", //$NON-NLS-1$
		"plugins.dependency.captions.form", //$NON-NLS-1$
		"plugins.dependency.captions.lemma", //$NON-NLS-1$
		"plugins.dependency.captions.features", //$NON-NLS-1$
		"plugins.dependency.captions.pos", //$NON-NLS-1$
		"plugins.dependency.captions.head", //$NON-NLS-1$
		"plugins.dependency.captions.relation", //$NON-NLS-1$
	};

	protected static String[] headOptions = { 
		LanguageUtils.DATA_ROOT_LABEL,
		LanguageUtils.DATA_UNDEFINED_LABEL,
	};

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class DependencyTableColumnModel extends DefaultTableColumnModel
			implements Localizable {

		private static final long serialVersionUID = 2661986099799307507L;

		protected DependencyTableColumnModel() {
			addColumn(new TableColumn(DependencyConstants.TABLE_INDEX_INDEX, 30, null, null));
			addColumn(new TableColumn(DependencyConstants.TABLE_INDEX_FORM, 100, null, null));
			addColumn(new TableColumn(DependencyConstants.TABLE_INDEX_LEMMA, 60, null, null));
			addColumn(new TableColumn(DependencyConstants.TABLE_INDEX_FEATURES, 60, null, null));
			addColumn(new TableColumn(DependencyConstants.TABLE_INDEX_POS, 40, null, null));

			JComboBox<String> comboBox = new JComboBox<String>(headOptions);
			comboBox.setEditable(true);
			comboBox.setMaximumRowCount(2);

			addColumn(new TableColumn(DependencyConstants.TABLE_INDEX_HEAD, 40, null,
					new DefaultCellEditor(comboBox)));
			addColumn(new TableColumn(DependencyConstants.TABLE_INDEX_REL, 60, null, null));
			
			ResourceManager.getInstance().getGlobalDomain().addItem(this);
			
			localize();
		}

		@Override
		public boolean getColumnSelectionAllowed() {
			return false;
		}

		@Override
		public void localize() {
			TableColumn column;
			for (int i = 0; i < columnKeys.length; i++) {
				column = getColumn(i);
				column.setHeaderValue(ResourceManager.getInstance().get(columnKeys[i]));
			}
		}
	}
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class DependencyTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -7800157966223397649L;
		
		/**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return data==null ? 0 : data.length();
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return columnKeys.length;
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(data==null) {
				return null;
			}
			
			DependencyDataEntry entry = data.getItem(rowIndex);
			
			switch (columnIndex) {
			case DependencyConstants.TABLE_INDEX_INDEX:
				return StringUtil.formatDecimal(rowIndex+1);
				
			case DependencyConstants.TABLE_INDEX_FORM:
				return entry.getForm();
				
			case DependencyConstants.TABLE_INDEX_LEMMA:
				return entry.getLemma();
				
			case DependencyConstants.TABLE_INDEX_POS:
				return entry.getPos();
				
			case DependencyConstants.TABLE_INDEX_FEATURES:
				return entry.getFeatures();
				
			case DependencyConstants.TABLE_INDEX_REL:
				return entry.getRelation();
				
			case DependencyConstants.TABLE_INDEX_HEAD:
				return LanguageUtils.getHeadLabel(entry.getHead());
			}
			
			return null;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if(columnIndex==DependencyConstants.TABLE_INDEX_INDEX) {
				return;
			}
			if(data==null) {
				return;
			}
			
			String sValue = String.valueOf(value);
			String normalizedValue = sValue==null || sValue.isEmpty() ?
					LanguageUtils.DATA_UNDEFINED_LABEL : sValue;
			
			DependencyDataEntry entry = data.getItem(rowIndex);
			switch (columnIndex) {
			case DependencyConstants.TABLE_INDEX_FORM:
				entry.setForm(normalizedValue);
				break;

			case DependencyConstants.TABLE_INDEX_LEMMA:
				entry.setLemma(normalizedValue);
				break;

			case DependencyConstants.TABLE_INDEX_POS:
				entry.setPos(normalizedValue);
				break;

			case DependencyConstants.TABLE_INDEX_REL:
				entry.setRelation(normalizedValue);
				break;

			case DependencyConstants.TABLE_INDEX_FEATURES:
				entry.setFeatures(normalizedValue);
				break;

			case DependencyConstants.TABLE_INDEX_HEAD:
				try {
					entry.setHead(LanguageUtils.parseHeadLabel(normalizedValue));
				} catch(NumberFormatException e) {
					String msg = String.format("Failed to set value of table cell [%d,%d]: %s",  //$NON-NLS-1$
							rowIndex, columnIndex, sValue);
					LoggerFactory.log(this, Level.SEVERE, msg, e);
				}
				break;
			}
		}	
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if(columnIndex==DependencyConstants.TABLE_INDEX_INDEX
					|| columnIndex==DependencyConstants.TABLE_INDEX_HEAD) {
				return Integer.class;
			} else {
				return String.class;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return isDataMutable() && columnIndex!=DependencyConstants.TABLE_INDEX_INDEX;
		}

		public void refresh() {
			fireTableDataChanged();
		}
	}
}
