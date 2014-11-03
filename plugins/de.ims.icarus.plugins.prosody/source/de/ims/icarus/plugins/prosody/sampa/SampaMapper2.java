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
package de.ims.icarus.plugins.prosody.sampa;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TCharSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TCharHashSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus.io.IOUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SampaMapper2 {

//	public static void main(String[] args) throws Exception {
////		SampaMapper mapper = getInstance();
////		String[] syllables = mapper.split0("Einschüchterung", "aIn|SYC|t@|RUN".split("\\|")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
////		System.out.println(Arrays.deepToString(syllables));
//
//		Core.debugInit(args);
//		ProsodyIOUtils.DEFAULT_SYLLABLES_FROM_SAMPA = true;
//
//		LoggerFactory.registerLogFile("de.ims.icarus.plugins.prosody", "icarus.prosody"); //$NON-NLS-1$ //$NON-NLS-2$
//		LoggerFactory.registerLogFile("de.ims.icarus.language.coref", "icarus.coref"); //$NON-NLS-1$ //$NON-NLS-2$
//
//		CoreferenceDocumentSet set = new CoreferenceDocumentSet();
//		Reader<?> reader = new ProsodyDocumentReader();
//		Location location = Locations.getFileLocation("data/prosody/dirndl-test-output-prosodic-format-0.3.1 (orig)"); //$NON-NLS-1$
//
//		CoreferenceUtils.loadDocumentSet((Reader<CoreferenceDocumentData>) reader, location, new Options(), set);
//
//		System.out.println("documents: "+set.size()); //$NON-NLS-1$
//	}

	public static URL getDefaultSampaTable() {
		return SampaMapper2.class.getResource("sampa-table-de.csv"); //$NON-NLS-1$
	}

	private static final char BLANK = '$';
	private static final String BLANK_STRING = "$"; //$NON-NLS-1$

	private static final long CHAR_MASK = (1L<<16)-1L;
	private static final long LOOKAHEAD_1_MASK = (1L<<32)-1L;
	private static final long LOOKAHEAD_2_MASK = (1L<<48)-1L;
	private static final long LOOKAHEAD_3_MASK = -1L;

	private static final long[] masks = {
		CHAR_MASK,
		LOOKAHEAD_1_MASK,
		LOOKAHEAD_2_MASK,
		LOOKAHEAD_3_MASK,
	};

//	private static SampaMapper2 getInstance() {
//		SampaMapper2 sm = instance;
//
//		if(sm==null) {
//			synchronized (SampaMapper2.class) {
//				sm = instance;
//				if(sm==null) {
//
//					sm = new SampaMapper2();
//					sm.init();
//
//					instance = sm;
//				}
//			}
//		}
//
//		return sm;
//	}

	/**
	 * Maps chars and compounds to sampa data
	 */
	private TLongObjectMap<SampaInfo> infoMap = new TLongObjectHashMap<>();

	/**
	 * Signals that a character sequence is the beginning of a possible compound
	 */
	private TLongSet compoundPrefixes = new TLongHashSet();

	/**
	 * Tells what syllable a character in the sampa sequence is part of
	 */
	private int[] sampaSyllableMap = new int[100]; // hopefully big enough for everything...

	/**
	 * Tells what syllable a character in the original word is part of
	 */
	private int[] charSyllableMap = new int[100]; // hopefully big enough for everything...

	private int wordCharCount;
	private int sampaCharCount;

	/**
	 * Sampa sequence
	 */
	private long[] sampaList = new long[100];

	private final StringBuilder buffer = new StringBuilder(100);

	/**
	 * Mighty combo data structure.
	 * Each entry is a bitwise combination of the character originally
	 * on that position in the word and a lookahead of up to 3 characters.
	 * Each lookahead character is shifted by 16 bits per lookahead-position.
	 */
	private long[] charList = new long[100];
	private char[] origCharList = new char[100];

	private final Matcher digitMatcher = Pattern.compile("^\\d+$").matcher(""); //$NON-NLS-1$ //$NON-NLS-2$
	private final Matcher garbageMatcher = Pattern.compile("[^\\w\\däöüÄÖÜß]").matcher(""); //$NON-NLS-1$ //$NON-NLS-2$

	public SampaMapper2() {
		this(getDefaultSampaTable());
	}

	public SampaMapper2(URL mappingRules) {
		Pattern p = Pattern.compile(","); //$NON-NLS-1$

		try(InputStream in = mappingRules.openStream()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, IOUtil.UTF8_CHARSET));
			String line;
			while((line=reader.readLine())!=null) {

				// Ignore comment lines
				if(line.startsWith("#")) { //$NON-NLS-1$
					continue;
				}

				String[] items = p.split(line);

				SampaInfo info = new SampaInfo(items);

				String symbol = info.getSymbol();
				infoMap.put(toKey(symbol), info);

				for(int i=0; i<symbol.length()-1; i++) {
					compoundPrefixes.add(toKey(symbol, 0, i));
				}
			}

			reader.close();
		} catch (IOException e) {
			//TODO
		}
	}

	private static final char[] charBuffer = new char[4];
	@SuppressWarnings("unused")
	private static char[] toChars(long key) {
		int maxIdx = 0;

		for(int i=0; i<charBuffer.length; i++) {
			int offset = i*16;
			long mask = CHAR_MASK<<offset;
			long val = key & mask;
			charBuffer[i] = (char) (val>>offset);
		}

		return Arrays.copyOf(charBuffer, maxIdx+1);
	}

	private static long toLowerKey(CharSequence s, int from, int to) {
		if(to-from+1>4)
			throw new IllegalArgumentException("Compound string exceeds supported length (4): /"+s+"/"); //$NON-NLS-1$ //$NON-NLS-2$

		long key = 0L;

		int pos = 0;
		for(int i=from; i<=to && i<s.length(); i++) {
			key |= ((long)Character.toLowerCase(s.charAt(i)) << pos);
			pos += 16;
		}

		return key;
	}

	private static long toKey(CharSequence s, int from, int to) {
		if(to-from+1>4)
			throw new IllegalArgumentException("Compound string exceeds supported length (4): /"+s+"/"); //$NON-NLS-1$ //$NON-NLS-2$

		long key = 0L;

		int pos = 0;
		for(int i=from; i<=to && i<s.length(); i++) {
			key |= ((long)s.charAt(i) << pos);
			pos += 16;
		}

		return key;
	}

	private static long toKey(CharSequence s) {
		return toKey(s, 0, s.length()-1);
	}

//	public static String[] split(String word, String... symbols) {
//		return getInstance().split0(word, symbols);
//	}

	public String[] split(String word, String... symbolSets) {
		// Buffer for finished syllables
		String[] result = new String[symbolSets.length];

		// Special case for numbers -> ignore everything syllable related
		digitMatcher.reset(word);
		if(digitMatcher.matches()) {
			Arrays.fill(result, BLANK_STRING);
			return result;
		}

		//TODO removed the special casing for num_symbolSets==num_characters to prevent erroneous syllable generation
		// Special case for exact match of syllable and character count
//		if(word.length()==symbolSets.length) {
//			for(int i=0; i<result.length; i++) {
//				result[i] = String.valueOf(word.charAt(i));
//			}
//			return result;
//		}

		fillWordList(word);

		fillSampaList(symbolSets);

		if(map(0, 0)) {
			int syllable = charSyllableMap[0];
			buffer.setLength(0);

			for(int i=0; i<wordCharCount; i++) {
				if(charSyllableMap[i]!=syllable) {
					result[syllable] = buffer.toString();
					buffer.setLength(0);
					syllable = charSyllableMap[i];
				}

				// Use original characters here!!!
				buffer.append(origCharList[i]);
			}

			result[result.length-1] = buffer.toString();
			buffer.setLength(0);

		} else {
			result = null;
		}

		return result;
	}

	private void fillWordList(String word) {

		// Remove all special characters
		garbageMatcher.reset(word);
		word = garbageMatcher.replaceAll(""); //$NON-NLS-1$

		wordCharCount = word.length();

		if(wordCharCount>charList.length) {
			charList = new long[wordCharCount*2];
			origCharList = new char[wordCharCount*2];
			charSyllableMap = new int[wordCharCount*2];
		}

		for(int i=0; i<wordCharCount; i++) {
			charList[i] = toLowerKey(word, i, i+3);
			origCharList[i] = word.charAt(i);
			charSyllableMap[i] = -1;
		}
	}

	private void fillSampaList(String[] sampaSets) {

		sampaCharCount = 0;
		for(int i=0; i<sampaSets.length; i++) {
			sampaCharCount += sampaSets[i].length();
		}

		// Ensure sufficient size of buffer
		if(sampaCharCount>sampaList.length) {
			sampaList = new long[sampaCharCount*2];
			sampaSyllableMap = new int[sampaCharCount*2];
		}

		// Insert characters and 3-lookahead
		int pos = 0;
		for(int i=0; i<sampaSets.length; i++) {
			String sampa = sampaSets[i];
			for(int j=0; j<sampa.length(); j++) {
				sampaList[pos] = toKey(sampa, j, j+3);
				sampaSyllableMap[pos] = i;

				pos++;
			}
		}
	}

	private boolean map(int sampaIndex, int charIndex) {
		if(sampaIndex>=sampaCharCount) {
			return false;
		}

		long sampaEntry = sampaList[sampaIndex];

		int syllableId = sampaSyllableMap[sampaIndex];

		for(int i=0; i<masks.length && sampaIndex+i<sampaCharCount; i++) {
			if(sampaSyllableMap[sampaIndex+i]!=syllableId) {
				break;
			}

			long mask = masks[i];
			long sampaSymbol = sampaEntry & mask;
			SampaInfo info = infoMap.get(sampaSymbol);

			if(info!=null) {
				if(map(sampaIndex, charIndex, i+1, info.getRoot())) {
					return true;
				}
			}

			// Check if further sampa compound aggregation is even possible
			if(!compoundPrefixes.contains(sampaSymbol)) {
				break;
			}
		}

		return false;
	}

	/**
	 * Called when the node has been reached by matching the previous character
	 */
	private boolean map(int sampaIndex, int charIndex, int sampaLength, Node node) {
		boolean sampaFinished = (sampaIndex+sampaLength)>=sampaCharCount-1;
		boolean charsFinished = charIndex>=wordCharCount;

		if(node.isTerminal() && sampaFinished && charsFinished) {
			return true;
		}

		if(charsFinished) {
			return false;
		}

		if(node.isTerminal()) {
			return map(sampaIndex+sampaLength, charIndex);
		}

		int minSuccessors = node.getMinSuccessorCount();
		int maxSuccessors = node.getMaxSuccessorCount();

		if(minSuccessors>(wordCharCount-charIndex)) {
			return false;
		}

		// Next char + 3-lookahead
		long charEntry = charList[charIndex];

		for(int i = minSuccessors-1; i<maxSuccessors && i<=3; i++) {
			long mask = masks[i];
			long lookahead = (charEntry & mask) >> 16;
			char next = (char)(charEntry & CHAR_MASK);

			// Step 1 char further
			Node successor = node.getNext(next, lookahead);
			if(successor!=null) {
				// Save syllable mapping
				charSyllableMap[charIndex] = sampaSyllableMap[sampaIndex];

				if(map(sampaIndex, charIndex+1, sampaLength, successor)) {
					return true;
				}
			}
		}

		return false;
	}

	private static class Node {
		final char symbol;
		final Node parent;
		private int minSuccessorCount = -1;
		private int maxSuccessorCount = -1;

		// Key is [lh3<<48,lh2<<32,lh1<<16,next]
		TLongObjectMap<Node> transitions = new TLongObjectHashMap<>(2);

		Node(Node parent, char symbol) {
			this.symbol = symbol;
			this.parent = parent;
		}

		int getMinSuccessorCount() {
			if(minSuccessorCount==-1) {
				int count = Integer.MAX_VALUE;

				for(Node node : transitions.valueCollection()) {
					count = Math.min(count, node.getMinSuccessorCount()+1);
				}

				if(count==Integer.MAX_VALUE) {
					count = 0;
				}

				minSuccessorCount = count;
			}
			return minSuccessorCount;
		}

		int getMaxSuccessorCount() {
			if(maxSuccessorCount==-1) {
				int count = 0;

				for(Node node : transitions.valueCollection()) {
					count = Math.max(count, node.getMaxSuccessorCount()+1);
				}

				maxSuccessorCount = count;
			}
			return maxSuccessorCount;
		}

		// lookahead can have up to 48 bits!
		private long toKey(long next, long lookahead) {
			return (lookahead<<16) | next;
		}

		void addTransition(Node node, long lookahead) {
			long key = toKey(node.symbol, lookahead);
			transitions.put(key, node);
		}

		Node getNext(char next, long lookahead) {
			return transitions.get(toKey(next, lookahead));
		}

		Node ensureNext(char next, long lookahead) {
			long key = toKey(next, lookahead);
			Node node = transitions.get(key);
			if(node==null) {
				node = new Node(this, next);
				transitions.put(key, node);
			}
			return node;
		}

		boolean isTerminal() {
			return transitions.isEmpty();
		}

		private void appendTo(StringBuilder sb) {
			sb.append('[');
			sb.append(symbol);
			for(Node node : transitions.valueCollection()) {
				node.appendTo(sb);
			}
			sb.append(']');
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			appendTo(sb);
			return sb.toString();
		}

		public int match(CharSequence s, int index, SampaInfo next) {
			int len = s.length();

			if(isTerminal()) {
				if(next==null) {
					return 0;
				}

				//Must have some characters left, so no checking for possible bounds exception

				char c = Character.toLowerCase(s.charAt(index));
				return (next!=null && next.isIntroChar(c)) ? 0 : -1;
			}

			char c = Character.toLowerCase(s.charAt(index));
			char lookahead = index<len-1 ? Character.toLowerCase(s.charAt(index+1)) : BLANK;

			Node nextNode;

			// Try with lookahead (only if there is space left)
			if(next==null || index<s.length()-1) {
				nextNode = getNext(c, lookahead);
				if(nextNode!=null) {
					int charCount = nextNode.match(s, index+1, next);
					if(charCount>-1) {
						return charCount+1;
					}
				}
			}

			// Bail out
			if(lookahead==BLANK) {
				return -1;
			}

			// Fallback to expecting terminal node
			nextNode = getNext(c, BLANK);
			if(nextNode!=null) {
				int charCount = nextNode.match(s, index+1, next);
				if(charCount>-1) {
					return charCount+1;
				}
			}

			return -1;
		}
	}

	private static class SampaInfo {
		private final String symbol;
		private final TCharSet introChars = new TCharHashSet();
		private final Node root = new Node(null, BLANK);

		public SampaInfo(String[] items) {
			this.symbol = items[0];
			item_loop : for(int i=1; i<items.length; i++) {
				String item = items[i];

				if(item.isEmpty()) {
					continue;
				}

				item = item.toLowerCase();

				introChars.add(item.charAt(0));

				Node node = root;
				int maxK = item.length()-1;
				for(int k=0; k<=maxK; k++) {
					char next = item.charAt(k);

					if(next==BLANK) {
						continue item_loop;
					}

					long lookahead = toKey(item, k+1, k+3);
					node = node.ensureNext(next, lookahead);
				}
			}
		}

		public Node getRoot() {
			return root;
		}

		public String getSymbol() {
			return symbol;
		}

		public boolean isIntroChar(char c) {
			return introChars.contains(c);
		}

		public boolean isEmpty() {
			return root.isTerminal();
		}

		@Override
		public String toString() {
			return symbol+" => "+root; //$NON-NLS-1$
		}
	}
}
