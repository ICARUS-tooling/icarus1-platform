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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
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

import javax.swing.BorderFactory;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.sound.SoundException;
import de.ims.icarus.plugins.prosody.sound.SoundOffsets;
import de.ims.icarus.plugins.prosody.sound.SoundPlayer;
import de.ims.icarus.plugins.prosody.sound.SoundPlayer.SoundFile;
import de.ims.icarus.plugins.prosody.ui.geom.AntiAliasingType;
import de.ims.icarus.plugins.prosody.ui.geom.Axis;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEHitBox;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEParams;
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

	private CurveInfo curveInfo;

//	private JToggleButton toggleExpandButton;
	private JToggleButton toggleDetailsButton;
	private JButton playSentenceButton;

	private WordCursorModel wordCursorModel;
	private JSlider wordCursor;
	private DetailPanel detailPanel;

	private Handler handler;

	private JLabel sentenceLabel;
	private JLabel headerLabel;
	private JLabel curveLabel;
	private Icon curveIcon = new Icon() {

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			if(curveInfo!=null) {
				g.drawImage(curveInfo.getImage(), x, y, c);
			}
		}

		@Override
		public int getIconWidth() {
			return curveInfo==null ? 0 : curveInfo.getImage().getWidth();
		}

		@Override
		public int getIconHeight() {
			return curveInfo==null ? 0 : curveInfo.getImage().getHeight();
		}
	};

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
				"left:pref, 2dlu, fill:pref, fill:pref:grow", //$NON-NLS-1$
				"top:pref, top:pref, pref, pref"); //$NON-NLS-1$

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

		// Sentence label
		sentenceLabel = new JLabel();
		sentenceLabel.setOpaque(false);
		add(sentenceLabel, CC.rc(4, 3));

		// Header label (to the left)
		headerLabel = new JLabel();
		headerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		headerLabel.setOpaque(false);
		add(headerLabel, CC.rc(4, 1));

		// Cursor
		wordCursorModel = new WordCursorModel();
		wordCursor = new JSlider(wordCursorModel);
		wordCursor.setFocusable(false);
		wordCursor.addChangeListener(handler);
		wordCursor.addMouseWheelListener(handler);
		wordCursor.addMouseListener(handler);
		wordCursor.setOpaque(false);
		add(wordCursor, CC.rc(2, 3));

		// Button for playing entire sentence
		playSentenceButton = new JButton();
		playSentenceButton.setIcon(iconRegistry.getIcon("speaker.png")); //$NON-NLS-1$
		playSentenceButton.setFocusable(false);
		playSentenceButton.setFocusPainted(false);
		playSentenceButton.setBorderPainted(false);
		playSentenceButton.addActionListener(handler);
		UIUtil.resizeComponent(playSentenceButton, 16, 16);
		add(playSentenceButton, CC.rc(2, 1));

		// Curve label
		curveLabel = new JLabel();
		curveLabel.setIcon(curveIcon);
//		curveLabel.setVisible(toggleExpandButton.isSelected());
		curveLabel.setOpaque(false);
		add(curveLabel, CC.rc(3, 3));

		// Upper details area
		detailPanel = new DetailPanel();
		detailPanel.setFocusable(false);
		detailPanel.setOpaque(false);
		detailPanel.addMouseListener(new TooltipFreezer());
		detailPanel.addMouseListener(handler);
		detailPanel.addMouseMotionListener(handler);
		add(detailPanel, CC.rcw(1, 3, 2));

		toggleDetails();
	}

	public void clear() {
		curveInfo = null;
		currentWord = -1;

		refresh();
	}

	public void refresh(ProsodicSentenceData data) {
		if (data == null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$

		curveInfo = new CurveInfo(data, curveLabel.getFontMetrics(curveLabel.getFont()), config.antiAliasingType, config.curveColor);

		refresh();
	}

	private void refresh() {

		if(curveInfo==null) {
			sentenceLabel.setText(null);
			headerLabel.setText(null);

//			toggleExpandButton.setSelected(false);
			toggleDetailsButton.setSelected(false);

		} else {

			// Refresh text + image
			sentenceLabel.setText(curveInfo.getText());
			headerLabel.setText((String) curveInfo.getSentence().getProperty(ProsodyConstants.SENTENCE_NUMBER_KEY));

			// Refresh graph internals
			Axis.Integer yAxis = (Axis.Integer)graph.getYAxis();
			double stepSize = yAxis.getMarkerStepSize();
			float maxD = curveInfo.getMaxD();
			float minD = curveInfo.getMinD();
			int yMax = (int) (Math.ceil(maxD/stepSize) * stepSize);
			int yMin = (int) (Math.floor(minD/stepSize) * stepSize);
			//TODO maybe we should iterate over all sentences to get the "real" outer boundaries?
			yAxis.setMinValue(yMin);
			yAxis.setMaxValue(yMax);

			// Synchronize cursor
//			wordCursor.setMaximum(curveInfo.getImage().getWidth());
			wordCursorModel.refresh();
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
		playSentenceButton.setVisible(showDetails);

		setBorder(showDetails ? expandedBorder : collapsedBorder);

		if(showDetails) {
			add(toggleDetailsButton, CC.rc(1, 1));
		} else {
			add(toggleDetailsButton, CC.rc(3, 1));
		}

		refreshDetails();
	}

	private void refreshDetails() {

		if(wordCursorModel.getValueIsAdjusting()) {
			return;
		}

		currentWord = wordCursorModel.getWordIndex();

//		System.out.printf("offset=%d syl=%d\n", offset, currentSyl); //$NON-NLS-1$

		detailPanel.revalidate();
		detailPanel.repaint();
	}

	private void play(float beginOffset, float endOffset) {

		if(beginOffset==LanguageConstants.DATA_UNDEFINED_VALUE
				|| endOffset==LanguageConstants.DATA_UNDEFINED_VALUE) {
			LoggerFactory.warning(this, String.format(
					"Cannot play part of sentence - at least one offset is undefined: [%.02f , %.02f]", beginOffset, endOffset)); //$NON-NLS-1$
			return;
		}

		try {
			ProsodicSentenceData sentence = curveInfo.getSentence();

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
			LoggerFactory.error(this, "Failed to play sound for part of sentence '"+curveInfo.getText()+"'", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void playSentence() {
		ProsodicSentenceData sentence = curveInfo.getSentence();
		float beginOffset = SoundOffsets.getBeginOffset(sentence);
		float endOffset = SoundOffsets.getEndOffset(sentence);

		play(beginOffset, endOffset);
	}

	private void playWord(int wordIndex) {
		ProsodicSentenceData sentence = curveInfo.getSentence();
		float beginOffset = SoundOffsets.getBeginOffset(sentence, wordIndex);
		float endOffset = SoundOffsets.getEndOffset(sentence, wordIndex);

		play(beginOffset, endOffset);
	}

	private void playSyllable(int wordIndex, int sylIndex) {
		ProsodicSentenceData sentence = curveInfo.getSentence();
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
			if(curveInfo==null || curveInfo.sylCount()==0) {
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

		private final DecimalFormat decimalFormat = new DecimalFormat("#,###,###,##0.00"); //$NON-NLS-1$

		/**
		 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseMoved(MouseEvent e) {
			if(e.getSource()==detailPanel) {
				refreshDetailPanel(e.getPoint());
			}
		}

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
		public static AntiAliasingType DEFAULT_ANTIALIASING_TYPE = AntiAliasingType.DEFAULT;
		public static final int DEFAULT_GRAPH_HEIGHT = 90;
		public static final int DEFAULT_GRAPH_WIDTH = 120;
		public static final int DEFAULT_WORD_SCOPE = 1;
		public static final boolean  DEFAULT_MOUSE_WHEEL_SCROLL_SUPPORTED = true;
		public static final int DEFAULT_WORD_SPACING = 3;
		public static final int DEFAULT_GRAPH_SPACING = 2;
		public static final boolean  DEFAULT_CLEAR_LABEL_BACKGROUND = true;
		public static final boolean  DEFAULT_LOOP_SOUND = false;

		private Color curveColor = DEFAULT_CURVE_COLOR;
		private AntiAliasingType antiAliasingType = DEFAULT_ANTIALIASING_TYPE;
		private int wordScope = DEFAULT_WORD_SCOPE;
		private int graphHeight = DEFAULT_GRAPH_HEIGHT;
		private int graphWidth = DEFAULT_GRAPH_WIDTH;
		private int wordSpacing = DEFAULT_WORD_SPACING;
		private int graphSpacing = DEFAULT_GRAPH_SPACING;
		private boolean mouseWheelScrollSupported = DEFAULT_MOUSE_WHEEL_SCROLL_SUPPORTED;
		private boolean clearLabelBackground = DEFAULT_CLEAR_LABEL_BACKGROUND;
		private boolean loopSound = DEFAULT_LOOP_SOUND;
	}

	private class TextPanel extends JComponent {

	}

	static final char SPACE = ' ';

	private class DetailPanel extends JComponent {

		private static final long serialVersionUID = 8943579954234061767L;
		private Rectangle lastPaintArea;

		public DetailPanel() {
			setBackground(new Color(0, true));
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
			int sylCount = curveInfo.sylCount(firstWord, lastWord);
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

		private int getFirstSylToPaint() {
			if(curveInfo==null) {
				return 0;
			}

			int leftSyl = curveInfo.firstSyl(currentWord);

			if(config.wordScope>0) {
				int addedWords = 0;
				int wordIndex = currentWord;
				// Expand to the left
				while(wordIndex>0 && addedWords<config.wordScope) {
					wordIndex--;
					if(curveInfo.hasSyllables(wordIndex)) {
						addedWords++;
						leftSyl = curveInfo.firstSyl(wordIndex);
					}
				}
			}

			return leftSyl;
		}

		private int getLastSylToPaint() {
			if(curveInfo==null) {
				return 0;
			}

			int rightSyl = curveInfo.lastSyl(currentWord);

			if(config.wordScope>0) {
				int addedWords = 0;
				int wordIndex = currentWord;
				// Expand to the left
				while(wordIndex<curveInfo.getSentence().length()-1 && addedWords<config.wordScope) {
					wordIndex++;
					if(curveInfo.hasSyllables(wordIndex)) {
						addedWords++;
						rightSyl = curveInfo.lastSyl(wordIndex);
					}
				}
			}

			return rightSyl;
		}

		private int getFirstWordToPaint() {
			if(curveInfo==null) {
				return 0;
			}

			int result = currentWord;

			if(config.wordScope>0) {
				int addedWords = 0;
				int wordIndex = currentWord;
				// Expand to the left
				while(wordIndex>0 && addedWords<config.wordScope) {
					wordIndex--;
					if(curveInfo.hasSyllables(wordIndex)) {
						addedWords++;
						result = wordIndex;
					}
				}
			}

			return result;
		}

		private int getLastWordToPaint() {
			if(curveInfo==null) {
				return 0;
			}

			int result = currentWord;

			if(config.wordScope>0) {
				int addedWords = 0;
				int wordIndex = currentWord;
				// Expand to the left
				while(wordIndex<curveInfo.getSentence().length()-1 && addedWords<config.wordScope) {
					wordIndex++;
					if(curveInfo.hasSyllables(wordIndex)) {
						addedWords++;
						result = wordIndex;
					}
				}
			}

			return result;
		}

		public PaIntEHitBox translate(Point p) {

			if(curveInfo==null || curveInfo.sylCount()==0) {
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
				int sylCount = curveInfo.sylCount(wordIndex);

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

						String sylLabel = curveInfo.getSentence().getSyllableLabel(wordIndex, j);

						// Label
						if(sylLabel!=null) {
							int sw = fm.stringWidth(sylLabel);
							int sx = area.x + area.width/2 - sw/2;
							int sy = area.y + fm.getHeight();

							if(y<sy && x>=sx && x<=sx+sw) {
								return new PaIntEHitBox(wordIndex, j);
							}
						}

						params.setParams(curveInfo.getSentence(), wordIndex, j);

						y -= area.y;
						x -= area.x;

						return graph.translate(x, y, g, area, params, translationAccuracy);
					}

					area.x += area.width;
				}

				int end = area.x;

				String wordLabel = curveInfo.getSentence().getForm(wordIndex);

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

			if(curveInfo==null || curveInfo.sylCount()==0) {
				return;
			}

			if(currentWord==-1) {
				return;
			}

			Graphics2D g = (Graphics2D) graphics;

			int leftWord = getFirstWordToPaint();
			int rightWord = getLastWordToPaint();

			int leftSyl = curveInfo.firstSyl(leftWord);
			int rightSyl = curveInfo.lastSyl(rightWord);

			int wordCount = rightWord - leftWord + 1;
			int graphCount = rightSyl - leftSyl + 1;

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
				int sylCount = curveInfo.sylCount(wordIndex);

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

					params.setParams(curveInfo.getSentence(), wordIndex, j);

					graph.paint(g, params, area);

					String sylLabel = curveInfo.getSentence().getSyllableLabel(wordIndex, j);

					// Label
					if(sylLabel!=null) {
						int sw = fm.stringWidth(sylLabel);
						int x = area.x + area.width/2 - sw/2;
						int y = area.y + fm.getHeight();

						if(config.clearLabelBackground) {
							Color c = g.getColor();

							g.setColor(bg);
							g.fillRect(x-1, y-fm.getAscent(), sw+2, fm.getHeight());

							g.setColor(c);
						}

						g.drawString(sylLabel, x, y);
					}

					area.x += area.width;
				}

				int end = area.x;

				String wordLabel = curveInfo.getSentence().getForm(wordIndex);

				if(wordLabel!=null) {
					int sw = fm.stringWidth(wordLabel);
					int x = (begin + end)/2 - sw/2;
					int y = fm.getHeight();

					if(config.clearLabelBackground) {
						Color c = g.getColor();

						g.setColor(bg);
						g.fillRect(x-1, y-fm.getAscent(), sw+2, fm.getHeight());

						g.setColor(c);
					}

					g.drawString(wordLabel, x, y);
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
			if(curveInfo==null) {
				return;
			}

			int newWordIndex = wordIndex;
			while(newWordIndex<curveInfo.getSentence().length()-1) {
				newWordIndex++;
				if(curveInfo.hasSyllables(newWordIndex)) {
					break;
				}
			}

			// Final sanity check to avoid getting stuck at the borders of a sentence with an invalid word index
			if(newWordIndex==wordIndex || !curveInfo.hasSyllables(newWordIndex)) {
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
			if(curveInfo==null) {
				return;
			}

			int newWordIndex = wordIndex;
			while(newWordIndex>0) {
				newWordIndex--;
				if(curveInfo.hasSyllables(newWordIndex)) {
					break;
				}
			}

			// Final sanity check to avoid getting stuck at the borders of a sentence with an invalid word index
			if(newWordIndex==wordIndex || !curveInfo.hasSyllables(newWordIndex)) {
				return;
			}

			wordIndex = newWordIndex;
			setRangePropertiesUnchecked(getWordCenter(newWordIndex), getExtent(), getMinimum(), getMaximum(), getValueIsAdjusting());
		}

		private void setRangePropertiesUnchecked(int newValue, int newExtent, int newMin, int newMax, boolean adjusting) {
			super.setRangeProperties(newValue, newExtent, newMin, newMax, adjusting);
		}

		private int snapToWord(int value) {
    		int sylIndex = curveInfo.offset2Syl(value);
    		return curveInfo.syl2Word(sylIndex);
		}

		private int getWordCenter(int wordIndex) {
    		int leftSyl = curveInfo.firstSyl(wordIndex);
    		int rightSyl = curveInfo.lastSyl(wordIndex);
    		int left = curveInfo.leftOffset(leftSyl);
    		int right = curveInfo.rightOffset(rightSyl);

    		return (left + right) / 2;
		}

	    @Override
		public void setRangeProperties(int newValue, int newExtent, int newMin, int newMax, boolean adjusting) {

	    	if(isIgnoreChanges()) {
	    		return;
	    	}

	    	// Adjust values only in case we have a valid set of curves
	    	if(curveInfo!=null && curveInfo.sylCount()>0) {
	    		newMax = Math.max(newMax, curveInfo.getImage().getWidth());
	    		// Snap to nearest word
	    		wordIndex = snapToWord(newValue);
	    		newValue = getWordCenter(wordIndex);
	    	}

	    	setRangePropertiesUnchecked(newValue, newExtent, newMin, newMax, adjusting);
	    }
	}
}
