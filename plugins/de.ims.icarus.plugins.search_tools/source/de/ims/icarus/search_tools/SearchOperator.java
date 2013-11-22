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

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.id.DuplicateIdentifierException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class SearchOperator implements Serializable {
	
	private static final long serialVersionUID = 771727767289393418L;

	private String symbol;
	
	@SuppressWarnings("unused")
	private SearchOperator() {
		// no-op
	}
	
	protected SearchOperator(String symbol) {
		if(symbol==null)
			throw new NullPointerException("Invalid symbol"); //$NON-NLS-1$
		
		this.symbol = symbol;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public abstract boolean apply(Object value, Object constraint);
	
	public abstract String getName();

	public abstract String getDescription();
	
	private static Map<String, SearchOperator> available = new LinkedHashMap<>();
	
	public static void register(SearchOperator operator) {
		if(operator==null)
			throw new NullPointerException("Invalid operator"); //$NON-NLS-1$
		if(available.containsKey(operator.getSymbol()))
			throw new DuplicateIdentifierException("Duplicate operator symbol: "+operator.getSymbol()); //$NON-NLS-1$
		
		available.put(operator.getSymbol(), operator);
	}
	
	public static SearchOperator getOperator(String symbol) {
		return available.get(symbol);
	}
	
	public static Set<String> symbols() {
		return CollectionUtils.getSetProxy(available.keySet());
	}
	
	public static Collection<SearchOperator> operators() {
		return CollectionUtils.getCollectionProxy(available.values());
	}
	
	public static SearchOperator[] values() {
		SearchOperator[] result = new SearchOperator[available.size()];
		return available.values().toArray(result);
	}
}
