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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.language.dependency.annotation.AnnotatedDependencyData;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.errormining.DependencyItemInNuclei;
import de.ims.icarus.plugins.errormining.DependencySentenceInfo;
import de.ims.icarus.plugins.errormining.DetailedNGramSentenceDataList;
import de.ims.icarus.plugins.errormining.ItemInNuclei;
import de.ims.icarus.plugins.errormining.annotation.NGramAnnotation;
import de.ims.icarus.plugins.errormining.ngram_tools.CompareStringLength;
import de.ims.icarus.plugins.errormining.ngram_tools.DependencyNucleusCache;
import de.ims.icarus.plugins.errormining.ngram_tools.NGramDataList;
import de.ims.icarus.plugins.errormining.ngram_tools.NGramDataListDependency;
import de.ims.icarus.plugins.errormining.ngram_tools.NGramParameters;
import de.ims.icarus.plugins.jgraph.view.GraphPresenter;
import de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.result.AbstractSearchResult;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.helper.UIHelperRegistry;
import de.ims.icarus.ui.table.TableColumnAdjuster;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.ListPresenter;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataList;
import de.ims.icarus.util.id.Identity;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class ErrorMiningSearchPresenter extends SearchResultPresenter {
	
	//general stuff
	protected JTabbedPane tabbedPane;
	
	//stuff for ngram visualization
	protected JList<Object> ngramList;
	protected NGramResultViewListModel ngramListModel;
	protected NGramResultViewListCellRenderer ngramListRenderer;
	
	//stuff for detailed distributed visualization
	protected JTable ngramTable;
	protected NGramResultViewTableModel ngramTableModel;
	protected TableColumnAdjuster ngramTableAdjuster;
	protected NGramResultViewTableCellRenderer ngramTableRenderer;

	
	private JScrollPane scrollPane;
	private JScrollPane scrollPaneDetailed;
	private JScrollPane scrollPaneStats;
	private JScrollPane scrollPaneStatsDetailed;
	
	//button filter stuff
	protected JSpinner lowerBound;
	protected SpinnerNumberModel lbm;
	protected JSpinner upperBound;
	protected SpinnerNumberModel ubm;
	protected JLabel resultCounter;
	protected JTextField textFilterField;
	protected JToolBar toolBarOverview;
	

	//detail stuff
	protected ListPresenter listPresenter;
	protected AWTPresenter detailsPresenter;	
	protected JSplitPane splitpaneDetails;
	
	
	//stats stuff
	protected JToolBar toolBarStatistic;
	protected JSpinner statsSpinner;
	protected SpinnerNumberModel sbm;	
	protected JTextField statisticTextFilterField;
	protected JLabel statisticCounter;
	protected JSplitPane splitpaneStats;
	protected JList<Object> statisticList;
	protected StatisticListModel statisticListModel;
	protected JTable statisticTable;
	protected StatisticTableModel statisticTableModel;
	protected TableColumnAdjuster statisticTableAdjuster;
	protected Map<String, List<StatsData>> statsResult;
	protected Map<String, List<StatsData>> statsResultFiltered;
	
	
	//result stuff dependency
	protected Map<String,ArrayList<DependencyItemInNuclei>> nGramResultDependency;
	protected Map<String,ArrayList<DependencyItemInNuclei>> nGramResultFilteredDependency;
	protected NGramDataListDependency ngListDependency;
	
	
	protected Map<String,ArrayList<ItemInNuclei>> nGramResult;
	protected Map<String,ArrayList<ItemInNuclei>> nGramResultFiltered;
	protected NGramDataList ngList;
	
	
	private int minimumGramsize;
	private int maximumGramsize;
	
	// 1 = dependency; 0 = part-of-speech
	private int searchMode;
	
	private static final Pattern numberPattern = Pattern.compile("^[0-9]"); //$NON-NLS-1$
	private static final int minDependencyGram = 2;

	
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
	@SuppressWarnings("unchecked")
	@Override
	protected void displayResult() {
		if (searchResult == null){
			//showDefaultInfo();
			return;
		}
		
		searchMode = (int) searchResult.getProperty("MODE"); //$NON-NLS-1$
		
		//TODO Debug
//		System.out.println("TEST"
//				//+ "\n"+searchResult.getTotalHitCount()
//				+ "\n"+searchResult.getRawEntry(0) +" "+ searchResult.getRawEntry(0).getIndex()
//				+ "\n"+searchResult.getRawEntry(1) +" "+ searchResult.getRawEntry(1).getIndex()
//				+ "\n"+searchResult.getRawEntry(2) +" "+ searchResult.getRawEntry(2).getIndex()
//				+ "\n"+searchResult.getRawEntry(3) +" "+ searchResult.getRawEntry(3).getIndex()
//				+ "\n"+searchResult.getRawEntry(5) +" "+ searchResult.getRawEntry(5).getIndex()
//				);
		minimumGramsize = searchResult.getSource().getParameters().getInteger(NGramParameters.GRAMS_GREATERX);
		maximumGramsize = Math.max(searchResult.getSource().getParameters().getInteger(NGramParameters.NGRAM_RESULT_LIMIT),
									(int) searchResult.getProperty("LARGEST_NGRAM")); //$NON-NLS-1$
		initializeSpinners();
		
		
		if(isPoSErrorMiningResult()){
			//System.out.println("Part-Of-Speech Result");
			nGramResult = (Map<String, ArrayList<ItemInNuclei>>) searchResult.getProperty("COMPLETE_NGRAM"); //$NON-NLS-1$
			//no limitations
			if(minimumGramsize == 0){
				nGramResultFiltered = nGramResult;
			} else {
				generateFilteredResult();
			}
		} else {
			//System.out.println("Dependency Result");
			nGramResultDependency = (Map<String, ArrayList<DependencyItemInNuclei>>) searchResult.getProperty("COMPLETE_NGRAM"); //$NON-NLS-1$
			//no limitations
			if(minimumGramsize == 0){
				nGramResultFilteredDependency = nGramResultDependency;
			} else {
				generateFilteredDependencyResult();
			}
		};		
		

		
		if(ngramListModel == null){
			ngramListModel = new NGramResultViewListModel();
		}
		
		if(statisticListModel == null){
			statisticListModel = new StatisticListModel();
		}
		
		if (nGramResult != null || nGramResultDependency != null) {
			ngramListModel.reload();
			if(ngramList != null){
				ngramList.setPrototypeCellValue(ngramListModel.getLargestElement());
			}
		}
		
		if (scrollPane != null) {
			scrollPane.setViewportView(ngramList);
		}
		
		
		//switch to first dab  (overview) when selecting new/other result!
		if(tabbedPane != null){
			if(isPoSErrorMiningResult()) {
				createStatistic(1);
			} else {
				createStatisticDependency(2);
			}
			
			tabbedPane.setSelectedIndex(0);
		}

	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#refresh()
	 */
	@Override
	public void refresh() {
		// TO DO Auto-generated method stub
	}
	
	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#buildContentPanel()
	 */
	@Override
	protected void buildContentPanel() {
		
		//general visualization stuff
		tabbedPane = new JTabbedPane();	
		contentPanel = new JPanel(new BorderLayout());
		
		
		//--------------------------------------------------------
		//first view
		JPanel overviewPanel = new JPanel(new BorderLayout()); 

		
		// Description Scrollpane
		scrollPane = new JScrollPane();
		UIUtil.defaultSetUnitIncrement(scrollPane);
		scrollPane.setPreferredSize(new Dimension(400, 400));
		scrollPane.setBorder(UIUtil.emptyBorder);

		
		// Detailed Scrollpane
		scrollPaneDetailed = new JScrollPane();
		scrollPaneDetailed.setBorder(UIUtil.emptyBorder);
		UIUtil.defaultSetUnitIncrement(scrollPaneDetailed);
		
		JSplitPane jsp = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				scrollPane,
				scrollPaneDetailed);
		
		Dimension minimumSize = new Dimension(300, 150);
		scrollPane.setMinimumSize(minimumSize);
		scrollPaneDetailed.setMinimumSize(minimumSize);
				
		//addfilter panel
		overviewPanel.add(buildOverviewToolbar(), BorderLayout.NORTH);
		overviewPanel.add(jsp, BorderLayout.CENTER);
	
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
		ngramList.setPrototypeCellValue(ngramListModel.getLargestElement());		
		ngramListRenderer = new NGramResultViewListCellRenderer();		
		ngramList.setCellRenderer(ngramListRenderer);
		
		scrollPane.setViewportView(ngramList);		
		

		ngramTableModel = new NGramResultViewTableModel();
		ngramTable = new JTable(ngramTableModel);
		ngramTable.setBorder(UIUtil.emptyBorder);
		ngramTable.setIntercellSpacing(new Dimension(3,3));
		ngramTable.setDefaultRenderer(String.class, new NGramResultViewTableCellRenderer());
		ngramTable.addMouseListener(getHandler());
		ngramTableAdjuster = new TableColumnAdjuster(ngramTable);
		ngramTableAdjuster.setOnlyAdjustLarger(false);
		scrollPaneDetailed.setViewportView(ngramTable);
				
		
		tabbedPane.addTab(ResourceManager.getInstance().get(
				"plugins.errormining.errorMiningSearchPresenter.tab.overview"), //$NON-NLS-1$
				IconRegistry.getGlobalRegistry().getIcon("container_obj.gif"), //$NON-NLS-1$
				overviewPanel);
		
		//--------------------------------------------------------
		//second view (detailed - after click on item in overview)
		JPanel graphOutlinePanel = new JPanel(new BorderLayout());
		splitpaneDetails = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitpaneDetails.setContinuousLayout(true);
		splitpaneDetails.setDividerSize(5);
		splitpaneDetails.setBorder(null);
		splitpaneDetails.setResizeWeight(1);
		splitpaneDetails.addComponentListener(getHandler());
		
		graphOutlinePanel.add(splitpaneDetails, BorderLayout.CENTER);
		
		tabbedPane.addTab(ResourceManager.getInstance().get(
				"plugins.errormining.errorMiningSearchPresenter.tab.detail"), //$NON-NLS-1$
				IconRegistry.getGlobalRegistry().getIcon("filter_errormining.png"), //$NON-NLS-1$
				graphOutlinePanel);

		
		//----------------------------------------------------------
		//third view (statistics)		
		JPanel statsPanel = new JPanel(new BorderLayout());
		
		splitpaneStats = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitpaneStats.setContinuousLayout(true);
		splitpaneStats.setDividerSize(5);
		splitpaneStats.setBorder(null);
		splitpaneStats.setResizeWeight(0.7);
		splitpaneStats.addComponentListener(getHandler());
		
		 
		scrollPaneStats = new JScrollPane();
		scrollPaneStats.setBorder(UIUtil.emptyBorder);
		UIUtil.defaultSetUnitIncrement(scrollPaneStats);

		statisticTableModel = new StatisticTableModel();
		statisticTable = new JTable(statisticTableModel);
		statisticTable.setIntercellSpacing(new Dimension(3,3));
		statisticTable.setBorder(UIUtil.emptyBorder);
		statisticTable.addMouseListener(getHandler());
		statisticTable.getSelectionModel().addListSelectionListener(getHandler());
		statisticTableAdjuster = new TableColumnAdjuster(ngramTable);
		statisticTableAdjuster.setOnlyAdjustLarger(false);
		
		scrollPaneStats.setViewportView(statisticTable);
		
		if(statisticListModel == null){
			statisticListModel = new StatisticListModel();
		}
		statisticList = new JList<Object>(statisticListModel);
		statisticList.setBorder(UIUtil.defaultContentBorder);
		statisticList.addMouseListener(getHandler());
		DefaultListSelectionModel statsSelectionModel = new DefaultListSelectionModel();
		statsSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		statisticList.setSelectionModel(statsSelectionModel);
		
		scrollPaneStatsDetailed = new JScrollPane();
		scrollPaneStatsDetailed.setBorder(UIUtil.emptyBorder);
		UIUtil.defaultSetUnitIncrement(scrollPaneStatsDetailed);		
		scrollPaneStatsDetailed.setViewportView(statisticList);
		
		splitpaneStats.setLeftComponent(scrollPaneStats);
		splitpaneStats.setRightComponent(scrollPaneStatsDetailed);		
		
		statsPanel.add(buildStatisticToolbar(), BorderLayout.NORTH);
		statsPanel.add(splitpaneStats, BorderLayout.CENTER);
		
		tabbedPane.addTab(ResourceManager.getInstance().get(
				"plugins.errormining.errorMiningSearchPresenter.tab.stats"), //$NON-NLS-1$
				IconRegistry.getGlobalRegistry().getIcon("uddi_registry_cat_node.gif"), //$NON-NLS-1$
				statsPanel);
		
		
		//add all stuff back to contentPanel
		contentPanel.add(tabbedPane);
		
		//showDefaultInfo();
		
		tabbedPane.setSelectedIndex(0);		

		refreshActions();
		
		displayResult();
	}
	
	
	/**
	 * @return
	 */
	private Component buildOverviewToolbar() {	
		//shows filter count
		resultCounter = new JLabel();
		resultCounter.setBorder(UIUtil.defaultContentBorder);
		refreshCount();
				
		//ngram bounds		
		lowerBound = new JSpinner(lbm);
		lowerBound.setMinimumSize(new Dimension(10,20));
		lowerBound.setToolTipText(ResourceManager.getInstance()
					.get("plugins.errormining.nGramResultView.lowerBound.description")); //$NON-NLS-1$
	
		upperBound = new JSpinner(ubm);
		upperBound.setMinimumSize(new Dimension(10,20));
		upperBound.setToolTipText(ResourceManager.getInstance()
					.get("plugins.errormining.nGramResultView.upperBound.description")); //$NON-NLS-1$
		
		upperBound.setBorder(UIUtil.defaultContentBorder);
		
		
		statsSpinner = new JSpinner(sbm);
		statsSpinner.setMinimumSize(new Dimension(10,20));
		statsSpinner.setToolTipText(ResourceManager.getInstance()
					.get("plugins.errormining.nGramResultView.statsSpinner.description")); //$NON-NLS-1$

		textFilterField = new JTextField();
		textFilterField.setMinimumSize(new Dimension(120,20));
		textFilterField.setPreferredSize(new Dimension(120,20));
		textFilterField.setMaximumSize(new Dimension(200,20));
		textFilterField.addActionListener(getHandler());
		UIUtil.createUndoSupport(textFilterField, 10);
		UIUtil.createDefaultTextMenu(textFilterField, true);		
		
		ActionComponentBuilder acb = new ActionComponentBuilder(getActionManager());
		acb.setActionListId("plugins.errormining.nGramResultView.toolBarList"); //$NON-NLS-1$
		acb.addOption("resultCounter", resultCounter); //$NON-NLS-1$
		acb.addOption("textfield", textFilterField); //$NON-NLS-1$
		acb.addOption("lowerBound", lowerBound); //$NON-NLS-1$
		acb.addOption("upperBound", upperBound); //$NON-NLS-1$
		acb.addOption("statsSpinner", statsSpinner); //$NON-NLS-1$
		toolBarOverview = acb.buildToolBar();

		return toolBarOverview;
	}
	
	
	private Component buildStatisticToolbar(){
		
		//shows filter count
		statisticCounter = new JLabel();
		statisticCounter.setBorder(UIUtil.defaultContentBorder);
		refreshStatisticCount();
		
		statisticTextFilterField = new JTextField();
		statisticTextFilterField.setMinimumSize(new Dimension(120,20));
		statisticTextFilterField.setPreferredSize(new Dimension(120,20));
		statisticTextFilterField.setMaximumSize(new Dimension(200,20));
		statisticTextFilterField.addActionListener(getHandler());
		UIUtil.createUndoSupport(statisticTextFilterField, 10);
		UIUtil.createDefaultTextMenu(statisticTextFilterField, true);		
		
		ActionComponentBuilder acb = new ActionComponentBuilder(getActionManager());
		acb.setActionListId("plugins.errormining.nGramResultView.toolBarStatistic"); //$NON-NLS-1$
		acb.addOption("statisticCounter", statisticCounter); //$NON-NLS-1$
		acb.addOption("statisticTextFilterField", statisticTextFilterField); //$NON-NLS-1$
		toolBarStatistic = acb.buildToolBar();

		return toolBarStatistic;
	}
	
	/**
	 * 
	 */
	private void initializeSpinners() {
		int minimum = 1;
		if(!isPoSErrorMiningResult()){
			minimum = minDependencyGram;
		}
		
		if(lbm == null){
			lbm = new SpinnerNumberModel(minimum, //initial value
					minimum, //min
	                maximumGramsize-1, //max
	                1);          //step
		} else {
			//lbm.setMinimum(Math.max(minimumGramsize, minimum));
			lbm.setMaximum(maximumGramsize-1);
			ubm.setValue(minimum);
		}
		
		
		if(ubm == null){
			ubm = new SpinnerNumberModel(maximumGramsize-1, //initial value
			minimum, //min
			maximumGramsize-1, //max
			1);          //step
		} else {
			ubm.setMaximum(maximumGramsize-1);
			ubm.setValue(maximumGramsize-1);
		}
		
		if(sbm == null){
			sbm = new SpinnerNumberModel(minimum, //initial value
			minimum, //min
			maximumGramsize-1, //max
			1);          //step
		}
	}

	private void refreshCount(){	
		if(ngramListModel == null){
			resultCounter.setText("0"); //$NON-NLS-1$
		} else {
			resultCounter.setText((String.valueOf(ngramListModel.getSize())));
		}
	}
	
	/**
	 * 
	 */
	private void refreshStatisticCount() {
		if(statisticTableModel.getRowCount() == 0){
			statisticCounter.setText("0"); //$NON-NLS-1$
		} else {
			statisticCounter.setText((String.valueOf(statisticTableModel.getRowCount())));
		}
		
	}

	private void resetFilters(){
		textFilterField.setText(null);
		lbm.setValue(1);
		ubm.setValue(ubm.getMaximum());		
		doResultFiltering();
	}

//	private void showDefaultInfo() {
//		// scrollPane.setViewportView(infoLabel);
//		//headerInfo.setText("NGrams: " + ngramListModel.getSize()); //$NON-NLS-1$	
//	}

	/**
	 * 
	 */
	private void resetStatisticFilter() {
		statisticTextFilterField.setText(null);
		doStatisticFiltering();
	}

	private void doResultFiltering() {
		TaskManager.getInstance().schedule(new FilterWorker(),
				TaskPriority.DEFAULT, true);
	}
	
	private void doStatisticFiltering() {
		TaskManager.getInstance().schedule(new StatsFilterWorker(),
				TaskPriority.DEFAULT, true);
	}

	
	private void generateFilteredStatistic(){		
		if(!statisticTextFilterField.equals("")){ //$NON-NLS-1$
			statsResultFiltered.clear();
			for (String key : statsResult.keySet()) {
				if (key.contains(statisticTextFilterField.getText())) {
					statsResultFiltered.put(key, statsResult.get(key));
				}
			}
		}else{
			statsResultFiltered.clear();
			statsResultFiltered.putAll(statsResult);
			
		}
	}
	
	/**
	 * 
	 */
	private void generateFilteredResult() {
		
		nGramResultFiltered = new LinkedHashMap<String,ArrayList<ItemInNuclei>>();		
		List<String> tmpKey = new ArrayList<String>(nGramResult.keySet());
		
		for (int i = 0; i < tmpKey.size();i++){
			String key = tmpKey.get(i);
			int currentSize = key.split(" ").length; //$NON-NLS-1$	
			
			//Check if we should also use textfilter
			if(!textFilterField.getText().equals("")){ //$NON-NLS-1$
				if(key.contains(textFilterField.getText())){
					if(currentSize >= minimumGramsize 
							&& currentSize <= maximumGramsize){				
								nGramResultFiltered.put(key, nGramResult.get(key));
					}
				}				
			} else {			
			//correct size?
			if(currentSize >= minimumGramsize 
				&& currentSize <= maximumGramsize){	
					nGramResultFiltered.put(key, nGramResult.get(key));
				}
			}
		}		
		//System.out.println(nGramResultFiltered.keySet());		
	}
	
	
	private void generateFilteredDependencyResult() {
		
		nGramResultFilteredDependency = new LinkedHashMap<String,ArrayList<DependencyItemInNuclei>>();		
		List<String> tmpKey = new ArrayList<String>(nGramResultDependency.keySet());
		
		for (int i = 0; i < tmpKey.size();i++){
			String key = tmpKey.get(i);
			int currentSize = key.split(" ").length; //$NON-NLS-1$	
			
			//Check if we should also use textfilter
			if(!textFilterField.getText().equals("")){ //$NON-NLS-1$
				if(key.contains(textFilterField.getText())){
					if(currentSize >= minimumGramsize 
							&& currentSize <= maximumGramsize){				
								nGramResultFilteredDependency.put(key, nGramResultDependency.get(key));
					}
				}				
			} else {			
			//correct size?
			if(currentSize >= minimumGramsize 
				&& currentSize <= maximumGramsize){	
					nGramResultFilteredDependency.put(key, nGramResultDependency.get(key));
				}
			}
		}		
		//System.out.println(nGramResultFiltered.keySet());		
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
			splitpaneDetails.setLeftComponent(detailsPresenter.getPresentingComponent());
		} else {
			//showDefaultInfo();
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
			
			splitpaneDetails.setRightComponent(listPresenter.getPresentingComponent());
		} else {
			//showInfo(null);
		}
	}
	
	
//	private void showDefaultInfo() {
//		scrollPane.setViewportView(infoLabel);
//		//headerInfo.setText("NGrams: " + ngramListModel.getSize()); //$NON-NLS-1$
//	}

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
		
		//System.out.println("LP ContentType:"+listPresenter.getContentType());
		setListPresenter(listPresenter);
		setDetailPresenter(detailsPresenter);
		
		if(sentenceList.size()>0) {
			listPresenter.getSelectionModel().setSelectionInterval(0, 0);
		} else {
			listPresenter.getSelectionModel().clearSelection();
		}
		
		//autofocus on detail panel
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
	
	protected void displaySelectedStatsData(){
		int selectedRow = statisticTable.getSelectedRow();
		// add correct column!!					
		String key = (String) statisticTable.getModel().getValueAt(selectedRow, 0);
		//System.out.println(key);					
		statisticTableModel.generateListEntryFromString(key);
		statisticList.clearSelection();
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

	
	/***
	 * Check if key is a valid uniGram or if its an added Nuclei in Step x or if
	 * its none of that. Used for Highlightning.
	 * 
	 * @param arrayList
	 * @param inputNGram
	 * @param lb
	 * @param rb
	 */
	protected boolean isNuclei(String key) {		
		//pos
		if(isPoSErrorMiningResult()){		
			if (nGramResult.containsKey(key)) {
				// not found = color orange
				return true;
			}
		}
		
		//dependency
		if(searchMode == 1){			
			if (nGramResultDependency.containsKey(key)) {
				// not found = color orange
				return true;
			}
		}
		
		// not found = color black
		return false;
	}
		
		
	/**
	 * Hack to ensure that only the nucleus / nuclei got a highlight for the
	 * specifc n-gram. Therefore we use the arrayList which contains the
	 * corpusIndex for all sentences which shall be displayed. - Then we check
	 * vor each token if it occured as a unigram. - After this we add the pos
	 * tag if sentencenr match with the one displayed and if the postag is not
	 * in the list. - Finally we check if there is more than one tag for a given
	 * token if so we know that this token must be still a variation nucleus and
	 * should be marked.
	 */
	protected boolean isNucleiList(String key, ArrayList<Integer> arrayList) {
	
		//pos mode
		if (isPoSErrorMiningResult()){
			if (nGramResult.containsKey(key)) {
				ArrayList<ItemInNuclei> arrL = nGramResult.get(key);
				ArrayList<String> tempTag = new ArrayList<String>();
	
				if (arrL != null) {
					for (int i = 0; i < arrL.size(); i++) {
						ItemInNuclei iin = arrL.get(i);
						// System.out.println(iin.getPosTag());
	
						for (int j = 0; j < iin.getSentenceInfoSize(); j++) {
							if (arrayList.contains(iin.getSentenceInfoAt(j)
									.getSentenceNr())) {
								if (!tempTag.contains(iin.getPosTag())) {
									tempTag.add(iin.getPosTag());
								}
							}
						}
					}
				}
	
				// not found = color orange
				if (tempTag.size() > 1) {
					return true;
				}
			}
		} else {
			if (nGramResultDependency.containsKey(key)) {
				ArrayList<DependencyItemInNuclei> arrL = nGramResultDependency
						.get(key);
				ArrayList<String> tempTag = new ArrayList<String>();

				if (arrL != null) {
					for (int i = 0; i < arrL.size(); i++) {
						DependencyItemInNuclei iin = arrL.get(i);
						// System.out.println(iin.getPosTag());

						for (int j = 0; j < iin.getSentenceInfoSize(); j++) {
							if (arrayList.contains(iin.getSentenceInfoAt(j)
									.getSentenceNr())) {
								if (!tempTag.contains(iin.getPosTag())) {
									tempTag.add(iin.getPosTag());
								}
							}
						}
					}
				}

				// not found = color orange
				if (tempTag.size() > 1) {
					return true;
				}
			}
			
		}

		// not found = color black
		return false;
	}
		
			
			
	/**
	 * 
	 * @param iin
	 * @return
	 */
	protected Object sentenceOccurences(ItemInNuclei iin) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < iin.getSentenceInfoSize(); i++) {
			sb.append(iin.getSentenceInfoAt(i).getSentenceNr());
			if (i < iin.getSentenceInfoSize() - 1) {
				sb.append(", "); //$NON-NLS-1$
			}
		}
		return sb.toString();
	}
	
	protected Object sentenceOccurences(DependencyItemInNuclei iin) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < iin.getSentenceInfoSize(); i++) {
			sb.append(iin.getSentenceInfoAt(i).getSentenceNr());
			if (i < iin.getSentenceInfoSize() - 1) {
				sb.append(", "); //$NON-NLS-1$
			}
		}
		return sb.toString();
	}
	
	
	protected Object sentenceNucleiOccurences(DependencyItemInNuclei iin) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < iin.getSentenceInfoSize(); i++) {
			sb.append(iin.getSentenceInfoAt(i).getSentenceNr());
			if (i < iin.getSentenceInfoSize() - 1) {
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
		for (int i = 0; i < iin.getSentenceInfoSize(); i++) {
			for (int n = 0; n < iin.getSentenceInfoAt(i)
					.getNucleiIndexListSize(); n++) {
				sb.append(iin.getSentenceInfoAt(i).getNucleiIndexListAt(n));
				if (n < iin.getSentenceInfoAt(i).getNucleiIndexListSize() - 1) {
					sb.append(", "); //$NON-NLS-1$
				}
			}
			if (i < iin.getSentenceInfoSize() - 1) {
				sb.append(", "); //$NON-NLS-1$
			}
		}
		return sb.toString();
	}
	
	protected Object getNucleiDependency(DependencyItemInNuclei ddin, int index) {
		StringBuilder sb = new StringBuilder();
//		for (int n = 0; n < ddin.getSentenceInfoAt(0)
//				.getNucleiIndexListSize(); n++) {
//			sb.append(ddin.getSentenceInfoAt(0).getNucleiIndexListAt(n));
//			if (n < ddin.getSentenceInfoAt(0).getNucleiIndexListSize() - 1) {
//				sb.append(", "); //$NON-NLS-1$
//			}
//		}
//		if (index < ddin.getSentenceInfoSize() - 1) {
//			sb.append(", "); //$NON-NLS-1$
//		}

		return ddin.getSentenceInfoAt(0).getNucleiIndexListAt(index);
	}
	
	private ArrayList<Integer> involvedSentences(String key) {
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		
		if(isPoSErrorMiningResult()){		
			ArrayList<ItemInNuclei> arrL = nGramResult.get(key);	
			if (arrL != null) {
				for (int i = 0; i < arrL.size(); i++) {
					ItemInNuclei iin = arrL.get(i);
					// System.out.println(iin.getPosTag());
	
					for (int j = 0; j < iin.getSentenceInfoSize(); j++) {
						tmp.add(iin.getSentenceInfoAt(j).getSentenceNr());
						// System.out.println(iin.getSentenceInfoAt(j).getSentenceNr());
					}
				}
			}
		} 

		if(searchMode == 1){
			ArrayList<DependencyItemInNuclei> arrL = nGramResultDependency.get(key);	
			if (arrL != null) {
				for (int i = 0; i < arrL.size(); i++) {
					DependencyItemInNuclei iin = arrL.get(i);
					//System.out.println(iin.getPosTag());	
					for (int j = 0; j < iin.getSentenceInfoSize(); j++) {
						tmp.add(iin.getSentenceInfoAt(j).getSentenceNr());
						//System.out.println(iin.getSentenceInfoAt(j).getSentenceNr());
					}
				}
			}
		}

		return tmp;
	}

	/**
	 * @param s
	 * @param arrayList
	 * @param arrayList
	 * @return
	 */
	private String colorStringArray(String[] s, ArrayList<Integer> arrayList) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length; i++) {
			if (isNucleiList(s[i], arrayList)) {
				//fixme neues data model wieder aktivieren
				//Color nuclei = ConfigRegistry.getGlobalRegistry().getColor(
				//		"plugins.errorMining.highlighting.nucleusHighlight"); //$NON-NLS-1$
				Color nuclei = new Color(ConfigRegistry.getGlobalRegistry()
						.getInteger("plugins.dependency.highlighting.nodeHighlight")); //$NON-NLS-1$
				String hex = "#" + Integer.toHexString(nuclei.getRGB()).substring(2); //$NON-NLS-1$
				// System.out.println(hex);
				sb.append("<font color=" + hex + ">"); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append(s[i]);
				sb.append("</font>"); //$NON-NLS-1$
			} else {
				sb.append(s[i]);
			}

			if (i < s.length - 1) {
				sb.append(" "); //$NON-NLS-1$
			}
		}
		return sb.toString();
	}
	
	private String colorStringDependency(String key, ArrayList<Integer> arrayList) {
		StringBuilder sb = new StringBuilder();
		String[] splittedKey = key.split(" ");  //$NON-NLS-1$
		
		//System.out.println("COLORDEP " +key);
		List<DependencyItemInNuclei> diinList = nGramResultDependency.get(key);
		int sentenceStart = diinList.get(0).getSentenceInfoAt(0).getSentenceBegin();
		int sentenceEnd = diinList.get(0).getSentenceInfoAt(0).getNucleiIndex();
		
		int colorOffset = diinList.get(0).getSentenceInfoAt(0).getSentenceBegin();
		
		if(splittedKey.length == 2){
			if(sentenceEnd-sentenceStart > 1){
				colorOffset = sentenceEnd;
			}
		}
		
		Color nuclei = new Color(ConfigRegistry.getGlobalRegistry()
				.getInteger("plugins.dependency.highlighting.nodeHighlight")); //$NON-NLS-1$
		

		for(int c = 0; c < splittedKey.length; c++){			
			if(isDependencyNuclei(c, diinList.get(0).getSentenceInfoAt(0), colorOffset)){
				String hex = "#" + Integer.toHexString(nuclei.getRGB()).substring(2); //$NON-NLS-1$
				// System.out.println(hex);
				sb.append("<font color=" + hex + ">"); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append(splittedKey[c]);
				sb.append(" "); //$NON-NLS-1$
				sb.append("</font>"); //$NON-NLS-1$			
			} else {
				sb.append(splittedKey[c]);
				sb.append(" "); //$NON-NLS-1$
			}			
		}
		
		
		return sb.toString();
	}
	
	private boolean isDependencyNuclei(int index, DependencySentenceInfo dsi, int colorOffset) {

		for(int n = 0; n < dsi.getNucleiIndexListSize(); n++){
//			System.out.println("colorOFF: " + colorOffset);
//			System.out.println("head: " + dsi.getNucleiIndexListAt(n));
//			System.out.println("index: " + index);
			if (index == (dsi.getNucleiIndexListAt(n)-colorOffset)){
				return true;
			}
		}
		return false;
	}
	
	protected boolean isOverviewSelected(){
		return tabbedPane.getSelectedIndex() == 0;
	}
	
	protected boolean isPoSErrorMiningResult(){
		return searchMode == 0;
	}
		
		
	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#registerActionCallbacks()
	 */
	@Override
	protected void registerActionCallbacks() {
		super.registerActionCallbacks();

		ActionManager actionManager = getActionManager();

		// Load actions
		URL actionLocation = ErrorMiningSearchPresenter.class
				.getResource("errormining-presenter-actions.xml"); //$NON-NLS-1$
		if (actionLocation == null)
			throw new CorruptedStateException(
					"Missing resources: errormining-presenter-actions.xml"); //$NON-NLS-1$

		try {
			actionManager.loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(contentPanel, e);
			return;
		}

		actionManager.addHandler(
				"plugins.errormining.nGramResultView.applyFilterAction", //$NON-NLS-1$
				callbackHandler, "applyFilter"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errorMining.nGramResultView.resetFilterAction", //$NON-NLS-1$
				callbackHandler, "resetFilter"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errormining.nGramResultView.sortAscAction", //$NON-NLS-1$
				callbackHandler, "sortAsc"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errormining.nGramResultView.sortDescAction", //$NON-NLS-1$
				callbackHandler, "sortDesc"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errormining.nGramResultView.openPreferencesAction", //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errormining.nGramResultView.statsAction", //$NON-NLS-1$
				callbackHandler, "statsGeneration"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errormining.nGramResultView.showDetailAction", //$NON-NLS-1$
				callbackHandler, "showDetail"); //$NON-NLS-1$
	}
	
		
	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#createCallbackHandler()
	 */
	@Override
	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandlerErrorMining();
	}
	
	
	/**
			 * @param key
			 * @return
			 * @return
			 */
			private SentenceDataList createDetailList(String key) {
				List<SentenceData> sentenceDataDetailedList = new ArrayList<SentenceData>();	
				//get corpus
				DataList<?> dl = ((AbstractSearchResult) searchResult).getTarget();
				
				//System.out.println("selectedKey " + key);				
								
				// pos search
				if (isPoSErrorMiningResult()) {
					ArrayList<ItemInNuclei> iinList = nGramResult.get(key);
					
					for (int i = 0; i < iinList.size(); i++) {
						ItemInNuclei iin = iinList.get(i);
	
						for (int s = 0; s < iin.getSentenceInfoSize(); s++) {
							int sentenceNr = iin.getSentenceInfoAt(s).getSentenceNr();
							//SentenceData sentenceData = (SentenceData) dl.get(sentenceNr);
							//System.out.println("SentenceData: "+ sentenceData.getText());
							//sentenceDataDetailedList.add(sentenceData);							
							
							AnnotatedDependencyData annoDepData = new AnnotatedDependencyData(
									(DependencyData) dl.get(sentenceNr));

							List<Integer> hlIndex = new ArrayList<Integer>();
							List<String> nucleusList = getNucleusList(key, involvedSentences((String) key));
							
							//System.out.println(nucleusList);							
							
							for(int j = 0; j < annoDepData.length(); j++){						
								
								String currentForm = annoDepData.getForm(j);
																
								if(nucleusList.contains(currentForm)){
									//add new index
									hlIndex.add(j);
									//System.out.println("NUCLEUS " + annoDepData.getForm(j) 
									//					+ "Position " + (j+1)
									//					+ " Satz " + (sentenceNr+1));									
								} else if (nucleusList.contains("[number-wildcard]")){ //$NON-NLS-1$
									if(numberPattern.matcher(currentForm).find()){										
										hlIndex.add(j);
									}
								}
							}
							
							int[] hlIndexArray = new int[hlIndex.size()];
							
							for (int h = 0; h < hlIndex.size(); h++) {
								hlIndexArray[h] = hlIndex.get(h);
							}							

							DefaultNGramHighlight defaultGHL = new DefaultNGramHighlight(hlIndexArray);
							annoDepData.setAnnotation(new NGramAnnotation(defaultGHL));

							sentenceDataDetailedList.add(annoDepData);
	
						}
					}
				}
					
				//dependency
				else {
					ArrayList<DependencyItemInNuclei> diinList = nGramResultDependency.get(key);			

					for(int i = 0; i < diinList.size(); i++){
						
						DependencyItemInNuclei diin = diinList.get(i);
						
						for (int s = 0; s < diin.getSentenceInfoSize(); s++){
							int sentenceNr = diin.getSentenceInfoAt(s).getSentenceNr();
							//SentenceData sentenceData = (SentenceData) dl.get(diin.getSentenceInfoAt(s).getSentenceNr());
							//System.out.println(sentenceData.getText() + "TEXT");
							
							AnnotatedDependencyData annoDepData = new AnnotatedDependencyData(
									(DependencyData) dl.get(sentenceNr));
							
							List<Integer> hlIndex = getNucleusDependencyNr(key, diin.getSentenceInfoAt(s));							
							//System.out.println("KEY"+involvedSentences((String) key));
							//System.out.println("NLL"+getNucleusDependency(key, diin));
							//System.out.println("NLL"+getNucleusDependencyNr(key, diin.getSentenceInfoAt(s)));	
	
							
							int[] hlIndexArray = new int[hlIndex.size()];							
							
							for (int h = 0; h < hlIndex.size(); h++) {
								hlIndexArray[h] = hlIndex.get(h);
							}							

							DefaultNGramHighlight defaultGHL = new DefaultNGramHighlight(hlIndexArray);
							annoDepData.setAnnotation(new NGramAnnotation(defaultGHL));
							
							sentenceDataDetailedList.add(annoDepData);
						}
					}
				}
				DetailedNGramSentenceDataList dsdl = 
						new DetailedNGramSentenceDataList(sentenceDataDetailedList);
				return dsdl;			
			}


	private SentenceDataList createDetailListFromTable(String key, String sentences) {
		List<SentenceData> sentenceDataDetailedList = new ArrayList<SentenceData>();
		//System.out.println("selectedKey " + sentences + key); //$NON-NLS-1$
		
		DataList<?> dl = ((AbstractSearchResult)searchResult).getTarget();
		
		String[] tmp = sentences.split(", "); //$NON-NLS-1$
	

		for(int i = 0; i < tmp.length; i++){
			int sentenceNr = Integer.parseInt(tmp[i]);
			
			// pos search
			if (isPoSErrorMiningResult()) {
				ArrayList<ItemInNuclei> iinList = nGramResult.get(key);

				for (int k = 0; k < iinList.size(); k++) {
					ItemInNuclei iin = iinList.get(k);					

					for (int s = 0; s < iin.getSentenceInfoSize(); s++) {

						if(iin.getSentenceInfoAt(s).getSentenceNr() == sentenceNr){
							//SentenceData sentenceData = (SentenceData) dl.get(sentenceNr);
							//System.out.println("SentenceData: "+ sentenceData.getText());
							//sentenceDataDetailedList.add(sentenceData);						
							
							AnnotatedDependencyData annoDepData = new AnnotatedDependencyData(
									(DependencyData) dl.get(sentenceNr));
	
							List<Integer> hlIndex = new ArrayList<Integer>();
							List<String> nucleusList = getNucleusList(key, involvedSentences((String) key));
							//System.out.println(nucleusList);							
							
							for(int j = 0; j < annoDepData.length(); j++){								
								
								String currentForm = annoDepData.getForm(j);
																
								if(nucleusList.contains(currentForm)){
									//add new index
									hlIndex.add(j);
									//System.out.println("NUCLEUS " + annoDepData.getForm(j) 
									//					+ "Position " + (j+1)
									//					+ " Satz " + (sentenceNr+1));									
								} else if (nucleusList.contains("[number-wildcard]")){ //$NON-NLS-1$
									if(numberPattern.matcher(currentForm).find()){										
										hlIndex.add(j);
									}
								}
							}
							
							int[] hlIndexArray = new int[hlIndex.size()];
							
							for (int h = 0; h < hlIndex.size(); h++) {
								hlIndexArray[h] = hlIndex.get(h);
							}							
	
							DefaultNGramHighlight defaultGHL = new DefaultNGramHighlight(hlIndexArray);
							annoDepData.setAnnotation(new NGramAnnotation(defaultGHL));
						
						
							sentenceDataDetailedList.add(annoDepData);
						}

					}
				}
			} 
			
			//dependency
			else {
				
				//System.out.println("key " + ngramList.getSelectedValue() +"#");
				key = (String) ngramList.getSelectedValue();
				ArrayList<DependencyItemInNuclei> diinL = nGramResultDependency.get(key);			

				//SentenceData sentenceData = (SentenceData) dl.get(sentenceNr);
				//System.out.println(sentenceData.getText() + "TEXT");
				

				for(int j = 0; j < diinL.size(); j++){
					DependencyItemInNuclei diin = diinL.get(j);					
					
					for (int s = 0; s < diin.getSentenceInfoSize(); s++){
						//System.out.println("SNR"+sentenceNr);
						if(diin.getSentenceInfoAt(s).getSentenceNr() == sentenceNr){
							//SentenceData sentenceData = (SentenceData) dl.get(diin.getSentenceInfoAt(s).getSentenceNr());
							//System.out.println(sentenceData.getText() + "TEXT");
							
							AnnotatedDependencyData annoDepData = new AnnotatedDependencyData(
									(DependencyData) dl.get(sentenceNr));
							
							List<Integer> hlIndex = getNucleusDependencyNr(key, diin.getSentenceInfoAt(s));							
							//System.out.println("KEY"+involvedSentences((String) key));
							//System.out.println("NLL"+getNucleusDependency(key, diin));
							//System.out.println("NLL"+getNucleusDependencyNr(key, diin.getSentenceInfoAt(s)));	

							int[] hlIndexArray = new int[hlIndex.size()];
							
							for (int h = 0; h < hlIndex.size(); h++) {
								hlIndexArray[h] = hlIndex.get(h);
							}							
		
							DefaultNGramHighlight defaultGHL = new DefaultNGramHighlight(hlIndexArray);
							annoDepData.setAnnotation(new NGramAnnotation(defaultGHL));
															
							sentenceDataDetailedList.add(annoDepData);							
						}
	
					//sentenceDataDetailedList.add(sentenceData);
					}
				}
					
			}
		}
		
		DetailedNGramSentenceDataList dsdl = 
				new DetailedNGramSentenceDataList(sentenceDataDetailedList);
		return dsdl;			
	}

	protected List<String> getNucleusList(String key, ArrayList<Integer> arrayList) {
		List<String> nucleusList = new ArrayList<String>();
		String[] s = key.split(" "); //$NON-NLS-1$
		for (int i = 0; i < s.length; i++) {
			if (isNucleiList(s[i], arrayList)) {
				nucleusList.add(s[i]);
			}
		}
		return nucleusList;
	}


	/**
	 * @param s
	 * @param value
	 */
	private List<String> getNucleusDependency(String key, DependencyItemInNuclei diin) {
	
		List<String> list = new ArrayList<String>();
		
		String[] splittedKey = key.split(" ");  //$NON-NLS-1$		
		int colorOffset = -1;
		
		DependencySentenceInfo dsi = diin.getSentenceInfoAt(0); 
		colorOffset = dsi.getSentenceBegin();
		
		//TODO remove if
		if(dsi.getSentenceEnd()-dsi.getSentenceBegin() > 1){
				//colorOffset = dsi.getSentenceEnd();
		}
		
		//System.out.println(colorOffset);
		for(int c = 0; c < splittedKey.length; c++){			
			if(isDependencyNuclei(c, dsi, colorOffset)){
				//System.out.println(splittedKey[c]);
				list.add(splittedKey[c]);
			}		
		}
		
		if (list.size() == 0){
			colorOffset = dsi.getSentenceEnd();
			for(int c = 0; c < splittedKey.length; c++){			
				if(isDependencyNuclei(c, dsi, colorOffset)){
					//System.out.println(splittedKey[c]);
					list.add(splittedKey[c]);
				}		
			}
			
		}
		
		return list;
	}
	
	
	private List<Integer> getNucleusDependencyNr(String key, DependencySentenceInfo dsi) {
		
		List<Integer> list = new ArrayList<Integer>();		
		String[] splittedKey = key.split(" ");  //$NON-NLS-1$		
		int colorOffset = dsi.getSentenceBegin();
		
		for(int c = 0; c < splittedKey.length; c++){
			if(isDependencyNuclei(c, dsi, colorOffset)){
				//System.out.println(splittedKey[c]);
				list.add(c + colorOffset - 1);
			}		
		}		
		return list;
	}


	private void createStatistic(int gramsize) {
				
				statsResult = new LinkedHashMap<String, List<StatsData>>();			
				statsResultFiltered = new LinkedHashMap<String, List<StatsData>>();				
				
				for(String key : nGramResult.keySet()){
					//System.out.println(key);
					if(key.split(" ").length == gramsize){ //$NON-NLS-1$
						List<ItemInNuclei> iinList = nGramResult.get(key);
					
						ArrayList<StatsData> tmp = new ArrayList<StatsData>();
						List<String> keyList = new ArrayList<String>();
						
						for(int i = 0; i < iinList.size(); i++){
							StatsData sd = new StatsData(iinList.get(i).getPosTag(),
									iinList.get(i).getCount());
							
							sd.addWordstringSize(key);
							
	//						if(sd.indexOfWordstring(key)==-1){
	//							//System.out.println("contain " + key);
	//							sd.addWordstringSize(key);
	//						}
							
	//						for(StatsData data : tmp){
	//							if (data.indexOfWordstring(key) == -1) {
	//								// System.out.println("contain " + key);
	//								sd.addWordstringSize(key);
	//							}
	//						}
							
							//System.out.println(iinList.get(i).getCount());
							tmp.add(sd);	
							keyList.add(iinList.get(i).getPosTag());
							//System.out.println(sd.getWordstringSize());
						}
						
						Collections.sort(keyList);
						Collections.sort(tmp);
						
						if(statsResult.containsKey(keyList.toString())){
							List<StatsData> sdl = statsResult.get(keyList.toString());
							//check if we have to increase existing tag count
							for(StatsData sd : tmp){
								if(sdl.contains(sd)){
									StatsData newSD = sdl.get(sdl.indexOf(sd));
									newSD.setCount(newSD.getCount() + sd.getCount());
									newSD.addWordstringSize(sd.getWordstringAt(0));
									sdl.set(sdl.indexOf(sd), newSD);
								}
							}						
						} else {					
							statsResult.put(keyList.toString(), tmp);
							statsResultFiltered.put(keyList.toString(), tmp);
						}
					}			
				}
				
				
				//debug
	//			for(String s : statsResult.keySet()){
	//				List<StatsData> sdList = statsResult.get(s);
	//				System.out.print("Tag " + s );	
	//				
	//				for (int i = 0; i < sdList.size(); i++) {
	//					System.out.print(" Tag: " +sdList.get(i).getTagKey());
	//					System.out.print(" Count: " +sdList.get(i).getCount());	
	//					
	//				}
	//				System.out.println();	
	//			}
				
				//generateFilteredStatistic();
	
				
				statisticTableModel.reload();
				//activate tab
				tabbedPane.setSelectedIndex(2);
	}
	
	
	private void createStatisticDependency(int gramsize) {
		
		statsResult = new LinkedHashMap<String, List<StatsData>>();			
		statsResultFiltered = new LinkedHashMap<String, List<StatsData>>();				
		
		for(String key : nGramResultDependency.keySet()){
			if(key.split(" ").length == gramsize){ //$NON-NLS-1$
				List<DependencyItemInNuclei> diinList = nGramResultDependency.get(key);
			
				ArrayList<StatsData> tmp = new ArrayList<StatsData>();
				List<String> keyList = new ArrayList<String>();
				
				for(int i = 0; i < diinList.size(); i++){
					StatsData sd = new StatsData(diinList.get(i).getPosTag(),
							diinList.get(i).getCount());
					
					sd.addWordstringSize(key);					
					
					//System.out.println(iinList.get(i).getCount());
					tmp.add(sd);	
					keyList.add(diinList.get(i).getPosTag());
					//System.out.println(sd.getWordstringSize());
				}
				
				Collections.sort(keyList);
				Collections.sort(tmp);
				
				if(statsResult.containsKey(keyList.toString())){
					List<StatsData> sdl = statsResult.get(keyList.toString());
					//check if we have to increase existing tag count
					for(StatsData sd : tmp){
						if(sdl.contains(sd)){
							StatsData newSD = sdl.get(sdl.indexOf(sd));
							newSD.setCount(newSD.getCount() + sd.getCount());
							newSD.addWordstringSize(sd.getWordstringAt(0));
							sdl.set(sdl.indexOf(sd), newSD);
						}
					}						
				} else {					
					statsResult.put(keyList.toString(), tmp);
					statsResultFiltered.put(keyList.toString(), tmp);
				}
			}			
		}
		
		
		//debug
//			for(String s : statsResult.keySet()){
//				List<StatsData> sdList = statsResult.get(s);
//				System.out.print("Tag " + s );	
//				
//				for (int i = 0; i < sdList.size(); i++) {
//					System.out.print(" Tag: " +sdList.get(i).getTagKey());
//					System.out.print(" Count: " +sdList.get(i).getCount());	
//					
//				}
//				System.out.println();	
//			}
		
		//generateFilteredStatistic();

		
		statisticTableModel.reload();
		//activate tab
		tabbedPane.setSelectedIndex(2);
}


	private class FilterWorker extends SwingWorker<Object, Object> implements Identity{
		
			/**
			 * @see javax.swing.SwingWorker#doInBackground()
			 */
			@Override
			protected Object doInBackground() throws Exception {
				if ((int) lbm.getValue() > (int) ubm.getValue()){
					DialogFactory.getGlobalFactory().showError(null,
							"plugins.errormining.nGramResultView.boundsError.title", //$NON-NLS-1$
							"plugins.errormining.nGramResultView.boundsError.message"); //$NON-NLS-1$
				} else {				
					minimumGramsize = (int) lbm.getValue();
					maximumGramsize = (int) ubm.getValue();
					
					if(isPoSErrorMiningResult()){
						generateFilteredResult();
					}
					if(searchMode == 1){
						generateFilteredDependencyResult();
					}
					ngramListModel.reload();
					refreshCount();
				}	
				return null;
			}
			
		
			/**
			 * @see javax.swing.SwingWorker#done()
			 */
			@Override
			protected void done() {
				try {
					get();
				} catch (CancellationException | InterruptedException e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Results filtering interrupted", e); //$NON-NLS-1$				
					UIUtil.beep();
				} catch (ExecutionException e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Executino exception when filtering the results", e); //$NON-NLS-1$				
					UIUtil.beep();
				}
			}


			/**
			 * @see de.ims.icarus.util.id.Identity#getId()
			 */
			@Override
			public String getId() {
				// TO DO Auto-generated method stub
				return null;
			}


			/**
			 * @see de.ims.icarus.util.id.Identity#getName()
			 */
			@Override
			public String getName() {
				return "Filter Results"; //$NON-NLS-1$
			}


			/**
			 * @see java.lang.Object#equals(java.lang.Object)
			 */
			@Override
			public boolean equals(Object obj) {
				if(obj instanceof FilterWorker) {
					return ((FilterWorker)obj).getOwner()==getOwner();
				}
				return false;
			}


			/**
			 * @see de.ims.icarus.util.id.Identity#getDescription()
			 */
			@Override
			public String getDescription() {
				// TO DO Auto-generated method stub
				return null;
			}


			/**
			 * @see de.ims.icarus.util.id.Identity#getIcon()
			 */
			@Override
			public Icon getIcon() {
				// TO DO Auto-generated method stub
				return null;
			}


			/**
			 * @see de.ims.icarus.util.id.Identity#getOwner()
			 */
			@Override
			public Object getOwner() {
				return this;
			}
			
		}

	
	private class StatsFilterWorker extends SwingWorker<Object, Object> implements Identity{
		
		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Object doInBackground() throws Exception {
			//System.out.println(statisticTextFilterField.getText());
			generateFilteredStatistic();
			statisticTableModel.reload();
			refreshStatisticCount();

			return null;
		}


		/**
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		protected void done() {
			try {
				get();
			} catch (CancellationException | InterruptedException e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Statistic filtering interrupted", e); //$NON-NLS-1$				
				UIUtil.beep();
			} catch (ExecutionException e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Executino exception when filtering statistic", e); //$NON-NLS-1$				
				UIUtil.beep();
			}
		}


		/**
		 * @see de.ims.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			// TO DO Auto-generated method stub
			return null;
		}


		/**
		 * @see de.ims.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return "Filter Statistic"; //$NON-NLS-1$
		}


		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof FilterWorker) {
				return ((FilterWorker)obj).getOwner()==getOwner();
			}
			return false;
		}


		/**
		 * @see de.ims.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			// TO DO Auto-generated method stub
			return null;
		}


		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			// TO DO Auto-generated method stub
			return null;
		}


		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}
		
	}

	public class CallbackHandlerErrorMining extends CallbackHandler{
		
		private CallbackHandlerErrorMining(){
			//noop
		}
		
		
		public void applyFilter(ActionEvent e) {
			try {
				
				if(isOverviewSelected()){
					doResultFiltering();
				} else {
					doStatisticFiltering();
				}
				
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to apply result filter", ex); //$NON-NLS-1$				
				UIUtil.beep();
			}
		}
		
		public void resetFilter(ActionEvent e) {
			try {
				if(isOverviewSelected()){
					resetFilters();
				} else {
					resetStatisticFilter();
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to reset result filter", ex); //$NON-NLS-1$				
				UIUtil.beep();
			}
		}
		
		public void sortAsc(ActionEvent e) {
			try {
				if(isOverviewSelected()){
					ngramListModel.setSort(true);
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to sort ascending", ex); //$NON-NLS-1$				
				UIUtil.beep();
			}
		}
		
		public void sortDesc(ActionEvent e) {
			try {
				if(isOverviewSelected()){
					ngramListModel.setSort(false);
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to sort descending", ex); //$NON-NLS-1$				
				UIUtil.beep();
			}
		}
		
		public void openPreferences(ActionEvent e) {
			try {
				UIUtil.openConfigDialog("plugins.errorMining"); //$NON-NLS-1$
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to open preferences", ex); //$NON-NLS-1$				
				UIUtil.beep();
			}
		}
		
		public void statsGeneration(ActionEvent e) {
			try {
				if(isPoSErrorMiningResult()){
					createStatistic((int)sbm.getValue());
				} else	{
					int max = Math.max(minDependencyGram, (int)sbm.getValue());
					createStatisticDependency(max);
				}
				
				if(statisticTable.getModel().getValueAt(0, 0) != null){					
					statisticTable.getSelectionModel().setSelectionInterval(0, 0);
					String key = (String) statisticTable.getModel().getValueAt(0, 0);
					statisticTableModel.generateListEntryFromString(key);
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to generate statistic", ex); //$NON-NLS-1$				
				UIUtil.beep();
			}
		}
		
		
		public void showDetail(ActionEvent e) {
			try {
				//TODO noch richten
				if(isOverviewSelected()){
					if(ngramList.getSelectedIndex() != -1){
						int index = ngramList.getSelectedIndex();
						ngramListModel.getElementAt(index);
						// System.out.println(ngramListModel.getElementAt(index));
						String key = (String) ngramListModel.getElementAt(index);
	
						showDetails(createDetailList(key));
					}
				} else {
					//System.out.println(statisticList.getSelectedIndices().length);
					//TODO maybe enable multi selection
					if(statisticList.getSelectedIndices().length != 0){	
						if(statisticList.getSelectedIndices().length == 1){	
							showDetails(createDetailList((String)statisticList.getSelectedValue()));
						} else {
							//showDetails(createDetailList(statisticList.getSelectedValuesList()));
						}
					}
				}
				
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to show details", ex); //$NON-NLS-1$				
				UIUtil.beep();
			}
		}
		
		
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
				
				if (me.getSource() == ngramList) {
					int index = ngramList.locationToIndex(me.getPoint());
					// System.out.println("Double clicked on Item " + index);
					 System.out.println(ngramListModel.getElementAt(index));
					String key = (String) ngramListModel.getElementAt(index);

					showDetails(createDetailList(key));
				}
				
				else if (me.getSource() == ngramTable) {
					int selectedRow = ngramTable.getSelectedRow();
					// add correct column!!
					showDetails(createDetailListFromTable(
							(String) ngramTable.getModel()
								.getValueAt(selectedRow, 0),
							(String) ngramTable.getModel()
								.getValueAt(selectedRow, 3)));
				}			
		    	
				else if (me.getSource() == statisticTable) {
					displaySelectedStatsData();
				}
				
				else if (me.getSource() == statisticList) {
					String key = (String) statisticList.getSelectedValue();
					showDetails(createDetailList(key));
				} else {
					//nothing
				}
		        
		     }
		}
		

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			// TO DO Auto-generated method stub			
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			
			
			if(e.getValueIsAdjusting()){
				return;
			}

			
			if (e.getSource() == ngramList){
				Object selectedObject = ngramList.getSelectedValue();
				//System.out.println((String) selectedObject);
				ngramTableModel.reload((String) selectedObject);	
				ngramTableAdjuster.adjustColumns();

			} else if (e.getSource() == statisticTable.getSelectionModel()){
				//no selection check
				if(statisticTable.getSelectedRow() != -1){
					displaySelectedStatsData();
				}
			} else {
				try {
					//for default presenter
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
			
			if(e.getSource() == textFilterField){ 
				doResultFiltering();				
			}
			
			if(e.getSource() == statisticTextFilterField){ 
				doStatisticFiltering();				
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
			// TO DO Auto-generated method stub
			
		}

		/**
		 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void intervalRemoved(ListDataEvent arg0) {
			// TO DO Auto-generated method stub
			
		}


		/**
		 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentResized(ComponentEvent e) {
			if(!trackResizing) {
				return;
			}
			
			int height = splitpaneDetails.getHeight();
			if(height==0) {
				return;
			}
			
			splitpaneDetails.setDividerLocation(Math.max(height/2, height-100));
			
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

		private Object[] keys;
		
		private Object largestElement;
		
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
		
		public Object getLargestElement(){
			return largestElement;			
		}


		public void reload() {
			//keys = nGramResult.keySet().toArray();

//			System.out.println("minsize: " 
//						+ searchResult.getSource().getParameters()
//							.getInteger(NGramParameters.GRAMS_GREATERX));
			
			List<String> myList = new ArrayList<>();
			Collection<String> source ;
			
			//check if pos (0) or dependency (1)
			if (searchMode==0){
				source = nGramResultFiltered.keySet();
			} else {
				source = nGramResultFilteredDependency.keySet();
			}
			
			String sMax = ""; //$NON-NLS-1$
			
			for (String element: source){
				myList.add(element);
				if(element.length() > sMax.length()){
					sMax = element;
				}
			}
			
			largestElement = sMax;
			
			if (ascending){
				//System.out.println("normal");
				CompareStringLength c = new CompareStringLength();
				Collections.sort(myList, c);
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
		
		private StringBuilder sb;


		public NGramResultViewListCellRenderer(){
	         setOpaque(true);
	         sb = new StringBuilder();
	     }


		  public Component getListCellRendererComponent(JList<?> list, Object value, int index,
			      boolean isSelected, boolean cellHasFocus) {
			String[] s = ((String) value).split(" "); //$NON-NLS-1$

			sb.setLength(0);
			
			if(isPoSErrorMiningResult()){
				sb.append("<html>").append((index + 1)).append(") ") //$NON-NLS-1$ //$NON-NLS-2$
					.append(" (").append(s.length).append("-Gram) ")  //$NON-NLS-1$//$NON-NLS-2$
					.append(colorStringArray(s, involvedSentences((String) value)))
					.append("</html>"); //$NON-NLS-1$
			} else {
				//System.out.println("SelectedListDependencyValue: " + value);			
								
				sb.append("<html>").append((index + 1)).append(") ") //$NON-NLS-1$ //$NON-NLS-2$
					.append(" (").append(s.length).append("-Gram) ")  //$NON-NLS-1$//$NON-NLS-2$
					.append(colorStringDependency((String)value, involvedSentences((String) value)))
					.append("</html>"); //$NON-NLS-1$
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				// text.setForeground(list.getSelectionForeground());
			} else {
				// the color returned from list.getBackground() is pure white
				setBackground(list.getBackground());
				// THIS works -- but is obviously hardcoded
				// setBackground(Color.WHITE);
				// text.setForeground(list.getForeground());
			}
			setText(sb.toString());
			
			return this;
		  }
	}
	
	
	
	class NGramResultViewTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -196214722892607695L;
		
		
		/**
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int row, int col) {
			//TODO option do copy sentences
//			if(col == 3){
//				return true;
//			}
			return false;

		}

		protected boolean ascending = true;
		protected ArrayList<ItemInNuclei> iinList;
		protected ArrayList<DependencyItemInNuclei> iinDList;
		protected Map<Integer, String> tmpMap;
		
		protected Map<Integer, DependencyNucleusCache> nucleusMap;
		
		String[] keySplitted = null;
		
		boolean multinuclei = false;
		int itemsAdded;
		
		public NGramResultViewTableModel(){
			
		}
		
		public void reload (String key){
			if (key == null) {
				iinList = null;
			} else {

				itemsAdded = 0;
				tmpMap = new LinkedHashMap<>();
				
				if(isPoSErrorMiningResult()){
					//iinList = nGramResult.get(key);					

					iinList = new ArrayList<>();					
					this.keySplitted = key.split(" ");	 //$NON-NLS-1$
					
					for (int i = 0; i < keySplitted.length; i++) {
						//System.out.println(keySplitted[i]);
						if (isNuclei(keySplitted[i])) {
							iinList.addAll(nGramResult.get(keySplitted[i]));
	
							/*
							 * size sets rowcount in table, (e.g. size = 2 row 0 and
							 * row 1 must get assigned with keySplitted[i] tag.
							 * Therefore put tmpMap the keys with the used row/index
							 */
	
							for (int j = 0; j < nGramResult.get(keySplitted[i])
									.size(); j++) {
								tmpMap.put(itemsAdded, keySplitted[i]);
								itemsAdded++;
							}
	
						}
					}
					
				} else {
					
					//dependency stuff
					System.out.println("KEY SM1: " + key);					
					iinDList = new ArrayList<>();					
					iinDList.addAll(nGramResultDependency.get(key));
					
					nucleusMap = new LinkedHashMap<>();					
					

					for (int i = 0; i < iinDList.size(); i++) {
						System.out.println("iind"+iinDList.get(i).getPosTag());
						System.out.println(getNucleusDependency(key, iinDList.get(i)));
						
						//FIXME
						List<String> nucleiList = getNucleusDependency(key, iinDList.get(i));
						for (int n = 0; n < nucleiList.size();n++) {
							DependencyNucleusCache dnc = new DependencyNucleusCache();
							dnc.setKey(nucleiList.get(n));
							dnc.setDiin(iinDList.get(i));
							dnc.setHeadIndex(n);
							nucleusMap.put(itemsAdded, dnc);
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
		            		"plugins.errormining.labels.SentenceNR"); //$NON-NLS-1$
		            case 4: return ResourceManager.getInstance().get(
		            		"plugins.errormining.labels.NucleiCount"); //$NON-NLS-1$
		            case 5: return ResourceManager.getInstance().get(
		            		"plugins.errormining.labels.NucleiIndex"); //$NON-NLS-1$
		            default: break;
		        }
		        return null;
		}
		

		/**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			if(isPoSErrorMiningResult()) {
				return iinList==null ? 0 : iinList.size();
			} else {
				return nucleusMap==null ? 0 : nucleusMap.size();	
			}
			
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (iinList == null && iinDList == null) {
				return null;
			}			

			if(isPoSErrorMiningResult()){
				//pos
				ItemInNuclei iin = iinList.get(rowIndex);			
				
				
				int nucleiCount = iin.getSentenceInfoAt(0).getNucleiIndexListSize();
				
//				System.out.println(multinuclei + " MULTI" 
//									+ " " + getNucleis(iin));
				
//				System.out.println("PosTag: " + iin.posTag +  
//									"RowIndex: " + rowIndex +
//									"ColIndex: " + columnIndex);
//				System.out.println(tmpMap.toString());
				
				//TODO really needed? better check nucleus size instead?!
//				if (iin.getSentenceInfoSize() == 1) {
				if (!multinuclei) {				
					switch (columnIndex) {
					case 0:
						return tmpMap.get(rowIndex);//keySplitted[nucleiGenIndex-start];
					case 1:
						return iin.getPosTag(); 
					case 2:
						return StringUtil.formatDecimal(iin.getCount());
					case 3:
						return sentenceOccurences(iin);
					case 4:
						return nucleiCount;
					case 5:
						return getNucleis(iin);
					default:
						break;
					}
				} else {
					switch (columnIndex) {
					case 0:
						return tmpMap.get(rowIndex);
					case 1:
						return iin.getPosTag();
					case 2:
						return iin.getCount();
					case 3:
						return sentenceOccurences(iin);					
					case 4:
						return iin.getSentenceInfoAt(0).getNucleiIndexListSize();
					case 5:
						return getNucleis(iin);
					default:
						break;
					}
				}
				return null;	
				
			} else {
				DependencyItemInNuclei iinD = nucleusMap.get(rowIndex).getDiin();				
				
				int nucleiDCount = iinD.getSentenceInfoAt(0).getNucleiIndexListSize();
				int index = nucleusMap.get(rowIndex).getHeadIndex();
				
				if(nucleiDCount == 0){
					switch (columnIndex) {
					case 0:
						return nucleusMap.get(rowIndex).getKey();//keySplitted[nucleiGenIndex-start];
					case 1:
						return getCombinedDependencyTag(iinD
								,Integer.valueOf((String) getValueAt(rowIndex, 3))
								,Integer.valueOf((String) getValueAt(rowIndex, 5)));
					case 2:
						return StringUtil.formatDecimal(iinD.getCount());
					case 3:
						return sentenceOccurences(iinD);
					case 4:
						return nucleiDCount;
					case 5:
						return getNucleiDependency(iinD, index);
					default:
						break;
					}
				} else {
					switch (columnIndex) {
					case 0:
						return nucleusMap.get(rowIndex).getKey();//keySplitted[nucleiGenIndex-start];
					case 1:
						//FIXME
						String[] number = ((String) getValueAt(rowIndex, 3)).split(", ");
						return getCombinedDependencyTag(iinD
								,Integer.valueOf(number[0])
								,(Integer) getValueAt(rowIndex, 5));
					case 2:
						return StringUtil.formatDecimal(iinD.getCount());
					case 3:
						return sentenceOccurences(iinD);
					case 4:
						return nucleiDCount;
					case 5:
						return getNucleiDependency(iinD, index);
					default:
						break;
					}
				}
				return null;
			}
		}			
 		
	}
	
	
	public class NGramResultViewTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = -4527456767069459763L;
		
		private boolean isColorTableEntry(String s, ArrayList<Integer> arrayList) {
			if(isPoSErrorMiningResult()){
				if (isNucleiList(s, arrayList)) {
					return true;
				}
			}
			return false;
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int col) {

		    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			if(col == 0){
			    String s = table.getModel().getValueAt(row, col).toString();
			    
			    String selectedKey = (String)ngramList.getSelectedValue();			    
			     
			    //System.out.print(s + " # " + selectedKey  //$NON-NLS-1$
			    //					+ " " + involvedSentences(selectedKey)); //$NON-NLS-1$
			    if (isColorTableEntry(s, involvedSentences(selectedKey))) {
			        //c.setForeground(ConfigRegistry.getGlobalRegistry()
					//		.getColor("plugins.errorMining.highlighting.nucleusHighlight")); //$NON-NLS-1$
			        c.setForeground(new Color(ConfigRegistry.getGlobalRegistry()
							.getInteger("plugins.dependency.highlighting.nodeHighlight"))); //$NON-NLS-1$
					
			    }   else {
			        c.setForeground(Color.BLACK);
			    }
			}
		    return c;
		}		
	}
	
	public class StatisticListModel extends AbstractListModel<Object> {
		
		List<String> keys;

		private static final long serialVersionUID = 1260708117644287162L;
		
		public StatisticListModel(){
			//noop
		}

		public void reload(List<String> list){
			keys = list;
			fireContentsChanged(this, 0, Math.max(getSize()-1, 0));
		}		


		/**
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		@Override
		public Object getElementAt(int index) {
			return keys.get(index);
		}

		/**
		 * @see javax.swing.ListModel#getSize()
		 */
		@Override
		public int getSize() {
			return keys==null ? 0 : keys.size();
		}
	
	}
	
	public class StatisticTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 2680904262921741087L;

		protected Map<String, List<StatsData>> statistic;
		protected Object[] keyAray;
		
		
		public StatisticTableModel(){
			//noop			
		}
		

		public void reload (){
			statistic = statsResultFiltered;
			keyAray = statsResultFiltered.keySet().toArray();
			refreshStatisticCount();
			fireTableDataChanged();
		}
		
		/**
		 * @see javax.swing.table.TableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int columnIndex) {

		      switch (columnIndex) {
		            case 0: return ResourceManager.getInstance().get(
		            		"plugins.errormining.labels.Tag"); //$NON-NLS-1$
		            case 1: return ResourceManager.getInstance().get(
		            		"plugins.errormining.labels.Count"); //$NON-NLS-1$
		        }
		        return null;
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return 2;
		}

		/**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return statistic==null ? 0 : statistic.size();
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			
			if (statistic == null) {
				return null;
			}		

			switch (columnIndex) {
				case 0:
					return keyAray[rowIndex].toString();
				case 1:
					return createCount(statistic.get(keyAray[rowIndex])); 
				default:
					break;
			}			
			return null;
		}

		private String createCount(List<StatsData> sdList) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < sdList.size(); i++) {
				if (i < sdList.size() - 1) {
					sb.append("[").append(sdList.get(i).getTagKey()) //$NON-NLS-1$
					.append(" ") //$NON-NLS-1$
					.append(sdList.get(i).getCount()).append("] "); //$NON-NLS-1$
				} else {
					sb.append("[").append(sdList.get(i).getTagKey()) //$NON-NLS-1$
					.append(" ") //$NON-NLS-1$
					.append(sdList.get(i).getCount()).append("]"); //$NON-NLS-1$
		
				}
			}
			return sb.toString();
		}
		
		public void generateListEntryFromString(String key){
			createWordList(statistic.get(key));
		}
		
		private void createWordList(List<StatsData> sdList) {
			List<String> tmp = new ArrayList<String>();			
			for (int i = 0; i < sdList.size(); i++) {
				for(int j = 0; j < sdList.get(i).getWordstringSize(); j++ ){
					String value = sdList.get(i).getWordstringAt(j);
					if(!(tmp.contains(value))){
						tmp.add(value);
					}
				}
			}
			statisticListModel.reload(tmp);
		}
		
	}	
	
	

	/**
	 * @param iinD
	 * @param headIndex 
	 * @param object 
	 * @return
	 */
	public Object getCombinedDependencyTag(DependencyItemInNuclei iinD, int sentenceNo, Integer headIndex) {
		DataList<?> dl = ((AbstractSearchResult) searchResult).getTarget();
		DependencyData dd = (DependencyData) dl.get(sentenceNo);
		headIndex = headIndex-1;
		//System.out.println("SNR"+sentenceNo);
		//System.out.println(dd.getForm(headIndex) +  " " + dd.getHead(headIndex) + " " + dd.getRelation(headIndex));

		StringBuilder sb = new StringBuilder();
		int tempIndex = dd.getHead(headIndex);
		String headAppendix;
		if (headIndex < tempIndex){
			headAppendix = "_R"; //$NON-NLS-1$
		} else {
			headAppendix = "_L"; //$NON-NLS-1$
		}
		sb.append(dd.getRelation(headIndex)).append(headAppendix);
		
		return sb.toString();
	}
	

}
