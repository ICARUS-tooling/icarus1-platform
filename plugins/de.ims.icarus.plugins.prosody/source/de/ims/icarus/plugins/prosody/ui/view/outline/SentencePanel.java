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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.text.DecimalFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.params.PaIntEParams;
import de.ims.icarus.plugins.prosody.pattern.LabelPattern;
import de.ims.icarus.plugins.prosody.sound.SoundException;
import de.ims.icarus.plugins.prosody.sound.SoundOffsets;
import de.ims.icarus.plugins.prosody.sound.SoundPlayer;
import de.ims.icarus.plugins.prosody.sound.SoundPlayer.SoundFile;
import de.ims.icarus.plugins.prosody.ui.TextArea;
import de.ims.icarus.plugins.prosody.ui.TextComponent;
import de.ims.icarus.plugins.prosody.ui.geom.AntiAliasingType;
import de.ims.icarus.plugins.prosody.ui.geom.Axis;
import de.ims.icarus.plugins.prosody.ui.geom.GridStyle;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEHitBox;
import de.ims.icarus.plugins.prosody.ui.view.PreviewSize;
import de.ims.icarus.plugins.prosody.ui.view.SentenceInfo;
import de.ims.icarus.plugins.prosody.ui.view.SyllableInfo;
import de.ims.icarus.plugins.prosody.ui.view.WordInfo;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.TooltipFreezer;
import de.ims.icarus.ui.UIUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SentencePanel extends JPanel{

	private static final long serialVersionUID = -8903948769330660811L;

//	private static LRUCache<ProsodicSentenceData, BufferedImage> curveCache = new LRUCache<>(100);

	private SentenceInfo sentenceInfo;

//	private JToggleButton toggleExpandButton;
	private JToggleButton toggleDetailsButton;
	private JButton playSentenceButton;

	private WordCursorModel wordCursorModel;
	private JSlider wordCursor;

	private Handler handler;

	private DetailPanel detailPanel;
	private TextPanel textPanel;
	private CurvePanel previewPanel;
	private TextComponent headerLabel;
	private TextComponent detailHeaderLabel;

	private int currentWord = -1;
	private PaIntEGraph graph = new PaIntEGraph();
	private PaIntEParams params = new PaIntEParams();

	private final PanelConfig config;

	private double translationAccuracy = 0.05;

	private static final Border collapsedBorder = BorderFactory.createEmptyBorder(1, 2, 1, 2);
	private static final Border expandedBorder = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.lightGray),
			BorderFactory.createEmptyBorder(0, 1, 0, 1));

	public SentencePanel(PanelConfig config) {
		if (config == null)
			throw new NullPointerException("Invalid config"); //$NON-NLS-1$

		this.config = config;

		handler = new Handler();

		IconRegistry iconRegistry = IconRegistry.getGlobalRegistry();
		FormLayout layout = new FormLayout(
				"left:pref, left:pref, 2dlu, pref, 2dlu, fill:pref, fill:pref:grow", //$NON-NLS-1$
				"top:pref, top:pref, top:pref, top:pref"); //$NON-NLS-1$

		setLayout(layout);

		setBackground(Color.white);

		// Toggle button for details panel
		toggleDetailsButton = new JToggleButton();
		toggleDetailsButton.setIcon(iconRegistry.getIcon("zoomin.gif")); //$NON-NLS-1$
		toggleDetailsButton.setSelectedIcon(iconRegistry.getIcon("zoomout.gif")); //$NON-NLS-1$
		toggleDetailsButton.setFocusable(false);
		toggleDetailsButton.setFocusPainted(false);
		toggleDetailsButton.setBorderPainted(false);
		toggleDetailsButton.addActionListener(handler);
		UIUtil.resizeComponent(toggleDetailsButton, 16, 16);
		add(toggleDetailsButton, CC.rc(3, 1));

//		// Toggle button for general visibility
//		toggleExpandButton = new JToggleButton();
//		toggleExpandButton.setSelected(true);
//		toggleExpandButton.setIcon(iconRegistry.getIcon("collapsed.gif")); //$NON-NLS-1$
//		toggleExpandButton.setSelectedIcon(iconRegistry.getIcon("expanded.gif")); //$NON-NLS-1$
//		toggleExpandButton.setFocusable(false);
//		toggleExpandButton.setFocusPainted(false);
//		toggleExpandButton.setBorderPainted(false);
//		toggleExpandButton.addActionListener(this);
//		UIUtil.resizeComponent(toggleExpandButton, 16, 16);
//		add(toggleExpandButton, CC.rc(4, 1));

		// Sentence panel
		textPanel = new TextPanel();
		textPanel.setOpaque(false);
		add(textPanel, CC.rc(4, 6));

		// Preview panel
		previewPanel = new CurvePanel();
		previewPanel.setOpaque(false);
		add(previewPanel, CC.rc(3, 6));

		// Header label (to the left)
		headerLabel = new TextComponent();
		headerLabel.getTextArea().setHorizontalAlignment(SwingConstants.RIGHT);
		headerLabel.setOpaque(false);
		add(headerLabel, CC.rchw(4, 1, 1, 4));

		// Detail Header label (to the left)
		detailHeaderLabel = new TextComponent();
		detailHeaderLabel.getTextArea().setHorizontalAlignment(SwingConstants.RIGHT);
		detailHeaderLabel.setOpaque(false);
		add(detailHeaderLabel, CC.rc(1, 4));

		// Cursor
		wordCursorModel = new WordCursorModel();
		wordCursor = new JSlider(wordCursorModel);
		wordCursor.setFocusable(false);
		wordCursor.addChangeListener(handler);
		wordCursor.addMouseWheelListener(handler);
		wordCursor.addMouseListener(handler);
		wordCursor.setOpaque(false);
		add(wordCursor, CC.rc(2, 6));

		// Button for playing entire sentence
		playSentenceButton = new JButton();
		playSentenceButton.setIcon(iconRegistry.getIcon("speaker.png")); //$NON-NLS-1$
		playSentenceButton.setFocusable(false);
		playSentenceButton.setFocusPainted(false);
		playSentenceButton.setBorderPainted(false);
		playSentenceButton.addActionListener(handler);
		UIUtil.resizeComponent(playSentenceButton, 16, 16);
		add(playSentenceButton, CC.rc(3, 2));

		// Upper details area
		detailPanel = new DetailPanel();
		detailPanel.setFocusable(false);
		detailPanel.setOpaque(false);
		detailPanel.addMouseListener(new TooltipFreezer());
		detailPanel.addMouseListener(handler);
		detailPanel.addMouseMotionListener(handler);
		add(detailPanel, CC.rchw(1, 6, 1, 2));

		toggleDetails();
	}

	public void clear() {
		sentenceInfo = null;
		currentWord = -1;

		refresh();
	}

	public void refresh(ProsodicSentenceData data) {
		if (data == null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$

		sentenceInfo = new SentenceInfo(data);

		refresh();
	}

	public SentenceInfo getSentenceInfo() {
		return sentenceInfo;
	}

	private void refresh() {

		if(sentenceInfo==null) {
			detailHeaderLabel.setLines(null);
			headerLabel.setLines(null);
			textPanel.clear();

//			toggleExpandButton.setSelected(false);
			toggleDetailsButton.setSelected(false);

		} else {
			ProsodicSentenceData sentence = sentenceInfo.getSentence();

			// Text area
			textPanel.rebuild();

			// Header section
			headerLabel.setLines(config.headerPattern.getText(sentence));

			// Refresh graph internals
			Axis.Integer yAxis = (Axis.Integer)graph.getYAxis();
			double stepSize = yAxis.getMarkerStepSize();
			float maxD = sentenceInfo.getMaxD();
			float minD = sentenceInfo.getMinD();
			int yMax = (int) (Math.ceil(maxD/stepSize) * stepSize);
			int yMin = (int) (Math.floor(minD/stepSize) * stepSize);
			//TODO maybe we should iterate over all sentences to get the "real" outer boundaries?
			yAxis.setMinValue(yMin);
			yAxis.setMaxValue(yMax);

			// Synchronize cursor
//			wordCursor.setMaximum(sentenceInfo.getImage().getWidth());
			wordCursorModel.refresh();

			refreshDetails();
		}

		revalidate();
		repaint();
	}

//	private void toggleExpand() {
//		boolean expanded = toggleExpandButton.isSelected();
//
//		curveLabel.setVisible(expanded);
//		sen
//	}

	private void toggleDetails() {
		boolean showDetails = toggleDetailsButton.isSelected();

		wordCursor.setVisible(showDetails);
		detailPanel.setVisible(showDetails);
		detailHeaderLabel.setVisible(showDetails);

		setBorder(showDetails ? expandedBorder : collapsedBorder);

		if(showDetails) {
			add(toggleDetailsButton, CC.rc(1, 1));
		} else {
			add(toggleDetailsButton, CC.rc(3, 1));
		}

		refreshDetails();
	}

	private void refreshDetails() {

		if(wordCursorModel.getValueIsAdjusting() || sentenceInfo==null) {
			return;
		}

		ProsodicSentenceData sentence = sentenceInfo.getSentence();

		detailPanel.rebuild();

		currentWord = wordCursorModel.getWordIndex();
		detailHeaderLabel.setLines(config.detailPattern.getText(sentence, currentWord));

//		System.out.printf("offset=%d syl=%d\n", offset, currentSyl); //$NON-NLS-1$

		revalidate();
		repaint();
	}

	private void play(float beginOffset, float endOffset) {

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
			LoggerFactory.error(this, "Failed to play sound for part of sentence '"+sentenceInfo.getText()+"'", e); //$NON-NLS-1$ //$NON-NLS-2$
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

		play(beginOffset, endOffset);
	}

	private void playSyllable(int wordIndex, int sylIndex) {
		ProsodicSentenceData sentence = sentenceInfo.getSentence();
		float beginOffset = SoundOffsets.getBeginOffset(sentence, wordIndex, sylIndex);
		float endOffset = SoundOffsets.getEndOffset(sentence, wordIndex, sylIndex);

		play(beginOffset, endOffset);
	}

	private class Handler extends MouseAdapter implements ActionListener, ChangeListener {

		private Cursor cursor;


		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			if(sentenceInfo==null || sentenceInfo.sylCount()==0) {
				return;
			}

			refreshDetails();
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==toggleDetailsButton) {
				toggleDetails();
			} else if(e.getSource()==playSentenceButton) {
				playSentence();
			}
//			else if(e.getSource()==toggleExpandButton) {
//				toggleExpand();
//			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseWheelMoved(java.awt.event.MouseWheelEvent)
		 */
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if(!config.mouseWheelScrollSupported) {
				return;
			}

			if(e.getWheelRotation()<0) {
				// Away from user => scroll left
				//TODO ask whether this is the preferred direction
				wordCursorModel.previousWord();
			} else {
				wordCursorModel.nextWord();
			}
		}

//		/**
//		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
//		 */
//		@Override
//		public void mousePressed(MouseEvent e) {
//			if(e.getSource()==wordCursor) {
//				wordCursorModel.setIgnoreChanges(true);
//			}
//		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.isPopupTrigger()) {
				//TODO implement popup menu
				return;
			}

			if(e.getSource()==wordCursor) {
//				wordCursorModel.setIgnoreChanges(false);
				int value = e.getPoint().x;
				wordCursorModel.setValue(value);
			} else if(e.getSource()==detailPanel) {
				PaIntEHitBox hitBox = detailPanel.translate(e.getPoint());
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

		/**
		 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseMoved(MouseEvent e) {
			if(e.getSource()==detailPanel) {
				refreshDetailPanel(e.getPoint());
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

		private void refreshDetailPanel(Point p) {
			PaIntEHitBox hitBox = detailPanel.translate(p);

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

			detailPanel.setCursor(cursor);
			detailPanel.setToolTipText(tooltip);
//			detailPanel.repaint();
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
			if(e.getSource()==detailPanel) {
				cursor = detailPanel.getCursor();
				refreshDetailPanel(e.getPoint());
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseExited(MouseEvent e) {
			if(e.getSource()==detailPanel) {
				detailPanel.setCursor(cursor);
				cursor = null;
			}
		}
	}

	public static class PanelConfig {

		public static Color DEFAULT_CURVE_COLOR = Color.black;
		public static Color DEFAULT_SYLLABLE_ALIGNMENT_COLOR = Color.orange;
		public static final boolean  DEFAULT_TEXT_SHOW_ALIGNMENT = false;
		public static final boolean  DEFAULT_PREVIEW_SHOW_ALIGNMENT = false;
		public static Color DEFAULT_WORD_ALIGNMENT_COLOR = Color.lightGray;
		public static AntiAliasingType DEFAULT_ANTIALIASING_TYPE = AntiAliasingType.DEFAULT;
		public static final int DEFAULT_GRAPH_HEIGHT = 90;
		public static final int DEFAULT_GRAPH_WIDTH = 120;
		public static final int DEFAULT_WORD_SCOPE = 0;
		public static final int DEFAULT_SYLLABLE_SCOPE = 1;
		public static final boolean  DEFAULT_MOUSE_WHEEL_SCROLL_SUPPORTED = true;
		public static final int DEFAULT_WORD_SPACING = 3;
		public static final int DEFAULT_GRAPH_SPACING = 2;
		public static final boolean  DEFAULT_CLEAR_LABEL_BACKGROUND = true;
		public static final boolean  DEFAULT_LOOP_SOUND = false;
		public static final PreviewSize DEFAULT_PREVIEW_SIZE = PreviewSize.MEDIUM;
		public static final float DEFAULT_LEFT_SYLLABLE_BOUND = 0F;
		public static final float DEFAULT_RIGHT_SYLLABLE_BOUND = 1F;

		public static final LabelPattern DEFAULT_HEADER_PATTERN = new LabelPattern("n"); //$NON-NLS-1$
		public static final LabelPattern DEFAULT_SENTENCE_PATTERN = new LabelPattern("$form$\n$pos$"); //$NON-NLS-1$
		public static final LabelPattern DEFAULT_DETAIL_PATTERN = new LabelPattern(""); //$NON-NLS-1$

		// General
		public AntiAliasingType antiAliasingType = DEFAULT_ANTIALIASING_TYPE;
		public boolean mouseWheelScrollSupported = DEFAULT_MOUSE_WHEEL_SCROLL_SUPPORTED;
		public boolean loopSound = DEFAULT_LOOP_SOUND;
		public Color syllableAlignmentColor = DEFAULT_SYLLABLE_ALIGNMENT_COLOR;
		public Color wordAlignmentColor = DEFAULT_WORD_ALIGNMENT_COLOR;

		// Detail
		public int wordScope = DEFAULT_WORD_SCOPE;
		public int syllableScope = DEFAULT_SYLLABLE_SCOPE;
		public int graphHeight = DEFAULT_GRAPH_HEIGHT;
		public int graphWidth = DEFAULT_GRAPH_WIDTH;
		public int wordSpacing = DEFAULT_WORD_SPACING;
		public int graphSpacing = DEFAULT_GRAPH_SPACING;
		public boolean clearLabelBackground = DEFAULT_CLEAR_LABEL_BACKGROUND;
		public LabelPattern detailPattern = DEFAULT_DETAIL_PATTERN;
		public Font detailFont = TextArea.DEFAULT_FONT;
		public Color detailTextColor = TextArea.DEFAULT_TEXT_COLOR;
		public Font detailAxisLabelFont = Axis.DEFAULT_FONT;
		public Color detailAxisColor = Axis.DEFAULT_AXIS_COLOR;
		public Color detailAxisLabelColor = Axis.DEFAULT_LABEL_COLOR;
		public Color detailAxisMarkerColor = Axis.DEFAULT_MARKER_COLOR;
		public int detailAxisMarkerHeight = Axis.DEFAULT_MARKER_HEIGHT;
		public boolean detailPaintGrid = PaIntEGraph.DEFAULT_PAINT_GRID;
		public boolean detailPaintBorder = PaIntEGraph.DEFAULT_PAINT_BORDER;
		public Color detailGridColor = PaIntEGraph.DEFAULT_GRID_COLOR;
		public Color detailBorderColor = PaIntEGraph.DEFAULT_BORDER_COLOR;
		public GridStyle detailGridStyle = PaIntEGraph.DEFAULT_GRID_STYLE;

		// Text
		public LabelPattern sentencePattern = DEFAULT_SENTENCE_PATTERN;
		public LabelPattern headerPattern = DEFAULT_HEADER_PATTERN;
		public Font sentenceFont = TextArea.DEFAULT_FONT;
		public Color sentenceTextColor = TextArea.DEFAULT_TEXT_COLOR;
		public boolean textShowAlignment = DEFAULT_TEXT_SHOW_ALIGNMENT;

		// Preview
		public PreviewSize previewSize = DEFAULT_PREVIEW_SIZE;
		public float leftSyllableBound = DEFAULT_LEFT_SYLLABLE_BOUND;
		public float rightSyllableBound = DEFAULT_RIGHT_SYLLABLE_BOUND;
		public Color curveColor = DEFAULT_CURVE_COLOR;
		public boolean previewShowAlignment = DEFAULT_PREVIEW_SHOW_ALIGNMENT;
	}

	private class CurvePanel extends JComponent {
		private static final long serialVersionUID = 2570632250556713583L;

		/**
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);

			SentenceInfo sentenceInfo = getSentenceInfo();

			if(sentenceInfo==null) {
				return;
			}

			Graphics2D g = (Graphics2D) graphics;
			config.antiAliasingType.apply(g);
			TextArea textArea = textPanel.textArea;

			Color c = g.getColor();
			g.setColor(config.curveColor);

			int h = getHeight();

			float minD = sentenceInfo.getMinD();
			float maxD = sentenceInfo.getMaxD();
			float scaleY = h/(maxD-minD);

			for(int wordIndex=0; wordIndex<sentenceInfo.wordCount(); wordIndex++) {
				WordInfo wordInfo = sentenceInfo.wordInfo(wordIndex);

				final int wx = wordInfo.getX();
				final int ww = wordInfo.getWidth();

				// Paint curve preview

				// Align curve with text
				int x = textArea.getLeftInsets();
				if(textArea.getHorizontalAlignment()==SwingConstants.RIGHT) {
					x = ww-textArea.getRightInsets()-wordInfo.getCurveWidth();
				} else if(textArea.getHorizontalAlignment()==SwingConstants.CENTER) {
					x += (ww-textArea.getLeftInsets()-textArea.getRightInsets())/2 - wordInfo.getCurveWidth()/2;
				}

				if(config.previewShowAlignment) {
					g.setColor(config.wordAlignmentColor);
					g.drawLine(wx+x, 0, wx+x, h);
					g.setColor(config.curveColor);
				}

				int w = 0;

				for(int sylIndex=0; sylIndex<wordInfo.sylCount(); sylIndex++) {
					SyllableInfo sylInfo = wordInfo.syllableInfo(sylIndex);

					int sylWidth = sylInfo.getWidth();

					if(sylIndex>0 && config.previewShowAlignment) {
						g.setColor(config.syllableAlignmentColor);
						g.drawLine(wx+x+w, 0, wx+x+w, h);
						g.setColor(config.curveColor);
					}

					w += sylWidth;

					// Ignore syllables with alignment outside bounds
					if(config.leftSyllableBound!=config.rightSyllableBound
							&& (sylInfo.getB()<config.leftSyllableBound || sylInfo.getB()>config.rightSyllableBound)) {
						continue;
					}

					int x0 = wx + x + sylInfo.getX();
					int x2 = x0 + sylInfo.getWidth();
					int x1 = (x0+x2) >> 1;

					float d = sylInfo.getD();
					float c1 = sylInfo.getC1();
					float c2 = sylInfo.getC2();

					int y0 = h - (int)((d-c1-minD) * scaleY);
					int y1 = h - (int)((d-minD) * scaleY);
					int y2 = h - (int)((d-c2-minD) * scaleY);

					// Prevent artifacts from antialiasing
					if(y0==y1 && y1==y2) {
						g.drawLine(x0, y0, x2, y2);
					} else {
						g.drawLine(x0, y0, x1, y1);
						g.drawLine(x1, y1, x2, y2);
					}
				}

				if(config.previewShowAlignment) {
					g.setColor(config.wordAlignmentColor);
					g.drawLine(wx+x+w, 0, wx+x+w, h);
					g.setColor(config.curveColor);
				}
			}

			g.setColor(c);
		}

		/**
		 * @see javax.swing.JComponent#getPreferredSize()
		 */
		@Override
		public Dimension getPreferredSize() {
			Dimension d = new Dimension();

			if(sentenceInfo!=null) {
				d.height = textPanel.curveHeight;
				d.width = sentenceInfo.getWidth();
			}

			return d;
		}

		/**
		 * @see javax.swing.JComponent#getMinimumSize()
		 */
		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

	}

	private class TextPanel extends JComponent {

		private static final long serialVersionUID = 823306346320292939L;

		private final TextArea textArea = new TextArea();

		private Dimension preferredSize;
		private int curveHeight;

		public void rebuild() {
			LabelPattern pattern = config.sentencePattern;
			textArea.setFont(config.sentenceFont);
			SentenceInfo sentenceInfo = getSentenceInfo();

			Dimension size = new Dimension();

			if(sentenceInfo!=null) {
				FontMetrics fm = getFontMetrics(textArea.getFont());
				curveHeight = config.previewSize.getHeight(fm);

				ProsodicSentenceData sentence = sentenceInfo.getSentence();

				Dimension areaSize = new Dimension();
				int width = 0;

				for(int wordIndex=0; wordIndex<sentenceInfo.wordCount(); wordIndex++) {
					// Honor ward spacing
					if(wordIndex>0) {
						width += fm.charWidth(' ');
					}

					WordInfo wordInfo = sentenceInfo.wordInfo(wordIndex);

					// Compute text lines and save them
					String[] lines = pattern.getText(sentence, wordIndex);
					wordInfo.setLines(lines);

					// Compute required space for text lines
					textArea.getSize(this, lines, areaSize);
					size.height = Math.max(size.height, areaSize.height);

//					System.out.printf("form=%s x=%d width=%d sw=%d\n",
//							lines[0], width, areaSize.width, fm.stringWidth(lines[0]));

					// Save word bounds
					wordInfo.setX(width);
					wordInfo.setWidth(areaSize.width);

					// Save syllable bounds
					int wordLength = 0;
					for(int sylIndex=0; sylIndex<wordInfo.sylCount(); sylIndex++) {
						SyllableInfo sylInfo = wordInfo.syllableInfo(sylIndex);
						int sylWidth = fm.stringWidth(sylInfo.getLabel());

						sylInfo.setX(wordLength);
						sylInfo.setWidth(sylWidth);

						wordLength += sylWidth;
					}

					wordInfo.setCurveWidth(wordLength);

					width += areaSize.width;
				}

				size.width = width;
			} else {
				curveHeight = 0;
			}

			sentenceInfo.setWidth(size.width);

			preferredSize = size;
		}

		public void clear() {
			preferredSize = new Dimension();
		}

		/**
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);

			SentenceInfo sentenceInfo = getSentenceInfo();

			if(sentenceInfo==null) {
				return;
			}

			Graphics2D g = (Graphics2D) graphics;

			textArea.setFont(config.sentenceFont);
			textArea.setTextColor(config.sentenceTextColor);

			int h = getHeight();

			Rectangle area = new Rectangle();

			area.height = h;

			for(int wordIndex=0; wordIndex<sentenceInfo.wordCount(); wordIndex++) {
				WordInfo wordInfo = sentenceInfo.wordInfo(wordIndex);
				String[] lines = wordInfo.getLines();

				area.x = wordInfo.getX();
				area.width = wordInfo.getWidth();

				if(config.textShowAlignment) {
					// Align curve with text
					int x = textArea.getLeftInsets();
					if(textArea.getHorizontalAlignment()==SwingConstants.RIGHT) {
						x = area.width-textArea.getRightInsets()-wordInfo.getCurveWidth();
					} else if(textArea.getHorizontalAlignment()==SwingConstants.CENTER) {
						x += (area.width-textArea.getLeftInsets()-textArea.getRightInsets())/2 - wordInfo.getCurveWidth()/2;
					}

					// First line
					g.setColor(config.wordAlignmentColor);
					g.drawLine(area.x+x, 0, area.x+x, h);

					int w = 0;

					for(int sylIndex=0; sylIndex<wordInfo.sylCount(); sylIndex++) {
						SyllableInfo sylInfo = wordInfo.syllableInfo(sylIndex);

						int sylWidth = sylInfo.getWidth();

						if(sylIndex>0) {
							g.setColor(config.syllableAlignmentColor);
							g.drawLine(area.x+x+w, 0, area.x+x+w, h);
						}

						w += sylWidth;
					}

					// Second line
					g.setColor(config.wordAlignmentColor);
					g.drawLine(area.x+x+w, 0, area.x+x+w, h);
					g.setColor(config.curveColor);
				}

				// Paint text
				textArea.paint(g, lines, area);
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

	static final char SPACE = ' ';

	private class DetailPanel extends JComponent {

		private static final long serialVersionUID = 8943579954234061767L;
		private Rectangle lastPaintArea;

		public DetailPanel() {
			setBackground(new Color(0, true));
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
		}

		/**
		 * @see javax.swing.JComponent#getMinimumSize()
		 */
		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		/**
		 * @see javax.swing.JComponent#getPreferredSize()
		 */
		@Override
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();

			Graphics g = getGraphics();

			int firstWord = getFirstWordToPaint();
			int lastWord = getLastWordToPaint();
			int sylCount = sentenceInfo.sylCount(firstWord, lastWord);
			int wordCount = lastWord-firstWord+1;

			int width = wordCount*graph.getYAxis().getRequiredWidth(g)
					+ (wordCount-1) * config.wordSpacing
					+ sylCount * config.graphWidth
					+ (sylCount - 2*wordCount) * config.graphSpacing;
			int height = config.graphHeight+graph.getXAxis().getRequiredHeight(g)+g.getFontMetrics().getHeight();

			g.dispose();

			d.width = Math.max(width, d.width);
			d.height = Math.max(height, d.height);

			return d;
		}

		private int getFirstWordToPaint() {
			if(sentenceInfo==null) {
				return 0;
			}

			int result = currentWord;

			if(config.wordScope>0) {
				int addedWords = 0;
				int wordIndex = currentWord;
				// Expand to the left
				while(wordIndex>0 && addedWords<config.wordScope) {
					wordIndex--;
					if(sentenceInfo.hasSyllables(wordIndex)) {
						addedWords++;
						result = wordIndex;
					}
				}
			}

			return result;
		}

		private int getLastWordToPaint() {
			if(sentenceInfo==null) {
				return 0;
			}

			int result = currentWord;

			if(config.wordScope>0) {
				int addedWords = 0;
				int wordIndex = currentWord;
				// Expand to the left
				while(wordIndex<sentenceInfo.getSentence().length()-1 && addedWords<config.wordScope) {
					wordIndex++;
					if(sentenceInfo.hasSyllables(wordIndex)) {
						addedWords++;
						result = wordIndex;
					}
				}
			}

			return result;
		}

		public PaIntEHitBox translate(Point p) {

			if(sentenceInfo==null || sentenceInfo.sylCount()==0) {
				return null;
			}

			if(currentWord==-1) {
				return null;
			}

			Rectangle lastPaintArea = this.lastPaintArea;

			if(lastPaintArea==null || (p.y>=lastPaintArea.y && !lastPaintArea.contains(p))) {
				return null;
			}

			int x = p.x-lastPaintArea.x;
			int y = p.y;

			Graphics2D g = (Graphics2D) getGraphics();
			FontMetrics fm = g.getFontMetrics();
			int yAxisWidth = graph.getYAxis().getRequiredWidth(g);

			int leftWord = getFirstWordToPaint();
			int rightWord = getLastWordToPaint();

			int wordCount = rightWord - leftWord + 1;

			Rectangle area = new Rectangle();
			area.y = fm.getHeight();

//			System.out.printf("x=%d y=%d fh=%d\n", x, y, fm.getHeight());

			// Iterate over words
			for(int i=0; i<wordCount; i++) {
				if(i>0) {
					area.x += config.wordSpacing;
				}

				int begin = area.x;
				int wordIndex = leftWord+i;
				int sylCount = sentenceInfo.sylCount(wordIndex);

				// Iterate over syllables of word
				for(int j=0; j<sylCount; j++) {
					area.width = config.graphWidth;
					area.height = lastPaintArea.height;

					if(j>0) {
						area.x += config.graphSpacing;
						graph.setPaintYAxis(false);
					} else {
						area.width += yAxisWidth;
						graph.setPaintYAxis(true);
					}

					if(area.contains(x, y)) {

						String sylLabel = sentenceInfo.getSentence().getSyllableLabel(wordIndex, j);

						// Label
						if(sylLabel!=null) {
							int sw = fm.stringWidth(sylLabel);
							int sx = area.x + area.width/2 - sw/2;
							int sy = area.y + fm.getHeight();

							if(y<sy && x>=sx && x<=sx+sw) {
								return new PaIntEHitBox(wordIndex, j);
							}
						}

						params.setParams(sentenceInfo.getSentence(), wordIndex, j);

						y -= area.y;
						x -= area.x;

						return graph.translate(x, y, g, area, params, translationAccuracy);
					}

					area.x += area.width;
				}

				int end = area.x;

				String wordLabel = sentenceInfo.getSentence().getForm(wordIndex);

				if(wordLabel!=null) {
					int sw = fm.stringWidth(wordLabel);
					int sx = (begin + end)/2 - sw/2;
					int sy = fm.getHeight();

//					System.out.printf("x=%d y=%d sw=%d sx=%d sy=%d area=%s\n",
//							x, y, sw, sx, sy, area);

					if(y<=sy && x>=sx && x<=sx+sw) {
						return new PaIntEHitBox(wordIndex);
					}
				}
			}

			return null;
		}

		/**
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);

			lastPaintArea = null;

			if(sentenceInfo==null || sentenceInfo.sylCount()==0) {
				return;
			}

			if(currentWord==-1) {
				return;
			}

			Graphics2D g = (Graphics2D) graphics;

			g.setColor(config.detailTextColor);
			g.setFont(config.detailFont);

			int leftWord = getFirstWordToPaint();
			int rightWord = getLastWordToPaint();

			int wordCount = rightWord - leftWord + 1;
			int graphCount = sentenceInfo.sylCount(leftWord, rightWord);

			int yAxisWidth = graph.getYAxis().getRequiredWidth(g);
			int xAxisHeight = graph.getXAxis().getRequiredHeight(g);

			int width = wordCount*graph.getYAxis().getRequiredWidth(g)
					+ (wordCount-1) * config.wordSpacing
					+ graphCount * config.graphWidth
					+ (graphCount - 2*wordCount) * config.graphSpacing;
			int height = config.graphHeight+xAxisHeight;
			int w = getWidth();

			Rectangle area = new Rectangle();

			if(width<w && !wordCursorModel.getValueIsAdjusting()) {
				int cursorPos = wordCursorModel.getValue();
				int x = cursorPos - width/2 - yAxisWidth/2;
				x = Math.max(0, Math.min(x, w-width));

				area.x = x;
			}

			FontMetrics fm = g.getFontMetrics();

			area.y = fm.getHeight();

			lastPaintArea = new Rectangle(area.x, area.y, width, height);

			Color bg = SentencePanel.this.getBackground();

			// Paint graphs in blocks per word
			for(int i=0; i<wordCount; i++) {
				if(i>0) {
					area.x += config.wordSpacing;
				}

				int begin = area.x;
				int wordIndex = leftWord+i;
				int sylCount = sentenceInfo.sylCount(wordIndex);

				for(int j=0; j<sylCount; j++) {
					area.width = config.graphWidth;
					area.height = height;

					if(j>0) {
						area.x += config.graphSpacing;
						graph.setPaintYAxis(false);
					} else {
						area.width += yAxisWidth;
						graph.setPaintYAxis(true);
					}

					params.setParams(sentenceInfo.getSentence(), wordIndex, j);

					graph.paint(g, params, area);

					String sylLabel = sentenceInfo.getSentence().getSyllableLabel(wordIndex, j);

					// Label
					if(sylLabel!=null) {
						int sw = fm.stringWidth(sylLabel);
						int x = area.x + area.width/2 - sw/2;
						int y = area.y + fm.getHeight();

						Color c = g.getColor();

						if(config.clearLabelBackground) {

							g.setColor(bg);
							g.fillRect(x-1, y-fm.getAscent(), sw+2, fm.getHeight());
						}

						g.setFont(config.detailFont);
						g.setColor(config.detailTextColor);
						g.drawString(sylLabel, x, y);

						g.setColor(c);
					}

					area.x += area.width;
				}

				int end = area.x;

				String wordLabel = sentenceInfo.getSentence().getForm(wordIndex);

				if(wordLabel!=null) {
					int sw = fm.stringWidth(wordLabel);
					int x = (begin + end)/2 - sw/2;
					int y = fm.getHeight();

					Color c = g.getColor();

					if(config.clearLabelBackground) {
						g.setColor(bg);
						g.fillRect(x-1, y-fm.getAscent(), sw+2, fm.getHeight());
					}

					g.setFont(config.detailFont);
					g.setColor(config.detailTextColor);
					g.drawString(wordLabel, x, y);

					g.setColor(c);
				}
			}
		}
	}

	private class WordCursorModel extends DefaultBoundedRangeModel {

		private static final long serialVersionUID = 1354033961825662955L;

		private int wordIndex = 0;
		private boolean ignoreChanges = false;

		public void refresh() {
			setRangeProperties(0, getExtent(), 0, 0, false);
		}

		public boolean isIgnoreChanges() {
			return ignoreChanges;
		}

		public void setIgnoreChanges(boolean ignoreChanges) {
			this.ignoreChanges = ignoreChanges;
		}

		public int getWordIndex() {
			return wordIndex;
		}

		/**
		 * Moves to the next valid word index (this is a word with
		 * at least one designated syllable in it).
		 */
		public void nextWord() {
			if(sentenceInfo==null) {
				return;
			}

			int newWordIndex = wordIndex;
			while(newWordIndex<sentenceInfo.getSentence().length()-1) {
				newWordIndex++;
				if(sentenceInfo.hasSyllables(newWordIndex)) {
					break;
				}
			}

			// Final sanity check to avoid getting stuck at the borders of a sentence with an invalid word index
			if(newWordIndex==wordIndex || !sentenceInfo.hasSyllables(newWordIndex)) {
				return;
			}

			wordIndex = newWordIndex;
			setRangePropertiesUnchecked(getWordCenter(newWordIndex), getExtent(), getMinimum(), getMaximum(), getValueIsAdjusting());
		}

		/**
		 * Moves to the previous valid word index (this is a word with
		 * at least one designated syllable in it).
		 */
		public void previousWord() {
			if(sentenceInfo==null) {
				return;
			}

			int newWordIndex = wordIndex;
			while(newWordIndex>0) {
				newWordIndex--;
				if(sentenceInfo.hasSyllables(newWordIndex)) {
					break;
				}
			}

			// Final sanity check to avoid getting stuck at the borders of a sentence with an invalid word index
			if(newWordIndex==wordIndex || !sentenceInfo.hasSyllables(newWordIndex)) {
				return;
			}

			wordIndex = newWordIndex;
			setRangePropertiesUnchecked(getWordCenter(newWordIndex), getExtent(), getMinimum(), getMaximum(), getValueIsAdjusting());
		}

		private void setRangePropertiesUnchecked(int newValue, int newExtent, int newMin, int newMax, boolean adjusting) {
			super.setRangeProperties(newValue, newExtent, newMin, newMax, adjusting);
		}

		private int snapToWord(int value) {
    		return sentenceInfo.nearestWordForOffset(value).getWordIndex();
		}

		private int getWordCenter(int wordIndex) {
			WordInfo wordInfo = sentenceInfo.wordInfo(wordIndex);
			return wordInfo.getX()+wordInfo.getWidth()/2;
		}

	    @Override
		public void setRangeProperties(int newValue, int newExtent, int newMin, int newMax, boolean adjusting) {

	    	if(isIgnoreChanges()) {
	    		return;
	    	}

	    	// Adjust values only in case we have a valid set of curves
	    	if(sentenceInfo!=null && sentenceInfo.sylCount()>0) {
	    		newMax = Math.max(newMax, sentenceInfo.getWidth());
	    		// Snap to nearest word
	    		wordIndex = snapToWord(newValue);
	    		newValue = getWordCenter(wordIndex);
	    	}

	    	setRangePropertiesUnchecked(newValue, newExtent, newMin, newMax, adjusting);
	    }
	}
}
