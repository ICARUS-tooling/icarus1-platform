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
package de.ims.icarus.plugins.prosody;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;

import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyUtils {

	private static Cursor speakerCursor;

	public static Cursor getSpeakerCursor() {
		if(speakerCursor==null) {
			URL url = ProsodyUtils.class.getResource("speaker.png"); //$NON-NLS-1$
			ImageIcon source = new ImageIcon(url);

			Dimension size = Toolkit.getDefaultToolkit().getBestCursorSize(0, 0);

			BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics.setColor(new Color(0, true));
			graphics.fillRect(0, 0, size.width, size.height);
			graphics.drawImage(source.getImage(), 0, 0, null);

			speakerCursor = Toolkit.getDefaultToolkit().createCustomCursor(
					image, new Point(0, 0), "speaker"); //$NON-NLS-1$
		}
		return speakerCursor;
	}

	public static ContentType getProsodyDocumentContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(ProsodicDocumentData.class);
	}

	public static ContentType getProsodySentenceContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(ProsodicSentenceData.class);
	}
}
