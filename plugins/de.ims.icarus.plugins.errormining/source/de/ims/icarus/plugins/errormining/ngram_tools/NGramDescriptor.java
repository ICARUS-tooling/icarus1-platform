/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining.ngram_tools;

import java.util.logging.Level;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.xml.jaxb.ExtensionAdapter;

import org.java.plugin.registry.Extension;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class NGramDescriptor {
	
	//TODO Plase Correct Editor Items
	
	@XmlTransient
	private NGram ngram;

	@XmlTransient
	private NGramFactory ngramFactory;

	@XmlTransient
	private NGramQuery query;

	@XmlTransient
	private NGramResult ngramResult;
	
	@XmlTransient
	private Object target;
	
	@XmlElement(name="factory")
	@XmlJavaTypeAdapter(value=ExtensionAdapter.class)
	private Extension factoryExtension;
	
	@XmlElement(name="query")
	private String queryString;
	
	public NGramDescriptor() {
		// no-op
	}
	
	public NGram getNGram() {
		return ngram;
	}

	public NGramFactory getNGramFactory() {
		if(ngramFactory==null) {
			try {
				ngramFactory = NGramManager.getInstance().getFactory(getFactoryExtension());
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to instantiate ngram-factory: "+String.valueOf(factoryExtension), e); //$NON-NLS-1$
			}
		}
		return ngramFactory;
	}

	public NGramQuery getQuery() {
		if(query==null) {
			query = getNGramFactory().createQuery();
		}
		return query;
	}

	public NGramResult getNGramResult() {
		return ngramResult;
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
		
		ngramFactory = null;
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
	
	

}
