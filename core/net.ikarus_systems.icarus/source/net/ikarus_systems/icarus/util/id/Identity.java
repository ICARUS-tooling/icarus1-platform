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

import javax.swing.Icon;

/**
 * @author Markus Gärtner 
 * @version $Id$
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
