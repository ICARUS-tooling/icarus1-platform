/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining.ngram_search;

import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.annotation.ResultAnnotator;
import de.ims.icarus.search_tools.corpus.AbstractCorpusSearch;
import de.ims.icarus.search_tools.tree.TargetTree;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramSearch extends AbstractCorpusSearch {

	/**
	 * @param nGramSearchFactory
	 * @param query
	 * @param options
	 * @param target
	 */
	public NGramSearch(NGramSearchFactory factory,
			SearchQuery query, Options options, Object target) {
		super(factory, query, options, target);
	}

	/**
	 * @see de.ims.icarus.search_tools.corpus.AbstractCorpusSearch#createAnnotator()
	 */
	@Override
	protected ResultAnnotator createAnnotator() {
		// TODO Auto-generated method stub
		return null;
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
	 * @see de.ims.icarus.search_tools.tree.AbstractTreeSearch#createTargetTree()
	 */
	@Override
	protected TargetTree createTargetTree() {
		// TODO Auto-generated method stub
		return null;
	}

}
