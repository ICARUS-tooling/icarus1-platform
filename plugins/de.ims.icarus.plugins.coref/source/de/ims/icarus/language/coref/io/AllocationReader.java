/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.coref.io;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.location.Location;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AllocationReader {

	public static final String BEGIN_DOCUMENT = "#begin document"; //$NON-NLS-1$
	public static final String END_DOCUMENT = "#end document"; //$NON-NLS-1$

	public static final String BEGIN_NODES = "#begin nodes"; //$NON-NLS-1$
	public static final String END_NODES = "#end nodes"; //$NON-NLS-1$

	public static final String BEGIN_EDGES = "#begin edges"; //$NON-NLS-1$
	public static final String END_EDGES = "#end edges"; //$NON-NLS-1$
	
	public static final String COMMENT_PREFIX = "#"; //$NON-NLS-1$
	
	
	public void init(Location location, 
			Options options, CoreferenceDocumentSet documentSet) throws Exception;
	
	public void readAllocation(CoreferenceAllocation target) throws Exception;
}
