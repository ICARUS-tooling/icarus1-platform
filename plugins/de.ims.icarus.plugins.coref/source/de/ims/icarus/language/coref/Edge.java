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
import java.util.regex.Pattern;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Edge extends CorefMember implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 598886774121859964L;
	
	private static final char TAB_CHAR = '\t';
	private static final String DIRECTION_STRING = ">>"; //$NON-NLS-1$
	
	private Span source;
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
	
	private static final Pattern DIR = Pattern.compile(DIRECTION_STRING);
	
	public static Edge parse(String s) {
		if(s==null || s.isEmpty())
			throw new IllegalArgumentException("Invalid string"); //$NON-NLS-1$
		
		int tabIndex = s.indexOf(TAB_CHAR);
		String[] spans = DIR.split(s.substring(0, tabIndex));
		
		Span source = Span.parse(spans[0]);
		Span target = Span.parse(spans[1]);
		
		Edge edge = new Edge(source, target);
		
		if(tabIndex<s.length()-1) {
			edge.setProperties(CorefProperties.parse(s.substring(tabIndex+1)));
		}
		
		return edge;
	}
	public static Edge parse(String s, SpanSet spanSet) {
		if(s==null || s.isEmpty())
			throw new IllegalArgumentException("Invalid string"); //$NON-NLS-1$
		if(spanSet==null)
			throw new IllegalArgumentException("Invalid span set"); //$NON-NLS-1$
		
		int tabIndex = s.indexOf(TAB_CHAR);
		String[] spans = DIR.split(s.substring(0, tabIndex));
		
		Span source = spanSet.getSpan(spans[0]);
		Span target = spanSet.getSpan(spans[1]);
		
		Edge edge = new Edge(source, target);
		
		if(tabIndex<s.length()-1) {
			edge.setProperties(CorefProperties.parse(s.substring(tabIndex+1)));
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
			throw new IllegalArgumentException("Invalid source"); //$NON-NLS-1$
		
		this.source = source;
	}

	public void setTarget(Span target) {
		if(source==null)
			throw new IllegalArgumentException("Invalid target"); //$NON-NLS-1$
		this.target = target;
	}
}
