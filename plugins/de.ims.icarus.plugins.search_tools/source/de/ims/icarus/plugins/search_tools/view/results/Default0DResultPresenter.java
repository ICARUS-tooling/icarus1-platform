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
package de.ims.icarus.plugins.search_tools.view.results;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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

import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.jgraph.view.GraphPresenter;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.helper.UIHelperRegistry;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.ListPresenter;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataList;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Default0DResultPresenter extends SearchResultPresenter {
	
	public static final int SUPPORTED_DIMENSIONS = 0;
	
	protected ListPresenter listPresenter;
	protected AWTPresenter detailsPresenter;
	
	protected DataList<? extends Object> listWrapper;
	
	protected JTextArea infoLabel;
	protected JSplitPane splitPane;

	public Default0DResultPresenter() {
		buildContentPanel();
	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#getSupportedDimensions()
	 */
	@Override
	public int getSupportedDimensions() {
		return SUPPORTED_DIMENSIONS;
	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#buildContentPanel()
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
		splitPane.addComponentListener(getHandler());
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
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#refresh()
	 */
	@Override
	public void refresh() {
		if(listWrapper==null) {
			return;
		}
		
		if(listPresenter!=null) {
			//listPresenter.getPresentingComponent().repaint();
			listPresenter.update();
		}
	}
	
	protected DataList<? extends Object> createListWrapper(SearchResult searchResult) {
		if(searchResult instanceof SentenceDataList) {
			return new SearchResultSentenceDataListWrapper(searchResult);
		} else {
			return new SearchResultListWrapper<>(searchResult);
		}
	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#displayResult()
	 */
	@Override
	protected void displayResult() {
		if(searchResult==null) {
			showInfo(null);
			return;
		}
		
		listWrapper = createListWrapper(searchResult);
		ContentType entryType = listWrapper.getContentType();
		
		// Ensure list presenter
		ListPresenter listPresenter = this.listPresenter;
		if(listPresenter==null || !PresenterUtils.presenterSupports(listPresenter, listWrapper)) {
			listPresenter = UIHelperRegistry.globalRegistry().findHelper(ListPresenter.class, listWrapper);
		}
		
		// Ensure details presenter
		AWTPresenter detailsPresenter = this.detailsPresenter;
		if(detailsPresenter==null || !PresenterUtils.presenterSupports(detailsPresenter, entryType)) {
			// Try graph presenter first
			detailsPresenter = UIHelperRegistry.globalRegistry().findHelper(GraphPresenter.class, entryType, true, false);
			if(detailsPresenter==null) {
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
			listPresenter.present(listWrapper, options);
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
		
		/*try {
			displaySelectedData();
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to present selected item", e); //$NON-NLS-1$

			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.presentationFailed", entryType.getId()); //$NON-NLS-1$
			showDetailInfo(text);
			return;
		}*/
		
		if(listWrapper.size()>0) {
			listPresenter.getSelectionModel().setSelectionInterval(0, 0);
		} else {
			listPresenter.getSelectionModel().clearSelection();
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
		options.putAll(getOptions());
		
		detailsPresenter.present(selectedObject, options);
	}
	
	protected class Handler0D extends Handler implements ListSelectionListener, ComponentListener {

		protected boolean trackResizing = true;
		
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

		/**
		 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentResized(ComponentEvent e) {
			if(!trackResizing) {
				return;
			}
			
			int height = splitPane.getHeight();
			if(height==0) {
				return;
			}
			
			splitPane.setDividerLocation(Math.max(height/2, height-100));
			
			trackResizing = false;
		}

		/**
		 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentHidden(ComponentEvent e) {
			trackResizing = true;
		}

		/**
		 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentMoved(ComponentEvent e) {
			// no-op
		}

		/**
		 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentShown(ComponentEvent e) {
			// no-op
		}
	}
}
