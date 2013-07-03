/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.errormining;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import net.ikarus_systems.icarus.Core;
import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.plugins.matetools.conll.CONLL09SentenceDataReader;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.dialog.FormBuilder;
import net.ikarus_systems.icarus.ui.dialog.SelectFormEntry;
import net.ikarus_systems.icarus.ui.helper.Editor;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.UnsupportedFormatException;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.location.DefaultFileLocation;
import net.ikarus_systems.icarus.util.location.UnsupportedLocationException;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;
import ngram_tools.NGramDataList;
import ngram_tools.NGramDescriptor;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class ErrorMiningView extends View {
	
	protected JTree ngramHistoryTree;
	protected NGramHistoryTreeModel ngramHistoryTreeModel;
	
	protected NGramEditor currentNGramEditor;
	
	private JPopupMenu popupMenu;

	private Handler handler;
	
	private CallbackHandler callbackHandler;
	
	private SwingWorker<Map<String,ArrayList<ItemInNuclei>>, Void> worker;
	
	public ErrorMiningView(){
		//noop
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
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
			
			//18 Sentences
			String  inputFileName = "E:\\test_small_modded.txt"; //$NON-NLS-1$
			
			//CONLL Training English (1334 Sentences)
			//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-development.txt";
			
			//CONLL Training English (39279 Sentences)
			//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-train.txt";
			
			//CONLL Training German 50472 Sentences (Aug)
			//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\tiger_release_aug07.corrected.conll09.txt";
			
			//CONLL Training German 50472 Sentences (Aug)
			//String  inputFileName = "E:\\tiger_release_aug07.corrected.16012013.conll09";

			
			int sentencesToRead = 18;
			
			File file = new File(inputFileName);	
			
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
			for(int i = 0; i < sentencesToRead; i++){
				DependencyData dd;
				dd = (DependencyData) conellReader.next();
				ngrams.initializeUniGrams(dd, sentenceNr);
				sentenceNr++;				
			}
			
			ngrams.nGramResults();
			
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

						ngList = new NGramDataList(get());

											
//						Message message = new Message(this, Commands.DISPLAY, ngList, options);
//						sendRequest(null, message);
						
						
						Message message = new Message(this, Commands.DISPLAY, get(), null);
						sendRequest(ErrorMiningConstants.NGRAM_RESULT_VIEW_ID, message);
						

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
			
			System.out.println("Finished " + ngrams.getResult().size());

			//Core.showNotice();
		}
		
		public void cancelNGram(ActionEvent e) {
			// TODO
			Core.showNotice();
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
		
		
	}
	
	
	
	
	protected class NGramEditor implements Editor<NGramDescriptor>{
		
		protected NGramDescriptor descriptor;
		
		protected FormBuilder formBuilder;
		
		protected NGramEditor(){
			
			Action a;
			
			formBuilder = FormBuilder.newLocalizingBuilder(new JPanel());
			
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
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#getEditorComponent()
		 */
		@Override
		public Component getEditorComponent() {
			return formBuilder.getContainer();
		}
		
		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
		 */
		@Override
		public void setEditingItem(NGramDescriptor item) {
			// TODO Auto-generated method stub
			
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#getEditingItem()
		 */
		@Override
		public NGramDescriptor getEditingItem() {
			return descriptor;
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#resetEdit()
		 */
		@Override
		public void resetEdit() {
			// TODO Auto-generated method stub
			
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#applyEdit()
		 */
		@Override
		public void applyEdit() {
			// TODO Auto-generated method stub
			
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#hasChanges()
		 */
		@Override
		public boolean hasChanges() {
			// TODO Auto-generated method stub
			return false;
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#close()
		 */
		@Override
		public void close() {
			//noop			
		}
		
	}



	

}
