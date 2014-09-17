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
package de.ims.icarus.plugins.prosody.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TextComponent extends JComponent {

	private static final long serialVersionUID = 1903935034549325101L;

	private TextArea textArea;
	private String[] lines;

	public TextComponent() {
		this(new TextArea());
	}

	public TextComponent(TextArea textArea) {
		setTextArea(textArea);
	}
	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Rectangle area = new Rectangle(getSize());
		textArea.paint(g, lines, area);
	}
	/**
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return textArea.getSize(this, lines);
	}
	/**
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	/**
	 * @return the textArea
	 */
	public TextArea getTextArea() {
		return textArea;
	}
	/**
	 * @return the lines
	 */
	public String[] getLines() {
		return lines;
	}
	/**
	 * @param textArea the textArea to set
	 */
	public void setTextArea(TextArea textArea) {
		if (textArea == null)
			throw new NullPointerException("Invalid textArea"); //$NON-NLS-1$

		this.textArea = textArea;

		revalidate();
		repaint();
	}
	/**
	 * @param lines the lines to set
	 */
	public void setLines(String[] lines) {
		this.lines = lines;

		revalidate();
		repaint();
	}


}
