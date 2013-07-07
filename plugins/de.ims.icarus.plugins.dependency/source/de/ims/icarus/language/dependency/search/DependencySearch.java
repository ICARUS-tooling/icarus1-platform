/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.dependency.search;

import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.language.dependency.annotation.DependencyResultAnnotator;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.annotation.ResultAnnotator;
import de.ims.icarus.search_tools.corpus.AbstractCorpusSearch;
import de.ims.icarus.search_tools.tree.TargetTree;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencySearch extends AbstractCorpusSearch {

	public DependencySearch(SearchFactory factory, SearchQuery query, 
			Options options, Object target) {
		super(factory, query, options, target);
	}

	/**
	 * @see de.ims.icarus.search_tools.corpus.AbstractCorpusSearch#createTargetTree()
	 */
	@Override
	protected TargetTree createTargetTree() {
		return new DependencyTargetTree();
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.AbstractTreeSearch#createSource(java.lang.Object)
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
	 * @see de.ims.icarus.search_tools.tree.AbstractTreeSearch#createAnnotator()
	 */
	@Override
	protected ResultAnnotator createAnnotator() {
		return new DependencyResultAnnotator(baseRootMatcher);
	}
}
