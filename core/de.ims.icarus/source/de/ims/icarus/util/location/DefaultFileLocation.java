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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.util.location;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.ims.icarus.io.IOUtil;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultFileLocation implements Location {

	private final Path path;

	public DefaultFileLocation(Path path) {
		if(path==null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$

		this.path = path;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Location) {
			return Locations.equals(this, (Location)obj);
		}

		return false;
	}

	/**
	 * @see de.ims.icarus.util.location.Location#getURL()
	 */
	@Override
	public URL getURL() {
		try {
			return path.toUri().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * @see de.ims.icarus.util.location.Location#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return true;
	}

	/**
	 * @see de.ims.icarus.util.location.Location#getLocalPath()
	 */
	@Override
	public Path getLocalPath() {
		return path;
	}

	/**
	 * @see de.ims.icarus.util.location.Location#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() throws IOException {
		OutputStream out = Files.newOutputStream(path);
		if(IOUtil.isGZipSource(path)) {
			out = new GZIPOutputStream(out);
		}

		return out;
	}

	/**
	 * @see de.ims.icarus.util.location.Location#openInputStream()
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		if(path==null)
			throw new IllegalStateException("No path defined"); //$NON-NLS-1$
		if(Files.notExists(path, LinkOption.NOFOLLOW_LINKS))
			throw new FileNotFoundException("File not found: "+path.toString()); //$NON-NLS-1$
		if(Files.size(path)==0)
			throw new IOException("File is empty: "+path.toString()); //$NON-NLS-1$

		InputStream in = Files.newInputStream(path);
		if(IOUtil.isGZipSource(path)) {
			in = new GZIPInputStream(in);
		}
		return in;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return path.hashCode();
	}

}
