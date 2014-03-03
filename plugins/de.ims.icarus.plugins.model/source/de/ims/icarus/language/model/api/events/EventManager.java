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
package de.ims.icarus.language.model.api.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.ims.icarus.language.model.api.Context;
import de.ims.icarus.language.model.api.Corpus;
import de.ims.icarus.language.model.api.CorpusMember;
import de.ims.icarus.language.model.api.Layer;
import de.ims.icarus.language.model.api.meta.MetaData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EventManager {

	private final Corpus corpus;

	private final List<CorpusListener> listeners = new CopyOnWriteArrayList<>();

	public EventManager(Corpus corpus) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		this.corpus = corpus;
	}

	public void addCorpusListener(CorpusListener listener) {
		if (listener == null)
			throw new NullPointerException("Invalid listener");  //$NON-NLS-1$

		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeCorpusListener(CorpusListener listener) {
		if (listener == null)
			throw new NullPointerException("Invalid listener");  //$NON-NLS-1$

		if(listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	public void fireCorpusChanged() {
		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus);

		for(CorpusListener listener : listeners) {
			listener.corpusChanged(event);
		}
	}

	public void fireCorpusSaved() {
		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus);

		for(CorpusListener listener : listeners) {
			listener.corpusSaved(event);
		}
	}

	public void fireContextAdded(Context context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				"context", context); //$NON-NLS-1$

		for(CorpusListener listener : listeners) {
			listener.contextAdded(event);
		}
	}

	public void fireContextRemoved(Context context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				"context", context); //$NON-NLS-1$

		for(CorpusListener listener : listeners) {
			listener.contextRemoved(event);
		}
	}

	public void fireMemberAdded(CorpusMember member) {
		if (member == null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				"member", member); //$NON-NLS-1$

		for(CorpusListener listener : listeners) {
			listener.memberAdded(event);
		}
	}

	public void fireMemberRemoved(CorpusMember member) {
		if (member == null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				"member", member); //$NON-NLS-1$

		for(CorpusListener listener : listeners) {
			listener.memberRemoved(event);
		}
	}

	public void fireMemberChanged(CorpusMember member) {
		if (member == null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				"member", member); //$NON-NLS-1$

		for(CorpusListener listener : listeners) {
			listener.memberChanged(event);
		}
	}

	public void fireMetaDataAdded(MetaData metaData, Layer layer) {
		if (metaData == null)
			throw new NullPointerException("Invalid metaData"); //$NON-NLS-1$
		if (layer == null)
			throw new NullPointerException("Invalid layer");  //$NON-NLS-1$

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				"metadata", metaData, "layer", layer); //$NON-NLS-1$ //$NON-NLS-2$

		for(CorpusListener listener : listeners) {
			listener.metaDataAdded(event);
		}
	}

	public void fireMetaDataRemoved(MetaData metaData, Layer layer) {
		if (metaData == null)
			throw new NullPointerException("Invalid metaData"); //$NON-NLS-1$
		if (layer == null)
			throw new NullPointerException("Invalid layer");  //$NON-NLS-1$

		if(listeners.isEmpty()) {
			return;
		}

		CorpusEvent event = new CorpusEvent(corpus,
				"metadata", metaData, "layer", layer); //$NON-NLS-1$ //$NON-NLS-2$

		for(CorpusListener listener : listeners) {
			listener.metaDataRemoved(event);
		}
	}
}
