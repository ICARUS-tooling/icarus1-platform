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
import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.search_tools.ConstraintContext;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.standard.AbstractSearchFactory;
import de.ims.icarus.search_tools.standard.DefaultSearchQuery;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentSearchFactory extends AbstractSearchFactory {

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
	 * @see de.ims.icarus.search_tools.SearchFactory#createSearch(java.lang.String, java.lang.String, de.ims.icarus.util.Options)
	 */
	@Override
	public Search createSearch(String q, String t, Options options)
			throws Exception {
		SearchQuery query = createQuery();
		query.parseQueryString(q);

		Object target = resolveTarget(t);

		return createSearch(query, target, options);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getSerializedForm()
	 */
	@Override
	public String getSerializedForm() {
		return "de.ims.icarus.coref@CoreferenceSearchFactory"; //$NON-NLS-1$
	}

	public Object resolveTarget(String target) {
		String[] parts = target.split("@"); //$NON-NLS-1$

		DocumentSetDescriptor documentSet = CoreferenceRegistry.getInstance().getDocumentSet(parts[0]);
		AllocationDescriptor allocation = null;

		if(!DEFAULT_ALLOC.equals(parts[1])) {
			allocation = documentSet.getById(parts[1]);

			if(allocation==null) {
				throw new IllegalArgumentException("No allocation registered for document set matching id: "+parts[1]); //$NON-NLS-1$
			}
		}

		return new CoreferenceDocumentSearchTarget(documentSet, allocation);
	}

	private static final String DEFAULT_ALLOC = "DEFAULT"; //$NON-NLS-1$

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getSerializedTarget(de.ims.icarus.search_tools.Search)
	 */
	@Override
	public String getSerializedTarget(Search search) {
		CoreferenceDocumentSearchTarget bundle = (CoreferenceDocumentSearchTarget) search.getTarget();

		StringBuilder sb = new StringBuilder();

		DocumentSetDescriptor documentSet = bundle.getDocumentSet();
		AllocationDescriptor allocation = bundle.getAllocation();

		sb.append(documentSet.getId()).append("@"); //$NON-NLS-1$

		if(allocation==null) {
			sb.append(DEFAULT_ALLOC);
		} else {
			sb.append(allocation.getId());
		}

		return sb.toString();
	}

}
