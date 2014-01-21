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
package de.ims.icarus.language.model.registry;

import java.util.concurrent.atomic.AtomicLong;

import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.LayerType;
import de.ims.icarus.language.model.manifest.CorpusManifest;
import de.ims.icarus.language.model.manifest.Manifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CorpusRegistry {

	private static volatile CorpusRegistry instance;

	public static CorpusRegistry getInstance() {
		CorpusRegistry tmp = instance;
		if(tmp==null) {
			synchronized (CorpusRegistry.class) {
				tmp = instance;
				if(tmp==null) {
					tmp = new CorpusRegistry();
					tmp.init();

					instance = tmp;
				}
			}
		}

		return tmp;
	}

	private final AtomicLong nextId = new AtomicLong();

	private CorpusRegistry() {
		if(instance!=null)
			throw new IllegalStateException("Cannot instantiate additional registry"); //$NON-NLS-1$
	}

	private void init() {
		// TODO
	}

	public long newId() {
		return nextId.incrementAndGet();
	}

	public LayerType getLayerType(String name) {

	}

	public Corpus getCorpus(CorpusManifest manifest) {

	}

	public Manifest getTemplate(String id) {

	}
}
