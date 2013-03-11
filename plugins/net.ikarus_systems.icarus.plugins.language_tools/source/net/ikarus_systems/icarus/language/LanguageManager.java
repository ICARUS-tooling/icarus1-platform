/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language;

import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.ikarus_systems.icarus.util.ClassProxy;
import net.ikarus_systems.icarus.util.Exceptions;
import net.ikarus_systems.icarus.util.UnknownIdentifierException;
import net.ikarus_systems.icarus.util.id.DuplicateIdentifierException;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class LanguageManager implements Serializable {

	private static final long serialVersionUID = 2940925679465686703L;
	
	// singleton
	private static LanguageManager instance;

	public static LanguageManager getInstance() {
		if(instance==null) {
			synchronized (LanguageManager.class) {
				if(instance==null)
					instance= new LanguageManager();
			}
		}
		
		return instance;
	}
	
	private Map<String, Object> grammars = Collections.synchronizedMap(new HashMap<String, Object>());
	
	private LanguageManager() {
		// no-op
	}
	
	// prevent multiple deserialization
	private Object readResolve() throws ObjectStreamException {
		throw new NotSerializableException();
	}
	
	// prevent cloning
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	public void registerGrammar(String id, Object grammar) {
		Exceptions.testNullArgument(id, "id"); //$NON-NLS-1$
		Exceptions.testNullArgument(grammar, "grammar"); //$NON-NLS-1$
		
		if(!(grammar instanceof Grammar) && !(grammar instanceof ClassProxy))
			throw new IllegalArgumentException("Invalid grammar object: "+grammar); //$NON-NLS-1$
		
		synchronized (grammars) {
			if(grammars.containsKey(id))
				throw new DuplicateIdentifierException("Duplicate grammar id: "+id); //$NON-NLS-1$
			
			grammars.put(id, grammar);
		}
	}
	
	public Grammar getGrammar(String id) {
		Exceptions.testNullArgument(id, "id"); //$NON-NLS-1$
		
		Object grammar = grammars.get(id);
		
		if(grammar==null)
			throw new UnknownIdentifierException("No such grammar: "+id); //$NON-NLS-1$
		
		if(grammar instanceof ClassProxy) {
			grammar = ((ClassProxy)grammar).loadObject();
			grammars.put(id, grammar);
		}
		
		return (Grammar) grammar;
	}
}