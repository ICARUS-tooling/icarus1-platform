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

import net.ikarus_systems.icarus.search_tools.SearchConstraint;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConstraintAdapter extends XmlAdapter<DefaultConstraint, SearchConstraint> {

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public SearchConstraint unmarshal(DefaultConstraint v) throws Exception {
		return v;
	}

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public DefaultConstraint marshal(SearchConstraint v) throws Exception {
		return v instanceof DefaultConstraint ? (DefaultConstraint)v :  new DefaultConstraint(v.getToken(), v.getValue(), v.getOperator());
	}

}
