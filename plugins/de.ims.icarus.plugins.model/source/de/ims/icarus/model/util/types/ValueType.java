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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import de.ims.icarus.eval.Expression;
import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.xml.XmlResource;
import de.ims.icarus.model.xml.sax.IconWrapper;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class ValueType implements XmlResource {

	private final Class<?> baseClass;
	private final String xmlForm;

	private static Map<String, ValueType> xmlLookup = new HashMap<>();

	public static ValueType parseValueType(String s) {
		return xmlLookup.get(s);
	}

	private ValueType(String xmlForm, Class<?> baseClass) {
		this.baseClass = baseClass;
		this.xmlForm = xmlForm;

		xmlLookup.put(xmlForm, this);
	}

	//FIXME add support for vector types (dimension + component type)

	public String toString(Object value) {
		return String.valueOf(value);
	}

	public abstract Object parse(String s, ClassLoader classLoader);

	/**
	 * @see de.ims.icarus.model.api.xml.XmlResource#getXmlValue()
	 */
	@Override
	public String getXmlValue() {
		return xmlForm;
	}

	public static final ValueType UNKNOWN = new ValueType("unknown", Object.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new IllegalStateException("Cannot parse data of type 'unknown'"); //$NON-NLS-1$
		}
	};

	// External
	public static final ValueType CUSTOM = new ValueType("custom", Object.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new IllegalStateException("Cannot parse data of type 'custom'"); //$NON-NLS-1$
		}
	};

	/**
	 * To reduce dependency we only store the extension's unique id, not the extension itself!
	 */
	public static final ValueType EXTENSION = new ValueType("extension", String.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return s;
		}

//		/**
//		 *
//		 * @see de.ims.icarus.model.util.types.ValueType#toString(java.lang.Object)
//		 */
//		@Override
//		public String toString(Object value) {
//			Extension extension = (Extension) value;
//			return extension.getUniqueId();
//		}
	};

	public static final ValueType ENUM = new ValueType("enum", Enum.class) { //$NON-NLS-1$
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			String[] parts = s.split("@"); //$NON-NLS-1$
			try {
				Class<?> clazz = classLoader.loadClass(parts[0]);

				return Enum.valueOf((Class<Enum>) clazz, parts[1]);
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
	};

	// "Primitive"
	public static final ValueType STRING = new ValueType("string", String.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return s;
		}
	};

	public static final ValueType BOOLEAN = new ValueType("boolean", Boolean.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Boolean.parseBoolean(s);
		}
	};

	public static final ValueType INTEGER = new ValueType("integer", Integer.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Integer.parseInt(s);
		}
	};

	public static final ValueType LONG = new ValueType("long", Long.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Long.parseLong(s);
		}
	};

	public static final ValueType DOUBLE = new ValueType("double", Double.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Double.parseDouble(s);
		}
	};

	public static final ValueType FLOAT = new ValueType("float", Float.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Float.parseFloat(s);
		}
	};

	// Resource links
	public static final ValueType URL = new ValueType("url", Url.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			try {
				return new Url(s);
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
			Url url = (Url) value;
			return url.getURL().toExternalForm();
		}
	};

	// Resource links
	public static final ValueType FILE = new ValueType("file", Path.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			return Paths.get(s);
		}

		/**
		 *
		 * @see de.ims.icarus.model.util.types.ValueType#toString(java.lang.Object)
		 */
		@Override
		public String toString(Object value) {
			Path path = (Path) value;
			return path.toString();
		}
	};

	public static final ValueType URL_RESOURCE = new ValueType("url-resource", Link.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new UnsupportedOperationException("Cannot parse data of type 'url-resource'"); //$NON-NLS-1$
		}

		/**
		 *
		 * @see de.ims.icarus.model.util.types.ValueType#toString(java.lang.Object)
		 */
		@Override
		public String toString(Object value) {
			throw new UnsupportedOperationException("Cannot serialize data of type 'url-resource'"); //$NON-NLS-1$
		}
	};

	// Predefined images
	public static final ValueType IMAGE = new ValueType("image", Icon.class) { //$NON-NLS-1$
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
	};

	public static final ValueType IMAGE_RESOURCE = new ValueType("image-resource", IconLink.class) { //$NON-NLS-1$
		@Override
		public Object parse(String s, ClassLoader classLoader) {
			throw new UnsupportedOperationException("Cannot parse data of type 'image-resource'"); //$NON-NLS-1$
		}

		/**
		 *
		 * @see de.ims.icarus.model.util.types.ValueType#toString(java.lang.Object)
		 */
		@Override
		public String toString(Object value) {
			throw new UnsupportedOperationException("Cannot serialize data of type 'image-resource'"); //$NON-NLS-1$
		}
	};

	public boolean isValidValue(Object value) {
		return value!=null && baseClass.isAssignableFrom(value.getClass());
	}

	public boolean isValidType(Class<?> type) {
		return baseClass.isAssignableFrom(type);
	}

	/**
	 * Returns a collection view on all the available value types
	 */
	public static Collection<ValueType> values() {
		return CollectionUtils.getCollectionProxy(xmlLookup.values());
	}

	/**
	 * Creates a set view that contains all available value types except the
	 * ones specified in the {@code exclusions} varargs parameter.
	 * @param exclusions
	 * @return
	 */
	public static Set<ValueType> filterWithout(ValueType...exclusions) {
		Set<ValueType> filter = new HashSet<>(xmlLookup.values());

		if(exclusions!=null) {
			for(ValueType type : exclusions) {
				filter.remove(type);
			}
		}

		return filter;
	}

	/**
	 * Creates a set view that contains only value types specified
	 * in the {@code exclusions} varargs parameter.
	 * @param inclusive
	 * @return
	 */
	public static Set<ValueType> filterIncluding(ValueType...inclusive) {
		Set<ValueType> filter = new HashSet<>();

		if(inclusive!=null) {
			for(ValueType type : inclusive) {
				filter.add(type);
			}
		}

		return filter;
	}

	public Class<?> checkValue(Object value) {
		Class<?> type = value.getClass();
		if(Expression.class.isAssignableFrom(type)) {
			type = ((Expression)value).getReturnType();
		}

		if(!baseClass.isAssignableFrom(type))
			throw new ModelException(ModelError.MANIFEST_TYPE_CAST,
					"Incompatible value type "+type.getName()+" for value-type "+xmlForm+" - expected "+baseClass.getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return type;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return xmlForm.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ValueType) {
			return xmlForm.equals(((ValueType)obj).xmlForm);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ValueType@"+xmlForm; //$NON-NLS-1$
	}
}
