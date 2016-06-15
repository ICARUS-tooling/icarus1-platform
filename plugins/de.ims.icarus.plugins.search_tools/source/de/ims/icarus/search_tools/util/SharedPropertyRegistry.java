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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SharedPropertyRegistry implements LanguageConstants {

	public static final Object SYLLABLE_LEVEL = "syllable"; //$NON-NLS-1$
	public static final Object WORD_LEVEL = "word"; //$NON-NLS-1$
	public static final Object SPAN_LEVEL = "span"; //$NON-NLS-1$
	public static final Object SENTENCE_LEVEL = "sentence"; //$NON-NLS-1$
	public static final Object DOCUMENT_LEVEL = "document"; //$NON-NLS-1$
	public static final Object ENVIRONMENT_LEVEL = "environment"; //$NON-NLS-1$

	private static final Map<String, ValueHandler> handlers = new HashMap<>();
	private static final Map<ContentType, Set<String>> typedSpecifiers = new HashMap<>();
	private static final Map<Object, Set<String>> levelSpecificSpecifiers = new HashMap<>();

	private static final Set<String> generalSpecifiers = new HashSet<>();

	private static <E extends Object> Set<String> set(Map<E, Set<String>> map, E key) {
		Set<String> result = map.get(key);
		if(result==null) {
			result = new HashSet<>();
			map.put(key, result);
		}
		return result;
	}

	public static boolean registerHandler(String specifier, ValueHandler handler, ContentType contentType, Object level) {
		if(contentType!=null) {
			set(typedSpecifiers, contentType).add(specifier);
		}
		if(level!=null) {
			set(levelSpecificSpecifiers, level).add(specifier);
		} else {
			generalSpecifiers.add(specifier);
		}

		return handlers.put(specifier, handler)==null;
	}

	public static ValueHandler getHandler(Object specifier) {
		if(specifier==null) {
			return ValueHandler.stringHandler;
		}

		ValueHandler handler = handlers.get(specifier);
		return handler==null ? ValueHandler.stringHandler : handler;
	}

	private static final String[] EMPTY_SPECIFIERS = {};

	public static final int INCLUDE_GENERAL_LEVEL = (1<<0);
	public static final int INCLUDE_COMPATIBLE_TYPES = (1<<1);

	public static String[] getSpecifiers(Object level, ContentType...types) {
		return getSpecifiers(level, 0, types);
	}

	public static String[] getSpecifiers(Object level, int flags, ContentType...types) {

		Set<String> buffer = new HashSet<>();

		if((flags & INCLUDE_COMPATIBLE_TYPES)==INCLUDE_COMPATIBLE_TYPES) {
			for(Entry<ContentType, Set<String>> entry : typedSpecifiers.entrySet()) {
				for(ContentType type : types) {
					if(ContentTypeRegistry.isCompatible(type, entry.getKey())) {
						buffer.addAll(entry.getValue());
					}
				}
			}
		} else {
			for(ContentType type : types) {
				Set<String> specifiers = typedSpecifiers.get(type);
				if(specifiers!=null) {
					buffer.addAll(specifiers);
				}
			}
		}

		if(level!=null) {
			Set<String> filter = levelSpecificSpecifiers.get(level);
			if(filter!=null) {
				buffer.retainAll(filter);
			}
		}

		if((flags & INCLUDE_GENERAL_LEVEL)==INCLUDE_GENERAL_LEVEL) {
			buffer.addAll(generalSpecifiers);
		}

		String[] result = EMPTY_SPECIFIERS;
		if(!buffer.isEmpty()) {
			result = new String[buffer.size()];
			buffer.toArray(result);
			Arrays.sort(result);
		}
		return result;
	}

	static {

		// General level
		SharedPropertyRegistry.registerHandler(NUMBER_KEY, ValueHandler.stringHandler, null, null);
		SharedPropertyRegistry.registerHandler(GENDER_KEY, ValueHandler.stringHandler, null, null);
		SharedPropertyRegistry.registerHandler(SIZE_KEY, ValueHandler.integerHandler, null, null);
		SharedPropertyRegistry.registerHandler(LENGTH_KEY, ValueHandler.integerHandler, null, null);
		SharedPropertyRegistry.registerHandler(INDEX_KEY, ValueHandler.integerHandler, null, null);
		SharedPropertyRegistry.registerHandler(ID_KEY, ValueHandler.stringHandler, null, null);

		// Word level
		ContentType contentType = LanguageUtils.getSentenceDataContentType();
		SharedPropertyRegistry.registerHandler(FORM_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(TAG_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(PARSE_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(LEMMA_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(SENSE_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(ENTITY_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(FRAMESET_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
		SharedPropertyRegistry.registerHandler(SPEAKER_KEY, ValueHandler.stringHandler, contentType, WORD_LEVEL);
	}
}
