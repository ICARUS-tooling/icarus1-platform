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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.java.plugin.util.IoUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultURLLocation implements Location {
	
	private final URL url;

	/**
	 * 
	 */
	public DefaultURLLocation(URL url) {
		if(url==null)
			throw new IllegalArgumentException("Invalid url"); //$NON-NLS-1$
			
		this.url = url;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Location#getURL()
	 */
	@Override
	public URL getURL() {
		return url;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Location#isLocal()
	 */
	@Override
	public boolean isLocal() {
		return IoUtil.url2file(url)!=null;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Location#getFile()
	 */
	@Override
	public File getFile() {
		return IoUtil.url2file(url);
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Location#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() throws IOException {
		return url.openConnection().getOutputStream();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Location#openInputStream()
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		return url.openStream();
	}

}
