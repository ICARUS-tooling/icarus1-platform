/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools.corpus;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;

import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.language.corpus.CorpusMetaData;
import net.ikarus_systems.icarus.language.corpus.CorpusRegistry;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.plugins.language_tools.LanguageToolsConstants;
import net.ikarus_systems.icarus.resources.Localizer;
import net.ikarus_systems.icarus.resources.ResourceDomain;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.opi.Commands;
import net.ikarus_systems.icarus.util.opi.Message;
import net.ikarus_systems.icarus.util.opi.ResultMessage;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusPropertiesView extends View {

	private JLabel infoLabel;
	
	private JLabel nameLabel;
	private JLabel typeLabel;
	private JLabel goldLabel;
	private JLabel loadedLabel;
	private JLabel editableLabel;
	
	private JTable propertiesTable;
	private PropertiesTableModel propertiesTableModel;
	private JLabel propertiesLabel;
	
	private JTable metaDataTable;
	private MetaDataTableModel metaDataTableModel;
	private JLabel metaDataLabel;
	
	private JScrollPane scrollPane;
	private JPanel contentPanel;
	
	private Corpus corpus;
	
	private JPopupMenu popupMenu;
	
	private Handler handler;
	private CallbackHandler callbackHandler;

	public CorpusPropertiesView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		URL actionLocation = CorpusPropertiesView.class.getResource("corpus-properties-view-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: corpus-properties-view-actions.xml"); //$NON-NLS-1$
		
		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.getLogger(CorpusPropertiesView.class).log(LoggerFactory.record(Level.SEVERE, 
					"Failed to load actions from file", e)); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}
		
		handler = new Handler();

		// Info label
		infoLabel = new JLabel();
		infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		infoLabel.setVerticalAlignment(SwingConstants.TOP);
		ResourceManager.getInstance().getGlobalDomain().prepareComponent(
				infoLabel, "plugins.languageTools.corpusPropertiesView.notAvailable", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);
		
		contentPanel = new JPanel();		
		GroupLayout layout = new GroupLayout(contentPanel);
		contentPanel.setLayout(layout);
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		GroupLayout.ParallelGroup hGroup = layout.createParallelGroup(Alignment.LEADING);
		
		Border border = new EmptyBorder(0, 3, 0, 3);
		Border titleBoarder = new EmptyBorder(5, 3, 5, 3);
		
		// Name
		nameLabel = new JLabel();
		nameLabel.setBorder(titleBoarder);
		nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
		vGroup.addComponent(nameLabel);
		hGroup.addComponent(nameLabel);
		
		// Type
		typeLabel = new JLabel();
		typeLabel.setBorder(border);
		typeLabel.setHorizontalAlignment(SwingConstants.LEFT);
		vGroup.addComponent(typeLabel);
		hGroup.addComponent(typeLabel);
		
		// Loaded label
		loadedLabel = new JLabel();
		loadedLabel.setBorder(border);
		loadedLabel.setHorizontalAlignment(SwingConstants.LEFT);
		vGroup.addComponent(loadedLabel);
		hGroup.addComponent(loadedLabel);
		
		// Gold label
		goldLabel = new JLabel();
		goldLabel.setBorder(border);
		goldLabel.setHorizontalAlignment(SwingConstants.LEFT);
		vGroup.addComponent(goldLabel);
		hGroup.addComponent(goldLabel);
		
		// Editable label
		editableLabel = new JLabel();
		editableLabel.setBorder(border);
		editableLabel.setHorizontalAlignment(SwingConstants.LEFT);
		vGroup.addComponent(editableLabel);
		hGroup.addComponent(editableLabel);
		
		// Properties table
		propertiesLabel = new JLabel();
		propertiesLabel.setBorder(titleBoarder);
		propertiesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		vGroup.addComponent(propertiesLabel);
		hGroup.addComponent(propertiesLabel);
		propertiesTableModel = new PropertiesTableModel();
		propertiesTable = new JTable(propertiesTableModel);
		propertiesTable.getTableHeader().setReorderingAllowed(false);
		propertiesTable.addMouseListener(handler);
		vGroup.addComponent(propertiesTable.getTableHeader());
		vGroup.addComponent(propertiesTable);
		hGroup.addComponent(propertiesTable.getTableHeader());
		hGroup.addComponent(propertiesTable);
		
		// MetaData table
		metaDataLabel = new JLabel();
		metaDataLabel.setBorder(titleBoarder);
		metaDataLabel.setHorizontalAlignment(SwingConstants.LEFT);
		vGroup.addComponent(metaDataLabel);
		hGroup.addComponent(metaDataLabel);
		metaDataTableModel = new MetaDataTableModel();
		metaDataTable = new JTable(metaDataTableModel);
		metaDataTable.getTableHeader().setReorderingAllowed(false);
		metaDataTable.addMouseListener(handler);
		vGroup.addComponent(metaDataTable.getTableHeader());
		vGroup.addComponent(metaDataTable);
		hGroup.addComponent(metaDataTable.getTableHeader());
		hGroup.addComponent(metaDataTable);
		
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);
		
		// Scroll pane
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		
		// ToolBar
		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.languageTools.corpusPropertiesView.toolBarList", null); //$NON-NLS-1$
		toolBar.add(Box.createHorizontalGlue());
		
		container.setLayout(new BorderLayout());
		container.add(toolBar, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		container.setPreferredSize(new Dimension(220, 200));
		container.setMinimumSize(new Dimension(180, 150));
		
		showDefaultInfo();
		
		CorpusRegistry.getInstance().addListener(Events.REMOVED, handler);
		CorpusRegistry.getInstance().addListener(Events.CHANGED, handler);

		registerActionCallbacks();
		refreshActions();
		
		addBroadcastListener(LanguageToolsConstants.CORPUS_EXPLORER_SELECTION_CHANGED, handler);
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
					"plugins.languageTools.corpusPropertiesView.popupMenuList", options); //$NON-NLS-1$
			
			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.getLogger(CorpusPropertiesView.class).log(LoggerFactory.record(
						Level.SEVERE, "Unable to create popup menu")); //$NON-NLS-1$
			}
		}
		
		if(popupMenu!=null) {			
			popupMenu.show(trigger.getComponent(), trigger.getX(), trigger.getY());
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#close()
	 */
	@Override
	public void close() {
		corpus = null;
		CorpusRegistry.getInstance().removeListener(handler);
		removeBroadcastListener(handler);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#isClosable()
	 */
	@Override
	public boolean isClosable() {
		return true;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		displayCorpus(null);
	}
	
	private void displayCorpus(Corpus corpus) {
		Corpus currentCorpus = getCorpus();
		if(currentCorpus==corpus) {
			return;
		}
		
		this.corpus = corpus;
		
		if(corpus==null) {
			showDefaultInfo();
		} else {
			refresh();
			scrollPane.setViewportView(contentPanel);
		}
	}
	
	private static String COLON = ": "; //$NON-NLS-1$
	
	private void refresh() {
		if(corpus==null) {
			return;
		}
		
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		
		// Name
		nameLabel.setText(resourceDomain.get(
				"plugins.languageTools.labels.name")+COLON+corpus.getName()); //$NON-NLS-1$
		
		// Type
		typeLabel.setText(resourceDomain.get(
				"plugins.languageTools.labels.type")+COLON+ //$NON-NLS-1$
				CorpusRegistry.getInstance().getExtension(corpus).getId());
		
		// Loaded
		loadedLabel.setText(resourceDomain.get(
				"plugins.languageTools.labels.loaded")+COLON+Boolean.toString(corpus.isLoaded())); //$NON-NLS-1$
		
		// Editable
		editableLabel.setText(resourceDomain.get(
				"plugins.languageTools.labels.editable")+COLON+Boolean.toString(corpus.isEditable())); //$NON-NLS-1$
		
		// Gold
		goldLabel.setText(resourceDomain.get(
				"plugins.languageTools.labels.gold")+COLON+Boolean.toString(corpus.hasGold())); //$NON-NLS-1$
		
		if(corpus.getProperties().isEmpty()) {
			propertiesLabel.setText(resourceDomain.get(
					"plugins.languageTools.corpusPropertiesView.noProperties")); //$NON-NLS-1$
			propertiesTable.setVisible(false);
			propertiesTable.getTableHeader().setVisible(false);
		} else {
			propertiesLabel.setText(resourceDomain.get(
					"plugins.languageTools.labels.properties")+COLON); //$NON-NLS-1$
			propertiesTable.setVisible(true);
			propertiesTable.getTableHeader().setVisible(true);
		}
		propertiesTableModel.reload();
		
		if(!corpus.isLoaded()) {
			metaDataLabel.setText(resourceDomain.get(
					"plugins.languageTools.corpusPropertiesView.notLoaded")); //$NON-NLS-1$
			metaDataTable.setVisible(false);
			metaDataTable.getTableHeader().setVisible(false);
		} else if(corpus.getMetaData()==null) {
			metaDataLabel.setText(resourceDomain.get(
					"plugins.languageTools.corpusPropertiesView.noMetaData")); //$NON-NLS-1$
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
	
	private Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#handleRequest(net.ikarus_systems.icarus.util.opi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.DISPLAY.equals(message.getCommand())) {
			Object data = message.getData();
			if(data instanceof Corpus) {
				displayCorpus((Corpus) data);
				return message.successResult(null);
			} else {
				return message.unsupportedDataResult();
			}
		} else {
			return message.unknownRequestResult();
		}
	}

	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.addHandler("plugins.languageTools.corpusPropertiesView.copyPropertiesAction",  //$NON-NLS-1$
				callbackHandler, "copyProperties"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.corpusPropertiesView.copyPropertyAction",  //$NON-NLS-1$
				callbackHandler, "copyProperty"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.corpusPropertiesView.copyMetaDataAction",  //$NON-NLS-1$
				callbackHandler, "copyMetaData"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.corpusPropertiesView.copyAllPropertiesAction",  //$NON-NLS-1$
				callbackHandler, "copyAllProperties"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.corpusPropertiesView.exportPropertiesAction",  //$NON-NLS-1$
				callbackHandler, "exportProperties"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.corpusPropertiesView.editCorpusAction",  //$NON-NLS-1$
				callbackHandler, "editCorpus"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.corpusPropertiesView.inspectCorpusAction",  //$NON-NLS-1$
				callbackHandler, "inspectCorpus"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.corpusPropertiesView.loadCorpusAction",  //$NON-NLS-1$
				callbackHandler, "loadCorpus"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.corpusPropertiesView.freeCorpusAction",  //$NON-NLS-1$
				callbackHandler, "freeCorpus"); //$NON-NLS-1$
	}

	private class PropertiesTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -4487347147496279674L;
		
		private List<String> keys;
		
		public PropertiesTableModel() {
			reload();
		}
		
		private void reload() {
			if(corpus==null) {
				keys = null;
			} else {
				Map<String, Object> properties = corpus.getProperties();
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
			if(keys==null || corpus==null) {
				return null;
			}
			
			String key = keys.get(rowIndex);
			return columnIndex==0 ? key : corpus.getProperty(key);
		}
		
	}
		
	private static String[] metaDataKeys = {
		CorpusMetaData.MIN_LENGTH,
		CorpusMetaData.MAX_LENGTH,
		CorpusMetaData.AVERAGE_LENGTH,
		CorpusMetaData.TOTAL_LENGTH,
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
			if(corpus==null) {
				return 0;
			}
			if(corpus.getMetaData()==null) {
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
			if(corpus==null) {
				return null;
			}
			CorpusMetaData metaData = corpus.getMetaData();
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
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
				
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
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			
			// Handle deleted or changed corpora
			if(sender==CorpusRegistry.getInstance()) {
				Corpus corpus = (Corpus) event.getProperty("corpus"); //$NON-NLS-1$
				if(corpus!=null && corpus==getCorpus()) {
					
					if(Events.DELETED.equals(event.getName())) {
						displayCorpus(null);
					} else if(Events.CHANGED.equals(event.getName())) {
						refresh();
					}
				}
				return;
			}
			
			// Handle changed selection in corpus explorer view
			Object item = event.getProperty("item"); //$NON-NLS-1$			
			Corpus corpus = null;
			
			if(item instanceof Corpus) {
				corpus = (Corpus) item;
			}
			
			displayCorpus(corpus);
		}

		/**
		 * @see net.ikarus_systems.icarus.resources.Localizer#localize(java.lang.Object)
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
		
		public void inspectCorpus(ActionEvent e) {
			Corpus corpus = getCorpus();
			if(corpus==null) {
				return;
			}
			
			Message message = new Message(Commands.DISPLAY, corpus, null);
			sendRequest(LanguageToolsConstants.CORPUS_INSPECT_VIEW_ID, message);
		}
		
		public void editCorpus(ActionEvent e) {
			Corpus corpus = getCorpus();
			if(corpus==null) {
				return;
			}
			
			Message message = new Message(Commands.EDIT, corpus, null);
			sendRequest(LanguageToolsConstants.CORPUS_EDIT_VIEW_ID, message);
		}
		
		public void loadCorpus(ActionEvent e) {
			// TODO
		}
		
		public void freeCorpus(ActionEvent e) {
			// TODO
		}
	}
}
