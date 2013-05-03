/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.dependency.helper;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.language.SentenceDataEvent;
import net.ikarus_systems.icarus.language.SentenceDataListener;
import net.ikarus_systems.icarus.language.dependency.DependencyConstants;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.language.helper.AbstractSentenceTablePresenter;
import net.ikarus_systems.icarus.ui.table.TableColumnManager;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyTablePresenter extends AbstractSentenceTablePresenter<DependencyData> {

	private TableColumnManager columnManager;
	
	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible(
				DependencyConstants.CONTENT_TYPE_ID, type);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataListener#dataChanged(net.ikarus_systems.icarus.language.SentenceDataEvent)
	 */
	@Override
	public void dataChanged(SentenceDataEvent event) {
		SentenceTableModel model = (SentenceTableModel) table.getModel();
		model.dataChanged(event);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.helper.AbstractSentenceTablePresenter#createTable()
	 */
	@Override
	protected JTable createTable() {
		
		Handle handle = ConfigRegistry.getGlobalRegistry().getHandle(
				"plugins.dependency.appearance.tableColumns"); //$NON-NLS-1$
		columnManager = new TableColumnManager(handle);
		
		JTable table = new JTable(new SentenceTableModel(), columnManager.getColumnModel());
		table.getTableHeader().setReorderingAllowed(false);
		table.setIntercellSpacing(new Dimension(4, 3));
		return table;
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private class SentenceTableModel extends AbstractTableModel implements SentenceDataListener {

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
			return columnManager==null ? 0 : columnManager.getColumnModel().getColumnCount();
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.language.SentenceDataListener#dataChanged(net.ikarus_systems.icarus.language.SentenceDataEvent)
		 */
		@Override
		public void dataChanged(SentenceDataEvent event) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
