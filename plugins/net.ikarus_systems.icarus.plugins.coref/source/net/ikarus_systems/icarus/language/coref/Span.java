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

import java.io.Serializable;

import net.ikarus_systems.icarus.language.LanguageConstants;
import net.ikarus_systems.icarus.util.CompactProperties;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Span implements Serializable, Comparable<Span> {
	
	private static final long serialVersionUID = 7035991391272077675L;
	
	private int sentenceId = -1;
	private int beginIndex;

	private int endIndex;
	
	private CompactProperties properties;
	
	private int clusterId = LanguageConstants.DATA_UNDEFINED_VALUE;

	public Span(int beginIndex, int endIndex, int clusterId) {
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.clusterId = clusterId;
	}
	
	public Span() {
		// no-op
	}

	public int getSentenceId() {
		return sentenceId;
	}

	public int getBeginIndex() {
		return beginIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setSentenceId(int sentenceIndex) {
		this.sentenceId = sentenceIndex;
	}

	public void setBeginIndex(int beginIndex) {
		this.beginIndex = beginIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	/*public int getDocumentId() {
		return documentId;
	}

	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}*/

	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}
	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}

	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new CompactProperties();
		}
		
		properties.setProperty(key, value);
	}
	
	@Override
	public Span clone() {
		Span clone = new Span(beginIndex, endIndex, clusterId);
		clone.setSentenceId(sentenceId);
		if(properties!=null) {
			clone.properties = properties.clone();
		}
		
		return clone;
	}
	
	@Override
	public String toString() {
		return String.format("{Span: %d.%d-%d (cluster: %d)}", sentenceId, beginIndex, endIndex, clusterId); //$NON-NLS-1$
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Span) {
			Span other = (Span) obj;
			return sentenceId==other.sentenceId && beginIndex==other.beginIndex
					&& endIndex==other.endIndex && clusterId==other.clusterId;
		}
		return false;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Span other) {
		if(other==null) {
			return 1;
		}
		
		if(sentenceId!=other.sentenceId) {
			return sentenceId-other.sentenceId; 
		}
		if(beginIndex!=other.beginIndex) {
			return beginIndex-other.beginIndex;
		}
		
		return (endIndex-beginIndex) - (other.endIndex-other.beginIndex);
	}
	
	public int getRange() {
		return endIndex - beginIndex;
	}
}
