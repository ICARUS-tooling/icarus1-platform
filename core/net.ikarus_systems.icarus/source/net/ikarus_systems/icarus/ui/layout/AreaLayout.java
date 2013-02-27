/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.layout;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.resources.Localizer;
import net.ikarus_systems.icarus.ui.Alignment;

/**
 * @author Markus Gärtner
 * @version $Id$
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
