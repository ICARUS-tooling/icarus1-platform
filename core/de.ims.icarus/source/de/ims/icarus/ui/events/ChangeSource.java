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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.ui.events;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ChangeSource {

	protected EventListenerList listenerList = new EventListenerList();

	protected final Object source;

	public ChangeSource() {
		source = null;
	}

	public ChangeSource(Object source) {
		if (source == null)
			throw new NullPointerException("Invalid source");

		this.source = source;
	}

	public void addChangeListener(ChangeListener listener) {
		listenerList.add(ChangeListener.class, listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listenerList.remove(ChangeListener.class, listener);
	}

	private Object getSource() {
		return source==null ? this : source;
	}

	public void fireStateChanged() {
		Object[] pairs = listenerList.getListenerList();

		ChangeEvent event = null;

		for (int i = pairs.length - 2; i >= 0; i -= 2) {
			if (pairs[i] == ChangeListener.class) {
				if (event == null) {
					event = new ChangeEvent(getSource());
				}

				((ChangeListener) pairs[i + 1]).stateChanged(event);
			}
		}
	}
}
