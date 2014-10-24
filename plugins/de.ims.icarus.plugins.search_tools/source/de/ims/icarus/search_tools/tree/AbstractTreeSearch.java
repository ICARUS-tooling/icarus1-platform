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
package de.ims.icarus.search_tools.tree;

import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.result.EntryBuilder;
import de.ims.icarus.search_tools.standard.AbstractParallelSearch;
import de.ims.icarus.search_tools.standard.GroupCache;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractTreeSearch extends AbstractParallelSearch {

	protected Matcher baseRootMatcher;

	protected AbstractTreeSearch(SearchFactory factory, SearchQuery query,
			Options parameters, Object target) {
		super(factory, query, parameters, target);
	}

	@Override
	protected boolean validateGraph() {
		return TreeUtils.validateTree(getSearchGraph());
	}

	@Override
	protected void initEngine() {
		baseRootMatcher = new MatcherBuilder(this).createRootMatcher();
		if(baseRootMatcher==null)
			throw new IllegalStateException("Invalid root matcher created"); //$NON-NLS-1$

		baseRootMatcher.setLeftToRight(SearchUtils.isLeftToRightSearch(this));
	}

	@Override
	protected Worker createWorker(int id) {
		return new SearchWorker(id);
	}

	protected abstract TargetTree createTargetTree();

	protected Matcher createRootMatcher() {
		if(baseRootMatcher==null)
			throw new IllegalStateException("No root matcher available!"); //$NON-NLS-1$

		return new MatcherBuilder(this).cloneMatcher(baseRootMatcher);
	}

	protected EntryBuilder createEntryBuilder() {
		return new EntryBuilder(TreeUtils.getMaxId(baseRootMatcher)+1);
	}

	protected GroupCache createCache() {
		return result.createCache();
	}

	protected Options createTreeOptions() {
		return null;
	}

	protected class SearchWorker extends Worker {

		protected TargetTree targetTree;
		protected GroupCache cache;
		protected EntryBuilder entryBuilder;
		protected Matcher rootMatcher;
		protected Options treeOptions;

		protected SearchWorker(int id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch.Worker#init()
		 */
		@Override
		protected void init() {
			targetTree = createTargetTree();
			cache = createCache();
			rootMatcher = createRootMatcher();
			entryBuilder = createEntryBuilder();
			treeOptions = createTreeOptions();

			rootMatcher.setSearchMode(searchMode);
			rootMatcher.setCache(cache);
			rootMatcher.setTargetTree(targetTree);
			rootMatcher.setEntryBuilder(entryBuilder);

			rootMatcher.prepare();
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch.Worker#process()
		 */
		@Override
		protected void process() {
			// Init utilities
			targetTree.reload(buffer.getData(), treeOptions);
			entryBuilder.setIndex(buffer.getIndex());

			// Let matcher do its part
			rootMatcher.matches();
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.AbstractParallelSearch.Worker#cleanup()
		 */
		@Override
		protected void cleanup() {
			rootMatcher.close();
			targetTree.close();
		}
	}
}
