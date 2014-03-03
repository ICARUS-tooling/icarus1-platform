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
package de.ims.icarus.language.model.standard.manifest;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.language.model.api.manifest.Implementation;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.plugins.PluginUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Implementations {

	private abstract static class LazyImplementation implements Implementation {

		private ClassLoader classLoader;
		private final String className;
		private Class<?> clazz;

		private final PluginDescriptor pluginDescriptor;

		public LazyImplementation(String className) {
			this(className, null);
		}

		public LazyImplementation(String className, PluginDescriptor pluginDescriptor) {
			if (className == null)
				throw new NullPointerException("Invalid className"); //$NON-NLS-1$

			this.className = className;
			this.pluginDescriptor = pluginDescriptor;
		}

		/**
		 * @see de.ims.icarus.language.model.api.manifest.Implementation#instantiate(java.lang.Class)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T> T instantiate(Class<T> resultClass)
				throws ClassNotFoundException, IllegalAccessException,
				InstantiationException {

			if(classLoader==null) {
				if(pluginDescriptor!=null) {
					classLoader = PluginUtil.getPluginManager().getPluginClassLoader(pluginDescriptor);
					//FIXME check if we need to manually activate the plugin?
				} else {
					classLoader = Implementations.class.getClassLoader();
				}
			}

			if(clazz==null) {
				clazz = classLoader.loadClass(className);
			}

			if(!resultClass.isAssignableFrom(clazz)) {
				throw new InstantiationException("Incompatible implementation: "+clazz+". Required "+resultClass); //$NON-NLS-1$ //$NON-NLS-2$
			}

			return (T) clazz.newInstance();
		}

		/**
		 * @see de.ims.icarus.language.model.api.xml.XmlElement#writeXml(de.ims.icarus.language.model.api.xml.XmlSerializer)
		 */
		@Override
		public void writeXml(XmlSerializer serializer) throws Exception {
			serializer.startEmptyElement("path-resolver"); //$NON-NLS-1$
			writeXmlContent(serializer);
			serializer.endElement("path-resolver"); //$NON-NLS-1$
		}

		protected abstract void writeXmlContent(XmlSerializer serializer) throws Exception;

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return className.hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof LazyImplementation) {
				LazyImplementation other = (LazyImplementation) obj;
				return className.equals(other.className);
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "LazyImplementation@"+className; //$NON-NLS-1$
		}
	}

	public static Implementation foreignImplementation(final String pluginId, final String className) {
		if (pluginId == null)
			throw new NullPointerException("Invalid pluginId"); //$NON-NLS-1$
		if (className == null)
			throw new NullPointerException("Invalid className"); //$NON-NLS-1$

		PluginDescriptor descriptor = PluginUtil.getPluginRegistry().getPluginDescriptor(pluginId);
		if(descriptor==null)
			throw new IllegalArgumentException("Unknown plugin-id: "+pluginId); //$NON-NLS-1$

		return new LazyImplementation(className, descriptor){

			@Override
			protected void writeXmlContent(XmlSerializer serializer)
					throws Exception {
				serializer.writeAttribute("plugin-id", pluginId); //$NON-NLS-1$
				serializer.writeAttribute("class", className); //$NON-NLS-1$
			}};
	}

	public static Implementation fixedImplementation(final String className) {
		if (className == null)
			throw new NullPointerException("Invalid className"); //$NON-NLS-1$

		return new LazyImplementation(className){

			@Override
			protected void writeXmlContent(XmlSerializer serializer)
					throws Exception {
				serializer.writeAttribute("class", className); //$NON-NLS-1$
			}};
	}

	public static Implementation foreignImplementation(final String extensionId) {
		if (extensionId == null)
			throw new NullPointerException("Invalid extensionId"); //$NON-NLS-1$

		Extension extension = PluginUtil.getExtension(extensionId);
		if(extension==null)
			throw new IllegalArgumentException("Unknown extension-id"); //$NON-NLS-1$

		Extension.Parameter param = extension.getParameter("class"); //$NON-NLS-1$
		if(param==null)
			throw new IllegalArgumentException("Extension does not declare a valid class parameter: "+extensionId); //$NON-NLS-1$

		return new LazyImplementation(param.valueAsString(), extension.getDeclaringPluginDescriptor()){

			@Override
			protected void writeXmlContent(XmlSerializer serializer)
					throws Exception {
				serializer.writeAttribute("extension-id", extensionId); //$NON-NLS-1$
			}};
	}
}
