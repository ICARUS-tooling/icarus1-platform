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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;
import de.ims.icarus.util.mem.Primitive;
import de.ims.icarus.util.strings.Splitable;
import de.ims.icarus.util.strings.StringPrimitives;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class Span extends CorefMember implements Serializable, Comparable<Span>, Cloneable {

	private static final long serialVersionUID = 7035991391272077675L;

	@Primitive
	private int sentenceIndex = -1;
	@Primitive
	private int beginIndex;
	@Primitive
	private int endIndex;

	@Primitive
	private boolean root = false;

	@Link(cache=true)
	private Cluster cluster;

	private static final char TAB_CHAR = '\t';

	public static final String ROOT_ID = "ROOT"; //$NON-NLS-1$

	private static Span sharedRoot;

	public static Span getROOT() {
		if(sharedRoot==null) {
			sharedRoot = new Span();
			sharedRoot.setROOT(true);
		}
		return sharedRoot;
	}

	protected Span(int beginIndex, int endIndex) {
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
	}

	public Span(int beginIndex, int endIndex, int sentenceId) {
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.sentenceIndex = sentenceId;
	}

	public Span() {
		// no-op
	}

	public int getSentenceIndex() {
		return sentenceIndex;
	}

	public int getBeginIndex() {
		return beginIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setSentenceIndex(int sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}

	public void setBeginIndex(int beginIndex) {
		this.beginIndex = beginIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public int getClusterId() {
		return cluster==null ? LanguageConstants.DATA_UNDEFINED_VALUE : cluster.getId();
	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	@Override
	public Span clone() {
		Span clone = new Span(getBeginIndex(), getEndIndex());
		clone.setCluster(getCluster());
		clone.setSentenceIndex(getSentenceIndex());
		clone.setProperties(cloneProperties());
		clone.setROOT(isROOT());

		return clone;
	}

	public void set(int sentenceIndex, int beginIndex, int endIndex) {
		setSentenceIndex(sentenceIndex);
		setBeginIndex(beginIndex);
		setEndIndex(endIndex);
	}

	private static final char MINUS_CHAR = '-';

	@Override
	public String toString() {
		return asString(this);
	}

	public static String asString(Span span) {
		if(span.isROOT()) {
			return ROOT_ID;
		}
		return String.format("%d-%d-%d", span.sentenceIndex,  //$NON-NLS-1$
				span.beginIndex+1, span.endIndex+1);
	}

	public void appendTo(StringBuilder sb) {
		if(isROOT()) {
			sb.append(ROOT_ID);
		} else {
			sb.append(sentenceIndex).append(MINUS_CHAR).append(beginIndex+1).append(MINUS_CHAR).append(endIndex+1);
		}
	}

	private static Matcher matcher;

	private static Matcher getMatcher(CharSequence s) {
		if(matcher==null) {
			matcher = Pattern.compile("^(\\d+)-(\\d+)-(\\d+)").matcher(s); //$NON-NLS-1$
		} else {
			matcher.reset(s);
		}

		return matcher;
	}

	public static Span parse(Splitable s) {
		return parse(s, 0);
	}

	public static Span parse(Splitable s, int from) {
		if(s==null || s.isEmpty())
			throw new NullPointerException("Invalid string"); //$NON-NLS-1$

		if(s.startsWith("ROOT")) { //$NON-NLS-1$
			return getROOT();
		} else {
			int tabIndex = s.indexOf(TAB_CHAR, from);
			if(tabIndex==-1) {
				tabIndex = s.length();
			}

			Matcher m = getMatcher(s);
			if(!m.find())
				throw new IllegalArgumentException("Unrecognized format for span: "+s); //$NON-NLS-1$

			Span span = new Span();
			span.setSentenceIndex(StringPrimitives.parseInt(s, m.start(1), m.end(1)-1));
			span.setBeginIndex(StringPrimitives.parseInt(s, m.start(2), m.end(2)-1)-1);
			span.setEndIndex(StringPrimitives.parseInt(s, m.start(3), m.end(3)-1)-1);

			if(tabIndex<s.length()-1) {
				span.setProperties(CorefProperties.parse(s, tabIndex+1));
			}

			return span;
		}
	}

	public static Span parse(String s) {
		return parse(s, 0);
	}

	public static Span parse(String s, int from) {
		if(s==null || s.isEmpty())
			throw new NullPointerException("Invalid string"); //$NON-NLS-1$

		if(s.startsWith("ROOT")) { //$NON-NLS-1$
			return getROOT();
		} else {
			int tabIndex = s.indexOf(TAB_CHAR, from);
			if(tabIndex==-1) {
				tabIndex = s.length();
			}

			Matcher m = getMatcher(s);
			if(!m.find())
				throw new IllegalArgumentException("Unrecognized format for span: "+s); //$NON-NLS-1$

			Span span = new Span();
			span.setSentenceIndex(StringPrimitives.parseInt(s, m.start(1), m.end(1)-1));
			span.setBeginIndex(StringPrimitives.parseInt(s, m.start(2), m.end(2)-1)-1);
			span.setEndIndex(StringPrimitives.parseInt(s, m.start(3), m.end(3)-1)-1);

			if(tabIndex<s.length()-1) {
				span.setProperties(CorefProperties.parse(s, tabIndex+1));
			}

			return span;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Span) {
			Span other = (Span) obj;
			return sentenceIndex==other.sentenceIndex && beginIndex==other.beginIndex
					&& endIndex==other.endIndex;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (sentenceIndex+31) * (beginIndex+31) * (endIndex+31);
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Span other) {
		if(other==null) {
			return 1;
		}

		if(sentenceIndex!=other.sentenceIndex) {
			return sentenceIndex-other.sentenceIndex;
		}
		if(beginIndex!=other.beginIndex) {
			return beginIndex-other.beginIndex;
		}

		return getRange() - other.getRange();
	}

	public int getRange() {
		return endIndex - beginIndex + 1;
	}

	public void setROOT(boolean root) {
		this.root = root;
	}

	public boolean isROOT() {
		return root;
	}

	public int getHead() {
		// TODO evaluate need to decrement head value
		Object hp = getProperty("HEAD"); //$NON-NLS-1$
		return hp instanceof Integer ? ((int) hp)-1 : getEndIndex();
	}
}
