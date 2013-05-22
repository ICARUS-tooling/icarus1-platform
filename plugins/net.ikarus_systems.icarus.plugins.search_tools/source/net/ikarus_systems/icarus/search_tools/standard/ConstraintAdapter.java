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

import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchUtils;

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
		String id = v.getId();
		ConstraintFactory factory = SearchUtils.getConstraintFactory(id);
		// TODO should we really pass a constraint 'as-is' if we cannot find a factory for it?
		if(factory==null) {
			return v;
		}
		
		return factory.createConstraint(v.getValue(), v.getOperator());
	}

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public DefaultConstraint marshal(SearchConstraint v) throws Exception {
		return new DefaultConstraint(v.getId(), v.getValue(), v.getOperator());
	}

}
