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
package de.ims.icarus.search_tools.util;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.language.LanguageConstants;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SharedPropertyRegistry implements LanguageConstants {

	private static Map<Object, ValueHandler> handlers = new HashMap<>();

	public static boolean registerHandler(Object specifier, ValueHandler handler) {
		return handlers.put(specifier, handler)==null;
	}

	public static ValueHandler getHandler(Object specifier) {
		if(specifier==null) {
			return ValueHandler.stringHandler;
		}

		ValueHandler handler = handlers.get(specifier);
		return handler==null ? ValueHandler.stringHandler : handler;
	}

	static {

		// General level
		SharedPropertyRegistry.registerHandler(NUMBER_KEY, ValueHandler.stringHandler);
		SharedPropertyRegistry.registerHandler(GENDER_KEY, ValueHandler.stringHandler);
		SharedPropertyRegistry.registerHandler(SIZE_KEY, ValueHandler.integerHandler);
		SharedPropertyRegistry.registerHandler(LENGTH_KEY, ValueHandler.integerHandler);
		SharedPropertyRegistry.registerHandler(INDEX_KEY, ValueHandler.integerHandler);
		SharedPropertyRegistry.registerHandler(ID_KEY, ValueHandler.stringHandler);

		// Word level
		SharedPropertyRegistry.registerHandler(FORM_KEY, ValueHandler.stringHandler);
		SharedPropertyRegistry.registerHandler(TAG_KEY, ValueHandler.stringHandler);
		SharedPropertyRegistry.registerHandler(PARSE_KEY, ValueHandler.stringHandler);
		SharedPropertyRegistry.registerHandler(LEMMA_KEY, ValueHandler.stringHandler);
		SharedPropertyRegistry.registerHandler(SENSE_KEY, ValueHandler.stringHandler);
		SharedPropertyRegistry.registerHandler(ENTITY_KEY, ValueHandler.stringHandler);
		SharedPropertyRegistry.registerHandler(FRAMESET_KEY, ValueHandler.stringHandler);
		SharedPropertyRegistry.registerHandler(SPEAKER_KEY, ValueHandler.stringHandler);
	}
}
