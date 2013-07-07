/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.search_tools.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.UndoManager;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.search_tools.SearchToolsConstants;
import de.ims.icarus.plugins.search_tools.view.graph.ConstraintGraphPresenter;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.SearchDescriptor;
import de.ims.icarus.search_tools.SearchGraph;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class QueryEditorView extends View {
	
	protected ConstraintGraphPresenter graphPresenter;
	protected JTextPane queryPane;
	
	protected JLabel infoLabel;
	protected JSplitPane splitPane;
	
	protected Handler handler;
	protected CallbackHandler callbackHandler;
	
	protected SearchDescriptor searchDescriptor;
	
	public QueryEditorView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		URL actionLocation = QueryEditorView.class.getResource("query-editor-view-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: query-editor-view-actions.xml"); //$NON-NLS-1$
		
		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}
		
		handler = createHandler();

		infoLabel = new JLabel();
		infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		infoLabel.setVerticalAlignment(SwingConstants.TOP);
		ResourceManager.getInstance().getGlobalDomain().prepareComponent(
				infoLabel, "plugins.searchTools.queryEditorView.notAvailable", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);
		
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
		
		ActionManager actionManager = getDefaultActionManager();
		ActionMap actionMap = queryPane.getActionMap();
		actionManager.addAction("plugins.searchTools.queryEditorView.undoAction", actionMap.get("undo")); //$NON-NLS-1$ //$NON-NLS-2$
		actionManager.addAction("plugins.searchTools.queryEditorView.redoAction", actionMap.get("redo")); //$NON-NLS-1$ //$NON-NLS-2$
		actionManager.addAction("plugins.searchTools.queryEditorView.clearAction", actionMap.get("clear")); //$NON-NLS-1$ //$NON-NLS-2$
		actionManager.addAction("plugins.searchTools.queryEditorView.selectAllAction", actionMap.get(DefaultEditorKit.selectAllAction)); //$NON-NLS-1$
		actionManager.addAction("plugins.searchTools.queryEditorView.cutAction", actionMap.get(DefaultEditorKit.cutAction)); //$NON-NLS-1$
		actionManager.addAction("plugins.searchTools.queryEditorView.copyAction", actionMap.get(DefaultEditorKit.copyAction)); //$NON-NLS-1$
		actionManager.addAction("plugins.searchTools.queryEditorView.pasteAction", actionMap.get(DefaultEditorKit.pasteAction)); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane(queryPane);
		scrollPane.setBorder(null);
		scrollPane.setPreferredSize(new Dimension(200, 50));
		
		JPanel lowerPanel = new JPanel(new BorderLayout());
		lowerPanel.add(createToolBar(), BorderLayout.NORTH);
		lowerPanel.add(scrollPane, BorderLayout.CENTER);
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, 
				graphPresenter.getPresentingComponent(), lowerPanel);
		splitPane.setDividerSize(5);
		splitPane.setResizeWeight(1);
		splitPane.setBorder(null);
		splitPane.setVisible(false);
		
		container.setLayout(new BorderLayout());
		container.add(infoLabel, BorderLayout.NORTH);
		container.add(splitPane, BorderLayout.CENTER);
		
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
		
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.addHandler("plugins.searchTools.queryEditorView.synchronizeGraphAction",  //$NON-NLS-1$
				callbackHandler, "synchronizeGraph"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.queryEditorView.synchronizeQueryAction",  //$NON-NLS-1$
				callbackHandler, "synchronizeQuery"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.queryEditorView.commitAction",  //$NON-NLS-1$
				callbackHandler, "commit"); //$NON-NLS-1$
	}
	
	protected JToolBar createToolBar() {
		return getDefaultActionManager().createToolBar(
				"plugins.searchTools.queryEditorView.toolBarList", null); //$NON-NLS-1$
	}
	
	protected Handler createHandler() {
		return new Handler();
	}
	
	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler();
	}
	
	public void setSearchDescriptor(SearchDescriptor searchDescriptor) {
		if(searchDescriptor!=null && searchDescriptor.equals(this.searchDescriptor)) {
			return;
		}
		
		this.searchDescriptor = searchDescriptor;
		
		SearchQuery searchQuery = searchDescriptor==null ? null : searchDescriptor.getQuery();
		
		if(searchQuery==null) {
			graphPresenter.clear();
			queryPane.setText(null);
			
			splitPane.setVisible(false);
			infoLabel.setVisible(true);
			return;
		}

		graphPresenter.setConstraintContext(searchDescriptor.getSearchFactory().getConstraintContext());
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

		splitPane.setVisible(true);
		infoLabel.setVisible(false);

		UndoManager undoManager = UIUtil.getUndoManager(queryPane);
		if(undoManager!=null) {
			undoManager.discardAllEdits();
		}
	}
	
	public SearchDescriptor getSearchDescriptor() {
		return searchDescriptor;
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
			return;
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
	
	public void commitQuery() throws Exception {
		synchronizeQuery();
		
		Message message = new Message(this, Commands.SELECT, 
				getSearchDescriptor(), null);
		
		sendRequest(SearchToolsConstants.SEARCH_MANAGER_VIEW_ID, message);
	}

	@Override
	public void reset() {
		setSearchDescriptor(null);
	}

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.PRESENT.equals(message.getCommand())
				|| Commands.DISPLAY.equals(message.getCommand())) {
			if(message.getData() instanceof SearchDescriptor) {
				selectViewTab();
				
				setSearchDescriptor((SearchDescriptor)message.getData());
				return message.successResult(this, null);
			} else {
				return message.unsupportedDataResult(this);
			}
		} else if(Commands.CLEAR.equals(message.getCommand())) {
			reset();
			return message.successResult(this, null);
		} else if(Commands.COMMIT.equals(message.getCommand())) {
			commitQuery();
			return message.successResult(this, searchDescriptor);
		} else {
			return message.unknownRequestResult(this);
		}
	}

	protected class Handler {
		protected Handler() {
			// no-op
		}
	}
	
	public class CallbackHandler {
		protected CallbackHandler() {
			// no-op
		}
		
		public void commit(ActionEvent e) {
			try {
				QueryEditorView.this.commitQuery();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to commit query", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void synchronizeQuery(ActionEvent e) {
			try {
				QueryEditorView.this.synchronizeQuery();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to synchronize query", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void synchronizeGraph(ActionEvent e) {
			try {
				QueryEditorView.this.synchronizeGraph();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to synchronize graph", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
	}
}
