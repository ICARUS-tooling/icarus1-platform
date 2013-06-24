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
import net.ikarus_systems.icarus.search_tools.SearchFactory;
import net.ikarus_systems.icarus.search_tools.SearchQuery;
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
			Object target, Options options) {
		super(factory, query, target, options);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.corpus.AbstractCorpusSearch#createTargetTree()
	 */
	@Override
	protected TargetTree createTargetTree() {
		return new DependencyTargetTree();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.corpus.AbstractCorpusSearch#createTargetList(java.lang.Object)
	 */
	@Override
	protected SentenceDataList createTargetList(Object target) {
		ContentType requiredType = DependencyUtils.getDependencyContentType();
		ContentType entryType = ContentTypeRegistry.getEntryType(target);
		if(entryType==null || !ContentTypeRegistry.isCompatible(requiredType, entryType))
			throw new IllegalArgumentException("Target is not a container holding dependency data items"); //$NON-NLS-1$
		
		if(!(target instanceof SentenceDataList))
			throw new IllegalArgumentException("Target is not a list"); //$NON-NLS-1$
		
		return (SentenceDataList) target;
	}
}
