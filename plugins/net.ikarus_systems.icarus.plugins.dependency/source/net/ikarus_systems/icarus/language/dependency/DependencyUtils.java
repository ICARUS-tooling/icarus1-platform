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

import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.language.Grammar;
import net.ikarus_systems.icarus.language.LanguageConstants;
import net.ikarus_systems.icarus.language.LanguageManager;
import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.treebank.Treebank;
import net.ikarus_systems.icarus.search_tools.SearchMode;
import net.ikarus_systems.icarus.search_tools.SearchParameters;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.util.Exceptions;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.Orientation;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyUtils implements DependencyConstants {

	private static Color formColor = new Color(4397250);
	private static Color posColor = new Color(2807039);
	private static Color lemmaColor = new Color(2807039);
	private static Color featuresColor = new Color(2807039);
	private static Color existenceColor = new Color(1677593);
	private static Color relationColor = new Color(1677593);
	private static Color directionColor = new Color(10789924);
	private static Color distanceColor = new Color(14748812);
	private static Color rootColor = new Color(4397250);

	private static Color nodeHighlightColor = new Color(-52429);
	private static Color edgeHighlightColor = new Color(-52429);
	
	private static String formColorString = getHexColorString(formColor);
	private static String posColorString = getHexColorString(posColor);
	private static String lemmaColorString = getHexColorString(lemmaColor);
	private static String featuresColorString = getHexColorString(featuresColor);
	private static String existenceColorString = getHexColorString(existenceColor);
	private static String relationColorString = getHexColorString(relationColor);
	private static String directionColorString = getHexColorString(directionColor);
	private static String distanceColorString = getHexColorString(distanceColor);
	private static String rootColorString = getHexColorString(rootColor);

	private static String nodeHighlightColorString = getHexColorString(nodeHighlightColor);
	private static String edgeHighlightColorString = getHexColorString(edgeHighlightColor);
	
	private static StyleContext styleContext = new StyleContext();
	
	private static String getHexColorString(Color color) {
		return Integer.toHexString((color.getRGB() & 0x00FFFFFF)
				| (color.getAlpha() << 24));
	}
	
	public static void loadConfig(ConfigRegistry config, Handle group) {
		formColor = new Color(config.getInteger(config.getChildHandle(group, "formHighlight"))); //$NON-NLS-1$
		posColor = new Color(config.getInteger(config.getChildHandle(group, "posHighlight"))); //$NON-NLS-1$
		lemmaColor = new Color(config.getInteger(config.getChildHandle(group, "lemmaHighlight"))); //$NON-NLS-1$
		featuresColor = new Color(config.getInteger(config.getChildHandle(group, "featuresHighlight"))); //$NON-NLS-1$
		existenceColor = new Color(config.getInteger(config.getChildHandle(group, "existenceHighlight"))); //$NON-NLS-1$
		relationColor = new Color(config.getInteger(config.getChildHandle(group, "relationHighlight"))); //$NON-NLS-1$
		directionColor = new Color(config.getInteger(config.getChildHandle(group, "directionHighlight"))); //$NON-NLS-1$
		distanceColor = new Color(config.getInteger(config.getChildHandle(group, "distanceHighlight"))); //$NON-NLS-1$
		rootColor = new Color(config.getInteger(config.getChildHandle(group, "rootHighlight"))); //$NON-NLS-1$

		nodeHighlightColor = new Color(config.getInteger(config.getChildHandle(group, "nodeHighlight"))); //$NON-NLS-1$
		edgeHighlightColor = new Color(config.getInteger(config.getChildHandle(group, "edgeHighlight"))); //$NON-NLS-1$

		/*combinationPolicy = (HighlightCombinationPolicy) 
				config.getValue(config.getChildHandle(group, "combinationPolicy"));*/

		formColorString = getHexColorString(formColor);
		posColorString = getHexColorString(posColor);
		lemmaColorString = getHexColorString(lemmaColor);
		featuresColorString = getHexColorString(featuresColor);
		existenceColorString = getHexColorString(existenceColor);
		relationColorString = getHexColorString(relationColor);
		directionColorString = getHexColorString(directionColor);
		distanceColorString = getHexColorString(distanceColor);
		rootColorString = getHexColorString(rootColor);
		nodeHighlightColorString = getHexColorString(nodeHighlightColor);
		edgeHighlightColorString = getHexColorString(edgeHighlightColor);
		
		styleContext = new StyleContext();		
		Style style;
		
		// GROUP 1
		style = styleContext.addStyle("caseDiff1", null); //$NON-NLS-1$
		StyleConstants.setBackground(style, getCaseDiffColor(0));
		StyleConstants.setBold(style, true);
		
		// GROUP 2
		style = styleContext.addStyle("caseDiff2", null); //$NON-NLS-1$
		StyleConstants.setBackground(style, getCaseDiffColor(1));
		StyleConstants.setBold(style, true);
		
		// GROUP 3
		style = styleContext.addStyle("caseDiff3", null); //$NON-NLS-1$
		StyleConstants.setBackground(style, getCaseDiffColor(2));
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
		StyleConstants.setIcon(style, IconRegistry.getGlobalRegistry().getIcon("stcksync_ov.gif")); //$NON-NLS-1$
	}

	
	public static Options createOptionsFromConfig() {

		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		
		Options options = new Options();
		options.put(SearchParameters.REGEX_ENABLED, 
				config.getBoolean("dependency.search.useRegex")); //$NON-NLS-1$
		options.put(SearchParameters.SEARCH_CASESENSITIVE, 
				config.getBoolean("dependency.search.caseSensitive")); //$NON-NLS-1$
		options.put(SearchParameters.SEARCH_ORIENTATION, 
				"leftToRight".equals( //$NON-NLS-1$
						config.getString("dependency.search.direction"))? //$NON-NLS-1$
								Orientation.LEFT_TO_RIGHT 
								: Orientation.RIGHT_TO_LEFT);
		options.put(SearchParameters.SEARCH_RESULT_LIMIT,
				config.getInteger("dependency.search.maxResultCount")); //$NON-NLS-1$
		SearchMode mode = SearchMode.MATCH;
		String modeString = config.getString("dependency.search.searchMode"); //$NON-NLS-1$
		
		// TODO use accurate mode check!
		if("occurrences".equals(modeString)) { //$NON-NLS-1$
			mode = SearchMode.HITS;
		} else if("exhaustiveSentences".equals(modeString)) { //$NON-NLS-1$
		}
		
		options.put(SearchParameters.SEARCH_MODE, mode);
		
		return options;
	}

	public static Color getCaseDiffColor(int index) {
		return null; // TODO
	}

	public static String getCaseDiffColorString(int index) {
		return getHexColorString(null); // TODO
	}

	/**
	 * @return the formColor
	 */
	public static Color getFormColor() {
		return formColor;
	}

	/**
	 * @return the posColor
	 */
	public static Color getPosColor() {
		return posColor;
	}

	/**
	 * @return the existenceColor
	 */
	public static Color getExistenceColor() {
		return existenceColor;
	}

	/**
	 * @return the lemmaColor
	 */
	public static Color getLemmaColor() {
		return lemmaColor;
	}

	/**
	 * @return the featuresColor
	 */
	public static Color getFeaturesColor() {
		return featuresColor;
	}

	/**
	 * @return the relationColor
	 */
	public static Color getRelationColor() {
		return relationColor;
	}

	/**
	 * @return the directionColor
	 */
	public static Color getDirectionColor() {
		return directionColor;
	}

	/**
	 * @return the distanceColor
	 */
	public static Color getDistanceColor() {
		return distanceColor;
	}

	/**
	 * @return the rootColor
	 */
	public static Color getRootColor() {
		return rootColor;
	}

	/**
	 * @return the formColorString
	 */
	public static String getFormColorString() {
		return formColorString;
	}

	/**
	 * @return the lemmaColorString
	 */
	public static String getLemmaColorString() {
		return lemmaColorString;
	}

	/**
	 * @return the featuresColorString
	 */
	public static String getFeaturesColorString() {
		return featuresColorString;
	}

	/**
	 * @return the posColorString
	 */
	public static String getPosColorString() {
		return posColorString;
	}

	/**
	 * @return the existenceColorString
	 */
	public static String getExistenceColorString() {
		return existenceColorString;
	}

	/**
	 * @return the relationColorString
	 */
	public static String getRelationColorString() {
		return relationColorString;
	}

	/**
	 * @return the directionColorString
	 */
	public static String getDirectionColorString() {
		return directionColorString;
	}

	/**
	 * @return the distanceColorString
	 */
	public static String getDistanceColorString() {
		return distanceColorString;
	}

	/**
	 * @return the rootColorString
	 */
	public static String getRootColorString() {
		return rootColorString;
	}

	/**
	 * @return the nodeHighlightColor
	 */
	public static Color getNodeHighlightColor() {
		return nodeHighlightColor;
	}


	/**
	 * @return the edgeHighlightColor
	 */
	public static Color getEdgeHighlightColor() {
		return edgeHighlightColor;
	}


	/**
	 * @return the nodeHighlightColorString
	 */
	public static String getNodeHighlightColorString() {
		return nodeHighlightColorString;
	}


	/**
	 * @return the edgeHighlightColorString
	 */
	public static String getEdgeHighlightColorString() {
		return edgeHighlightColorString;
	}
	
	public static StyleContext getStyleContext() {
		return styleContext;
	}

	public static DependencyData createEmptySentenceData() {
		return new SimpleDependencyData();
	}
	
	public static String createExampleQuery() {
		return "[pos=<*>[form=the]][pos=NN]"; //$NON-NLS-1$
	}

	public static DependencyData createExampleSentenceData() {
		return new SimpleDependencyData(
				new String[] { "That", "thing", "I", "will", "never", "forget" },  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				new String[] { "that", "thing", "i", "will", "never", "forget" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				new String[] { "_", "_", "_", "_", "_", "_" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				new String[] { "DT", "NN", "PRP", "MD", "RB", "VB" },  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				new String[] {"NMOD", "ROOT", "SBJ", "NMOD", "TMP", "VC" },  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				new short[] { 1, 5, 3, LanguageUtils.DATA_HEAD_ROOT, 3, 3 },
				new long[]{ 0, 0, 0, 0, 0, 0}); // TODO
	}
	
	public static final SentenceData dummySentenceData = 
		new SimpleDependencyData(
				new String[]{"Test"},  //$NON-NLS-1$
				new String[]{""},   //$NON-NLS-1$
				new String[]{""},   //$NON-NLS-1$
				new String[]{""},  //$NON-NLS-1$
				new String[]{""},  //$NON-NLS-1$
				new short[]{LanguageUtils.DATA_UNDEFINED_VALUE},
				new long[]{0});
	
	/*public static String getExistenceLabel(int existence) {
		switch (existence) {
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_GROUP_VALUE:
			return DATA_GROUP_LABEL;
		default:
			return String.valueOf(true);
		}
	}
	
	public static int parseExistenceLabel(String label) {
		if(DATA_GROUP_LABEL.equals(label))
			return DATA_GROUP_VALUE;
		else if(DATA_UNDEFINED_LABEL.equals(label))
			return DATA_UNDEFINED_VALUE;
		else
			// TODO should we check if label equals "true"?
			return DATA_YES_VALUE;
	}*/

	public static int getDataField(int column) {
		switch (column) {
		case DATA_FIELD_FORM:
			return TABLE_INDEX_FORM;
		case DATA_FIELD_POS:
			return TABLE_INDEX_POS;
		case DATA_FIELD_HEAD:
			return TABLE_INDEX_HEAD;
		case DATA_FIELD_RELATION:
			return TABLE_INDEX_REL;
		default:
			return -1;
		}
	}

	public static int getColumn(int field) {
		switch (field) {
		case TABLE_INDEX_FORM:
			return DATA_FIELD_FORM;
		case TABLE_INDEX_POS:
			return DATA_FIELD_POS;
		case TABLE_INDEX_HEAD:
			return DATA_FIELD_HEAD;
		case TABLE_INDEX_REL:
			return DATA_FIELD_RELATION;
		default:
			return -1;
		}
	}
	
	private static class DataEntry {
		public short index = LanguageUtils.DATA_UNDEFINED_VALUE;
		public String form = ""; //$NON-NLS-1$
		public String lemma = ""; //$NON-NLS-1$
		public String features = ""; //$NON-NLS-1$
		public String pos = ""; //$NON-NLS-1$
		public String relation = ""; //$NON-NLS-1$
		public DataEntry head = null;
	}
	
	public static SimpleDependencyData parseData(String text) {
		Exceptions.testNullArgument(text, "text"); //$NON-NLS-1$

		int length = text.length();
		if (length < 2 || text.charAt(0) != '['
				|| text.charAt(length - 1) != ']')
			return null;

		char c;
		boolean escape = false;
		Stack<DataEntry> trace = new Stack<DataEntry>();
		List<DataEntry> items = new Vector<DataEntry>();
		DataEntry entry;
		StringBuilder sb = new StringBuilder(20);
		String key = null, value = null;

		for (int i = 0; i < length; i++) {
			c = text.charAt(i);

			if (escape) {
				sb.append(c);
				escape = false;
				continue;
			}

			if (Character.isWhitespace(c))
				continue;

			switch (c) {
			case '\\':
				escape = true;
				break;
			case '=': {
				key = sb.toString();
				sb.setLength(0);
			}
				break;
			case ',': {
				value = sb.toString();
				sb.setLength(0);
				addConstraint(trace.peek(), key, value);
			}
				break;
			case '[': {
				if (key != null) {
					value = sb.toString();
					addConstraint(trace.peek(), key, value);
				}

				entry = new DataEntry();
				if (trace.size() > 0)
					entry.head = trace.peek();

				sb.setLength(0);
				key = null;
				value = null;
				trace.push(entry);
			}
				break;
			case ']': {
				entry = trace.pop();
				if (key != null) {
					value = sb.toString();
					addConstraint(entry, key, value);
				}
				sb.setLength(0);
				key = null;
				value = null;

				entry.index = (short) items.size();
				items.add(entry);
			}
				break;
			default:
				sb.append(c);
				break;
			}

		}

		if (trace.size() > 0)
			throw new IllegalStateException("Unclosed '[' in sentence description"); //$NON-NLS-1$

		int size = items.size();
		String[] forms = new String[size];
		String[] lemmas = new String[size];
		String[] features = new String[size];
		String[] poss = new String[size];
		String[] relations = new String[size];
		short[] heads = new short[size];
		
		for(int i=0; i<size; i++) {
			entry = items.get(i);
			forms[i] = entry.form;
			lemmas[i] = entry.lemma;
			features[i] = entry.features;
			poss[i] = entry.pos;
			relations[i] = entry.relation;
			heads[i] = entry.head==null ? LanguageUtils.DATA_HEAD_ROOT : entry.head.index;
		}
		
		return new SimpleDependencyData(forms, lemmas, features, poss, relations, heads, null);
	}

	protected static void addConstraint(DataEntry entry,
			String key, String value) {
		if (CONSTRAINT_KEY_FORM.equals(key))
			entry.form = value;
		else if (CONSTRAINT_KEY_LEMMA.equals(key))
			entry.lemma = value;
		else if (CONSTRAINT_KEY_FEATURES.equals(key))
			entry.features = value;
		else if (CONSTRAINT_KEY_POS.equals(key))
			entry.pos = value;
		else if (CONSTRAINT_KEY_RELATION.equals(key))
			entry.relation = value;
		else
			throw new IllegalArgumentException(String.format(
					"Unknown field key: '%s' (value='%s')", String //$NON-NLS-1$
							.valueOf(key), String.valueOf(value)));
	}
	
	public static String getHighlightDump(int highlight) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("bin=").append(Integer.toBinaryString(highlight)); //$NON-NLS-1$
		
		if(isHighlight(highlight)) {
			sb.append(" hl="); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_GENERAL) == HIGHLIGHT_GENERAL)
				sb.append("+"); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_FORM) == HIGHLIGHT_FORM)
				sb.append("f"); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_POS) == HIGHLIGHT_POS)
				sb.append("p"); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_RELATION) == HIGHLIGHT_RELATION)
				sb.append("r"); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_DIRECTION) ==  HIGHLIGHT_DIRECTION)
				sb.append("d"); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_DISTANCE) == HIGHLIGHT_DISTANCE)
				sb.append("s"); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_NODE_GENERAL) == HIGHLIGHT_NODE_GENERAL)
				sb.append("N"); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_EDGE_GENERAL) == HIGHLIGHT_EDGE_GENERAL)
				sb.append("E"); //$NON-NLS-1$
	
			sb.append(" gps="); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_GROUP) == HIGHLIGHT_GROUP)
				sb.append("+"); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_FORM_GROUP) == HIGHLIGHT_FORM_GROUP)
				sb.append("f"); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_POS_GROUP) == HIGHLIGHT_POS_GROUP)
				sb.append("p"); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_RELATION_GROUP) == HIGHLIGHT_RELATION_GROUP)
				sb.append("r"); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_DIRECTION_GROUP) ==  HIGHLIGHT_DIRECTION_GROUP)
				sb.append("d"); //$NON-NLS-1$
			if((highlight & HIGHLIGHT_DISTANCE_GROUP) == HIGHLIGHT_DISTANCE_GROUP)
				sb.append("s"); //$NON-NLS-1$
		}
		
		return sb.toString();
	}
	
	public static boolean isGroup(int highlight) {
		return (highlight & HIGHLIGHT_GROUP) == HIGHLIGHT_GROUP;
	}
	
	public static boolean isHighlight(int highlight) {
		return (highlight & HIGHLIGHT_GENERAL) == HIGHLIGHT_GENERAL;
	}
	
	public static boolean isNodeHighlight(int highlight) {
		return (highlight & HIGHLIGHT_NODE_GENERAL) == HIGHLIGHT_NODE_GENERAL;
	}
	
	public static boolean isEdgeHighlight(int highlight) {
		return (highlight & HIGHLIGHT_EDGE_GENERAL) == HIGHLIGHT_EDGE_GENERAL;
	}

	public static boolean isHighlight(int highlight, int mask) {
		return (highlight & mask) == mask;
	}

	public static boolean isFormHighlight(int highlight) {
		return (highlight & HIGHLIGHT_FORM) == HIGHLIGHT_FORM;
	}

	public static boolean isLemmaHighlight(int highlight) {
		return (highlight & HIGHLIGHT_LEMMA) == HIGHLIGHT_LEMMA;
	}

	public static boolean isFeaturesHighlight(int highlight) {
		return (highlight & HIGHLIGHT_FEATURES) == HIGHLIGHT_FEATURES;
	}

	public static boolean isPosHighlight(int highlight) {
		return (highlight & HIGHLIGHT_POS) == HIGHLIGHT_POS;
	}

	public static boolean isRelationHighlight(int highlight) {
		return (highlight & HIGHLIGHT_RELATION) == HIGHLIGHT_RELATION;
	}

	public static boolean isDistanceHighlight(int highlight) {
		return (highlight & HIGHLIGHT_DISTANCE) == HIGHLIGHT_DISTANCE;
	}

	public static boolean isDirectionHighlight(int highlight) {
		return (highlight & HIGHLIGHT_DIRECTION) == HIGHLIGHT_DIRECTION;
	}

	public static String getNodeHighlightColorString(int highlight) {
		if((highlight & HIGHLIGHT_NODE_GENERAL) != HIGHLIGHT_NODE_GENERAL)
			return null;
		
		/*System.out.printf("getNodeHighlightColorString: index=%d highlight=%s form=%b pos=%b\n", 
				index, Integer.toBinaryString(highlight), 
				(highlight & HIGHLIGHT_FORM) == HIGHLIGHT_FORM,
				(highlight & HIGHLIGHT_POS) == HIGHLIGHT_POS);*/
		
		if((highlight & HIGHLIGHT_FORM) == HIGHLIGHT_FORM)
			return formColorString;
		else if((highlight & HIGHLIGHT_POS) == HIGHLIGHT_POS)
			return posColorString;
		
		return nodeHighlightColorString;
	}
	
	public static String getEdgeHighlightColorString(int highlight) {
		if((highlight & HIGHLIGHT_EDGE_GENERAL) != HIGHLIGHT_EDGE_GENERAL)
			return null;
		
		/*System.out.printf("getEdgeHighlightColorString: index=%d highlight=%s existence=%b relation=%b direction=%b distance=%b\n", 
				index, Integer.toBinaryString(highlight),  
				(highlight & HIGHLIGHT_EXISTENCE) == HIGHLIGHT_EXISTENCE,
				(highlight & HIGHLIGHT_RELATION) == HIGHLIGHT_RELATION,
				(highlight & HIGHLIGHT_DIRECTION) == HIGHLIGHT_DIRECTION,
				(highlight & HIGHLIGHT_DISTANCE) == HIGHLIGHT_DISTANCE);*/

		if((highlight & HIGHLIGHT_EXISTENCE) == HIGHLIGHT_EXISTENCE)
			return existenceColorString;
		else if((highlight & HIGHLIGHT_RELATION) == HIGHLIGHT_RELATION)
			return relationColorString;
		else if((highlight & HIGHLIGHT_DIRECTION) ==  HIGHLIGHT_DIRECTION)
			return directionColorString;
		else if((highlight & HIGHLIGHT_DISTANCE) == HIGHLIGHT_DISTANCE)
			return distanceColorString;
		
		return edgeHighlightColorString;
	}

	public static Color getNodeHighlightColor(int highlight) {
		if((highlight & HIGHLIGHT_NODE_GENERAL) != HIGHLIGHT_NODE_GENERAL)
			return null;
		
		if((highlight & HIGHLIGHT_FORM) == HIGHLIGHT_FORM)
			return formColor;
		else if((highlight & HIGHLIGHT_POS) == HIGHLIGHT_POS)
			return posColor;
		
		return nodeHighlightColor;
	}
	
	public static Color getEdgeHighlightColor(int highlight) {
		if((highlight & HIGHLIGHT_EDGE_GENERAL) != HIGHLIGHT_EDGE_GENERAL)
			return null;

		if((highlight & HIGHLIGHT_EXISTENCE) == HIGHLIGHT_EXISTENCE)
			return existenceColor;
		else if((highlight & HIGHLIGHT_RELATION) == HIGHLIGHT_RELATION)
			return relationColor;
		else if((highlight & HIGHLIGHT_DIRECTION) ==  HIGHLIGHT_DIRECTION)
			return directionColor;
		else if((highlight & HIGHLIGHT_DISTANCE) == HIGHLIGHT_DISTANCE)
			return distanceColor;
		
		return edgeHighlightColor;
	}
	
	public static Color getHighlightColor(int highlight) {
		if(highlight==0)
			return null;
		
		if((highlight & HIGHLIGHT_FORM) == HIGHLIGHT_FORM)
			return formColor;
		else if((highlight & HIGHLIGHT_LEMMA) == HIGHLIGHT_LEMMA)
			return lemmaColor;
		else if((highlight & HIGHLIGHT_FEATURES) == HIGHLIGHT_FEATURES)
			return featuresColor;
		else if((highlight & HIGHLIGHT_POS) == HIGHLIGHT_POS)
			return posColor;
		else if((highlight & HIGHLIGHT_RELATION) == HIGHLIGHT_RELATION)
			return relationColor;
		else if((highlight & HIGHLIGHT_DIRECTION) ==  HIGHLIGHT_DIRECTION)
			return directionColor;
		else if((highlight & HIGHLIGHT_DISTANCE) == HIGHLIGHT_DISTANCE)
			return distanceColor;
		else if((highlight & HIGHLIGHT_NODE_GENERAL) == HIGHLIGHT_NODE_GENERAL)
			return nodeHighlightColor;
		else if((highlight & HIGHLIGHT_EDGE_GENERAL) == HIGHLIGHT_EDGE_GENERAL)
			return edgeHighlightColor;
		else 
			return nodeHighlightColor;
	}
	
	public static String getHighlightColorString(int highlight) {
		if(highlight==0)
			return null;
		
		if((highlight & HIGHLIGHT_FORM) == HIGHLIGHT_FORM)
			return formColorString;
		else if((highlight & HIGHLIGHT_LEMMA) == HIGHLIGHT_LEMMA)
			return lemmaColorString;
		else if((highlight & HIGHLIGHT_FEATURES) == HIGHLIGHT_FEATURES)
			return featuresColorString;
		else if((highlight & HIGHLIGHT_POS) == HIGHLIGHT_POS)
			return posColorString;
		else if((highlight & HIGHLIGHT_RELATION) == HIGHLIGHT_RELATION)
			return relationColorString;
		else if((highlight & HIGHLIGHT_DIRECTION) ==  HIGHLIGHT_DIRECTION)
			return directionColorString;
		else if((highlight & HIGHLIGHT_DISTANCE) == HIGHLIGHT_DISTANCE)
			return distanceColorString;
		else if((highlight & HIGHLIGHT_NODE_GENERAL) == HIGHLIGHT_NODE_GENERAL)
			return nodeHighlightColorString;
		else if((highlight & HIGHLIGHT_EDGE_GENERAL) == HIGHLIGHT_EDGE_GENERAL)
			return edgeHighlightColorString;
		else
			return nodeHighlightColorString;
	}
	
	public static int getConcurrentHighlightCount(int highlight) {
		if(highlight==0)
			return 0;
		
		int count = 0;
		
		if((highlight & HIGHLIGHT_FORM) == HIGHLIGHT_FORM
				&& (highlight & HIGHLIGHT_FORM_GROUP) != HIGHLIGHT_FORM_GROUP)
			count++;
		if((highlight & HIGHLIGHT_LEMMA) == HIGHLIGHT_LEMMA
				&& (highlight & HIGHLIGHT_LEMMA_GROUP) != HIGHLIGHT_LEMMA_GROUP)
			count++;
		if((highlight & HIGHLIGHT_FEATURES) == HIGHLIGHT_FEATURES
				&& (highlight & HIGHLIGHT_FEATURES_GROUP) != HIGHLIGHT_FEATURES_GROUP)
			count++;
		if((highlight & HIGHLIGHT_POS) == HIGHLIGHT_POS
				&& (highlight & HIGHLIGHT_POS_GROUP) != HIGHLIGHT_POS_GROUP)
			count++;
		if((highlight & HIGHLIGHT_RELATION) == HIGHLIGHT_RELATION
				&& (highlight & HIGHLIGHT_RELATION_GROUP) != HIGHLIGHT_RELATION_GROUP)
			count++;
		if((highlight & HIGHLIGHT_DIRECTION) == HIGHLIGHT_DIRECTION
				&& (highlight & HIGHLIGHT_DIRECTION_GROUP) != HIGHLIGHT_DIRECTION_GROUP)
			count++;
		if((highlight & HIGHLIGHT_DISTANCE) == HIGHLIGHT_DISTANCE
				&& (highlight & HIGHLIGHT_DISTANCE_GROUP) != HIGHLIGHT_DISTANCE_GROUP)
			count++;
		
		return count;
	}
	
	public static int getConcurrentCaseDiffCount(int highlight) {
		if(highlight==0)
			return 0;
		
		int count = 0;
		
		if((highlight & HIGHLIGHT_FORM_GROUP) == HIGHLIGHT_FORM_GROUP)
			count++;
		if((highlight & HIGHLIGHT_LEMMA_GROUP) == HIGHLIGHT_LEMMA_GROUP)
			count++;
		if((highlight & HIGHLIGHT_FEATURES_GROUP) == HIGHLIGHT_FEATURES_GROUP)
			count++;
		if((highlight & HIGHLIGHT_POS_GROUP) == HIGHLIGHT_POS_GROUP)
			count++;
		if((highlight & HIGHLIGHT_EXISTENCE_GROUP) == HIGHLIGHT_EXISTENCE_GROUP)
			count++;
		if((highlight & HIGHLIGHT_RELATION_GROUP) == HIGHLIGHT_RELATION_GROUP)
			count++;
		if((highlight & HIGHLIGHT_DIRECTION_GROUP) == HIGHLIGHT_DIRECTION_GROUP)
			count++;
		if((highlight & HIGHLIGHT_DISTANCE_GROUP) == HIGHLIGHT_DISTANCE_GROUP)
			count++;
		
		return count;
	}
	
	public static Grammar getDependencyGrammar() {
		return LanguageManager.getInstance().getGrammar(GRAMMAR_ID);
	}

	public static ContentType getDependencyContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(DependencyData.class);
	}
	
	public static String getForm(DependencyNodeData item) {
		return item.hasChildren() ? item.getForm2() : item.getForm();
	}
	
	public static String getForms(DependencyNodeData[] items) {
		if(items==null)
			return ""; //$NON-NLS-1$
		
		String[] buffer = new String[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = items[i].getForm();
		
		return Arrays.toString(buffer);
	}
	
	public static String getLemma(DependencyNodeData item) {
		return item.hasChildren() ? item.getLemma2() : item.getLemma();
	}
	
	public static String getLemmas(DependencyNodeData[] items) {
		if(items==null)
			return ""; //$NON-NLS-1$
		
		String[] buffer = new String[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = items[i].getLemma();
		
		return Arrays.toString(buffer);
	}
	
	public static String getFeatures(DependencyNodeData item) {
		return item.hasChildren() ? item.getFeatures2() : item.getFeatures();
	}
	
	public static String getFeatures(DependencyNodeData[] items) {
		if(items==null)
			return ""; //$NON-NLS-1$
		
		String[] buffer = new String[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = items[i].getFeatures();
		
		return Arrays.toString(buffer);
	}
	
	public static String getPos(DependencyNodeData item) {
		return item.hasChildren() ? item.getPos2() : item.getPos();
	}
	
	public static String getPoss(DependencyNodeData[] items) {
		if(items==null)
			return ""; //$NON-NLS-1$
		
		String[] buffer = new String[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = items[i].getPos();
		
		return Arrays.toString(buffer);
	}
	
	public static String getRelation(DependencyNodeData item) {
		//return item.hasChildren() ? item.getRelation2() : item.getRelation();
		return item.getRelation();
	}
	
	public static String getRelations(DependencyNodeData[] items) {
		if(items==null)
			return ""; //$NON-NLS-1$
		
		String[] buffer = new String[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = items[i].getRelation();
		
		return Arrays.toString(buffer);
	}
	
	public static String getHeads(DependencyNodeData[] items) {
		if(items==null)
			return ""; //$NON-NLS-1$
		
		String[] buffer = new String[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = LanguageUtils.getHeadLabel(items[i].getHead());
		
		return Arrays.toString(buffer);
	}
	
	public static String getIndices(DependencyNodeData[] items) {
		if(items==null)
			return ""; //$NON-NLS-1$
		
		int[] buffer = new int[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = items[i].getIndex()+1;
		
		return Arrays.toString(buffer);
	}
	
	public static String getDirection(DependencyNodeData item) {
		if(item.hasHead()) {
			return item.getHead()<item.getIndex() ? 
					LanguageUtils.DATA_RIGHT_LABEL : LanguageUtils.DATA_LEFT_LABEL;
		} else {
			return ""; //$NON-NLS-1$
		}
	}
	
	public static boolean isDependencyTreebank(Treebank treebank) {
		Exceptions.testNullArgument(treebank, "treebank"); //$NON-NLS-1$

		return ContentTypeRegistry.isCompatible(
				DependencyConstants.CONTENT_TYPE_ID, treebank.getContentType());
	}
	
	/**
	 * Algorithm by 
	 * <a href="http://ufal.mff.cuni.cz:8080/pub/files/havelka2005.pdf">Havelka 2005</a>
	 * 
	 * Naive approach used for now instead!
	 * <p>
	 * Note that all projectivity flags are assumed to be <b>not</b> set
	 * when passed to this method!
	 */
	public static void fillProjectivityFlags(short[] heads, long[] flags) {
		for(int i=0; i<heads.length; i++) {
			if(LanguageUtils.isProjective(i, heads)) {
				flags[i] |= LanguageConstants.FLAG_PROJECTIVE;
			}
		}
	}
	
	public static boolean checkBooleanConstraint(int constraint, boolean value) {
		switch (constraint) {
		case LanguageUtils.DATA_YES_VALUE:
			return value;
		case LanguageUtils.DATA_NO_VALUE:
			return !value;

		default:
			return true;
		}
	}
	
	public static DependencyTree getTreeFromGraph(mxGraph graph, Object root) {
		Exceptions.testNullArgument(graph, "graph"); //$NON-NLS-1$
		Exceptions.testNullArgument(root, "root"); //$NON-NLS-1$
		
		DependencyTree tree = new DependencyTree();
		Set<Object> visited = new HashSet<Object>();
		feedTreeFromGraph(tree, graph, root, visited);
		
		return tree;
	}
	
	private static void feedTreeFromGraph(DependencyTree node, mxGraph graph, 
			Object cell, Set<Object> visited) {
		if(visited.contains(cell))
			throw new IllegalArgumentException("Supplied graph is cyclic!"); //$NON-NLS-1$
		
		mxIGraphModel model = graph.getModel();
		visited.add(cell);
		node.setData(model.getValue(cell));
		
		for(Object edge : graph.getOutgoingEdges(cell)) {
			feedTreeFromGraph(node.append(null), graph, 
					model.getTerminal(edge, false), visited);
		}
	}
}