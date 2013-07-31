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
package de.ims.icarus.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.jar.JarFile;
import java.util.logging.Level;

import de.ims.icarus.Core;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.Options;


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

    /**
     * Checks if resource exist and can be opened.
     * @param url absolute URL which points to a resource to be checked
     * @return <code>true</code> if given URL points to an existing resource
     */
    public static boolean isResourceExists(final URL url) {
        File file = urlToFile(url);
        if (file != null) {
            return file.canRead();
        }
        if ("jar".equalsIgnoreCase(url.getProtocol())) { //$NON-NLS-1$
            return isJarResourceExists(url);
        }
        return isUrlResourceExists(url);
    }

    /**
     * Checks if resource URL exist and can be opened.
     * @param url absolute URL which points to a resource to be checked
     * @return <code>true</code> if given URL points to an existing resource
     */
    public static boolean isUrlResourceExists(final URL url) {
        try {
            InputStream is = url.openStream();
            try {
                is.close();
            } catch (IOException ioe) {
                // ignore
            }
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Checks if resource jar exist and can be opened.
     * @param url absolute URL which points to a jar resource to be checked
     * @return <code>true</code> if given URL points to an existing resource
     */
    public static boolean isJarResourceExists(final URL url) {
        try {
            String urlStr = url.toExternalForm();
            int p = urlStr.indexOf("!/"); //$NON-NLS-1$
            if (p == -1) {// this is invalid JAR file URL
                return false;
            }
            URL fileUrl = new URL(urlStr.substring(4, p));
            File file = urlToFile(fileUrl);
            if (file == null) {// this is non-local JAR file URL
                return isUrlResourceExists(url);
            }
            if (!file.canRead()) {
                return false;
            }
            if (p == urlStr.length() - 2) {// URL points to the root entry of JAR file
                return true;
            }
            JarFile jarFile = new JarFile(file);
            try {
                return jarFile.getEntry(urlStr.substring(p + 2)) != null;
            } finally {
                jarFile.close();
            }
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Utility method to convert local URL to a {@link File} object.
     * @param url an URL
     * @return file object for given URL or <code>null</code> if URL is not
     *         local
     */
    @SuppressWarnings("deprecation")
    public static File urlToFile(final URL url) {
        String prot = url.getProtocol();
        if ("jar".equalsIgnoreCase(prot)) { //$NON-NLS-1$
            if (url.getFile().endsWith("!/")) { //$NON-NLS-1$
                String urlStr = url.toExternalForm();
                try {
                    return urlToFile(
                            new URL(urlStr.substring(4, urlStr.length() - 2)));
                } catch (MalformedURLException mue) {
                    // ignore
                }
            }
            return null;
        }
        if (!"file".equalsIgnoreCase(prot)) { //$NON-NLS-1$
            return null;
        }
        try {
            // Method URL.toURI() may produce URISyntaxException for some
            // "valid" URL's that contain spaces or other "illegal" characters.
            //return new File(url.toURI());
            return new File(URLDecoder.decode(url.getFile(), "UTF-8")); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return new File(URLDecoder.decode(url.getFile()));
        }
    }
    
    /**
     * Utility method to convert a {@link File} object to a local URL.
     * @param file a file object
     * @return absolute URL that points to the given file
     * @throws MalformedURLException if file can't be represented as URL for
     *         some reason
     */
    public static URL fileToUrl(final File file) throws MalformedURLException {
        try {
            return file.getCanonicalFile().toURI().toURL();
        } catch (MalformedURLException mue) {
            throw mue;
        } catch (IOException ioe) {
            throw new MalformedURLException("unable to create canonical file: "  //$NON-NLS-1$
            		+ file + " " + ioe); //$NON-NLS-1$
        }
    }
    
    public static File toRelativeFile(File f) {
    	if(f==null) {
    		return f;
    	}
    	
    	String root = Core.getCore().getRootFolder().getAbsolutePath();
    	String path = null;
    	try {
    		path = f.getCanonicalPath();
    	} catch(Exception e) {
    		LoggerFactory.log(IOUtil.class, Level.WARNING, 
    				"Error converting file to canonical path: "+f.getAbsolutePath(), e); //$NON-NLS-1$
    	}
    	
    	if(path==null) {
    		return f;
    	}
    	
    	if(path.startsWith(root)) {
    		path = path.substring(root.length()+1);
    		return new File(path);
    	} else {
    		return f;
    	}
    }
}
