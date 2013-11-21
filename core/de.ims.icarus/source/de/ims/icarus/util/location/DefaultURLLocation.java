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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.java.plugin.util.IoUtil;

import de.ims.icarus.io.IOUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultURLLocation implements Location {
	
	private final URL url;

	public DefaultURLLocation(URL url) {
		if(url==null)
			throw new NullPointerException("Invalid url"); //$NON-NLS-1$
			
		this.url = url;
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
		return url;
	}

	/**
	 * @see de.ims.icarus.util.location.Location#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return getFile()!=null;
	}

	/**
	 * @see de.ims.icarus.util.location.Location#getFile()
	 */
	@Override
	public File getFile() {
		return IoUtil.url2file(url);
	}

	/**
	 * @see de.ims.icarus.util.location.Location#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() throws IOException {
		return url.openConnection().getOutputStream();
	}

	/**
	 * @see de.ims.icarus.util.location.Location#openInputStream()
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		InputStream in = url.openStream();
		if(IOUtil.isZipSource(url.toExternalForm())) {
			in = new GZIPInputStream(in);
		}
		return in;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return url.hashCode();
	}

}
