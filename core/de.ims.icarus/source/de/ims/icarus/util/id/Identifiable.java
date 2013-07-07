/*
 * $Revision: 17 $
 * $Date: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/id/Identifiable.java $
 *
 * $LastChangedDate: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $ 
 * $LastChangedRevision: 17 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.util.id;

import java.util.Comparator;

/**
 * @author Markus Gärtner 
 * @version $Id: Identifiable.java 17 2013-03-25 00:44:03Z mcgaerty $
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
