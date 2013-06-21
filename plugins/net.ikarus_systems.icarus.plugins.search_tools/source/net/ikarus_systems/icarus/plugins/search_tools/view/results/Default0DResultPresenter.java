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

import java.awt.BorderLayout;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.jgraph.view.GraphPresenter;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.helper.UIHelperRegistry;
import net.ikarus_systems.icarus.ui.view.AWTPresenter;
import net.ikarus_systems.icarus.ui.view.ListPresenter;
import net.ikarus_systems.icarus.ui.view.PresenterUtils;
import net.ikarus_systems.icarus.ui.view.UnsupportedPresentationDataException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.DataList;
import net.ikarus_systems.icarus.util.data.DataListPresenter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Default0DResultPresenter extends SearchResultPresenter {
	
	public static final int SUPPORTED_DIMENSIONS = 0;
	
	protected ListPresenter listPresenter;
	protected AWTPresenter detailsPresenter;
	
	protected DataList<Object> listWrapper;
	
	protected JTextArea infoLabel;
	protected JSplitPane splitPane;

	public Default0DResultPresenter() {
		buildContentPanel();
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#getSupportedDimensions()
	 */
	@Override
	public int getSupportedDimensions() {
		return SUPPORTED_DIMENSIONS;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#buildContentPanel()
	 */
	@Override
	protected void buildContentPanel() {
		contentPanel = new JPanel(new BorderLayout());
		
		handler = createHandler();

		infoLabel = UIUtil.defaultCreateInfoLabel(contentPanel);
		contentPanel.add(infoLabel, BorderLayout.NORTH);
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setContinuousLayout(false);
		splitPane.setDividerSize(5);
		splitPane.setBorder(null);
		splitPane.setResizeWeight(1);
		contentPanel.add(splitPane, BorderLayout.CENTER);

		showInfo(null);
	}
	
	@Override
	protected Handler createHandler() {
		return new Handler0D();
	}

	@Override
	protected Handler0D getHandler() {
		return (Handler0D) super.getHandler();
	}

	protected void showInfo(String text) {
		if(text==null) {
			text = ResourceManager.getInstance().get(
					"plugins.searchTools.default0DResultPresenter.notAvailable"); //$NON-NLS-1$
		}
		infoLabel.setText(text);
		
		infoLabel.setVisible(true);
		splitPane.setVisible(false);
		splitPane.setLeftComponent(null);
		splitPane.setRightComponent(null);
		
		// Close any active presenter and discard its reference
		if(detailsPresenter!=null) {
			detailsPresenter.close();
			detailsPresenter = null;
		}		
		if(listPresenter!=null) {
			listPresenter.close();
			listPresenter = null;
		}
	}
	
	protected void showDetailInfo(String text) {
		if(text==null) {
			text = ResourceManager.getInstance().get(
					"plugins.searchTools.default0DResultPresenter.notAvailable"); //$NON-NLS-1$
		}
		
		JLabel label = new JLabel(text);
		label.setBorder(new EmptyBorder(10, 10, 10, 10));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		
		splitPane.setLeftComponent(label);
		
		// Close any active presenter and discard its reference
		if(detailsPresenter!=null) {
			detailsPresenter.close();
			detailsPresenter = null;
		}
	}
	
	protected void setDetailPresenter(AWTPresenter detailsPresenter) {
		if(this.detailsPresenter==detailsPresenter) {
			return;
		}
		
		if(this.detailsPresenter!=null) {
			this.detailsPresenter.close();
		}
		
		this.detailsPresenter = detailsPresenter;
		
		if(this.detailsPresenter!=null) {
			splitPane.setLeftComponent(detailsPresenter.getPresentingComponent());
		} else {
			showDetailInfo(null);
		}
	}
	
	protected void setListPresenter(ListPresenter listPresenter) {
		if(this.listPresenter==listPresenter) {
			return;
		}
		
		if(this.listPresenter!=null) {
			this.listPresenter.getSelectionModel().removeListSelectionListener(getHandler());
			this.listPresenter.close();
		}
		
		this.listPresenter = listPresenter;
		
		if(this.listPresenter!=null) {
			this.listPresenter.getSelectionModel().addListSelectionListener(getHandler());
			
			splitPane.setRightComponent(listPresenter.getPresentingComponent());
		} else {
			showInfo(null);
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#refresh()
	 */
	@Override
	public void refresh() {
		if(listWrapper==null) {
			return;
		}
		
		if(listPresenter!=null) {
			listPresenter.getPresentingComponent().repaint();
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#displayResult()
	 */
	@Override
	protected void displayResult() {
		if(searchResult==null) {
			showInfo(null);
			return;
		}
		
		listWrapper = new SearchResultListWrapper(searchResult);
		ContentType entryType = listWrapper.getContentType();
		
		// Ensure list presenter
		ListPresenter listPresenter = this.listPresenter;
		if(listPresenter==null) {
			listPresenter = new DataListPresenter<>();
		}
		
		// Ensure details presenter
		AWTPresenter detailsPresenter = this.detailsPresenter;
		if(detailsPresenter==null || !PresenterUtils.presenterSupports(detailsPresenter, entryType)) {
			// Try graph presenter first
			detailsPresenter = UIHelperRegistry.globalRegistry().findHelper(GraphPresenter.class, entryType, true, false);
			if(detailsPresenter!=null) {
				((GraphPresenter)detailsPresenter).init();
			} else {
				detailsPresenter = UIHelperRegistry.globalRegistry().findHelper(AWTPresenter.class, entryType, true, true);
			}
		}
		
		// Signal missing list presenter
		if(detailsPresenter==null) {
			String text = ResourceManager.getInstance().get(
					"plugins.searchTools.default0DResultPresenter.unsupportedEntryType", entryType.getId()); //$NON-NLS-1$
			showInfo(text);
			return;
		}
		
		// Now present data
		try {
			listPresenter.present(listWrapper, null);
		} catch (UnsupportedPresentationDataException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to present data list: "+listWrapper, e); //$NON-NLS-1$

			String text = ResourceManager.getInstance().get(
					"plugins.searchTools.default0DResultPresenter.presentationFailed", listWrapper.getContentType()); //$NON-NLS-1$
			showInfo(text);
			return;
		}
		
		setListPresenter(listPresenter);
		setDetailPresenter(detailsPresenter);
		
		try {
			displaySelectedData();
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to present selected item", e); //$NON-NLS-1$

			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.presentationFailed", entryType.getId()); //$NON-NLS-1$
			showDetailInfo(text);
			return;
		}
		
		infoLabel.setVisible(false);
		splitPane.setVisible(true);
	}
	
	protected void displaySelectedData() throws Exception {
		if(listPresenter==null || detailsPresenter==null) {
			return;
		}
		
		ListSelectionModel selectionModel = listPresenter.getSelectionModel();
		
		if(selectionModel.getValueIsAdjusting()) {
			return;
		}
		
		int selectedIndex = selectionModel.getMinSelectionIndex();
		Object selectedObject = null;
		
		if(selectedIndex!=-1) {
			selectedObject = listPresenter.getListModel().getElementAt(selectedIndex);
		}
		
		if(selectedObject==null) {
			detailsPresenter.clear();
			return;
		} 
		
		// Display selected object in details presenter
		Options options = new Options();
		options.put(Options.INDEX, selectedIndex);
		options.put(Options.CONTENT_TYPE, listPresenter.getContentType());
		
		detailsPresenter.present(selectedObject, options);
	}
	
	protected class Handler0D extends Handler implements ListSelectionListener {

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			try {
				displaySelectedData();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to handle change in selection: "+e, ex); //$NON-NLS-1$
			}
		}
		
	}
}
