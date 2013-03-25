/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.id;

import java.util.Comparator;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface Identifiable {

	Identity getIdentity();
	
	public static final Comparator<Identifiable> COMPARATOR = new Comparator<Identifiable>() {

		@Override
		public int compare(Identifiable i1, Identifiable i2) {
			return Identity.COMPARATOR.compare(i1.getIdentity(), i2.getIdentity());
		}
		
	};
}
