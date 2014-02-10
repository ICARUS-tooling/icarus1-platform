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

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.Layer;
import de.ims.icarus.language.model.io.ContextReader;
import de.ims.icarus.language.model.io.ContextWriter;
import de.ims.icarus.language.model.manifest.ContextManifest;
import de.ims.icarus.language.model.manifest.ContextReaderManifest;
import de.ims.icarus.language.model.manifest.ContextWriterManifest;
import de.ims.icarus.language.model.manifest.Implementation;
import de.ims.icarus.language.model.registry.CorpusRegistry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LoadableContext extends AbstractContext {

	private final List<Layer> layers = new ArrayList<>(5);

	private volatile State state = State.BLANK;
	private final Object lock = new Object();

	private volatile ContextReader reader;
	private volatile ContextWriter writer;

	protected enum State {
		BLANK,
		LOADING,
		LAODED;
	}

	protected LoadableContext(Corpus corpus, ContextManifest manifest) {
		super(corpus, manifest);
	}

	protected final Object getLock() {
		return lock;
	}

	/**
	 * Atomically attempts to set a new state if and only if the current state
	 * matches the expected state. Returns {@code true} if the operation was
	 * successful.
	 */
	protected final boolean setState(State expected, State newState) {
		synchronized (getLock()) {
			if(state==expected) {
				state = newState;
				return true;
			} else
				return false;
		}
	}

	protected final State getState() {
		synchronized (getLock()) {
			return state;
		}
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoaded()
	 */
	@Override
	public final boolean isLoaded() {
		return getState()==State.LAODED;
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoading()
	 */
	@Override
	public final boolean isLoading() {
		return getState()==State.LOADING;
	}

	/**
	 * @see de.ims.icarus.io.Loadable#load()
	 */
	@Override
	public final void load() throws Exception {
		// Abort if loading already in progress
		if(!setState(State.BLANK, State.LOADING))
			return;


		ContextReader reader = getReader();

		if(reader==null)
			throw new IllegalStateException("Failed to instantiate reader"); //$NON-NLS-1$

		List<Layer> newLayers = reader.readContext(getManifest());

		if(newLayers==null) {
			LoggerFactory.warning(this,
					"Layer source was empty: "+getManifest().getLocationManifest().getPath()); //$NON-NLS-1$
		}

		Corpus corpus = getCorpus();

		for(Layer layer : newLayers) {
			corpus.addLayer(layer);
		}

		layers.addAll(newLayers);
	}

	/**
	 * @see de.ims.icarus.language.model.Context#getLayers()
	 */
	@Override
	public final List<Layer> getLayers() {
		return CollectionUtils.getListProxy(layers);
	}

	private ContextReader loadReader() throws Exception {

		ContextReaderManifest readerManifest = getManifest().getReaderManifest();
		if(readerManifest==null)
			throw new IllegalStateException("Missing reader manifest on context: "+getManifest()); //$NON-NLS-1$

		String formatId = readerManifest.getFormatId();
		if(formatId!=null) {
			return CorpusRegistry.getInstance().getContextReader(formatId);
		}

		Implementation implementation = readerManifest.getImplementation();
		if(implementation!=null) {
			return implementation.instantiate(ContextReader.class);
		}

		throw new IllegalStateException("No reader information available"); //$NON-NLS-1$
	}

	public ContextReader getReader() throws Exception {
		if(reader==null) {
			synchronized (getLock()) {
				if(reader==null) {
					reader = loadReader();
				}
			}
		}

		return reader;
	}

	private ContextWriter loadWriter() throws Exception {

		ContextWriterManifest writerManifest = getManifest().getWriterManifest();
		if(writerManifest==null) {
			// Note that for a writer it is perfectly legal to provide no manifest
			return null;
		}

		String formatId = writerManifest.getFormatId();
		if(formatId!=null) {
			return CorpusRegistry.getInstance().getContextWriter(formatId);
		}

		Implementation implementation = writerManifest.getImplementation();
		if(implementation!=null) {
			return implementation.instantiate(ContextWriter.class);
		}

		// While it is legal to provide no writer manifest at all, in
		// the case that one such manifest was defined it must declare either
		// a format id or a valid implementation!
		throw new IllegalStateException("No reader information available"); //$NON-NLS-1$
	}

	public ContextWriter getWriter() throws Exception {
		if(writer==null) {
			synchronized (getLock()) {
				if(writer==null) {
					writer = loadWriter();
				}
			}
		}

		return writer;
	}

	/**
	 * @see de.ims.icarus.io.Loadable#free()
	 */
	@Override
	public void free() {
		for(Layer layer : layers) {
			getCorpus().removeLayer(layer);
		}

		layers.clear();
	}
}
