/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.dependency.search;

import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.language.dependency.annotation.DependencyResultAnnotator;
import net.ikarus_systems.icarus.search_tools.SearchFactory;
import net.ikarus_systems.icarus.search_tools.SearchQuery;
import net.ikarus_systems.icarus.search_tools.annotation.ResultAnnotator;
import net.ikarus_systems.icarus.search_tools.corpus.AbstractCorpusSearch;
import net.ikarus_systems.icarus.search_tools.tree.TargetTree;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

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
	 * @see net.ikarus_systems.icarus.search_tools.corpus.AbstractCorpusSearch#createTargetTree()
	 */
	@Override
	protected TargetTree createTargetTree() {
		return new DependencyTargetTree();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.tree.AbstractTreeSearch#createSource(java.lang.Object)
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
	 * @see net.ikarus_systems.icarus.search_tools.tree.AbstractTreeSearch#createAnnotator()
	 */
	@Override
	protected ResultAnnotator createAnnotator() {
		return new DependencyResultAnnotator(baseRootMatcher);
	}
}
