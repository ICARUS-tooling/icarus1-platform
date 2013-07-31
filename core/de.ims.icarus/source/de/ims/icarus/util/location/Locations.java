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
package de.ims.icarus.util.location;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.logging.LoggerFactory;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class Locations {

	private Locations() {
		// no-op
	}
	

	public static Location getLocation(String path) throws MalformedURLException {
		if(path==null)
			throw new IllegalArgumentException("Invalid path"); //$NON-NLS-1$
		
		try {
			File file = new File(path);
			file = IOUtil.toRelativeFile(file);
			if(file.exists()) {
				return new DefaultFileLocation(file.getCanonicalFile());
			}
		} catch(IOException e) {
			LoggerFactory.log(Locations.class, Level.WARNING, 
					"Failed to generate location for path: "+path, e); //$NON-NLS-1$
		}
		
		return new DefaultURLLocation(new URL(path));
	}
	
	public static Location getFileLocation(String path) {
		if(path==null || path.isEmpty())
			return null;
		
		try {
			File file = new File(path);
			file = IOUtil.toRelativeFile(file);
			if(file.exists()) {
				return new DefaultFileLocation(file.getCanonicalFile());
			}
		} catch(IOException e) {
			LoggerFactory.log(Locations.class, Level.WARNING, 
					"Failed to generate file location for path: "+path, e); //$NON-NLS-1$
		}
		
		return null;
	}
	
	public static String getPath(Location location) {
		if(location==null) {
			return null;
		}
		if(location.isLocal()) {
			return location.getFile().getAbsolutePath();
		} else {
			return location.getURL().toString();
		}
	}
	
	public static String getRelativePath(Location location) {
		if(location==null) {
			return null;
		}
		if(location.isLocal()) {
			return IOUtil.toRelativeFile(location.getFile()).getPath();
		} else {
			return location.getURL().toString();
		}
	}
	
	public static boolean equals(Location l1, Location l2) {
		if(l1==null && l2==null) {
			return true;
		}
		if(l1==null || l2==null) {
			return false;
		}
		
		if(l1.isLocal() && l2.isLocal()) {
			return l1.getFile().equals(l2.getFile());
		} else if(!l1.isLocal() && !l2.isLocal()) {
			return l1.getURL().equals(l2.getURL());
		} else {
			return false;
		}
	}
}
