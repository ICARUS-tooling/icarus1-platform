/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.data;

import java.util.logging.Level;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;

import org.java.plugin.registry.Extension;

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
	 * @see net.ikarus_systems.icarus.util.data.DataConverter#convert(java.lang.Object)
	 */
	@Override
	public Object convert(Object source) throws DataConversionException {
		Converter converter = getConverter();
		if(converter==null)
			throw new DataConversionException("Invalid converter"); //$NON-NLS-1$
		
		return converter.convert(source);
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataConverter#getInputType()
	 */
	@Override
	public ContentType getInputType() {
		return ContentTypeRegistry.getInstance().getType(
				extension.getParameter("inputType").valueAsString()); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataConverter#getResultType()
	 */
	@Override
	public ContentType getResultType() {
		return ContentTypeRegistry.getInstance().getType(
				extension.getParameter("resultType").valueAsString()); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataConverter#getAccuracy()
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
