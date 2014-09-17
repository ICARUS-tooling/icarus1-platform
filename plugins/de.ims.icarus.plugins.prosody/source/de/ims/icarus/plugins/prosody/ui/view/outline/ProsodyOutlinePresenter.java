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
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.MutableComboBoxModel;

import org.java.plugin.registry.Extension;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.config.ConfigUtils;
import de.ims.icarus.language.coref.annotation.AnnotatedCoreferenceDocumentData;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotationManager;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.coref.view.CoreferenceDocumentDataPresenter;
import de.ims.icarus.plugins.coref.view.PatternExample;
import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.pattern.LabelPattern;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.view.outline.SentencePanel.PanelConfig;
import de.ims.icarus.resources.Localizable;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.TooltipFreezer;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.ChoiceFormEntry;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.dialog.DummyFormEntry;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.list.ListUtils;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.Presenter;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.HtmlUtils;
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
	protected final PanelConfig panelConfig = new PanelConfig();

	protected JComboBox<Object> sentencePatternSelect, detailPatternSelect, headerPatternSelect;
	protected JLabel patternSelectInfo;

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

		actionManager.addHandler("plugins.prosody.prosodyOutlinePresenter.openPreferencesAction",
				callbackHandler, "openPreferences");
		actionManager.addHandler("plugins.prosody.prosodyOutlinePresenter.refreshAction",
				callbackHandler, "refresh");
		actionManager.addHandler("plugins.prosody.prosodyOutlinePresenter.editLabelPatternsAction",
				callbackHandler, "editLabelPatterns");
		//TODO
	}

	protected void refreshActions() {
		ActionManager actionManager = getActionManager();

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

		refreshActions();

		if(data==null || data.size()==0) {

			JLabel label = new JLabel("Nothing to display");//FIXME externalize string //$NON-NLS-1$
			contentPane.setViewportView(label);

			return;
		}

		FormLayout layout = new FormLayout("fill:pref:grow"); //$NON-NLS-1$
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);

		for(int i=0; i<data.size(); i++) {
			ProsodicSentenceData sentence = data.get(i);

			SentencePanel sentencePanel = new SentencePanel(panelConfig);
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

	private LabelPattern loadPattern(Handle handle, LabelPattern defaultPattern) {
		//TODO add sanity check and user notification
		String s = handle.getSource().getString(handle);
		if(s==null) {
			return defaultPattern;
		}

		s = LabelPattern.unescapePattern(s);

		return new LabelPattern(s);
	}

	protected void reloadConfig(Handle handle) {
		ConfigRegistry registry = handle.getSource();

		// General
		panelConfig.antiAliasingType = registry.getValue(registry.getChildHandle(handle, "antiAliasingType"), PanelConfig.DEFAULT_ANTIALIASING_TYPE); //$NON-NLS-1$
		panelConfig.mouseWheelScrollSupported = registry.getBoolean(registry.getChildHandle(handle, "mouseWheelScrollSupported")); //$NON-NLS-1$
		panelConfig.loopSound = registry.getBoolean(registry.getChildHandle(handle, "loopSound")); //$NON-NLS-1$
		panelConfig.wordAlignmentColor = registry.getColor(registry.getChildHandle(handle, "wordAlignmentColor")); //$NON-NLS-1$
		panelConfig.syllableAlignmentColor = registry.getColor(registry.getChildHandle(handle, "syllableAlignmentColor")); //$NON-NLS-1$

		// Text
		Handle textHandle = registry.getChildHandle(handle, "text"); //$NON-NLS-1$
		panelConfig.sentencePattern = loadPattern(registry.getChildHandle(textHandle, "sentencePattern"), PanelConfig.DEFAULT_SENTENCE_PATTERN); //$NON-NLS-1$
		panelConfig.headerPattern = loadPattern(registry.getChildHandle(textHandle, "headerPattern"), PanelConfig.DEFAULT_HEADER_PATTERN); //$NON-NLS-1$
		panelConfig.textShowAlignment = registry.getBoolean(registry.getChildHandle(textHandle, "showAlignment")); //$NON-NLS-1$
		Handle fontHandle = registry.getChildHandle(textHandle, "font"); //$NON-NLS-1$
		panelConfig.sentenceFont = ConfigUtils.defaultReadFont(fontHandle);
		panelConfig.sentenceTextColor = registry.getColor(registry.getChildHandle(fontHandle, "fontColor")); //$NON-NLS-1$

		// Preview
		Handle previewHandle = registry.getChildHandle(handle, "preview"); //$NON-NLS-1$
		panelConfig.previewSize = registry.getValue(registry.getChildHandle(previewHandle, "previewSize"), PanelConfig.DEFAULT_PREVIEW_SIZE); //$NON-NLS-1$
		panelConfig.leftSyllableBound = registry.getFloat(registry.getChildHandle(previewHandle, "leftSyllableBound")); //$NON-NLS-1$
		panelConfig.rightSyllableBound = registry.getFloat(registry.getChildHandle(previewHandle, "rightSyllableBound")); //$NON-NLS-1$
		panelConfig.curveColor = registry.getColor(registry.getChildHandle(previewHandle, "curveColor")); //$NON-NLS-1$
		panelConfig.previewShowAlignment = registry.getBoolean(registry.getChildHandle(previewHandle, "showAlignment")); //$NON-NLS-1$

		// Detail
		Handle detailHandle = registry.getChildHandle(handle, "detail"); //$NON-NLS-1$
		panelConfig.wordScope = registry.getInteger(registry.getChildHandle(detailHandle, "wordScope")); //$NON-NLS-1$
		panelConfig.syllableScope = registry.getInteger(registry.getChildHandle(detailHandle, "syllableScope")); //$NON-NLS-1$
		panelConfig.graphHeight = registry.getInteger(registry.getChildHandle(detailHandle, "graphHeight")); //$NON-NLS-1$
		panelConfig.graphWidth = registry.getInteger(registry.getChildHandle(detailHandle, "graphWidth")); //$NON-NLS-1$
		panelConfig.wordSpacing = registry.getInteger(registry.getChildHandle(detailHandle, "wordSpacing")); //$NON-NLS-1$
		panelConfig.graphSpacing = registry.getInteger(registry.getChildHandle(detailHandle, "graphSpacing")); //$NON-NLS-1$
		panelConfig.clearLabelBackground = registry.getBoolean(registry.getChildHandle(detailHandle, "clearLabelBackground")); //$NON-NLS-1$
		panelConfig.detailPattern = loadPattern(registry.getChildHandle(detailHandle, "detailPattern"), PanelConfig.DEFAULT_DETAIL_PATTERN); //$NON-NLS-1$
		fontHandle = registry.getChildHandle(detailHandle, "font"); //$NON-NLS-1$
		panelConfig.detailFont = ConfigUtils.defaultReadFont(fontHandle);
		panelConfig.detailTextColor = registry.getColor(registry.getChildHandle(fontHandle, "fontColor")); //$NON-NLS-1$

		panelConfig.detailAxisColor = registry.getColor(registry.getChildHandle(detailHandle, "axisColor")); //$NON-NLS-1$
		panelConfig.detailAxisMarkerColor = registry.getColor(registry.getChildHandle(detailHandle, "axisMarkerColor")); //$NON-NLS-1$
		panelConfig.detailAxisMarkerHeight = registry.getInteger(registry.getChildHandle(detailHandle, "axisMarkerHeight")); //$NON-NLS-1$
		fontHandle = registry.getChildHandle(detailHandle, "axisFont"); //$NON-NLS-1$
		panelConfig.detailAxisLabelFont = ConfigUtils.defaultReadFont(fontHandle);
		panelConfig.detailAxisLabelColor = registry.getColor(registry.getChildHandle(fontHandle, "fontColor")); //$NON-NLS-1$

		panelConfig.detailPaintBorder = registry.getBoolean(registry.getChildHandle(detailHandle, "paintBorder")); //$NON-NLS-1$
		panelConfig.detailBorderColor = registry.getColor(registry.getChildHandle(detailHandle, "borderColor")); //$NON-NLS-1$
		panelConfig.detailPaintGrid = registry.getBoolean(registry.getChildHandle(detailHandle, "paintGrid")); //$NON-NLS-1$
		panelConfig.detailGridColor = registry.getColor(registry.getChildHandle(detailHandle, "gridColor")); //$NON-NLS-1$
		panelConfig.detailGridStyle = registry.getValue(registry.getChildHandle(detailHandle, "gridStyle"), PaIntEGraph.DEFAULT_GRID_STYLE); //$NON-NLS-1$

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

		public void openPreferences(ActionEvent e) {
			try {
				UIUtil.openConfigDialog(configPath);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to open preferences", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void refresh(ActionEvent e) {
			try {
				ProsodyOutlinePresenter.this.refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to refresh presenter", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		private JComboBox<Object> createPatternSelect(String defaultPattern) {
			JComboBox<Object> patternSelect = new JComboBox<>();
			String pattern = defaultPattern;

			if(pattern!=null && pattern.trim().isEmpty()) {
				pattern = null;
			}

			patternSelect.setEditable(true);
			if(pattern!=null) {
				MutableComboBoxModel<Object> model = (MutableComboBoxModel<Object>) patternSelect.getModel();
				model.addElement(pattern);
			}
			patternSelect.setSelectedItem(pattern);
			UIUtil.resizeComponent(patternSelect, 300, 24);

			return patternSelect;
		}

		private String getPattern(JComboBox<Object> comboBox) {
			Object value = comboBox.getSelectedItem();
			String pattern = null;

			if(value instanceof String) {
				pattern = (String) value;
			} else if(value instanceof PatternExample) {
				pattern = ((PatternExample)value).getPattern();
			}

			return pattern;
		}

		private void addPattern(Object pattern, JComboBox<Object> comboBox) {

			// Legal pattern
			MutableComboBoxModel<Object> model =
					(MutableComboBoxModel<Object>) comboBox.getModel();

			int index = ListUtils.indexOf(pattern, model);

			if(index==-1) {
				model.addElement(pattern);
			}
		}

		private String createPatternSelectTooltip() {
			StringBuilder sb = new StringBuilder(300);
			ResourceManager rm = ResourceManager.getInstance();

			sb.append("<html>"); //$NON-NLS-1$
			sb.append("<h3>").append(rm.get("plugins.prosody.labelPattern.title")).append("</h3>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sb.append("<table>"); //$NON-NLS-1$
			sb.append("<tr><th>") //$NON-NLS-1$
				.append(rm.get("plugins.prosody.labelPattern.character")).append("</th><th>") //$NON-NLS-1$ //$NON-NLS-2$
				.append(rm.get("plugins.prosody.labelPattern.description")).append("</th></tr>"); //$NON-NLS-1$ //$NON-NLS-2$

			Map<Object, Object> mc = LabelPattern.magicCharacters;
			for(Entry<Object, Object> entry : mc.entrySet()) {
				String c = entry.getKey().toString();
				String key = entry.getValue().toString();

				sb.append("<tr><td>").append(HtmlUtils.escapeHTML(c)) //$NON-NLS-1$
				.append("</td><td>").append(rm.get(key)).append("</td></tr>"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			sb.append("</table>"); //$NON-NLS-1$

			return sb.toString();
		}

		public void editLabelPatterns(ActionEvent e) {
			if(sentencePatternSelect==null) {
				String pattern = ConfigRegistry.getGlobalRegistry().getString(
						configPath+".text.sentencePattern"); //$NON-NLS-1$
				sentencePatternSelect = createPatternSelect(pattern);
			}
			if(headerPatternSelect==null) {
				String pattern = ConfigRegistry.getGlobalRegistry().getString(
						configPath+".text.headerPattern"); //$NON-NLS-1$
				headerPatternSelect = createPatternSelect(pattern);
			}
			if(detailPatternSelect==null) {
				String pattern = ConfigRegistry.getGlobalRegistry().getString(
						configPath+".detail.detailPattern"); //$NON-NLS-1$
				detailPatternSelect = createPatternSelect(pattern);
			}

			if(patternSelectInfo==null) {
				final JLabel label = new JLabel();
				label.addMouseListener(new TooltipFreezer());
				label.setIcon(UIUtil.getInfoIcon());

				Localizable localizable = new Localizable() {

					@Override
					public void localize() {
						label.setToolTipText(createPatternSelectTooltip());
					}
				};

				localizable.localize();
				ResourceManager.getInstance().getGlobalDomain().addItem(localizable);

				patternSelectInfo = label;
			}

			sentencePatternSelect.setSelectedItem(LabelPattern.escapePattern(panelConfig.sentencePattern.getPattern()));
			headerPatternSelect.setSelectedItem(LabelPattern.escapePattern(panelConfig.headerPattern.getPattern()));
			detailPatternSelect.setSelectedItem(LabelPattern.escapePattern(panelConfig.detailPattern.getPattern()));

			FormBuilder formBuilder = FormBuilder.newLocalizingBuilder();
			formBuilder.addEntry("info", new DummyFormEntry( //$NON-NLS-1$
					"plugins.prosody.prosodyOutlinePresenter.dialogs.editPattern.info", patternSelectInfo)); //$NON-NLS-1$
			formBuilder.addEntry("sentencePattern", new ChoiceFormEntry( //$NON-NLS-1$
					"plugins.prosody.prosodyOutlinePresenter.dialogs.editPattern.sentencePattern", sentencePatternSelect)); //$NON-NLS-1$
			formBuilder.addEntry("headerPattern", new ChoiceFormEntry( //$NON-NLS-1$
					"plugins.prosody.prosodyOutlinePresenter.dialogs.editPattern.headerPattern", headerPatternSelect)); //$NON-NLS-1$
			formBuilder.addEntry("detailPattern", new ChoiceFormEntry( //$NON-NLS-1$
					"plugins.prosody.prosodyOutlinePresenter.dialogs.editPattern.detailPattern", detailPatternSelect)); //$NON-NLS-1$

			formBuilder.buildForm();

			if(DialogFactory.getGlobalFactory().showGenericDialog(
					null,
					DialogFactory.OK_CANCEL_OPTION,
					"plugins.prosody.prosodyOutlinePresenter.dialogs.editPattern.title", //$NON-NLS-1$
					"plugins.prosody.prosodyOutlinePresenter.dialogs.editPattern.message", //$NON-NLS-1$
					formBuilder.getContainer(),
					true)) {

				// Sentence pattern
				String sentencePattern = getPattern(sentencePatternSelect);
				if(sentencePattern==null || sentencePattern.isEmpty()) {
					// Ensure minimum label!
					sentencePattern = "$form$"; //$NON-NLS-1$
				}

				try {
					panelConfig.sentencePattern = new LabelPattern(LabelPattern.unescapePattern(sentencePattern));
					addPattern(sentencePattern, sentencePatternSelect);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Invalid node pattern: "+sentencePattern, ex); //$NON-NLS-1$

					UIUtil.beep();
					DialogFactory.getGlobalFactory().showError(null,
							"plugins.prosody.prosodyOutlinePresenter.dialogs.invalidSentencePattern.title",  //$NON-NLS-1$
							"plugins.prosody.prosodyOutlinePresenter.dialogs.invalidSentencePattern.message",  //$NON-NLS-1$
							sentencePattern);

					return;
				}

				// Header pattern
				String headerPattern = getPattern(headerPatternSelect);
				if(headerPattern==null) {
					headerPattern = ""; //$NON-NLS-1$
				}

				try {
					panelConfig.headerPattern = new LabelPattern(LabelPattern.unescapePattern(headerPattern));
					addPattern(headerPattern, headerPatternSelect);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Invalid edge pattern: "+headerPattern, ex); //$NON-NLS-1$

					UIUtil.beep();
					DialogFactory.getGlobalFactory().showError(null,
							"plugins.prosody.prosodyOutlinePresenter.dialogs.invalidHeaderPattern.title",  //$NON-NLS-1$
							"plugins.prosody.prosodyOutlinePresenter.dialogs.invalidHeaderPattern.message",  //$NON-NLS-1$
							headerPattern);

					return;
				}

				// Header pattern
				String detailPattern = getPattern(detailPatternSelect);
				if(detailPattern==null) {
					detailPattern = ""; //$NON-NLS-1$
				}

				try {
					panelConfig.detailPattern = new LabelPattern(LabelPattern.unescapePattern(detailPattern));
					addPattern(detailPattern, detailPatternSelect);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Invalid edge pattern: "+detailPattern, ex); //$NON-NLS-1$

					UIUtil.beep();
					DialogFactory.getGlobalFactory().showError(null,
							"plugins.prosody.prosodyOutlinePresenter.dialogs.invalidDetailPattern.title",  //$NON-NLS-1$
							"plugins.prosody.prosodyOutlinePresenter.dialogs.invalidDetailPattern.message",  //$NON-NLS-1$
							detailPattern);

					return;
				}

				ProsodyOutlinePresenter.this.refresh();
			}
		}
	}
}
