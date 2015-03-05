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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.language;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.ims.icarus.util.CompactProperties;
import de.ims.icarus.util.mem.Link;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement
public class BasicSentenceData<P extends CompactProperties> implements SentenceData, LanguageConstants {

	private static final long serialVersionUID = 6042849879442946254L;

	@XmlTransient
	protected P properties;

	@XmlTransient
	protected TMap<Key, Object> indexedProperties;

	@Link
	@XmlElement(name="form")
	protected String[] forms;

	public BasicSentenceData(String...forms) {
		if(forms==null)
			throw new NullPointerException("Invalid forms array"); //$NON-NLS-1$

		this.forms = forms;
	}

	public BasicSentenceData(SentenceData source) {
		this.forms = LanguageUtils.getForms(source);
	}

	public BasicSentenceData() {
		//no-op
	}

	public void setForms(String[] forms) {
		if(this.forms!=null)
			throw new IllegalStateException("Form tokens already set");

		this.forms = forms;
	}

	public P getProperties() {
		if(properties==null) {
			properties = createProperties();
		}

		return properties;
	}

	@SuppressWarnings("unchecked")
	protected P createProperties() {
		return (P) new CompactProperties();
	}

	public Object getProperty(String key) {
		switch (key) {
		case SIZE_KEY:
			return length();

		case INDEX_KEY:
			return getIndex();

		default:
			return properties==null ? null : properties.get(key);
		}
	}

	public void setProperty(String key, Object value) {
		getProperties().put(key, value);
	}

	public void setProperties(P properties) {
		this.properties = properties;
	}

	/**
	 * @see de.ims.icarus.ui.text.TextItem#getText()
	 */
	@Override
	public String getText() {
		return LanguageUtils.combine(this);
	}

	@Override
	public String toString() {
		return getText();
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getIndex()
	 */
	@Override
	public int getIndex() {
		return -1;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getForm(int)
	 */
	@Override
	public String getForm(int index) {
		return forms==null ? null : forms[index];
	}

	private static final Key sharedKey = new Key();

	protected final Object getIndexedProperty(int index, String key) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$

		if(indexedProperties==null) {
			return null;
		}

		//FIXME maybe synchronize?

		sharedKey.index = index;
		sharedKey.key = key;

		try {
			return indexedProperties.get(sharedKey);
		} finally {
			sharedKey.index = -1;
			sharedKey.key = null;
		}
	}

	public void setProperty(int index, String key, Object value) {
		Key newKey = new Key(key, index);

		if(indexedProperties==null) {
			indexedProperties = new THashMap<>();
		}

		indexedProperties.put(newKey, value);
	}

	/**
	 * @see de.ims.icarus.language.coref.DefaultCoreferenceData#getProperty(int, java.lang.String)
	 */
	@Override
	public Object getProperty(int index, String key) {
		switch (key) {
		case INDEX_KEY:
			return index;

		case SIZE_KEY:
		case LENGTH_KEY:
			return getForm(index).length();

		default:
			return getIndexedProperty(index, key);
		}
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
		return forms==null ? 0 : forms.length;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getSourceGrammar()
	 */
	@Override
	public Grammar getSourceGrammar() {
		return null;
	}

	@Override
	public SentenceData clone() {
		return new BasicSentenceData<P>(this);
	}

	@SuppressWarnings("unchecked")
	protected P cloneProperties() {
		return (P) (properties==null ? null : properties.clone());
	}

	protected static class Key {
		public String key;
		public int index;

		public Key() {
			// no-op
		}

		public Key(String key, int index) {
			this.key = key;
			this.index = index;
		}

		public Key(Key source) {
			key = source.key;
			index = source.index;
		}

		@Override
		public Key clone() {
			return new Key(this);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return key.hashCode() * (1+index);
		}
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Key) {
				Key other = (Key) obj;
				return index==other.index && key.equals(other.key);
			}
			return false;
		}
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Key:"+key+"["+index+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
}
