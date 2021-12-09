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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.search;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.DocumentData;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanSet;
import de.ims.icarus.plugins.coref.CorefConstants;
import de.ims.icarus.search_tools.tree.AbstractTargetTree;
import de.ims.icarus.search_tools.tree.CompactTree;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DocumentTargetTree extends AbstractTargetTree<DocumentData> implements CorefConstants {

	protected SpanSet spanSet;
	protected EdgeSet edgeSet;
	//protected Edge[] headMap;
	protected TObjectIntMap<Span> indexMap;
	protected TIntObjectMap<Edge> headMap;

	protected CompactTree tree;

	public DocumentTargetTree() {
		super();
	}

	@Override
	protected boolean supports(Object data) {
		return data instanceof DocumentData;
	}

	@Override
	public void close() {
		super.close();

		spanSet = null;
		edgeSet = null;
		headMap = null;
		indexMap = null;
	}

	@Override
	protected void prepare(Options options) {
		// Fetch span and edge information
		CoreferenceAllocation allocation = (CoreferenceAllocation) options.get("allocation"); //$NON-NLS-1$
		spanSet = CoreferenceUtils.getSpanSet(data, allocation);
		edgeSet = CoreferenceUtils.getEdgeSet(data, allocation);

		// Generate reverse lookup for span indices
		if(indexMap==null) {
			indexMap = new TObjectIntHashMap<>();
		} else {
			indexMap.clear();
		}

		int size = fetchSize();

		for(int i=0; i<size; i++) {
			indexMap.put(spanSet.get(i), i);
		}

		// Generate head lookup
//		if(headMap==null || headMap.length<size) {
//			headMap = new Edge[spanSet.size()];
//		} else {
//			Arrays.fill(headMap, 0, Math.max(0, size-1), null);
//		}
		if(headMap==null) {
			headMap = new TIntObjectHashMap<>();
		} else {
			headMap.clear();
		}

		if(edgeSet==null) {
			return;
		}

//		for(int i=0; i<edgeSet.size(); i++) {
//			Edge edge = edgeSet.get(i);
//			// TODO right now we ignore the virtual edge from the generic doc root!
//			if(!edge.getSource().isROOT()) {
//				headMap[indexMap.get(edge.getTarget())] = edge;
//			}
//		}

		for(int i=0; i<edgeSet.size(); i++) {
			Edge edge = edgeSet.get(i);
			// TODO right now we ignore the virtual edge from the generic doc root!
			if(edge.getSource().isROOT()) {
				continue;
			}

			int index = indexMap.get(edge.getTarget());

			headMap.put(index, edge);
		}
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.AbstractTargetTree#fetchSize()
	 */
	@Override
	protected int fetchSize() {
		return spanSet==null ? 0 : spanSet.size();
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.AbstractTargetTree#fetchHead(int)
	 */
	@Override
	protected int fetchHead(int index) {
		int head = LanguageConstants.DATA_HEAD_ROOT;
		Edge edge = headMap.get(index);

		if(edge!=null) {
			Span parent = edge.getSource();
			if(!parent.isROOT()) {
				head = indexMap.get(parent);
			}
		}

		return head;
	}

	// Coreference data access methods

	// SPAN METHODS

	public Span getSpan() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return spanSet.get(nodePointer);
	}

	public boolean isVirtual() {
		return getSpan().isVirtual();
	}

	public int getSentenceIndex() {
		return getSpan().getSentenceIndex();
	}

	public int getBeginIndex() {
		return getSpan().getBeginIndex();
	}

	public int getEndIndex() {
		return getSpan().getEndIndex();
	}

	public int getRange() {
		return getSpan().getRange();
	}

	public int getClusterId() {
		return getSpan().getClusterId();
	}

	public Object getSpanProperty(String key) {
		switch (key) {
		case INDEX_KEY:
			return indexMap.get(getSpan());

		default:
			return getSpan().getProperty(key);
		}
	}

	// EDGE METHODS

	public Edge getEdge() {
		/*if(edgePointer==-1)
		throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$*/
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		Edge edge = headMap.get(nodePointer);

		if(edge==null)
			throw new CorruptedStateException("Current node has no head edge"); //$NON-NLS-1$

		return edge;
	}

	public Object getEdgeProperty(String key) {
		return getEdge().getProperty(key);
	}

	@Override
	public int getDirection() {
		/*if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$*/
		if(nodePointer==-1)
			throw new CorruptedStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		Edge edge = getEdge();

		return edge.getTarget().compareTo(edge.getSource())>0 ?
				LanguageConstants.DATA_LEFT_VALUE : LanguageConstants.DATA_RIGHT_VALUE;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getDistance()
	 */
	@Override
	public int getDistance() {
		// TODO Auto-generated method stub
		return 0;
	}

	// DOCUMENT METHODS

	public DocumentData getDocument() {
		return data;
	}

	// SENTENCE METHODS

	public CoreferenceData getSentence() {
		int sentenceIndex = getSpan().getSentenceIndex();
		if(sentenceIndex==-1)
			throw new IllegalStateException("No valid sentence index available on current scope"); //$NON-NLS-1$

		return getDocument().get(sentenceIndex);
	}

	private StringBuilder buffer = new StringBuilder(100);

	public String getForms() {
		buffer.setLength(0);

		CoreferenceData sentence = getSentence();

		CoreferenceUtils.appendForms(buffer, sentence, getSpan());

		return buffer.toString();
	}

	public String getSentenceProperties(String key) {
		buffer.setLength(0);

		CoreferenceData sentence = getSentence();

		CoreferenceUtils.appendProperties(buffer, key, sentence, getSpan());

		return buffer.toString();
	}

	public Object getHeadProperty(String key) {
		CoreferenceData sentence = getSentence();
		Span span = getSpan();
		int head = span.getHead();
		return sentence.getProperty(head, key);
	}

	@Override
	public Object getProperty(String key) {
		return getSpanProperty(key);
	}
}
