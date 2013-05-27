/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.dependency.table;

import java.awt.Dimension;
import java.util.logging.Level;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import net.ikarus_systems.icarus.language.MutableSentenceData;
import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.language.SentenceDataEvent;
import net.ikarus_systems.icarus.language.SentenceDataListener;
import net.ikarus_systems.icarus.language.dependency.DependencyConstants;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.language.dependency.MutableDependencyData;
import net.ikarus_systems.icarus.language.dependency.MutableDependencyData.DependencyDataEntry;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.resources.Localizable;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.table.TablePresenter;
import net.ikarus_systems.icarus.ui.table.TooltipTableCellRenderer;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyTablePresenter extends TablePresenter implements SentenceDataListener {

	protected MutableDependencyData data;
	
	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible(
				DependencyUtils.getDependencyContentType(), type);
	}
	
	public ContentType getContentType() {
		return DependencyUtils.getDependencyContentType();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataListener#dataChanged(net.ikarus_systems.icarus.language.SentenceDataEvent)
	 */
	@Override
	public void dataChanged(SentenceDataEvent event) {
		DependencyTableModel model = (DependencyTableModel) table.getModel();
		model.refresh();
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.table.TablePresenter#createTable()
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
	 * @see net.ikarus_systems.icarus.ui.table.TablePresenter#setData(java.lang.Object, net.ikarus_systems.icarus.util.Options)
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
		return data instanceof MutableDependencyData;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#clear()
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
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return data;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#isPresenting()
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
				return rowIndex+1;
				
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
