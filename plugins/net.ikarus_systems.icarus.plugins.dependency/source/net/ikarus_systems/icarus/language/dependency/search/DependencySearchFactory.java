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

import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.search_tools.ConstraintContext;
import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchFactory;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.SearchQuery;
import net.ikarus_systems.icarus.search_tools.standard.DefaultParameterEditor;
import net.ikarus_systems.icarus.search_tools.standard.DefaultSearchQuery;
import net.ikarus_systems.icarus.ui.helper.Editor;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.UnsupportedFormatException;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

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
	 * @see net.ikarus_systems.icarus.search_tools.SearchFactory#createSearch(net.ikarus_systems.icarus.search_tools.SearchQuery, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public Search createSearch(SearchQuery query, Object target, Options options)
			throws UnsupportedFormatException {
		return new DependencySearch(this, query, target, options);
	}

	public ContentType getContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(DependencyData.class);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchFactory#createQuery()
	 */
	@Override
	public SearchQuery createQuery() {
		return new DefaultSearchQuery(getContentType());
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchFactory#getConstraintContext()
	 */
	@Override
	public ConstraintContext getConstraintContext() {
		return SearchManager.getInstance().getConstraintContext(getContentType());
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchFactory#createParameterEditor()
	 */
	@Override
	public Editor<Options> createParameterEditor() {
		return new DefaultParameterEditor();
	}

}