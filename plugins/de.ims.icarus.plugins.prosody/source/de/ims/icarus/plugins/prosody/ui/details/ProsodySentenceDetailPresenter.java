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
package de.ims.icarus.plugins.prosody.ui.details;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.MutableComboBoxModel;
import javax.swing.Scrollable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.config.ConfigUtils;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.coref.view.PatternExample;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.painte.PaIntEParams;
import de.ims.icarus.plugins.prosody.pattern.LabelPattern;
import de.ims.icarus.plugins.prosody.sound.SoundOffsets;
import de.ims.icarus.plugins.prosody.sound.SoundPlayer;
import de.ims.icarus.plugins.prosody.sound.SoundPlayer.SoundFile;
import de.ims.icarus.plugins.prosody.ui.TextArea;
import de.ims.icarus.plugins.prosody.ui.geom.Axis;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.view.SentenceInfo;
import de.ims.icarus.plugins.prosody.ui.view.SyllableInfo;
import de.ims.icarus.plugins.prosody.ui.view.WordInfo;
import de.ims.icarus.plugins.prosody.ui.view.outline.SentencePanel.PanelConfig;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.layout.WrapLayout;
import de.ims.icarus.ui.list.ListUtils;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.HtmlUtils;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodySentenceDetailPresenter implements AWTPresenter.TableBasedPresenter {

	private SentenceInfo sentenceInfo;

	private PanelConfig config = new PanelConfig();

	private Options options;
	private JPanel contentPanel;
	private JPanel sentencePanel;

	private CallbackHandler callbackHandler;
	private Handler handler;

	protected ActionManager actionManager;

	protected static final String configPath = "plugins.prosody.appearance.outline"; //$NON-NLS-1$

	private static ActionManager sharedActionManager;

	protected static synchronized final ActionManager getSharedActionManager() {
		if(sharedActionManager==null) {
			sharedActionManager = ActionManager.globalManager().derive();

			URL actionLocation = ProsodySentenceDetailPresenter.class.getResource("prosody-sentence-detail-presenter-actions.xml"); //$NON-NLS-1$
			if(actionLocation==null)
				throw new CorruptedStateException("Missing resources: prosody-sentence-detail-presenter-actions.xml"); //$NON-NLS-1$

			try {
				sharedActionManager.loadActions(actionLocation);
			} catch (IOException e) {
				LoggerFactory.log(ProsodySentenceDetailPresenter.class, Level.SEVERE,
						"Failed to load actions from file", e); //$NON-NLS-1$
			}
		}

		return sharedActionManager;
	}

	public ProsodySentenceDetailPresenter() {
		config.sentencePattern = new LabelPattern("#syllable_label#\n#syllable_duration#"); //$NON-NLS-1$
	}

	protected ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = getSharedActionManager().derive();

			registerActionCallbacks();
		}

		return actionManager;
	}

	protected void registerActionCallbacks() {
		ActionManager actionManager = getActionManager();

		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}

		actionManager.addHandler("plugins.prosody.prosodySentenceDetailPresenter.refreshAction", //$NON-NLS-1$
				callbackHandler, "refresh"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.prosodySentenceDetailPresenter.expandAllAction", //$NON-NLS-1$
				callbackHandler, "expandAll"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.prosodySentenceDetailPresenter.collapseAllAction", //$NON-NLS-1$
				callbackHandler, "collapseAll"); //$NON-NLS-1$
	}

	protected void refreshActions() {
		ActionManager actionManager = getActionManager();

		//TODO
	}

	/**
	 * @see de.ims.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPanel==null) {
			contentPanel = createContentPanel();

			refresh();
		}
		return contentPanel;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible(ProsodyUtils.getProsodySentenceContentType(), type);
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if (data == null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
		if(!PresenterUtils.presenterSupports(this, data))
			throw new UnsupportedPresentationDataException("Data not supported: "+data.getClass()); //$NON-NLS-1$

		if(options==null) {
			options = Options.emptyOptions;
		}

		this.options = options.clone();
		setData((ProsodicSentenceData) data);

		if(contentPanel==null) {
			return;
		}

		//TODO reset filter

		refresh();
	}

	private void setData(ProsodicSentenceData sentence) {
		if(sentence!=null) {
			sentenceInfo = new SentenceInfo(sentence);
		} else {
			sentenceInfo = null;
		}
	}

	public void refresh() {
		if(contentPanel==null) {
			return;
		}

		sentencePanel.setBackground(config.backgroundColor);

		sentencePanel.removeAll();

		if(sentenceInfo!=null) {
			for(int i=0; i<sentenceInfo.wordCount(); i++) {
				WordPanel wordPanel = new WordPanel(sentenceInfo.wordInfo(i));
				wordPanel.rebuild();
				sentencePanel.add(wordPanel);
			}
		}

		sentencePanel.revalidate();
		sentencePanel.repaint();

		refreshActions();
	}

	protected ActionComponentBuilder createToolBar() {
		ActionComponentBuilder builder = new ActionComponentBuilder(getActionManager());
		builder.setActionListId("plugins.prosody.prosodySentenceDetailPresenter.toolBarList"); //$NON-NLS-1$

		return builder;
	}

	protected JPanel createContentPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JToolBar toolBar = createToolBar().buildToolBar();
		if(toolBar!=null) {
			panel.add(toolBar, BorderLayout.NORTH);
		}

		sentencePanel = new SentencePanel();
		//FIXME apply line break behavior!

		JScrollPane scrollPane = new JScrollPane(sentencePanel);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		scrollPane.setBorder(UIUtil.topLineBorder);

		panel.add(scrollPane, BorderLayout.CENTER);

		registerActionCallbacks();

		refreshActions();

		return panel;
	}

	protected Handler getHandler() {
		if(handler==null) {
			handler = new Handler();
		}
		return handler;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		setData(null);

		refresh();
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		clear();
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return sentenceInfo!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return sentenceInfo==null ? null : sentenceInfo.getSentence();
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
		config.antiAliasingType = registry.getValue(registry.getChildHandle(handle, "antiAliasingType"), PanelConfig.DEFAULT_ANTIALIASING_TYPE); //$NON-NLS-1$
		config.mouseWheelScrollSupported = registry.getBoolean(registry.getChildHandle(handle, "mouseWheelScrollSupported")); //$NON-NLS-1$
		config.loopSound = registry.getBoolean(registry.getChildHandle(handle, "loopSound")); //$NON-NLS-1$
		config.wordAlignmentColor = registry.getColor(registry.getChildHandle(handle, "wordAlignmentColor")); //$NON-NLS-1$
		config.syllableAlignmentColor = registry.getColor(registry.getChildHandle(handle, "syllableAlignmentColor")); //$NON-NLS-1$

		// Text
		Handle textHandle = registry.getChildHandle(handle, "text"); //$NON-NLS-1$
		config.sentencePattern = loadPattern(registry.getChildHandle(textHandle, "sentencePattern"), PanelConfig.DEFAULT_SENTENCE_PATTERN); //$NON-NLS-1$
		config.headerPattern = loadPattern(registry.getChildHandle(textHandle, "headerPattern"), PanelConfig.DEFAULT_HEADER_PATTERN); //$NON-NLS-1$
		config.textShowAlignment = registry.getBoolean(registry.getChildHandle(textHandle, "showAlignment")); //$NON-NLS-1$
		Handle fontHandle = registry.getChildHandle(textHandle, "font"); //$NON-NLS-1$
		config.sentenceFont = ConfigUtils.defaultReadFont(fontHandle);
		config.sentenceTextColor = registry.getColor(registry.getChildHandle(fontHandle, "fontColor")); //$NON-NLS-1$

		// Preview
		Handle previewHandle = registry.getChildHandle(handle, "preview"); //$NON-NLS-1$
		config.previewSize = registry.getValue(registry.getChildHandle(previewHandle, "previewSize"), PanelConfig.DEFAULT_PREVIEW_SIZE); //$NON-NLS-1$
		config.leftSyllableBound = registry.getFloat(registry.getChildHandle(previewHandle, "leftSyllableBound")); //$NON-NLS-1$
		config.rightSyllableBound = registry.getFloat(registry.getChildHandle(previewHandle, "rightSyllableBound")); //$NON-NLS-1$
		config.curveColor = registry.getColor(registry.getChildHandle(previewHandle, "curveColor")); //$NON-NLS-1$
		config.previewShowAlignment = registry.getBoolean(registry.getChildHandle(previewHandle, "showAlignment")); //$NON-NLS-1$

		// Detail
		Handle detailHandle = registry.getChildHandle(handle, "detail"); //$NON-NLS-1$
		config.wordScope = registry.getInteger(registry.getChildHandle(detailHandle, "wordScope")); //$NON-NLS-1$
		config.syllableScope = registry.getInteger(registry.getChildHandle(detailHandle, "syllableScope")); //$NON-NLS-1$
		config.graphHeight = registry.getInteger(registry.getChildHandle(detailHandle, "graphHeight")); //$NON-NLS-1$
		config.graphWidth = registry.getInteger(registry.getChildHandle(detailHandle, "graphWidth")); //$NON-NLS-1$
		config.wordSpacing = registry.getInteger(registry.getChildHandle(detailHandle, "wordSpacing")); //$NON-NLS-1$
		config.graphSpacing = registry.getInteger(registry.getChildHandle(detailHandle, "graphSpacing")); //$NON-NLS-1$
		config.clearLabelBackground = registry.getBoolean(registry.getChildHandle(detailHandle, "clearLabelBackground")); //$NON-NLS-1$
		config.detailPattern = loadPattern(registry.getChildHandle(detailHandle, "detailPattern"), PanelConfig.DEFAULT_DETAIL_PATTERN); //$NON-NLS-1$
		fontHandle = registry.getChildHandle(detailHandle, "font"); //$NON-NLS-1$
		config.detailFont = ConfigUtils.defaultReadFont(fontHandle);
		config.detailTextColor = registry.getColor(registry.getChildHandle(fontHandle, "fontColor")); //$NON-NLS-1$

		config.detailAxisColor = registry.getColor(registry.getChildHandle(detailHandle, "axisColor")); //$NON-NLS-1$
		config.detailAxisMarkerColor = registry.getColor(registry.getChildHandle(detailHandle, "axisMarkerColor")); //$NON-NLS-1$
		config.detailAxisMarkerHeight = registry.getInteger(registry.getChildHandle(detailHandle, "axisMarkerHeight")); //$NON-NLS-1$
		fontHandle = registry.getChildHandle(detailHandle, "axisFont"); //$NON-NLS-1$
		config.detailAxisLabelFont = ConfigUtils.defaultReadFont(fontHandle);
		config.detailAxisLabelColor = registry.getColor(registry.getChildHandle(fontHandle, "fontColor")); //$NON-NLS-1$

		config.detailPaintBorder = registry.getBoolean(registry.getChildHandle(detailHandle, "paintBorder")); //$NON-NLS-1$
		config.detailBorderColor = registry.getColor(registry.getChildHandle(detailHandle, "borderColor")); //$NON-NLS-1$
		config.detailPaintGrid = registry.getBoolean(registry.getChildHandle(detailHandle, "paintGrid")); //$NON-NLS-1$
		config.detailGridColor = registry.getColor(registry.getChildHandle(detailHandle, "gridColor")); //$NON-NLS-1$
		config.detailGridStyle = registry.getValue(registry.getChildHandle(detailHandle, "gridStyle"), PaIntEGraph.DEFAULT_GRID_STYLE); //$NON-NLS-1$

		// Refresh is required to allow the underlying document
		// to adjust its style definitions to the new font and color settings
		refresh();
	}

	private class Handler extends MouseAdapter {

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
				ProsodySentenceDetailPresenter.this.refresh();
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
//			if(sentencePatternSelect==null) {
//				String pattern = ConfigRegistry.getGlobalRegistry().getString(
//						configPath+".text.sentencePattern"); //$NON-NLS-1$
//				sentencePatternSelect = createPatternSelect(pattern);
//			}
//			if(headerPatternSelect==null) {
//				String pattern = ConfigRegistry.getGlobalRegistry().getString(
//						configPath+".text.headerPattern"); //$NON-NLS-1$
//				headerPatternSelect = createPatternSelect(pattern);
//			}
//			if(detailPatternSelect==null) {
//				String pattern = ConfigRegistry.getGlobalRegistry().getString(
//						configPath+".detail.detailPattern"); //$NON-NLS-1$
//				detailPatternSelect = createPatternSelect(pattern);
//			}
//
//			if(patternSelectInfo==null) {
//				final JLabel label = new JLabel();
//				label.addMouseListener(new TooltipFreezer());
//				label.setIcon(UIUtil.getInfoIcon());
//
//				Localizable localizable = new Localizable() {
//
//					@Override
//					public void localize() {
//						label.setToolTipText(createPatternSelectTooltip());
//					}
//				};
//
//				localizable.localize();
//				ResourceManager.getInstance().getGlobalDomain().addItem(localizable);
//
//				patternSelectInfo = label;
//			}
//
//			sentencePatternSelect.setSelectedItem(LabelPattern.escapePattern(config.sentencePattern.getPattern()));
//			headerPatternSelect.setSelectedItem(LabelPattern.escapePattern(config.headerPattern.getPattern()));
//			detailPatternSelect.setSelectedItem(LabelPattern.escapePattern(config.detailPattern.getPattern()));
//
//			FormBuilder formBuilder = FormBuilder.newLocalizingBuilder();
//			formBuilder.addEntry("info", new DummyFormEntry( //$NON-NLS-1$
//					"plugins.prosody.prosodyOutlinePresenter.dialogs.editPattern.info", patternSelectInfo)); //$NON-NLS-1$
//			formBuilder.addEntry("sentencePattern", new ChoiceFormEntry( //$NON-NLS-1$
//					"plugins.prosody.prosodyOutlinePresenter.dialogs.editPattern.sentencePattern", sentencePatternSelect)); //$NON-NLS-1$
//			formBuilder.addEntry("headerPattern", new ChoiceFormEntry( //$NON-NLS-1$
//					"plugins.prosody.prosodyOutlinePresenter.dialogs.editPattern.headerPattern", headerPatternSelect)); //$NON-NLS-1$
//			formBuilder.addEntry("detailPattern", new ChoiceFormEntry( //$NON-NLS-1$
//					"plugins.prosody.prosodyOutlinePresenter.dialogs.editPattern.detailPattern", detailPatternSelect)); //$NON-NLS-1$
//
//			formBuilder.buildForm();
//
//			if(DialogFactory.getGlobalFactory().showGenericDialog(
//					null,
//					DialogFactory.OK_CANCEL_OPTION,
//					"plugins.prosody.prosodyOutlinePresenter.dialogs.editPattern.title", //$NON-NLS-1$
//					"plugins.prosody.prosodyOutlinePresenter.dialogs.editPattern.message", //$NON-NLS-1$
//					formBuilder.getContainer(),
//					true)) {
//
//				// Sentence pattern
//				String sentencePattern = getPattern(sentencePatternSelect);
//				if(sentencePattern==null || sentencePattern.isEmpty()) {
//					// Ensure minimum label!
//					sentencePattern = "$form$"; //$NON-NLS-1$
//				}
//
//				try {
//					config.sentencePattern = new LabelPattern(LabelPattern.unescapePattern(sentencePattern));
//					addPattern(sentencePattern, sentencePatternSelect);
//				} catch(Exception ex) {
//					LoggerFactory.log(this, Level.SEVERE,
//							"Invalid node pattern: "+sentencePattern, ex); //$NON-NLS-1$
//
//					UIUtil.beep();
//					DialogFactory.getGlobalFactory().showError(null,
//							"plugins.prosody.prosodyOutlinePresenter.dialogs.invalidSentencePattern.title",  //$NON-NLS-1$
//							"plugins.prosody.prosodyOutlinePresenter.dialogs.invalidSentencePattern.message",  //$NON-NLS-1$
//							sentencePattern);
//
//					return;
//				}
//
//				// Header pattern
//				String headerPattern = getPattern(headerPatternSelect);
//				if(headerPattern==null) {
//					headerPattern = ""; //$NON-NLS-1$
//				}
//
//				try {
//					config.headerPattern = new LabelPattern(LabelPattern.unescapePattern(headerPattern));
//					addPattern(headerPattern, headerPatternSelect);
//				} catch(Exception ex) {
//					LoggerFactory.log(this, Level.SEVERE,
//							"Invalid edge pattern: "+headerPattern, ex); //$NON-NLS-1$
//
//					UIUtil.beep();
//					DialogFactory.getGlobalFactory().showError(null,
//							"plugins.prosody.prosodyOutlinePresenter.dialogs.invalidHeaderPattern.title",  //$NON-NLS-1$
//							"plugins.prosody.prosodyOutlinePresenter.dialogs.invalidHeaderPattern.message",  //$NON-NLS-1$
//							headerPattern);
//
//					return;
//				}
//
//				// Header pattern
//				String detailPattern = getPattern(detailPatternSelect);
//				if(detailPattern==null) {
//					detailPattern = ""; //$NON-NLS-1$
//				}
//
//				try {
//					config.detailPattern = new LabelPattern(LabelPattern.unescapePattern(detailPattern));
//					addPattern(detailPattern, detailPatternSelect);
//				} catch(Exception ex) {
//					LoggerFactory.log(this, Level.SEVERE,
//							"Invalid edge pattern: "+detailPattern, ex); //$NON-NLS-1$
//
//					UIUtil.beep();
//					DialogFactory.getGlobalFactory().showError(null,
//							"plugins.prosody.prosodyOutlinePresenter.dialogs.invalidDetailPattern.title",  //$NON-NLS-1$
//							"plugins.prosody.prosodyOutlinePresenter.dialogs.invalidDetailPattern.message",  //$NON-NLS-1$
//							detailPattern);
//
//					return;
//				}
//
//				ProsodySentenceDetailPresenter.this.refresh();
//			}
		}

		public void playDocument(ActionEvent e) {
			if(sentenceInfo==null) {
				return;
			}

			try {

				ProsodicSentenceData sentence = sentenceInfo.getSentence();

				SoundPlayer player = SoundPlayer.getInstance();
				SoundFile soundFile = player.getSoundFile(sentence);

				if(!soundFile.isOpen()) {
					player.open(soundFile);
				}

				float beginOffset = SoundOffsets.getBeginOffset(sentence);
				float endOffset = SoundOffsets.getEndOffset(sentence);

				soundFile.setStartOffset(beginOffset);
				soundFile.setEndOffset(endOffset);
				soundFile.setRepeating(false);

				player.start(soundFile);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to play current document", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void stopPlayback(ActionEvent e) {
			if(sentenceInfo==null) {
				return;
			}

			try {

				SoundPlayer player = SoundPlayer.getInstance();
				SoundFile soundFile = player.getSoundFile(sentenceInfo.getSentence());

				player.stop(soundFile);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to stop audio playback", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void pausePlayback(ActionEvent e) {
			if(sentenceInfo==null) {
				return;
			}

			try {

				SoundPlayer player = SoundPlayer.getInstance();
				SoundFile soundFile = player.getSoundFile(sentenceInfo.getSentence());

				if(soundFile.isActive()) {
					player.pause(soundFile);
				} else if(soundFile.isPaused()) {
					player.resume(soundFile);
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to pause audio playback", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void pausePlayback(boolean b) {
			// no-op
		}
	}

	private class SentencePanel extends JPanel implements Scrollable {

		private static final long serialVersionUID = 6607236927795690435L;

		public SentencePanel() {
			super(new WrapLayout(FlowLayout.LEFT, 10, 10));

			setBackground(UIManager.getColor("List.background"));
		}

		/**
		 * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
		 */
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		/**
		 * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
		 */
		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect,
				int orientation, int direction) {
			// TODO Auto-generated method stub
			return 0;
		}

		/**
		 * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
		 */
		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect,
				int orientation, int direction) {
			// TODO Auto-generated method stub
			return 0;
		}

		/**
		 * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
		 */
		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		/**
		 * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
		 */
		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}

	}

	private static final Border wordBorder = new LineBorder(Color.lightGray);

	private class WordPanel extends JComponent {

		private static final long serialVersionUID = 2807556483402002664L;

		private final WordInfo wordInfo;
		private final TextArea textArea;
		private final PaIntEGraph graph;
		private final PaIntEParams params;

		private Dimension preferredSize;

		public WordPanel(WordInfo wordInfo) {
			this.wordInfo = wordInfo;
			textArea = new TextArea();
			textArea.setInsets(new Insets(0, 2, 3, 2));
			graph = new PaIntEGraph();
			params = new PaIntEParams();
			setBackground(config.backgroundColor);
			setBorder(wordBorder);
		}

		public WordInfo getWordInfo() {
			return wordInfo;
		}

		public void rebuild() {

			setFont(config.detailFont);
			setForeground(config.detailTextColor);


			Axis.Integer xAxis = (Axis.Integer) graph.getXAxis();
			Axis.Integer yAxis = (Axis.Integer) graph.getYAxis();

			// Apply config

			graph.setBorderColor(config.detailBorderColor);
			graph.setGridColor(config.detailGridColor);
			graph.setGridStyle(config.detailGridStyle);
			graph.setPaintBorder(config.detailPaintBorder);
			graph.setPaintGrid(config.detailPaintGrid);

			xAxis.setAxisColor(config.detailAxisColor);
			xAxis.setLabelColor(config.detailAxisLabelColor);
			xAxis.setLabelFont(config.detailAxisLabelFont);
			xAxis.setMarkerColor(config.detailAxisMarkerColor);
			xAxis.setMarkerHeight(config.detailAxisMarkerHeight);
			xAxis.setMinValue(-config.syllableScope);
			xAxis.setMaxValue(config.syllableScope);

			yAxis.setAxisColor(config.detailAxisColor);
			yAxis.setLabelColor(config.detailAxisLabelColor);
			yAxis.setLabelFont(config.detailAxisLabelFont);
			yAxis.setMarkerColor(config.detailAxisMarkerColor);
			yAxis.setMarkerHeight(config.detailAxisMarkerHeight);

			LabelPattern pattern = config.sentencePattern;
			textArea.setFont(config.sentenceFont);


			WordInfo wordInfo = getWordInfo();

			Dimension size = new Dimension();

			if(wordInfo!=null) {
				FontMetrics fm = getFontMetrics(config.sentenceFont);

				ProsodicSentenceData sentence = wordInfo.getSentenceInfo().getSentence();

				Dimension areaSize = new Dimension();
				int width = 0;

				for(int sylIndex=0; sylIndex<wordInfo.sylCount(); sylIndex++) {
					// Honor graph spacing
					if(sylIndex>0) {
						width += config.graphSpacing;
					}

					SyllableInfo sylInfo = wordInfo.syllableInfo(sylIndex);

					// Compute text lines and save them
					String[] lines = pattern.getText(sentence, wordInfo.getWordIndex(), sylIndex);
					sylInfo.setProperty("lines", lines); //$NON-NLS-1$

					// Compute required space for text lines
					textArea.getSize(this, lines, areaSize);
					size.height = Math.max(size.height, areaSize.height);

					int sylWidth = Math.max(areaSize.width, config.graphWidth);
					sylWidth = Math.max(sylWidth, fm.stringWidth(sylInfo.getLabel()));

//					System.out.printf("form=%s x=%d width=%d sw=%d\n",
//							lines[0], width, areaSize.width, fm.stringWidth(lines[0]));

					// Save syl bounds
					sylInfo.setX(width);
					sylInfo.setWidth(sylWidth);

					width += sylWidth;
				}

				size.width = width;

				size.height += config.graphHeight + 3*config.graphSpacing + fm.getHeight();
			}

			wordInfo.setWidth(size.width);

			preferredSize = size;
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);

			WordInfo wordInfo = getWordInfo();

			if(wordInfo==null) {
				return;
			}

			ProsodicSentenceData sentence = wordInfo.getSentenceInfo().getSentence();

			Graphics2D g = (Graphics2D) graphics;

			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			FontMetrics fm = getFontMetrics(config.sentenceFont);
			Color bg = getBackground();

			textArea.setFont(config.sentenceFont);
			textArea.setTextColor(config.sentenceTextColor);

			int h = getHeight();
			int w = getWidth();
			int yOffset = config.graphHeight + 3*config.graphSpacing + fm.getHeight();

			Rectangle area = new Rectangle();

			area.height = h;

			for(int sylIndex=0; sylIndex<wordInfo.sylCount(); sylIndex++) {
				SyllableInfo sylInfo = wordInfo.syllableInfo(sylIndex);

				area.x = sylInfo.getX();
				area.width = sylInfo.getWidth();

				// Paint text
				area.y = yOffset;
				area.height = h-yOffset;

				String[] lines = (String[]) sylInfo.getProperty("lines"); //$NON-NLS-1$

				textArea.paint(g, lines, area);

				// Paint graph

				if(area.width>config.graphWidth) {
					area.x = (area.width/2) - (config.graphWidth/2);
				}
				area.height = config.graphHeight;
				area.y = fm.getHeight()+config.graphSpacing;

				params.setParams(sentence, wordInfo.getWordIndex(), sylIndex);

				graph.paint(g, params, area);

				String sylLabel = sylInfo.getLabel();

				// Syllable Label
				if(sylLabel!=null) {
					int sw = fm.stringWidth(sylLabel);
					int x = area.x + area.width/2 - sw/2;
					int y = area.y + fm.getHeight();

					Color c = g.getColor();

					if(config.clearLabelBackground) {
						g.setColor(bg);
						g.fillRect(x-1, y-fm.getAscent(), sw+2, fm.getHeight());
					}

					g.setFont(config.sentenceFont);
					g.setColor(config.sentenceTextColor);
					g.drawString(sylLabel, x, y);

					g.setColor(c);
				}
			}

			String wordLabel = wordInfo.getLabel();

			if(wordLabel!=null) {
				int sw = fm.stringWidth(wordLabel);
				int x = w/2 - sw/2;
				int y = fm.getHeight();

				Color c = g.getColor();

				if(config.clearLabelBackground) {
					g.setColor(bg);
					g.fillRect(x-1, y-fm.getAscent(), sw+2, fm.getHeight());
				}

				g.setFont(config.sentenceFont);
				g.setColor(config.sentenceTextColor);
				g.drawString(wordLabel, x, y);

				g.setColor(c);
			}
		}

		/**
		 * @see javax.swing.JComponent#getPreferredSize()
		 */
		@Override
		public Dimension getPreferredSize() {
			if(preferredSize==null) {
				rebuild();
			}
			return preferredSize;
		}

		/**
		 * @see javax.swing.JComponent#getMinimumSize()
		 */
		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}
	}
}
