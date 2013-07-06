/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.standard;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.ims.icarus.search_tools.SearchNode;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class NodeAdapter extends XmlAdapter<DefaultGraphNode, SearchNode> {

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public SearchNode unmarshal(DefaultGraphNode v) throws Exception {
		return v;
	}

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public DefaultGraphNode marshal(SearchNode v) throws Exception {
		return v instanceof DefaultGraphNode ? (DefaultGraphNode)v : new DefaultGraphNode(v);
	}

}
