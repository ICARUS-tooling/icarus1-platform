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

import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.Span;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EntityGridNode {
	
	public static final short FALSE_PREDICTED_SPAN = 1;
	public static final short MISSING_GOLD_SPAN = 2; 
	
	private final CoreferenceData sentence;
	private final Span[] spans;
	private final short[] types;

	public EntityGridNode(CoreferenceData sentence, Span[] spans, short[] types) {
		if(sentence==null)
			throw new IllegalArgumentException("Invalid sentence"); //$NON-NLS-1$
		if(spans==null)
			throw new IllegalArgumentException("Invalid spans array"); //$NON-NLS-1$
		if(types==null)
			throw new IllegalArgumentException("Invalid types array"); //$NON-NLS-1$
		if(spans.length!=types.length)
			throw new IllegalArgumentException("Size mismatch between spans and types"); //$NON-NLS-1$
		
		this.sentence = sentence;
		this.spans = spans;
		this.types = types;
	}
	
	public CoreferenceData getSentence() {
		return sentence;
	}

	public int getSpanCount() {
		return spans.length;
	}
	
	public Span getSpan(int index) {
		return spans[index];
	}
	
	public short getType(int index) {
		return types[index];
	}
	
	public boolean isFalsePredictedSpan(int index) {
		return types[index]==FALSE_PREDICTED_SPAN;
	}
	
	public boolean isMissingGoldSpan(int index) {
		return types[index]==MISSING_GOLD_SPAN;
	}
	
	public boolean hasFalsePredictedSpan() {
		for(short type : types) {
			if(type==FALSE_PREDICTED_SPAN) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasMissingGoldSpan() {
		for(short type : types) {
			if(type==MISSING_GOLD_SPAN) {
				return true;
			}
		}
		return false;
	}
}
