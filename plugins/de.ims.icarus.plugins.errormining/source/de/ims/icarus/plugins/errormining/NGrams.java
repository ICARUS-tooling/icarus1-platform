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

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.matetools.conll.CONLL09SentenceDataGoldReader;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.location.DefaultFileLocation;
import de.ims.icarus.util.location.UnsupportedLocationException;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGrams {

	//options
	protected int nGramCount;
	protected int fringeSize;
	protected int nGramLimit;
	protected boolean useFringe;
	protected boolean useNumberWildcard;

	//protected List<ItemInNuclei> items;
	protected Map<String, ArrayList<ItemInNuclei>> nGramCache;
	protected List<NGramQAttributes> queryList;
	protected Options options;

	protected List<DependencyData> corpus;

	private boolean usedFringe = false;
	private static Pattern numberPattern = Pattern.compile("^[0-9]"); //$NON-NLS-1$
	private static String numberString = "[number-wildcard]"; //$NON-NLS-1$


	private static final int pos_flag = 1;  // Binary 00001
	private static final int dependency_flag = 2;  // Binary 00010
	private static final int form_flag = 4;  // Binary 00100
	private static final int lemma_flag = 8;  // Binary 01000

	private int miningMode = 0;



	private static NGrams instance;

	public static NGrams getInstance() {
		if (instance == null) {
			synchronized (NGrams.class) {
				if (instance == null) {
					instance = new NGrams();
				}
			}
		}
		return instance;
	}


	//Debug
	public NGrams(){
//		Options options = new Options();
//		options = Options.emptyOptions;
//
//		this.nGramCount = 1; //normally we start with unigrams so n will be 1
//
//		//0 collect ngrams until no new ngrams are found
//		this.nGramLimit = options.getInteger("NGramLIMIT");  //$NON-NLS-1$
//		this.fringeStart = options.getInteger("FringeSTART");  //$NON-NLS-1$
//		this.fringeEnd = options.getInteger("FringeEND");  //$NON-NLS-1$
//		this.useFringe = options.getBoolean("UseFringe"); //$NON-NLS-1$
//
//		nGramCache = new LinkedHashMap<String,ArrayList<ItemInNuclei>>();
//		corpus = new ArrayList<DependencyData>();
	}


	/**
	 * Constructor for NGramSearch (main plugin)
	 * @param options
	 * @param queryList
	 */
	public NGrams(Options options, List<NGramQAttributes> queryList){

		this.options = options;

		this.nGramCount = 1; //normally we start with unigrams so n will be 1

		this.queryList = queryList;

//		System.out.println("Options limit " + options.getInteger("NGramLIMIT"));
//		System.out.println("Options f-start " + options.getInteger("FringeSTART"));
//		System.out.println("Options f-end " + options.getInteger("FringeEND"));
//		System.out.println("Options fringe " + options.getBoolean("UseFringe"));
//		System.out.println("Options NumberWC " + options.getBoolean("UseNumberWildcard"));

		//option defined in NGramSearch createOptions();

		this.fringeSize = options.getInteger("FringeSIZE"); //$NON-NLS-1$
		this.useFringe = options.getBoolean("UseFringe"); //$NON-NLS-1$
		this.nGramLimit = options.getInteger("NGramLIMIT"); //$NON-NLS-1$
		this.useNumberWildcard = options.getBoolean("UseNumberWildcard"); //$NON-NLS-1$

		nGramCache = new LinkedHashMap<String,ArrayList<ItemInNuclei>>();
		corpus = new ArrayList<DependencyData>();

		miningMode = setMiningMode();
		System.out.println(miningMode);
	}


	/**
	 * @return
	 */
	private int setMiningMode() {
		String tag = ConfigRegistry.getGlobalRegistry().getString(
				"plugins.errorMining.tagmining.variationTag"); //$NON-NLS-1$

		String[] stringArray = tag.split("#"); //$NON-NLS-1$

		for(String key : stringArray){
			switch (key) {
			case "pos": //$NON-NLS-1$
				miningMode = miningMode | pos_flag;
				break;
			case "dependency": //$NON-NLS-1$
				miningMode = miningMode | dependency_flag;
				break;
			case "form": //$NON-NLS-1$
				miningMode = miningMode | form_flag;
				break;
			case "lemma": //$NON-NLS-1$
				miningMode = miningMode | lemma_flag;
			}
		}
		return miningMode;
	}


	public NGrams(int nGramCount, Options options){

		if (options == null) {
			options = Options.emptyOptions;
		}

		this.nGramCount = nGramCount; //normally we start with unigrams so n will be 1

		this.queryList = new ArrayList<NGramQAttributes>();
//		this.useFringe = true;

//		System.out.println("Options limit " + options.getInteger("NGramLIMIT"));
//		System.out.println("Options f-start " + options.getInteger("FringeSTART"));
//		System.out.println("Options f-end " + options.getInteger("FringeEND"));
//		System.out.println("Options fringe " + options.getBoolean("UseFringe"));
//		System.out.println("Options NumberWC " + options.getBoolean("UseNumberWildcard"));

//		//0 collect ngrams until no new ngrams are found
		this.nGramLimit = options.getInteger("NGramLIMIT");  //$NON-NLS-1$
		this.fringeSize = options.getInteger("FringeSIZE");  //$NON-NLS-1$
		this.useFringe = options.getBoolean("UseFringe"); //$NON-NLS-1$
		this.useNumberWildcard = options.getBoolean("UseNumberWildcard"); //$NON-NLS-1$

		nGramCache = new LinkedHashMap<String,ArrayList<ItemInNuclei>>();
		corpus = new ArrayList<DependencyData>();
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
	protected SentenceInfo returnSentenceInfoNREqual(ArrayList<ItemInNuclei> l1,
										int sentenceNR){

		for (int i = 0; i < l1.size(); i++){
			ItemInNuclei item = l1.get(i);

			for (int j = 0 ; j < item.getSentenceInfoSize(); j++){
				SentenceInfo si = item.getSentenceInfoAt(j);
				if(si.getSentenceNr() == sentenceNR){
					//maybe list?!
					//System.out.println("NucleiToBeAdded " + si.getNucleiSentencePositionAt(0));
					return si;
				}
			}
		}
		return null;
	}




	public List<String>  getVariationForTag(String tag){
		List<String> resultVari = null;

		if (nGramCache.containsKey(tag)){
			resultVari = new ArrayList<String>();
			ArrayList<ItemInNuclei> arri = nGramCache.get(tag);


			for(int i = 0; i < arri.size(); i++){
				ItemInNuclei iin = arri.get(i);
				resultVari.add(iin.getPosTag());
			}

		}

		//Debug
		//System.out.println(tag + " : " + resultVari); //$NON-NLS-1$
		return resultVari;

	}



//	protected ArrayList<ItemInNuclei> leftSide(ArrayList<ItemInNuclei> items,
//													SentenceInfo si, String newPos){
//
//		boolean knownTag = false;
//
//		for (int i = 0; i < items.size(); i++) {
//			// increment when tag found again
//			ItemInNuclei item = items.get(i);
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
//			ItemInNuclei item = new ItemInNuclei();
//			//item.setPosTag(ensureValid(getTagQuery(getTag(dd,wordIndex))));
//			item.addNewSentenceInfoLeft(si);
//			items.add(item);
//		}
//
//		return items;
//	}


	/**
	 * Step 1) Initialize Corpus / Create uniGrams
	 * Loop trough the Corpus and add all occuring Words with their specific PoSTags
	 * to the nGramCache. Also Store CorpusPosition (SentenceNr/Wordpositon-in-Sentence)
	 * If one Word has more than one Tag assigned add the new Tag aswell to the Wordform.
	 * Step 2) we will only use that unigrams whicht contains at least 2 different PoS Tags.
	 *
	 * @param dd
	 * @param sentenceNr
	 */
	public void initializeUniGrams(DependencyData dd, int sentenceNr) {

		//TODO should be removed later
		corpus.add(dd);

		for (int wordIndex = 0; wordIndex < dd.length(); wordIndex++) {
			String currentWord = checkForNumber(dd.getForm(wordIndex));
			//checkForNumber(dd.getForm(wordIndex));
			//System.out.print(currentWord + " ");

			//System.out.println(dd.getForm(wordIndex) + ": " + getDependencyLabel(dd, wordIndex));


			// item already in list? only add new tags
			if (nGramCache.containsKey(currentWord)) {

				if (getTagQuery(getTag(dd,wordIndex)) != null){
					ArrayList<ItemInNuclei> items = nGramCache.get(currentWord);

					boolean knownTag = false;

					for (int i = 0; i < items.size(); i++) {
						// increment when tag found again
						ItemInNuclei item = items.get(i);
						//System.out.println(item.getPosTag() +" vs "+ getTagQuery(getTagQuery(getTag(dd,wordIndex))));
						if (item.getPosTag().equals(getTagQuery(getTag(dd,wordIndex)))) {
							int oldCount = item.getCount();
							item.setCount(oldCount + 1);
							item.addNewSentenceInfoUniGrams(sentenceNr, wordIndex+1);
							knownTag = true;
						}
					}

					// new pos tag found; add to list
					if (!knownTag) {
						//System.out.println("faulty Tag@ " + sentenceNr + " " + sentencelength);
						ItemInNuclei item = new ItemInNuclei();
						item.setPosTag(ensureValid(getTagQuery(getTag(dd,wordIndex))));
						item.addNewSentenceInfoUniGrams(sentenceNr, wordIndex+1);
						items.add(item);
					}
				}
			} else {
				if (getTagQuery(getTag(dd,wordIndex)) != null){
					ArrayList<ItemInNuclei> items = new ArrayList<ItemInNuclei>();
					ItemInNuclei item = new ItemInNuclei();
					item.setPosTag(ensureValid(getTagQuery(getTag(dd,wordIndex))));
					item.addNewSentenceInfoUniGrams(sentenceNr, wordIndex+1);
					items.add(item);

					nGramCache.put(currentWord, items);
				}
			}
		}
	}


	/**
	 * @param dd
	 * @param wordIndex
	 * @return
	 */
	private String getTag(DependencyData dd, int wordIndex) {

		switch (miningMode) {

		//pos (1)
		case 1:
			return dd.getPos(wordIndex);

		//dependency (2)
		case 2:
			return getDependencyLabel(dd, wordIndex);

		//pos(1) + dependency(2)
		case 3:
			String pd = dd.getPos(wordIndex)
					+ "_" + getDependencyLabel(dd, wordIndex); //$NON-NLS-1$
			return pd;

		//form(4)
		case 4:
			return dd.getForm(wordIndex);

		//pos(1) + form(4)
		case 5:
			String pf = dd.getPos(wordIndex) + "_" + dd.getForm(wordIndex); //$NON-NLS-1$
			return pf;

		//dependency(2) + form(4)
		case 6:
			String df = getDependencyLabel(dd, wordIndex)
						+ "_" + dd.getForm(wordIndex); //$NON-NLS-1$
			return df;

		//pos(1) + dependency(2) + form(4)
		case 7:
			String pdf = dd.getPos(wordIndex)
						+ "_" + getDependencyLabel(dd, wordIndex)  //$NON-NLS-1$
						+ "_" + dd.getForm(wordIndex); //$NON-NLS-1$
			return pdf;

		//lemma(8)
		case 8:
			return dd.getLemma(wordIndex);

		//pos(1) + lemma(8)
		case 9:
			String pl = dd.getPos(wordIndex) + "_" + dd.getLemma(wordIndex); //$NON-NLS-1$
			return pl;

		//dependency(2) + lemma(8)
		case 10:
			String dl = getDependencyLabel(dd, wordIndex)
						+ "_" + dd.getLemma(wordIndex); //$NON-NLS-1$
			return dl;

		//default normales pos mining (fallback)
		default:
			return dd.getPos(wordIndex);
		}
	}


	/**
	 * @param dd
	 * @param wordIndex
	 * @return
	 */
	private String getDependencyLabel(DependencyData dd, int wordIndex) {

		int offset = wordIndex - dd.getHead(wordIndex);
		StringBuilder sb = new StringBuilder();

		sb.append(dd.getRelation(wordIndex));

		if(!sb.toString().equals("ROOT")){ //$NON-NLS-1$
			sb.append("_").append(offset); //$NON-NLS-1$
//			if(wordIndex != -1){
//				sb.append("_").append(dd.getForm(dd.getHead(wordIndex))); //$NON-NLS-1$
//			}
		}

		return sb.toString();
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
	 * Step 2: Filter uniGrams
	 * nGramCache contains all items with an PoS Tag. If only one type of
	 * PoS Tag is assigned to a Word this won't be an error at all becouse
	 * only one PoS Tag is assigned at all. Calling this Method removes all
	 * Wordtokens with only one PoS Tag. (equals Step 2 Dickinson)
	 */

	private Map<String, ArrayList<ItemInNuclei>> removeItemsLengthOne(Map<String,ArrayList<ItemInNuclei>> input){
		ArrayList<String> removeFromNGrams = new ArrayList<String>();
		for(Iterator<String> i = input.keySet().iterator(); i.hasNext();){
			String key = i.next();
			ArrayList<ItemInNuclei> arrItem = input.get(key);
			//System.out.print("\n" + key);
			//only one PoS Tag found -> add to delet list;

			//System.out.println("RL1: >>> " + key + " key " + arrItem.size() );

			if (arrItem.size() == 1){

				//ItemInNuclei iin = arrItem.get(0);


//				//simple firsat path
				if(nGramCount == 1){
//					System.out.println(key + " <------ remove  " + iin.getCount()
//										+ " NGCOUNT " + nGramCount );
					removeFromNGrams.add(key);
				}

				//FIXME enough to check length?!
				if (nGramCount > 1) {
//					System.out.println(key + " <------ remove  " + iin.getCount()
//										+ " NGCOUNT " + nGramCount );
					removeFromNGrams.add(key);
				}

//
//				//At this point we have exactly ONE sentenceinfo
//				// otherwise the ArrItemsize wont be 1.....
//				ItemInNuclei iin = arrItem.get(0);
//
//				int nc = 0;
//				boolean remove = true;
//				String[] ks = key.split(" "); //$NON-NLS-1$
//
//
//				for(int k = 0; k < ks.length; k++){
//
//					List<String> tmpTags = new ArrayList<String>();
//
//					for(int s = 0; s < iin.getSentenceInfoSize(); s++){
//						SentenceInfo si = iin.getSentenceInfoAt(s);
//							if(nGramCache.containsKey(ks[k])){
//								checkKeyVariation(tmpTags, ks[k],si);
//							}
//						nc = Math.max(nc, si.getNucleiIndexListSize());
//					}
//					System.out.println("Key+Tagset "+ ks[k]+" " + tmpTags.toString());
//					//nc = si.getNucleiIndexListSize();
//
//
//					if(tmpTags.size() > nc){
//					//System.out.println("keep >>>> " + key);
//					remove = false;
//					}
//
//				}
//
//				if(remove){
//					System.out.println("remove " + key);
//					removeFromNGrams.add(key);
//				}


//				if(tmpTags.size() <= nc){
//					System.out.println("remove " + key);
//					removeFromNGrams.add(key);
//				}

//				System.out.print(nc + "  > ");
//				System.out.println(tmpTags.toString());
//				}
			}
		}

		//System.out.println("Items to Remove: " + removeFromnGrams.size());
		for(int i = 0; i < removeFromNGrams.size(); i++){
			input.remove(removeFromNGrams.get(i));
		}

		removeFromNGrams.clear();
		return input;
	}


	/**
	 * @param tmpTags
	 * @param string
	 * @param si
	 */
	private void checkKeyVariation(List<String> tmpTags, String key, SentenceInfo si) {

		ArrayList<ItemInNuclei> iiN = nGramCache.get(key);

		for(int i = 0; i < iiN.size(); i++){
			ItemInNuclei in = iiN.get(i);

			for(int s = 0; s < in.getSentenceInfoSize(); s++){
				SentenceInfo sentenceInfo = in.getSentenceInfoAt(s);

				if(sentenceInfo.getSentenceNr() == si.getSentenceNr()){
					//System.out.println(" ReturnTag " + in.getPosTag());
					if (!tmpTags.contains(in.getPosTag())){
						tmpTags.add(in.getPosTag());
					}
				}
			}
		}
	}


	protected boolean isNuclei(String key) {

		if (nGramCache.containsKey(key)){
			//not found = color orange
			return true;
		}

		//not found = color black
		return false;
	}


	/**
	 * Examine only non-fringe nuclei, fringe = edge of variation n-gram
	 *
	 * @param input
	 * @return
	 */
	private Map<String, ArrayList<ItemInNuclei>> distrustFringeHeuristic(Map<String,ArrayList<ItemInNuclei>> input){

		ArrayList<String> removeFringeFromNGrams = new ArrayList<String>();

		for(Iterator<String> i = input.keySet().iterator(); i.hasNext();){
			String key = i.next();
			ArrayList<ItemInNuclei> arrItem = input.get(key);
			//System.out.println(key);

			int nonfringeItems = 0;

			String[] keySplitted = key.split(" "); //$NON-NLS-1$
			for (int k = fringeSize; k < keySplitted.length-fringeSize; k++) {
				if(containsNonFringe(k, arrItem)){
					nonfringeItems++;
				}
			}

			//count > 0 => we have at least one item that is no fringe
			if(nonfringeItems == 0){
				if(!removeFringeFromNGrams.contains(key)){
					//System.out.println("FringeKey " + key);
					removeFringeFromNGrams.add(key);
				}
			}

//			for(int j = 0; j < arrItem.size(); j++){
//
//
//				ItemInNuclei iin = arrItem.get(j);
//				System.out.println(iin.getPosTag());

//				for (int s = 0; s < iin.getSentenceInfoSize(); s++){
//					SentenceInfo si = iin.getSentenceInfoAt(s);
//
//					/*
//					 * we only want to remove items where we have fringe;
//					 * when there is a ngram with more than one nuclei we check
//					 * for every nuclei if its fringe, if there is at most
//					 * one nuclei NOT fringe we will keep the ngram no matter if
//					 * the others are fringe nucleis or not
//					 */
//
//					// new version should work now and only delete fringe
//					// within an ngram with 2+ nuclei if both are fringe
//					if(getFringeItem(si)){
//						//System.out.println("FringeKey " + key);
//						if(!removeFringeFromNGrams.contains(key)){
//							//System.out.println("FringeKey " + key);
//							removeFringeFromNGrams.add(key);
//						}
//					}
//				}
//			}
		}

		//System.out.println("FringeItems to Remove: " + removeFringeFromNGrams.size());
		for(int i = 0; i < removeFringeFromNGrams.size(); i++){
			//System.out.println("Remove " + removeFringeFromNGrams.get(i));
			input.remove(removeFringeFromNGrams.get(i));
		}

		removeFringeFromNGrams.clear();
		return input;

	}



	/**
	 * @param k
	 * @param arrItem
	 */
	private boolean containsNonFringe(int k, ArrayList<ItemInNuclei> iinList) {
		List<String> test = new ArrayList<>();

		for(int i = 0; i < iinList.size(); i++){
			String[] s = iinList.get(i).getPosTag().split(" "); //$NON-NLS-1$
			if(!test.contains(s[k])){
				test.add(s[k]);
			}
		}
		if (test.size() > 1){
			return true;
		}
		return false;
	}


	private boolean containsVariation(int k, ArrayList<ItemInNuclei> iinList) {
		List<String> test = new ArrayList<>();

		for(int i = 0; i < iinList.size(); i++){
			String[] s = iinList.get(i).getPosTag().split(" "); //$NON-NLS-1$
			if(!test.contains(s[k])){
				test.add(s[k]);
			}
		}
		if (test.size() > 1){
			return true;
		}
		return false;
	}


	/**
	 * @param si
	 */
	private boolean getFringeItem(SentenceInfo si) {
		boolean fringe = true;
		int start = si.getSentenceBegin();
		int end = si.getSentenceEnd();

		int nonFringe = si.getNucleiIndexListSize();

		for(int n = 0; n < si.getNucleiIndexListSize(); n++){
			//is fringe?
			fringe = isFringe(si.getNucleiIndexListAt(n), start, end);
//			System.out.print(" Fringe:" + si.getNucleiIndexListAt(n)
//					+ " start:" + start + " end:" + end + " " + fringe + "\n");

			if(fringe){
				nonFringe--;
			}
		}


		// at least one nucleus is non fringe
		if(nonFringe > 0){
			return false;
		} else {
			return true;
		}
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


	private void createNGrams(Map<String, ArrayList<ItemInNuclei>> inputNGram,
									boolean lb, boolean rb){

		Map<String, ArrayList<ItemInNuclei>> outputNGram = new LinkedHashMap<String, ArrayList<ItemInNuclei>>();
		Map<String, ArrayList<ItemInNuclei>> outputNGramR = new LinkedHashMap<String, ArrayList<ItemInNuclei>>();
		boolean reachedLeftBoarder = lb;
		boolean reachedRightBoarder = rb;

		for(Iterator<String> it = inputNGram.keySet().iterator(); it.hasNext();){
			String key = it.next();
			ArrayList<ItemInNuclei> iiArr = inputNGram.get(key);
			//System.out.println(key);

			for (int tagSize = 0 ; tagSize < iiArr.size(); tagSize++){

				ItemInNuclei iin = iiArr.get(tagSize);

				for (int s = 0 ; s < iin.getSentenceInfoSize(); s++){

					SentenceInfo si = iin.getSentenceInfoAt(s);
					//System.out.println("SISIZE"+si.getNucleiIndexListSize());

					//start sentencecount at 1 so decrement
					DependencyData dd = corpus.get(si.getSentenceNr());
					int startIndex = si.getSentenceBegin()-1;
					int endIndex = si.getSentenceEnd()-1;

					//System.out.println("Startindex " + startIndex);

					/*
					System.out.println("Sentence: " + si.getSentenceNr() +
										" NucleiLSize " + si.getNucleiIndexListSize() +
										" NucleiForm " + dd.getForm(si.getNucleiIndexListAt(0)-1));
					*/

					StringBuilder posTagBuilder = new StringBuilder();

					/*
					 * *************************************************************
					 * first: check if item is not the last item in the sentence,
					 * (we can't skip sentence boarders)
					 * second: check if item is already in input Map
					 * *************************************************************
					 */
					if (!reachedLeftBoarder) {

						StringBuilder leftKey = new StringBuilder();
						// new key
						if (startIndex > 0) {

							String leftForm = checkForNumber(dd.getForm(startIndex -1));
							String leftPOS = getTagQuery(getTag(dd,startIndex - 1));

							//check if leftword is found in grams -> add new nucleipos to sentence
							boolean addNewNuclei = nGramCache.containsKey(leftForm);

							leftKey.append(leftForm).append(" ").append(key); //$NON-NLS-1$
							posTagBuilder.append(leftPOS).append(" ").append(iin.getPosTag()); //$NON-NLS-1$
							//System.out.println("Left: "+ leftForm + " " + leftPOS);
							//System.out.println(leftKey + " " + posTagBuilder.toString());

							// extend existing gram with values
							if (outputNGram.containsKey(leftKey.toString())) {

								// leftSide(outputNGram.get(leftKey.toString()), si, leftPOS);
								ArrayList<ItemInNuclei> itemsTemp = outputNGram.get(leftKey.toString());

								boolean knownTag = false;

								for (int i = 0; i < itemsTemp.size(); i++) {
									// increment when tag found again
									ItemInNuclei item = itemsTemp.get(i);
									//System.out.println(item.getPosTag() + " vs "+ leftPOS);

									if (item.getPosTag().equals(posTagBuilder.toString())) {
										if (addNewNuclei){
											SentenceInfo sitemp = returnSentenceInfoNREqual(nGramCache.get(leftForm), si.getSentenceNr());
											addNucleiLeft(item, posTagBuilder.toString(), si, sitemp, true);
											knownTag = true;
										} else {
											addSentenceInfoLeft(item, posTagBuilder.toString(), si, true);
											knownTag = true;
										}
									}
								}

								//outputNGram.put(leftKey.toString(), itemsTemp);


								// new pos tag found; add to list
								if (!knownTag) {
									ItemInNuclei item = new ItemInNuclei();
									//already an unigram
									if (addNewNuclei){
										SentenceInfo sitemp = returnSentenceInfoNREqual(nGramCache.get(leftForm), si.getSentenceNr());
										addNucleiLeft(item, posTagBuilder.toString(), si, sitemp, false);
										itemsTemp.add(item);
									} else {
										addSentenceInfoLeft(item, posTagBuilder.toString(), si, false);
										itemsTemp.add(item);
									}
								}

							} else {
								ArrayList<ItemInNuclei> items = new ArrayList<ItemInNuclei>();
								ItemInNuclei item = new ItemInNuclei();

								//already an unigram
								if (addNewNuclei){
									SentenceInfo sitemp = returnSentenceInfoNREqual(nGramCache.get(leftForm), si.getSentenceNr());
									addNucleiLeft(item, posTagBuilder.toString(), si, sitemp, false);
									items.add(item);
								} else {
									addSentenceInfoLeft(item, posTagBuilder.toString(), si, false);
									items.add(item);
								}
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


						if (endIndex < sentenceSize) {

							String rightForm = checkForNumber(dd.getForm(endIndex + 1));
							String rightPOS = getTagQuery(getTag(dd,endIndex + 1));

							//check if leftword is found in grams -> add new nucleipos to sentence
							boolean addNewNuclei = nGramCache.containsKey(rightForm);

							rightKey.append(key).append(" ").append(rightForm); //$NON-NLS-1$
							posTagBuilder.delete(0, posTagBuilder.length());
							posTagBuilder.append(iin.getPosTag()).append(" ").append(rightPOS); //$NON-NLS-1$

							// extend existing gram with values
							if (outputNGramR.containsKey(rightKey.toString())) {

								// leftSide(outputNGram.get(leftKey.toString()), si, leftPOS);
								ArrayList<ItemInNuclei> itemsTemp = outputNGramR.get(rightKey.toString());

								boolean knownTag = false;

								for (int i = 0; i < itemsTemp.size(); i++) {
									// increment when tag found again
									ItemInNuclei item = itemsTemp.get(i);

									// System.out.println(item.getPosTag()
									// +" vs "+ getTagQuery(getTag(dd,wordIndex)));

									if (item.getPosTag().equals(posTagBuilder.toString())) {
										//int oldCount = item.getCount();
										if (addNewNuclei){
											SentenceInfo sitemp = returnSentenceInfoNREqual(nGramCache.get(rightForm), si.getSentenceNr());
											addNucleiRigth(item, posTagBuilder.toString(), si, sitemp, true);
											knownTag = true;
										} else {
											addSentenceInfoRigth(item, si, posTagBuilder.toString(), true);
											knownTag = true;
										}
									}
								}


								// new pos tag found; add to list
								if (!knownTag) {
									ItemInNuclei item = new ItemInNuclei();
									//already an unigram
									if (addNewNuclei){
										SentenceInfo sitemp = returnSentenceInfoNREqual(nGramCache.get(rightForm), si.getSentenceNr());
										addNucleiRigth(item, posTagBuilder.toString(), si, sitemp, false);
										itemsTemp.add(item);
									} else {
										addSentenceInfoRigth(item, si, posTagBuilder.toString(), false);
										itemsTemp.add(item);
									}
								}

							} else {
								ArrayList<ItemInNuclei> items = new ArrayList<ItemInNuclei>();
								ItemInNuclei item = new ItemInNuclei();

								//already an unigram
								if (addNewNuclei){
									SentenceInfo sitemp = returnSentenceInfoNREqual(nGramCache.get(rightForm), si.getSentenceNr());
									addNucleiRigth(item, posTagBuilder.toString(), si, sitemp, false);
									items.add(item);
								} else {
									addSentenceInfoRigth(item, si, posTagBuilder.toString(), false);
									items.add(item);
								}
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
//		System.out.println("SizeL " + outputNGram.size());
//		System.out.println("SizeR " + outputNGramR.size());
		outputNGram = mergeResults(outputNGram, outputNGramR);
//		System.out.println("SizeMerged " + outputNGram.size());

//		//TODO enable filter option for dependency?
//		if(nGramCount == 4){
//			nGramPoSFilter(outputNGram);
//		}


//		for(Iterator<String> i = outputNGram.keySet().iterator(); i.hasNext();){
//			String key = i.next();
//			ArrayList<ItemInNuclei> arrItem = outputNGram.get(key);
//			//only one PoS Tag found -> add to delete list;
//			if (arrItem.size() > 1){
//				System.out.println("ToKeep ---------------> " + key);
//			}
//		}

		if (outputNGram.size() > 0) {

			// items with length one -> no longer variation --> remove
			outputNGram = removeItemsLengthOne(outputNGram);

			// remove items at the fringe

			if (useFringe) {
				//System.out.println(fringeSize);
				if(nGramCount >= fringeSize*2){
					outputNGram = distrustFringeHeuristic(outputNGram);
					if(!usedFringe) {
						usedFringe = true;
					}
				}

			}

			//add results into Cache
			nGramCache.putAll(outputNGram);


			//continue creating ngrams?
			if (continueNGrams()){
				nGramCount++;
				//TODO print to console
				nGramResults(outputNGram);
				createNGrams(outputNGram, false, false);
			}

		}
	}


	private Map<String, ArrayList<ItemInNuclei>> mergeResults(
			Map<String, ArrayList<ItemInNuclei>> outputNGram,
			Map<String, ArrayList<ItemInNuclei>> outputNGramR) {

		Map<String, ArrayList<ItemInNuclei>> result = new LinkedHashMap<String, ArrayList<ItemInNuclei>>();

		result.putAll(outputNGram);


	for(Iterator<String> it = outputNGramR.keySet().iterator(); it.hasNext();){
		String key = it.next();
		if (result.containsKey(key)){
			//System.out.println(key + " "+ outputNGram.get(key).get(0).getPosTag());
			//System.out.println(key + " "+ outputNGramR.get(key).get(0).getPosTag());
			//need merge
			result.put(key, outputNGram.get(key));
		} else {
			result.put(key, outputNGramR.get(key));
		}
	}

	//TODO add clean up of unused nucleus
	//System.out.println("Checker " + isNucleiList("Dynamics", involvedSentences("General Dynamics")));
//	for(Iterator<String> cleanKey = result.keySet().iterator(); cleanKey.hasNext();){
//		String keyToCheck = cleanKey.next();
//		ArrayList<ItemInNuclei> arrayList = result.get(keyToCheck);
//		//cleanNuclei(arrayList, keyToCheck);
//	}

	return result;
	}





	//TODO method to clean up unecessary nucleus 20.11
	private void cleanNuclei(SentenceInfo si, String cleanKey){
		String[] splitKey = cleanKey.split(" "); //$NON-NLS-1$

		for(String s:splitKey){
			System.out.print(s +  " ### "  //$NON-NLS-1$
								+ cleanKey + " ---> "); //$NON-NLS-1$
			System.out.println(isNucleiList(s, involvedSentences(cleanKey)));

			//general nuclei check
			if(isNucleiList(s, involvedSentences(s))){
				int i = 0;
				if(!isNucleiList(s, involvedSentences(cleanKey))){
					System.out.println("lösche nucleus " + s + i); //$NON-NLS-1$

					si.deleteNucleiAtIndex(i);
					i++;
//					System.out.println(si.getSentenceBegin());
//					System.out.println(si.getSentenceEnd());
//					System.out.println(si.getNucleiIndex());
//					System.out.println(si.getNucleiIndexListSize());
				}
			}
		}
	}



	protected ArrayList<Integer>  involvedSentences(String key){
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		ArrayList<ItemInNuclei> arrL = nGramCache.get(key);
		if (arrL != null){
			for(int i = 0; i < arrL.size(); i++){
				ItemInNuclei iin = arrL.get(i);
				//System.out.println(iin.getPosTag());
				for(int j = 0; j < iin.getSentenceInfoSize();j++){
					tmp.add(iin.getSentenceInfoAt(j).getSentenceNr());
					//System.out.println("snr " + iin.getSentenceInfoAt(j).getSentenceNr());
				}
			}
		}
		//System.out.println("SentencesSize " + tmp.size());
		return tmp;
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
		return (nGramLimit > nGramCount+1);
	}


	protected boolean isNucleiList(String key, ArrayList<Integer> arrayList) {

		if (nGramCache.containsKey(key)){
			ArrayList<ItemInNuclei> arrL = nGramCache.get(key);
			ArrayList<String> tempTag = new ArrayList<String>();

			if (arrL != null){
				for(int i = 0; i < arrL.size(); i++){
					ItemInNuclei iin = arrL.get(i);
					//System.out.println(iin.getPosTag());

					for(int j = 0; j < iin.getSentenceInfoSize();j++){
						if(arrayList.contains(iin.getSentenceInfoAt(j).getSentenceNr())){
							if(!tempTag.contains(iin.getPosTag())){
							tempTag.add(iin.getPosTag());
							}
						}
					}
				}
			}

			//not found = color orange
			if(tempTag.size() > 1) {
				return true;
			}
		}

		//not found = color black
		return false;
	}


	/**
	 * @param item
	 * @param posTag
	 * @param si
	 * @param sitemp
	 * @param inc
	 */
	private void addNucleiRigth(ItemInNuclei item, String posTag,
			SentenceInfo si, SentenceInfo sitemp, boolean inc) {
		if (inc){
			int oldCount = item.getCount();
			item.setCount(oldCount + 1);
		}
		//System.out.println("Right: posTag " + posTag);
		//System.out.println("Right: nuclei INDEX " + si.getNucleiIndex());
		item.setPosTag(ensureValid(posTag));
		item.addNewNucleiToSentenceInfoRight(si, sitemp);
	}


	/**
	 * @param item
	 * @param posTag
	 * @param si
	 * @param sitemp
	 * @param inc
	 */
	private void addNucleiLeft(ItemInNuclei item, String posTag,
			SentenceInfo si, SentenceInfo sitemp, boolean inc) {
		if (inc){
			int oldCount = item.getCount();
			item.setCount(oldCount + 1);
		}
		//System.out.println("Left: posTag " + posTag);
		//System.out.println("Left nuclei INDEX " + si.getNucleiIndex());
		item.setPosTag(ensureValid(posTag));
		item.addNewNucleiToSentenceInfoLeft(si, sitemp);
	}



	/**
	 * @param item
	 * @param si
	 * @param posTag
	 * @param inc
	 */
	private void addSentenceInfoRigth(ItemInNuclei item, SentenceInfo si,
			String posTag, boolean inc) {
		if (inc){
			int oldCount = item.getCount();
			item.setCount(oldCount + 1);
		}
//		System.out.println("SentenceRight: posTag " + posTag);
//		System.out.println("SentenceRight INDEX " + si.getNucleiIndex());
		item.setPosTag(ensureValid(posTag));
		item.addNewSentenceInfoRigth(si);
	}


	/**
	 * @param item
	 * @param posTag
	 * @param si
	 */
	private void addSentenceInfoLeft(ItemInNuclei item, String posTag, SentenceInfo si, boolean inc) {
		if (inc){
			int oldCount = item.getCount();
			item.setCount(oldCount + 1);
		}
//		System.out.println("SentenceLeft: posTag " + posTag);
//		System.out.println("SentenceLeft INDEX " + si.getNucleiIndex());
		item.setPosTag(ensureValid(posTag));
		item.addNewSentenceInfoLeft(si);

	}

	protected String getTagQuery(String qtag){
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
					return "[TagIgnored]"; //$NON-NLS-1$
				}
			}
		}

		return tag;

	}


	public Map<String, ArrayList<ItemInNuclei>> getResult(){
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
			io.nGramsToXML(nGramCache);
		} catch (TransformerException e) {
			LoggerFactory.log(this,Level.SEVERE, "XML Transform Exeption", e); //$NON-NLS-1$
		} catch (Exception e) {
			LoggerFactory.log(this,Level.SEVERE, "XML Output Exeption", e); //$NON-NLS-1$
		}
	}


	public void cleanUpNucleus(){
		for(Iterator<String> it = getResult().keySet().iterator(); it.hasNext();){
			String wordform = it.next();
			ArrayList<ItemInNuclei> arrItem = getResult().get(wordform);
			//cleanNuclei(arrItem, wordform);

			for(int i = 0; i < arrItem.size(); i++){
				ItemInNuclei item = arrItem.get(i);

				for(int j = 0; j <item.getSentenceInfoSize(); j++) {
					SentenceInfo si = item.getSentenceInfoAt(j);

					//System.out.println("CACHE:"+si.getNucleiIndexListSize());
					for(int n = 0; n < si.getNucleiIndexListSize(); n++){
						cleanNuclei(si, wordform);
						System.out.println("Nucleus " + si.getNucleiIndexListAt(n)); //$NON-NLS-1$
					}

				}
			}
		}
	}




	// Print out Resulting nGrams:
	protected void nGramResults(Map<String, ArrayList<ItemInNuclei>> inputNGram){


//		System.out.print("\n###################################\n"+ nGramCount + "-Gram: ");
//		System.out.println("Found " + inputNGram.size() + " different nGrams");

//		for(Iterator<String> i = inputNGram.keySet().iterator(); i.hasNext();){
//			String key = i.next();
//			ArrayList<ItemInNuclei> arrItem = inputNGram.get(key);
//
//			System.out.println("\n### Wordform: " + key + " ###");
//			for (int j = 0; j < arrItem.size();j++){
//				ItemInNuclei iin = arrItem.get(j);
//				System.out.println("PoSTag: "+ iin.getPosTag() +
//								  "  PoSCount: " + iin.getCount());
//
//				for (int k = 0; k < iin.getSentenceInfoSize(); k++){
//					System.out.print("SentenceNr: " + iin.getSentenceInfoAt(k).getSentenceNr());
//					System.out.print(" | NucleiCount: " + iin.getSentenceInfoAt(k).getNucleiIndexListSize());
//					System.out.print(" NucleiIndex: " + iin.getSentenceInfoAt(k).getNucleiIndex());
//					System.out.print(" NucleiPos: "); printNuclei(iin.getSentenceInfoAt(k));
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

	public void nGramResults(){
		//System.out.println("Corpussize: " + corpus.size());

		//System.out.print("NGramsize " + nGramCount + " ");
		//System.out.println("Found " + nGramCache.size() + " uniGrams");
		removeItemsLengthOne(nGramCache);
		//System.out.println("Remaining " + nGramCache.size() + " filtered uniGrams");

//		for(Iterator<String> i = nGramCache.keySet().iterator(); i.hasNext();){
//			String key = i.next();
//			ArrayList<ItemInNuclei> arrItem = nGramCache.get(key);
//
//			System.out.println("\n### Wordform: " + key + " ###");
//			for (int j = 0; j < arrItem.size();j++){
//				ItemInNuclei iin = arrItem.get(j);
//				System.out.println("PoSTag: "+ iin.getPosTag() +
//								  " PoSCount: " + iin.getCount());
//
//				for (int k = 0; k < iin.getSentenceInfoSize(); k++){
//					System.out.print("SentenceNr: " + iin.getSentenceInfoAt(k).getSentenceNr());
//					System.out.print(" NucleiCount: " + iin.getSentenceInfoAt(k).getNucleiIndexListSize());
//					System.out.print(" NucleiIndex: " + iin.getSentenceInfoAt(k).getNucleiIndex());
//					System.out.print(" NucleiPos: "); printNuclei(iin.getSentenceInfoAt(k));
//					System.out.print(" Begin: " + iin.getSentenceInfoAt(k).getSentenceBegin());
//					System.out.println(" End: " + iin.getSentenceInfoAt(k).getSentenceEnd());
//				}
//			}
//		}


		if(nGramLimit != 1) {
			createNGrams(nGramCache, false, false);
		}

		//show fringe info dialog the following  must apply:
		// 1) fringe must be used but
		// 2) fringe was never triggered
		// 3) only show when n-gram found > 3 (fringe only triggered for n-grams | n > 3)
		if (useFringe && !usedFringe && nGramCount > 2){
				DialogFactory.getGlobalFactory().showInfo(null,
						"plugins.errormining.dialogs.fringeSizeWarning.title", //$NON-NLS-1$
						"plugins.errormining.dialogs.fringeSizeWarning.message", //$NON-NLS-1$
						nGramCount+1, fringeSize, fringeSize*2 + 1);
		}



		System.out.println("Before " + nGramCache.size());
		clearResults();
		System.out.println("After " + nGramCache.size());
	}



	/**
	 *
	 */
	private void clearResults() {

		List<String> removeFromNGrams = new ArrayList<String>();

//		List<Integer> senteceNumberList = new ArrayList<Integer>();
//		List<Integer> tempSentenceNumber = new ArrayList<Integer>();

		List<String> invertedKeyList = new ArrayList<String>(nGramCache.keySet());

		//Collections.reverse(invertedKeyList);

		//for (String key : nGramCache.keySet()) {

		for(int k = invertedKeyList.size()-1; k > 0; k--){
			String key = invertedKeyList.get(k);
			nGramCache.get(key);

			if (key.split(" ").length > 1) { //$NON-NLS-1$

				ArrayList<ItemInNuclei> arrL = nGramCache.get(key);
				ArrayList<Integer> removeList = variationIndex(arrL);

				int entry = 0;

				//tempSentenceNumber.clear();

				for (int i = 0; i < arrL.size(); i++) {


					ItemInNuclei iin = arrL.get(i);
					for (int s = 0; s < iin.getSentenceInfoSize(); s++) {
						SentenceInfo si = iin.getSentenceInfoAt(s);
						int start = si.getSentenceBegin() - 1;
						int end = si.getSentenceEnd() - 1;

//						if(!tempSentenceNumber.contains(si.getSentenceNr())){
//							tempSentenceNumber.add(si.getSentenceNr());
//						}

						DependencyData dd = corpus.get(si.getSentenceNr());

//						System.out.println(key);
//						System.out.println("Start " + start + " Ende " + end);
						int tmp = removeList.size();

						for (Integer index : removeList) {
							int head = dd.getHead(start + index);

							if (head < start || head > end) {
								tmp--;
							}
						}


//						System.out.println(removeList);
//						System.out.println(tmp);

						// for(int t = start; t < end; t++){
						// int head = dd.getHead(t);
						// System.out.print(dd.getForm(t));
						// if(head != -1){
						// if (head < start || head > end) {
						// System.out.println("  ---> REMOVE " + head +" ");
						// // System.out.println(" " +dd.getForm(t) + " "
						// // + dd.getForm(head));
						// // remove = true;
						// // System.out.println("NL " + si.getNucleusList());
						// // System.out.println("RL " + removeList);
						// // System.out.println(t-start);
						// if(removeList.contains(t-start)){
						// removeList.remove(removeList.indexOf(t-start));
						// }
						// } else {
						// System.out.println(" ---> KEEP");
						// // System.out.println(dd.getForm(t) + " "
						// // + dd.getForm(head));
						// }
						// }
						// }

						//System.out.println("RL-2 " + removeList);

						if (tmp == 0) {
						//if (tmp == 0 && !senteceNumberList.contains(si.getSentenceNr())) {
							//System.out.println("REMOVE");
							entry++;
						}
					}

					if (entry == arrL.size()) {
						removeFromNGrams.add(key);
						//tempSentenceNumber.clear();
					}
//						else {
//						senteceNumberList.addAll(tempSentenceNumber);
//						tempSentenceNumber.clear();
//					}
				}


			}

		}

		System.out.println("-----------------------------");
		for(int i = 0; i < removeFromNGrams.size(); i++){
			//System.out.println(">>>"+removeFromNGrams.get(i));
			nGramCache.remove(removeFromNGrams.get(i));
		}

		//removeItemsLengthOne(nGramCache);

	}


	/**
	 * @param arrL
	 * @return
	 */
	private ArrayList<Integer> variationIndex(ArrayList<ItemInNuclei> arrL) {

		ArrayList<Integer> variIndex = new ArrayList<Integer>();

		for(int i = 0 ; i < arrL.get(0).getPosTag().split(" ").length; i++){ //$NON-NLS-1$
			if(containsVariation(i, arrL)){
				//System.out.println("VARIATION " + i);
				variIndex.add(i);
			}
		}
		return variIndex;
	}


	/**
	 * maybe extension filter for dependency structure
	 * (show error pos + dependency)
	 * @param outputNGram
	 * @param filter
	 * @return
	 */
	public List<String> nGramPoSFilter(Map<String, ArrayList<ItemInNuclei>> outputNGram, int filter){
		List<Integer> indexList = new ArrayList<Integer>();
		List<String> filterList = new ArrayList<String>();
		for(String key : nGramCache.keySet()){
			String[] tmp = key.split(" "); //$NON-NLS-1$
			if(tmp.length == filter){
				//System.out.println(key);
				if(!filterList.contains(key)){
					filterList.add(key);
				}

				ArrayList<ItemInNuclei> iinL = nGramCache.get(key);
				for(int i = 0; i < iinL.size(); i++){
					for(int s = 0; s < iinL.get(i).getSentenceInfoSize(); s++){
						if(!indexList.contains(iinL.get(i).getSentenceInfoAt(s).getSentenceNr())){
							indexList.add(iinL.get(i).getSentenceInfoAt(s).getSentenceNr());
						}
					}
				}
			}
		}
		System.out.println("Filter " + indexList);
		System.out.println("Filter Keys " + filterList);
		return filterList;
	}

	@SuppressWarnings("unused")
	private void printNuclei(SentenceInfo sentenceInfo){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < sentenceInfo.getNucleiIndexListSize(); i++){
			sb.append(sentenceInfo.getNucleiIndexListAt(i)).append(" ");			 //$NON-NLS-1$
		}
		sb.append(" | "); //$NON-NLS-1$
		System.out.print(sb.toString());
	}



	public static void main(String[] args) throws UnsupportedFormatException {

		//18 Sentences
		//String  inputFileName = "E:\\test_small_modded.txt"; //$NON-NLS-1$
		//String  inputFileName = "E:\\test_small_modded_v2.txt"; //$NON-NLS-1$
		//String  inputFileName = "E:\\tiger_release_aug07_short"; //$NON-NLS-1$
		//String  inputFileName = "E:\\double_nucleus.txt"; //$NON-NLS-1$

		//CONLL Training English (1334 Sentences)
		String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\Icarus\\data\\treebanks\\CoNLL2009-ST-English-development.txt"; //$NON-NLS-1$

		//CONLL Training English (39279 Sentences)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-train.txt";

		//CONLL Training German 50472 Sentences (Aug)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\tiger_release_aug07.corrected.conll09.txt";

		//CONLL Training German 50472 Sentences (Aug)
		//String  inputFileName = "E:\\tiger_release_aug07.corrected.16012013.conll09";


		int sentencesToRead = 500;

		Path file = Paths.get(inputFileName);

		CONLL09SentenceDataGoldReader conellReader = new CONLL09SentenceDataGoldReader();
		DefaultFileLocation dloc = new DefaultFileLocation(file);
		Options o = null;


		Options on = new Options();
		on.put("FringeSTART", 3); //$NON-NLS-1$
		on.put("FringeEND", 5); //$NON-NLS-1$ // 0 = infinity , number = limit
		on.put("NGramLIMIT", 0); //$NON-NLS-1$
		on.put("UseFringe", true); //$NON-NLS-1$
		on.put("UseNumberWildcard", false); //$NON-NLS-1$


		NGrams ngrams = new NGrams(1, on);
		try {
			conellReader.init(dloc, o);
			int sentenceNr = 0;

			for(int i = 0; i < sentencesToRead; i++){
				DependencyData dd = (DependencyData) conellReader.next();

				ngrams.initializeUniGrams(dd, sentenceNr);
				sentenceNr++;
			}
			ngrams.nGramResults();

			//Filter for Dependency structures?
			ngrams.nGramPoSFilter(ngrams.getResult(), 4);

			//ngrams.outputToFile();

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
