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
package de.ims.icarus.xml;

import javax.xml.bind.ValidationException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class StAXUtil {
	
	private static final Object lock = new Object();
	
	private static XMLInputFactory inputFactory;
	private static XMLOutputFactory outputFactory;

	private StAXUtil() {
		// no-op
	}
	
	public static XMLInputFactory getSharedInputFactory() {
		if(inputFactory==null) {
			synchronized (lock) {
				if(inputFactory==null) {
					inputFactory = XMLInputFactory.newInstance();
				}
			}
		}
		return inputFactory;
	}
	
	public static XMLOutputFactory getSharedOutputFactory() {
		if(outputFactory==null) {
			synchronized (lock) {
				if(outputFactory==null) {
					outputFactory = XMLOutputFactory.newInstance();
				}
			}
		}
		return outputFactory;
	}
	
	public static String errorText(XMLStreamReader reader) {
		Location location = reader.getLocation();
		return "ParseError at [row,col]:["+location.getLineNumber()+","+ //$NON-NLS-1$ //$NON-NLS-2$
        	location.getColumnNumber()+"]"; //$NON-NLS-1$
	}
	
	public static String errorText(XMLStreamReader reader, String msg) {
		Location location = reader.getLocation();
		return "ParseError at [row,col]:["+location.getLineNumber()+","+ //$NON-NLS-1$ //$NON-NLS-2$
        	location.getColumnNumber()+"]\n"+msg; //$NON-NLS-1$
	}

	public static String readText(XMLStreamReader reader, String tag)
			throws XMLStreamException, ValidationException {
		String value = null;
		while(true) {
			int eventType = reader.nextTag();
			if(eventType==XMLStreamConstants.START_ELEMENT) {
				String parsedTag = reader.getLocalName();
				if(!tag.equals(parsedTag)) {
					throw new ValidationException(errorText(reader, "Expected '"+parsedTag+"' - found '"+tag+"'")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				value = reader.getElementText();
			} else if(eventType==XMLStreamConstants.END_ELEMENT) {
				break;
			} else {
				throw new ValidationException(errorText(reader, "Unexpected xml")); //$NON-NLS-1$
			}
		}
		return value;
	}
}
