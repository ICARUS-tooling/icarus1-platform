/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools;

import java.util.logging.Level;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


import org.java.plugin.registry.Extension;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.xml.jaxb.ExtensionAdapter;
import de.ims.icarus.xml.jaxb.MapAdapter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class SearchDescriptor {

	@XmlTransient
	private Search search;

	@XmlTransient
	private SearchFactory searchFactory;

	@XmlTransient
	private SearchQuery query;

	@XmlTransient
	private SearchResult searchResult;
	
	@XmlTransient
	private Object target;
	
	@XmlJavaTypeAdapter(value=MapAdapter.class)
	private Options parameters;
	
	@XmlElement(name="factory")
	@XmlJavaTypeAdapter(value=ExtensionAdapter.class)
	private Extension factoryExtension;
	
	@XmlElement(name="query")
	private String queryString;
	
	public SearchDescriptor() {
		// no-op
	}
	
	public Search getSearch() {
		return search;
	}
	
	public boolean createSearch() throws Exception {
		if(search!=null)
			throw new IllegalStateException("Search already created"); //$NON-NLS-1$
		
		SearchFactory factory = getSearchFactory();
		
		if(factory==null)
			throw new IllegalStateException("No valid factory available to create new search"); //$NON-NLS-1$
			
		Options options = getParameters();
		if(options==null) {
			options = Options.emptyOptions;
		}
		search = factory.createSearch(getQuery(), getTarget(), options);
		
		if(!search.init()) {
			return false;
		}
		
		searchResult = search.getResult();
		
		return true;
	}

	public SearchFactory getSearchFactory() {
		if(searchFactory==null) {
			try {
				searchFactory = SearchManager.getInstance().getFactory(getFactoryExtension());
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to instantiate search-factory: "+String.valueOf(factoryExtension), e); //$NON-NLS-1$
			}
		}
		return searchFactory;
	}

	public SearchQuery getQuery() {
		if(query==null) {
			query = getSearchFactory().createQuery();
			if(queryString!=null) {
				try {
					query.parseQueryString(queryString);
				} catch (UnsupportedFormatException e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to create search query for string: "+queryString, e); //$NON-NLS-1$
				}
			}
		}
		return query;
	}

	public SearchResult getSearchResult() {
		if(searchResult==null && search!=null) {
			searchResult = search.getResult();
		}
		return searchResult;
	}
	
	public void setSearchResult(SearchResult searchResult) {
		if(searchResult==null)
			throw new IllegalArgumentException("Invalid search result"); //$NON-NLS-1$
		if(this.searchResult==searchResult) {
			return;
		}
		if(this.searchResult!=null)
			throw new IllegalArgumentException("Search result already set"); //$NON-NLS-1$
		
		this.searchResult = searchResult;
	}

	public Extension getFactoryExtension() {
		if(factoryExtension==null)
			throw new IllegalStateException("No search-factory extension set!"); //$NON-NLS-1$
		return factoryExtension;
	}

	public String getQueryString() {
		if(queryString==null) {
			queryString = getQuery().getQueryString();
		}
		
		return queryString;
	}

	public void setFactoryExtension(Extension factoryExtension) {
		if(factoryExtension==null)
			throw new IllegalArgumentException("Invalid factory extension"); //$NON-NLS-1$
		
		if(this.factoryExtension==factoryExtension) {
			return;
		}
		
		this.factoryExtension = factoryExtension;
		
		searchFactory = null;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}
	
	public Options getParameters() {
		return parameters;
	}

	public void setParameters(Options parameters) {
		this.parameters = parameters;
	}

	@Override
	public SearchDescriptor clone() {
		SearchDescriptor clone = cloneShallow();
		clone.search = search;
		clone.searchResult = searchResult;
		
		return clone;
	}

	/**
	 * Creates a new {@code SearchDescriptor} that holds the exact same
	 * values for the following fields:
	 * <ul>
	 * <li>factoryExtension</li>
	 * <li>searchFactory</li>
	 * <li>queryString</li>
	 * <li>target</li>
	 * </ul>
	 * 
	 * @see java.lang.Object#clone()
	 */
	public SearchDescriptor cloneShallow() {
		SearchDescriptor clone = new SearchDescriptor();
		clone.factoryExtension = factoryExtension;
		clone.searchFactory = searchFactory;
		clone.queryString = queryString;
		clone.query = query==null ? null : query.clone();
		clone.target = target;
		clone.parameters = parameters==null ? null : parameters.clone();
		
		return clone;
	}
	
	public boolean isActive() {
		return search!=null && search.isRunning();
	}
}
