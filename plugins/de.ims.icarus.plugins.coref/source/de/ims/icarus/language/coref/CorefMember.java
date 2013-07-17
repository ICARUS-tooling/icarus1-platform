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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class CorefMember {

	protected CorefProperties properties;
	
	protected CorefMember() {
		// no-op
	}
	
	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}

	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new CorefProperties();
		}
		
		properties.put(key, value);
	}
	
	public void setProperties(CorefProperties properties) {
		this.properties = properties;
	}
	
	protected CorefProperties cloneProperties() {
		return properties==null ? null : properties.clone();
	}
}
