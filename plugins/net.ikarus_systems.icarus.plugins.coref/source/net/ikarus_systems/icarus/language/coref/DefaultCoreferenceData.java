/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.coref;

import net.ikarus_systems.icarus.language.Grammar;
import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.util.CompactProperties;

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
	 * @see net.ikarus_systems.icarus.language.SentenceData#getForm(int)
	 */
	@Override
	public String getForm(int index) {
		return forms[index];
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return length()==0;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#length()
	 */
	@Override
	public int length() {
		return forms.length;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#getSourceGrammar()
	 */
	@Override
	public Grammar getSourceGrammar() {
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.coref.CoreferenceData#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}

	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new CompactProperties();
		}
		
		properties.setProperty(key, value);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.coref.CoreferenceData#getSpans()
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
}
