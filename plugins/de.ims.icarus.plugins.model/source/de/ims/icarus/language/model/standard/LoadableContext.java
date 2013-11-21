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
package de.ims.icarus.language.model.standard;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.model.Context;
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.Layer;
import de.ims.icarus.language.model.manifest.ContextManifest;
import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.location.Location;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class LoadableContext implements Context {
	
	private final Corpus corpus;
	private final ContextManifest manifest;

	private List<Layer> layers = new ArrayList<>(5);
	
	private volatile State state = State.BLANK;
	private final Object lock = new Object();
	
	private Location location;
	
	protected enum State {
		BLANK,
		LOADING,
		LAODED;
	}
	
	protected LoadableContext(Corpus corpus, ContextManifest manifest) {
		if(corpus==null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$
		if(manifest==null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$
		
		this.corpus = corpus;
		this.manifest = manifest;
	}
	
	protected Object getLock() {
		return lock;
	}
	
	/**
	 * Atomically attempts to set a new state if and only if the current state
	 * matches the expected state. Returns {@code true} if the operation was
	 * successful.
	 */
	protected boolean setState(State expected, State newState) {
		synchronized (getLock()) {
			if(state==expected) {
				state = newState;
				return true;
			} else {
				return false;
			}
		}
	}
	
	protected State getState() {
		synchronized (getLock()) {
			return state;
		}
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoaded()
	 */
	@Override
	public boolean isLoaded() {
		return getState()==State.LAODED;
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoading()
	 */
	@Override
	public boolean isLoading() {
		return getState()==State.LOADING;
	}

	/**
	 * @see de.ims.icarus.io.Loadable#load()
	 */
	@Override
	public void load() throws Exception {
		// Abort if loading already in progress
		if(!setState(State.BLANK, State.LOADING)) {
			return;
		}
		
		// TODO
	}

	/**
	 * @see de.ims.icarus.language.model.Context#getLocation()
	 */
	@Override
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		if(location==null)
			throw new NullPointerException("Invalid location"); //$NON-NLS-1$
		
		this.location = location;
	}

	/**
	 * @see de.ims.icarus.language.model.Context#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @see de.ims.icarus.language.model.Context#getLayers()
	 */
	@Override
	public List<Layer> getLayers() {
		return CollectionUtils.getListProxy(layers);
	}

	/**
	 * @see de.ims.icarus.language.model.Context#getManifest()
	 */
	@Override
	public ContextManifest getManifest() {
		return manifest;
	}

}
