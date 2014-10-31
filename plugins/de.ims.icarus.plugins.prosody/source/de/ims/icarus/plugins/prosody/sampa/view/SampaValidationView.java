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
package de.ims.icarus.plugins.prosody.sampa.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.TransferHandler;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.list.FileListTransferHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SampaValidationView extends View {

	private JList<File> wordFilesList;
	private JList<File> syllableFilesList;

	private CallbackHandler callbackHandler;
	private Handler handler;

	public SampaValidationView() {
		// no-op
	}

	@Override
	public void init(JComponent container) {

		// Load actions
		if (!defaultLoadActions(SampaValidationView.class,
				"sampa-validation-view-actions.xml")) { //$NON-NLS-1$
			return;
		}

		// Init ui
		container.setLayout(new BorderLayout());

		FileListTransferHandler transferHandler = new FileListTransferHandler();
		FileListCellRenderer renderer = new FileListCellRenderer();

		wordFilesList = createFilesList(transferHandler, renderer);
		syllableFilesList = createFilesList(transferHandler, renderer);

		JScrollPane wordScrollPane = new JScrollPane(wordFilesList);
		UIUtil.defaultSetUnitIncrement(wordScrollPane);
		wordScrollPane.setBorder(UIUtil.defaultContentBorder);

		JScrollPane syllableScrollPane = new JScrollPane(syllableFilesList);
		UIUtil.defaultSetUnitIncrement(syllableScrollPane);
		syllableScrollPane.setBorder(UIUtil.defaultContentBorder);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, wordScrollPane, syllableScrollPane);
		UIUtil.defaultHideSplitPaneDecoration(splitPane);
		splitPane.setBorder(UIUtil.topLineBorder);

		container.add(splitPane, BorderLayout.CENTER);

		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"", null);
		container.add(toolBar, BorderLayout.NORTH);

		registerActionCallbacks();

		refreshActions();
	}

	private JList<File> createFilesList(TransferHandler transferHandler, ListCellRenderer<?> renderer) {

		JList<File> list = new JList<>();
		list.setDragEnabled(true);
		list.setBorder(null);
		list.setCellRenderer((ListCellRenderer<? super File>) renderer);
		list.setTransferHandler(transferHandler);

		return list;
	}

	private Handler getHandler() {
		if (handler == null) {
			handler = new Handler();
		}
		return handler;
	}

	private void registerActionCallbacks() {
		if (callbackHandler == null) {
			callbackHandler = new CallbackHandler();
		}

		ActionManager actionManager = getDefaultActionManager();

		// TODO register callabck methods
	}

	private void refreshActions() {
		// TODO refresh actions
	}

	private class Handler {

		// TODO add handler methods

	}

	public class CallbackHandler {

		private CallbackHandler() {
			// no-op
		}

		public void xxx(ActionEvent e) {
			// TODO modify and add callback methods

			try {

			} catch (Exception ex) {
				LoggerFactory.error(this, "xxx", ex);
				UIUtil.beep();

				showError(ex);
			}
		}
	}

	private static class FileListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			File file = (File) value;

			value = file.getName();

			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			setToolTipText(UIUtil.toSwingTooltip(file.getAbsolutePath()));

			return this;
		}

	}
}
