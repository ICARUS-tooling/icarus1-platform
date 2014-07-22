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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.util.classes.ClassProxy;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.id.DuplicateIdentifierException;


/**
 * Holds all available tokens, aliases and {@code ConstraintFactory}
 * object associated with a certain {@code ContentType}. Note that
 * all methods that modify the content of a context are <i>non-destructive</i>
 * i.e. only {@code add} new factories, tokens or aliases but never
 * {@code remove} them!
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConstraintContext {
	
	private final ContentType contentType;
	
	private Set<String> tokens = new LinkedHashSet<>();
	private Map<String, String> aliases = new HashMap<>();
	private Map<String, Object> factories = new HashMap<>();
	
	private List<ConstraintFactory> nodeFactoryCache;
	private List<ConstraintFactory> edgeFactoryCache;
	
	public ContentType getContentType() {
		return contentType;
	}

	public ConstraintContext(ContentType contentType) {
		if(contentType==null)
			throw new NullPointerException("Invalid content type"); //$NON-NLS-1$
		
		this.contentType = contentType;
	}
	
	public void registerFactory(String token, Object factory) {
		if(token==null || token.isEmpty())
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$
		
		token = token.toLowerCase();
		
		if(factories.containsKey(token))
			throw new DuplicateIdentifierException("Duplicate factory for token: "+token+" in context "+contentType.getId()); //$NON-NLS-1$ //$NON-NLS-2$

		if(factory instanceof ConstraintFactory
				|| factory instanceof String
				|| factory instanceof Class
				|| factory instanceof ClassProxy) {
			factories.put(token, factory);
		} else
			throw new NullPointerException("Invalid factory: "+factory.getClass()+" for token '"+token+"' in context "+contentType.getId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		nodeFactoryCache = null;
		edgeFactoryCache = null;
	}
	
	public void addToken(String token) {
		if(token==null || token.isEmpty())
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$
		
		token = token.toLowerCase();
		
		if(tokens.contains(token))
			throw new IllegalArgumentException("Token '"+token+"' already registered to context "+contentType.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		
		tokens.add(token);
	}
	
	public void addAlias(String alias, String token) {
		if(alias==null || alias.isEmpty())
			throw new NullPointerException("Invalid alias"); //$NON-NLS-1$
		if(token==null || token.isEmpty())
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$
		
		alias = alias.toLowerCase();
		
		if(aliases.containsKey(alias))
			throw new IllegalArgumentException("Alias '"+alias+"' already registered to context "+contentType.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		
		aliases.put(alias, token);
	}
	
	public boolean isRegistered(String token) {
		if(token==null || token.isEmpty())
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$
		
		token = token.toLowerCase();
		
		return factories.containsKey(token);
	}
	
	public ConstraintFactory getFactory(String token) {
		if(token==null || token.isEmpty())
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$

		token = token.toLowerCase();
		
		Object factory = factories.get(token);
		if(factory!=null && !(factory instanceof ConstraintFactory)) {
			try {
				if(factory instanceof String) {
					factory = Class.forName((String)factory);
				}
				if(factory instanceof Class) {
					factory = ((Class<?>)factory).newInstance();
				} else if(factory instanceof ClassProxy) {
					factory = ((ClassProxy)factory).loadObjectUnsafe();
				}
				
				// Refresh mapping
				factories.put(token, factory);
			} catch(Exception e) {
				LoggerFactory.log(SearchUtils.class, Level.SEVERE, 
						"Failed to instantiate constraint factory for token: "+token+" in context "+contentType.getId(), e); //$NON-NLS-1$ //$NON-NLS-2$
				
				factories.remove(token);				
				factory = null;
			}
		}
		
		return (ConstraintFactory) factory;
	}
	
	public Set<String> getTokens() {
		return Collections.unmodifiableSet(tokens);
	}
	
	public Set<String> getLegalTokens() {
		Set<String> result = new LinkedHashSet<>(tokens);
		result.addAll(aliases.keySet());
		
		return result;
	}
	
	public Set<String> getAliases() {
		return Collections.unmodifiableSet(aliases.keySet());
	}
	
	public String getToken(String alias) {
		if(alias==null || alias.isEmpty())
			throw new NullPointerException("Invalid alias"); //$NON-NLS-1$
		
		alias = alias.toLowerCase();
		
		return aliases.get(alias);
	}
	
	/**
	 * Finds a token that either directly is a completion of the
	 * given {@code fragment} string or has an alias that could 
	 * serve as a completion. A string is considered to be a completion
	 * of the input {@code fragment} if its {@link String#startsWith(String)}
	 * method returns {@code true} with the {@code fragment} as argument.
	 * <p>
	 * This search is case-insensitive. If no token could be found
	 * {@code null} will be returned.
	 */
	public String completeToken(String fragment) {
		if(fragment==null || fragment.isEmpty())
			throw new NullPointerException("Invalid fragment"); //$NON-NLS-1$
		
		fragment = fragment.toLowerCase();
		
		// Try main tokens first
		for(String token : tokens) {
			if(token.startsWith(fragment)) {
				return token;
			}
		}
		
		// Now try aliases
		for(Entry<String, String> entry : aliases.entrySet()) {
			if(entry.getKey().startsWith(fragment)) {
				return entry.getValue();
			}
		}
		
		return null;
	}
	
	public List<ConstraintFactory> getFactories() {
		List<ConstraintFactory> result = new ArrayList<>(tokens.size());
		
		for(String token : tokens) {
			result.add(getFactory(token));
		}
		
		return result;
	}
	
	public List<ConstraintFactory> getNodeFactories() {
		if(nodeFactoryCache==null) {
			nodeFactoryCache = new ArrayList<>();
			
			for(String token : tokens) {
				ConstraintFactory factory = getFactory(token);
				if(factory!=null && factory.getConstraintType()==ConstraintFactory.NODE_CONSTRAINT_TYPE) {
					nodeFactoryCache.add(factory);
				}
			}
			
			nodeFactoryCache = Collections.unmodifiableList(nodeFactoryCache);
		}
		
		return nodeFactoryCache;
	}
	
	public List<ConstraintFactory> getEdgeFactories() {
		if(edgeFactoryCache==null) {
			edgeFactoryCache = new ArrayList<>();
			
			for(String token : tokens) {
				ConstraintFactory factory = getFactory(token);
				if(factory!=null && factory.getConstraintType()==ConstraintFactory.EDGE_CONSTRAINT_TYPE) {
					edgeFactoryCache.add(factory);
				}
			}
			
			edgeFactoryCache = Collections.unmodifiableList(edgeFactoryCache);
		}
		
		return edgeFactoryCache;
	}
}