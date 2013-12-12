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
package de.ims.icarus.language.model.standard.manifest;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.model.io.ContextReader;
import de.ims.icarus.language.model.manifest.ContextManifest;
import de.ims.icarus.language.model.manifest.LayerManifest;
import de.ims.icarus.util.ClassProxy;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.location.Location;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultContextManifest extends AbstractManifest implements ContextManifest {

	private final List<LayerManifest> layerManifests = new ArrayList<>();

	private Object readerClass;

	private Location location;

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getLayerManifests()
	 */
	@Override
	public List<LayerManifest> getLayerManifests() {
		return CollectionUtils.getListProxy(layerManifests);
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getReaderClass()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ContextReader> getReaderClass() {
		if(readerClass instanceof ClassProxy) {
			try {
				readerClass = ((ClassProxy) readerClass).loadClass();
			} catch (ClassNotFoundException e) {
				throw new CorruptedStateException("Failed to load reader class: "+readerClass, e); //$NON-NLS-1$
			}
		}

		return (Class<? extends ContextReader>) readerClass;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#getLocation()
	 */
	@Override
	public Location getLocation() {
		return location;
	}

	/**
	 * @param readerClass the readerClass to set
	 */
	public void setReaderClass(Class<? extends ContextReader> readerClass) {
		if(readerClass==null)
			throw new NullPointerException("Invalid reader class"); //$NON-NLS-1$

		this.readerClass = readerClass;
	}

	/**
	 * @param proxy the readerClass to set
	 */
	public void setReaderClass(ClassProxy proxy) {
		if(proxy==null)
			throw new NullPointerException("Invalid proxy"); //$NON-NLS-1$

		this.readerClass = proxy;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		if(location==null)
			throw new NullPointerException("Invalid locations"); //$NON-NLS-1$

		this.location = location;
	}

	public void addLayerManifest(LayerManifest layerManifest) {
		if(layerManifest==null)
			throw new NullPointerException("Invalid layer manifest"); //$NON-NLS-1$
		if(layerManifests.contains(layerManifest))
			throw new IllegalArgumentException("Layer manifest already registered: "+layerManifest.getId()); //$NON-NLS-1$

		layerManifests.add(layerManifest);
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextManifest#setName(java.lang.String)
	 */
	@Override
	public void setName(String newName) {
		throw new UnsupportedOperationException("Renaming not supported"); //$NON-NLS-1$
	}
}
