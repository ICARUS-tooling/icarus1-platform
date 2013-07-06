/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util.data;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContentTypeCollection {

	private final Set<ContentType> contentTypes = new LinkedHashSet<>();
	
	ContentTypeCollection() {
		// no-op
	}
	
	ContentTypeCollection(Collection<ContentType> types) {
		contentTypes.addAll(types);
	}
	
	void addType(ContentType type) {
		contentTypes.add(type);
	}
	
	void addTypes(Collection<ContentType> types) {
		contentTypes.addAll(types);
	}

	public boolean contains(ContentType type) {
		return contentTypes.contains(type);
	}
	
	public ContentType[] getContentTypes() {
		return contentTypes.toArray(new ContentType[contentTypes.size()]);
	}
	
	public int size() {
		return contentTypes.size();
	}
	
	public boolean isEmpty() {
		return contentTypes.isEmpty();
	}

	
	/**
	 * Returns {@code true} if and only if this collection contains
	 * a {@code ContentType} that is compatible towards the {@code target}
	 * parameter as defined by {@link ContentTypeRegistry#isCompatible(ContentType, ContentType)}
	 */
	public boolean isCompatibleTo(ContentType target) {
		for(ContentType type : contentTypes) {
			if(ContentTypeRegistry.isCompatible(type, target)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns {@code true} if and only if this collection contains
	 * a {@code ContentType} the {@code target} parameter is considered
	 * to be compatible towards as measured by 
	 * {@link ContentTypeRegistry#isCompatible(ContentType, ContentType)}
	 */
	public boolean isCompatibleType(ContentType target) {
		for(ContentType type : contentTypes) {
			if(ContentTypeRegistry.isCompatible(target, type)) {
				return true;
			}
		}
		return false;
	}
}
