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
package de.ims.icarus.plugins.coref.view.grid;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.coref.Cluster;
import de.ims.icarus.language.coref.CorefComparison;
import de.ims.icarus.language.coref.CorefErrorType;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanCache;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotationManager;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentHighlighting;
import de.ims.icarus.util.Installable;
import de.ims.icarus.util.annotation.AnnotationController;
import de.ims.icarus.util.annotation.AnnotationManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EntityGridTableModel extends AbstractTableModel implements Installable {

	private static final long serialVersionUID = 1954436908379655973L;

	protected CoreferenceDocumentData document;

	protected Map<String, EntityGridNode> nodes;
	protected TIntObjectMap<ErrorSummary> columnSummaries = new TIntObjectHashMap<>();

	protected EntityGridColumnModel columnModel = new EntityGridColumnModel();

	protected boolean includeGoldMentions = true;
	protected boolean markFalseMentions = true;
	protected boolean filterSingletons = true;

	protected AnnotationController annotationController;

	protected SpanCache cache;

	public EntityGridTableModel() {
		// no-op
	}

	@Override
	public void install(Object target) {
		if(target instanceof AnnotationController) {
			annotationController = (AnnotationController) target;
		} else {
			annotationController = null;
		}
	}

	@Override
	public void uninstall(Object target) {
		annotationController = null;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return document==null ? 0 : document.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return columnModel.getColumnCount();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return EntityGridNode.class;
	}

	protected String getKey(int row, int column) {
		return row+"_"+column; //$NON-NLS-1$
	}

	public ErrorSummary getErrorSummary(int column) {
		ErrorSummary summary = columnSummaries.get(column);

		if(summary==null) {
			summary = new ErrorSummary();

			for(int row = 0; row<getRowCount(); row++) {
				EntityGridNode node = getValueAt(row, column);
				if(node==null) {
					continue;
				}

				for(int i=0; i<node.getSpanCount(); i++) {
					summary.add(node.getErrorType(i));
				}
			}

			columnSummaries.put(column, summary);
		}

		return summary;
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public EntityGridNode getValueAt(int rowIndex, int columnIndex) {
		if(nodes==null || nodes.isEmpty())
			return null;
		return nodes.get(getKey(rowIndex, columnIndex));
	}

	public CoreferenceDocumentData getDocument() {
		return document;
	}

	public void setDocument(CoreferenceDocumentData document) {
		if(document==this.document)
			return;

		this.document = document;

		fireTableStructureChanged();
	}

	protected CoreferenceDocumentAnnotationManager getAnnotationManager() {
		AnnotationManager annotationManager = annotationController==null ? null :
			annotationController.getAnnotationManager();

		if(annotationManager instanceof CoreferenceDocumentAnnotationManager)
			return (CoreferenceDocumentAnnotationManager) annotationManager;
		else
			return null;
	}

	public void reload(CoreferenceAllocation allocation, CoreferenceAllocation goldAllocation) {

		if(nodes==null) {
			nodes = new HashMap<>();
		} else {
			nodes.clear();
		}
		columnModel.clear();

		columnSummaries.clear();

		if(document==null)
			return;

		// Fetch edge sets
		EdgeSet edgeSet = CoreferenceUtils.getEdgeSet(document, allocation);
		if(edgeSet==null)
			return;
		EdgeSet goldSet = CoreferenceUtils.getGoldEdgeSet(document, goldAllocation);
		if(edgeSet==goldSet) {
			goldSet = null;
		}

		CorefComparison comparison = CoreferenceUtils.compare(edgeSet, goldSet, filterSingletons);

		if(cache==null) {
			cache = new SpanCache();
		} else {
			cache.clear();
		}
		cache.cacheEdges(comparison.getEdgeSet().getEdges());

		// Finally merge all edges into one list
		List<Span> spans = new ArrayList<>();
		spans.addAll(comparison.getSpans());
		if(includeGoldMentions && comparison.getGoldSpans()!=null) {
			spans.addAll(comparison.getGoldSpans());
		}

		Collections.sort(spans);

		// Maps a root span to the respective column
		Map<Span, Integer> columnMap = new HashMap<>();
		List<Cluster> clusterList = new ArrayList<>();
		Cluster dummyCluster = new Cluster(-1);
		int currentRow = -1;
		Map<Integer, List<Span>> spanBuffer = new HashMap<>();
		Map<Integer, List<CorefErrorType>> typeBuffer = new HashMap<>();

		for(Span span : spans) {
			int row = span.getSentenceIndex();

			// Process buffered node data
			if(row!=currentRow) {
				processBuffer(currentRow, spanBuffer, typeBuffer);
				currentRow = row;
				clearBuffer(spanBuffer, typeBuffer);
			}

			// Get column id for span
			Span root = comparison.isGold(span) ? comparison.getGoldRoot(span) : comparison.getRoot(span);
//			System.out.printf("root for %s: %s\n",span,root);
			if(root==null) {
				root = span;
			}
			Integer column = columnMap.get(root);
			if(column==null) {
				column = columnMap.size();
				columnMap.put(root, column);

				Cluster cluster = span.getCluster();
				if(cluster==null) {
					cluster = dummyCluster;
				}

				clusterList.add(cluster);
			}

			// Get type of current span
			CorefErrorType type = null;
			if(markFalseMentions) {
				type = comparison.getErrorType(span);
			}
			if(type==null) {
				type = CorefErrorType.TRUE_POSITIVE_MENTION;
			}

			// Add span and type info to buffer
			fillBuffer(span, type, column, spanBuffer, typeBuffer);
		}

		// Process remaining data in  buffer
		processBuffer(currentRow, spanBuffer, typeBuffer);

		columnSummaries.clear();

		// Now reload table columns
		columnModel.reload(clusterList);
	}

//	protected void feedSpans(Collection<Span> buffer, EdgeSet edgeSet,
//			boolean filterSingletons) {
//		if(edgeSet==null) {
//			return;
//		}
//		Collection<Edge> edges = edgeSet.getEdges();
//		if(filterSingletons) {
//			edges = CoreferenceUtils.removeSingletons(edges);
//		}
//
//		for(Edge edge : edges) {
//			buffer.add(edge.getTarget());
//		}
//	}

	protected void clearBuffer(Map<Integer, List<Span>> spanBuffer,
			Map<Integer, List<CorefErrorType>> typeBuffer) {
		/*for(List<Span> spanList : spanBuffer.values()) {
			spanList.clear();
		}
		for(List<Short> typeList : typeBuffer.values()) {
			typeList.clear();
		}*/
		spanBuffer.clear();
		typeBuffer.clear();
	}

	protected void fillBuffer(Span span, CorefErrorType type, int column,
			Map<Integer, List<Span>> spanBuffer,
			Map<Integer, List<CorefErrorType>> typeBuffer) {
		// Add span
		List<Span> spanList = spanBuffer.get(column);
		if(spanList==null) {
			spanList = new ArrayList<>();
			spanBuffer.put(column, spanList);
		}
		spanList.add(span);

		// Add type
		List<CorefErrorType> typeList = typeBuffer.get(column);
		if(typeList==null) {
			typeList = new ArrayList<>();
			typeBuffer.put(column, typeList);
		}
		typeList.add(type);
	}

	protected void processBuffer(int row, Map<Integer, List<Span>> spanBuffer,
			Map<Integer, List<CorefErrorType>> typeBuffer) {
		if(spanBuffer.isEmpty())
			return;

		for(Entry<Integer, List<Span>> entry : spanBuffer.entrySet()) {
			int column = entry.getKey();
			List<Span> spanList = entry.getValue();
			List<CorefErrorType> typeList = typeBuffer.get(column);
			CoreferenceData sentence = document.get(row);

			int size = spanList.size();

			Span[] spans = new Span[size];
			CorefErrorType[] types = new CorefErrorType[size];

			for(int i=0; i<size; i++) {
				spans[i] = spanList.get(i);
				types[i] = typeList.get(i);
			}

			String key = getKey(row, column);
			Color[] highlightColors = null;

			CoreferenceDocumentAnnotationManager annotationManager = getAnnotationManager();
			if(annotationManager!=null && annotationManager.hasAnnotation()) {
				highlightColors = new Color[size];
				for(int i=0; i<size; i++) {
					Span span = spans[i];
					int index = cache.getIndex(span);
					long highlight = annotationManager.getHighlight(index);
					if(!CoreferenceDocumentHighlighting.getInstance().isHighlighted(highlight)) {
						continue;
					}
					Color c = CoreferenceDocumentHighlighting.getInstance().getGroupColor(highlight);
					if(c==null) {
						c = CoreferenceDocumentHighlighting.getInstance().getHighlightColor(highlight);
					}

					highlightColors[i] = c;
				}
			}

			EntityGridNode node = new EntityGridNode(sentence, spans, types, highlightColors);

			nodes.put(key, node);
		}
	}

	public EntityGridColumnModel getColumnModel() {
		return columnModel;
	}

	public boolean isIncludeGoldMentions() {
		return includeGoldMentions;
	}

	public boolean isMarkFalseMentions() {
		return markFalseMentions;
	}

	public boolean isFilterSingletons() {
		return filterSingletons;
	}

	public void setIncludeGoldMentions(boolean showFalseNegatives) {
		this.includeGoldMentions = showFalseNegatives;
	}

	public void setMarkFalseMentions(boolean markFalsePositives) {
		this.markFalseMentions = markFalsePositives;
	}

	public void setFilterSingletons(boolean filterSingletons) {
		this.filterSingletons = filterSingletons;
	}

	public class EntityGridColumnModel extends DefaultTableColumnModel {

		private static final long serialVersionUID = -7530784522005750109L;

		protected List<Cluster> clusters;

		protected EntityGridTableHeaderRenderer headerRenderer;

		public void reload(List<Cluster> clusterList) {
			clusters = clusterList;

			tableColumns = new Vector<>();

			for(int i=0; i<clusters.size(); i++) {
				TableColumn column = new TableColumn(i);

				column.setIdentifier(clusters.get(i));
				column.setMinWidth(EntityGridPresenter.DEFAULT_CELL_WIDTH);
				column.setPreferredWidth(EntityGridPresenter.DEFAULT_CELL_WIDTH);
				//column.setMaxWidth(EntityGridPresenter.DEFAULT_CELL_WIDTH*3);

				addColumn(column);
			}

			reloadLabels();

			fireTableStructureChanged();
		}

		public void clear() {
			clusters = null;
		}

		public String getPrototypeLabel() {
			String prototypeLabel = null;

			for(TableColumn column : tableColumns) {
				String label = (String) column.getHeaderValue();
				if(label!=null && (prototypeLabel==null || label.length()>prototypeLabel.length())) {
					prototypeLabel = label;
				}
			}

			return prototypeLabel;
		}

		public void resetColumnSize() {
			int size = getColumnCount();
			if(size==0)
				return;
			for(int i=0; i<size; i++) {
				TableColumn column = getColumn(i);
				column.setPreferredWidth(EntityGridPresenter.DEFAULT_CELL_WIDTH);
				column.setWidth(EntityGridPresenter.DEFAULT_CELL_WIDTH);
			}
		}

		public void reloadLabels() {
			int size = getColumnCount();
			if(size==0)
				return;

			ClusterLabelType labelType = (ClusterLabelType) ConfigRegistry.getGlobalRegistry().getValue(
					"plugins.coref.appearance.grid.clusterLabelType"); //$NON-NLS-1$

			for(int i=0; i<size; i++) {
				TableColumn column = getColumn(i);
				Cluster cluster = (Cluster) column.getIdentifier();

				column.setHeaderValue(labelType.getLabel(cluster, document));
				column.setHeaderRenderer(getHeaderRenderer());
			}

			fireColumnAdded(new TableColumnModelEvent(this, 0, size-1));
		}

		public EntityGridTableHeaderRenderer getHeaderRenderer() {
			return headerRenderer;
		}

		public void setHeaderRenderer(EntityGridTableHeaderRenderer headerRenderer) {
			if(this.headerRenderer!=null && this.headerRenderer.equals(headerRenderer))
				return;

			this.headerRenderer = headerRenderer;
			reloadLabels();
		}
	}
}
