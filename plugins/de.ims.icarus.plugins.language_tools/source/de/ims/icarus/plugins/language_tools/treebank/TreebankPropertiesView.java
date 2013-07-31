/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.language_tools.treebank;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import de.ims.icarus.language.DataType;
import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankListDelegate;
import de.ims.icarus.language.treebank.TreebankMetaData;
import de.ims.icarus.language.treebank.TreebankRegistry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.language_tools.LanguageToolsConstants;
import de.ims.icarus.resources.Localizer;
import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.table.TooltipTableCellRenderer;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankPropertiesView extends View {

	private JTextArea infoLabel;
	
	private JTextArea propertiesArea;
	
	private JTable propertiesTable;
	private PropertiesTableModel propertiesTableModel;
	private JLabel propertiesLabel;
	
	private JTable metaDataTable;
	private MetaDataTableModel metaDataTableModel;
	private JLabel metaDataLabel;
	
	private JScrollPane scrollPane;
	private JPanel contentPanel;
	
	private Treebank treebank;
	
	private JPopupMenu popupMenu;
	
	private Handler handler;
	private CallbackHandler callbackHandler;

	public TreebankPropertiesView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		if(!defaultLoadActions(TreebankPropertiesView.class, "treebank-properties-view-actions.xml")) { //$NON-NLS-1$
			return;
		}
		
		handler = new Handler();

		// Info label
		infoLabel = UIUtil.defaultCreateInfoLabel(container);
		ResourceManager.getInstance().getGlobalDomain().prepareComponent(
				infoLabel, "plugins.languageTools.treebankPropertiesView.notAvailable", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);
		
		contentPanel = new JPanel(new GridBagLayout());
		
		Border titleBoarder = new EmptyBorder(5, 3, 5, 3);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.weightx = 100;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		// General properties
		propertiesArea = new JTextArea();
		propertiesArea.setFont(UIManager.getFont("Label.font")); //$NON-NLS-1$
		UIUtil.disableHtml(propertiesArea);
		propertiesArea.setBorder(titleBoarder);
		propertiesArea.setEditable(false);
		propertiesArea.addMouseListener(handler);
		propertiesArea.setFocusable(false);

		// Shared renderer for tables
		TableCellRenderer renderer = new TooltipTableCellRenderer();
		UIUtil.disableHtml(renderer);
		
		// Properties table
		propertiesLabel = new JLabel();
		propertiesLabel.setBorder(titleBoarder);
		propertiesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		propertiesTableModel = new PropertiesTableModel();
		propertiesTable = new JTable(propertiesTableModel);
		propertiesTable.setDefaultRenderer(Object.class, renderer);
		propertiesTable.getTableHeader().setReorderingAllowed(false);
		propertiesTable.addMouseListener(handler);
		UIUtil.enableToolTip(propertiesTable);
		propertiesTable.setFocusable(false);
		
		// MetaData table
		metaDataLabel = new JLabel();
		metaDataLabel.setBorder(titleBoarder);
		metaDataLabel.setHorizontalAlignment(SwingConstants.LEFT);
		metaDataTableModel = new MetaDataTableModel();
		metaDataTable = new JTable(metaDataTableModel);
		metaDataTable.setDefaultRenderer(Object.class, renderer);
		metaDataTable.getTableHeader().setReorderingAllowed(false);
		metaDataTable.addMouseListener(handler);
		UIUtil.enableToolTip(metaDataTable);
		metaDataTable.setFocusable(false);

		contentPanel.add(propertiesArea, gbc);
		contentPanel.add(propertiesLabel, gbc);
		contentPanel.add(propertiesTable.getTableHeader(), gbc);
		contentPanel.add(propertiesTable, gbc);
		contentPanel.add(metaDataLabel, gbc);
		contentPanel.add(metaDataTable.getTableHeader(), gbc);
		contentPanel.add(metaDataTable, gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 100;
		contentPanel.add(Box.createGlue(), gbc);
		
		// Scroll pane
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		
		// ToolBar
		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.languageTools.treebankPropertiesView.toolBarList", null); //$NON-NLS-1$
		
		container.setLayout(new BorderLayout());
		container.add(toolBar, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		container.setPreferredSize(new Dimension(220, 200));
		container.setMinimumSize(new Dimension(180, 150));		
		
		showDefaultInfo();
		
		TreebankRegistry.getInstance().addListener(Events.REMOVED, handler);
		TreebankRegistry.getInstance().addListener(Events.CHANGED, handler);

		registerActionCallbacks();
		refreshActions();
		
		addBroadcastListener(LanguageToolsConstants.TREEBANK_EXPLORER_SELECTION_CHANGED, handler);
	}

	private void showDefaultInfo() {
		scrollPane.setViewportView(infoLabel);
	}

	private void refreshActions() {
		// TODO
	}
	
	private void showPopup(MouseEvent trigger) {
		if(popupMenu==null) {
			// Create new popup menu
			
			Options options = new Options();
			popupMenu = getDefaultActionManager().createPopupMenu(
					"plugins.languageTools.treebankPropertiesView.popupMenuList", options); //$NON-NLS-1$
			
			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}
		
		if(popupMenu!=null) {			
			popupMenu.show(trigger.getComponent(), trigger.getX(), trigger.getY());
		}
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#close()
	 */
	@Override
	public void close() {
		treebank = null;
		TreebankRegistry.getInstance().removeListener(handler);
		removeBroadcastListener(handler);
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		displayTreebank(null);
	}
	
	private void displayTreebank(Treebank treebank) {
		Treebank currentTreebank = getTreebank();
		if(currentTreebank==treebank) {
			return;
		}
		
		if(currentTreebank!=null) {
			currentTreebank.removeListener(handler);
		}
		
		this.treebank = treebank;
		
		if(treebank==null) {
			showDefaultInfo();
		} else {
			treebank.addListener(null, handler);
			refresh();
			contentPanel.setBackground(UIManager.getColor("TextArea.background")); //$NON-NLS-1$
			scrollPane.setViewportView(contentPanel);
		}
	}
	
	private static String COLON = ": "; //$NON-NLS-1$
	private static String LF = "\n"; //$NON-NLS-1$
	
	private void refresh() {
		if(treebank==null) {
			return;
		}
		
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		
		StringBuilder sb = new StringBuilder(200);
		
		// General properties
		sb.append(resourceDomain.get("plugins.languageTools.labels.name")) //$NON-NLS-1$
		.append(COLON)
		.append(treebank.getName())
		.append(LF);
		
		// Type
		sb.append(resourceDomain.get("plugins.languageTools.labels.type")) //$NON-NLS-1$
		.append(COLON)
		.append(TreebankRegistry.getInstance().getExtension(treebank).getId())
		.append(LF);
		
		// Loaded
		sb.append(resourceDomain.get("plugins.languageTools.labels.loaded")) //$NON-NLS-1$
		.append(COLON)
		.append(Boolean.toString(treebank.isLoaded()))
		.append(LF);
		
		// Editable
		sb.append(resourceDomain.get("plugins.languageTools.labels.editable")) //$NON-NLS-1$
		.append(COLON)
		.append(Boolean.toString(treebank.isEditable()))
		.append(LF);
		
		// Gold
		sb.append(resourceDomain.get("plugins.languageTools.labels.gold")) //$NON-NLS-1$
		.append(COLON)
		.append(Boolean.toString(treebank.supportsType(DataType.GOLD))); // no LF on last line!
		
		propertiesArea.setText(sb.toString());
		
		if(treebank.getProperties().isEmpty()) {
			propertiesLabel.setText(resourceDomain.get(
					"plugins.languageTools.treebankPropertiesView.noProperties")); //$NON-NLS-1$
			propertiesTable.setVisible(false);
			propertiesTable.getTableHeader().setVisible(false);
		} else {
			propertiesLabel.setText(resourceDomain.get(
					"plugins.languageTools.labels.properties")+COLON); //$NON-NLS-1$
			propertiesTable.setVisible(true);
			propertiesTable.getTableHeader().setVisible(true);
		}
		propertiesTableModel.reload();
		
		if(!treebank.isLoaded()) {
			metaDataLabel.setText(resourceDomain.get(
					"plugins.languageTools.treebankPropertiesView.notLoaded")); //$NON-NLS-1$
			metaDataTable.setVisible(false);
			metaDataTable.getTableHeader().setVisible(false);
		} else if(treebank.getMetaData()==null) {
			metaDataLabel.setText(resourceDomain.get(
					"plugins.languageTools.treebankPropertiesView.noMetaData")); //$NON-NLS-1$
			metaDataTable.setVisible(false);
			metaDataTable.getTableHeader().setVisible(false);
		} else {
			metaDataLabel.setText(resourceDomain.get(
					"plugins.languageTools.labels.metaData")+COLON); //$NON-NLS-1$
			metaDataTable.setVisible(true);
			metaDataTable.getTableHeader().setVisible(true);
		}
		metaDataTableModel.reload();
	}
	
	private Treebank getTreebank() {
		return treebank;
	}

	/**
	 * Accepted commands:
	 * <ul>
	 * <li>{@link Commands#DISPLAY}</li>
	 * <li>{@link Commands#CLEAR}</li>
	 * </ul>
	 * 
	 * @see de.ims.icarus.plugins.core.View#handleRequest(de.ims.icarus.util.mpi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.DISPLAY.equals(message.getCommand())) {
			Object data = message.getData();
			if(data instanceof Treebank) {
				displayTreebank((Treebank) data);
				
				return message.successResult(this, null);
			} else {
				return message.unsupportedDataResult(this);
			}
		} else if(Commands.CLEAR.equals(message.getCommand())) {
			reset();
			
			return message.successResult(this, null);
		} else {
			return message.unknownRequestResult(this);
		}
	}

	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.addHandler("plugins.languageTools.treebankPropertiesView.copyPropertiesAction",  //$NON-NLS-1$
				callbackHandler, "copyProperties"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.treebankPropertiesView.copyPropertyAction",  //$NON-NLS-1$
				callbackHandler, "copyProperty"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.treebankPropertiesView.copyMetaDataAction",  //$NON-NLS-1$
				callbackHandler, "copyMetaData"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.treebankPropertiesView.copyAllPropertiesAction",  //$NON-NLS-1$
				callbackHandler, "copyAllProperties"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.treebankPropertiesView.exportPropertiesAction",  //$NON-NLS-1$
				callbackHandler, "exportProperties"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.treebankPropertiesView.editTreebankAction",  //$NON-NLS-1$
				callbackHandler, "editTreebank"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.treebankPropertiesView.inspectTreebankAction",  //$NON-NLS-1$
				callbackHandler, "inspectTreebank"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.treebankPropertiesView.loadTreebankAction",  //$NON-NLS-1$
				callbackHandler, "loadTreebank"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.treebankPropertiesView.freeTreebankAction",  //$NON-NLS-1$
				callbackHandler, "freeTreebank"); //$NON-NLS-1$
	}

	private class PropertiesTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -4487347147496279674L;
		
		private List<String> keys;
		
		public PropertiesTableModel() {
			reload();
		}
		
		private void reload() {
			if(treebank==null) {
				keys = null;
			} else {
				Map<String, Object> properties = treebank.getProperties();
				keys = new ArrayList<>(properties.keySet());
				Collections.sort(keys);
			}
			
			fireTableDataChanged();
		}

		/**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return keys==null ? 0 : keys.size();
		}

		/**
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			String key = column==0 ? 
					"plugins.languageTools.labels.property" //$NON-NLS-1$
					: "plugins.languageTools.labels.value"; //$NON-NLS-1$
			return ResourceManager.getInstance().get(key);
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return 2;
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(keys==null || treebank==null) {
				return null;
			}
			
			String key = keys.get(rowIndex);
			return columnIndex==0 ? key : treebank.getProperty(key);
		}
		
	}
		
	private static String[] metaDataKeys = {
		TreebankMetaData.MIN_LENGTH,
		TreebankMetaData.MAX_LENGTH,
		TreebankMetaData.AVERAGE_LENGTH,
		TreebankMetaData.TOTAL_LENGTH,
	};
	
	private class MetaDataTableModel extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7792685469229446994L;
		
		private void reload() {
			fireTableDataChanged();
		}

		/**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			if(treebank==null) {
				return 0;
			}
			if(treebank.getMetaData()==null) {
				return 0;
			}
			return metaDataKeys.length;
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return 2;
		}

		/**
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			String key = column==0 ? 
					"plugins.languageTools.labels.key" //$NON-NLS-1$
					: "plugins.languageTools.labels.value"; //$NON-NLS-1$
			return ResourceManager.getInstance().get(key);
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(treebank==null) {
				return null;
			}
			TreebankMetaData metaData = treebank.getMetaData();
			if(metaData==null) {
				return null;
			}
			
			String key = metaDataKeys[rowIndex];
			return columnIndex==0 ? key : metaData.getValue(key);
		}
		
	}
	
	private class Handler extends MouseAdapter implements EventListener, Localizer {
		
		private void maybeShowPopup(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			
			// Handle deleted or changed treebanks
			if(sender==TreebankRegistry.getInstance()) {
				Treebank treebank = (Treebank) event.getProperty("treebank"); //$NON-NLS-1$
				if(treebank!=null && treebank==getTreebank()) {
					
					if(Events.DELETED.equals(event.getName())) {
						displayTreebank(null);
					} else if(Events.CHANGED.equals(event.getName())) {
						refresh();
					}
				}
				return;
			}
			
			// Any changes within the treebank require refresh of displayed information
			if(sender==getTreebank()) {
				refresh();
				return;
			}
			
			// Handle changed selection in treebank explorer view
			Object item = event.getProperty("item"); //$NON-NLS-1$			
			Treebank treebank = null;
			
			if(item instanceof Treebank) {
				treebank = (Treebank) item;
			}
			
			displayTreebank(treebank);
		}

		/**
		 * @see de.ims.icarus.resources.Localizer#localize(java.lang.Object)
		 */
		@Override
		public void localize(Object item) {
			refresh();
		}
		
	}
	
	public final class CallbackHandler {
		
		private CallbackHandler() {
			// no-op
		}
		
		public void copyProperties(ActionEvent e) {
			// TODO
		}
		
		public void copyProperty(ActionEvent e) {
			// TODO
		}
		
		public void copyMetaData(ActionEvent e) {
			// TODO
		}
		
		public void copyAllProperties(ActionEvent e) {
			// TODO
		}
		
		public void exportProperties(ActionEvent e) {
			// TODO
		}
		
		public void inspectTreebank(ActionEvent e) {
			Treebank treebank = getTreebank();
			if(treebank==null) {
				return;
			}
			
			try {
				ContentType contentType = ContentTypeRegistry.getInstance().getTypeForClass(Treebank.class);
				
				Options options = new Options();
				options.put(Options.CONTENT_TYPE, contentType);
				
				TreebankListDelegate delegate = TreebankRegistry.getInstance().getListDelegate(treebank);
				
				Message message = new Message(this, Commands.DISPLAY, delegate, options);
				
				sendRequest(null, message);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to inspect treebank", ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
		}
		
		public void editTreebank(ActionEvent e) {
			Treebank treebank = getTreebank();
			if(treebank==null) {
				return;
			}
			
			try {
				Message message = new Message(this, Commands.EDIT, treebank, null);
				
				sendRequest(LanguageToolsConstants.TREEBANK_EDIT_VIEW_ID, message);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to edit treebank", ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
		}
		
		public void loadTreebank(ActionEvent e) {
			// TODO
		}
		
		public void freeTreebank(ActionEvent e) {
			// TODO
		}
	}
}
