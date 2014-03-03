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
package de.ims.icarus.language.model.standard.context;

import de.ims.icarus.language.model.api.Context;
import de.ims.icarus.language.model.api.Corpus;
import de.ims.icarus.language.model.api.manifest.ContextManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractContext implements Context {

	private final Corpus corpus;
	private final ContextManifest manifest;

	protected AbstractContext(Corpus corpus, ContextManifest manifest) {
		if(corpus==null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$
		if(manifest==null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		this.corpus = corpus;
		this.manifest = manifest;
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoaded()
	 */
	@Override
	public boolean isLoaded() {
		return true;
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoading()
	 */
	@Override
	public boolean isLoading() {
		return false;
	}

	/**
	 * @see de.ims.icarus.io.Loadable#load()
	 */
	@Override
	public void load() throws Exception {
		throw new UnsupportedOperationException(
				"Loading not supported by this context"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.io.Loadable#free()
	 */
	@Override
	public void free() {
		throw new UnsupportedOperationException(
				"Freeing not supported by this context"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.api.Context#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Context#getManifest()
	 */
	@Override
	public ContextManifest getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Context#addNotify(de.ims.icarus.language.model.api.Corpus)
	 */
	@Override
	public void addNotify(Corpus corpus) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.model.api.Context#removeNotify(de.ims.icarus.language.model.api.Corpus)
	 */
	@Override
	public void removeNotify(Corpus corpus) {
		// no-op
	}
}
