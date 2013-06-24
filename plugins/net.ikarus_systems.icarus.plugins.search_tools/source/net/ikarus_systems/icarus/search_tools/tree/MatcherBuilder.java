/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ikarus_systems.icarus.search_tools.EdgeType;
import net.ikarus_systems.icarus.search_tools.NodeType;
import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.SearchNode;
import net.ikarus_systems.icarus.util.Order;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MatcherBuilder {
	
	protected final Search search;
	
	protected Map<SearchNode, SearchNode> parentMap;
	protected Map<SearchNode, SearchEdge> headMap;
	protected Map<SearchNode, Matcher> matcherMap;
	protected Map<Matcher, Matcher> cloneMap;
	
	protected Matcher rootMatcher;
	protected Matcher lastMatcher;

	public MatcherBuilder(Search search) {
		if(search==null)
			throw new IllegalArgumentException("Invalid search"); //$NON-NLS-1$
		
		this.search = search;
	}
	
	public Matcher createRootMatcher() {
		SearchGraph graph = search.getQuery().getSearchGraph();
	}
	
	protected List<SearchNode> getExclusions(SearchNode node) {
		List<SearchNode> result = new ArrayList<>();
		int edgeCount = node.getOutgoingEdgeCount();
		for(int i=0; i<edgeCount; i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			if(edge.getEdgeType()==EdgeType.LINK || edge.getEdgeType()==EdgeType.PRECEDENCE) {
				continue;
			}
			SearchNode target = edge.getTarget();
			if(target.getNodeType()==NodeType.DISJUNCTION) {
				result
				continue;
			}
		}
		
		return result;
	}
	
	public Matcher cloneMatcher(Matcher matcher) {
		if(cloneMap==null) {
			cloneMap = new  HashMap<>();
		}
		
		Matcher clone = cloneMap.get(matcher);
		if(clone==null) {
			// Create a shallow clone
			clone = matcher.clone();
			// Immediately save reference to clone to prevent
			// duplicates when cloning inner members
			cloneMap.put(matcher, clone);
			
			// Now do the deep cloning with respect to already cloned members
			clone.setParent(cloneMatcher(matcher.getParent()));
			clone.setNext(cloneMatcher(matcher.getNext()));
			clone.setAlternate(cloneMatcher(matcher.getAlternate()));
			
			clone.setAfter(cloneMatchers(matcher.getAfter()));
			clone.setBefore(cloneMatchers(matcher.getBefore()));
		}
		return clone;
	}
	
	protected Matcher[] cloneMatchers(Matcher[] matchers) {
		if(matchers==null) {
			return null;
		}
		
		int size = matchers.length;
		Matcher[] clones = new Matcher[size];
		
		for(int i=0; i<size; i++) {
			clones[i] = cloneMatcher(matchers[i]);
		}
		
		return clones;
	}

	protected static class PrecedencePair {
		private final SearchNode source, target;
		
		public PrecedencePair(SearchEdge edge) {
			this(edge.getSource(), edge.getTarget());
		}
		
		public PrecedencePair(SearchNode source, SearchNode target) {
			this.source = source;
			this.target = target;
		}

		public SearchNode getSource() {
			return source;
		}

		public SearchNode getTarget() {
			return target;
		}
	}
}
