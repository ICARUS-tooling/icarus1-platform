/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language;

import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;

import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.language_tools.LanguageToolsConstants;
import de.ims.icarus.util.ClassProxy;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.id.DuplicateIdentifierException;
import de.ims.icarus.util.id.UnknownIdentifierException;


/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class LanguageManager {
	
	private ContentType sentenceDataContentType;
	
	private Map<String, Object> grammars = Collections.synchronizedMap(
			new LinkedHashMap<String, Object>());
	
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
	
	private LanguageManager() {
		sentenceDataContentType = ContentTypeRegistry.getInstance()
				.getTypeForClass(SentenceData.class);
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
	
	public ContentType getSentenceDataContentType() {
		return sentenceDataContentType;
	}
	
	public static Collection<Extension> getAvailableTokenizers() {
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				LanguageToolsConstants.LANGUAGE_TOOLS_PLUGIN_ID+"@Tokenizer"); //$NON-NLS-1$
		
		return PluginUtil.getExtensions(extensionPoint, true, true, null);
	}
}