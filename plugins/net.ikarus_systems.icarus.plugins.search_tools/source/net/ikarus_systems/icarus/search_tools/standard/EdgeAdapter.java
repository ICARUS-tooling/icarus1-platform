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

import net.ikarus_systems.icarus.search_tools.SearchEdge;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EdgeAdapter extends XmlAdapter<DefaultGraphEdge, SearchEdge> {

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public SearchEdge unmarshal(DefaultGraphEdge v) throws Exception {
		return v;
	}

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public DefaultGraphEdge marshal(SearchEdge v) throws Exception {
		return v instanceof DefaultGraphEdge ? (DefaultGraphEdge)v : new DefaultGraphEdge(v);
	}

}
