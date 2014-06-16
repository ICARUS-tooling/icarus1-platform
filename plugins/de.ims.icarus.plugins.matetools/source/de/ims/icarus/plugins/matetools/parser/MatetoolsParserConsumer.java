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
package de.ims.icarus.plugins.matetools.parser;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.plugins.core.IcarusFrame;
import de.ims.icarus.plugins.matetools.MatetoolsConstants;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.transfer.DefaultConsumer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MatetoolsParserConsumer extends DefaultConsumer {


	/**
	 * @see de.ims.icarus.util.transfer.Consumer#process(java.lang.Object, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void process(Object data, Object source, Options options)
			throws Exception {

		String text = null;

		if(data instanceof SentenceData) {
			SentenceData sd = (SentenceData) data;
			text = LanguageUtils.combine(sd);
		} else if(data instanceof String) {
			text = (String) data;
		} else
			throw new IllegalArgumentException("Unsupported data: "+data); //$NON-NLS-1$

		//TODO maybe apply some options like delimiters etc?

		if(text.isEmpty()) {
			return;
		}

		IcarusFrame frame = IcarusFrame.getActiveFrame();
		MatetoolsParserPerspective perspective = (MatetoolsParserPerspective) frame.getCurrentPerspective();
		MatetoolsParserInputView view = (MatetoolsParserInputView) perspective.getView(MatetoolsConstants.MATETOOLS_PARSER_INPUT_VIEW_ID);

		view.setText(text);
	}

	/**
	 * @see de.ims.icarus.util.transfer.Consumer#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType contentType) {
		return ContentTypeRegistry.isCompatible("SentenceDataContentType", contentType) //$NON-NLS-1$
				|| ContentTypeRegistry.isCompatible("StringContentType", contentType); //$NON-NLS-1$
	}

}
