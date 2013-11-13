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
package de.ims.icarus.plugins.coref.view.graph;

import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.plugins.jgraph.cells.GraphNode;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorefNodeData extends CorefCellData<Span> implements GraphNode {

	private static final long serialVersionUID = 857912220195344541L;
	
	protected CoreferenceData sentence;
	
	protected String label;

	protected CorefNodeData() {
		// no-op
	}
	
	public CorefNodeData(Span span, CoreferenceData sentence) {
		super(span);
		setSentence(sentence);
	}
	
	public CorefNodeData(Span span, CoreferenceData sentence, int nodeType) {
		super(span, nodeType);
		setSentence(sentence);
	}

	public CorefNodeData(Span data, CoreferenceData sentence, int type, long highlight) {
		super(data, type, highlight);
		setSentence(sentence);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CorefNodeData) {
			CorefNodeData other = (CorefNodeData) obj;
			return data.equals(other.getSpan()) && sentence.equals(other.getSentence());
		}
		return false;
	}

	public Span getSpan() {
		return data;
	}

	public CoreferenceData getSentence() {
		return sentence;
	}

	@Override
	public String createLabel() {
		if(data==null) {
			return "-"; //$NON-NLS-1$
		} else if(data.isROOT()) {
			return "-1\nGenericDocRoot"; //$NON-NLS-1$
		} else {
			StringBuilder sb = new StringBuilder();
			
			sb.append(data.getClusterId()).append('\n');
			
			int i0 = data.getBeginIndex();
			int i1 = data.getEndIndex();
			
			for(int i=i0; i<=i1; i++) {
				if(i>i0) {
					sb.append(' ');
				}
				sb.append(sentence.getForm(i));
			}
			
			sb.append('\n');
			data.appendTo(sb);
			
			return sb.toString();
		}
	}

	public void setSpan(Span span) {
		setData(span);
	}

	public void setSentence(CoreferenceData sentence) {
		if(sentence==null)
			throw new NullPointerException("Invalid sentence"); //$NON-NLS-1$
		
		this.sentence = sentence;
		label = null;
	}

	@Override
	public CorefNodeData clone() {
		return new CorefNodeData(getSpan(), getSentence());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return data.hashCode();
	}
}
