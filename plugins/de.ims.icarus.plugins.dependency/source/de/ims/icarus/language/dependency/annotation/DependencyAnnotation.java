/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.dependency.annotation;

import de.ims.icarus.util.annotation.Annotation;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DependencyAnnotation extends Annotation {
	
	boolean isHighlighted(int index);

	int getGroupId(int index);
	
	int getGroupId(int index, String token);
	
	boolean isNodeHighlighted(int index);
	boolean isEdgeHighlighted(int index);
	boolean isTransitiveHighlighted(int index);
	
	long getHighlight(int index);
	
	boolean isTokenHighlighted(int index, String token);
	
	int getCorpusIndex();
}
