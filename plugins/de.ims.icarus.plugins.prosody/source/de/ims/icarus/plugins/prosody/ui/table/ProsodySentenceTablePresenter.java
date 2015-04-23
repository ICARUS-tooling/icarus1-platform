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
package de.ims.icarus.plugins.prosody.ui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.treetable.TreeTable;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodySentenceTablePresenter implements AWTPresenter.TableBasedPresenter {

	private TreeTable treeTable;
	private ProsodyTreeTableModel model = new ProsodyTreeTableModel();
	private Options options;
	private JPanel contentPanel;

	private CallbackHandler callbackHandler;
	private Handler handler;

	protected ActionManager actionManager;

	protected static final String configPath = "plugins.prosody.appearance.table"; //$NON-NLS-1$

	private static ActionManager sharedActionManager;

	protected static synchronized final ActionManager getSharedActionManager() {
		if(sharedActionManager==null) {
			sharedActionManager = ActionManager.globalManager().derive();

			URL actionLocation = ProsodySentenceTablePresenter.class.getResource("prosody-sentence-table-presenter-actions.xml"); //$NON-NLS-1$
			if(actionLocation==null)
				throw new CorruptedStateException("Missing resources: prosody-sentence-table-presenter-actions.xml"); //$NON-NLS-1$

			try {
				sharedActionManager.loadActions(actionLocation);
			} catch (IOException e) {
				LoggerFactory.log(ProsodySentenceTablePresenter.class, Level.SEVERE,
						"Failed to load actions from file", e); //$NON-NLS-1$
			}
		}

		return sharedActionManager;
	}

	protected ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = getSharedActionManager().derive();

			registerActionCallbacks();
		}

		return actionManager;
	}

	protected void registerActionCallbacks() {
		ActionManager actionManager = getActionManager();

		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}

		actionManager.addHandler("plugins.prosody.prosodySentenceTablePresenter.refreshAction", //$NON-NLS-1$
				callbackHandler, "refresh"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.prosodySentenceTablePresenter.expandAllAction", //$NON-NLS-1$
				callbackHandler, "expandAll"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.prosodySentenceTablePresenter.collapseAllAction", //$NON-NLS-1$
				callbackHandler, "collapseAll"); //$NON-NLS-1$
	}

	protected void refreshActions() {

		//TODO
	}

	/**
	 * @see de.ims.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPanel==null) {
			contentPanel = createContentPanel();

			refresh();
		}
		return contentPanel;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible(ProsodyUtils.getProsodySentenceContentType(), type);
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if (data == null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
		if(!PresenterUtils.presenterSupports(this, data))
			throw new UnsupportedPresentationDataException("Data not supported: "+data.getClass()); //$NON-NLS-1$

		if(options==null) {
			options = Options.emptyOptions;
		}

		this.options = options.clone();
		setData((ProsodicSentenceData) data);

		if(contentPanel==null) {
			return;
		}

		//TODO reset filter

		refresh();
	}

	private void setData(ProsodicSentenceData sentence) {
		model.rebuild(sentence);
	}

	public void refresh() {
		refreshActions();
	}

	protected ActionComponentBuilder createToolBar() {
		ActionComponentBuilder builder = new ActionComponentBuilder(getActionManager());
		builder.setActionListId("plugins.prosody.prosodySentenceTablePresenter.toolBarList"); //$NON-NLS-1$

		return builder;
	}

	protected JPanel createContentPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JToolBar toolBar = createToolBar().buildToolBar();
		if(toolBar!=null) {
			panel.add(toolBar, BorderLayout.NORTH);
		}

		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		for(int i=0; i<model.getColumnCount(); i++) {
			TableColumn tableColumn = new TableColumn(i);
			tableColumn.setHeaderValue(model.getColumn(i).getName());
			tableColumn.setIdentifier(model.getColumn(i));

			columnModel.addColumn(tableColumn);
		}

		treeTable = new TreeTable(model, columnModel);
		UIUtil.enableRighClickTableSelection(treeTable);
		treeTable.setFillsViewportHeight(true);
		treeTable.addMouseListener(getHandler());
		treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		treeTable.getTreeTableCellRenderer().setRootVisible(false);
		treeTable.getTreeTableCellRenderer().setShowsRootHandles(true);
		treeTable.getTreeTableCellRenderer().setCellRenderer(new ProsodyTreeCellRenderer());
		treeTable.getTableHeader().setReorderingAllowed(false);
//		treeTable.getTableHeader().setDefaultRenderer(new TooltipTableCellRenderer());

		JScrollPane scrollPane = new JScrollPane(treeTable);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		scrollPane.setBorder(UIUtil.topLineBorder);

		panel.add(scrollPane, BorderLayout.CENTER);

		registerActionCallbacks();

		refreshActions();

		return panel;
	}

	protected Handler getHandler() {
		if(handler==null) {
			handler = new Handler();
		}
		return handler;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		model.clear();
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		model.clear();

		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return !model.isEmpty();
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return model.getSentence();
	}

	private class Handler extends MouseAdapter {

	}

	public class CallbackHandler {
		protected CallbackHandler() {
			// no-op
		}

		public void refresh(ActionEvent e) {
			try {
				ProsodySentenceTablePresenter.this.refresh();
			} catch(Exception ex) {
				LoggerFactory.error(this, "Failed to refresh table view", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}

		public void expandAll(ActionEvent e) {
			try {
				UIUtil.expandAll(treeTable.getTreeTableCellRenderer(), true);
			} catch(Exception ex) {
				LoggerFactory.error(this, "Failed to expand tree", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}

		public void collapseAll(ActionEvent e) {
			try {
				UIUtil.expandAll(treeTable.getTreeTableCellRenderer(), false);
			} catch(Exception ex) {
				LoggerFactory.error(this, "Failed to collapse tree", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
	}
}
