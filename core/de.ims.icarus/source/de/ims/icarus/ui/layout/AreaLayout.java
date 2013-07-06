/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/layout/AreaLayout.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui.layout;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.resources.Localizer;
import de.ims.icarus.ui.Alignment;


/**
 * @author Markus Gärtner
 * @version $Id: AreaLayout.java 7 2013-02-27 13:18:56Z mcgaerty $
 *
 */
public interface AreaLayout {

	/**
	 * Build the view and present all registered components on the
	 * initially set container.
	 */
	void doLayout();
	
	/**
	 * Initiate the layout with the given {@code JComponent} as root 
	 * container.
	 */
	void init(JComponent container);
	
	/**
	 * Add the given component to the specified area
	 */
	void add(JComponent comp, Alignment alignment);
	
	/**
	 * Removes the given component from whatever area it
	 * was previously added to.
	 * @param comp
	 */
	void remove(JComponent comp);
	
	/**
	 * Maximizes or minimizes the given component.
	 */
	void toggle(JComponent comp);
	
	/**
	 * Registers a {@code ChangeListener} to be notified when
	 * the selection in a {@code JTabbedPane} changes.
	 */
	void setContainerWatcher(ChangeListener listener);
	
	/**
	 * Changes the {@code Localizer} to be used for localizing
	 * tab components.
	 */
	void setTabLocalizer(Localizer localizer);
}
