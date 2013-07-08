/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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

	@Override
	protected URL resolvePath(URL baseUrl, String path) {
		String context = baseUrl.toExternalForm();
		if(path.startsWith("lib/") && context.endsWith("!/")) {  //$NON-NLS-1$//$NON-NLS-2$
			context = stripJarContext(context);
			int offset = context.lastIndexOf(File.separatorChar);
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
		
		return super.resolvePath(baseUrl, path);
	}
	
	protected String stripJarContext(String context) {
		int beginIndex = context.startsWith("jar:") ? 4 : 0; //$NON-NLS-1$
		int endIndex = context.length();
		if(context.endsWith("!/")) { //$NON-NLS-1$
			endIndex -= 2;
		}
		
		return context.substring(beginIndex, endIndex);
	}
}
