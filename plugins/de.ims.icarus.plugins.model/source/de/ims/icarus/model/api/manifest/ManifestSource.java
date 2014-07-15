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
package de.ims.icarus.model.api.manifest;

import java.net.URL;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestSource {

	public static ManifestSource customSource(URL url, ClassLoader classLoader, boolean readOnly, boolean template) {
		return new ManifestSource(url, classLoader, readOnly, template);
	}

	public static ManifestSource readOnlySource(URL url, ClassLoader classLoader, boolean template) {
		return new ManifestSource(url, classLoader, true, template);
	}

	public static ManifestSource templateSource(URL url, ClassLoader classLoader, boolean readOnly) {
		return new ManifestSource(url, classLoader, readOnly, true);
	}

	public static ManifestSource staticTemplateSource(URL url, ClassLoader classLoader) {
		return new ManifestSource(url, classLoader, true, true);
	}

	public static ManifestSource liveSource(URL url, ClassLoader classLoader) {
		return new ManifestSource(url, classLoader, false, false);
	}

	private final URL url;
	private final ClassLoader classLoader;
	private final boolean readOnly;
	private final boolean template;

	private ManifestSource(URL url, ClassLoader classLoader, boolean readOnly, boolean template) {
		if (url == null)
			throw new NullPointerException("Invalid url"); //$NON-NLS-1$

		this.url = url;
		this.classLoader = classLoader;
		this.readOnly = readOnly;
		this.template = template;
	}

	/**
	 * @return the url
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @return the template
	 */
	public boolean isTemplate() {
		return template;
	}

	/**
	 * @return the classLoader
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}


}
