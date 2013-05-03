/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayout;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphStyle;
import net.ikarus_systems.icarus.plugins.jgraph.util.CellBuffer;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.view.AWTPresenter;
import net.ikarus_systems.icarus.ui.view.UnsupportedPresentationDataException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.swing.view.mxInteractiveCanvas;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class GraphPresenter extends mxGraphComponent implements AWTPresenter, GraphOwner {

	private static final long serialVersionUID = -3776528318060931576L;

	protected JToolBar mainToolBar;
	protected JToolBar secondaryToolBar;
	
	protected ActionManager actionManager;
	
	protected GraphLayout graphLayout;
	protected GraphStyle graphStyle;
	
	protected JComponent contentPanel;
	
	protected CallbackHandler callbackHandler;
	
	protected Handler handler;
	
	protected GraphUndoManager undoManager;
	
	protected boolean compressEnabled;
	protected boolean autoZoomEnabled;
	protected boolean wheelZoomEnabled;
	
	protected final boolean editable;
	
	protected GraphPresenter(boolean editable) {
		super(null);
		
		this.editable = editable;
		
		// TODO do it this way?
		setEnabled(editable);
	}
	
	protected ActionManager createActionManager() {
		ActionManager actionManager = ActionManager.globalManager().derive();
		
		// TODO load default actions
		
		return actionManager;
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public abstract ContentType getContentType();
	
	/**
	 * Per default we accept all data that is content-type compatible.
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible(getContentType(), type);
	}

	protected abstract void setData(Object data, Options options);
	
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		
		// Subclasses might want to use other checking methods?
		if(!ContentTypeRegistry.isCompatible(getContentType(), data))
			throw new UnsupportedPresentationDataException("Unsupported data: "+data.getClass()); //$NON-NLS-1$
		
		setData(data, options);
		
		syncToGraph();
		clearUndoHistory();
	}

	/**
	 * Hook for subclasses to check if the underlying data is mutable
	 * in addition to the graph presenter itself being editable.
	 */
	protected boolean canEdit() {
		return isEditable();
	}
	
	protected ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = createActionManager();
		}
		
		return actionManager;
	}
	
	protected void init() {
		setGraph(createGraph());
		
		installUtilities();
		
		installKeyboardActions();
		
		registerActionCallbacks();
		
		mainToolBar = createMainToolBar();
		secondaryToolBar = createSecondaryToolBar();
		
		if(mainToolBar!=null) {
			setCorner(ScrollPaneConstants.COLUMN_HEADER, mainToolBar);
		}
		
		if(secondaryToolBar!=null) {
			setCorner(ScrollPaneConstants.ROW_HEADER, secondaryToolBar);
		}
	}
	
	protected void buildContentPanel() {
		init();

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(this, BorderLayout.CENTER);
		
		contentPanel = panel;
	}
	
	protected void installUtilities() {
		if(handler==null) {
			handler = createHandler();
		}
		
		addMouseWheelListener(handler);
		
		GraphUndoManager undoManager = getUndoManager();
		if(undoManager!=null) {
			undoManager.addListener(mxEvent.UNDO, handler);
			undoManager.addListener(mxEvent.REDO, handler);
		}
	}
	
	protected void installKeyboardActions() {
		SwingUtilities.replaceUIInputMap(this, 
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, 
				(InputMap) UIManager.get("ScrollPane.ancestorInputMap")); //$NON-NLS-1$

		SwingUtilities.replaceUIInputMap(this, 
				JComponent.WHEN_FOCUSED, 
				createFocusedInputMap());
		
		SwingUtilities.replaceUIActionMap(this, createActionMap());
	}
	
	protected InputMap createFocusedInputMap() {
		InputMap inputMap = new InputMap();

		inputMap.put(KeyStroke.getKeyStroke("F2"), "edit"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("DELETE"), "delete"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("UP"), "selectParent"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("DOWN"), "selectChild"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "selectNext"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("LEFT"), "selectPrevious"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "enterGroup"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("PAGE_UP"), "exitGroup"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("HOME"), "home"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("ENTER"), "expand"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("BACK_SPACE"), "collapse"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("control A"), "selectAll"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("control D"), "selectNone"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("control X"), "cut"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("CUT"), "cut"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("control C"), "copy"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("COPY"), "copy"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("control V"), "paste"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("PASTE"), "paste"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("control G"), "group"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("control U"), "ungroup"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("control ADD"), "zoomIn"); //$NON-NLS-1$ //$NON-NLS-2$
		inputMap.put(KeyStroke.getKeyStroke("control SUBTRACT"), "zoomOut"); //$NON-NLS-1$ //$NON-NLS-2$
		
		return inputMap;
	}
	
	protected ActionMap createActionMap() {
		ActionMap map = (ActionMap) UIManager.get("ScrollPane.actionMap"); //$NON-NLS-1$

		map.put("edit", mxGraphActions.getEditAction()); //$NON-NLS-1$
		map.put("delete", mxGraphActions.getDeleteAction()); //$NON-NLS-1$
		map.put("home", mxGraphActions.getHomeAction()); //$NON-NLS-1$
		map.put("enterGroup", mxGraphActions.getEnterGroupAction()); //$NON-NLS-1$
		map.put("exitGroup", mxGraphActions.getExitGroupAction()); //$NON-NLS-1$
		map.put("collapse", mxGraphActions.getCollapseAction()); //$NON-NLS-1$
		map.put("expand", mxGraphActions.getExpandAction()); //$NON-NLS-1$
		map.put("toBack", mxGraphActions.getToBackAction()); //$NON-NLS-1$
		map.put("toFront", mxGraphActions.getToFrontAction()); //$NON-NLS-1$
		map.put("selectNone", mxGraphActions.getSelectNoneAction()); //$NON-NLS-1$
		map.put("selectAll", mxGraphActions.getSelectAllAction()); //$NON-NLS-1$
		map.put("selectNext", mxGraphActions.getSelectNextAction()); //$NON-NLS-1$
		map.put("selectPrevious", mxGraphActions.getSelectPreviousAction()); //$NON-NLS-1$
		map.put("selectParent", mxGraphActions.getSelectParentAction()); //$NON-NLS-1$
		map.put("selectChild", mxGraphActions.getSelectChildAction()); //$NON-NLS-1$
		map.put("cut", TransferHandler.getCutAction()); //$NON-NLS-1$
		map.put("copy", TransferHandler.getCopyAction()); //$NON-NLS-1$
		map.put("paste", TransferHandler.getPasteAction()); //$NON-NLS-1$
		map.put("group", mxGraphActions.getGroupAction()); //$NON-NLS-1$
		map.put("ungroup", mxGraphActions.getUngroupAction()); //$NON-NLS-1$
		map.put("zoomIn", mxGraphActions.getZoomInAction()); //$NON-NLS-1$
		map.put("zoomOut", mxGraphActions.getZoomOutAction()); //$NON-NLS-1$

		return map;
	}
	
	protected JToolBar createMainToolBar() {
		// TODO
		return actionManager.createToolBar("", null); //$NON-NLS-1$
	}
	
	protected JToolBar createSecondaryToolBar() {
		return null;
	}
	
	protected Handler createHandler() {
		return new Handler();
	}
	
	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler();
	}
	
	protected GraphUndoManager createUndoManager() {
		return new GraphUndoManager(40);
	}
	
	protected mxGraph createGraph() {
		mxGraph graph = new mxGraph();

		graph.setGridSize(15);
		
		graph.setMultigraph(false);
		graph.setAllowDanglingEdges(false);
		graph.setAllowLoops(false);
		graph.setCellsDisconnectable(false);
		graph.setCellsResizable(false);
		graph.setEdgeLabelsMovable(false);
		graph.setAutoSizeCells(true);
		graph.setHtmlLabels(false);
		graph.setGridEnabled(false);

		graph.getSelectionModel().setSingleSelection(false);

		return graph;
	}
	
	protected void registerActionCallbacks() {
		
	}
	
	protected void refreshActions() {
		
	}
	
	public boolean isCompressEnabled() {
		return compressEnabled;
	}

	public boolean isAutoZoomEnabled() {
		return autoZoomEnabled;
	}

	public void setCompressEnabled(boolean compressEnabled) {		
		boolean oldValue = this.compressEnabled;
		this.compressEnabled = compressEnabled;
		
		firePropertyChange("compressEnabled", oldValue, compressEnabled); //$NON-NLS-1$
	}

	public void setAutoZoomEnabled(boolean autoZoomEnabled) {
		boolean oldValue = this.autoZoomEnabled;
		this.autoZoomEnabled = autoZoomEnabled;
		
		firePropertyChange("autoZoomEnabled", oldValue, autoZoomEnabled); //$NON-NLS-1$
	}

	public boolean isWheelZoomEnabled() {
		return wheelZoomEnabled;
	}

	public void setWheelZoomEnabled(boolean wheelZoomEnabled) {
		boolean oldValue = this.wheelZoomEnabled;
		this.wheelZoomEnabled = wheelZoomEnabled;
		
		firePropertyChange("wheelZoomEnabled", oldValue, wheelZoomEnabled); //$NON-NLS-1$
	}

	public GraphUndoManager getUndoManager() {
		if(undoManager==null) {
			undoManager = createUndoManager();
		}
		return undoManager;
	}
	
	@Override
	public void setGraph(mxGraph value) {
		mxGraph oldValue = graph;

		GraphUndoManager undoManager = getUndoManager();

		// Uninstalls listeners for existing graph
		if (graph != null) {
			graph.removeListener(repaintHandler);
			graph.getModel().removeListener(updateHandler);
			graph.getView().removeListener(updateHandler);
			graph.removePropertyChangeListener(viewChangeHandler);
			graph.getView().removeListener(scaleHandler);
			
			if(undoManager!=null) {
				undoManager.uninstall(graph);
			}
		}

		graph = value;
		
		if (graph != null) {	
			// Updates the buffer if the model changes
			graph.addListener(mxEvent.REPAINT, repaintHandler);
	
			// Installs the update handler to sync the overlays and controls
			graph.getModel().addListener(mxEvent.CHANGE, updateHandler);
	
			// Repaint after the following events is handled via
			// mxGraph.repaint-events
			// The respective handlers are installed in mxGraph.setView
			mxGraphView view = graph.getView();
	
			view.addListener(mxEvent.SCALE, updateHandler);
			view.addListener(mxEvent.TRANSLATE, updateHandler);
			view.addListener(mxEvent.SCALE_AND_TRANSLATE, updateHandler);
			view.addListener(mxEvent.UP, updateHandler);
			view.addListener(mxEvent.DOWN, updateHandler);
	
			graph.addPropertyChangeListener(viewChangeHandler);
	
			// Resets the zoom policy if the scale changes
			graph.getView().addListener(mxEvent.SCALE, scaleHandler);
			graph.getView().addListener(mxEvent.SCALE_AND_TRANSLATE, scaleHandler);
	
			// Invoke the update handler once for initial state
			updateHandler.invoke(graph.getView(), null);
			
			if(undoManager!=null) {
				undoManager.install(graph);
			}
			if(!editable) {
				graph.setCellsEditable(false);
				graph.setCellsLocked(true);
				graph.setCellsMovable(false);
			}
		}

		firePropertyChange("graph", oldValue, graph); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		setData(null, null);
		clearUndoHistory();
		clearGraph();
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		clear();
		setGraph(null);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPanel==null) {
			buildContentPanel();
		}
		
		return contentPanel;
	}

	// Expansion of original class

	@Override
	public Dimension getPreferredSizeForPage() {
		return super.getPreferredSizeForPage();
	}

	@Override
	public Dimension getScaledPreferredSizeForGraph() {
		return super.getScaledPreferredSizeForGraph();
	}

	@Override
	public mxPoint getPageTranslate(double scale) {
		return super.getPageTranslate(scale);
	}
	
	public void setGraphLayout(GraphLayout layout) {
		if(layout==null)
			throw new IllegalArgumentException("Invalid layout"); //$NON-NLS-1$
		
		// Identical layout class -> abort
		if(graphLayout!=null && graphLayout.getClass().equals(layout.getClass())) {
			return;
		}
		
		Object oldValue = graphLayout;
		graphLayout = layout;
				
		firePropertyChange("graphLayout", oldValue, layout); //$NON-NLS-1$
	}
	
	public GraphStyle getGraphStyle() {
		return graphStyle;
	}
	
	public void setGraphStyle(GraphStyle style) {
		if(style==null)
			throw new IllegalArgumentException("Invalid style"); //$NON-NLS-1$
		
		if(graph==null) {
			return;
		}
		
		// Identical style class -> abort
		if(graphStyle!=null && graphStyle.getClass().equals(style.getClass())) {
			return;
		}
		
		Object oldValue = graphStyle;
		graphStyle = style;
		
		graph.setStylesheet(style.createStylesheet(this, null));
		
		firePropertyChange("graphStyle", oldValue, style); //$NON-NLS-1$
	}
	
	public GraphLayout getGraphLayout() {
		return graphLayout;
	}
	
	public void setCanvas(mxInteractiveCanvas canvas) {
		if(canvas==null)
			throw new IllegalArgumentException("Invalid canvas"); //$NON-NLS-1$

		if(this.canvas!=canvas) {
			mxInteractiveCanvas oldCanvas = this.canvas;
			
			canvas.setDrawLabels(oldCanvas.isDrawLabels());
			canvas.setGraphics(oldCanvas.getGraphics());
			canvas.setImageBasePath(oldCanvas.getImageBasePath());
			canvas.setScale(oldCanvas.getScale());
			Point tr = oldCanvas.getTranslate();
			canvas.setTranslate(tr.x, tr.y);
						
			this.canvas = canvas;
			
			firePropertyChange("canvas", oldCanvas, canvas); //$NON-NLS-1$
		}
	}
	
	public void clearUndoHistory() {
		if(undoManager!=null) {
			undoManager.clear();
		}
	}
	
	public Object[] getSelectionCells() {
		return graph.getSelectionCells();
	}
	
	public Object[] getSelectionEdges() {
		Object[] cells = graph.getSelectionCells();
		List<Object> edges = null;
		mxIGraphModel model = graph.getModel();
		for(Object cell : cells) {
			if(!model.isEdge(cell)) {
				continue;
			}
			
			if(edges==null) {
				edges = new ArrayList<>();
			}
			
			edges.add(cell);
		}
		
		return edges==null ? new Object[0] : edges.toArray();
	}
	
	public Object[] getSelectionVertices() {
		Object[] cells = graph.getSelectionCells();
		List<Object> vertices = null;
		mxIGraphModel model = graph.getModel();
		for(Object cell : cells) {
			if(!model.isVertex(cell)) {
				continue;
			}
			
			if(vertices==null) {
				vertices = new ArrayList<>();
			}
			
			vertices.add(cell);
		}
		
		return vertices==null ? new Object[0] : vertices.toArray();
	}
	
	protected abstract void syncToGraph();

	protected abstract void syncToData();
	
	/**
	 * Hook for subclasses to generate an array of cells that should
	 * be passed to the methods defined by {@link GraphLayout}. The
	 * array is expected to represent the exact order of cells as
	 * desired by the {@code GraphPresenter} implementation.
	 */
	protected Object[] getLayoutCells() {
		return graph.getChildVertices(graph.getDefaultParent());
	}
	
	public void refreshLayout() {
		if(graphLayout==null) {
			return;
		}
		
		Object[] cells = getLayoutCells();
		
		if(isCompressEnabled()) {
			Dimension size = getSize();
			mxRectangle bounds = new mxRectangle(0, 0, size.getWidth(), size.getHeight());
			
			graphLayout.compressGraph(this, cells, null, null, bounds);
		} else {
			graphLayout.layoutGraph(this, cells, null);
		}
	}
	
	public void moveCells(Object[] cells, double deltaX, double deltaY) {
		if (!isEditable()) {
			return;
		}

		if (cells == null) {
			cells = graph.getSelectionCells();
		}

		mxIGraphModel model = graph.getModel();

		model.beginUpdate();
		try {
			for (Object cell : cells) {
				mxGeometry geometry = model.getGeometry(cell);
				if (!model.isVertex(cell) || geometry.isRelative()) {
					continue;
				}
				
				geometry = (mxGeometry) geometry.clone();
				
				geometry.setX(geometry.getX()+deltaX);
				geometry.setY(geometry.getY()+deltaY);

				model.setGeometry(cell, geometry);
			}
		} finally {
			model.endUpdate();
		}
	}
	
	public void deleteCells(Object[] cells) {
		
	}
	
	public void cloneCells(Object[] cells) {
		// no-op
	}
	
	public void rebuildGraph() {
		syncToGraph();
		clearUndoHistory();
	}
	
	public void clearGraph() {
		mxIGraphModel model = graph.getModel();
		if(model instanceof mxGraphModel) {
			((mxGraphModel)model).clear();
		}
	}
	
	public CellBuffer exportCells(Object[] cells) {
		return null;
	}
	
	public void importCells(CellBuffer buffer) {
		Object[] cells = CellBuffer.buildCells(buffer);
		if(cells==null || cells.length==0) {
			return;
		}

		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			graph.addCells(cells);
			syncToData();
		} finally {
			model.endUpdate();
		}
	}
	
	/**
	 * Presents the user with a typical print dialog and prints
	 * the entire graph.
	 */
	public void printGraph() {
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH-mm"); //$NON-NLS-1$
		PrinterJob pj = PrinterJob.getPrinterJob();
		pj.setJobName("graph_" + df.format(new Date())); //$NON-NLS-1$

		// TODO change to cross-platform print dialog?
		if (pj.printDialog()) {
			PageFormat pf = getPageFormat();

			Paper paper = new Paper();
			double margin = 36;
			paper.setImageableArea(margin, margin, paper.getWidth()
					- margin * 2, paper.getHeight() - margin * 2);
			pf.setPaper(paper);
			pj.setPrintable(this, pf);

			try {
				pj.print();
			} catch (PrinterException e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to print graph", e); //$NON-NLS-1$
				
				DialogFactory.getGlobalFactory().showError(null, 
						"graph.messages.printFailed.title", //$NON-NLS-1$
						"graph.messages.printFailed.error", e); //$NON-NLS-1$
			}
		}
	}
	
	public void addNode() {
		
	}
	
	public void addEdge(Object source, Object target, boolean orderEdge) {
		
	}
	
	public void undo() {
		if(undoManager==null || !undoManager.canUndo()) {
			return;
		}
		
		undoManager.undo();
	}
	
	public void redo() {
		if(undoManager==null || !undoManager.canRedo()) {
			return;
		}
		
		undoManager.redo();
	}
	
	protected void handleUndoOperation(mxEventObject evt) {
		syncToData();
	}
	
	protected class Handler implements mxIEventListener, MouseWheelListener {

		/**
		 * @see com.mxgraph.util.mxEventSource.mxIEventListener#invoke(java.lang.Object, com.mxgraph.util.mxEventObject)
		 */
		@Override
		public void invoke(Object sender, mxEventObject evt) {
			
			// Handle undo/redo operations by re-synchronizing graph to data 
			if(sender==getUndoManager()) {
				handleUndoOperation(evt);
			}
			
		}

		/**
		 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
		 */
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if(!isWheelZoomEnabled()) {
				return;
			}
			
			if (e.isControlDown()) {
				if (e.getPreciseWheelRotation() < 0) {
					zoomIn();
				} else {
					zoomOut();
				}

				e.consume();
			}
		}
		
	}
	
	protected class CallbackHandler {
		
		public void cloneCells(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			if(graph.getSelectionCount()==0) {
				return;
			}
			
			Object[] cells = getSelectionCells();

			try {
				GraphPresenter.this.cloneCells(cells);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to clone cells", ex); //$NON-NLS-1$
			}
		}
		
		public void deleteCells(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			if(graph.getSelectionCount()==0) {
				return;
			}
			
			Object[] cells = getSelectionCells();

			try {
				GraphPresenter.this.deleteCells(cells);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to delete cells", ex); //$NON-NLS-1$
			}
		}
		
		public void moveCells(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			if(graph.getSelectionCount()==0) {
				return;
			}
			
			Object[] cells = getSelectionVertices();
			if(cells.length==0) {
				return;
			}
			
			double deltaX = 0;
			double deltaY = 0;
			
			double step = graph.getGridSize();
			
			switch (UIUtil.getDirection(e.getActionCommand())) {
			case SwingConstants.NORTH:
				deltaY = -step;
				break;
			case SwingConstants.NORTH_EAST:
				deltaY = -step;
				deltaX = step;
				break;
			case SwingConstants.NORTH_WEST:
				deltaY = -step;
				deltaX = -step;
				break;
			case SwingConstants.EAST:
				deltaX = step;
				break;
			case SwingConstants.WEST:
				deltaX = -step;
				break;
			case SwingConstants.SOUTH:
				deltaY = step;
				break;
			case SwingConstants.SOUTH_EAST:
				deltaY = step;
				deltaX = step;
				break;
			case SwingConstants.SOUTH_WEST:
				deltaY = step;
				deltaX = -step;
				break;
			}
			
			if(deltaX==0 && deltaY==0) {
				return;
			}
			
			try {
				GraphPresenter.this.moveCells(cells, deltaX, deltaY);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to move cells", ex); //$NON-NLS-1$
			}
		}
		
		public void copyCells(ActionEvent e) {
			
		}
		
		public void editCell(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
		}
		
		public void clearGraph(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
		}
		
		public void exportCells(ActionEvent e) {
			
		}
		
		public void importCells(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
		}
		
		public void printGraph(ActionEvent e) {			
			try {
				GraphPresenter.this.printGraph();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add node", ex); //$NON-NLS-1$
			}
		}
		
		public void addNode(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				GraphPresenter.this.addNode();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add node", ex); //$NON-NLS-1$
			}
		}
		
		public void addEdge(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				addEdge(e, false);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add edge", ex); //$NON-NLS-1$
			}
		}
		
		public void addOrderEdge(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				addEdge(e, true);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add order edge", ex); //$NON-NLS-1$
			}
		}
		
		protected void addEdge(ActionEvent e, boolean orderEdge) {
			if(graph.getSelectionCount()!=2) {
				return;
			}
			
			Object[] cells = getSelectionCells();
			
			Object source = cells[0];
			Object target = cells[1];
			
			mxIGraphModel model = graph.getModel();
			if(!model.isVertex(source) || !model.isVertex(target)) {
				return;
			}
			
			GraphPresenter.this.addEdge(source, target, orderEdge);
		}
		
		public void undoEdit(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				GraphPresenter.this.undo();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to undo edit", ex); //$NON-NLS-1$
			}
		}
		
		public void redoEdit(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				GraphPresenter.this.redo();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed redo edit", ex); //$NON-NLS-1$
			}
		}
		
		public void zoomIn(ActionEvent e) {
			try {
				GraphPresenter.this.zoomIn();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to zoom in", ex); //$NON-NLS-1$
			}
		}
		
		public void zoomOut(ActionEvent e) {
			try {
				GraphPresenter.this.zoomOut();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to zoom out", ex); //$NON-NLS-1$
			}
		}
		
		public void resetZoom(ActionEvent e) {
			try {
				GraphPresenter.this.zoomActual();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to reset zoom", ex); //$NON-NLS-1$
			}
		}
	}
}
