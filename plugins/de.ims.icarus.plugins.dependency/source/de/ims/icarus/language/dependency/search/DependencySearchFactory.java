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
package de.ims.icarus.language.dependency.search;

import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankRegistry;
import de.ims.icarus.plugins.language_tools.treebank.TreebankManagerPerspective;
import de.ims.icarus.search_tools.ConstraintContext;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.standard.DefaultParameterEditor;
import de.ims.icarus.search_tools.standard.DefaultSearchQuery;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencySearchFactory implements SearchFactory {

	public DependencySearchFactory() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createSearch(de.ims.icarus.search_tools.SearchQuery, de.ims.icarus.util.Options)
	 */
	@Override
	public Search createSearch(SearchQuery query, Object target, Options options)
			throws UnsupportedFormatException {
		return new DependencySearch(this, query, options, target);
	}

	public ContentType getContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(DependencyData.class);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createQuery()
	 */
	@Override
	public SearchQuery createQuery() {
		return new DefaultSearchQuery(getContentType());
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getConstraintContext()
	 */
	@Override
	public ConstraintContext getConstraintContext() {
		return SearchManager.getInstance().getConstraintContext(getContentType());
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createParameterEditor()
	 */
	@Override
	public Editor<Options> createParameterEditor() {
		return new DefaultParameterEditor();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createExampleSearch()
	 */
	@Override
	public Search createExampleSearch() throws Exception {
		// Ensure target treebank
		TreebankManagerPerspective.ensureExampleTreebank();
		
		// Generate query
		SearchQuery query = new DefaultSearchQuery(getContentType());
		query.parseQueryString("[lemma<*>1[relation=SBJ,lemma<*>2]]"); //$NON-NLS-1$
		
		// Fetch target
		String name = TreebankManagerPerspective.EXAMPLE_TREEBANK_NAME;
		Treebank treebank = TreebankRegistry.getInstance().getTreebankByName(name);
		if(treebank==null)
			throw new IllegalStateException("Missing example treebank: "+name); //$NON-NLS-1$
		Object target = TreebankRegistry.getInstance().getListDelegate(treebank);
		
		return createSearch(query, target, null);
	}

}