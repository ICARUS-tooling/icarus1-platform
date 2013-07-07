/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface LanguageConstants {
	// flags
	public static final int FLAG_PROJECTIVE = (1 << 0);

	/**
	 * Head value to mark the root node.
	 */
	public static final short DATA_HEAD_ROOT = -1;

	public static final String DATA_ROOT_LABEL = "<root>"; //$NON-NLS-1$

	public static final String DATA_UNDEFINED_LABEL = "?"; //$NON-NLS-1$

	public static final String DATA_GROUP_LABEL = "<*>"; //$NON-NLS-1$

	public static final String DATA_LEFT_LABEL = "<<"; //$NON-NLS-1$

	public static final String DATA_RIGHT_LABEL = ">>"; //$NON-NLS-1$

	public static final int DATA_LEFT_VALUE = -1;

	public static final int DATA_RIGHT_VALUE = 1;

	public static final int DATA_GROUP_VALUE = -3;

	public static final int DATA_UNDEFINED_VALUE = -2;

	public static final int DATA_YES_VALUE = 0;

	public static final int DATA_NO_VALUE = -1;
}
