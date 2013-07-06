/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.standard;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.ikarus_systems.icarus.search_tools.SearchOperator;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class OperatorAdapter extends XmlAdapter<String, SearchOperator> {

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public SearchOperator unmarshal(String v) throws Exception {
		return SearchOperator.getOperator(v);
	}

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public String marshal(SearchOperator v) throws Exception {
		return v.getSymbol();
	}

}
