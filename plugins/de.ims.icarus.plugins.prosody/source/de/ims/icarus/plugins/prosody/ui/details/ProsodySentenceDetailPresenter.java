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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.MutableComboBoxModel;
import javax.swing.Scrollable;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.config.ConfigUtils;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.coref.view.PatternExample;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.annotation.AnnotatedProsodicSentenceData;
import de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotation;
import de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotationManager;
import de.ims.icarus.plugins.prosody.annotation.ProsodyHighlighting;
import de.ims.icarus.plugins.prosody.painte.PaIntEParams;
import de.ims.icarus.plugins.prosody.pattern.LabelPattern;
import de.ims.icarus.plugins.prosody.sound.SoundException;
import de.ims.icarus.plugins.prosody.sound.SoundOffsets;
import de.ims.icarus.plugins.prosody.sound.SoundPlayer;
import de.ims.icarus.plugins.prosody.sound.SoundPlayer.SoundFile;
import de.ims.icarus.plugins.prosody.ui.TextArea;
import de.ims.icarus.plugins.prosody.ui.geom.Axis;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEHitBox;
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
import de.ims.icarus.util.Installable;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.annotation.AnnotationController;
import de.ims.icarus.util.annotation.AnnotationManager;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodySentenceDetailPresenter implements AWTPresenter.TableBasedPresenter, Installable {

	public static final String DEFAULT_SENTENCE_PATTERN =
			  "#syllable_label#\n#syllable_duration#\n" //$NON-NLS-1$
			+ "#painte_a1#,#painte_a2#,#painte_b#\n" //$NON-NLS-1$
			+ "#painte_c1#,#painte_c2#,#painte_d#"; //$NON-NLS-1$

	private SentenceInfo sentenceInfo;

	private PanelConfig config = new PanelConfig();

	private Options options;
	private JPanel contentPanel;
	private SentencePanel sentencePanel;

	protected AnnotationController annotationSource;

	private CallbackHandler callbackHandler;
	private Handler handler;

	private JPopupMenu popupMenu;

	protected ActionManager actionManager;

	protected static final String configPath = "plugins.prosody.appearance.details"; //$NON-NLS-1$

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
		config.sentencePattern = new LabelPattern(DEFAULT_SENTENCE_PATTERN);
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

		actionManager.addHandler("plugins.prosody.prosodySentenceDetailPresenter.openPreferencesAction", //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.prosodySentenceDetailPresenter.refreshAction", //$NON-NLS-1$
				callbackHandler, "refresh"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.prosodySentenceDetailPresenter.editLabelPatternsAction", //$NON-NLS-1$
				callbackHandler, "editLabelPatterns"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.prosodySentenceDetailPresenter.playSentenceAction", //$NON-NLS-1$
				callbackHandler, "playSentence"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.prosodySentenceDetailPresenter.stopPlaybackAction", //$NON-NLS-1$
				callbackHandler, "stopPlayback"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.prosodySentenceDetailPresenter.pausePlaybackAction", //$NON-NLS-1$
				callbackHandler, "pausePlayback"); //$NON-NLS-1$
	}

	private SoundFile getSoundFile() {
		SoundFile soundFile = null;

		if(sentenceInfo!=null) {
			ProsodicSentenceData sentence = sentenceInfo.getSentence();

			try {
				soundFile = SoundPlayer.getInstance().getSoundFile(sentence);
			} catch (SoundException e) {
				LoggerFactory.warning(this, "Unable to fetch sound file for document: "+sentence.getDocument().getId(), e); //$NON-NLS-1$
			}
		}

		return soundFile;
	}

	protected void refreshActions() {
		ActionManager actionManager = getActionManager();


		boolean hasData = sentenceInfo!=null;
		SoundFile soundFile = getSoundFile();
		boolean isPlaying = soundFile!=null && soundFile.isActive();
		boolean isPaused = soundFile!=null && soundFile.isPaused();

		actionManager.setEnabled(hasData,
				"plugins.prosody.prosodySentenceDetailPresenter.playSentenceAction"); //$NON-NLS-1$
		actionManager.setEnabled(isPlaying || isPaused,
				"plugins.prosody.prosodySentenceDetailPresenter.stopPlaybackAction"); //$NON-NLS-1$
		actionManager.setEnabled(isPlaying || isPaused,
				"plugins.prosody.prosodySentenceDetailPresenter.pausePlaybackAction"); //$NON-NLS-1$

		actionManager.setSelected(isPaused,
				"plugins.prosody.prosodySentenceDetailPresenter.pausePlaybackAction"); //$NON-NLS-1$
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
		SoundFile oldSoundFile = getSoundFile();
		if(oldSoundFile!=null) {
			oldSoundFile.removeChangeListener(getHandler());
		}

		if(sentence!=null) {
			sentenceInfo = new SentenceInfo(sentence);
		} else {
			sentenceInfo = null;
		}

		SoundFile newSoundFile = getSoundFile();
		if(newSoundFile!=null) {
			newSoundFile.addChangeListener(getHandler());
		}
	}

	private static final String COL_KEY = "color"; //$NON-NLS-1$
	private static final String GROUP_KEY = "group"; //$NON-NLS-1$

	public void refresh() {
		if(contentPanel==null) {
			return;
		}

		sentencePanel.setBackground(config.backgroundColor);

		sentencePanel.removeAll();

		if(sentenceInfo!=null) {

	    	ProsodicAnnotationManager manager = (ProsodicAnnotationManager) getAnnotationManager();
	    	ProsodicAnnotation annotation = getAnnotation();
	    	manager.setAnnotation(annotation);

			final boolean hasHighlight = manager.hasAnnotation();

			for(int wordIndex=0; wordIndex<sentenceInfo.wordCount(); wordIndex++) {
				WordInfo wordInfo = sentenceInfo.wordInfo(wordIndex);

				boolean wordHighlighted = false;

				if(hasHighlight) {
					long highlight = manager.getHighlight(wordIndex);

					wordHighlighted = ProsodyHighlighting.getInstance().isHighlighted(highlight);

					if(wordHighlighted) {
						Color col = ProsodyHighlighting.getInstance().getGroupColor(highlight);
						if(col!=null) {
							wordInfo.setProperty(GROUP_KEY, true);
						} else {
							col = ProsodyHighlighting.getInstance().getHighlightColor(highlight);
						}

						wordInfo.setProperty(COL_KEY, col);
					}
				}

				if(wordHighlighted) {
					for(int sylIndex=0; sylIndex<wordInfo.sylCount(); sylIndex++) {
						SyllableInfo sylInfo = wordInfo.syllableInfo(sylIndex);

						long highlight = annotation.getHighlight(wordIndex, sylIndex);
						if(ProsodyHighlighting.getInstance().isHighlighted(highlight)) {
							Color col = ProsodyHighlighting.getInstance().getGroupColor(highlight);
							if(col!=null) {
								sylInfo.setProperty(GROUP_KEY, true);
							} else {
								col = ProsodyHighlighting.getInstance().getHighlightColor(highlight);
							}

							sylInfo.setProperty(COL_KEY, col);
						}
					}
				}

				WordPanel wordPanel = new WordPanel(wordInfo);
				wordPanel.rebuild();
				wordPanel.addMouseListener(getHandler());
				wordPanel.addMouseMotionListener(getHandler());
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
	 * @see de.ims.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		if(target instanceof AnnotationController) {
			if(this.annotationSource!=null && this.annotationSource!=target)
				throw new IllegalStateException("Cannot be assigned to multiple annotation controllers"); //$NON-NLS-1$

			this.annotationSource = (AnnotationController)target;
		} else {
			this.annotationSource = null;
		}
	}

	/**
	 * @see de.ims.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		this.annotationSource = null;
	}

	protected AnnotationManager getAnnotationManager() {
		return annotationSource==null ? null : annotationSource.getAnnotationManager();
	}

	protected ProsodicAnnotation getAnnotation() {
		return (sentenceInfo!=null && sentenceInfo.getSentence() instanceof AnnotatedProsodicSentenceData) ?
				((AnnotatedProsodicSentenceData)sentenceInfo.getSentence()).getAnnotation() : null;
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
		ConfigRegistry.getGlobalRegistry().removeGroupListener(configPath, getHandler());
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

	private void play(float beginOffset, float endOffset) {

		if(sentenceInfo==null)
			throw new IllegalStateException("Cannot play anything without a specified sentence..."); //$NON-NLS-1$

		if(beginOffset<0 || endOffset<0) {
			LoggerFactory.warning(this, String.format(
					"Cannot play part of sentence - at least one offset is undefined or invalid: [%.02f , %.02f]", beginOffset, endOffset)); //$NON-NLS-1$
			return;
		}

		try {
			ProsodicSentenceData sentence = sentenceInfo.getSentence();

			SoundPlayer player = SoundPlayer.getInstance();
			SoundFile soundFile = player.getSoundFile(sentence);

			if(!soundFile.isOpen()) {
				player.open(soundFile);
			}

			soundFile.setStartOffset(beginOffset);
			soundFile.setEndOffset(endOffset);
			soundFile.setRepeating(config.loopSound);

			player.start(soundFile);

		} catch (SoundException e) {
			LoggerFactory.error(this, "Failed to play sound for part of sentence '"+sentenceInfo.getLabel()+"'", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void playSentence() {
		ProsodicSentenceData sentence = sentenceInfo.getSentence();
		float beginOffset = SoundOffsets.getBeginOffset(sentence);
		float endOffset = SoundOffsets.getEndOffset(sentence);

		play(beginOffset, endOffset);
	}

	private void playWord(int wordIndex) {
		ProsodicSentenceData sentence = sentenceInfo.getSentence();
		float beginOffset = SoundOffsets.getBeginOffset(sentence, wordIndex);
		float endOffset = SoundOffsets.getEndOffset(sentence, wordIndex);

//		System.out.printf("begin=%.03f end=%.03f\n", beginOffset, endOffset); //$NON-NLS-1$

		play(beginOffset, endOffset);
	}

	private void playSyllable(int wordIndex, int sylIndex) {
		ProsodicSentenceData sentence = sentenceInfo.getSentence();
		float beginOffset = SoundOffsets.getBeginOffset(sentence, wordIndex, sylIndex);
		float endOffset = SoundOffsets.getEndOffset(sentence, wordIndex, sylIndex);

//		System.out.printf("begin=%.03f end=%.03f\n", beginOffset, endOffset); //$NON-NLS-1$

		play(beginOffset, endOffset);
	}

	private void showPopup(MouseEvent trigger) {
		if(popupMenu==null) {
			// Create new popup menu

			Options options = null;
			popupMenu = getActionManager().createPopupMenu(
					"plugins.prosody.prosodySentenceDetailPresenter.popupMenuList", options); //$NON-NLS-1$

			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}

		if(popupMenu!=null) {
			refreshActions();

			popupMenu.show((Component) trigger.getSource(), trigger.getX(), trigger.getY());
		}
	}

	private class Handler extends MouseAdapter implements ChangeListener, ConfigListener {

		private Cursor cursor;

		private void maybeShowPopup(MouseEvent e) {
			if(e.isPopupTrigger()) {
				//TODO
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.isPopupTrigger()) {
				// Already handled by pressed/release callbacks
				return;
			}

			if(e.getSource() instanceof WordPanel) {
				WordPanel panel = (WordPanel) e.getSource();
				PaIntEHitBox hitBox = panel.translate(e.getPoint());
				if(hitBox==null) {
					return;
				}

				switch (hitBox.getType()) {
				case SYL_LABEL:
					playSyllable(hitBox.getWordIndex(), hitBox.getSylIndex());
					break;
				case WORD_LABEL:
					playWord(hitBox.getWordIndex());
					break;

				default:
					break;
				}
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

		/**
		 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseMoved(MouseEvent e) {
			if(e.getSource() instanceof WordPanel) {
				WordPanel panel = (WordPanel) e.getSource();
				refreshWordPanel(panel, e.getPoint());
			}
		}

		private final DecimalFormat decimalFormat = new DecimalFormat("#,###,###,##0.00"); //$NON-NLS-1$
		private final String defaultTooltipFormat =
				"<html>" //$NON-NLS-1$
				+ "A<sub>1</sub>:&nbsp;%.02f<br>" //$NON-NLS-1$
				+ "A<sub>2</sub>:&nbsp;%.02f<br>" //$NON-NLS-1$
				+ "B<sub>&nbsp;</sub>:&nbsp;%.02f<br>" //$NON-NLS-1$
				+ "C<sub>1</sub>:&nbsp;%.02f<br>" //$NON-NLS-1$
				+ "C<sub>2</sub>:&nbsp;%.02f<br>" //$NON-NLS-1$
				+ "D<sub>&nbsp;</sub>:&nbsp;%.02f"; //$NON-NLS-1$

		private void refreshWordPanel(WordPanel panel, Point p) {
			PaIntEHitBox hitBox = panel.translate(p);

			String tooltip = null;
			Cursor cursor = this.cursor;

			if(hitBox!=null) {
				switch (hitBox.getType()) {
				case AXIS:
					tooltip = decimalFormat.format(hitBox.getAxisValue());
					break;

				case CURVE:
					tooltip = decimalFormat.format(hitBox.getX())+'/'+decimalFormat.format(hitBox.getY());
					break;

				case WORD_LABEL:
				case SYL_LABEL:
//					cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
					cursor = ProsodyUtils.getSpeakerCursor();
					break;

				case GRAPH:
					PaIntEParams params = hitBox.getParams();
					tooltip = String.format(Locale.ENGLISH, defaultTooltipFormat,
							params.getA1(), params.getA2(), params.getB(),
							params.getC1(), params.getC2(), params.getD());
					break;

				default:
					break;
				}
			}

			panel.setCursor(cursor);
			panel.setToolTipText(tooltip);
//			detailPanel.repaint();
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
			if(e.getSource() instanceof WordPanel) {
				WordPanel panel = (WordPanel) e.getSource();
				cursor = panel.getCursor();
				refreshWordPanel(panel, e.getPoint());
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseExited(MouseEvent e) {
			if(e.getSource() instanceof WordPanel) {
				WordPanel panel = (WordPanel) e.getSource();
				panel.setCursor(cursor);
				cursor = null;
			}
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			refreshActions();
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
//					"plugins.prosody.prosodySentenceDetailPresenter.dialogs.editPattern.info", patternSelectInfo)); //$NON-NLS-1$
//			formBuilder.addEntry("sentencePattern", new ChoiceFormEntry( //$NON-NLS-1$
//					"plugins.prosody.prosodySentenceDetailPresenter.dialogs.editPattern.sentencePattern", sentencePatternSelect)); //$NON-NLS-1$
//			formBuilder.addEntry("headerPattern", new ChoiceFormEntry( //$NON-NLS-1$
//					"plugins.prosody.prosodySentenceDetailPresenter.dialogs.editPattern.headerPattern", headerPatternSelect)); //$NON-NLS-1$
//			formBuilder.addEntry("detailPattern", new ChoiceFormEntry( //$NON-NLS-1$
//					"plugins.prosody.prosodySentenceDetailPresenter.dialogs.editPattern.detailPattern", detailPatternSelect)); //$NON-NLS-1$
//
//			formBuilder.buildForm();
//
//			if(DialogFactory.getGlobalFactory().showGenericDialog(
//					null,
//					DialogFactory.OK_CANCEL_OPTION,
//					"plugins.prosody.prosodySentenceDetailPresenter.dialogs.editPattern.title", //$NON-NLS-1$
//					"plugins.prosody.prosodySentenceDetailPresenter.dialogs.editPattern.message", //$NON-NLS-1$
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
//							"plugins.prosody.prosodySentenceDetailPresenter.dialogs.invalidSentencePattern.title",  //$NON-NLS-1$
//							"plugins.prosody.prosodySentenceDetailPresenter.dialogs.invalidSentencePattern.message",  //$NON-NLS-1$
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
//							"plugins.prosody.prosodySentenceDetailPresenter.dialogs.invalidHeaderPattern.title",  //$NON-NLS-1$
//							"plugins.prosody.prosodySentenceDetailPresenter.dialogs.invalidHeaderPattern.message",  //$NON-NLS-1$
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
//							"plugins.prosody.prosodySentenceDetailPresenter.dialogs.invalidDetailPattern.title",  //$NON-NLS-1$
//							"plugins.prosody.prosodySentenceDetailPresenter.dialogs.invalidDetailPattern.message",  //$NON-NLS-1$
//							detailPattern);
//
//					return;
//				}
//
//				ProsodySentenceDetailPresenter.this.refresh();
//			}
		}

		public void playSentence(ActionEvent e) {
			if(sentenceInfo==null) {
				return;
			}

			try {

				ProsodySentenceDetailPresenter.this.playSentence();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to play current sentence", ex); //$NON-NLS-1$

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

			setBackground(UIManager.getColor("List.background")); //$NON-NLS-1$
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

	private static class WordBorder extends LineBorder {

		private static final Color DEFAULT_COLOR = Color.lightGray;

		/**
		 * @param color
		 */
		public WordBorder() {
			super(DEFAULT_COLOR);
		}

		public void setLineColor(Color color) {
			if (color == null) {
				color = DEFAULT_COLOR;
			}

			lineColor = color;
		}
	}

	private class WordPanel extends JComponent {

		private static final long serialVersionUID = 2807556483402002664L;

		private final WordInfo wordInfo;
		private final TextArea textArea;
		private final PaIntEGraph graph;
		private final PaIntEParams params;

		private double translationAccuracy = 0.05;

		private Dimension preferredSize;

		private final WordBorder border = new WordBorder();

		public WordPanel(WordInfo wordInfo) {
			this.wordInfo = wordInfo;
			textArea = new TextArea();
			textArea.setInsets(new Insets(0, 2, 3, 2));
			graph = new PaIntEGraph();
			params = new PaIntEParams();
			setBackground(config.backgroundColor);
			setBorder(border);
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
			graph.getCurve().setAntiAliasingType(config.antiAliasingType);

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

			WordInfo wordInfo = getWordInfo();

			yAxis.setMinValue(Math.min(50, (int)wordInfo.getSentenceInfo().getMinD()));
			yAxis.setMaxValue(Math.max(200, (int)wordInfo.getSentenceInfo().getMaxD()));

			LabelPattern pattern = config.sentencePattern;
			textArea.setFont(config.sentenceFont);

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

					int sylWidth = Math.max(areaSize.width, config.graphWidth);
					sylWidth = Math.max(sylWidth, fm.stringWidth(labelForSyllable(sylInfo)));

//					System.out.printf("form=%s x=%d width=%d sw=%d\n",
//							lines[0], width, areaSize.width, fm.stringWidth(lines[0]));

					// Save syl bounds
					sylInfo.setX(width);
					sylInfo.setWidth(sylWidth);

					width += sylWidth;
				}

				width = Math.max(width, fm.stringWidth(labelForWord(wordInfo)));

				width = Math.max(width, 35);

				size.width = width;

				size.height += fm.getHeight()*pattern.getElementCount()
						+ textArea.getTopInsets() +textArea.getBottomInsets()
						+ config.graphHeight + 3*config.graphSpacing + fm.getHeight();
			}

			border.setLineColor((Color) wordInfo.getProperty(COL_KEY));

			wordInfo.setWidth(size.width);

			preferredSize = size;
		}

		private String labelForSyllable(SyllableInfo sylInfo) {
			return sylInfo.getLabel();
		}

		private String labelForWord(WordInfo wordInfo) {
			return wordInfo.getLabel();
		}

		public PaIntEHitBox translate(Point p) {

			WordInfo wordInfo = getWordInfo();

			if(wordInfo==null || wordInfo.sylCount()==0) {
				return null;
			}

			Rectangle bounds = new Rectangle(getSize());

			if(bounds==null || (p.y>=bounds.y && !bounds.contains(p))) {
				return null;
			}

			int x = p.x;
			int y = p.y;

			int wordIndex = wordInfo.getWordIndex();

			Graphics2D g = (Graphics2D) getGraphics();
			FontMetrics fm = g.getFontMetrics();

			Rectangle area = new Rectangle();
			area.y = fm.getHeight() + config.graphSpacing;

			// Iterate over syllables of word
			for(int sylIndex=0; sylIndex<wordInfo.sylCount(); sylIndex++) {
				SyllableInfo syInfo = wordInfo.syllableInfo(sylIndex);

				area.x = syInfo.getX();
				area.width = syInfo.getWidth();
				area.height = config.graphHeight;

				if(sylIndex>0) {
					area.x += config.graphSpacing;
				}

				if(area.contains(x, y)) {

					SyllableInfo sylInfo = wordInfo.syllableInfo(sylIndex);

					String sylLabel = labelForSyllable(sylInfo);

					// Label
					if(sylLabel!=null) {
						int sw = fm.stringWidth(sylLabel);
						int sx = area.x + area.width/2 - sw/2;
						int sy = area.y + fm.getHeight();

						if(y<sy && x>=sx && x<=sx+sw) {
							return new PaIntEHitBox(wordIndex, sylIndex);
						}
					}

					params.setParams(wordInfo.getSentenceInfo().getSentence(), wordIndex, sylIndex);

					y -= area.y;
					x -= area.x;

					return graph.translate(x, y, g, area, params, translationAccuracy);
				}

				area.x += area.width;
			}

			String wordLabel = labelForWord(wordInfo);

			if(wordLabel!=null) {
				int sw = fm.stringWidth(wordLabel);
				int sx = bounds.width/2 - sw/2;
				int sy = fm.getHeight();

//					System.out.printf("x=%d y=%d sw=%d sx=%d sy=%d area=%s\n",
//							x, y, sw, sx, sy, area);

				if(y<=sy && x>=sx && x<=sx+sw) {
					return new PaIntEHitBox(wordIndex);
				}
			}

			return null;
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
					area.x += (area.width-config.graphWidth)/2;
				}
				area.height = config.graphHeight;
				area.y = fm.getHeight()+config.graphSpacing;

				params.setParams(sentence, wordInfo.getWordIndex(), sylIndex);

				Color curveColor = (Color)sylInfo.getProperty(COL_KEY);
				if(curveColor==null) {
					curveColor = config.curveColor;
				}

				graph.getCurve().setColor(curveColor);
				graph.paint(g, params, area);

				String sylLabel = labelForSyllable(sylInfo);

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

					Color fg = (Color)sylInfo.getProperty(COL_KEY);
					if(fg==null) {
						fg = config.sentenceTextColor;
					}

					g.setFont(config.sentenceFont);
					g.setColor(fg);
					g.drawString(sylLabel, x, y);

					g.setColor(c);
				}
			}

			String wordLabel = labelForWord(wordInfo);

			if(wordLabel!=null) {
				int sw = fm.stringWidth(wordLabel);
				int x = w/2 - sw/2;
				int y = fm.getHeight();

				Color c = g.getColor();

				if(config.clearLabelBackground) {
					g.setColor(bg);
					g.fillRect(x-1, y-fm.getAscent(), sw+2, fm.getHeight());
				}

				Color fg = (Color)wordInfo.getProperty(COL_KEY);
				if(fg==null) {
					fg = config.sentenceTextColor;
				}

				g.setFont(config.sentenceFont);
				g.setColor(fg);
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
