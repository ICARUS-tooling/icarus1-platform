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


/**
 * Advanced version of the {@link Converter} interface. Serves as
 * bridge between two {@code ContentType} instances. 
 * <p>
 * Note that each {@code DataConverter} implementation is supposed to handle
 * only <b>one</b> conversion, namely the one defined by the return values
 * of its {@link #getInputType()} and {@link #getResultType()} methods.
 * In addition the {@link #getAccuracy()} method returns an <i>estimated</i>
 * value of accuracy. This accuracy {@code a} with 0&lt;{@code a}&le;1
 * describes the amount of <i>preserved</i> content when converting data. A
 * value of {@code 1} means there will be no loss of data, while a value
 * close to {@code 0} indicates an almost entire loss. The framework regarding
 * content types (e.g. the {@link ContentTypeRegistry}) use this accuracy indicator
 * as a performance measurement when deciding which converter to use.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DataConverter extends Converter {

	Object convert(Object source) throws DataConversionException;
	
	ContentType getInputType();
	
	ContentType getResultType();
	
	double getAccuracy();
}
