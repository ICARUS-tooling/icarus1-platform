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

import de.ims.icarus.util.Options;


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
 * content types (e.g. the {@link ContentTypeRegistry}) uses this accuracy indicator
 * as a performance measurement when deciding which converter to use.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DataConverter extends Converter {

	@Override
	Object convert(Object source, Options options) throws DataConversionException;

	ContentType getInputType();

	ContentType getResultType();

	double getAccuracy();
}
