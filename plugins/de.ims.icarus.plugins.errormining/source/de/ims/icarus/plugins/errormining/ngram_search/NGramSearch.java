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

import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.language.dependency.annotation.DependencyResultAnnotator;
import de.ims.icarus.language.dependency.search.DependencyTargetTree;
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
	 * @see de.ims.icarus.search_tools.tree.AbstractTreeSearch#createTargetTree()
	 */
	@Override
	protected TargetTree createTargetTree() {
		//TODO other formats?
		return new DependencyTargetTree();
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
	 * @see de.ims.icarus.search_tools.corpus.AbstractCorpusSearch#createAnnotator()
	 */
	@Override
	protected ResultAnnotator createAnnotator() {
		// TODO fix annotator
		return new DependencyResultAnnotator(baseRootMatcher);
	}

}
