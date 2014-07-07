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
package de.ims.icarus.search_tools;

import de.ims.icarus.plugins.search_tools.view.editor.DefaultQueryEditor;
import de.ims.icarus.plugins.search_tools.view.editor.QueryEditor;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchFactory {

	Search createSearch(String query, String target, Options options) throws Exception;

	String getSerializedForm();

	String getSerializedTarget(Search search);

	/**
	 * Constructs a new {@code Search} object that can be scheduled
	 * to run the <i>search-constraints</i> encoded within the
	 * {@code query} argument. Implementations are required to support
	 * all the parameters defined in {@code SearchParameters} when passed
	 * as part of the {@code options} argument.
	 *
	 * @throws UnsupportedFormatException in case the {@code query} contains
	 * illegal {@code SearchConstraint} instances or is of an unsupported
	 * structure (e.g. a factory for tree-structures would reject a full
	 * grown graph object)
	 */
	Search createSearch(SearchQuery query, Object target, Options options) throws UnsupportedFormatException;

	Search createExampleSearch() throws Exception;

	ConstraintContext getConstraintContext();

	/**
	 * Creates an empty query usable for setting up
	 * a search.
	 */
	SearchQuery createQuery();

	Editor<Options> createParameterEditor();

	/**
	 * Returns the class of the {@link QueryEditor} to be used when editing
	 * queries for this factory. If a  {@code null} value is returned the
	 * framework assumes the {@link DefaultQueryEditor} implementation to be
	 * sufficient.
	 */
	Class<? extends QueryEditor> getDefaultEditorClass();

	/**
	 * Generates a context specific label for the given {@code SearchQuery}.
	 * A return value of {@code null} indicates that the framework should
	 * generate a default label.
	 */
	String getQueryLabel(SearchQuery query);
}
