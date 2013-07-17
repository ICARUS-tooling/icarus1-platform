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

import de.ims.icarus.language.Grammar;
import de.ims.icarus.language.LanguageUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultCoreferenceData extends CorefMember implements CoreferenceData {

	private static final long serialVersionUID = 1641469565583964051L;
	
	protected final String[] forms;
	
	protected CoreferenceDocumentData document;
	
	protected int sentenceIndex = -1;
	
	public DefaultCoreferenceData(CoreferenceDocumentData document, String[] forms) {
		if(forms==null)
			throw new IllegalArgumentException("Invalid forms array"); //$NON-NLS-1$
		
		this.forms = forms;
		setDocument(document);
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getForm(int)
	 */
	@Override
	public String getForm(int index) {
		return forms[index];
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return length()==0;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#length()
	 */
	@Override
	public int length() {
		return forms.length;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getSourceGrammar()
	 */
	@Override
	public Grammar getSourceGrammar() {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceData#getSpans()
	 */
	@Override
	public Span[] getSpans() {
		return sentenceIndex==-1 ? null
				: getDocument().getSpanSet().getSpans(sentenceIndex);
	}

	@Override
	public CoreferenceData clone() {
		DefaultCoreferenceData clone = new DefaultCoreferenceData(document, forms);
		clone.setProperties(cloneProperties());
		
		return clone;
	}
	
	@Override
	public String toString() {
		return LanguageUtils.combine(this);
	}

	/**
	 * @see de.ims.icarus.ui.helper.TextItem#getText()
	 */
	@Override
	public String getText() {
		return LanguageUtils.combine(this);
	}

	public CoreferenceDocumentData getDocument() {
		return document;
	}

	public void setDocument(CoreferenceDocumentData document) {
		if(forms!=null && forms.length==0) {
			return;
		}
		
		if(document==null)
			throw new IllegalArgumentException("Invalid document"); //$NON-NLS-1$
		
		this.document = document;
	}

	public int getSentenceIndex() {
		return sentenceIndex;
	}

	public void setSentenceIndex(int sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}
}
