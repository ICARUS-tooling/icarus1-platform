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

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.ims.icarus.Core;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.SearchResultExportHandler;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.helper.DirectoryFileFilter;

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
		splitPane.setDividerLocation(170);
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

	/**
	 * @see de.ims.icarus.search_tools.SearchResultExportHandler#exportResult(de.ims.icarus.search_tools.result.SearchResult)
	 */
	@Override
	public void exportResult(SearchResult searchResult) {
		initGUI();

		if(!DialogFactory.getGlobalFactory().showGenericDialog(null,
				ResourceManager.getInstance().get("plugins.prosody.prosodySearchResultExportHandler.dialogs.title"), //$NON-NLS-1$
				null, contentPanel, true, "export", "cancel")) { //$NON-NLS-1$ //$NON-NLS-2$
			// Cancelled by user
			return;
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
			} else if(e.getSource()==bInsertPattern) {
				//TODO
			} else {

			}
		}

	}
}
