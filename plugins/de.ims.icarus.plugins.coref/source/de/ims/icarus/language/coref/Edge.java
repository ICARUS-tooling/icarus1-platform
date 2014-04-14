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
package de.ims.icarus.language.coref;

import java.io.Serializable;

import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Reference;
import de.ims.icarus.util.mem.ReferenceType;
import de.ims.icarus.util.strings.Splitable;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class Edge extends CorefMember implements Cloneable, Serializable, Comparable<Edge> {

	private static final long serialVersionUID = 598886774121859964L;

	private static final char TAB_CHAR = '\t';
	private static final String DIRECTION_STRING = ">>"; //$NON-NLS-1$

	@Reference(ReferenceType.DOWNLINK)
	private Span source;
	@Reference(ReferenceType.DOWNLINK)
	private Span target;

	protected Edge() {
		// no-op
	}

	public Edge(Span source, Span target) {
		setSource(source);
		setTarget(target);
	}

	@Override
	public String toString() {
		if(source==null || target==null) {
			return "?"; //$NON-NLS-1$
		}

		StringBuilder sb = new StringBuilder();
		appendTo(sb);

		return sb.toString();
	}

	public void appendTo(StringBuilder sb) {
		source.appendTo(sb);
		sb.append(DIRECTION_STRING);
		target.appendTo(sb);
		sb.append(TAB_CHAR);
		if(properties!=null) {
			properties.appendTo(sb);
		}
	}

	public static Edge parse(Splitable s) {
		return parse(s, 0);
	}

	public static Edge parse(Splitable s, int from) {
		if(s==null || s.isEmpty())
			throw new NullPointerException("Invalid string"); //$NON-NLS-1$

		int tabIndex = s.indexOf(TAB_CHAR, from);
		if(tabIndex==-1) {
			tabIndex = s.length();
		}
		Splitable part = s.subSequence(from, tabIndex);
		if(part.split(DIRECTION_STRING)!=2)
			throw new IllegalArgumentException("Invalid edge format"); //$NON-NLS-1$

		Splitable s0 = part.getSplitCursor(0);
		Splitable s1 = part.getSplitCursor(1);

		Span source = Span.parse(s0);
		Span target = Span.parse(s1);

		s0.recycle();
		s1.recycle();

		part.recycle();

		Edge edge = new Edge(source, target);

		if(tabIndex<s.length()-1) {
			edge.setProperties(CorefProperties.parse(s, tabIndex+1));
		}

		return edge;
	}

	public static Edge parse(Splitable s, SpanSet spanSet) {
		return parse(s, 0, spanSet);
	}

	public static Edge parse(Splitable s, int from, SpanSet spanSet) {
		if(s==null || s.isEmpty())
			throw new NullPointerException("Invalid string"); //$NON-NLS-1$
		if(spanSet==null)
			throw new NullPointerException("Invalid span set"); //$NON-NLS-1$

		int tabIndex = s.indexOf(TAB_CHAR, from);
		if(tabIndex==-1) {
			tabIndex = s.length();
		}
		Splitable part = s.subSequence(from, tabIndex-1);
		if(part.split(DIRECTION_STRING)!=2)
			throw new IllegalArgumentException("Invalid edge format"); //$NON-NLS-1$

		Splitable s0 = part.getSplitCursor(0);
		Splitable s1 = part.getSplitCursor(1);

		Span source = spanSet.getSpan(s0.toString());
		Span target = spanSet.getSpan(s1.toString());

		s0.recycle();
		s1.recycle();

		part.recycle();

		Edge edge = new Edge(source, target);

		if(tabIndex<s.length()-1) {
			edge.setProperties(CorefProperties.parse(s, tabIndex+1));
		}

		return edge;
	}

	@Override
	public Edge clone() {
		Edge clone = new Edge(getSource(), getTarget());
		clone.setProperties(cloneProperties());

		return clone;
	}

	@Override
	public int hashCode() {
		return source.hashCode() * target.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Edge) {
			Edge other = (Edge)obj;
			return source.equals(other.getSource())
					&& target.equals(other.getTarget());
		}
		return false;
	}

	public Span getSource() {
		return source;
	}

	public Span getTarget() {
		return target;
	}

	public void setSource(Span source) {
		if(source==null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$

		this.source = source;
	}

	public void setTarget(Span target) {
		if(target==null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$
		this.target = target;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Edge other) {
		int result = source.compareTo(other.source);
		if(result==0) {
			result = target.compareTo(other.target);
		}
		return result;

	}
}
