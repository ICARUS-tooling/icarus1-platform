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
package de.ims.icarus.plugins;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;

import org.java.plugin.standard.StandardPathResolver;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LibPathResolver extends StandardPathResolver {

	public LibPathResolver() {
		// no-op
	}
	
//	@Override
//	public URL resolvePath(Identity identity, String path) {
//		URL url = super.resolvePath(identity, path);
//		if(identity instanceof Library) {
//			String newUrl = stripJarContext(url.toExternalForm());
//			try {
//				url = new URL(newUrl);
//			} catch (MalformedURLException e) {
//	            log.error("can't create URL: " + newUrl); //$NON-NLS-1$
//	            throw new IllegalArgumentException("path " + path //$NON-NLS-1$
//	                    + " in context of " + identity //$NON-NLS-1$
//	                    + " cause creation of malformed URL"); //$NON-NLS-1$
//			}
//		}
//		
//		return url;
//	}

	@Override
	protected URL resolvePath(URL baseUrl, String path) {
		String context = baseUrl.toExternalForm();
		if(path.startsWith("lib/") && context.endsWith("!/")) {  //$NON-NLS-1$//$NON-NLS-2$
			context = stripJarContext(context);
			@SuppressWarnings("resource")
			int offset = context.lastIndexOf(FileSystems.getDefault().getSeparator());
			if(offset==-1) {
				offset = context.lastIndexOf('/');
			}
			context = context.substring(0, offset+1);

			try {
				//System.out.printf("resolving lib: context=%s path=%s\n", context, path); //$NON-NLS-1$
				return new URL(context+path);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
//        try {
//        	baseUrl = new URL(stripJarContext(baseUrl.toExternalForm()));
//        	
//            if ("".equals(path) || "/".equals(path)) { //$NON-NLS-1$ //$NON-NLS-2$
//                return baseUrl;
//            }
//            return new URL(baseUrl, path);
//        } catch (MalformedURLException mue) {
//            log.error("can't create URL in context of " + baseUrl //$NON-NLS-1$
//                    + " and path " + path, mue); //$NON-NLS-1$
//            throw new IllegalArgumentException("path " + path //$NON-NLS-1$
//                    + " in context of " + baseUrl //$NON-NLS-1$
//                    + " cause creation of malformed URL"); //$NON-NLS-1$
//        }

		return super.resolvePath(baseUrl, path);
	}

	protected String stripJarContext(String context) {
//		context = context.replace("!/", "/");  //$NON-NLS-1$//$NON-NLS-2$
		int beginIndex = context.startsWith("jar:") ? 4 : 0; //$NON-NLS-1$
		int endIndex = context.length();
		if(context.endsWith("!/")) { //$NON-NLS-1$
			endIndex -= 2;
		}

		return context.substring(beginIndex, endIndex);
	}
}
