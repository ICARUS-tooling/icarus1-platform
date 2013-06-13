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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.ikarus_systems.icarus.search_tools.standard.DefaultSearchOperator;
import net.ikarus_systems.icarus.util.id.DuplicateIdentifierException;

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
			throw new IllegalArgumentException("Invalid symbol"); //$NON-NLS-1$
		
		this.symbol = symbol;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public abstract boolean apply(Object value, Object constraint);
	
	public abstract String getName();

	public abstract String getDescription();
	
	public static final SearchOperator GROUPING = DefaultSearchOperator.GROUPING;
	
	private static Map<String, SearchOperator> available = new LinkedHashMap<>();
	private static Set<String> symbols = Collections.unmodifiableSet(available.keySet());
	private static Collection<SearchOperator> operators = Collections.unmodifiableCollection(available.values());
	
	public static void register(SearchOperator operator) {
		if(operator==null)
			throw new IllegalArgumentException("Invalid operator"); //$NON-NLS-1$
		if(available.containsKey(operator.getSymbol()))
			throw new DuplicateIdentifierException("Duplicate operator symbol: "+operator.getSymbol()); //$NON-NLS-1$
		
		available.put(operator.getSymbol(), operator);
	}
	
	public static SearchOperator getOperator(String symbol) {
		return available.get(symbol);
	}
	
	public static Set<String> symbols() {
		return symbols;
	}
	
	public static Collection<SearchOperator> operators() {
		return operators;
	}
	
	public static SearchOperator[] values() {
		return operators.toArray(new SearchOperator[0]);
	}
}
