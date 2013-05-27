/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools;

import java.util.logging.Level;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.xml.jaxb.ExtensionAdapter;

import org.java.plugin.registry.Extension;

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

	public SearchFactory getSearchFactory() {
		if(searchFactory==null) {
			try {
				searchFactory = (SearchFactory) PluginUtil.instantiate(factoryExtension);
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
		}
		return query;
	}

	public SearchResult getSearchResult() {
		return searchResult;
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
	
	public SearchDescriptor clone() {
		SearchDescriptor clone = new SearchDescriptor();
		clone.factoryExtension = factoryExtension;
		clone.searchFactory = searchFactory;
		clone.queryString = queryString;
		
		return clone;
	}
}
