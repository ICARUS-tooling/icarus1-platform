/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.ikarus_systems.icarus.util.mpi.Commands;

/**
 * Represents an abstract <i>capability</i> originating from an 
 * arbitrary entity that is described in a short string.
 * <p>
 * The general convention of declaring capability strings is as follows:<br>
 * <i>command</i>_<i>target</i><br>
 * <i>command</i> is the textual representation of a command the entity is
 * able to handle. For some common examples check the list of predefined commands
 * in the {@link Commands} list.<br>
 * <i>target</i> describes an optional restriction to the type of data that can be
 * handled via a given command. For example a component for text-input can only
 * grant access to text data, so it could declare a {@code get_text} capability.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class Capability {
	
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
	 * the {@code get_text} capability.
	 */
	public boolean isGeneralizationOf(Capability capability) {
		String targetToken = capability.getToken();
		if(targetToken.length()==token.length()) {
			return false;
		}
		
		return targetToken.startsWith(token) &&
				targetToken.charAt(token.length())=='_';
	}

	/**
	 * @see #isGeneralizationOf(Capability)
	 */
	public boolean isGeneralizationOf(String s) {
		return isGeneralizationOf(getCapability(s));
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
}
