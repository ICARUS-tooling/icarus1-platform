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
package de.ims.icarus.plugins.coref.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.java.plugin.registry.Extension;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry.LoadJob;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.ExtensionListModel;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.coref.CoreferencePlugin;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentDataPresenter implements AWTPresenter {

	private CoreferenceDocumentData document;
	private Options options;
	private AWTPresenter presenter;
	
	private Map<Extension, AWTPresenter> presenterInstances;
	
	private Handler handler;
	private CallbackHandler callbackHandler;
	private ActionManager actionManager;
	
	private JPanel contentPanel;
	private JTextArea infoLabel;
	private JComboBox<Extension> presenterSelect;
	private JLabel loadingLabel;
	private JLabel headerLabel;
	private JToolBar toolBar;

	public CoreferenceDocumentDataPresenter() {
		// no-op
	}
	
	private ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = ActionManager.globalManager().derive();
		}
		return actionManager;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible(
				CoreferenceUtils.getCoreferenceDocumentContentType(), type);
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if(data==null)
			throw new UnsupportedPresentationDataException("Invalid data"); //$NON-NLS-1$
		if(!(data instanceof CoreferenceDocumentData))
			throw new UnsupportedPresentationDataException("Unsupported data type: "+data.getClass()); //$NON-NLS-1$
		
		displayDocument((CoreferenceDocumentData) data, options);
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		if(presenter!=null) {
			presenter.clear();
		}
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
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

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return document!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return document;
	}

	/**
	 * @see de.ims.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPanel==null) {
			buildPanel();
		}
		return contentPanel;
	}


	private void buildPanel() {
		
		// Load actions
		ActionManager actionManager = getActionManager();
		URL actionLocation = CoreferenceDocumentDataPresenter.class.getResource("coreference-document-data-presenter-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: coreference-document-data-presenter-actions.xml"); //$NON-NLS-1$
		
		try {
			actionManager.loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to load actions from file: "+actionLocation, e); //$NON-NLS-1$
			
			UIDummies.createDefaultErrorOutput(contentPanel, e);
			return;
		}
		
		handler = new Handler();
		
		contentPanel = new JPanel(new BorderLayout());
		
		infoLabel = UIUtil.defaultCreateInfoLabel(contentPanel);
		
		loadingLabel = UIUtil.defaultCreateLoadingLabel(contentPanel);
		
		presenterSelect = new JComboBox<>(
				new ExtensionListModel(CoreferencePlugin.getCoreferencePresenterExtensions(), true));
		presenterSelect.setEditable(false);
		presenterSelect.setRenderer(ExtensionListCellRenderer.getSharedInstance());
		presenterSelect.setSelectedItem(loadDefaultPresenter());
		UIUtil.fitToContent(presenterSelect, 80, 150, 22);
		presenterSelect.addActionListener(handler);
		
		headerLabel = new JLabel();
		UIUtil.resizeComponent(headerLabel, 300, 22);
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		headerLabel.setBorder(new EmptyBorder(0, 4, 0, 4));
		
		Options options = new Options();
		options.put("selectPresenter", presenterSelect); //$NON-NLS-1$
		options.put("headerLabel", headerLabel); //$NON-NLS-1$
		
		toolBar = getActionManager().createToolBar(
				"plugins.coref.coreferenceDocumentView.toolBarList", options); //$NON-NLS-1$
		
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
		headerLabel.setText("-"); //$NON-NLS-1$
		
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
		
		ActionManager actionManager = getActionManager();
		
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
		
		// Hint on whether to rebuild content panel
		boolean requiresUIUpdate = contentPanel.getComponentCount()==0;
		
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

		
		String header = document.getId();
		
		if(header==null) {
			header = (String) document.getProperty(CoreferenceDocumentData.DOCUMENT_HEADER_PROPERTY);
		}
		if(header==null) {
			header = "<unnamed> "+StringUtil.formatDecimal(document.getDocumentIndex()); //$NON-NLS-1$
		}
		header = ResourceManager.getInstance().get("plugins.coref.labels.document") //$NON-NLS-1$
				+": "+header; //$NON-NLS-1$
		headerLabel.setText(header);
		
		if(requiresUIUpdate) {
			contentPanel.removeAll();
			contentPanel.add(toolBar, BorderLayout.NORTH);
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
	
	public class CallbackHandler {
		private CallbackHandler() {
			// no-op
		}
		
		public void clearView(ActionEvent e) {
			try {
				clear();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to reset view", ex); //$NON-NLS-1$
				
				UIUtil.beep();
			}
		}
		
		public void refreshView(ActionEvent e) {
			try {
				refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to refresh view", ex); //$NON-NLS-1$
				
				UIUtil.beep();
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
