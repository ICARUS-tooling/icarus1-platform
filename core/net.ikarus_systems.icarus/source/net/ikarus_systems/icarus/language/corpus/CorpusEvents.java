/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.corpus;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface CorpusEvents {

	public static final String ADDED = "corpus:added"; //$NON-NLS-1$
	public static final String REMOVED = "corpus:removed"; //$NON-NLS-1$
	public static final String UPDATED = "corpus:updated"; //$NON-NLS-1$
	public static final String LOADING = "corpus:loading"; //$NON-NLS-1$
	public static final String LOADED = "corpus:loaded"; //$NON-NLS-1$
	public static final String CHANGED = "corpus:changed"; //$NON-NLS-1$
	public static final String LOCATION = "corpus:location"; //$NON-NLS-1$
	public static final String EDITABLE = "corpus:editable"; //$NON-NLS-1$
	public static final String METADATA = "corpus:metadata"; //$NON-NLS-1$
}
