/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.weblicht.webservice;

import java.util.HashMap;
import java.util.Map;

import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.language.AvailabilityObserver;
import net.ikarus_systems.icarus.language.DataType;
import net.ikarus_systems.icarus.language.Grammar;
import net.ikarus_systems.icarus.language.LanguageConstants;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.util.data.ContentType;
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
			throw new IllegalArgumentException("Invalid TCF"); //$NON-NLS-1$
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
	 * @see net.ikarus_systems.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		return getTextCorpusStream().getSentencesLayer().size();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#get(int)
	 */
	@Override
	public SentenceData get(int index) {
		return get(index, DataType.SYSTEM);
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return DependencyUtils.getDependencyContentType();

	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener listener) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#supportsType(net.ikarus_systems.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		return type == DataType.SYSTEM;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int,
	 *      net.ikarus_systems.icarus.language.DataType)
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
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int,
	 *      net.ikarus_systems.icarus.language.DataType,
	 *      net.ikarus_systems.icarus.language.AvailabilityObserver)
	 */
	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		return get(index, type);
	}

	private class TCFSentenceData implements DependencyData {

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
			this.sentenceIndex = index;

			DependencyUtils.fillProjectivityFlags(heads, flags);

		}

		@Override
		public TCFSentenceData clone() {
			return this;
		}

		/**
		 * @see net.ikarus_systems.icarus.language.SentenceData#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			return token == null || token.length == 0;
		}

		/**
		 * @see net.ikarus_systems.icarus.language.SentenceData#length()
		 */
		@Override
		public int length() {
			return token.length;
		}

		/**
		 * @see net.ikarus_systems.icarus.language.SentenceData#getSourceGrammar()
		 */
		@Override
		public Grammar getSourceGrammar() {
			return DependencyUtils.getDependencyGrammar();
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getForm(int)
		 */
		@Override
		public String getForm(int index) {
			return ensureDummy(token[index].getString(), "<empty>"); //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getPos(int)
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
		 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getRelation(int)
		 */
		@Override
		public String getRelation(int index) {
			
			// dependency stuff
			if (tcs.getDependencyParsingLayer() != null) {
				Dependency[] dep = tcs.getDependencyParsingLayer()
						.getParse(sentenceIndex).getDependencies();
				return ensureValid(dep[index].getFunction());
			} else {
				return ""; //$NON-NLS-1$
			}
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getLemma(int)
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
		 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getFeatures(int)
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
		 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getHead(int)
		 */
		@Override
		public int getHead(int index) {
			if (tcs.getDependencyParsingLayer() != null) {
				Dependency[] dep4 = tcs.getDependencyParsingLayer()
						.getParse(sentenceIndex).getDependencies();

				// check if dependent or root
				if (tcs.getDependencyParsingLayer().getGovernorTokens(
						dep4[index]) != null) {
					//System.out.println("is no root" + index);
					
					Token[] tSource = tcs.getDependencyParsingLayer()
							.getGovernorTokens(dep4[index]);

					/*
					 * workaround, we count dependency per sentence in our
					 * simple dependency data, tcf use global counting for
					 * dependencys: tSource[0].getOrder() - tokenOffset[0].getOrder
					 * will give us the correct index
					 */
					Sentence toksentence = tcs.getSentencesLayer().getSentence(
							tSource[0]);
					Token[] tokenOffset = tcs.getSentencesLayer().getTokens(
							toksentence);
					// System.out.println(st[0].getOrder());

					return (short) (tSource[0].getOrder() - tokenOffset[0]
							.getOrder());

				} else {
					// is root node
					//System.out.println("is root " + index);
					return LanguageConstants.DATA_HEAD_ROOT;
				}				
			} else {
				// default value when sentence invalid
				return LanguageConstants.DATA_UNDEFINED_VALUE;
			}

		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#isFlagSet(int,
		 *      long)
		 */
		@Override
		public boolean isFlagSet(int index, long flag) {
			return flags != null && (flags[index] & flag) == flag;
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getFlags(int)
		 */
		@Override
		public long getFlags(int index) {
			return flags == null ? 0 : flags[index];
		}

	}

}