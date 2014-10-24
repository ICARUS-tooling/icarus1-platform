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
package de.ims.icarus.plugins.prosody.search;

import de.ims.icarus.language.coref.registry.CoreferenceRegistry;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.language.dependency.search.DependencySearchFactory;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetSelector.DocumentSetDelegate;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodySearchFactory extends DependencySearchFactory {

	public ProsodySearchFactory() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createSearch(de.ims.icarus.search_tools.SearchQuery, de.ims.icarus.util.Options)
	 */
	@Override
	public Search createSearch(SearchQuery query, Object target, Options options)
			throws UnsupportedFormatException {
		return new ProsodySearch(this, query, options, target);
	}

	@Override
	public ContentType getContentType() {
		return ProsodyUtils.getProsodySentenceContentType();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createExampleSearch()
	 */
	@Override
	public Search createExampleSearch() throws Exception {
		return null;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getSerializedForm()
	 */
	@Override
	public String getSerializedForm() {
		return "de.ims.icarus.prosody@ProsodySearchFactory"; //$NON-NLS-1$
	}

	@Override
	public Object resolveTarget(String target) {

		DocumentSetDescriptor documentSet = CoreferenceRegistry.getInstance().getDocumentSet(target);

		return new DocumentSetDelegate(documentSet);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getSerializedTarget(de.ims.icarus.search_tools.Search)
	 */
	@Override
	public String getSerializedTarget(Search search) {
		DocumentSetDelegate bundle = (DocumentSetDelegate) search.getTarget();

		return bundle.getDescriptor().getId();
	}
}
