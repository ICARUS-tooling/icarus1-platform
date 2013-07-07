/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.dependency.annotation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import net.ikarus_systems.icarus.config.ConfigEvent;
import net.ikarus_systems.icarus.config.ConfigListener;
import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.search_tools.Grouping;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.ui.IconRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyHighlighting {
	
	public static final long GENERAL_HIGHLIGHT = 1;	
	public static final long NODE_HIGHLIGHT = (1 << 1);
	public static final long EDGE_HIGHLIGHT = (1 << 2);
	public static final long TRANSITIVE_HIGHLIGHT = (1 << 4);

	private static final long HEADER_MASK = 0x0F;
	
	private static final long GROUP_MASK = 0x0F;
	private static final long BLOCK_MASK = 0x1F;
	
	// Number of bits used for one token including its group id
	private static final int BLOCK_SIZE = 5;

	private static final int GENERAL_OFFSET = 5;
	private static final int TOKEN_OFFSET = GENERAL_OFFSET + BLOCK_SIZE;
	
	private static final long GENERAL_GROUP_MASK = (GROUP_MASK << GENERAL_OFFSET);
	
	private static long nodeHighlightMask = -1;
	private static long edgeHighlightMask = -1;
	private static long nodeGroupingMask = -1;
	private static long edgeGroupingMask = -1;

	private static List<String> tokens = new ArrayList<>();
	private static Map<String, Long> highlightMap = new HashMap<>();
	private static Map<String, Integer> offsetMap = new HashMap<>();
	private static Set<String> nodeSet = new LinkedHashSet<>();
	private static Set<String> edgeSet = new LinkedHashSet<>();

	private static Map<String, Color> tokenColors = new Hashtable<>();
	
	private static AtomicInteger tokenCount = new AtomicInteger();
	
	public static long getHighlight(String token) {
		return highlightMap.get(token);
	}
	
	public static int getOffset(String token) {
		return offsetMap.get(token);
	}
	
	public static boolean isNodeConstraint(String token) {
		return nodeSet.contains(token);
	}
	
	public static long getNodeHighlightMask() {
		if(nodeHighlightMask==-1) {
			nodeHighlightMask = createHighlightMask(true);
		}
		return nodeHighlightMask;
	}
	
	public static long getEdgeHighlightMask() {
		if(edgeHighlightMask==-1) {
			edgeHighlightMask = createHighlightMask(false);
		}
		return edgeHighlightMask;
	}
	
	private static long createHighlightMask(boolean node) {
		Set<String> tokens = node ? nodeSet : edgeSet;
		long mask = 0L;
		
		for(String token : tokens) {
			mask |= (1L << getOffset(token));
		}
		
		//System.out.println((node ? "nodeMask: " : "edgeMask: ")+Long.toBinaryString(mask));
		
		return mask;
	}
	
	public static long getNodeGroupingMask() {
		if(nodeGroupingMask==-1) {
			nodeGroupingMask = createGroupingMask(true);
		}
		return nodeGroupingMask;
	}
	
	public static long getEdgeGroupingMask() {
		if(edgeGroupingMask==-1) {
			edgeGroupingMask = createGroupingMask(false);
		}
		return edgeGroupingMask;
	}
	
	private static long createGroupingMask(boolean node) {
		Set<String> tokens = node ? nodeSet : edgeSet;
		long mask = 0L;
		
		for(String token : tokens) {
			mask |= (GROUP_MASK << (getOffset(token)+1));
		}
		
		//System.out.println((node ? "nodeGrMask: " : "edgeGrMask: ")+Long.toBinaryString(mask));
		
		return mask;
	}
	
	public static boolean isHighlighted(long highlight) {
		return highlight!=0L;
	}

	public static long getHighlight(SearchConstraint[] constraints, boolean node, boolean edge) {
		long highlight = GENERAL_HIGHLIGHT;
		if(node) {
			highlight |= NODE_HIGHLIGHT; 
		}
		if(edge) {
			highlight |= EDGE_HIGHLIGHT;
		}
		
		if(constraints==null) {
			return highlight;
		}
		
		long minGroup = 0L;
		
		for(SearchConstraint constraint : constraints) {
			if(constraint.isUndefined()) {
				continue;
			}
			
			int offset = getOffset(constraint.getToken());
			
			if(SearchManager.isGroupingOperator(constraint.getOperator())) {
				long group = (int) constraint.getValue() + 1L;
				
				if(group>15)
					throw new IllegalArgumentException("Unable to highlight group id: "+group); //$NON-NLS-1$
				
				if(minGroup==0L) {
					minGroup = group;
				}
				
				highlight |= (group << (offset+1));
			}
			
			highlight |= (1L << offset);
		}
		
		if(minGroup!=0) {
			highlight |= (minGroup << GENERAL_OFFSET);
		}
		
		return highlight;
	}
	
	public static void registerToken(String token, boolean node, Color col) {
		int count = tokenCount.getAndIncrement();
		int offset = TOKEN_OFFSET + (count * BLOCK_SIZE);
		
		if(63<(offset+BLOCK_SIZE))
			throw new IllegalStateException("Cannot store additional highlight data within a single long integer"); //$NON-NLS-1$
		
		long highlight = (1L << offset);
		
		if(node) {
			highlight |= NODE_HIGHLIGHT;
		} else {
			highlight |= EDGE_HIGHLIGHT;
		}
		
		highlightMap.put(token, highlight);
		offsetMap.put(token, offset);
		tokens.add(token);
		if(node) {
			nodeSet.add(token);
		} else {
			edgeSet.add(token);
		}
		tokenColors.put(token, col);
		
		nodeGroupingMask = nodeHighlightMask = -1;
		edgeGroupingMask = edgeHighlightMask = -1;
	}
	
	private static Pattern pattern;
	private static String toBin(long val) {
		if(pattern==null) {
			pattern = Pattern.compile("([01]{8})"); //$NON-NLS-1$
		}
		String res = Long.toBinaryString(val);
		res = String.format("%1$64s", res).replace(' ', '0'); //$NON-NLS-1$
		res = pattern.matcher(res).replaceAll("$1 "); //$NON-NLS-1$
		return res;
	}
	
	public static void dumpHighlightDB() {
		for(String token : tokens) {
			System.out.printf("%s: offset=%d token=%s\n",  //$NON-NLS-1$
					toBin(getHighlight(token)), getOffset(token), token);
		}
		System.out.printf("%s: node-highlight-mask\n", toBin(getNodeHighlightMask())); //$NON-NLS-1$
		System.out.printf("%s: node-grouping-mask\n", toBin(getNodeGroupingMask())); //$NON-NLS-1$
		System.out.printf("%s: edge-highlight-mask\n", toBin(getEdgeHighlightMask())); //$NON-NLS-1$
		System.out.printf("%s: edge-grouping-mask\n", toBin(getNodeGroupingMask())); //$NON-NLS-1$
		System.out.printf("%s: general highlight\n", toBin(GENERAL_HIGHLIGHT)); //$NON-NLS-1$
		System.out.printf("%s: node highlight\n", toBin(NODE_HIGHLIGHT)); //$NON-NLS-1$
		System.out.printf("%s: edge highlight\n", toBin(EDGE_HIGHLIGHT)); //$NON-NLS-1$
		System.out.printf("%s: transitive highlight\n", toBin(TRANSITIVE_HIGHLIGHT)); //$NON-NLS-1$
	}

	static {
		registerToken("form", true, new Color(2807039)); //$NON-NLS-1$
		registerToken("lemma", true, new Color(2807039)); //$NON-NLS-1$
		registerToken("pos", true, new Color(2807039)); //$NON-NLS-1$
		registerToken("features", true, new Color(2807039)); //$NON-NLS-1$
		registerToken("projectivity", false, new Color(1677593)); //$NON-NLS-1$
		registerToken("relation", false, new Color(1677593)); //$NON-NLS-1$
		registerToken("distance", false, new Color(14748812)); //$NON-NLS-1$
		registerToken("direction", false, new Color(10789924)); //$NON-NLS-1$
		
		//dumpHighlightDB();
	}
	
	public static String[] getTokens() {
		return tokens.toArray(new String[0]);
	}
	
	public static boolean isNodeToken(String token) {
		return nodeSet.contains(token);
	}
	
	public static boolean isEdgeToken(String token) {
		return edgeSet.contains(token);
	}
	
	public static String dumpHighlight(long highlight) {
		if(highlight==0L) {
			return "<none>"; //$NON-NLS-1$
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(Long.toBinaryString(highlight));
		sb.append(" general=").append((highlight & GENERAL_HIGHLIGHT) !=0L); //$NON-NLS-1$
		sb.append(" node=").append((highlight & NODE_HIGHLIGHT) !=0L); //$NON-NLS-1$
		sb.append(" edge=").append((highlight & EDGE_HIGHLIGHT) !=0L); //$NON-NLS-1$
		sb.append(" trans=").append((highlight & TRANSITIVE_HIGHLIGHT) !=0L); //$NON-NLS-1$
		sb.append(" minGroup=").append(GROUP_MASK & (highlight >> GENERAL_OFFSET)); //$NON-NLS-1$
		for(String token : tokens) {
			sb.append(" ").append(token).append("="); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append((highlight & getHighlight(token)) !=0L);
			
			int offset = getOffset(token) + 1;
			int groupId = (int) (GROUP_MASK & (highlight >> offset));
			if(groupId>0) {
				sb.append("(gp=").append(groupId).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		return sb.toString();
	}

	private static Color transitiveHighlightColor = new Color(1677593);
	private static Color nodeHighlightColor = new Color(-3407668);
	private static Color edgeHighlightColor = new Color(-3407668);
	
	private static StyleContext styleContext = new StyleContext();
	
	private static ConfigListener configListener;
	
	public static synchronized void loadConfig() {
		
		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		Handle group = config.getHandle("plugins.dependency.highlighting"); //$NON-NLS-1$
		
		if(configListener==null) {
			configListener = new ConfigListener() {
				
				@Override
				public void invoke(ConfigRegistry sender, ConfigEvent event) {
					loadConfig();
				}
			};
			config.addGroupListener(group, configListener);
		}
		
		List<String> tokens = new ArrayList<>(tokenColors.keySet());
		for(String token : tokens) {
			Color col = new Color(config.getInteger(config.getChildHandle(group, token+"Highlight"))); //$NON-NLS-1$
			tokenColors.put(token, col);
		}
		
		transitiveHighlightColor = new Color(config.getInteger(config.getChildHandle(group, "transitiveHighlight"))); //$NON-NLS-1$
		nodeHighlightColor = new Color(config.getInteger(config.getChildHandle(group, "nodeHighlight"))); //$NON-NLS-1$
		edgeHighlightColor = new Color(config.getInteger(config.getChildHandle(group, "edgeHighlight"))); //$NON-NLS-1$

		styleContext = new StyleContext();		
		Style style;
		
		// GROUP 1
		style = styleContext.addStyle("group1", null); //$NON-NLS-1$
		StyleConstants.setBackground(style, Grouping.getGrouping(0).getColor());
		StyleConstants.setBold(style, true);
		
		// GROUP 2
		style = styleContext.addStyle("group2", null); //$NON-NLS-1$
		StyleConstants.setBackground(style, Grouping.getGrouping(1).getColor());
		StyleConstants.setBold(style, true);
		
		// GROUP 3
		style = styleContext.addStyle("group3", null); //$NON-NLS-1$
		StyleConstants.setBackground(style, Grouping.getGrouping(2).getColor());
		StyleConstants.setBold(style, true);

		
		// NODE
		style = styleContext.addStyle("node", null); //$NON-NLS-1$
		StyleConstants.setForeground(style, nodeHighlightColor);
		StyleConstants.setItalic(style, true);
		
		// EDGE
		style = styleContext.addStyle("edge", null); //$NON-NLS-1$
		StyleConstants.setForeground(style, edgeHighlightColor);
		StyleConstants.setItalic(style, true);

		// MULTIPLE HIGHLIGHTS
		style = styleContext.addStyle("multiple", null); //$NON-NLS-1$
		StyleConstants.setIcon(style, IconRegistry.getGlobalRegistry().getIcon("multiple_annotation.gif")); //$NON-NLS-1$
	}
	
	public static StyleContext getStyleContext() {
		return styleContext;
	}
	
	public static Color getHighlightColor(String token) {
		return tokenColors.get(token);
	}
	
	public static Color getTransitiveHighlightColor() {
		return transitiveHighlightColor;
	}

	public static Color getNodeHighlightColor() {
		return nodeHighlightColor;
	}

	public static Color getEdgeHighlightColor() {
		return edgeHighlightColor;
	}

	public static Color getHighlightColor(long highlight, String token) {
		if(highlight==0L) {
			return null;
		}
		long hl = getHighlight(token);
		if(hl==0L) {
			return null;
		}
		if((hl & highlight) != hl) {
			return null;
		}
		
		Color col = null;
		int offset = getOffset(token);
		int groupIndex = (int) (GROUP_MASK & (highlight >> (offset+1)));
		if(groupIndex>0) {
			col = Grouping.getGrouping(groupIndex-1).getColor();
		}
		
		if(col==null) {
			col = getHighlightColor(token);
		}
		
		return col;
	}
	
	public static Color getHighlightColor(long highlight) {
		if(highlight==0L) {
			return null;
		}
		
		Color col = null;
		
		int offset = TOKEN_OFFSET;
		for(int i=0; i<tokens.size(); i++) {
			long hl = 1L << offset;
			if((highlight & hl) == hl) {
				String token = tokens.get(i);
				col = getHighlightColor(token);
				break;
			}
			offset += BLOCK_SIZE;
		}
		
		if(col==null) {
			if((highlight & NODE_HIGHLIGHT) == NODE_HIGHLIGHT) {
				col = nodeHighlightColor;
			} else if((highlight & EDGE_HIGHLIGHT) == EDGE_HIGHLIGHT) {
				col = edgeHighlightColor;
			} else if((highlight & TRANSITIVE_HIGHLIGHT) == TRANSITIVE_HIGHLIGHT) {
				col = transitiveHighlightColor;
			}
		}
		
		return col;
	}
	
	public static Color getNodeHighlightColor(long highlight) {
		if(highlight==0L) {
			return null;
		}
		
		Color col = null;
		
		for(String token : nodeSet) {
			long hl = getHighlight(token);
			if((highlight & hl) == hl) {
				col = getHighlightColor(token);
				break;
			}
		}
		
		if(col==null) {
			if((highlight & NODE_HIGHLIGHT) == NODE_HIGHLIGHT) {
				col = nodeHighlightColor;
			}
		}
		
		return col;
	}
	
	public static Color getEdgeHighlightColor(long highlight) {
		if(highlight==0L) {
			return null;
		}
		
		Color col = null;
		
		for(String token : edgeSet) {
			long hl = getHighlight(token);
			if((highlight & hl) == hl) {
				col = getHighlightColor(token);
				break;
			}
		}
		
		if(col==null) {
			if((highlight & EDGE_HIGHLIGHT) == EDGE_HIGHLIGHT) {
				col = edgeHighlightColor;
			} else if((highlight & TRANSITIVE_HIGHLIGHT) == TRANSITIVE_HIGHLIGHT) {
				col = transitiveHighlightColor;
			}
		}
		
		return col;
	}
	
	public static Color getGroupColor(long highlight) {
		if(highlight==0L) {
			return null;
		}

		int groupIndex = (int) (GROUP_MASK & (highlight >> GENERAL_OFFSET));
		if(groupIndex>0) {
			return Grouping.getGrouping(groupIndex-1).getColor();
		}
		
		return null;
	}

	public static int getGroupId(long highlight) {
		return (int) (GROUP_MASK & (highlight >> GENERAL_OFFSET)) - 1;
	}

	public static int getNodeGroupId(long highlight) {
		if((highlight & getNodeGroupingMask()) == 0L) {
			return -1;
		}
		
		for(int i=0; i<tokens.size(); i++) {
			int offset = TOKEN_OFFSET + BLOCK_SIZE  * i + 1;
			int groupId = (int) (GROUP_MASK & (highlight >> offset));
			if(groupId>0 && nodeSet.contains(tokens.get(i))) {
				return groupId-1;
			}
		}
		
		return -1;
	}

	public static int getEdgeGroupId(long highlight) {
		if((highlight & getEdgeGroupingMask()) == 0L) {
			return -1;
		}
		
		for(int i=0; i<tokens.size(); i++) {
			int offset = TOKEN_OFFSET + BLOCK_SIZE * i + 1;
			int groupId = (int) (GROUP_MASK & (highlight >> offset));
			if(groupId>0 && edgeSet.contains(tokens.get(i-1))) {
				return groupId-1;
			}
		}
		
		return -1;
	}

	public static int getGroupId(long highlight, String token) {
		int offset = getOffset(token) + 1;
		return (int) (GROUP_MASK & (highlight >> offset)) - 1;
	}

	public static boolean isNodeHighlighted(long highlight) {
		return (highlight& NODE_HIGHLIGHT) == NODE_HIGHLIGHT;
	}

	public static boolean isEdgeHighlighted(long highlight) {
		return (highlight & EDGE_HIGHLIGHT) == EDGE_HIGHLIGHT;
	}

	public static boolean isTransitiveHighlighted(long highlight) {
		return (highlight & TRANSITIVE_HIGHLIGHT) == TRANSITIVE_HIGHLIGHT;
	}

	public static boolean isTokenHighlighted(long highlight, String token) {
		long hl = (1L << getOffset(token));
		return (highlight & hl) == hl;
	}
	
	public static boolean isConcurrentHighlight(long highlight) {
		if(highlight==0L) {
			return false;
		}
		
		long mask = (BLOCK_MASK << TOKEN_OFFSET);
		int count = 0;
		
		for(int i=0; i<tokens.size(); i++) {
			if((highlight & mask) != 0L) {
				count++;
				
				if(count>1) {
					break;
				}
			}
			
			mask = (mask << BLOCK_SIZE);
		}
		
		return count>1;
	}
	
	public static int getConcurrentHighlightCount(long highlight) {
		if(highlight == 0L) {
			return 0;
		}
		
		long mask = (1L << TOKEN_OFFSET);
		int count = 0;
		
		for(int i=0; i<tokens.size(); i++) {
			if((highlight & mask) != 0L) {
				count++;
			}
			
			mask = (mask << BLOCK_SIZE);
		}
		
		return count;
	}
	
	public static int getConcurrentGroupCount(long highlight) {
		if(highlight==0L || (highlight & GENERAL_GROUP_MASK) == 0L) {
			return 0;
		}
		
		long mask = (GROUP_MASK << (TOKEN_OFFSET + 1));
		int count = 0;
		
		for(int i=0; i<tokens.size(); i++) {
			if((highlight & mask) != 0L) {
				count++;
			}
			
			mask = (mask << BLOCK_SIZE);
		}
		
		return count;
	}
	
	public static long createCompositeHighlight(long[] highlights) {
		long result = 0L;
		for(long highlight : highlights) {
			result |= (highlight & HEADER_MASK);
		}

		int groupId = -1;
		
		// Traverse node highlights
		for(String token : nodeSet) {
			boolean assigned = false;
			int offset = getOffset(token);
			long hl = (1L << offset);
			long mask = (GROUP_MASK << (offset + 1));
			int grp = -1;
			
			for(long highlight : highlights) {
				// Check for normal highlight
				if(!assigned && (highlight & hl)==hl) {
					result |= hl;
				}
				// Check for grouping highlight
				grp = (int) (((highlight & mask) >> (offset + 1)) -1);
				if(grp!=-1) {
					result |= hl; // grouping implies regular highlight flag!
					result |= (highlight & mask);
					if(groupId!=-1) {
						groupId = grp;
					}
				}
				
				if(assigned && grp!=-1) {
					break;
				}
			}
		}
		
		// Traverse edge highlights
		for(String token : edgeSet) {
			boolean assigned = false;
			int offset = getOffset(token);
			long hl = (1L << offset);
			long mask = (GROUP_MASK << (offset + 1));
			int grp = -1;
			
			for(long highlight : highlights) {
				// Check for normal highlight
				if(!assigned && (highlight & hl)==hl) {
					result |= hl;
				}
				// Check for grouping highlight
				grp = (int) (((highlight & mask) >> (offset + 1)) -1);
				if(grp!=-1) {
					result |= hl; // grouping implies regular highlight flag!
					result |= (highlight & mask);
					if(groupId!=-1) {
						groupId = grp;
					}
				}
				
				if(assigned && grp!=-1) {
					break;
				}
			}
		}
		
		if(groupId!=-1) {
			result |= (groupId << GENERAL_OFFSET);
		}
		
		return result;
	}
}
