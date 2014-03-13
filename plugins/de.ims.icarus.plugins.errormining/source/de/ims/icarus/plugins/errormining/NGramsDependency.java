/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus G�rtner and Gregor Thiele
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
package de.ims.icarus.plugins.errormining;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.matetools.conll.CONLL09SentenceDataGoldReader;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.location.DefaultFileLocation;
import de.ims.icarus.util.location.UnsupportedLocationException;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramsDependency {


	//options
	protected int nGramCount;
	protected final int nGramLimit;
	protected final boolean useFringe;
	protected int fringeSize;
	protected boolean useNumberWildcard;

	//protected List<DependencyItemInNuclei> items;
	protected Map<String,ArrayList<DependencyItemInNuclei>> nGramCache;
	protected List<NGramQAttributes> queryList;
	protected Options options;


	//NIL stuff
	protected Map<Integer,ArrayList<Integer>> nilCache;
	protected Map<Integer,ArrayList<Integer>> nilEqualSentencesCache;
	protected Map<String,ArrayList<Integer>> nilLexiconCache;


	protected List<DependencyData> corpus;


	private static NGramsDependency instance;

	private static Pattern numberPattern = Pattern.compile("^[0-9]"); //$NON-NLS-1$
	private static String numberString = "[number-wildcard]"; //$NON-NLS-1$

	public static NGramsDependency getInstance() {
		if (instance == null) {
			synchronized (NGramsDependency.class) {
				if (instance == null) {
					instance = new NGramsDependency();
				}
			}
		}
		return instance;
	}


	//Debug Konstructor
	public NGramsDependency(){
		Options options = new Options();
		options = Options.emptyOptions;

		this.nGramCount = 1; //normally we start with unigrams so n will be 1


		//0 collect ngrams until no new ngrams are found
		this.nGramLimit = (int) options.get("NGramLIMIT");  //$NON-NLS-1$
		this.fringeSize = (int) options.get("FringeSIZE", 1);  //$NON-NLS-1$
		this.useFringe = false;
		this.useNumberWildcard = options.getBoolean("UseNumberWildcard"); //$NON-NLS-1$

		nGramCache = new LinkedHashMap<String,ArrayList<DependencyItemInNuclei>>();
		corpus = new ArrayList<DependencyData>();

		nilCache = new LinkedHashMap<Integer,ArrayList<Integer>>();
		nilLexiconCache = new LinkedHashMap<String,ArrayList<Integer>>();
	}


	public NGramsDependency(Options options, List<NGramQAttributes> queryList){

		this.options = options;

		this.nGramCount = 1; //normally we start with unigrams so n will be 1

		this.queryList = queryList;

		//0 collect ngrams until no new ngrams are found

		this.fringeSize = (int) options.get("FringeSIZE", 1);  //$NON-NLS-1$

		this.useFringe = options.getBoolean("UseFringe"); //$NON-NLS-1$
		this.nGramLimit = options.getInteger("NGramLIMIT"); //$NON-NLS-1$
		this.useNumberWildcard = options.getBoolean("UseNumberWildcard"); //$NON-NLS-1$

		nGramCache = new LinkedHashMap<String,ArrayList<DependencyItemInNuclei>>();
		corpus = new ArrayList<DependencyData>();

		nilCache = new LinkedHashMap<Integer,ArrayList<Integer>>();
		nilLexiconCache = new LinkedHashMap<String,ArrayList<Integer>>();
	}



	public NGramsDependency(int nGramCount, Options options){

		this.options = options;

		this.nGramCount = nGramCount; //normally we start with unigrams so n will be 1


		//0 collect ngrams until no new ngrams are found
		this.fringeSize = (int) options.get("FringeSIZE", 1);  //$NON-NLS-1$

		this.useFringe = options.getBoolean("UseFringe"); //$NON-NLS-1$
		this.nGramLimit = options.getInteger("NGramLIMIT"); //$NON-NLS-1$
		this.useNumberWildcard = options.getBoolean("UseNumberWildcard"); //$NON-NLS-1$

		nGramCache = new LinkedHashMap<String,ArrayList<DependencyItemInNuclei>>();
		corpus = new ArrayList<DependencyData>();

		nilCache = new LinkedHashMap<Integer,ArrayList<Integer>>();
		nilLexiconCache = new LinkedHashMap<String,ArrayList<Integer>>();
	}



	protected String ensureValid(String input) {
		return input==null ? "NULL" : input; //$NON-NLS-1$
	}


	/**
	 *
	 * @param l1
	 * @param sentenceNR
	 * @return
	 */
	//FIXME
	protected DependencySentenceInfo returnDependencySentenceInfoNREqual(ArrayList<DependencyItemInNuclei> l1,
										int sentenceNR){

		for (int i = 0; i < l1.size(); i++){
			DependencyItemInNuclei item = l1.get(i);
			//System.out.println("EQUAL-SNR-TAG " + item.getPosTag());
			for (int j = 0 ; j < item.getSentenceInfoSize(); j++){
				DependencySentenceInfo si = (DependencySentenceInfo) item.getSentenceInfoAt(j);
				if(si.getSentenceNr() == sentenceNR){
					//maybe list?!
					//System.out.println("NucleiToBeAdded " + si.getNucleiIndexListAt(0));
					return si;
				}
			}
		}
		return null;
	}



	//needed?
	public List<String>  getLabelVariation(String tag){
		List<String> resultVari = null;

		if (nGramCache.containsKey(tag)){
			resultVari = new ArrayList<String>();
			ArrayList<DependencyItemInNuclei> arri = nGramCache.get(tag);

			for(int i = 0; i < arri.size(); i++){
				DependencyItemInNuclei iin = arri.get(i);
				resultVari.add(iin.getPosTag());
			}
		}

		//System.out.println(tag + " : " + resultVari); //$NON-NLS-1$
		return resultVari;
	}



//	protected ArrayList<DependencyItemInNuclei> leftSide(ArrayList<DependencyItemInNuclei> items,
//													SentenceInfo si, String newPos){
//
//		boolean knownTag = false;
//
//		for (int i = 0; i < items.size(); i++) {
//			// increment when tag found again
//			DependencyItemInNuclei item = items.get(i);
//			//System.out.println(item.getPosTag() +" vs "+ "getLeftPoS VB");
//			if (item.getPosTag().equals("getLeftPoS VB")) { //$NON-NLS-1$
//				int oldCount = item.getCount();
//				System.out.print("oldcount = "+ oldCount);
//				item.setCount(oldCount + 1);
//				System.out.println(" newcount = "+ item.getCount());
//				item.addNewSentenceInfoLeft(si);
//				knownTag = true;
//			}
//		}
//
//		System.out.println(knownTag);
//		if (!knownTag) {
//			//System.out.println("faulty Tag@ " + sentenceNr + " " + sentencelength);
//			DependencyItemInNuclei item = new DependencyItemInNuclei();
//			//item.setPosTag(ensureValid(dd.getPos(wordIndex)));
//			item.addNewSentenceInfoLeft(si);
//			items.add(item);
//		}
//
//		return items;
//	}


	/**
	 * Step 1) Initialize Corpus / Create uniGrams
	 * Loop trough the Corpus and add all occurring Words with their specific PoSTags
	 * to the nGramCache. Also Store CorpusPosition (SentenceNr/Wordpositon-in-Sentence)
	 * If one Word has more than one Tag assigned add the new Tag aswell to the Wordform.
	 * Step 2) we will only use that unigrams which contains at least 2 different PoS Tags.
	 *
	 * @param dd
	 * @param sentenceNr
	 */
	public void initializeUniGrams(DependencyData dd, int sentenceNr) {

		corpus.add(dd);

		//simple sentence length nil check (not sufficient)
//		if(nilCache.containsKey(dd.length())){
//			nilCache.get(dd.length()).add(sentenceNr);
//		} else {
//			ArrayList<Integer> snrList = new ArrayList<Integer>();
//			snrList.add(sentenceNr);
//			nilCache.put(dd.length(), snrList);
//		}



		for (int wordIndex = 0; wordIndex < dd.length(); wordIndex++) {
			String currentWord = checkForNumber(dd.getForm(wordIndex));
			//System.out.print(currentWord + " "); //$NON-NLS-1$


			if(nilLexiconCache.containsKey(currentWord)){
				if(!nilLexiconCache.get(currentWord).contains(sentenceNr)){
					nilLexiconCache.get(currentWord).add(sentenceNr);
				}

			} else {
				ArrayList<Integer> snrList = new ArrayList<Integer>();
				snrList.add(sentenceNr);
				nilLexiconCache.put(currentWord, snrList);
			}


			int headIndex = dd.getHead(wordIndex);

			StringBuilder sb = new StringBuilder();

			String headAppendix;
			if (wordIndex < headIndex){
				headAppendix = "_R"; //$NON-NLS-1$
			} else {
				headAppendix = "_L"; //$NON-NLS-1$
			}

			String addedword = "ROOT"; //$NON-NLS-1$
			String tag = dd.getRelation(wordIndex) + headAppendix;

			if (headIndex > 0 ){
//				System.out.println("RLabel " + dd.getRelation(wordIndex)
//						+ " Head " + head + " " + checkForNumber(dd.getForm(head))
//						+ " WIndex " + wordIndex);
				//TODO doublecheck working correct way? no more workaround needed
				if (wordIndex < headIndex){
					addedword = currentWord + " " + getTagQueryDependency(checkForNumber(dd.getForm(headIndex))); //$NON-NLS-1$
				} else {
					addedword = getTagQueryDependency(checkForNumber(dd.getForm(headIndex))) + " " + currentWord; //$NON-NLS-1$
				}

				//addedword = currentWord + " " + getTagQueryDependency(checkForNumber(dd.getForm(headIndex))); //$NON-NLS-1$
				sb.append("<").append(addedword).append(" {")  //$NON-NLS-1$//$NON-NLS-2$
				.append(tag)
				.append("}>"); //$NON-NLS-1$

			} else {
				//System.out.println("else " + addedword);
				sb.append("<").append(addedword).append(" {") //$NON-NLS-1$ //$NON-NLS-2$
				.append(dd.getRelation(wordIndex))
				.append("}>");				 //$NON-NLS-1$
			}
			//System.out.println(sb.toString());

			// item already in list? only add new tags
			// but first check if overlap (same sentence)
			// Example: showed --do--> char | showed --io--> char
			// {showed, chair, <do,io>} correct
			// we want not two nuclei {showed, chair, <do>}, {showed, chair, <io>}
			if (nGramCache.containsKey(addedword)) {

				ArrayList<DependencyItemInNuclei> items = nGramCache.get(addedword);

				//System.out.println("#Tag: " + tag);
				boolean knownTag = false;
				boolean overlapTag = false;

				for (int i = 0; i < items.size(); i++) {
					// increment when tag found again
					DependencyItemInNuclei item = items.get(i);
					//System.out.println(item.getPosTag() +" vs "+ tag);
					for(int k = 0 ; k < item.getSentenceInfoSize(); k++){

						if (item.getSentenceInfoAt(k).getSentenceNr()==sentenceNr){
							if ((headIndex+1)==item.getSentenceInfoAt(k).getSentenceHeadIndex()){
//								System.out.println("found same sentence");
//								System.out.println("Token: " + addedword);
//								System.out.println("SNR: " + sentenceNr);
//								System.out.println("WNR: " + wordIndex);
//								System.out.println("Tag: " + tag + " "
//										+ item.getPosTag());
//								System.out.println("Head: "
//										+ (headIndex + 1)
//										+ " "
//										+ item.getSentenceInfoAt(k)
//												.getSentenceHeadIndex());

								overlapTag = true;
							}
						}
					}
					if (item.getPosTag().equals(tag)) {
						int oldCount = item.getCount();
						item.setCount(oldCount + 1);
						item.addNewDependencySentenceInfoUniGrams(sentenceNr, wordIndex+1, headIndex+1);
						knownTag = true;
					}

					//FIXME
					if (overlapTag && !knownTag) {
//						System.out.println("overlap");
//						System.out.println("Token: " + addedword);
//						System.out.println("SNR: " + sentenceNr);
//						System.out.println("WNR: " + wordIndex);
//						System.out.println("tag: " + tag);
//						item.setPosTag(tag);
//						item.addNewDependencySentenceInfoUniGrams(sentenceNr, wordIndex+1, headIndex+1);
//						knownTag = true;
					}
				}




				// new pos tag found; add to list
				if (!knownTag) {
					//System.out.println("faulty Tag@ " + sentenceNr + " " + sentencelength);
					DependencyItemInNuclei item = new DependencyItemInNuclei();
					item.setPosTag(ensureValid(tag));
					item.addNewDependencySentenceInfoUniGrams(sentenceNr, wordIndex+1, headIndex+1);
					items.add(item);
				}

			} else {

				if(!addedword.equals("ROOT")){ //$NON-NLS-1$
					ArrayList<DependencyItemInNuclei> items = new ArrayList<DependencyItemInNuclei>();
					DependencyItemInNuclei item = new DependencyItemInNuclei();
					item.setPosTag(ensureValid(tag));
					item.addNewDependencySentenceInfoUniGrams(sentenceNr, wordIndex+1, headIndex+1);
					items.add(item);

					nGramCache.put(addedword, items);
				}
			}
		}
	}



	/**
	 * Step 2: Filter uniGrams
	 * nGramCache contains all items with an PoS Tag. If only one type of
	 * PoS Tag is assigned to a Word this won't be an error at all because
	 * only one PoS Tag is assigned at all. Calling this Method removes all
	 * Wordtokens with only one PoS Tag. (equals Step 2 Dickinson)
	 */

	private Map<String, ArrayList<DependencyItemInNuclei>> removeItemsLengthOne(Map<String,ArrayList<DependencyItemInNuclei>> input){
		//input = addNilLabels(input);
		ArrayList<String> removeFromNGrams = new ArrayList<String>();
		for(Iterator<String> i = input.keySet().iterator(); i.hasNext();){
			String key = i.next();
			ArrayList<DependencyItemInNuclei> arrItem = input.get(key);
			//only one PoS Tag found -> add to delet list;
			if (arrItem.size() == 1){
				removeFromNGrams.add(key);
			}
		}

		//System.out.println("Items to Remove: " + removeFromnGrams.size());
		for(int i = 0; i < removeFromNGrams.size(); i++){
			input.remove(removeFromNGrams.get(i));
		}

		removeFromNGrams.clear();
		return input;
	}

	@Deprecated
	//only test stuff
	private Map<String, ArrayList<DependencyItemInNuclei>> addNilLabels(Map<String,ArrayList<DependencyItemInNuclei>> input){

		for (Iterator<String> i = input.keySet().iterator(); i.hasNext();) {
			String key = i.next();
			ArrayList<DependencyItemInNuclei> arrItem = input.get(key);

			ArrayList<DependencyItemInNuclei> arrItemNew = new ArrayList<DependencyItemInNuclei>();

			for (int j = 0; j < arrItem.size(); j++) {

				DependencyItemInNuclei arri = arrItem.get(j);

				// System.out.println(key);
				for (int s = 0; s < arri.getSentenceInfoSize(); s++) {
					DependencySentenceInfo dsi = arri.getSentenceInfoAt(s);
					int sentenceNr = dsi.getSentenceNr();

					// sentence in double list?
					if (nilEqualSentencesCache.containsKey(sentenceNr)) {
						// edge between two nodes in both sentences?
						List<Integer> sameSentenceIDList = nilEqualSentencesCache
								.get(sentenceNr);

//						System.out.println(sameSentenceIDList
//											+ " :"	+ sentenceNr);
						for (Integer sentenceIndex : sameSentenceIDList) {
							DependencyData dd = corpus.get(sentenceIndex);
							int nucleiIndex = dsi.getNucleiIndex() - 1;
							//System.out.println(checkForNumber(dd.getForm(nucleiIndex))+dd.getHead(nucleiIndex));
							int headIndex = dsi.getSentenceHeadIndex() - 1;


							if (dd.getHead(nucleiIndex) != headIndex) {
								DependencyItemInNuclei newDIIN = new DependencyItemInNuclei();
								//addSentenceInfoLeft(arri, "nil", dsi, false);
								//addSentenceInfoLeft(newDIIN, "nil", dsi, true);
								newDIIN.addNewDependencySentenceInfoUniGrams(sentenceIndex, dsi.getNucleiIndex(), dsi.getSentenceHeadIndex());
								newDIIN.setPosTag("nil");	 //$NON-NLS-1$
								arrItemNew.add(newDIIN);
								//System.out.println(key + " nilLabel");
								//return input;
							}
						}

					}
				}
			}

			arrItem.addAll(arrItemNew);
			input.put(key, arrItem);
		}

		return input;
	}

	private Map<Integer, ArrayList<Integer>> removeItemsSnr(Map<Integer,ArrayList<Integer>> input){

		ArrayList<Integer> removeFromNGrams = new ArrayList<Integer>();
		for(Integer snr : input.keySet()){
			//only one PoS Tag found -> add to delet list;
			if (input.get(snr).size() == 1){
				removeFromNGrams.add(snr);
			}
		}

		//System.out.println("Items to Remove: " + removeFromnGrams.size());
		for(int i = 0; i < removeFromNGrams.size(); i++){
			input.remove(removeFromNGrams.get(i));
		}
		return input;
	}

	//method used for replace numbers by general number token
	private String checkForNumber(String currentWord) {
		//System.out.print("INput " + currentWord);
		if (useNumberWildcard){
			if (numberPattern.matcher(currentWord).find()){
				//System.out.println(" out " + numberString);
				return numberString;
			}
		}
		//System.out.println(" out " + currentWord);
		return currentWord;
	}


	/**
	 * Examine only non-fringe nuclei, fringe = edge of variation n-gram
	 *
	 * @param input
	 * @return
	 */
	private Map<String, ArrayList<DependencyItemInNuclei>> distrustFringeHeuristic(Map<String,ArrayList<DependencyItemInNuclei>> input){

		ArrayList<String> removeFringeFromNGrams = new ArrayList<String>();
		for(Iterator<String> i = input.keySet().iterator(); i.hasNext();){
			String key = i.next();
			ArrayList<DependencyItemInNuclei> arrItem = input.get(key);

			for(int j = 0; j < arrItem.size(); j++){
				DependencyItemInNuclei iin = arrItem.get(j);
				for (int s = 0; s < iin.getSentenceInfoSize(); s++){
					DependencySentenceInfo si = (DependencySentenceInfo) iin.getSentenceInfoAt(s);
					int start = si.getSentenceBegin();
					int end = si.getSentenceEnd();

					for(int n = 0; n < si.getNucleiIndexListSize(); n++){

						//is fringe?
						if(isFringe(si.getNucleiIndexListAt(n), start, end)){

							if(!removeFringeFromNGrams.contains(key)){
								//System.out.println("FringeKey " + key);
								removeFringeFromNGrams.add(key);
							}
						}
					}
				}
			}
		}

		//System.out.println("FringeItems to Remove: " + removeFringeFromNGrams.size());
		for(int i = 0; i < removeFringeFromNGrams.size(); i++){
			input.remove(removeFringeFromNGrams.get(i));
		}

		removeFringeFromNGrams.clear();
		return input;

	}


	/**
	 * Check if nucleiPosition is the start/begining of the sentence
	 * return true if it is, and @distrustFringe remove all "true" items
	 *
	 * @param nucleiPosition
	 * @param end
	 * @param start
	 * @return
	 */
	private boolean isFringe(int nucleiPosition, int start, int end) {

		//check if nuclei is at the beginning / end of the ngram
		if(nucleiPosition == start || nucleiPosition == end){
			return true;
		}

		return false;
	}

	private StringBuilder rebuildGapTag(int startIndex, DependencySentenceInfo si, DependencyData dd){
		StringBuilder sb = new StringBuilder();
		for (int i = startIndex; i < si.getSentenceHeadIndex(); i++){
			//sb.append(checkForNumber(dd.getForm(i))).append(" "); //$NON-NLS-1$
			if(i < (si.getSentenceEnd()-1)){
				sb.append(checkForNumber(dd.getForm(i))).append(" "); //$NON-NLS-1$
			} else {
				sb.append(checkForNumber(dd.getForm(i)));
			}
		}

		return sb;
	}

	private String rebuildTag(int startIndex, DependencySentenceInfo si, DependencyData dd){
		StringBuilder sb = new StringBuilder();
		for (int i = startIndex; i < si.getSentenceEnd(); i++){
			if(i < (si.getSentenceEnd()-1)){
				sb.append(checkForNumber(dd.getForm(i))).append(" "); //$NON-NLS-1$
			} else {
				sb.append(checkForNumber(dd.getForm(i)));
			}
		}
		return sb.toString();
	}


	private void createNGrams(Map<String, ArrayList<DependencyItemInNuclei>> inputNGram,
									boolean lb, boolean rb){

		Map<String, ArrayList<DependencyItemInNuclei>> outputNGram = new LinkedHashMap<String, ArrayList<DependencyItemInNuclei>>();
		Map<String, ArrayList<DependencyItemInNuclei>> outputNGramR = new LinkedHashMap<String, ArrayList<DependencyItemInNuclei>>();
		boolean reachedLeftBoarder = lb;
		boolean reachedRightBoarder = rb;

		for(Iterator<String> it = inputNGram.keySet().iterator(); it.hasNext();){
			String key = it.next();
			ArrayList<DependencyItemInNuclei> iiArr = inputNGram.get(key);

			//System.out.println("CreateNGRAMS: " + key);

			for (int tagSize = 0 ; tagSize < iiArr.size(); tagSize++){

				DependencyItemInNuclei iin = iiArr.get(tagSize);

				for (int s = 0 ; s < iin.getSentenceInfoSize(); s++){

					DependencySentenceInfo si = (DependencySentenceInfo) iin.getSentenceInfoAt(s);

//					System.out.println("head: " + si.getSentenceHeadIndex()
//										+ " nuclei " +si.getNucleiIndex()
//										+ " end " +si.getSentenceEnd()
//										+ " begin " +si.getSentenceBegin());


					//start sentencecount at 1 so decrement
					DependencyData dd = corpus.get(si.getSentenceNr());
					int startIndex = si.getSentenceBegin()-1;
					int endIndex = si.getSentenceEnd()-1;

					boolean hasGap = si.getSentenceEnd()+1 < si.getSentenceHeadIndex();
					//System.out.println("HasGap " +hasGap);

					// there are words between dependenca nodepair, first fill gap
					// between nodes before continue adding surrounding items
//					if (si.getSentenceEnd()+1 < si.getSentenceHeadIndex()){
//						StringBuilder sb = new StringBuilder();
//
//						for (int i = startIndex; i < si.getSentenceHeadIndex(); i++){
//							sb.append(checkForNumber(dd.getForm(i))).append(" "); //$NON-NLS-1$
//						}
//						System.out.println(sb.toString() + hasGap);
//					}


					/*
					 * *************************************************************
					 * first: check if item is not the last item in the sentence,
					 * (we can't skip sentence boarders)
					 * second: check if item is already in input Map
					 * *************************************************************
					 */
					if (!reachedLeftBoarder) {
						//System.out.println("LEFT<<<<<<<<<<<<<< " + key);

						StringBuilder leftKey = new StringBuilder();

						// new key
						if (startIndex > 0) {

							String leftForm = checkForNumber(checkForNumber(dd.getForm(startIndex - 1)));
							//System.out.println(leftForm + " " + startIndex);
							//String leftPOS = dd.getPos(startIndex - 1);


							//check if leftword is found in grams -> add new nucleipos to sentence
							//boolean addNewNuclei = nGramCache.containsKey(leftForm);
							boolean addNewNuclei = newNucleiCheck(startIndex-1, dd);

							//leftKey.append(leftForm).append(" ").append(key); //$NON-NLS-1$

							if (hasGap){
								leftKey = rebuildGapTag(startIndex, si, dd);
							} else {
								String tmp = rebuildTag(startIndex, si, dd);
								leftKey.append(leftForm).append(" ").append(tmp); //$NON-NLS-1$
							}

							//System.out.println("GRAM " + nGramCount + " " +leftKey.toString());
//							System.out.println("LEFTKEY " + leftKey
//									+ " FORM "  + leftForm
//									+ " LEFT "  + addNewNuclei);

							// extend existing gram with values
							if (outputNGram.containsKey(leftKey.toString())) {


								// leftSide(outputNGram.get(leftKey.toString()), si, leftPOS);
								ArrayList<DependencyItemInNuclei> itemsTemp = outputNGram.get(leftKey.toString());

								boolean knownTag = false;

								for (int i = 0; i < itemsTemp.size(); i++) {
									// increment when tag found again
									DependencyItemInNuclei item = itemsTemp.get(i);

//									System.out.println("leftContains " + leftKey.toString());
//									System.out.println(item.getPosTag() + " vs "+ iin.getPosTag());


									if (item.getPosTag().equals(iin.getPosTag())) {
										if (addNewNuclei){
											String nucleiKey = getNucleiForm(startIndex-1, dd);
											DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(nucleiKey), si.getSentenceNr());
											//DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(leftForm), si.getSentenceNr());
											//addNucleiLeft(item, iin.getPosTag(), si, sitemp, true);
											if(sitemp == null){
												addSentenceInfoLeft(item, iin.getPosTag(), si, true);
											} else {
												addNucleiLeft(item, iin.getPosTag(), si, sitemp, false);
											}
											knownTag = true;
										} else {
											addSentenceInfoLeft(item, iin.getPosTag(), si, true);
											knownTag = true;
										}
									}
								}


								//outputNGram.put(leftKey.toString(), itemsTemp);


								// new pos tag found; add to list
								if (!knownTag) {

									//System.out.println("unknown tag " + iin.getPosTag());
									DependencyItemInNuclei item = new DependencyItemInNuclei();
									//already an unigram
									if (addNewNuclei){
										String nucleiKey = getNucleiForm(startIndex-1, dd);
										//System.out.println(nucleiKey);
										DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(nucleiKey), si.getSentenceNr());
										//DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(leftForm), si.getSentenceNr());
										//addNucleiLeft(item, iin.getPosTag(), si, sitemp, false);
										if(sitemp == null){
											addSentenceInfoLeft(item, iin.getPosTag(), si, true);
										} else {
											addNucleiLeft(item, iin.getPosTag(), si, sitemp, false);
										}
										itemsTemp.add(item);
									} else {
										addSentenceInfoLeft(item, iin.getPosTag(), si, false);
										itemsTemp.add(item);
									}

//									System.out.println("EXISTINGITEM ~~~~~~~ "+leftKey
//											+ " " + item.getPosTag()
//											+ " " + item.getSentenceInfoAt(0).sentenceBegin
//											+ " " + item.getSentenceInfoAt(0).sentenceEnd);
								}

							} else {
								ArrayList<DependencyItemInNuclei> items = new ArrayList<DependencyItemInNuclei>();
								DependencyItemInNuclei item = new DependencyItemInNuclei();
								//already an unigram
								if (addNewNuclei){
									String nucleiKey = getNucleiForm(startIndex-1, dd);
									//System.out.println("NewNuclei: " + nucleiKey);
									//System.out.println("for Key " +leftKey.toString());
									DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(nucleiKey), si.getSentenceNr());
									//DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(leftKey.toString()), si.getSentenceNr());
									if(sitemp == null){
										addSentenceInfoLeft(item, iin.getPosTag(), si, false);
									} else {
										addNucleiLeft(item, iin.getPosTag(), si, sitemp, false);
									}
									items.add(item);
								} else {
									addSentenceInfoLeft(item, iin.getPosTag(), si, false);
									items.add(item);
								}

//								System.out.println("NEWITEM ~~~~~~~ "+leftKey
//										+ " " + item.getPosTag()
//										+ " " + item.getSentenceInfoAt(0).sentenceBegin
//										+ " " + item.getSentenceInfoAt(0).sentenceEnd);

								outputNGram.put(leftKey.toString(), items);
							}


						} else {
							//reachedLeftBoarder = true;
						}
					}



					/*
					 * *************************************************************
					 * first: check if item is not the last item in the sentence,
					 * (we can't skip sentence boarders)
					 * second: check if item is already in input Map
					 * *************************************************************
					 */
					if(!reachedRightBoarder){

						StringBuilder rightKey = new StringBuilder();
						int sentenceSize = dd.length()-1;

						//System.out.println("End: " + endIndex + " SIze " + dd.length());

//						if ((endIndex < sentenceSize)
//								&& (!inputNGram.containsKey("getRWord"))) {


						if (endIndex < sentenceSize-1) {

							String rightForm = checkForNumber(dd.getForm(endIndex + 1));
							//String rightPOS = dd.getPos(endIndex + 2);

							//check if leftword is found in grams -> add new nucleipos to sentence
							//FIXME
							//boolean addNewNuclei = nGramCache.containsKey(rightForm);

							boolean addNewNuclei = newNucleiCheck(endIndex+2, dd);


							if (hasGap){
								rightKey = rebuildGapTag(startIndex, si, dd);
							} else {
								String tmp = rebuildTag(startIndex, si, dd);
								rightKey.append(tmp).append(" ").append(rightForm); //$NON-NLS-1$
							}

							// extend existing gram with values
							//FIXME -R
							if (outputNGramR.containsKey(rightKey.toString())) {

								// leftSide(outputNGram.get(leftKey.toString()), si, leftPOS);
								ArrayList<DependencyItemInNuclei> itemsTemp = outputNGramR.get(rightKey.toString());

								boolean knownTag = false;

								for (int i = 0; i < itemsTemp.size(); i++) {
									// increment when tag found again
									DependencyItemInNuclei item = itemsTemp.get(i);


									//System.out.println("rigthContains " + rightKey.toString());
									//System.out.println(item.getPosTag() + " vs "+ iin.getPosTag());
									//System.out.println("EQTAG: " + item.getPosTag().equals(iin.getPosTag()));

									if (item.getPosTag().equals(iin.getPosTag())) {
										//int oldCount = item.getCount();
										if (addNewNuclei){
											String nucleiKey = getNucleiForm(endIndex+2, dd);
											DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(nucleiKey), si.getSentenceNr());
											if(sitemp == null){
												addSentenceInfoRigth(item, si, iin.getPosTag(), true);
											} else {
												addNucleiRigth(item, iin.getPosTag(), si, sitemp, false);
											}
											//addNucleiRigth(item, iin.getPosTag(), si, sitemp, true);
											knownTag = true;
										} else {
											addSentenceInfoRigth(item, si, iin.getPosTag(), true);
											knownTag = true;
										}
									}
								}


								// new pos tag found; add to list
								if (!knownTag) {
									DependencyItemInNuclei item = new DependencyItemInNuclei();
									//already an unigram
									if (addNewNuclei){
										String nucleiKey = getNucleiForm(endIndex+2, dd);
										DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(nucleiKey), si.getSentenceNr());
										//addNucleiRigth(item, iin.getPosTag(), si, sitemp, false);
										if(sitemp == null){
											addSentenceInfoRigth(item, si, iin.getPosTag(), true);
										} else {
											addNucleiRigth(item, iin.getPosTag(), si, sitemp, false);
										}
										itemsTemp.add(item);
									} else {
										//System.out.println(iin.getPosTag());
										addSentenceInfoRigth(item, si, iin.getPosTag(), false);
										itemsTemp.add(item);
									}

//									System.out.println("~~~~~~~ EXISTINGITEM "+rightKey
//											+ " " + item.getPosTag()
//											+ " " + item.getSentenceInfoAt(0).sentenceBegin
//											+ " " + item.getSentenceInfoAt(0).sentenceEnd);
								}



							} else {
								ArrayList<DependencyItemInNuclei> items = new ArrayList<DependencyItemInNuclei>();
								DependencyItemInNuclei item = new DependencyItemInNuclei();

								//already an unigram
								//FIXME returnDependencySentenceInfoNREqual
								if (addNewNuclei){
									String nucleiKey = getNucleiForm(endIndex+2, dd);
									//System.out.println(nucleiKey + " >>>" + rightForm);
									DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(nucleiKey), si.getSentenceNr());
									if(sitemp == null){
										addSentenceInfoRigth(item, si, iin.getPosTag(), false);
									} else {
										addNucleiRigth(item, iin.getPosTag(), si, sitemp, false);
									}
									items.add(item);
								} else {
									addSentenceInfoRigth(item, si, iin.getPosTag(), false);
									items.add(item);
								}

//								System.out.println("~~~~~~~ NEWITEM "+rightKey
//										+ " " + item.getPosTag()
//										+ " " + item.getSentenceInfoAt(0).sentenceBegin
//										+ " " + item.getSentenceInfoAt(0).sentenceEnd);

								//System.out.println("<<<<<<"+rightKey.toString());
								outputNGramR.put(rightKey.toString(), items);
							}


						} else {
							//reachedRightBoarder = true;
						}
					}

				}
			}
		}



		//merge leftSide (outputNGram) with rightSide (outputNGramR)
		outputNGram = mergeResults(outputNGram,outputNGramR);
		//outputNGram.putAll(outputNGramR);


//		for(Iterator<String> i = outputNGram.keySet().iterator(); i.hasNext();){
//			String key = i.next();
//			ArrayList<DependencyItemInNuclei> arrItem = outputNGram.get(key);
//			//only one PoS Tag found -> add to delete list;
//			if (arrItem.size() > 1){
//				System.out.println("ToKeep ---------------> " + key);
//			}
//		}

		nGramCount++;

		if (outputNGram.size() > 0) {

			//items with length one -> no longer variation --> remove
			outputNGram = removeItemsLengthOne(outputNGram);

//			// remove items at the fringe
//			//TODO fixfringe
//			if(nGramCount <= fringeEnd && nGramCount >= fringeSize){
////				System.out.println(nGramCount + " | "
////								+ fringeStart + " | "
////								+ fringeEnd);
//				//outputNGram = distrustFringeHeuristic(outputNGram);
//			}

			//add results into Cache
			nGramCache.putAll(outputNGram);

			// print to console
			nGramResults(outputNGram);


			//continue creating ngrams?
			if (continueNGrams()){
				createNGrams(outputNGram, false, false);
			}
		}
	}




	/**
	 * @param outputNGram
	 * @param outputNGramR
	 * @return
	 */
	private Map<String, ArrayList<DependencyItemInNuclei>> mergeResults(
			Map<String, ArrayList<DependencyItemInNuclei>> outputNGram,
			Map<String, ArrayList<DependencyItemInNuclei>> outputNGramR) {

		Map<String, ArrayList<DependencyItemInNuclei>> result = new LinkedHashMap<String, ArrayList<DependencyItemInNuclei>>();

		result.putAll(outputNGram);

//		//results from left side including merge when same key on rigth
//		for(Iterator<String> it = outputNGram.keySet().iterator(); it.hasNext();){
//			String key = it.next();
//			//merge
//			if (outputNGramR.containsKey(key)){
//				//System.out.println("key" + key);
//				ArrayList<DependencyItemInNuclei> arrL = outputNGram.get(key);
//				arrL.addAll(outputNGramR.get(key));
//				result.put(key, arrL);
//			} else {
//				result.put(key, outputNGram.get(key));
//			}
//		}

		//missing results from right side
		for(Iterator<String> it = outputNGramR.keySet().iterator(); it.hasNext();){
			String key = it.next();
			if(key.equals("Dutch / Shell-Gruppe")){
//				//System.out.println("LEFT" + result.containsKey(key));
//				System.out.println("LEFT" + outputNGram.get(key));
//				System.out.println("RIGHT" + outputNGramR.get(key));
//				ArrayList<DependencyItemInNuclei> right = outputNGramR.get(key);
//				for(int r = 0; r < right.size();r++){
//					DependencyItemInNuclei di = right.get(r);
//					System.out.println(di.getPosTag());
//				}
			}

//			ArrayList<DependencyItemInNuclei> test = new ArrayList<>();
//			if(outputNGram.containsKey(key) && !outputNGramR.containsKey(key)){
//				result.put(key, outputNGram.get(key));
//			}
//			else if(!outputNGram.containsKey(key) && outputNGramR.containsKey(key)){
//				result.put(key, outputNGramR.get(key));
//			}
//			else{
//				ArrayList<DependencyItemInNuclei> left = outputNGram.get(key);
//				ArrayList<DependencyItemInNuclei> right = outputNGramR.get(key);
//				for(int r = 0; r < right.size();r++){
//					DependencyItemInNuclei di = right.get(r);
//					System.out.println(di.getPosTag());
//				}
//			}
//			//System.out.println(test.size());
//			result.put(key, test);


//			//alt (bugged)
			if (!result.containsKey(key)){
//				System.out.println("MergeKEY " +key);
//				System.out.println("TAG " + outputNGram.get(key).get(0).getPosTag());
//				System.out.println(outputNGram.get(key).get(0).getSentenceInfoAt(0).getSentenceHeadIndex());
				result.put(key, outputNGramR.get(key));
			} else {
//				System.out.println("MergeKEY-R " +key);
//				System.out.println("TAG-R " + outputNGramR.get(key).get(0).getPosTag());
//				System.out.println(outputNGramR.get(key).get(0).getSentenceInfoAt(0).getSentenceHeadIndex());


				ArrayList<DependencyItemInNuclei> right = outputNGramR.get(key);
				ArrayList<DependencyItemInNuclei> left = outputNGram.get(key);

				ArrayList<DependencyItemInNuclei> merged = new ArrayList<DependencyItemInNuclei>();
//				System.out.println(key);
//				System.out.println("R "+ right.size());
//				System.out.println("L "+ left.size());
//				merged.addAll(left);
//				merged.addAll(right);
//				System.out.println("M "+ merged.size() );
//				for(int r = 0; r < right.size(); r++){
//					left.add(right.get(r));
//				}

				//mergeDIINList(right,left);

				// for(int r = 0; r < left.size();r++){
				// DependencyItemInNuclei di = left.get(r);
				// System.out.println(di.getPosTag());
				// }
				//
				// System.out.println("---");
				//
				// for(int r = 0; r < right.size();r++){
				// DependencyItemInNuclei di = right.get(r);
				// System.out.println(di.getPosTag());
				// }

				for (int b = 0; b < right.size(); b++) {
					if (left.contains(right.get(b))) {
						DependencyItemInNuclei di = right.get(b);
						// System.out.println("INSIDE " + di.getPosTag());
					}
				}


//
//					//HashMap<Integer,ArrayList<Integer>> tmpSentences= new LinkedHashMap<Integer,ArrayList<Integer>>();
//					HashMap<Integer,Integer> tmpSentences= new LinkedHashMap<Integer,Integer>();
//					for(int l = 0; l < left.size();l++){
//						DependencyItemInNuclei di = right.get(l);
//
//						for(int s = 0;  s < di.getSentenceInfoSize(); s++){
//							int snr = di.getSentenceInfoAt(s).getSentenceNr();
//							if(!tmpSentences.containsKey(snr)){
//								ArrayList<Integer> newSNR = new ArrayList<Integer>();
//								newSNR.add(s);
//								tmpSentences.put(snr, s);
//							}else {
//								//ArrayList<Integer> newSNR = tmpSentences.get(snr);
//								//newSNR.add(s);
//								tmpSentences.put(snr, s);
//							}
//						}
//					}
//					System.out.println(tmpSentences);
//
//					for(int r = 0; r < right.size();r++){
//						DependencyItemInNuclei di = right.get(r);
//
//						for(int s = 0;  s < di.getSentenceInfoSize(); s++){
//							int snr = di.getSentenceInfoAt(s).getSentenceNr();
//							if(tmpSentences.containsKey(snr)){
////								left.get(tmpSentences.get(snr).get(0));
////								ArrayList<Integer> newSNR = tmpSentences.get(snr);
////								newSNR.add(s);
////								tmpSentences.put(snr, newSNR);
//
//								System.out.println("LLIST" + left.get(snr).getSentenceInfoAt(0).getNucleusList());
//								for(Integer nucleusInteger : di.getSentenceInfoAt(s).getNucleusList()){
//									System.out.println(nucleusInteger);
//									if(!left.get(snr).getSentenceInfoAt(0).getNucleusList().contains(nucleusInteger)){
//										left.get(snr).getSentenceInfoAt(0).addNucleiIndexList(nucleusInteger);
//										System.out.println("add " + nucleusInteger);
//									}
//								}
//								System.out.println(di.getSentenceInfoAt(s).getNucleusList());
//
//							}
//						}
//					}
//					System.out.println(tmpSentences);
//
//					//left.get(1).getSentenceInfoAt(0).addNucleiIndexList(6);
//				}

				result.put(key, outputNGram.get(key));
			}
		}

		return result;
	}


	/**
	 * @param right
	 * @param left
	 */
	private void mergeDIINList(ArrayList<DependencyItemInNuclei> right,
			ArrayList<DependencyItemInNuclei> left) {

		System.out.println("MergeList");
		for(int i = 0 ; i < left.size(); i++){
			for(int j = 0 ; j < left.size(); j++){

			}
		}
	}




	/**
	 * @param i
	 * @param dd
	 * @return
	 */
	private String getNucleiForm(int i, DependencyData dd) {
		int headIndex = dd.getHead(i);
		String nucleiTag = null;

		if (headIndex > 0 ){
			nucleiTag = checkForNumber(dd.getForm(i)) + " "  //$NON-NLS-1$
								+ 	getTagQueryDependency(checkForNumber(dd.getForm(headIndex)));


//		System.out.println("NUCLEI: " + checkForNumber(dd.getForm(i))
////					+ " Index " + i
////					+ " Head " + dd.getHead(i)
//					+ " TAG " + nucleiTag
//					+ " " + nGramCache.containsKey(nucleiTag));
		}

		return nucleiTag;
	}


	/**
	 * @param i
	 * @param dd
	 */
	private boolean newNucleiCheck(int i, DependencyData dd) {
		int headIndex = dd.getHead(i);
		String nucleiTag = null;

		if (headIndex > 0 ){
			nucleiTag = checkForNumber(dd.getForm(i)) + " " + getTagQueryDependency(checkForNumber(dd.getForm(headIndex))); //$NON-NLS-1$

//		System.out.println("NUCLEI: " + checkForNumber(dd.getForm(i))
////					+ " Index " + i
////					+ " Head " + dd.getHead(i)
//					+ " TAG " + nucleiTag
//					+ " " + nGramCache.containsKey(nucleiTag));
		}

		return nGramCache.containsKey(nucleiTag);

	}


	/**
	 * Continue until no new NGram found or until NGram Limit is reached
	 * @return
	 */
	private boolean continueNGrams() {
		//0 -> collect all ngrams until algorithm end
		if(nGramLimit == 0){
			return true;
		}

		return (nGramLimit > nGramCount);
	}


	/**
	 * @param item
	 * @param posTag
	 * @param si
	 * @param sitemp
	 * @param inc
	 */
	private void addNucleiRigth(DependencyItemInNuclei item, String posTag,
			DependencySentenceInfo si, DependencySentenceInfo sitemp, boolean inc) {
		if (inc){
			int oldCount = item.getCount();
			item.setCount(oldCount + 1);
		}
		//item.setPosTag(ensureValid(rigthPOS + " "+ iin.getPosTag()));
		item.setPosTag(ensureValid(posTag));
		item.addNewNucleiToSentenceInfoRight(si, sitemp);
	}


	/**
	 * @param item
	 * @param string
	 * @param si
	 * @param sitemp
	 * @param inc
	 */
	private void addNucleiLeft(DependencyItemInNuclei item, String posTag,
			DependencySentenceInfo si, DependencySentenceInfo sitemp, boolean inc) {
		if (inc){
			int oldCount = item.getCount();
			item.setCount(oldCount + 1);
		}
		//item.setPosTag(ensureValid(leftPOS + " "+ iin.getPosTag()));
		item.setPosTag(ensureValid(posTag));
		item.addNewNucleiToSentenceInfoLeft(si, sitemp);
	}



	/**
	 * @param item
	 * @param si
	 * @param posTag
	 * @param inc
	 */
	private void addSentenceInfoRigth(DependencyItemInNuclei item, DependencySentenceInfo si,
			String posTag, boolean inc) {
		if (inc){
			int oldCount = item.getCount();
			item.setCount(oldCount + 1);
		}
		item.setPosTag(ensureValid(posTag));
		item.addNewSentenceInfoRigth(si);
	}


	/**
	 * @param item
	 * @param posTag
	 * @param si
	 */
	private void addSentenceInfoLeft(DependencyItemInNuclei item, String posTag, DependencySentenceInfo si, boolean inc) {
		if (inc){
			int oldCount = item.getCount();
			item.setCount(oldCount + 1);
		}

		item.setPosTag(ensureValid(posTag));
		item.addNewSentenceInfoLeft(si);
	}

	protected String getTagQueryDependency(String qtag){
		String tag = qtag;
		for(int i = 0; i < queryList.size(); i++){
			NGramQAttributes att = queryList.get(i);
			//System.out.println(tag + " vs " + att.getKey());

			//is there a key in querylist?
			if (att.getKey().equals(tag)){
				//System.out.print("Hit " + tag + " vs " + att.getKey());

				//shall we use key?
				if(att.include){

					if(att.getValue().equals("")){ //$NON-NLS-1$
						//reuse old tag
						//System.out.println(" Included Oldtag " + tag);
						return tag;
					} else {
						//use newspecified tag
						//System.out.println(" Included Newtag " + att.getValue());
						return att.getValue();
					}

				} else {
					//ignore tag for search
					//System.out.println(" excluded ");
					return null;
				}
			}
		}
		return tag;
	}


	public Map<String, ArrayList<DependencyItemInNuclei>> getResult(){
		return nGramCache;
	}


	public int getPasses(){
		return nGramCount;
	}



	/**
	 * @throws ParserConfigurationException
	 *
	 */
	public void outputToFile() {
		nGramIO io = new nGramIO();
		try {
			io.nGramsToXMLDependency(nGramCache);
		} catch (TransformerException e) {
			LoggerFactory.log(this,Level.SEVERE, "XML Transform Exeption", e); //$NON-NLS-1$
		} catch (Exception e) {
			LoggerFactory.log(this,Level.SEVERE, "XML Output Exeption", e); //$NON-NLS-1$
		}

	}



	/**
	 * Print out Resulting nGrams:
	 *
	 */

	@SuppressWarnings("nls")
	protected void nGramResults(Map<String, ArrayList<DependencyItemInNuclei>> inputNGram){


//		System.out.print("\n###################################\n"+ nGramCount + "-Gram: ");
//		System.out.println("Found " + inputNGram.size() + " different nGrams");

//		for(Iterator<String> i = inputNGram.keySet().iterator(); i.hasNext();){
//			String key = i.next();
//			ArrayList<DependencyItemInNuclei> arrItem = inputNGram.get(key);
//
//			System.out.println("\n### Wordform: " + key + " ###");
//			for (int j = 0; j < arrItem.size();j++){
//				DependencyItemInNuclei iin = arrItem.get(j);
//				System.out.println("PoSTag: "+ iin.getPosTag() +
//								  "  PoSCount: " + iin.getCount());
//
//				for (int k = 0; k < iin.getSentenceInfoSize(); k++){
//					System.out.print("SentenceNr: " + iin.getSentenceInfoAt(k).getSentenceNr());
//					System.out.print(" | NucleiCount: " + iin.getSentenceInfoAt(k).getNucleiIndexListSize());
//					System.out.print(" NucleiIndex: " + iin.getSentenceInfoAt(k).getNucleiIndex());
//					System.out.print(" NucleiPos: "); printNuclei((DependencySentenceInfo) iin.getSentenceInfoAt(k));
//					System.out.print(" HeadIndex: " + iin.getSentenceInfoAt(k).getSentenceHeadIndex());
//					System.out.print(" Begin: " + iin.getSentenceInfoAt(k).getSentenceBegin());
//					System.out.println(" End: " + iin.getSentenceInfoAt(k).getSentenceEnd());
//				}
//			}
//		}
	}



	/**
	 * Print out Resulting nGrams:
	 *
	 */

	@SuppressWarnings("nls")
	public void nGramResults(){

		//TODO enable NIL Heuristic
		/**
		//prepare nilCache - remove all unique sentences (length only occur 1x)
		removeItemsSnr(nilCache);
		//build list nilEqualSentencesCache with same sentences
		//same = every token has the same wordform.
		createEqualSentences(nilCache);

		addNilLabels(nGramCache);
		*/

//		System.out.println("Equal Sentences---------");
//		for (Integer snr : nilEqualSentencesCache.keySet()){;
//			System.out.println(snr + ": " + nilEqualSentencesCache.get(snr));
//		}

		//fringe for dependency = nil heuristic
		if(useFringe){
			generateNIL(nGramCache);
		}




//		System.out.println("Corpussize: " + corpus.size());

//		for (Iterator<Entry<String, ArrayList<DependencyItemInNuclei>>> it = nGramCache.entrySet().iterator();
//				it.hasNext(); ){
//			System.out.println(it.next().getKey());
//		}

		System.out.print("NGramsize " + nGramCount + " ");
		System.out.println("Found " + nGramCache.size() + " uniGrams");
		removeItemsLengthOne(nGramCache);
		System.out.println("Remaining " + nGramCache.size() + " filtered uniGrams");

//		for(Iterator<String> i = nGramCache.keySet().iterator(); i.hasNext();){
//			String key = i.next();
//			ArrayList<DependencyItemInNuclei> arrItem = nGramCache.get(key);
//
//			System.out.println("\n### Wordform: " + key + " ###");
//			for (int j = 0; j < arrItem.size();j++){
//				DependencyItemInNuclei iin = arrItem.get(j);
//				System.out.println("PoSTag: "+ iin.getPosTag() +
//								  " PoSCount: " + iin.getCount());
//
//				for (int k = 0; k < iin.getSentenceInfoSize(); k++){
//					System.out.print("SentenceNr: " + iin.getSentenceInfoAt(k).getSentenceNr());
//					System.out.print(" NucleiCount: " + iin.getSentenceInfoAt(k).getNucleiIndexListSize());
//					System.out.print(" NucleiIndex: " + iin.getSentenceInfoAt(k).getNucleiIndex());
//					System.out.print(" NucleiPos: "); printNuclei((DependencySentenceInfo) iin.getSentenceInfoAt(k));
//					System.out.print(" HeadIndex: " + iin.getSentenceInfoAt(k).getSentenceHeadIndex());
//					System.out.print(" Begin: " + iin.getSentenceInfoAt(k).getSentenceBegin());
//					System.out.println(" End: " + iin.getSentenceInfoAt(k).getSentenceEnd());
//				}
//			}
//		}

		//TO DO change false false (4 running both directions (l/r))
		createNGrams(nGramCache, false, false);
	}


	/**
	 * @param nGra´ßmCache2
	 */
	private void generateNIL(Map<String, ArrayList<DependencyItemInNuclei>> ngrams) {

//		System.out.println(nilLexiconCache.keySet());
//		System.out.println("von" + nilLexiconCache.get("von"));
//		System.out.println("UN" + nilLexiconCache.get("UN"));



		//we have to check the entire keyset
		//nevertheless only pairs where one "real" edge occurs are interesting
		for(String key : ngrams.keySet()){
			//System.out.println("KEY> "+ key + ": ");

			//keySplit[0] = wordform nodeA; [1] = wordform nodeB
			String[] keySplit = key.split(" "); //$NON-NLS-1$

			//List including all sentenceNo where the current keypair occurs
			List<Integer> list = sentencesWithSameKeypair(keySplit);

			boolean nilAdded = false;

			if(list.size() > 1){
				//System.out.println(key + list);

				for (int i = 0; i < list.size(); i++) {
					int corpusIndex = list.get(i);

					DependencyData dd = corpus.get(corpusIndex);
					// System.out.println("CI"+corpus.indexOf(dd));

					List<DependencyItemInNuclei> diinL = ngrams.get(key);

					// for(int i = 0; i < diinL.size(); i++){
					DependencyItemInNuclei diin = diinL.get(0);

					NilItem nilItem = checkNIL(dd, key, diin);

					if (nilItem.isNil()) {
						// DependencyItemInNuclei diin = diinL.get(0);

						int head = diin.getSentenceInfoAt(0).getSentenceHeadIndex();
						int dependent = diin.getSentenceInfoAt(0).getNucleiIndex();
						// System.out.println("A" + nilItem.getA() + " B" +
						// nilItem.getB());

						if(!nilAdded){
							DependencyItemInNuclei newDIIN = new DependencyItemInNuclei();
							if (head < dependent) {
								newDIIN.addNewDependencySentenceInfoUniGrams(corpusIndex, nilItem.getA(), nilItem.getB());
							} else {
								newDIIN.addNewDependencySentenceInfoUniGrams(corpusIndex, nilItem.getB(), nilItem.getA());
							}
							newDIIN.setPosTag("nil"); //$NON-NLS-1$
							diinL.add(newDIIN);
							nilAdded = true;

						} else {
							for(int w = 0; w < diinL.size(); w++){
								DependencyItemInNuclei diTemp = diinL.get(w);
								if(diTemp.getPosTag().equals("nil")){ //$NON-NLS-1$
//									diTemp.addNewDependencySentenceInfoUniGrams(
//											corpusIndex,
//											nilItem.getA(),
//											nilItem.getB());
									if (head < dependent) {
										diTemp.addNewDependencySentenceInfoUniGrams(corpusIndex, nilItem.getA(), nilItem.getB());
									} else {
										diTemp.addNewDependencySentenceInfoUniGrams(corpusIndex, nilItem.getB(), nilItem.getA());
									}
									diTemp.count++;
								}
							}
						}



//						if (head < dependent) {
//							//DependencyItemInNuclei newDIIN = new DependencyItemInNuclei();
//							newDIIN.addNewDependencySentenceInfoUniGrams(
//									corpusIndex, nilItem.getA(), nilItem.getB());
//							newDIIN.setPosTag("nil"); //$NON-NLS-1$
//							diinL.add(newDIIN);
//						} else {
//							//DependencyItemInNuclei newDIIN = new DependencyItemInNuclei();
//							newDIIN.addNewDependencySentenceInfoUniGrams(
//									corpusIndex, nilItem.getB(), nilItem.getA());
//							newDIIN.setPosTag("nil"); //$NON-NLS-1$
//							diinL.add(newDIIN);
//						}
//						// System.out.println("nil" + key + " " + nilItem.getA()
//						// + nilItem.getB());
					}
				}
			}
		}
	}


	/**
	 * Use the local wordlexicon to look up the set of sentences where the
	 * the specified wordpair occur. The intersection set is returned.
	 * (Of course only sentences where both words occure are interesting when
	 * looking for nil edges.)
	 * @param keySplit
	 * @return
	 */
	private List<Integer> sentencesWithSameKeypair(String[] keyPair) {
		List<Integer> tempList = new ArrayList<Integer>();
		tempList.addAll(nilLexiconCache.get(keyPair[0]));

//		System.out.println("a:" + keyPair[0] + " b:" + keyPair[1]);
//		System.out.println("a:" + nilLexiconCache.get(keyPair[0]));
//		System.out.println("b:" + nilLexiconCache.get(keyPair[1]));

		tempList.retainAll(nilLexiconCache.get(keyPair[1]));
		//System.out.println("tl:" + tempList);

		return tempList;
	}


	/**
	 * @param dd
	 * @param diin
	 * @param dependent
	 * @param headIndex
	 * @param headIndex
	 * @return
	 * @return
	 */
	private NilItem checkNIL(DependencyData dd, String key, DependencyItemInNuclei diin) {

		String[] keySplit = key.split(" "); //$NON-NLS-1$

		List<Integer> indexOfTokens = new ArrayList<Integer>();
		List<Integer> indexOfTokens2 = new ArrayList<Integer>();

		NilItem nilItem;

		//System.out.println("k0: " + keySplit[0] + " k1 " + keySplit[1]); //$NON-NLS-1$ //$NON-NLS-2$
		for(int d = 0; d < dd.length(); d++){
			//System.out.println(checkForNumber(dd.getForm(d)) + dIndex + " " + hIndex);
			if(checkForNumber(dd.getForm(d)).equals(keySplit[0])){
				indexOfTokens.add(d);
//				if(dd.getHead(d) == -1){
//					System.out.println(checkForNumber(dd.getForm(d)) + " nil");
//					indexOfTokens.add(d);
//				} else {
//					System.out.println(checkForNumber(dd.getForm(dd.getHead(d))));
//					if(!checkForNumber(dd.getForm(dd.getHead(d))).equals(keySplit[1])){
//						//System.out.println(nGramCache.containsKey(checkForNumber(dd.getForm(d))
						//+ " " + checkForNumber(dd.getForm(dd.getHead(d)))));
//						//System.out.println("D>"+checkForNumber(dd.getForm(d)) + " " + checkForNumber(dd.getForm(dd.getHead(d))));
//						indexOfTokens.add(d);
//					}
//				}
			}

			else if(checkForNumber(dd.getForm(d)).equals(keySplit[1])){
				indexOfTokens2.add(d);
			}
		}

//		System.out.println(indexOfTokens);
//		System.out.println(indexOfTokens2);
//		System.out.println("HCHECK "  //$NON-NLS-1$
//							+ recieveHead(dd, indexOfTokens, indexOfTokens2)
//							+ " : "  //$NON-NLS-1$
//							+ recieveHead(dd, indexOfTokens2, indexOfTokens));

		// second check both lists should contain more than one entry
		// already ensured because only sentences that contain the keypair
		// are passed
		if(indexOfTokens.size() > 0 && indexOfTokens2.size() > 0){
			//check both directions
			boolean nil = !recieveHead(dd, indexOfTokens, indexOfTokens2)
			 				&& !recieveHead(dd, indexOfTokens2, indexOfTokens);
			//System.out.println("FOUND NIL" + nil);
			//System.out.println(key);

			nilItem = new NilItem(nil, getIndex2Key(dd, keySplit[0], indexOfTokens), getIndex2Key(dd, keySplit[1],indexOfTokens2));
			return nilItem;
		}

		return new NilItem(false, 1, 1);
	}




	/**
	 * @param dd
	 * @param diin
	 * @param diin
	 * @param string
	 * @param indexOfTokens
	 * @return
	 */
	private int getIndex2Key(DependencyData dd, String key, List<Integer> list) {
		for(Integer i : list){
			if(checkForNumber(dd.getForm(i)).equals(key)){
				return i+1;
			}
		}
		return -1;
	}


	/**
	 * @param dd
	 * @param indexOfTokens
	 * @param indexOfTokens2
	 */
	private boolean recieveHead(DependencyData dd, List<Integer> l1,
			List<Integer> l2) {
		for (Integer i : l1) {
			if (dd.getHead(i) != -1) {
				if (l2.contains(dd.getHead(i))) {
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * @param nilCache2
	 */
	private void createEqualSentences(Map<Integer, ArrayList<Integer>> nilCache) {
		nilEqualSentencesCache = new LinkedHashMap<>();

		for (Integer length : removeItemsSnr(nilCache).keySet()){

			for (int snr = 0; snr < nilCache.get(length).size(); snr++) {
				List<Integer> tmp = nilCache.get(length);

				List<Integer> result = sentenceMultipleOccurence(tmp.get(snr), tmp);
				if(result.size() > 0){
					nilEqualSentencesCache.put(tmp.get(snr), (ArrayList<Integer>) result);
				}
				//System.out.println("Test " + tmp.get(snr) +" " + sentenceMultipleOccurence(tmp.get(snr), tmp) + tmp);
			}
		}
	}


	/**
	 * @param snr
	 * @param tmp
	 */
	private List<Integer> sentenceMultipleOccurence(int snr, List<Integer> tmp) {
		DependencyData dd = corpus.get(snr);

		List<Integer> equalSentences = new ArrayList<Integer>();

		for(int i = 0; i < tmp.size(); i++){
			//dont compare same sentences
			if(snr != tmp.get(i)){
				DependencyData ddTemp = corpus.get(tmp.get(i));

				if(isEqualDependencyData(dd,ddTemp)){
					equalSentences.add(tmp.get(i));
				}
				//System.out.println("snr " + snr + " compare " + tmp.get(i));
			}
		}
		return equalSentences;
	}


	/**
	 * @param dd
	 * @param ddTemp
	 * @return
	 */
	private boolean isEqualDependencyData(DependencyData dd, DependencyData ddTemp) {

		int midIndex = dd.length()/2;

		//check if form in the middle is the same
		if(!checkForNumber(dd.getForm(midIndex)).equals(checkForNumber(ddTemp.getForm(midIndex)))){
			return false;
		} else {
			for(int index = 0; index < dd.length(); index++){
				if(!checkForNumber(dd.getForm(index)).equals(checkForNumber(ddTemp.getForm(index)))){
					return false;
				}
			}
		}
		return true;
	}


	@SuppressWarnings("unused")
	private void printNuclei(DependencySentenceInfo sentenceInfo){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < sentenceInfo.getNucleiIndexListSize(); i++){
			sb.append(sentenceInfo.getNucleiIndexListAt(i)).append(" ");			 //$NON-NLS-1$
		}
		sb.append(" | "); //$NON-NLS-1$
		System.out.print(sb.toString());
	}



	public static void main(String[] args) throws UnsupportedFormatException {

		//do not change sentenceindex!!
		int sentenceNr = 1;


		//18 Sentences
		//String  inputFileName = "E:\\test_small_modded.txt"; //$NON-NLS-1$
		//String  inputFileName = "E:\\test_small_mod_overlap.txt"; //$NON-NLS-1$
		String  inputFileName = "E:\\overlap_error.txt"; //$NON-NLS-1$

		//CONLL Training English (1334 Sentences)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-development.txt";

		//CONLL Training English (39279 Sentences)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-train.txt";

		//CONLL Training German 50472 Sentences (Aug)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\tiger_release_aug07.corrected.conll09.txt";

		//CONLL Training German 50472 Sentences (Aug)
		//String  inputFileName = "E:\\tiger_release_aug07.corrected.16012013.conll09";


		int sentencesToRead = 2;

		Path file = Paths.get(inputFileName);

		CONLL09SentenceDataGoldReader conellReader = new CONLL09SentenceDataGoldReader();
		DefaultFileLocation dloc = new DefaultFileLocation(file);
		Options on = new Options();
		on.put("FringeSTART", 3); //$NON-NLS-1$
		on.put("FringeEND", 5); //$NON-NLS-1$ // 0 = infinity , number = limit
		on.put("NGramLIMIT", 0); //$NON-NLS-1$
		on.put("UseFringe", true); //$NON-NLS-1$
		on.put("UseNumberWildcard", false); //$NON-NLS-1$

		NGramsDependency ngrams = new NGramsDependency(1, on);
		try {

			conellReader.init(dloc, on);
			//while (cr.next() != null) {
			for(int i = 0; i < sentencesToRead; i++){
				DependencyData dd = (DependencyData) conellReader.next();
				ngrams.initializeUniGrams(dd, sentenceNr);
				sentenceNr++;
			}
			ngrams.nGramResults();

			ngrams.outputToFile();

			System.out.println("Finished nGram Processing"); //$NON-NLS-1$

		} catch (IOException e) {
			System.out.println("Main Debug IOExeption"); //$NON-NLS-1$
			e.printStackTrace();
		} catch (UnsupportedLocationException e) {
			System.out.println("Main Debug UnsupportedLocationException"); //$NON-NLS-1$
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Main Debug Exception"); //$NON-NLS-1$
			e.printStackTrace();
		}
	}


}
