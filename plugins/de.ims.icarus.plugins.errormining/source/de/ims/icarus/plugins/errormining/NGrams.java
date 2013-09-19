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

import java.io.File;
import java.io.IOException;
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
public class NGrams {
	
	protected int nGramCount;
	protected int fringeStart;
	protected int fringeEnd;
	protected int nGramLimit;
	protected boolean useFringe;
	protected boolean useNumberWildcard;
	
	//protected List<ItemInNuclei> items;
	protected Map<String, ArrayList<ItemInNuclei>> nGramCache;	
	protected List<NGramQAttributes> queryList;
	protected Options options;
	
	protected List<DependencyData> corpus;
	
	private static Pattern numberPattern = Pattern.compile("^[0-9]"); //$NON-NLS-1$
	private static String numberString = "[number-wildcard]"; //$NON-NLS-1$

	
	
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
	
	
	//Debug Konstructor
	public NGrams(){
		Options options = new Options();
		options = Options.emptyOptions;
		
		this.nGramCount = 1; //normally we start with unigrams so n will be 1		
		
		//0 collect ngrams until no new ngrams are found
		this.nGramLimit = (int) options.get("NGramLIMIT", 4);  //$NON-NLS-1$
		this.fringeStart = (int) options.get("FringeSTART", 0);  //$NON-NLS-1$
		this.fringeEnd = (int) options.get("FringeEND", 0);  //$NON-NLS-1$
		this.useFringe = false;
		
		nGramCache = new LinkedHashMap<String,ArrayList<ItemInNuclei>>();
		corpus = new ArrayList<DependencyData>();
	}
	
	
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
		this.fringeStart = options.getInteger("FringeSTART"); //$NON-NLS-1$
		this.fringeEnd = options.getInteger("FringeEND"); //$NON-NLS-1$
		this.useFringe = options.getBoolean("UseFringe"); //$NON-NLS-1$
		this.nGramLimit = options.getInteger("NGramLIMIT"); //$NON-NLS-1$
		this.useNumberWildcard = options.getBoolean("UseNumberWildcard"); //$NON-NLS-1$
				
		nGramCache = new LinkedHashMap<String,ArrayList<ItemInNuclei>>();
		corpus = new ArrayList<DependencyData>();
	}
	
	
	public NGrams(int nGramCount, Options options){
		
		if (options == null) {
			options = Options.emptyOptions;
		}
		
		this.nGramCount = nGramCount; //normally we start with unigrams so n will be 1
		
		this.queryList = new ArrayList<NGramQAttributes>();
		this.useFringe = true;
		
//		//0 collect ngrams until no new ngrams are found
		this.nGramLimit = (int) options.get("NGramLIMIT");  //$NON-NLS-1$
		this.fringeStart = (int) options.get("FringeSTART", 3);  //$NON-NLS-1$
		this.fringeEnd = (int) options.get("FringeEND", 5);  //$NON-NLS-1$
		
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
//			//item.setPosTag(ensureValid(getTagQuery(dd.getPos(wordIndex))));
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

			// item already in list? only add new tags
			if (nGramCache.containsKey(currentWord)) {
				if (getTagQuery(dd.getPos(wordIndex)) != null){
					ArrayList<ItemInNuclei> items = nGramCache.get(currentWord);
	
					boolean knownTag = false;
					
					for (int i = 0; i < items.size(); i++) {
						// increment when tag found again
						ItemInNuclei item = items.get(i);
						//System.out.println(item.getPosTag() +" vs "+ getTagQuery(getTagQuery(dd.getPos(wordIndex))));
						if (item.getPosTag().equals(getTagQuery(dd.getPos(wordIndex)))) {
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
						item.setPosTag(ensureValid(getTagQuery(dd.getPos(wordIndex))));
						item.addNewSentenceInfoUniGrams(sentenceNr, wordIndex+1);
						items.add(item);
					}
				}
			} else {
				if (getTagQuery(dd.getPos(wordIndex)) != null){
					ArrayList<ItemInNuclei> items = new ArrayList<ItemInNuclei>();
					ItemInNuclei item = new ItemInNuclei();
					item.setPosTag(ensureValid(getTagQuery(dd.getPos(wordIndex))));
					item.addNewSentenceInfoUniGrams(sentenceNr, wordIndex+1);
					items.add(item);
	
					nGramCache.put(currentWord, items);
				}
			}
		}
	}
	

	//method used for replace numbers by general number token
	private String checkForNumber(String currentWord) {
		if (numberPattern.matcher(currentWord).find()){
			//System.out.println("ContainsNumber " + currentWord);
			return numberString;
		}
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

			//only one PoS Tag found -> add to delet list;
			if (arrItem.size() == 1){
				
				removeFromNGrams.add(key);
				
//				//simple firsat path
//				if(nGramCount==1){
//					removeFromNGrams.add(key);
//				} else {
//					
//				//At this point we have exactly ONE sentenceinfo
//				// otherwise the ArrItemsize wont be 1.....
//				ItemInNuclei iin = arrItem.get(0);				
//				
//				
//				int nc = 0;
//				boolean remove = true;
//				String[] ks = key.split(" "); //$NON-NLS-1$
//				System.out.println(key);
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
//					System.out.println("key "+ ks[k]+" " + tmpTags.toString());
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
			
			for(int j = 0; j < arrItem.size(); j++){
				ItemInNuclei iin = arrItem.get(j);
				for (int s = 0; s < iin.getSentenceInfoSize(); s++){
					SentenceInfo si = iin.getSentenceInfoAt(s);
					
					/*
					 * we only want to remove items where we have fringe;
					 * when there is a ngram with more than one nuclei we check
					 * for every nuclei if its fringe, if there is at most
					 * one nuclei NOT fringe we will keep the ngram no matter if
					 * the others are fringe nucleis or not
					 */
					
					//new version should work now and only delete fringe
					// within an ngram wit 2+ nuclei if both are fringe
					if(!getFringeItem(si)){
						//System.out.println("FringeKey " + key);
						if(!removeFringeFromNGrams.contains(key)){
							//System.out.println("FringeKey " + key);
							removeFringeFromNGrams.add(key);
						}
					}
					

//					for(int n = 0; n < si.getNucleiIndexListSize(); n++){		
//						//is fringe?
//						if(isFringe(si.getNucleiIndexListAt(n), start, end)){
//							
//							if(!removeFringeFromNGrams.contains(key)){
//								//System.out.println("FringeKey " + key);
//								removeFringeFromNGrams.add(key);
//							}
//						}
//					}
					
				}
			}
		}
				
		//System.out.println("FringeItems to Remove: " + removeFringeFromNGrams.size());
		for(int i = 0; i < removeFringeFromNGrams.size(); i++){
			System.out.println("Remove " + removeFringeFromNGrams.get(i));
			input.remove(removeFringeFromNGrams.get(i));
		}

		removeFringeFromNGrams.clear();
		return input;
		
	}
	
	
	/**
	 * @param si
	 */
	private boolean getFringeItem(SentenceInfo si) {
		boolean fringe = true;
		int start = si.getSentenceBegin();
		int end = si.getSentenceEnd();
		for(int n = 0; n < si.getNucleiIndexListSize(); n++){			
			//is fringe?
			//TODO fringe fixen
			if(fringe){
			fringe = isFringe(si.getNucleiIndexListAt(n), start, end);
//			System.out.print(" Fringe:" + si.getNucleiIndexListAt(n)
//					+ " start:" + start + " end:" + end + " " + fringe + "\n");
			}
			
			if (!fringe) {
				//System.out.println("Do not delete " + si.getNucleiIndexListAt(n));
				return true;
			}
		}
		return fringe;
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

			
			for (int tagSize = 0 ; tagSize < iiArr.size(); tagSize++){
				
				ItemInNuclei iin = iiArr.get(tagSize);
			
				for (int s = 0 ; s < iin.getSentenceInfoSize(); s++){
					
					SentenceInfo si = iin.getSentenceInfoAt(s);
					
					//start sentencecount at 1 so decrement
					DependencyData dd = corpus.get(si.getSentenceNr()-1);
					int startIndex = si.getSentenceBegin()-1;
					int endIndex = si.getSentenceEnd()-1;
					
					//System.out.println("Startindex " + startIndex);				

					/*
					System.out.println("Sentence: " + si.getSentenceNr() +
										" NucleiPos " + si.getNucleiIndexListSize() + 
										" NucleiForm " + dd.getForm(si.getNucleiIndexListAt(0)-1));
					*/
					
					//StringBuilder posTagBuilder = new StringBuilder();
					
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
							//String leftPOS = dd.getPos(startIndex - 1);
							
							//check if leftword is found in grams -> add new nucleipos to sentence
							boolean addNewNuclei = nGramCache.containsKey(leftForm);
							
							leftKey.append(leftForm).append(" ").append(key); //$NON-NLS-1$
							//posTagBuilder.append(leftPOS).append(" ").append(iin.getPosTag()); //$NON-NLS-1$
							//System.out.println("Left: "+ leftForm + " " + leftPOS);
							
							// extend existing gram with values
							if (outputNGram.containsKey(leftKey.toString())) {	
								
								// leftSide(outputNGram.get(leftKey.toString()), si, leftPOS);
								ArrayList<ItemInNuclei> itemsTemp = outputNGram.get(leftKey.toString());

								boolean knownTag = false;
								
								for (int i = 0; i < itemsTemp.size(); i++) {
									// increment when tag found again
									ItemInNuclei item = itemsTemp.get(i);
									//System.out.println(item.getPosTag() + " vs "+ leftPOS);
									
									if (item.getPosTag().equals(iin.getPosTag())) {
										if (addNewNuclei){											
											SentenceInfo sitemp = returnSentenceInfoNREqual(nGramCache.get(leftForm), si.getSentenceNr());
											addNucleiLeft(item, iin.getPosTag(), si, sitemp, true);
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
									ItemInNuclei item = new ItemInNuclei();
									//already an unigram
									if (addNewNuclei){										
										SentenceInfo sitemp = returnSentenceInfoNREqual(nGramCache.get(leftForm), si.getSentenceNr());
										addNucleiLeft(item, iin.getPosTag(), si, sitemp, false);
										itemsTemp.add(item);
									} else {
										addSentenceInfoLeft(item, iin.getPosTag(), si, false);
										itemsTemp.add(item);
									}
								}

							} else {
								ArrayList<ItemInNuclei> items = new ArrayList<ItemInNuclei>();
								ItemInNuclei item = new ItemInNuclei();
								
								//already an unigram
								if (addNewNuclei){									
									SentenceInfo sitemp = returnSentenceInfoNREqual(nGramCache.get(leftForm), si.getSentenceNr());
									addNucleiLeft(item, iin.getPosTag(), si, sitemp, false);
									items.add(item);
								} else {
									addSentenceInfoLeft(item, iin.getPosTag(), si, false);
									items.add(item);
								}
								outputNGram.put(leftKey.toString(), items);
							}
		
							
						} else {
							reachedLeftBoarder = true;
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
							//String rightPOS = dd.getPos(endIndex + 1);
							
							//check if leftword is found in grams -> add new nucleipos to sentence
							boolean addNewNuclei = nGramCache.containsKey(rightForm);							
							
							rightKey.append(key).append(" ").append(rightForm); //$NON-NLS-1$
							//posTagBuilder.append(iin.getPosTag()).append(" ").append(rightPOS); //$NON-NLS-1$
							
							// extend existing gram with values
							if (outputNGramR.containsKey(rightKey.toString())) {	

								// leftSide(outputNGram.get(leftKey.toString()), si, leftPOS);
								ArrayList<ItemInNuclei> itemsTemp = outputNGramR.get(rightKey.toString());

								boolean knownTag = false;

								for (int i = 0; i < itemsTemp.size(); i++) {
									// increment when tag found again
									ItemInNuclei item = itemsTemp.get(i);									
					
									
									// System.out.println(item.getPosTag()
									// +" vs "+ getTagQuery(dd.getPos(wordIndex)));
									

									if (item.getPosTag().equals(iin.getPosTag())) {
										//int oldCount = item.getCount();
										if (addNewNuclei){											
											SentenceInfo sitemp = returnSentenceInfoNREqual(nGramCache.get(rightForm), si.getSentenceNr());
											addNucleiRigth(item, iin.getPosTag(), si, sitemp, true);
											knownTag = true;
										} else {											
											addSentenceInfoRigth(item, si, iin.getPosTag(), true);
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
										addNucleiRigth(item, iin.getPosTag(), si, sitemp, false);
										itemsTemp.add(item);
									} else {		
										addSentenceInfoRigth(item, si, iin.getPosTag(), false);
										itemsTemp.add(item);
									}
								}

							} else {
								ArrayList<ItemInNuclei> items = new ArrayList<ItemInNuclei>();
								ItemInNuclei item = new ItemInNuclei();
								
								//already an unigram
								if (addNewNuclei){									
									SentenceInfo sitemp = returnSentenceInfoNREqual(nGramCache.get(rightForm), si.getSentenceNr());
									addNucleiRigth(item, iin.getPosTag(), si, sitemp, false);
									items.add(item);
								} else {
									addSentenceInfoRigth(item, si, iin.getPosTag(), false);
									items.add(item);
								}

								//System.out.println("<<<<<<"+rightKey.toString());
								outputNGramR.put(rightKey.toString(), items);
							}
							
							
						} else {
							reachedRightBoarder = true;
						}
					}
					
				}	
			}
		}
		
		
	
		//merge leftSide (outputNGram) with rightSide (outputNGramR)
		outputNGram.putAll(outputNGramR);

		
//		for(Iterator<String> i = outputNGram.keySet().iterator(); i.hasNext();){
//			String key = i.next();
//			ArrayList<ItemInNuclei> arrItem = outputNGram.get(key);
//			//only one PoS Tag found -> add to delete list;
//			if (arrItem.size() > 1){
//				System.out.println("ToKeep ---------------> " + key);
//			}
//		}
		
		nGramCount++;

		if (outputNGram.size() > 0) {
			// TODO remove and endable recursive
			
			//items with length one -> no longer variation --> remove
			outputNGram = removeItemsLengthOne(outputNGram);
			
			// remove items at the fringe
			
			//FIXME fringe
			if (useFringe) {
				//System.out.println("UseFringe");
				if(nGramCount <= fringeEnd && nGramCount >= fringeStart){
//					System.out.println(nGramCount + " | " 
//									+ fringeStart + " | " 
//									+ fringeEnd);
					outputNGram = distrustFringeHeuristic(outputNGram);
				}				
			}


			
			//add results into Cache
			nGramCache.putAll(outputNGram);
			
			//print to console
			nGramResults(outputNGram);			
			
			
			//continue creating ngrams?
			if (continueNGrams()){				
				createNGrams(outputNGram, false, false);
			}
		}				
		
	
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
	private void addNucleiRigth(ItemInNuclei item, String posTag,
			SentenceInfo si, SentenceInfo sitemp, boolean inc) {
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
	private void addSentenceInfoRigth(ItemInNuclei item, SentenceInfo si,
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
	private void addSentenceInfoLeft(ItemInNuclei item, String posTag, SentenceInfo si, boolean inc) {
		if (inc){
			int oldCount = item.getCount();
			item.setCount(oldCount + 1);	
		}
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
					return null;
				}
			}
		}
		
		return tag;
		
	}
	
	
	public Map<String, ArrayList<ItemInNuclei>> getResult(){
		return nGramCache;
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



	/**
	 * Print out Resulting nGrams:
	 *  
	 */
	
	@SuppressWarnings("nls")
	protected void nGramResults(Map<String, ArrayList<ItemInNuclei>> inputNGram){
	
		
		System.out.print("\n###################################\n"+ nGramCount + "-Gram: ");
		System.out.println("Found " + inputNGram.size() + " different nGrams");
		
		for(Iterator<String> i = inputNGram.keySet().iterator(); i.hasNext();){
			String key = i.next();
			ArrayList<ItemInNuclei> arrItem = inputNGram.get(key);
			
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
		}
	}
	
	
	
	/**
	 * Print out Resulting nGrams:
	 *  
	 */
	
	@SuppressWarnings("nls")
	public void nGramResults(){
		System.out.println("Corpussize: " + corpus.size());
		
		System.out.print("NGramsize " + nGramCount + " ");
		System.out.println("Found " + nGramCache.size() + " uniGrams");
		//TODO
		removeItemsLengthOne(nGramCache);
		System.out.println("Remaining " + nGramCache.size() + " filtered uniGrams");
		
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
		
		//TODO change false false
		createNGrams(nGramCache, false, false);

	}
	
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
		
		//CONLL Training English (1334 Sentences)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-development.txt";
		
		//CONLL Training English (39279 Sentences)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-train.txt";
		
		//CONLL Training German 50472 Sentences (Aug)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\tiger_release_aug07.corrected.conll09.txt";
		
		//CONLL Training German 50472 Sentences (Aug)
		String  inputFileName = "E:\\tiger_release_aug07.corrected.16012013.conll09";

		
		int sentencesToRead = 500;
		
		File file = new File(inputFileName);		
		
		CONLL09SentenceDataGoldReader conellReader = new CONLL09SentenceDataGoldReader();	
		DefaultFileLocation dloc = new DefaultFileLocation(file);
		Options o = null;
		
		
		Options on = new Options();
		on.put("FringeSTART", 21); //$NON-NLS-1$
		on.put("FringeEND", 21); //$NON-NLS-1$ // 0 = infinity , number = limit
		on.put("NGramLIMIT", 0); //$NON-NLS-1$

	
		NGrams ngrams = new NGrams(1, on);
		try {
			
//			Treebank treebank = (Treebank) new TreebankDescriptor();
//			treebank.set(dd, i, DataType.SYSTEM);
//			TreebankRegistry.getInstance().newTreebank("", "Test");
			
			conellReader.init(dloc, o);
			
			int sentenceNr = 1;
			
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
