/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.ikarus_systems.icarus.io.IOUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultFileLocation extends Location {
	
	private final File file;

	public DefaultFileLocation(File file) {
		if(file==null)
			throw new IllegalArgumentException("Invalid file"); //$NON-NLS-1$
		
		this.file = file;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Location) {
			return Locations.equals(this, (Location)obj);
		}
		
		return false;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.location.Location#getURL()
	 */
	@Override
	public URL getURL() {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.util.location.Location#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return true;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.location.Location#getFile()
	 */
	@Override
	public File getFile() {
		return file;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.location.Location#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() throws IOException {
		OutputStream out = new FileOutputStream(file);
		if(IOUtil.isZipSource(file.getName())) {
			out = new GZIPOutputStream(out);
		}
		
		return out;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.location.Location#openInputStream()
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		InputStream in = new FileInputStream(file);
		if(IOUtil.isZipSource(file.getName())) {
			in = new GZIPInputStream(in);
		}
		return in;
	}

}
