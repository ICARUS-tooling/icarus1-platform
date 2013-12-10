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

import de.ims.icarus.language.coref.CorefErrorType;
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

	private CorefErrorType type;

	protected CorefNodeData() {
		// no-op
	}
	
	public CorefNodeData(Span span, CoreferenceData sentence) {
		super(span);
		setSentence(sentence);
	}
	
	public CorefNodeData(Span span, CoreferenceData sentence, CorefErrorType type) {
		super(span);
		setSentence(sentence);
	}
	
	public CorefNodeData(Span span, CoreferenceData sentence, CorefErrorType type, boolean gold) {
		super(span, gold);
		setSentence(sentence);
	}

	public CorefNodeData(Span data, CoreferenceData sentence, CorefErrorType type, boolean gold, long highlight) {
		super(data, gold, highlight);
		setSentence(sentence);
		setType(type);
	}

	public CorefErrorType getErrorType() {
		return type;
	}

	public void setType(CorefErrorType type) {
		this.type = type;
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
