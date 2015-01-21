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
package de.ims.icarus.model.standard.layer;

import javax.swing.Icon;

import org.java.plugin.registry.Extension;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.model.api.layer.LayerType;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.util.classes.ClassProxy;
import de.ims.icarus.util.classes.ClassUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LayerTypeWrapper implements LayerType {

	private LayerType proxy;

	private final Object source;
	private final String id;

	public LayerTypeWrapper(String id, ClassProxy proxy) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		if (proxy == null)
			throw new NullPointerException("Invalid proxy"); //$NON-NLS-1$

		this.id = id;
		this.source = proxy;
	}

	public LayerTypeWrapper(String id, String className) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		if (className == null)
			throw new NullPointerException("Invalid className"); //$NON-NLS-1$

		this.id = id;
		this.source = className;
	}

	public LayerTypeWrapper(String id, Extension extension) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		if (extension == null)
			throw new NullPointerException("Invalid extension"); //$NON-NLS-1$

		this.id = id;
		this.source = extension;
	}

	private LayerType getProxy() {
		if(proxy==null) {
			synchronized (this) {
				if(proxy==null) {
					try {
						proxy = (LayerType) ClassUtils.instantiate(source);
					} catch (ClassNotFoundException | InstantiationException
							| IllegalAccessException e) {
						LoggerFactory.error(this, "Failed to instantiate layer type proxy: "+source, e); //$NON-NLS-1$

						throw new IllegalStateException("Unable to load layer type proxy", e); //$NON-NLS-1$
					}
				}
			}
		}
		return proxy;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return getProxy().getName();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return getProxy().getDescription();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return getProxy().getIcon();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return getProxy().getOwner();
	}

	/**
	 * @see de.ims.icarus.model.api.layer.LayerType#getSharedManifest()
	 */
	@Override
	public LayerManifest getSharedManifest() {
		return getProxy().getSharedManifest();
	}
}
