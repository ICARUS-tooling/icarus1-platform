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
package de.ims.icarus.util.data;

import java.util.logging.Level;

import org.java.plugin.registry.Extension;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ExtensionDataConverter implements DataConverter {
	
	private final Extension extension;
	private Converter converter;
	private boolean invalid = false;
	
	public ExtensionDataConverter(Extension extension) {
		if(extension==null)
			throw new IllegalArgumentException("Invalid extension"); //$NON-NLS-1$
		
		this.extension = extension;
	}
	
	private synchronized Converter getConverter() {
		if(!invalid) {
			try {
				converter = (Converter) PluginUtil.instantiate(extension);
			} catch (Exception e) {
				invalid = true;
				LoggerFactory.log(this, Level.SEVERE, "Failed to instantiate converter: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}
		return converter;
	}

	/**
	 * @see de.ims.icarus.util.data.DataConverter#convert(java.lang.Object)
	 */
	@Override
	public Object convert(Object source, Options options) throws DataConversionException {
		Converter converter = getConverter();
		if(converter==null)
			throw new DataConversionException("Invalid converter"); //$NON-NLS-1$
		
		return converter.convert(source, options);
	}

	/**
	 * @see de.ims.icarus.util.data.DataConverter#getInputType()
	 */
	@Override
	public ContentType getInputType() {
		return ContentTypeRegistry.getInstance().getType(
				extension.getParameter("inputType").valueAsString()); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.util.data.DataConverter#getResultType()
	 */
	@Override
	public ContentType getResultType() {
		return ContentTypeRegistry.getInstance().getType(
				extension.getParameter("resultType").valueAsString()); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.util.data.DataConverter#getAccuracy()
	 */
	@Override
	public double getAccuracy() {
		return extension.getParameter("accuracy").valueAsNumber().doubleValue(); //$NON-NLS-1$
	}

	@Override
	public int hashCode() {
		return extension.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ExtensionDataConverter) {
			return ((ExtensionDataConverter)obj).extension==extension;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Converter: "+extension.getUniqueId(); //$NON-NLS-1$
	}

}
