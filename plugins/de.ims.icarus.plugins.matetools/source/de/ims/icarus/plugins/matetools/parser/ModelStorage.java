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
package de.ims.icarus.plugins.matetools.parser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="modelStorage")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModelStorage {
	
	@XmlAttribute
	private String language;
	
	@XmlElement(name="parser", required=false)
	private String parserModelPath;
	
	@XmlElement(name="tagger", required=false)
	private String taggerModelPath;
	
	@XmlElement(name="lemmatizer", required=false)
	private String lemmatizerModelPath;
	
	@XmlElement(name="morphTagger", required=false)
	private String morphTaggerModelPath;

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getParserModelPath() {
		return parserModelPath;
	}

	public void setParserModelPath(String parserModelPath) {
		this.parserModelPath = parserModelPath;
	}

	public String getTaggerModelPath() {
		return taggerModelPath;
	}

	public void setTaggerModelPath(String taggerModelPath) {
		this.taggerModelPath = taggerModelPath;
	}

	public String getLemmatizerModelPath() {
		return lemmatizerModelPath;
	}

	public void setLemmatizerModelPath(String lemmatizerModelPath) {
		this.lemmatizerModelPath = lemmatizerModelPath;
	}

	public String getMorphTaggerModelPath() {
		return morphTaggerModelPath;
	}

	public void setMorphTaggerModelPath(String morphTaggerModelPath) {
		this.morphTaggerModelPath = morphTaggerModelPath;
	}
	
	public boolean isLanguage(String language) {
		return this.language!=null && this.language.equals(language);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ModelStorage) {
			ModelStorage other = (ModelStorage) obj;
			
			return equals(language, other.language)
					&& equals(parserModelPath, other.parserModelPath)
					&& equals(lemmatizerModelPath, other.lemmatizerModelPath)
					&& equals(taggerModelPath, other.taggerModelPath)
					&& equals(morphTaggerModelPath, other.morphTaggerModelPath);
		}
		return false;
	}
	
	private static boolean equals(String s1, String s2) {
		return (s1!=null && s1.equals(s2)) || (s1==null && s2==null);
	}
	
	public boolean isEmpty() {
		clear();
		
		return parserModelPath==null 
				&& taggerModelPath==null
				&& morphTaggerModelPath==null
				&& lemmatizerModelPath==null;
	}
	
	/**
	 * Ensures that all entries are either {@code null} or a non-empty string.
	 */
	public void clear() {
		if(language!=null && language.isEmpty()) {
			language = null;
		}
		if(lemmatizerModelPath!=null && lemmatizerModelPath.isEmpty()) {
			lemmatizerModelPath = null;
		}
		if(taggerModelPath!=null && taggerModelPath.isEmpty()) {
			taggerModelPath = null;
		}
		if(morphTaggerModelPath!=null && morphTaggerModelPath.isEmpty()) {
			morphTaggerModelPath = null;
		}
		if(parserModelPath!=null && parserModelPath.isEmpty()) {
			parserModelPath = null;
		}
	}
	
	@Override
	public ModelStorage clone() {
		ModelStorage newStorage = new ModelStorage();
		
		newStorage.language = language;
		newStorage.lemmatizerModelPath = lemmatizerModelPath;
		newStorage.taggerModelPath = taggerModelPath;
		newStorage.morphTaggerModelPath = morphTaggerModelPath;
		newStorage.parserModelPath = parserModelPath;
		
		return newStorage;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return language.hashCode();
	}
}