/*
 * $Revision: 33 $
 * $Date: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/events/Events.java $
 *
 * $LastChangedDate: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $ 
 * $LastChangedRevision: 33 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui.events;

/**
 * @author Markus Gärtner
 * @version $Id: Events.java 33 2013-05-13 12:33:31Z mcgaerty $
 *
 */
public interface Events {

	public static final String PROPERTY = "property"; //$NON-NLS-1$
	public static final String ADD = "adding"; //$NON-NLS-1$
	public static final String ADDED = "added"; //$NON-NLS-1$
	public static final String REMOVE = "removing"; //$NON-NLS-1$
	public static final String REMOVED = "removed"; //$NON-NLS-1$
	public static final String INSERT = "inserting"; //$NON-NLS-1$
	public static final String INSERTED = "inserted"; //$NON-NLS-1$
	public static final String MOVE = "moving"; //$NON-NLS-1$
	public static final String MOVED = "moved"; //$NON-NLS-1$
	public static final String CLEAN = "cleaning"; //$NON-NLS-1$
	public static final String CLEANED = "cleaned"; //$NON-NLS-1$
	public static final String DELETE = "deleting"; //$NON-NLS-1$
	public static final String DELETED = "deleted"; //$NON-NLS-1$
	public static final String CHANGE = "changing"; //$NON-NLS-1$
	public static final String CHANGED = "changed"; //$NON-NLS-1$

	public static final String SELECTION_CHANGED = "selectionChanged"; //$NON-NLS-1$
}
