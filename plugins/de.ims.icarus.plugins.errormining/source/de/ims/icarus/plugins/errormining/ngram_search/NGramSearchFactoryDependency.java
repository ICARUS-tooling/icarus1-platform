/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus G�rtner and Gregor Thiele
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

import java.util.List;

import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.plugins.errormining.NGramQAttributes;
import de.ims.icarus.plugins.errormining.NGramQueryEditor;
import de.ims.icarus.plugins.search_tools.view.editor.QueryEditor;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.ConstraintContext;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchNode;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.standard.DefaultSearchGraph;
import de.ims.icarus.search_tools.standard.DefaultSearchQuery;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramSearchFactoryDependency implements SearchFactory {

	public NGramSearchFactoryDependency() {
		// no-op
	}


	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createSearch(de.ims.icarus.search_tools.SearchQuery, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public Search createSearch(SearchQuery query, Object target, Options options)
			throws UnsupportedFormatException {
		return new NGramSearch(this, query, options, target);
	}

	public ContentType getContentType() {
		//return ContentTypeRegistry.getInstance().getTypeForClass(DependencyData.class);
		return DependencyUtils.getDependencyContentType();
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
		return new NGramParameterEditor();
	}


	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createExampleSearch()
	 */
	@Override
	public Search createExampleSearch() throws Exception {
		return null;
	}


	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getDefaultEditorClass()
	 */
	@Override
	public Class<? extends QueryEditor> getDefaultEditorClass() {
		return NGramQueryEditor.class;
	}


	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getQueryLabel(de.ims.icarus.search_tools.SearchQuery)
	 */
	@Override
	public String getQueryLabel(SearchQuery query) {
		String queryLabel = ResourceManager.getInstance()
				.get("plugins.errorMining.nGramQueryEditor.labelNoQuery"); //$NON-NLS-1$

		//Extract NGramQuerylist  from Search Graph
		if (query != null){

			DefaultSearchGraph graph = (DefaultSearchGraph) query.getSearchGraph();

			if (graph.getNodes() != null) {
				SearchNode[] sn = new SearchNode[1];
				sn = graph.getNodes();
				SearchConstraint[] constraints = new SearchConstraint[1];

				//holding object list<ngrammattributes>
				constraints = sn[0].getConstraints();

				@SuppressWarnings("unchecked")
				List<NGramQAttributes> nqList = (List<NGramQAttributes>)
														constraints[0].getValue();
				if(nqList.size() == 1){
					queryLabel = ResourceManager.getInstance()
							.get("plugins.errorMining.nGramQueryEditor.labelOneQuery"); //$NON-NLS-1$
				} else {
					queryLabel = nqList.size() + " "+ ResourceManager.getInstance() //$NON-NLS-1$
							.get("plugins.errorMining.nGramQueryEditor.labelMultiQuery"); //$NON-NLS-1$

				}
				//System.out.println("Using #Querys: " + nqList.size()); //$NON-NLS-1$
			}
		}

		return queryLabel;
	}


	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createSearch(java.lang.String, java.lang.String, de.ims.icarus.util.Options)
	 */
	@Override
	public Search createSearch(String query, String target, Options options)
			throws Exception {
		throw new UnsupportedOperationException();
	}


	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getSerializedForm()
	 */
	@Override
	public String getSerializedForm() {
		throw new UnsupportedOperationException();
	}


	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getSerializedTarget(de.ims.icarus.search_tools.Search)
	 */
	@Override
	public String getSerializedTarget(Search search) {
		throw new UnsupportedOperationException();
	}

}
