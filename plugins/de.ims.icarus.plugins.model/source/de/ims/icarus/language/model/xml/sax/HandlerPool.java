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
package de.ims.icarus.language.model.xml.sax;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import de.ims.icarus.logging.LogReport;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class HandlerPool {

	private Map<Class<? extends ModelElementHandler<?>>, Stack<ModelElementHandler<?>>> pool
		= new HashMap<>();

	private final LogReport report;

	public HandlerPool(LogReport report) {
		if (report == null)
			throw new NullPointerException("Invalid report"); //$NON-NLS-1$

		this.report = report;
	}

	@SuppressWarnings("unchecked")
	public <T extends ModelElementHandler<?>> T getHandler(Class<T> clazz) {
		if (clazz == null)
			throw new NullPointerException("Invalid clazz"); //$NON-NLS-1$

		Stack<ModelElementHandler<?>> handlers = pool.get(clazz);

		if(handlers==null) {
			handlers = new Stack<>();
			pool.put(clazz, handlers);
		}

		T handler = null;

		if(!handlers.isEmpty()) {
			handler = (T) handlers.pop();
		}

		if(handler==null) {
			try {
				handler = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				report.error("Failed to instantiate handler: "+clazz, e); //$NON-NLS-1$
			}
		}

		return handler;
	}

	public void recycle(ModelElementHandler<?> handler) {
		if (handler == null)
			throw new NullPointerException("Invalid handler"); //$NON-NLS-1$

		@SuppressWarnings("unchecked")
		Class<ModelElementHandler<?>> clazz = (Class<ModelElementHandler<?>>) handler.getClass();

		Stack<ModelElementHandler<?>> handlers = pool.get(clazz);

		if(handlers==null) {
			handlers = new Stack<>();
			pool.put(clazz, handlers);
		}

		handler.clear();

		handlers.push(handler);
	}
}
