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
package de.ims.icarus.plugins.prosody.ui.view.outline;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.java.plugin.registry.Extension;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.language.coref.annotation.AnnotatedCoreferenceDocumentData;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotationManager;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.coref.view.CoreferenceDocumentDataPresenter;
import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.Presenter;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Installable;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.annotation.AnnotationControl;
import de.ims.icarus.util.annotation.AnnotationController;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.transfer.ConsumerMenu;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyOutlinePresenter implements AWTPresenter,
	AnnotationController, Installable, Presenter.TextBasedPresenter{

	protected ProsodicDocumentData data;

	protected CoreferenceDocumentDataPresenter.PresenterMenu presenterMenu;
	protected JPopupMenu popupMenu;
	protected ConsumerMenu consumerMenu;

	protected Handler handler;
	protected CallbackHandler callbackHandler;

	protected Options options;

	protected CoreferenceDocumentDataPresenter parent;
	private static ActionManager sharedActionManager;
	protected ActionManager actionManager;
	protected CoreferenceDocumentAnnotationManager annotationManager;

	protected JPanel contentPanel;
	protected JScrollPane contentPane;

	protected static final String configPath = "plugins.prosody.appearance.outline"; //$NON-NLS-1$

	protected static synchronized final ActionManager getSharedActionManager() {
		if(sharedActionManager==null) {
			sharedActionManager = ActionManager.globalManager().derive();

			URL actionLocation = ProsodyOutlinePresenter.class.getResource("prosody-outline-presenter-actions.xml"); //$NON-NLS-1$
			if(actionLocation==null)
				throw new CorruptedStateException("Missing resources: prosody-outline-presenter-actions.xml"); //$NON-NLS-1$

			try {
				sharedActionManager.loadActions(actionLocation);
			} catch (IOException e) {
				LoggerFactory.log(ProsodyOutlinePresenter.class, Level.SEVERE,
						"Failed to load actions from file", e); //$NON-NLS-1$
			}
		}

		return sharedActionManager;
	}

	protected CoreferenceDocumentDataPresenter.PresenterMenu createPresenterMenu() {
		return new CoreferenceDocumentDataPresenter.PresenterMenu(this, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String uid = e.getActionCommand();
				Extension extension = PluginUtil.getExtension(uid);
				if(extension!=null) {
					togglePresenter(extension);
				}
			}
		});
	}

	protected void togglePresenter(Extension extension) {
		if(extension==null)
			throw new NullPointerException("Invalid extension"); //$NON-NLS-1$

		if(parent==null) {
			return;
		}

		//TODO
	}

	protected ConsumerMenu createConsumerMenu() {
		return new ConsumerMenu(this);
	}

	protected ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = getSharedActionManager().derive();

			registerActionCallbacks();
		}

		return actionManager;
	}

	@Override
	public CoreferenceDocumentAnnotationManager getAnnotationManager() {
		if(annotationManager==null) {
			annotationManager = new CoreferenceDocumentAnnotationManager();
			annotationManager.addPropertyChangeListener("position", getHandler()); //$NON-NLS-1$
			annotationManager.addPropertyChangeListener("displayMode", getHandler()); //$NON-NLS-1$
		}
		return annotationManager;
	}

	protected AnnotationControl createAnnotationControl() {
		AnnotationControl annotationControl = new AnnotationControl(true);
		annotationControl.setAnnotationManager(getAnnotationManager());
		return annotationControl;
	}

	protected void registerActionCallbacks() {
		ActionManager actionManager = getActionManager();

		if(callbackHandler==null) {
			callbackHandler = createCallbackHandler();
		}

		//TODO
	}

	protected Handler createHandler() {
		return new Handler();
	}

	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler();
	}

	protected Handler getHandler() {
		if(handler==null) {
			handler = createHandler();
		}
		return handler;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible(getContentType(), type);
	}

	/**
	 * @return
	 */
	public ContentType getContentType() {
		return ProsodyUtils.getProsodyDocumentContentType();
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if(data==null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
		if(!PresenterUtils.presenterSupports(this, data))
			throw new UnsupportedPresentationDataException("Data not supported: "+data.getClass()); //$NON-NLS-1$

		if(options==null) {
			options = Options.emptyOptions;
		}

		this.options = options.clone();
		setData(data);

		if(contentPanel==null) {
			return;
		}

		//TODO reset filter

		refresh();
	}

	protected void setData(Object data) {
		this.data = (ProsodicDocumentData) data;

		if(data instanceof AnnotatedCoreferenceDocumentData) {
			getAnnotationManager().setAnnotation(((AnnotatedData)data).getAnnotation());
		}
	}

	protected void refresh() {

		contentPane.setViewportView(null);

		if(data==null || data.size()==0) {

			JLabel label = new JLabel("Nothing to display");//FIXME externalize string //$NON-NLS-1$
			contentPane.setViewportView(label);

			return;
		}

		FormLayout layout = new FormLayout("fill:pref:grow"); //$NON-NLS-1$
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);

		for(int i=0; i<data.size(); i++) {
			ProsodicSentenceData sentence = data.get(i);

			SentencePanel sentencePanel = new SentencePanel();
			sentencePanel.refresh(sentence);
			builder.append(sentencePanel);
		}

		contentPane.setViewportView(builder.getPanel());
	}

	@Override
	public void install(Object target) {
		parent = null;

		if(target instanceof CoreferenceDocumentDataPresenter) {
			parent = (CoreferenceDocumentDataPresenter) target;
		}
	}

	@Override
	public void uninstall(Object target) {
		target = null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
//		if(!isPresenting()) {
//			return;
//		}

		options = null;
		setData(null);

	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return data!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public ProsodicDocumentData getPresentedData() {
		return data;
	}

	/**
	 * @see de.ims.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPanel==null) {
			ConfigRegistry.getGlobalRegistry().addGroupListener(configPath, getHandler());
			contentPanel = createContentPanel();
			reloadConfig(ConfigRegistry.getGlobalRegistry().getHandle(configPath));
			refresh();
		}

		return contentPanel;
	}

	protected ActionComponentBuilder createToolBar() {
		ActionComponentBuilder builder = new ActionComponentBuilder(getActionManager());
		builder.setActionListId("plugins.prosody.prosodyOutlinePresenter.toolBarList"); //$NON-NLS-1$

		AnnotationControl annotationControl = createAnnotationControl();
		if(annotationControl!=null) {
			builder.addOption("annotationControl", annotationControl.getComponents()); //$NON-NLS-1$
		}

		return builder;
	}

	protected JPanel createContentPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		contentPane = new JScrollPane();
		UIUtil.defaultSetUnitIncrement(contentPane);
		panel.add(contentPane, BorderLayout.CENTER);

		JToolBar toolBar = createToolBar().buildToolBar();
		if(toolBar!=null) {
			panel.add(toolBar, BorderLayout.NORTH);
		}

		return panel;
	}

	protected void reloadConfig(Handle handle) {
		//TODO

		// Refresh is required to allow the underlying document
		// to adjust its style definitions to the new font and color settings
		refresh();
	}

	protected class Handler implements ConfigListener, PropertyChangeListener {

		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see de.ims.icarus.config.ConfigListener#invoke(de.ims.icarus.config.ConfigRegistry, de.ims.icarus.config.ConfigEvent)
		 */
		@Override
		public void invoke(ConfigRegistry sender, ConfigEvent event) {
			reloadConfig(event.getHandle());
		}

	}

	public class CallbackHandler {
		protected CallbackHandler() {
			// no-op
		}
	}
}
