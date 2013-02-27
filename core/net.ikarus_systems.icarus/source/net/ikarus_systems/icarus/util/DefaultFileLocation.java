/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultFileLocation implements Location {
	
	private final File file;

	public DefaultFileLocation(File file) {
		if(file==null)
			throw new IllegalArgumentException("Invalid file"); //$NON-NLS-1$
		
		this.file = file;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Location#getURL()
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
	 * @see net.ikarus_systems.icarus.util.Location#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return true;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Location#getFile()
	 */
	@Override
	public File getFile() {
		return file;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Location#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() throws IOException {
		return new FileOutputStream(file);
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Location#openInputStream()
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		return new FileInputStream(file);
	}

}
