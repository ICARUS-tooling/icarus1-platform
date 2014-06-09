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
package de.ims.icarus.model.api.events;

import java.util.Hashtable;
import java.util.Map;

import de.ims.icarus.model.api.Container;
import de.ims.icarus.model.api.Context;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.CorpusMember;
import de.ims.icarus.model.api.Structure;
import de.ims.icarus.model.api.layer.Layer;
import de.ims.icarus.model.api.meta.MetaData;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusEvent {

	private final Corpus corpus;

	/**
	 * Holds the properties of the event.
	 */
	private final Map<String, Object> properties;

	/**
	 * Holds the consumed state of the event. Default is false.
	 */
	private boolean consumed = false;

	/**
	 * Constructs a new event for the given name.
	 */
	public CorpusEvent(Corpus corpus) {
		this(corpus, (Object[]) null);
	}

	/**
	 * Constructs a new event for the given name and properties. The optional
	 * properties are specified using a sequence of keys and values, eg.
	 * {@code new mxEventObject("eventName", key1, val1, .., keyN, valN))}
	 */
	public CorpusEvent(Corpus corpus, Object... args) {
		this.corpus = corpus;
		properties = new Hashtable<String, Object>();

		if (args != null) {
			for (int i = 0; i < args.length; i += 2) {
				if (args[i + 1] != null) {
					properties.put(String.valueOf(args[i]), args[i + 1]);
				}
			}
		}
	}

	/**
	 *
	 */
	public Map<String, Object> getProperties() {
		return CollectionUtils.getMapProxy(properties);
	}

	/**
	 *
	 */
	public Object getProperty(String key) {
		return properties.get(key);
	}

	/**
	 * Returns true if the event has been consumed.
	 */
	public boolean isConsumed() {
		return consumed;
	}

	/**
	 * Consumes the event.
	 */
	public void consume() {
		consumed = true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("CorpusEvent: "); //$NON-NLS-1$
		if(consumed) {
			sb.append(" (consumed)"); //$NON-NLS-1$
		}

		sb.append("["); //$NON-NLS-1$
		for(Map.Entry<String, Object> entry : properties.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		sb.append("]"); //$NON-NLS-1$

		return sb.toString();
	}

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	// HELPER METHODS

	public Context getContext() {
		return (Context) getProperty("context"); //$NON-NLS-1$
	}

	public CorpusMember getMember() {
		return (CorpusMember) getProperty("member"); //$NON-NLS-1$
	}

	public Layer getLayer() {
		return (Layer) getProperty("layer"); //$NON-NLS-1$
	}

	public Container getContainer() {
		return (Container) getProperty("container"); //$NON-NLS-1$
	}

	public Structure getStructure() {
		return (Structure) getProperty("structure"); //$NON-NLS-1$
	}

	public MetaData getMetaData() {
		return (MetaData) getProperty("metadata"); //$NON-NLS-1$
	}
}
