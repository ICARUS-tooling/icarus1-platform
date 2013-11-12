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
package de.ims.icarus.plugins.coref.view.ea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.DefaultCoreferenceData;
import de.ims.icarus.language.coref.DefaultCoreferenceDocumentData;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanSet;
import de.ims.icarus.util.ClassUtils;
import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.Counter;
import de.ims.icarus.util.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultErrorAnalysisTableModel extends AbstractErrorAnalysisTableModel {

	private static final long serialVersionUID = -2382086379114481950L;
	
	@SuppressWarnings({ "nls", "unused" })
	public static void main(String[] args) throws Exception {
		
		CoreferenceDocumentSet documentSet = new CoreferenceDocumentSet();
		
		DefaultCoreferenceDocumentData document = 
				(DefaultCoreferenceDocumentData) documentSet.newDocument("test");

		DefaultCoreferenceData sentence = document.newData(
				new String[]{"A","B","C","A","D","C","B"});
		
		DefaultErrorAnalysisTableModel model = new DefaultErrorAnalysisTableModel();
		model.setDocuments(CollectionUtils.asSet((CoreferenceDocumentData)document));
		
		// False negatives
		model.test("Singleton 1st (fn)", null, null, "N0", "R-0");
		model.test("Singleton 2nd (fn)", null, null, "N0 N3", "R-0 R-1");
		model.test("Starts 1st (fn)", null, null, "N0 N1", "R-0 0-1");
		model.test("Starts 2nd (fn)", null, null, "N0 N1 N2", "R-1 1-2");
		
		// Correct matches
		model.test("Singleton 1st (c)",	"N0", "R-0", "N0", "R-0");
		model.test("Singleton 2nd (c)", "N0 N3", "R-0 R-1", "N0 N3", "R-0 R-1");
		model.test("Starts 1st (c)", "N0 N1", "R-0 0-1", "N0 N1", "R-0 0-1");
		model.test("Starts 2nd (c)", "N0 N3 N4", "R-0 R-1 1-2", "N0 N3 N4", "R-0 R-1 1-2");
		model.test("Anaphor 1st (c)", "N2 N3", "R-0 0-1", "N2 N3", "R-0 0-1");
		model.test("Anaphor 2nd (c)", "N2 N3 N5", "R-0 R-1 1-2", "N2 N3 N5", "R-0 R-1 1-2");
		
		// False positives
		model.test("Singleton 1st (fp)", "N0", "R-0", null, null);		
		model.test("Singleton 2nd (fp)", "N0 N3", "R-0 R-1", null, null);		
		model.test("Starts 1st (fp)", "N0 N1", "R-0 0-1", null, null);		
		model.test("Starts 2nd (fp)", "N0 N1 N2", "R-1 1-2", null, null);
		
		// Mismatches
		model.test("Singleton 1st (err)", "N0 N1", "R-0 0-1", "N0 N1", "R-0 R-1");
		model.test("Singleton 2nd (err)", "N0 N3", "R-0 0-1", "N0 N3", "R-0 R-1");
		model.test("Starts 1st (err)", "N0", "R-0", "N0 N1", "R-0 0-1");
		model.test("Starts 2nd (err)", "N0 N3", "R-0 R-1", "N0 N3", "R-0 0-1");
	}
	
	private void test(String info, String spans, String edges,
			String goldSpans, String goldEdges) {
		System.out.println(info);
		setAllocation(genAlloc(spans, edges));
		setGoldAllocation(genAlloc(goldSpans, goldEdges));
		analyzeDocuments();
		System.out.println(dump());
	}
	
	@SuppressWarnings("nls")
	private static CoreferenceAllocation genAlloc(String spanSpec, String edgeSpec) {
		EdgeSet edgeSet = new EdgeSet();
		SpanSet spanSet = new SpanSet();

		if(spanSpec!=null && !spanSpec.isEmpty()) {
			List<Span> spans = new ArrayList<>();
			for(String item : spanSpec.split(" ")) {
				int index = Integer.parseInt(item.substring(1));
				Span span = new Span(index, index, 0);
				switch (item.charAt(0)) {
				case 'N': span.setProperty("Type", "Name"); break;
				case 'C': span.setProperty("Type", "Common"); break;
				case 'P': span.setProperty("Type", "Pronoun"); break;
				}
				spans.add(span);
			}
			spanSet.setSpans(0, spans.toArray(new Span[0]));
		}
		
		if(edgeSpec!=null && !edgeSpec.isEmpty()) {
			for(String item : edgeSpec.split(" ")) {
				String[] indices = item.split("-");
				Span source = "R".equals(indices[0]) ? 
						Span.getROOT() : spanSet.get(Integer.parseInt(indices[0]));
				Span target = spanSet.get(Integer.parseInt(indices[1]));
				edgeSet.add(new Edge(source, target));
			}
		}
		
		CoreferenceAllocation result = new CoreferenceAllocation();
		result.setEdgeSet("test", edgeSet);
		result.setSpanSet("test", spanSet);
		
		return result;
	}
	
	protected ResultContainer[] containers;

	public DefaultErrorAnalysisTableModel() {
		setRowHeaders("Singleton", "Starts Entity", "Anaphoric"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return containers==null ? 0 : containers.length;
	}
	
	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(containers==null) {
			return null;
		}
		
		ResultContainer container = containers[columnIndex];
		double count = container.getCount(rowIndex);
		
		if(count<=0) {
			return null;
		}
		
		double predicted = container.getPredicted(rowIndex);
		double recall = 100d * predicted/count;
		
//		System.out.printf("type=%s row=%d count=%d pred=%1.0f recall=%1.02f\n",
//				container.getLabel(),rowIndex,count,predicted,recall);
		
		return String.format("%3.02f%% %s", recall,  //$NON-NLS-1$
				StringUtil.formatShortenedDecimal(count));
		
//		return String.format("%3.02f%% %s/%s", recall,  //$NON-NLS-1$
//				StringUtil.formatShortenedDecimal(predicted), 
//				StringUtil.formatShortenedDecimal(count));
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return containers==null ? null : containers[column].getLabel();
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	/**
	 * @see de.ims.icarus.plugins.coref.view.ea.AbstractErrorAnalysisTableModel#analyzeDocument()
	 */
	@Override
	protected void analyzeDocuments() {
		
		Set<CoreferenceDocumentData> documents = getDocuments();

		ResultContainer nom1 = new ResultContainer("Nominal", true); //$NON-NLS-1$
		ResultContainer nom2 = new ResultContainer("Nominal", false); //$NON-NLS-1$
		ResultContainer prop1 = new ResultContainer("Proper", true); //$NON-NLS-1$
		ResultContainer prop2 = new ResultContainer("Proper", false); //$NON-NLS-1$
		ResultContainer pron1 = new ResultContainer("Pronoun", true); //$NON-NLS-1$
		ResultContainer pron2 = new ResultContainer("Pronoun", false); //$NON-NLS-1$
		
		for(CoreferenceDocumentData document : documents) {
			
			String documentId = document.getId();
			
			SpanSet spans = allocation.getSpanSet(documentId);
			SpanSet goldSpans = goldAllocation.getSpanSet(documentId);
			if(spans==null || goldSpans==null) {
				return;
			}
			
			EdgeSet edges = allocation.getEdgeSet(documentId);
			EdgeSet goldEdges = goldAllocation.getEdgeSet(documentId);
			if(edges==null || goldEdges==null) {
				return;
			}
			
			Buffer goldBuffer = createBuffer(goldEdges);
			Buffer buffer = createBuffer(edges);
			
			Set<Span> union = new HashSet<>();
			union.addAll(buffer.spans);
			union.addAll(goldBuffer.spans);
			
			Set<Span> nomLookup = new HashSet<>();
			Set<Span> propLookup = new HashSet<>();
			Set<Span> pronLookup = new HashSet<>();
			
			Map<String, Span> headLookup = new HashMap<>();
			
			for(Span span : union) {
				String type = (String)span.getProperty("Type"); //$NON-NLS-1$
				
				CoreferenceData sentence = document.get(span.getSentenceIndex());
				String head = sentence.getForm(span.getHead());
				
				Span introducer = headLookup.get(head);
				if(introducer==null || span.compareTo(introducer)<0) {
					headLookup.put(head, span);
				}
				
				switch (type) {
				case "Common": //$NON-NLS-1$
					propLookup.add(span);
					break;
	
				case "Pronoun": //$NON-NLS-1$
					nomLookup.add(span);
					break;
	
				case "Name": //$NON-NLS-1$
					pronLookup.add(span);
					break;
				}
			}
			Set<Span> introducesHead = new HashSet<>(headLookup.values());
			
			headLookup.clear();
			union.clear();
			
			processBuffer(buffer, goldBuffer, nomLookup, introducesHead, nom1, nom2);
			processBuffer(buffer, goldBuffer, propLookup, introducesHead, prop1, prop2);
			processBuffer(buffer, goldBuffer, pronLookup, introducesHead, pron1, pron2);
		}
		
		containers = new ResultContainer[]{
				nom1,
				nom2,
				prop1,
				prop2,
				pron1,
				pron2
		};
	}
	
	protected Buffer createBuffer(EdgeSet edgeSet) {
		Buffer buffer = new Buffer();
		
		for(Edge edge : edgeSet.getEdges()) {
			Span source = edge.getSource();
			Span target = edge.getTarget();
			
			buffer.spans.add(target);
			
			if(edge.getSource().isROOT()) {
				// For now treat every starting span as singleton
				buffer.singletons.add(target);
			} else {
				// Increment child counter for source span
				buffer.childCounters.increment(source);
				// Map antecedent
				buffer.antecedents.put(target, source);
				
				// Map cluster root
				Span root = buffer.antecedents.get(source);
				if(root==null) {
					root = source;
				}
				buffer.clusterRoots.put(target, root);
			}
		}
		
		// Remove from the singleton set all the spans that
		// have proven to be members of a coreference relation
		buffer.singletons.removeAll(buffer.antecedents.values());
		
		return buffer;
	}
	
	protected void processBuffer(Buffer buffer, Buffer goldBuffer, Set<Span> spans, 
			Set<Span> introducers, ResultContainer r1, ResultContainer r2) {
		
		for(Span span : spans) {
			ResultContainer r = introducers.contains(span) ? r1 : r2;
			
			int type = 0;
			boolean correct = true;
			
			if(!goldBuffer.spans.contains(span)) {
				// 'False negatives' are treated as correct! 
				type = getType(buffer, span);
			} else {
				type = getType(goldBuffer, span);
				
				Span goldRoot = goldBuffer.clusterRoots.get(span);
				Span predRoot = buffer.clusterRoots.get(span);
				
//				System.out.printf("span=%s goldRoot=%s predRoot=%s\n",
//						span, goldRoot, predRoot);
//				
//				correct = buffer.spans.contains(span)
//						&& ClassUtils.equals(goldRoot, predRoot)
//						&& buffer.singletons.contains(span)==goldBuffer.singletons.contains(span);
				
				switch (type) {
				case 0:
					correct = buffer.singletons.contains(span) && goldBuffer.singletons.contains(span);
					break;

				case 1:
					correct = goldRoot==null && predRoot==null
						&& buffer.childCounters.hasCount(span) && goldBuffer.childCounters.hasCount(span);
					break;

				case 2:
					correct = ClassUtils.equals(goldRoot, predRoot);
					break;
				}
				
				correct &= buffer.spans.contains(span);
			}
			
			switch (type) {
			case 0:
				r.singletons.add(span);
				if(correct) r.singletonsPred.add(span);
				break;

			case 1:
				r.starts.add(span);
				if(correct) r.startsPred.add(span);
				break;

			case 2:
				r.anaphora.add(span);
				if(correct) r.anaphoraPred.add(span);
				break;

			default:
				break;
			}
		}
	}
	
	protected int getType(Buffer buffer, Span span) {
		if(buffer.singletons.contains(span)) {
			return 0;
		} else if(buffer.clusterRoots.get(span)==null) {
			return 1;
		} else {
			return 2;
		}
	}
	
	protected String dump() {
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i<getRowCount(); i++) {
			for(int j=0; j<getColumnCount(); j++) {
				sb.append(getValueAt(i, j)).append("  "); //$NON-NLS-1$
			}
			sb.append('\n');
		}
		
		return sb.toString();
	}
	
	protected static class Buffer {
		/**
		 * Maps a span to its antecedent
		 */
		public Map<Span, Span> antecedents = new HashMap<>();
		/**
		 * Maps a span to its cluster root
		 */
		public Map<Span, Span> clusterRoots = new HashMap<>();
		/**
		 * Counts the number of spans that are descendants
		 * of of a given span
		 */
		public Counter<Span> childCounters = new Counter<>();
		/**
		 * Contains all spans that are considered singletons
		 */
		public Set<Span> singletons = new HashSet<>();
		/**
		 * Lookup of all the spans in this buffer
		 */
		public Set<Span> spans = new HashSet<>();
	}
	
	protected static class ResultContainer {
		public Set<Span> singletons = new HashSet<>();
		public Set<Span> singletonsPred = new HashSet<>();
		public Set<Span> starts = new HashSet<>();
		public Set<Span> startsPred = new HashSet<>();
		public Set<Span> anaphora = new HashSet<>();
		public Set<Span> anaphoraPred = new HashSet<>();
		
		private final String type;
		private final boolean first;
		
		public ResultContainer(String type, boolean first) {
			this.type = type;
			this.first = first;
		}
		
		public String getLabel() {
			return type+" ("+(first ? "1nd" : "2nd+")+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		
		public int getCount(int row) {
			switch (row) {
			case 0:
				return singletons.size();
			case 1:
				return starts.size();
			case 2:
				return anaphora.size();

			default:
				throw new IllegalArgumentException("Invalid row: "+row); //$NON-NLS-1$
			}
		}
		
		public int getPredicted(int row) {
			switch (row) {
			case 0:
				return singletonsPred.size();
			case 1:
				return startsPred.size();
			case 2:
				return anaphoraPred.size();

			default:
				throw new IllegalArgumentException("Invalid row: "+row); //$NON-NLS-1$
			}
		}
	}
}
