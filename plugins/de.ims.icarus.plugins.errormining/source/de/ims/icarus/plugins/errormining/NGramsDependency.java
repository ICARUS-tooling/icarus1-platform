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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import de.ims.icarus.language.dependency.DependencyData;
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
	
	protected int nGramCount;
	protected int nGramLimit;
	protected int fringeStart;
	protected int fringeEnd;
	
	//protected List<DependencyItemInNuclei> items;
	protected Map<String,ArrayList<DependencyItemInNuclei>> nGramCache;	
	
	protected List<DependencyData> corpus;
	
	
	private static NGramsDependency instance;
	
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
		Options options = Options.emptyOptions;

		this.nGramCount = 1; //normally we start with unigrams so n will be 1
		
		
		//0 collect ngrams until no new ngrams are found
		this.nGramLimit = (int) options.get("NGramLIMIT");  //$NON-NLS-1$
		this.fringeStart = (int) options.get("FringeSTART", 3);  //$NON-NLS-1$
		this.fringeEnd = (int) options.get("FringeEND", 0);  //$NON-NLS-1$
		
		nGramCache = new LinkedHashMap<String,ArrayList<DependencyItemInNuclei>>();
		corpus = new ArrayList<DependencyData>();
	}
	
	
	
	public NGramsDependency(int nGramCount, Options options){
		
		if (options == null) {
			options = Options.emptyOptions;
		}
		
		this.nGramCount = nGramCount; //normally we start with unigrams so n will be 1
		
		
		//0 collect ngrams until no new ngrams are found
		this.nGramLimit = (int) options.get("NGramLIMIT");  //$NON-NLS-1$
		this.fringeStart = (int) options.get("FringeSTART", 3);  //$NON-NLS-1$
		this.fringeEnd = (int) options.get("FringeEND", 0);  //$NON-NLS-1$
		
		nGramCache = new LinkedHashMap<String,ArrayList<DependencyItemInNuclei>>();
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
	protected DependencySentenceInfo returnDependencySentenceInfoNREqual(ArrayList<DependencyItemInNuclei> l1,
										int sentenceNR){

		for (int i = 0; i < l1.size(); i++){
			DependencyItemInNuclei item = l1.get(i);
			
			for (int j = 0 ; j < item.getSentenceInfoSize(); j++){
				DependencySentenceInfo si = (DependencySentenceInfo) item.getSentenceInfoAt(j);
				
				if(si.getSentenceNr() == sentenceNR){
					//TODO maybe list?!
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
			ArrayList<DependencyItemInNuclei> arri = nGramCache.get(tag);
			
			
			for(int i = 0; i < arri.size(); i++){				
				DependencyItemInNuclei iin = arri.get(i);
				resultVari.add(iin.getPosTag());
			}

		}
		
		System.out.println(tag + " : " + resultVari); //$NON-NLS-1$
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
	 * Loop trough the Corpus and add all occuring Words with their specific PoSTags
	 * to the nGramCache. Also Store CorpusPosition (SentenceNr/Wordpositon-in-Sentence)
	 * If one Word has more than one Tag assigned add the new Tag aswell to the Wordform.
	 * Step 2) we will only use that unigrams whicht contains at least 2 different PoS Tags.
	 * 
	 * @param dd
	 * @param sentenceNr
	 */
	public void initializeUniGrams(DependencyData dd, int sentenceNr) {
		
		corpus.add(dd);
		
		
		
		for (int wordIndex = 0; wordIndex < dd.length(); wordIndex++) {
			String currentWord = dd.getForm(wordIndex);
			//System.out.print(currentWord + " "); //$NON-NLS-1$
			
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
//						+ " Head " + head + " " + dd.getForm(head)
//						+ " WIndex " + wordIndex);
				addedword = currentWord + " " + dd.getForm(headIndex); //$NON-NLS-1$
				sb.append("<").append(addedword).append(" {")
				.append(tag)
				.append("}>");
				
			} else {
				sb.append("<").append(addedword).append(" {")
				.append(dd.getRelation(wordIndex))
				.append("}>");				
			}
			//System.out.println(sb.toString());

			// item already in list? only add new tags
			if (nGramCache.containsKey(addedword)) { //$NON-NLS-1$

				ArrayList<DependencyItemInNuclei> items = nGramCache.get(addedword);
				//System.out.println("#Tag: " + tag);
				boolean knownTag = false;
				
				for (int i = 0; i < items.size(); i++) {
					// increment when tag found again
					DependencyItemInNuclei item = items.get(i);
					//System.out.println(item.getPosTag() +" vs "+ tag);
					if (item.getPosTag().equals(tag)) {
						int oldCount = item.getCount();
						item.setCount(oldCount + 1);
						item.addNewDependencySentenceInfoUniGrams(sentenceNr, wordIndex+1, headIndex+1);
						knownTag = true;
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
	 * PoS Tag is assigned to a Word this won't be an error at all becouse
	 * only one PoS Tag is assigned at all. Calling this Method removes all
	 * Wordtokens with only one PoS Tag. (equals Step 2 Dickinson)
	 */

	private Map<String, ArrayList<DependencyItemInNuclei>> removeItemsLengthOne(Map<String,ArrayList<DependencyItemInNuclei>> input){
		
		ArrayList<String> removeFromNGrams = new ArrayList<String>();
		for(Iterator<String> i = input.keySet().iterator(); i.hasNext();){
			String key = i.next();
			ArrayList<DependencyItemInNuclei> arrItem = input.get(key);
			//oonly one PoS Tag found -> add to delet list;
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
			sb.append(dd.getForm(i)).append(" "); //$NON-NLS-1$
		}
		
		return sb;
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
					DependencyData dd = corpus.get(si.getSentenceNr()-1);
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
//							sb.append(dd.getForm(i)).append(" "); //$NON-NLS-1$
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
						//System.out.println("left<<<<<<<<<<<<<<" + key);

						StringBuilder leftKey = new StringBuilder();
						
						// new key
						if (startIndex > 0) {

							String leftForm = dd.getForm(startIndex -1);
							//String leftPOS = dd.getPos(startIndex - 1);
							
							//check if leftword is found in grams -> add new nucleipos to sentence
							//boolean addNewNuclei = nGramCache.containsKey(leftForm);
							boolean addNewNuclei = newNucleiCheck(startIndex-1, dd);

							//leftKey.append(leftForm).append(" ").append(key); //$NON-NLS-1$
						
							if (hasGap){
								leftKey = rebuildGapTag(startIndex, si, dd);
							} else {
								leftKey.append(leftForm).append(" ").append(key); //$NON-NLS-1$
							}
							//System.out.println("GRAM " + nGramCount + " " +leftKey.toString());
//							System.out.println(leftKey
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
									//System.out.println(item.getPosTag() + " vs "+ leftPOS);
									
									if (item.getPosTag().equals(iin.getPosTag())) {
										if (addNewNuclei){											
											String nucleiKey = getNucleiForm(startIndex-1, dd);
											DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(nucleiKey), si.getSentenceNr());
											//DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(leftForm), si.getSentenceNr());
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
									DependencyItemInNuclei item = new DependencyItemInNuclei();
									//already an unigram
									if (addNewNuclei){	
										String nucleiKey = getNucleiForm(startIndex-1, dd);
										DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(nucleiKey), si.getSentenceNr());
										//DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(leftForm), si.getSentenceNr());
										addNucleiLeft(item, iin.getPosTag(), si, sitemp, false);
										itemsTemp.add(item);
									} else {
										addSentenceInfoLeft(item, iin.getPosTag(), si, false);
										itemsTemp.add(item);
									}
								}

							} else {
								ArrayList<DependencyItemInNuclei> items = new ArrayList<DependencyItemInNuclei>();
								DependencyItemInNuclei item = new DependencyItemInNuclei();
								
								//already an unigram
								if (addNewNuclei){
									String nucleiKey = getNucleiForm(startIndex-1, dd);
									System.out.println(nucleiKey);
									System.out.println(leftKey.toString());
									DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(nucleiKey), si.getSentenceNr());
									//DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(leftForm), si.getSentenceNr());
									addNucleiLeft(item, iin.getPosTag(), si, sitemp, false);
									items.add(item);
								} else {
									addSentenceInfoLeft(item, iin.getPosTag(), si, false);
									items.add(item);
								}
								System.out.println("NEWITEM ~~~~~~~ "+leftKey
										+ " " + item.getSentenceInfoAt(0).sentenceBegin
										+ " " + item.getSentenceInfoAt(0).sentenceEnd);
								
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

							String rightForm = dd.getForm(endIndex + 2);
							//String rightPOS = dd.getPos(endIndex + 2);
							
							//check if leftword is found in grams -> add new nucleipos to sentence
							//FIXME
							//boolean addNewNuclei = nGramCache.containsKey(rightForm);
							
							boolean addNewNuclei = newNucleiCheck(endIndex+2, dd);

							if (hasGap){
								rightKey = rebuildGapTag(startIndex, si, dd);
							} else {
								rightKey.append(key).append(" ").append(rightForm); //$NON-NLS-1$
							}
							
							// extend existing gram with values
							if (outputNGramR.containsKey(rightKey.toString())) {	

								// leftSide(outputNGram.get(leftKey.toString()), si, leftPOS);
								ArrayList<DependencyItemInNuclei> itemsTemp = outputNGramR.get(rightKey.toString());

								boolean knownTag = false;

								for (int i = 0; i < itemsTemp.size(); i++) {
									// increment when tag found again
									DependencyItemInNuclei item = itemsTemp.get(i);									
					
									
									// System.out.println(item.getPosTag()
									// +" vs "+ tag);
									

									if (item.getPosTag().equals(iin.getPosTag())) {
										//int oldCount = item.getCount();
										if (addNewNuclei){
											String nucleiKey = getNucleiForm(endIndex+2, dd);
											DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(nucleiKey), si.getSentenceNr());
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
									DependencyItemInNuclei item = new DependencyItemInNuclei();
									//already an unigram
									if (addNewNuclei){										
										String nucleiKey = getNucleiForm(endIndex+2, dd);
										DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(nucleiKey), si.getSentenceNr());
										addNucleiRigth(item, iin.getPosTag(), si, sitemp, false);
										itemsTemp.add(item);
									} else {		
										addSentenceInfoRigth(item, si, iin.getPosTag(), false);
										itemsTemp.add(item);
									}
								}

							} else {
								ArrayList<DependencyItemInNuclei> items = new ArrayList<DependencyItemInNuclei>();
								DependencyItemInNuclei item = new DependencyItemInNuclei();
								
								//already an unigram
								if (addNewNuclei){									
									String nucleiKey = getNucleiForm(endIndex+2, dd);
									
									System.out.println(nucleiKey + " >>>" + rightForm);
									DependencySentenceInfo sitemp = returnDependencySentenceInfoNREqual(nGramCache.get(nucleiKey), si.getSentenceNr());
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
							//reachedRightBoarder = true;
						}
					}
					
				}	
			}
		}
		
		
	
		//merge leftSide (outputNGram) with rightSide (outputNGramR)
		outputNGram.putAll(outputNGramR);

		
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
			// TODO remove and endable recursive
			
			//items with length one -> no longer variation --> remove
			outputNGram = removeItemsLengthOne(outputNGram);
			
			// remove items at the fringe
			

			//TODO fixfringe
			if(nGramCount <= fringeEnd && nGramCount >= fringeStart){
//				System.out.println(nGramCount + " | " 
//								+ fringeStart + " | " 
//								+ fringeEnd);
				//outputNGram = distrustFringeHeuristic(outputNGram);
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
	 * @param i
	 * @param dd
	 * @return
	 */
	private String getNucleiForm(int i, DependencyData dd) {
		int headIndex = dd.getHead(i);
		String nucleiTag = null;
		
		if (headIndex > 0 ){
			nucleiTag = dd.getForm(i) + " " + dd.getForm(headIndex); //$NON-NLS-1$
		
		
//		System.out.println("NUCLEI: " + dd.getForm(i)
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
			nucleiTag = dd.getForm(i) + " " + dd.getForm(headIndex); //$NON-NLS-1$
		
		//TODO
//		System.out.println("NUCLEI: " + dd.getForm(i)
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
		System.out.println("NUCLEI TAG>>>>>>>>> "+posTag);
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
	private void addNucleiLeft(DependencyItemInNuclei item, String posTag,
			DependencySentenceInfo si, DependencySentenceInfo sitemp, boolean inc) {
		if (inc){
			int oldCount = item.getCount();
			item.setCount(oldCount + 1);	
		}
		//item.setPosTag(ensureValid(leftPOS + " "+ iin.getPosTag()));
		item.setPosTag(ensureValid(posTag));
		item.addNewNucleiToSentenceInfoLeft(si, sitemp);
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<nucleiLEFT");
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
	
	
	public Map<String, ArrayList<DependencyItemInNuclei>> getResult(){
		return nGramCache;
	}
	


	/**
	 * @throws ParserConfigurationException 
	 * 
	 */
	private void outputToFile() {
		nGramIO io = new nGramIO();
		try {
			io.nGramsToXMLDependency(nGramCache);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}



	/**
	 * Print out Resulting nGrams:
	 *  
	 */
	
	@SuppressWarnings("nls")
	protected void nGramResults(Map<String, ArrayList<DependencyItemInNuclei>> inputNGram){
	
		
		System.out.print("\n###################################\n"+ nGramCount + "-Gram: ");
		System.out.println("Found " + inputNGram.size() + " different nGrams");
		
		for(Iterator<String> i = inputNGram.keySet().iterator(); i.hasNext();){
			String key = i.next();
			ArrayList<DependencyItemInNuclei> arrItem = inputNGram.get(key);
			
			System.out.println("\n### Wordform: " + key + " ###");
			for (int j = 0; j < arrItem.size();j++){
				DependencyItemInNuclei iin = arrItem.get(j);
				System.out.println("PoSTag: "+ iin.getPosTag() +
								  "  PoSCount: " + iin.getCount());
				
				for (int k = 0; k < iin.getSentenceInfoSize(); k++){
					System.out.print("SentenceNr: " + iin.getSentenceInfoAt(k).getSentenceNr());
					System.out.print(" | NucleiCount: " + iin.getSentenceInfoAt(k).getNucleiIndexListSize());
					System.out.print(" NucleiIndex: " + iin.getSentenceInfoAt(k).getNucleiIndex());
					System.out.print(" NucleiPos: "); printNuclei((DependencySentenceInfo) iin.getSentenceInfoAt(k));
					System.out.print(" HeadIndex: " + iin.getSentenceInfoAt(k).getSentenceHeadIndex());
					System.out.print(" Begin: " + iin.getSentenceInfoAt(k).getSentenceBegin());
					System.out.println(" End: " + iin.getSentenceInfoAt(k).getSentenceEnd());
				}
			}
		}
	}
	
	
	
	/**
	 * Print out Resulting nGrams:
	 *  
	 */
	
	@SuppressWarnings("nls")
	public void nGramResults(){
		System.out.println("Corpussize: " + corpus.size());
		
//		for (Iterator<Entry<String, ArrayList<DependencyItemInNuclei>>> it = nGramCache.entrySet().iterator();
//				it.hasNext(); ){
//			System.out.println(it.next().getKey());
//		}
		
		System.out.print("NGramsize " + nGramCount + " ");
		System.out.println("Found " + nGramCache.size() + " uniGrams");
		//TODO
		removeItemsLengthOne(nGramCache);
		System.out.println("Remaining " + nGramCache.size() + " filtered uniGrams");
		
		for(Iterator<String> i = nGramCache.keySet().iterator(); i.hasNext();){
			String key = i.next();
			ArrayList<DependencyItemInNuclei> arrItem = nGramCache.get(key);
			
			System.out.println("\n### Wordform: " + key + " ###");
			for (int j = 0; j < arrItem.size();j++){
				DependencyItemInNuclei iin = arrItem.get(j);
				System.out.println("PoSTag: "+ iin.getPosTag() +
								  " PoSCount: " + iin.getCount());
				
				for (int k = 0; k < iin.getSentenceInfoSize(); k++){
					System.out.print("SentenceNr: " + iin.getSentenceInfoAt(k).getSentenceNr());
					System.out.print(" NucleiCount: " + iin.getSentenceInfoAt(k).getNucleiIndexListSize());
					System.out.print(" NucleiIndex: " + iin.getSentenceInfoAt(k).getNucleiIndex());
					System.out.print(" NucleiPos: "); printNuclei((DependencySentenceInfo) iin.getSentenceInfoAt(k));
					System.out.print(" HeadIndex: " + iin.getSentenceInfoAt(k).getSentenceHeadIndex());
					System.out.print(" Begin: " + iin.getSentenceInfoAt(k).getSentenceBegin());
					System.out.println(" End: " + iin.getSentenceInfoAt(k).getSentenceEnd());
				}
			}
		}
		
		//TODO change false false (running both sides)
		createNGrams(nGramCache, false, false);
	}
	
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
		String  inputFileName = "E:\\test_small_modded.txt"; //$NON-NLS-1$
		
		//CONLL Training English (1334 Sentences)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-development.txt";
		
		//CONLL Training English (39279 Sentences)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-train.txt";
		
		//CONLL Training German 50472 Sentences (Aug)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\tiger_release_aug07.corrected.conll09.txt";
		
		//CONLL Training German 50472 Sentences (Aug)
		//String  inputFileName = "E:\\tiger_release_aug07.corrected.16012013.conll09";

		
		int sentencesToRead = 2;
		
		File file = new File(inputFileName);		
		
		CONLL09SentenceDataGoldReader conellReader = new CONLL09SentenceDataGoldReader();	
		DefaultFileLocation dloc = new DefaultFileLocation(file);
		Options o = null;
		
		
		Options on = new Options();
		on.put("FringeSTART", 3); //$NON-NLS-1$
		on.put("FringeEND", 5); //$NON-NLS-1$ // 0 = infinity , number = limit
		on.put("NGramLIMIT", 0); //$NON-NLS-1$

	
		NGramsDependency ngrams = new NGramsDependency(1, on);
		try {
			
//			Treebank treebank = (Treebank) new TreebankDescriptor();
//			treebank.set(dd, i, DataType.SYSTEM);
//			TreebankRegistry.getInstance().newTreebank("", "Test");
			
			conellReader.init(dloc, o);

			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

}
