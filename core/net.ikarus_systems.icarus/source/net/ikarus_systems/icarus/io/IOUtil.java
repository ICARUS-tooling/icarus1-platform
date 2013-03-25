/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class IOUtil {
	
	public static final String UTF8_ENCODING = "UTF-8"; //$NON-NLS-1$

	private IOUtil() {
		// no-op
	}

	public static String readStream(InputStream input) throws IOException {
		return readStream(input, UTF8_ENCODING);
	}

	public static String readStream(InputStream input, String encoding) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = input.read(buffer)) > 0) {
			baos.write(buffer, 0, len);
		}
		input.close();
		return new String(baos.toByteArray(), encoding);
	}

	public static String readStreamUnchecked(InputStream input) {
		return readStreamUnchecked(input, UTF8_ENCODING);
	}

	public static String readStreamUnchecked(InputStream input, String encoding) {
		try {
			return readStream(input, encoding);
		} catch (IOException e) {
			// ignore
		}
		
		return null;
	}

	public static boolean isLocalFile(URL url) {
		String scheme = url.getProtocol();
		return "file".equalsIgnoreCase(scheme) && !hasHost(url); //$NON-NLS-1$
	}

	public static boolean hasHost(URL url) {
		String host = url.getHost();
		return host != null && !"".equals(host); //$NON-NLS-1$
	}

	public static boolean isLocal(URL url) {
		if (isLocalFile(url)) {
			return true;
		}
		String protocol = url.getProtocol();
		if ("jar".equalsIgnoreCase(protocol)) { //$NON-NLS-1$
			String path = url.getPath();
			int emIdx = path.lastIndexOf('!');
			String subUrlString = emIdx == -1 ? path : path.substring(0, emIdx);
			try {
				URL subUrl = new URL(subUrlString);
				return isLocal(subUrl);
			} catch (java.net.MalformedURLException mfu) {
				return false;
			}
		} else {
			return false;
		}
	}
}
