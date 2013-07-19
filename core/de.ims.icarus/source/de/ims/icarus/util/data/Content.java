/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util.data;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Content {

	/**
	 * Returns the {@code ContentType} that describes this object
	 */
	ContentType getEnclosingType();
}
