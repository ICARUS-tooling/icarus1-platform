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

import net.ikarus_systems.icarus.util.Options;

/**
 * Basic converter interface without any means of restrictions for
 * in- or output. Incompatibility of data will be signaled by
 * {@link DataConversionException} being thrown.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Converter {

	Object convert(Object source, Options options) throws DataConversionException;
}
