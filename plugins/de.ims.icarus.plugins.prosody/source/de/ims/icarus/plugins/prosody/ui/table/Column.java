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
package de.ims.icarus.plugins.prosody.ui.table;

import java.util.Locale;

import javax.swing.Icon;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.ui.view.SyllableInfo;
import de.ims.icarus.plugins.prosody.ui.view.WordInfo;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.treetable.TreeTableModel;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class Column implements Identity, ProsodyConstants, LanguageConstants {

	private final String key;

	protected Column(String key) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$

		this.key = key;
	}

	public Class<?> getColumnClass() {
		return String.class;
	}

	@Override
	public String getId() {
		return key;
	}

	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.prosody.prosodyTableColumns."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.prosody.prosodyTableColumns."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public Object getOwner() {
		return this;
	}

	public abstract Object getValue(WordInfo wordInfo);

	public abstract Object getValue(SyllableInfo syllableInfo);

	public static class PropertyColumn extends Column {

		private final String wordProperty;
		private final String syllableProperty;

		public PropertyColumn(String wordProperty, String syllableProperty) {
			super("property"); //$NON-NLS-1$

			if(wordProperty==null && syllableProperty==null)
				throw new IllegalArgumentException("At least one of either word or syllable property key must be non-null!"); //$NON-NLS-1$

			this.wordProperty = wordProperty;
			this.syllableProperty = syllableProperty;
		}

		@Override
		public String getName() {
			String label = wordProperty;

			if(label==null) {
				label = syllableProperty;
			} else if(syllableProperty!=null) {
				label += "/"+syllableProperty; //$NON-NLS-1$
			}

			return label;
		}

		@Override
		public String getDescription() {
			return super.getDescription();
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.ui.table.Column#getValue(de.ims.icarus.plugins.prosody.ui.view.WordInfo)
		 */
		@Override
		public Object getValue(WordInfo wordInfo) {
			return wordProperty==null ? null : wordInfo.getBaseProperty(wordProperty);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.ui.table.Column#getValue(de.ims.icarus.plugins.prosody.ui.view.SyllableInfo)
		 */
		@Override
		public Object getValue(SyllableInfo syllableInfo) {
			return syllableProperty==null ? null : syllableInfo.getBaseProperty(syllableProperty);
		}

	}

	public static final Column labelColumn = new Column("label") { //$NON-NLS-1$

		@Override
		public Object getValue(SyllableInfo syllableInfo) {
			return syllableInfo.getLabel();
		}

		@Override
		public Object getValue(WordInfo wordInfo) {
			return wordInfo.getLabel();
		}
	};

	public static final Column rootColumn = new Column("root") { //$NON-NLS-1$

		@Override
		public Object getValue(SyllableInfo syllableInfo) {
			return String.valueOf(syllableInfo.getSylIndex()+1);
		}

		@Override
		public Object getValue(WordInfo wordInfo) {
			String label = String.valueOf(wordInfo.getWordIndex()+1);

			if(wordInfo.hasSyllables()) {
				label += "  ["+wordInfo.sylCount()+"]"; //$NON-NLS-1$ //$NON-NLS-2$
			}

			return label;
		}

		@Override
		public Class<?> getColumnClass() {
			return TreeTableModel.class;
		}
	};

	private static String formatFloat(float f) {
		return String.format(Locale.ENGLISH, "%.02f", f); //$NON-NLS-1$
	}

	public static final Column durationColumn = new Column("duration") { //$NON-NLS-1$

		@Override
		public Object getValue(SyllableInfo syllableInfo) {
			WordInfo wordInfo = syllableInfo.getWordInfo();
			ProsodicSentenceData sentence = wordInfo.getSentenceInfo().getSentence();
			float duration = sentence.getSyllableDuration(wordInfo.getWordIndex(), syllableInfo.getSylIndex());
			return duration==DATA_UNDEFINED_VALUE ? null : formatFloat(duration);
		}

		@Override
		public Object getValue(WordInfo wordInfo) {
			ProsodicSentenceData sentence = wordInfo.getSentenceInfo().getSentence();
			float beginTs = sentence.getBeginTimestamp(wordInfo.getWordIndex());
			float endTs = sentence.getEndTimestamp(wordInfo.getWordIndex());

			if(beginTs==DATA_UNDEFINED_VALUE || endTs==DATA_UNDEFINED_VALUE) {
				return null;
			}

			return formatFloat(endTs-beginTs);
		}
	};
}
