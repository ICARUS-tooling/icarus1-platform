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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.search_tools.EdgeType;
import de.ims.icarus.search_tools.NodeType;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.SearchGraph;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchNode;
import de.ims.icarus.util.Options;


/**
 * Possible violations (report is optional):
 * <ul>
 * <li>Edge count violation (min/max of incoming/outgoing or total)</li>
 * <li>Totally undefined graph (mere structural search)</li>
 * <li>Complementary graphs with root-operation 'disjunction'</li>
 * <li>Multiple roots</li>
 * <li>Negated transitive edges</li>
 * <li>Cyclic sub-graphs</li>
 * <li>Link-edge containing constraints</li>
 * <li>Existence of link-edges.</li>
 * <li></li>
 * <li></li>
 * <li></li>
 * </ul>
 * <p>
 * 
 * Possible violations (always reported):
 * <ul>
 * <li>Root-node with incoming edges</li>
 * <li>Leaf-node with outgoing edges</li>
 * <li>{@code null}-entry in a root-list (graph-level)</li>
 * <li>{@code null}-entry in a node-list (graph-level)</li>
 * <li>{@code null}-entry in a edge-list (graph-level)</li>
 * <li>{@code null}-entry in a constraint-list (node/edge-level)</li>
 * <li>Node with incoming edges that is not registered as a root-node</li>
 * <li></li>
 * <li></li>
 * <li></li>
 * </ul>
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class GraphValidator {
	
	// Constraints that cause error reports
	public static final String MAX_INCOMING_EDGES = "maxIncomingEdges"; //$NON-NLS-1$
	public static final String MIN_INCOMING_EDGES = "minIncomingEdges"; //$NON-NLS-1$
	public static final String MAX_OUTGOING_EDGES = "maxOutgoingEdges"; //$NON-NLS-1$
	public static final String MIN_OUTGOING_EDGES = "minOutgoingEdges"; //$NON-NLS-1$
	public static final String MAX_TOTAL_EDGES = "maxTotalEdges"; //$NON-NLS-1$
	
	// Constraints that cause warning reports
	public static final String ALLOW_UNDEFINED_GRAPH = "allowUndefinedGraph";  //$NON-NLS-1$
	public static final String ALLOW_COMPLEMENTARY_DISJUNCTION = "allowComplementaryDisjunction";  //$NON-NLS-1$
	public static final String ALLOW_MULTIPLE_ROOTS = "allowMultipleRoots";  //$NON-NLS-1$
	public static final String ALLOW_NEGATED_TRANSITIVES = "allowNegatedTransitives";  //$NON-NLS-1$
	public static final String ALLOW_CYCLES = "allowCycles";  //$NON-NLS-1$
	public static final String ALLOW_LINK_CONSTRAINTS = "allowLinkConstraints";  //$NON-NLS-1$
	public static final String ALLOW_NEGATED_LINKS = "allowNegatedLinks";  //$NON-NLS-1$
	public static final String ALLOW_NEGATED_DISJUNCTIONS = "allowNegatedDisjunctions";  //$NON-NLS-1$
	public static final String ALLOW_LINKS = "allowLinks";  //$NON-NLS-1$
	public static final String ALLOW_TRANSITIVES = "allowTransitives";  //$NON-NLS-1$
	public static final String ALLOW_DOUBLE_NEGATIVE = "allowDoubleNegation";  //$NON-NLS-1$

	public GraphValidator() {
		// no-op
	}

	public GraphValidationResult validateTree(SearchGraph graph, Options options) {
		if(graph==null)
			throw new IllegalArgumentException("Invalid graph"); //$NON-NLS-1$
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		options.put(MAX_INCOMING_EDGES, 1);
		
		return validateGraph(graph, options);
	}
	
	public GraphValidationResult validateGraph(SearchGraph graph, Options options) {
		return validateGraph(graph, options, null);
	}

	public GraphValidationResult validateGraph(SearchGraph graph, Options options, GraphValidationResult result) {
		if(graph==null)
			throw new IllegalArgumentException("Invalid graph"); //$NON-NLS-1$
		if(graph.getNodes()==null)
			throw new IllegalArgumentException("Invalid nodes in graph"); //$NON-NLS-1$
		if(graph.getEdges()==null)
			throw new IllegalArgumentException("Invalid edges in graph"); //$NON-NLS-1$
		if(graph.getRootNodes()==null)
			throw new IllegalArgumentException("Invalid roots in graph"); //$NON-NLS-1$
		
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		SearchNode[] nodes = graph.getNodes();
		SearchEdge[] edges = graph.getEdges();
		Set<SearchNode> roots = new HashSet<>();
		Set<Object> undefinedCells = new HashSet<>();
		Map<Object, Integer> groupCounts = new HashMap<>();
		
		boolean finalize = result==null;
		
		if(result==null) {
			result = new GraphValidationResult();
		}
		
		int minIncEdgeCount = options.get(MIN_INCOMING_EDGES, 0);
		int maxIncEdgeCount = options.get(MAX_INCOMING_EDGES, 0);
		int minOutEdgeCount = options.get(MIN_OUTGOING_EDGES, 0);
		int maxOutEdgeCount = options.get(MAX_OUTGOING_EDGES, 0);
		int maxTotalEdgeCount = options.get(MAX_TOTAL_EDGES, 0);
		
		// Validate roots
		for(int rootIdx = 0; rootIdx<graph.getRootNodes().length; rootIdx++) {
			SearchNode root = graph.getRootNodes()[rootIdx];
			if(root==null) {
				result.addError("plugins.searchTools.graphValidation.missingRoot", rootIdx); //$NON-NLS-1$
				continue;
			}
			if(root.getNodeType()==NodeType.DISJUNCTION) {
				result.addError("plugins.searchTools.graphValidation.illegalRootType",  //$NON-NLS-1$
						root.getId(), root.getNodeType().getName());
			}
			roots.add(root);
		}
		
		// Validate nodes
		for(int nodeIdx = 0; nodeIdx<nodes.length; nodeIdx++) {
			SearchNode node = nodes[nodeIdx];
			if(node==null) {
				result.addError("plugins.searchTools.graphValidation.missingNode", nodeIdx); //$NON-NLS-1$
				continue;
			}
			
			String id = node.getId();
			
			// Make sure all roots are reported properly
			if(node.getIncomingEdgeCount()==0 && !roots.contains(node)) {
				result.addError("plugins.searchTools.graphValidation.unregisteredRoot", id); //$NON-NLS-1$
			}
			
			// Check defined edge count constraints
			int incEdgeCount = incomingStrongEdgeCount(node);
			int outEdgeCount = outgoingStrongEdgeCount(node);
			// min incoming
			if(minIncEdgeCount!=0 && incEdgeCount<minIncEdgeCount) {
				result.addError("plugins.searchTools.graphValidation.unsatisfiedMinIncoming",  //$NON-NLS-1$
						minIncEdgeCount, id, incEdgeCount);
			}
			// min outgoing
			if(minOutEdgeCount!=0 && outEdgeCount<minOutEdgeCount) {
				result.addError("plugins.searchTools.graphValidation.unsatisfiedMinOutgoing",  //$NON-NLS-1$
						minOutEdgeCount, id, outEdgeCount);
			}
			// max incoming
			if(maxIncEdgeCount!=0 && incEdgeCount>maxIncEdgeCount) {
				result.addError("plugins.searchTools.graphValidation.unsatisfiedMaxIncoming",  //$NON-NLS-1$
						maxIncEdgeCount, id, incEdgeCount);
			}
			// max outgoing
			if(maxOutEdgeCount!=0 && outEdgeCount>maxOutEdgeCount) {
				result.addError("plugins.searchTools.graphValidation.unsatisfiedMaxOutgoing",  //$NON-NLS-1$
						maxOutEdgeCount, id, outEdgeCount);
			}
			// total count
			if(maxTotalEdgeCount!=0 && (incEdgeCount+outEdgeCount)>maxTotalEdgeCount) {
				result.addError("plugins.searchTools.graphValidation.unsatisfiedMaxTotalEdges",  //$NON-NLS-1$
						maxTotalEdgeCount, id, incEdgeCount+outEdgeCount);
			}
			
			// Validate constraints
			int groupCount = 0;
			boolean undefined = true;
			SearchConstraint[] constraints = node.getConstraints();
			if(constraints!=null) {
				for(int i=0; i<constraints.length; i++) {
					SearchConstraint constraint = constraints[i];
					if(constraint==null) {
						result.addError("plugins.searchTools.graphValidation.missingNodeConstraint", i, id); //$NON-NLS-1$
						continue;
					}
					if(!constraint.isUndefined()) {
						undefined = false;
					}
					if(SearchManager.isGroupingOperator(constraint.getOperator())) {
						groupCount++;
					}
				}
			}
			// Mark undefined node
			if(undefined && node.getNodeType()==NodeType.GENERAL) {
				undefinedCells.add(node);
			}
			// Save group count of node
			if(groupCount>0) {
				groupCounts.put(node, groupCount);
			}
			
			// Report leaf with outgoing edges
			if(node.getNodeType()==NodeType.LEAF && node.getOutgoingEdgeCount()>0) {
				result.addError("plugins.searchTools.graphValidation.invalidLeaf", id); //$NON-NLS-1$
			}

			// Report root with incoming edges
			if(node.getNodeType()==NodeType.ROOT && node.getIncomingEdgeCount()>0) {
				result.addError("plugins.searchTools.graphValidation.invalidRoot", id); //$NON-NLS-1$
			}
			
			// Check
			if(node.getNodeType()==NodeType.DISJUNCTION && node.isNegated()
					&& !options.get(ALLOW_NEGATED_DISJUNCTIONS, false)) {
				result.addWarning("plugins.searchTools.graphValidation.negatedDisjunctionNode", id); //$NON-NLS-1$
			}
		}

		// Validate edges
		for(int edgeIdx = 0; edgeIdx<edges.length; edgeIdx++) {
			SearchEdge edge = edges[edgeIdx];
			if(edge==null) {
				result.addError("plugins.searchTools.graphValidation.missingEdge", edgeIdx); //$NON-NLS-1$
				continue;
			}
			
			String id = edge.getId();
			
			// Validate source and target
			if(edge.getSource()==null) {
				result.addError("plugins.searchTools.graphValidation.missingSource", id); //$NON-NLS-1$
			}
			if(edge.getTarget()==null) {
				result.addError("plugins.searchTools.graphValidation.missingTarget", id); //$NON-NLS-1$
			}
			if(edge.getSource()!=null && edge.getSource()==edge.getTarget()) {
				result.addError("plugins.searchTools.graphValidation.loopEdge", id); //$NON-NLS-1$
			}
			
			// Validate constraints
			int groupCount = 0;
			boolean undefined = true;
			SearchConstraint[] constraints = edge.getConstraints();
			if(constraints!=null) {
				for(int i=0; i<constraints.length; i++) {
					SearchConstraint constraint = constraints[i];
					if(constraint==null) {
						result.addError("plugins.searchTools.graphValidation.missingEdgeConstraint", i, id); //$NON-NLS-1$
						continue;
					}
					if(!constraint.isUndefined()) {
						undefined = false;
					}
					if(SearchManager.isGroupingOperator(constraint.getOperator())) {
						groupCount++;
					}
				}
			}
			// Mark undefined edge
			if(undefined && edge.getEdgeType()==EdgeType.DOMINANCE) {
				undefinedCells.add(edge);
			}
			// Save group count of edge
			if(groupCount>0) {
				groupCounts.put(edge, groupCount);
			}
			
			// Check for illegal types and/or negations
			// Existence of link edges
			if(edge.getEdgeType()==EdgeType.LINK && !options.get(ALLOW_LINKS, true)) {
				result.addWarning("plugins.searchTools.graphValidation.illegalLinkEdge", id); //$NON-NLS-1$
			}
			// Existence of transitive edges
			if(edge.getEdgeType()==EdgeType.TRANSITIVE && !options.get(ALLOW_TRANSITIVES, true)) {
				result.addWarning("plugins.searchTools.graphValidation.illegalTransitiveEdge", id); //$NON-NLS-1$
			}
			// Negated link edges
			if(edge.getEdgeType()==EdgeType.LINK && edge.isNegated() && !options.get(ALLOW_NEGATED_LINKS, true)) {
				result.addWarning("plugins.searchTools.graphValidation.negatedLinkEdge", id); //$NON-NLS-1$
			}
			// Negated transitive edges
			if(edge.getEdgeType()==EdgeType.TRANSITIVE && edge.isNegated() && !options.get(ALLOW_NEGATED_TRANSITIVES, true)) {
				result.addWarning("plugins.searchTools.graphValidation.negatedTransitiveEdge", id); //$NON-NLS-1$
			}
			
			// Double negative
			if(edge.isNegated() && edge.getTarget().isNegated() && !options.get(ALLOW_DOUBLE_NEGATIVE, false)) {
				result.addWarning("plugins.searchTools.graphValidation.doubleNegative", id); //$NON-NLS-1$
			}
			
			// Disjunction-target with other edge than link or general
			if(edge.getTarget().getNodeType()==NodeType.DISJUNCTION && edge.getEdgeType()==EdgeType.TRANSITIVE) {
				result.addError("plugins.searchTools.graphValidation.illegalTransitiveTarget", id, edge.getTarget().getNodeType()); //$NON-NLS-1$
			}
			if(edge.getTarget().getNodeType()==NodeType.DISJUNCTION && edge.getEdgeType()==EdgeType.PRECEDENCE) {
				result.addError("plugins.searchTools.graphValidation.illegalPrecedenceTarget", id, edge.getTarget().getNodeType()); //$NON-NLS-1$
			}
			
			// Report nested disjunction
			if(edge.getSource().getNodeType()==NodeType.DISJUNCTION 
					&& edge.getTarget().getNodeType()==NodeType.DISJUNCTION) {
				result.addError("plugins.searchTools.graphValidation.nestedDisjunction", id); //$NON-NLS-1$
			}
		}
		
		// Check for undefined graphs
		for(SearchNode root : roots) {
			String id = root.getId();
			if(isUndefinedGraph(root, undefinedCells)) {
				result.addWarning("plugins.searchTools.graphValidation.undefinedGraph", id); //$NON-NLS-1$
			}
		}
		
		// Check for cycles
		if(!options.get(ALLOW_CYCLES, false)) {
			Set<Object> visited = new HashSet<>();
			for(SearchNode root : roots) {
				cycleCheck(result, root, visited, null, null);
			}
		}
		
		if(!options.get(ALLOW_COMPLEMENTARY_DISJUNCTION, false) 
				&& graph.getRootOperator()==SearchGraph.OPERATOR_DISJUNCTION) {
			// TODO perform complementary check
		}
		
		if(finalize) {
			result.lock();
		}
		
		return result;
	}
	
	private int incomingStrongEdgeCount(SearchNode node) {
		int count = 0;
		int edgeCount = node.getIncomingEdgeCount();
		for(int i=0; i<edgeCount; i++) {
			SearchEdge edge = node.getIncomingEdgeAt(i);
			if(edge==null)
				throw new IllegalArgumentException("Invalid incoming edge at index "+i+" in node "+node.getId()); //$NON-NLS-1$ //$NON-NLS-2$
			if(edge.getEdgeType()==EdgeType.DOMINANCE || edge.getEdgeType()==EdgeType.TRANSITIVE) {
				count++;
			}
		}
		return count;
	}
	
	private int outgoingStrongEdgeCount(SearchNode node) {
		int count = 0;
		int edgeCount = node.getOutgoingEdgeCount();
		for(int i=0; i<edgeCount; i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			if(edge==null)
				throw new IllegalArgumentException("Invalid outgoing edge at index "+i+" in node "+node.getId()); //$NON-NLS-1$ //$NON-NLS-2$
			if(edge.getEdgeType()==EdgeType.DOMINANCE || edge.getEdgeType()==EdgeType.TRANSITIVE) {
				count++;
			}
		}
		return count;
	}
	
	private boolean isUndefinedGraph(SearchNode node, Set<Object> undefinedCells) {
		if(node.isNegated() || node.getNodeType()!=NodeType.GENERAL) {
			return false;
		}
		if(!undefinedCells.contains(node)) {
			return false;
		}
		
		int edgeCount = node.getOutgoingEdgeCount();
		for(int i=0; i<edgeCount; i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			if(edge.isNegated() || edge.getEdgeType()!=EdgeType.DOMINANCE) {
				return false;
			}
			if(!undefinedCells.contains(edge)) {
				return false;
			}
			
			if(!isUndefinedGraph(edge.getTarget(), undefinedCells)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Finds cycles 
	 */
	private void cycleCheck(GraphValidationResult result, SearchNode node, 
			Set<Object> visited, Set<Object> cycle, EdgeType edgeType) {
		if(visited.contains(node)) {
			return;
		}
		
		visited.add(node);
		
		if(cycle!=null && cycle.contains(node)) {
			result.addError("plugins.searchTools.graphValidation.cyclicGraph", node.getId()); //$NON-NLS-1$
			return;
		}
		
		int edgeCount = node.getOutgoingEdgeCount();
		for(int i=0; i<edgeCount; i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			Set<Object> currentCycle = cycle;
			if(currentCycle==null || edgeType==null || edge.getEdgeType()!=edgeType) {
				currentCycle = new HashSet<>();
			}
			
			currentCycle.add(node);
			
			cycleCheck(result, edge.getTarget(), visited, currentCycle, edge.getEdgeType());
		}
	}
}
