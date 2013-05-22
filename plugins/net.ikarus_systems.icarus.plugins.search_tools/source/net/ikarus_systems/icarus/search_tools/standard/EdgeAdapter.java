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
public class EdgeAdapter extends XmlAdapter<DefaultTreeEdge, SearchEdge> {

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public SearchEdge unmarshal(DefaultTreeEdge v) throws Exception {
		return v;
	}

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public DefaultTreeEdge marshal(SearchEdge v) throws Exception {
		return v instanceof DefaultTreeEdge ? (DefaultTreeEdge)v : new DefaultTreeEdge(v);
	}

}
