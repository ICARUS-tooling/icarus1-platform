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
package de.ims.icarus.language.model.xml.stream;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.ims.icarus.language.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class XmlStreamSerializer implements XmlSerializer {

	private final XMLStreamWriter writer;

	private Stack<String> trace = new Stack<>();
	private Map<String, String> attributes = new LinkedHashMap<>();
	private StringBuilder characters = new StringBuilder();

	private char[] indentBuffer;

	private int indent = 1;
	private boolean nested = false;

	public XmlStreamSerializer(XMLStreamWriter writer) {
		if (writer == null)
			throw new NullPointerException("Invalid writer"); //$NON-NLS-1$

		this.writer = writer;

		buildIndentBuffer(10);
	}

	private void buildIndentBuffer(int length) {
		indentBuffer = new char[length];

		Arrays.fill(indentBuffer, '\t');
		indentBuffer[0] = '\n';
	}

	private void writeIndent() throws XMLStreamException {

		if(indent>=indentBuffer.length) {
			buildIndentBuffer(indent*2);
		}

		writer.writeCharacters(indentBuffer, 0, indent);
	}

	private void flushAttributes() throws XMLStreamException {
		if(attributes.isEmpty()) {
			return;
		}

		for(Entry<String, String> entry : attributes.entrySet()) {
			writer.writeAttribute(entry.getKey(), entry.getValue());
		}

		attributes.clear();
	}

	private boolean flushCharacters() throws XMLStreamException {
		if(characters.length()==0) {
			return false;
		}

		writer.writeCharacters(characters.toString());

		characters.setLength(0);

		return true;
	}

	private void pushElement(String localName) throws XMLStreamException {
		if (localName == null)
			throw new NullPointerException("Invalid localName"); //$NON-NLS-1$

		flushAttributes();

		trace.push(localName);

		writeIndent();
		indent++;

		writer.writeStartElement(localName);
		nested = false;
	}

	private void popElement(String localName) throws XMLStreamException {
		if (localName == null)
			throw new NullPointerException("Invalid localName"); //$NON-NLS-1$
		if (!localName.equals(trace.pop()))
			throw new IllegalArgumentException("Unexpected end tag: "+localName); //$NON-NLS-1$

		flushAttributes();
		if(flushCharacters()) {
			writer.writeEndElement();
			indent--;
		} else {
			indent--;
			if(nested) {
				writeIndent();
			}
			writer.writeEndElement();
		}

		nested = true;
	}

	private void pushAttribute(String localName, String value) {
		if (localName == null)
			throw new NullPointerException("Invalid localName"); //$NON-NLS-1$
		if (value == null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$

		attributes.put(localName, value);
	}

	private void pushCharacters(String text) {
		characters.append(text);
	}

	/**
	 * @see de.ims.icarus.language.model.xml.XmlSerializer#startElement(java.lang.String)
	 */
	@Override
	public void startElement(String name) throws XMLStreamException {
		pushElement(name);
	}

	/**
	 * @see de.ims.icarus.language.model.xml.XmlSerializer#writeAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void writeAttribute(String name, String value) throws XMLStreamException {
		pushAttribute(name, value);
	}

	/**
	 * @see de.ims.icarus.language.model.xml.XmlSerializer#writeAttribute(java.lang.String, int)
	 */
	@Override
	public void writeAttribute(String name, int value) throws XMLStreamException {
		pushAttribute(name, String.valueOf(value));
	}

	/**
	 * @see de.ims.icarus.language.model.xml.XmlSerializer#writeAttribute(java.lang.String, double)
	 */
	@Override
	public void writeAttribute(String name, double value) throws XMLStreamException {
		pushAttribute(name, String.valueOf(value));
	}

	/**
	 * @see de.ims.icarus.language.model.xml.XmlSerializer#writeAttribute(java.lang.String, boolean)
	 */
	@Override
	public void writeAttribute(String name, boolean value) throws XMLStreamException {
		pushAttribute(name, String.valueOf(value));
	}

	/**
	 * @see de.ims.icarus.language.model.xml.XmlSerializer#endElement(java.lang.String)
	 */
	@Override
	public void endElement(String name) throws XMLStreamException {
		popElement(name);
	}

	/**
	 * @see de.ims.icarus.language.model.xml.XmlSerializer#writeText(java.lang.String)
	 */
	@Override
	public void writeText(String text) throws XMLStreamException {
		pushCharacters(text);
	}

	/**
	 * @see de.ims.icarus.language.model.xml.XmlSerializer#startDocument()
	 */
	@Override
	public void startDocument() throws XMLStreamException {
		writer.writeStartDocument();
	}

	/**
	 * @see de.ims.icarus.language.model.xml.XmlSerializer#endDocument()
	 */
	@Override
	public void endDocument() throws XMLStreamException {
		writer.writeEndDocument();
	}

	/**
	 * @see de.ims.icarus.language.model.xml.XmlSerializer#close()
	 */
	@Override
	public void close() throws Exception {
		writer.flush();
		writer.close();
	}

}
