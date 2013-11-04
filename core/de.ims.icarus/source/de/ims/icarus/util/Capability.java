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
package de.ims.icarus.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.id.UnknownIdentifierException;
import de.ims.icarus.util.mpi.Commands;


/**
 * Represents an abstract <i>capability</i> originating from an 
 * arbitrary entity that is described in a short string.
 * <p>
 * The general convention of declaring capability strings is as follows:<br>
 * <i>command</i>_<i>content-type</i><br>
 * <i>command</i> is the textual representation of a command the entity is
 * able to handle. For some common examples check the list of predefined commands
 * in the {@link Commands} list.<br>
 * <i>target</i> describes an optional restriction to the type of data that can be
 * handled via a given command. For example a component for text-input can only
 * grant access to text data, so it could declare a {@code get_StringContentType} capability.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class Capability {
	
	public static final char SEPARATOR = '_';
	
	private static Map<String, Capability> capabilities;
	
	private final String token;

	private Capability(String token) {
		this.token = token;
	}
	
	public String getToken() {
		return token;
	}

	public boolean matches(String s) {
		return token.equals(s);
	}
	
	/**
	 * Checks whether this abstract capability is a more generalized
	 * version of the capability given as argument.
	 * <p>
	 * Capability A is considered to be a generalized version of capability
	 * B when the token of A is a true prefix of the token of B and the next
	 * character in B's token after that prefix is the underscore character '_'.
	 * <p>
	 * So for example the {@code get} capability is a generalized version of 
	 * the {@code get_StringContentType} capability.
	 */
	public boolean isGeneralizationOf(Capability capability) {
		String targetToken = capability.getToken();
		if(targetToken.length()<=token.length()) {
			return false;
		}
		
		return targetToken.startsWith(token) &&
				targetToken.charAt(token.length())==SEPARATOR;
	}

	/**
	 * @see #isGeneralizationOf(Capability)
	 */
	public boolean isGeneralizationOf(String s) {
		return isGeneralizationOf(getCapability(s));
	}
	
	public String extractCommand() {
		int index = token.indexOf(SEPARATOR);
		return index==-1 ? token : token.substring(0, index);
	}
	
	public String extractContentTypeId() {
		int index = token.indexOf(SEPARATOR);
		return index==-1 ? null : token.substring(index);
	}
	
	public ContentType extractContentType() {
		String contentTypeId = extractContentTypeId();
		if(contentTypeId==null) {
			return null;
		}
		try {
			return ContentTypeRegistry.getInstance().getType(contentTypeId);
		} catch(UnknownIdentifierException e) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return token;
	}
	
	@Override
	public int hashCode() {
		return token.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Capability) {
			return ((Capability)obj).token.equals(token);
		}
		return false;
	}
	
	public static Capability getCapability(String token) {
		if(capabilities==null) {
			synchronized (Capability.class) {
				if(capabilities==null) {
					capabilities = new HashMap<>();
					capabilities = Collections.synchronizedMap(capabilities);
				}
			}
		}
		
		Capability capability = null;
		synchronized (capabilities) {
			capability = capabilities.get(token);
			if(capability==null) {
				capability = new Capability(token);
				capabilities.put(token, capability);
			}
		}
		
		return capability;
	}
	
	public static Capability getCapability(String command, ContentType contentType) {
		if(contentType==null)
			throw new NullPointerException("Invalid content type"); //$NON-NLS-1$
		return getCapability(command, contentType.getId());
	}

	
	public static Capability getCapability(String command, String contentTypeId) {
		if(command==null)
			throw new NullPointerException("Invalid command"); //$NON-NLS-1$
		if(contentTypeId==null)
			throw new NullPointerException("Invalid content type id"); //$NON-NLS-1$
		
		String token = command+SEPARATOR+contentTypeId;
		return getCapability(token);
	}
}
