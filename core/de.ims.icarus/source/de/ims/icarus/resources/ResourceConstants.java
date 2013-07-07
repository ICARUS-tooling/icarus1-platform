/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/resources/ResourceConstants.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.resources;

/**
 * @author Markus Gärtner 
 * @version $Id: ResourceConstants.java 7 2013-02-27 13:18:56Z mcgaerty $
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
