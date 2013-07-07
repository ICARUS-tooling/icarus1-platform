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
import de.ims.icarus.util.CompactProperties;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultCoreferenceData implements CoreferenceData {

	private static final long serialVersionUID = 1641469565583964051L;
	
	protected String[] forms;
	protected CompactProperties properties;
	protected Span[] spans;
	
	public DefaultCoreferenceData(String[] forms, Span[] spans) {
		if(forms==null)
			throw new IllegalArgumentException("Invalid forms array"); //$NON-NLS-1$
		
		this.forms = forms;
		this.spans = spans;
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
	 * @see de.ims.icarus.language.coref.CoreferenceData#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}

	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new CompactProperties();
		}
		
		properties.put(key, value);
	}

	/**
	 * @see de.ims.icarus.language.coref.CoreferenceData#getSpans()
	 */
	@Override
	public Span[] getSpans() {
		return spans;
	}

	@Override
	public CoreferenceData clone() {
		DefaultCoreferenceData clone = new DefaultCoreferenceData(forms, spans);
		if(properties!=null) {
			clone.properties = properties.clone();
		}
		
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
}
