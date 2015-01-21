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
package de.ims.icarus.model.standard.manifest;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.eval.Expression;
import de.ims.icarus.eval.ExpressionFactory;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.xml.ModelXmlAttributes;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlTags;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.plugins.PluginUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ExpressionXmlHandler implements ModelXmlHandler, ModelXmlAttributes, ModelXmlTags {

	private final ExpressionFactory factory;

	public ExpressionXmlHandler(ExpressionFactory factory) {
		if (factory == null)
			throw new NullPointerException("Invalid factory"); //$NON-NLS-1$

		this.factory = factory;
	}

	/**
	 * Default constructor. Uses the basic {@link ExpressionFactory} implemenattion
	 * as factory to build the expression object.
	 */
	public ExpressionXmlHandler() {

		this.factory = new ExpressionFactory();
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#startElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (qName) {
		case TAG_EVAL: {
			// no-op
		} break;

		case TAG_CODE: {
			// no-op
		} break;

		case TAG_VARIABLE: {
			String name = ModelXmlUtils.normalize(attributes, ATTR_NAME);
			String classname = ModelXmlUtils.normalize(attributes, ATTR_NAMESPACE);
			String pluginId = ModelXmlUtils.normalize(attributes, ATTR_PLUGIN_ID);

			ClassLoader classLoader = getClass().getClassLoader();

			if(pluginId!=null) {
				classLoader = PluginUtil.getClassLoader(pluginId);
			}

			Class<?> namespace;
			try {
				namespace = classLoader.loadClass(classname);
			} catch (ClassNotFoundException e) {
				throw new SAXException("Unable to load namespace class for variable: "+name, e); //$NON-NLS-1$
			}


			factory.addVariable(name, namespace);
		} break;

		default:
			throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+TAG_EVAL+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return this;
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {
		switch (qName) {
		case TAG_EVAL: {
			return null;
		}

		case TAG_CODE: {
			factory.setCode(text);
		} break;

		case TAG_VARIABLE: {
			// no-op
		} break;

		default:
			throw new SAXException("Unrecognized end tag  '"+qName+"' in "+TAG_EVAL+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return this;
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {
		throw new UnsupportedOperationException();
	}

	public Expression createExpression() {
		return factory.build();
	}
}
