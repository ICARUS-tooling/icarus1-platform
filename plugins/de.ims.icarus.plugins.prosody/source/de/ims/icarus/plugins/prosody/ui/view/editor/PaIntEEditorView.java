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
package de.ims.icarus.plugins.prosody.ui.view.editor;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

import javax.activation.DataHandler;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEParamsWrapper;
import de.ims.icarus.plugins.prosody.painte.PaIntEUtils;
import de.ims.icarus.plugins.prosody.ui.geom.Axis;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEIcon;
import de.ims.icarus.plugins.prosody.ui.helper.PaIntEParamsTableCellRenderer;
import de.ims.icarus.plugins.prosody.ui.view.editor.PaIntERegistry.PaIntERegistryTableModel;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.NumberDocument;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.events.ChangeSource;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.events.ListenerProxies;
import de.ims.icarus.ui.table.TooltipTableCellRenderer;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEEditorView extends View {

	private static final int PARAM_A1 = 0;
	private static final int PARAM_A2 = 1;
	private static final int PARAM_B = 2;
	private static final int PARAM_C1 = 3;
	private static final int PARAM_C2 = 4;
	private static final int PARAM_D = 5;
	private static final int PARAM_ALIGNMENT = 6;

	private CallbackHandler callbackHandler;
	private Handler handler;

	private GraphComponent graphComponent;

	private final List<ParamsPanel> paramsPanels = new ArrayList<>();
	// Panel the current popup menu belongs to
	private transient ParamsPanel owningPanel;
	private Box paramsComponent;

	private JTable paramsTable;
	private PaIntERegistryTableModel paramsTableModel;

	private TransferManager transferManager;

	private JPopupMenu popupMenu;

	private boolean paintAllCompact = false;

	private static final String[] paramIds = {
		"a1", //$NON-NLS-1$
		"a2", //$NON-NLS-1$
		"b", //$NON-NLS-1$
		"c1", //$NON-NLS-1$
		"c2", //$NON-NLS-1$
		"d", //$NON-NLS-1$
		"alignment", //$NON-NLS-1$
	};

	private static final String configPath = "plugins.prosody.appearance.painteEditor"; //$NON-NLS-1$

	private static final Color[] defaultColors = {
		Color.black,
		Color.green,
		Color.blue,
		Color.red,
		Color.magenta,
		Color.orange,
		Color.pink,
		Color.yellow,
	};

	public PaIntEEditorView() {
		// no-op
	}

	@Override
	public void init(JComponent container) {

		// Load actions
		if (!defaultLoadActions(PaIntEEditorView.class,
				"painte-editor-view-actions.xml")) { //$NON-NLS-1$
			return;
		}

		// Init ui
		container.setLayout(new BorderLayout());

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard!=null) {
			clipboard.addFlavorListener(ListenerProxies.getProxy(FlavorListener.class, getHandler()));
		}

		transferManager = new TransferManager();

		graphComponent = new GraphComponent();
		graphComponent.setBorder(new EmptyBorder(10, 10, 10, 10));
		graphComponent.setTransferHandler(transferManager);

		paramsComponent = Box.createHorizontalBox();
		paramsComponent.add(Box.createHorizontalGlue());

		JScrollPane paramsScrollPane = new JScrollPane(paramsComponent);
		paramsScrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(paramsScrollPane);

		JSplitPane upperSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, graphComponent, paramsScrollPane);
		upperSplitPane.setBorder(null);
		upperSplitPane.setResizeWeight(0);
		upperSplitPane.setDividerLocation(300);

		PaIntEParamsTableCellRenderer renderer = new PaIntEParamsTableCellRenderer(graphComponent.getGraph());
		int iconCellWidth = renderer.getPaIntEIcon().getIconWidth()+2;
		int iconCellHeight = renderer.getPaIntEIcon().getIconHeight()+2;

		paramsTableModel = PaIntERegistry.getInstance().createTableModel();
		paramsTableModel.install(this);
		paramsTable = new JTable(paramsTableModel);
		paramsTable.setColumnSelectionAllowed(false);
		paramsTable.setDragEnabled(true);
		paramsTable.addMouseListener(getHandler());
		paramsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paramsTable.setDefaultRenderer(PaIntEParamsWrapper.class, renderer);
		paramsTable.setDefaultRenderer(String.class, new TooltipTableCellRenderer());
		paramsTable.setRowHeight(iconCellHeight);
		paramsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		paramsTable.setDragEnabled(true);
		paramsTable.setTransferHandler(transferManager);
		paramsTable.getSelectionModel().addListSelectionListener(getHandler());
		UIUtil.enableRighClickTableSelection(paramsTable);

		TableColumn iconColumn = paramsTable.getColumnModel().getColumn(1);
		iconColumn.setMinWidth(iconCellWidth);
		iconColumn.setWidth(iconCellWidth);

		JScrollPane lowerScrollPane = new JScrollPane(paramsTable);
		lowerScrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(lowerScrollPane);

		JSplitPane globalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, upperSplitPane, lowerScrollPane);
		globalSplitPane.setBorder(UIUtil.topLineBorder);
//		globalSplitPane.setResizeWeight(0.7);

		container.add(globalSplitPane, BorderLayout.CENTER);

		JToolBar toolBar = createToolBar();
		if(toolBar!=null) {
			container.add(toolBar, BorderLayout.NORTH);
		}

		addParamsPanel(null);

		registerActionCallbacks();

		refreshActions();

		ConfigRegistry.getGlobalRegistry().addGroupListener(configPath, getHandler());
		PaIntERegistry.getInstance().addListener(null, getHandler());

		reloadConfig();
	}

	public void refresh() {
		refreshParamComponents();
	}

	@Override
	public void close() {
		super.close();

		paramsTableModel.uninstall(this);

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard!=null) {
			clipboard.removeFlavorListener(ListenerProxies.getProxy(FlavorListener.class, getHandler()));
		}

		PaIntERegistry.getInstance().removeListener(getHandler());
	}

	private JToolBar createToolBar() {
		return getDefaultActionManager().createToolBar(
				"plugins.prosody.painteEditorView.toolBarList", null); //$NON-NLS-1$
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

		// GENERAL ACTIONS

		actionManager.addHandler("plugins.prosody.painteEditorView.openPreferencesAction", //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.removePainteParamsAction", //$NON-NLS-1$
				callbackHandler, "removePainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.renamePainteParamsAction", //$NON-NLS-1$
				callbackHandler, "renamePainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.usePainteParamsAction", //$NON-NLS-1$
				callbackHandler, "usePainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.clearParamsRegistryAction", //$NON-NLS-1$
				callbackHandler, "clearParamsRegistry"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.importParamsRegistryAction", //$NON-NLS-1$
				callbackHandler, "importParamsRegistry"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.exportParamsRegistryAction", //$NON-NLS-1$
				callbackHandler, "exportParamsRegistry"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.copyPainteParamsAction", //$NON-NLS-1$
				callbackHandler, "copyPainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.copyPainteParamsIdAction", //$NON-NLS-1$
				callbackHandler, "copyPainteParamsId"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.copyPainteParamsCompactAction", //$NON-NLS-1$
				callbackHandler, "copyPainteParamsCompact"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.pastePainteParamsAction", //$NON-NLS-1$
				callbackHandler, "pastePainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.collapseAllPanelsAction", //$NON-NLS-1$
				callbackHandler, "collapseAllPanels"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.expandAllPanelsAction", //$NON-NLS-1$
				callbackHandler, "expandAllPanels"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.togglePaintAllCompactAction", //$NON-NLS-1$
				callbackHandler, "togglePaintAllCompact"); //$NON-NLS-1$

		// PANEL ACTIONS
		actionManager.addHandler("plugins.prosody.painteEditorView.addParamsPanelAction", //$NON-NLS-1$
				callbackHandler, "addParamsPanel"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.removeParamsPanelAction", //$NON-NLS-1$
				callbackHandler, "removeParamsPanel"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.renameParamsPanelAction", //$NON-NLS-1$
				callbackHandler, "renameParamsPanel"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.clearParamsPanelAction", //$NON-NLS-1$
				callbackHandler, "clearParamsPanel"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.importParamsPanelAction", //$NON-NLS-1$
				callbackHandler, "importParamsPanel"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.exportParamsPanelAction", //$NON-NLS-1$
				callbackHandler, "exportParamsPanel"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.exportParamsPanelCompactAction", //$NON-NLS-1$
				callbackHandler, "exportParamsPanelCompact"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.exportParamsPanelIdAction", //$NON-NLS-1$
				callbackHandler, "exportParamsPanelId"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.saveParamsPanelAction", //$NON-NLS-1$
				callbackHandler, "saveParamsPanel"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.saveParamsPanelAsAction", //$NON-NLS-1$
				callbackHandler, "saveParamsPanelAs"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.undoParamsPanelAction", //$NON-NLS-1$
				callbackHandler, "undoParamsPanel"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.redoParamsPanelAction", //$NON-NLS-1$
				callbackHandler, "redoParamsPanel"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.colorParamsPanelAction", //$NON-NLS-1$
				callbackHandler, "colorParamsPanel"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.toggleParamsPanelPaintCompactAction", //$NON-NLS-1$
				callbackHandler, "toggleParamsPanelPaintCompact"); //$NON-NLS-1$
	}

	private void refreshActions() {
		ActionManager actionManager = getDefaultActionManager();

		int index = paramsTable.getSelectedRow();
		int registrySize = paramsTableModel.getRowCount();

		boolean hasSelection = index!=-1;
		boolean hasRegistry = registrySize>0;
		boolean multipleParams = paramsPanels.size()>0; //TODO temporary fix, amybe change abck to 1?

		boolean pasteable = isPaIntEClipboardContent();

		actionManager.setEnabled(hasSelection,
				"plugins.prosody.painteEditorView.removePainteParamsAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.renamePainteParamsAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.copyPainteParamsAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.copyPainteParamsCompactAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.copyPainteParamsIdAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.usePainteParamsAction"); //$NON-NLS-1$
		actionManager.setEnabled(hasRegistry,
				"plugins.prosody.painteEditorView.exportParamsRegistryAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.clearParamsRegistryAction"); //$NON-NLS-1$
		actionManager.setEnabled(multipleParams,
				"plugins.prosody.painteEditorView.collapseAllPanelsAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.expandAllPanelsAction"); //$NON-NLS-1$
		actionManager.setEnabled(pasteable,
				"plugins.prosody.painteEditorView.pastePainteParamsAction"); //$NON-NLS-1$
	}

	private boolean isPaIntEClipboardContent() {
		try {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t = clipboard.getContents(this);
			if(t!=null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				new PaIntEConstraintParams((String) t.getTransferData(DataFlavor.stringFlavor));
				return true;
			}
		} catch(Exception e) {
			//ignore
		}

		return false;
	}

	private Handle getConfigHandle() {
		return ConfigRegistry.getGlobalRegistry().getHandle(configPath);
	}

	private void reloadConfig() {
		Handle handle = getConfigHandle();
		graphComponent.reloadConfig(handle);

		for(ParamsPanel panel : paramsPanels) {
			panel.reloadConfig(handle);
		}

		refreshGraph();
	}

	private ParamsPanel addParamsPanel(ParamsPanel source) {
		ParamsPanel panel = new ParamsPanel(getUnusedDefaultColor());

		panel.reloadConfig(getConfigHandle());

		int index = paramsComponent.getComponentCount()-1;

		if(source!=null) {
			index = paramsPanels.indexOf(source)+1;
			panel.setParams(source.getParams());
		}

		paramsPanels.add(index, panel);
		paramsComponent.add(panel, index);

		panel.refresh();

		refreshParamComponents();
		refreshActions();

		return panel;
	}

	private void removeParamsPanel(ParamsPanel panel) {
		paramsPanels.remove(panel);
		paramsComponent.remove(panel);

		paramsComponent.revalidate();
		paramsComponent.repaint();

		refreshParamComponents();
		refreshActions();
	}

	private PaIntEParamsWrapper getSelectedWrapper() {
		int row = paramsTable.getSelectedRow();
		return row==-1 ? null : paramsTableModel.getItem(row);
	}

	private void editSelectedParamsDescription() {
		PaIntEParamsWrapper params = getSelectedWrapper();
		if(params==null) {
			return;
		}

		String newDescription = DialogFactory.getGlobalFactory().showTextInputDialog(getFrame(),
				"plugins.prosody.painteEditorView.dialogs.editParamsDescription.title",  //$NON-NLS-1$
				"plugins.prosody.painteEditorView.dialogs.editParamsDescription.message",  //$NON-NLS-1$
				params.getDescription(), params.getName());

		if(newDescription==null) {
			return;
		}

		PaIntERegistry.getInstance().editParamsDescription(params, newDescription);
	}

	private void toggleSelectedParamsCompact() {
		PaIntEParamsWrapper params = getSelectedWrapper();
		if(params==null) {
			return;
		}

		boolean compact = params.isCompact();

		PaIntERegistry.getInstance().setCompact(params, !compact);
	}

	private void loadSelectedParams() {
		PaIntEParamsWrapper params = getSelectedWrapper();
		if(params==null) {
			return;
		}

		addParamsPanel(null).setWrapper(params);

		refreshActions();
		refreshParamComponents();
	}

	private void refreshGraph() {
		if(graphComponent==null) {
			return;
		}

		graphComponent.refresh();
	}

	private Color getUnusedDefaultColor() {
		Set<Color> usedColors = new HashSet<>();

		for(ParamsPanel panel : paramsPanels) {
			usedColors.add(panel.color());
		}

		for(Color color : defaultColors) {
			if(!usedColors.contains(color)) {
				return color;
			}
		}

		return Color.black;
	}

	private void refreshParamComponents() {
		refreshParamComponents(null);
	}

	private void refreshParamComponents(PaIntEParamsWrapper wrapper) {

		for(ParamsPanel panel : paramsPanels) {
			if(wrapper!=null && panel.getWrapper()==wrapper) {
				panel.syncToBuffer();
			}
			panel.refresh();
		}

		refreshGraph();
	}

	public void expandParamsPanels(boolean expanded) {
		for(ParamsPanel panel : paramsPanels) {
			panel.setExpandedState(expanded);
		}
	}

	private ParamsPanel getOwningPanel(AWTEvent e) {
		if(owningPanel!=null) {
			return owningPanel;
		}

		Object source = e.getSource();
		if(source instanceof Component) {
			return (ParamsPanel) SwingUtilities.getAncestorOfClass(ParamsPanel.class, (Component) source);
		}

		return null;
	}

	private void pasteParams(ParamsPanel panel) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard==null) {
			return;
		}

		String text = null;

		try {
			Transferable t = clipboard.getContents(this);
			if(t==null || !t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return;
			}

			text = (String) t.getTransferData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			LoggerFactory.error(this, "Failed to fetch string content of clipboard", e); //$NON-NLS-1$
		}

		if(text==null) {
			return;
		}

		PaIntEConstraintParams params = null;

		try {
			params = new PaIntEConstraintParams(text);
		} catch(Exception e) {
			//ignore invalid formats
		}

		if(params==null) {
			return;
		}

		if(panel==null) {
			panel = addParamsPanel(null);
		}

		panel.setParams(params);
		panel.clearHistory();
	}

	private void showPopup(MouseEvent trigger) {
		if(popupMenu==null) {
			// Create new popup menu

			Options options = null;
			popupMenu = getDefaultActionManager().createPopupMenu(
					"plugins.prosody.painteEditorView.popupMenuList", options); //$NON-NLS-1$

			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}

		if(popupMenu!=null) {
			refreshActions();

			popupMenu.show(paramsTable, trigger.getX(), trigger.getY());
		}
	}

	private static PaIntEParams extractParams(Object obj) {
		if(obj instanceof String) {
			return PaIntEParams.parsePaIntEParams((String) obj);
		} else if(obj instanceof PaIntEParams) {
			return (PaIntEParams) obj;
		} else if(obj instanceof PaIntEParamsWrapper) {
			return ((PaIntEParamsWrapper)obj).getParams();
		} else {
			return null;
		}
	}

	private static PaIntEParamsWrapper extractWrapper(Object obj) {
		if(obj instanceof PaIntEParamsWrapper) {
			// Deserialization during transfer yields a new instance -> intern to ensure consistency!
			return PaIntERegistry.getInstance().intern((PaIntEParamsWrapper) obj);
		} else {
			return null;
		}
	}

	private class Handler extends MouseAdapter implements ConfigListener, ListSelectionListener, FlavorListener, EventListener {

		private void maybeShowPopupMenu(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		/**
		 * @see de.ims.icarus.config.ConfigListener#invoke(de.ims.icarus.config.ConfigRegistry, de.ims.icarus.config.ConfigEvent)
		 */
		@Override
		public void invoke(ConfigRegistry sender, ConfigEvent event) {
			reloadConfig();
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {

			if(e.getClickCount()==2) {

				int columnIndex = paramsTable.columnAtPoint(e.getPoint());

				if(columnIndex==3) {
					editSelectedParamsDescription();
				} else if(columnIndex!=2) {
					loadSelectedParams();
				}
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopupMenu(e);
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopupMenu(e);
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			refreshActions();
		}

		/**
		 * @see java.awt.datatransfer.FlavorListener#flavorsChanged(java.awt.datatransfer.FlavorEvent)
		 */
		@Override
		public void flavorsChanged(FlavorEvent e) {
			refreshActions();
		}

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {

			if(Events.CHANGED.equals(event.getName())
					&& event.getProperty("wrapper")!=null) { //$NON-NLS-1$
				PaIntEParamsWrapper wrapper = (PaIntEParamsWrapper) event.getProperty("wrapper"); //$NON-NLS-1$
				refreshParamComponents(wrapper);
			} else {
				refreshParamComponents();
			}

			refreshGraph();
		}

	}

	public class CallbackHandler {

		private CallbackHandler() {
			// no-op
		}

		public void openPreferences(ActionEvent e) {
			try {
				UIUtil.openConfigDialog(configPath);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to open preferences", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void refresh(ActionEvent e) {
			try {
				PaIntEEditorView.this.refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to refresh presenter", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void copyPainteParams(ActionEvent e) {
			try {
				PaIntEParamsWrapper params = getSelectedWrapper();
				if(params==null) {
					return;
				}
				PaIntEUtils.copyWrapper(params);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to copy painte parameters to clipboard", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void copyPainteParamsId(ActionEvent e) {
			try {
				PaIntEParamsWrapper params = getSelectedWrapper();
				if(params==null) {
					return;
				}
				PaIntEUtils.copyWrapperId(params);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to copy painte parameters id to clipboard", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void copyPainteParamsCompact(ActionEvent e) {
			try {
				PaIntEParamsWrapper params = getSelectedWrapper();
				if(params==null) {
					return;
				}
				PaIntEUtils.copyWrapperCompact(params);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to copy painte parameters in compact format to clipboard", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void pastePainteParams(ActionEvent e) {
			try {
				PaIntEEditorView.this.pasteParams(null);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to paste painte parameters from clipboard", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void removePainteParams(ActionEvent e) {
			try {
				PaIntEParamsWrapper params = getSelectedWrapper();
				if(params==null) {
					return;
				}

				//FIXME add confirmation dialog?

				PaIntERegistry.getInstance().removeParams(params);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to remove painte parameters from history", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void usePainteParams(ActionEvent e) {
			try {
				loadSelectedParams();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to select painte parameters for editing", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void renamePainteParams(ActionEvent e) {
			try {
				PaIntEParamsWrapper params = getSelectedWrapper();
				if(params==null) {
					return;
				}

				String currentName = params.getLabel();
				String newName = DialogFactory.getGlobalFactory().showInputDialog(getFrame(),
						"plugins.prosody.painteEditorView.dialogs.renameParams.title",  //$NON-NLS-1$
						"plugins.prosody.painteEditorView.dialogs.renameParams.message",  //$NON-NLS-1$
						currentName, currentName);

				// Cancelled by user
				if(!PaIntERegistry.isLegalName(newName)) {
					return;
				}

				// No changes
				if(currentName.equals(newName)) {
					return;
				}

				PaIntERegistry.getInstance().renameParams(params, newName);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to rename painte parameters", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void clearParamsRegistry(ActionEvent e) {
			try {
				if(!DialogFactory.getGlobalFactory().showConfirm(getFrame(),
						"plugins.prosody.painteEditorView.dialogs.clearRegistry.title", //$NON-NLS-1$
						"plugins.prosody.painteEditorView.dialogs.clearRegistry.message")) { //$NON-NLS-1$
					return;
				}

				PaIntERegistry.getInstance().removeAllParams();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to clear params history", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void exportParamsRegistry(ActionEvent e) {
			try {

				if(paramsTableModel.getRowCount()==0) {
					return;
				}

				// Obtain destination file (factory handles the 'overwrite' dialog)
				Path file = DialogFactory.getGlobalFactory().showDestinationFileDialog(
						getFrame(),
						"plugins.prosody.painteEditorView.dialogs.exportRegistry.title",  //$NON-NLS-1$
						null);

				if(file==null) {
					return;
				}

				PaIntERegistry.getInstance().exportParams(file);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to export params history", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void importParamsRegistry(ActionEvent e) {
			try {

				// Obtain source file
				Path file = DialogFactory.getGlobalFactory().showSourceFileDialog(
						getFrame(),
						"plugins.prosody.painteEditorView.dialogs.importRegistry.title",  //$NON-NLS-1$
						null);

				if(file==null || Files.notExists(file)) {
					return;
				}

				PaIntERegistry.getInstance().importParams(file);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to import params history", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void collapseAllPanels(ActionEvent e) {
			try {
				expandParamsPanels(false);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to collapse all panels", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void expandAllPanels(ActionEvent e) {
			try {
				expandParamsPanels(true);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to expand all panels", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void togglePaintAllCompact(boolean b) {
			try {
				paintAllCompact = b;
				refreshGraph();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to toggle 'paintAllCompact' property", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void togglePaintAllCompact(ActionEvent e) {
			// no-op
		}

		// PANELS ACTIONS

		// Allowed both for general context and panels!
		public void addParamsPanel(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);

			try {
				PaIntEEditorView.this.addParamsPanel(owningPanel);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to add params panel", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void removeParamsPanel(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {
				PaIntEEditorView.this.removeParamsPanel(owningPanel);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to remove panel", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void clearParamsPanel(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {
				owningPanel.clear();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to clear panel", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void colorParamsPanel(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {
				owningPanel.selectColor();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to select color for panel", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void renameParamsPanel(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {
				PaIntEParamsWrapper params = owningPanel.getWrapper();

				String currentName = params.getLabel();
				String newName = DialogFactory.getGlobalFactory().showInputDialog(getFrame(),
						"plugins.prosody.painteEditorView.dialogs.renameParams.title",  //$NON-NLS-1$
						"plugins.prosody.painteEditorView.dialogs.renameParams.message",  //$NON-NLS-1$
						currentName, currentName);

				// Cancelled by user
				if(!PaIntERegistry.isLegalName(newName)) {
					return;
				}

				// No changes
				if(currentName.equals(newName)) {
					return;
				}

				if(owningPanel.isSaved()) {
					PaIntERegistry.getInstance().renameParams(params, newName);
				} else {
					params.setLabel(newName);
				}

				refreshParamComponents();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to rename panel", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void importParamsPanel(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {
				PaIntEEditorView.this.pasteParams(owningPanel);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to import parameters to panel", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void exportParamsPanel(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {

				PaIntEUtils.copyParams(owningPanel.getParams());
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to export parameters from panel", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void exportParamsPanelCompact(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {

				PaIntEUtils.copyParamsCompact(owningPanel.getParams());
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to export panel parameters in compact form", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void exportParamsPanelId(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {

				PaIntEUtils.copyWrapperId(owningPanel.getWrapper());
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to export panel id", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void saveParamsPanel(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {

				if(owningPanel.isRegistered()) {
					owningPanel.save();
					return;
				}

				PaIntEParamsWrapper params = owningPanel.getWrapper();
				String label = params.getLabel();

				//TODO add loca for save dialog
				while(label!=null && DEFAULT_NAME.equals(label)) {
					label = DialogFactory.getGlobalFactory().showInputDialog(getFrame(),
							"plugins.prosody.painteEditorView.dialogs.renameParams.title",  //$NON-NLS-1$
							"plugins.prosody.painteEditorView.dialogs.renameParams.message",  //$NON-NLS-1$
							label);
				}

				// Cancelled by user
				if(!PaIntERegistry.isLegalName(label)) {
					return;
				}

				owningPanel.syncToWrapper();
				params.setLabel(label);

				PaIntERegistry.getInstance().addParams(params);

			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to save panel", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void saveParamsPanelAs(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {
				// Ensure up2date data in the wrapper!
				owningPanel.syncToWrapper();

				PaIntEParamsWrapper params = owningPanel.getWrapper().clone();
				String label = params.getLabel();

				//TODO add loca for save-as dialog
				while(label!=null && (DEFAULT_NAME.equals(label)
						|| PaIntERegistry.getInstance().containsName(label))) {
					label = DialogFactory.getGlobalFactory().showInputDialog(getFrame(),
							"plugins.prosody.painteEditorView.dialogs.renameParams.title",  //$NON-NLS-1$
							"plugins.prosody.painteEditorView.dialogs.renameParams.message",  //$NON-NLS-1$
							label);
				}

				// Cancelled by user
				if(!PaIntERegistry.isLegalName(label)) {
					return;
				}

				params.setLabel(label);

				PaIntERegistry.getInstance().addParams(params);

			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to save panel as new registry entry", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void undoParamsPanel(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {
				owningPanel.undo();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to undo panel", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void redoParamsPanel(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {
				owningPanel.redo();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to redo panel", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void toggleParamsPanelPaintCompact(boolean b) {
			// no-op
		}

		public void toggleParamsPanelPaintCompact(ActionEvent e) {
			ParamsPanel owningPanel = getOwningPanel(e);
			if(owningPanel==null) {
				return;
			}

			try {
				AbstractButton b = (AbstractButton) e.getSource();
				owningPanel.setPaintCompact(b.isSelected());
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to toggle 'paintCompact' property for panel", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}
	}

	private static final String DEFAULT_NAME = "<unnamed>"; //$NON-NLS-1$

	private static class History extends ChangeSource {
		private int undoLimit = 20;
		private final List<ParamsChange> items = new ArrayList<>();
		private int cursor = -1;

		public void add(ParamsChange item) {
			// Clear redo section
			if(canRedo()) {
				items.subList(cursor+1, items.size()).clear();
			}

			item.execute();

			items.add(item);
			if(items.size()>undoLimit) {
				items.remove(0);
			} else {
				cursor++;
			}

			fireStateChanged();
		}

		public boolean canRedo() {
			return !items.isEmpty() && cursor<items.size()-1;
		}

		public boolean canUndo() {
			return !items.isEmpty() && cursor>=0;
		}

		public PaIntEParams undo() {
			if(!canUndo())
				throw new IllegalStateException("Cannot undo"); //$NON-NLS-1$

			ParamsChange item = items.get(cursor);
			cursor--;

			fireStateChanged();

			return item.execute();
		}

		public PaIntEParams redo() {
			if(!canRedo())
				throw new IllegalStateException("Cannot redo"); //$NON-NLS-1$

			cursor++;
			ParamsChange item = items.get(cursor);

			fireStateChanged();

			return item.execute();
		}

		public void clear() {
			items.clear();
			cursor = -1;

			fireStateChanged();
		}
	}

	private static class ParamsChange {
		private PaIntEParams before, after;

		public ParamsChange(PaIntEParams before, PaIntEParams after) {
			this.before = before;
			this.after = after;
		}

		public PaIntEParams execute() {
			PaIntEParams newParams = after;
			after = before;
			before = newParams;

			return newParams;
		}

	}

	private class ParamsPanel extends JPanel implements ActionListener, PropertyChangeListener, ChangeListener {

		private static final long serialVersionUID = -3162540273589172485L;

		private JPopupMenu popupMenu;

		private ParamComponents[] paramComponents;

		private PaIntEParamsWrapper wrapper = new PaIntEParamsWrapper(DEFAULT_NAME);
		private final PaIntEParams buffer = new PaIntEParams();

		private Color color;
		private final JButton colorButton, menuButton, undoButton, redoButton;
		private final JToggleButton toggleButton;
		private final JLabel titleLabel;

		private final History history;
		private boolean paintCompact = false;

		private final Icon colorIcon = new Icon() {

			/**
			 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
			 */
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				final int w = getIconWidth()-1;
				final int h = getIconHeight()-1;

				g.setColor(color);
				g.fillRect(x, y, w, h);

				g.setColor(Color.black);
				g.drawRect(x, y, w, h);
			}

			/**
			 * @see javax.swing.Icon#getIconWidth()
			 */
			@Override
			public int getIconWidth() {
				return 16;
			}

			/**
			 * @see javax.swing.Icon#getIconHeight()
			 */
			@Override
			public int getIconHeight() {
				return 16;
			}
		};

		public ParamsPanel(Color color) {
			if(color==null) {
				color = Color.black;
			}

			paramComponents = new ParamComponents[7];
			for(int i=0; i<paramComponents.length; i++) {
				paramComponents[i] = new ParamComponents(this, paramIds[i]);
			}

			history = new History();
			history.addChangeListener(this);

			this.color = color;

			toggleButton = new JToggleButton();
			toggleButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("navi_left_mini.png")); //$NON-NLS-1$
			toggleButton.setSelectedIcon(IconRegistry.getGlobalRegistry().getIcon("navi_right_mini.png")); //$NON-NLS-1$
			toggleButton.setFocusable(false);
			toggleButton.setFocusPainted(false);
			toggleButton.addActionListener(this);

			colorButton = new JButton(colorIcon);
			colorButton.setFocusable(false);
			colorButton.setFocusPainted(false);
			colorButton.addActionListener(this);

			titleLabel = new JLabel();
			titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

			setTransferHandler(transferManager);

			menuButton = new JButton(IconRegistry.getGlobalRegistry().getIcon("node_open.gif")); //$NON-NLS-1$
			menuButton.addActionListener(this);
			menuButton.setFocusable(false);
			menuButton.setFocusPainted(false);

			undoButton = new JButton(IconRegistry.getGlobalRegistry().getIcon("undo_edit.gif")); //$NON-NLS-1$
			undoButton.addActionListener(this);
			undoButton.setFocusable(false);
			undoButton.setFocusPainted(false);

			redoButton = new JButton(IconRegistry.getGlobalRegistry().getIcon("redo_edit.gif")); //$NON-NLS-1$
			redoButton.addActionListener(this);
			redoButton.setFocusable(false);
			redoButton.setFocusPainted(false);

			JToolBar toolBar = getDefaultActionManager().createEmptyToolBar();
			toolBar.add(titleLabel);
			toolBar.add(Box.createGlue());
			toolBar.add(colorButton);
			toolBar.add(undoButton);
			toolBar.add(redoButton);
			toolBar.add(menuButton);
			toolBar.add(toggleButton);

			FormLayout layout = new FormLayout(
					"fill:pref, 3dlu, pref, 3dlu, pref, 2dlu, min(65dlu;pref), 2dlu, pref", //$NON-NLS-1$
					"pref, 4dlu, pref, pref, pref, pref, pref, pref, pref"); //$NON-NLS-1$
			layout.setRowGroups(new int[][]{{3,4,5,6,7,8,9}});

			DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
			builder.add(toolBar, CC.rchw(1, 1, 1, 9));
			builder.nextLine();

			for(int i=0; i<paramComponents.length; i++) {
				ParamComponents paramComps = paramComponents[i];
				int row = 3+i;
				builder.add(paramComps.titleLabel, CC.rc(row, 1));
				builder.add(paramComps.textField, CC.rc(row, 3));
				builder.add(paramComps.minLabel, CC.rc(row, 5));
				builder.add(paramComps.slider, CC.rc(row, 7));
				builder.add(paramComps.maxLabel, CC.rc(row, 9));
			}

			setBorder(BorderFactory.createCompoundBorder(UIUtil.rightLineBorder, UIUtil.defaultContentBorder));

			refreshSize();
		}

		public void refresh() {
			String label = wrapper.getLabel();

			if(!isSaved()) {
				label += "*"; //$NON-NLS-1$
			}

			titleLabel.setText(label);
			titleLabel.setToolTipText(UIUtil.toUnwrappedSwingTooltip(wrapper.getDescription()));

//			setPaintCompact(wrapper.isCompact());

//			removeButton.setVisible(paramsPanels.size()>1 && !toggleButton.isSelected());
		}

		public boolean isSaved() {
			return isRegistered() && wrapper.getParams().equals(buffer);
		}

		public boolean isRegistered() {
			return PaIntERegistry.getInstance().containsParams(wrapper);
		}

		public double min(int index) {
			return paramComponents[index].min;
		}

		public double max(int index) {
			return paramComponents[index].max;
		}

		public double value(int index) {
			return paramComponents[index].getValue();
		}

		public Color color() {
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
			colorButton.repaint();
		}

		public void clear() {
			reloadConfig(getConfigHandle());

			getParams();
			refresh();
		}

		public void syncToWrapper() {
			wrapper.getParams().setParams(buffer);
		}

		public void syncToBuffer() {
			buffer.setParams(wrapper.getParams());

			refreshComponents();
		}

		public PaIntEParams getParams() {

			double[] params = new double[7];

			for(int i=0; i<params.length; i++) {
				params[i] = value(i);
			}

			buffer.setParams(params);

			return buffer;
		}

		public PaIntEParamsWrapper getWrapper() {
			return wrapper;
		}

		public void clearHistory() {
			history.clear();
		}

		public void undo() {
			PaIntEParams params = (PaIntEParams) history.undo();
//			System.out.println("undo: "+params);
			setParams0(params);

			refresh();
		}

		public void redo() {
			PaIntEParams params = (PaIntEParams) history.redo();
//			System.out.println("redo: "+params);
			setParams0(params);

			refresh();
		}

		public void save() {
			syncToWrapper();

			PaIntERegistry.getInstance().paramsChanged(wrapper);
		}

		public void setWrapper(PaIntEParamsWrapper newParams) {
			wrapper = newParams;
//			paintCompact = wrapper.isCompact();

			double[] params = newParams.getParams().getParams(new double[7]);

			for(int i=0; i<params.length; i++) {
				paramComponents[i].setValue(params[i]);
			}

			syncToBuffer();

			history.clear();
		}

		public void setParams(PaIntEParams newParams) {
			history.add(new ParamsChange(buffer.clone(), newParams));

			setParams0(newParams);
		}

		private void refreshComponents() {

			double[] params = buffer.getParams(new double[7]);

			for(int i=0; i<params.length; i++) {
				paramComponents[i].setValue(params[i]);
			}
		}

		public void setParams0(PaIntEParams newParams) {
			buffer.setParams(newParams);

//			System.out.println("setParams0: "+newParams);

			refreshComponents();

			if(isSaved()) {
				PaIntERegistry.getInstance().paramsChanged(wrapper);
			} else {
				refreshGraph();
			}
		}

		private void reloadConfig(Handle handle) {
			ConfigRegistry registry = handle.getSource();

			boolean overwriteValue = !isRegistered() && !isSaved();

			for(int i=0; i<paramComponents.length; i++) {
				ParamComponents paramComps = paramComponents[i];
				String id = paramComps.id;

				Handle paramsHandle = registry.getChildHandle(handle, id+"Bounds"); //$NON-NLS-1$

				double min = registry.getDouble(registry.getChildHandle(paramsHandle, "lower")); //$NON-NLS-1$
				double max = registry.getDouble(registry.getChildHandle(paramsHandle, "upper")); //$NON-NLS-1$
				paramComps.setMinMax(min, max);

				if(overwriteValue) {
					double value = registry.getDouble(registry.getChildHandle(paramsHandle, "default")); //$NON-NLS-1$

					value = Math.min(max, Math.max(value, min));

					paramComps.setValue(value);
				}
			}

			history.clear();
		}

		public void setPaintCompact(boolean paintCompact) {
			if(this.paintCompact==paintCompact) {
				return;
			}

			this.paintCompact = paintCompact;

			//TODO maybe disable unused parameter components?

			refreshGraph();
		}

		public boolean isPaintCompact() {
			return paintCompact;
		}

		public boolean shouldPaintCompact() {
			return paintCompact || wrapper.isCompact();
		}

		public void setExpandedState(boolean expanded) {
			colorButton.setVisible(expanded);
			undoButton.setVisible(expanded);
			redoButton.setVisible(expanded);
//			addButton.setVisible(expanded);
//			pasteButton.setVisible(expanded);
//			saveButton.setVisible(expanded);
//			removeButton.setVisible(expanded && paramsPanels.size()>1);

			for(ParamComponents comp : paramComponents) {
				comp.setExpandedState(expanded);
			}

			Color col = Color.black;
			if(!expanded) {
				col = color();
			}
			titleLabel.setForeground(col);

			if(expanded==toggleButton.isSelected()) {
				toggleButton.setSelected(!expanded);
			}

			refreshSize();
		}

		private void refreshSize() {
			Dimension d = getPreferredSize();
			d.height = Short.MAX_VALUE;
			setMaximumSize(d);
		}

		private void refreshPanelActions() {
			ActionManager actionManager = getDefaultActionManager();

			//FIXME needs checking on side effects!
//			syncToWrapper(); -> no synching or we would always overwrite base data!

			boolean saved = isSaved();
			boolean savable = !saved;
			boolean closable = paramsPanels.size() > 1;
			boolean pasteable = isPaIntEClipboardContent();

			boolean forcedCompact = wrapper.isCompact();

			boolean canUndo = history.canUndo();
			boolean canRedo = history.canRedo();

			actionManager.setEnabled(closable,
					"plugins.prosody.painteEditorView.removeParamsPanelAction"); //$NON-NLS-1$
			actionManager.setEnabled(pasteable,
					"plugins.prosody.painteEditorView.importParamsPanelAction"); //$NON-NLS-1$
			actionManager.setEnabled(savable,
					"plugins.prosody.painteEditorView.saveParamsPanelAction"); //$NON-NLS-1$
			actionManager.setEnabled(saved,
					"plugins.prosody.painteEditorView.exportParamsPanelIdAction"); //$NON-NLS-1$
			actionManager.setEnabled(canUndo,
					"plugins.prosody.painteEditorView.undoParamsPanelAction"); //$NON-NLS-1$
			actionManager.setEnabled(canRedo,
					"plugins.prosody.painteEditorView.redoParamsPanelAction"); //$NON-NLS-1$
			actionManager.setSelected(paintCompact || forcedCompact,
					"plugins.prosody.painteEditorView.toggleParamsPanelPaintCompactAction"); //$NON-NLS-1$
			actionManager.setEnabled(!forcedCompact,
					"plugins.prosody.painteEditorView.toggleParamsPanelPaintCompactAction"); //$NON-NLS-1$

			Action colorAction = actionManager.getAction(
					"plugins.prosody.painteEditorView.colorParamsPanelAction"); //$NON-NLS-1$
			colorAction.putValue(Action.SMALL_ICON, colorIcon);

			undoButton.setEnabled(canUndo);
			redoButton.setEnabled(canRedo);
		}

		private boolean changeInProgress = false;
		private PaIntEParams beforeChange;

		void valueChanged(ParamComponents comp, boolean valueIsAdjusting) {
			if(!changeInProgress) {
				changeInProgress = true;
				beforeChange = buffer.clone();
			}

			if(!valueIsAdjusting) {
				PaIntEParams afterChange = buffer.clone();
				history.add(new ParamsChange(beforeChange, afterChange));
				beforeChange = null;

				changeInProgress = false;
			}

			refreshGraph();
			refresh();
		}

		private void showPopuMenu() {
			if(popupMenu==null) {
				popupMenu = getDefaultActionManager().createPopupMenu(
						"plugins.prosody.painteEditorView.parameterPanelMenuList", null); //$NON-NLS-1$
				popupMenu.setInvoker(menuButton);
				popupMenu.addPropertyChangeListener("visible", this); //$NON-NLS-1$
			}

			refreshPanelActions();

			popupMenu.show(menuButton, 0, menuButton.getHeight());
		}

		public void selectColor() {
            Color selectedColor = JColorChooser.showDialog(null,
            		ResourceManager.getInstance().get("config.chooseColorPick.name"), //$NON-NLS-1$
            		color);

            if(selectedColor!=null) {
            	color = selectedColor;

            	refreshGraph();
            }
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==colorButton) {
				selectColor();
			} else if(e.getSource()==menuButton) {
				showPopuMenu();
			} else if(e.getSource()==toggleButton) {
				setExpandedState(!((JToggleButton)e.getSource()).isSelected());
			} else if(e.getSource()==undoButton) {
				undo();
			} else if(e.getSource()==redoButton) {
				redo();
			}
		}

		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if("visible".equals(evt.getPropertyName())) { //$NON-NLS-1$
				boolean visible = (boolean) evt.getNewValue();

				if(visible) {
					owningPanel = this;
				} else {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							owningPanel = null;
						}
					});
				}
			}
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			refreshPanelActions();
		}
	}

	private class ParamComponents extends MouseAdapter implements ActionListener, ChangeListener {
		private final String id;

		private final JLabel titleLabel, minLabel, maxLabel;
		private final JTextField textField;
		private final JSlider slider;

		private double min, max;

		private final ParamsPanel panel;

		public ParamComponents(ParamsPanel panel, String id) {
			if (id == null)
				throw new NullPointerException("Invalid id"); //$NON-NLS-1$

			this.panel = panel;
			this.id = id;

			titleLabel = new JLabel();
			titleLabel.setText(ResourceManager.getInstance().get("plugins.prosody.painteEditorView.labels."+id+".name")); //$NON-NLS-1$ //$NON-NLS-2$
			titleLabel.setToolTipText(ResourceManager.getInstance().get("plugins.prosody.painteEditorView.labels."+id+".description")); //$NON-NLS-1$ //$NON-NLS-2$

			minLabel = new JLabel("-"); //$NON-NLS-1$
			maxLabel = new JLabel("+"); //$NON-NLS-1$

			textField = new JTextField(6);
			textField.setDocument(new NumberDocument(true));
			textField.addActionListener(this);

			slider = new JSlider(SwingConstants.HORIZONTAL);
			slider.setMinorTickSpacing(1);
			slider.setMajorTickSpacing(10);
			slider.setMaximum(1000);
			slider.setFocusable(false);
//			slider.setPaintTicks(true);
			slider.addMouseWheelListener(this);
			slider.addChangeListener(this);
		}

		public void setExpandedState(boolean expanded) {
			minLabel.setVisible(expanded);
			maxLabel.setVisible(expanded);
			slider.setVisible(expanded);
		}

		private String toLabel(double value) {
			return String.format(Locale.ENGLISH, "%.02f", value); //$NON-NLS-1$
		}

		private boolean ignoreChanges = false;

		public void setValue(double value) {
			ignoreChanges = true;

			try {
				textField.setText(toLabel(value));

				double relValue = (value-min)/(max-min);
				int newValue = slider.getMinimum()+(int)((slider.getMaximum()-slider.getMinimum()) * relValue);

				slider.setValue(newValue);
			} finally {
				ignoreChanges = false;
			}
		}

		public void setMinMax(double newMin, double newMax) {
			min = newMin;
			max = newMax;

			minLabel.setText(toLabel(min));
			maxLabel.setText(toLabel(max));
		}

		public double getValue() {
//			float value = slider.getValue();
//			float range = (float)slider.getMaximum()-(float)slider.getMinimum();
//
//			return min + (max-min)*(value/range);
			String label = textField.getText();
			return (label==null || label.isEmpty()) ? min :Double.parseDouble(label);
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseWheelMoved(java.awt.event.MouseWheelEvent)
		 */
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if(e.getWheelRotation()<0) {
				// Away from user => scroll right
				if(slider.getValue()<slider.getMaximum()); {
					slider.setValue(slider.getValue()+1);
				}
			} else {
				if(slider.getValue()>slider.getMinimum()); {
					slider.setValue(slider.getValue()-1);
				}
			}
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			if(ignoreChanges) {
				return;
			}

			double value = slider.getValue();
			double range = (double)slider.getMaximum()-(double)slider.getMinimum();

			value = min + (max-min)*(value/range);
			textField.setText(toLabel(value));

			panel.valueChanged(this, slider.getValueIsAdjusting());
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(ignoreChanges) {
				return;
			}

			if(e.getSource()==textField) {
				double value = Double.parseDouble(textField.getText());
				setValue(value);
			}

			panel.valueChanged(this, false);
		}
	}

	private class TransferManager extends TransferHandler {

		private static final long serialVersionUID = 4248110860048880727L;

		private int sourceIndex = -1;
		private boolean logErrors = false;
		private PaIntEIcon transferIcon;

		public boolean isLogErrors() {
			return logErrors;
		}

		public void setLogErrors(boolean logErrors) {
			this.logErrors = logErrors;
		}

		@Override
		public boolean canImport(TransferSupport info) {
			if (!info.isDrop()
					|| !info.isDataFlavorSupported(UIUtil.localObjectFlavor)) {
				return false;
			}

			if (info.getComponent() != paramsTable) {
				// Ensure only copy mode is accepted
				boolean copySupported = (COPY & info.getSourceDropActions()) == COPY;
				if (copySupported) {
					info.setDropAction(COPY);
					return true;
				}

				return false;
			}

			return true;
		}

		@Override
		public int getSourceActions(JComponent c) {
			return c==paramsTable ? COPY_OR_MOVE : NONE;
		}

		@Override
		public Icon getVisualRepresentation(Transferable t) {
			Object[] values = getTransferedValues(t);

			if(values.length==0) {
				return null;
			}

			if(transferIcon==null) {
				transferIcon = new PaIntEIcon();
			}

			PaIntEParams params = extractParams(values[0]);

			if(params==null) {
				return null;
			}

			transferIcon.getParams().setParams(params);

			return transferIcon;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			if(c!=paramsTable) {
				return null;
			}

			sourceIndex = paramsTable.getSelectedRow();
			PaIntEParamsWrapper wrapper = paramsTableModel.getItem(sourceIndex);
			Object[] values = new Object[]{wrapper};

			return new DataHandler(values,
					UIUtil.localObjectFlavor.getMimeType());
		}

		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {
			sourceIndex = -1;
		}

		protected Object[] getTransferedValues(Transferable t) {

			try {
				return (Object[]) t.getTransferData(UIUtil.localObjectFlavor);
			} catch (UnsupportedFlavorException e) {
				if(isLogErrors()) {
					LoggerFactory.error(this, "Encountered incompatible transfer data flavor", e); //$NON-NLS-1$
				}
			} catch (IOException e) {
				if(isLogErrors()) {
					LoggerFactory.error(this, "Failed to deserialize trasfer data", e); //$NON-NLS-1$
				}
			}

			return new Object[0];
		}

		@Override
		public boolean importData(TransferSupport info) {
			if (!canImport(info)) {
				return false;
			}

			Object[] values = getTransferedValues(info.getTransferable());

			if(values.length==0) {
				return false;
			}

			int addCount = 0;

			if(info.getComponent()==paramsTable) {

				JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
				int index = dl.getRow();
				int max = paramsTableModel.getRowCount()-1;
				if (index < 0 || index > max) {
					index = max;
				}

				if(index!=sourceIndex) {
					PaIntERegistry.getInstance().moveParams(sourceIndex, index);
				}
			} else {
				ParamsPanel panel = null;
				if(info.getComponent() instanceof ParamsPanel) {
					panel = (ParamsPanel) info.getComponent();
				}

				boolean append = false;

				for (int i = 0; i < values.length; i++) {
					PaIntEParamsWrapper wrapper = extractWrapper(values[i]);

					if(wrapper==null) {
						continue;
					}

					if(append || panel==null) {
						panel = addParamsPanel(panel);
					}
					panel.setWrapper(wrapper);
					panel.refresh();
					append = true;

					addCount++;
				}
			}

			refreshGraph();

			return addCount>0;
		}
	}

	private class GraphComponent extends JComponent {

		private static final long serialVersionUID = -3436549345676825118L;

		private final PaIntEGraph graph;

		public GraphComponent() {
			graph = new PaIntEGraph();
			graph.getCurve().setMaxSampleCount(200);
		}

		public void reloadConfig(Handle handle) {
			ConfigRegistry registry = handle.getSource();

			graph.setPaintGrid(registry.getBoolean(registry.getChildHandle(handle, "paintGrid"))); //$NON-NLS-1$
			graph.setGridColor(registry.getColor(registry.getChildHandle(handle, "gridColor"))); //$NON-NLS-1$
			graph.setGridStyle(registry.getValue(registry.getChildHandle(handle, "gridStyle"), PaIntEGraph.DEFAULT_GRID_STYLE)); //$NON-NLS-1$

			int graphHeight = registry.getInteger(registry.getChildHandle(handle, "graphHeight")); //$NON-NLS-1$
			int graphWidth = registry.getInteger(registry.getChildHandle(handle, "graphWidth")); //$NON-NLS-1$
			int leftExtend = registry.getInteger(registry.getChildHandle(handle, "leftSyllableExtent")); //$NON-NLS-1$
			int rightExtend = registry.getInteger(registry.getChildHandle(handle, "rightSyllableExtent")); //$NON-NLS-1$

			setMinimumSize(new Dimension(graphWidth, graphHeight));

			Axis.Integer xAxis = (Axis.Integer) graph.getXAxis();
			xAxis.setMinValue(-leftExtend);
			xAxis.setMaxValue(rightExtend);
		}

		public PaIntEGraph getGraph() {
			return graph;
		}

		public void refresh() {
			if(!paramsPanels.isEmpty()) {
				ParamsPanel panel = paramsPanels.get(0);

				double dMax = panel.max(PARAM_D);
				double dMin = panel.min(PARAM_D);

				double cMax = Math.max(panel.max(PARAM_C1), panel.max(PARAM_C2));

				for(int i=1; i<paramsPanels.size(); i++) {
					ParamsPanel p = paramsPanels.get(i);
					cMax = Math.max(cMax, Math.max(p.max(PARAM_C1), p.max(PARAM_C2)));
				}

				dMin = Math.min(dMin, dMin-cMax);

				// Make sure y axis doesn't go into negative space!
				dMin = Math.max(dMin, 0F);

				Axis.Integer yAxis = (Axis.Integer) graph.getYAxis();
				yAxis.setMinValue((int) Math.floor(dMin));
				yAxis.setMaxValue((int) Math.ceil(dMax));
			}

			repaint();
		}

		/**
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			if(paramsPanels.isEmpty()) {
				return;
			}

			Dimension size = getSize();
			Insets insets = getInsets();

			Rectangle area = new Rectangle(insets.left, insets.top,
					size.width-insets.left-insets.right, size.height-insets.top-insets.bottom);

			int yAxisWidth = graph.getYAxis().getRequiredWidth(g);
			int xAxisHeight = graph.getXAxis().getRequiredHeight(g);

			graph.paint(g, null, area);

			area.x += yAxisWidth;
			area.height -= xAxisHeight;
			area.width -= yAxisWidth;

			for(int i=0; i<paramsPanels.size(); i++) {
				paintPanel(g, paramsPanels.get(i), area);
			}

			graph.getCurve().setColor(Color.black);
		}

		private void paintPanel(Graphics g, ParamsPanel panel, Rectangle area) {

			graph.getCurve().setPaintComapct(paintAllCompact || panel.shouldPaintCompact());

			graph.getCurve().setColor(panel.color());
			graph.getCurve().paint(g, panel.getParams(), area,
					graph.getXAxis(), graph.getYAxis());
		}
	}
}
