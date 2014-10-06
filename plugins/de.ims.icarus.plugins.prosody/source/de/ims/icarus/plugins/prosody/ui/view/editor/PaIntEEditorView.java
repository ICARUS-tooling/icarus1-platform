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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
import de.ims.icarus.plugins.prosody.ui.geom.Axis;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.list.PaIntEParamsListCellRenderer;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.NumberDocument;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.events.ListenerProxies;
import de.ims.icarus.ui.list.ListItemTransferHandler;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.classes.ClassUtils;

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
	private Box paramsComponent;

	private JList<PaIntEParamsWrapper> paramsHistoryList;
	private DefaultListModel<PaIntEParamsWrapper> paramsHistoryListModel;

	private JPopupMenu popupMenu;

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

		graphComponent = new GraphComponent();
		graphComponent.setBorder(new EmptyBorder(10, 10, 10, 10));
		graphComponent.setTransferHandler(new PainteGraphTransferHandler());

		paramsComponent = Box.createHorizontalBox();
		paramsComponent.add(Box.createHorizontalGlue());

		JScrollPane paramsScrollPane = new JScrollPane(paramsComponent);
		paramsScrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(paramsScrollPane);

		JSplitPane upperSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, graphComponent, paramsScrollPane);
		upperSplitPane.setBorder(null);
		upperSplitPane.setResizeWeight(0);
		upperSplitPane.setDividerLocation(300);

//		JScrollPane upperScrollPane = new JScrollPane(upperSplitPane);
//		upperScrollPane.setBorder(UIUtil.topLineBorder);
//		UIUtil.defaultSetUnitIncrement(upperScrollPane);
//		upperScrollPane.setMinimumSize(paramsComponent.getPreferredSize());

		paramsHistoryListModel = new DefaultListModel<>();
		paramsHistoryList = new JList<>(paramsHistoryListModel);
		paramsHistoryList.setCellRenderer(new PaIntEParamsListCellRenderer(graphComponent.getGraph()));
		paramsHistoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paramsHistoryList.addMouseListener(getHandler());
		paramsHistoryList.addListSelectionListener(getHandler());
		paramsHistoryList.setDragEnabled(true);
		paramsHistoryList.setTransferHandler(new ListItemTransferHandler());
		UIUtil.enableRighClickListSelection(paramsHistoryList);

		JScrollPane lowerScrollPane = new JScrollPane(paramsHistoryList);
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

		reloadConfig();
	}

	public void refresh() {
		refreshParamComponents();
	}

	@Override
	public void close() {
		super.close();

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard!=null) {
			clipboard.removeFlavorListener(ListenerProxies.getProxy(FlavorListener.class, getHandler()));
		}
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

		actionManager.addHandler("plugins.prosody.painteEditorView.openPreferencesAction", //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.removePainteParamsAction", //$NON-NLS-1$
				callbackHandler, "removePainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.renamePainteParamsAction", //$NON-NLS-1$
				callbackHandler, "renamePainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.usePainteParamsAction", //$NON-NLS-1$
				callbackHandler, "usePainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.clearParamsHistoryAction", //$NON-NLS-1$
				callbackHandler, "clearParamsHistory"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.importParamsHistoryAction", //$NON-NLS-1$
				callbackHandler, "importParamsHistory"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.exportParamsHistoryAction", //$NON-NLS-1$
				callbackHandler, "exportParamsHistory"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.addParamsPanelAction", //$NON-NLS-1$
				callbackHandler, "addParamsPanel"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.copyPainteParamsAction", //$NON-NLS-1$
				callbackHandler, "copyPainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.collapseAllPanelsAction", //$NON-NLS-1$
				callbackHandler, "collapseAllPanels"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.expandAllPanelsAction", //$NON-NLS-1$
				callbackHandler, "expandAllPanels"); //$NON-NLS-1$
	}

	private void refreshActions() {
		ActionManager actionManager = getDefaultActionManager();

		int index = paramsHistoryList.getSelectedIndex();
		int historySize = paramsHistoryListModel.getSize();

		boolean hasSelection = index!=-1;
		boolean hasHistory = historySize>0;
		boolean multipleParams = paramsPanels.size()>0; //TODO temporary fix, amybe change abck to 1?

		boolean pasteable = false;
		try {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t = clipboard.getContents(this);
			if(t!=null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				new PaIntEConstraintParams((String) t.getTransferData(DataFlavor.stringFlavor));
				pasteable = true;
			}
		} catch(Exception e) {
			//ignore
		}

		actionManager.setEnabled(hasSelection,
				"plugins.prosody.painteEditorView.removePainteParamsAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.renamePainteParamsAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.copyPainteParamsAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.usePainteParamsAction"); //$NON-NLS-1$
		actionManager.setEnabled(hasHistory,
				"plugins.prosody.painteEditorView.exportParamsHistoryAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.clearParamsHistoryAction"); //$NON-NLS-1$
		actionManager.setEnabled(multipleParams,
				"plugins.prosody.painteEditorView.collapseAllPanelsAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.expandAllPanelsAction"); //$NON-NLS-1$

		for(ParamsPanel panel : paramsPanels) {
			panel.refreshPasteButton(pasteable);
		}
	}

	private Handle getConfigHandle() {
		return ConfigRegistry.getGlobalRegistry().getHandle(configPath);
	}

	private void reloadConfig() {
		Handle handle = getConfigHandle();

		for(ParamsPanel panel : paramsPanels) {
			panel.reloadConfig(handle);
		}

		refreshGraph();
	}

	private ParamsPanel addParamsPanel(ParamsPanel source) {
		ParamsPanel panel = new ParamsPanel();
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

		refreshParamComponents();
		refreshActions();
	}

	private void loadSelectedParams() {
		PaIntEParamsWrapper params = paramsHistoryList.getSelectedValue();
		if(params==null) {
			return;
		}

		addParamsPanel(null).setParams(params.getParams());

		refreshActions();
	}

	private void refreshGraph() {
		if(graphComponent==null) {
			return;
		}

		graphComponent.refresh();
	}

	private void refreshParamComponents() {

		for(ParamsPanel panel : paramsPanels) {
			panel.refresh();
		}

		refreshGraph();
	}

	private void addParamsToHistory(PaIntEParams params) {
		paramsHistoryListModel.addElement(new PaIntEParamsWrapper(params));
	}

	public void expandParamsPanels(boolean expanded) {
		for(ParamsPanel panel : paramsPanels) {
			panel.setExpandedState(expanded);
		}
	}

	public void copyParams(PaIntEParams params) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard==null) {
			return;
		}

		String text = new PaIntEConstraintParams(params).toString();
		StringSelection data = new StringSelection(text);

		clipboard.setContents(data, data);
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

		try {
			PaIntEConstraintParams params = new PaIntEConstraintParams(text);
			panel.setParams(params.toPaIntEParams());
		} catch(Exception e) {
			//ignore invalid formats
		}
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

			popupMenu.show(paramsHistoryList, trigger.getX(), trigger.getY());
		}
	}

	private class Handler extends MouseAdapter implements ConfigListener, ListSelectionListener, FlavorListener {

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
				loadSelectedParams();
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
				PaIntEParamsWrapper params = paramsHistoryList.getSelectedValue();
				if(params==null) {
					return;
				}
				copyParams(params.getParams());
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to copy painte parameters to clipboard", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void removePainteParams(ActionEvent e) {
			try {
				int index = paramsHistoryList.getSelectedIndex();
				if(index==-1) {
					return;
				}
				paramsHistoryListModel.remove(index);
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
				PaIntEParamsWrapper params = paramsHistoryList.getSelectedValue();
				if(params==null) {
					return;
				}


				String currentName = params.getLabel();
				String newName = DialogFactory.getGlobalFactory().showInputDialog(getFrame(),
						"plugins.prosody.painteEditorView.dialogs.renameParams.title",  //$NON-NLS-1$
						"plugins.prosody.painteEditorView.dialogs.renameParams.message",  //$NON-NLS-1$
						currentName);

				// Cancelled by user
				if(newName==null) {
					return;
				}

				// No changes
				if(ClassUtils.equals(currentName, newName)) {
					return;
				}

				params.setLabel(newName);

				paramsHistoryList.repaint();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to rename painte parameters", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void clearParamsHistory(ActionEvent e) {
			try {
				paramsHistoryListModel.removeAllElements();
				paramsHistoryList.clearSelection();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to clear params history", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void exportParamsHistory(ActionEvent e) {
			try {

				if(paramsHistoryListModel.getSize()==0) {
					return;
				}

				// Obtain destination file (factory handles the 'overwrite' dialog)
				Path file = DialogFactory.getGlobalFactory().showDestinationFileDialog(
						getFrame(),
						"plugins.prosody.painteEditorView.dialogs.exportHistory.title",  //$NON-NLS-1$
						null);

				if(file==null) {
					return;
				}

				// Collect history elements
				ParamsHistory history = new ParamsHistory();
				for(int i=0; i<paramsHistoryListModel.getSize(); i++) {
					history.items.add(paramsHistoryListModel.get(i));
				}

				JAXBContext context = JAXBContext.newInstance(ParamsHistory.class);
				Marshaller marshaller = context.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				marshaller.marshal(history, Files.newOutputStream(file));

				paramsHistoryList.clearSelection();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to export params history", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void importParamsHistory(ActionEvent e) {
			try {

				// Obtain source file
				Path file = DialogFactory.getGlobalFactory().showSourceFileDialog(
						getFrame(),
						"plugins.prosody.painteEditorView.dialogs.importHistory.title",  //$NON-NLS-1$
						null);

				if(file==null || Files.notExists(file)) {
					return;
				}

				JAXBContext context = JAXBContext.newInstance(ParamsHistory.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();

				ParamsHistory history = (ParamsHistory) unmarshaller.unmarshal(Files.newInputStream(file));

				if(history.items.isEmpty()) {
					return;
				}

				for(PaIntEParamsWrapper params : history.items) {
					paramsHistoryListModel.addElement(params);
				}

				paramsHistoryList.clearSelection();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to import params history", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void addParamsPanel(ActionEvent e) {
			try {
				PaIntEEditorView.this.addParamsPanel(null);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to add params panel", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
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
	}

	@XmlRootElement(name="params-history")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ParamsHistory {
		@XmlElement(name="entry")
		public List<PaIntEParamsWrapper> items = new ArrayList<>();
	}

	private class ParamsPanel extends JPanel implements ActionListener {

		private static final long serialVersionUID = -3162540273589172485L;

		private ParamComponents[] paramComponents;

		private final PaIntEParams painteParams = new PaIntEParams();

		private Color color = Color.black;
		private final JButton colorButton, addButton, removeButton, copyButton, pasteButton, saveButton;
		private final JToggleButton toggleButton;
		private final JLabel titleLabel;

		private Icon colorIcon = new Icon() {

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

		public ParamsPanel() {
			paramComponents = new ParamComponents[] {
					new ParamComponents("a1"), //$NON-NLS-1$
					new ParamComponents("a2"), //$NON-NLS-1$
					new ParamComponents("b"), //$NON-NLS-1$
					new ParamComponents("c1"), //$NON-NLS-1$
					new ParamComponents("c2"), //$NON-NLS-1$
					new ParamComponents("d"), //$NON-NLS-1$
					new ParamComponents("alignment"), //$NON-NLS-1$
			};

			colorButton = new JButton();
			colorButton.setIcon(colorIcon);
			colorButton.addActionListener(this);

			addButton = new JButton();
			addButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("add_obj.gif")); //$NON-NLS-1$
			addButton.addActionListener(this);

			removeButton = new JButton();
			removeButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("delete_obj.gif")); //$NON-NLS-1$
			removeButton.addActionListener(this);

			copyButton = new JButton();
			copyButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("copy_edit.gif")); //$NON-NLS-1$
			copyButton.addActionListener(this);

			pasteButton = new JButton();
			pasteButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("paste_edit.gif")); //$NON-NLS-1$
			pasteButton.addActionListener(this);

			saveButton = new JButton();
			saveButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("chart_curve_add.png")); //$NON-NLS-1$
			saveButton.addActionListener(this);

			toggleButton = new JToggleButton();
			toggleButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("navi_left_mini.png")); //$NON-NLS-1$
			toggleButton.setSelectedIcon(IconRegistry.getGlobalRegistry().getIcon("navi_right_mini.png")); //$NON-NLS-1$
			toggleButton.addActionListener(this);

			titleLabel = new JLabel();
			titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

			JToolBar toolBar = getDefaultActionManager().createEmptyToolBar();
			toolBar.add(titleLabel);
			toolBar.add(Box.createGlue());
			toolBar.add(copyButton);
			toolBar.add(pasteButton);
			toolBar.add(colorButton);
			toolBar.add(saveButton);
			toolBar.add(addButton);
			toolBar.add(removeButton);
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
			int index = paramsPanels.indexOf(this);

			String title = ResourceManager.getInstance().get("plugins.prosody.painteEditorView.labels.params")+" ("+(index+1)+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			titleLabel.setText(title);

			removeButton.setVisible(paramsPanels.size()>1 && !toggleButton.isSelected());
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

		public PaIntEParams getParams() {

			double[] params = new double[7];

			for(int i=0; i<params.length; i++) {
				params[i] = paramComponents[i].getValue();
			}

			painteParams.setParams(params);

			return painteParams;
		}

		public void setParams(PaIntEParams newParams) {
			painteParams.setParams(newParams);

			double[] params = newParams.getParams(new double[7]);

			for(int i=0; i<params.length; i++) {
				paramComponents[i].setValue(params[i]);
			}

			refreshGraph();
		}

		private void reloadConfig(Handle handle) {
			ConfigRegistry registry = handle.getSource();

			for(int i=0; i<paramComponents.length; i++) {
				ParamComponents paramComps = paramComponents[i];
				String id = paramComps.id;

				double min = registry.getDouble(registry.getChildHandle(handle, id+"LowerBound")); //$NON-NLS-1$
				double max = registry.getDouble(registry.getChildHandle(handle, id+"UpperBound")); //$NON-NLS-1$
				double value = registry.getDouble(registry.getChildHandle(handle, id+"Default")); //$NON-NLS-1$

				value = Math.min(max, Math.max(value, min));

				paramComps.setMinMax(min, max);
				paramComps.setValue(value);
			}
		}

		public void setExpandedState(boolean expanded) {
			colorButton.setVisible(expanded);
			addButton.setVisible(expanded);
			pasteButton.setVisible(expanded);
			saveButton.setVisible(expanded);
			removeButton.setVisible(expanded && paramsPanels.size()>1);

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

		public void refreshPasteButton(boolean pasteable) {
			pasteButton.setEnabled(pasteable);
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==colorButton) {
	            Color selectedColor = JColorChooser.showDialog(null,
	            		ResourceManager.getInstance().get("config.chooseColorPick.name"), //$NON-NLS-1$
	            		color);

	            if(selectedColor!=null) {
	            	color = selectedColor;

	            	refreshGraph();
	            }
			} else if(e.getSource()==addButton) {
				addParamsPanel(this);
			} else if(e.getSource()==removeButton) {
				removeParamsPanel(this);
			} else if(e.getSource()==saveButton) {
				addParamsToHistory(getParams());
			} else if(e.getSource()==toggleButton) {
				setExpandedState(!((JToggleButton)e.getSource()).isSelected());
			} else if(e.getSource()==copyButton) {
				copyParams(getParams());
			} else if(e.getSource()==pasteButton) {
				pasteParams(this);
			}
		}
	}

	private class ParamComponents extends MouseAdapter implements ActionListener, ChangeListener {
		private final String id;

		private final JLabel titleLabel, minLabel, maxLabel;
		private final JTextField textField;
		private final JSlider slider;

		private Color color = Color.black;
		private double min, max;

		public ParamComponents(String id) {
			if (id == null)
				throw new NullPointerException("Invalid id"); //$NON-NLS-1$

			this.id = id;

			titleLabel = new JLabel();
			titleLabel.setText(ResourceManager.getInstance().get("plugins.prosody.painteEditorView.labels."+id+".name")); //$NON-NLS-1$ //$NON-NLS-2$

			minLabel = new JLabel("-"); //$NON-NLS-1$
			maxLabel = new JLabel("+"); //$NON-NLS-1$

			textField = new JTextField(6);
			textField.setDocument(new NumberDocument(true));
			textField.addActionListener(this);

			slider = new JSlider(SwingConstants.HORIZONTAL);
			slider.addChangeListener(this);
			slider.setMinorTickSpacing(1);
			slider.setMajorTickSpacing(10);
			slider.setMaximum(1000);
//			slider.setPaintTicks(true);
			slider.addMouseWheelListener(this);
		}

		public void setExpandedState(boolean expanded) {
			minLabel.setVisible(expanded);
			maxLabel.setVisible(expanded);
			slider.setVisible(expanded);
		}

		private String toLabel(double value) {
			return String.format(Locale.ENGLISH, "%.02f", value); //$NON-NLS-1$
		}

		public void setValue(double value) {
			textField.setText(toLabel(value));

			double relValue = (value-min)/(max-min);
			int newValue = slider.getMinimum()+(int)((slider.getMaximum()-slider.getMinimum()) * relValue);

			slider.setValue(newValue);
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
			double value = slider.getValue();
			double range = (double)slider.getMaximum()-(double)slider.getMinimum();

			value = min + (max-min)*(value/range);
			textField.setText(toLabel(value));

//			if(!slider.getValueIsAdjusting()) {
//			}
			refreshGraph();
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==textField) {
				float value = Float.parseFloat(textField.getText());
				setValue(value);
			}


			refreshGraph();
		}
	}

	private class PainteGraphTransferHandler extends TransferHandler {


		@Override
		public boolean canImport(TransferSupport info) {
			if (!info.isDrop() || !info.isDataFlavorSupported(UIUtil.localObjectFlavor)) {
				return false;
			}

			// Ensure only copy mode is accepted
		    boolean copySupported = (COPY & info.getSourceDropActions()) == COPY;
		    if (copySupported) {
		        info.setDropAction(COPY);
		        return true;
		    }

			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean importData(TransferSupport info) {
			if (!canImport(info)) {
				return false;
			}
			try {
				Object[] values = (Object[]) info.getTransferable()
						.getTransferData(UIUtil.localObjectFlavor);
				for (int i = 0; i < values.length; i++) {
					PaIntEParamsWrapper params = (PaIntEParamsWrapper)values[i];
					addParamsPanel(null).setParams(params.getParams());
				}
				return true;
			} catch (UnsupportedFlavorException ufe) {
				ufe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			return false;
		}
	}

	private class GraphComponent extends JComponent {

		private static final long serialVersionUID = -3436549345676825118L;

		private final PaIntEGraph graph;

		public GraphComponent() {
			graph = new PaIntEGraph();
			graph.getCurve().setMaxSampleCount(200);
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
			graph.getCurve().setColor(panel.color());
			graph.getCurve().paint(g, panel.getParams(), area,
					graph.getXAxis(), graph.getYAxis());
		}

		/**
		 * @see javax.swing.JComponent#getPreferredSize()
		 */
		@Override
		public Dimension getPreferredSize() {
			// TODO Auto-generated method stub
			return super.getPreferredSize();
		}

		/**
		 * @see javax.swing.JComponent#getMinimumSize()
		 */
		@Override
		public Dimension getMinimumSize() {
			return new Dimension(300, 200);
		}

	}
}
