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
package de.ims.icarus.ui.helper;

import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class RendererCache {

	private volatile static RendererCache instance;

	public static RendererCache getInstance() {
		RendererCache result = instance;

		if (result == null) {
			synchronized (RendererCache.class) {
				result = instance;

				if (result == null) {
					instance = new RendererCache();
					result = instance;
				}
			}
		}

		return result;
	}

	private Map<Object, String> uiMap = new WeakHashMap<>();

	public boolean requiresNewUI(Object renderer) {
		if (renderer == null)
			throw new NullPointerException("Invalid renderer"); //$NON-NLS-1$

		LookAndFeel laf = UIManager.getLookAndFeel();
		if(laf==null) {
			return true;
		}

		String latestRendererUI = uiMap.get(renderer);
		String currentUI = laf.getClass().getName();

		if(latestRendererUI==null || !latestRendererUI.equals(currentUI)) {
			uiMap.put(renderer, currentUI);
			return true;
		}

		return false;
	}
}
