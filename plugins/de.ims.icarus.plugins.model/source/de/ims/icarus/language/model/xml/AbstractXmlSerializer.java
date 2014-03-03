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
package de.ims.icarus.language.model.xml;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractXmlSerializer implements XmlSerializer {

	private Stack<String> trace = new Stack<>();
	private Map<String, String> attributes = new LinkedHashMap<>();
	private StringBuilder buffer = new StringBuilder();

	private void flushAttributes() {
		for(Entry<String, String> entry : attributes.entrySet()) {

		}
	}

	private void flushCharacters() {

	}

	protected abstract void writeAttribute0(String name, String value);

	protected abstract void writeBeginElement0(String name);

	protected abstract void writeEndElement0(String name, boolean empty);

	protected abstract void writeCharacters0(String text);

	/**
	 * @see de.ims.icarus.language.model.api.xml.XmlSerializer#startElement(java.lang.String)
	 */
	@Override
	public void startElement(String name) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.api.xml.XmlSerializer#writeAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void writeAttribute(String name, String value) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.api.xml.XmlSerializer#writeAttribute(java.lang.String, int)
	 */
	@Override
	public void writeAttribute(String name, int value) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.api.xml.XmlSerializer#writeAttribute(java.lang.String, double)
	 */
	@Override
	public void writeAttribute(String name, double value) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.api.xml.XmlSerializer#writeAttribute(java.lang.String, boolean)
	 */
	@Override
	public void writeAttribute(String name, boolean value) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.api.xml.XmlSerializer#endElement(java.lang.String)
	 */
	@Override
	public void endElement(String name) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.api.xml.XmlSerializer#writeText(java.lang.String)
	 */
	@Override
	public void writeText(String text) throws IOException {
		// TODO Auto-generated method stub

	}

}
