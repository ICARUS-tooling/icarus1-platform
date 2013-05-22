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

/**
 * A rooted tree view on arbitrary underlying data. This interface
 * is merely a viewport on the data structure and serves as navigable
 * scope to point access on certain nodes or edges in the graph.
 * <p>
 * Subclasses are responsible for providing the methods to actually access
 * the content of nodes and edges since this interface only specifies the
 * methods for <i>traversing</i> the tree structure.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface TargetTree {

	/**
	 * Returns the number of nodes (vertices) in the tree
	 */
	int size();
	
	/**
	 * Clears any pointer structures
	 */
	void reset();
	
	/**
	 * Releases all internal data structures
	 */
	void close();
	
	/**
	 * Rebuild the graph representation based on the given data input.
	 */
	void reload(Object source);
	
	/**
	 * Returns the index of the node currently being viewed or
	 * {@code -1} if the current scope is not pointed on a node.
	 */
	int getNodeIndex();
	
	/**
	 * Returns the index of the edge currently being viewed or
	 * {@code -1} if the current scope is not pointed on an edge.
	 */
	int getEdgeIndex();
	
	/**
	 * Returns the number of outgoing edges on the currently viewed
	 * node.
	 * 
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node
	 */
	int getEdgeCount();
	
	/**
	 * Moves the scope to the edge at position {@code index} of the
	 * current node.
	 * <p>
	 * Precondition: {@code #getNodeIndex()}!=-1 && {@code #getEdgeIndex()}==-1
	 * Postcondition: {@code #getEdgeIndex()}==index && {@code #getNodeIndex()}==-1
	 * 
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node
	 * @throws IndexOutOfBoundsException in case the provided {@code index} is
	 * out of bounds: 0&le;index&lt;edgeCount
	 */
	void viewEdge(int index);
	
	/**
	 * Moves the scope to the edge at position {@code index} of the
	 * node specified by {@code nodeIndex}.
	 * <p>
	 * Postcondition: {@code #getEdgeIndex()}==edgeIndex && {@code #getNodeIndex()}==-1
	 * 
	 * @throws IndexOutOfBoundsException in case the provided {@code nodeIndex} is
	 * not in the range of 0 &le; nodeIndex &lt; {@code size()} or {@code edgeIndex} is
	 * out of bounds: 0 &le; index &lt; {@code getEdgeCount()} when moved to the
	 * new node.
	 */
	void viewEdge(int nodeindex, int edgeIndex);
	
	/**
	 * Returns the index of the source node of the currently viewed edge.
	 * 
	 * @throws IllegalStateException in case the scope is not pointed on
	 * an edge
	 */
	int getSourceIndex();
	
	/**
	 * Returns the index of the target node of the currently viewed edge.
	 * 
	 * @throws IllegalStateException in case the scope is not pointed on
	 * an edge
	 */
	int getTargetIndex();
	
	/**
	 * Returns the index of the one single root node in this graph.
	 */
	int getRootIndex();
	
	/**
	 * Returns the index of the current node's parent node or {@code -1}
	 * if the current node is the root node.
	 * 
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node
	 */
	int getParentIndex();
	
	/**
	 * Moves the scope to the node at position {@code index} in the graph.
	 * <p>
	 * Precondition: {@code #getNodeIndex()}!=-1 && {@code #getEdgeIndex()}==-1
	 * Postcondition: {@code #getEdgeIndex()}==index && {@code #getNodeIndex()}==-1
	 * 
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node
	 * @throws IndexOutOfBoundsException in case the provided {@code index} is
	 * out of bounds: 0&le;index&lt;edgeCount
	 */
	void viewNode(int index);
	
	/**
	 * Returns the height of the sub-tree whose root node is the node currently
	 * being viewed. For a leaf node this method must return {@code 1} and for any
	 * other node it is {@code 1} plus the maximum of any of its child nodes height.
	 * 
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node
	 */
	int getHeight();
	
	/**
	 * Returns the number of nodes in the sub-tree whose root the node currently 
	 * being viewed is. This count does not include the node itself and is {@code 0}
	 * for leaf nodes.
	 * 
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node
	 */
	int getDescendantCount();
	
	// Shorthand methods
	
	/**
	 * Moves the scope to the node thta is the parent of the current node.
	 * <p>
	 * Precondition: {@code #getNodeIndex()}!=-1 && {@code #getEdgeIndex()}==-1 && {@code i}=={@code #getParentIndex()}
	 * Postcondition: {@code #getEdgeIndex()}==-1 && {@code #getNodeIndex()}=={@code i}
	 * 
	 * @throws IllegalStateException in case the scope is not pointed on
	 * a node or the current node is the root node
	 */
	void viewParent();
	
	void viewTarget();
	
	void viewSource();
}
