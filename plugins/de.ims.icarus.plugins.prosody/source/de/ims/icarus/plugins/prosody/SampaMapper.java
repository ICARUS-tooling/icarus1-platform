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
package de.ims.icarus.plugins.prosody;

import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus.Core;
import de.ims.icarus.io.IOUtil;
import de.ims.icarus.io.Reader;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.plugins.prosody.io.ProsodyDocumentReader;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.Locations;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SampaMapper {

	public static void main(String[] args) throws Exception {
//		SampaMapper mapper = getInstance();
//		String[] syllables = mapper.split0("Einschüchterung", "aIn|SYC|t@|RUN".split("\\|")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//		System.out.println(Arrays.deepToString(syllables));

		Core.debugInit(args);

		CoreferenceDocumentSet set = new CoreferenceDocumentSet();
		Reader<?> reader = new ProsodyDocumentReader();
		Location location = Locations.getFileLocation("data/prosody/dirndl-test-output-prosodic-format-0.3.1"); //$NON-NLS-1$

		CoreferenceUtils.loadDocumentSet((Reader<CoreferenceDocumentData>) reader, location, new Options(), set);
	}

	private static volatile SampaMapper instance;

	private static final char BLANK = '$';
	private static final String BLANK_STRING = "$"; //$NON-NLS-1$

	private static SampaMapper getInstance() {
		SampaMapper sm = instance;

		if(sm==null) {
			synchronized (SampaMapper.class) {
				sm = instance;
				if(sm==null) {

					sm = new SampaMapper();
					sm.init();

					instance = sm;
				}
			}
		}

		return sm;
	}

	private Map<String, SampaInfo> infoMap = new HashMap<>();
	private TCharObjectMap<String> monoSymbols = new TCharObjectHashMap<>();
	private TCharSet comboundSymbols = new TCharHashSet();

	private final Matcher digitMatcher = Pattern.compile("^\\d+$").matcher(""); //$NON-NLS-1$ //$NON-NLS-2$
	private final Matcher garbageMatcher = Pattern.compile("[^\\w\\däöüÄÖÜß]").matcher(""); //$NON-NLS-1$ //$NON-NLS-2$

	private void init() {
		Pattern p = Pattern.compile(","); //$NON-NLS-1$

		try(InputStream in = SampaMapper.class.getResourceAsStream("sampa-table-de.csv")) { //$NON-NLS-1$
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, IOUtil.DEFAULT_CHARSET));
			String line;
			while((line=reader.readLine())!=null) {
				String[] items = p.split(line);

				SampaInfo info = new SampaInfo(items);

				String symbol = info.getSymbol();
				infoMap.put(symbol, info);

				if(symbol.length()==1) {
					monoSymbols.put(symbol.charAt(0), symbol);
				} else {
					comboundSymbols.add(symbol.charAt(0));
				}
			}
		} catch (IOException e) {
			//TODO
		}
	}

	public static String[] split(String word, String... symbols) {
		return getInstance().split0(word, symbols);
	}

	private String[] split0(String word, String... symbolSets) {
		// Buffer for finished syllables
		String[] result = new String[symbolSets.length];

		// Secial case of numbers -> ignore everything syllable related
		digitMatcher.reset(word);
		if(digitMatcher.matches()) {
			Arrays.fill(result, BLANK_STRING);
			return result;
		}

		// Special case for exact match of syllable and character count
		if(word.length()==symbolSets.length) {
			for(int i=0; i<result.length; i++) {
				result[i] = String.valueOf(word.charAt(i));
			}
			return result;
		}

		// Remove all special characters
		garbageMatcher.reset(word);
		word = garbageMatcher.replaceAll(""); //$NON-NLS-1$

		List<SampaInfo> sampaInfos = new ArrayList<>();
		int[] counts = new int[symbolSets.length];

		// Collect sampa symbols involved
		for(int i=0; i<symbolSets.length; i++) {
			counts[i] = parseSymbols(word, symbolSets[i], sampaInfos);
		}

		// Special case of exact match between character and sampa symbol count
		if(word.length()==sampaInfos.size() && symbolSets.length==1) {
			return new String[]{word};
		}

		// Map sampa to characters

		// Begin index of current syllable
		int begin = 0;
		// Moving end index of current syllable
		int cursor = begin;
		// Index of current syllable
		int syllable = 0;
		// Number of sampa symbols mapped to current syllable (used to detect syllable border)
		int sampaCount = 0;

		int maxI = sampaInfos.size()-1;
		for(int i=0; i<=maxI; i++) {
			SampaInfo currentSampa = sampaInfos.get(i);
			SampaInfo nextSampa = i<maxI ? sampaInfos.get(i+1) : null;

			int charCount = currentSampa.find(word, cursor, nextSampa);
			if(charCount<=0)
				throw new IllegalArgumentException(String.format(
						"Unable to map SAMPA symbol [%s] to word '%s' at index %d", //$NON-NLS-1$
						currentSampa.getSymbol(), word, cursor));

			cursor += charCount;
			sampaCount++;

			if(sampaCount>=counts[syllable]) {
				result[syllable] = word.substring(begin, cursor);
				begin = cursor;
				syllable++;
				sampaCount = 0;
			}
		}

		return result;
	}

//	private int map(String word, int charIndex, List<SampaInfo> sampaInfos, int infoIndex) {
//		SampaInfo info = sampaInfos.get(infoIndex);
//		SampaInfo nextInfo = infoIndex<sampaInfos.size()-1 ? sampaInfos.get(infoIndex+1) : null;
//		Node node = info.getRoot();
//
//		int charCount = 0;
//
//		while(true) {
//			char c = word.charAt(charIndex);
//			char lookahead = charIndex<word.length()-1 ? word.charAt(charIndex+1) : BLANK;
//
//			// Try lookahead
//			Node nextNode = node.getNext(c, lookahead);
//
//		}
//
//		return charCount;
//	}

//	private int find(String word, int index, SampaInfo info, SampaInfo next) {
//		Node node = info.getRoot();
//		int charCount = 0;
//		while(true) {
//			if(node==null || node.isTerminal()) {
//				break;
//			}
//
//			char c = word.charAt(index);
//			char lookahead = index<word.length()-1 ? word.charAt(index+1) : BLANK;
//
//			if(lookahead==BLANK && next!=null)
//				throw new IllegalStateException("No more room for SAMPA symbol "+next+" in word '"+word+"' after index "+index);
//
//			// Try with lookahead
//			Node nextNode = node.getNext(c, lookahead);
//
//			if(nextNode!=null) {
//				char
//			}
//		}
//
//		return charCount;
//	}

	private int parseSymbols(String word, String set, List<SampaInfo> sampaInfos) {
		int len = set.length();
		int infoCount = 0;

		for(int i=0; i<len; i++) {
			char c = set.charAt(i);
			String symbol = null;

			if(comboundSymbols.contains(c) && i<len-1) {
				symbol = String.valueOf(c)+String.valueOf(set.charAt(++i));
				if(!infoMap.containsKey(symbol)) {
					symbol = null;
					i--; // back one symbol to not consume next one!
				}
			}

			if(symbol==null) {
				symbol = monoSymbols.get(c);
			}

			if(symbol==null)
				throw new IllegalArgumentException("Unable to recognize SAMPA symbols: /"+set+"/ in word: "+word); //$NON-NLS-1$ //$NON-NLS-2$

			SampaInfo info = infoMap.get(symbol);

			if(info==null)
				throw new IllegalArgumentException("Illegal symbol /"+symbol+"/ in SAMPA set /"+set+"/ in word: "+word); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			// Skip empty definitions
			if(info.isEmpty()) {
				continue;
			}

			sampaInfos.add(info);
			infoCount++;
		}

		return infoCount;
	}

	private static class Node {
		final char symbol;
		final Node parent;

		// Key is [lookahead,next] as in (lookahead<<16)|next
		TIntObjectMap<Node> transitions = new TIntObjectHashMap<>(2);

		Node(Node parent, char symbol) {
			this.symbol = symbol;
			this.parent = parent;
		}

		private int toKey(int next, int lookahead) {
			return (lookahead<<16) | next;
		}

		void addTransition(Node node, char lookahead) {
			int key = toKey(node.symbol, lookahead);
			transitions.put(key, node);
		}

		Node getNext(char next, char lookahead) {
			return transitions.get(toKey(next, lookahead));
		}

		Node ensureNext(char next, char lookahead) {
			int key = toKey(next, lookahead);
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
			for(int i=1; i<items.length; i++) {
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
						continue;
					}

					char lookahead = k<maxK ? item.charAt(k+1) : BLANK;
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

		public int find(CharSequence s, int index, SampaInfo next) {
			return root.match(s, index, next);
		}
	}
}
