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
 * $Revision: 126 $ 
 * $Date: 2013-08-02 15:07:35 +0200 (Fr, 02 Aug 2013) $ 
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.errormining/source/de/ims/icarus/plugins/errormining/ngram_search/NGramSearchFactory.java $ 
 * 
 * $LastChangedDate: 2013-08-02 15:07:35 +0200 (Fr, 02 Aug 2013) $  
 * $LastChangedRevision: 126 $  
 * $LastChangedBy: mcgaerty $ 
 */
package de.ims.icarus.plugins.errormining.ngram_search;

import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.plugins.errormining.NGramQueryEditor;
import de.ims.icarus.plugins.search_tools.view.editor.QueryEditor;
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
 * @author Gregor Thiele
 * @version $Id: NGramSearchFactory.java 126 2013-08-02 13:07:35Z mcgaerty $
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

}
