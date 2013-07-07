/*
 * $Revision: 46 $
 * $Date: 2013-06-13 12:32:58 +0200 (Do, 13 Jun 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/location/DefaultURLLocation.java $
 *
 * $LastChangedDate: 2013-06-13 12:32:58 +0200 (Do, 13 Jun 2013) $ 
 * $LastChangedRevision: 46 $ 
 * $LastChangedBy: mcgaerty $
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
 * @version $Id: DefaultURLLocation.java 46 2013-06-13 10:32:58Z mcgaerty $
 *
 */
public class DefaultURLLocation extends Location {
	
	private final URL url;

	public DefaultURLLocation(URL url) {
		if(url==null)
			throw new IllegalArgumentException("Invalid url"); //$NON-NLS-1$
			
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

}
