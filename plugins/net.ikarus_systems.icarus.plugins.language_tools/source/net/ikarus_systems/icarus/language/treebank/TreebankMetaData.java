/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface TreebankMetaData {
	
	public static final String MAX_LENGTH = "maxLength"; //$NON-NLS-1$
	public static final String MIN_LENGTH = "minLength"; //$NON-NLS-1$
	public static final String TOTAL_LENGTH = "totalLength"; //$NON-NLS-1$
	public static final String ITEM_COUNT = "itemCount"; //$NON-NLS-1$
	public static final String AVERAGE_LENGTH = "averageLength"; //$NON-NLS-1$

	Object getValue(String key);
}
