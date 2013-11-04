/* 
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus G�rtner and Gregor Thiele
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
package de.ims.icarus.plugins.errormining;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.java.plugin.registry.Extension;

import de.ims.icarus.Core;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.ExtensionListModel;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.errormining.ngram_tools.NGramDataList;
import de.ims.icarus.plugins.errormining.ngram_tools.NGramDataListDependency;
import de.ims.icarus.plugins.matetools.conll.CONLL09SentenceDataGoldReader;
import de.ims.icarus.plugins.search_tools.SearchToolsConstants;
import de.ims.icarus.plugins.search_tools.view.SearchHistory;
import de.ims.icarus.plugins.search_tools.view.SearchHistoryListCellRenderer;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchDescriptor;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.SearchTargetSelector;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.dialog.SelectFormEntry;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.data.DataContainer;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.util.location.DefaultFileLocation;
import de.ims.icarus.util.location.UnsupportedLocationException;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class ErrorMiningView extends View {
	
	protected JList<SearchDescriptor> searchHistoryList;
	protected SearchHistory searchHistory;
	
	protected NGramEditor currentNGramEditor;

	protected Handler handler;
	
	protected CallbackHandler callbackHandler;	
	
	protected JPopupMenu popupMenu;
	
	public ErrorMiningView(){
		//noop
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		

		// Load actions
		URL actionLocation = ErrorMiningView.class
				.getResource("errormining-view-actions.xml"); //$NON-NLS-1$
		if (actionLocation == null)
			throw new CorruptedStateException(
					"Missing resources: errormining-view-actions.xml"); //$NON-NLS-1$

		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}

		handler = new Handler();	
		
		// Header tool-bar
		Options options = new Options("multiline", true); //$NON-NLS-1$
		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.errorMining.errorMiningView.toolBarList", options); //$NON-NLS-1$
			
		//current NGram Editor
		currentNGramEditor = new NGramEditor();
		// Set initially empty NGram
		currentNGramEditor.setEditingItem(newDescriptor());
		
		
		JPanel editorPanel = new JPanel(new BorderLayout());
		editorPanel.add(toolBar, BorderLayout.NORTH);
		editorPanel.add(currentNGramEditor.getEditorComponent(), BorderLayout.CENTER);

		// History tool-bar
		options = new Options("multiline", true); //$NON-NLS-1$
		toolBar = getDefaultActionManager().createToolBar(
				"plugins.errorMining.errorMiningView.historyToolBarList", options); //$NON-NLS-1$
		
		// History
		searchHistory = SearchHistory.getSharedInstance();
		searchHistoryList = new JList<>(searchHistory);
		searchHistoryList.setBorder(UIUtil.defaultContentBorder);
		searchHistoryList.setCellRenderer(new SearchHistoryListCellRenderer());
		searchHistoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchHistoryList.addListSelectionListener(handler);
		searchHistoryList.addMouseListener(handler);
		UIUtil.enableToolTip(searchHistoryList);
		JScrollPane scrollPane = new JScrollPane(searchHistoryList);
		scrollPane.setBorder(UIUtil.topLineBorder);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		UIUtil.enableRighClickListSelection(searchHistoryList);
		
		JPanel historyPanel = new JPanel(new BorderLayout());
		historyPanel.setBorder(UIUtil.topLineBorder);
		historyPanel.add(toolBar, BorderLayout.NORTH);
		historyPanel.add(scrollPane, BorderLayout.CENTER);
		
		container.setLayout(new BorderLayout());
		container.add(editorPanel, BorderLayout.NORTH);
		container.add(historyPanel, BorderLayout.CENTER);
				
		
		// History
//		ngramHistoryTreeModel = new NGramHistoryTreeModel();
//		ngramHistoryTree = new JTree(ngramHistoryTreeModel);
//		ngramHistoryTree.setEditable(false);
//		ngramHistoryTree.setRootVisible(false);
//		ngramHistoryTree.setShowsRootHandles(true);
//		ngramHistoryTree.addTreeSelectionListener(handler);
//		ngramHistoryTree.setCellRenderer(new NGramHistoryTreeCellRenderer());
//		JScrollPane scrollPane = new JScrollPane(ngramHistoryTree);
//		scrollPane.setBorder(UIUtil.topLineBorder);
//		UIUtil.defaultSetUnitIncrement(scrollPane);
		
		
//		JPanel contentPanel = new JPanel(new BorderLayout());
//		contentPanel.add(currentNGramEditor.getEditorComponent(), BorderLayout.NORTH);
//		contentPanel.add(scrollPane, BorderLayout.CENTER);
//		
//		container.setLayout(new BorderLayout());
//		container.add(createToolBar(), BorderLayout.NORTH);
//		container.add(contentPanel, BorderLayout.CENTER);
		
		
		registerActionCallbacks();

		refreshActions();
	}
	
	
	
	protected void refreshActions() {
		ActionManager actionManager = getDefaultActionManager();		
		
		// Refresh editor actions
		SearchDescriptor descriptor = currentNGramEditor.getEditingItem();
		Search search = descriptor == null ? null : descriptor.getSearch();

		boolean canRun = descriptor != null && descriptor.getTarget() != null;

		actionManager.setEnabled(canRun,
				"plugins.errorMining.errorMiningView.executeNGramAction"); //$NON-NLS-1$		

		// Refresh history actions
		descriptor = searchHistoryList.getSelectedValue();
		boolean selected = descriptor!=null;
		search = selected ? descriptor.getSearch() : null;
		boolean canCancel = search!=null && search.isRunning();
		boolean hasResult = search!=null && search.getResult()!=null;
		
		actionManager.setEnabled(hasResult,
				"plugins.errorMining.errorMiningView.viewResultAction"); //$NON-NLS-1$		
		actionManager.setEnabled(canCancel,
				"plugins.errorMining.errorMiningView.cancelNGramAction"); //$NON-NLS-1$		
		actionManager.setEnabled(searchHistory.getSize()>0, 
				"plugins.errorMining.errorMiningView.clearHistoryAction"); //$NON-NLS-1$
		actionManager.setEnabled(selected, 
				"plugins.errorMining.errorMiningView.viewNGramAction",  //$NON-NLS-1$
				"plugins.errorMining.errorMiningView.removeNGramAction"); //$NON-NLS-1$
			
	}
	
	
	protected void showPopup(MouseEvent trigger) {
		if(popupMenu==null) {
			// Create new popup menu
			
			Options options = new Options();
			popupMenu = getDefaultActionManager().createPopupMenu(
					"plugins.searchTools.searchManagerView.historyPopupMenuList", options); //$NON-NLS-1$
			
			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}
		
		if(popupMenu!=null) {
			popupMenu.show(searchHistoryList, trigger.getX(), trigger.getY());
		}
	}
	
	
	protected void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		
		
		actionManager.addHandler("plugins.errorMining.errorMiningView.newNGramAction",  //$NON-NLS-1$
				callbackHandler, "newNGram"); //$NON-NLS-1$
		actionManager.addHandler("plugins.errorMining.errorMiningView.executeNGramAction",  //$NON-NLS-1$
				callbackHandler, "executeNGram"); //$NON-NLS-1$
		actionManager.addHandler("plugins.errorMining.errorMiningView.cancelNGramAction",  //$NON-NLS-1$
				callbackHandler, "cancelNGram"); //$NON-NLS-1$
		actionManager.addHandler("plugins.errorMining.errorMiningView.removeNGramAction",  //$NON-NLS-1$
				callbackHandler, "removeNGram"); //$NON-NLS-1$
		actionManager.addHandler("plugins.errorMining.errorMiningView.clearHistoryAction",  //$NON-NLS-1$
				callbackHandler, "clearHistory"); //$NON-NLS-1$
		actionManager.addHandler("plugins.errorMining.errorMiningView.openPreferencesAction",  //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler("plugins.errorMining.errorMiningView.editQueryAction",  //$NON-NLS-1$
				callbackHandler, "editQuery"); //$NON-NLS-1$
		actionManager.addHandler("plugins.errorMining.errorMiningView.viewResultAction",  //$NON-NLS-1$
				callbackHandler, "viewResult"); //$NON-NLS-1$
		actionManager.addHandler("plugins.errorMining.errorMiningView.viewNGramAction",  //$NON-NLS-1$
				callbackHandler, "viewNGram"); //$NON-NLS-1$
		actionManager.addHandler("plugins.errorMining.errorMiningView.selectFactoryAction",  //$NON-NLS-1$
				callbackHandler, "selectFactory"); //$NON-NLS-1$
		actionManager.addHandler("plugins.errorMining.errorMiningView.selectTargetAction",  //$NON-NLS-1$
				callbackHandler, "selectTarget"); //$NON-NLS-1$
		actionManager.addHandler("plugins.errorMining.errorMiningView.editParameterAction",  //$NON-NLS-1$
				callbackHandler, "editParameter"); //$NON-NLS-1$


		
	}
	
	
	//TODO change to ngram
	protected SearchDescriptor newDescriptor() {
		Collection<Extension> factoryExtensions = SearchManager.getSearchFactoryExtensions();
		if(factoryExtensions==null || factoryExtensions.isEmpty())
			throw new IllegalStateException("Cannot create search descriptor - no search factories available"); //$NON-NLS-1$
		
		SearchDescriptor descriptor = new SearchDescriptor();
		descriptor.setFactoryExtension(factoryExtensions.iterator().next());
		System.out.println(descriptor.getFactoryExtension());
		
		return descriptor;
	}
	
	protected void syncEditorViews() {
		if(currentNGramEditor.getEditingItem()==null) {
			return;
		}
		
		Message message = new Message(this, Commands.COMMIT, null, null);
		try {
			sendRequest(null, message);
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to synchronize editor views", e); //$NON-NLS-1$
		}
		
		refreshActions();
	}
	
	protected class Handler extends MouseAdapter implements
			ListSelectionListener, PropertyChangeListener {
		protected Search observedSearch;

		protected Handler() {
			// no-op
		}

		protected void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() != 2 || !SwingUtilities.isLeftMouseButton(e)) {
				return;
			}

			int index = searchHistoryList.locationToIndex(e.getPoint());
			if (index == -1) {
				return;
			}
			Rectangle bounds = searchHistoryList.getCellBounds(index, index);
			if (!bounds.contains(e.getPoint())) {
				return;
			}

			SearchDescriptor descriptor = searchHistoryList.getModel()
					.getElementAt(index);

			try {
				// Display search
				SearchDescriptor clone = descriptor.clone();
				currentNGramEditor.setEditingItem(clone);
				refreshActions();

				// Display result
				SearchResult result = descriptor.getSearchResult();

				if (result == null) {
					return;
				}

				Options options = new Options();
				Message message = new Message(this, Commands.PRESENT, result,
						options);

				sendRequest(SearchToolsConstants.SEARCH_RESULT_VIEW_ID, message);
			} catch (Exception ex) {
				LoggerFactory
						.log(this,
								Level.SEVERE,
								"Failed to view result for search at index: " + index, ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (observedSearch != null) {
				observedSearch.removePropertyChangeListener("state", this); //$NON-NLS-1$
				observedSearch = null;
			}

			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if (descriptor != null) {
				observedSearch = descriptor.getSearch();
			}

			if (observedSearch != null) {
				observedSearch.addPropertyChangeListener("state", this); //$NON-NLS-1$
			}

			refreshActions();
		}

		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			refreshActions();
		}

	}	
	
	
	public final class CallbackHandler {

		private CallbackHandler() {
			// no-op
		}
		
		
		public void newNGram(ActionEvent e) {
			try {
				syncEditorViews();				
				
				currentNGramEditor.setEditingItem(newDescriptor());
				
				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to create new errormining search", ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
		}
		
		
		public void executeNGram(ActionEvent e) {

//			if(currentNGramEditor.getEditingItem()==null) {
//				return;
//			}
//			
//			try {
//				
//				syncEditorViews();
//				SearchDescriptor descriptor = currentNGramEditor.getEditingItem();
//				
//				/*
//				SearchDescriptor updatedDescriptor = currentNGramEditor.getEditingItem();
//				if(updatedDescriptor!=null && updatedDescriptor!=descriptor) {
//					descriptor.setQuery(updatedDescriptor.getQuery());
//				}
//				*/
//				
//				Search search = descriptor.getSearch();
//				if(search!=null && search.isRunning()) {
//					UIUtil.beep();
//					return;
//				}
//				
//				// Generate query
//
//				System.out.println("Search " + descriptor.getSearch()); //$NON-NLS-1$
//				System.out.println("SFactory " + descriptor.getSearchFactory()); //$NON-NLS-1$
//				System.out.println("Target " + descriptor.getTarget()); //$NON-NLS-1$
//				System.out.println("Query " + descriptor.getQuery()); //$NON-NLS-1$
//				System.out.println("Parameter " + descriptor.getParameters()); //$NON-NLS-1$
//
//				// Create a new descriptor without a search object
//				SearchDescriptor clone = descriptor.cloneShallow();
//				// Let factory create a blank new search object
//				if(!clone.createSearch()) {
//					return;
//				}
//				currentNGramEditor.setEditingItem(descriptor.clone());
//				
//				searchHistory.addSearch(clone);
//				//SearchManager.getInstance().executeSearch(clone.getSearch());
//				
//				refreshActions();
//				
//				System.out.println("Finished NGRAM");
//				
//			} catch(Exception ex) {
//				LoggerFactory.log(this, Level.SEVERE, 
//						"Failed to execute search", ex); //$NON-NLS-1$
//				UIUtil.beep();
//				
//				showError(ex);
//			}
			
			
			//#############
			testPoSNGram();
			//testDepRelNGram();
			
			//enable autosave?!
			//ngrams.outputToFile();
		}

		
//Debug stuff
		private void testPoSNGram() {
			
			ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
			System.out.println("Filepath: "  //$NON-NLS-1$
					+ config.getString("plugins.errorMining.appearance.filepath")); //$NON-NLS-1$
			
			String inputFileName = config.getString("plugins.errorMining.appearance.filepath"); //$NON-NLS-1$
			
			//18 Sentences			
			//String  inputFileName = "E:\\test_small_modded.txt"; //$NON-NLS-1$
			
			//CONLL Training English (1334 Sentences)
			//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-development.txt";
			
			//CONLL Training English (39279 Sentences)
			//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-train.txt";
			
			//CONLL Training German 50472 Sentences (Aug)
			//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\tiger_release_aug07.corrected.conll09.txt";
			
			//CONLL Training German 50472 Sentences (Aug)
			//String  inputFileName = "E:\\tiger_release_aug07.corrected.16012013.conll09";

			//int sentencesToRead = 18;
			int sentencesToRead = config.getInteger("plugins.errorMining.appearance.limit"); //$NON-NLS-1$
			
			File file = new File(inputFileName);
			
			
			final List<SentenceData> corpus = new ArrayList<SentenceData>();
			
			Options on = new Options();
			on.put("FringeSTART", 3); //$NON-NLS-1$
			on.put("FringeEND", 5); //$NON-NLS-1$ // 0 = infinity , number = limit
			on.put("NGramLIMIT", 0); //$NON-NLS-1$
			final NGrams ngrams = new NGrams(1, on);
			
			CONLL09SentenceDataGoldReader conellReader = new CONLL09SentenceDataGoldReader();	
			DefaultFileLocation dloc = new DefaultFileLocation(file);
			Options o = null;
			try {
				conellReader.init(dloc, o);
			
			int sentenceNr = 1;
			//System.out.println("ReadSentences: " + sentencesToRead);
			for(int i = 0; i < sentencesToRead; i++){
				SentenceData sd = conellReader.next();
				corpus.add(sd);
				ngrams.initializeUniGrams((DependencyData) sd, sentenceNr);
				sentenceNr++;				
			}
			
			SentenceData sd = conellReader.next();
			
			while (sd != null) {				
				//System.out.println(sentenceNr + " - " + sd.getText());
				corpus.add(sd);
				ngrams.initializeUniGrams((DependencyData) sd, sentenceNr);
				sd = conellReader.next();
				sentenceNr++;				
			}
			
			
			ngrams.nGramResults();
			System.out.println("Corpussize: " + corpus.size()); //$NON-NLS-1$
			
//			List<String> tmpKey = new ArrayList<String>(ngrams.getResult().keySet());
//			Collections.reverse(tmpKey);
//			for(int j = 0; j < tmpKey.size(); j++){				
//				System.out.println("key " + tmpKey.get(j));
//			}
			
			
			}catch (UnsupportedLocationException | IOException | UnsupportedFormatException ex) {
				ex.printStackTrace();
			}
			
			
			//Execute the SwingWorker; the GUI will not freeze
			TaskManager.getInstance().schedule(new ErrorMiningJob(ngrams, corpus), TaskPriority.DEFAULT, true);
			System.out.println("Finished " + ngrams.getResult().size() + "ngramme"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		
		private void testDepRelNGram() {
			
			ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
			System.out.println("Filepath: "  //$NON-NLS-1$
					+ config.getString("plugins.errorMining.appearance.filepath")); //$NON-NLS-1$
			
			String inputFileName = config.getString("plugins.errorMining.appearance.filepath"); //$NON-NLS-1$
			
			//18 Sentences			
			//String  inputFileName = "E:\\test_small_modded.txt"; //$NON-NLS-1$
			
			//CONLL Training English (1334 Sentences)
			//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-development.txt";
			
			//CONLL Training English (39279 Sentences)
			//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-train.txt";
			
			//CONLL Training German 50472 Sentences (Aug)
			//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\tiger_release_aug07.corrected.conll09.txt";
			
			//CONLL Training German 50472 Sentences (Aug)
			//String  inputFileName = "E:\\tiger_release_aug07.corrected.16012013.conll09";

			//int sentencesToRead = 18;
			int sentencesToRead = config.getInteger("plugins.errorMining.appearance.limit"); //$NON-NLS-1$
			
			File file = new File(inputFileName);
			
			
			final List<SentenceData> corpus = new ArrayList<SentenceData>();
			
			Options on = new Options();
			on.put("FringeSTART", 3); //$NON-NLS-1$
			on.put("FringeEND", 5); //$NON-NLS-1$ // 0 = infinity , number = limit
			on.put("NGramLIMIT", 0); //$NON-NLS-1$
			final NGramsDependency ngrams = new NGramsDependency(1, on);
			
			CONLL09SentenceDataGoldReader conellReader = new CONLL09SentenceDataGoldReader();	
			DefaultFileLocation dloc = new DefaultFileLocation(file);
			Options o = null;
			try {
				conellReader.init(dloc, o);
			
			int sentenceNr = 1;
			//System.out.println("ReadSentences: " + sentencesToRead);
			for(int i = 0; i < sentencesToRead; i++){
				SentenceData sd = conellReader.next();
				corpus.add(sd);
				ngrams.initializeUniGrams((DependencyData) sd, sentenceNr);
				sentenceNr++;				
			}
			
			SentenceData sd = conellReader.next();
			
			while (sd != null) {				
				//System.out.println(sentenceNr + " - " + sd.getText());
				corpus.add(sd);
				ngrams.initializeUniGrams((DependencyData) sd, sentenceNr);
				sd = conellReader.next();
				sentenceNr++;				
			}
			
			
			ngrams.nGramResults();
			System.out.println("Corpussize: " + corpus.size()); //$NON-NLS-1$
			
//			List<String> tmpKey = new ArrayList<String>(ngrams.getResult().keySet());
//			Collections.reverse(tmpKey);
//			for(int j = 0; j < tmpKey.size(); j++){				
//				System.out.println("key " + tmpKey.get(j));
//			}
			
			
			}catch (UnsupportedLocationException | IOException | UnsupportedFormatException ex) {
				ex.printStackTrace();
			}
			
			
			//Execute the SwingWorker; the GUI will not freeze
			TaskManager.getInstance().schedule(new ErrorMiningJobDependency(ngrams, corpus), TaskPriority.DEFAULT, true);
			System.out.println("Finished " + ngrams.getResult().size() + "dep-ngramme"); //$NON-NLS-1$ //$NON-NLS-2$
		}
//----		


		public void openPreferences(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		public void editQuery(ActionEvent e) {
			if(currentNGramEditor.getEditingItem()==null) {
				return;
			}
			SearchDescriptor descriptor = currentNGramEditor.getEditingItem();
			if(descriptor==null) {
				return;
			}
			
			SearchQuery searchQuery = descriptor.getQuery();
			if(searchQuery==null) {
				LoggerFactory.log(this, Level.WARNING, 
						"No ngram-query present on search-descriptor"); //$NON-NLS-1$
				return;
			}
			
			try {
				Message message = new Message(ErrorMiningView.this, 
						Commands.PRESENT, descriptor, null);
				
				sendRequest(ErrorMiningConstants.NGRAM_QUERY_VIEW_ID, message);
			}  catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to forward editing of query", ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
		}
		
		public void viewResult(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		public void editParameter(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		
		public void viewNGram(ActionEvent e) {
			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if(descriptor==null) {
				return;
			}
			
			try {
				SearchDescriptor clone = descriptor.cloneShallow();
				currentNGramEditor.setEditingItem(clone);

				refreshActions();
				
				// Forward to query editor directly
				editQuery(e);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to edit selected search", ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
		}
		
		
		public void selectFactory(ActionEvent e) {
			if(currentNGramEditor.getEditingItem()==null) {
				return;
			}
			
			try {
				Collection<Extension> extensions = SearchManager.getInstance().availableSearchFactories();
				Extension extension = PluginUtil.showExtensionDialog(getFrame(), 
						"plugins.errorMining.errorMiningView.dialogs.selectType.title",  //$NON-NLS-1$
						extensions, true);

				if(extension==null) {
					return;
				}
				
				currentNGramEditor.getEditingItem().setFactoryExtension(extension);
				currentNGramEditor.refresh();

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to select new search factory", ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
		}
		
		
		public void selectTarget(ActionEvent e) {
			
			if(currentNGramEditor.getEditingItem()==null) {
				return;
			}
						
			
			SearchFactory factory = currentNGramEditor.getEditingItem().getSearchFactory();

			if(factory==null) {
				UIUtil.beep();
				return;
			}
			
			try {
				ContentType contentType = factory.getConstraintContext().getContentType();
				SearchTargetDialog dialog = new SearchTargetDialog(contentType);
				dialog.showDialog();
				
				Object target = dialog.getTarget();
				if(target==null) {
					return;
				}
				
				boolean compatible = false;
				if(target instanceof DataContainer) {
					// If target is a container allow checking against its internal content type
					compatible = ContentTypeRegistry.isCompatible(contentType, 
							((DataContainer)target).getContentType());
				} else {
					// No information about content type accessible, so
					// just do a plain check (will fail often?)
					compatible = ContentTypeRegistry.isCompatible(contentType, target);
				}
				
				if(!compatible) {
					DialogFactory.getGlobalFactory().showError(null, 
							"plugins.searchTools.searchManagerView.dialogs.selectTarget.title",  //$NON-NLS-1$
							"plugins.searchTools.searchManagerView.dialogs.selectTarget.incompatible",  //$NON-NLS-1$
							target.getClass().getName(), contentType.getName());
					return;
				}

				currentNGramEditor.getEditingItem().setTarget(target);
				currentNGramEditor.refresh();

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to select target for current search", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void cancelNGram(ActionEvent e) {
			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if(descriptor==null) {
				return;
			}
			
			try {
				Search search = descriptor.getSearch();
				if(search!=null) {
					SearchManager.getInstance().cancelSearch(search);
				}

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to cancel selected ngram", ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
		}
		
		public void removeNGram(ActionEvent e) {
			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if(descriptor==null) {
				return;
			}
			
			try {
				
				if(descriptor.isActive()) {
					DialogFactory.getGlobalFactory().showWarning(getFrame(), 
							"plugins.searchTools.errorMiningView.dialogs.removeNGram.title",  //$NON-NLS-1$
							"plugins.searchTools.errorMiningView.dialogs.removeNGram.message"); //$NON-NLS-1$
					return;
				}
				
				int index = searchHistoryList.getSelectedIndex();
				searchHistory.removeSearch(descriptor);
				
				Search search = descriptor.getSearch();
				if(search!=null) {
					SearchManager.getInstance().cancelSearch(search);
				}
				
				// Maintain selected index to allow for more fluent
				// use of selection related actions
				if(index==-1 || index>=searchHistory.getSize()) {
					index = searchHistory.getSize()-1;
				}
				if(index!=-1) {
					searchHistoryList.setSelectedIndex(index);
				} else {
					refreshActions();
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to remove selected ngram from history", ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
		}


		public void clearHistory(ActionEvent e) {
			try {
				// Abort if there is a search still active and running
				for(int i=0; i<searchHistory.getSize(); i++) {
					if(searchHistory.getElementAt(i).isActive()) {
						DialogFactory.getGlobalFactory().showWarning(getFrame(), 
								"plugins.searchTools.errorMiningView.dialogs.removeNGram.title",  //$NON-NLS-1$
								"plugins.searchTools.errorMiningView.dialogs.removeNGram.message"); //$NON-NLS-1$
						return;
					}
				}
				
				searchHistory.clear();
		
				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to clear errormining history", ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
		}
		
		
	}
	
	

	
	
	
	protected class NGramEditor implements Editor<SearchDescriptor>, ActionListener{		
		protected SearchDescriptor descriptor;
		
		protected FormBuilder formBuilder;
				
		protected Timer timer;
		
		
		protected NGramEditor(){
			
			Action a;
			
			formBuilder = FormBuilder.newLocalizingBuilder(new JPanel());
			// Factory
			a = getDefaultActionManager().getAction("plugins.errorMining.errorMiningView.selectFactoryAction"); //$NON-NLS-1$
			formBuilder.addEntry("factory", new SelectFormEntry( //$NON-NLS-1$
					"plugins.errorMining.errorMiningView.nGramEditor.labels.factory", null, a)); //$NON-NLS-1$
						
			// Target
			a = getDefaultActionManager().getAction("plugins.errorMining.errorMiningView.selectTargetAction"); //$NON-NLS-1$
			formBuilder.addEntry("target", new SelectFormEntry( //$NON-NLS-1$
					"plugins.errorMining.errorMiningView.nGramEditor.labels.target", null, a)); //$NON-NLS-1$
			// Query
			a = getDefaultActionManager().getAction("plugins.errorMining.errorMiningView.editQueryAction"); //$NON-NLS-1$
			formBuilder.addEntry("query", new SelectFormEntry( //$NON-NLS-1$
					"plugins.errorMining.errorMiningView.nGramEditor.labels.query", null, a)); //$NON-NLS-1$
			// Result
			a = getDefaultActionManager().getAction("plugins.errorMining.errorMiningView.viewResultAction"); //$NON-NLS-1$
			formBuilder.addEntry("result", new SelectFormEntry( //$NON-NLS-1$
					"plugins.errorMining.errorMiningView.nGramEditor.labels.result", null, a)); //$NON-NLS-1$
			
			formBuilder.buildForm();
			((JComponent)formBuilder.getContainer()).setBorder(UIUtil.topLineBorder);
			
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#getEditorComponent()
		 */
		@Override
		public Component getEditorComponent() {
			return formBuilder.getContainer();
		}
		
		
		public void refresh() {
			SearchDescriptor descriptor = getEditingItem();
			if (descriptor != null) {
				setEditingItem(descriptor);
			}
		}
		

		
		/**
		 * @see de.ims.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
		 */
		@Override
		public void setEditingItem(SearchDescriptor item) {

			if (item == null)
				throw new NullPointerException("Invalid search-descriptor"); //$NON-NLS-1$

			descriptor = item;

			Search search = descriptor.getSearch();
			if (search != null && search.isRunning()) {
				if (timer == null) {
					timer = new Timer(1000, this);
				}
				timer.start();
			} else if (timer != null) {
				timer.stop();
			}
			// Factory
			formBuilder
					.setValue(
							"factory", PluginUtil.getIdentity(descriptor.getFactoryExtension())); //$NON-NLS-1$

			//TODO change to ngrammstuff
			// Target
			String name = StringUtil.getName(descriptor.getTarget());
			if (name == null || name.isEmpty()) {
				name = ResourceManager.getInstance().get(
						"plugins.searchTools.undefinedStats"); //$NON-NLS-1$
			}
			formBuilder.setValue("target", name); //$NON-NLS-1$

			// Query
			String query = SearchUtils.getQueryStats(descriptor.getQuery());
			if (query == null || query.isEmpty()) {
				query = ResourceManager.getInstance().get(
						"plugins.searchTools.emptyStats"); //$NON-NLS-1$
			}
			formBuilder.setValue("query", query); //$NON-NLS-1$

		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#getEditingItem()
		 */
		@Override
		public SearchDescriptor getEditingItem() {
			return descriptor;
		}
		

		/**
		 * @see de.ims.icarus.ui.helper.Editor#resetEdit()
		 */
		@Override
		public void resetEdit() {
			//noop			
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#applyEdit()
		 */
		@Override
		public void applyEdit() {
			//noop
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#hasChanges()
		 */
		@Override
		public boolean hasChanges() {
			return false;
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#close()
		 */
		@Override
		public void close() {
			//noop			
		}
		
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			SearchDescriptor descriptor = getEditingItem();
			if(descriptor!=null) {
				setEditingItem(descriptor);
			} else if(timer!=null) {
				timer.stop();
			}
		}
		
	}
	
	/**
	 * 
	 * @author Gregor Thiele
	 * @version $Id$
	 *
	 */
	
	protected class ErrorMiningJob extends SwingWorker<Map<String,ArrayList<ItemInNuclei>>, Object>
	implements Identity {

		protected NGrams ngrams;
		protected List<SentenceData> corpus;
		
		
		public ErrorMiningJob(NGrams ngrams,
				List<SentenceData> corpus){
			this.ngrams = ngrams;
			this.corpus = corpus;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return "ErrorMining"; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Map<String,ArrayList<ItemInNuclei>> doInBackground() throws Exception {
			return ngrams.getResult();
		}
		
		@Override
		protected void done() {
			try {
				ContentType contentType = ContentTypeRegistry
						.getInstance().getTypeForClass(
								SentenceDataList.class);

				Options options = new Options();
				options.put(Options.CONTENT_TYPE, contentType);
				// send some kind of hint that we want the
				// presenter not to modify content?
				// -> Should be no problem since we only contain
				// immutable data objects?
				
				NGramDataList ngList = new NGramDataList(get(), corpus);
				//System.out.println("NGSIZE: " + ngList.size());

				if (isDone()) {
					//Algorithm Results (Debug)
					Message messageDebug = new Message(this, Commands.DISPLAY, ngList, null);
					sendRequest(ErrorMiningConstants.NGRAM_RESULT_VIEW_ID, messageDebug);

					
					//Algorithm List View
					Message messageSentences = new Message(this, Commands.DISPLAY, ngList, null);
					sendRequest(ErrorMiningConstants.NGRAM_RESULT_SENTENCE_VIEW_ID, messageSentences);

					
					//Dependency View				
					Message messageUser = new Message(this, Commands.DISPLAY, ngList, null);
					sendRequest(null, messageUser);				
					
					
					System.out.println("Worker c/done " + isCancelled() + isDone()); //$NON-NLS-1$
				}
				

			} catch (InterruptedException e) {
				LoggerFactory.log(this,Level.SEVERE, "NGram Execution Interrupted ", e); //$NON-NLS-1$
			} catch (ExecutionException e) {
				LoggerFactory.log(this,Level.SEVERE, "NGram Execution Exception ", e); //$NON-NLS-1$
			}				
				
		}	
		
	}
	
	
	protected class ErrorMiningJobDependency extends SwingWorker<Map<String,ArrayList<DependencyItemInNuclei>>, Object>
	implements Identity {

		protected NGramsDependency ngrams;
		protected List<SentenceData> corpus;
		
		
		public ErrorMiningJobDependency(NGramsDependency ngrams,
				List<SentenceData> corpus){
			this.ngrams = ngrams;
			this.corpus = corpus;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return "ErrorMining"; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Map<String, ArrayList<DependencyItemInNuclei>> doInBackground() throws Exception {
			return ngrams.getResult();
		}
		
		@Override
		protected void done() {
			try {
				ContentType contentType = ContentTypeRegistry
						.getInstance().getTypeForClass(
								SentenceDataList.class);

				Options options = new Options();
				options.put(Options.CONTENT_TYPE, contentType);
				// send some kind of hint that we want the
				// presenter not to modify content?
				// -> Should be no problem since we only contain
				// immutable data objects?
				
				
				NGramDataListDependency ngList = new NGramDataListDependency(get(), corpus);
				//System.out.println("NGSIZE-Dependency: " + ngList.size());

				if (isDone()) {
					//Algorithm Results (Debug)
					Message messageDebug = new Message(this, Commands.DISPLAY, ngList, null);
					sendRequest(ErrorMiningConstants.NGRAM_RESULT_VIEW_ID, messageDebug);

					
					//Algorithm List View
					Message messageSentences = new Message(this, Commands.DISPLAY, ngList, null);
					sendRequest(ErrorMiningConstants.NGRAM_RESULT_SENTENCE_VIEW_ID, messageSentences);

					
					//Dependency View				
					Message messageUser = new Message(this, Commands.DISPLAY, ngList, null);
					sendRequest(null, messageUser);				
					
					
					System.out.println("Worker c/done " + isCancelled() + isDone()); //$NON-NLS-1$
				}
				

			} catch (InterruptedException e) {
				LoggerFactory.log(this,Level.SEVERE, "NGram Execution Interrupted ", e); //$NON-NLS-1$
			} catch (ExecutionException e) {
				LoggerFactory.log(this,Level.SEVERE, "NGram Execution Exception ", e); //$NON-NLS-1$
			}				
				
		}	
		
	}
	
	
	
	
	
	protected static class SearchTargetDialog implements ActionListener {
		protected final ContentType contentType;
		
		protected Object target;
		protected JPanel panel;
		
		protected Map<Extension, SearchTargetSelector> selectorInstances;
		
		public SearchTargetDialog(ContentType contentType) {
			if(contentType==null)
				throw new NullPointerException("Invalid content-type"); //$NON-NLS-1$
			
			this.contentType = contentType;
		}
		
		public void showDialog() {
			Collection<Extension> extensions = SearchManager.getInstance().availableTargetSelectors();
			if(extensions==null || extensions.isEmpty()) 
				throw new IllegalStateException("No target selectors available"); //$NON-NLS-1$
			
			panel = new JPanel(new BorderLayout());
			
			JToolBar toolBar = ActionManager.globalManager().createEmptyToolBar();;
			
			JLabel label = new JLabel();
			label.setText(ResourceManager.getInstance().get(
					"plugins.errorMining.errorMiningView.dialogs.selectTarget.label")); //$NON-NLS-1$
			label.setBorder(new EmptyBorder(1, 5, 1, 10));
			toolBar.add(label);
			
			JComboBox<Extension> cb = new JComboBox<>(
					new ExtensionListModel(extensions, true));
			cb.setEditable(false);
			cb.setRenderer(new ExtensionListCellRenderer());
			UIUtil.fitToContent(cb, 150, 250, 22);
			cb.addActionListener(this);
			toolBar.add(cb);
			
			panel.add(toolBar, BorderLayout.NORTH);
			panel.setPreferredSize(new Dimension(400, 300));

			cb.setSelectedIndex(0);
			
			target = null;
			if(DialogFactory.getGlobalFactory().showGenericDialog(null, 
					"plugins.errorMining.errorMiningView.dialogs.selectTarget.label",  //$NON-NLS-1$
					null, panel, true, "ok", "cancel")) { //$NON-NLS-1$ //$NON-NLS-2$
				Extension extension = (Extension) cb.getSelectedItem();
				if(extension!=null) {
					target = getSelector(extension).getSelectedItem();
				}
			}
		}
		
		public Object getTarget() {
			return target;
		}
		
		protected SearchTargetSelector getSelector(Extension extension) {
			if(extension==null)
				throw new NullPointerException();
			
			if(selectorInstances==null) {
				selectorInstances = new HashMap<>();
			}
			
			SearchTargetSelector selector = selectorInstances.get(extension);
			if(selector==null) {
				try {
					selector = (SearchTargetSelector) PluginUtil.instantiate(extension);
					selector.setAllowedContentType(contentType);
					selectorInstances.put(extension, selector);
				} catch (Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to instantiate target selector: "+extension.getUniqueId(), e); //$NON-NLS-1$
				}
			}
			
			return selector;
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox<?> cb = (JComboBox<?>) e.getSource();
			
			Extension extension = (Extension) cb.getSelectedItem();
			if(extension==null) {
				return;
			}
			
			SearchTargetSelector selector = getSelector(extension);
			if(selector==null) {
				UIUtil.beep();
				return;
			}
			
			if(panel.getComponentCount()>1) {
				panel.remove(1);
			}
			panel.add(selector.getSelectorComponent(), BorderLayout.CENTER);
			panel.revalidate();
			panel.repaint();
			selector.getSelectorComponent().requestFocusInWindow();
		}
	}
	
	
	
	protected class SearchEditor implements Editor<SearchDescriptor>, ActionListener {
		protected SearchDescriptor descriptor;
		
		protected FormBuilder formBuilder;
		
		protected Timer timer;
		
		protected SearchEditor() {
			Action a;
			
			formBuilder = FormBuilder.newLocalizingBuilder(new JPanel());
			// Factory
			a = getDefaultActionManager().getAction("plugins.searchTools.searchManagerView.selectFactoryAction"); //$NON-NLS-1$
			formBuilder.addEntry("factory", new SelectFormEntry( //$NON-NLS-1$
					"plugins.searchTools.searchManagerView.searchEditor.labels.factory", null, a)); //$NON-NLS-1$
			// Target
			a = getDefaultActionManager().getAction("plugins.searchTools.searchManagerView.selectTargetAction"); //$NON-NLS-1$
			formBuilder.addEntry("target", new SelectFormEntry( //$NON-NLS-1$
					"plugins.searchTools.searchManagerView.searchEditor.labels.target", null, a)); //$NON-NLS-1$
			// Query
			a = getDefaultActionManager().getAction("plugins.searchTools.searchManagerView.editQueryAction"); //$NON-NLS-1$
			formBuilder.addEntry("query", new SelectFormEntry( //$NON-NLS-1$
					"plugins.searchTools.searchManagerView.searchEditor.labels.query", null, a)); //$NON-NLS-1$
			// Parameters
			a = getDefaultActionManager().getAction("plugins.searchTools.searchManagerView.editParametersAction"); //$NON-NLS-1$
			formBuilder.addEntry("parameters", new SelectFormEntry( //$NON-NLS-1$
					"plugins.searchTools.searchManagerView.searchEditor.labels.parameters", null, a)); //$NON-NLS-1$
			
			formBuilder.buildForm();
			((JComponent)formBuilder.getContainer()).setBorder(UIUtil.topLineBorder);
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#getEditorComponent()
		 */
		@Override
		public Component getEditorComponent() {
			return formBuilder.getContainer();
		}
		
		public void refresh() {
			SearchDescriptor descriptor = getEditingItem();
			if(descriptor!=null) {
				setEditingItem(descriptor);
			}
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
		 */
		@Override
		public void setEditingItem(SearchDescriptor item) {
			if(item==null)
				throw new NullPointerException("Invalid search-descriptor"); //$NON-NLS-1$
						
			descriptor = item;
			
			Search search = descriptor.getSearch();
			if(search!=null && search.isRunning()) {
				if(timer==null) {
					timer = new Timer(1000, this);
				}
				timer.start();
			} else if(timer!=null) {
				timer.stop();
			}
			
			// Factory
			formBuilder.setValue("factory", PluginUtil.getIdentity(descriptor.getFactoryExtension())); //$NON-NLS-1$
			
			// Target
			String name = StringUtil.getName(descriptor.getTarget());
			if(name==null || name.isEmpty()) {
				name = ResourceManager.getInstance().get("plugins.searchTools.undefinedStats"); //$NON-NLS-1$
			}
			formBuilder.setValue("target", name); //$NON-NLS-1$
			
			// Query
			String query = SearchUtils.getQueryStats(descriptor.getQuery());
			if(query==null || query.isEmpty()) {
				query = ResourceManager.getInstance().get("plugins.searchTools.emptyStats"); //$NON-NLS-1$
			}
			formBuilder.setValue("query", query); //$NON-NLS-1$			
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#getEditingItem()
		 */
		@Override
		public SearchDescriptor getEditingItem() {
			return descriptor;
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#resetEdit()
		 */
		@Override
		public void resetEdit() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#applyEdit()
		 */
		@Override
		public void applyEdit() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#hasChanges()
		 */
		@Override
		public boolean hasChanges() {
			return false;
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#close()
		 */
		@Override
		public void close() {
			// no-op
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			SearchDescriptor descriptor = getEditingItem();
			if(descriptor!=null) {
				setEditingItem(descriptor);
			} else if(timer!=null) {
				timer.stop();
			}
		}
	}
	




	

}
