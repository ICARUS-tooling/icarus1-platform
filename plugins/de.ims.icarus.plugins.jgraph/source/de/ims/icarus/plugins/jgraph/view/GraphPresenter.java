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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.jgraph.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.ActionMap;
import javax.swing.ComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.java.plugin.registry.Extension;

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.model.mxIGraphModel.mxAtomicGraphModelChange;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxConnectPreview;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.swing.view.mxInteractiveCanvas;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;
import com.mxgraph.view.mxStylesheet;

import de.ims.icarus.Core;
import de.ims.icarus.config.ConfigDelegate;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.ExtensionListModel;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.jgraph.layout.DefaultArcLayout;
import de.ims.icarus.plugins.jgraph.layout.DefaultGraphRenderer;
import de.ims.icarus.plugins.jgraph.layout.DefaultGraphStyle;
import de.ims.icarus.plugins.jgraph.layout.GraphLayout;
import de.ims.icarus.plugins.jgraph.layout.GraphOwner;
import de.ims.icarus.plugins.jgraph.layout.GraphRenderer;
import de.ims.icarus.plugins.jgraph.layout.GraphStyle;
import de.ims.icarus.plugins.jgraph.layout.LayoutRegistry;
import de.ims.icarus.plugins.jgraph.util.CellBuffer;
import de.ims.icarus.plugins.jgraph.util.GraphUtils;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionList.EntryType;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.config.ConfigDialog;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.helper.Configurable;
import de.ims.icarus.ui.helper.DefaultFileFilter;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.Presenter;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.annotation.AnnotationControl;
import de.ims.icarus.util.annotation.AnnotationManager;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.xml.jaxb.JAXBUtils;


/**
 * Central place of graph visualization. The primary job of a {@code GraphPresenter}
 * implementation is the conversion of data objects into graph-cells and -edges
 * and the handling of data specific user interaction.
 * Several aspects of graph visualization have been moved to three optional helper
 * classes:
 * <p>
 * {@link GraphStyle} objects are used to provide basic styles for a graph and to
 * fetch the actual style of each cell.
 * <p>
 * A {@link GraphLayout} implements a special type of arrangement regarding vertices
 * and edges. Typically a {@code GraphLayout} enforces a certain edge style along
 * with it.
 * <p>
 * {@link GraphRenderer} are an advanced version of the basic {@link mxInteractiveCanvas}
 * used by a {@link mxGraphComponent} to render its cells and labels. In addition to
 * rendering a {@code GraphRenderer} object can be used to generate cell labels
 * and tool-tips as well as to calculate the required size of a given cell based
 * on its content.
 * <p>
 * Typically a {@code GraphPresenter} will offer the user a variety of aforementioned
 * helpers to select from in order to customize the actual graph visualization.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class GraphPresenter extends mxGraphComponent implements AWTPresenter, 
		GraphOwner, Configurable, Presenter.GraphBasedPresenter {

	private static final long serialVersionUID = -3776528318060931576L;
	
	protected ActionManager actionManager;
	
	//protected AnnotationManager annotationManager;
	protected AnnotationControl annotationControl;
	
	protected GraphLayout graphLayout;
	protected GraphStyle graphStyle;
	protected GraphRenderer graphRenderer;
	
	protected CallbackHandler callbackHandler;
	
	protected Handler handler;
	
	protected GraphUndoManager undoManager;
	
	protected mxRubberband rubberband;
	
	protected boolean compressEnabled;
	protected boolean autoZoomEnabled;
	protected boolean wheelZoomEnabled;
	
	protected Dimension minimumNodeSize;
	protected Dimension preferredNodeSize;
	protected Dimension maximumNodeSize;
	
	protected boolean editable = true;
	protected boolean enforceTree = false;
	protected boolean allowCycles = true;
	
	protected boolean markIncomingEdges = false;
	protected boolean markOutgoingEdges = false;
	
	protected Color incomingEdgeColor;
	protected Color outgoingEdgeColor;
	
	protected int ignoreDataChange = 0;
	protected int ignoreGraphChange = 0;
	protected int ignoreModCount = 0;
	
	protected JPopupMenu popupMenu;
	
	protected int modCount = 0;
	protected int lastRebuildModCount = modCount;
	
	protected ConfigDelegate configDelegate;
	protected EdgeHighlightHandler edgeHighlightHandler;
	
	protected Component presentingComponent;
	
	protected GraphPresenter() {
		super(null);
		
		setGraph(createGraph());
	}

	protected ActionManager createActionManager() {
		ActionManager actionManager = ActionManager.globalManager().derive();

		// Load default actions
		URL actionLocation = GraphPresenter.class.getResource(
				"graph-presenter-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: graph-presenter-actions.xml"); //$NON-NLS-1$
		try {
			actionManager.loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, "Failed to load actions from file: "+actionLocation, e); //$NON-NLS-1$
		}
		
		return actionManager;
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(boolean editable) {
		if(this.editable==editable) {
			return;
		}
		
		boolean oldValue = this.editable;
		this.editable = editable;
		
		// Force rebuild of valid popup-menu the next time it should be shown
		popupMenu = null;
		setImportEnabled(editable);
		setDragEnabled(editable);
		
		refreshActions();
		
		firePropertyChange("editable", oldValue, editable); //$NON-NLS-1$
	}
	
	public abstract ContentType getContentType();
	
	/**
	 * Per default we accept all data that is content-type compatible.
	 * @see de.ims.icarus.ui.view.Presenter#supports(java.lang.Object)
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
		if(!PresenterUtils.presenterSupports(this, data))
			throw new UnsupportedPresentationDataException("Unsupported data: "+data.getClass()); //$NON-NLS-1$
		
		// TODO ask user if he wants to cancel edit?
		stopEditing(true);
		
		setData(data, options);
		
		AnnotationManager annotationManager = getAnnotationManager();
		if(annotationManager!=null && data instanceof AnnotatedData) {
			annotationManager.removePropertyChangeListener(getHandler());
			annotationManager.setAnnotation(((AnnotatedData)data).getAnnotation());
			annotationManager.addPropertyChangeListener(getHandler());
		}
		
		rebuildGraph();
		clearUndoHistory();
	}
	
	protected Component createPresentingComponent() {
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(this, BorderLayout.CENTER);
		
		Component upperComponent = createUpperComponent();
		Component leftComponent = createLeftComponent();
		
		if(upperComponent!=null) {
			contentPanel.add(upperComponent, BorderLayout.NORTH);
		}
		
		if(leftComponent!=null) {
			contentPanel.add(leftComponent, BorderLayout.WEST);
		}
		
		return contentPanel;
	}

	@Override
	protected mxConnectionHandler createConnectionHandler() {
		return new DelegatingConnectionHandler();
	}
	
	/*@Override
	protected TransferHandler createTransferHandler() {
		//return new GraphTransferHandler();
		return super.createTransferHandler();
	}*/

	protected mxConnectPreview createConnectPreview() {
		return new DelegatingConnectPreview();
	}
	
	protected ConfigDelegate createConfigDelegate() {
		return new GraphConfigDelegate("plugins.jgraph.appearance.default", null); //$NON-NLS-1$
	}
	
	protected EdgeHighlightHandler createEdgeHighlightHandler() {
		return new EdgeHighlightHandler();
	}
	
	public EdgeHighlightHandler getEdgeHighlightHandler() {
		return edgeHighlightHandler;
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
	
	protected Handler getHandler() {
		if(handler==null) {
			handler = createHandler();
		}
		return handler;
	}
	
	protected void init() {
		initGraphComponentInternals();
		
		installUtilities();
		
		installKeyboardActions();
		
		loadPreferences();
		
		registerActionCallbacks();
	}
	
	protected void initGraphComponentInternals() {
		setImportEnabled(isEditable());
		setDragEnabled(isEditable());
		setToolTips(true);
		setKeepSelectionVisibleOnZoom(true);
		setGridVisible(true);

		setBorder(UIUtil.topLineBorder);
		getViewport().setOpaque(true);
		getViewport().setBackground(Color.WHITE);
		
		UIUtil.defaultSetUnitIncrement(this);

		setAntiAlias(true);
		setTextAntiAlias(true);
	}
	
	protected void loadPreferences() {
		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		setAutoZoomEnabled(config.getBoolean(
				"plugins.jgraph.appearance.default.autoZoom")); //$NON-NLS-1$
		setCompressEnabled(config.getBoolean(
				"plugins.jgraph.appearance.default.compressGraph")); //$NON-NLS-1$
	}
	
	protected void installUtilities() {		
		addMouseWheelListener(getHandler());
		addComponentListener(getHandler());
		
		// Create config delegate BEFORE any default implementations
		// for style/layout/renderer so they can access config data!
		configDelegate = createConfigDelegate();
		if(configDelegate!=null) {
			configDelegate.addChangeListener(getHandler());
			
			configDelegate.reload();
		}
		
		annotationControl = createAnnotationControl();
		if(annotationControl!=null) {
			annotationControl.addPropertyChangeListener("annotationManager", getHandler()); //$NON-NLS-1$
		}
		
		AnnotationManager annotationManager = getAnnotationManager();
		if(annotationManager!=null) {
			annotationManager.addPropertyChangeListener(getHandler());
		}
		
		edgeHighlightHandler = createEdgeHighlightHandler();
		
		setGraphLayout(createDefaultGraphLayout());
		setGraphStyle(createDefaultGraphStyle());
		setGraphRenderer(createDefaultGraphRenderer());
		
		getGraphControl().addMouseListener(getHandler());
		
		rubberband = new mxRubberband(this);
		
		GraphUndoManager undoManager = getUndoManager();
		if(undoManager!=null) {
			undoManager.addListener(null, getHandler());
		}
		
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard!=null) {
			clipboard.addFlavorListener(getHandler());
		}
	}
	
	protected GraphLayout createDefaultGraphLayout() {
		return new DefaultArcLayout();
	}
	
	protected GraphStyle createDefaultGraphStyle() {
		return new DefaultGraphStyle();
	}
	
	protected GraphRenderer createDefaultGraphRenderer() {
		return new DefaultGraphRenderer();
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
	
	protected String editableMainToolBarListId = "plugins.jgraph.graphPresenter.editableMainToolBarList"; //$NON-NLS-1$
	protected String uneditableMainToolBarListId = "plugins.jgraph.graphPresenter.uneditableMainToolBarList"; //$NON-NLS-1$
		
	protected Component createUpperComponent() {
		return createUpperToolBar().buildToolBar();
	}
	
	protected ActionComponentBuilder createUpperToolBar() {
		ActionComponentBuilder builder = new ActionComponentBuilder(getActionManager());
		
		builder.setActionListId(isEditable() ? editableMainToolBarListId : uneditableMainToolBarListId);

		Options options = new Options();
		feedSelector(options, SELECT_LAYOUT_COMMAND);
		feedSelector(options, SELECT_STYLE_COMMAND);
		feedSelector(options, SELECT_RENDERER_COMMAND);
		options.put("multiline", true); //$NON-NLS-1$
		
		if(annotationControl!=null) {
			List<Object> items = new ArrayList<>();
			items.add(EntryType.SEPARATOR);
			CollectionUtils.feedItems(items, (Object[])annotationControl.getComponents());
			
			options.put("annotationControl", items.toArray()); //$NON-NLS-1$
		}
		
		builder.addOptions(options);
		
		return builder;
	}
	
	protected AnnotationControl createAnnotationControl() {
		return new AnnotationControl(true);
	}
	
	protected JComboBox<Extension> feedSelector(Options options, final String command) {
		
		String propertyName;
		Object currentValue = null;
		Collection<Extension> items = null;
		switch (command) {
		case SELECT_LAYOUT_COMMAND:
			items = LayoutRegistry.getInstance().getCompatibleLayouts(getContentType(), true);
			propertyName = "graphLayout"; //$NON-NLS-1$
			currentValue = getGraphLayout();
			break;

		case SELECT_STYLE_COMMAND:
			items = LayoutRegistry.getInstance().getCompatibleStyles(getContentType(), true);
			propertyName = "graphStyle"; //$NON-NLS-1$
			currentValue = getGraphStyle();
			break;

		case SELECT_RENDERER_COMMAND:
			items = LayoutRegistry.getInstance().getCompatibleRenderers(getContentType(), true);
			propertyName = "graphRenderer"; //$NON-NLS-1$
			currentValue = getGraphRenderer();
			break;

		default:
			throw new IllegalArgumentException("Unknown selection command: "+command); //$NON-NLS-1$
		}
		
		// Return null if there are no extensions or if it would
		// not make sense to present a selection
		if(items==null || items.size()<2) {
			return null;
		}
		
		final JComboBox<Extension> comboBox = new JComboBox<>(
				new ExtensionListModel(items, true));
		
		// Keeps selection on the combo-box in sync
		// wit current set object in the presenter
		PropertyChangeListener listener = new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Object selectedValue = evt.getNewValue();
				if(selectedValue==null) {
					comboBox.setSelectedItem(null);
				} else {
					String selectedClassName = selectedValue.getClass().getName();
					ComboBoxModel<Extension> model = comboBox.getModel();
					for(int i=0; i<model.getSize(); i++) {
						Extension extension = model.getElementAt(i);
						if(extension.getParameter("class").valueAsString().equals(selectedClassName)) { //$NON-NLS-1$
							comboBox.setSelectedItem(extension);
							break;
						}
					}
				}
				
			}
		};
		
		// Ensure correct initial selection
		if(currentValue!=null) {
			listener.propertyChange(new PropertyChangeEvent(this, propertyName, null, currentValue));
		}

		addPropertyChangeListener(propertyName, listener);
		
		comboBox.setRenderer(ExtensionListCellRenderer.getSharedInstance());
		comboBox.setEditable(false);
		comboBox.addActionListener(getHandler());
		comboBox.setActionCommand(command);
		comboBox.setFocusable(false);
		String tooltip = ResourceManager.getInstance().get(
				"plugins.jgraph.toolBar."+command+".descriptions", (String)null); //$NON-NLS-1$ //$NON-NLS-2$
		comboBox.setToolTipText(UIUtil.toSwingTooltip(tooltip));
		
		UIUtil.fitToContent(comboBox, 100, 200, 22);
		
		if(options!=null) {
			options.put(command, comboBox);
			options.put(command+"Label", "plugins.jgraph.toolBar."+command+".label"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		return comboBox;
	}
	
	protected Component createLeftComponent() {
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
		return new DelegatingGraph();
	}
	
	protected void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = createCallbackHandler();
		}
		
		ActionManager actionManager = getActionManager();
		if(actionManager==null) 
			throw new CorruptedStateException("No valid action-manager present"); //$NON-NLS-1$
		
		// Init 'selected' states
		actionManager.setSelected(isAutoZoomEnabled(), 
				"plugins.jgraph.graphPresenter.toggleAutoZoomAction"); //$NON-NLS-1$
		actionManager.setSelected(isCompressEnabled(), 
				"plugins.jgraph.graphPresenter.toggleCompressAction"); //$NON-NLS-1$
		actionManager.setSelected(isMarkIncomingEdges(), 
				"plugins.jgraph.graphPresenter.toggleMarkIncomingAction"); //$NON-NLS-1$
		actionManager.setSelected(isMarkOutgoingEdges(), 
				"plugins.jgraph.graphPresenter.toggleMarkOutgoingAction"); //$NON-NLS-1$
		
		// Register callback functions
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.cloneCellsAction",  //$NON-NLS-1$
				callbackHandler, "cloneCells"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.copyCellsAction",  //$NON-NLS-1$
				callbackHandler, "copyCells"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.pasteCellsAction",  //$NON-NLS-1$
				callbackHandler, "pasteCells"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.moveCellsNorthAction",  //$NON-NLS-1$
				callbackHandler, "moveCells"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.moveCellsSouthAction",  //$NON-NLS-1$
				callbackHandler, "moveCells"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.moveCellsWestAction",  //$NON-NLS-1$
				callbackHandler, "moveCells"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.moveCellsEastAction",  //$NON-NLS-1$
				callbackHandler, "moveCellst"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.deleteCellsAction",  //$NON-NLS-1$
				callbackHandler, "deleteCells"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.printGraphAction",  //$NON-NLS-1$
				callbackHandler, "printGraph"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.clearGraphAction",  //$NON-NLS-1$
				callbackHandler, "clearGraph"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.addNodeAction",  //$NON-NLS-1$
				callbackHandler, "addNode"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.addEdgeAction",  //$NON-NLS-1$
				callbackHandler, "addEdge"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.addOrderEdgeAction",  //$NON-NLS-1$
				callbackHandler, "addOrderEdge"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.editCellAction",  //$NON-NLS-1$
				callbackHandler, "editCell"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.importCellsAction",  //$NON-NLS-1$
				callbackHandler, "importCells"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.exportCellsAction",  //$NON-NLS-1$
				callbackHandler, "exportCells"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.undoEditAction",  //$NON-NLS-1$
				callbackHandler, "undoEdit"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.redoEditAction",  //$NON-NLS-1$
				callbackHandler, "redoEdit"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.zoomInAction",  //$NON-NLS-1$
				callbackHandler, "zoomIn"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.zoomOutAction",  //$NON-NLS-1$
				callbackHandler, "zoomOut"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.resetZoomAction",  //$NON-NLS-1$
				callbackHandler, "resetZoom"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.openPreferencesAction",  //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.rebuildGraphAction",  //$NON-NLS-1$
				callbackHandler, "rebuildGraph"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.collapseCellsAction",  //$NON-NLS-1$
				callbackHandler, "collapseCells"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.expandCellsAction",  //$NON-NLS-1$
				callbackHandler, "expandCells"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.toggleAutoZoomAction",  //$NON-NLS-1$
				callbackHandler, "toggleAutoZoom"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.toggleCompressAction",  //$NON-NLS-1$
				callbackHandler, "toggleCompress"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.toggleMarkIncomingAction",  //$NON-NLS-1$
				callbackHandler, "toggleMarkIncoming"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.jgraph.graphPresenter.toggleMarkOutgoingAction",  //$NON-NLS-1$
				callbackHandler, "toggleMarkOutgoing"); //$NON-NLS-1$
	}
	
	protected void refreshActions() {
		
		ActionManager actionManager = getActionManager();
		
		mxIGraphModel model = graph.getModel();
		Object parent = graph.getDefaultParent();
		
		int selectionCount = graph.getSelectionCount();
		boolean editable = isEditable();
		boolean empty = model.getChildCount(parent)==0;
		boolean selected = !graph.isSelectionEmpty();
		boolean canUndo = undoManager!=null && undoManager.canUndo();
		boolean canRedo = undoManager!=null && undoManager.canRedo();
		boolean rebuildAllowed = lastRebuildModCount<modCount;
		
		boolean pastable = editable;
		if(pastable) {
			try {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable t = clipboard.getContents(this);
				TransferHandler th = getTransferHandler();
				pastable = t!=null && th.canImport(this, t.getTransferDataFlavors());
			} catch(IllegalStateException e) {
				// ignore
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to determine content of system clipboard", e); //$NON-NLS-1$
				pastable = false;
			}
		}

		Object[] selectedVertices = getSelectionVertices();
		
		boolean allowNewEdge = editable && selectionCount==2;
		if(allowNewEdge && !allowCycles) {
			allowNewEdge = !GraphUtils.isAncestor(model, 
					selectedVertices[1], selectedVertices[0], true, false);
		}
		
		boolean allowOrderEdge = editable && selectionCount==2;
		if(allowOrderEdge && !allowCycles) {
			allowOrderEdge = !GraphUtils.isAncestor(model, 
					selectedVertices[1], selectedVertices[0], false, true);
		}
		
		actionManager.setEnabled(editable,
				"plugins.jgraph.graphPresenter.addNodeAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(editable && rebuildAllowed,
				"plugins.jgraph.graphPresenter.rebuildGraphAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(editable && importEnabled,
				"plugins.jgraph.graphPresenter.importCellsAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(pastable,
				"plugins.jgraph.graphPresenter.pasteCellsAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(!empty,
				"plugins.jgraph.graphPresenter.printGraphAction", //$NON-NLS-1$
				"plugins.jgraph.graphPresenter.zoomInAction", //$NON-NLS-1$
				"plugins.jgraph.graphPresenter.zoomOutAction", //$NON-NLS-1$
				"plugins.jgraph.graphPresenter.resetZoomAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(!empty && exportEnabled,
				"plugins.jgraph.graphPresenter.exportCellsAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(selected && exportEnabled,
				"plugins.jgraph.graphPresenter.copyCellsAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(editable && selected,
				"plugins.jgraph.graphPresenter.deleteCellsAction", //$NON-NLS-1$
				"plugins.jgraph.graphPresenter.cloneCellsAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(editable && !empty,
				"plugins.jgraph.graphPresenter.clearGraphAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(editable && canUndo,
				"plugins.jgraph.graphPresenter.undoEditAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(editable && canRedo,
				"plugins.jgraph.graphPresenter.redoEditAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(editable && selectionCount==1,
				"plugins.jgraph.graphPresenter.editCellAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(selectionCount==1 && canCollapse(graph.getSelectionCell()),
				"plugins.jgraph.graphPresenter.collapseCellsAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(selectionCount==1 && canExpand(graph.getSelectionCell()),
				"plugins.jgraph.graphPresenter.expandCellsAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(allowNewEdge,
				"plugins.jgraph.graphPresenter.addEdgeAction"); //$NON-NLS-1$

		actionManager.setEnabled(allowOrderEdge,
				"plugins.jgraph.graphPresenter.addOrderEdgeAction"); //$NON-NLS-1$
	}
	
	protected String editablePopupMenuListId = "plugins.jgraph.graphPresenter.editablePopupMenuList"; //$NON-NLS-1$
	protected String uneditablePopupMenuListId = "plugins.jgraph.graphPresenter.uneditablePopupMenuList"; //$NON-NLS-1$
	
	protected JPopupMenu createPopupMenu() {
		
		String actionListId = isEditable() ? editablePopupMenuListId 
				: uneditablePopupMenuListId;
		return getActionManager().createPopupMenu(actionListId, null);
	}
	
	protected void showPopup(MouseEvent trigger) {
		if(popupMenu==null) {
			// Create new popup menu
			
			popupMenu = createPopupMenu();
			
			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}
		
		if(popupMenu!=null) {
			refreshActions();
			
			popupMenu.show(this.getGraphControl(), trigger.getX(), trigger.getY());
		}
	}
	
	public boolean isCompressEnabled() {
		return compressEnabled;
	}

	public boolean isAutoZoomEnabled() {
		return autoZoomEnabled;
	}

	public void setCompressEnabled(boolean compressEnabled) {	
		if(compressEnabled==this.compressEnabled) {
			return;
		}
		
		boolean oldValue = this.compressEnabled;
		this.compressEnabled = compressEnabled;
		
		rebuildGraph();
		
		getActionManager().setSelected(isCompressEnabled(), 
				"plugins.jgraph.graphPresenter.toggleCompressAction"); //$NON-NLS-1$
		
		firePropertyChange("compressEnabled", oldValue, compressEnabled); //$NON-NLS-1$
	}

	public void setAutoZoomEnabled(boolean autoZoomEnabled) {
		if(autoZoomEnabled==this.autoZoomEnabled) {
			return;
		}
		
		boolean oldValue = this.autoZoomEnabled;
		this.autoZoomEnabled = autoZoomEnabled;
		
		refreshLayout();
		
		// Reset zoom if auto-zoom is now disabled
		if(!autoZoomEnabled) {
			zoomActual();
		}
		
		getActionManager().setSelected(isAutoZoomEnabled(), 
				"plugins.jgraph.graphPresenter.toggleAutoZoomAction"); //$NON-NLS-1$
		
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

	public boolean isMarkIncomingEdges() {
		return markIncomingEdges;
	}

	public boolean isMarkOutgoingEdges() {
		return markOutgoingEdges;
	}

	public void setMarkIncomingEdges(boolean markIncomingEdges) {
		if(markIncomingEdges==this.markIncomingEdges) {
			return;
		}
		
		boolean oldValue = this.markIncomingEdges;
		this.markIncomingEdges = markIncomingEdges;

		if(edgeHighlightHandler!=null) {
			edgeHighlightHandler.reload();
		}

		refreshEdges();
		
		firePropertyChange("markIncomingEdges", oldValue, markIncomingEdges); //$NON-NLS-1$
	}

	public void setMarkOutgoingEdges(boolean markOutgoingEdges) {
		if(markOutgoingEdges==this.markOutgoingEdges) {
			return;
		}
		
		boolean oldValue = this.markOutgoingEdges;
		this.markOutgoingEdges = markOutgoingEdges;

		if(edgeHighlightHandler!=null) {
			edgeHighlightHandler.reload();
		}
		
		refreshEdges();
		
		firePropertyChange("markOutgoingEdges", oldValue, markOutgoingEdges); //$NON-NLS-1$
	}

	public Color getIncomingEdgeColor() {
		return incomingEdgeColor;
	}

	public Color getOutgoingEdgeColor() {
		return outgoingEdgeColor;
	}

	public void setIncomingEdgeColor(Color incomingEdgeColor) {
		this.incomingEdgeColor = incomingEdgeColor;
	}

	public void setOutgoingEdgeColor(Color outgoingEdgeColor) {
		this.outgoingEdgeColor = outgoingEdgeColor;
	}

	public GraphUndoManager getUndoManager() {
		if(!isEditable()) {
			return null;
		}
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
			
			graph.getSelectionModel().removeListener(getHandler());
			graph.removePropertyChangeListener(getHandler());
			graph.getModel().removeListener(getHandler());
			
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

			// Keeps actions synchronized
			graph.getModel().addListener(mxEvent.UNDO, getHandler());
			graph.addPropertyChangeListener("model", getHandler()); //$NON-NLS-1$
			graph.addPropertyChangeListener("view", getHandler()); //$NON-NLS-1$
			graph.getSelectionModel().addListener(mxEvent.CHANGE, getHandler());
			
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
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		setData(null, null);
		clearGraph();
		clearUndoHistory();
		
		AnnotationManager annotationManager = getAnnotationManager();
		if(annotationManager!=null) {
			annotationManager.setAnnotation(null);
		}
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		clear();
		setGraph(null);
		
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard!=null) {
			clipboard.removeFlavorListener(getHandler());
		}
	}

	/**
	 * @see de.ims.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(presentingComponent==null) {
			init();
			
			presentingComponent = createPresentingComponent();
		}
		
		return presentingComponent;
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
		if(graph==null) {
			return;
		}
		if(layout==graphLayout) {
			return;
		}
		
		executeChange(new LayoutChange(layout));
	}
	
	public GraphStyle getGraphStyle() {
		return graphStyle;
	}
	
	public void setGraphStyle(GraphStyle style) {
		if(graph==null) {
			return;
		}
		if(style==graphStyle) {
			return;
		}
		
		executeChange(new StyleChange(style));
	}
	
	public GraphLayout getGraphLayout() {
		return graphLayout;
	}
	
	public GraphRenderer getGraphRenderer() {
		return graphRenderer;
	}

	public void setGraphRenderer(GraphRenderer renderer) {
		if(graph==null) {
			return;
		}
		if(renderer==graphRenderer) {
			return;
		}
		
		executeChange(new RendererChange(renderer));
	}

	public void setCanvas(mxInteractiveCanvas canvas) {
		if(canvas==null)
			throw new NullPointerException("Invalid canvas"); //$NON-NLS-1$

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
	
	public AnnotationManager getAnnotationManager() {
		return annotationControl==null ? null : annotationControl.getAnnotationManager();
	}

	public void setAnnotationManager(AnnotationManager annotationManager) {
		if(annotationControl==null) {
			return;
		}
		
		AnnotationManager oldValue = getAnnotationManager();
		if(oldValue==annotationManager) {
			return;
		}
		
		if(oldValue!=null) {
			oldValue.removePropertyChangeListener(getHandler());
		}
		
		if(annotationControl!=null) {
			annotationControl.setAnnotationManager(annotationManager);
		}
		
		if(annotationManager!=null) {
			annotationManager.addPropertyChangeListener(getHandler());
		}
		
		firePropertyChange("annotationManager", oldValue, annotationManager); //$NON-NLS-1$
	}

	public Dimension getMinimumNodeSize() {
		return minimumNodeSize;
	}

	public Dimension getPreferredNodeSize() {
		return preferredNodeSize;
	}

	public Dimension getMaximumNodeSize() {
		return maximumNodeSize;
	}

	public void setMinimumNodeSize(Dimension minimumNodeSize) {
		this.minimumNodeSize = minimumNodeSize;
	}

	public void setPreferredNodeSize(Dimension preferredNodeSize) {
		this.preferredNodeSize = preferredNodeSize;
	}

	public void setMaximumNodeSize(Dimension maximumNodeSize) {
		this.maximumNodeSize = maximumNodeSize;
	}
	
	public boolean isEnforceTree() {
		return enforceTree;
	}

	public boolean isAllowCycles() {
		return allowCycles;
	}

	public void setEnforceTree(boolean enforceTree) {
		if(this.enforceTree==enforceTree) {
			return;
		}
		
		boolean oldValue = this.enforceTree;
		this.enforceTree = enforceTree;
		
		firePropertyChange("enforceTree", oldValue, enforceTree); //$NON-NLS-1$
	}

	public void setAllowCycles(boolean allowCycles) {
		if(this.allowCycles==allowCycles) {
			return;
		}
		
		boolean oldValue = this.allowCycles;
		this.allowCycles = allowCycles;
		
		firePropertyChange("allowCycles", oldValue, allowCycles); //$NON-NLS-1$
	}

	public ConfigDelegate getConfigDelegate() {
		return configDelegate;
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
	
	/**
	 * Build up graph cells and structure and refresh styles and layout
	 * (in that order).
	 */
	protected abstract void syncToGraph();

	/**
	 * Read current graph content and synchronize to internal data.
	 */
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
	
	public void refreshAll() {
		mxIGraphModel model = graph.getModel();
		
		pauseGraphChangeHandling();
		model.beginUpdate();
		try {
			refreshStyles();
			refreshLayout();
		} finally {
			model.endUpdate();
			resumeGraphChangeHandling();
		}
	}
	
	protected Options createLayoutOptions() {
		return null;
	}
	
	public void refreshLayout() {
		if(graphLayout==null) {
			return;
		}
		
		Object[] cells = getLayoutCells();
		if(cells==null || cells.length==0) {
			return;
		}
		
		Options layoutOptions = createLayoutOptions();
		Dimension size = getSize();
		mxRectangle bounds = new mxRectangle(0, 0, size.getWidth(), size.getHeight());
		
		pauseGraphChangeHandling();
		try {
			if(isCompressEnabled()) {			
				bounds = graphLayout.compressGraph(this, cells, layoutOptions, bounds);
			} else {
				bounds = graphLayout.layoutGraph(this, cells, layoutOptions);
			}
		} finally {
			resumeGraphChangeHandling();
		}
		
		
		// TODO maybe move the zoom and scroll handling into the Handler.invoke() method?
		
		if(isAutoZoomEnabled()) {
			final mxRectangle gSize = bounds;
			final Dimension cSize = size;
			
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					double newScale = Math.min(cSize.getWidth()/gSize.getWidth(), 
							cSize.getHeight()/gSize.getHeight());
					
					if(newScale>1) {
						newScale = 1;
					}
					
					zoomTo(newScale, centerZoom);
				}
			});
		} else {
			final Object cell = graphLayout.getSignificantCell(this, cells, layoutOptions);
			
			if(cell!=null) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						scrollCellToVisible(cell);
					}
				});
			}
		}
	}
	
	protected Options createStyleOptions() {
		return null;
	}

	public void refreshEdges() {
		refreshCells(graph.getChildEdges(graph.getDefaultParent()));
	}

	public void refreshVertices() {
		refreshCells(graph.getChildVertices(graph.getDefaultParent()));
	}
	
	public void refreshCells(Object...cells) {
		if(graphStyle==null) {
			return;
		}
		if(cells==null || cells.length==0) {
			return;
		}
		
		mxIGraphModel model = graph.getModel();
		pauseGraphChangeHandling();
		model.beginUpdate();
		try {
			Options styleOptions = createStyleOptions();
			Options layoutOptions = createLayoutOptions();
			
			for(Object cell : cells) {
				model.setStyle(cell, graphStyle.getStyle(this, cell, styleOptions));
				if(model.isVertex(cell)) {
					graph.cellSizeUpdated(cell, false);
				}
				if(graphLayout!=null && model.isEdge(cell)) {
					model.setStyle(cell, graphLayout.getEdgeStyle(this, cell, layoutOptions));
				}
			}
		} finally {
			model.endUpdate();
			resumeGraphChangeHandling();
		}
	}
	
	/**
	 * Uses the current {@code GraphStyle} to refresh style strings
	 * on all cells. Note that since this technically erases all style
	 * modifications done by the current {@code GraphLayout} this call
	 * should always be followed by {@link #refreshLayout()} to allow
	 * for proper final styles!
	 * <p>
	 * If no {@code GraphStyle} is currently set this method does nothing.
	 */
	public void refreshStyles() {
		if(graphStyle==null) {
			return;
		}
		
		mxIGraphModel model = graph.getModel();
		Object parent = graph.getDefaultParent();
		Options styleOptions = createStyleOptions();
		
		pauseGraphChangeHandling();
		model.beginUpdate();
		try {
			for(Object cell : graph.getChildCells(parent)) {
				String style = graphStyle.getStyle(this, cell, styleOptions);
				if(style==null) {
					style = model.isVertex(cell) ? "defaultVertex" : "defaultEdge"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				model.setStyle(cell, style);
			}
		} finally {
			model.endUpdate();
			resumeGraphChangeHandling();
		}
	}
	
	protected void showMessage(int messageType, String message, Object...params) {
		switch (messageType) {
		case JOptionPane.ERROR_MESSAGE:
			DialogFactory.getGlobalFactory().showError(null, 
					"plugins.jgraph.graphPresenter.messages.error",  //$NON-NLS-1$
					message, params);
			break;

		case JOptionPane.WARNING_MESSAGE:
			DialogFactory.getGlobalFactory().showWarning(null, 
					"plugins.jgraph.graphPresenter.messages.warning",  //$NON-NLS-1$
					message, params);
			break;

		default:
			DialogFactory.getGlobalFactory().showInfo(null, 
					"plugins.jgraph.graphPresenter.messages.info",  //$NON-NLS-1$
					message, params);
			break;
		}
	}
	
	public boolean isOrderEdge(Object cell) {
		return GraphUtils.isOrderEdge(graph.getModel(), cell);
	}
	
	public boolean isLinkEdge(Object cell) {
		return false;
	}
	
	public void moveCells(Object[] cells, double deltaX, double deltaY) {
		if (!isEditable()) {
			return;
		}
		
		graph.getModel().beginUpdate();
		try {
			GraphUtils.moveCells(graph, cells, deltaX, deltaY);
		} finally {
			graph.getModel().endUpdate();
		}
	}

	
	public void editCell(Object cell) {
		if (!isEditable() || isOrderEdge(cell)) {
			return;
		}
		
		startEditingAtCell(cell);
	}
	
	public void deleteCells(Object[] cells) {
		if (!isEditable()) {
			return;
		}
		
		graph.getModel().beginUpdate();
		try {
			GraphUtils.deleteCells(graph, cells);
		} finally {
			graph.getModel().endUpdate();
		}
	}
	
	public void cloneCells(Object[] cells) {
		throw new UnsupportedOperationException("Method not overriden by subclass: "+getClass()); //$NON-NLS-1$
	}
	
	public void copyCells() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard==null) {
			return;
		}
		getTransferHandler().exportToClipboard(this, clipboard, TransferHandler.COPY);
	}
	
	public void pasteCells() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard==null) {
			return;
		}
		
		try {
			Transferable transferable = clipboard.getContents(this);
			TransferHandler transferHandler = getTransferHandler();
			if(!transferHandler.canImport(this, transferable.getTransferDataFlavors())) {
				return;
			}
			
			transferHandler.importData(this, transferable);
		} catch(IllegalStateException e) {
			// ignore
		}
	}
	
	public void rebuildGraph() {
		pauseModCountHandling();
		pauseGraphChangeHandling();
		graph.getModel().beginUpdate();
		try {
			lastRebuildModCount = modCount;
			syncToGraph();
			
			refreshAll();
		} finally {
			graph.getModel().endUpdate();
			resumeModCountHandling();
			resumeGraphChangeHandling();
		}
		// TODO verify need to clear undo history!
		//clearUndoHistory();
	}
	
	/**
	 * Removes all cells from the graph and synchronizes the
	 * empty graph to the internal data, effectively clearing it
	 * of whatever content it is holding.
	 */
	public void clearGraph() {
		graph.getModel().beginUpdate();
		try {
			GraphUtils.clearGraph(graph);
		} finally {
			graph.getModel().endUpdate();
		}
	}
	
	/**
	 * Creates and returns an interchangeable snapshot of the
	 * current graph and all of it's internal data fit for exportation.
	 */
	public CellBuffer exportCells(Object[] cells) {
		throw new UnsupportedOperationException("Method not overriden by subclass: "+getClass()); //$NON-NLS-1$
	}
	
	/**
	 * Imports and integrates the given {@code CellBuffer}'s content
	 * into the graph and synchronizes the resulting graph to the internal
	 * data.
	 */
	public void importCells(CellBuffer buffer) {
		throw new UnsupportedOperationException("Method not overriden by subclass: "+getClass()); //$NON-NLS-1$
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
	
	/**
	 * Appends a new empty node to the graph and synchronizes it 
	 * with the internal data. Note that the meaning of the term 
	 * <i>empty</i> is implementation depending!
	 */
	public void addNode() {
		throw new UnsupportedOperationException("Method not overriden by subclass: "+getClass()); //$NON-NLS-1$
	}
	
	public void addEdge(Object source, Object target, boolean orderEdge) {
		throw new UnsupportedOperationException("Method not overriden by subclass: "+getClass()); //$NON-NLS-1$
	}
	
	public void undo() {
		if(undoManager==null || !undoManager.canUndo()) {
			return;
		}
		
		// Perform undo
		undoManager.undo();
	}
	
	public void redo() {
		if(undoManager==null || !undoManager.canRedo()) {
			return;
		}
		
		// Perform redo 
		undoManager.redo();
	}
	
	@Override
	public void openConfig() {
		if(configDelegate!=null) {
			Handle handle = configDelegate.getHandle();
			ConfigRegistry registry = handle.getSource();
			new ConfigDialog(registry, handle).setVisible(true);
		}
	}
	
	public void collapseCells(Object[] cells) {
		throw new UnsupportedOperationException("Method not overriden by subclass: "+getClass()); //$NON-NLS-1$
	}
	
	public void expandCells(Object[] cells) {
		throw new UnsupportedOperationException("Method not overriden by subclass: "+getClass()); //$NON-NLS-1$
	}
	
	public void pauseChangeHandling() {
		ignoreDataChange++;
	}
	
	public void resumeChangeHandling() {
		ignoreDataChange--;
	}
	
	public boolean isIgnoringChanges() {
		return ignoreDataChange>0;
	}
	
	public void pauseGraphChangeHandling() {
		ignoreGraphChange++;
	}
	
	public void resumeGraphChangeHandling() {
		ignoreGraphChange--;
	}
	
	public boolean isIgnoringGraphChanges() {
		return ignoreGraphChange>0;
	}
	
	protected void pauseModCountHandling() {
		ignoreModCount++;
	}
	
	protected void resumeModCountHandling() {
		ignoreModCount--;
	}
	
	protected boolean isIgnoringModCounts() {
		return ignoreModCount>0;
	}
	
	public boolean isHighlightedIncomingEdge(Object edge) {
		return edgeHighlightHandler!=null && edgeHighlightHandler.isIncomingHighlighted(edge);
	}
	
	public boolean isHighlightedOutgoingEdge(Object edge) {
		return edgeHighlightHandler!=null && edgeHighlightHandler.isOutgoingHighlighted(edge);
	}
	
	/**
	 * Hook for subclasses to allow renderers or layouts to determine
	 * whether a given cell can be expanded and should therefore be decorated
	 * with additional hints for the user.
	 */
	public boolean canExpand(Object cell) {
		return false;
	}
	
	/**
	 * Hook for subclasses to allow renderers or layouts to determine
	 * whether a given cell can be collapsed and should therefore be decorated
	 * with additional hints for the user.
	 */
	public boolean canCollapse(Object cell) {
		return false;
	}
	
	protected void executeChange(mxAtomicGraphModelChange change) {
		
		mxGraphModel model = (mxGraphModel) graph.getModel();
		
		pauseGraphChangeHandling();
		model.beginUpdate();
		try {
			model.execute(change);
		} finally {
			model.endUpdate();
			resumeGraphChangeHandling();
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class DelegatingGraph extends mxGraph {

		public DelegatingGraph() {
			super();
			
			init();
		}

		public DelegatingGraph(mxIGraphModel model, mxStylesheet stylesheet) {
			super(model, stylesheet);
			
			init();
		}

		public DelegatingGraph(mxIGraphModel model) {
			super(model);
			
			init();
		}

		public DelegatingGraph(mxStylesheet stylesheet) {
			super(stylesheet);
			
			init();
		}

		protected void init() {

			setGridSize(10);
			
			setMultigraph(false);
			setAllowDanglingEdges(false);
			setAllowLoops(false);
			setCellsDisconnectable(false);
			setCellsResizable(false);
			setEdgeLabelsMovable(false);
			setAutoSizeCells(true);
			setHtmlLabels(false);
			setGridEnabled(true);
			setSplitEnabled(false);
			setCellsCloneable(true);
			setCellsMovable(true);

			getSelectionModel().setSingleSelection(false);
		}

		/**
		 * Computes the required size for the given cell.
		 * If a default size is set using {@link GraphPresenter#setPreferredNodeSize(Dimension)}
		 * then this value will be used ignoring all other settings.
		 * Otherwise asks the current {@code GraphRenderer} of the enclosing
		 * {@code GraphPresenter} if present or if this fails delegates
		 * to the super method.
		 * <p>
		 * In addition the size will be adjusted to fit within the
		 * {@code minimumNodeSize} and {@code maximumNodeSize} if those
		 * fields are not {@code null}.
		 * 
		 * @see GraphRenderer#getPreferredSizeForCell(GraphOwner, Object)
		 * @see com.mxgraph.view.mxGraph#getPreferredSizeForCell(java.lang.Object)
		 */
		@Override
		public mxRectangle getPreferredSizeForCell(Object cell) {
			if(preferredNodeSize!=null) {
				return new mxRectangle(0, 0, 
						preferredNodeSize.getWidth(), preferredNodeSize.getHeight());
			}
			
			mxRectangle size = null;
			
			if(graphRenderer!=null) {
				size = graphRenderer.getPreferredSizeForCell(GraphPresenter.this, cell);
			}
			
			if(size==null) {
				size = super.getPreferredSizeForCell(cell);
			}
			
			// Adjust size
			if(minimumNodeSize!=null) {
				if(size==null) {
					size = new mxRectangle(0, 0, minimumNodeSize.width, minimumNodeSize.height);
				} else {
					size.setWidth(Math.max(size.getWidth(), minimumNodeSize.getWidth()));
					size.setHeight(Math.max(size.getHeight(), minimumNodeSize.getHeight()));
				}
			}
			if(maximumNodeSize!=null) {
				if(size==null) {
					size = new mxRectangle(0, 0, maximumNodeSize.width, maximumNodeSize.height);
				} else {
					size.setWidth(Math.min(size.getWidth(), maximumNodeSize.getWidth()));
					size.setHeight(Math.min(size.getHeight(), maximumNodeSize.getHeight()));
				}
			}
			
			return size;
		}

		/**
		 * Generates a label text for the given cell.
		 * Asks the current {@code GraphRenderer} of the enclosing
		 * {@code GraphPresenter} if present or if this fails delegates
		 * to the super method.
		 * 
		 * @see GraphRenderer#convertValueToString(GraphOwner, Object)
		 * @see com.mxgraph.view.mxGraph#convertValueToString(java.lang.Object)
		 */
		@Override
		public String convertValueToString(Object cell) {
			String value = null;
			
			if(graphRenderer!=null) {
				value = graphRenderer.convertValueToString(GraphPresenter.this, cell);
			}
			if(value==null) {
				value = super.convertValueToString(cell);
			}
			
			return value;
		}

		/**
		 * Generates a tool-tip text for the given cell.
		 * Asks the current {@code GraphRenderer} of the enclosing
		 * {@code GraphPresenter} if present or if this fails delegates
		 * to the super method. 
		 * 
		 * @see GraphRenderer#getToolTipForCell(GraphOwner, Object)
		 * @see com.mxgraph.view.mxGraph#getToolTipForCell(java.lang.Object)
		 */
		@Override
		public String getToolTipForCell(Object cell) {
			String value = null;
			
			if(graphRenderer!=null) {
				value = graphRenderer.getToolTipForCell(GraphPresenter.this, cell);
			}
			if(value==null) {
				value = UIUtil.toSwingTooltip(super.getToolTipForCell(cell));
			}
			
			return value;
		}
	}
	
	/**
	 * Constant used as action command for default layout combo-box
	 */
	protected static final String SELECT_LAYOUT_COMMAND = "selectLayout"; //$NON-NLS-1$

	/**
	 * Constant used as action command for default style combo-box
	 */
	protected static final String SELECT_STYLE_COMMAND = "selectStyle"; //$NON-NLS-1$

	/**
	 * Constant used as action command for default renderer combo-box
	 */
	protected static final String SELECT_RENDERER_COMMAND = "selectRenderer"; //$NON-NLS-1$
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class DelegatingConnectionHandler extends mxConnectionHandler {

		public DelegatingConnectionHandler() {
			super(GraphPresenter.this);
		}

		/**
		 * Delegates to {@link GraphPresenter#createConnectPreview()}
		 * 
		 * @see GraphPresenter#createConnectPreview()
		 * @see com.mxgraph.swing.handler.mxConnectionHandler#createConnectPreview()
		 */
		@Override
		protected mxConnectPreview createConnectPreview() {
			return GraphPresenter.this.createConnectPreview();
		}
		
		/**
		 * This implementation suppresses any error messages.
		 * 
		 * @see com.mxgraph.swing.handler.mxConnectionHandler#validateConnection(java.lang.Object, java.lang.Object)
		 */
		@Override
		public String validateConnection(Object source, Object target) {
			String result = super.validateConnection(source, target);
			
			if(result==null && GraphUtils.isAncestor(graph.getModel(), source, target, true, false)) {
				result = ""; //$NON-NLS-1$
			}
			
			if(result!=null && !result.isEmpty()) {
				result = ""; //$NON-NLS-1$
			}
			
			return result;
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class DelegatingConnectPreview extends mxConnectPreview {

		public DelegatingConnectPreview() {
			super(GraphPresenter.this);
		}

		@Override
		protected Object createCell(mxCellState startState, String style) {
			mxICell cell = (mxICell) super.createCell(startState, style);
			
			if(graphStyle!=null) {
				cell.setStyle(graphStyle.getStyle(GraphPresenter.this, cell, createStyleOptions()));
			}
			
			if(graphLayout!=null) {
				cell.setStyle(graphLayout.getEdgeStyle(GraphPresenter.this, cell, createLayoutOptions()));
			}
			
			return cell;
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class Handler extends MouseAdapter implements mxIEventListener, MouseWheelListener, 
			ActionListener, ComponentListener, PropertyChangeListener,
			FlavorListener, ChangeListener {

		/**
		 * @see com.mxgraph.util.mxEventSource.mxIEventListener#invoke(java.lang.Object, com.mxgraph.util.mxEventObject)
		 */
		@Override
		public void invoke(Object sender, mxEventObject evt) {
			
			// Refresh highlight info for edges
			if(sender==graph.getSelectionModel()) {
				EdgeHighlightHandler edgeHighlightHandler = getEdgeHighlightHandler();
				if(edgeHighlightHandler!=null) {
					edgeHighlightHandler.reload();
				}
				
				refreshEdges();
			}
			
			// Keep actions synchronized with graph
			refreshActions();
			
			// Keep data synchronized with graph
			if(!isIgnoringGraphChanges() && (sender instanceof mxIGraphModel
					|| sender instanceof mxUndoManager)) {
				// Sync to data
				pauseChangeHandling();
				try {
					syncToData();
				} finally {
					resumeChangeHandling();
				}
			}
			
			if(!isIgnoringModCounts()) {
				modCount++;
			}
		}
		
		protected void maybeShowPopup(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
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
				try {
					if (e.getPreciseWheelRotation() < 0) {
						zoomIn();
					} else {
						zoomOut();
					}
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to handle wheel-zoom command for event: "+e, ex); //$NON-NLS-1$
				}

				e.consume();
			}
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			// Per default the handler is only used as ActionListener
			// on the three combo-boxes for selection of layout, style and renderer
			if(!(e.getSource() instanceof JComboBox)) {
				return;
			}
			
			JComboBox<?> comboBox = (JComboBox<?>)e.getSource();
			Object value = comboBox.getSelectedItem();
			if(!(value instanceof Extension)) {
				return;
			}
			
			try {
				Object instantiatedValue = PluginUtil.instantiate((Extension)value);
				valueSelected(e.getActionCommand(), instantiatedValue);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to handle combo-box selection for command '"+e.getActionCommand()+"' with value: "+value, ex); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		protected void valueSelected(String actionCommand, Object value) {

			switch (actionCommand) {
			case SELECT_LAYOUT_COMMAND:
				setGraphLayout((GraphLayout)value);
				break;

			case SELECT_STYLE_COMMAND:
				setGraphStyle((GraphStyle)value);
				break;

			case SELECT_RENDERER_COMMAND:
				setGraphRenderer((GraphRenderer)value);
				break;

			default:
				LoggerFactory.log(this, Level.SEVERE, 
						"Unknown command for combo-box selection: "+actionCommand); //$NON-NLS-1$
			}
		}

		/**
		 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentResized(ComponentEvent e) {
			if(!isCompressEnabled() && !isAutoZoomEnabled()) {
				return;
			}
			
			try {
				// We need to rebuild cells since previous compression
				// might have collapsed some nodes and tracking them is way
				// more work than simply re-synchronizing the graph
				rebuildGraph();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to handle resize event: "+e, ex); //$NON-NLS-1$
			}
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
			if(!isCompressEnabled() && !isAutoZoomEnabled()) {
				return;
			}
			
			try {
				// We need to rebuild cells since previous compression
				// might have collapsed some nodes and tracking them is way
				// more work than simply re-synchronizing the graph
				rebuildGraph();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to handle show event: "+e, ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentHidden(ComponentEvent e) {
			// no-op
		}

		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getSource() instanceof AnnotationManager) {
				// OBSOLETE
				// Discard all cell states cause highlighters are allowed to alter
				// them during rendering and we want no artifacts
				//getGraph().getView().reload();
				//getGraphControl().repaint();
				
				// Since highlighting of annotations influences so many other
				// operations like layout compression and styling we need to refresh all
				if(isCompressEnabled()) {
					rebuildGraph();
				} else {
					getGraph().getView().reload();
					refreshAll();
				}
				
				return;
			}
			
			if("view".equals(evt.getPropertyName())) { //$NON-NLS-1$
				mxGraphView oldView = (mxGraphView)evt.getOldValue();
				mxGraphView newView = (mxGraphView)evt.getNewValue();
				
				if(oldView!=null) {
					oldView.removeListener(this);
				}
				
				if(newView!=null) {
					newView.addListener(mxEvent.UNDO, this);
				}
			} else if("model".equals(evt.getPropertyName())) { //$NON-NLS-1$
				mxIGraphModel oldModel = (mxIGraphModel)evt.getOldValue();
				mxIGraphModel newModel = (mxIGraphModel)evt.getNewValue();
				
				if(oldModel!=null) {
					oldModel.removeListener(this);
				}
				
				if(newModel!=null) {
					newModel.addListener(mxEvent.UNDO, this);
				}
			} else if("annotationManager".equals(evt.getPropertyName())) { //$NON-NLS-1$
				AnnotationManager oldManager = (AnnotationManager)evt.getOldValue();
				AnnotationManager newManager = (AnnotationManager)evt.getNewValue();
				
				if(oldManager!=null) {
					oldManager.removePropertyChangeListener(this);
				}
				
				if(newManager!=null) {
					newManager.addPropertyChangeListener(this);
				}
			}
		}

		/**
		 * @see java.awt.datatransfer.FlavorListener#flavorsChanged(java.awt.datatransfer.FlavorEvent)
		 */
		@Override
		public void flavorsChanged(FlavorEvent e) {
			refreshActions();
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			// We delay reaction to config changes to give
			// styles and other sub-tools registered to the config
			// registry the chance for proper loading. Otherwise
			// they would all call subsequent refreshes while other tools
			// are still accessing the config data.
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					graph.getView().reload();
					
					refreshAll();
				}
			});
		}
	}
	
	protected class LayoutChange extends mxAtomicGraphModelChange {
		
		protected GraphLayout layout, previous;
		
		public LayoutChange(GraphLayout layout) {
			this.layout = layout;
			this.previous = this.layout;
		}

		/**
		 * @see com.mxgraph.util.mxUndoableEdit.mxUndoableChange#execute()
		 */
		@Override
		public void execute() {
			layout = previous;
			previous = graphLayout;
			
			if(graphLayout!=null) {
				graphLayout.uninstall(GraphPresenter.this);
			}
			
			graphLayout = layout;
			
			if(graphLayout!=null) {
				graphLayout.install(GraphPresenter.this);
			}
			
			refreshAll();
					
			firePropertyChange("graphLayout", previous, layout); //$NON-NLS-1$
		}
		
	}
	
	protected class RendererChange extends mxAtomicGraphModelChange {
		
		protected GraphRenderer renderer, previous;
		
		public RendererChange(GraphRenderer renderer) {
			this.renderer = renderer;
			this.previous = this.renderer;
		}

		/**
		 * @see com.mxgraph.util.mxUndoableEdit.mxUndoableChange#execute()
		 */
		@Override
		public void execute() {
			renderer = previous;
			previous = graphRenderer;

			
			if(graphRenderer!=null) {
				graphRenderer.uninstall(GraphPresenter.this);
			}
			
			graphRenderer = renderer;
			
			if(graphRenderer!=null) {
				graphRenderer.install(GraphPresenter.this);
			}
			
			setCanvas(renderer);
			
			repaint();

			firePropertyChange("graphRenderer", previous, renderer); //$NON-NLS-1$
		}
		
	}
	
	protected class StyleChange extends mxAtomicGraphModelChange {
		
		protected GraphStyle style, previous;
		
		public StyleChange(GraphStyle style) {
			this.style = style;
			this.previous = this.style;
		}

		/**
		 * @see com.mxgraph.util.mxUndoableEdit.mxUndoableChange#execute()
		 */
		@Override
		public void execute() {
			style = previous;
			previous = graphStyle;
			
			
			if(graphStyle!=null) {
				graphStyle.uninstall(GraphPresenter.this);
			}
			
			graphStyle = style;
			
			if(graphStyle!=null) {
				graphStyle.install(GraphPresenter.this);
			}
			
			graph.setStylesheet(style.createStylesheet(GraphPresenter.this, createStyleOptions()));
			
			refreshAll();
			
			firePropertyChange("graphStyle", previous, style); //$NON-NLS-1$
		}
		
	}
	
	protected class GraphConfigDelegate extends ConfigDelegate {
		
		public GraphConfigDelegate(Handle handle) {
			super(handle);
		}

		public GraphConfigDelegate(String path, ConfigRegistry registry) {
			super(path, registry);
		}

		/**
		 * @see de.ims.icarus.config.ConfigDelegate#getValue(java.lang.String)
		 */
		@Override
		public Object getValue(String name) {
			Handle handle = getHandle();
			ConfigRegistry registry = getHandle().getSource();
			handle = registry.getChildHandle(handle, name);
			return handle==null ? null : registry.getValue(handle);
		}

		@Override
		public void reload() {
			// Only load general graph properties here
			// All layout/style related fields should only be read
			// by GraphStyle or GraphLayout implementations!
			
			setGridVisible(getBoolean("gridVisible")); //$NON-NLS-1$
			getGraph().setGridEnabled(getBoolean("gridEnabled")); //$NON-NLS-1$
			getGraph().setGridSize(getInteger("gridSize")); //$NON-NLS-1$
			setGridStyle(getInteger("gridStyle")); //$NON-NLS-1$
			setGridColor(new Color(getInteger("gridColor"))); //$NON-NLS-1$
			
			// Highlighting of incoming/outgoing edges is a basic graph
			// presenter feature and as such the related properties are
			// stored and processed on the presenter itself rather then
			// on GraphStyle or GraphLayout implementations! 
			setIncomingEdgeColor(new Color(getInteger("incomingEdgeColor"))); //$NON-NLS-1$
			setOutgoingEdgeColor(new Color(getInteger("outgoingEdgeColor"))); //$NON-NLS-1$
			
			// Those values should only be regarded as "default startup" values
			// so no need to mess with users current decision on graph level!
			//setAutoZoomEnabled(getValue("autoZoom", isAutoZoomEnabled())); //$NON-NLS-1$
			//setCompressEnabled(getValue("compressGraph", isCompressEnabled())); //$NON-NLS-1$
		}
	}
	
	public class EdgeHighlightHandler {
		protected Set<Object> outgoing = new HashSet<>();
		protected Set<Object> incoming = new HashSet<>();
		
		protected void reload() {
			outgoing.clear();
			incoming.clear();
			if(graph==null || graph.getSelectionCount()==0) {
				return;
			}
			
			if(!isMarkIncomingEdges() && !isMarkOutgoingEdges()) {
				return;
			}
			
			Object[] vertices = getSelectionVertices();
			if(vertices==null || vertices.length==0) {
				return;
			}
			
			Set<Object> selection = CollectionUtils.asSet(vertices);
			
			mxIGraphModel model = graph.getModel();
			for(Object edge : graph.getAllEdges(vertices)) {
				boolean isIncoming = selection.contains(model.getTerminal(edge, false));
				if(isMarkIncomingEdges() && isIncoming) {
					incoming.add(edge);
				} else if(isMarkOutgoingEdges() && !isIncoming) {
					outgoing.add(edge);
				}
			}
		}
		
		public boolean isOutgoingHighlighted(Object edge) {
			return outgoing.contains(edge);
		}
		
		public boolean isIncomingHighlighted(Object edge) {
			return incoming.contains(edge);
		}
	}
	
	/**
	 * Central bridging point for the action callback framework
	 * of class {@link ActionManager} and the actual handling methods in
	 * {@link GraphPresenter}.
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public class CallbackHandler {
		
		protected CallbackHandler() {
			// no-op
		}
		
		/**
		 * @see GraphPresenter#cloneCells(Object[])
		 */
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
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to clone cells", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#deleteCells(Object[])
		 */
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
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to delete cells", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#moveCells(Object[], double, double)
		 */
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
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to move cells", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#pasteCells()
		 */
		public void pasteCells(ActionEvent e) {
			try {
				GraphPresenter.this.pasteCells();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to paste cells", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#copyCells(Object[])
		 */
		public void copyCells(ActionEvent e) {
			if(graph.getSelectionCount()==0) {
				return;
			}
						
			try {
				GraphPresenter.this.copyCells();
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to copy cells", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#editCell(Object)
		 */
		public void editCell(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			Object cell = graph.getSelectionCell();
			if(cell==null) {
				return;
			}
			
			try {
				GraphPresenter.this.editCell(cell);
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to edit cell: "+cell, ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#clearGraph()
		 */
		public void clearGraph(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				GraphPresenter.this.clearGraph();
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to clear graph", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#exportCells(Object[])
		 */
		public void exportCells(ActionEvent e) {
			try {
				Object[] cells = getSelectionCells();
				if(cells==null || cells.length==0) {
					cells = graph.getChildCells(graph.getDefaultParent());
				}
				
				JFileChooser fileChooser = new JFileChooser();
				// Setting up Filter for File extensions
				FileFilter defaultFilter = new DefaultFileFilter(
						".xml",	ResourceManager.getInstance().get( //$NON-NLS-1$
								"plugins.jgraph.graphPresenter.fileTypes.xml")); //$NON-NLS-1$
				fileChooser.addChoosableFileFilter(defaultFilter);
				fileChooser.addChoosableFileFilter(new DefaultFileFilter(
						".svg",	ResourceManager.getInstance().get( //$NON-NLS-1$
								"plugins.jgraph.graphPresenter.fileTypes.svg"))); //$NON-NLS-1$
				fileChooser.addChoosableFileFilter(new DefaultFileFilter(
						".png", ResourceManager.getInstance().get( //$NON-NLS-1$
								"plugins.jgraph.graphPresenter.fileTypes.png"))); //$NON-NLS-1$
				fileChooser.addChoosableFileFilter(new DefaultFileFilter.ImageFileFilter(
						ResourceManager.getInstance().get(
								"plugins.jgraph.graphPresenter.fileTypes.allImages"))); //$NON-NLS-1$
				fileChooser.setFileFilter(defaultFilter);
				fileChooser.setFileFilter(defaultFilter);
				
				fileChooser.setCurrentDirectory(Core.getCore().getDataFolder());
				fileChooser.setApproveButtonText(ResourceManager.getInstance().get("save")); //$NON-NLS-1$
				fileChooser.setDialogTitle(ResourceManager.getInstance().get("plugins.jgraph.graphPresenter.messages.export")); //$NON-NLS-1$

				String filename = null;
				FileFilter selectedFilter = null;

				if (fileChooser.showDialog(null, null) != JFileChooser.APPROVE_OPTION) {
					return;
				}

				filename = fileChooser.getSelectedFile().getAbsolutePath();
				selectedFilter = fileChooser.getFileFilter();

				// Append file extension if missing in name
				if (selectedFilter instanceof DefaultFileFilter) {
					String ext = ((DefaultFileFilter) selectedFilter).getExtension();
					if (!filename.toLowerCase().endsWith(ext)) {
						filename += ext;
					}
				}

				// Overwrite if already existing?
				if (new File(filename).exists()
						&& !DialogFactory.getGlobalFactory().showConfirm(null, 
								"plugins.jgraph.graphPresenter.messages.export",  //$NON-NLS-1$
								"plugins.jgraph.graphPresenter.messages.overwriteExisting")) { //$NON-NLS-1$
					return;
				}

				String extension = filename.substring(filename.lastIndexOf('.') + 1);

				if (extension.equalsIgnoreCase("svg")) { //$NON-NLS-1$
					
					// SVG
					mxUtils.writeFile(mxXmlUtils.getXml(mxCellRenderer
							.createSvgDocument(graph, cells.length==0 ? null : cells, 1, null, null)
							.getDocumentElement()), filename);
				} else if (extension.equalsIgnoreCase("xml")) { //$NON-NLS-1$
					
					// XML
					CellBuffer buffer = GraphPresenter.this.exportCells(cells);
					if(buffer==null || buffer.isEmpty()) {
						return;
					}

					JAXBContext context = JAXBUtils.getSharedJAXBContext();
					Marshaller marshaller = context.createMarshaller();
					marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					marshaller.marshal(buffer, new File(filename));
				} else {
					
					// IMAGE
					Color bg = Color.white;
					// Set Picture Background transparent?
					if (extension.equalsIgnoreCase("png") //$NON-NLS-1$
							&& DialogFactory.getGlobalFactory().showConfirm(null, 
									"plugins.jgraph.graphPresenter.messages.export",  //$NON-NLS-1$
									"plugins.jgraph.graphPresenter.messages.transparentBackground")) { //$NON-NLS-1$
						bg = new Color(0, 0, 0, 0);
					}

					BufferedImage image = mxCellRenderer.createBufferedImage(
							graph, cells, 1, bg, true,
							graph.getBoundingBox(graph.getCurrentRoot()), 
							getCanvas());

					if (image != null) {
						ImageIO.write(image, extension, new File(filename));
					} else {
						DialogFactory.getGlobalFactory().showError(null, 
								"plugins.jgraph.graphPresenter.messages.export",  //$NON-NLS-1$
								"plugins.jgraph.graphPresenter.messages.invalidImage"); //$NON-NLS-1$
					}
				}
				
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
					"Failed to export cells", ex); //$NON-NLS-1$
				
				DialogFactory.getGlobalFactory().showError(null, 
					"plugins.jgraph.graphPresenter.messages.export",  //$NON-NLS-1$
					"plugins.jgraph.graphPresenter.messages.exportFailed", //$NON-NLS-1$
					ex.getMessage());
			}
		}

		/**
		 * @see GraphPresenter#importCells(CellBuffer)
		 */
		public void importCells(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				JFileChooser fileChooser = new JFileChooser();
				// Setting up Filter for File extensions
				FileFilter defaultFilter = new DefaultFileFilter(
						".xml",	ResourceManager.getInstance().get( //$NON-NLS-1$
								"plugins.jgraph.graphPresenter.fileTypes.xml")); //$NON-NLS-1$
				fileChooser.addChoosableFileFilter(defaultFilter);
				fileChooser.setFileFilter(defaultFilter);
				
				fileChooser.setCurrentDirectory(Core.getCore().getDataFolder());
				fileChooser.setApproveButtonText(ResourceManager.getInstance().get("open")); //$NON-NLS-1$
				fileChooser.setDialogTitle(ResourceManager.getInstance().get("plugins.jgraph.graphPresenter.messages.import")); //$NON-NLS-1$
	
				if (fileChooser.showDialog(null, null) != JFileChooser.APPROVE_OPTION) {
					return;
				}
				
				String extension = fileChooser.getSelectedFile().getAbsolutePath().toLowerCase();
				
				if (extension.endsWith(".xml")) { //$NON-NLS-1$

					JAXBContext context = JAXBUtils.getSharedJAXBContext();
					Unmarshaller unmarshaller = context.createUnmarshaller();
					
					CellBuffer buffer = (CellBuffer) unmarshaller.unmarshal(fileChooser.getSelectedFile());
					
					GraphPresenter.this.importCells(buffer);
				} else {
					DialogFactory.getGlobalFactory().showError(null, 
						"plugins.jgraph.graphPresenter.messages.import",  //$NON-NLS-1$
						"plugins.jgraph.graphPresenter.messages.invalidFile", //$NON-NLS-1$
						extension);
				}
				
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
					"Failed to import cells", ex); //$NON-NLS-1$
				
				DialogFactory.getGlobalFactory().showError(null, 
					"plugins.jgraph.graphPresenter.messages.import",  //$NON-NLS-1$
					"plugins.jgraph.graphPresenter.messages.importFailed", //$NON-NLS-1$
					ex.getMessage());
			}
		}

		/**
		 * @see GraphPresenter#printGraph()
		 */
		public void printGraph(ActionEvent e) {			
			try {
				GraphPresenter.this.printGraph();
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add node", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#addNode()
		 */
		public void addNode(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				GraphPresenter.this.addNode();
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add node", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#addEdge(Object, Object, boolean)
		 */
		public void addEdge(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				addEdge(e, false);
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add edge", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#addEdge(Object, Object, boolean)
		 */
		public void addOrderEdge(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				addEdge(e, true);
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add order edge", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#addEdge(Object, Object, boolean)
		 */
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

		/**
		 * @see GraphPresenter#undo()
		 */
		public void undoEdit(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				GraphPresenter.this.undo();
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to undo edit", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#redo()
		 */
		public void redoEdit(ActionEvent e) {
			if(!canEdit()) {
				return;
			}
			
			try {
				GraphPresenter.this.redo();
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed redo edit", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#zoomIn()
		 */
		public void zoomIn(ActionEvent e) {
			try {
				GraphPresenter.this.zoomIn();
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to zoom in", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#zoomOut()
		 */
		public void zoomOut(ActionEvent e) {
			try {
				GraphPresenter.this.zoomOut();
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to zoom out", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#zoomActual()
		 */
		public void resetZoom(ActionEvent e) {
			try {
				GraphPresenter.this.zoomActual();
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to reset zoom", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#openConfig()
		 */
		public void openPreferences(ActionEvent e) {
			try {
				GraphPresenter.this.openConfig();
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to open preferences", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#rebuildGraph()
		 */
		public void rebuildGraph(ActionEvent e) {
			try {
				GraphPresenter.this.rebuildGraph();
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to rebuild graph", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#collapseCells(Object[])
		 */
		public void collapseCells(ActionEvent e) {			
			if(graph.getSelectionCount()==0) {
				return;
			}
			
			Object[] cells = getSelectionVertices();
			if(cells.length==0) {
				return;
			}
			
			try {
				GraphPresenter.this.collapseCells(cells);
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to collapse cells", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see GraphPresenter#expandCells(Object[])
		 */
		public void expandCells(ActionEvent e) {			
			if(graph.getSelectionCount()==0) {
				return;
			}
			
			Object[] cells = getSelectionVertices();
			if(cells.length==0) {
				return;
			}
			
			try {
				GraphPresenter.this.expandCells(cells);
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to expand cells", ex); //$NON-NLS-1$
			}
		}

		public void toggleAutoZoom(ActionEvent e) {
			// no-op
		}

		/**
		 * @see GraphPresenter#setAutoZoomEnabled(boolean)
		 * @see GraphPresenter#isAutoZoomEnabled()
		 */
		public void toggleAutoZoom(boolean b) {
			try {
				setAutoZoomEnabled(b);
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'autoZoomEnabled' state", ex); //$NON-NLS-1$
			}
		}

		public void toggleCompress(ActionEvent e) {
			// no-op
		}

		/**
		 * @see GraphPresenter#setCompressEnabled(boolean)
		 * @see GraphPresenter#isCompressEnabled()
		 */
		public void toggleCompress(boolean b) {
			try {
				setCompressEnabled(b);
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'compressEnabled' state", ex); //$NON-NLS-1$
			}
		}

		public void toggleMarkIncoming(ActionEvent e) {
			// no-op
		}

		/**
		 * @see GraphPresenter#setMarkIncomingEdges(boolean)
		 * @see GraphPresenter#isMarkIncomingEdges()
		 */
		public void toggleMarkIncoming(boolean b) {
			try {
				setMarkIncomingEdges(b);
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'markIncoming' state", ex); //$NON-NLS-1$
			}
		}

		public void toggleMarkOutgoing(ActionEvent e) {
			// no-op
		}

		/**
		 * @see GraphPresenter#setMarkOutgoingEdges(boolean)
		 * @see GraphPresenter#isMarkOutgoingEdges()
		 */
		public void toggleMarkOutgoing(boolean b) {
			try {
				setMarkOutgoingEdges(b);
			} catch(Exception ex) {
				UIUtil.beep();
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'markOutgoing' state", ex); //$NON-NLS-1$
			}
		}
	}
	
	/*protected class GraphTransferHandler extends mxGraphTransferHandler {

		@Override
		public void setDragImage(Image img) {
			try {
				System.out.println("setDragImage: img="+String.valueOf(img));
				super.setDragImage(img);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public Image getDragImage() {
			Image img = super.getDragImage();
			System.out.println("getDragImage: img="+String.valueOf(img));
			
			return img;
		}

		@Override
		public void setDragImageOffset(Point p) {
			try {
				System.out.println("setDragImageOffset: p="+String.valueOf(p));
				super.setDragImageOffset(p);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public Point getDragImageOffset() {
			Point p = super.getDragImageOffset();
			System.out.println("getDragImageOffset: img="+String.valueOf(p));
			
			return p;
		}

		@Override
		public void exportAsDrag(JComponent comp, InputEvent e, int action) {
			try {
				System.out.printf("exportAsDrag: comp=%s event=%s action=%d\n",
						String.valueOf(comp), String.valueOf(e), action);
				super.exportAsDrag(comp, e, action);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void exportToClipboard(JComponent comp, Clipboard clip,
				int action) throws IllegalStateException {
			try {
				System.out.printf("exportToClipboard: comp=%s clip=%s action=%d\n",
						String.valueOf(comp), String.valueOf(clip), action);
				super.exportToClipboard(comp, clip, action);
			} catch(IllegalStateException ex) {
				ex.printStackTrace();
				throw ex;
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public boolean importData(TransferSupport support) {
			boolean result =  super.importData(support);
			
			System.out.printf("importData: support=%s result=%b\n",
					String.valueOf(support), result);
			
			return result;
		}

		@Override
		public boolean canImport(TransferSupport support) {
			boolean result = super.canImport(support);

			System.out.printf("canImport: support=%s result=%b\n",
					String.valueOf(support), result);
			
			return result;
		}

		@Override
		public Icon getVisualRepresentation(Transferable t) {
			Icon icon = super.getVisualRepresentation(t);
			
			System.out.printf("getVisualRepresentation: transferable=%s icon=%s\n",
					String.valueOf(t), String.valueOf(icon));
			
			return icon;
		}
		
	}*/
	
	/*protected class GraphTransferable extends mxGraphTransferable {

		public GraphTransferable(Object[] cells, mxRectangle bounds,
				ImageIcon image) {
			super(cells, bounds, image);
		}

		@Override
		protected DataFlavor[] getRicherFlavors() {
			// TODO Auto-generated method stub
			return super.getRicherFlavors();
		}
		
	}*/
	
	static {
		 try {
			mxGraphTransferable.dataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
					+ "; class=com.mxgraph.swing.util.mxGraphTransferable", null, //$NON-NLS-1$
					mxGraphTransferable.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			LoggerFactory.log(GraphPresenter.class, Level.SEVERE, 
					"Failed to adjust class-loader of DnD support", e); //$NON-NLS-1$
		}
	}
}
