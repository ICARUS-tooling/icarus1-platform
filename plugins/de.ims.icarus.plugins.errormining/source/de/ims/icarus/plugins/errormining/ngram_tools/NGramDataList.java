/* 
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
import de.ims.icarus.language.annotation.AnnotatedSentenceData;
import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.language.dependency.MutableDependencyData.DependencyDataEntry;
import de.ims.icarus.plugins.errormining.ErrorMiningConstants;
import de.ims.icarus.plugins.errormining.ItemInNuclei;
import de.ims.icarus.util.annotation.Annotation;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramDataList implements SentenceDataList {
	
	
	/**
	 * @return the nGramMap
	 */
	public Map<String, ArrayList<ItemInNuclei>> getnGramMap() {
		return nGramMap;
	}

	/**
	 * @param nGramMap the nGramMap to set
	 */
	public void setnGramMap(Map<String, ArrayList<ItemInNuclei>> nGramMap) {
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




	protected Map<String,ArrayList<ItemInNuclei>> nGramMap;
	protected int index;
	protected List<Integer> sentences;
	
	
	protected List<SentenceData> corpus;
	protected Map<Integer, NewNGramSentenceData> nGramMapCache;
	
	//SentenceView
	protected List<CorpusType> corpusList;	

	
	
	public NGramDataList(Map<String,ArrayList<ItemInNuclei>> nGramMap,
			List<SentenceData> corpus){
		if (nGramMap == null)
			throw new IllegalArgumentException("No Data"); //$NON-NLS-1$
		
		this.corpus = corpus;
		setNGramMap(nGramMap);
		
	}
	
	public List<CorpusType> getCorpusList(){
		return corpusList;
	}

	/**
	 * @param nGramMap
	 */
	void setNGramMap(Map<String, ArrayList<ItemInNuclei>> nGramMap) {
		if (this.nGramMap != null) {
			return;
		}
		nGramMapCache = new HashMap<Integer, NewNGramSentenceData>();

		//TODO
		this.nGramMap = nGramMap;
		
		filterNGramMap(nGramMap);
	}
	
	
	private Map<String, ArrayList<ItemInNuclei>> filterNGramMap(Map<String, ArrayList<ItemInNuclei>> nGramMap){
		
		List<String> tmpKey = new ArrayList<String>(nGramMap.keySet());
		Collections.reverse(tmpKey);
		
		corpusList = new ArrayList<CorpusType>();
		sentences = new ArrayList<Integer>();
		
		Map<String, ArrayList<ItemInNuclei>> filterMap = new LinkedHashMap<String,ArrayList<ItemInNuclei>>();
		
//		
//		for(int j = 0; j < tmpKey.size(); j++){
//			System.out.println();
//			System.out.println("key" + tmpKey.get(j));
//		}
		
		
		for(int i = 0; i < tmpKey.size(); i++){
			String key = tmpKey.get(i);
			String[] keysplit = key.split(" "); //$NON-NLS-1$
			
			//System.out.println(key + keysplit.length);
			
			ArrayList<ItemInNuclei> value = nGramMap.get(key);
			for (int j = 0; j < value.size();j++){
				ItemInNuclei iin = value.get(j);
//				System.out.println("PoSTag: "+ iin.getPosTag() +
//								  " PoSCount: " + iin.getCount());
				
					for (int k = 0; k < iin.getSentenceInfoSize(); k++){
						int sentenceNR = iin.getSentenceInfoAt(k).getSentenceNr()-1;
//						System.out.println(key + " " + sentenceNR);
						if(sentences.contains(sentenceNR)){	
							//donothing
						} else {
							if(keysplit.length > 1 ){
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
		
		return filterMap;
	}
	
	private Map<String, ArrayList<ItemInNuclei>> getNGramMap(){
		return nGramMap;
	}
	
	
	private List<Integer> getSentences(){
		return sentences;
	}
	
	
	private SentenceData getNGramDataFromIndex(int index) {
		if (!nGramMapCache.containsKey(index)) {
			NewNGramSentenceData ngramData = new NewNGramSentenceData(index);
			nGramMapCache.put(index, ngramData);
		}
		return nGramMapCache.get(index);
	}


	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		//return getNGramMap().size();
		return sentences.size();
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
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType)
	 */
	@Override
	public SentenceData get(int index, DataType type) {
		if (type != DataType.SYSTEM) {
			return null;
		}
		return sentences == null ? null : getNGramDataFromIndex(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType, net.ikarus_systems.icarus.language.AvailabilityObserver)
	 */
	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		return get(index, type);
	}
	
	
	
	
//	//data class
//	private class NewNGramSentenceData implements AnnotatedSentenceData {
//
//		private static final long serialVersionUID = 3303973536847711267L;
//
//		private List<DependencyDataEntry> items = new ArrayList<>();
//		
//		protected Annotation annotation = null; // TODO change to default value?
//		
//		DependencyData dd;
//		
//		public NewNGramSentenceData(int index){
//			dd = (DependencyData) corpus.get(index);
//			
//			
//		}
//		
//		
//		//TODO
//		@Override
//		public NewNGramSentenceData clone() {
//			return this;
//		}
//		
//		
//
//		/**
//		 * @see net.ikarus_systems.icarus.language.SentenceData#getForm(int)
//		 */
//		@Override
//		public String getForm(int index) {
//			//return items.get(index).getForm();
//			return dd.getForm(index);
//		}
//
//		/**
//		 * @see net.ikarus_systems.icarus.language.SentenceData#isEmpty()
//		 */
//		@Override
//		public boolean isEmpty() {
//			//return items.isEmpty();
//			return dd == null || dd.isEmpty();
//		}
//
//		/**
//		 * @see net.ikarus_systems.icarus.language.SentenceData#length()
//		 */
//		@Override
//		public int length() {
//			//return items.size();
//			return dd.length();
//		}
//
//		/**
//		 * @see net.ikarus_systems.icarus.language.SentenceData#getSourceGrammar()
//		 */
//		@Override
//		public Grammar getSourceGrammar() {
//			return DependencyUtils.getDependencyGrammar();
//		}
//
//		/**
//		 * @see net.ikarus_systems.icarus.language.annotation.AnnotatedSentenceData#getAnnotation()
//		 */
//		@Override
//		public Annotation getAnnotation() {
//			return annotation;
//		}
//
//
//		/**
//		 * @see net.ikarus_systems.icarus.ui.helper.TextItem#getText()
//		 */
//		@Override
//		public String getText() {
//			return dd.getText();
//		}
//
//	}

	
	
	/**
	 * 
	 * @author Gregor Thiele
	 * @version $Id$
	 *
	 */
	
	
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
		
	}


}
