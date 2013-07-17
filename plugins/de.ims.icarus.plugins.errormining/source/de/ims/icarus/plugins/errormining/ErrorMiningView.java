/* 
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

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
import de.ims.icarus.plugins.matetools.conll.CONLL09SentenceDataReader;
import de.ims.icarus.plugins.search_tools.view.SearchHistory;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchDescriptor;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchTargetSelector;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.dialog.SelectFormEntry;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.data.DataContainer;
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

	private Handler handler;
	
	private CallbackHandler callbackHandler;
	
	
	//gui output used
	protected JTree ngramHistoryTree;
	protected NGramHistoryTreeModel ngramHistoryTreeModel;	
	private SwingWorker<Map<String,ArrayList<ItemInNuclei>>, Void> worker;
	
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

		handler = createHandler();	
		
		
		//current NGram Editor
		currentNGramEditor = new NGramEditor();
		// Set initially empty NGram
//		NGramDescriptor descriptor = new NGramDescriptor();
//		descriptor.setFactoryExtension(factoryExtensions.iterator().next());
//		currentNGramEditor.setEditingItem(descriptor);
		
				
		
		// History
		ngramHistoryTreeModel = new NGramHistoryTreeModel();
		ngramHistoryTree = new JTree(ngramHistoryTreeModel);
		ngramHistoryTree.setEditable(false);
		ngramHistoryTree.setRootVisible(false);
		ngramHistoryTree.setShowsRootHandles(true);
		ngramHistoryTree.addTreeSelectionListener(handler);
		ngramHistoryTree.setCellRenderer(new NGramHistoryTreeCellRenderer());
		JScrollPane scrollPane = new JScrollPane(ngramHistoryTree);
		scrollPane.setBorder(UIUtil.topLineBorder);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(currentNGramEditor.getEditorComponent(), BorderLayout.NORTH);
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		
		container.setLayout(new BorderLayout());
		container.add(createToolBar(), BorderLayout.NORTH);
		container.add(contentPanel, BorderLayout.CENTER);
		
		
		registerActionCallbacks();

		refreshActions();
	}
	
	
	protected JToolBar createToolBar() {
		return getDefaultActionManager().createToolBar(
				"plugins.errorMining.errorMiningView.toolBarList", null); //$NON-NLS-1$
	}
	
	
	protected void refreshActions() {
		// no-op
	}
	
	
	protected void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = createCallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		
		
		actionManager.addHandler("plugins.errorMining.errorMiningView.newNGramAction",  //$NON-NLS-1$
				callbackHandler, "newNGram"); //$NON-NLS-1$
		actionManager.addHandler("plugins.errorMining.errorMiningView.executeNGramAction",  //$NON-NLS-1$
				callbackHandler, "executeNGram"); //$NON-NLS-1$
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
		actionManager.addHandler("plugins.errorMining.errorMiningView.selectTargetAction",  //$NON-NLS-1$
				callbackHandler, "selectTarget"); //$NON-NLS-1$
		
	}
	
	
	protected Handler createHandler() {
		return new Handler();
	}
	
	
	
	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler();
	}
	
	
	
	protected class Handler implements TreeSelectionListener {
		
		/**
		 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
		 */
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}	
	
	
	public final class CallbackHandler {

		private CallbackHandler() {
			// no-op
		}
		
		
		public void newNGram(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		
		public void executeNGram(ActionEvent e) {
			// TODO rework
			
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
			
			CONLL09SentenceDataReader conellReader = new CONLL09SentenceDataReader();	
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
			
//			SentenceData sd = conellReader.next();
//			
//			while (sd != null) {				
//				System.out.println(sentenceNr + " - " + sd.getText());
//				corpus.add(sd);
//				ngrams.initializeUniGrams((DependencyData) sd, sentenceNr);
//				sd = conellReader.next();
//				sentenceNr++;				
//			}
			
			
			ngrams.nGramResults();
			System.out.println("Corpussize: " + corpus.size()); //$NON-NLS-1$
			
//			List<String> tmpKey = new ArrayList<String>(ngrams.getResult().keySet());
//			Collections.reverse(tmpKey);
//			for(int j = 0; j < tmpKey.size(); j++){				
//				System.out.println("key" + tmpKey.get(j));
//			}
			
			
			}catch (UnsupportedLocationException | IOException | UnsupportedFormatException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			
			
			//~~~~~~~~
			
			// Construct a new SwingWorker
			worker = new SwingWorker<Map<String,ArrayList<ItemInNuclei>>, Void>() {

				@Override
				protected Map<String, ArrayList<ItemInNuclei>> doInBackground()
						throws Exception {
					//return NGramsExecution.getInstance().runNGrams();
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
						// TODO send some kind of hint that we want the
						// presenter not to modify content?
						// -> Should be no problem since we only contain
						// immutable data objects?
						NGramDataList ngList;

						ngList = new NGramDataList(get(), corpus);

						Message messageUser = new Message(this, Commands.DISPLAY, ngList, null);
						sendRequest(null, messageUser);
						
						//Algorithm Results (Debug)
						Message messageDebug = new Message(this, Commands.DISPLAY, get(), null);
						sendRequest(ErrorMiningConstants.NGRAM_RESULT_VIEW_ID, messageDebug);
						
						//TODO fix worker more worker no freeze no duplicated lists
						Message corpusData = new Message(this, Commands.SET, corpus, null);	
						sendRequest(ErrorMiningConstants.NGRAM_RESULT_SENTENCE_VIEW_ID, corpusData);
						
						Message messageSentences = new Message(this, Commands.DISPLAY, get(), null);
						sendRequest(ErrorMiningConstants.NGRAM_RESULT_SENTENCE_VIEW_ID, messageSentences);

						
						System.out.println("Worker c/done " + isCancelled() + isDone()); //$NON-NLS-1$

						if (isDone()) {
							// ActionManager.globalManager()
						}
						

					} catch (InterruptedException e) {
						LoggerFactory.log(this,Level.SEVERE, "NGram Execution Interrupted ", e); //$NON-NLS-1$
					} catch (ExecutionException e) {
						LoggerFactory.log(this,Level.SEVERE, "NGram Execution Exception ", e); //$NON-NLS-1$
					}				
						
				}			
			};
			
			// Execute the SwingWorker; the GUI will not freeze
			worker.execute();
			
			System.out.println("Finished " + ngrams.getResult().size()); //$NON-NLS-1$

			//Core.showNotice();
		}

		
		public void removeNGram(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		public void clearHistory(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		public void openPreferences(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		public void editQuery(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		public void viewResult(ActionEvent e) {
			// TODO
			Core.showNotice();
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
				//currentNGramEditor.refresh();

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to select target for current search", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		
	}
	
	

	
	
	
	protected class NGramEditor implements Editor<SearchDescriptor>{
		
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
			// TODO Auto-generated method stub
		
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
		
	}
	
	
	
	
	protected static class SearchTargetDialog implements ActionListener {
		protected final ContentType contentType;
		
		protected Object target;
		protected JPanel panel;
		
		protected Map<Extension, SearchTargetSelector> selectorInstances;
		
		public SearchTargetDialog(ContentType contentType) {
			if(contentType==null)
				throw new IllegalArgumentException("Invalid content-type"); //$NON-NLS-1$
			
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
					"plugins.searchTools.searchManagerView.dialogs.selectTarget.title",  //$NON-NLS-1$
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
				throw new IllegalArgumentException("Invalid search-descriptor"); //$NON-NLS-1$
						
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
			
			// Result
			/*String result = SearchUtils.getResultStats(descriptor.getSearchResult());
			if(result==null || result.isEmpty()) {
				result = ResourceManager.getInstance().get("plugins.searchTools.emptyStats"); //$NON-NLS-1$
			}
			formBuilder.setValue("result", result); //$NON-NLS-1$*/
			
			// Parameters
			// TODO provide any kind of textual representation?
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
