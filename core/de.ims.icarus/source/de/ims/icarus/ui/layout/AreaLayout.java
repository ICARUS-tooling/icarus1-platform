/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.layout;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.resources.Localizer;
import de.ims.icarus.ui.Alignment;


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
