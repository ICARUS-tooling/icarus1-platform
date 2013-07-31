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
package de.ims.icarus.plugins.errormining.ngram_search;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.search_tools.SearchGraph;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.standard.DefaultQueryParser;
import de.ims.icarus.search_tools.standard.DefaultSearchGraph;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramSearchQuery implements SearchQuery {
	
	protected SearchGraph graph;
	protected String query;
	
	protected Map<String, Object> properties;
	
	protected DefaultQueryParser parser;
	protected final ContentType contentType;
	
	public NGramSearchQuery(ContentType contentType) {
		if(contentType==null)
			throw new IllegalArgumentException("Invalid content-type"); //$NON-NLS-1$
		
		this.contentType = contentType;
		
		graph = new DefaultSearchGraph();
		query = ""; //$NON-NLS-1$
	}
	
	
	
	
	@Override
	public SearchQuery clone() {
		NGramSearchQuery clone = new NGramSearchQuery(getContentType());
//		clone.graph = graph.clone();
//		clone.query = query;
//		clone.parser = parser;
		
		if(properties!=null) {
			clone.properties = new HashMap<>(properties);
		}
		
		return clone;
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
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#getQueryString()
	 */
	@Override
	public String getQueryString() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#setProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(String key, Object value) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#getSearchGraph()
	 */
	@Override
	public SearchGraph getSearchGraph() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#setSearchGraph(de.ims.icarus.search_tools.SearchGraph)
	 */
	@Override
	public void setSearchGraph(SearchGraph graph)
			throws UnsupportedFormatException {
		// TODO Auto-generated method stub

	}

}
