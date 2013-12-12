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
package de.ims.icarus.language.coref;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorefComparison {

	private EdgeSet edgeSet;
	private EdgeSet goldSet;

	private Set<Edge> edges = Collections.emptySet();
	private Set<Edge> goldEdges = Collections.emptySet();

	private Map<Span, CorefErrorType> errors = Collections.emptyMap();;

	private Set<Span> spans = Collections.emptySet();
	private Set<Span> goldSpans = Collections.emptySet();

	private Map<Span, Span> rootMap = Collections.emptyMap();
	private Map<Span, Span> goldRootMap = Collections.emptyMap();

	public CorefComparison() {
		// no-op
	}

	/**
	 * @return the edgeSet
	 */
	public EdgeSet getEdgeSet() {
		return edgeSet;
	}

	/**
	 * @return the goldSet
	 */
	public EdgeSet getGoldSet() {
		return goldSet;
	}

	/**
	 * @return the spans
	 */
	public Set<Span> getSpans() {
		return spans;
	}

	public CorefErrorType getErrorType(Span span) {
		return errors==null ? null : errors.get(span);
	}

	/**
	 * @return the goldSpans
	 */
	public Set<Span> getGoldSpans() {
		return goldSpans;
	}

	public boolean isGold(Span span) {
		return goldSpans==null ? false : goldSpans.contains(span);
	}

	public boolean isPredicted(Span span) {
		return spans==null ? false : spans.contains(span);
	}

	/**
	 * @param edgeSet the edgeSet to set
	 */
	void setEdgeSet(EdgeSet edgeSet) {
		this.edgeSet = edgeSet;
	}

	/**
	 * @param goldSet the goldSet to set
	 */
	void setGoldSet(EdgeSet goldSet) {
		this.goldSet = goldSet;
	}

	/**
	 * @param errors the errors to set
	 */
	void setErrors(Map<Span, CorefErrorType> errors) {
		this.errors = errors;
	}

	/**
	 * @param spans the spans to set
	 */
	void setSpans(Set<Span> spans) {
		this.spans = spans;
	}

	/**
	 * @param goldSpans the goldSpans to set
	 */
	void setGoldSpans(Set<Span> goldSpans) {
		this.goldSpans = goldSpans;
	}

	/**
	 * @return the edges
	 */
	public Set<Edge> getEdges() {
		return edges;
	}

	/**
	 * @return the goldEdges
	 */
	public Set<Edge> getGoldEdges() {
		return goldEdges;
	}

	public boolean isGoldEdge(Edge edge) {
		return goldEdges==null ? false : goldEdges.contains(edge);
	}

	public boolean isPredictedEdge(Edge edge) {
		return edges==null ? false : edges.contains(edge);
	}

	/**
	 * @param edges the edges to set
	 */
	void setEdges(Set<Edge> edges) {
		this.edges = edges;
	}

	/**
	 * @param goldEdges the goldEdges to set
	 */
	void setGoldEdges(Set<Edge> goldEdges) {
		this.goldEdges = goldEdges;
	}

	/**
	 * @param rootMap the rootMap to set
	 */
	void setRootMap(Map<Span, Span> rootMap) {
		this.rootMap = rootMap;
	}

	public Span getRoot(Span span) {
		return rootMap==null ? null : rootMap.get(span);
	}

	/**
	 * @param goldRootMap the goldRootMap to set
	 */
	void setGoldRootMap(Map<Span, Span> goldRootMap) {
		this.goldRootMap = goldRootMap;
	}

	public Span getGoldRoot(Span span) {
		return goldRootMap==null ? null : goldRootMap.get(span);
	}
}
