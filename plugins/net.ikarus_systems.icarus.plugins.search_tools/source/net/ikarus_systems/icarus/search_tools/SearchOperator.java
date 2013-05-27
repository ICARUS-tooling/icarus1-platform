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

import net.ikarus_systems.icarus.resources.ResourceManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum SearchOperator {

	EQUALS("=", "equals"), //$NON-NLS-1$ //$NON-NLS-2$
	
	EQUALS_NOT("!=", "equalsNot"), //$NON-NLS-1$ //$NON-NLS-2$
	
	MATCHES("=~", "mathes"), //$NON-NLS-1$ //$NON-NLS-2$
	
	MATCHES_NOT("!~", "matchesNot"), //$NON-NLS-1$ //$NON-NLS-2$
	
	CONTAINS("=#", "contains"), //$NON-NLS-1$ //$NON-NLS-2$
	
	CONTAINS_NOT("!#", "containsNot"), //$NON-NLS-1$ //$NON-NLS-2$
	
	LESS_THAN("<", "lessThan"), //$NON-NLS-1$ //$NON-NLS-2$
	
	LESS_OR_EQUAL("<=", "lessOrEqual"), //$NON-NLS-1$ //$NON-NLS-2$
	
	GREATER_THAN(">", "greaterThan"), //$NON-NLS-1$ //$NON-NLS-2$
	
	GREATER_OR_EQUAL(">=", "greaterOrEqual"), //$NON-NLS-1$ //$NON-NLS-2$
	
	GROUPING("<*>", "grouping"); //$NON-NLS-1$ //$NON-NLS-2$
	
	private String symbol;
	private String key;
	
	private SearchOperator(String symbol, String key) {
		this.symbol = symbol;
		this.key = key;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.operator."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.operator."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
		
	}
	
	public SearchOperator parseSymbol(String symbol) {
		if(symbol==null)
			throw new IllegalArgumentException("Invalid symbol"); //$NON-NLS-1$
		
		for(SearchOperator operator : values()) {
			if(operator.getSymbol().equals(symbol)) {
				return operator;
			}
		}
		
		throw new IllegalArgumentException("Unknown symbol: "+symbol); //$NON-NLS-1$
	}
}
