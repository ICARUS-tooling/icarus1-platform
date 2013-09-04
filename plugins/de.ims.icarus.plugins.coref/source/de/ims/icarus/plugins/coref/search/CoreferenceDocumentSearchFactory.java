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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.search;

import de.ims.icarus.language.coref.CoreferenceDocumentData;
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
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentSearchFactory implements SearchFactory {

	public CoreferenceDocumentSearchFactory() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createSearch(de.ims.icarus.search_tools.SearchQuery, de.ims.icarus.util.Options)
	 */
	@Override
	public Search createSearch(SearchQuery query, Object target, Options options)
			throws UnsupportedFormatException {
		return new CoreferenceDocumentSearch(this, query, options, target);
	}

	public ContentType getContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(CoreferenceDocumentData.class);
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
		return null;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getDefaultEditorClass()
	 */
	@Override
	public Class<? extends QueryEditor> getDefaultEditorClass() {
		// Allow the framework to assign the default editor implementation
		return null;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getQueryLabel(de.ims.icarus.search_tools.SearchQuery)
	 */
	@Override
	public String getQueryLabel(SearchQuery query) {
		// Allow framework to generate a default label
		return null;
	}

}
