/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.errormining;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.plugins.matetools.conll.CONLL09SentenceDataReader;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.UnsupportedFormatException;
import net.ikarus_systems.icarus.util.location.DefaultFileLocation;
import net.ikarus_systems.icarus.util.location.UnsupportedLocationException;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGrams {
	
	protected int nGramCount;
	//protected List<ItemInNuclei> items;
	protected Map<String,ArrayList<ItemInNuclei>> nGramCache;
	
	
	protected List<DependencyData> corpus;
	
	
	
	public NGrams(int nGramCount){
		this.nGramCount = nGramCount; //normally we start with unigrams so n will be 1
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
					//TODO maybe list?!
					//System.out.println("NucleiToBeAdded " + si.getNucleiSentencePositionAt(0));
					return si;
				}				
			}
		}
		return null;
		
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
	private void initializeUniGrams(DependencyData dd, int sentenceNr) {
		
		corpus.add(dd);
		
		for (int wordIndex = 0; wordIndex < dd.length(); wordIndex++) {
			String currentWord = dd.getForm(wordIndex);
			//System.out.print(currentWord + " ");

			// item already in list? only add new tags
			if (nGramCache.containsKey(currentWord)) {

				ArrayList<ItemInNuclei> items = nGramCache.get(currentWord);

				boolean knownTag = false;
				
				for (int i = 0; i < items.size(); i++) {
					// increment when tag found again
					ItemInNuclei item = items.get(i);
					//System.out.println(item.getPosTag() +" vs "+ dd.getPos(wordIndex));
					if (item.getPosTag().equals(dd.getPos(wordIndex))) {
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
					item.setPosTag(ensureValid(dd.getPos(wordIndex)));
					item.addNewSentenceInfoUniGrams(sentenceNr, wordIndex+1);
					items.add(item);
				}

			} else {
				ArrayList<ItemInNuclei> items = new ArrayList<ItemInNuclei>();
				ItemInNuclei item = new ItemInNuclei();
				item.setPosTag(ensureValid(dd.getPos(wordIndex)));
				item.addNewSentenceInfoUniGrams(sentenceNr, wordIndex+1);
				items.add(item);

				nGramCache.put(currentWord, items);
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

	private Map<String, ArrayList<ItemInNuclei>> removeItemsLengthOne(Map<String,ArrayList<ItemInNuclei>> input){
		
		ArrayList<String> removeFromnGrams = new ArrayList<String>();
		for(Iterator<String> i = input.keySet().iterator(); i.hasNext();){
			String key = i.next();
			ArrayList<ItemInNuclei> arrItem = input.get(key);
			//oonly one PoS Tag found -> add to delet list;
			if (arrItem.size() == 1){
				removeFromnGrams.add(key);
			}
		}
				
		//System.out.println("Items to Remove: " + removeFromnGrams.size());
		for(int i = 0; i < removeFromnGrams.size(); i++){
			input.remove(removeFromnGrams.get(i));
		}

		removeFromnGrams.clear();
		return input;
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
										" NucleiPos " + si.getNucleiSentencePositionSize() + 
										" NucleiForm " + dd.getForm(si.getNucleiSentencePositionAt(0)-1));
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

							String leftForm = dd.getForm(startIndex -1);
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
//											//item.setPosTag(ensureValid(leftPOS + " "+ iin.getPosTag()));
//											item.setPosTag(ensureValid(iin.getPosTag()));
//											item.addNewNucleiToSentenceInfoLeft(si, sitemp);
//											item.setCount(oldCount + 1);
											knownTag = true;
										} else {
											addSentenceInfoLeft(item, iin.getPosTag(), si, true);
//											item.setPosTag(ensureValid(iin.getPosTag()));											
//											item.addNewSentenceInfoLeft(si);
//											item.setCount(oldCount + 1);
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
//										//item.setPosTag(ensureValid(leftPOS + " "+ iin.getPosTag()));								
//										item.setPosTag(ensureValid(iin.getPosTag()));
//										item.addNewNucleiToSentenceInfoLeft(si, sitemp);
										addNucleiLeft(item, iin.getPosTag(), si, sitemp, false);
										itemsTemp.add(item);
									} else {		
//										item.setPosTag(ensureValid(iin.getPosTag()));
//										item.addNewSentenceInfoLeft(si);
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
//									//item.setPosTag(ensureValid(leftPOS + " "+ iin.getPosTag()));									
//									item.setPosTag(ensureValid(iin.getPosTag()));
//									item.addNewNucleiToSentenceInfoLeft(si, sitemp);
									addNucleiLeft(item, iin.getPosTag(), si, sitemp, false);
									items.add(item);
								} else {
//									item.setPosTag(ensureValid(iin.getPosTag()));
//									item.addNewSentenceInfoLeft(si);
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

							String rightForm = dd.getForm(endIndex + 1);
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
									// +" vs "+ dd.getPos(wordIndex));
									

									if (item.getPosTag().equals(iin.getPosTag())) {
										//int oldCount = item.getCount();
										if (addNewNuclei){											
											SentenceInfo sitemp = returnSentenceInfoNREqual(nGramCache.get(rightForm), si.getSentenceNr());
//											//item.setPosTag(ensureValid(rightPOS + " "+ iin.getPosTag()));
//											item.setPosTag(ensureValid(iin.getPosTag()));
//											item.addNewNucleiToSentenceInfoRight(si, sitemp);
//											item.setCount(oldCount + 1);
											addNucleiRigth(item, iin.getPosTag(), si, sitemp, true);
											knownTag = true;
										} else {											
											addSentenceInfoRigth(item, si, iin.getPosTag(), true);
//											item.setPosTag(ensureValid(iin.getPosTag()));											
//											item.addNewSentenceInfoRigth(si);
//											item.setCount(oldCount + 1);
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
//										//item.setPosTag(ensureValid(leftPOS + " "+ iin.getPosTag()));								
//										item.setPosTag(ensureValid(iin.getPosTag()));
//										item.addNewNucleiToSentenceInfoRight(si, sitemp);
										addNucleiRigth(item, iin.getPosTag(), si, sitemp, false);
										itemsTemp.add(item);
									} else {		
//										item.setPosTag(ensureValid(iin.getPosTag()));
//										item.addNewSentenceInfoRigth(si);
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
//									//item.setPosTag(ensureValid(leftPOS + " "+ iin.getPosTag()));									
//									item.setPosTag(ensureValid(iin.getPosTag()));
//									item.addNewNucleiToSentenceInfoRight(si, sitemp);
									addNucleiRigth(item, iin.getPosTag(), si, sitemp, false);
									items.add(item);
								} else {
//									item.setPosTag(ensureValid(iin.getPosTag()));
//									item.addNewSentenceInfoRigth(si);									
									addSentenceInfoRigth(item, si, iin.getPosTag(), false);
//									System.out.println(key + " + " + isDuplicate(si,iin)
//												+" " + item.getCount());
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
		
		nGramCount++;
		//nGramCache.clear();
		//nGramCache.putAll(outputNGram);
		
		outputNGram.putAll(outputNGramR);
		
		//TODO Output

		
//		for(Iterator<String> i = outputNGram.keySet().iterator(); i.hasNext();){
//			String key = i.next();
//			ArrayList<ItemInNuclei> arrItem = outputNGram.get(key);
//			//only one PoS Tag found -> add to delete list;
//			if (arrItem.size() > 1){
//				System.out.println("ToKeep ---------------> " + key);
//			}
//		}
		

		if(outputNGram.size() > 0){			
			//TODO remove and endable recursive
			nGramResults(outputNGram);
			nGramCache.putAll(outputNGram);
			createNGrams(removeItemsLengthOne(outputNGram), false, false);		
		}		
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


	/**
	 * @throws ParserConfigurationException 
	 * 
	 */
	private void outputToFile() {
		nGramIO io = new nGramIO();
		try {
			io.nGramsToXML(nGramCache);
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
	protected void nGramResults(Map<String, ArrayList<ItemInNuclei>> inputNGram){
	
		
		System.out.print("\n###################################\n"+ nGramCount + "-Gram: ");
		System.out.println("Found " + inputNGram.size() + " different nGrams");
		
		for(Iterator<String> i = inputNGram.keySet().iterator(); i.hasNext();){
			String key = i.next();
			ArrayList<ItemInNuclei> arrItem = inputNGram.get(key);
			
			System.out.println("\n### Wordform: " + key + " ###");
			for (int j = 0; j < arrItem.size();j++){
				ItemInNuclei iin = arrItem.get(j);
				System.out.println("PoSTag: "+ iin.getPosTag() +
								  "  PoSCount: " + iin.getCount());
				
				for (int k = 0; k < iin.getSentenceInfoSize(); k++){
					System.out.print("SentenceNr: " + iin.getSentenceInfoAt(k).getSentenceNr());
					System.out.print(" | NucleiCount: " + iin.getSentenceInfoAt(k).getNucleiSentencePositionSize());
					System.out.print(" NucleiPos: "); printNuclei(iin.getSentenceInfoAt(k));
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
		
		System.out.print("NGramsize " + nGramCount + " ");
		System.out.println("Found " + nGramCache.size() + " uniGrams");
		//TODO
		removeItemsLengthOne(nGramCache);
		System.out.println("Remaining " + nGramCache.size() + " filtered uniGrams");
		
		for(Iterator<String> i = nGramCache.keySet().iterator(); i.hasNext();){
			String key = i.next();
			ArrayList<ItemInNuclei> arrItem = nGramCache.get(key);
			
			System.out.println("\n### Wordform: " + key + " ###");
			for (int j = 0; j < arrItem.size();j++){
				ItemInNuclei iin = arrItem.get(j);
				System.out.println("PoSTag: "+ iin.getPosTag() +
								  " PoSCount: " + iin.getCount());
				
				for (int k = 0; k < iin.getSentenceInfoSize(); k++){
					System.out.print("SentenceNr: " + iin.getSentenceInfoAt(k).getSentenceNr());
					System.out.print(" NucleiCount: " + iin.getSentenceInfoAt(k).getNucleiSentencePositionSize());
					System.out.print(" NucleiPos: "); printNuclei(iin.getSentenceInfoAt(k));
					System.out.print(" Begin: " + iin.getSentenceInfoAt(k).getSentenceBegin());
					System.out.println(" End: " + iin.getSentenceInfoAt(k).getSentenceEnd());
				}
			}
		}
		
		//TODO change false false
		createNGrams(nGramCache, false, false);
	}
	
	private void printNuclei(SentenceInfo sentenceInfo){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < sentenceInfo.getNucleiSentencePositionSize(); i++){
			sb.append(sentenceInfo.getNucleiSentencePositionAt(i)).append(" ");			 //$NON-NLS-1$
		}
		sb.append(" | "); //$NON-NLS-1$
		System.out.print(sb.toString());
	}



	public static void main(String[] args) throws UnsupportedFormatException {
		
		//18 Sentences
		//String  inputFileName = "E:\\test_small_modded.txt"; //$NON-NLS-1$
		
		//CONLL Training English (1334 Sentences)
		String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-development.txt";
		
		//CONLL Training English (39279 Sentences)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\CoNLL2009-ST-English-train.txt";
		
		//CONLL Training German 50472 Sentences (Aug)
		//String  inputFileName = "D:\\Eigene Dateien\\smashii\\workspace\\IMS Explorer\\corpora\\tiger_release_aug07.corrected.conll09.txt";
		
		//CONLL Training German 50472 Sentences (Aug)
		//String  inputFileName = "E:\\tiger_release_aug07.corrected.16012013.conll09";

		
		int sentencesToRead = 1334;
		
		File file = new File(inputFileName);		
		
		CONLL09SentenceDataReader conellReader = new CONLL09SentenceDataReader();	
		DefaultFileLocation dloc = new DefaultFileLocation(file);
		Options o = null;

	
		NGrams ngrams = new NGrams(1);
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
