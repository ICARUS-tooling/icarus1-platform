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
package de.ims.icarus.language.model.xml.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.language.model.manifest.Implementation;
import de.ims.icarus.language.model.manifest.Template;
import de.ims.icarus.language.model.meta.ValueType;
import de.ims.icarus.language.model.registry.CorpusRegistry;
import de.ims.icarus.language.model.standard.manifest.Implementations;
import de.ims.icarus.logging.LogReport;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class ModelElementHandler<E extends Object> {

	private final String tag;

	private final StringBuilder buffer = new StringBuilder();

	private ModelElementHandler<?> parent;

	private boolean templateMode = false;

	protected E element;

	protected ModelElementHandler(String tag) {
		if (tag == null)
			throw new NullPointerException("Invalid tag"); //$NON-NLS-1$

		this.tag = tag;

		revive();
	}

	public String getTag() {
		return tag;
	}

	public boolean isTemplateMode() {
		return templateMode;
	}

	void setTemplateMode(boolean templateMode) {
		this.templateMode = templateMode;
	}

	void setParent(ModelElementHandler<?> parent) {
		if (parent == null)
			throw new NullPointerException("Invalid parent"); //$NON-NLS-1$

		this.parent = parent;
	}

	public ModelElementHandler<?> getParent() {
		if(parent==null)
			throw new IllegalStateException("No parent available"); //$NON-NLS-1$

		return parent;
	}

	public LogReport getReport() {
		return getParent().getReport();
	}

	public HandlerPool getPool() {
		return getParent().getPool();
	}

	public Template resolveTemplate(String templateId) {
		try {
			return CorpusRegistry.getInstance().getTemplate(templateId);
		} catch(IllegalArgumentException e) {
			// Ignore missing template - logging is done in the calling methods!
			return null;
		}
	}

	protected void registerTemplate(Template template) {
		getParent().registerTemplate(template);
	}

	public <T extends Object> T lookup(String id, Class<T> clazz) {
		return getParent().lookup(id, clazz);
	}

	protected void register(String id, Object item) {
		getParent().register(id, item);
	}

//	protected void map(Template source, Object item) {
//		getParent().map(source, item);
//	}
//
//	public <T extends Object> T replace(T source) {
//		return getParent().replace(source);
//	}

	public void clear() {
		parent = null;
		buffer.setLength(0);
		element = null;
		templateMode = false;

		release();
	}

	protected void release() {
		// for subclasses
	}

	protected void revive() {
		// for subclasses
	}

	void feedCharacters(char[] ch, int start, int length) {
		buffer.append(ch, start, length);
	}

	protected String getText() {
		String text = buffer.length()==0 ? "" : buffer.toString(); //$NON-NLS-1$
		buffer.setLength(0);

		return text;
	}

	/**
	 * @return the element
	 */
	public E getElement() {
		if(element==null)
			throw new IllegalStateException("No element available"); //$NON-NLS-1$

		return element;
	}

	public <T extends Template> T defaultGetTemplate(Attributes attributes, Class<T> clazz) {
		String templateId = attributes.getValue("template-id"); //$NON-NLS-1$
		if(templateId==null) {
			return null;
		}

		Object template = resolveTemplate(templateId);
		if(template==null) {
			getReport().error("No such template: "+templateId); //$NON-NLS-1$
			return null;
		}

		if(!clazz.isAssignableFrom(template.getClass())) {
			getReport().error("Incompatible template: "+templateId+". Expected "+clazz); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}

		return clazz.cast(template);
	}

	public Implementation createImplementation(Attributes attributes) {
		if (attributes == null)
			throw new NullPointerException("Invalid attributes"); //$NON-NLS-1$

		String extensionId = attributes.getValue("extension-id"); //$NON-NLS-1$
		String pluginId = attributes.getValue("plugin-id"); //$NON-NLS-1$
		String className = attributes.getValue("class"); //$NON-NLS-1$

		if(extensionId!=null) {
			return Implementations.foreignImplementation(extensionId);
		} else if(className!=null) {
			if(pluginId!=null) {
				return Implementations.foreignImplementation(pluginId, className);
			} else {
				return Implementations.fixedImplementation(className);
			}
		}

		return null;
	}

	public ModelElementHandler<?> startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		throw new SAXException("Unexpected start element: "+localName); //$NON-NLS-1$
	}

	public ModelElementHandler<?> endElement(String uri, String localName, String qName)
			throws SAXException {
//		if(tag.equals(localName)) {
//			return getParent();
//		} else
			throw new SAXException("Unexpected end element: "+localName); //$NON-NLS-1$
	}

	protected long longText() {
		String s = getText();
		return s.isEmpty() ? 0 : Long.parseLong(s);
	}

	protected int intText() {
		String s = getText();
		return s.isEmpty() ? 0 : Integer.parseInt(s);
	}

	protected float floatText() {
		String s = getText();
		return s.isEmpty() ? 0 : Float.parseFloat(s);
	}

	protected double doubleText() {
		String s = getText();
		return s.isEmpty() ? 0 : Double.parseDouble(s);
	}

	protected boolean booleanText() {
		String s = getText();
		return s.isEmpty() ? false : Boolean.parseBoolean(s);
	}

	protected boolean bitText() {
		return "1".equals(getText()); //$NON-NLS-1$
	}

	public static String stringValue(Attributes attr, String key) {
		return attr.getValue(key);
	}

	public static long longValue(String s) {
		return Long.parseLong(s);
	}

	public static long longValue(Attributes attr, String key) {
		return longValue(attr.getValue(key));
	}

	public static double doubleValue(String s) {
		return Double.parseDouble(s);
	}

	public static double doubleValue(Attributes attr, String key) {
		return doubleValue(attr.getValue(key));
	}

	public static float floatValue(String s) {
		return Float.parseFloat(s);
	}

	public static float floatValue(Attributes attr, String key) {
		return floatValue(attr.getValue(key));
	}

	public static int intValue(String s) {
		return Integer.parseInt(s);
	}

	public static int intValue(Attributes attr, String key) {
		return intValue(attr.getValue(key));
	}

	public static boolean booleanValue(String s) {
		return s!=null && ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean booleanValue(Attributes attr, String key) {
		return bitValue(attr.getValue(key));
	}

	public static boolean bitValue(String s) {
		return "1".equals(s); //$NON-NLS-1$
	}

	public static boolean bitValue(Attributes attr, String key) {
		return bitValue(attr.getValue(key));
	}

	public static ValueType typeValue(Attributes attr) {
		String s = attr.getValue("type"); //$NON-NLS-1$
		return typeValue(s);
	}

	public static ValueType typeValue(String s) {
		return s==null ? null : ValueType.parseValueType(s);
	}

	public static Object value(Attributes attr, String key, ValueType valueType) {
		return value(attr.getValue(key), valueType);
	}

	public static Object value(String s, ValueType valueType) {
		switch (valueType) {
		case BOOLEAN:
			return booleanValue(s);
		case INTEGER:
			return intValue(s);
		case DOUBLE:
			return doubleValue(s);
		case STRING:
			return s;

		default:
			throw new IllegalArgumentException("Cannot parse custom or default type: "+s); //$NON-NLS-1$
		}
	}
}
