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
package de.ims.icarus.model.standard.context;

import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.manifest.ContextManifest;

/**
 * Implements a {@code Context} suitable as a the default context of a
 * {@link Corpus} instance. It is different to a regular context in that
 * it does not allow to be added to/removed from a corpus. Attempting to
 * do so will throw an {@code AssertionError}
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultContext extends LoadableContext {

	public DefaultContext(Corpus corpus, ContextManifest manifest) {
		super(corpus, manifest);
	}

	/**
	 * @see de.ims.icarus.model.api.Context#addNotify(de.ims.icarus.model.api.Corpus)
	 */
	@Override
	public void addNotify(Corpus corpus) {
		throw new AssertionError();
	}

	/**
	 * @see de.ims.icarus.model.api.Context#removeNotify(de.ims.icarus.model.api.Corpus)
	 */
	@Override
	public void removeNotify(Corpus corpus) {
		throw new AssertionError();
	}
}
