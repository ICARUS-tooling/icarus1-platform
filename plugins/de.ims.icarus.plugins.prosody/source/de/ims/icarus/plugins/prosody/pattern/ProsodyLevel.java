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
package de.ims.icarus.plugins.prosody.pattern;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum ProsodyLevel {
	SYLLABLE("syl"), //$NON-NLS-1$
	WORD("word"), //$NON-NLS-1$
	SENTENCE("sent"), //$NON-NLS-1$
	DOCUMENT("doc"), //$NON-NLS-1$
	ENVIRONMENT("env"), //$NON-NLS-1$
	;

	private final String token;

	ProsodyLevel(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	private static Map<String, ProsodyLevel> tokenMap;

	public static ProsodyLevel parseLevel(String s) {
		if(tokenMap==null) {
			Map<String, ProsodyLevel> map = new HashMap<>();

			for(ProsodyLevel level : values()) {
				map.put(level.getToken(), level);
			}

			tokenMap = map;
		}

		return tokenMap.get(s);
	}
}