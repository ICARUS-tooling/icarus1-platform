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
package de.ims.icarus.plugins.matetools.parser;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.java.plugin.registry.Extension;

import de.ims.icarus.Core;
import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.LanguageManager;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.language.dependency.DependencySentenceData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.language.tokenizer.TokenListCellRenderer;
import de.ims.icarus.language.tokenizer.TokenizationResult;
import de.ims.icarus.language.tokenizer.Tokenizer;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.ExtensionListModel;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.ToolBarDelegate;
import de.ims.icarus.plugins.jgraph.view.ListGraphView;
import de.ims.icarus.plugins.language_tools.input.TextInputView;
import de.ims.icarus.plugins.matetools.parser.MatetoolsPipeline.PipelineOwner;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogDispatcher;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.helper.Outline;
import de.ims.icarus.ui.tasks.TaskConstants;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.AbstractDataList;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MatetoolsParserInputView extends TextInputView {

	private volatile PipelineWorker worker;
	private JComboBox<Extension> tokenizerSelect;
	private JComboBox<String> modelStorageSelect;

	private CallbackHandler callbackHandler;
	private Handler handler;

	//private TaskProgressPanel progressPanel;

	private ResultList resultList;

	private final PipelineOwner pipelineOwner = new PipelineOwner() {

		@Override
		public String getName() {
			return getIdentity().getName();
		}

		@Override
		public void outputChanged(DependencySentenceData currentOutput) {
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

		//progressPanel = new TaskProgressPanel();

		inputArea.getDocument().addDocumentListener(handler);
		ConfigRegistry.getGlobalRegistry().addGroupListener(
				"plugins.matetools.parser", handler); //$NON-NLS-1$
	}

	@Override
	protected void refreshActions() {
		boolean enabled = worker==null;

		if(enabled) {
			String text = inputArea.getText();
			enabled = text!=null && !text.trim().isEmpty();
		}

		boolean running = MatetoolsPipeline.isPipelineRunning();

		getDefaultActionManager().setEnabled(enabled,
				"plugins.matetools.matetoolsParserInputView.startPipelineAction"); //$NON-NLS-1$
		getDefaultActionManager().setEnabled(!running,
				"plugins.matetools.matetoolsParserInputView.unloadModelsAction"); //$NON-NLS-1$
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
		actionManager.addHandler("plugins.matetools.matetoolsParserInputView.unloadModelsAction",  //$NON-NLS-1$
				callbackHandler, "unloadModels"); //$NON-NLS-1$
		actionManager.addHandler("plugins.matetools.matetoolsParserInputView.openPreferencesAction",  //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
	}

	@Override
	protected ActionComponentBuilder createToolBar() {
		ActionComponentBuilder builder = new ActionComponentBuilder(getDefaultActionManager());
		builder.setActionListId("plugins.matetools.matetoolsParserInputView.toolBarList"); //$NON-NLS-1$

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

		if(modelStorageSelect==null) {
			modelStorageSelect = new JComboBox<>(new DefaultComboBoxModel<String>());
			modelStorageSelect.setEditable(false);

			UIUtil.fitToContent(modelStorageSelect, 130, 200, 24);
		}

		builder.addOption("selectTokenizer", tokenizerSelect); //$NON-NLS-1$
		builder.addOption("selectModelSet", modelStorageSelect); //$NON-NLS-1$

		refreshModelSelect();

		return builder;
	}

	@Override
	protected void buildToolBar(ToolBarDelegate delegate) {
		delegate.add(getDefaultActionManager().getAction(
				"plugins.matetools.matetoolsParserInputView.openPreferencesAction")); //$NON-NLS-1$
		delegate.add(getDefaultActionManager().getAction(
				"plugins.matetools.matetoolsParserInputView.startPipelineAction")); //$NON-NLS-1$
	}

	/*@Override
	protected void refreshInfoPanel(InfoPanel infoPanel) {
		infoPanel.add(progressPanel, GridBagConstraints.CENTER);
	}*/

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

	private void refreshModelSelect() {
		if(modelStorageSelect==null) {
			return;
		}

		Object selectedItem = modelStorageSelect.getSelectedItem();

		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)modelStorageSelect.getModel();
		model.removeAllElements();

		List<?> storages = (List<?>) ConfigRegistry.getGlobalRegistry().getValue(
				"plugins.matetools.parser.models"); //$NON-NLS-1$

		if(storages==null || storages.isEmpty()) {
			modelStorageSelect.setSelectedItem(null);
		} else {
			boolean selectionValid = false;

			for(Object item : storages) {
				ModelStorage ms = (ModelStorage) item;
				if(!selectionValid && selectedItem!=null && ms.getLanguage().equals(selectedItem)) {
					selectionValid = true;
				}
				model.addElement(ms.getLanguage());
			}

			if(selectionValid) {
				modelStorageSelect.setSelectedItem(selectedItem);
			} else if(model.getSize()>0) {
				modelStorageSelect.setSelectedIndex(0);
			} else {
				modelStorageSelect.setSelectedItem(null);
			}
		}
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

		// Fetch model storage
		List<?> storages = (List<?>) ConfigRegistry.getGlobalRegistry().getValue(
				"plugins.matetools.parser.models"); //$NON-NLS-1$
		ModelStorage storage = null;
		String language = (String) modelStorageSelect.getSelectedItem();
		if(language!=null && storages!=null) {
			for(Object item : storages) {
				ModelStorage ms = (ModelStorage) item;
				if(ms.isLanguage(language)) {
					storage = ms;
					break;
				}
			}
		}
		if(storage==null || storage.isEmpty()) {
			new DialogDispatcher(null,
					"plugins.matetools.matetoolsParserInputView.title",  //$NON-NLS-1$
					"plugins.matetools.matetoolsParserInputView.invalidModel",  //$NON-NLS-1$
					language).showAsError();
			return;
		}

		// Fetch tokenizer
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
		worker = new PipelineWorker(tokens, storage, options);

		// Refresh progress panel
		//progressPanel.setTitle(worker.getDescription());
		//progressPanel.setTask(worker);

		// Schedule worker for concurrent execution on background thread
		TaskManager.getInstance().schedule(
				worker, null, TaskPriority.DEFAULT, true);

		refreshActions();
	}

	private void appendResult(DependencySentenceData data) {
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

	private class Handler implements DocumentListener, ConfigListener {

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

		/**
		 * @see de.ims.icarus.config.ConfigListener#invoke(de.ims.icarus.config.ConfigRegistry, de.ims.icarus.config.ConfigEvent)
		 */
		@Override
		public void invoke(ConfigRegistry sender, ConfigEvent event) {
			refreshModelSelect();
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
				UIUtil.beep();

				showError(ex);
			}
		}

		public void unloadModels(ActionEvent e) {

			MatetoolsPipeline pipeline = MatetoolsPipeline.getPipeline(pipelineOwner);

			// Ensure we have ownership of the pipeline or abort otherwise
			// This is required to make sure that we cannot interfere with a planned operation
			// from another pipeline owner!
			if(pipeline==null) {
				return;
			}

			try {
				 pipeline.unloadModels();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to unload models for parser-pipeline", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void openPreferences(ActionEvent e) {
			try {
				UIUtil.openConfigDialog("plugins.matetools.parser"); //$NON-NLS-1$
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to open preferences", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
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

	private class PipelineWorker extends SwingWorker<DependencySentenceData, DependencySentenceData> implements Identity {

		private final String[] tokens;
		private final ModelStorage storage;
		private final Options options;

		private int stage = 0;

		PipelineWorker(String[] tokens, ModelStorage storage, Options options) {
			if(tokens==null)
				throw new NullPointerException("Invalid tokens array"); //$NON-NLS-1$

			this.tokens = tokens;
			this.storage = storage;
			this.options = options;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected DependencySentenceData doInBackground() throws Exception {
			MatetoolsPipeline pipeline = MatetoolsPipeline.getPipeline(pipelineOwner);
			if(pipeline==null)
				throw new IllegalStateException("Synchronization failed - pipeline not owned by "+pipelineOwner.getName()); //$NON-NLS-1$

			return pipeline.runPipeline(tokens, storage, options);
		}

		private void publishIntermediateResult(DependencySentenceData data) {
			stage++;
			setProgress(stage*25);
			firePropertyChange(TaskConstants.INFO_PROPERTY, null, getDescription());
			publish(data);
		}

		@Override
		protected void process(List<DependencySentenceData> chunks) {
			if(chunks==null || chunks.isEmpty()) {
				return;
			}

			// Fetch latest state
			DependencySentenceData data = chunks.get(chunks.size()-1);

			// Send data item
			Options options = new Options();
			options.put(Options.CONTENT_TYPE, DependencyUtils.getDependencyContentType());

			Message message = new Message(MatetoolsParserInputView.this,
					Commands.DISPLAY, data, options);

			sendRequest(Outline.class, message);
		}

		@Override
		protected void done() {
			worker = null;
			DependencySentenceData data;
			try {
				data = get();
				if(data!=null) {
					appendResult(data);
				}
			} catch(InterruptedException | CancellationException e) {
				// ignore
			} catch (Throwable e) {
//				if(e instanceof ExecutionException) {
//					e = e.getCause();
//				}
				LoggerFactory.log(this, Level.SEVERE,
						"Unexpected exception while obtaining final pipeline computation result", e); //$NON-NLS-1$
				UIUtil.beep();

				if(!Core.getCore().handleThrowable(e)) {
					showErrorDialog(e);
				}
			} finally {
				MatetoolsPipeline.releasePipeline(pipelineOwner);

				refreshActions();
			}
		}

		private void showErrorDialog(Throwable e) {
			String title = "plugins.matetools.matetoolsParserInputView.errorTitle"; //$NON-NLS-1$

			String message = null;
			if(e instanceof OutOfMemoryError) {
				message = "plugins.matetools.matetoolsParserInputView.outOfMemoryError"; //$NON-NLS-1$
			}

			if(message==null) {
				message = "plugins.matetools.matetoolsParserInputView.generalError"; //$NON-NLS-1$
			}

			DialogFactory.getGlobalFactory().showError(getFrame(), title, message);
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return getIdentity().getId()+"-PipelineWorker"; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"plugins.matetools.matetoolsParserInputView.pipelineWorker.name"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			String key = stageLabels[stage];
			return ResourceManager.getInstance().get(key);
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}

	}

	private class ResultList extends AbstractDataList<SentenceData> implements SentenceDataList {

		private List<DependencySentenceData> items;

		private void addData(DependencySentenceData data) {
			if(items==null) {
				items = new ArrayList<>();
			}

			items.add(data);

			fireChangeEvent();
		}

		/**
		 * @see de.ims.icarus.util.data.DataList#size()
		 */
		@Override
		public int size() {
			return items==null ? 0 : items.size();
		}

		/**
		 * @see de.ims.icarus.util.data.DataList#get(int)
		 */
		@Override
		public DependencySentenceData get(int index) {
			return items==null ? null : items.get(index);
		}

		/**
		 * @see de.ims.icarus.util.data.DataList#getContentType()
		 */
		@Override
		public ContentType getContentType() {
			return DependencyUtils.getDependencyContentType();
		}

		/**
		 * @see de.ims.icarus.language.SentenceDataList#supportsType(de.ims.icarus.language.DataType)
		 */
		@Override
		public boolean supportsType(DataType type) {
			return type==DataType.SYSTEM;
		}

		/**
		 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType)
		 */
		@Override
		public SentenceData get(int index, DataType type) {
			return type==DataType.SYSTEM ? get(index) : null;
		}

		/**
		 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType, de.ims.icarus.language.AvailabilityObserver)
		 */
		@Override
		public SentenceData get(int index, DataType type,
				AvailabilityObserver observer) {
			return get(index, type);
		}

	}
}
