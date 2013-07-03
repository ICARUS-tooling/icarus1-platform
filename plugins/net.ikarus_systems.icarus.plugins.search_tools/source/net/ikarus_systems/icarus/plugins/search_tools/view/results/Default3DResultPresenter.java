/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view.results;

import java.lang.ref.Reference;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.ui.list.RowHeaderList;
import net.ikarus_systems.icarus.ui.table.TableRowHeaderRenderer;
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Default3DResultPresenter extends SearchResultPresenter {
	
	protected JTabbedPane tabbedPane;
	
	protected JPanel overviewPanel;
	
	protected JList<Object> fixedList;
	protected JTable table;
	
	protected RowHeaderList rowHeader;
	protected TableRowHeaderRenderer rowHeaderRenderer;
	protected FixedDimensionResultListModel fixedListModel;
	protected SearchResultTableModel tableModel;
	protected ResultCountTableCellRenderer cellRenderer;
	
	protected Map<Object, Reference<SearchResult>> subResults;

	public Default3DResultPresenter() {
		buildContentPanel();
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#getSupportedDimensions()
	 */
	@Override
	public int getSupportedDimensions() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#displayResult(net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	protected void displayResult(Options options) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#refresh()
	 */
	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#buildContentPanel()
	 */
	@Override
	protected void buildContentPanel() {
		// TODO Auto-generated method stub
		
	}

}
