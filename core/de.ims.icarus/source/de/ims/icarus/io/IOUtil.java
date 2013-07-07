/*
 * $Revision: 56 $
 * $Date: 2013-07-03 18:16:44 +0200 (Mi, 03 Jul 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/io/IOUtil.java $
 *
 * $LastChangedDate: 2013-07-03 18:16:44 +0200 (Mi, 03 Jul 2013) $ 
 * $LastChangedRevision: 56 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;

import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id: IOUtil.java 56 2013-07-03 16:16:44Z mcgaerty $
 *
 */
public final class IOUtil {
	
	public static final String UTF8_ENCODING = "UTF-8"; //$NON-NLS-1$

	private IOUtil() {
		// no-op
	}
	
	public static boolean isZipSource(String name) {
		return name.endsWith("zip") || name.endsWith(".gzip") || name.endsWith(".gz"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
	
    public static void copyStream(final InputStream in, final OutputStream out,
            int bufferSize) throws IOException {
    	if(bufferSize==0) {
    		bufferSize = 4096;
    	}
        byte[] buf = new byte[bufferSize];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
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
	
	public static BufferedReader getReader(InputStream is, Charset cs) throws IOException {
		return new BufferedReader(new InputStreamReader(is, cs));
	}
	
	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$
	
	public static final String CHARSET_OPTION = "charset"; //$NON-NLS-1$
	public static final String CHARSET_NAME_OPTION = "charsetName"; //$NON-NLS-1$
	public static final String ENCODING_OPTION = "encoding"; //$NON-NLS-1$
	
	public static Charset getCharset(Options options, Charset defaultCharset) {
		Object charset = null;
		if(options!=null) {
			charset = options.firstSet(CHARSET_OPTION, 
					CHARSET_NAME_OPTION, ENCODING_OPTION);
		}
		
		if(charset == null) {
			charset = defaultCharset==null ? DEFAULT_CHARSET : defaultCharset;
		} else if(charset instanceof String) {
			charset = Charset.forName((String)charset);
		}
		
		if(!(charset instanceof Charset))
			throw new IllegalArgumentException("Invalid charset: "+charset.getClass()); //$NON-NLS-1$
		
		return (Charset) charset;
	}
	
	public static Charset getCharset(Options options) {
		return getCharset(options, null);
	}
}
