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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.AttributedString;
import java.text.NumberFormat;
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
import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.entity.TitleEntity;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.TableOrder;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.config.ConfigUtils;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.language.dependency.DependencySentenceData;
import de.ims.icarus.language.dependency.annotation.AnnotatedDependencyData;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.errormining.DependencyItemInNuclei;
import de.ims.icarus.plugins.errormining.DependencySentenceInfo;
import de.ims.icarus.plugins.errormining.DetailedNGramSentenceDataList;
import de.ims.icarus.plugins.errormining.ItemInNuclei;
import de.ims.icarus.plugins.errormining.NGrams;
import de.ims.icarus.plugins.errormining.SentenceInfo;
import de.ims.icarus.plugins.errormining.annotation.NGramAnnotation;
import de.ims.icarus.plugins.errormining.ngram_tools.CompareStringLength;
import de.ims.icarus.plugins.errormining.ngram_tools.DependencyNucleusCache;
import de.ims.icarus.plugins.errormining.ngram_tools.NGramDataList;
import de.ims.icarus.plugins.errormining.ngram_tools.NGramDataListDependency;
import de.ims.icarus.plugins.errormining.ngram_tools.NGramParameters;
import de.ims.icarus.plugins.errormining.ngram_tools.NucleusCache;
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
import de.ims.icarus.ui.helper.DefaultFileFilter;
import de.ims.icarus.ui.helper.UIHelperRegistry;
import de.ims.icarus.ui.tab.ButtonTabComponent;
import de.ims.icarus.ui.tab.TabController;
import de.ims.icarus.ui.table.TableColumnAdjuster;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.ListPresenter;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataList;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.util.strings.StringUtil;

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
	//protected NGramResultViewTableCellRenderer ngramTableRenderer;


	private JScrollPane scrollPane;
	private JScrollPane scrollPaneDetailed;
	//private ListPresenter listPresenter;
	//private AWTPresenter detailsPresenter;
	//private JSplitPane splitpaneDetails;
	private JScrollPane scrollPaneStats;
	private JScrollPane scrollPaneStatsDetailed;

	//button filter stuff
	protected JSpinner lowerBound;
	protected SpinnerNumberModel lbm;
	protected JSpinner upperBound;
	protected SpinnerNumberModel ubm;
	protected JLabel resultCounter;
	protected JCheckBox exactTextMatch;
	protected JTextField textFilterField;
	protected JCheckBox onlyFilterNuclei;
	protected JToolBar toolBarOverview;




	//stats stuff
	protected JToolBar toolBarStatistic;
	protected JSpinner statsSpinner;
	protected SpinnerNumberModel sbm;
	protected JTextField statisticTextFilterField;
	protected JCheckBox exactTagMatch;
	protected JLabel statisticCounter;
	protected JSplitPane splitpaneStats;
	protected JList<Object> statisticList;
	protected StatisticListModel statisticListModel;
	protected JTable statisticTable;
	protected StatisticTableModel statisticTableModel;
	protected TableColumnAdjuster statisticTableAdjuster;
	protected Map<String, List<StatsData>> statsResult;
	protected Map<String, List<StatsData>> statsResultFiltered;
	//protected DefaultCategoryDataset dataset;
	protected ChartPanel barChartPanel;
	protected JFreeChart chart;
	protected Plot plot;


	//result stuff dependency
	protected Map<String,ArrayList<DependencyItemInNuclei>> nGramResultDependency;
	protected Map<String,ArrayList<DependencyItemInNuclei>> nGramResultFilteredDependency;
	protected NGramDataListDependency ngListDependency;
	protected List<String> posFilter;


	protected Map<String,ArrayList<ItemInNuclei>> nGramResult;
	protected Map<String,ArrayList<ItemInNuclei>> nGramResultFiltered;
	protected NGramDataList ngList;

	private int minimumGramsize;
	private int maximumGramsize;
	private boolean showPercentage = false;
	private boolean barCompare = false;


	//Color Stuff
	protected Color nilColor = Color.green;
	protected Color headColor = Color.blue;
	protected Color nucleusColor;

	// 1 = dependency; 0 = part-of-speech
	private int searchMode;

	private static final Pattern numberPattern = Pattern.compile("^[0-9]"); //$NON-NLS-1$
	private static final int minDependencyGram = 2;
	private static final int maximumCharPerTab = 40;
	private static final String labelmatrix = "Total#"; //$NON-NLS-1$
	private static final String nilString = "nil"; //$NON-NLS-1$
	private static final String barChart = "barchart"; //$NON-NLS-1$
	private static final String pieChart = "piechart"; //$NON-NLS-1$
	private static final String percentA = "percentA"; //$NON-NLS-1$
	private static final String percentB = "percentB"; //$NON-NLS-1$


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
			return;
		}

		searchMode = (int) searchResult.getProperty("MODE"); //$NON-NLS-1$

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
		refresh();


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

			posFilter = (List<String>) searchResult.getProperty("POSFILTER"); //$NON-NLS-1$
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

		//switch to first tab  (overview) when selecting new/other result!
		if(tabbedPane != null && searchResult.getTotalMatchCount() != 0){
			if(isPoSErrorMiningResult()) {
				createStatistic(1);
			} else {
				createStatisticDependency(2);
			}
			tabbedPane.setSelectedIndex(0);
		}

		//refresh();

	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#refresh()
	 */
	@Override
	public void refresh() {

		nucleusColor = new Color(ConfigRegistry.getGlobalRegistry()
				.getInteger("plugins.dependency.highlighting.nodeHighlight")); //$NON-NLS-1$

		//close tabs
		if (tabbedPane != null){
			int tabCount = tabbedPane.getTabCount();
			// first tab = overview and second tab = matrix
			// both should be never closed so we start at 2
			while(tabCount > 2){
				tabCount--;
				tabbedPane.remove(tabCount);
			}

		}

		if(ngramTable != null){
			ngramTableModel = new NGramResultViewTableModel();
			ngramTable.setModel(ngramTableModel);
		}


		if(statisticTable != null){
			statisticTable.removeAll();
		}

		if(statisticList != null) {
			statisticList.removeAll();
		}

		if(plot != null){
			refreshChart("", null); //$NON-NLS-1$
		}

		if(textFilterField != null){
			textFilterField.setText(null);
		}

		if(onlyFilterNuclei != null){
			onlyFilterNuclei.setSelected(false);
		}

		if(statisticTextFilterField != null){
			statisticTextFilterField.setText(null);
		}

		refreshCount();
	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#buildContentPanel()
	 */
	@Override
	protected void buildContentPanel() {

		//general visualization stuff
		tabbedPane = createTabbedPane();
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
		ngramTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ngramTableAdjuster = new TableColumnAdjuster(ngramTable);
		ngramTableAdjuster.setOnlyAdjustLarger(false);
		scrollPaneDetailed.setViewportView(ngramTable);


		tabbedPane.addTab(ResourceManager.getInstance().get(
				"plugins.errormining.errorMiningSearchPresenter.tab.overview"), //$NON-NLS-1$
				IconRegistry.getGlobalRegistry().getIcon("container_obj.gif"), //$NON-NLS-1$
				overviewPanel);


		//----------------------------------------------------------
		//second view (statistics confusion matrix)
		JPanel statsPanel = new JPanel(new BorderLayout());
		JSplitPane statisticHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		statisticHorizontal.setContinuousLayout(true);
		statisticHorizontal.setDividerSize(5);
		statisticHorizontal.setBorder(UIUtil.topLineBorder);
		statisticHorizontal.setResizeWeight(0.6);
		statisticHorizontal.addComponentListener(getHandler());


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
		statisticTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		statisticTableAdjuster = new TableColumnAdjuster(ngramTable);
		statisticTableAdjuster.setOnlyAdjustLarger(false);

		scrollPaneStats.setViewportView(statisticTable);

		if(statisticListModel == null){
			statisticListModel = new StatisticListModel();
		}
		statisticList = new JList<Object>(statisticListModel);
		statisticList.setBorder(UIUtil.defaultContentBorder);
		statisticList.addListSelectionListener(getHandler());
		statisticList.addMouseListener(getHandler());
		statisticList.getModel().addListDataListener(getHandler());
		DefaultListSelectionModel statsSelectionModel = new DefaultListSelectionModel();
		statsSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		statisticList.setSelectionModel(statsSelectionModel);
		statisticList.setCellRenderer(new NGramResultViewListCellRenderer());

		scrollPaneStatsDetailed = new JScrollPane();
		scrollPaneStatsDetailed.setBorder(UIUtil.emptyBorder);
		UIUtil.defaultSetUnitIncrement(scrollPaneStatsDetailed);
		scrollPaneStatsDetailed.setViewportView(statisticList);

		splitpaneStats.setLeftComponent(scrollPaneStats);
		splitpaneStats.setRightComponent(scrollPaneStatsDetailed);


		statisticHorizontal.setLeftComponent(splitpaneStats);
		statisticHorizontal.setRightComponent(buildBarChart());

		statsPanel.add(buildStatisticToolbar(), BorderLayout.NORTH);
		statsPanel.add(statisticHorizontal, BorderLayout.CENTER);

		tabbedPane.addTab(ResourceManager.getInstance().get(
				"plugins.errormining.errorMiningSearchPresenter.tab.stats"), //$NON-NLS-1$
				IconRegistry.getGlobalRegistry().getIcon("uddi_registry_cat_node.gif"), //$NON-NLS-1$
				statsPanel);


		//add all stuff back to contentPanel
		contentPanel.add(tabbedPane);

		//showDefaultInfo();

		tabbedPane.setSelectedIndex(0);

		refreshActions();

		if(searchResult.getTotalMatchCount() > 0){
			displayResult();
		}

		refreshCount();
	}


	/**
	 * @return
	 */
	private Component buildBarChart() {

		//new empty dataset
		 // dataset = new DefaultCategoryDataset();
		  //dataset.setValue(2, "x", "Rahul");
		  //dataset.setValue(7, "x", "Vinod");
		  //dataset.setValue(4, "x", "Deepak");

		if(ConfigRegistry.getGlobalRegistry()
				.getBoolean("plugins.errorMining.appearance.resultMatrix.useSimpleBarChart")){ //$NON-NLS-1$
		ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
		}


		//chart customization
		chart = ChartFactory.createBarChart(
				ResourceManager.getInstance().get(
						"plugins.errormining.labels.barChart.head"), //$NON-NLS-1$
				ResourceManager.getInstance().get(
						"plugins.errormining.labels.barChart.x-axis"), //$NON-NLS-1$
				ResourceManager.getInstance().get(
						"plugins.errormining.labels.barChart.y-axis"), //$NON-NLS-1$
				null, //data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false); // URLs?

		chart.setBackgroundPaint(ConfigRegistry.getGlobalRegistry()
			.getColor("plugins.errorMining.appearance.resultMatrix.defaultChartBackgroundPaint")); //$NON-NLS-1$
		chart.getTitle().setPaint(ConfigRegistry.getGlobalRegistry()
				.getColor("plugins.errorMining.appearance.resultMatrix.defaultChartTitlePaint")); //$NON-NLS-1$
		chart.setAntiAlias(true);



		// plot customization
		plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(ConfigRegistry.getGlobalRegistry()
			.getColor("plugins.errorMining.appearance.resultMatrix.defaultPlotBackgroundPaint")); //$NON-NLS-1$
		((CategoryPlot) plot).setRenderer(new CustomBarHighlightRenderer());
		//percentage test
//		plot.getRenderer().setSeriesItemLabelGenerator(1,
//				new StandardCategoryItemLabelGenerator("{3}",
//							NumberFormat.getPercentInstance())
//				);


		// only integer ticks
		NumberAxis rangeAxis = (NumberAxis) ((CategoryPlot) plot).getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRange(true);
		rangeAxis.setUpperMargin(0.15);


		((CategoryPlot) plot).setRangeGridlinePaint(ConfigRegistry
				.getGlobalRegistry()
				.getColor("plugins.errorMining.appearance.resultMatrix.defaultGridlinePaint")); //$NON-NLS-1$
		CustomBarHighlightRenderer br = (CustomBarHighlightRenderer) ((CategoryPlot) plot).getRenderer();
		br.setMaximumBarWidth(ConfigRegistry.getGlobalRegistry().getDouble(
				"plugins.errorMining.appearance.resultMatrix.defaultBarWidth")); //$NON-NLS-1$
		br.setDrawBarOutline(true);
		br.setShadowVisible(false);



		Handle handle = ConfigRegistry.getGlobalRegistry().getHandle(
				"plugins.errorMining.appearance.font"); //$NON-NLS-1$

		Font font = ConfigUtils.defaultReadFont(handle);

		CategoryItemRenderer renderer = ((CategoryPlot) plot).getRenderer();
		renderer.setBaseItemLabelGenerator(new CustomLabelGenerator());
		renderer.setBaseItemLabelFont(font);
		renderer.setBaseItemLabelsVisible(true);
		//TODO add option
		renderer.setBaseItemLabelPaint(Color.black);
		renderer.setSeriesPositiveItemLabelPosition(
								1,
								new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12,
								TextAnchor.BASELINE_CENTER));

		//add tooltip (current value of bar)
		//renderer.setSeriesToolTipGenerator(0, new CustomToolTipGenerator());
		initializeChartTooltip(renderer);

		barChartPanel = new ChartPanel(chart);
		barChartPanel.addChartMouseListener(getHandler());
		return barChartPanel;
	}



	private JFreeChart createBarChart(CategoryDataset data) {

		if(ConfigRegistry.getGlobalRegistry()
				.getBoolean("plugins.errorMining.appearance.resultMatrix.useSimpleBarChart")){ //$NON-NLS-1$
			ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
		}



		//chart customization
		chart = ChartFactory.createBarChart(
				ResourceManager.getInstance().get(
						"plugins.errormining.labels.barChart.head"), //$NON-NLS-1$
				ResourceManager.getInstance().get(
						"plugins.errormining.labels.barChart.x-axis"), //$NON-NLS-1$
				ResourceManager.getInstance().get(
						"plugins.errormining.labels.barChart.y-axis"), //$NON-NLS-1$
				data, //data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false); // URLs?



		chart.setBackgroundPaint(ConfigRegistry.getGlobalRegistry()
			.getColor("plugins.errorMining.appearance.resultMatrix.defaultChartBackgroundPaint")); //$NON-NLS-1$
		chart.setAntiAlias(true);

		// empty data check: when nothing is selected, only when switching the first
		// time to stats tab. ignore further configurations and return empty barchart
		if(data.getColumnCount() == 0 && data.getRowCount() == 0){
			chart.setTitle(""); //$NON-NLS-1$
		} else {

		chart.setTitle(formatDependencyKey((String) statisticTable.getModel().getValueAt(statisticTable.getSelectedRow(), 1)));
		}


		// plot customization
		plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(ConfigRegistry.getGlobalRegistry()
			.getColor("plugins.errorMining.appearance.resultMatrix.defaultPlotBackgroundPaint")); //$NON-NLS-1$
		((CategoryPlot) plot).setRenderer(new CustomBarHighlightRenderer());
		//percentage test
//		plot.getRenderer().setSeriesItemLabelGenerator(1,
//				new StandardCategoryItemLabelGenerator("{3}",
//							NumberFormat.getPercentInstance())
//				);


		// only integer ticks
		NumberAxis rangeAxis = (NumberAxis) ((CategoryPlot) plot).getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRange(true);
		rangeAxis.setUpperMargin(0.15);


		((CategoryPlot) plot).setRangeGridlinePaint(ConfigRegistry
				.getGlobalRegistry()
				.getColor("plugins.errorMining.appearance.resultMatrix.defaultGridlinePaint")); //$NON-NLS-1$
		CustomBarHighlightRenderer br = (CustomBarHighlightRenderer) ((CategoryPlot) plot).getRenderer();
		br.setMaximumBarWidth(ConfigRegistry.getGlobalRegistry().getDouble(
				"plugins.errorMining.appearance.resultMatrix.defaultBarWidth")); //$NON-NLS-1$
		br.setDrawBarOutline(true);
		br.setShadowVisible(false);


		Handle handle = ConfigRegistry.getGlobalRegistry().getHandle(
				"plugins.errorMining.appearance.font"); //$NON-NLS-1$

		Font font = ConfigUtils.defaultReadFont(handle);

		CategoryItemRenderer renderer = ((CategoryPlot) plot).getRenderer();
		renderer.setBaseItemLabelGenerator(new CustomLabelGenerator());
		renderer.setBaseItemLabelFont(font);
		renderer.setBaseItemLabelsVisible(true);
		//TODO add option
		renderer.setBaseItemLabelPaint(Color.black);
		renderer.setSeriesPositiveItemLabelPosition(
								1,
								new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12,
								TextAnchor.BASELINE_CENTER));

		//add tooltip (current value of bar)
		//renderer.setSeriesToolTipGenerator(0, new CustomToolTipGenerator());
		initializeChartTooltip(renderer);
		return chart;
	}

	private JFreeChart createPieChart(CategoryDataset data) {

		//NOTE: explode only supported when using 2D piecharts :-(
		/*
		 * title the chart title (null permitted).
		 * dataset the dataset (null permitted).
		 * order the order that the data is extracted (by row or by column) (null not permitted).
		 * legend include a legend?
		 * tooltips generate tooltips?
		 * urls generate URLs?
		 */

		// empty data check: when nothing is selected, only when switching the first
		// time to stats tab. ignore further configurations and return empty barchart
		if(statisticTable.getSelectedRow() == -1){
			chart = ChartFactory.createMultiplePieChart("", //$NON-NLS-1$
					data,
					TableOrder.BY_ROW,
					true,
					true,
					false);

		} else{
			chart = ChartFactory.createMultiplePieChart(
			formatDependencyKey((String) statisticTable.getModel()
					.getValueAt(statisticTable.getSelectedRow(), 1)),
			data,
			TableOrder.BY_ROW,
			true,
			true,
			false);
			//TODO change to tooltips (title entity?
//			chart = CustomMultiPie.createMultiplePieChart3D(
//					formatDependencyKey((String) statisticTable.getModel()
//							.getValueAt(statisticTable.getSelectedRow(), 1)),
//					data,
//					TableOrder.BY_ROW,
//					true,
//					true,
//					false);
		}


//		chart.setBackgroundPaint(ConfigRegistry
//				.getGlobalRegistry()
//				.getColor("plugins.errorMining.appearance.resultMatrix.defaultPlotBackgroundPaint")); //$NON-NLS-1$

		MultiplePiePlot mpp = (MultiplePiePlot) chart.getPlot();
		mpp.setDrawingSupplier(new CustomPieDrawingSupplier(), true);


		Handle handle = ConfigRegistry.getGlobalRegistry().getHandle(
				"plugins.errorMining.appearance.font"); //$NON-NLS-1$
		Font font = ConfigUtils.defaultReadFont(handle);

		JFreeChart subchart = mpp.getPieChart();

		PiePlot pp = (PiePlot) subchart.getPlot();
		pp.setBackgroundPaint(ConfigRegistry.getGlobalRegistry()
				.getColor("plugins.errorMining.appearance.resultMatrix.defaultPlotBackgroundPaint")); //$NON-NLS-1$
        pp.setLabelGenerator(new CustomPieLabelGenerator());
        pp.setToolTipGenerator(new CustomPieToolTipGenerator());

        pp.setLabelFont(font);



//        pp.setAutoPopulateSectionPaint(true);
//        pp.setAutoPopulateSectionOutlineStroke(true);
//        pp.setAutoPopulateSectionOutlinePaint(true);


        //mpp.setDrawingSupplier(new DefaultDrawingSupplier(), true);


//        for(int i = 0 ; i <= data.getRowCount(); i++){
//        	if(i % 2 == 0){
//        		pp.setBaseSectionPaint(Color.red);
//        		System.out.println("red");
//        	}else {
//        		pp.setBaseSectionPaint(Color.blue);
//        		System.out.println("blue");
//        	}
//        }
        //pp.setInteriorGap(0.30);

        return chart;
	}

	protected void initializeChartTooltip(CategoryItemRenderer renderer){
		int size = 0;

		if(((CategoryPlot) plot).getDataset() != null){
			size = ((CategoryPlot) plot).getDataset().getRowCount();
		}

		for(int i = 0; i < size; i++){
			renderer.setSeriesToolTipGenerator(i, new CustomToolTipGenerator());
		}
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
		((JSpinner.DefaultEditor) lowerBound.getEditor()).getTextField().addKeyListener(getHandler());
		lowerBound.setBorder(UIUtil.defaultContentBorder);

		upperBound = new JSpinner(ubm);
		upperBound.setMinimumSize(new Dimension(10,20));
		upperBound.setToolTipText(ResourceManager.getInstance()
					.get("plugins.errormining.nGramResultView.upperBound.description")); //$NON-NLS-1$
		((JSpinner.DefaultEditor) upperBound.getEditor()).getTextField().addKeyListener(getHandler());
		upperBound.setBorder(UIUtil.defaultContentBorder);

		textFilterField = new JTextField();
		textFilterField.setMinimumSize(new Dimension(120,20));
		textFilterField.setPreferredSize(new Dimension(120,20));
		textFilterField.setMaximumSize(new Dimension(200,20));
		textFilterField.addActionListener(getHandler());
		UIUtil.createUndoSupport(textFilterField, 10);
		UIUtil.createDefaultTextMenu(textFilterField, true);

		onlyFilterNuclei = new JCheckBox();
		onlyFilterNuclei.setToolTipText(ResourceManager.getInstance()
				.get("plugins.errormining.nGramResultView.onlyNuclei.description")); //$NON-NLS-1$
		onlyFilterNuclei.addActionListener(getHandler());

		exactTextMatch = new JCheckBox();
		exactTextMatch.setToolTipText(ResourceManager.getInstance()
				.get("plugins.errormining.nGramResultView.exactTextMatch.description")); //$NON-NLS-1$
		exactTextMatch.addActionListener(getHandler());



		ActionComponentBuilder acb = new ActionComponentBuilder(getActionManager());
		acb.setActionListId("plugins.errormining.nGramResultView.toolBarList"); //$NON-NLS-1$
		acb.addOption("resultCounter", resultCounter); //$NON-NLS-1$
		acb.addOption("textfield", textFilterField); //$NON-NLS-1$
		acb.addOption("onlyNuclei", onlyFilterNuclei); //$NON-NLS-1$
		acb.addOption("exactTextMatch", exactTextMatch); //$NON-NLS-1$
		acb.addOption("lowerBound", lowerBound); //$NON-NLS-1$
		acb.addOption("upperBound", upperBound); //$NON-NLS-1$
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

		exactTagMatch = new JCheckBox();
		exactTagMatch.setToolTipText(ResourceManager.getInstance()
				.get("plugins.errormining.nGramResultView.exactTagMatch.description")); //$NON-NLS-1$
		exactTagMatch.addActionListener(getHandler());


		statsSpinner = new JSpinner(sbm);
		statsSpinner.setMinimumSize(new Dimension(10,20));
		statsSpinner.setToolTipText(ResourceManager.getInstance()
					.get("plugins.errormining.nGramResultView.statsSpinner.description")); //$NON-NLS-1$
		((JSpinner.DefaultEditor) statsSpinner.getEditor()).getTextField().addKeyListener(getHandler());

		//----------------------------------------------------
		//percentageSwitch
		//switch chart styles (bar or pie)
		JPanel percentModePanel = new JPanel(new GridLayout(0, 2));
		percentModePanel.setMaximumSize(new Dimension(60, 30));
		//GridBagConstraints gbc = GridBagUtil.makeGbcN(0, 0, 1, 1);


	    percentModePanel.setBorder(UIUtil.emptyBorder);
		ButtonGroup percentButtonGroup = new ButtonGroup();

		//use another button to switch or keep numeric/percent toggle?

//	    JRadioButton numericButton = new JRadioButton(
//	    		buildHTMLimgLabel(IconRegistry.getGlobalRegistry()
//				.getIcon("percent.png").toString()),true); //$NON-NLS-1$
//	    numericButton.addItemListener(getHandler());
//	    numericButton.setName(numeric);
//	    percentButtonGroup.add(numericButton);
//	    percentModePanel.add(BorderLayout.WEST, numericButton);

	    JRadioButton percentAButton = new JRadioButton(
	    		buildHTMLimgLabel(IconRegistry.getGlobalRegistry()
				.getIcon("percentA.png").toString()),true); //$NON-NLS-1$
	    percentAButton.addItemListener(getHandler());
	    percentAButton.setName(percentA);
	    percentButtonGroup.add(percentAButton);
	    percentModePanel.add(percentAButton);

	    JRadioButton percentBButton = new JRadioButton(
	    		buildHTMLimgLabel(IconRegistry. getGlobalRegistry()
        		.getIcon("percentB.png").toString())); //$NON-NLS-1$
	    percentBButton.addItemListener(getHandler());
	    percentBButton.setName(percentB);
	    percentButtonGroup.add(percentBButton);
	    percentModePanel.add(percentBButton);




		//----------------------------------------------------
		//switch chart styles (bar or pie)
		JPanel chartStylePanel = new JPanel(new GridLayout(0, 2));
		chartStylePanel.setMaximumSize(new Dimension(60, 30));
		chartStylePanel.setBorder(UIUtil.emptyBorder);
		ButtonGroup buttonGroup = new ButtonGroup();

		//barchart
	    JRadioButton barChartButton = new JRadioButton(buildHTMLimgLabel(
				IconRegistry.getGlobalRegistry()
				.getIcon("chart-bar.png").toString()),true); //$NON-NLS-1$
	    barChartButton.addItemListener(getHandler());
	    barChartButton.setName(barChart);
	    buttonGroup.add(barChartButton);
	    chartStylePanel.add(barChartButton);

	    //piechart
	    JRadioButton pieChartButton = new JRadioButton(buildHTMLimgLabel(
        		IconRegistry. getGlobalRegistry()
        		.getIcon("chart-pie.png").toString())); //$NON-NLS-1$
	    pieChartButton.addItemListener(getHandler());
	    pieChartButton.setName(pieChart);
	    buttonGroup.add(pieChartButton);
	    chartStylePanel.add(pieChartButton);


		ActionComponentBuilder acb = new ActionComponentBuilder(getActionManager());
		acb.setActionListId("plugins.errormining.nGramResultView.toolBarStatistic"); //$NON-NLS-1$
		acb.addOption("statisticCounter", statisticCounter); //$NON-NLS-1$
		acb.addOption("statisticTextFilterField", statisticTextFilterField); //$NON-NLS-1$
		acb.addOption("exactTagMatch", exactTagMatch); //$NON-NLS-1$
		acb.addOption("statsSpinner", statsSpinner); //$NON-NLS-1$
		acb.addOption("chartStyleSwitch", chartStylePanel); //$NON-NLS-1$
		acb.addOption("percenModeSwitch", percentModePanel); //$NON-NLS-1$

		toolBarStatistic = acb.buildToolBar();


		return toolBarStatistic;
	}

	/**
	 * @param location
	 * @return
	 */
	private String buildHTMLimgLabel(String location) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><img src='"); //$NON-NLS-1$
        sb.append(location);
        sb.append("' width=16 height=16>"); //$NON-NLS-1$
		return sb.toString();
	}

	/**
	 *
	 */
	private void initializeSpinners() {
		int minimum = 1;
		int maximum = Math.max(maximumGramsize-1,minimum);
		if(!isPoSErrorMiningResult()){
			minimum = minDependencyGram;
		}


		//lower bound spinner (overview)
		if(lbm == null){
			lbm = new SpinnerNumberModel(minimum, //initial value
					minimum, //min
	                maximum, //max
	                1);          //step
		} else {
			//lbm.setMinimum(Math.max(minimumGramsize, minimum));
			lbm.setMaximum(maximum);
			lbm.setValue(minimum);
		}


		//upper bound spinner (overview)
		if(ubm == null){
			ubm = new SpinnerNumberModel(maximum, //initial value
			minimum, //min
			maximum, //max
			1);          //step
		} else {
			ubm.setMaximum(maximum);
			ubm.setValue(maximum);
		}



		//statistic spinner
		if(sbm == null){
			sbm = new SpinnerNumberModel(minimum, //initial value
			minimum, //min
			maximum, //max
			1);          //step
		} else {
			sbm.setMaximum(maximum);
			sbm.setValue(minimum);

		}

	}

	//	protected void displaySelectedData() throws Exception {
	//		if(listPresenter==null || detailsPresenter==null) {
	//			return;
	//		}
	//
	//		ListSelectionModel selectionModel = listPresenter.getSelectionModel();
	//
	//		if(selectionModel.getValueIsAdjusting()) {
	//			return;
	//		}
	//
	//		int selectedIndex = selectionModel.getMinSelectionIndex();
	//		Object selectedObject = null;
	//
	//		if(selectedIndex!=-1) {
	//			selectedObject = listPresenter.getListModel().getElementAt(selectedIndex);
	//		}
	//
	//		if(selectedObject==null) {
	//			detailsPresenter.clear();
	//			return;
	//		}
	//
	//		// Display selected object in details presenter
	//		Options options = new Options();
	//		options.put(Options.INDEX, selectedIndex);
	//		options.put(Options.CONTENT_TYPE, listPresenter.getContentType());
	//		options.putAll(getOptions());
	//
	//		detailsPresenter.present(selectedObject, options);
	//	}

		private void refreshCount(){

		if(resultCounter == null){
			resultCounter = new JLabel("0"); //$NON-NLS-1$
		}

		if(ngramListModel == null){
			resultCounter.setText("0"); //$NON-NLS-1$
		} else {
			String count = StringUtil.formatDecimal(ngramListModel.getSize());
			resultCounter.setText(count);
		}
	}

	/**
	 *
	 */
	private void refreshStatisticCount() {
		if(statisticTableModel.getRowCount() == 0){
			statisticCounter.setText("0"); //$NON-NLS-1$
		} else {
			String count = StringUtil.formatDecimal(statisticTableModel.getRowCount());
			statisticCounter.setText(count);
		}
		refreshChart("",null); //$NON-NLS-1$
	}

	//	protected void displaySelectedData() throws Exception {
	//		if(listPresenter==null || detailsPresenter==null) {
	//			return;
	//		}
	//
	//		ListSelectionModel selectionModel = listPresenter.getSelectionModel();
	//
	//		if(selectionModel.getValueIsAdjusting()) {
	//			return;
	//		}
	//
	//		int selectedIndex = selectionModel.getMinSelectionIndex();
	//		Object selectedObject = null;
	//
	//		if(selectedIndex!=-1) {
	//			selectedObject = listPresenter.getListModel().getElementAt(selectedIndex);
	//		}
	//
	//		if(selectedObject==null) {
	//			detailsPresenter.clear();
	//			return;
	//		}
	//
	//		// Display selected object in details presenter
	//		Options options = new Options();
	//		options.put(Options.INDEX, selectedIndex);
	//		options.put(Options.CONTENT_TYPE, listPresenter.getContentType());
	//		options.putAll(getOptions());
	//
	//		detailsPresenter.present(selectedObject, options);
	//	}



	/**
	 *
	 * @param title
	 * @param list
	 */
	private void refreshChart(String title, List<StatsData> list) {

		chart.setTitle(formatDependencyKey(title));

		if(list == null){
			((CategoryPlot) plot).setDataset(0, new DefaultCategoryDataset());
			return;
		}


		//clear before adding new data
		DefaultCategoryDataset dataset = new DefaultCategoryDataset(); //.clear();
		StringBuilder sb = new StringBuilder();


		for(int i = 0; i < list.size(); i++){
			StatsData sd = list.get(i);
			//clear old sb
			sb.setLength(0);
			sb.append(sd.getTagKey())
					.append(" (") //$NON-NLS-1$
					.append(sd.getCount())
					.append(")"); //$NON-NLS-1$
			//row compare key "x" compare always to one value
			//dataset.addValue(sd.getCount(), String.valueOf(sd.getCount()), formatDependencyKey(sd.getTagKey()));
			dataset.addValue(sd.getCount(),
					labelmatrix,
					formatDependencyKey(sd.getTagKey()));
		}

		initializeChartTooltip(((CategoryPlot) plot).getRenderer());

//		for(int d = 0; d < dataset.getRowCount(); d++){
//			plot.getRenderer().setSeriesPaint(d, Color.red);
//		}
		setCategoryChartData(dataset);

	}


	private void setCategoryChartData(DefaultCategoryDataset dataset){
		//switch between barchart and piechart
		if(chart.getPlot() instanceof MultiplePiePlot){

			((CategoryPlot) plot).setDataset(0, dataset);
			//System.out.println("mp");

			MultiplePiePlot mpp = (MultiplePiePlot) chart.getPlot();
			//mpp.setDrawingSupplier(new DefaultDrawingSupplier());
			//reset dataset
			mpp.setDataset(null);
			mpp.setDataset(dataset);

//			JFreeChart subchart = mpp.getPieChart();

//			PiePlot pp = (PiePlot) subchart.getPlot();
//			pp.clearSectionPaints(true);

//			PiePlot pp = (PiePlot) subchart.getPlot();

			//System.out.println("PI " +pp.getPieIndex());
			//System.out.println("DSC " +mpp.getDataset().getRowCount());
//			//TODO fix title entitys....
//			System.out.println("PieIndex: " + 	pp.getPieIndex());
//			System.out.println("SCC " + subchart.getSubtitleCount());
//
//
//			for(int i = 0; i < mpp.getDataset().getRowCount(); i++){
//				//subchart.setTitle("Index " + i);
//				//subchart.getTitle().setToolTipText("Index " + i);
//				//subchart.addSubtitle(i, t);
//			}

			//System.out.println(chart.getSubtitles().get(0).toString());
			//System.out.println(mpp.getDrawingSupplier());



		} else {
			//System.out.println("bc");
			((CategoryPlot) plot).setDataset(0, dataset);
		}

	}

	/**
	 *
	 * @param dcd
	 * @param listAdd
	 * @param unique
	 * @return
	 */
	private DefaultCategoryDataset buildDefaultCategoryDataset(DefaultCategoryDataset dcd, List<StatsData> listAdd, String unique) {

		for(int i = 0; i < listAdd.size(); i++){
			StatsData sd = listAdd.get(i);
			//System.out.println(sd.getCount());
			dcd.addValue(sd.getCount(), unique, formatDependencyKey(sd.getTagKey()));
		}
		return dcd;
	}


	private void resetFilters(){
		textFilterField.setText(null);
		lbm.setValue(1);
		ubm.setValue(ubm.getMaximum());
		onlyFilterNuclei.setSelected(false);
		exactTextMatch.setSelected(false);
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
		statsSpinner.setValue(1);
		exactTagMatch.setSelected(false);
		refreshChart("",null); //$NON-NLS-1$
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
		if(!statisticTextFilterField.getText().equals("")){ //$NON-NLS-1$
			statsResultFiltered.clear();
			String filter = statisticTextFilterField.getText();
			filter = filter.replace("->", "_R"); //$NON-NLS-1$ //$NON-NLS-2$
			filter = filter.replace("<-", "_L");  //$NON-NLS-1$//$NON-NLS-2$
			for (String key : statsResult.keySet()) {
				//System.out.println(key);
//				String tmp = key.replace("[", "");
//				tmp = tmp.replace("]", "");
//				String[] sa = tmp.split(", ");
//
//				for(String s : sa){
//					System.out.println(s.matches(filter) + " Key: " + s +" Filter: " + filter);
//					//System.out.println(s.contains(filter) + " Key: " + s +" Filter: " + filter);
//					if(s.matches(filter)){
//						statsResultFiltered.put(key, statsResult.get(key));
//					}
//				}

				String tmp = key.replace("[", ""); //$NON-NLS-1$ //$NON-NLS-2$
				tmp = tmp.replace("]", "");  //$NON-NLS-1$ //$NON-NLS-2$

				//allow multiple tag filtering

					int matches = 0;
					String[] mfArray = filter.split(" "); //$NON-NLS-1$

					for(String s : tmp.split(", ")){ //$NON-NLS-1$

						for(String multifilter : mfArray){
							//System.out.println(multifilter);
							//System.out.println(s.contains(multifilter) + " Filter: " + multifilter);
							if (exactTagMatch(s, multifilter)) {
								matches++;
							}
						}
					}

					if(matches == mfArray.length){
						statsResultFiltered.put(key, statsResult.get(key));
					}
			}
		} else {
			statsResultFiltered.clear();
			statsResultFiltered.putAll(statsResult);
		}

		//clear list when no results
		if(statsResultFiltered.size() == 0){
			if(statisticListModel != null){
				statisticListModel.removeAll();
			}
		}
	}


	private boolean exactTagMatch(String tag, String filter){
		if(exactTagMatch.isSelected()){
			return tag.matches(filter);
		}else {
			return tag.contains(filter);
		}
	}


	private boolean exactTextMatch(String tag, String filter){
		if(exactTextMatch.isSelected()){
			return tag.matches(filter);
		}else {
			return tag.contains(filter);
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

			//System.out.println("USE"+onlyNuclei.isSelected());
			//Check if we should also use textfilter
			if(!textFilterField.getText().equals("")){ //$NON-NLS-1$
				//if(key.contains(textFilterField.getText())){
				if(exactTextMatch(key, textFilterField.getText())){
					if(currentSize >= minimumGramsize
							&& currentSize <= maximumGramsize){
						//System.out.println(key + isNuclei(key.split(" ")[1]));
//						System.out.println(key.split(" ")[1]
//											+ " "
//											//+ nGramResult.keySet().contains(key.split(" ")[1]
//											+ (key.split(" ")[1].contains(textFilterField.getText())));

						//only filter for nuclei items
						if(onlyFilterNuclei.isSelected()){
							for(int k = 0; k < currentSize; k++){
								if(isNucleiItem(k, nGramResult.get(key))){
									//regex
									if(exactTextMatch(key.split(" ")[k], textFilterField.getText())){ //$NON-NLS-1$
										nGramResultFiltered.put(key, nGramResult.get(key));
									}
//									if(key.split(" ")[k].contains(textFilterField.getText())){ //$NON-NLS-1$
//										nGramResultFiltered.put(key, nGramResult.get(key));
//									}

								}
								//System.out.println(key.split(" ")[k] + " "+ isNuclei(s));
							}
						} else {
							nGramResultFiltered.put(key, nGramResult.get(key));
						}
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


						// only filter for nuclei items
						if (onlyFilterNuclei.isSelected()) {
							if(isDependencyNucleus(key, textFilterField.getText())){
								nGramResultFilteredDependency.put(key, nGramResultDependency.get(key));
							}

						} else {
							nGramResultFilteredDependency.put(key, nGramResultDependency.get(key));
						}

						//		nGramResultFilteredDependency.put(key, nGramResultDependency.get(key));
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


//	private void showDefaultInfo() {
//		scrollPane.setViewportView(infoLabel);
//		//headerInfo.setText("NGrams: " + ngramListModel.getSize()); //$NON-NLS-1$
//	}

	private void showDetails(SentenceDataList sentenceList){

		int index = tabbedPane.getTabCount();


		String title = (String) ngramList.getSelectedValue();

		if(title == null){
			if(statisticListModel.getSize() == 1){
				title = (String) statisticListModel.getElementAt(0);
			} else {
				title = (String) statisticList.getSelectedValue();
			}
		}


		SubNgramResultContainer subresult = new SubNgramResultContainer(title, sentenceList);
		subresult.init();

		tabbedPane.insertTab(createTabTitle(title), null, subresult, UIUtil.toSwingTooltip(title), index);
		tabbedPane.setTabComponentAt(index, new ButtonTabComponent(tabbedPane));
		tabbedPane.setSelectedIndex(index);

		//display data using default presenter after tab is created
		try {
			//for default presenter
			displaySelectedTabData();
		} catch(Exception ex) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to display detailed data with awt presenter: "+ex, ex); //$NON-NLS-1$
		}
	}

	private void showDetails(SentenceDataList sentenceList, String title){

		int index = tabbedPane.getTabCount();


		SubNgramResultContainer subresult = new SubNgramResultContainer(title, sentenceList);
		subresult.init();

		tabbedPane.insertTab(createTabTitle(title), null, subresult, UIUtil.toSwingTooltip(title), index);
		tabbedPane.setTabComponentAt(index, new ButtonTabComponent(tabbedPane));
		tabbedPane.setSelectedIndex(index);

		//display data using default presenter after tab is created
		try {
			//for default presenter
			displaySelectedTabData();
		} catch(Exception ex) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to display detailed data with awt presenter: "+ex, ex); //$NON-NLS-1$
		}
	}


	/**
	 * @param title
	 * @return
	 */
	private String createTabTitle(String title) {
		StringBuilder sb = new StringBuilder();

		int maxlength = Math.min(title.length(), maximumCharPerTab);

		sb.append("(").append(title.split(" ").length) //$NON-NLS-1$ //$NON-NLS-2$
				.append("-Gram) ") //$NON-NLS-1$
				.append(title.subSequence(0, maxlength));

		//
		if(title.length() > maximumCharPerTab){
			sb.append(" [...]"); //$NON-NLS-1$
		}

		return sb.toString();
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

		//List<StatsData> tmp = (List<StatsData>) statisticTable.getValueAt(selectedRow, 1);
		//System.out.println(tmp.get(0));
		String key = (String) statisticTable.getModel().getValueAt(selectedRow, 1);
		refreshChart(key, statsResultFiltered.get(key));
		statisticTableModel.generateListEntryFromString(key);
		statisticList.clearSelection();
	}

//	protected void displaySelectedData() throws Exception {
//		if(listPresenter==null || detailsPresenter==null) {
//			return;
//		}
//
//		ListSelectionModel selectionModel = listPresenter.getSelectionModel();
//
//		if(selectionModel.getValueIsAdjusting()) {
//			return;
//		}
//
//		int selectedIndex = selectionModel.getMinSelectionIndex();
//		Object selectedObject = null;
//
//		if(selectedIndex!=-1) {
//			selectedObject = listPresenter.getListModel().getElementAt(selectedIndex);
//		}
//
//		if(selectedObject==null) {
//			detailsPresenter.clear();
//			return;
//		}
//
//		// Display selected object in details presenter
//		Options options = new Options();
//		options.put(Options.INDEX, selectedIndex);
//		options.put(Options.CONTENT_TYPE, listPresenter.getContentType());
//		options.putAll(getOptions());
//
//		detailsPresenter.present(selectedObject, options);
//	}

	protected void displaySelectedTabData() throws Exception {
		if(tabbedPane.getSelectedComponent() instanceof SubNgramResultContainer){
			SubNgramResultContainer subresult = (SubNgramResultContainer) tabbedPane.getSelectedComponent();
			subresult.displaySelectedData();
		}
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


	protected boolean isDependencyNucleus(String key, String filter){
		List<DependencyItemInNuclei> diinList = nGramResultDependency.get(key);
		int sentenceStart = diinList.get(0).getSentenceInfoAt(0).getSentenceBegin();
		int sentenceNucleiIndex = diinList.get(0).getSentenceInfoAt(0).getNucleiIndex();

		int colorOffset = diinList.get(0).getSentenceInfoAt(0).getSentenceBegin();

		if(key.split(" ").length == 2){ //$NON-NLS-1$
			if(sentenceNucleiIndex-sentenceStart > 1){
				colorOffset = sentenceNucleiIndex;
			}
		}

		for(int c = 0; c < key.split(" ").length; c++){ //$NON-NLS-1$
			if(isDependencyNuclei(c, diinList.get(0).getSentenceInfoAt(0), colorOffset)){
				if(key.split(" ")[c].contains(filter)){ //$NON-NLS-1$
					return true;
				}
			}
			else if(isDependencyHead(c, diinList.get(0).getSentenceInfoAt(0), key.split(" ").length, colorOffset)){ //$NON-NLS-1$
				if(key.split(" ")[c].contains(filter)){ //$NON-NLS-1$
					return true;
				}
			}
		}
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
			int sNO = iin.getSentenceInfoAt(i).getSentenceNr();
			sb.append(sNO);
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
//		StringBuilder sb = new StringBuilder();
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
	 * @param value
	 * @param value
	 * @return
	 */
	private String colorStringArray(String[] s, String value) {
		StringBuilder sb = new StringBuilder();

		//length = 1 -> we have nucleus always color nucleus
		if(s.length == 1){
			String hex = "#" + Integer.toHexString(nucleusColor.getRGB()).substring(2); //$NON-NLS-1$
			// System.out.println(hex);
			sb.append("<font color=" + hex + ">"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(s[0]);
			sb.append("</font>"); //$NON-NLS-1$

		} else {
			for (int i = 0; i < s.length; i++) {
				if (isNucleiItem(i, nGramResult.get(value))) {
					//fixme neues data model wieder aktivieren
					String hex = "#" + Integer.toHexString(nucleusColor.getRGB()).substring(2); //$NON-NLS-1$
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
		}
		return sb.toString();
	}

	private String colorStringDependency(String key, ArrayList<Integer> arrayList) {
		StringBuilder sb = new StringBuilder();
		String[] splittedKey = key.split(" ");  //$NON-NLS-1$

		//System.out.println("COLORDEP " +key);
		List<DependencyItemInNuclei> diinList = nGramResultDependency.get(key);
		int sentenceStart = diinList.get(0).getSentenceInfoAt(0).getSentenceBegin();
		int sentenceNucleiIndex = diinList.get(0).getSentenceInfoAt(0).getNucleiIndex();
		//int sentenceEnd = diinList.get(0).getSentenceInfoAt(0).getSentenceEnd();

		int colorOffset = diinList.get(0).getSentenceInfoAt(0).getSentenceBegin();

		if(splittedKey.length == 2){
			if(sentenceNucleiIndex-sentenceStart > 1){
				colorOffset = sentenceNucleiIndex;
			}
		}

		boolean space = spacingNeeded(diinList, splittedKey.length);

//		System.out.println(key);
//		System.out.println("Start"+ sentenceStart);
//		System.out.println("Nuclei"+ sentenceNucleiIndex);
//		System.out.println(sentenceNucleiIndex-sentenceStart);
		for(int c = 0; c < splittedKey.length; c++){
			if(isDependencyNuclei(c, diinList.get(0).getSentenceInfoAt(0), colorOffset)){
				String hex = "#" + Integer.toHexString(nucleusColor.getRGB()).substring(2); //$NON-NLS-1$
				// System.out.println(hex);
				sb.append("<font color=" + hex + ">"); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append(splittedKey[c]);
				sb.append(" "); //$NON-NLS-1$
				sb.append("</font>"); //$NON-NLS-1$
				if(space){
					sb.append("[...] "); //$NON-NLS-1$
					space = false;
				}
			}
			else if(isDependencyHead(c, diinList.get(0).getSentenceInfoAt(0), splittedKey.length, colorOffset)){
				String hex = "#" + Integer.toHexString(headColor.getRGB()).substring(2); //$NON-NLS-1$
				sb.append("<font color=" + hex + ">"); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append(splittedKey[c]);
				sb.append(" "); //$NON-NLS-1$
				sb.append("</font>"); //$NON-NLS-1$
			}
			else {
				sb.append(splittedKey[c]);
				sb.append(" "); //$NON-NLS-1$
			}
		}

		return sb.toString();
	}

	/**
	 * @param relationLabel
	 * @return
	 */
	private String createColoredHTMLString(String relationLabel, Color color) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>"); //$NON-NLS-1$
		String hex = "#" + Integer.toHexString(color.getRGB()).substring(2); //$NON-NLS-1$
		// System.out.println(hex);
		sb.append("<font color=" + hex + ">"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(relationLabel);
		sb.append(" "); //$NON-NLS-1$
		sb.append("</font>"); //$NON-NLS-1$
		sb.append("<html>"); //$NON-NLS-1$
		return sb.toString();
	}

	/**
	 * @param diinList
	 * @param length
	 */
	private boolean spacingNeeded(List<DependencyItemInNuclei> diinList, int length) {

		for(int i = 0; i < diinList.size(); i++){
			DependencyItemInNuclei diin = diinList.get(i);
			for(int s = 0; s < diin.getSentenceInfoSize(); s++){
				SentenceInfo si = diin.getSentenceInfoAt(s);
				int cover = si.getSentenceEnd() - si.getSentenceBegin() + 1;

				if(cover > length){
					return true;
				}
				//System.out.println(cover + "#" + length);
			}
		}
		return false;
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


	private boolean isDependencyHead(int index, DependencySentenceInfo dsi, int length, int colorOffset) {

		for(int n = 0; n < dsi.getNucleiIndexListSize(); n++){

			//DataList<?> dl = ((AbstractSearchResult) searchResult).getTarget();
			//DependencySentenceData dd = (DependencySentenceData) dl.get(0);

			//FIXME painting
			int head = dsi.getSentenceHeadIndex();
			//int offset = headIndex - dsi.getSentenceBegin();
			//int nucleus = dsi.getNucleiIndexListAt(n)-1;
			//int head = dd.getHead(nucleus) + 1;
			int offset = head - dsi.getSentenceBegin();

//			System.out.println("colorOffset: " + colorOffset);
//			System.out.println("head: " + dsi.getSentenceHeadIndex());
//			System.out.println("head: " + head);
//			System.out.println(dd.getForm(nucleus) + " Nucleus: " + nucleus + " " + dsi.getSentenceNr());
//			System.out.println("off: " + offset + "vs" + (dsi.getSentenceEnd()-dsi.getSentenceBegin()));
//			System.out.println("index: " + index);


			if(offset < (dsi.getSentenceEnd()-dsi.getSentenceBegin())){
				//offset = headIndex - dsi.getSentenceBegin();
			}


			if(length == 2){
				return true;
			}

			if (index == offset){
				return true;
			}

		}
		return false;
	}

	/**
	 *
	 * @param nucleusIndex
	 * @param sentenceNo
	 * @return
	 */
	private int getHeadIndexFromNucleus(int nucleusIndex, int sentenceNo){
		DataList<?> dl = ((AbstractSearchResult) searchResult).getTarget();
		DependencySentenceData dd = (DependencySentenceData) dl.get(sentenceNo);
		return dd.getHead(nucleusIndex);
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
		actionManager.addHandler(
				"plugins.errormining.nGramResultView.toggleNumberCompareModeAction", //$NON-NLS-1$
				callbackHandler, "toggleNumberCompareMode"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errormining.nGramResultView.toggleNumberModeAction", //$NON-NLS-1$
				callbackHandler, "toggleNumberMode"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errormining.nGramResultView.toggleChartDisplayModeAction", //$NON-NLS-1$
				callbackHandler, "toggleChartDisplayMode"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errormining.nGramResultView.exportBarchart", //$NON-NLS-1$
				callbackHandler, "exportBarchart"); //$NON-NLS-1$

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
		// get corpus
		DataList<?> dl = ((AbstractSearchResult) searchResult).getTarget();
		List<Integer> sIdx = new ArrayList<Integer>();

		// System.out.println("selectedKey " + key);

		// pos search
		if (isPoSErrorMiningResult()) {
			ArrayList<ItemInNuclei> iinList = nGramResult.get(key);

			// System.out.println("keysize " + key.split(" ").length);

			if (key.split(" ").length == 1) { //$NON-NLS-1$
				for (int i = 0; i < iinList.size(); i++) {
					ItemInNuclei iin = iinList.get(i);

					for (int s = 0; s < iin.getSentenceInfoSize(); s++) {
						int sentenceNr = iin.getSentenceInfoAt(s)
								.getSentenceNr();

						AnnotatedDependencyData annoDepData = new AnnotatedDependencyData(
								(DependencySentenceData) dl.get(sentenceNr));

						List<Integer> hlIndex = new ArrayList<Integer>();
						hlIndex.add(iin.getSentenceInfoAt(s).getNucleiIndex() - 1);

						int[] hlIndexArray = new int[hlIndex.size()];

						for (int h = 0; h < hlIndex.size(); h++) {
							hlIndexArray[h] = hlIndex.get(h);
						}

						DefaultNGramHighlight defaultGHL = new DefaultNGramHighlight(
								hlIndexArray, false);
						annoDepData.setAnnotation(new NGramAnnotation(
								defaultGHL));

						sentenceDataDetailedList.add(annoDepData);
					}
				}

			} else {

				for (int i = 0; i < iinList.size(); i++) {
					ItemInNuclei iin = iinList.get(i);

					for (int s = 0; s < iin.getSentenceInfoSize(); s++) {
						int sentenceNr = iin.getSentenceInfoAt(s)
								.getSentenceNr();


						// SentenceData sentenceData = (SentenceData)
						// dl.get(sentenceNr);
						// System.out.println("SentenceData: "+
						// sentenceData.getText());
						// sentenceDataDetailedList.add(sentenceData);

						AnnotatedDependencyData annoDepData = new AnnotatedDependencyData(
								(DependencySentenceData) dl.get(sentenceNr));

						List<Integer> hlIndex = new ArrayList<Integer>();
						List<String> nucleusList = getNucleusList(key, involvedSentences((String) key));
						//System.out.println(key);
						//System.out.println(nucleusList);


						int start = iin.getSentenceInfoAt(s).getSentenceBegin()-1;
						int end = iin.getSentenceInfoAt(s).getSentenceEnd();

						for (int j = start; j < end; j++) {
							String currentForm = annoDepData.getForm(j);

							if (isNucleiItem(j-start, nGramResult.get(key))) {
								//System.out.println("fail" + currentForm);
								// add new index
								hlIndex.add(j);
//								 System.out.println("NUCLEUS " +
//								 annoDepData.getForm(j)
//								 + "Position " + (j+1)
//								 + " Satz " + (sentenceNr+1));
							} else if (nucleusList
									.contains("[number-wildcard]")) { //$NON-NLS-1$
								if (numberPattern.matcher(currentForm).find()) {
									hlIndex.add(j);
								}
							}
						}

						int[] hlIndexArray = new int[hlIndex.size()];

						for (int h = 0; h < hlIndex.size(); h++) {
							hlIndexArray[h] = hlIndex.get(h);
						}

						DefaultNGramHighlight defaultGHL = new DefaultNGramHighlight(
								hlIndexArray, false);
						annoDepData.setAnnotation(new NGramAnnotation(
								defaultGHL));

						sentenceDataDetailedList.add(annoDepData);

					}
				}
			}
		}

		// dependency
		else {
			ArrayList<DependencyItemInNuclei> diinList = nGramResultDependency
					.get(key);

			// System.out.println("clicked " + key);
			for (int i = 0; i < diinList.size(); i++) {

				DependencyItemInNuclei diin = diinList.get(i);
				//System.out.println(diin.getPosTag());

				for (int s = 0; s < diin.getSentenceInfoSize(); s++) {
					int sentenceNr = diin.getSentenceInfoAt(s).getSentenceNr();
					// SentenceData sentenceData = (SentenceData)
					// dl.get(diin.getSentenceInfoAt(s).getSentenceNr());
					// System.out.println(sentenceData.getText() + "TEXT");

					AnnotatedDependencyData annoDepData = new AnnotatedDependencyData(
							(DependencySentenceData) dl.get(sentenceNr));

					int start = diin.getSentenceInfoAt(s).getSentenceBegin()-1;
					int end = diin.getSentenceInfoAt(s).getSentenceEnd()-1;

					List<Integer> hlIndex = null;
					if(diin.getPosTag().equals(nilString)){
						hlIndex = new ArrayList<Integer>();
						int id;
						for(int n = 0; n < diin.getSentenceInfoAt(s).getNucleiIndexListSize(); n++){
							id =  inRange(start,end,diin.getSentenceInfoAt(s).getNucleiIndexListAt(n)-1, annoDepData);
							//hlIndex.add(diin.getSentenceInfoAt(s).getNucleiIndexListAt(n)-1);
							hlIndex.add(id);
						}
						id = inRange(start,end,diin.getSentenceInfoAt(s).getSentenceHeadIndex()-1, annoDepData);
						//hlIndex.add(diin.getSentenceInfoAt(s).getSentenceHeadIndex()-1);
						hlIndex.add(id);
					}else {
						hlIndex = getNucleusDependencyNr(key,
							diin.getSentenceInfoAt(s));
					}

					// System.out.println("KEY"+involvedSentences((String)
					// key));
					// System.out.println("NLL"+getNucleusDependency(key,
					// diin));
					// System.out.println("NLL"+getNucleusDependencyNr(key,
					// diin.getSentenceInfoAt(s)));

					int[] hlIndexArray = new int[hlIndex.size() * 2];
					int index = 0;
					for (int h = 0; h < hlIndex.size(); h++) {
						hlIndexArray[index] = hlIndex.get(h);
						hlIndexArray[index + 1] = annoDepData.getHead(hlIndex
								.get(h));
						index = index + 2;
					}

					if (!sIdx.contains(sentenceNr)) {
						sIdx.add(sentenceNr);
						// System.out.println(hlIndexArray[0] + " "
						// + hlIndexArray[1]);
						DefaultNGramHighlight defaultGHL = new DefaultNGramHighlight(
								hlIndexArray, true);
						annoDepData.setAnnotation(new NGramAnnotation(defaultGHL));

						sentenceDataDetailedList.add(annoDepData);
					}
				}
			}
		}
		DetailedNGramSentenceDataList dsdl = new DetailedNGramSentenceDataList(
				sentenceDataDetailedList);
		return dsdl;
	}

	/**
	 * @param start
	 * @param end
	 * @param index
	 * @param annoDepData
	 * @return
	 */
	private int inRange(int start, int end, int index,
			AnnotatedDependencyData add) {
		if(index < start || index > end){
			String wordform = add.getForm(index);
			for(int i = start; i < end; i++){
				if(add.getForm(i).equals(wordform)){
					return i;
				}
			}
		}
		return index;
	}

	/**
	 * @param key
	 * @return
	 * @return
	 */
	private SentenceDataList createDetailList(String key, String label) {
		List<SentenceData> sentenceDataDetailedList = new ArrayList<SentenceData>();
		// get corpus
		DataList<?> dl = ((AbstractSearchResult) searchResult).getTarget();
		List<Integer> sIdx = new ArrayList<Integer>();
		// System.out.println("selectedKey " + key);

		// pos search
		if (isPoSErrorMiningResult()) {
			ArrayList<ItemInNuclei> iinList = nGramResult.get(key);

			// System.out.println("keysize " + key.split(" ").length);

			if (key.split(" ").length == 1) { //$NON-NLS-1$
				for (int i = 0; i < iinList.size(); i++) {
					ItemInNuclei iin = iinList.get(i);

					if(iin.getPosTag().equals(label)){

						for (int s = 0; s < iin.getSentenceInfoSize(); s++) {
							int sentenceNr = iin.getSentenceInfoAt(s)
									.getSentenceNr();

							AnnotatedDependencyData annoDepData = new AnnotatedDependencyData(
									(DependencySentenceData) dl.get(sentenceNr));

							List<Integer> hlIndex = new ArrayList<Integer>();
							hlIndex.add(iin.getSentenceInfoAt(s).getNucleiIndex() - 1);

							int[] hlIndexArray = new int[hlIndex.size()];

							for (int h = 0; h < hlIndex.size(); h++) {
								hlIndexArray[h] = hlIndex.get(h);
							}

							DefaultNGramHighlight defaultGHL = new DefaultNGramHighlight(
									hlIndexArray, false);
							annoDepData.setAnnotation(new NGramAnnotation(
									defaultGHL));
							sentenceDataDetailedList.add(annoDepData);
						}
					}
				}

			} else {

				for (int i = 0; i < iinList.size(); i++) {
					ItemInNuclei iin = iinList.get(i);

					if(iin.getPosTag().equals(label)){
						for (int s = 0; s < iin.getSentenceInfoSize(); s++) {
							int sentenceNr = iin.getSentenceInfoAt(s)
									.getSentenceNr();
							// SentenceData sentenceData = (SentenceData)
							// dl.get(sentenceNr);
							// System.out.println("SentenceData: "+
							// sentenceData.getText());
							// sentenceDataDetailedList.add(sentenceData);

							AnnotatedDependencyData annoDepData = new AnnotatedDependencyData(
									(DependencySentenceData) dl.get(sentenceNr));

							List<Integer> hlIndex = new ArrayList<Integer>();
							List<String> nucleusList = getNucleusList(key,
									involvedSentences((String) key));

							int start = iin.getSentenceInfoAt(s).getSentenceBegin()-1;
							int end = iin.getSentenceInfoAt(s).getSentenceEnd();

							for (int j = start; j < end; j++) {
								String currentForm = annoDepData.getForm(j);

								if (isNucleiItem(j-start, nGramResult.get(key))) {
									//System.out.println("fail" + currentForm);
									// add new index
									hlIndex.add(j);
									// System.out.println("NUCLEUS " +
									// annoDepData.getForm(j)
									// + "Position " + (j+1)
									// + " Satz " + (sentenceNr+1));
								} else if (nucleusList
										.contains("[number-wildcard]")) { //$NON-NLS-1$
									if (numberPattern.matcher(currentForm).find()) {
										hlIndex.add(j);
									}
								}
							}

							int[] hlIndexArray = new int[hlIndex.size()];

							for (int h = 0; h < hlIndex.size(); h++) {
								hlIndexArray[h] = hlIndex.get(h);
							}

							DefaultNGramHighlight defaultGHL = new DefaultNGramHighlight(
									hlIndexArray, false);
							annoDepData.setAnnotation(new NGramAnnotation(
									defaultGHL));

							sentenceDataDetailedList.add(annoDepData);

						}
					}
				}
			}
		}

		// dependency
		else {
			ArrayList<DependencyItemInNuclei> diinList = nGramResultDependency
					.get(key);

			// System.out.println("clicked " + key);
			for (int i = 0; i < diinList.size(); i++) {

				DependencyItemInNuclei diin = diinList.get(i);

				if(diin.getPosTag().equals(label)){

					for (int s = 0; s < diin.getSentenceInfoSize(); s++) {
						int sentenceNr = diin.getSentenceInfoAt(s).getSentenceNr();
						// SentenceData sentenceData = (SentenceData)
						// dl.get(diin.getSentenceInfoAt(s).getSentenceNr());
						// System.out.println(sentenceData.getText() + "TEXT");

						AnnotatedDependencyData annoDepData = new AnnotatedDependencyData(
								(DependencySentenceData) dl.get(sentenceNr));

						int start = diin.getSentenceInfoAt(s).getSentenceBegin()-1;
						int end = diin.getSentenceInfoAt(s).getSentenceEnd()-1;

						List<Integer> hlIndex = null;
						if(diin.getPosTag().equals(nilString)){
							hlIndex = new ArrayList<Integer>();
							int id;
							for(int n = 0; n < diin.getSentenceInfoAt(s).getNucleiIndexListSize(); n++){
								id =  inRange(start,end,diin.getSentenceInfoAt(s).getNucleiIndexListAt(n)-1, annoDepData);
								//hlIndex.add(diin.getSentenceInfoAt(s).getNucleiIndexListAt(n)-1);
								hlIndex.add(id);
							}
							id = inRange(start,end,diin.getSentenceInfoAt(s).getSentenceHeadIndex()-1, annoDepData);
							//hlIndex.add(diin.getSentenceInfoAt(s).getSentenceHeadIndex()-1);
							hlIndex.add(id);
						}else {
							hlIndex = getNucleusDependencyNr(key,
								diin.getSentenceInfoAt(s));
						}

//						List<Integer> hlIndex = getNucleusDependencyNr(key,
//								diin.getSentenceInfoAt(s));
						// System.out.println("KEY"+involvedSentences((String)
						// key));
						// System.out.println("NLL"+getNucleusDependency(key,
						// diin));
						// System.out.println("NLL"+getNucleusDependencyNr(key,
						// diin.getSentenceInfoAt(s)));

						int[] hlIndexArray = new int[hlIndex.size() * 2];
						int index = 0;
						for (int h = 0; h < hlIndex.size(); h++) {
							hlIndexArray[index] = hlIndex.get(h);
							hlIndexArray[index + 1] = annoDepData.getHead(hlIndex
									.get(h));
							index = index + 2;
						}

						if (!sIdx.contains(sentenceNr)) {
							sIdx.add(sentenceNr);
							// System.out.println(hlIndexArray[0] + " "
							// + hlIndexArray[1]);
							DefaultNGramHighlight defaultGHL = new DefaultNGramHighlight(
									hlIndexArray, true);
							annoDepData.setAnnotation(new NGramAnnotation(defaultGHL));

							sentenceDataDetailedList.add(annoDepData);
						}
					}
				}
			}
		}
		DetailedNGramSentenceDataList dsdl = new DetailedNGramSentenceDataList(
				sentenceDataDetailedList);
		return dsdl;
	}

	private SentenceDataList createDetailListFromTable(String key, String sentences, String label) {


		List<SentenceData> sentenceDataDetailedList = new ArrayList<SentenceData>();
		//System.out.println("selectedKey: " + key + " sNR: " +sentences); //$NON-NLS-1$

		DataList<?> dl = ((AbstractSearchResult)searchResult).getTarget();

		String[] tmp = sentences.split(", "); //$NON-NLS-1$
		List<Integer> sIdx = new ArrayList<Integer>();


		for(int i = 0; i < tmp.length; i++){
			int sentenceNr = Integer.parseInt(tmp[i]);

			// pos search
			if (isPoSErrorMiningResult()) {

				ArrayList<ItemInNuclei> iinList = nGramResult.get(key);

				for (int k = 0; k < iinList.size(); k++) {
					ItemInNuclei iin = iinList.get(k);

					if (iin.getPosTag().equals(label)) {

						for (int s = 0; s < iin.getSentenceInfoSize(); s++) {

							if (iin.getSentenceInfoAt(s).getSentenceNr() == sentenceNr) {
								// SentenceData sentenceData = (SentenceData)
								// dl.get(sentenceNr);
								// System.out.println("SentenceData: "+
								// sentenceData.getText());
								// sentenceDataDetailedList.add(sentenceData);

								AnnotatedDependencyData annoDepData = new AnnotatedDependencyData(
										(DependencySentenceData) dl.get(sentenceNr));

								List<Integer> hlIndex = new ArrayList<Integer>();
								List<String> nucleusList = getNucleusList(key,
										involvedSentences((String) key));

								int start = iin.getSentenceInfoAt(s)
										.getSentenceBegin() - 1;
								int end = iin.getSentenceInfoAt(s)
										.getSentenceEnd();

								for (int j = start; j < end; j++) {
									String currentForm = annoDepData.getForm(j);

									if (isNucleiItem(j - start,	nGramResult.get(key))) {
										// System.out.println("fail" +
										// currentForm);
										// add new index
										hlIndex.add(j);
										// System.out.println("NUCLEUS " +
										// annoDepData.getForm(j)
										// + "Position " + (j+1)
										// + " Satz " + (sentenceNr+1));
									} else if (nucleusList
											.contains("[number-wildcard]")) { //$NON-NLS-1$
										if (numberPattern.matcher(currentForm)
												.find()) {
											hlIndex.add(j);
										}
									}
								}

								int[] hlIndexArray = new int[hlIndex.size()];

								for (int h = 0; h < hlIndex.size(); h++) {
									hlIndexArray[h] = hlIndex.get(h);
								}

								DefaultNGramHighlight defaultGHL = new DefaultNGramHighlight(
										hlIndexArray, false);
								annoDepData.setAnnotation(new NGramAnnotation(
										defaultGHL));

								sentenceDataDetailedList.add(annoDepData);
							}

						}
					}
				}
			}

			//dependency
			else {

				//System.out.println("key " + ngramList.getSelectedValue() +"#" + label);
				key = (String) ngramList.getSelectedValue();

				//workaround list manchmal null?! fire event?
				if(key == null){
					key = ""; //$NON-NLS-1$
				}
				ArrayList<DependencyItemInNuclei> diinL = nGramResultDependency.get(key);

				//SentenceData sentenceData = (SentenceData) dl.get(sentenceNr);
				//System.out.println(sentenceData.getText() + "TEXT");
				//List<Integer> nilSNO = new ArrayList<Integer>();

				for(int j = 0; j < diinL.size(); j++){
					DependencyItemInNuclei diin = diinL.get(j);

					int nucleusCount = diin.getSentenceInfoAt(0).getNucleiIndexListSize();

					if (diin.getPosTag().equals(label) || diin.getPosTag().equals(nilString) || nucleusCount > 1) {


						for (int s = 0; s < diin.getSentenceInfoSize(); s++) {
							// System.out.println("SNR"+sentenceNr);
							if (diin.getSentenceInfoAt(s).getSentenceNr() == sentenceNr) {
								// SentenceData sentenceData = (SentenceData)
								// dl.get(diin.getSentenceInfoAt(s).getSentenceNr());
								// System.out.println(sentenceData.getText() +
								// "TEXT");

								AnnotatedDependencyData annoDepData = new AnnotatedDependencyData(
										(DependencySentenceData) dl.get(sentenceNr));

								List<Integer> hlIndex = null;

								int start = diin.getSentenceInfoAt(s).getSentenceBegin()-1;
								int end = diin.getSentenceInfoAt(s).getSentenceEnd()-1;

								if(diin.getPosTag().equals(nilString)){
									hlIndex = new ArrayList<Integer>();
									int id;
									for(int n = 0; n < diin.getSentenceInfoAt(s).getNucleiIndexListSize(); n++){
										id =  inRange(start,end,diin.getSentenceInfoAt(s).getNucleiIndexListAt(n)-1, annoDepData);
										//hlIndex.add(diin.getSentenceInfoAt(s).getNucleiIndexListAt(n)-1);
										hlIndex.add(id);
									}
									id = inRange(start,end,diin.getSentenceInfoAt(s).getSentenceHeadIndex()-1, annoDepData);
									//hlIndex.add(diin.getSentenceInfoAt(s).getSentenceHeadIndex()-1);
									hlIndex.add(id);
								}else {
									hlIndex = getNucleusDependencyNr(key,
										diin.getSentenceInfoAt(s));
								}

//								List<Integer> hlIndex = getNucleusDependencyNr(
//										key, diin.getSentenceInfoAt(s));
								// System.out.println("KEY"+involvedSentences((String)key));
								// System.out.println("NLL"+getNucleusDependency(key, diin));
								// System.out.println("NLL"+getNucleusDependencyNr(key,diin.getSentenceInfoAt(s)));


								int[] hlIndexArray = new int[hlIndex.size() * 2];
								int index = 0;
								for (int h = 0; h < hlIndex.size(); h++) {
									hlIndexArray[index] = hlIndex.get(h);
									//is -> Root getHeadIndex
									if(annoDepData.getHead(hlIndex.get(h)) == -1){
										hlIndexArray[index + 1] = diin.getSentenceInfoAt(s).getSentenceHeadIndex()-1;
									} else {
										hlIndexArray[index + 1] = annoDepData.getHead(hlIndex.get(h));
									}

									index = index + 2;
								}

								if (!sIdx.contains(sentenceNr)) {
									sIdx.add(sentenceNr);
									// System.out.println(hlIndexArray[0] + " "
									// + hlIndexArray[1]);
									DefaultNGramHighlight defaultGHL = new DefaultNGramHighlight(
											hlIndexArray, true);
									annoDepData.setAnnotation(new NGramAnnotation(defaultGHL));

									sentenceDataDetailedList.add(annoDepData);
								}

							}

							// sentenceDataDetailedList.add(sentenceData);
						}
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
	private List<String> getNucleus(String key, ItemInNuclei iin) {
		List<String> list = new ArrayList<String>();
		String[] splittedKey = key.split(" ");  //$NON-NLS-1$

		//System.out.println(colorOffset);
		for(int c = 0; c < splittedKey.length; c++){
			if(isNuclei(splittedKey[c])){
				//System.out.println(splittedKey[c]);
				list.add(splittedKey[c]);
			}
		}
		return list;
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

//		//TO DO remove if
//		if(dsi.getSentenceEnd()-dsi.getSentenceBegin() > 1){
//				//colorOffset = dsi.getSentenceEnd();
//		}

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

		if (list.size() == 0){
			colorOffset = dsi.getSentenceEnd();
			for(int c = 0; c < splittedKey.length; c++){
				if(isDependencyNuclei(c, dsi, colorOffset)){
					//System.out.println(splittedKey[c]);
					list.add(c + colorOffset - 1);
				}
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
//				//reninitialize list
				statisticListModel.reload(null);
				//activate tab
				tabbedPane.setSelectedIndex(1);
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
			//reninitialize list
			statisticListModel.reload(null);
			//activate tab
			tabbedPane.setSelectedIndex(1);
		}


	/**
	 * @param tagKey
	 * @return
	 */
	private String formatDependencyKeyHTML(String tagKey) {
		String formattedKey = tagKey;
		if(formattedKey.contains("_L")){ //$NON-NLS-1$
			formattedKey = formattedKey.replace("_L", " &lt;-"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if(formattedKey.contains(" <-")){ //$NON-NLS-1$
			formattedKey = formattedKey.replace(" <-", " &lt;-"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if(formattedKey.contains("_R")){ //$NON-NLS-1$
			formattedKey = formattedKey.replace("_R", " ->"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return formattedKey;
	}

	private String formatDependencyKey(String tagKey) {
		String formattedKey = tagKey;
		if(formattedKey.contains("_L")){ //$NON-NLS-1$
			formattedKey = formattedKey.replace("_L", " ->"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if(formattedKey.contains("_R")){ //$NON-NLS-1$
			formattedKey = formattedKey.replace("_R", " <-"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return formattedKey;
	}

	private String reFormatDependencyKey(String tagKey) {
		String formattedKey = tagKey;
		if(formattedKey.contains(" ->")){ //$NON-NLS-1$
			formattedKey = formattedKey.replace(" ->", "_L"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if(formattedKey.contains(" <-")){ //$NON-NLS-1$
			formattedKey = formattedKey.replace(" <-", "_R"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return formattedKey;
	}


	/**
	 * @param label
	 * @return
	 */
	private String reFormatHTMLString(String s) {
		//only format strings containing color information (encoded as html)
		if(!s.contains("<html>")){ //$NON-NLS-1$
			return s;
		}
		return (String) s.subSequence(26, s.indexOf(" </font><html>")); //$NON-NLS-1$
	}

	/**
		 * @param iinD
		 * @param dependentIndex
		 * @param object
		 * @return
		 */
		protected Object getCombinedDependencyTag(DependencyItemInNuclei iinD, int sentenceNo, int dependentIndex) {
			DataList<?> dl = ((AbstractSearchResult) searchResult).getTarget();
			DependencySentenceData dd = (DependencySentenceData) dl.get(sentenceNo);
			dependentIndex = dependentIndex-1;

	//		System.out.println("SNR "+ sentenceNo
	//						+  " " + dd.getForm(dependentIndex)
	//						+  " " + dd.getHead(dependentIndex)
	//						+  " " + dd.getRelation(dependentIndex)
	//						+  " DependentIndex" + dependentIndex);


			StringBuilder sb = new StringBuilder();
			int headIndex = dd.getHead(dependentIndex);

			String headAppendix;
			if (dependentIndex > headIndex){
				headAppendix = " ->";//"_R"; //$NON-NLS-1$
			} else {
				headAppendix = " <-"; //"_L"; //$NON-NLS-1$
			}
			sb.append(dd.getRelation(dependentIndex)).append(headAppendix);

//			if(iinD.getPosTag().equals(nilString)){
//				return reFormatDependencyKey(sb.toString());
//				//System.out.println(reFormatDependencyKey(sb.toString()));
//				//System.out.println(sb.toString());
//			}

			return sb.toString();
		}

	protected Object getHeadTag(DependencyItemInNuclei iinD, int sentenceNo, Integer dependentIndex) {
			DataList<?> dl = ((AbstractSearchResult) searchResult).getTarget();
			DependencySentenceData dd = (DependencySentenceData) dl.get(sentenceNo);

			dependentIndex = dependentIndex-1;
			int headIndex = dd.getHead(dependentIndex);

//			System.out.println("SNR "+ sentenceNo
//			+  " " + dd.getForm(dependentIndex)
//			+  " " + dd.getForm(dd.getHead(dependentIndex))
//			+  " HeadIndex " + headIndex
//			+  " DependentIndex" + dependentIndex);

			if(headIndex == -1){
				return createColoredHTMLString(dd.getForm(iinD.getSentenceInfoAt(0).getNucleiIndex()-1), nilColor);
			}
			else if (dependentIndex > headIndex){
				return createColoredHTMLString(dd.getForm(dependentIndex), nucleusColor);
			} else {
				return createColoredHTMLString(dd.getForm(dd.getHead(dependentIndex)), headColor);
			}
		}

	protected Object getDependentTag(DependencyItemInNuclei iinD,
			int sentenceNo, Integer dependentIndex) {
		DataList<?> dl = ((AbstractSearchResult) searchResult).getTarget();
		DependencySentenceData dd = (DependencySentenceData) dl.get(sentenceNo);

		dependentIndex = dependentIndex - 1;
		int headIndex = dd.getHead(dependentIndex);

		// System.out.println("SNR "+ sentenceNo
		// + " " + dd.getForm(dependentIndex)
		// + " " + dd.getForm(dd.getHead(dependentIndex))
		// + " HeadIndex " + headIndex
		// + " DependentIndex" + dependentIndex);

		if (headIndex == -1) {
			return createColoredHTMLString(dd.getForm(iinD.getSentenceInfoAt(0)
					.getSentenceHeadIndex() - 1), nilColor);
		} else if (dependentIndex < headIndex) {
			return createColoredHTMLString(dd.getForm(dependentIndex),
					nucleusColor);
		} else {
			return createColoredHTMLString(
					dd.getForm(dd.getHead(dependentIndex)), headColor);
		}
	}

	protected Object getNilNode(DependencyItemInNuclei iinD,
			int sentenceNo, Integer nodeIndex) {
		DataList<?> dl = ((AbstractSearchResult) searchResult).getTarget();
		DependencySentenceData dd = (DependencySentenceData) dl.get(sentenceNo);


		// System.out.println("SNR "+ sentenceNo
		// + " " + dd.getForm(dependentIndex)
		// + " " + dd.getForm(dd.getHead(dependentIndex))
		// + " HeadIndex " + headIndex
		// + " DependentIndex" + dependentIndex);
		if(dd.getHead(nodeIndex) == -1){
			return createColoredHTMLString(dd.getForm(nodeIndex), nilColor);
		}


		return createColoredHTMLString(dd.getForm(dd.getHead(nodeIndex)), nilColor);
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
			//model check
			if( ngramListModel.getSize() > 0){
				try {
					if(isPoSErrorMiningResult()){
						createStatistic((int)sbm.getValue());
					} else	{
						int max = Math.max(minDependencyGram, (int)sbm.getValue());
						createStatisticDependency(max);
					}
					if(statisticTable.getModel().getValueAt(0, 1) != null){
						statisticTable.getSelectionModel().setSelectionInterval(0, 1);
						String key = (String) statisticTable.getModel().getValueAt(0, 1);
						statisticTableModel.generateListEntryFromString(key);
					}
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Failed to generate statistic", ex); //$NON-NLS-1$
					UIUtil.beep();
				}
			}
		}


		public void showDetail(ActionEvent e) {
			try {

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
					//TO DO maybe enable multi selection
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

		public void toggleNumberCompareMode(boolean b) {
			try {
				barCompare = b;
				chart.fireChartChanged();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to toggle number mode", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}

		public void toggleNumberCompareMode(ActionEvent e) {
			// ignore
		}


		public void toggleNumberMode(boolean b) {
			try {
				showPercentage = b;
				chart.fireChartChanged();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to toggle number mode", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}

		public void toggleNumberMode(ActionEvent e) {
			// ignore
		}


		public void toggleChartDisplayMode(boolean b) {
			try {
				//System.out.println(b);
				if(b){

//				chart = ChartFactory.createMultiplePieChart(
//						ResourceManager.getInstance().get(
//								"plugins.errormining.labels.barChart.head"), //$NON-NLS-1$,  // chart title
//						plot.getDataset(), // data
//						TableOrder.BY_ROW, // table order (row/column)
//						true, // include legend
//						true,
//						false);
//
//				barchartPanel.setChart(chart);
				barChartPanel.setChart(createPieChart(((CategoryPlot) plot).getDataset()));

				} else {

//					chart = ChartFactory.createBarChart(
//							ResourceManager.getInstance().get(
//									"plugins.errormining.labels.barChart.head"), //$NON-NLS-1$
//							ResourceManager.getInstance().get(
//									"plugins.errormining.labels.barChart.x-axis"), //$NON-NLS-1$
//							ResourceManager.getInstance().get(
//									"plugins.errormining.labels.barChart.y-axis"), //$NON-NLS-1$
//							plot.getDataset(),
//							PlotOrientation.VERTICAL, // orientation
//							true, // include legend
//							true, // tooltips?
//							false); // URLs?
					barChartPanel.setChart(createBarChart(((CategoryPlot) plot).getDataset()));
				}

//				MultiplePiePlot plot = (MultiplePiePlot) chart.getPlot();
//				JFreeChart subchart = plot.getPieChart();
//				plot.setLimit(0.10);
//				PiePlot p = (PiePlot) subchart.getPlot();
//				// p.setLabelGenerator(new
//				// StandardPieItemLabelGenerator("{0}"));
//				// p.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
//				p.setInteriorGap(0.30);


				//barchartPanel.setChart(chart);

			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to toggle number mode", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}

		public void toggleChartDisplayMode(ActionEvent e) {
			// ignore
		}


		public void exportBarchart(ActionEvent e){
			try {
				ConfigRegistry config = ConfigRegistry.getGlobalRegistry();

				DefaultFileFilter ff = new DefaultFileFilter(
						".png", //$NON-NLS-1$
						ResourceManager.getInstance().get(
								"plugins.errormining.dialog.exportBarchart.filetype")); //$NON-NLS-1$

				Path file = DialogFactory.getGlobalFactory().showDestinationFileDialog(
						null,
						ResourceManager.getInstance().get(
								"plugins.errormining.dialog.exportBarchart"), //$NON-NLS-1$
						null,
						ff);

				if (file == null){
					return;
				}

				int width = config.getInteger("plugins.errorMining.appearance.export.width"); //$NON-NLS-1$
				int height = config.getInteger("plugins.errorMining.appearance.export.height"); //$NON-NLS-1$
				int compression = config.getInteger("plugins.errorMining.appearance.export.compression"); //$NON-NLS-1$
				boolean encodeAlpha = config.getBoolean("plugins.errorMining.appearance.export.encodeAlpha"); //$NON-NLS-1$


				ChartUtilities.saveChartAsPNG(
						file.toFile(), // file
						chart, // chart
						width, // width
						height, // height
						null, // chart info
						encodeAlpha, // alpha
						compression); // Compression
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to show details", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}



	}


	protected class HandlerErrorMining extends Handler implements
			ActionListener, ListSelectionListener, EventListener,
			ListDataListener, ComponentListener, ChartMouseListener, KeyListener, ItemListener {

		protected boolean trackResizing = true;

		protected PieSectionEntity pse = null;

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent me) {


			if (me.getClickCount() == 2) {

				if (me.getSource() == ngramList) {
					int index = ngramList.locationToIndex(me.getPoint());
					// System.out.println("Double clicked on Item " + index);
					// System.out.println(ngramListModel.getElementAt(index));
					String key = (String) ngramListModel.getElementAt(index);
					showDetails(createDetailList(key));
				}

				else if (me.getSource() == ngramTable) {
					int selectedRow = ngramTable.getSelectedRow();

					//System.out.println("selRow" + selectedRow);
					// add correct column!!
					String nucleus = (String) ngramTable.getModel().getValueAt(
							selectedRow, 0);
					String label = (String) ngramTable.getModel().getValueAt(
							selectedRow, 1);
					String sentences = String.valueOf(ngramTable.getModel()
							.getValueAt(selectedRow, 4));

					if (!isPoSErrorMiningResult()) {
						label = reFormatDependencyKey(label);
					}


					//nucleus = (String) ngramTable.getModel().getValueAt(
					//		selectedRow, 1);

					showDetails(createDetailListFromTable(nucleus,
							sentences, label));

//					if (reFormatHTMLString(label).equals(nilString)) {
//
//						String nodeA = nucleus;
//						String nodeB = (String) ngramTable.getModel()
//								.getValueAt(selectedRow, 2);
//						// System.out.println(reFormatHTMLString(nodeA) + " " +
//						// reFormatHTMLString(nodeB));
//
//						showDetails(createNilDetailListFromTable(
//								reFormatHTMLString(nodeA),
//								reFormatHTMLString(nodeB), sentences, label));
//					} else {
//						showDetails(createDetailListFromTable(nucleus,
//								sentences, label));
//					}
				}

				else if (me.getSource() == statisticTable) {
					displaySelectedStatsData();
				}

				else if (me.getSource() == statisticList) {
					String key = (String) statisticList.getSelectedValue();
					// System.out.println(createDetailList(key).size());
					// System.out.println(key);
					showDetails(createDetailList(key));
				} else {
					// noop
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
					//removed (due to performance issues)
					//ngramTableAdjuster.adjustColumns();

				} else if (e.getSource() == statisticTable.getSelectionModel()){

					//no selection check
					if(statisticTable.getSelectedRow() != -1){
						displaySelectedStatsData();
					}

				} else if (e.getSource() == statisticList){
						//String key = (String) statisticList.getSelectedValue();
						List<Object> keys = statisticList.getSelectedValuesList();

						if(keys == null){
							return;
						}

						if(statisticListModel.getSize() > 1){
							//System.out.println(keys);

							int selectedRow = statisticTable.getSelectedRow();
							String keyTable = (String) statisticTable.getModel().getValueAt(selectedRow, 1);
							String title = keyTable; // + "\n " + keys;
							refreshChart(title, statsResultFiltered.get(keyTable));

							//addBarChartItems(title, statsList, statsResultFiltered.get(keyTable)
							DefaultCategoryDataset dcd = (DefaultCategoryDataset) ((CategoryPlot) plot).getDataset(0);

							for(Object o : keys){
								List<StatsData> statsList = new ArrayList<StatsData>();
								String key = o.toString();
								if(isPoSErrorMiningResult()){
									List<ItemInNuclei> iinList = nGramResult.get(key);
									for(int i = 0; i < iinList.size(); i++){
										StatsData sd = new StatsData(iinList.get(i).getPosTag(),
												iinList.get(i).getCount());
										sd.addWordstringSize(key);
										statsList.add(sd);
									}

								} else {
									List<DependencyItemInNuclei> iinList = nGramResultDependency.get(key);
									for(int i = 0; i < iinList.size(); i++){
										StatsData sd = new StatsData(iinList.get(i).getPosTag(),
												iinList.get(i).getCount());
										sd.addWordstringSize(key);
										statsList.add(sd);
									}
								}

								//int selectedRow = statisticTable.getSelectedRow();
								// add correct column!!
								//String keyTable = (String) statisticTable.getModel().getValueAt(selectedRow, 1);
								//String title = keyTable + "\n " + key; //$NON-NLS-1$
								//addBarChartItems(title, statsList, statsResultFiltered.get(keyTable), o.toString());
								dcd = buildDefaultCategoryDataset(dcd, statsList, o.toString());
							}
							setCategoryChartData(dcd);
							//((CategoryPlot) plot).setDataset(0, dcd);
						}

				} else {
					try {
						//for default presenter
						displaySelectedTabData();
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
	//			if(!trackResizing) {
	//				return;
	//			}
	//
	//			int height = splitpaneDetails.getHeight();
	//			if(height==0) {
	//				return;
	//			}
	//
	//			splitpaneDetails.setDividerLocation(Math.max(height/2, height-100));
	//
	//			trackResizing = false;
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


		/**
		 * @see org.jfree.chart.ChartMouseListener#chartMouseClicked(org.jfree.chart.ChartMouseEvent)
		 */
		@Override
		public void chartMouseClicked(ChartMouseEvent e) {
			//System.out.println(e.getEntity());

			String key;
			String label;

			if (e.getEntity() instanceof PieSectionEntity) {
				PieSectionEntity pieSection = (PieSectionEntity) e.getEntity();

				if (pieSection != null) {
					// multiplot index to determine the corect (clicked) pieplot

					int pieIndex = pieSection.getPieIndex();

					// System.out.println(pieSectoin.getDataset().getKey(sectionIndex));
					label = pieSection.getSectionKey().toString();

					// pieplotlegend(0) = total#; followed by the selected
					// pielabels
					if (pieIndex == 0) {
						if (statisticList.getSelectedValuesList().size() == 0) {
							DialogFactory
									.getGlobalFactory()
									.showInfo(
											null,
											"plugins.errorMining.errorMiningSearchPresenter.dialogs.noStatSelection.title", //$NON-NLS-1$
											"plugins.errorMining.errorMiningSearchPresenter.dialogs.noStatSelection.message"); //$NON-NLS-1$
							// for(int i = 0; i < statisticListModel.getSize();
							// i++){
							// key = (String)
							// statisticListModel.getElementAt(i);
							//		            		showDetails(createDetailList(key,label), key + " " + label ); //$NON-NLS-1$
							// }
						} else {
							for (int i = 0; i < statisticList
									.getSelectedValuesList().size(); i++) {

								key = (String) statisticList
										.getSelectedValuesList().get(i);

								showDetails(createDetailList(key),key
										+ " " + chart.getTitle().getText()); //$NON-NLS-1$
										//+ " [" + plot.getLegendItems().get(pieIndex).getLabel().toString() + "]"); //$NON-NLS-1$ //$NON-NLS-
							}
						}
					} else {

						key = plot.getLegendItems().get(pieIndex).getLabel();

						// System.out.println(pieSection.getSectionKey());
						// System.out.println(pieSection.getDataset().getKeys());
						// System.out.println(plot.getLegendItems().get(pieIndex).getLabel());

						//System.out.println("Key: " + key + "  Label: " + label);
						showDetails(createDetailList(key, label), key
								+ " [" + label + "]"); //$NON-NLS-1$ //$NON-NLS-2$

						// displayResultFromStats(key, label);
					}
				}
			}



			if (e.getEntity() instanceof TitleEntity) {

				//TitleEntity titelItem = (TitleEntity) e.getEntity();
				//System.out.println("source " + e.getSource());

				if(e.getChart().getPlot() instanceof MultiplePiePlot){
					//System.out.println("x: " + e.getTrigger().getX() +  "y: " +e.getTrigger().getY());
					int x = e.getTrigger().getX();
					int y = e.getTrigger().getY()-20;

					ChartRenderingInfo chartrenderinginfo = barChartPanel.getChartRenderingInfo();
					EntityCollection entities = chartrenderinginfo.getEntityCollection();



					if (entities.getEntity(x,y) instanceof PieSectionEntity) {
						PieSectionEntity pieSection = (PieSectionEntity) entities.getEntity(x,y);
						int pieIndex = pieSection.getPieIndex();
						label = pieSection.getSectionKey().toString();
						//System.out.println("Label "+label);

						// pieplotlegend(0) = total#; followed by the selected pielabels
						if (pieIndex == 0) {
							if (statisticList.getSelectedValuesList().size() == 0) {
								DialogFactory
										.getGlobalFactory()
										.showInfo(
												null,
												"plugins.errorMining.errorMiningSearchPresenter.dialogs.noStatSelection.title", //$NON-NLS-1$
												"plugins.errorMining.errorMiningSearchPresenter.dialogs.noStatSelection.message"); //$NON-NLS-1$
								// for(int i = 0; i < statisticListModel.getSize();
								// i++){
								// key = (String)
								// statisticListModel.getElementAt(i);
								//		            		showDetails(createDetailList(key,label), key + " " + label ); //$NON-NLS-1$
								// }
							} else {
								for (int i = 0; i < statisticList
										.getSelectedValuesList().size(); i++) {

									key = (String) statisticList
											.getSelectedValuesList().get(i);

									showDetails(createDetailList(key),key
											+ " " + chart.getTitle().getText()); //$NON-NLS-1$
											//+ " [" + plot.getLegendItems().get(pieIndex).getLabel().toString() + "]"); //$NON-NLS-1$ //$NON-NLS-
								}
							}
						} else {
							key = plot.getLegendItems().get(pieIndex).getLabel();

							// System.out.println(pieSection.getSectionKey());
							// System.out.println(pieSection.getDataset().getKeys());
							// System.out.println(plot.getLegendItems().get(pieIndex).getLabel());

							//System.out.println("Key: " + key + "  Label: " + label);
							showDetails(createDetailList(key), key);
						}
					}


					//System.out.println("EntityAtXY" + entities.getEntity(x,y) + e.getChart().getPlot());

				}

//				MultiplePiePlot mpp = (MultiplePiePlot) e.getChart().getPlot();
//				JFreeChart subchart = mpp.getPieChart();
//				PiePlot pp = (PiePlot) subchart.getPlot();

//				System.out.println("ChartTitle:  " + chart.getTitle().getText());
//				System.out.println("SChartTitle:  " + subchart.getTitle().getText());
//				System.out.println("TitleEntiry: " + item.getTitle().toString());
//				System.out.println("PieIndex: " +pp.getPieIndex());
//				JFreeChart test = e.getChart();
//				System.out.println("Test: " + test.getTitle().getText().toString());
//				System.out.println(mpp.getDataset().getColumnKeys());
//				System.out.println(mpp.getDataset().getRowKeys());


//				for(int i = 0; i < mpp.getLegendItems().getItemCount(); i++){
//					System.out.println(mpp.getLegendItems().get(i));
//				}

//				if (!isPoSErrorMiningResult()) {
//					label = reFormatDependencyKey(label);
//				}
//
//				displayResultFromStats("", label); //$NON-NLS-1$
			}


			if (e.getEntity() instanceof CategoryItemEntity) {

				ChartEntity entity = e.getEntity();
				// System.out.println(entity);

				// details with filtered clicked on bars
				CategoryItemEntity item = (CategoryItemEntity) entity;
				// System.out.println(item.getColumnKey().toString());

				label = item.getColumnKey().toString();
				key = item.getRowKey().toString();

				if (!isPoSErrorMiningResult()) {
					label = reFormatDependencyKey(label);
				}

				if (key.equals(labelmatrix)) {
					displayResultFromStats(key, label);
				} else {
					showDetails(createDetailList(key, label), key
							+ " [" + label + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				}

			}


			// details with filtered (clicked on label x-axis)
			if (e.getEntity() instanceof CategoryLabelEntity) {

				CategoryLabelEntity item = (CategoryLabelEntity) e.getEntity();

				label = item.getKey().toString();

				if (!isPoSErrorMiningResult()) {
					label = reFormatDependencyKey(label);
				}

				displayResultFromStats("", label); //$NON-NLS-1$
			}


			// details complete label (clicked legend)
			if (e.getEntity() instanceof LegendItemEntity) {

				LegendItemEntity item = (LegendItemEntity) e.getEntity();

				if (e.getChart().getPlot() instanceof MultiplePiePlot) {

					label = item.getSeriesKey().toString();
					if (statisticList.getSelectedValuesList().size() == 0) {
						DialogFactory
								.getGlobalFactory()
								.showInfo(
										null,
										"plugins.errorMining.errorMiningSearchPresenter.dialogs.noStatSelection.title", //$NON-NLS-1$
										"plugins.errorMining.errorMiningSearchPresenter.dialogs.noStatSelection.message"); //$NON-NLS-1$
						// for(int i = 0; i < statisticListModel.getSize();
						// i++){
						// key = (String) statisticListModel.getElementAt(i);
						//		            		showDetails(createDetailList(key,label), key + " " + label ); //$NON-NLS-1$
						// }
					} else {
						for (int i = 0; i < statisticList
								.getSelectedValuesList().size(); i++) {
							key = (String) statisticList
									.getSelectedValuesList().get(i);
							// showDetails(createDetailList(key));

							showDetails(createDetailList(key, label), key
									+ " [" + label + "]"); //$NON-NLS-1$ //$NON-NLS-2$

						}
					}
				} else {

					key = item.getSeriesKey().toString();

					if (!key.equals(labelmatrix)) {
						showDetails(createDetailList(key));
					} else {
						if (statisticList.getSelectedValuesList().size() == 0) {
							DialogFactory
									.getGlobalFactory()
									.showInfo(
											null,
											"plugins.errorMining.errorMiningSearchPresenter.dialogs.noStatSelection.title", //$NON-NLS-1$
											"plugins.errorMining.errorMiningSearchPresenter.dialogs.noStatSelection.message"); //$NON-NLS-1$
							// for(int i = 0; i < statisticListModel.getSize();
							// i++){
							// key = (String)
							// statisticListModel.getElementAt(i);
							//		            		showDetails(createDetailList(key,label), key + " " + label ); //$NON-NLS-1$
							// }
						} else {
							for (int i = 0; i < statisticList
									.getSelectedValuesList().size(); i++) {
								key = (String) statisticList
										.getSelectedValuesList().get(i);
								showDetails(createDetailList(key));
							}
						}
					}
				}
			}
		}


		/**
			 * @see org.jfree.chart.ChartMouseListener#chartMouseMoved(org.jfree.chart.ChartMouseEvent)
			 */
			@Override
			public void chartMouseMoved(ChartMouseEvent e) {
				ChartEntity entity = e.getEntity();
				CustomBarHighlightRenderer cbhr = (CustomBarHighlightRenderer) ((CategoryPlot) plot).getRenderer();
				//System.out.println(e.getEntity());


	            if (!  (entity instanceof CategoryItemEntity
	            		|| entity instanceof PieSectionEntity
	            		|| entity instanceof LegendItemEntity)) {

	            	if(((JFreeChart) e.getSource()).getPlot() instanceof MultiplePiePlot){
//	            	MultiplePiePlot mpp = (MultiplePiePlot) ((JFreeChart) e.getSource()).getPlot();
//	            	//setPieSliceHighlight(mpp, null, false);

//	            		if(pse != null){
//	    					MultiplePiePlot mpp = (MultiplePiePlot) ((JFreeChart) e.getSource()).getPlot();
//
//	    					JFreeChart subchart = mpp.getPieChart();
//	    					PiePlot pp = (PiePlot) subchart.getPlot();
//	    					pp.setExplodePercent(pse.getSectionKey(), 0);
//	    					subchart.fireChartChanged();
//	    					chart.fireChartChanged();
//	            		}

	            	}
	                cbhr.setHighlightedItem(-1, -1);
	                barChartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	                return;
	            }



	            if(entity instanceof PieSectionEntity){
	            	//do nothing with colorstuff
					MultiplePiePlot mpp = (MultiplePiePlot) ((JFreeChart) e.getSource()).getPlot();
					JFreeChart subchart = mpp.getPieChart();
//					PiePlot pp = (PiePlot) subchart.getPlot();

//					if(pse == null){
//						pse = (PieSectionEntity)e.getEntity();
//						pp.setExplodePercent(pse.getSectionKey(), 0.25);
//
//					} else if (pse != (PieSectionEntity)e.getEntity()) {
//						pp.setExplodePercent(pse.getSectionKey(), 0);
//						pse = (PieSectionEntity)e.getEntity();
//						pp.setExplodePercent(pse.getSectionKey(), 0.25);
//					}

//					pse = (PieSectionEntity)e.getEntity();
////					setPieSliceHighlight(mpp, pse, true);
//
//
//
//					System.out.println(pse.getSectionKey());
//					pp.setExplodePercent(pse.getSectionKey(), 0.3);

					subchart.fireChartChanged();
					chart.fireChartChanged();
	            }

	            if (entity instanceof CategoryItemEntity){
	            	//barhighlight
		            CategoryItemEntity cie = (CategoryItemEntity) entity;
		            CategoryDataset dataset = cie.getDataset();

		        	cbhr.setHighlightedItem(dataset.getRowIndex(cie.getRowKey()),
		                    dataset.getColumnIndex(cie.getColumnKey()));
	            }

	        	barChartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			}


			/**
			 * @param key
			 * @param label
			 */
			private void displayResultFromStats(String key, String label) {
				StringBuilder sbLabel = new StringBuilder();

				sbLabel.append(" [").append(label).append("]"); //$NON-NLS-1$ //$NON-NLS-2$

				if (key.equals(labelmatrix) && statisticListModel.getSize() == 1) {
					key = (String) statisticListModel.getElementAt(0);
					showDetails(createDetailList(key, label),
							key + sbLabel.toString());
				}

				else {
					if (statisticList.getSelectedValuesList().size() == 0) {
						DialogFactory
								.getGlobalFactory()
								.showInfo(
										null,
										"plugins.errorMining.errorMiningSearchPresenter.dialogs.noStatSelection.title", //$NON-NLS-1$
										"plugins.errorMining.errorMiningSearchPresenter.dialogs.noStatSelection.message"); //$NON-NLS-1$
						// for(int i = 0; i < statisticListModel.getSize(); i++){
						// key = (String) statisticListModel.getElementAt(i);
						//	            		showDetails(createDetailList(key,label), key + sbLabel.toString()); //$NON-NLS-1$
						// }
					} else {
						for (int i = 0; i < statisticList.getSelectedValuesList()
								.size(); i++) {
							key = (String) statisticList.getSelectedValuesList()
									.get(i);
							showDetails(createDetailList(key, label),
									key + sbLabel.toString());

						}
					}
				}

			}


			/**
			 * @param mpp
			 */
			private void setPieSliceHighlight(MultiplePiePlot mpp, PieSectionEntity pse, boolean highlight) {
				JFreeChart subchart = mpp.getPieChart();
				PiePlot pp = (PiePlot) subchart.getPlot();

				Color c = (Color) pp.getSectionPaint(pse.getSectionKey());


				if(highlight){
					pp.setSectionPaint(pse.getSectionKey(), c.brighter());
				} else {
					pp.setBaseSectionPaint(Color.blue);
				}

				chart.fireChartChanged();

				System.out.println(pp.getSectionPaint(pse.getSectionKey()));
				System.out.println("\n");				 //$NON-NLS-1$
			}


			/**
			 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyTyped(KeyEvent e) {
				// TO DO Auto-generated method stub

			}


			/**
			 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				// TO DO Auto-generated method stub

			}


			/**
			 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyReleased(KeyEvent e) {

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					//Enter pressed for lower
			        if (SwingUtilities.getAncestorOfClass(JSpinner.class, (Component) e.getSource()) == lowerBound) {
			          doResultFiltering();
			        }
					//Enter pressed for upper
			        else if (SwingUtilities.getAncestorOfClass(JSpinner.class, (Component) e.getSource()) == upperBound) {
			        	doResultFiltering();
			        }
					//Enter pressed for stat
			        else if (SwingUtilities.getAncestorOfClass(JSpinner.class, (Component) e.getSource()) == statsSpinner) {
			            doStatisticFiltering();
			        }
			        else {
			            //noop
			        }
			    }

			}


		/**
		 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
		 */
		@Override
		public void itemStateChanged(ItemEvent e) {

			// System.out.println(e.getSource());

			if (e.getSource() instanceof JRadioButton) {
				JRadioButton jrb = (JRadioButton) e.getSource();
				switch (jrb.getName().toString()) {
				case barChart:
					barChartPanel.setChart(createBarChart(((CategoryPlot) plot)
							.getDataset()));
					break;
				case pieChart:
					barChartPanel.setChart(createPieChart(((CategoryPlot) plot)
							.getDataset()));
					break;
				case percentA:
					barCompare = false;
					chart.fireChartChanged();
					break;
				case percentB:
					barCompare = true;
					chart.fireChartChanged();
					break;

				default:
					break;
				}
			}
		}
	}


	//test stuff...
	public static class CustomMultiPie extends ChartFactory{

	    public static JFreeChart createMultiplePieChart3D(String title,
	            CategoryDataset dataset, TableOrder order, boolean legend,
	            boolean tooltips, boolean urls) {

	        ParamChecks.nullNotPermitted(order, "order"); //$NON-NLS-1$
	        MultiplePiePlot plot = new MultiplePiePlot(dataset);
	        plot.setDataExtractOrder(order);
	        plot.setBackgroundPaint(null);
	        plot.setOutlineStroke(null);


	        JFreeChart pieChart = new JFreeChart(new PiePlot3D(null));
	        TextTitle seriesTitle = new TextTitle("Series Title", //$NON-NLS-1$
	                new Font("SansSerif", Font.BOLD, 12)); //$NON-NLS-1$
	        seriesTitle.setPosition(RectangleEdge.BOTTOM);

	        System.out.println(seriesTitle);
	        pieChart.setTitle(seriesTitle);
	        pieChart.getTitle().setToolTipText(seriesTitle.getText());
	        pieChart.removeLegend();
	        pieChart.setBackgroundPaint(null);
	        plot.setPieChart(pieChart);


	        if (tooltips) {
	            PieToolTipGenerator tooltipGenerator
	                = new StandardPieToolTipGenerator();
	            PiePlot pp = (PiePlot) plot.getPieChart().getPlot();
	            pp.setToolTipGenerator(tooltipGenerator);
	        }

	        if (urls) {
	            PieURLGenerator urlGenerator = new StandardPieURLGenerator();
	            PiePlot pp = (PiePlot) plot.getPieChart().getPlot();
	            pp.setURLGenerator(urlGenerator);
	        }

	        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
	                plot, legend);

	        ChartFactory.getChartTheme().apply(chart);
	        return chart;
	    }
	}


	//teststuff....
	public class MultiplePiePlotWithTooltips extends MultiplePiePlot {


		public void addToolTip(){
			String tt = super.getPieChart().getTitle().toString();
			super.getPieChart().getTitle().setToolTipText(tt);
		};


		/**
		 * @see org.jfree.chart.plot.MultiplePiePlot#draw(java.awt.Graphics2D, java.awt.geom.Rectangle2D, java.awt.geom.Point2D, org.jfree.chart.plot.PlotState, org.jfree.chart.plot.PlotRenderingInfo)
		 */
		@Override
		public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
				PlotState parentState, PlotRenderingInfo info) {
			// TODO Auto-generated method stub

			String tt = super.getPieChart().getTitle().toString();
			super.getPieChart().getTitle().setToolTipText(tt);
			super.draw(g2, area, anchor, parentState, info);
		}
	}




	/**
	 *
	 * @author Gregor Thiele
	 * @version $Id$
	 *
	 */
	public class DifferenceBarRenderer extends BarRenderer {

		private static final long serialVersionUID = 7871113296140703067L;


		public DifferenceBarRenderer() {
			super();

		}

		@Override
		public Paint getItemPaint(int row, int column) {
			CategoryDataset cd = getPlot().getDataset();
			if (cd != null) {
				String l_rowKey = (String) cd.getRowKey(row);
				String l_colKey = (String) cd.getColumnKey(column);
				double l_value = cd.getValue(l_rowKey, l_colKey).doubleValue();
				return l_value > 4 ? Color.GREEN : (l_value > 2 ? Color.ORANGE
						: Color.RED);
			}
			return null;
		}
	}


	/**
	 *
	 * @author Gregor Thiele
	 * @version $Id$
	 *
	 */

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
//					System.out.println("Min: " + minimumGramsize);
//					System.out.println("Max: " + maximumGramsize);
//
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
							"Execution exception when filtering the results", e); //$NON-NLS-1$
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


	/**
	 *
	 * @author Gregor Thiele
	 * @version $Id$
	 *
	 */
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
						"Execution exception when filtering statistic", e); //$NON-NLS-1$
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


		  @Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,
			      boolean isSelected, boolean cellHasFocus) {
			String[] s = ((String) value).split(" "); //$NON-NLS-1$

			sb.setLength(0);

			if(isPoSErrorMiningResult()){
				sb.append("<html>").append((index + 1)).append(") ") //$NON-NLS-1$ //$NON-NLS-2$
					.append(" (").append(s.length).append("-Gram) ")  //$NON-NLS-1$//$NON-NLS-2$
					.append(" ("+ getTotalOccurences((String) value) +") ")  //$NON-NLS-1$//$NON-NLS-2$
					.append(colorStringArray(s, (String) value))
					.append("</html>"); //$NON-NLS-1$
			} else {
				//dependency result
				//System.out.println("SelectedListDependencyValue: " + value);

				sb.append("<html>").append((index + 1)).append(") ") //$NON-NLS-1$ //$NON-NLS-2$
					.append(" (").append(s.length).append("-Gram) ");  //$NON-NLS-1$//$NON-NLS-2$
					if(posFilter.contains((String)value)){
						sb.append(createColoredHTMLString("POS-ERROR", Color.red)); //$NON-NLS-1$
					}
					sb.append(" ("+ getTotalOccurencesDependency((String) value) +") ")  //$NON-NLS-1$//$NON-NLS-2$
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


		/**
		 * @param value
		 * @return
		 */
		private String getTotalOccurences(String key) {
			int count = 0;
			List<ItemInNuclei> iinL = nGramResult.get(key);
			for(int i = 0; i < iinL.size(); i++){
				count = count + iinL.get(i).getCount();
			}

			if(count > 100 && count < 1000){
				return "100+"; //$NON-NLS-1$
			} else if (count > 1000){
				return "&nbsp;&nbsp;1k+"; //$NON-NLS-1$
			}
			else {
				String value = toLength(StringUtil.formatDecimal(count), 5);
				return value.replace(" ", "&nbsp;"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		private String getTotalOccurencesDependency(String key) {
			int count = 0;
			List<DependencyItemInNuclei> diinL = nGramResultDependency.get(key);
			for(int i = 0; i < diinL.size(); i++){
				count = count + diinL.get(i).getCount();
			}

			if(count > 100 && count < 1000){
				return "100+"; //$NON-NLS-1$
			} else if (count > 1000){
				return "&nbsp;&nbsp;1k+"; //$NON-NLS-1$
			}
			else {
				String value = toLength(StringUtil.formatDecimal(count), 5);
				return value.replace(" ", "&nbsp;"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}


	public String toLength(String value, int len) {
	    final int count = len - value.length();
	    if (count > 0) {
	        StringBuffer result = new StringBuffer(len);

	        for (int i = count; i >= 0; i--) {
	            result.append(' ');
	        }
	        result.append(value);
	        return result.toString();
	    } else {
	        return value;
	    }
	}


	/**
	 * @param j
	 * @param iinList
	 */
	private boolean isNucleiItem(int j, ArrayList<ItemInNuclei> iinList) {
		List<String> test = new ArrayList<String>();
		//List<Integer> sentencesList = new ArrayList<Integer>();


		for(int i = 0; i < iinList.size(); i++){
			String[] s = iinList.get(i).getPosTag().split(" "); //$NON-NLS-1$

//			for(int s = 0; s < iinList.get(i).getSentenceInfoSize(); s++){
//				int currentNo = iinList.get(i).getSentenceInfoAt(s).getSentenceNr();
//				if(sentencesList.contains(currentNo)){
//					return true;
//				}
//			}


			if(!test.contains(s[j])){
				test.add(s[j]);
			}
		}

		if (test.size() > 1){
			return true;
		}

		return false;
	}


	/**
	 * @param arrL
	 * @return
	 */
	private ArrayList<Integer> variationIndex(ArrayList<ItemInNuclei> arrL) {

		ArrayList<Integer> variIndex = new ArrayList<Integer>();

		for(int i = 0 ; i < arrL.get(0).getPosTag().split(" ").length; i++){ //$NON-NLS-1$
			if(containsVariation(i, arrL)){
				//System.out.println("VARIATION " + i);
				variIndex.add(i);
			}
		}
		return variIndex;
	}

	private boolean containsVariation(int k, ArrayList<ItemInNuclei> iinList) {
		List<String> test = new ArrayList<>();

		for(int i = 0; i < iinList.size(); i++){
			String[] s = iinList.get(i).getPosTag().split(" "); //$NON-NLS-1$
			if(!test.contains(s[k])){
				test.add(s[k]);
			}
		}
		if (test.size() > 1){
			return true;
		}
		return false;
	}


	class NGramResultViewTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -196214722892607695L;


		int advancedMining = (int) searchResult.getProperty("ADVANCEDMINING"); //$NON-NLS-1$


		/**
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int row, int col) {
			//TODO option sätze in zwischenablage kopieren
//			if(col == 3){
//				return true;
//			}
			return false;

		}

		protected boolean ascending = true;
		protected ArrayList<ItemInNuclei> iinList;
		protected ArrayList<DependencyItemInNuclei> iinDList;
		protected Map<Integer, NucleusCache> tmpMap;
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

				if(isPoSErrorMiningResult()){

					iinList = new ArrayList<>();
					iinList.addAll(nGramResult.get(key));

					//System.out.println(key);

					tmpMap = new LinkedHashMap<>();

					this.keySplitted = key.split(" ");	 //$NON-NLS-1$

					List<Integer> nucleusIndexList = variationIndex(nGramResult.get(key));


					int length = iinList.size();
					//System.out.println(nucleusIndexList);
					//System.out.println(length + "<" + iinList.size()
					//						  + "<" + nucleusIndexList.size());

					int advancedMining = (int) searchResult.getProperty("ADVANCEDMINING"); //$NON-NLS-1$

					for(int i = 0; i < length; i++){
						for(Integer index : nucleusIndexList){
							//System.out.println(keySplitted[index]);
							NucleusCache nc = new NucleusCache();
							nc.setKey(keySplitted[index]);
							ItemInNuclei iin = iinList.get(i);
							nc.setIIN(iin);
							//System.out.println(index+iin.getSentenceInfoAt(0).getSentenceBegin());
							int offsetIndex = index + iin.getSentenceInfoAt(0).getSentenceBegin()-1;
							nc.setTag( getTag(offsetIndex,
										iin.getSentenceInfoAt(0).getSentenceNr()));
							if(advancedMining != 1){
								nc.setHead( getHead(offsetIndex,
										iin.getSentenceInfoAt(0).getSentenceNr()));
							}
							//System.out.println(nc.getTag() + " " + nc.getHead());
							tmpMap.put(itemsAdded, nc);
							itemsAdded++;
						}
					}
				} else {

					//dependency stuff
					//System.out.println("KEY SM1: " + key);
					iinDList = new ArrayList<>();
					iinDList.addAll(nGramResultDependency.get(key));

					nucleusMap = new LinkedHashMap<>();

					for (int i = 0; i < iinDList.size(); i++) {
						//System.out.println("iind"+iinDList.get(i).getPosTag());
						//System.out.println(getNucleusDependency(key, iinDList.get(i)));

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
		 * @param wordIndex
		 * @param sentenceNr
		 * @return
		 */
		private String getTag(Integer wordIndex, int sentenceNr) {
			DataList<?> dl = ((AbstractSearchResult) searchResult).getTarget();
			DependencySentenceData dd = (DependencySentenceData) dl.get(sentenceNr);

			return NGrams.getInstance().getTag(dd,
									wordIndex,
									(int) searchResult.getProperty("ADVANCEDMINING")); //$NON-NLS-1$
		}

		/**
		 * @param wordIndex
		 * @param sentenceNr
		 * @return
		 */
		private String getHead(Integer wordIndex, int sentenceNr) {
			DataList<?> dl = ((AbstractSearchResult) searchResult).getTarget();
			DependencySentenceData dd = (DependencySentenceData) dl.get(sentenceNr);
			if(dd.getHead(wordIndex) != -1){
				return dd.getForm(dd.getHead(wordIndex));
			} else {
				return ""; //$NON-NLS-1$
			}
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
			//TODO lower pos table cells
			if(isPoSErrorMiningResult()){
				int advancedMining = (int) searchResult.getProperty(
											"ADVANCEDMINING"); //$NON-NLS-1$
				//System.out.println(advancedMining);
				if(advancedMining == 1){
					return 3;
				} else {
					return 4;
				}
			} else {
				//return 4;
				return 7;
			}
		}


		/**
		 * @see javax.swing.table.TableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int columnIndex) {

		      if(isPoSErrorMiningResult()){
				switch (columnIndex) {
				case 0:
				if(advancedMining == 1){
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.Token"); //$NON-NLS-1$
				} else {
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.DependentNode"); //$NON-NLS-1$
				}
				case 1:
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.Tag"); //$NON-NLS-1$
				case 2:
					if(advancedMining == 1){
						return ResourceManager.getInstance().get(
								"plugins.errormining.labels.NucleiCount"); //$NON-NLS-1$
					} else {
						return ResourceManager.getInstance().get(
								"plugins.errormining.labels.HeadNode"); //$NON-NLS-1$
					}

				case 3:
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.NucleiCount"); //$NON-NLS-1$
				case 4:
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.SentenceNR"); //$NON-NLS-1$
				case 5:
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.NucleiIndex"); //$NON-NLS-1$
				default:
					break;
				}
		    } else {
				switch (columnIndex) {
				case 0:
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.DependentNode"); //$NON-NLS-1$
				case 1:
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.Tag"); //$NON-NLS-1$
				case 2:
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.HeadNode"); //$NON-NLS-1$
				case 3:
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.SentenceNR"); //$NON-NLS-1$
				case 4:
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.NucleiCount"); //$NON-NLS-1$
				case 5:
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.NucleiIndex"); //$NON-NLS-1$
				case 6:
					return ResourceManager.getInstance().get(
							"plugins.errormining.labels.Count"); //$NON-NLS-1$

				default:
					break;
				}
			}
			return null;
		}


		/**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			if(isPoSErrorMiningResult()) {
				return tmpMap==null ? 0 : tmpMap.size();
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

				//nullcheck
				if(tmpMap.get(rowIndex) == null){
					return null;
				}

				//ItemInNuclei iin = iinList.get(rowIndex);
				ItemInNuclei iin = tmpMap.get(rowIndex).getIIN();

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
						return tmpMap.get(rowIndex).getKey();//keySplitted[nucleiGenIndex-start];
					case 1:
						return tmpMap.get(rowIndex).getTag();//iin.getPosTag();

					case 2:
						if(advancedMining == 1){
							return StringUtil.formatDecimal(iin.getCount());
						} else {
							return tmpMap.get(rowIndex).getHead();
						}
					case 3:
						return StringUtil.formatDecimal(iin.getCount());
					case 4:
						return sentenceOccurences(iin);
					case 5:
						return nucleiCount;
					case 6:
						return getNucleis(iin);
					default:
						break;
					}
				} else {
					switch (columnIndex) {
					case 0:
						return tmpMap.get(rowIndex).getKey();
					case 1:
						return tmpMap.get(rowIndex).getTag();//iin.getPosTag();
					case 2:
						if(advancedMining == 1){
							return StringUtil.formatDecimal(iin.getCount());
						} else {
							return tmpMap.get(rowIndex).getHead();
						}
					case 3:
						return iin.getCount();

					case 4:
						return sentenceOccurences(iin);
					case 5:
						return iin.getSentenceInfoAt(0).getNucleiIndexListSize();
					case 6:
						return getNucleis(iin);
					default:
						break;
					}
				}
				return null;

			} else {
				/**
				 * Dependency Visialization stuff
				 */

				//nullcheck
				if(nucleusMap.get(rowIndex) == null){
					return null;
				}

				DependencyItemInNuclei iinD = nucleusMap.get(rowIndex).getDiin();

				int nucleiDCount = iinD.getSentenceInfoAt(0).getNucleiIndexListSize();
				int index = nucleusMap.get(rowIndex).getHeadIndex();
				String[] number;

				if (!multinuclei) {
					switch (columnIndex) {

					case 0:
						number = ((String) getValueAt(rowIndex, 3)).split(", "); //$NON-NLS-1$
//						if(iinD.getPosTag().equals(nilString)){
//							return getNilNode(iinD
//									,Integer.valueOf(number[0])
//									,(Integer) getValueAt(rowIndex, 5)-1);
//						}
						return getDependentTag(iinD
								,Integer.valueOf(number[0])
								,(Integer) getValueAt(rowIndex, 5));
						//return nucleusMap.get(rowIndex).getKey();//keySplitted[nucleiGenIndex-start];

					case 1:
						number = ((String) getValueAt(rowIndex, 3)).split(", "); //$NON-NLS-1$
						String relationLabel = (String) getCombinedDependencyTag(iinD
															,Integer.valueOf(number[0])
															,(Integer) getValueAt(rowIndex, 5));
//						if(iinD.getPosTag().equals(nilString)){
//							return createColoredHTMLString(relationLabel, nilColor);
//							//return createColoredHTMLString(formatDependencyKeyHTML(nilString), nilColor);
//						}
						return relationLabel;

					case 2:
						number  = ((String) getValueAt(rowIndex, 3)).split(", "); //$NON-NLS-1$
//						if(iinD.getPosTag().equals(nilString)){
//							return getNilNode(iinD
//									,Integer.valueOf(number[0])
//									,(Integer) getValueAt(rowIndex, 5));
//									//,iinD.getSentenceInfoAt(0).getSentenceHeadIndex()-1);
//						}
						return getHeadTag(iinD
								,Integer.valueOf(number[0])
								,(Integer) getValueAt(rowIndex, 5));

					case 3:
						return sentenceOccurences(iinD);
					case 4:
						return nucleiDCount;
					case 5:
						return getNucleiDependency(iinD, index);
					case 6:
						return StringUtil.formatDecimal(iinD.getCount());
					default:
						break;
					}
				} else {

					switch (columnIndex) {

					case 0:
						number = ((String) getValueAt(rowIndex, 3)).split(", "); //$NON-NLS-1$
//						if(iinD.getPosTag().equals(nilString)){
//							return getNilNode(iinD
//									,Integer.valueOf(number[0])
//									,(Integer) getValueAt(rowIndex, 5)-1);
//							//return getValueAt(0, 0);
//						}
						return getDependentTag(iinD
								,Integer.valueOf(number[0])
								,(Integer) getValueAt(rowIndex, 5));
						//return nucleusMap.get(0).getKey();//keySplitted[nucleiGenIndex-start];

					case 1:
						number = ((String) getValueAt(rowIndex, 3)).split(", "); //$NON-NLS-1$
						String relationLabel = (String) getCombinedDependencyTag(iinD
															,Integer.valueOf(number[0])
															,(Integer) getValueAt(rowIndex, 5));
						if(iinD.getPosTag().equals(nilString)){
							return createColoredHTMLString(formatDependencyKeyHTML(relationLabel), nilColor);
							//return createColoredHTMLString(formatDependencyKeyHTML(nilString), nilColor);
						}
						return relationLabel;

					case 2:
						number = ((String) getValueAt(rowIndex, 3)).split(", "); //$NON-NLS-1$
//						if(iinD.getPosTag().equals(nilString)){
//							return getNilNode(iinD
//									,Integer.valueOf(number[0])
//									,(Integer) getValueAt(rowIndex, 5));
//									//,iinD.getSentenceInfoAt(0).getSentenceHeadIndex()-1);
//						}
						return getHeadTag(iinD
								,Integer.valueOf(number[0])
								,(Integer) getValueAt(rowIndex, 5));
					case 3:
						return sentenceOccurences(iinD);
					case 4:
						return nucleiDCount;
					case 5:
						return getNucleiDependency(iinD, index);
					case 6:
						return StringUtil.formatDecimal(iinD.getCount());

					default:
						break;
					}
				}
			}
			return null;
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

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int col) {

		    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);


		    if(col == 0){

		    	// nullcheck <select first listentry (upper ngram list) must be
		    	// checked because when filtering the results the preselected
		    	// ngram may be removed (e.g. doesent occur within the x-grams)
		    	if(table.getModel().getValueAt(row, col) == null){
		    		ngramList.setSelectedIndex(0);
		    	}

				String s;
				if (table.getModel().getValueAt(row, col) == null) {
					s = ""; //$NON-NLS-1$
				} else {
					s = table.getModel().getValueAt(row, col).toString();
				}

			    String selectedKey = (String) ngramList.getSelectedValue();

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

		public void removeAll(){
			keys.clear();
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
		protected Object[] keyArray;


		public StatisticTableModel(){
			//noop
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}


		public void reload (){
			statistic = statsResultFiltered;
			keyArray = statsResultFiltered.keySet().toArray();
			refreshStatisticCount();
			fireTableDataChanged();

			statisticTable.getModel().addTableModelListener(new TableModelListener() {
		        @Override
		        public void tableChanged(TableModelEvent e) {
		            SwingUtilities.invokeLater(new Runnable() {
		                @Override
		                public void run() {
		                    statisticTable.changeSelection(0, 0, false, true);
		                }
		            });
		        }
		    });
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int columnIndex) {

		      switch (columnIndex) {
		      		case 0: return ResourceManager.getInstance().get(
	            		"plugins.errormining.labels.LabelDistribution"); //$NON-NLS-1$
		            case 1: return ResourceManager.getInstance().get(
		            	"plugins.errormining.labels.Tag"); //$NON-NLS-1$

		        }
		        return null;
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return 1;
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
					return createCount(statistic.get(keyArray[rowIndex]));
				case 1:
					return keyArray[rowIndex];
				default:
					break;
			}
			return null;
		}

		public void generateListEntryFromString(String key){
			createWordList(statistic.get(key));
		}

		private String createCount(List<StatsData> sdList) {

			StringBuilder sb = new StringBuilder();
			boolean flipColor = false;

			// System.out.println(hex);
			sb.append("<html>"); //$NON-NLS-1$
			for (int i = 0; i < sdList.size(); i++) {
				Color nuclei = changeColor(flipColor);
				String hex = "#" + Integer.toHexString(nuclei.getRGB()).substring(2); //$NON-NLS-1$


				if (i < sdList.size() - 1) {
					sb.append("<font color=" + hex + ">") //$NON-NLS-1$ //$NON-NLS-2$
					.append("[").append(formatDependencyKeyHTML(sdList.get(i).getTagKey())) //$NON-NLS-1$
					.append(" ") //$NON-NLS-1$
					.append(sdList.get(i).getCount()).append("] "); //$NON-NLS-1$

				} else {
					sb.append("<font color=" + hex + ">") //$NON-NLS-1$ //$NON-NLS-2$
					.append("[").append(formatDependencyKeyHTML(sdList.get(i).getTagKey())) //$NON-NLS-1$
					.append(" ") //$NON-NLS-1$
					.append(sdList.get(i).getCount()).append("]"); //$NON-NLS-1$
				}
				sb.append("</font>"); //$NON-NLS-1$
				flipColor = !flipColor;
			}
			sb.append("</html>"); //$NON-NLS-1$
			//System.out.println(sb.toString());

			return sb.toString();
		}

		private Color changeColor(boolean flip){
			if(flip){
				return new Color(ConfigRegistry.getGlobalRegistry()
						.getInteger("plugins.errorMining.appearance.resultMatrix.firstItem")); //$NON-NLS-1$

			}
			return new Color(ConfigRegistry.getGlobalRegistry()
					.getInteger("plugins.errorMining.appearance.resultMatrix.secondItem")); //$NON-NLS-1$
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

	protected JTabbedPane createTabbedPane() {
		return new ClosableTabbedPane();
	}

	protected void checkViewMode(boolean instertionPending) {
		int tabCount = tabbedPane==null ? 0 : tabbedPane.getTabCount();

		if(tabCount==0 && instertionPending) {
			// Expand
			expandView();
		} else if(tabCount==2 && !instertionPending) {
			// Shrink (only overview + detail tab are left)
			shrinkView();
		}
	}

	protected void expandView() {
		if(tabbedPane!=null) {
			return;
		}

		tabbedPane = createTabbedPane();
		//String title = ResourceManager.getInstance().get(
		//		"plugins.searchTools.searchResultPresenter.labels.overview"); //$NON-NLS-1$

		//contentPanel.remove(overviewPanel);
		//tabbedPane.insertTab(title, null, overviewPanel, null, 0);

		//contentPanel.add(tabbedPane, BorderLayout.CENTER);
	}

	protected void shrinkView() {
		if(tabbedPane==null) {
			return;
		}

		for(int i=2; i<tabbedPane.getTabCount(); i++) {
			SubNgramResultContainer container = (SubNgramResultContainer)tabbedPane.getComponentAt(i);
			container.close();
		}

		int tabCount = tabbedPane.getTabCount();
		while(tabCount > 2){
			tabbedPane.remove(tabCount);
			tabCount--;
		}

		//tabbedPane.removeAll();
		//contentPanel.remove(tabbedPane);
		//contentPanel.add(overviewPanel, BorderLayout.CENTER);
		//tabbedPane = null;
	}





	/**
	 *
	 * @author Gregor Thiele
	 * @version $Id$
	 *
	 */
	protected class CustomPieLabelGenerator extends StandardPieSectionLabelGenerator {

		private static final long serialVersionUID = -4726538399012030031L;

		NumberFormat defaultFormat = NumberFormat.getPercentInstance();
		boolean barCompare = false;

		public CustomPieLabelGenerator(){
			defaultFormat.setMinimumFractionDigits(2);
		}

		/**
		 * @see org.jfree.chart.labels.PieSectionLabelGenerator#generateSectionLabel(org.jfree.data.general.PieDataset, java.lang.Comparable)
		 */
		@Override
		public String generateSectionLabel(PieDataset dataset, Comparable key) {

			StringBuilder label = new StringBuilder();
			label.append(key);
			label.append(" ("); //$NON-NLS-1$
			if(showPercentage){
				label.append(defaultFormat.format(
						dataset.getValue(key).doubleValue()/getTotalCount(dataset)));
			} else {
				label.append(dataset.getValue(key).intValue());
			}
			label.append(")"); //$NON-NLS-1$

			return label.toString();
		}

		/**
		 * @see org.jfree.chart.labels.PieSectionLabelGenerator#generateAttributedSectionLabel(org.jfree.data.general.PieDataset, java.lang.Comparable)
		 */
		@Override
		public AttributedString generateAttributedSectionLabel(
				PieDataset dataset, Comparable key) {
			// TO DO Auto-generated method stub
			return getAttributedLabel(dataset.getIndex(key));
		}

		public double getTotalCount(PieDataset dataset){
			double total = 0;
			for(int col = 0; col < dataset.getKeys().size(); col++){
				total = total + dataset.getValue(dataset.getKey(col)).doubleValue();
			}
			//System.out.println("TOTAL "+ total);
			return total;
		}

	}



	/**
	 *
	 * @author Gregor Thiele
	 * @version $Id$
	 *
	 */

	protected class CustomPieToolTipGenerator implements PieToolTipGenerator {

		/**
		 * @see org.jfree.chart.labels.PieToolTipGenerator#generateToolTip(org.jfree.data.general.PieDataset, java.lang.Comparable)
		 */
		@Override
		public String generateToolTip(PieDataset dataset, Comparable key) {
			return StringUtil.formatDecimal(dataset.getValue(key).intValue());
		}
	}

	protected class CustomPieDrawingSupplier extends DefaultDrawingSupplier{


		private static final long serialVersionUID = -789075994286903520L;



	}



	protected class CustomLabelGenerator implements CategoryItemLabelGenerator {

		NumberFormat defaultFormat = NumberFormat.getPercentInstance();

		public CustomLabelGenerator(){
			defaultFormat.setMinimumFractionDigits(2);
		}


		/**
		 * @see org.jfree.chart.labels.CategoryItemLabelGenerator#generateColumnLabel(org.jfree.data.category.CategoryDataset, int)
		 */
		@Override
		public String generateColumnLabel(CategoryDataset dataset, int column) {
			// TO DO Auto-generated method stub
			return null;
		}

		/**
		 * @see org.jfree.chart.labels.CategoryItemLabelGenerator#generateLabel(org.jfree.data.category.CategoryDataset, int, int)
		 */
		@Override
		public String generateLabel(CategoryDataset dataset, int row, int column) {

			String label = null;
			if(showPercentage){
				label  = defaultFormat.format(
						dataset.getValue(row, column).doubleValue()/
											getTotalCount(dataset, row, column ));
			} else {
				label = StringUtil.formatDecimal(dataset.getValue(row, column).intValue());
			}

			//System.out.println("Label " + label);
			//System.out.println(row + " |col " + column);
			//System.out.println("Total " + getTotalCount(dataset, row, column));

			return label;
		}


		public double getTotalCount(CategoryDataset dataset, int row, int column){
			double total = 0;

			if(barCompare){
				total = dataset.getValue(0, column).doubleValue();
			} else {
				for(int col = 0; col < dataset.getColumnCount(); col++){
					total = total + dataset.getValue(row, col).doubleValue();
				}
			}
			return total;
		}

		/**
		 * @see org.jfree.chart.labels.CategoryItemLabelGenerator#generateRowLabel(org.jfree.data.category.CategoryDataset, int)
		 */
		@Override
		public String generateRowLabel(CategoryDataset dataset, int row) {
			// TO DO Auto-generated method stub
			return null;
		}
	}


	protected class CustomToolTipGenerator implements CategoryToolTipGenerator  {
	    @Override
		public String generateToolTip(CategoryDataset dataset, int row, int column) {
	    	String label = StringUtil.formatDecimal(dataset.getValue(row, column).intValue());
	    	return label;
	    }
	}


	protected class CustomBarHighlightRenderer extends BarRenderer {

		private static final long serialVersionUID = 1791567745153742371L;

		/** The row to highlight (-1 for none). */
	    private int highlightRow = -1;

	    /** The column to highlight (-1 for none). */
	    private int highlightColumn = -1;

	    /**
	     * Sets the item to be highlighted (use (-1, -1) for no highlight).
	     *
	     * @param r  the row index.
	     * @param c  the column index.
	     */
	    public void setHighlightedItem(int r, int c) {
	        if (this.highlightRow == r && this.highlightColumn == c) {
	            return;  // nothing to do
	        }
	        this.highlightRow = r;
	        this.highlightColumn = c;
	        notifyListeners(new RendererChangeEvent(this));
	    }

	    /**
	     * Return a special color for the highlighted item.
	     *
	     * @param row  the row index.
	     * @param column  the column index.
	     *
	     * @return The outline paint.
	     */
	    @Override
		public Paint getItemOutlinePaint(int row, int column) {
	        if (row == this.highlightRow && column == this.highlightColumn) {
	            return Color.green;
	        }
	        return super.getItemOutlinePaint(row, column);

	    }
	}


	protected class ClosableTabbedPane extends JTabbedPane implements TabController {

		private static final long serialVersionUID = -8989316268794923006L;

		/**
		 * @see de.ims.icarus.ui.tab.TabController#closeTab(java.awt.Component)
		 */
		@Override
		public boolean closeTab(Component comp) {
			if(comp instanceof SubNgramResultContainer) {
				SubNgramResultContainer container = (SubNgramResultContainer) comp;
				container.close();
			}

			remove(comp);
			checkViewMode(false);

			return true;
		}

		/**
		 * @see de.ims.icarus.ui.tab.TabController#closeChildren(java.awt.Component)
		 */
		@Override
		public boolean closeChildren(Component comp) {
			// Not supported
			return false;
		}
	}


	protected class SubNgramResultContainer extends JPanel {

		private static final long serialVersionUID = -124096642718184615L;

		//detail stuff
		private ListPresenter listPresenter;
		private AWTPresenter detailsPresenter;

		private JSplitPane splitpaneDetails;
		private SentenceDataList sentenceList;
		private final String title;


		public SubNgramResultContainer(String title, SentenceDataList sentenceList) {
			super(new BorderLayout());
			if(title==null)
				throw new NullPointerException("Invalid title"); //$NON-NLS-1$

			this.sentenceList = sentenceList;
			this.title = title;

			splitpaneDetails = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitpaneDetails.setContinuousLayout(true);
			splitpaneDetails.setDividerSize(5);
			splitpaneDetails.setBorder(null);
			splitpaneDetails.setResizeWeight(1);
			splitpaneDetails.addComponentListener(getHandler());
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


		public ListPresenter getListPresenter(){
			return listPresenter;
		}

		public AWTPresenter getDetailsPresenter(){
			return detailsPresenter;
		}

		public String getTitle() {
			return title;
		}


		public SentenceDataList getSentenceDataList() {
			return sentenceList;
		}


		public void init() {
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
				detailsPresenter = UIHelperRegistry.globalRegistry().findHelper(GraphBasedPresenter.class, entryType, true, false);
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
			add(splitpaneDetails, BorderLayout.CENTER);

			if(sentenceList.size() > 0) {
				listPresenter.getSelectionModel().setSelectionInterval(0, 0);
			} else {
				listPresenter.getSelectionModel().clearSelection();
			}
		}



		public void displaySelectedData() throws Exception {
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

		public void close() {
			try {
				listPresenter.close();
				detailsPresenter.close();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to close presenter tab for ngram-result: "+getTitle(), e); //$NON-NLS-1$
			}
		}
	}


}
