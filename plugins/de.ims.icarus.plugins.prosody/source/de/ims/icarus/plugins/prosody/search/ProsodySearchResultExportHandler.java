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
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.ims.icarus.Core;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.prosody.annotation.AnnotatedProsodicSentenceData;
import de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotationManager;
import de.ims.icarus.plugins.prosody.annotation.ProsodyHighlighting;
import de.ims.icarus.plugins.prosody.pattern.ProsodyData;
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
import de.ims.icarus.util.Options;
import de.ims.icarus.util.ToolException;
import de.ims.icarus.util.strings.pattern.TextSource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodySearchResultExportHandler implements SearchResultExportHandler {

	// Location UI
	private JRadioButton rbSingleFile, rbFolder;
	private JButton bSingleFile, bFolder;
	private JTextField tfSingleFile, tfFolder, tfFileNamePattern;
	private JComboBox<String> cbGroupBy;

	// Format UI
	private JTextArea taHeader, taContent;
	private JButton bInsertPattern;
	private JPopupMenu patterMenu;
	private JComboBox<ProsodyLevel> cbLevel;

	private JPanel contentPanel;
	private Handler handler;

	private void initGUI() {
		handler = new Handler();

		ResourceManager rm = ResourceManager.getInstance();
		IconRegistry ir = IconRegistry.getGlobalRegistry();

		// Location Panel
		FormLayout locationLayout = new FormLayout(
				"20dlu, pref, 3dlu, fill:pref:grow, 2dlu, pref", //$NON-NLS-1$
				"pref, 6dlu, pref, 2dlu, pref, 2dlu, pref"); //$NON-NLS-1$
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
		bFolder = new JButton(ir.getIcon("fldr_obj.gif")); //$NON-NLS-1$
		bFolder.setToolTipText(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.tooltips.selectFolder")); //$NON-NLS-1$
		bFolder.addActionListener(handler);

		locationPanel.add(rbFolder, CC.rcw(3, 1, 2));
		locationPanel.add(tfFolder, CC.rc(3, 4));
		locationPanel.add(bFolder, CC.rc(3, 6));

		tfFileNamePattern = new JTextField();
		JLabel lFileNamePattern = new JLabel(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.fileNamePattern")); //$NON-NLS-1$

		locationPanel.add(lFileNamePattern, CC.rc(5, 2));
		locationPanel.add(tfFileNamePattern, CC.rc(5, 4));

		DefaultComboBoxModel<String> groupByModel = new DefaultComboBoxModel<>();
		//TODO fill model
		cbGroupBy = new JComboBox<>(groupByModel);
		cbGroupBy.setEditable(true);
		JLabel lGroupBy = new JLabel(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.groupBy")); //$NON-NLS-1$

		locationPanel.add(lGroupBy, CC.rc(7, 2));
		locationPanel.add(cbGroupBy, CC.rc(7, 4));

		ButtonGroup typeGroup = new ButtonGroup();
		typeGroup.add(rbFolder);
		typeGroup.add(rbSingleFile);

		// Initial selection
		rbSingleFile.setSelected(true);
		switchLocationType(true);


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
		JScrollPane spHeader = new JScrollPane(taHeader);
		JToolBar tbHeader = ActionManager.globalManager().createEmptyToolBar();
		tbHeader.add(lHeader);
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
		bInsertPattern = new JButton(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.labels.insertPattern")); //$NON-NLS-1$
		bInsertPattern.setToolTipText(
				rm.get("plugins.prosody.prosodySearchResultExportHandler.tooltips.insertPattern")); //$NON-NLS-1$
		bInsertPattern.addActionListener(handler);
		JScrollPane spContent = new JScrollPane(taContent);
		JToolBar tbContent = ActionManager.globalManager().createEmptyToolBar();
		tbContent.add(lContent);
		tbContent.add(Box.createGlue());
		tbContent.add(bInsertPattern);
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
	}

	private void switchLocationType(boolean isSingleFile) {
		tfSingleFile.setEditable(isSingleFile);
		bSingleFile.setEnabled(isSingleFile);


		tfFolder.setEnabled(!isSingleFile);
		bFolder.setEnabled(!isSingleFile);
		tfFileNamePattern.setEnabled(!isSingleFile);
		cbGroupBy.setEnabled(!isSingleFile);
	}

	//FIXME add loca for left out keys!!!
	private String[] getEnvironmentProperties() {
		return new String[]{
			CURRENT_FILE_KEY,
			CURRENT_TIME_KEY,
			CURRENT_DATE_KEY,
			TOTAL_MATCH_COUNT_KEY,
			TOTAL_HIT_COUNT_KEY,
			GROUP_COUNT_KEY,
			QUERY_KEY,

			CURRENT_HIT_COUNT_KEY,
			CURRENT_GROUP_KEY,
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

	private void showPatternMenu() {
		if(patterMenu==null) {
			patterMenu = new JPopupMenu();

			for(ProsodyLevel level : ProsodyLevel.values()) {
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

						menuItem.addActionListener(handler);
					}
				}

				menu.setEnabled(properties!=null && properties.length>0);

				patterMenu.add(menu);
			}
		}

		patterMenu.show(bInsertPattern, bInsertPattern.getWidth(), 0);
	}

	// Global Environment
	public static final String CURRENT_FILE_KEY = "current_file"; //$NON-NLS-1$
	public static final String CURRENT_TIME_KEY = "current_time"; //$NON-NLS-1$
	public static final String CURRENT_DATE_KEY = "current_date"; //$NON-NLS-1$
	public static final String TOTAL_MATCH_COUNT_KEY = "total_matches"; //$NON-NLS-1$
	public static final String TOTAL_HIT_COUNT_KEY = "total_hits"; //$NON-NLS-1$
	public static final String GROUP_COUNT_KEY = "group_count"; //$NON-NLS-1$
	public static final String QUERY_KEY = "query"; //$NON-NLS-1$

	// Grouping Environment
	public static final String CURRENT_HIT_COUNT_KEY = "hit_count"; //$NON-NLS-1$
	public static final String CURRENT_GROUP_KEY = "current_group"; //$NON-NLS-1$

	/**
	 * @see de.ims.icarus.search_tools.SearchResultExportHandler#exportResult(de.ims.icarus.search_tools.result.SearchResult)
	 */
	@Override
	public void exportResult(SearchResult searchResult) throws ToolException {
		initGUI();

		if(!DialogFactory.getGlobalFactory().showGenericDialog(null,
				ResourceManager.getInstance().get("plugins.prosody.prosodySearchResultExportHandler.dialogs.title"), //$NON-NLS-1$
				null, contentPanel, true, "export", "cancel")) { //$NON-NLS-1$ //$NON-NLS-2$
			// Cancelled by user
			return;
		}


		// Fetch pattern
		String headerPattern = taHeader.getText().trim();
		String contentPattern = taContent.getText().trim();

		if(contentPattern==null || contentPattern.isEmpty())
			throw new ToolException("No content pattern defined"); //$NON-NLS-1$

		ProsodyLevel outputLevel = (ProsodyLevel) cbLevel.getSelectedItem();

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
			if(headerPattern!=null) {
				headerTextSource = ProsodyPatternContext.createTextSource(outputLevel, headerPattern);
			}
		} catch (ParseException e) {
			throw new ToolException("Content pattern definition is invalid", e); //$NON-NLS-1$
		}

		List<Path> filesList = new ArrayList<>();
		List<Options> environmentsList = new ArrayList<>();
		List<Iterable<ResultEntry>> entryList = new ArrayList<>();

		Options globalEnvironment = new Options();
		globalEnvironment.put(CURRENT_DATE_KEY, new Date());
		globalEnvironment.put(CURRENT_TIME_KEY, System.currentTimeMillis());
		globalEnvironment.put(TOTAL_MATCH_COUNT_KEY, searchResult.getTotalMatchCount());
		globalEnvironment.put(TOTAL_HIT_COUNT_KEY, searchResult.getTotalHitCount());
		globalEnvironment.put(GROUP_COUNT_KEY, searchResult.getDimension());
		globalEnvironment.put(QUERY_KEY, searchResult.getSource().getQuery().getQueryString());

		if(rbSingleFile.isSelected()) {
			// One file for all content

			Path file = Paths.get(tfSingleFile.getText());
			filesList.add(file);

			List<ResultEntry> entries = new ArrayList<>(searchResult.getTotalMatchCount());
			for(int i=0; i<searchResult.getTotalMatchCount(); i++) {
				entries.add(searchResult.getRawEntry(i));
			}
			entryList.add(entries);

			Options environment = globalEnvironment.clone();
			environment.put(CURRENT_FILE_KEY, file.toString());

			environmentsList.add(environment);

		} else {
			// Group stuff
		}

		//TODO complete!
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
			} else if(e.getSource()==bInsertPattern) {
				showPatternMenu();
			} else {
				// Source is a JMenuItem with a "pattern" client property holding
				// the textual pattern to be inserted

				JMenuItem menuItem = (JMenuItem) e.getSource();
				String pattern = (String) menuItem.getClientProperty("pattern"); //$NON-NLS-1$

				int pos = taContent.getCaretPosition();
				try {
					taContent.getDocument().insertString(pos, pattern, null);
				} catch (BadLocationException ex) {
					LoggerFactory.error(this, "Failed to insert pattern at index "+pos, ex); //$NON-NLS-1$
				}
			}
		}

	}

	private static final ProsodicAnnotationManager annotationManager = new ProsodicAnnotationManager();

	private interface GroupSelector {
		Object getGroup(SearchResult searchResult, int globalIndex, ResultEntry entry, Options env, int...indices);
	}

	private static class PatternGroupSelector implements GroupSelector {

		private final ProsodyData patternProxy = new ProsodyData();

		private final TextSource textSource;

		public PatternGroupSelector(TextSource textSource) {
			if (textSource == null)
				throw new NullPointerException("Invalid textSource");

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
	}
}
