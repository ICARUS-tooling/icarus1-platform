/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.tree;

import java.util.List;

import de.ims.icarus.search_tools.annotation.ResultAnnotator;
import de.ims.icarus.util.CorruptedStateException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractTreeResultAnnotator implements ResultAnnotator {
	
	protected Matcher[] matchers;

	public AbstractTreeResultAnnotator(Matcher rootMatcher) {
		List<Matcher> buffer = TreeUtils.collectMatchers(rootMatcher);
		TreeUtils.clearDuplicates(buffer);
		
		matchers = buffer.toArray(new Matcher[0]);
		
		for(int i=0; i<matchers.length; i++) {
			if(matchers[i].getId()!=i)
				throw new CorruptedStateException();
		}
	}
}
