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

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.LayerType;
import de.ims.icarus.language.model.io.ContextReader;
import de.ims.icarus.language.model.io.ContextWriter;
import de.ims.icarus.language.model.manifest.CorpusManifest;
import de.ims.icarus.language.model.manifest.Template;

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

	private Map<String, Template> templates = new HashMap<>();
	private Map<String, LayerType> layerTypes = new HashMap<>();
	private Map<String, ContextReader> contextReaders = new HashMap<>();
	private Map<String, ContextWriter> contextWriters = new HashMap<>();

	private CorpusRegistry() {
		if(instance!=null)
			throw new IllegalStateException("Cannot instantiate additional registry"); //$NON-NLS-1$
	}

	private void init() {
		// TODO
	}

	public LayerType getLayerType(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name");

		LayerType layerType = layerTypes.get(name);

		if(layerType==null)
			throw new IllegalArgumentException("No such layer-type: "+name);

		return layerType;
	}

	public void addCorpus(CorpusManifest manifest) {

	}

	public void removeCorpus(CorpusManifest manifest) {

	}

	public Corpus getCorpus(CorpusManifest manifest) {
		//FIXME
		return null;
	}

	public Template getTemplate(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id");

		Template template = templates.get(id);

		if(template==null)
			throw new IllegalArgumentException("No template registered for id: "+id);

		return template;
	}

	public void registerTemplate(Template template) {
		//TODO
	}

	public ContextReader getContextReader(String formatId) {
		if (formatId == null)
			throw new NullPointerException("Invalid formatId");  //$NON-NLS-1$

		ContextReader reader = contextReaders.get(formatId);

		if(reader==null)
			throw new IllegalArgumentException("No reader registered for format-id: "+formatId); //$NON-NLS-1$

		return reader;
	}

	public ContextWriter getContextWriter(String formatId) {
		if (formatId == null)
			throw new NullPointerException("Invalid formatId");  //$NON-NLS-1$

		ContextWriter writer = contextWriters.get(formatId);

		if(writer==null)
			throw new IllegalArgumentException("No writer registered for format-id: "+formatId); //$NON-NLS-1$

		return writer;
	}
}
