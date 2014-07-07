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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.java.plugin.registry.Extension;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchDescriptor;
import de.ims.icarus.search_tools.result.EntryBuilder;
import de.ims.icarus.search_tools.result.Hit;
import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.strings.StringPrimitives;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchReader implements SearchXmlConstants {

	private SearchDescriptor descriptor;
	private Options options = new Options();
	private Search search;
	private SearchResult result;
	private SearchResolver resolver;

	private final Path path;

	public SearchReader(Path path) {
		if (path == null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$

		this.path = path;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SearchReader) {
			return path.equals(((SearchReader)obj).path);
		}
		return false;
	}

	/**
	 * @return the path
	 */
	public Path getPath() {
		return path;
	}

	public SearchDescriptor load() throws IOException, SAXException {

		descriptor = new SearchDescriptor();

		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = null;
		try {
			parser = parserFactory.newSAXParser();
		} catch (ParserConfigurationException | SAXException e) {
			throw new IllegalStateException("Failed to create SAXParser", e); //$NON-NLS-1$
		}
		XMLReader reader = null;
		try {
			reader = parser.getXMLReader();
		} catch (SAXException e) {
			throw new IllegalStateException("Failed to create XMLReader", e); //$NON-NLS-1$
		}
		reader.setContentHandler(new Handler());

		try (InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
			if(IOUtil.isGZipSource(path)) {
				reader.parse(new InputSource(new GZIPInputStream(in)));
			}

			reader.parse(new InputSource(in));
		}

		return descriptor;
	}

	private class Handler extends DefaultHandler {

		private EntryBuilder builder;

		private String target;
		private String query;

		private String key;
		private ParameterType type;

		private int dimension;

		private int index;
		private int[] groupIndices;
		private int[] hitIndices;

		private int groupId;
		private List<String> labels = new ArrayList<>();

		private StringBuilder buffer = new StringBuilder();

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			switch (qName) {
			case TAG_SEARCH: {
				String factoryUid = attributes.getValue(ATTR_FACTORY);
				Extension extension = PluginUtil.getExtension(factoryUid);

				descriptor.setFactoryExtension(extension);
				// Instantiate factory
				descriptor.getSearchFactory();
			} break;

			case TAG_PARAMETER: {
				key = attributes.getValue(ATTR_KEY);
				type = ParameterType.parseParameterType(attributes.getValue(ATTR_TYPE));
			} break;

			case TAG_GROUP: {
				groupId = Integer.parseInt(attributes.getValue(ATTR_DIMENSION));
				labels.clear();
			} break;

			case TAG_ENTRY: {
				index = Integer.parseInt(attributes.getValue(ATTR_INDEX));
				if(dimension>0) {
					parseIndices(attributes.getValue(ATTR_INDICES), groupIndices);
				}
			} break;

			case TAG_RESULT: {
				if(query==null)
					throw new IllegalArgumentException("Missing query"); //$NON-NLS-1$
				if(target==null)
					throw new IllegalArgumentException("Missing target"); //$NON-NLS-1$

				descriptor.setParameters(options);

				try {
					descriptor.createSearch(query, target);
				} catch (Exception e) {
					throw new SAXException("Failed to create search", e); //$NON-NLS-1$
				}

				search = descriptor.getSearch();
				resolver = search.getSearchResolver();
				result = search.getResult();
				dimension = result.getDimension();

				groupIndices = new int[dimension];
			} break;

			default:
				break;
			}
		}

		private String getText() {
			String text = buffer.toString();
			buffer.setLength(0);
			return text.trim();
		}

		private void parseIndices(String s, int[] buffer) {
			int index = 0;
			int from = 0;
			int to;
			while((to = s.indexOf(' ', from))!=-1) {
				buffer[index++] = StringPrimitives.parseInt(s, from, to-1);
				from = to+1;
			}
			buffer[index] = StringPrimitives.parseInt(s, from, -1);
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			switch (qName) {
			case TAG_TARGET:
				target = getText();
				break;
			case TAG_QUERY:
				query = getText();
				break;

			case TAG_PARAMETER: {
				Object value = type.parse(getText());
				options.put(key, value);
			} break;

			case TAG_LABEL: {
				labels.add(getText());
			} break;

			case TAG_GROUP: {
				resolver.setGroupLabels(groupId, labels.toArray(new String[labels.size()]));
				labels.clear();
			} break;

			case TAG_SEARCH: {
				resolver.finalizeSearch();
			} break;

			case TAG_HIT: {
				if(hitIndices==null) {
					String[] items = getText().split(" "); //$NON-NLS-1$
					hitIndices = new int[items.length];
					for(int i=0; i<items.length; i++) {
						hitIndices[i] = Integer.parseInt(items[i]);
					}

					builder = new EntryBuilder(items.length);
				} else {
					parseIndices(getText(), hitIndices);
				}

				builder.addHit(new Hit(hitIndices.clone()));
			} break;

			case TAG_ENTRY: {
				builder.setIndex(index);
				ResultEntry entry = builder.toEntry();

				//TODO ensure that groupIndices array does not get corrupted by hijacking
				resolver.addResultEntry(entry, groupIndices);
			} break;

			default:
				break;
			}
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			buffer.append(ch, start, length);
		}
	}
}
