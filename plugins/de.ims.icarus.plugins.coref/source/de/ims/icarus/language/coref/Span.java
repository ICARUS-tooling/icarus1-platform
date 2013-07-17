/*
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


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Span extends CorefMember implements Serializable, Comparable<Span>, Cloneable {
	
	private static final long serialVersionUID = 7035991391272077675L;
	
	private int sentenceIndex = -1;
	private int beginIndex;
	private int endIndex;
	
	private int clusterId = LanguageConstants.DATA_UNDEFINED_VALUE;
	
	public static final String ROOT_ID = "ROOT"; //$NON-NLS-1$
	
	public static final Span ROOT = new Span(
			LanguageConstants.DATA_UNDEFINED_VALUE,
			LanguageConstants.DATA_UNDEFINED_VALUE,
			LanguageConstants.DATA_UNDEFINED_VALUE);

	protected Span(int beginIndex, int endIndex, int clusterId) {
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.clusterId = clusterId;
	}
	
	public Span(int beginIndex, int endIndex, int sentenceId, int clusterId) {
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.sentenceIndex = sentenceId;
		this.clusterId = clusterId;
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
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}
	
	@Override
	public Span clone() {
		Span clone = new Span(beginIndex, endIndex, clusterId);
		clone.setSentenceIndex(sentenceIndex);
		clone.setProperties(cloneProperties());
		
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
		if(span==ROOT) {
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
	
	private static Pattern pattern;
	
	private static Pattern getPattern() {
		if(pattern==null) {
			pattern = Pattern.compile("(\\d+)-(\\d+)-(\\d+)"); //$NON-NLS-1$
		}
		return pattern;
	}
	
	public static Span parse(String s) {
		if(s==null || s.isEmpty())
			throw new IllegalArgumentException("Invalid string"); //$NON-NLS-1$
		
		if(s.startsWith("ROOT")) { //$NON-NLS-1$
			return ROOT;
		} else {
			Matcher m = getPattern().matcher(s);
			if(!m.find())
				throw new IllegalArgumentException("Unrecognized format for span: "+s); //$NON-NLS-1$

			Span span = new Span();	
			span.setSentenceIndex(Integer.parseInt(m.group(1)));
			span.setBeginIndex(Integer.parseInt(m.group(2))-1);
			span.setEndIndex(Integer.parseInt(m.group(3))-1);
			
			return span;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Span) {
			Span other = (Span) obj;
			return sentenceIndex==other.sentenceIndex && beginIndex==other.beginIndex
					&& endIndex==other.endIndex && clusterId==other.clusterId;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (sentenceIndex+1) * (beginIndex+1) * (endIndex+1) * (clusterId+2);
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
		
		return (endIndex-beginIndex) - (other.endIndex-other.beginIndex);
	}
	
	public int getRange() {
		return endIndex - beginIndex;
	}
	
	public boolean isROOT() {
		return ROOT==this;
	}
}
