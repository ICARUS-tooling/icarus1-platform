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

 * $Revision: 389 $
 * $Date: 2015-04-23 12:19:15 +0200 (Do, 23 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.prosody/source/de/ims/icarus/plugins/prosody/pattern/ExtractionLevel.java $
 *
 * $LastChangedDate: 2015-04-23 12:19:15 +0200 (Do, 23 Apr 2015) $
 * $LastChangedRevision: 389 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.coref.pattern;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.id.Identity;

/**
 *
 * @author Markus Gärtner
 * @version $Id: ExtractionLevel.java 389 2015-04-23 10:19:15Z mcgaerty $
 *
 */
public enum CorefLevel implements Identity {
	WORD("word", "word") { //$NON-NLS-1$ //$NON-NLS-2$
		@Override
		public String[] getAvailableProperties() {
			return CoreferenceUtils.getDefaultWordPropertyKeys();
		}
		@Override
		public CorefLevel up() { return SENTENCE; }
	},
	SPAN("span", "span") { //$NON-NLS-1$ //$NON-NLS-2$
		@Override
		public String[] getAvailableProperties() {
			return CoreferenceUtils.getDefaultSpanPropertyKeys();
		}

		@Override
		public CorefLevel up() { return SENTENCE; }
	},
	EDGE("edge", "edge") { //$NON-NLS-1$ //$NON-NLS-2$
		@Override
		public String[] getAvailableProperties() {
			return CoreferenceUtils.getDefaultEdgePropertyKeys();
		}

		@Override
		public CorefLevel down() { return WORD; }
	},
	SENTENCE("sent", "sentence") { //$NON-NLS-1$ //$NON-NLS-2$
		@Override
		public String[] getAvailableProperties() {
			return CoreferenceUtils.getDefaultSentencePropertyKeys();
		}

		@Override
		public CorefLevel down() { return WORD; }
	},
	DOCUMENT("doc", "document") { //$NON-NLS-1$ //$NON-NLS-2$
		@Override
		public String[] getAvailableProperties() {
			return CoreferenceUtils.getDefaultDocumentPropertyKeys();
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

	public static int dif(CorefLevel from, CorefLevel to) {
		int result = dif0(from, to);
		if(result==-1) {
			result = -dif0(to, from);
		}
		return result;
	}

	private static int dif0(CorefLevel from, CorefLevel to) {
		int dif = -1;

		CorefLevel parent = from;
		while(true) {
			parent = parent.up();

			if(parent==null) {
				break;
			}

			dif++;

			if(parent==to) {
				return dif;
			}
		}

		return -1;
	}

	private final String token;
	private final String key;

	CorefLevel(String token, String key) {
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

	private static volatile Map<String, CorefLevel> tokenMap;

	public static CorefLevel parseLevel(String s) {
		if(tokenMap==null) {
			Map<String, CorefLevel> map = new HashMap<>();

			for(CorefLevel level : values()) {
				map.put(level.getToken(), level);
			}

			tokenMap = map;
		}

		return tokenMap.get(s);
	}

	private static final CorefLevel[] values = values();

	private CorefLevel forOrdinal(int index) {
		return (index>=0 && index<values.length) ? values[index] : null;
	}

	public CorefLevel up() {
		return forOrdinal(ordinal()+1);
	}

	public CorefLevel down() {
		return forOrdinal(ordinal()-1);
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
				"plugins.coref.pattern."+token+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.coref.pattern."+token+".description"); //$NON-NLS-1$ //$NON-NLS-2$
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