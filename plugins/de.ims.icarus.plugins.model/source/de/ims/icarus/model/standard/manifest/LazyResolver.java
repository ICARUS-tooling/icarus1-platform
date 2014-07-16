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
package de.ims.icarus.model.standard.manifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class LazyResolver {

	protected abstract class Link<O extends Object> {
		private final String id;
		O target;

		public Link(String id) {
			if (id == null)
				throw new NullPointerException("Invalid id"); //$NON-NLS-1$

			this.id = id;
		}

		protected abstract O resolve();

		public String getId() {
			return id;
		}

		public O get() {
			if(target==null) {
				target = resolve();
			}

			return target;
		}
	}

	protected abstract class MemoryLink<O extends Object> extends Link<O> {

		private volatile boolean resolved = false;

		public MemoryLink(String id) {
			super(id);
		}

		@Override
		public O get() {
			if(!resolved && target==null) {
				target = resolve();
				resolved = true;
			}

			return target;
		}
	}
}
