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
 *
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining.ngram_search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.errormining.DependencyItemInNuclei;
import de.ims.icarus.plugins.errormining.DependencySentenceInfo;
import de.ims.icarus.plugins.errormining.ItemInNuclei;
import de.ims.icarus.plugins.errormining.NGramData;
import de.ims.icarus.plugins.errormining.NGramQAttributes;
import de.ims.icarus.plugins.errormining.NGrams;
import de.ims.icarus.plugins.errormining.NGramsDependency;
import de.ims.icarus.plugins.errormining.annotation.NGramHighlighting;
import de.ims.icarus.plugins.errormining.annotation.NGramResultAnnotator;
import de.ims.icarus.plugins.errormining.ngram_tools.NGramParameters;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchNode;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.annotation.AnnotationBuffer;
import de.ims.icarus.search_tools.annotation.ResultAnnotator;
import de.ims.icarus.search_tools.result.AbstractSearchResult;
import de.ims.icarus.search_tools.result.DefaultSearchResult0D;
import de.ims.icarus.search_tools.result.EntryBuilder;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.standard.AbstractParallelSearch;
import de.ims.icarus.search_tools.standard.DefaultSearchGraph;
import de.ims.icarus.search_tools.standard.GroupCache;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramSearch extends AbstractParallelSearch implements NGramParameters{
	
	protected ErrorminingMatcher matcher;
	protected final boolean useFringe;
	protected final int fringeStart;
	protected final int fringeEnd;
	protected final boolean useNumberWildcard;
	protected final int ngramResultLimit;
	protected final int gramsGreaterX;
	protected final int sentenceLimit;
	protected final boolean createXML;
	protected List<NGramQAttributes> nqList;
	

	/**
	 * @param nGramSearchFactory
	 * @param query
	 * @param options
	 * @param target
	 */
	public NGramSearch(NGramSearchFactory factory,
			SearchQuery query, Options options, Object target) {
		super(factory, query, options, target);

		useNumberWildcard = getParameters().getBoolean(USE_NUMBER_WILDCARD, DEFAULT_USE_NUMBER_WILDCARD);
		useFringe = getParameters().getBoolean(USE_FRINGE_HEURISTIC, DEFAULT_USE_FRINGE_HEURISTIC);
		fringeStart = getParameters().getInteger(FRINGE_START, DEFAULT_FRINGE_START);
		fringeEnd = getParameters().getInteger(FRINGE_END, DEFAULT_FRINGE_END);
		ngramResultLimit = getParameters().getInteger(NGRAM_RESULT_LIMIT, DEFAULT_NGRAM_RESULT_LIMIT);
		sentenceLimit = getParameters().getInteger(SENTENCE_LIMIT, DEFAULT_SENTENCE_LIMIT);
		gramsGreaterX = getParameters().getInteger(GRAMS_GREATERX, DEFAULT_GRAMS_GREATERX);
		createXML = getParameters().getBoolean(CREATE_XML_OUTPUT, DEFAULT_CREATE_XML_OUTPUT);
	}


	public NGramSearch(NGramSearchFactoryDependency factory,
			SearchQuery query, Options options, Object target) {
		super(factory, query, options, target);
		
		useNumberWildcard = getParameters().getBoolean(USE_NUMBER_WILDCARD, DEFAULT_USE_NUMBER_WILDCARD);
		useFringe = getParameters().getBoolean(USE_FRINGE_HEURISTIC, DEFAULT_USE_FRINGE_HEURISTIC);
		fringeStart = getParameters().getInteger(FRINGE_START, DEFAULT_FRINGE_START);
		fringeEnd = getParameters().getInteger(FRINGE_END, DEFAULT_FRINGE_END);
		ngramResultLimit = getParameters().getInteger(NGRAM_RESULT_LIMIT, DEFAULT_NGRAM_RESULT_LIMIT);
		sentenceLimit = getParameters().getInteger(SENTENCE_LIMIT, DEFAULT_SENTENCE_LIMIT);
		gramsGreaterX = getParameters().getInteger(GRAMS_GREATERX, DEFAULT_GRAMS_GREATERX);
		createXML = getParameters().getBoolean(CREATE_XML_OUTPUT, DEFAULT_CREATE_XML_OUTPUT);
	}



	/**
	 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch#init()
	 */
	@Override
	public boolean init() {
		initEngine();
		
		result = createResult();
		if(result==null) {
			return false;
		}
		
		source = createSource(SearchManager.getTarget(this));
		if(source==null)
			throw new IllegalStateException("Invalid source created"); //$NON-NLS-1$
		
		return true;
	}

	

	/**
	 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch#getMaxWorkerCount()
	 */
	@Override
	protected int getMaxWorkerCount() {
		return 1;
	}


	/**
	 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch#createResult()
	 */
	@Override
	protected SearchResult createResult() {
		SearchResult result = new NGramSearchResult(this);
		
		
		ResultAnnotator annotator = createAnnotator();
		if(annotator!=null && result instanceof AbstractSearchResult) {
			AnnotationBuffer annotationBuffer = new AnnotationBuffer(
					result, annotator, ANNOTATION_BUFFER_SIZE);
			((AbstractSearchResult)result).setAnnotationBuffer(annotationBuffer);
		}
		
		return result;
	}


	/**
	 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch#itemProcessed(de.ims.icarus.search_tools.standard.AbstractParallelSearch.ItemBuffer)
	 */
	@Override
	protected void itemProcessed(ItemBuffer buffer) {
		synchronized (this) {
			processed++;
			double total = source.size();
			setProgress(50 + (int)(processed/total * 50d));
		}
	}


	/**
	 * @see de.ims.icarus.search_tools.corpus.AbstractCorpusSearch#createSource(java.lang.Object)
	 */
	@Override
	protected SentenceDataList createSource(Object target) {
		ContentType requiredType = DependencyUtils.getDependencyContentType();
		ContentType entryType = ContentTypeRegistry.getEntryType(target);
		if(entryType==null || !ContentTypeRegistry.isCompatible(requiredType, entryType))
			throw new IllegalArgumentException("Target is not a container holding dependency data items"); //$NON-NLS-1$
		
		if(!(target instanceof SentenceDataList))
			throw new IllegalArgumentException("Target is not a list"); //$NON-NLS-1$
		
		return (SentenceDataList) target;
	}



	/**
	 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch#validateGraph()
	 */
	@Override
	protected boolean validateGraph() {
		return true;
	}


	/**
	 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch#initEngine()
	 */
	@Override
	protected void initEngine() {
		// TODO Auto-generated method stub		
	}


	/**
	 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch#createWorker(int)
	 */
	@Override
	protected Worker createWorker(int id) {

		if(getFactory() instanceof NGramSearchFactory) {
			//System.out.println("PoS Search");
			return new NGramWorker(id);
		}
		
		if(getFactory() instanceof NGramSearchFactoryDependency) {
			//System.out.println("Dependency Search");
			return new NGramWorkerDependency(id);
		}
		
		
		LoggerFactory.log(this,Level.SEVERE, "Unknown ErrorMining Factory"); //$NON-NLS-1$
		return null;
		
	}


	/**
	 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch#createAnnotator()
	 */
	@Override
	protected ResultAnnotator createAnnotator() {
		return new NGramResultAnnotator(NGramHighlighting.getInstance(), nqList);
	}
	
	
	protected EntryBuilder createEntryBuilder() {
		//TODO only one node so entrybuilder 1 is sufficient (no tree matching!!)
		return new EntryBuilder(1);
	}
	
	protected GroupCache createCache() {
		return result.createCache();
	}

	
	protected Options createOptions() {
		
		Options options = new Options();
		options.put("UseFringe", useFringe); //$NON-NLS-1$
		options.put("FringeSTART", fringeStart); //$NON-NLS-1$
		options.put("FringeEND", fringeEnd); //$NON-NLS-1$ // 0 = infinity , number = limit

		options.put("NGramLIMIT", ngramResultLimit); //$NON-NLS-1$
		options.put("UseNumberWildcard", useNumberWildcard); //$NON-NLS-1$
		return options;
	}
	
	

	@SuppressWarnings("unchecked")
	protected List<NGramQAttributes> createQueryList(){
		DefaultSearchGraph graph = (DefaultSearchGraph) getSearchGraph();
		if (graph.getNodes() != null) {
			SearchNode[] sn = new SearchNode[1];
			sn = graph.getNodes();
			SearchConstraint[] constraints = new SearchConstraint[1];			
			
			//holding object list<ngrammattributes>
			constraints = sn[0].getConstraints();			

			nqList = (List<NGramQAttributes>) constraints[0].getValue();
		} else {
			//When we have no query specified new empty list
			nqList = new ArrayList<NGramQAttributes>();
		}
		return nqList;		
	}



	protected ErrorminingMatcher createNGramResultMatcher() {
		
		if(getFactory() instanceof NGramSearchFactory){
			return new NGramResultMatcherPoS();
		}
		
		if(getFactory() instanceof NGramSearchFactoryDependency){
			return new NGramResultMatcherDependency();
		}
		
		LoggerFactory.log(this,Level.SEVERE, "Unnown ErrorMining Factory"); //$NON-NLS-1$
		return null;
	}

	
//stuff for dependency
		protected class NGramWorkerDependency extends Worker{
			
			protected GroupCache cache;
			protected EntryBuilder entryBuilder;
		
			
			protected Map<String, ArrayList<DependencyItemInNuclei>> ngramsResultMap;
			protected List<MappedNGramResult> helferList;
			
			protected Options ngramOptions;
			protected NGramsDependency ngrams;
			protected List<NGramQAttributes> queryList;
	
			/**
			 * @param id
			 */
			protected NGramWorkerDependency(int id) {
				super(id);
			}
	
			/**
			 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch.Worker#init()
			 */
			@Override
			protected void init() {			
				//System.out.println(source.size());
				
				cache = createCache();
				entryBuilder = createEntryBuilder();
				matcher = createNGramResultMatcher();
				
				Options ngramOptions = createOptions();
	
				
				ngrams = new NGramsDependency(ngramOptions, createQueryList());
				
				int sentenceNr = 1;
	
				// if zero read everything
				// 1 makes no sense at all (can't find any ngrams)
				// we treat one same as zero (look at all sentences...)
				if (sentenceLimit == 0) {
					for (int i = 0; i < source.size(); i++){
						
						SentenceData sd = (SentenceData)source.get(i);
						
						//Debug
						// for (int j = 0 ; j < dd.length(); j++){
						// 	System.out.print(dd.getForm(j) + " " + dd.getPos(j) + " ");
						// }
						// System.out.println();
						
						ngrams.initializeUniGrams((DependencyData) sd, sentenceNr);
						//NGrams.getInstance().initializeUniGrams((DependencyData) sd, sentenceNr);
		
						sentenceNr++;					
					}				
				} 
				//read either only sentences until limit or corpussize
				else {
					for (int i = 0; i < source.size(); i++){
						
						SentenceData sd = (SentenceData)source.get(i);
						
						//Debug
						// for (int j = 0 ; j < dd.length(); j++){
						// 	System.out.print(dd.getForm(j) + " " + dd.getPos(j) + " ");
						// }
						// System.out.println();
						if(sentenceNr <= sentenceLimit){
							ngrams.initializeUniGrams((DependencyData) sd, sentenceNr);
							//NGrams.getInstance().initializeUniGrams((DependencyData) sd, sentenceNr);
						}
						sentenceNr++;					
					}	
					
				}
				
				ngrams.nGramResults();					
				
				ngramsResultMap = ngrams.getResult();
	
				helferList = new ArrayList<MappedNGramResult>();
	
				List<String> tmpKey = new ArrayList<String>(ngramsResultMap.keySet());
				Collections.reverse(tmpKey);
				
				for (int i = 0; i < tmpKey.size();i++){
					String key = tmpKey.get(i);
					
					if(key.split(" ").length > gramsGreaterX){ //$NON-NLS-1$
						ArrayList<DependencyItemInNuclei> value = ngramsResultMap.get(key);
						for (int j = 0; j < value.size();j++){
							DependencyItemInNuclei iin = value.get(j);
		//					System.out.println("PoSTag: "+ iin.getPosTag() +
		//									  " PoSCount: " + iin.getCount());
							
								for (int k = 0; k < iin.getSentenceInfoSize(); k++){
									DependencySentenceInfo si = iin.getSentenceInfoAt(k);
									int sentenceNR = si.getSentenceNr()-1;
		//							System.out.println(key + " " + sentenceNR);
		//							System.out.println(
		//									iin.getSentenceInfoAt(k).getNucleiIndexListSize());
									
									MappedNGramResult mapping = 
											new MappedNGramResult(sentenceNR, key, si);
									if(helferList.contains(mapping)){	
										//donothing
									} else {
										helferList.add(mapping);	
									}
								}
						}
					}
					
					//ggf direkt in matcher erzeugen lassen
					matcher.setSentenceList(helferList);
					((ErrorMiningMatcherDependency) matcher).setResultNGramsDependency(ngramsResultMap);
					matcher.setCache(cache);
					matcher.setEntryBuilder(entryBuilder);
					
				}
				
				if(createXML){
					ngrams.outputToFile();
				}
				
				result.setProperty("COMPLETE_NGRAM", ngramsResultMap); //$NON-NLS-1$
				result.setProperty("LARGEST_NGRAM", ngrams.getPasses()); //$NON-NLS-1$
				result.setProperty("MODE", 1); //$NON-NLS-1$
				
				
				//Debug output for all sentences in result with largest ngram-span
	//			for (int i = 0; i < helferList.size();i++){
	//				System.out.println(helferList.get(i).getIndex()+1 + " "
	//						+ helferList.get(i).getKey());
	//			}
				

				//NGrams.getInstance().nGramResults();	
			}
	
			/**
			 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch.Worker#process()
			 */
			@Override
			protected void process() {
				
				//only show results when we have hits....
				if(ngramsResultMap.size() > 0){
					
					//System.out.println(buffer.getData());
					
					//initialisiere
					entryBuilder.setIndex(buffer.getIndex());
					
					//check for hits
					matcher.matches(buffer.getIndex());		

				}	
			}
	
			/**
			 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch.Worker#cleanup()
			 */
			@Override
			protected void cleanup() {
//				System.out.println("Matches: " + result.getTotalMatchCount());
//				System.out.println("Hits: " + result.getTotalHitCount());
//				System.out.println(result.getAnnotationType());
				
			}
			
		}



protected class NGramWorker extends Worker{
		
		protected GroupCache cache;
		protected EntryBuilder entryBuilder;	
		
		protected Map<String, ArrayList<ItemInNuclei>> ngramsResultMap;
		protected List<MappedNGramResult> helferList;
		
		protected Options ngramOptions;
		protected NGrams ngrams;
		protected List<NGramQAttributes> queryList;

		/**
		 * @param id
		 */
		protected NGramWorker(int id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch.Worker#init()
		 */
		@Override
		protected void init() {		

			//System.out.println(source.size());
			
			cache = createCache();
			entryBuilder = createEntryBuilder();
			matcher = (NGramResultMatcherPoS) createNGramResultMatcher();
			
			Options ngramOptions = createOptions();
			
			ngrams = new NGrams(ngramOptions, createQueryList());
			

			int maxSentences = sentenceLimit;
			
			if(maxSentences== 0){
				maxSentences = source.size()-1;
			}
			
			//progress bar
			double progress = 0;
			
			// if zero read everything
			// 1 makes no sense at all (can't find any ngrams)
			// we treat one same as zero (look at all sentences...)
			for (int i = 0; i <= maxSentences; i++){					
					SentenceData sd = (SentenceData)source.get(i);					
					//Debug
					// for (int j = 0 ; j < dd.length(); j++){
					// 	System.out.print(dd.getForm(j) + " " + dd.getPos(j) + " ");
					// }
					// System.out.println();					
					ngrams.initializeUniGrams((DependencyData) sd, i);
					//NGrams.getInstance().initializeUniGrams((DependencyData) sd, sentenceNr);
					progress = (double) i / maxSentences * 50d;
					setProgress((int)progress);			
			}
			
			//start generation process
			ngrams.nGramResults();			
			//grab result map
			ngramsResultMap = ngrams.getResult();

			
			//FIXME
			//remove unwanted nucleus
			//ngrams.cleanUpNucleus();
			
			helferList = new ArrayList<MappedNGramResult>();

			//List<String> tmpKey = new ArrayList<String>(ngramsResultMap.keySet());
			//Collections.reverse(tmpKey);
			
//			for (String key : ngramsResultMap.keySet()) {
//
//				if (key.split(" ").length > gramsGreaterX) { //$NON-NLS-1$
//
//					ArrayList<ItemInNuclei> item = ngramsResultMap.get(key);
//					for (int j = 0; j < item.size(); j++) {
//						ItemInNuclei iin = item.get(j);
//						// System.out.println("PoSTag: "+ iin.getPosTag() +
//						// " PoSCount: " + iin.getCount());
//
//						for (int k = 0; k < iin.getSentenceInfoSize(); k++) {
//							SentenceInfo si = iin.getSentenceInfoAt(k);
//							int sentenceNR = si.getSentenceNr() - 1;
//							// System.out.println(key + " " + sentenceNR);
//							// System.out.println(
//							// iin.getSentenceInfoAt(k).getNucleiIndexListSize());
//
//							// FIXME zwei nuclei im satz aber nicht
//							// verschmolzen!
//							MappedNGramResult mapping = new MappedNGramResult(
//									sentenceNR, key, si);
//							
//							helferList.add(mapping);
//
////							if (helferList.contains(mapping)) {
////								// donothing
////								// System.out.println(si.getSentenceNr() +
////								// key);;
////								if (disjunctNuclei(mapping, key, helferList)) {
////									// FIXME enable für sentence hitcount
////									// MappedNGramResult tmp =
////									// helferList.get(helferList.indexOf(mapping));
////									// tmp.addKey(key);
////									helferList.add(mapping);
////								}
////							} else {
////								helferList.add(mapping);
////							}
//						}
//					}
//				}
//			}
//				
////				System.out.println("~~~~~~~~~~~~~~~~~~~~");
////				for (int x = 0; x < helferList.size();x++){					
////					System.out.println(helferList.get(x).getCoverStart());
////					System.out.println(helferList.get(x).getCoverEnd());
////					System.out.println(helferList.get(x).getIndex());
////				}
//			
//			//ggf direkt in matcher erzeugen lassen
//			matcher.setSentenceList(helferList);
//			((NGramResultMatcherPoS) matcher).setResultNGrams(ngramsResultMap);
			
			matcher.setCache(cache);
			matcher.setEntryBuilder(entryBuilder);						
			
			if(createXML){
				ngrams.outputToFile();
			}			

			
			//TODO
			result.setProperty("COMPLETE_NGRAM", ngramsResultMap); //$NON-NLS-1$
			result.setProperty("LARGEST_NGRAM", ngrams.getPasses()); //$NON-NLS-1$
			result.setProperty("MODE", 0); //$NON-NLS-1$
			
			//Debug output for all sentences in result with largest ngram-span
//			for (int i = 0; i < helferList.size();i++){
//				System.out.println(helferList.get(i).getIndex()+1 + " "
//						+ helferList.get(i).getKey());
//			}
			
			//ngrams.outputToFile();
			//NGrams.getInstance().nGramResults();
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch.Worker#process()
		 */
		@Override
		protected void process() {
			
			//only show results when we have hits....
			if(ngramsResultMap.size() > 0){
			
			//System.out.println(ngramsResultMap.size());
			//System.out.println("BUFFER Index->" + buffer.getIndex());
			
//			//initialisiere
			entryBuilder.setIndex(buffer.getIndex());
			
//			//check for hits
			matcher.matches(buffer.getIndex());		
			
//			// for every entry in  list we check if current buffer index 
//			// equals; note all indices saved within the list are results and have
//			// to show up in the resulting list!
//			for(int i = 0; i < helferList.size(); i++){
//				if(helferList.get(i).containsIntIndex(buffer.getIndex())){				
//					cache.commit(entryBuilder.toEntry());
//				}
//			}	

			}

		}

		/**
		 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch.Worker#cleanup()
		 */
		@Override
		protected void cleanup() {
//			System.out.println("Matches: " + result.getTotalMatchCount());
//			System.out.println("Hits: " + result.getTotalHitCount());
//			System.out.println(result.getAnnotationType());
//			System.out.println(result.getContentType());
		}
		
	}



	/**
	 * we only want every sentence at most once in our results, therefore we have
	 * to check if for a given key we already created a hit. this is done by simple
	 * string contains matching. When a ngram contains more than one key this is 
	 * already represented by two nucleis within the ngram. Otherwise we have a 
	 * new key which is not covered by the ngram we have added so far and we 
	 * add the new key to the helferList which contains all keys (larges ngrams) 
	 * from an given variation nuclei
	 * 
	 * @param mapping
	 * @param ngramsResultMap 
	 * @param helferList 
	 * @return 
	 */
	private boolean disjunctNuclei(MappedNGramResult mapping, String newKey, List<MappedNGramResult> helferList) {
		MappedNGramResult tmp = helferList.get(helferList.indexOf(mapping));
		
		boolean newHit = false;
		//TODO remove crappy stuff below
//		for(int i = 0; i < tmp.getKeyListSize(); i++){
//			//System.out.println(tmp.getKeyAt(i).equals(newKey));
//			if (!tmp.getKeyAt(i).equals(newKey)){
//				newHit = true;
//			}			
//		}
		
        for(int i = 0; i < tmp.getKeyListSize(); i++){
            if (!tmp.getKeyAt(i).contains(newKey)){
                newHit = true;
            }
        }
		
//		System.out.println(mapping.getCoverStart() + " " + mapping.getCoverEnd());
//		System.out.println(tmp.getCoverStart() + " " + tmp.getCoverEnd());
		
//		if(mapping.getCoverStart() < tmp.getCoverStart()){
//			newHit = true;
//		}
//		
//		if(mapping.getCoverEnd() > tmp.getCoverEnd()){
//			newHit = true;
//		}
		
		return newHit;
	}
	
	private static class NGramSearchResult extends DefaultSearchResult0D {

		/**
		 * @param search
		 */
		public NGramSearchResult(Search search) {
			super(search);
		}

		/**
		 * @see de.ims.icarus.search_tools.result.AbstractSearchResult#getContentType()
		 */
		@Override
		public ContentType getContentType() {
			return ContentTypeRegistry.getInstance().getTypeForClass(NGramData.class);
		}
		
	}
}
