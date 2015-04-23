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
package de.ims.icarus.search_tools.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchWriter implements SearchXmlConstants {

	private final Search search;
	private final SearchResolver resolver;
	private final SearchResult result;

	private StringBuilder characters = new StringBuilder();

	private char[] indentBuffer;
	private int indent = 0;
	private boolean nested = false;

	private XMLStreamWriter writer;

	public SearchWriter(Search search) {
		if (search == null)
			throw new NullPointerException("Invalid search"); //$NON-NLS-1$

		if(!search.isSerializable())
			throw new IllegalArgumentException("Search not serializable: "+search); //$NON-NLS-1$
		if(!search.isDone())
			throw new IllegalArgumentException("Search not yet finished - cannot serialize: "+search); //$NON-NLS-1$

		this.result = search.getResult();

		if (result == null)
			throw new NullPointerException("Invalid result"); //$NON-NLS-1$
		if(!result.isFinal())
			throw new IllegalArgumentException("Result is not final - cannot serialize: "+search); //$NON-NLS-1$

		this.search = search;
		this.resolver = search.getSearchResolver();

		buildIndentBuffer(10);
	}

	/**
	 * @return the search
	 */
	public Search getSearch() {
		return search;
	}

	private void buildIndentBuffer(int length) {
		indentBuffer = new char[length];

		Arrays.fill(indentBuffer, '\t');
	}

	private void writeIndent() throws XMLStreamException {

		if(indent>=indentBuffer.length) {
			buildIndentBuffer(indent*2);
		}

		writer.writeCharacters(indentBuffer, 0, indent);
	}

	private void writeLineBreak() throws XMLStreamException {
		//TODO maybe reduce linebreak to a single newline character?
		writer.writeCharacters("\r\n"); //$NON-NLS-1$
	}

	public void write(OutputStream out) throws IOException, InterruptedException, XMLStreamException {

		XMLOutputFactory factory = XMLOutputFactory.newFactory();

		writer = factory.createXMLStreamWriter(out, "UTF-8"); //$NON-NLS-1$

		SearchFactory searchFactory = search.getFactory();

		writer.writeStartDocument();

		// Start search
		writeLineBreak();
		startElement(TAG_SEARCH);
		writeAttribute(ATTR_FACTORY, searchFactory.getSerializedForm());

		// Write target
		writeElement(TAG_TARGET, searchFactory.getSerializedTarget(search));
		// Write query
		writeElement(TAG_QUERY, search.getQuery().getQueryString());
		// Write parameters
		Options options = search.getParameters();
		if(!options.isEmpty() && !(options = resolver.prepareWrite(options)).isEmpty()) {
			for(Entry<String, Object> entry : options.entrySet()) {
				writeParameter(entry.getKey(), entry.getValue());
			}
		}

		// Write result
		startElement(TAG_RESULT);
		writeAttribute(ATTR_DIMENSION, String.valueOf(result.getDimension()));

		// Write groups
		for(int i=0; i<result.getDimension(); i++) {
			startElement(TAG_GROUP);
			writeAttribute(ATTR_DIMENSION, String.valueOf(i));

			for(int j = 0; j<result.getInstanceCount(i); j++) {
				//FIXME check if special conversion is required for group instance labels!
				writeElement(TAG_LABEL, String.valueOf(result.getInstanceLabel(i, j)));
			}

			endElement(TAG_GROUP);
		}

		// Now write entries (delegate to resolver)
		resolver.writeResultEntries(this);

		// End result
		endElement(TAG_RESULT);

		// End search
		endElement(TAG_SEARCH);

		writer.writeEndDocument();
	}

	private void writeParameter(String key, Object value) throws XMLStreamException {
		startElement(TAG_PARAMETER);
		writeAttribute(ATTR_KEY, key);

		ParameterType type = ParameterType.getType(value);
		writeAttribute(ATTR_TYPE, type.toString());
		writeText(type.toString(value));
		endElement(TAG_PARAMETER);
	}

	private String asString(int[] items) {
		characters.setLength(0);
		characters.append(items[0]);
		for(int i=1; i<items.length; i++) {
			characters.append(' ').append(items[i]);
		}

		return characters.toString();
	}

	public void writeEntry(ResultEntry entry) throws XMLStreamException {
		startElement(TAG_ENTRY);

		writeAttribute(ATTR_INDEX, String.valueOf(entry.getIndex()));

		for(int i=0; i<entry.getHitCount(); i++) {
			writeElement(TAG_HIT, asString(entry.getHit(i).getIndices()));
		}

		endElement(TAG_ENTRY);
	}

	public void writeEntry(ResultEntry entry, int[] indices) throws XMLStreamException {
		startElement(TAG_ENTRY);

		writeAttribute(ATTR_INDEX, String.valueOf(entry.getIndex()));

		if(indices!=null && indices.length>0) {
			writeAttribute(ATTR_INDICES, asString(indices));
		}

		for(int i=0; i<entry.getHitCount(); i++) {
			writeElement(TAG_HIT, asString(entry.getHit(i).getIndices()));
		}

		endElement(TAG_ENTRY);
	}

	public void writeAttribute(String name, String value) throws XMLStreamException {
		if(value==null) {
			return;
		}
		writer.writeAttribute(name, value);
	}

	public void writeElement(String tag, String text) throws XMLStreamException {
		characters.setLength(0);

		startElement(tag);
		writeText(text);
		endElement(tag);
	}

	public void startElement(String name) throws XMLStreamException {
		writeLineBreak();
		writeIndent();

		writer.writeStartElement(name);
		indent++;
		nested = false;
	}

	public void endElement(String name) throws XMLStreamException {
		indent--;
		if(nested) {
			writeLineBreak();
			writeIndent();
			writer.writeEndElement();
		} else {
			if(flushCharacters()) {
				writer.writeEndElement();
			}
		}

		nested = true;
	}

	public void writeText(String text) throws XMLStreamException {
		if(text==null) {
			return;
		}
		pushCharacters(text);
	}

	private boolean flushCharacters() throws XMLStreamException {
		if(characters.length()==0) {
			return false;
		}

		writer.writeCharacters(characters.toString());

		characters.setLength(0);

		return true;
	}

	private void pushCharacters(String text) {
		characters.append(text);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SearchWriter) {
			return search==((SearchWriter)obj).search;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return search.hashCode();
	}
}
