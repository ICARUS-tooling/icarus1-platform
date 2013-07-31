/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.dependency;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;


import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.Grammar;
import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.LanguageManager;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.dependency.annotation.DependencyAnnotation;
import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.search_tools.ConstraintContext;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchMode;
import de.ims.icarus.search_tools.SearchParameters;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.Orientation;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyUtils implements DependencyConstants {
	
	public static Options createOptionsFromConfig() {

		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		
		Options options = new Options();
		options.put(SearchParameters.SEARCH_CASESENSITIVE, 
				config.getBoolean("dependency.search.caseSensitive")); //$NON-NLS-1$
		options.put(SearchParameters.SEARCH_ORIENTATION, 
				"leftToRight".equals( //$NON-NLS-1$
						config.getString("dependency.search.direction"))? //$NON-NLS-1$
								Orientation.LEFT_TO_RIGHT 
								: Orientation.RIGHT_TO_LEFT);
		options.put(SearchParameters.SEARCH_RESULT_LIMIT,
				config.getInteger("dependency.search.maxResultCount")); //$NON-NLS-1$
		SearchMode mode = SearchMode.MATCHES;
		String modeString = config.getString("dependency.search.searchMode"); //$NON-NLS-1$
		
		// TODO use accurate mode check!
		if("occurrences".equals(modeString)) { //$NON-NLS-1$
			mode = SearchMode.HITS;
		} else if("exhaustiveSentences".equals(modeString)) { //$NON-NLS-1$
		}
		
		options.put(SearchParameters.SEARCH_MODE, mode);
		
		return options;
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
	
	public static Grammar getDependencyGrammar() {
		return LanguageManager.getInstance().getGrammar(GRAMMAR_ID);
	}

	public static ContentType getDependencyContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(DependencyData.class);
	}

	public static ContentType getDependencyNodeContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(DependencyNodeData.class);
	}

	public static ContentType getDependencyAnnotationType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(DependencyAnnotation.class);
	}
	
	public static String getForm(DependencyNodeData item) {
		return item.hasChildren() ? item.getForm2() : item.getForm();
	}
	
	public static ConstraintContext getDependencyContext() {
		return SearchManager.getInstance().getConstraintContext(getDependencyContentType());
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
	
	public static String getDistance(DependencyNodeData item) {
		if(item.hasHead()) {
			return String.valueOf(Math.abs(item.getHead()-item.getIndex()));
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
	 */
	public static void fillProjectivityFlags(short[] heads, long[] flags) {
		for(int i=0; i<heads.length; i++) {
			flags[i] &= ~LanguageConstants.FLAG_PROJECTIVE;
			if(LanguageUtils.isProjective(i, heads[i], heads)) {
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
