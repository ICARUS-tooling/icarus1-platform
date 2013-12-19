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
package de.ims.icarus.plugins.errormining.ngram_tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ChangeListener;

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.Grammar;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.plugins.errormining.DependencyItemInNuclei;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramDataListDependency implements SentenceDataList {
	
	protected Map<String,ArrayList<DependencyItemInNuclei>> nGramMap;
	protected int index;
	protected List<Integer> sentences;
	protected List<SentenceData> corpus;
	protected Map<Integer, NewNGramSentenceData> nGramMapCache;
	//SentenceView
	protected List<CorpusType> corpusList;
	
	public NGramDataListDependency(Map<String,ArrayList<DependencyItemInNuclei>> nGramMap,
			List<SentenceData> corpus){
		if (nGramMap == null)
			throw new IllegalArgumentException("No Data"); //$NON-NLS-1$
		
//		System.out.println("nGramap " + nGramMap.size()
//							+ " CSize" + corpus.size());
		
		this.corpus = corpus;
		setNGramMap(nGramMap);
		
	}
	
	/**
	 * @return the nGramMap
	 */
	public Map<String, ArrayList<DependencyItemInNuclei>> getnGramMap() {
		return nGramMap;
	}

	/**
	 * @param nGramMap the nGramMap to set
	 */
	public void setnGramMap(Map<String, ArrayList<DependencyItemInNuclei>> nGramMap) {
		this.nGramMap = nGramMap;
	}

	/**
	 * @return the corpus
	 */
	public List<SentenceData> getCorpus() {
		return corpus;
	}

	/**
	 * @param corpus the corpus to set
	 */
	public void setCorpus(List<SentenceData> corpus) {
		this.corpus = corpus;
	}

	public List<CorpusType> getCorpusList(){
		return corpusList;
	}

	/**
	 * @param <nGramMapCache>
	 * @param nGramMap
	 */
	void setNGramMap(Map<String, ArrayList<DependencyItemInNuclei>> nGramMap) {
		if (this.nGramMap != null) {
			return;
		}
		nGramMapCache = new HashMap<Integer, NewNGramSentenceData>();

		// System.out.println(nGramMap.size());
		// TODO filtering
		this.nGramMap = nGramMap;		
		filterNGramMap(nGramMap);

	}

	private Map<String, ArrayList<DependencyItemInNuclei>> filterNGramMap(Map<String, ArrayList<DependencyItemInNuclei>> nGramMap){
		
		List<String> tmpKey = new ArrayList<String>(nGramMap.keySet());
		Collections.reverse(tmpKey);
		
		corpusList = new ArrayList<CorpusType>();
		sentences = new ArrayList<Integer>();
		
		Map<String, ArrayList<DependencyItemInNuclei>> filterMap = new LinkedHashMap<String,ArrayList<DependencyItemInNuclei>>();
		
//		
//		for(int j = 0; j < tmpKey.size(); j++){
//			System.out.println();
//			System.out.println("key" + tmpKey.get(j));
//		}
		
		
		for(int i = 0; i < tmpKey.size(); i++){
			String key = tmpKey.get(i);
			String[] keysplit = key.split(" "); //$NON-NLS-1$
			
			//System.out.println(key + keysplit.length);
			
			ArrayList<DependencyItemInNuclei> value = nGramMap.get(key);
			for (int j = 0; j < value.size();j++){
				DependencyItemInNuclei iin = value.get(j);
//				System.out.println("PoSTag: "+ iin.getPosTag() +
//								  " PoSCount: " + iin.getCount());
				
					for (int k = 0; k < iin.getSentenceInfoSize(); k++){
						int sentenceNR = iin.getSentenceInfoAt(k).getSentenceNr()-1;
//						System.out.println(key + " " + sentenceNR);
						if(sentences.contains(sentenceNR)){	
							//donothing
						} else {
							//wenn nicht nur nuclei in liste aufnehmen
							//TODO maybe workaround when only nuclei displayed
							if(keysplit.length > 1){
								filterMap.put(key, value);
								corpusList.add(new CorpusType(sentenceNR, key));
								sentences.add(sentenceNR);
							}
							
						}
					}
			}
		}
		
		
//		for (int i = 0; i < sentences.size(); i++){
//			System.out.println("Satz: " + sentences.get(i));
//		}
		
		// for(Iterator<String> i = filterMap.keySet().iterator(); i.hasNext();){
		//		String key = i.next();
		//		ArrayList<ItemInNuclei> arrItem = filterMap.get(key);
		//		
		//		System.out.println("\n### Wordform: " + key + " ###");
		//		for (int j = 0; j < arrItem.size();j++){
		//			ItemInNuclei iin = arrItem.get(j);
		//			System.out.println("PoSTag: "+ iin.getPosTag() +
		//							  "  PoSCount: " + iin.getCount());
		//			
		//			for (int k = 0; k < iin.getSentenceInfoSize(); k++){
		//				System.out.print("SentenceNr: " + iin.getSentenceInfoAt(k).getSentenceNr());
		//				System.out.print(" | NucleiCount: " + iin.getSentenceInfoAt(k).getNucleiIndexListSize());
		//				System.out.print(" NucleiIndex: " + iin.getSentenceInfoAt(k).getNucleiIndex());
		//				//System.out.print(" NucleiPos: "); printNuclei(iin.getSentenceInfoAt(k));
		//				System.out.print(" Begin: " + iin.getSentenceInfoAt(k).getSentenceBegin());
		//				System.out.println(" End: " + iin.getSentenceInfoAt(k).getSentenceEnd());
		//			}
		//		}
		//	}
		
		//remaining keys in filtered map
//		List<String> tmpK = new ArrayList<String>(filterMap.keySet());
//		Collections.reverse(tmpKey);
//		for(int k = 0; k < tmpK.size(); k++){				
//			System.out.println("key " + tmpK.get(k));
//		}

		return filterMap;
	}
	
	private SentenceData getNGramDataFromIndex(int index) {
		if (!nGramMapCache.containsKey(index)) {
			NewNGramSentenceData ngramData = new NewNGramSentenceData(index);
			nGramMapCache.put(index, ngramData);
		}
		return nGramMapCache.get(index);
	}
	

	
	/**
	 * @see de.ims.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		return sentences.size();
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
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType)
	 */
	@Override
	public SentenceData get(int index, DataType type) {
		if (type != DataType.SYSTEM) {
			return null;
		}
		return sentences == null ? null : getNGramDataFromIndex(index);
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataList#get(int, de.ims.icarus.language.DataType, de.ims.icarus.language.AvailabilityObserver)
	 */
	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		return get(index, type);
	}	
	
	
	private class NewNGramSentenceData implements DependencyData {


		private static final long serialVersionUID = 4937843525979650838L;
		
		DependencyData dd;
		
		public NewNGramSentenceData(int index) {
			
			dd = (DependencyData) corpus.get(index);
			
		}

		public NewNGramSentenceData clone() {
			return this;
		}
		/**
		 * @see de.ims.icarus.language.SentenceData#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			return dd == null || dd.isEmpty();
		}

		/**
		 * @see de.ims.icarus.language.SentenceData#length()
		 */
		@Override
		public int length() {
			return dd.length();
		}

		/**
		 * @see de.ims.icarus.language.SentenceData#getSourceGrammar()
		 */
		@Override
		public Grammar getSourceGrammar() {
			return dd.getSourceGrammar();
		}

		/**
		 * @see de.ims.icarus.ui.helper.TextItem#getText()
		 */
		@Override
		public String getText() {
			return dd.getText();
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencyData#getForm(int)
		 */
		@Override
		public String getForm(int index) {
			return dd.getForm(index);
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencyData#getPos(int)
		 */
		@Override
		public String getPos(int index) {
			return dd.getPos(index);
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencyData#getRelation(int)
		 */
		@Override
		public String getRelation(int index) {
			return dd.getRelation(index);
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencyData#getLemma(int)
		 */
		@Override
		public String getLemma(int index) {
			return dd.getLemma(index);
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencyData#getFeatures(int)
		 */
		@Override
		public String getFeatures(int index) {
			return dd.getFeatures(index);
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencyData#getHead(int)
		 */
		@Override
		public int getHead(int index) {
			return dd.getHead(index);
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencyData#isFlagSet(int, long)
		 */
		@Override
		public boolean isFlagSet(int index, long flag) {
			return dd.isFlagSet(index, flag);
		}

		/**
		 * @see de.ims.icarus.language.dependency.DependencyData#getFlags(int)
		 */
		@Override
		public long getFlags(int index) {
			return dd.getFlags(index);
		}

		/**
		 * @see de.ims.icarus.language.SentenceData#getIndex()
		 */
		@Override
		public int getIndex() {
			return dd.getIndex();
		}
		
	}

}
