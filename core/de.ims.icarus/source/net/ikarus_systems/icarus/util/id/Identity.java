/*
 * $Revision: 17 $
 * $Date: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/id/Identity.java $
 *
 * $LastChangedDate: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $ 
 * $LastChangedRevision: 17 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.util.id;

import java.util.Comparator;

import javax.swing.Icon;

/**
 * @author Markus Gärtner 
 * @version $Id: Identity.java 17 2013-03-25 00:44:03Z mcgaerty $
 *
 */
public interface Identity {
	
	String getId();
	
	String getName();
	
	String getDescription();
	
	Icon getIcon();
	
	Object getOwner();
	
	public static final Comparator<Identity> COMPARATOR = new Comparator<Identity>() {

		@Override
		public int compare(Identity i1, Identity i2) {
			String name1 = i1.getName();
			String name2 = i2.getName();
			if(name1!=null && name2!=null) {
				return name1.compareTo(name2);
			} else {
				return i1.getId().compareTo(i2.getId());
			}
		}
		
	};
}
