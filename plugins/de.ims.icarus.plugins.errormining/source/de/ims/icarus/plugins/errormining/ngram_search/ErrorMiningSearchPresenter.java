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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.errormining.ngram_search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.errormining.DependencyItemInNuclei;
import de.ims.icarus.plugins.errormining.DetailedNGramSentenceDataList;
import de.ims.icarus.plugins.errormining.ErrorMiningView;
import de.ims.icarus.plugins.errormining.ItemInNuclei;
import de.ims.icarus.plugins.errormining.ngram_tools.NGramDataList;
import de.ims.icarus.plugins.errormining.ngram_tools.NGramDataListDependency;
import de.ims.icarus.plugins.jgraph.view.GraphPresenter;
import de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.result.AbstractSearchResult;
import de.ims.icarus.ui.GridBagUtil;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.helper.UIHelperRegistry;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.ListPresenter;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataList;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class ErrorMiningSearchPresenter extends SearchResultPresenter {

	
	//stuff for ngram visualization
	protected JList<Object> ngramList;
	protected NGramResultViewListModel ngramListModel;
	protected NGramResultViewListCellRenderer ngramListRenderer;
	
	//stuff for detailed distributed visualization
	protected JTable ngramTable;
	protected NGramResultViewTableModel ngramTableModel;
	
	private JLabel header;
	private JLabel infoLabel;
	
	private JScrollPane scrollPane;
	private JScrollPane scrollPaneDetailed;
	
	private JLabel nucleiCount;
	private JLabel nucleiName;
	
	protected JPanel headerPanel;
	protected JButton sortAsc;
	protected JButton sortDesc;
	
	
	protected JTabbedPane tabbedPane;
	protected ListPresenter listPresenter;
	protected AWTPresenter detailsPresenter;
	
	protected JSplitPane splitPane;
	
	
	//result stuff	
	protected Map<String,ArrayList<DependencyItemInNuclei>> nGramResultDependency;
	protected NGramDataListDependency ngListDependency;
	
	
	protected Map<String,ArrayList<ItemInNuclei>> nGramResult;
	protected NGramDataList ngList;
	
	
	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#getSupportedDimensions()
	 */
	@Override
	public int getSupportedDimensions() {
		return 0;
	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#displayResult()
	 */
	@Override
	protected void displayResult() {
		if (searchResult == null){
			showDefaultInfo();
			return;
		}
		
		nGramResult = (Map<String, ArrayList<ItemInNuclei>>) searchResult.getProperty("COMPLETE_NGRAM");
		//System.out.println("SIZE " + nGramResult.size());
		
		if(ngramListModel == null){
			ngramListModel = new NGramResultViewListModel();
		}
		
		if (nGramResult != null) {
			ngramListModel.reload();
		}
		
		if (scrollPane != null) {
			scrollPane.setViewportView(ngramList);
		}
		

	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#refresh()
	 */
	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#buildContentPanel()
	 */
	@Override
	protected void buildContentPanel() {
		//ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		
		// Load actions
		URL actionLocation = ErrorMiningView.class
				.getResource("errormining-view-actions.xml"); //$NON-NLS-1$
		if (actionLocation == null)
			throw new CorruptedStateException(
					"Missing resources: errormining-view-actions.xml"); //$NON-NLS-1$

		try {
			getActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(contentPanel, e);
			return;
		}
		
		tabbedPane = new JTabbedPane();
		contentPanel = new JPanel(new BorderLayout());
		
		JPanel overviewPanel = new JPanel(new BorderLayout()); 
		
		
		// Header label
		header = new JLabel(""); //$NON-NLS-1$
		header.setBorder(new EmptyBorder(3, 5, 10, 20));
		header.setFont(header.getFont().deriveFont(header.getFont().getSize2D() + 2));

		
		// Info label
		infoLabel = new JLabel();
		infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		infoLabel.setVerticalAlignment(SwingConstants.TOP);
		ResourceManager.getInstance().getGlobalDomain()
				.prepareComponent(infoLabel,"plugins.errormining.nGramResultView.notAvailable", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);		
			

		
		// Description Scrollpane
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);	
		UIUtil.defaultSetUnitIncrement(scrollPane);
		scrollPane.setPreferredSize(new Dimension(400, 400));
		
		
		//######
		nucleiCount = new JLabel();
		nucleiName = new JLabel();

		JPanel detailedView = new JPanel();
		detailedView.setLayout(new GridLayout(1, 2));
		detailedView.add(nucleiName);
		detailedView.add(nucleiCount);

		
		// Detailed Scrollpane
		scrollPaneDetailed = new JScrollPane();
		scrollPaneDetailed.setBorder(null);	
		UIUtil.defaultSetUnitIncrement(scrollPaneDetailed);
		
		JSplitPane jsp = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				scrollPane,
				scrollPaneDetailed);
		
		Dimension minimumSize = new Dimension(300, 150);
		scrollPane.setMinimumSize(minimumSize);
		scrollPaneDetailed.setMinimumSize(minimumSize);
		
		//buttons
		sortAsc = new JButton(IconRegistry.getGlobalRegistry()
				.getIcon("sort_rows_asc_alph.gif")); //$NON-NLS-1$
		sortAsc.setToolTipText(	UIUtil.toSwingTooltip(
					ResourceManager.getInstance()
					.get("plugins.errormining.nGramResultView.sortAscAction.description"))); //$NON-NLS-1$
		sortAsc.setActionCommand("asc"); //$NON-NLS-1$
		sortAsc.addActionListener(getHandler());
		
		sortDesc = new JButton(IconRegistry.getGlobalRegistry()
				.getIcon("sort_rows_desc_alph.gif")); //$NON-NLS-1$
		sortDesc.setToolTipText(	UIUtil.toSwingTooltip(
				ResourceManager.getInstance()
				.get("plugins.errormining.nGramResultView.sortDescAction.description"))); //$NON-NLS-1$
		sortDesc.setActionCommand("desc"); //$NON-NLS-1$
		sortDesc.addActionListener(getHandler());
		
		headerPanel = new JPanel(new GridBagLayout());
		headerPanel.add(header, GridBagUtil.makeGbc(0, 0, 1, 1, 1));
		headerPanel.add(sortAsc, GridBagUtil.makeGbc(1, 0, 1, 1, 0));
		headerPanel.add(sortDesc, GridBagUtil.makeGbc(2, 0, 1, 1, 0));
		
		overviewPanel.add(headerPanel, BorderLayout.NORTH);
		overviewPanel.add(jsp, BorderLayout.CENTER);
		
		
		// TODO needed?
		// header.setText("Found " + nGramResult.size() + " NGrams"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Create and initialize JList
		if(ngramListModel == null){
			ngramListModel = new NGramResultViewListModel();
		}
		ngramList = new JList<Object>(ngramListModel);
		ngramList.setBorder(UIUtil.defaultContentBorder);
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ngramList.setSelectionModel(selectionModel);
		ngramList.addListSelectionListener(getHandler());
		ngramList.addMouseListener(getHandler());
		ngramList.getModel().addListDataListener(getHandler());	

		ngramListRenderer = new NGramResultViewListCellRenderer();		
		
		ngramList.setCellRenderer(ngramListRenderer);
		
		scrollPane.setViewportView(ngramList);		
		
		//TODO Detailed Stuff
		nucleiCount = new JLabel(ResourceManager.getInstance()
				.get("plugins.errormining.labels.NucleiCount")); //$NON-NLS-1$
		nucleiName = new JLabel(ResourceManager.getInstance()
						.get("plugins.errormining.labels.NucleiName")); //$NON-NLS-1$
		
		ngramTableModel = new NGramResultViewTableModel();
		ngramTable = new JTable(ngramTableModel);		
		scrollPaneDetailed.setViewportView(ngramTable);
		
		
		
		tabbedPane.addTab("Overview", overviewPanel);
		
		
		//second view
		JPanel graphOutlinePanel = new JPanel(new BorderLayout());
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setContinuousLayout(false);
		splitPane.setDividerSize(5);
		splitPane.setBorder(null);
		splitPane.setResizeWeight(1);
		splitPane.addComponentListener(getHandler());
		
		graphOutlinePanel.add(splitPane, BorderLayout.CENTER);
		
		tabbedPane.addTab("Detail", graphOutlinePanel);
		
		
		
		
		//add all stuff back to contentPanel
		contentPanel.add(tabbedPane);
		
		//showDefaultInfo();
		
		registerActionCallbacks();
		refreshActions();
		
		displayResult();

	}
	
	
	private void showDefaultInfo() {
		scrollPane.setViewportView(infoLabel);
		header.setText(""); //$NON-NLS-1$	
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
			//showDetailInfo(null);
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
			//showInfo(null);
		}
	}
	
	
	private void showDetails(SentenceDataList sentenceList){
		// Ensure list presenter
		ListPresenter listPresenter = this.listPresenter;
		if(listPresenter==null || !PresenterUtils.presenterSupports(listPresenter, sentenceList)) {
			listPresenter = UIHelperRegistry.globalRegistry().findHelper(ListPresenter.class, sentenceList);
		}
		
		ContentType entryType = sentenceList.getContentType();
		
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
			return;
		}
		
		// Now present data
		try {
			listPresenter.present(sentenceList, options);
		} catch (UnsupportedPresentationDataException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to present data list: "+sentenceList, e); //$NON-NLS-1$
			return;
		}
		
		setListPresenter(listPresenter);
		setDetailPresenter(detailsPresenter);

		
		if(sentenceList.size()>0) {
			listPresenter.getSelectionModel().setSelectionInterval(0, 0);
		} else {
			listPresenter.getSelectionModel().clearSelection();
		}
		
		tabbedPane.setSelectedIndex(1);
	}


	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#createHandler()
	 */
	@Override
	protected Handler createHandler() {
		return new HandlerErrorMining();
	}

	@Override
	protected HandlerErrorMining getHandler() {
		return (HandlerErrorMining) super.getHandler();
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


	
	//	protected String getNuclei(String[] s, String tag) {
	//
	//		for(int i = 0; i < s.length; i++){
	//			if(isNuclei(s[i])){
	//				ArrayList<ItemInNuclei>  iin = nGramResult.get(s[i]);
	//				
	//				if(internCount == tag){
	//					return s[i];
	//				}
	//			}			
	//		}
	//		return null;
	//
	//	}
		
		
		
		
		//	protected String getNuclei(String[] s, String tag) {
	//
	//		for(int i = 0; i < s.length; i++){
	//			if(isNuclei(s[i])){
	//				ArrayList<ItemInNuclei>  iin = nGramResult.get(s[i]);
	//				
	//				if(internCount == tag){
	//					return s[i];
	//				}
	//			}			
	//		}
	//		return null;
	//
	//	}
		
		
		
		
		/***
		 * Check if key is a valid uniGram or if its an added Nuclei in Step x or if its
		 * none of that. Used for Highlightning.
		 * @param inputNGram
		 * @param lb
		 * @param rb
		 */
		
		protected boolean isNuclei(String key) {
	
			if (nGramResult.containsKey(key)){
				//not found = color orange
				return true;
			}
			
			//not found = color black
			return false;
		}

		//	protected String getNuclei(String[] s, String tag) {
		//
		//		for(int i = 0; i < s.length; i++){
		//			if(isNuclei(s[i])){
		//				ArrayList<ItemInNuclei>  iin = nGramResult.get(s[i]);
		//				
		//				if(internCount == tag){
		//					return s[i];
		//				}
		//			}			
		//		}
		//		return null;
		//
		//	}
			
			
			
			
			/**
			 * 
			 * @param iin
			 * @return
			 */
			protected Object sentenceOccurences(ItemInNuclei iin) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < iin.getSentenceInfoSize(); i++){
					//FIXME -1 needed?
					sb.append(iin.getSentenceInfoAt(i).getSentenceNr());
					if (i < iin.getSentenceInfoSize()){
						sb.append(", "); //$NON-NLS-1$
					}			
				}
				return sb.toString();		
			}

		/**
		 * 
		 * @param iin
		 * @return
		 */
		protected Object getNucleis(ItemInNuclei iin) {		
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < iin.getSentenceInfoSize(); i++){			
				for(int n  = 0; n < iin.getSentenceInfoAt(i).getNucleiIndexListSize(); n++){
					sb.append(iin.getSentenceInfoAt(i).getNucleiIndexListAt(n));
					if (n < iin.getSentenceInfoAt(i).getNucleiIndexListSize()-1){
						sb.append(", "); //$NON-NLS-1$
					}
				}
				
				if (i < iin.getSentenceInfoSize()-1){
					sb.append(", "); //$NON-NLS-1$
				}
			}
	
			return sb.toString();		
		}



	protected class HandlerErrorMining extends Handler implements ActionListener, 
	ListSelectionListener, EventListener, ListDataListener, ComponentListener {
		
		protected boolean trackResizing = true;
		
		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent me) {
		    if (me.getClickCount() == 2) {
		        int index = ngramList.locationToIndex(me.getPoint());
		        //System.out.println("Double clicked on Item " + index);
		        
		        ngramListModel.getElementAt(index);
		        //System.out.println(ngramListModel.getElementAt(index));
		        String key = (String) ngramListModel.getElementAt(index);	        

		        showDetails(createDetailList(key));
		        
		     }
		}
		

		/**
		 * @param key
		 * @return 
		 * @return
		 */
		private SentenceDataList createDetailList(String key) {
			//TODO add later presentation		
			
			List<SentenceData> sentenceDataDetailedList = new ArrayList<SentenceData>();

			//System.out.println("selectedKey " + key);
			ArrayList<ItemInNuclei> iinList = nGramResult.get(key);			
		
			DataList<?> dl = ((AbstractSearchResult)searchResult).getTarget();
			for(int i = 0; i < iinList.size(); i++){
				ItemInNuclei iin = iinList.get(i);
				
				for (int s = 0; s < iin.getSentenceInfoSize(); s++){
					SentenceData sentenceData =
							(SentenceData) dl.get(iin.getSentenceInfoAt(s).getSentenceNr()-1);
					//System.out.println(sentenceData.getText() + "TEXT");
					sentenceDataDetailedList.add(sentenceData);
					
				}
			}
			
			DetailedNGramSentenceDataList dsdl = 
					new DetailedNGramSentenceDataList(sentenceDataDetailedList);
			return dsdl;
			
		}


		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			// TODO Auto-generated method stub
			
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			
			if (e.getSource() == ngramList){
				Object selectedObject = ngramList.getSelectedValue();
				ngramTableModel.reload((String) selectedObject);	
			} else {
				try {
					displaySelectedData();
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to handle change in selection: "+e, ex); //$NON-NLS-1$
				}
			}
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {

			//System.out.println(e.getSource() + e.getActionCommand());
			if (e.getActionCommand().equals("asc")){ //$NON-NLS-1$
				//System.out.println("a");
				ngramListModel.setSort(true);
			}
			
			if (e.getActionCommand().equals("desc")){ //$NON-NLS-1$
				//System.out.println("d");
				ngramListModel.setSort(false);
			}
			
		}

		/**
		 * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void contentsChanged(ListDataEvent e) {
//			System.out.println("Result NGRAM-List ContentsChanged: "  //$NON-NLS-1$
//						+ e.getIndex0() +    ", " + e.getIndex1()); //$NON-NLS-1$
			ngramList.clearSelection();		
		}

		/**
		 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void intervalAdded(ListDataEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		/**
		 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void intervalRemoved(ListDataEvent arg0) {
			// TODO Auto-generated method stub
			
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
	
	
	/**
	 * Stuff for List Visualization
	 * 
	 * */
	class NGramResultViewListModel extends AbstractListModel<Object> {

		private static final long serialVersionUID = 7917508880767604173L;
		
		protected boolean ascending = true;

		Object[] keys;
		
		public void setSort(boolean newSort){
			if (newSort != ascending){
				ascending = newSort;
				reload();
				fireContentsChanged(this, 0, keys.length);
			}
		}
	
		
		/**
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		@Override
		public Object getElementAt(int index) {
			return keys[index];
		}

		/**
		 * @see javax.swing.ListModel#getSize()
		 */
		@Override
		public int getSize() {
			return keys.length;
		}


		public void reload() {
			//keys = nGramResult.keySet().toArray();
			
			List<Object> myList;
			if (nGramResult != null){
				myList = new ArrayList<Object>(nGramResult.keySet());
			} else {
				myList = new ArrayList<Object>(nGramResultDependency.keySet());
			}
			
			if (ascending){
				//System.out.println("normal");
				//Collections.sort(myList);
			} else {
				//System.out.println("reverse");
				Collections.reverse(myList);
			}
			
			keys = myList.toArray();
			
			fireContentsChanged(this, 0, Math.max(getSize()-1, 0));
			
		}
	}
	
	class NGramResultViewListCellRenderer extends JLabel 
									implements ListCellRenderer<Object> {
		

		private static final long serialVersionUID = 6942839834724864784L;


		public NGramResultViewListCellRenderer(){
	         setOpaque(true);
	     }


		  public Component getListCellRendererComponent(JList<?> list, Object value, int index,
			      boolean isSelected, boolean cellHasFocus) {
			String[] s = ((String) value).split(" "); //$NON-NLS-1$
			

			String text =	"<html>"  //$NON-NLS-1$
							+ (index + 1) + ") "  //$NON-NLS-1$
							+ " (" + s.length + "-Gram) "  //$NON-NLS-1$//$NON-NLS-2$
							+ colorString(s)
							+ "</html>"; //$NON-NLS-1$

		      if (isSelected) {
		          setBackground(list.getSelectionBackground());
		         // text.setForeground(list.getSelectionForeground());
		        } else {
		          // the color returned from list.getBackground() is pure white
		          setBackground(list.getBackground());
		          // THIS works -- but is obviously hardcoded
		          // setBackground(Color.WHITE);
		          //text.setForeground(list.getForeground());
		        }
			setText(text);
			
			return this;
		  }


		/**
		 * @param s
		 * @return
		 */
		private String colorString(String[] s) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < s.length ; i++){
				if (isNuclei(s[i])){
					Color nuclei = ConfigRegistry.getGlobalRegistry()
							.getColor("plugins.errorMining.highlighting.nucleiHighlight"); //$NON-NLS-1$
					String hex = "#"+Integer.toHexString(nuclei.getRGB()).substring(2); //$NON-NLS-1$
					//System.out.println(hex);
					sb.append("<font color=" +  hex + ">"); //$NON-NLS-1$ //$NON-NLS-2$
					sb.append(s[i]);
					sb.append("</font>"); //$NON-NLS-1$
				} else{
					sb.append(s[i]);
				}
				
				if ( i < s.length -1){
					sb.append(" "); //$NON-NLS-1$
				}
			}
			return sb.toString();
		}
	}
	
	
	
	class NGramResultViewTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -196214722892607695L;
		
		
		protected ArrayList<ItemInNuclei> iinList = null;
		protected Map<Integer, String> tmpMap;
		
		String[] keySplitted = null;
		
		boolean multinuclei = false;
		int itemsAdded;
		
		public NGramResultViewTableModel(){
		}
		
		public void reload (String key){
			if (key == null) {
				iinList = null;
			} else {
				//iinList = nGramResult.get(key);
				iinList = new ArrayList<>();
				tmpMap = new LinkedHashMap<>();
				
				this.keySplitted = key.split(" ");	 //$NON-NLS-1$
				
				itemsAdded = 0;
				
				for(int i = 0; i < keySplitted.length; i++){
					if(isNuclei(keySplitted[i])){
						iinList.addAll(nGramResult.get(keySplitted[i]));
						
						/* size sets rowcount in table, (e.g. size = 2 row 0 and row 1
						 * must get assigned with keySplitted[i] tag.
						 * Therefore put  tmpMap the keys with the used row/index
						 */
						
						for(int j = 0 ; j < nGramResult.get(keySplitted[i]).size(); j++){
							tmpMap.put(itemsAdded, keySplitted[i]);
							itemsAdded++;							
						}

					}					
				}
				
				if (itemsAdded > 1) multinuclei = true;
				
			}
			fireTableDataChanged();
		}
		
		
		/**
		 * @see javax.swing.table.TableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}
		

		/**
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return 6;
		}
		
		
		/**
		 * @see javax.swing.table.TableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int columnIndex) {

		      switch (columnIndex) {
		            case 1: return ResourceManager.getInstance().get(
		            		"plugins.errormining.labels.Tag"); //$NON-NLS-1$
		            case 2: return ResourceManager.getInstance().get(
		            		"plugins.errormining.labels.Count"); //$NON-NLS-1$
		            case 3: return ResourceManager.getInstance().get(
		            		"plugins.errormining.labels.NucleiCount"); //$NON-NLS-1$
		            case 4: return ResourceManager.getInstance().get(
		            		"plugins.errormining.labels.NucleiIndex"); //$NON-NLS-1$
		            case 5: return ResourceManager.getInstance().get(
		            		"plugins.errormining.labels.SentenceNR"); //$NON-NLS-1$
		            default: break;
		        }
		        return null;
		}
		

		/**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
//			int count = 0;
//			if (iinList != null){
//				for(int i = 0; i < iinList.size(); i++){
//					int ns = iinList.get(i).getSentenceInfoAt(0).getNucleiIndexListSize();
//					count = iinList.size() * ns;
//					//System.out.println("List: " + iinList.size() + count);
//				}
//			}
//			return count;
			return iinList==null ? 0 : iinList.size();
			
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (iinList == null) {
				return null;
			}			

			ItemInNuclei iin = iinList.get(rowIndex);			
			
			
			int nucleiCount = iin.getSentenceInfoAt(0).getNucleiIndexListSize();
			
//			System.out.println(multinuclei + " MULTI" 
//								+ " " + getNucleis(iin));
			
//			System.out.println("PosTag: " + iin.posTag +  
//								"RowIndex: " + rowIndex +
//								"ColIndex: " + columnIndex);
//			System.out.println(tmpMap.toString());
			
			//TODO really needed? better check nuclei size instead?!
//			if (iin.getSentenceInfoSize() == 1) {
			if (!multinuclei) {				
				switch (columnIndex) {
				case 0:
					return tmpMap.get(rowIndex);//keySplitted[nucleiGenIndex-start];
				case 1:
					return iin.getPosTag(); 
				case 2:
					return iin.getCount();
				case 3:
					return nucleiCount;
				case 4:
					return getNucleis(iin);
				case 5:
					return sentenceOccurences(iin);
				default:
					break;
				}
			}
			
				
			if (multinuclei) {
				switch (columnIndex) {
				case 0:
//					System.out.println("Items " + itemsAdded);
//					String s = getNuclei(keySplitted, iin.getPosTag());
//					nuclei++;
//					System.out.println(nuclei);
					return tmpMap.get(rowIndex);
				case 1:
					return iin.getPosTag();
				case 2:
					return iin.getCount();
				case 3:
					return iin.getSentenceInfoAt(0).getNucleiIndexListSize();
				case 4:
					return getNucleis(iin);
				case 5:
					return sentenceOccurences(iin);
				default:
					break;
				}
			}
			return null;
		}
		
		
	}
	
	
//	protected String getNuclei(String[] s, String tag) {
//
//		for(int i = 0; i < s.length; i++){
//			if(isNuclei(s[i])){
//				ArrayList<ItemInNuclei>  iin = nGramResult.get(s[i]);
//				
//				if(internCount == tag){
//					return s[i];
//				}
//			}			
//		}
//		return null;
//
//	}

	

	

}
