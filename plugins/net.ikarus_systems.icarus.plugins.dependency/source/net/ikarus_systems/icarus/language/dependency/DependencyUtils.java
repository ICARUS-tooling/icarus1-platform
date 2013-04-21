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
import java.awt.Component;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.naming.directory.SearchResult;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import net.ikarus_systems.icarus.Core;
import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.language.MutableSentenceData;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.treebank.Treebank;
import net.ikarus_systems.icarus.util.Exceptions;
import net.ikarus_systems.icarus.util.Options;

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
	
	private static HighlightCombinationPolicy combinationPolicy;
	
	private static StyleContext styleContext = new StyleContext();
	
	private static String getHexColorString(Color color) {
		return Integer.toHexString((color.getRGB() & 0x00FFFFFF)
				| (color.getAlpha() << 24));
	}

	public static void loadConfig(ConfigRegistry config, Handle group) {
		formColor = new Color(config.getInteger(config.getChildHandle(group, "formHighlight")));
		posColor = new Color(config.getInteger(config.getChildHandle(group, "posHighlight")));
		lemmaColor = new Color(config.getInteger(config.getChildHandle(group, "lemmaHighlight")));
		featuresColor = new Color(config.getInteger(config.getChildHandle(group, "featuresHighlight")));
		existenceColor = new Color(config.getInteger(config.getChildHandle(group, "existenceHighlight")));
		relationColor = new Color(config.getInteger(config.getChildHandle(group, "relationHighlight")));
		directionColor = new Color(config.getInteger(config.getChildHandle(group, "directionHighlight")));
		distanceColor = new Color(config.getInteger(config.getChildHandle(group, "distanceHighlight")));
		rootColor = new Color(config.getInteger(config.getChildHandle(group, "rootHighlight")));

		nodeHighlightColor = new Color(config.getInteger(config.getChildHandle(group, "nodeHighlight")));
		edgeHighlightColor = new Color(config.getInteger(config.getChildHandle(group, "edgeHighlight")));

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
		
		// CASEDIFF 1
		style = styleContext.addStyle("caseDiff1", null);
		StyleConstants.setBackground(style, getCaseDiffColor(0));
		StyleConstants.setBold(style, true);
		
		// CASEDIFF 2
		style = styleContext.addStyle("caseDiff2", null);
		StyleConstants.setBackground(style, getCaseDiffColor(1));
		StyleConstants.setBold(style, true);
		
		// CASEDIFF 3
		style = styleContext.addStyle("caseDiff3", null);
		StyleConstants.setBackground(style, getCaseDiffColor(2));
		StyleConstants.setBold(style, true);

		
		// NODE
		style = styleContext.addStyle("node", null);
		StyleConstants.setForeground(style, nodeHighlightColor);
		StyleConstants.setItalic(style, true);
		
		// EDGE
		style = styleContext.addStyle("edge", null);
		StyleConstants.setForeground(style, edgeHighlightColor);
		StyleConstants.setItalic(style, true);

		// MULTIPLE HIGHLIGHTS
		style = styleContext.addStyle("multiple", null);
		StyleConstants.setIcon(style, GuiUtils.getIcon("multiple_annotation.gif"));
	}

	
	public static Options createOptionsFromConfig() {

		ConfigRegistry config = Core.core().getConfig();
		
		Options options = new Options();
		options.put(ExplorerConstants.SEARCH_USE_REGEX, 
				config.getBoolean("dependency.search.useRegex"));
		options.put(ExplorerConstants.SEARCH_CASESENSITIVE, 
				config.getBoolean("dependency.search.caseSensitive"));
		options.put(ExplorerConstants.SEARCH_ORIENTATION, 
				"leftToRight".equals(
						config.getString("dependency.search.direction"))?
								Orientation.LEFT_TO_RIGHT 
								: Orientation.RIGHT_TO_LEFT);
		options.put(ExplorerConstants.SEARCH_MAX_RESULT_COUNT,
				config.getInteger("dependency.search.maxResultCount"));
		boolean exhaustive = false;
		SearchMode mode = SearchMode.SENTENCES;
		String modeString = config.getString("dependency.search.searchMode");
		if("occurrences".equals(modeString)) {
			mode = SearchMode.OCCURENCES;
			exhaustive = true;
		} else if("exhaustiveSentences".equals(modeString)) {
			exhaustive = true;
		}
		
		options.put(ExplorerConstants.SEARCH_EXHAUSTIVE_SEARCH, exhaustive);
		options.put(ExplorerConstants.SEARCH_MODE, mode);
		
		return options;
	}

	public static Color getCaseDiffColor(int index) {
		return CaseDiffStorage.getColor(index);
	}

	public static String getCaseDiffColorString(int index) {
		return getHexColorString(CaseDiffStorage.getColor(index));
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
	 * @return the combinationPolicy
	 */
	public static HighlightCombinationPolicy getCombinationPolicy() {
		return combinationPolicy;
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
	
	public static String combine(SentenceData data) {
		StringBuilder sb = new StringBuilder(data.getForms().length*4);
		for(String token : data.getForms())
			sb.append(token).append(" ");
		
		return sb.toString().trim();
	}

	public static DependencyData createEmptySentenceData() {
		return new SimpleDependencyData();
	}
	
	public static String createExampleQuery() {
		return "[pos=<*>[form=the]][pos=NN]";
	}

	public static DependencyData createExampleSentenceData() {
		return new SimpleDependencyData(
				new String[] { "That", "thing", "I", "will", "never", "forget" }, 
				new String[] { "that", "thing", "i", "will", "never", "forget" },
				new String[] { "_", "_", "_", "_", "_", "_" },
				new String[] { "DT", "NN", "PRP", "MD", "RB", "VB" }, 
				new String[] {"NMOD", "ROOT", "SBJ", "NMOD", "TMP", "VC" }, 
				new int[] { 1, 5, 3, DATA_HEAD_ROOT, 3, 3 });
	}
	
	public static JLabel[] createCaseDiffCountLabels(int count) {
		JLabel[] caseDiffCountLabels = new JLabel[count];
		for(int i=0; i<caseDiffCountLabels.length; i++) {
			caseDiffCountLabels[i] = new JLabel();
			//GuiUtils.resizeComponent(caseDiffCountLabels[i], 75, 25);
		}
		
		return caseDiffCountLabels;
	}
	
	public static void refreshCaseDiffCountLabels(JLabel[] caseDiffCountLabels, 
			SearchResult result) {
		refreshCaseDiffCountLabels(caseDiffCountLabels, result, 0);
	}
	
	public static void refreshCaseDiffCountLabels(JLabel[] caseDiffCountLabels, 
			SearchResult result, int offset) {
		if(result==null) {
			for(JLabel label : caseDiffCountLabels)
				label.setText("");
		} else {			
			String label;						
			for(int i=0; i<caseDiffCountLabels.length; i++) {
				if(i<result.getDimension()) {
					label = String.format("'%s': %s", 
							result.getCaseDiffType(i),
							ExplorerUtils.formatDecimal(
									result.getCaseDiffLabelCount(i)));
					
					caseDiffCountLabels[i].setForeground(getCaseDiffColor(i+offset));
					caseDiffCountLabels[i].setText(label);
				} else {
					caseDiffCountLabels[i].setText("");
				}
			}
		}
	}
	
	public static final SentenceData dummySentenceData = 
		new SimpleDependencyData(
				new String[]{"Test"}, 
				new String[]{""},  
				new String[]{""},  
				new String[]{""}, 
				new String[]{""}, 
				new int[]{DependencyConstants.DATA_UNDEFINED_VALUE});
	
	/*public static String getExistenceLabel(int existence) {
		switch (existence) {
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_CASEDIFF_VALUE:
			return DATA_CASEDIFF_LABEL;
		default:
			return String.valueOf(true);
		}
	}
	
	public static int parseExistenceLabel(String label) {
		if(DATA_CASEDIFF_LABEL.equals(label))
			return DATA_CASEDIFF_VALUE;
		else if(DATA_UNDEFINED_LABEL.equals(label))
			return DATA_UNDEFINED_VALUE;
		else
			// TODO should we check if label equals "true"?
			return DATA_YES_VALUE;
	}*/
	
	public static String getBooleanLabel(int value) {
		switch (value) {
		case DATA_CASEDIFF_VALUE:
			return DATA_CASEDIFF_LABEL;
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_YES_VALUE:
			return String.valueOf(true);
		case DATA_NO_VALUE:
			return String.valueOf(false);
		}
		
		throw new IllegalArgumentException("Unknown value: "+value);
	}
	
	public static int parseBooleanLabel(String label) {
		if(DATA_CASEDIFF_LABEL.equals(label))
			return DATA_CASEDIFF_VALUE;
		else if(DATA_UNDEFINED_LABEL.equals(label))
			return DATA_UNDEFINED_VALUE;
		else if(Boolean.parseBoolean(label))
			return DATA_YES_VALUE;
		else
			return DATA_NO_VALUE;
	}

	public static String getHeadLabel(int head) {
		switch (head) {
		case DATA_HEAD_ROOT:
			return DATA_ROOT_LABEL;
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_CASEDIFF_VALUE:
			return DATA_CASEDIFF_LABEL;
		default:
			return String.valueOf(head + 1);
		}
	}

	public static String getLabel(int value) {
		switch (value) {
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_CASEDIFF_VALUE:
			return DATA_CASEDIFF_LABEL;
		default:
			return String.valueOf(value);
		}
	}

	public static String getDirectionLabel(int value) {
		switch (value) {
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_CASEDIFF_VALUE:
			return DATA_CASEDIFF_LABEL;
		case DATA_LEFT_VALUE:
			return DATA_LEFT_LABEL;
		case DATA_RIGHT_VALUE:
			return DATA_RIGHT_LABEL;
		}

		return null;
	}

	public static int parseHeadLabel(String head) {
		head = head.trim();
		if (DATA_ROOT_LABEL.equals(head))
			return DATA_HEAD_ROOT;
		else if (DATA_UNDEFINED_LABEL.equals(head))
			return DATA_UNDEFINED_VALUE;
		else if (DATA_CASEDIFF_LABEL.equals(head))
			return DATA_CASEDIFF_VALUE;
		else
			return Integer.parseInt(head) - 1;
	}

	public static int parseLabel(String value) {
		value = value.trim();
		if (value.isEmpty() || DATA_UNDEFINED_LABEL.equals(value))
			return DATA_UNDEFINED_VALUE;
		else if (DATA_CASEDIFF_LABEL.equals(value))
			return DATA_CASEDIFF_VALUE;
		else
			return Integer.parseInt(value);
	}

	public static int parseDirectionLabel(String direction) {
		direction = direction.trim();
		if (DATA_CASEDIFF_LABEL.equals(direction))
			return DATA_CASEDIFF_VALUE;
		else if (DATA_LEFT_LABEL.equals(direction))
			return DATA_LEFT_VALUE;
		else if (DATA_RIGHT_LABEL.equals(direction))
			return DATA_RIGHT_VALUE;
		else
			return DATA_UNDEFINED_VALUE;
	}

	public static String normalizeLabel(String value) {
		if(value==null)
			return DATA_UNDEFINED_LABEL;
		
		value = value.trim();
		if (value.isEmpty())
			return DATA_UNDEFINED_LABEL;
		else
			return value;
	}

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
		public int index = DATA_UNDEFINED_VALUE;
		public String form = "";
		public String lemma = "";
		public String features = "";
		public String pos = "";
		public String relation = "";
		public DataEntry head = null;
	}
	
	public static SimpleDependencyData parseData(String text) {
		Exceptions.testNullArgument(text, "text");

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

				entry.index = items.size();
				items.add(entry);
			}
				break;
			default:
				sb.append(c);
				break;
			}

		}

		if (trace.size() > 0)
			throw new IllegalStateException("Unclosed '[' in sentence description");

		int size = items.size();
		String[] forms = new String[size];
		String[] lemmas = new String[size];
		String[] features = new String[size];
		String[] poss = new String[size];
		String[] relations = new String[size];
		int[] heads = new int[size];
		
		for(int i=0; i<size; i++) {
			entry = items.get(i);
			forms[i] = entry.form;
			lemmas[i] = entry.lemma;
			features[i] = entry.features;
			poss[i] = entry.pos;
			relations[i] = entry.relation;
			heads[i] = entry.head==null ? DATA_HEAD_ROOT : entry.head.index;
		}
		
		return new SimpleDependencyData(forms, lemmas, features, poss, relations, heads);
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
					"Unknown field key: '%s' (value='%s')", String
							.valueOf(key), String.valueOf(value)));
	}
	
	public static String getHighlightDump(int highlight) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("bin=").append(Integer.toBinaryString(highlight));
		
		if(isHighlight(highlight)) {
			sb.append(" hl=");
			if((highlight & HIGHLIGHT_GENERAL) == HIGHLIGHT_GENERAL)
				sb.append("+");
			if((highlight & HIGHLIGHT_FORM) == HIGHLIGHT_FORM)
				sb.append("f");
			if((highlight & HIGHLIGHT_POS) == HIGHLIGHT_POS)
				sb.append("p");
			if((highlight & HIGHLIGHT_RELATION) == HIGHLIGHT_RELATION)
				sb.append("r");
			if((highlight & HIGHLIGHT_DIRECTION) ==  HIGHLIGHT_DIRECTION)
				sb.append("d");
			if((highlight & HIGHLIGHT_DISTANCE) == HIGHLIGHT_DISTANCE)
				sb.append("s");
			if((highlight & HIGHLIGHT_NODE_GENERAL) == HIGHLIGHT_NODE_GENERAL)
				sb.append("N");
			if((highlight & HIGHLIGHT_EDGE_GENERAL) == HIGHLIGHT_EDGE_GENERAL)
				sb.append("E");
	
			sb.append(" cd=");
			if((highlight & HIGHLIGHT_CASEDIFF) == HIGHLIGHT_CASEDIFF)
				sb.append("+");
			if((highlight & HIGHLIGHT_FORM_CASEDIFF) == HIGHLIGHT_FORM_CASEDIFF)
				sb.append("f");
			if((highlight & HIGHLIGHT_POS_CASEDIFF) == HIGHLIGHT_POS_CASEDIFF)
				sb.append("p");
			if((highlight & HIGHLIGHT_RELATION_CASEDIFF) == HIGHLIGHT_RELATION_CASEDIFF)
				sb.append("r");
			if((highlight & HIGHLIGHT_DIRECTION_CASEDIFF) ==  HIGHLIGHT_DIRECTION_CASEDIFF)
				sb.append("d");
			if((highlight & HIGHLIGHT_DISTANCE_CASEDIFF) == HIGHLIGHT_DISTANCE_CASEDIFF)
				sb.append("s");
		}
		
		return sb.toString();
	}
	
	public static boolean isCaseDiff(int highlight) {
		return (highlight & HIGHLIGHT_CASEDIFF) == HIGHLIGHT_CASEDIFF;
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
				&& (highlight & HIGHLIGHT_FORM_CASEDIFF) != HIGHLIGHT_FORM_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_LEMMA) == HIGHLIGHT_LEMMA
				&& (highlight & HIGHLIGHT_LEMMA_CASEDIFF) != HIGHLIGHT_LEMMA_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_FEATURES) == HIGHLIGHT_FEATURES
				&& (highlight & HIGHLIGHT_FEATURES_CASEDIFF) != HIGHLIGHT_FEATURES_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_POS) == HIGHLIGHT_POS
				&& (highlight & HIGHLIGHT_POS_CASEDIFF) != HIGHLIGHT_POS_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_RELATION) == HIGHLIGHT_RELATION
				&& (highlight & HIGHLIGHT_RELATION_CASEDIFF) != HIGHLIGHT_RELATION_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_DIRECTION) == HIGHLIGHT_DIRECTION
				&& (highlight & HIGHLIGHT_DIRECTION_CASEDIFF) != HIGHLIGHT_DIRECTION_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_DISTANCE) == HIGHLIGHT_DISTANCE
				&& (highlight & HIGHLIGHT_DISTANCE_CASEDIFF) != HIGHLIGHT_DISTANCE_CASEDIFF)
			count++;
		
		return count;
	}
	
	public static int getConcurrentCaseDiffCount(int highlight) {
		if(highlight==0)
			return 0;
		
		int count = 0;
		
		if((highlight & HIGHLIGHT_FORM_CASEDIFF) == HIGHLIGHT_FORM_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_LEMMA_CASEDIFF) == HIGHLIGHT_LEMMA_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_FEATURES_CASEDIFF) == HIGHLIGHT_FEATURES_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_POS_CASEDIFF) == HIGHLIGHT_POS_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_EXISTENCE_CASEDIFF) == HIGHLIGHT_EXISTENCE_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_RELATION_CASEDIFF) == HIGHLIGHT_RELATION_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_DIRECTION_CASEDIFF) == HIGHLIGHT_DIRECTION_CASEDIFF)
			count++;
		if((highlight & HIGHLIGHT_DISTANCE_CASEDIFF) == HIGHLIGHT_DISTANCE_CASEDIFF)
			count++;
		
		return count;
	}
	
	public static void propagateResultViewChanged(Component comp, Object source) {
		Exceptions.testNullArgument(comp, "comp");
		
		SearchView observer = (SearchView) SwingUtilities.getAncestorOfClass(
				SearchView.class, comp);
		
		if(observer!=null)
			observer.resultViewChanged(source);
	}
	
	public static void propagateSearchStateChanged(Component comp, Object source) {
		Exceptions.testNullArgument(comp, "comp");
		
		SearchView observer = (SearchView) SwingUtilities.getAncestorOfClass(
				SearchView.class, comp);
		
		if(observer!=null)
			observer.searchStateChanged(source);
	}
	
	public static String getForms(DependencyNodeData[] items) {
		if(items==null)
			return "";
		
		String[] buffer = new String[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = items[i].getSoleForm();
		
		return Arrays.toString(buffer);
	}
	
	public static String getLemmas(DependencyNodeData[] items) {
		if(items==null)
			return "";
		
		String[] buffer = new String[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = items[i].getSoleLemma();
		
		return Arrays.toString(buffer);
	}
	
	public static String getFeatures(DependencyNodeData[] items) {
		if(items==null)
			return "";
		
		String[] buffer = new String[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = items[i].getSoleFeatures();
		
		return Arrays.toString(buffer);
	}
	
	public static String getPoss(DependencyNodeData[] items) {
		if(items==null)
			return "";
		
		String[] buffer = new String[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = items[i].getSolePos();
		
		return Arrays.toString(buffer);
	}
	
	public static String getRelations(DependencyNodeData[] items) {
		if(items==null)
			return "";
		
		String[] buffer = new String[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = items[i].getSoleRelation();
		
		return Arrays.toString(buffer);
	}
	
	public static String getHeads(DependencyNodeData[] items) {
		if(items==null)
			return "";
		
		String[] buffer = new String[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = getHeadLabel(items[i].getHead());
		
		return Arrays.toString(buffer);
	}
	
	public static String getIndices(DependencyNodeData[] items) {
		if(items==null)
			return "";
		
		int[] buffer = new int[items.length];
		for(int i=0; i<items.length; i++)
			buffer[i] = items[i].getIndex()+1;
		
		return Arrays.toString(buffer);
	}
	
	public static boolean isDependencyTreebank(Treebank treebank) {
		Exceptions.testNullArgument(treebank, "treebank");
		
		return DependencyData.class.isAssignableFrom(treebank.getEntryClass());
	}
	
	public static boolean checkBooleanConstraint(int constraint, boolean value) {
		switch (constraint) {
		case DATA_YES_VALUE:
			return value;
		case DATA_NO_VALUE:
			return !value;

		default:
			return true;
		}
	}
	
	public static DependencyTree getTreeFromGraph(mxGraph graph, Object root) {
		Exceptions.testNullArgument(graph, "graph");
		Exceptions.testNullArgument(root, "root");
		
		DependencyTree tree = new DependencyTree();
		Set<Object> visited = new HashSet<Object>();
		feedTreeFromGraph(tree, graph, root, visited);
		
		return tree;
	}
	
	private static void feedTreeFromGraph(DependencyTree node, mxGraph graph, 
			Object cell, Set<Object> visited) {
		if(visited.contains(cell))
			throw new IllegalArgumentException("Supplied graph is cyclic!");
		
		mxIGraphModel model = graph.getModel();
		visited.add(cell);
		node.setData(model.getValue(cell));
		
		for(Object edge : graph.getOutgoingEdges(cell)) {
			feedTreeFromGraph(node.append(null), graph, 
					model.getTerminal(edge, false), visited);
		}
	}
}
