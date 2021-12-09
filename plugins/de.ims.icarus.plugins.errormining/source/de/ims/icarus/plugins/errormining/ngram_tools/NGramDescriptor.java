/* 
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus G�rtner and Gregor Thiele
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
package de.ims.icarus.plugins.errormining.ngram_tools;

import java.util.logging.Level;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.java.plugin.registry.Extension;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.xml.jaxb.ExtensionAdapter;

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
			throw new NullPointerException("Invalid factory extension"); //$NON-NLS-1$
		
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
