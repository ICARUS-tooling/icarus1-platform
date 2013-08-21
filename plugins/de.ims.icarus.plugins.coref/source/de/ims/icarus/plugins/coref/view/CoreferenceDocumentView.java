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
package de.ims.icarus.plugins.coref.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.java.plugin.registry.Extension;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry.LoadJob;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.ExtensionListModel;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.coref.CoreferencePlugin;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentView extends View {

	private CoreferenceDocumentData document;
	private Options options;
	private AWTPresenter presenter;
	
	private Map<Extension, AWTPresenter> presenterInstances;
	
	private Handler handler;
	private CallbackHandler callbackHandler;
	
	private JPanel contentPanel;
	private JTextArea infoLabel;
	private JComboBox<Extension> presenterSelect;
	private JLabel loadingLabel;

	public CoreferenceDocumentView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		if(!defaultLoadActions(CoreferenceDocumentView.class, 
				"coreference-document-view-actions.xml")) { //$NON-NLS-1$
			return;
		}
		
		handler = new Handler();
		
		container.setLayout(new BorderLayout());
		
		contentPanel = new JPanel(new BorderLayout());
		
		infoLabel = UIUtil.defaultCreateInfoLabel(container);
		
		loadingLabel = UIUtil.defaultCreateLoadingLabel(container);
		
		presenterSelect = new JComboBox<>(
				new ExtensionListModel(CoreferencePlugin.getCoreferencePresenterExtensions(), true));
		presenterSelect.setEditable(false);
		presenterSelect.setRenderer(ExtensionListCellRenderer.getSharedInstance());
		presenterSelect.setSelectedItem(loadDefaultPresenter());
		UIUtil.fitToContent(presenterSelect, 80, 150, 22);
		presenterSelect.addActionListener(handler);
		
		Options options = new Options();
		options.put("selectPresenter", presenterSelect); //$NON-NLS-1$
		
		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.coref.coreferenceDocumentView.toolBarList", options); //$NON-NLS-1$
		
		container.add(toolBar, BorderLayout.NORTH);
		container.add(contentPanel, BorderLayout.CENTER);
		
		registerActionCallbacks();
		refreshActions();
		
		showInfo(null);
	}
	
	private Object loadDefaultPresenter() {

		Object defaultPresenter = ConfigRegistry.getGlobalRegistry().getValue(
				"plugins.coref.appearance.defaultDocumentPresenter"); //$NON-NLS-1$
		
		if("NONE".equals(defaultPresenter)) { //$NON-NLS-1$
			defaultPresenter = null;
		}
		
		if(defaultPresenter instanceof String) {
			try {
				defaultPresenter = PluginUtil.getExtension((String) defaultPresenter);
			} catch(Exception e) {
				LoggerFactory.log(this,	Level.SEVERE, "Failed to fetch presenter-extension: "+defaultPresenter, e); //$NON-NLS-1$
			}
		}
		
		return defaultPresenter;
	}
	
	private void showInfo(String text) {
		if(text==null) {
			text = ResourceManager.getInstance().get(
					"plugins.coref.coreferenceDocumentView.notAvailable"); //$NON-NLS-1$
		}
		infoLabel.setText(text);
		
		contentPanel.removeAll();
		contentPanel.add(infoLabel, BorderLayout.CENTER);
		contentPanel.revalidate();
		contentPanel.repaint();
		
		if(presenter!=null) {
			presenter.clear();
			presenter = null;
		}
	}
	
	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.addHandler("plugins.coref.coreferenceDocumentView.refreshViewAction",  //$NON-NLS-1$
				callbackHandler, "refreshView"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentView.clearViewAction",  //$NON-NLS-1$
				callbackHandler, "clearView"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentView.openPreferencesAction", //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
	}
	
	private void refreshActions() {
		// TODO
	}
	
	private AWTPresenter getSelectedPresenter() {
		Extension extension = (Extension) presenterSelect.getSelectedItem();
		if(extension==null)
			throw new IllegalStateException("No presenter selected"); //$NON-NLS-1$
		
		if(presenterInstances==null) {
			presenterInstances = new HashMap<>();
		}
		
		AWTPresenter presenter = presenterInstances.get(extension);
		if(presenter==null) {
			try {
				presenter = (AWTPresenter) PluginUtil.instantiate(extension);
				
				presenterInstances.put(extension, presenter);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to instantiate presenter: "+extension.getUniqueId(), e); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		return presenter;
	}

	@Override
	public void close() {
		showInfo(null);
		
		if(presenterInstances!=null) {
			for(Entry<Extension, AWTPresenter> entry : presenterInstances.entrySet()) {
				AWTPresenter presenter = entry.getValue();
				Extension extension = entry.getKey();
				try {
					presenter.close();
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to close presenter: "+extension.getUniqueId(), e); //$NON-NLS-1$
				}
			}
		}
	}

	@Override
	public void reset() {
		showInfo(null);
	}
	
	private void displayDocument(CoreferenceDocumentData document, Options options) {
		if(document==null)
			throw new IllegalArgumentException("Invalid document"); //$NON-NLS-1$
		
		this.document = document;
		this.options = options==null ? null : options.clone();
		
		refresh();
	}
	
	private void refresh() {
		
		CoreferenceDocumentData document = this.document;
		Options options = this.options;
		
		if(document==null) {
			return;
		}
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		// Check if some allocations need to be loaded
		if(checkAllocation(options, "allocation") //$NON-NLS-1$
				|| checkAllocation(options, "goldAllocation")) { //$NON-NLS-1$
			contentPanel.removeAll();
			contentPanel.add(loadingLabel, BorderLayout.CENTER);
			return;
		}
		
		boolean requiresUIUpdate = false;
		
		if(presenter==null) {
			presenter = getSelectedPresenter();
			requiresUIUpdate = true;
		}
		
		if(presenter==null) {
			showInfo(ResourceManager.getInstance().get(
					"plugins.coref.coreferenceDocumentView.invalidPresenter")); //$NON-NLS-1$
			return;
		}
		
		try {
			presenter.present(document, options);
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to forward presentation of document to presenter: "+presenter.getClass().getName(), e); //$NON-NLS-1$
			
			UIUtil.beep();
			showInfo(ResourceManager.getInstance().get(
					"plugins.coref.coreferenceDocumentView.presentationFailed")); //$NON-NLS-1$
			return;
		}
		
		if(requiresUIUpdate) {
			contentPanel.removeAll();
			contentPanel.add(presenter.getPresentingComponent(), BorderLayout.CENTER);
			contentPanel.revalidate();
			contentPanel.repaint();
		}
	}
	
	private boolean checkAllocation(final Options options, final String key) {
		Object value = options.get(key);
		if(value instanceof AllocationDescriptor) {
			final AllocationDescriptor descriptor = (AllocationDescriptor) value;

			options.put(key, descriptor.getAllocation());
			
			if(!descriptor.isLoaded() && !descriptor.isLoading()) {				
				final String name = descriptor.getName();
				String title = ResourceManager.getInstance().get(
						"plugins.coref.labels.loadingAllocation"); //$NON-NLS-1$
				Object task = new LoadJob(descriptor) {
					@Override
					protected void done() {
						try {
							get();
						} catch(CancellationException | InterruptedException e) {
							// ignore
						} catch(Exception e) {
							LoggerFactory.log(this, Level.SEVERE, 
									"Failed to load allocation: "+name, e); //$NON-NLS-1$
							
							UIUtil.beep();
							showError(e);
						} finally {
							contentPanel.remove(loadingLabel);
							contentPanel.revalidate();
							contentPanel.repaint();
									
							refresh();
						}
					}				
				};
				TaskManager.getInstance().schedule(task, title, null, null, 
						TaskPriority.DEFAULT, true);
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.PRESENT.equals(message.getCommand())
				|| Commands.DISPLAY.equals(message.getCommand())) {
			Object data = message.getData();
			if(data instanceof CoreferenceDocumentData) {
				displayDocument((CoreferenceDocumentData) data, message.getOptions());
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
	
	public class CallbackHandler {
		private CallbackHandler() {
			// no-op
		}
		
		public void clearView(ActionEvent e) {
			try {
				reset();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to reset view", ex); //$NON-NLS-1$
				
				UIUtil.beep();
				showError(ex);
			}
		}
		
		public void refreshView(ActionEvent e) {
			try {
				refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to refresh view", ex); //$NON-NLS-1$
				
				UIUtil.beep();
				showError(ex);
			}
		}
		
		public void openPreferences(ActionEvent e) {
			
		}
	}
	
	private class Handler implements ActionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(presenter!=null) {
				presenter.clear();
			}
			
			presenter = null;
			
			refresh();
		}
		
	}
}