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
package de.ims.icarus.plugins.prosody.ui.list;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.annotation.AnnotatedProsodicSentenceData;
import de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotation;
import de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotationManager;
import de.ims.icarus.plugins.prosody.annotation.ProsodyHighlighting;
import de.ims.icarus.plugins.prosody.pattern.LabelPattern;
import de.ims.icarus.plugins.prosody.ui.TextArea;
import de.ims.icarus.plugins.prosody.ui.TextComponent;
import de.ims.icarus.plugins.prosody.ui.geom.AntiAliasingType;
import de.ims.icarus.plugins.prosody.ui.view.PreviewSize;
import de.ims.icarus.plugins.prosody.ui.view.SentenceInfo;
import de.ims.icarus.plugins.prosody.ui.view.SyllableInfo;
import de.ims.icarus.plugins.prosody.ui.view.WordInfo;
import de.ims.icarus.plugins.prosody.ui.view.outline.SentencePanel.PanelConfig;
import de.ims.icarus.ui.events.ListenerProxies;
import de.ims.icarus.ui.list.AbstractListCellRendererPanel;
import de.ims.icarus.util.Installable;
import de.ims.icarus.util.annotation.AnnotationController;
import de.ims.icarus.util.annotation.AnnotationManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyListCellRenderer extends AbstractListCellRendererPanel<Object> implements SwingConstants, Installable {

	public static final String DEFAULT_HEADER_PATTERN = "%documentId::15:% (n) $speaker:::?:$\\:  "; //$NON-NLS-1$

	private static final String CONFIG_PATH = "plugins.prosody.appearance.search.list"; //$NON-NLS-1$

	private static final long serialVersionUID = 8194402816943023015L;

	private static final int DEFAULT_HEIGHT = 15;


	protected AnnotationController annotationSource;

	private ProsodicSentenceData sentence;
	private SentenceInfo sentenceInfo;

	private JLabel sentenceLabel;
	private SentenceComponent sentenceComponent;
	private TextComponent headerLabel;

	private LabelPattern labelPattern = new LabelPattern();
	private boolean showCurvePreview = true;
	private PreviewSize previewSize = PanelConfig.DEFAULT_PREVIEW_SIZE;
	private AntiAliasingType antiAliasingType = PanelConfig.DEFAULT_ANTIALIASING_TYPE;
	private Color curveColor = PanelConfig.DEFAULT_CURVE_COLOR;
	private Color textColor = TextArea.DEFAULT_TEXT_COLOR;
	private Font textFont = TextArea.DEFAULT_FONT;
	private float leftSyllableBound = PanelConfig.DEFAULT_LEFT_SYLLABLE_BOUND;
	private float rightSyllableBound = PanelConfig.DEFAULT_RIGHT_SYLLABLE_BOUND;

	private final ConfigListener configListener = new ConfigListener() {

		@Override
		public void invoke(ConfigRegistry sender, ConfigEvent event) {
			reloadConfig();
		}
	};

	public ProsodyListCellRenderer() {

		setLayout(new FormLayout(
				"pref, 3dlu, pref:grow", //$NON-NLS-1$
				"fill:pref")); //$NON-NLS-1$

		headerLabel = new TextComponent();
		headerLabel.getTextArea().setVerticalAlignment(BOTTOM);
		add(headerLabel, CC.rc(1, 1));

//		sentenceLabel = new JLabel();
//		sentenceLabel.setHorizontalAlignment(LEFT);
//		sentenceLabel.setVerticalAlignment(TOP);
//		sentenceLabel.setVerticalTextPosition(BOTTOM);
//		sentenceLabel.setHorizontalTextPosition(CENTER);
//		sentenceLabel.setIcon(curveIcon);
//		add(sentenceLabel, CC.rc(1, 3));

		sentenceComponent = new SentenceComponent();
		add(sentenceComponent, CC.rc(1, 3));
	}

	@Override
	protected void prepareRenderer(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		sentence = (ProsodicSentenceData) value;
		sentenceInfo = new SentenceInfo(sentence);

		sentenceComponent.rebuild();

		if(labelPattern==null) {
			headerLabel.setLines(null);
		} else {
			headerLabel.setLines(labelPattern.getText(sentence));
		}
	}

	protected void reloadConfig() {
		ConfigRegistry registry = ConfigRegistry.getGlobalRegistry();
		Handle handle = registry.getHandle(CONFIG_PATH);

		String headerPattern = registry.getString(registry.getChildHandle(handle, "headerPattern")); //$NON-NLS-1$

		try {
			labelPattern.compile(headerPattern);
		} catch (Exception e) {
			LoggerFactory.error(this, "Faled to set new header label pattern: "+String.valueOf(headerPattern), e); //$NON-NLS-1$
		}

		showCurvePreview = registry.getBoolean(registry.getChildHandle(handle, "showCurvePreview")); //$NON-NLS-1$
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


		ConfigListener listener = ListenerProxies.getProxy(ConfigListener.class, configListener);
		ConfigRegistry.getGlobalRegistry().addGroupListener(CONFIG_PATH, listener);

		reloadConfig();
	}

	/**
	 * @see de.ims.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		this.annotationSource = null;

		ConfigListener listener = ListenerProxies.getProxy(ConfigListener.class, configListener);
		ConfigRegistry.getGlobalRegistry().removeGroupListener(CONFIG_PATH, listener);
	}

	protected AnnotationManager getAnnotationManager() {
		return annotationSource==null ? null : annotationSource.getAnnotationManager();
	}

	protected ProsodicAnnotation getAnnotation() {
		return (sentence instanceof AnnotatedProsodicSentenceData) ? ((AnnotatedProsodicSentenceData)sentence).getAnnotation() : null;
	}

	private class SentenceComponent extends JComponent {

		private static final long serialVersionUID = 8840493114006248007L;

		private Dimension preferredSize;
		private int curveHeight;
		private int textHeight;

		private static final String COL_KEY = "color";
		private static final String GROUP_KEY = "group";

		public void rebuild() {

	    	ProsodicAnnotationManager manager = (ProsodicAnnotationManager) getAnnotationManager();
	    	ProsodicAnnotation annotation = getAnnotation();
	    	manager.setAnnotation(annotation);

			Dimension size = new Dimension();

			if(sentenceInfo!=null) {
				FontMetrics fm = getFontMetrics(textFont);
				curveHeight = previewSize.getHeight(fm);
				textHeight = fm.getHeight();

				final boolean hasHighlight = manager.hasAnnotation();

				int width = 0;

				for(int wordIndex=0; wordIndex<sentenceInfo.wordCount(); wordIndex++) {
					// Honor ward spacing
					if(wordIndex>0) {
						width += fm.charWidth(' ');
					}

					WordInfo wordInfo = sentenceInfo.wordInfo(wordIndex);

					// Compute text lines and save them
					String token = wordInfo.getLabel();

//					System.out.printf("form=%s x=%d width=%d sw=%d\n",
//							lines[0], width, areaSize.width, fm.stringWidth(lines[0]));

					// Save word bounds
					wordInfo.setX(width);
					wordInfo.setWidth(fm.stringWidth(token));

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

					// Save syllable bounds
					int wordLength = 0;
					for(int sylIndex=0; sylIndex<wordInfo.sylCount(); sylIndex++) {
						SyllableInfo sylInfo = wordInfo.syllableInfo(sylIndex);
						int sylWidth = fm.stringWidth(sylInfo.getLabel());

						sylInfo.setX(wordLength);
						sylInfo.setWidth(sylWidth);

						wordLength += sylWidth;

						if(!wordHighlighted) {
							continue;
						}

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

					wordInfo.setCurveWidth(wordLength);

					width += wordInfo.getWidth();
				}

				size.width = width;
				size.height = textHeight;
				if(showCurvePreview) {
					size.height += curveHeight;
				}
			} else {
				curveHeight = 0;
				textHeight = 0;
			}

			sentenceInfo.setWidth(size.width);

			preferredSize = size;
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


		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);

			if(sentence==null) {
				return;
			}

			Graphics2D g = (Graphics2D) graphics;
			Color c = g.getColor();

			int y = 0;

			if(showCurvePreview) {
				antiAliasingType.apply(g);

				g.setColor(curveColor);

				int h = curveHeight;

				float minD = sentenceInfo.getMinD();
				float maxD = sentenceInfo.getMaxD();
				float scaleY = h/(maxD-minD);

				for(int wordIndex=0; wordIndex<sentenceInfo.wordCount(); wordIndex++) {
					WordInfo wordInfo = sentenceInfo.wordInfo(wordIndex);

					final int wx = wordInfo.getX();

					// Paint curve preview
					for(int sylIndex=0; sylIndex<wordInfo.sylCount(); sylIndex++) {
						SyllableInfo sylInfo = wordInfo.syllableInfo(sylIndex);

						// Ignore syllables with alignment outside bounds
						if(leftSyllableBound!=rightSyllableBound
								&& (sylInfo.getB()<leftSyllableBound || sylInfo.getB()>rightSyllableBound)) {
							continue;
						}

						int x0 = wx + sylInfo.getX();
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
				}

				y += curveHeight;
			}

			g.setFont(textFont);

			FontMetrics fm = g.getFontMetrics();

			y += fm.getAscent();

			for(int wordIndex=0; wordIndex<sentenceInfo.wordCount(); wordIndex++) {
				WordInfo wordInfo = sentenceInfo.wordInfo(wordIndex);

//				System.out.printf("form=%s x=%d y=%d\n",
//						wordInfo.getLines(), wordInfo.getX(), y);

				// Paint text
				Color wordCol = (Color) wordInfo.getProperty(COL_KEY);
				if(wordCol!=null) {

					if(wordInfo.hasSyllables()) {
						int x = wordInfo.getX();

						for(SyllableInfo sylInfo : wordInfo.getSyllables()) {

							Color sylCol = (Color) sylInfo.getProperty(COL_KEY);
							if(sylCol==null) {
								sylCol = textColor;
							}

							g.setColor(sylCol);
							g.drawString(sylInfo.getLabel(), x, y);

							x += sylInfo.getWidth();
						}
					} else {
						g.setColor(wordCol);
						g.drawString(wordInfo.getLabel(), wordInfo.getX(), y);
					}
				} else {
					g.setColor(textColor);
					g.drawString(wordInfo.getLabel(), wordInfo.getX(), y);
				}

			}

			g.setColor(c);
		}
	}
}
