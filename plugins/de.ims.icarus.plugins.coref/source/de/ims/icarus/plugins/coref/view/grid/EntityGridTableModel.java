/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import de.ims.icarus.language.coref.Cluster;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanSet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EntityGridTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1954436908379655973L;
	
	protected CoreferenceDocumentData document;
	
	protected Map<String, EntityGridNode> nodes;
	
	protected EntityGridColumnModel columnModel = new EntityGridColumnModel();
	
	public EntityGridTableModel() {
		// no-op
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

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public EntityGridNode getValueAt(int rowIndex, int columnIndex) {
		if(nodes==null || nodes.isEmpty()) {
			return null;
		}
		return nodes.get(getKey(rowIndex, columnIndex));
	}
	
	public CoreferenceDocumentData getDocument() {
		return document;
	}

	public void setDocument(CoreferenceDocumentData document) {
		if(document==this.document) {
			return;
		}
		
		this.document = document;
		
		fireTableStructureChanged();
	}

	public void reload(CoreferenceAllocation allocation, CoreferenceAllocation goldAllocation, 
			boolean filterSingletons, boolean showGoldNodes) {
		
		if(nodes==null) {
			nodes = new HashMap<>();
		} else {
			nodes.clear();
		}
		columnModel.clear();
		
		if(document==null) {
			return;
		}
		
		// Fetch edge sets
		EdgeSet edgeSet = CoreferenceUtils.getEdgeSet(document, allocation);
		if(edgeSet==null) {
			return;
		}
		EdgeSet goldEdges = CoreferenceUtils.getGoldEdgeSet(document, goldAllocation);
		if(!showGoldNodes || edgeSet==goldEdges) {
			goldEdges = null;
		}
				
		// Fetch span sets
		SpanSet spanSet = CoreferenceUtils.getSpanSet(document, allocation);
		if(spanSet==null) {
			return;
		}
		SpanSet goldSpans = CoreferenceUtils.getGoldSpanSet(document, goldAllocation);
		if(spanSet==goldSpans) {
			goldSpans = null;
		}
		
		Set<Span> tmp = new HashSet<>();
		Set<Span> goldLookup = new HashSet<>();
		Set<Span> failLookup = new HashSet<>();
		
		feedSpans(tmp, edgeSet, filterSingletons);
		feedSpans(goldLookup, goldEdges, filterSingletons);
		
		// Create lookup for those spans that appear in the regular set
		// but not in the gold set
		failLookup.addAll(tmp);
		failLookup.removeAll(goldLookup);
		
		// Ensure gold lookup only contains those spans that
		// exist solely in the gold set (missing in the regular set)
		goldLookup.removeAll(tmp);
		
		// Finally merge all edges into one list
		List<Span> spans = new ArrayList<>(tmp.size()+goldLookup.size());
		spans.addAll(tmp);
		spans.addAll(goldLookup);
		
		Collections.sort(spans);

		// Maps a cluster id to the respective column
		Map<Integer, Integer> columnMap = new HashMap<>();
		List<Cluster> clusterList = new ArrayList<>();
		Cluster dummyCluster = new Cluster(-1);
		int currentRow = -1;
		Map<Integer, List<Span>> spanBuffer = new HashMap<>();
		Map<Integer, List<Short>> typeBuffer = new HashMap<>();
		
		for(Span span : spans) {			
			int row = span.getSentenceIndex();
			
			// Process buffered node data
			if(row!=currentRow) {
				processBuffer(currentRow, spanBuffer, typeBuffer);
				currentRow = row;
				clearBuffer(spanBuffer, typeBuffer);
			}
			
			// Get column id for span
			int clusterId = span.getClusterId();
			Integer column = columnMap.get(clusterId);
			if(column==null) {
				column = columnMap.size();
				columnMap.put(clusterId, column);
				
				Cluster cluster = span.getCluster();
				if(cluster==null) {
					cluster = dummyCluster;
				}
				
				clusterList.add(cluster);
			}
			
			// Get type of current span
			short type = 0;
			if(failLookup.contains(span)) {
				type = EntityGridNode.FALSE_PREDICTED_SPAN;
			} else if(goldLookup.contains(span)) {
				type = EntityGridNode.MISSING_GOLD_SPAN;
			}
			
			// Add span and type info to buffer
			fillBuffer(span, type, column, spanBuffer, typeBuffer);
		}
		
		// Process remaining data in  buffer
		processBuffer(currentRow, spanBuffer, typeBuffer);
		
		// Now reload table columns
		columnModel.reload(clusterList);
	}
	
	protected void feedSpans(Collection<Span> buffer, EdgeSet edgeSet, 
			boolean filterSingletons) {
		if(edgeSet==null) {
			return;
		}
		Collection<Edge> edges = edgeSet.getEdges();
		if(filterSingletons) {
			edges = CoreferenceUtils.removeSingletons(edges);
		}
		
		for(Edge edge : edges) {
			buffer.add(edge.getTarget());
		}
	}
	
	protected void clearBuffer(Map<Integer, List<Span>> spanBuffer,
			Map<Integer, List<Short>> typeBuffer) {
		/*for(List<Span> spanList : spanBuffer.values()) {
			spanList.clear();
		}
		for(List<Short> typeList : typeBuffer.values()) {
			typeList.clear();
		}*/
		spanBuffer.clear();
		typeBuffer.clear();
	}
	
	protected void fillBuffer(Span span, short type, int column, 
			Map<Integer, List<Span>> spanBuffer,
			Map<Integer, List<Short>> typeBuffer) {
		// Add span
		List<Span> spanList = spanBuffer.get(column);
		if(spanList==null) {
			spanList = new ArrayList<>();
			spanBuffer.put(column, spanList);
		}
		spanList.add(span);

		// Add type
		List<Short> typeList = typeBuffer.get(column);
		if(typeList==null) {
			typeList = new ArrayList<>();
			typeBuffer.put(column, typeList);
		}
		typeList.add(type);
	}
	
	protected void processBuffer(int row, Map<Integer, List<Span>> spanBuffer,
			Map<Integer, List<Short>> typeBuffer) {
		if(spanBuffer.isEmpty()) {
			return;
		}
		
		for(Entry<Integer, List<Span>> entry : spanBuffer.entrySet()) {
			int column = entry.getKey();
			List<Span> spanList = entry.getValue();
			List<Short> typeList = typeBuffer.get(column);
			CoreferenceData sentence = document.get(row);
			
			int size = spanList.size(); 
					
			Span[] spans = new Span[size];
			short[] types = new short[size];
			
			for(int i=0; i<size; i++) {
				spans[i] = spanList.get(i);
				types[i] = typeList.get(i);
			}
			
			String key = getKey(row, column);
			EntityGridNode node = new EntityGridNode(sentence, spans, types);
			
			nodes.put(key, node);
		}
	}
	
	public EntityGridColumnModel getColumnModel() {
		return columnModel;
	}

	public class EntityGridColumnModel extends DefaultTableColumnModel {

		private static final long serialVersionUID = -7530784522005750109L;
		
		protected List<Cluster> clusters;
		
		protected ClusterLabelType labelType = ClusterLabelType.FIRST;
		
		public void reload(List<Cluster> clusterList) {
			clusters = clusterList;
			
			tableColumns = new Vector<>();
			
			for(int i=0; i<clusters.size(); i++) {
				TableColumn column = new TableColumn(i);
				
				column.setIdentifier(clusters.get(i));
				
				addColumn(column);
			}
			
			reloadLabels();
			
			fireTableStructureChanged();
		}
		
		public void clear() {
			clusters = null;
		}
		
		public void reloadLabels() {
			int size = getColumnCount();
			if(size==0) {
				return;
			}
			
			for(int i=0; i<size; i++) {
				TableColumn column = getColumn(i);
				Cluster cluster = (Cluster) column.getIdentifier();
				
				column.setHeaderValue(labelType.getLabel(cluster, document));
			}
			
			fireColumnAdded(new TableColumnModelEvent(this, 0, size-1));
		}
	}
}
