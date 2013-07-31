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

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConverterChain implements DataConverter {
	
	private final DataConverter[] converters;
	private final double accuracy;

	public ConverterChain(DataConverter converterA, DataConverter converterB) {
		if(converterA==null)
			throw new IllegalArgumentException("Invalid first convetrer"); //$NON-NLS-1$
		if(converterB==null)
			throw new IllegalArgumentException("Invalid second converter"); //$NON-NLS-1$

		List<DataConverter> buffer = new ArrayList<>();
		appendConverter(converterA, buffer);
		appendConverter(converterB, buffer);
		
		this.converters = buffer.toArray(new DataConverter[buffer.size()]);
		validateChain();
		accuracy = calcAccuracy();
	}

	public ConverterChain(DataConverter[] converters) {
		if(converters==null)
			throw new IllegalArgumentException("Invalid converter array"); //$NON-NLS-1$
		if(converters.length==0)
			throw new IllegalArgumentException("Empty converter array"); //$NON-NLS-1$
		
		List<DataConverter> buffer = new ArrayList<>();
		for(DataConverter converter : converters) {
			appendConverter(converter, buffer);
		}
		
		this.converters = buffer.toArray(new DataConverter[buffer.size()]);
		validateChain();
		accuracy = calcAccuracy();
	}

	public ConverterChain(List<DataConverter> converters) {
		if(converters==null)
			throw new IllegalArgumentException("Invalid converter list"); //$NON-NLS-1$
		if(converters.size()==0)
			throw new IllegalArgumentException("Empty converter list"); //$NON-NLS-1$

		List<DataConverter> buffer = new ArrayList<>();
		for(DataConverter converter : converters) {
			appendConverter(converter, buffer);
		}
		
		this.converters = buffer.toArray(new DataConverter[buffer.size()]);
		validateChain();
		accuracy = calcAccuracy();
	}
	
	private void appendConverter(DataConverter converter, List<DataConverter> list) {
		if(converter instanceof ConverterChain) {
			ConverterChain chain = (ConverterChain) converter;
			for(DataConverter conv : chain.converters) {
				appendConverter(conv, list);
			}
		} else {
			list.add(converter);
		}
	}
	
	private double calcAccuracy() {
		double accuracy = 1.0;
		for(DataConverter converter : converters) {
			accuracy *= converter.getAccuracy();
		}
		return accuracy;
	}
	
	private void validateChain() {
		ContentType type = getInputType();
		for(int i=0; i<converters.length; i++) {
			DataConverter converter = converters[i];
			if(converter.getInputType()!=type)
				throw new IllegalArgumentException(
						"Current type does not match required type for converter at index " //$NON-NLS-1$
						+i+": expected "+converter.getInputType()+" - got "+type); //$NON-NLS-1$ //$NON-NLS-2$
			type = converter.getResultType();
		}
	}

	/**
	 * @see de.ims.icarus.util.data.DataConverter#convert(java.lang.Object)
	 */
	@Override
	public Object convert(Object source, Options options) throws DataConversionException {
		for(int i=0; i<converters.length; i++) {
			DataConverter converter = converters[i];
			try {
				source = converter.convert(source, options);
			} catch(DataConversionException e) {
				throw new DataConversionException(
						"Failed to convert data at index "+i //$NON-NLS-1$
						+" by converter "+converter, e); //$NON-NLS-1$
			}
		}
		return source;
	}

	/**
	 * @see de.ims.icarus.util.data.DataConverter#getInputType()
	 */
	@Override
	public ContentType getInputType() {
		return converters[0].getInputType();
	}

	/**
	 * @see de.ims.icarus.util.data.DataConverter#getResultType()
	 */
	@Override
	public ContentType getResultType() {
		return converters[converters.length-1].getResultType();
	}

	/**
	 * @see de.ims.icarus.util.data.DataConverter#getAccuracy()
	 */
	@Override
	public double getAccuracy() {
		return accuracy;
	}
}
