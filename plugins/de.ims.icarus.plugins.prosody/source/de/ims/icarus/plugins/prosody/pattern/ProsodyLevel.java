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

import javax.swing.Icon;

import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.id.Identity;

/**
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum ProsodyLevel implements Identity {
	SYLLABLE("syl", "syllable") { //$NON-NLS-1$ //$NON-NLS-2$
		@Override
		public String[] getAvailableProperties() {
			return ProsodyUtils.getDefaultSyllablePropertyKeys();
		}
	},
	WORD("word", "word") { //$NON-NLS-1$ //$NON-NLS-2$
		@Override
		public String[] getAvailableProperties() {
			return ProsodyUtils.getDefaultWordPropertyKeys();
		}
	},
	SENTENCE("sent", "sentence") { //$NON-NLS-1$ //$NON-NLS-2$
		@Override
		public String[] getAvailableProperties() {
			return ProsodyUtils.getDefaultSentencePropertyKeys();
		}
	},
	DOCUMENT("doc", "document") { //$NON-NLS-1$ //$NON-NLS-2$
		@Override
		public String[] getAvailableProperties() {
			return ProsodyUtils.getDefaultDocumentPropertyKeys();
		}
	},
	ENVIRONMENT("env", "environment") { //$NON-NLS-1$ //$NON-NLS-2$
		/**
		 * Returns {@code null} since environmental properties are, well, ... environment specific.
		 * @see de.ims.icarus.plugins.prosody.pattern.ProsodyLevel#getAvailableProperties()
		 */
		@Override
		public String[] getAvailableProperties() {
			return null;
		}
	},
	;

	private final String token;
	private final String key;

	ProsodyLevel(String token, String key) {
		this.token = token;
		this.key = key;
	}

	public String getToken() {
		return token;
	}

	public String getKey() {
		return key;
	}

	public abstract String[] getAvailableProperties();

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

	private static ProsodyLevel[] values;

	public ProsodyLevel up() {
		if(values==null) {
			values = values();
		}

		return values[ordinal()+1];
	}

	public ProsodyLevel down() {
		if(values==null) {
			values = values();
		}

		return values[ordinal()-1];
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return token;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.prosody.pattern."+token+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.prosody.pattern."+token+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return null;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}
}