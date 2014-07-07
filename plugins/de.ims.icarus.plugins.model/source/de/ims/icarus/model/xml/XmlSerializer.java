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
package de.ims.icarus.model.xml;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface XmlSerializer {

	void startDocument() throws Exception;

	void startElement(String name) throws Exception;

	//FIXME make sure that implementations can handle the difference themselves!
//	void startEmptyElement(String name) throws Exception;

	void writeAttribute(String name, String value) throws Exception;
	void writeAttribute(String name, int value) throws Exception;
	void writeAttribute(String name, long value) throws Exception;
	void writeAttribute(String name, double value) throws Exception;
	void writeAttribute(String name, boolean value) throws Exception;

	void endElement(String name) throws Exception;

	void writeText(String text) throws Exception;

	void endDocument() throws Exception;

	void close() throws Exception;
}
