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

import net.ikarus_systems.icarus.search_tools.SearchNode;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class NodeAdapter extends XmlAdapter<DefaultTreeNode, SearchNode> {

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public SearchNode unmarshal(DefaultTreeNode v) throws Exception {
		return v;
	}

	/**
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public DefaultTreeNode marshal(SearchNode v) throws Exception {
		return v instanceof DefaultTreeNode ? (DefaultTreeNode)v : new DefaultTreeNode(v);
	}

}
