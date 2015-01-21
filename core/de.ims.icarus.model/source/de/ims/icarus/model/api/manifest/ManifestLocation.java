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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.ims.icarus.logging.LoggerFactory;

/**
 * Models information about the physical location of a manifest resource
 * and defines whether the source is read-only and/or template-only.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class ManifestLocation {

	private final ClassLoader classLoader;
	private final boolean readOnly;
	private final boolean template;

	protected ManifestLocation(ClassLoader classLoader, boolean readOnly, boolean template) {
		if(classLoader==null) {
			classLoader = getClass().getClassLoader();
		}

		this.classLoader = classLoader;
		this.readOnly = readOnly;
		this.template = template;
	}

	public URL getUrl() {
		return null;
	}

	/**
	 * Open the underlying resource for read access.
	 *
	 * @return
	 * @throws IOException
	 * @throws UnsupportedOperationException in case the location is write-only
	 */
	public abstract InputStream getInputStream() throws IOException;

	/**
	 * Open the underlying resource for write access.
	 *
	 * @return
	 * @throws IOException
	 * @throws UnsupportedOperationException in case the location is read-only
	 */
	public abstract OutputStream getOutputStream() throws IOException;

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
	 * Returns the {@code ClassLoader} instance that is associated with the
	 * physical location this {@code ManifestLocation} wraps.
	 *
	 * @return the classLoader
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public static class URLManifestLocation extends ManifestLocation {

		private final URL url;

		/**
		 * @param classLoader
		 * @param readOnly
		 * @param template
		 */
		public URLManifestLocation(URL url, ClassLoader classLoader,
				boolean readOnly, boolean template) {
			super(classLoader, readOnly, template);

			if (url == null)
				throw new NullPointerException("Invalid url"); //$NON-NLS-1$

			this.url = url;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ManifestLocation#getUrl()
		 */
		@Override
		public URL getUrl() {
			return url;
		}

		/**
		 * @throws IOException
		 * @see de.ims.icarus.model.api.manifest.ManifestLocation#getInputStream()
		 */
		@Override
		public InputStream getInputStream() throws IOException {
			return url.openConnection().getInputStream();
		}

		/**
		 * @throws IOException
		 * @see de.ims.icarus.model.api.manifest.ManifestLocation#getOutputStream()
		 */
		@Override
		public OutputStream getOutputStream() throws IOException {
			return url.openConnection().getOutputStream();
		}

	}

	public static class FileManifestLocation extends ManifestLocation {

		private final Path path;

		/**
		 * @param classLoader
		 * @param readOnly
		 * @param template
		 */
		public FileManifestLocation(Path path, ClassLoader classLoader,
				boolean readOnly, boolean template) {
			super(classLoader, readOnly, template);

			if (path == null)
				throw new NullPointerException("Invalid path"); //$NON-NLS-1$

			this.path = path;
		}

		/**
		 * @return the path
		 */
		public Path getPath() {
			return path;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ManifestLocation#getUrl()
		 */
		@Override
		public URL getUrl() {
			try {
				return path.toUri().toURL();
			} catch (MalformedURLException e) {

				LoggerFactory.warning(this, "Failed to convert path into URL", e); //$NON-NLS-1$

				return null;
			}
		}

		/**
		 * @throws IOException
		 * @see de.ims.icarus.model.api.manifest.ManifestLocation#getInputStream()
		 */
		@Override
		public InputStream getInputStream() throws IOException {
			return Files.newInputStream(path, StandardOpenOption.READ);
		}

		/**
		 * @throws IOException
		 * @see de.ims.icarus.model.api.manifest.ManifestLocation#getOutputStream()
		 */
		@Override
		public OutputStream getOutputStream() throws IOException {
			return Files.newOutputStream(path,
					StandardOpenOption.WRITE,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		}
	}

	public static class VirtualManifestInputLocation extends ManifestLocation {

		private String content;

		public VirtualManifestInputLocation(ClassLoader classLoader, boolean template) {
			super(classLoader, true, template);
		}

		public VirtualManifestInputLocation(String content, ClassLoader classLoader, boolean template) {
			super(classLoader, true, template);

			setContent(content);
		}

		public String getContent() {
			return content;
		}

		public void setContent(String text) {
			if (text == null)
				throw new NullPointerException("Invalid text"); //$NON-NLS-1$

			content = text;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ManifestLocation#getInputStream()
		 */
		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(getContent().getBytes(StandardCharsets.UTF_8));
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ManifestLocation#getOutputStream()
		 */
		@Override
		public OutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}

	}

	public static class VirtualManifestOutputLocation extends ManifestLocation {

		private ByteArrayOutputStream buffer;

		public VirtualManifestOutputLocation(ClassLoader classLoader, boolean template) {
			super(classLoader, false, template);
		}

		public String getContent() {
			try {
				return buffer.toString("UTF-8"); //$NON-NLS-1$
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException("UTF-8 encoding must be supported", e); //$NON-NLS-1$
			}
		}

		public void reset() {
			buffer = new ByteArrayOutputStream();
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ManifestLocation#getInputStream()
		 */
		@Override
		public InputStream getInputStream() throws IOException {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ManifestLocation#getOutputStream()
		 */
		@Override
		public OutputStream getOutputStream() throws IOException {
			reset();
			return buffer;
		}

	}
}
