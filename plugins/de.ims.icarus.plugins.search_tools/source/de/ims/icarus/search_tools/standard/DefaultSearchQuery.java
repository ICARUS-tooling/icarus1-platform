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
package de.ims.icarus.search_tools.standard;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.search_tools.ConstraintContext;
import de.ims.icarus.search_tools.SearchGraph;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultSearchQuery implements SearchQuery, Cloneable {

	protected SearchGraph graph;
	protected String query;

	protected Map<String, Object> properties;

	protected DefaultQueryParser parser;
	protected final ContentType contentType;

	public DefaultSearchQuery(ContentType contentType) {
		if(contentType==null)
			throw new NullPointerException("Invalid content-type"); //$NON-NLS-1$

		this.contentType = contentType;

		graph = new DefaultSearchGraph();
		query = ""; //$NON-NLS-1$
	}

	protected DefaultQueryParser createParser() throws Exception {
		ConstraintContext context = SearchManager.getInstance().getConstraintContext(getContentType());
		if(context==null)
			throw new IllegalStateException("Unable to fetch constraint-context for content-type: "+getContentType().getId()); //$NON-NLS-1$

		return new DefaultQueryParser(context, null);
	}

	public final ContentType getContentType() {
		return contentType;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#parseQueryString(java.lang.String)
	 */
	@Override
	public void parseQueryString(String query)
			throws UnsupportedFormatException {
		if(query==null)
			throw new NullPointerException("Invalid query"); //$NON-NLS-1$

		if(this.query!=null && this.query.equals(query)) {
			return;
		}

		this.query = query;

		try {
			queryToGraph();
		} catch (Exception e) {
			throw new UnsupportedFormatException(
					"Error while parsing query", e); //$NON-NLS-1$
		}
	}

	protected void graphToQuery() throws Exception {
		if(graph==null) {
			query = null;
			return;
		}

		if(parser==null) {
			parser = createParser();
		}

		query = parser.toQuery(graph, null);
	}

	protected void queryToGraph() throws Exception {
		if(query==null) {
			graph = null;
			return;
		}

		if(parser==null) {
			parser = createParser();
		}

		graph = parser.parseQuery(query, null);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#getQueryString()
	 */
	@Override
	public String getQueryString() {
		return query;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#setProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new HashMap<>();
		}

		properties.put(key, value);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#getSearchGraph()
	 */
	@Override
	public SearchGraph getSearchGraph() {
		return graph;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#setSearchGraph(de.ims.icarus.search_tools.SearchGraph)
	 */
	@Override
	public void setSearchGraph(SearchGraph graph) throws UnsupportedFormatException {
		if(graph==null)
			throw new NullPointerException("Invalid graph"); //$NON-NLS-1$

		if(this.graph!=null && this.graph.equals(graph)) {
			return;
		}

		this.graph = graph;

		try {
			graphToQuery();
		} catch (Exception e) {
			throw new UnsupportedFormatException(
					"Error while converting graph to query", e); //$NON-NLS-1$
		}
	}

	@Override
	public SearchQuery clone() {
		DefaultSearchQuery clone = new DefaultSearchQuery(getContentType());
		clone.graph = graph.clone();
		clone.query = query;
		clone.parser = parser;

		if(properties!=null) {
			clone.properties = new HashMap<>(properties);
		}

		return clone;
	}

}
