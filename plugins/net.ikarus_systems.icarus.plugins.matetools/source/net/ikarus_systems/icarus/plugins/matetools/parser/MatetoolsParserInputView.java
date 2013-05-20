/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.matetools.parser;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.language.AvailabilityObserver;
import net.ikarus_systems.icarus.language.DataType;
import net.ikarus_systems.icarus.language.LanguageManager;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.language.tokenizer.TokenListCellRenderer;
import net.ikarus_systems.icarus.language.tokenizer.TokenizationResult;
import net.ikarus_systems.icarus.language.tokenizer.Tokenizer;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.ExtensionListCellRenderer;
import net.ikarus_systems.icarus.plugins.ExtensionListModel;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.core.ToolBarDelegate;
import net.ikarus_systems.icarus.plugins.jgraph.view.ListGraphView;
import net.ikarus_systems.icarus.plugins.language_tools.input.TextInputView;
import net.ikarus_systems.icarus.plugins.matetools.parser.MatetoolsPipeline.PipelineOwner;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.config.ConfigDialog;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.helper.Outline;
import net.ikarus_systems.icarus.ui.tasks.TaskManager;
import net.ikarus_systems.icarus.ui.tasks.TaskPriority;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.AbstractDataList;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.id.Identity;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MatetoolsParserInputView extends TextInputView {
	
	private volatile PipelineWorker worker;
	private JComboBox<Extension> tokenizerSelect;
	
	private CallbackHandler callbackHandler;
	private Handler handler;
	
	private ResultList resultList;
	
	private final PipelineOwner pipelineOwner = new PipelineOwner() {
		
		@Override
		public String getName() {
			return getIdentity().getName();
		}

		@Override
		public void outputChanged(DependencyData currentOutput) {
			PipelineWorker worker = MatetoolsParserInputView.this.worker;
			if(worker!=null) {
				worker.publishIntermediateResult(currentOutput);
			}
		}
	};

	public MatetoolsParserInputView() {
		// no-op
	}

	@Override
	public void init(JComponent container) {
		
		// Load actions
		URL actionLocation = MatetoolsParserInputView.class.getResource("matetools-parser-input-view-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: matetools-parser-input-view-actions.xml"); //$NON-NLS-1$
		
		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}
		
		handler = new Handler();
		
		super.init(container);
		
		inputArea.getDocument().addDocumentListener(handler);
	}

	@Override
	protected void refreshActions() {
		boolean enabled = worker==null;
		
		if(enabled) {
			String text = inputArea.getText();
			enabled = text!=null && !text.trim().isEmpty();
		}
		
		getDefaultActionManager().setEnabled(enabled, 
				"plugins.matetools.matetoolsParserInputView.startPipelineAction"); //$NON-NLS-1$
	}

	@Override
	protected void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		// Register default actions
		super.registerActionCallbacks();
		
		// Now register new actions
		ActionManager actionManager = getDefaultActionManager();
		actionManager.addHandler("plugins.matetools.matetoolsParserInputView.startPipelineAction",  //$NON-NLS-1$
				callbackHandler, "startPipeline"); //$NON-NLS-1$
		actionManager.addHandler("plugins.matetools.matetoolsParserInputView.openPreferencesAction",  //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
	}

	@Override
	protected JToolBar createToolBar() {
		Options options = new Options();
		
		if(tokenizerSelect==null) {
			Collection<Extension> availableTokenizers = LanguageManager.getAvailableTokenizers();
			tokenizerSelect = new JComboBox<>(new ExtensionListModel(availableTokenizers, true));
			tokenizerSelect.setEditable(false);
			tokenizerSelect.setRenderer(new ExtensionListCellRenderer());
			
			UIUtil.fitToContent(tokenizerSelect, 130, 200, 24);
			
			if(availableTokenizers.isEmpty()) {
				tokenizerSelect.setEnabled(false);
			} else {
				tokenizerSelect.setSelectedIndex(0);
			}
		}
		
		options.put("selectTokenizer", tokenizerSelect); //$NON-NLS-1$
		
		return getDefaultActionManager().createToolBar(
				"plugins.matetools.matetoolsParserInputView.toolBarList", options); //$NON-NLS-1$
	}

	@Override
	protected void buildToolBar(ToolBarDelegate delegate) {
		delegate.add(getDefaultActionManager().getAction(
				"plugins.matetools.matetoolsParserInputView.openPreferencesAction")); //$NON-NLS-1$
		delegate.add(getDefaultActionManager().getAction(
				"plugins.matetools.matetoolsParserInputView.startPipelineAction")); //$NON-NLS-1$
	}
	
	private Tokenizer getTokenizer() {
		if(tokenizerSelect==null) {
			return null;
		}
		
		Extension extension = (Extension) tokenizerSelect.getSelectedItem();
		
		if(extension==null) {
			return null;
		}
		
		try {
			return (Tokenizer) PluginUtil.instantiate(extension);
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to instantiate tokenizer: "+extension.getUniqueId(), e); //$NON-NLS-1$
		}
		
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void executePipeline() throws Exception {
		// No concurrent parsing
		if(worker!=null) {
			return;
		}
		
		if(inputArea==null) {
			return;
		}
		
		String input = inputArea.getText();
		
		if(input==null || input.isEmpty()) {
			return;
		}
		
		Tokenizer tokenizer = getTokenizer();
		
		if(tokenizer==null)
			throw new IllegalStateException("Failed to obtain tokenizer"); //$NON-NLS-1$
	
		Options options = new Options();
		options.put(Tokenizer.PRECEDED_ROOT_OPTION, true);
		TokenizationResult tokenizationResult = tokenizer.tokenize(input, options);
		
		if(tokenizationResult==null || tokenizationResult.getResultCount()==0) {
			DialogFactory.getGlobalFactory().showError(null, 
					"plugins.matetools.matetoolsParserInputView.title",  //$NON-NLS-1$
					"plugins.matetools.matetoolsParserInputView.invalidTokenization"); //$NON-NLS-1$
			return;
		}
		
		String[] tokens = null;
		
		if(tokenizationResult.getResultCount()>1) {
			Vector buffer = new Vector(tokenizationResult.getResultCount());
			for(int i=0; i<tokenizationResult.getResultCount(); i++) {
				buffer.add(tokenizationResult.getTokens(i));
			}
			JList list = new JList(buffer);
			list.setCellRenderer(new TokenListCellRenderer());
			
			JScrollPane scrollPane = new JScrollPane(list);
			scrollPane.setPreferredSize(new Dimension(300, 300));
			
			DialogFactory.getGlobalFactory().showGenericDialog(null, 
					"plugins.matetools.matetoolsParserInputView.title",  //$NON-NLS-1$
					"plugins.matetools.matetoolsParserInputView.multipleTokenization", //$NON-NLS-1$
					scrollPane, true, "ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
			
			if(list.getSelectedIndex()!=-1) {
				tokens = (String[]) list.getSelectedValue();
			}
		} else {
			tokens = tokenizationResult.getTokens(0);
		}
		
		// Fail silently
		if(tokens==null) {
			return;
		}
		
		options = new Options();
		// TODO assign new option parameters?
		worker = new PipelineWorker(tokens, options);
		
		// Schedule worker for concurrent execution on background thread
		TaskManager.getInstance().schedule(
				worker, null, TaskPriority.DEFAULT, true);
		
		refreshActions();
	}
	
	private void appendResult(DependencyData data) {		
		if(resultList==null) {
			resultList = new ResultList();
		}
		
		resultList.addData(data);
		
		// Send result list
		Options options = new Options();
		options.put(Options.CONTENT_TYPE, ContentTypeRegistry.getInstance().getTypeForClass(SentenceDataList.class));
		
		Message message = new Message(MatetoolsParserInputView.this, 
				Commands.DISPLAY, resultList, options);
		
		sendRequest(ListGraphView.class, message);
	}

	private class Handler implements DocumentListener {
		
		private Handler() {
			// no-op
		}

		/**
		 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
		 */
		@Override
		public void insertUpdate(DocumentEvent e) {
			refreshActions();
		}

		/**
		 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
		 */
		@Override
		public void removeUpdate(DocumentEvent e) {
			refreshActions();
		}

		/**
		 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
		 */
		@Override
		public void changedUpdate(DocumentEvent e) {
			refreshActions();
		}
	}
	
	public class CallbackHandler {
		
		protected CallbackHandler() {
			// no-op
		}
		
		public void startPipeline(ActionEvent e) {
			
			// Ensure we have ownership of the pipeline or abort otherwise
			if(MatetoolsPipeline.getPipeline(pipelineOwner)==null) {
				return;
			}
			
			
			try {
				 executePipeline();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to start parser-pipeline", ex); //$NON-NLS-1$
			}
		}
		
		public void openPreferences(ActionEvent e) {
			try {
				new ConfigDialog(ConfigRegistry.getGlobalRegistry(), 
						"plugins.matetools.parser").setVisible(true); //$NON-NLS-1$
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to open preferences", ex); //$NON-NLS-1$
			}
		}
	}
	
	private static final String[] stageLabels = {
		"plugins.matetools.matetoolsParserInputView.pipelineWorker.stage0Blank", //$NON-NLS-1$
		"plugins.matetools.matetoolsParserInputView.pipelineWorker.stage1Lemmatizing", //$NON-NLS-1$
		"plugins.matetools.matetoolsParserInputView.pipelineWorker.stage2Tagging", //$NON-NLS-1$
		"plugins.matetools.matetoolsParserInputView.pipelineWorker.stage3MTagging", //$NON-NLS-1$
		"plugins.matetools.matetoolsParserInputView.pipelineWorker.stage4Parsing", //$NON-NLS-1$
	};
	
	private class PipelineWorker extends SwingWorker<DependencyData, DependencyData> implements Identity {

		private final String[] tokens;
		private final Options options;
		
		private int stage = 0;
		
		PipelineWorker(String[] tokens, Options options) {
			if(tokens==null)
				throw new IllegalArgumentException("Invalid tokens array"); //$NON-NLS-1$
			
			this.tokens = tokens;
			this.options = options;
		}
		
		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected DependencyData doInBackground() throws Exception {
			MatetoolsPipeline pipeline = MatetoolsPipeline.getPipeline(pipelineOwner);
			if(pipeline==null)
				throw new IllegalStateException("Synchronization failed - pipeline not owned by "+pipelineOwner.getName()); //$NON-NLS-1$
			
			return pipeline.runPipeline(tokens, options);
		}
		
		private void publishIntermediateResult(DependencyData data) {
			stage++;
			setProgress(stage*25);
			publish(data);
		}

		@Override
		protected void process(List<DependencyData> chunks) {
			if(chunks==null || chunks.isEmpty()) {
				return;
			}
			
			// Fetch latest state
			DependencyData data = chunks.get(chunks.size()-1);

			// Send data item
			Options options = new Options();
			options.put(Options.CONTENT_TYPE, DependencyUtils.getDependencyContentType());
			
			Message message = new Message(MatetoolsParserInputView.this, 
					Commands.DISPLAY, data, options);
			
			sendRequest(Outline.class, message);
		}

		@Override
		protected void done() {
			DependencyData data;
			try {
				data = get();
				if(data!=null) {
					appendResult(data);
				}
			} catch (Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unexpected exception while obtaining final pipeline ocmputation result", e); //$NON-NLS-1$
			}
			
			MatetoolsPipeline.releasePipeline(pipelineOwner);
			
			refreshActions();
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return getIdentity().getId()+"-PipelineWorker"; //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"plugins.matetools.matetoolsParserInputView.pipelineWorker.name"); //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			String key = stageLabels[stage];
			return ResourceManager.getInstance().get(key);
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}
		
	}
	
	private class ResultList extends AbstractDataList<SentenceData> implements SentenceDataList {

		private List<DependencyData> items;
		
		private void addData(DependencyData data) {
			if(items==null) {
				items = new ArrayList<>();
			}
			
			items.add(data);
			
			fireChangeEvent();
		}
		
		/**
		 * @see net.ikarus_systems.icarus.util.data.DataList#size()
		 */
		@Override
		public int size() {
			return items==null ? 0 : items.size();
		}

		/**
		 * @see net.ikarus_systems.icarus.util.data.DataList#get(int)
		 */
		@Override
		public DependencyData get(int index) {
			return items==null ? null : items.get(index);
		}

		/**
		 * @see net.ikarus_systems.icarus.util.data.DataList#getContentType()
		 */
		@Override
		public ContentType getContentType() {
			return DependencyUtils.getDependencyContentType();
		}

		/**
		 * @see net.ikarus_systems.icarus.language.SentenceDataList#supportsType(net.ikarus_systems.icarus.language.DataType)
		 */
		@Override
		public boolean supportsType(DataType type) {
			return type==DataType.SYSTEM;
		}

		/**
		 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType)
		 */
		@Override
		public SentenceData get(int index, DataType type) {
			return type==DataType.SYSTEM ? get(index) : null;
		}

		/**
		 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType, net.ikarus_systems.icarus.language.AvailabilityObserver)
		 */
		@Override
		public SentenceData get(int index, DataType type,
				AvailabilityObserver observer) {
			return get(index, type);
		}
		
	}
}
