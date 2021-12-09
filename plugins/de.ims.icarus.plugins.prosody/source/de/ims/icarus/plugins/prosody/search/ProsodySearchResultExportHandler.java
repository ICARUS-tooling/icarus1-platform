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
package de.ims.icarus.plugins.prosody.search;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.ims.icarus.Core;
import de.ims.icarus.io.IOUtil;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.annotation.AnnotatedProsodicSentenceData;
import de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotationManager;
import de.ims.icarus.plugins.prosody.annotation.ProsodyHighlighting;
import de.ims.icarus.plugins.prosody.pattern.PatternDataProxy;
import de.ims.icarus.plugins.prosody.pattern.ProsodyLevel;
import de.ims.icarus.plugins.prosody.pattern.ProsodyPatternContext;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.SearchResultExportHandler;
import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.helper.DirectoryFileFilter;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.ToolException;
import de.ims.icarus.util.strings.StringUtil;
import de.ims.icarus.util.strings.pattern.TextSource;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodySearchResultExportHandler implements SearchResultExportHandler, ProsodyConstants {

	// Location UI
	private JRadioButton rbSingleFile, rbFolder;
	private JButton bSingleFile, bFolder;
	private JTextField tfSingleFile, tfFolder, tfFileNamePattern;
	private JComboBox<Object> cbGroupBy;
	private JComboBox<String> cbEncoding;
	private JCheckBox cbClearFolder;

	// Format UI
	private JTextArea taHeader, taContent;
	private JButton bInsertPatternHeader, bInsertPatternContent;
	private JPopupMenu patterMenu;
	private JComboBox<ProsodyLevel> cbLevel;

	private JPanel contentPanel;
	private Handler handler;

	private boolean initAndShowGUI() {
		// Ensure we are on the event-dispatch thread

		if(!SwingUtilities.isEventDispatchThread()) {
			final MutableBoolean result = new MutableBoolean(false);
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						result.setValue(initAndShowGUI());
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				LoggerFactory.error(this, "Error dispatching result export handler GUI to event-dispatch thread", e); //$NON-NLS-1$
			}

			return result.getValue();
		}

		handler = new Handler();

		ResourceManager rm = ResourceManager.getInstance();
		IconRegistry ir = IconRegistry.getGlobalRegistry();

		// Location Panel
		FormLayout locationLayout = new FormLayout(
				"20dlu, pref, 3dlu, fill:pref:grow, 2dlu, pref", //$NON-NLS-1$
				"pref, 6dlu, pref, 2dlu, pref, 2dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref"); //$NON-NLS-1$
		locationLayout.setRowGroups(new int[][]{{1, 3, 5, 7}});
		JPanel locationPanel = new JPanel(locationLayout);
		locationPanel.setBorder(UIUtil.defaultContentBorder);

		// Single File
		rbSingleFile = new JRadioButton(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.file")); //$NON-NLS-1$
		rbSingleFile.setToolTipText(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.tooltips.file")); //$NON-NLS-1$
		rbSingleFile.addActionListener(handler);
		tfSingleFile = new JTextField();
//		tfSingleFile.setText("D:\\Workspaces\\Default\\Icarus\\export\\test.txt"); //$NON-NLS-1$ //FIXME debug
		bSingleFile = new JButton(ir.getIcon("fldr_obj.gif")); //$NON-NLS-1$
		bSingleFile.setToolTipText(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.tooltips.selectFile")); //$NON-NLS-1$
		bSingleFile.addActionListener(handler);

		locationPanel.add(rbSingleFile, CC.rcw(1, 1, 2));
		locationPanel.add(tfSingleFile, CC.rc(1, 4));
		locationPanel.add(bSingleFile, CC.rc(1, 6));

		// Multiple Files
		rbFolder = new JRadioButton(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.folder")); //$NON-NLS-1$
		rbFolder.setToolTipText(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.tooltips.folder")); //$NON-NLS-1$
		rbFolder.addActionListener(handler);
		tfFolder = new JTextField();
//		tfFolder.setText("D:\\Workspaces\\Default\\Icarus\\export\\test"); //FIXME debug //$NON-NLS-1$
		bFolder = new JButton(ir.getIcon("fldr_obj.gif")); //$NON-NLS-1$
		bFolder.setToolTipText(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.tooltips.selectFolder")); //$NON-NLS-1$
		bFolder.addActionListener(handler);

		locationPanel.add(rbFolder, CC.rcw(3, 1, 2));
		locationPanel.add(tfFolder, CC.rc(3, 4));
		locationPanel.add(bFolder, CC.rc(3, 6));

		JLabel lClearFolder = new JLabel(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.clearFolder")); //$NON-NLS-1$
		cbClearFolder = new JCheckBox();
		cbClearFolder.setToolTipText(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.tooltips.clearFolder")); //$NON-NLS-1$

		locationPanel.add(lClearFolder, CC.rc(5, 2));
		locationPanel.add(cbClearFolder, CC.rc(5, 4));

		tfFileNamePattern = new JTextField();
		tfFileNamePattern.setText("{env:group_label}_({env:group_size}).txt"); //FIXME debug //$NON-NLS-1$
		JLabel lFileNamePattern = new JLabel(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.fileNamePattern")); //$NON-NLS-1$

		locationPanel.add(lFileNamePattern, CC.rc(7, 2));
		locationPanel.add(tfFileNamePattern, CC.rc(7, 4));

		DefaultComboBoxModel<Object> groupByModel = new DefaultComboBoxModel<>();
		groupByModel.addElement(new GroupIdGroupSelector());
		groupByModel.addElement(new GroupLabelGroupSelector());
		groupByModel.addElement("{doc:"+DOCUMENT_ID+"}"); //$NON-NLS-1$ //$NON-NLS-2$
		groupByModel.addElement("{doc:"+INDEX_KEY+"}"); //$NON-NLS-1$ //$NON-NLS-2$
		groupByModel.addElement("{doc:"+AUDIO_FILE_KEY+"}"); //$NON-NLS-1$ //$NON-NLS-2$
		groupByModel.addElement("{sent:"+SENTENCE_NUMBER_KEY+"}"); //$NON-NLS-1$ //$NON-NLS-2$
		groupByModel.addElement("{sent:"+INDEX_KEY+"}"); //$NON-NLS-1$ //$NON-NLS-2$
		groupByModel.addElement("{word:"+FORM_KEY+"}"); //$NON-NLS-1$ //$NON-NLS-2$
		groupByModel.addElement("{word:"+POS_KEY+"}"); //$NON-NLS-1$ //$NON-NLS-2$
		//TODO fill model (add some typical examples)
		cbGroupBy = new JComboBox<>(groupByModel);
		cbGroupBy.setEditable(true);
		cbGroupBy.setSelectedItem("{doc:documentId}"); //FIXME debug //$NON-NLS-1$
		JLabel lGroupBy = new JLabel(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.groupBy")); //$NON-NLS-1$

		locationPanel.add(lGroupBy, CC.rc(9, 2));
		locationPanel.add(cbGroupBy, CC.rc(9, 4));

		ButtonGroup typeGroup = new ButtonGroup();
		typeGroup.add(rbFolder);
		typeGroup.add(rbSingleFile);

		// Initial selection
		rbSingleFile.setSelected(true);
		switchLocationType(true);

		locationPanel.add(new JSeparator(SwingConstants.HORIZONTAL), CC.rchw(11, 1, 1, 6));

		Vector<String> encodings = new Vector<>(Charset.availableCharsets().keySet());
		DefaultComboBoxModel<String> encodingModel = new DefaultComboBoxModel<>(encodings);
		cbEncoding = new JComboBox<>(encodingModel);
		cbEncoding.setEditable(false);
		cbEncoding.setSelectedItem(IOUtil.UTF8_ENCODING);
		JLabel lEncoding = new JLabel(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.encoding")); //$NON-NLS-1$

		locationPanel.add(lEncoding, CC.rc(13, 2));
		locationPanel.add(cbEncoding, CC.rc(13, 4));

		// Format panel
		JPanel formatPanel = new JPanel(new BorderLayout());
		formatPanel.setBorder(UIUtil.defaultContentBorder);

		// Toolbar
		JLabel lLevel = new JLabel(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.outputLevel")); //$NON-NLS-1$
		lLevel.setToolTipText(rm.get("plugins.prosody.prosodySearchResultExportHandler.tooltips.outputLevel")); //$NON-NLS-1$
		lLevel.setBorder(UIUtil.defaultContentBorder);
		DefaultComboBoxModel<ProsodyLevel> levelModel = new DefaultComboBoxModel<>();
		levelModel.addElement(ProsodyLevel.SYLLABLE);
		levelModel.addElement(ProsodyLevel.WORD);
		levelModel.addElement(ProsodyLevel.SENTENCE);
		levelModel.addElement(ProsodyLevel.DOCUMENT);
		levelModel.setSelectedItem(ProsodyLevel.SYLLABLE);
		cbLevel = new JComboBox<>(levelModel);
		cbLevel.setEditable(false);
		JToolBar tbFormat = ActionManager.globalManager().createEmptyToolBar();
		tbFormat.add(lLevel);
		tbFormat.add(Box.createGlue());
		tbFormat.add(cbLevel);

		formatPanel.add(tbFormat, BorderLayout.NORTH);

		// Header
		JPanel pHeader = new JPanel(new BorderLayout(0, 6));
		JLabel lHeader = new JLabel(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.headerFormat")); //$NON-NLS-1$
		lHeader.setBorder(UIUtil.defaultContentBorder);
		taHeader = new JTextArea();
		taHeader.setLineWrap(true);
		taHeader.setWrapStyleWord(true);
		taHeader.setBorder(null);
		taHeader.setText("File: {env:current_file}\nDate: {env:current_date}\nLines: {env:group_size}\nContent-Pattern: {env:content_pattern}\n#"); //FIXME debug //$NON-NLS-1$
		bInsertPatternHeader = new JButton(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.insertPattern")); //$NON-NLS-1$
		bInsertPatternHeader.setToolTipText(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.tooltips.insertPattern")); //$NON-NLS-1$
		bInsertPatternHeader.addActionListener(handler);
		JScrollPane spHeader = new JScrollPane(taHeader);
		JToolBar tbHeader = ActionManager.globalManager().createEmptyToolBar();
		tbHeader.add(lHeader);
		tbHeader.add(Box.createGlue());
		tbHeader.add(bInsertPatternHeader);
		pHeader.add(tbHeader, BorderLayout.NORTH);
		pHeader.add(spHeader, BorderLayout.CENTER);

		// Content
		JPanel pContent = new JPanel(new BorderLayout(0, 6));
		JLabel lContent = new JLabel(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.contentFormat")); //$NON-NLS-1$
		lContent.setBorder(UIUtil.defaultContentBorder);
		taContent = new JTextArea();
		taContent.setLineWrap(true);
		taContent.setWrapStyleWord(true);
		taContent.setBorder(null);
		taContent.setText("{env:current_row}\t{syl:syllable_form}"); //FIXME debug //$NON-NLS-1$
		bInsertPatternContent = new JButton(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.insertPattern")); //$NON-NLS-1$
		bInsertPatternContent.setToolTipText(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.tooltips.insertPattern")); //$NON-NLS-1$
		bInsertPatternContent.addActionListener(handler);
		JScrollPane spContent = new JScrollPane(taContent);
		JToolBar tbContent = ActionManager.globalManager().createEmptyToolBar();
		tbContent.add(lContent);
		tbContent.add(Box.createGlue());
		tbContent.add(bInsertPatternContent);
		pContent.add(tbContent, BorderLayout.NORTH);
		pContent.add(spContent, BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		splitPane.setBorder(null);
		splitPane.setDividerLocation(110);
		splitPane.setDividerSize(7);
		splitPane.setTopComponent(pHeader);
		splitPane.setBottomComponent(pContent);

		formatPanel.add(splitPane, BorderLayout.CENTER);

		// Main layout
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.location"), //$NON-NLS-1$
				locationPanel);
		tabbedPane.add(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.format"), //$NON-NLS-1$
				formatPanel);

		contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(tabbedPane, BorderLayout.CENTER);

		contentPanel.setPreferredSize(new Dimension(500, 400));


		return DialogFactory.getGlobalFactory().showGenericDialog(null,
				ResourceManager.getInstance().get("plugins.prosody.prosodySearchResultExportHandler.dialogs.title"), //$NON-NLS-1$
				null, contentPanel, true, "export", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void switchLocationType(boolean isSingleFile) {
		tfSingleFile.setEditable(isSingleFile);
		bSingleFile.setEnabled(isSingleFile);


		tfFolder.setEnabled(!isSingleFile);
		bFolder.setEnabled(!isSingleFile);
		cbClearFolder.setEnabled(!isSingleFile);
		tfFileNamePattern.setEnabled(!isSingleFile);
		cbGroupBy.setEnabled(!isSingleFile);
	}

	//FIXME add loca for left out keys!!!
	private String[] getEnvironmentProperties() {
		return new String[]{
			CURRENT_FILE_KEY,
			CURRENT_FILENAME_KEY,
			CURRENT_TIME_KEY,
			CURRENT_DATE_KEY,
			CURRENT_ROW_KEY,
			TOTAL_MATCH_COUNT_KEY,
			TOTAL_HIT_COUNT_KEY,
			GROUP_COUNT_KEY,
			QUERY_KEY,
			TARGET_KEY,

			GROUP_INDEX_KEY,
			GROUP_LABEL_KEY,
			GROUP_SIZE_KEY,

			HEADER_PATTERN_KEY,
			CONTENT_PATTERN_KEY,

			GROUPS_LABEL_KEY,
		};
	}

	private static String getLabel(ProsodyLevel level, String property) {
		return ResourceManager.getInstance().get(
				"plugins.prosody.properties."+level.getKey()+"."+property+".name"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private static String getToolTip(ProsodyLevel level, String property) {
		return ResourceManager.getInstance().get(
				"plugins.prosody.properties."+level.getKey()+"."+property+".description"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private void showPatternMenu(JButton invoker) {
		JPopupMenu patterMenu = new JPopupMenu();

		for(ProsodyLevel level : ProsodyLevel.values()) {

			if(invoker==bInsertPatternHeader && level!=ProsodyLevel.ENVIRONMENT) {
				continue;
			}

			JMenu menu = new JMenu(level.getName());
			menu.setToolTipText(level.getDescription());

			String[] properties = level.getAvailableProperties();
			if(level==ProsodyLevel.ENVIRONMENT) {
				properties = getEnvironmentProperties();
			}

			if(properties!=null) {
				Arrays.sort(properties);

				for(String property : properties) {
					String label = getLabel(level, property);
					String tooltip = getToolTip(level, property);
					String statement = ProsodyPatternContext.createStatement(level, property);

					JMenuItem menuItem = menu.add(label+" "+statement); //$NON-NLS-1$
					menuItem.setToolTipText(tooltip);
					menuItem.putClientProperty("pattern", statement); //$NON-NLS-1$
					menuItem.putClientProperty("isContent", invoker==bInsertPatternContent); //$NON-NLS-1$

					menuItem.addActionListener(handler);
				}
			}

			menu.setEnabled(properties!=null && properties.length>0);

			patterMenu.add(menu);
		}

		patterMenu.show(invoker, invoker.getWidth(), 0);
	}

	// Global Environment
	public static final String CURRENT_FILE_KEY = "current_file"; //$NON-NLS-1$
	public static final String CURRENT_FILENAME_KEY = "current_filename"; //$NON-NLS-1$
	public static final String CURRENT_TIME_KEY = "current_time"; //$NON-NLS-1$
	public static final String CURRENT_DATE_KEY = "current_date"; //$NON-NLS-1$
	public static final String TOTAL_MATCH_COUNT_KEY = "total_matches"; //$NON-NLS-1$
	public static final String TOTAL_HIT_COUNT_KEY = "total_hits"; //$NON-NLS-1$
	public static final String GROUP_COUNT_KEY = "group_count"; //$NON-NLS-1$
	public static final String QUERY_KEY = "query"; //$NON-NLS-1$
	public static final String TARGET_KEY = "target"; //$NON-NLS-1$
	public static final String CURRENT_ROW_KEY = "current_row"; //$NON-NLS-1$

	public static final String HEADER_PATTERN_KEY = "header_pattern"; //$NON-NLS-1$
	public static final String CONTENT_PATTERN_KEY = "content_pattern"; //$NON-NLS-1$

	// Grouping Environment
	public static final String GROUP_LABEL_KEY = "group_label"; //$NON-NLS-1$
	public static final String GROUP_SIZE_KEY = "group_size"; //$NON-NLS-1$
	public static final String GROUP_INDEX_KEY = "group_index"; //$NON-NLS-1$
	public static final String HIT_COUNT_KEY = "hit_count"; //$NON-NLS-1$
	public static final String GROUPS_LABEL_KEY = "groups_label"; //$NON-NLS-1$
	public static final String GROUP_0_LABEL_KEY = "group1_label"; //$NON-NLS-1$
	public static final String GROUP_1_LABEL_KEY = "group2_label"; //$NON-NLS-1$
	public static final String GROUP_2_LABEL_KEY = "group3_label"; //$NON-NLS-1$
	public static final String GROUPS_ID_KEY = "groups_id"; //$NON-NLS-1$
	public static final String GROUP_0_ID_KEY = "group1_id"; //$NON-NLS-1$
	public static final String GROUP_1_ID_KEY = "group2_id"; //$NON-NLS-1$
	public static final String GROUP_2_ID_KEY = "group3_id"; //$NON-NLS-1$


	// Export states and utilities
	private final List<Path> filesList = new ArrayList<>();
	private final List<Options> environmentsList = new ArrayList<>();
	private final List<List<EntryInfo>> entryList = new ArrayList<>();

	private final Options globalEnvironment = new Options();

	private boolean singleFileExport;
	private GroupSelector groupSelector;

	private SearchResult searchResult;

	private final PatternDataProxy dataDummy = new PatternDataProxy();

	/**
	 * @see de.ims.icarus.search_tools.SearchResultExportHandler#exportResult(de.ims.icarus.search_tools.result.SearchResult)
	 */
	@Override
	public void exportResult(SearchResult searchResult) throws ToolException {
		if (searchResult == null)
			throw new NullPointerException("Invalid searchResult"); //$NON-NLS-1$

		this.searchResult = searchResult;

		if(!initAndShowGUI()) {
			// Cancelled by user
			return;
		}

		doExport();
	}

	private void doExport() throws ToolException {

		final Charset encoding = Charset.forName((String) cbEncoding.getSelectedItem());

		// Fetch pattern
		String headerPattern = taHeader.getText().trim();
		String contentPattern = taContent.getText().trim();

		// Header pattern is allowed to be empty, content pattern is not!
		if(contentPattern==null || contentPattern.isEmpty())
			throw new ToolException("No content pattern defined"); //$NON-NLS-1$

		final ProsodyLevel outputLevel = (ProsodyLevel) cbLevel.getSelectedItem();

		// Create content text source
		TextSource contentTextSource = null;
		try {
			contentTextSource = ProsodyPatternContext.createTextSource(outputLevel, contentPattern);
		} catch (ParseException e) {
			throw new ToolException("Content pattern definition is invalid", e); //$NON-NLS-1$
		}

		// Create header text source
		TextSource headerTextSource = null;
		try {
			if(headerPattern!=null && !headerPattern.isEmpty()) {
				headerTextSource = ProsodyPatternContext.createTextSource(outputLevel, headerPattern);
			}
		} catch (ParseException e) {
			throw new ToolException("Header pattern definition is invalid", e); //$NON-NLS-1$
		}

		globalEnvironment.put(CURRENT_DATE_KEY, new Date());
		globalEnvironment.put(CURRENT_TIME_KEY, System.currentTimeMillis());
		globalEnvironment.put(TOTAL_MATCH_COUNT_KEY, searchResult.getTotalMatchCount());
		globalEnvironment.put(TOTAL_HIT_COUNT_KEY, searchResult.getTotalHitCount());
		globalEnvironment.put(GROUP_COUNT_KEY, searchResult.getDimension());
		globalEnvironment.put(QUERY_KEY, searchResult.getSource().getQuery().getQueryString());
		globalEnvironment.put(TARGET_KEY, StringUtil.getName(searchResult.getSource().getTarget()));
		globalEnvironment.put(HEADER_PATTERN_KEY, headerPattern);
		globalEnvironment.put(CONTENT_PATTERN_KEY, contentPattern);

		singleFileExport = rbSingleFile.isSelected();

		if(singleFileExport) {
			// One file for all content

			Path file = Paths.get(tfSingleFile.getText());
			filesList.add(file);

			List<EntryInfo> entries = new ArrayList<>(searchResult.getTotalMatchCount());
			for(int i=0; i<searchResult.getTotalMatchCount(); i++) {
				entries.add(new EntryInfo(searchResult.getRawEntry(i), i));
			}

			Collections.sort(entries);
			entryList.add(entries);

			Options environment = globalEnvironment.clone();
			environment.put(CURRENT_FILE_KEY, file.toString());
			environment.put(CURRENT_FILENAME_KEY, file.getFileName());

			environmentsList.add(environment);

		} else {
			// Group stuff

			final Path root = Paths.get(tfFolder.getText());
			final boolean clearFolder = cbClearFolder.isSelected();

			if(!Files.exists(root)) {
//				throw new ToolException("Designated root folder for export does not exist: "+root); //$NON-NLS-1$
				try {
					Files.createDirectory(root);
				} catch (IOException e) {
					throw new ToolException("Failed to create root folder for export: "+root, e); //$NON-NLS-1$
				}
			} else if(clearFolder) {
				try {
					IOUtil.cleanDirectory(root);
				} catch (IOException e) {
					throw new ToolException("Failed to clear root folder for export: "+root, e); //$NON-NLS-1$
				}
			}

			// Create filename text source
			TextSource filenameTextSource = null;
			try {
				filenameTextSource = ProsodyPatternContext.createTextSource(outputLevel, tfFileNamePattern.getText());
			} catch (ParseException e) {
				throw new ToolException("Filename pattern definition is invalid", e); //$NON-NLS-1$
			}

			int dimension = searchResult.getDimension();

			// Fetch selector
			groupSelector = getGroupSelector(cbGroupBy.getSelectedItem());

			Map<String, List<EntryInfo>> map = new HashMap<>(100);

			// Perform grouping
			if(dimension==0) {
				groupWithoutGroups(map);
			} else {
				groupRecursive(map, new int[dimension], 0);
			}

			List<String> groups = new ArrayList<>(map.keySet());
			Collections.sort(groups);

			Set<String> filenames = new HashSet<>(groups.size());

			for(int i=0; i<groups.size(); i++) {
				String group = groups.get(i);
				List<EntryInfo> list = map.get(group);

				Options environment = globalEnvironment.clone();
				environment.put(GROUP_LABEL_KEY, group);
				environment.put(GROUP_SIZE_KEY, list.size());
				environment.put(GROUP_INDEX_KEY, i);

				dataDummy.clear();

				String filename = filenameTextSource.getText(dataDummy, environment);

				if(filename==null || filename.isEmpty())
					throw new ToolException("Generated filename is null or empty for group: "+group); //$NON-NLS-1$

				if(filename.indexOf(File.separatorChar)!=-1)
					throw new ToolException("Generated filename contains illegal separator character: "+filename); //$NON-NLS-1$

				if(filename.indexOf(File.pathSeparatorChar)!=-1)
					throw new ToolException("Generated filename contains illegal path separator character: "+filename); //$NON-NLS-1$

				if(!filenames.add(filename))
					throw new ToolException("Duplicate filename '"+filename+"' for group: "+group); //$NON-NLS-1$ //$NON-NLS-2$

				// Ensure sorted entry lists
				Collections.sort(list);

				Path file = root.resolve(filename);
				environment.put(CURRENT_FILE_KEY, file.toString());
				environment.put(CURRENT_FILENAME_KEY, file.getFileName());

				entryList.add(list);
				filesList.add(file);
				environmentsList.add(environment);
			}
		}

		// Sanity check for messed up grouping
		if(filesList.size()!=entryList.size() || entryList.size()!=environmentsList.size())
			throw new CorruptedStateException("Something went wrong, sizes of export buffers do not match..."); //$NON-NLS-1$

		// Grouping done - now start the real export

		for(int i=0; i<filesList.size(); i++) {
			Path file = filesList.get(i);
			List<EntryInfo> entries = entryList.get(i);
			Options environment = environmentsList.get(i);

			final OutputHandler outputHandler = getOutputHandler(outputLevel);
			int row = -1;

			try(BufferedWriter writer = Files.newBufferedWriter(file, encoding,
					StandardOpenOption.CREATE,
					StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING)) {

				outputHandler.init(searchResult, entries, environment);

				dataDummy.clear();

				// Write optional header
				if(headerTextSource!=null) {
					String header = headerTextSource.getText(dataDummy, environment);
					if(header!=null && !header.isEmpty()) {
						writer.write(header);
						writer.newLine();
					}
				}

				// Iterate output items
				while(outputHandler.next()) {
					row++;

					if(row>0) {
						writer.newLine();
					}

					environment.put(CURRENT_ROW_KEY, row);

					String content = contentTextSource.getText(outputHandler.getBuffer(), environment);

					// We allow empty lines, linebreak is added on next iteration
					if(content!=null && !content.isEmpty()) {
						writer.write(content);
					}
				}

			} catch (IOException e) {
				throw new ToolException("Failed to export group "+i+" to file "+file, e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		//TODO complete!
	}

	private OutputHandler getOutputHandler(ProsodyLevel outputLevel) {

		switch (outputLevel) {
		case SYLLABLE: return new SyllableOutputHandler();
		case WORD: return new WordOutputHandler();
		case SENTENCE: return new SentenceOutputHandler();
		case DOCUMENT: return new DocumentOutputHandler();

		default:
			throw new CorruptedStateException("Unsupported output level: "+outputLevel); //$NON-NLS-1$
		}
	}

	private GroupSelector getGroupSelector(Object source) throws ToolException {
		if (source == null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$

		if(source instanceof GroupSelector) {
			return (GroupSelector) source;
		}

		if(source instanceof String) {
			String pattern = (String) source;

			try {
				return new PatternGroupSelector(pattern);
			} catch (ParseException e) {
				throw new ToolException("Invalid pattern string for group selector: "+pattern, e); //$NON-NLS-1$
			}
		}

		throw new ToolException("Invalid source - must be a GroupSelector implementation or a pattern string: "+source.getClass()); //$NON-NLS-1$
	}

	private void groupWithoutGroups(Map<String, List<EntryInfo>> map) throws ToolException {

		for(int i=0; i<searchResult.getTotalMatchCount(); i++) {
			ResultEntry entry = searchResult.getRawEntry(i);
			EntryInfo info = new EntryInfo(entry, i);

			// Group without grouping indices
			Object group = groupSelector.getGroup(searchResult, i, entry, globalEnvironment);

			if(group==null)
				throw new ToolException("Unable to fetch gorup assignment for result entry: "+entry); //$NON-NLS-1$

			String key = String.valueOf(group);

			List<EntryInfo> list = map.get(key);

			if(list==null) {
				list = new ArrayList<>();
				map.put(key, list);
			}

			list.add(info);
		}
	}

	private void groupRecursive(Map<String, List<EntryInfo>> map, int[] indices, int groupId) throws ToolException {

		final boolean doRecursion = groupId<indices.length-1;

		for(int i=0; i<searchResult.getInstanceCount(groupId); i++) {
			indices[groupId] = i;

			if(doRecursion) {
				groupRecursive(map, indices, groupId+1);
			} else {
				List<ResultEntry> entries = searchResult.getRawEntryList(indices);

				if(entries==null || entries.isEmpty()) {
					continue;
				}

				final Options environment = globalEnvironment.clone();
				environment.put(GROUPS_ID_KEY, Arrays.toString(indices));
				environment.put(GROUPS_LABEL_KEY, createGroupsLabel(searchResult, indices));
				//TODO add instance labels to environment

				for(ResultEntry entry : entries) {

					EntryInfo info = new EntryInfo(entry, indices);

					// Group with grouping indices
					Object group = groupSelector.getGroup(searchResult, -1, entry, environment, indices);

					if(group==null)
						throw new ToolException("Unable to fetch gorup assignment for result entry: "+entry); //$NON-NLS-1$

					String key = String.valueOf(group);

					List<EntryInfo> list = map.get(key);

					if(list==null) {
						list = new ArrayList<>();
						map.put(key, list);
					}

					list.add(info);
				}
			}
		}
	}

	private static class EntryInfo implements Comparable<EntryInfo> {
		final ResultEntry entry;
		final int globalIndex;
		final int[] indices;

		public EntryInfo(ResultEntry entry, int globalIndex) {
			this.entry = entry;
			this.globalIndex = globalIndex;
			this.indices = null;
		}

		public EntryInfo(ResultEntry entry, int[] indices) {
			this.entry = entry;
			this.globalIndex = -1;
			this.indices = indices.clone();
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(EntryInfo o) {
			return entry.getIndex()-o.entry.getIndex();
		}
	}

	private class Handler implements ActionListener {

		private JFileChooser fileChooser;

		private File showFileChooser(boolean acceptFiles) {
			if(fileChooser==null) {
				fileChooser = new JFileChooser(Core.getCore().getRootFolder().toFile());
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setControlButtonsAreShown(true);
				fileChooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
			}

			ResourceManager rm = ResourceManager.getInstance();

			if(acceptFiles) {
				fileChooser.setAcceptAllFileFilterUsed(true);
				fileChooser.setApproveButtonText(
						rm.get("select")); //$NON-NLS-1$
				fileChooser.setApproveButtonToolTipText(
						rm.get("plugins.prosody.prosodySearchResultExportHandler.tooltips.selectFile")); //$NON-NLS-1$
				fileChooser.setDialogTitle(
						rm.get("plugins.prosody.prosodySearchResultExportHandler.dialogs.selectFileTitle")); //$NON-NLS-1$
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(null);
			} else {
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.setApproveButtonText(
						rm.get("select")); //$NON-NLS-1$
				fileChooser.setApproveButtonToolTipText(
						rm.get("plugins.prosody.prosodySearchResultExportHandler.tooltips.selectFolder")); //$NON-NLS-1$
				fileChooser.setDialogTitle(
						rm.get("plugins.prosody.prosodySearchResultExportHandler.dialogs.selectFolderTitle")); //$NON-NLS-1$
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setFileFilter(new DirectoryFileFilter());
			}

			int result = fileChooser.showDialog(contentPanel, null);

			return result==JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile() : null;
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==rbFolder || e.getSource()==rbSingleFile) {
				switchLocationType(rbSingleFile.isSelected());
			} else if(e.getSource()==bSingleFile) {
				File file = showFileChooser(true);
				if(file!=null) {
					tfSingleFile.setText(file.getAbsolutePath());
				}
			} else if(e.getSource()==bFolder) {
				File file = showFileChooser(false);
				if(file!=null) {
					tfFolder.setText(file.getAbsolutePath());
				}
			} else if(e.getSource()==bInsertPatternContent) {
				showPatternMenu(bInsertPatternContent);
			} else if(e.getSource()==bInsertPatternHeader) {
				showPatternMenu(bInsertPatternHeader);
			} else {
				// Source is a JMenuItem with a "pattern" client property holding
				// the textual pattern to be inserted

				JMenuItem menuItem = (JMenuItem) e.getSource();
				boolean isContent = (boolean) menuItem.getClientProperty("isContent"); //$NON-NLS-1$

				JTextArea textArea = isContent ? taContent : taHeader;

				String pattern = (String) menuItem.getClientProperty("pattern"); //$NON-NLS-1$

				int pos = textArea.getCaretPosition();
				try {
					textArea.getDocument().insertString(pos, pattern, null);
				} catch (BadLocationException ex) {
					LoggerFactory.error(this, "Failed to insert pattern at index "+pos, ex); //$NON-NLS-1$
				}
			}
		}

	}

	private static final ProsodicAnnotationManager annotationManager = new ProsodicAnnotationManager();

	private static void checkResultForGroups(SearchResult searchResult, int...indices) {
		if(searchResult.getDimension()==0)
			throw new IllegalArgumentException("Search result does not declare groups"); //$NON-NLS-1$
		if(indices==null || indices.length==0)
			throw new IllegalArgumentException("Provided group indices array is null or empty"); //$NON-NLS-1$
	}

	private static final StringBuilder buffer = new StringBuilder(50);

	private static final String EMPTY_LABEL = "-"; //$NON-NLS-1$

	private static String createGroupsLabel(SearchResult searchResult, int...indices) {


		buffer.setLength(0);

		for(int groupId=0; groupId<indices.length; groupId++) {

			if(buffer.length()>0) {
				buffer.append('_');
			}

			Object label = EMPTY_LABEL;
			if(groupId!=-1) {
				label = searchResult.getInstanceLabel(groupId, indices[groupId]);
			}

			if(label==null) {
				label = EMPTY_LABEL;
			}

			buffer.append(label);
		}

		return buffer.toString();
	}

	private static abstract class OutputHandler {

		private List<EntryInfo> items;
		private SearchResult searchResult;
		private Options environment;
		protected final PatternDataProxy buffer = new PatternDataProxy();

		public void init(SearchResult searchResult, List<EntryInfo> items, Options environment) {
			this.searchResult = searchResult;
			this.items = items;
			this.environment = environment;
		}

		public PatternDataProxy getBuffer() {
			return buffer;
		}

		protected int entryCount() {
			return items.size();
		}

		protected EntryInfo entryAt(int index) {
			return items.get(index);
		}

		protected ProsodicSentenceData sentenceAt(int index) {
			return (ProsodicSentenceData) ((SentenceDataList)searchResult.getSource().getTarget()).get(index);
		}

		protected AnnotatedProsodicSentenceData annotatedSentenceAt(ResultEntry entry) {
			return (AnnotatedProsodicSentenceData) searchResult.getAnnotatedEntry(entry);
		}

		protected SearchResult getSearchResult() {
			return searchResult;
		}

		protected Options getEnvironment() {
			return environment;
		}

		public abstract boolean next();
	}

	private static class SyllableOutputHandler extends OutputHandler {

		private int entryCursor = 0;
		private EntryInfo currentEntry;
		private TIntList syllables = new TIntArrayList();
		private TIntList words = new TIntArrayList();
		private int hitCursor;

		@Override
		public boolean next() {
			if(currentEntry==null) {
				if(entryCursor>=entryCount()) {
					return false;
				}

				currentEntry = entryAt(entryCursor);
				entryCursor++;
				hitCursor = 0;

				scanEntry();

				if(words.isEmpty())
					throw new CorruptedStateException("Result entry does not contain a highlighted element: "+currentEntry.entry); //$NON-NLS-1$
			}

//			System.out.printf("index=%d word=%d syl=%d\n", //$NON-NLS-1$
//					currentEntry.entry.getIndex(), words.get(hitCursor), syllables.get(hitCursor));

			buffer.set(sentenceAt(currentEntry.entry.getIndex()), words.get(hitCursor), syllables.get(hitCursor));

			hitCursor++;

			if(hitCursor>=syllables.size()) {
				currentEntry = null;
			}

			return true;
		}

		private void scanEntry() {
			syllables.clear();
			words.clear();

			AnnotatedProsodicSentenceData sentence = annotatedSentenceAt(currentEntry.entry);
			annotationManager.setAnnotation(sentence.getAnnotation());

			ProsodyHighlighting highlighting = ProsodyHighlighting.getInstance();

			if(annotationManager.hasAnnotation()) {

				for(int i=0; i<sentence.length(); i++) {
					long highlight = annotationManager.getHighlight(i);

					if(highlighting.isHighlighted(highlight)) {
						int sylCount = sentence.getSyllableCount(i);

						boolean highlightFound = false;

						for(int j=0; j<sylCount; j++) {

							long sylHighlight = sentence.getAnnotation().getHighlight(i, j);
							if(highlighting.isSpecificHighlight(sylHighlight)) {

								syllables.add(j);
								words.add(i);

								highlightFound = true;
							}
						}

						if(!highlightFound) {
							//TODO make some log entry?
							System.out.println("Defaulting to first syllable in word "+i+" because no syllable highlight was found. Sentence is '"+sentence+"'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							syllables.add(0);
							words.add(i);
						}
					}
				}
			}
		}
	}

	private static class WordOutputHandler extends OutputHandler {

		private int entryCursor = 0;
		private EntryInfo currentEntry;
		private TIntList words = new TIntArrayList();
		private int hitCursor;

		@Override
		public boolean next() {
			if(currentEntry==null) {
				if(entryCursor>=entryCount()) {
					return false;
				}

				currentEntry = entryAt(entryCursor);
				entryCursor++;
				hitCursor = 0;

				scanEntry();
			}


			buffer.set(sentenceAt(currentEntry.entry.getIndex()), words.get(hitCursor));

			hitCursor++;

			if(hitCursor>=words.size()) {
				currentEntry = null;
			}

			return true;
		}

		private void scanEntry() {
			words.clear();

			AnnotatedProsodicSentenceData sentence = annotatedSentenceAt(currentEntry.entry);
			annotationManager.setAnnotation(sentence.getAnnotation());

			ProsodyHighlighting highlighting = ProsodyHighlighting.getInstance();

			if(annotationManager.hasAnnotation()) {

				for(int i=0; i<sentence.length(); i++) {
					long highlight = annotationManager.getHighlight(i);

					if(highlighting.isHighlighted(highlight)) {
						words.add(i);
					}
				}
			}
		}
	}

	private static class SentenceOutputHandler extends OutputHandler implements Comparator<EntryInfo> {
		private int entryCursor = 0;
		private EntryInfo currentEntry;

		@Override
		public int compare(EntryInfo o1, EntryInfo o2) {
			return o1.entry.getIndex()-o2.entry.getIndex();
		}

		@Override
		public void init(SearchResult searchResult, List<EntryInfo> items,
				Options environment) {

			// Ensure sorted entries according to their sentence index
			Collections.sort(items, this);

			super.init(searchResult, items, environment);
		}

		@Override
		public boolean next() {
			if(entryCursor>=entryCount()) {
				return false;
			}

			if(currentEntry==null) {
				// Just use the first available sentence

				currentEntry = entryAt(entryCursor);
				entryCursor++;
			} else {
				// Search for the next sentence other than the current one

				for(;;) {
					if(entryCursor>=entryCount()) {
						return false;
					}

					EntryInfo entry = entryAt(entryCursor);
					entryCursor++;

					if(compare(entry, currentEntry)!=0) {
						// New sentence -> use it
						currentEntry = entry;
						break;
					}
				}
			}

			buffer.set(sentenceAt(currentEntry.entry.getIndex()));

			return true;
		}

	}

	private static class DocumentOutputHandler extends OutputHandler implements Comparator<EntryInfo> {
		private int entryCursor = 0;
		private ProsodicDocumentData currentDocument;

		@Override
		public int compare(EntryInfo o1, EntryInfo o2) {
			return o1.entry.getIndex()-o2.entry.getIndex();
		}

		@Override
		public void init(SearchResult searchResult, List<EntryInfo> items,
				Options environment) {

			// Ensure sorted entries according to their sentence index
			Collections.sort(items, this);

			super.init(searchResult, items, environment);
		}

		@Override
		public boolean next() {
			if(entryCursor>=entryCount()) {
				return false;
			}

			if(currentDocument==null) {
				// Just use the first available document

				EntryInfo entry = entryAt(entryCursor);
				entryCursor++;
				currentDocument = sentenceAt(entry.entry.getIndex()).getDocument();
			} else {
				// Search for the next document other than the current one

				for(;;) {
					if(entryCursor>=entryCount()) {
						return false;
					}

					EntryInfo entry = entryAt(entryCursor);
					entryCursor++;

					ProsodicDocumentData document = sentenceAt(entry.entry.getIndex()).getDocument();

					if(document!=currentDocument) {
						// New document -> use it
						currentDocument = document;
						break;
					}
				}
			}

			buffer.set(currentDocument);

			return true;
		}

	}

	private interface GroupSelector {
		Object getGroup(SearchResult searchResult, int globalIndex, ResultEntry entry, Options env, int...indices);
	}

	private static class GroupLabelGroupSelector implements GroupSelector {

		/**
		 * @see de.ims.icarus.plugins.prosody.search.ProsodySearchResultExportHandler.GroupSelector#getGroup(de.ims.icarus.search_tools.result.SearchResult, int, de.ims.icarus.search_tools.result.ResultEntry, de.ims.icarus.util.Options, int[])
		 */
		@Override
		public Object getGroup(SearchResult searchResult, int globalIndex,
				ResultEntry entry, Options env, int... indices) {

			checkResultForGroups(searchResult, indices);

			return createGroupsLabel(searchResult, indices);
		}

		@Override
		public String toString() {
			return "Group-Label Array"; //$NON-NLS-1$
		}

	}

	private static class GroupIdGroupSelector implements GroupSelector {

		/**
		 * @see de.ims.icarus.plugins.prosody.search.ProsodySearchResultExportHandler.GroupSelector#getGroup(de.ims.icarus.search_tools.result.SearchResult, int, de.ims.icarus.search_tools.result.ResultEntry, de.ims.icarus.util.Options, int[])
		 */
		@Override
		public Object getGroup(SearchResult searchResult, int globalIndex,
				ResultEntry entry, Options env, int... indices) {

			checkResultForGroups(searchResult, indices);

			return Arrays.toString(indices);
		}

		@Override
		public String toString() {
			return "Group-Id Array"; //$NON-NLS-1$
		}

	}

	private static class PatternGroupSelector implements GroupSelector {

		private final PatternDataProxy patternProxy = new PatternDataProxy();

		private final TextSource textSource;

		public PatternGroupSelector(String pattern) throws ParseException {
			textSource = ProsodyPatternContext.createTextSource(pattern, null);
		}

		public PatternGroupSelector(TextSource textSource) {
			if (textSource == null)
				throw new NullPointerException("Invalid textSource"); //$NON-NLS-1$

			this.textSource = textSource;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.search.ProsodySearchResultExportHandler.GroupSelector#getGroup(de.ims.icarus.search_tools.result.SearchResult, int, de.ims.icarus.search_tools.result.ResultEntry, int[])
		 */
		@Override
		public Object getGroup(SearchResult searchResult, int globalIndex,
				ResultEntry entry, Options env, int... indices) {

			AnnotatedProsodicSentenceData sentence = (AnnotatedProsodicSentenceData) searchResult.getAnnotatedEntry(entry);
			annotationManager.setAnnotation(sentence.getAnnotation());

			ProsodyHighlighting highlighting = ProsodyHighlighting.getInstance();

			if(annotationManager.hasAnnotation()) {
				boolean highlightFound = false;

				for(int i=0; i<sentence.length() && !highlightFound; i++) {
					long highlight = annotationManager.getHighlight(i);

					if(highlighting.isHighlighted(highlight)) {
						int sylCount = sentence.getSyllableCount(i);

						for(int j=0; j<sylCount && !highlightFound; j++) {

							long sylHighlight = sentence.getAnnotation().getHighlight(i, j);
							if(highlighting.isHighlighted(sylHighlight)) {

								// Full hit
								patternProxy.set(sentence, i, j);
								highlightFound = true;
							}
						}

						// Ensure the word is marked
						if(!highlightFound) {
							patternProxy.set(sentence, i);
							highlightFound = true;
						}
					}
				}

				// Ensure sentence is considered
				if(!highlightFound) {
					patternProxy.set(sentence);
				}
			} else {
				patternProxy.set(sentence);
			}

			return textSource.getText(patternProxy, env);
		}

		@Override
		public String toString() {
			return textSource.getExternalForm();
		}
	}
}
