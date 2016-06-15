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
package de.ims.icarus.plugins.search_tools.view.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.UndoManager;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.search_tools.view.graph.ConstraintGraphPresenter;
import de.ims.icarus.search_tools.SearchDescriptor;
import de.ims.icarus.search_tools.SearchGraph;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.standard.DefaultSearchGraph;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultQueryEditor extends QueryEditor {

	protected ConstraintGraphPresenter graphPresenter;
	protected JTextPane queryPane;
	protected JSplitPane splitPane;

	protected ActionManager actionManager;
	protected CallbackHandler callbackHandler;

	protected JPanel contentPanel;

	public DefaultQueryEditor() {
		// no-op
	}

	protected ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = ActionManager.globalManager().derive();
		}

		return actionManager;
	}

	@Override
	public boolean supports(ContentType contentType) {
		return ContentTypeRegistry.isCompatible(
				LanguageUtils.getSentenceDataContentType(), contentType);
	}


	protected void buildContentPanel() {

		contentPanel = new JPanel();

		// Load actions
		String path = "default-query-editor-actions.xml"; //$NON-NLS-1$
		URL actionLocation = DefaultQueryEditor.class.getResource(path);
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: "+path); //$NON-NLS-1$

		try {
			getActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, "Failed to load actions from file: "+actionLocation, e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(contentPanel, e);
			return;
		}

		graphPresenter = new ConstraintGraphPresenter();

		queryPane = new JTextPane(){

			private static final long serialVersionUID = -2519157276726844336L;

			@Override
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}

		};
		queryPane.setFont(UIManager.getFont("Label.font")); //$NON-NLS-1$
		UIUtil.disableHtml(queryPane);
		UIUtil.createUndoSupport(queryPane, 40);
		UIUtil.addPopupMenu(queryPane, UIUtil.createDefaultTextMenu(queryPane, true));
		queryPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		queryPane.setPreferredSize(new Dimension(400, 55));

		ActionMap actionMap = queryPane.getActionMap();
		ActionManager actionManager = getActionManager();
		actionManager.addAction("plugins.searchTools.defaultQueryEditor.undoAction", actionMap.get("undo")); //$NON-NLS-1$ //$NON-NLS-2$
		actionManager.addAction("plugins.searchTools.defaultQueryEditor.redoAction", actionMap.get("redo")); //$NON-NLS-1$ //$NON-NLS-2$
		actionManager.addAction("plugins.searchTools.defaultQueryEditor.clearAction", actionMap.get("clear")); //$NON-NLS-1$ //$NON-NLS-2$
		actionManager.addAction("plugins.searchTools.defaultQueryEditor.selectAllAction", actionMap.get(DefaultEditorKit.selectAllAction)); //$NON-NLS-1$
		actionManager.addAction("plugins.searchTools.defaultQueryEditor.cutAction", actionMap.get(DefaultEditorKit.cutAction)); //$NON-NLS-1$
		actionManager.addAction("plugins.searchTools.defaultQueryEditor.copyAction", actionMap.get(DefaultEditorKit.copyAction)); //$NON-NLS-1$
		actionManager.addAction("plugins.searchTools.defaultQueryEditor.pasteAction", actionMap.get(DefaultEditorKit.pasteAction)); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane(queryPane);
		scrollPane.setBorder(null);
		scrollPane.setPreferredSize(new Dimension(200, 50));

		JPanel lowerPanel = new JPanel(new BorderLayout());
		lowerPanel.add(createToolBar().buildToolBar(), BorderLayout.NORTH);
		lowerPanel.add(scrollPane, BorderLayout.CENTER);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
				graphPresenter.getPresentingComponent(), lowerPanel);
		splitPane.setDividerSize(5);
		splitPane.setResizeWeight(1);
		splitPane.setBorder(null);

		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(splitPane, BorderLayout.CENTER);

		registerActionCallbacks();

		refreshActions();
	}

	protected void refreshActions() {
		// no-op
	}

	protected void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = createCallbackHandler();
		}

		ActionManager actionManager = getActionManager();

		actionManager.addHandler("plugins.searchTools.defaultQueryEditor.synchronizeGraphAction",  //$NON-NLS-1$
				callbackHandler, "synchronizeGraph"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.defaultQueryEditor.synchronizeQueryAction",  //$NON-NLS-1$
				callbackHandler, "synchronizeQuery"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.defaultQueryEditor.commitAction",  //$NON-NLS-1$
				callbackHandler, "commit"); //$NON-NLS-1$
	}

	protected ActionComponentBuilder createToolBar() {
		ActionComponentBuilder builder = new ActionComponentBuilder(getActionManager());
		builder.setActionListId("plugins.searchTools.defaultQueryEditor.toolBarList"); //$NON-NLS-1$

		return builder;
	}

	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler();
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#applyEdit()
	 */
	@Override
	public void applyEdit() {
		try {
			synchronizeQuery();
		} catch (Exception e) {
			throw new IllegalStateException("Synchronization of query failed", e); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#getEditorComponent()
	 */
	@Override
	public Component getEditorComponent() {
		if(contentPanel==null) {
			buildContentPanel();
		}
		return contentPanel;
	}

	/**
	 *
	 * @see de.ims.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
	 */
	@Override
	public void setEditingItem(SearchDescriptor searchDescriptor) {
		if(contentPanel==null) {
			buildContentPanel();
		}

		if(searchDescriptor!=null && searchDescriptor.equals(this.searchDescriptor)) {
			return;
		}

		this.searchDescriptor = searchDescriptor;

		SearchQuery searchQuery = searchDescriptor==null ? null : searchDescriptor.getQuery();

		if(searchQuery==null) {
			graphPresenter.clear();
			queryPane.setText(null);
			return;
		}

		graphPresenter.setConstraintContext(searchQuery.getConstraintContext());
		SearchGraph graph = searchQuery.getSearchGraph();
		if(graph!=null) {
			try {
				graphPresenter.present(searchQuery.getSearchGraph(), null);
			} catch (UnsupportedPresentationDataException e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to forward presentation of search graph for query:\n"+searchQuery.getQueryString(), e); //$NON-NLS-1$
				graph = null;
			}
		}

		if(graph==null){
			graphPresenter.clear();
		}

		queryPane.setText(searchQuery.getQueryString());

		UndoManager undoManager = UIUtil.getUndoManager(queryPane);
		if(undoManager!=null) {
			undoManager.discardAllEdits();
		}
	}

	public void synchronizeQuery() throws Exception {
		if(searchDescriptor==null) {
			return;
		}

		SearchQuery searchQuery = searchDescriptor.getQuery();
		if(searchQuery==null) {
			return;
		}

		SearchGraph searchGraph = graphPresenter.snapshot();
		if(searchGraph==null) {
			searchGraph = new DefaultSearchGraph();
		}

		searchQuery.setSearchGraph(searchGraph);

		queryPane.setText(searchQuery.getQueryString());
	}

	public void synchronizeGraph() throws Exception {
		if(searchDescriptor==null) {
			return;
		}

		SearchQuery searchQuery = searchDescriptor.getQuery();
		if(searchQuery==null) {
			return;
		}

		String query = queryPane.getText();
		if(query==null || query.isEmpty()) {
			return;
		}

		searchQuery.parseQueryString(query);

		graphPresenter.present(searchQuery.getSearchGraph(), null);
	}

	protected void showError(Throwable t) {
		DialogFactory.getGlobalFactory().showError(null,
				"plugins.core.icarusCorePlugin.errorDialog.title",  //$NON-NLS-1$
				"plugins.core.icarusCorePlugin.errorDialog.message",  //$NON-NLS-1$
				t.getMessage());
	}

	public class CallbackHandler {
		protected CallbackHandler() {
			// no-op
		}

		public void commit(ActionEvent e) {
			try {
				DefaultQueryEditor.this.synchronizeQuery();
				DefaultQueryEditor.this.commit();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to commit query", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void synchronizeQuery(ActionEvent e) {
			try {
				DefaultQueryEditor.this.synchronizeQuery();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to synchronize query", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void synchronizeGraph(ActionEvent e) {
			try {
				DefaultQueryEditor.this.synchronizeGraph();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to synchronize graph", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}
	}
}
