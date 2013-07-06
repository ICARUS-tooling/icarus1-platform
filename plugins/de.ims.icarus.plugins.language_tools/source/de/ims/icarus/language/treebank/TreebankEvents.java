/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.treebank;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface TreebankEvents {

	public static final String ADDED = "treebank:added"; //$NON-NLS-1$
	public static final String REMOVED = "treebank:removed"; //$NON-NLS-1$
	public static final String UPDATED = "treebank:updated"; //$NON-NLS-1$
	public static final String LOADING = "treebank:loading"; //$NON-NLS-1$
	public static final String LOADED = "treebank:loaded"; //$NON-NLS-1$
	public static final String FREEING = "treebank:freeing"; //$NON-NLS-1$
	public static final String FREED = "treebank:freed"; //$NON-NLS-1$
	public static final String CHANGED = "treebank:changed"; //$NON-NLS-1$
	public static final String LOCATION = "treebank:location"; //$NON-NLS-1$
	public static final String EDITABLE = "treebank:editable"; //$NON-NLS-1$
	public static final String METADATA = "treebank:metadata"; //$NON-NLS-1$
}
