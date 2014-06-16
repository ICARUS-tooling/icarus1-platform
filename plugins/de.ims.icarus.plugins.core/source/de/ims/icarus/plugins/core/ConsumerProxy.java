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
package de.ims.icarus.plugins.core;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.java.plugin.registry.Extension;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.util.transfer.Consumer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
class ConsumerProxy implements Consumer {

	private Consumer consumer;
	private Identity identity;
	private List<ContentType> contentTypes;

	private final Extension extension;

	ConsumerProxy(Extension extension) {
		if (extension == null)
			throw new NullPointerException("Invalid extension"); //$NON-NLS-1$

		this.extension = extension;
	}

	Identity identity() {
		if(identity==null) {
			identity = PluginUtil.getIdentity(extension);
		}
		return identity;
	}

	List<ContentType> contentTypes() {
		if(contentTypes==null) {
			contentTypes = new ArrayList<>();
			for(Extension.Parameter param : extension.getParameters("contentType")) { //$NON-NLS-1$
				contentTypes.add(ContentTypeRegistry.getInstance().getType(param.valueAsExtension()));
			}
		}
		return contentTypes;
	}

	Consumer consumer() {
		if(consumer==null) {
			try {
				consumer = (Consumer) PluginUtil.instantiate(extension);
			} catch (InstantiationException|IllegalAccessException e) {
				LoggerFactory.error(this, "Unable to access consumer constructor", e); //$NON-NLS-1$
			} catch (ClassNotFoundException e) {
				LoggerFactory.error(this, "Consumer class not found", e); //$NON-NLS-1$
			}
		}
		return consumer;
	}

	public Extension getExtension() {
		return extension;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return identity().getId();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return identity().getName();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return identity().getDescription();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return identity().getIcon();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	private void ensureView() {
		// Cannot be null
		IcarusFrame frame = IcarusFrame.getActiveFrame();

		Extension.Parameter perspectiveParam = extension.getParameter("perspective"); //$NON-NLS-1$
		if(perspectiveParam!=null) {
			Extension perspectiveExtension = perspectiveParam.valueAsExtension();
			try {
				frame.openPerspective(perspectiveExtension, false);
			} catch (Exception e) {
				LoggerFactory.error(this, "Failed to open required perspective " //$NON-NLS-1$
							+perspectiveExtension.getId()+" for consumer "+consumer.getId(), e); //$NON-NLS-1$
			}
		}

		Extension.Parameter viewParameter  = extension.getParameter("view"); //$NON-NLS-1$
		if(viewParameter!=null) {
			Perspective perspective = frame.getCurrentPerspective();
			Extension viewExtension = viewParameter.valueAsExtension();
			View view = perspective.getView(viewExtension);

			if(view==null)
				throw new IllegalStateException("Missing required view " //$NON-NLS-1$
							+viewExtension.getId()+" for consumer "+consumer.getId()); //$NON-NLS-1$

			perspective.setActiveView(view);
		}
	}

	/**
	 * @see de.ims.icarus.util.transfer.Consumer#process(java.lang.Object, java.lang.Object, de.ims.icarus.util.Options, boolean)
	 */
	@Override
	public void process(Object data, Object source, Options options) throws Exception {
		Consumer consumer = consumer();

		if(consumer==null) {
			return;
		}

		ensureView();

		consumer.process(data, source, options);
	}

	/**
	 * @see de.ims.icarus.util.transfer.Consumer#processBatch(java.lang.Object[], java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void processBatch(Object[] data, Object source, Options options)
			throws Exception {
		if(!supportsBatch())
			throw new UnsupportedOperationException("Unable to consume batch data"); //$NON-NLS-1$

		Consumer consumer = consumer();

		if(consumer==null) {
			return;
		}

		ensureView();

		consumer.processBatch(data, source, options);
	}

	/**
	 * @see de.ims.icarus.util.transfer.Consumer#supportsBatch()
	 */
	@Override
	public boolean supportsBatch() {
		return extension.getParameter("batch").valueAsBoolean(); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.util.transfer.Consumer#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType contentType) {
		for(ContentType type : contentTypes()) {
			if(ContentTypeRegistry.isCompatible(type, contentType)) {
				return true;
			}
		}
		return false;
	}
}
