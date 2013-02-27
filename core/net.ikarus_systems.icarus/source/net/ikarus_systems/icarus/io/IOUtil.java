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
}
