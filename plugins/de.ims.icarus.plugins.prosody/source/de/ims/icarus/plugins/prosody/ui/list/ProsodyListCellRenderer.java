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
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ui.geom.AntiAliasingType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 8194402816943023015L;

	private static final int DEFAULT_HEIGHT = 15;

	private ProsodicSentenceData sentence;
	private float minD, maxD;
	private int textWidth = 1;

	private Icon curveIcon = new Icon() {

		@Override
		public void paintIcon(Component comp, Graphics g, int x, int y) {

			Color c = g.getColor();

			g.setColor(Color.black);

			AntiAliasingType.DEFAULT.apply((Graphics2D) g);
			FontMetrics fm = g.getFontMetrics();
			int height = getIconHeight();
			float scaleY = height/(maxD-minD);

			for(int i=0; i<sentence.length(); i++) {
				if(i>0) {
					x += fm.charWidth(' ');
				}

				String token = sentence.getForm(i);
				int tokenLength = fm.stringWidth(token);
				int numSyls = sentence.getSyllableCount(i);

//				System.out.printf("token=%s sylCount=%d x=%d width=%d\n",
//						token, numSyls, x, fm.stringWidth(token));

				int begin = x;

				for(int k =0; k<numSyls; k++) {
					int sylWidth;
					if(sentence.isMapsSyllables()) {
						int offset0 = sentence.getSyllableOffset(i, k);
						int offset1 = k<numSyls-1 ? sentence.getSyllableOffset(i, k+1) : token.length();
						sylWidth = fm.stringWidth(token.substring(offset0, offset1));
					} else {
						float beginTs = sentence.getBeginTimestamp(i);
						float duration = sentence.getEndTimestamp(i)-beginTs;
						float sylDuration = sentence.getSyllableDuration(i, k);
						sylWidth = (int)(tokenLength * sylDuration/duration);
					}

					float b = sentence.getPainteB(i, k);

					// Ignore peaks outside the current syllable
					if(b>=0F && b<=1F) {

						float d = sentence.getPainteD(i, k);
						float c1 = sentence.getPainteC1(i, k);
						float c2 = sentence.getPainteC2(i, k);

						int x0 = begin;
						int x2 = x0+sylWidth;
						int x1 = (x2+x0)>>1;

						int y0 = y + height - (int)((d-c1-minD) * scaleY);
						int y1 = y + height - (int)((d-minD) * scaleY);
						int y2 = y + height - (int)((d-c2-minD) * scaleY);

//						System.out.printf("syl=%s y0=%d y1=%d y2=%d x0=%d x1=%d x2=%d\n",
//								sylToken, y0, y1, y2, x0, x1, x2);

						if(y0==y1 && y1==y2) {
							g.drawLine(x0, y0, x2, y2);
						} else {
							g.drawLine(x0, y0, x1, y1);
							g.drawLine(x1, y1, x2, y2);
						}
					}

					begin += sylWidth;
				}

				x += tokenLength;
			}

			g.setColor(c);
		}

		@Override
		public int getIconWidth() {
			return textWidth;
		}

		@Override
		public int getIconHeight() {
			return DEFAULT_HEIGHT;
		}
	};

	public ProsodyListCellRenderer() {
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(TOP);
		setVerticalTextPosition(BOTTOM);
		setHorizontalTextPosition(CENTER);
	}


	/**
	 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		sentence = (ProsodicSentenceData) value;

		String label = LanguageUtils.combine(sentence);
		if(label==null) {
			label = ""; //$NON-NLS-1$
		}

		super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);

		float minD = Float.MAX_VALUE;
		float maxD = Float.MIN_VALUE;

		// First pass, calc min and max D values
		for(int i=0; i<sentence.length(); i++) {
			for(int k =0; k<sentence.getSyllableCount(i); k++) {
				float d = sentence.getPainteD(i, k);
				float c1 = sentence.getPainteC1(i, k);
				float c2 = sentence.getPainteC2(i, k);
				maxD = Math.max(maxD, d);
				minD = Math.min(minD, d-Math.max(c1, c2));
			}
		}

		this.minD = minD;
		this.maxD = maxD;

		textWidth = getFontMetrics(getFont()).stringWidth(label);

		setText(label);
		setIcon(curveIcon);

//		System.out.println(getPreferredSize());

		return this;
	}
}
