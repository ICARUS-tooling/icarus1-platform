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
package de.ims.icarus.model.util.types;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import org.java.plugin.registry.Extension;

import de.ims.icarus.model.xml.XmlResource;
import de.ims.icarus.model.xml.sax.IconWrapper;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum ValueType implements XmlResource {

	UNKNOWN("unknown", Object.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new IllegalStateException("Cannot parse data of type 'unknown'"); //$NON-NLS-1$
		}
	},

	// External
	CUSTOM("custom", Object.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new IllegalStateException("Cannot parse data of type 'custom'"); //$NON-NLS-1$
		}
	},
	EXTENSION("extension", Extension.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return PluginUtil.getExtension(s);
		}

		/**
		 *
		 * @see de.ims.icarus.model.util.types.ValueType#toString(java.lang.Object)
		 */
		@Override
		public String toString(Object value) {
			Extension extension = (Extension) value;
			return extension.getUniqueId();
		}
	},

	ENUM("enum", Enum.class) { //$NON-NLS-1$
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			String[] parts = s.split("@"); //$NON-NLS-1$
			try {
				Class<?> clazz = classLoader.loadClass(parts[0]);

				return Enum.valueOf((Class<Enum>)clazz, parts[1]);
			} catch (ClassNotFoundException e) {
				throw new CorruptedStateException("Unable to parse enum parameter: "+s, e); //$NON-NLS-1$
			}
		}

		/**
		 *
		 * @see de.ims.icarus.model.util.types.ValueType#toString(java.lang.Object)
		 */
		@Override
		public String toString(Object value) {
			Enum<?> enumType = (Enum<?>) value;

			return enumType.getDeclaringClass().getName()+"@"+enumType.name(); //$NON-NLS-1$
		}
	},

	// "Primitive"
	STRING("string", String.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return s;
		}
	},
	BOOLEAN("boolean", Boolean.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Boolean.parseBoolean(s);
		}
	},
	INTEGER("integer", Integer.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Integer.parseInt(s);
		}
	},
	LONG("long", Long.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Long.parseLong(s);
		}
	},
	DOUBLE("double", Double.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Double.parseDouble(s);
		}
	},
	FLOAT("float", Float.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Float.parseFloat(s);
		}
	},

	// Resource links
	URL("url", URL.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			try {
				return new URL(s);
			} catch (MalformedURLException e) {
				throw new CorruptedStateException("Serialized form of url is invalid: "+s); //$NON-NLS-1$
			}
		}

		/**
		 *
		 * @see de.ims.icarus.model.util.types.ValueType#toString(java.lang.Object)
		 */
		@Override
		public String toString(Object value) {
			URL url = (URL) value;
			return url.toExternalForm();
		}
	},
	URL_RESOURCE("url-resource", Link.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new IllegalStateException("Cannot parse data of type 'url-resource'"); //$NON-NLS-1$
		}

		/**
		 *
		 * @see de.ims.icarus.model.util.types.ValueType#toString(java.lang.Object)
		 */
		@Override
		public String toString(Object value) {
			throw new IllegalStateException("Cannot serialize data of type 'url-resource'"); //$NON-NLS-1$
		}
	},

	// Predefined images
	IMAGE("image", Icon.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return new IconWrapper(s);
		}

		/**
		 *
		 * @see de.ims.icarus.model.util.types.ValueType#toString(java.lang.Object)
		 */
		@Override
		public String toString(Object value) {
			if(value instanceof IconWrapper) {
				return ((IconWrapper)value).getXmlValue();
			} else
				throw new IllegalArgumentException("Cannot serialize icon: "+value); //$NON-NLS-1$
		}
	},
	IMAGE_RESOURCE("image-resource", IconLink.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new IllegalStateException("Cannot parse data of type 'image-resource'"); //$NON-NLS-1$
		}

		/**
		 *
		 * @see de.ims.icarus.model.util.types.ValueType#toString(java.lang.Object)
		 */
		@Override
		public String toString(Object value) {
			throw new IllegalStateException("Cannot serialize data of type 'image-resource'"); //$NON-NLS-1$
		}
	};

	private final Class<?> baseClass;
	private final String xmlForm;

	private ValueType(String xmlForm, Class<?> baseClass) {
		this.baseClass = baseClass;
		this.xmlForm = xmlForm;
	}

	public abstract Object parse(String s, ClassLoader classLoader);
	public String toString(Object value) {
		return String.valueOf(value);
	}

	/**
	 * @see de.ims.icarus.model.api.xml.XmlResource#getXmlValue()
	 */
	@Override
	public String getXmlValue() {
		return xmlForm;
	}

	private static Map<String, ValueType> xmlLookup;

	public static ValueType parseValueType(String s) {
		if(xmlLookup==null) {
			Map<String, ValueType> map = new HashMap<>();
			for(ValueType type : values()) {
				map.put(type.xmlForm, type);
			}
			xmlLookup = map;
		}

		return xmlLookup.get(s);
	}

	public boolean isValidValue(Object value) {
		return value!=null && baseClass.isAssignableFrom(value.getClass());
	}
}
