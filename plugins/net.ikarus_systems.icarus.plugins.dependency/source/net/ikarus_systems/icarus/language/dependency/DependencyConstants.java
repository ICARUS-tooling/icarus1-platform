/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.dependency;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DependencyConstants {
	
	public static final String GRAMMAR_ID = "dependency"; //$NON-NLS-1$

	public static final int DATA_FIELD_ALL = (1 << 6);

	// mask fields for data events
	public static final int DATA_FIELD_FORM = (1 << 1);
	public static final int DATA_FIELD_LEMMA = (1 << 2);
	public static final int DATA_FIELD_FEATURES = (1 << 3);
	public static final int DATA_FIELD_POS = (1 << 4);
	public static final int DATA_FIELD_HEAD = (1 << 5);
	public static final int DATA_FIELD_RELATION = (1 << 6);
	public static final int DATA_FIELD_INDEX = (1 << 7);
	public static final int DATA_FIELD_FLAG = (1 << 10);

	/**
	 * Head value to mark the root node.
	 */
	public static final int DATA_HEAD_ROOT = -1;

	public static final String DATA_ROOT_LABEL = "<root>"; //$NON-NLS-1$

	public static final String DATA_UNDEFINED_LABEL = "?"; //$NON-NLS-1$

	public static final String DATA_CASEDIFF_LABEL = "<*>"; //$NON-NLS-1$

	public static final String DATA_LEFT_LABEL = "<<"; //$NON-NLS-1$

	public static final String DATA_RIGHT_LABEL = ">>"; //$NON-NLS-1$

	public static final int DATA_LEFT_VALUE = -1;

	public static final int DATA_RIGHT_VALUE = 1;

	public static final int DATA_CASEDIFF_VALUE = -3;

	public static final int DATA_UNDEFINED_VALUE = -2;

	public static final int DATA_YES_VALUE = 0;

	public static final int DATA_NO_VALUE = -1;

	/**
	 * Level value to signal that no validation has been performed yet.
	 */
	public static final int DATA_LEVEL_UNDEFINED = -1;

	public static final int TABLE_INDEX_INDEX = 0;
	public static final int TABLE_INDEX_FORM = 1;
	public static final int TABLE_INDEX_LEMMA = 2;
	public static final int TABLE_INDEX_FEATURES = 3;
	public static final int TABLE_INDEX_POS = 4;
	public static final int TABLE_INDEX_HEAD = 5;
	public static final int TABLE_INDEX_REL = 6;

	// constraint key labels
	public static final String CONSTRAINT_KEY_POS = "pos"; //$NON-NLS-1$
	public static final String CONSTRAINT_KEY_FORM = "form"; //$NON-NLS-1$
	public static final String CONSTRAINT_KEY_LEMMA = "lemma"; //$NON-NLS-1$
	public static final String CONSTRAINT_KEY_FEATURES = "feats"; //$NON-NLS-1$
	public static final String CONSTRAINT_KEY_RELATION = "rel"; //$NON-NLS-1$
	public static final String CONSTRAINT_KEY_DIR = "dir"; //$NON-NLS-1$
	public static final String CONSTRAINT_KEY_DIST = "dist"; //$NON-NLS-1$
	public static final String CONSTRAINT_KEY_TRANS = "trans"; //$NON-NLS-1$
	public static final String CONSTRAINT_KEY_EDGE = "edge"; //$NON-NLS-1$
	public static final String CONSTRAINT_KEY_ROOT = "root"; //$NON-NLS-1$
	
	// constraint order
	public static final int CASEDIFF_ANY = 0;
	public static final int CASEDIFF_ROOT = 1;
	public static final int CASEDIFF_FORM = 2;
	public static final int CASEDIFF_LEMMA = 3;
	public static final int CASEDIFF_FEATURES = 4;
	public static final int CASEDIFF_POS = 5;
	public static final int CASEDIFF_EXISTENCE = 6;
	public static final int CASEDIFF_RELATION = 7;
	public static final int CASEDIFF_DIRECTION = 8;
	public static final int CASEDIFF_DISTANCE = 9;

	// constraint metadata keys
	public static final String CONSTRAINT_CASEDIFF_ID = "caseDiffId"; //$NON-NLS-1$
	public static final String CONSTRAINT_CASEDIFF_COUNT = "caseDiffCount"; //$NON-NLS-1$
	public static final String CONSTRAINT_LOCATION = "location"; //$NON-NLS-1$

	// highlight flags and mask fields
	public static final int HIGHLIGHT_NONE = 0;
	public static final int HIGHLIGHT_GENERAL = 1;
	public static final int HIGHLIGHT_CASEDIFF = (1 << 2);
	public static final int HIGHLIGHT_NODE_GENERAL = (1 << 3) | HIGHLIGHT_GENERAL;
	public static final int HIGHLIGHT_EDGE_GENERAL = (1 << 4) | HIGHLIGHT_GENERAL;
	public static final int HIGHLIGHT_ROOT = (1 << 8) | HIGHLIGHT_NODE_GENERAL;
	public static final int HIGHLIGHT_ROOT_CASEDIFF = (1 << 9) | HIGHLIGHT_CASEDIFF;
	public static final int HIGHLIGHT_FORM = (1 << 10) | HIGHLIGHT_NODE_GENERAL;
	public static final int HIGHLIGHT_FORM_CASEDIFF = (1 << 11) | HIGHLIGHT_CASEDIFF;
	public static final int HIGHLIGHT_LEMMA = (1 << 12) | HIGHLIGHT_NODE_GENERAL;
	public static final int HIGHLIGHT_LEMMA_CASEDIFF = (1 << 13) | HIGHLIGHT_CASEDIFF;
	public static final int HIGHLIGHT_FEATURES = (1 << 14) | HIGHLIGHT_NODE_GENERAL;
	public static final int HIGHLIGHT_FEATURES_CASEDIFF = (1 << 15) | HIGHLIGHT_CASEDIFF;
	public static final int HIGHLIGHT_POS = (1 << 16) | HIGHLIGHT_NODE_GENERAL;
	public static final int HIGHLIGHT_POS_CASEDIFF = (1 << 17) | HIGHLIGHT_CASEDIFF;
	public static final int HIGHLIGHT_EXISTENCE = (1 << 18) | HIGHLIGHT_EDGE_GENERAL;
	public static final int HIGHLIGHT_EXISTENCE_CASEDIFF = (1 << 19) | HIGHLIGHT_CASEDIFF;
	public static final int HIGHLIGHT_RELATION = (1 << 20) | HIGHLIGHT_EDGE_GENERAL;
	public static final int HIGHLIGHT_RELATION_CASEDIFF = (1 << 21) | HIGHLIGHT_CASEDIFF;
	public static final int HIGHLIGHT_DISTANCE = (1 << 22) | HIGHLIGHT_EDGE_GENERAL;
	public static final int HIGHLIGHT_DISTANCE_CASEDIFF = (1 << 23) | HIGHLIGHT_CASEDIFF;
	public static final int HIGHLIGHT_DIRECTION = (1 << 24) | HIGHLIGHT_EDGE_GENERAL;
	public static final int HIGHLIGHT_DIRECTION_CASEDIFF = (1 << 25) | HIGHLIGHT_CASEDIFF;
	
	public static final int HIGHLIGHT_EDGE_MASK = 
		(HIGHLIGHT_RELATION | HIGHLIGHT_DIRECTION | HIGHLIGHT_DISTANCE) & ~HIGHLIGHT_GENERAL;	
	public static final int HIGHLIGHT_EDGE_CASEDIFF_MASK = 
		(HIGHLIGHT_EXISTENCE_CASEDIFF | HIGHLIGHT_RELATION_CASEDIFF | HIGHLIGHT_DIRECTION_CASEDIFF | HIGHLIGHT_DISTANCE_CASEDIFF) & ~HIGHLIGHT_CASEDIFF;
	
	public static final int HIGHLIGHT_NODE_MASK = 
		(HIGHLIGHT_FORM | HIGHLIGHT_POS | HIGHLIGHT_LEMMA | HIGHLIGHT_FEATURES) & ~HIGHLIGHT_GENERAL;
	public static final int HIGHLIGHT_NODE_CASEDIFF_MASK = 
		(HIGHLIGHT_FORM_CASEDIFF | HIGHLIGHT_POS_CASEDIFF | HIGHLIGHT_LEMMA_CASEDIFF | HIGHLIGHT_FEATURES_CASEDIFF) & ~HIGHLIGHT_CASEDIFF;

	// graph action identifiers
	public static final String GRAPH_SET_UNDEFINED_ACTION = "Graph.setUndefined"; //$NON-NLS-1$
	public static final String GRAPH_SET_NODES_UNDEFINED_ACTION = "Graph.setNodesUndefined"; //$NON-NLS-1$
	public static final String GRAPH_SET_EDGES_UNDEFINED_ACTION = "Graph.setEdgesUndefined"; //$NON-NLS-1$
	public static final String GRAPH_SET_FORM_CASEDIFF_ACTION = "Graph.setFormCaseDiff"; //$NON-NLS-1$
	public static final String GRAPH_SET_POS_CASEDIFF_ACTION = "Graph.setPosCaseDiff"; //$NON-NLS-1$
	public static final String GRAPH_SET_DIRECTION_CASEDIFF_ACTION = "Graph.setDirectionCaseDiff"; //$NON-NLS-1$
	public static final String GRAPH_SET_DISTANCE_CASEDIFF_ACTION = "Graph.setDistanceCaseDiff"; //$NON-NLS-1$
	public static final String GRAPH_SET_RELATION_CASEDIFF_ACTION = "Graph.setRelationCaseDiff"; //$NON-NLS-1$
	public static final String GRAPH_SET_FORM_UNDEFINED_ACTION = "Graph.setFormUndefined"; //$NON-NLS-1$
	public static final String GRAPH_SET_POS_UNDEFINED_ACTION = "Graph.setPosUndefined"; //$NON-NLS-1$
	public static final String GRAPH_SET_DIRECTION_UNDEFINED_ACTION = "Graph.setDirectionUndefined"; //$NON-NLS-1$
	public static final String GRAPH_SET_DISTANCE_UNDEFINED_ACTION = "Graph.setDistanceUndefined"; //$NON-NLS-1$
	public static final String GRAPH_SET_RELATION_UNDEFINED_ACTION = "Graph.setRelationUndefined"; //$NON-NLS-1$
}
