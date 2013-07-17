/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.resources;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface ResourceConstants {

	/**
	 * The default key used to store localization
	 * data in an object's properties for the {@code text}
	 * field.
	 */
	public static final String DEFAULT_TEXT_KEY = "_localizationKey_text"; //$NON-NLS-1$
	
	/**
	 * The default key used to store localization
	 * data in an object's properties for the {@code description}
	 * field. This data is optional and most localization
	 * facilities do not report an error in the case it is
	 * missing for a certain object.
	 */
	public static final String DEFAULT_DESCRIPTION_KEY = "_localizationKey_desc"; //$NON-NLS-1$
}
