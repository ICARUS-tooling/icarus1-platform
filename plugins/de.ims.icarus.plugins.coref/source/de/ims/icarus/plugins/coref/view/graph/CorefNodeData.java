/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.graph;

import java.io.Serializable;

import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.plugins.jgraph.cells.GraphNode;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorefNodeData implements Serializable, GraphNode {

	private static final long serialVersionUID = 857912220195344541L;
	
	protected Span span;
	protected CoreferenceData sentence;
	
	protected String label;

	protected CorefNodeData() {
		// no-op
	}
	
	public CorefNodeData(Span span, CoreferenceData sentence) {
		setSpan(span);
		setSentence(sentence);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CorefNodeData) {
			CorefNodeData other = (CorefNodeData) obj;
			return span.equals(other.getSpan()) && sentence.equals(other.getSentence());
		}
		return false;
	}

	@Override
	public String toString() {
			return getLabel();
	}

	public Span getSpan() {
		return span;
	}

	public CoreferenceData getSentence() {
		return sentence;
	}

	public String getLabel() {
		if(label==null) {
			if(span.isROOT()) {
				label = "-1\nGenericDocRoot"; //$NON-NLS-1$
			} else {
				StringBuilder sb = new StringBuilder();
				
				sb.append(span.getClusterId()+1).append('\n');
				
				int i0 = span.getBeginIndex();
				int i1 = span.getEndIndex();
				
				for(int i=i0; i<=i1; i++) {
					if(i>i0) {
						sb.append(' ');
					}
					sb.append(sentence.getForm(i));
				}
				
				sb.append('\n');
				span.appendTo(sb);
				
				label = sb.toString();
			}
		}
		return label;
	}

	public void setSpan(Span span) {
		if(span==null)
			throw new IllegalArgumentException("Invalid span"); //$NON-NLS-1$
		
		this.span = span;
		label = null;
	}

	public void setSentence(CoreferenceData sentence) {
		if(sentence==null)
			throw new IllegalArgumentException("Invalid sentence"); //$NON-NLS-1$
		
		this.sentence = sentence;
		label = null;
	}

	@Override
	public CorefNodeData clone() {
		return new CorefNodeData(getSpan(), getSentence());
	}
}
