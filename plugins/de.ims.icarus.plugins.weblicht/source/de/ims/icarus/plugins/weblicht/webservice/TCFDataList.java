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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.weblicht.webservice;

import java.util.HashMap;
import java.util.Map;

import javax.swing.event.ChangeListener;

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.Grammar;
import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.language.dependency.DependencySentenceData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.util.data.ContentType;
import de.tuebingen.uni.sfs.wlf1.io.TextCorpusStreamed;
import de.tuebingen.uni.sfs.wlf1.tc.api.Dependency;
import de.tuebingen.uni.sfs.wlf1.tc.api.Feature;
import de.tuebingen.uni.sfs.wlf1.tc.api.Sentence;
import de.tuebingen.uni.sfs.wlf1.tc.api.Token;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class TCFDataList implements SentenceDataList {

	protected TextCorpusStreamed tcs;
	protected int index;

	Map<Integer, TCFSentenceData> tcfMapCache;

	public TCFDataList(TextCorpusStreamed tcs) {
		if (tcs == null)
			throw new NullPointerException("Invalid TCF"); //$NON-NLS-1$
		setTextCorpusStream(tcs);

	}

	void setTextCorpusStream(TextCorpusStreamed tcs) {
		if (this.tcs != null) {
			return;
		}
		tcfMapCache = new HashMap<Integer, TCFSentenceData>();
		this.tcs = tcs;

	}

	private TextCorpusStreamed getTextCorpusStream() {
		return tcs;
	}

	protected String ensureValid(String input) {
		return input == null ? "" : input; //$NON-NLS-1$
	}

	protected String ensureDummy(String input, String dummy) {
		return input == null ? dummy : input;
	}

	private SentenceData getTCFDataFromIndex(int index) {
		if (!tcfMapCache.containsKey(index)) {
			TCFSentenceData tcfData = new TCFSentenceData(index);
			tcfMapCache.put(index, tcfData);
		}
		return tcfMapCache.get(index);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		return getTextCorpusStream().getSentencesLayer().size();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#get(int)
	 */
	@Override
	public SentenceData get(int index) {
		return get(index, DataType.SYSTEM);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return DependencyUtils.getDependencyContentType();

	}

	/**
	 * @see de.ims.icarus.util.data.DataList#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.util.data.DataList#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener listener) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#supportsType(de.ims.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		return type == DataType.SYSTEM;
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int,
	 *      de.ims.icarus.language.DataType)
	 */
	@Override
	public SentenceData get(int index, DataType type) {
		if (type != DataType.SYSTEM) {
			return null;
		}
		// return tcs == null ? null : getSentenceDataFromIndex(index);
		return tcs == null ? null : getTCFDataFromIndex(index);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int,
	 *      de.ims.icarus.language.DataType,
	 *      de.ims.icarus.language.AvailabilityObserver)
	 */
	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		return get(index, type);
	}

	private class TCFSentenceData implements DependencySentenceData {

		private static final long serialVersionUID = -1706328677253900802L;

		private int sentenceIndex;
		private Sentence sentence;
		private long[] flags;
		private Token[] token;
		private String[] forms;

		public TCFSentenceData(int index) {

			// Get Sentence Layer
			sentence = tcs.getSentencesLayer().getSentence(index);
			// Extract Tokens from Sentence Layer
			token = tcs.getSentencesLayer().getTokens(sentence);
			int size = token.length;

			short[] heads = new short[size];
			flags = new long[size];
			forms = new String[size];
			for(int i = 0; i < size; i++){
				heads[i] = (short) getHead(i);
				forms[i] = getForm(i);
			}
			sentenceIndex = index;

			DependencyUtils.fillProjectivityFlags(heads, flags);

		}

		@Override
		public TCFSentenceData clone() {
			return this;
		}

		/**
		 * @see de.ims.icarus.language.SentenceData#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			return token == null || token.length == 0;
		}

		/**
		 * @see de.ims.icarus.language.SentenceData#length()
		 */
		@Override
		public int length() {
			return token.length;
		}

		/**
		 * @see de.ims.icarus.language.SentenceData#getSourceGrammar()
		 */
		@Override
		public Grammar getSourceGrammar() {
			return DependencyUtils.getDependencyGrammar();
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencySentenceData#getForm(int)
		 */
		@Override
		public String getForm(int index) {
			return ensureDummy(token[index].getString(), "<empty>"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencySentenceData#getPos(int)
		 */
		@Override
		public String getPos(int index) {
			if (tcs.getPosTagsLayer() != null) {
				return ensureValid(tcs.getPosTagsLayer().getTag(token[index])
						.getString());
			} else {
				return ""; //$NON-NLS-1$
			}
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencySentenceData#getRelation(int)
		 */
		@Override
		public String getRelation(int index) {



			// dependency stuff
			if (tcs.getDependencyParsingLayer() != null) {

				Dependency[] dep = tcs.getDependencyParsingLayer()
						.getParse(sentenceIndex).getDependencies();


				//workaround for wrong index, recalculate to be save
				// see getHead for more information
				boolean undefinedVal = true;
				for (int i = 0; i < dep.length; i++) {
					Token[] tmpTok = tcs.getDependencyParsingLayer()
							.getDependentTokens(dep[i]);
					Sentence toksentence = tcs.getSentencesLayer().getSentence(
							tmpTok[0]);
					Token[] tokenOffset = tcs.getSentencesLayer().getTokens(							toksentence);

					if ((tmpTok[0].getOrder()-tokenOffset[0].getOrder()) == index) {
						index = i;
						undefinedVal = false;
					}
				}
				if (undefinedVal) {
					return "";
				}

				return ensureValid(dep[index].getFunction());
			} else {
				return ""; //$NON-NLS-1$
			}
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencySentenceData#getLemma(int)
		 */
		@Override
		public String getLemma(int index) {
			if (tcs.getLemmasLayer() != null) {
				return ensureValid(tcs.getLemmasLayer().getLemma(token[index])
						.getString());
			} else {
				return ""; //$NON-NLS-1$
			}
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencySentenceData#getFeatures(int)
		 */
		@Override
		public String getFeatures(int index) {
			String morphfeatures = ""; //$NON-NLS-1$
			if (tcs.getMorphologyLayer() != null) {
				if (tcs.getMorphologyLayer().getAnalysis(token[index]) != null) {
					Feature feature[] = tcs.getMorphologyLayer()
							.getAnalysis(token[index]).getFeatures();
					StringBuilder sb = new StringBuilder(feature.length * 30);
					for (int f = 0; f < feature.length; f++) {
						// Only Extract Values of Features ?
						sb.append(feature[f].getName()).append("=") //$NON-NLS-1$
								.append(feature[f].getValue());
						if (f < feature.length - 1)
							sb.append("|"); //$NON-NLS-1$
					}
					morphfeatures = sb.toString();
				}
			}
			return ensureValid(morphfeatures);
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencySentenceData#getHead(int)
		 */
		@Override
		public int getHead(int index) {
			if (tcs.getDependencyParsingLayer() != null) {

				Dependency[] dep4 = tcs.getDependencyParsingLayer()
						.getParse(sentenceIndex).getDependencies();


				/* wrong index check if appropiate candidate in dep list,
				 * some parser output may not recognize all tokens
				 *
				 * workaround for this issue: we check everytime if the dependency
				 * arraylist has a token with the current index (tmpTok[0].getOrder)
				 * if we a match we just have to set the correct (new) index, otherwise
				 * we assume that the data has an undefined value.
				 */
				boolean undefinedVal = true;
				for (int i = 0; i < dep4.length; i++) {
					Token[] tmpTok = tcs.getDependencyParsingLayer()
							.getDependentTokens(dep4[i]);

					//again offset numbercrunching
					Sentence toksentence = tcs.getSentencesLayer().getSentence(
							tmpTok[0]);
					Token[] tokenOffset = tcs.getSentencesLayer().getTokens(							toksentence);

					if ((tmpTok[0].getOrder()-tokenOffset[0].getOrder()) == index) {
						index = i;
						undefinedVal = false;
					}
				}

				if (undefinedVal) {
					return LanguageConstants.DATA_UNDEFINED_VALUE;
				}

				// check if dependent or root
				if (tcs.getDependencyParsingLayer().getGovernorTokens(
						dep4[index]) != null) {

					// System.out.println("is no root" + index);
					Token[] tSource = tcs.getDependencyParsingLayer()
							.getGovernorTokens(dep4[index]);


					/*
					 * workaround, we count dependency per sentence in our
					 * simple dependency data, tcf use global counting for
					 * dependencys: tSource[0].getOrder() -
					 * tokenOffset[0].getOrder will give us the correct index
					 */
					Sentence toksentence = tcs.getSentencesLayer().getSentence(
							tSource[0]);
					Token[] tokenOffset = tcs.getSentencesLayer().getTokens(
							toksentence);
//					 System.out.println("govID " + tSource[0].getOrder()
//					 + " Offset " + tokenOffset[0].getOrder());

					return (short) (tSource[0].getOrder() - tokenOffset[0]
							.getOrder());

				} else {
					// is root node
					// System.out.println("is root " + index);
					return LanguageConstants.DATA_HEAD_ROOT;
				}
			}
			// default value when sentence invalid
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencySentenceData#isFlagSet(int,
		 *      long)
		 */
		@Override
		public boolean isFlagSet(int index, long flag) {
			return flags != null && (flags[index] & flag) == flag;
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencySentenceData#getFlags(int)
		 */
		@Override
		public long getFlags(int index) {
			return flags == null ? 0 : flags[index];
		}

		/**
		 * @see de.ims.icarus.ui.text.TextItem#getText()
		 */
		@Override
		public String getText() {
			return LanguageUtils.combine(this);
		}

		/**
		 * @see de.ims.icarus.language.SentenceData#getIndex()
		 */
		@Override
		public int getIndex() {
			return sentenceIndex;
		}

		/**
		 * @see de.ims.icarus.language.SentenceData#getProperty(int, java.lang.String)
		 */
		@Override
		public Object getProperty(int index, String key) {
			return null;
		}

		/**
		 * @see de.ims.icarus.language.SentenceData#getProperty(java.lang.String)
		 */
		@Override
		public Object getProperty(String key) {
			switch (key) {
			case LanguageConstants.INDEX_KEY: return getIndex();
			}

			return null;
		}

	}

}
