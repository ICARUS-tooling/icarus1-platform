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
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.logging.Level;

import javax.swing.SwingWorker;

import de.ims.icarus.Core;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.tasks.TaskManager;
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
		return name.endsWith(".zip"); //$NON-NLS-1$
	}

	public static boolean isGZipSource(String name) {
		return name.endsWith(".gzip") || name.endsWith(".gz"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean isGZipSource(Path path) {
		return path.endsWith(".gzip") || path.endsWith(".gz"); //$NON-NLS-1$ //$NON-NLS-2$
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

    public static void copyStream(final InputStream in, final OutputStream out) throws IOException {
    	copyStream(in, out, 0);
    }

    public static void copyStream(final InputStream in, final OutputStream out,
            int bufferSize) throws IOException {
    	if(bufferSize==0) {
    		bufferSize = 8000;
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
			charset = options.firstSet(
					CHARSET_OPTION,
					CHARSET_NAME_OPTION,
					ENCODING_OPTION);
		}

		if(charset == null) {
			charset = defaultCharset==null ? DEFAULT_CHARSET : defaultCharset;
		} else if(charset instanceof String) {
			charset = Charset.forName((String)charset);
		}

		if(!(charset instanceof Charset))
			throw new NullPointerException("Invalid charset: "+charset.getClass()); //$NON-NLS-1$

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
    	try {
	        Path path = Paths.get(url.toURI());
	        if (path != null) {
	            return Files.isReadable(path);
	        }
	        if ("jar".equalsIgnoreCase(url.getProtocol())) { //$NON-NLS-1$
	            return isJarResourceExists(url);
	        }
	        return isUrlResourceExists(url);
    	} catch(URISyntaxException e) {
    		return false;
    	}
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
            Path path = Paths.get(url.toURI());
            if (path == null) {// this is non-local JAR file URL
                return isUrlResourceExists(url);
            }
            if (!Files.isReadable(path)) {
                return false;
            }
            if (p == urlStr.length() - 2) {// URL points to the root entry of JAR file
                return true;
            }
            JarFile jarFile = new JarFile(path.toFile());
            try {
                return jarFile.getEntry(urlStr.substring(p + 2)) != null;
            } finally {
                jarFile.close();
            }
        } catch (IOException | URISyntaxException ioe) {
            return false;
        }
    }

    /**
     * Utility method to convert a {@link Path} object to a local URL.
     * @param p a file object
     * @return absolute URL that points to the given file
     * @throws MalformedURLException if file can't be represented as URL for
     *         some reason
     */
    public static URL fileToUrl(Path p) throws MalformedURLException {
        try {
            return p.toAbsolutePath().toUri().toURL();
        } catch (MalformedURLException mue) {
            throw mue;
        } catch (IOError ioe) {
            throw new MalformedURLException("unable to create absolute path: "  //$NON-NLS-1$
            		+ p + " " + ioe); //$NON-NLS-1$
        }
    }

    public static Path toRelativePath(Path p) {
    	if(p==null) {
    		return p;
    	}

    	Path root = Core.getCore().getRootFolder();
    	try {
    		p = p.toAbsolutePath();
    	} catch(IOError e) {
    		LoggerFactory.log(IOUtil.class, Level.WARNING,
    				"Error converting to absolute path: "+p, e); //$NON-NLS-1$
    		return p;
    	}

    	if(p.startsWith(root)) {
    		p = p.subpath(root.getNameCount(), p.getNameCount());
    	}
		return p;
    }

    public static boolean canFree(Loadable loadable) {
    	if(loadable==null)
    		throw new NullPointerException("Invalid loadable"); //$NON-NLS-1$

    	return loadable.isLoaded() && !loadable.isLoading();
    }

	public static class LoadJob extends SwingWorker<Loadable, Object> {

		private final Loadable loadable;

		public LoadJob(Loadable loadable) {
			if(loadable==null)
				throw new NullPointerException("Invalid loadable"); //$NON-NLS-1$

			this.loadable = loadable;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof LoadJob) {
				return ((LoadJob)obj).loadable==loadable;
			}
			return false;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Loadable doInBackground() throws Exception {
			TaskManager.getInstance().setIndeterminate(this, true);

			try {
				// Wait while target is loading
				while(loadable.isLoading());

				if(loadable.isLoaded()) {
					return null;
				}

				loadable.load();
			} finally {
				TaskManager.getInstance().setIndeterminate(this, false);
			}

			return loadable;
		}
	}
}
