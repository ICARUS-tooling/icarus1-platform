/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.ui.events.WeakEventSource;
import net.ikarus_systems.icarus.util.CollectionUtils;
import net.ikarus_systems.icarus.util.id.DuplicateIdentifierException;
import net.ikarus_systems.icarus.util.id.Identity;
import net.ikarus_systems.icarus.util.id.UnknownIdentifierException;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.mpi.Message;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ContentTypeRegistry {
	
	private static ContentTypeRegistry instance;
	
	/**
	 * Maps content type ids to their instances.
	 */
	private Map<String, ContentType> contentTypes = new LinkedHashMap<>();
	
	/**
	 * Reverse mapping of classes to their directly defining
	 * content type instances.
	 */
	private Map<String, ContentType> classMap = new HashMap<>();
	
	/**
	 * Maps results of {@link #findEnclosingType(Class)} calls to
	 * argument class.
	 */
	private Map<Class<?>, ContentType> classCache;
	
	/**
	 * Collection of all registered {@code raw} converters, i.e.
	 * all converters that have not been created by this framework
	 * as an result of <i>conversion expansion</i>
	 */
	private Set<DataConverter> rawConverters = new LinkedHashSet<>();
	
	/**
	 * Mapping of all existing conversion chains for fast lookup.
	 */
	private Map<ContentTypePair, DataConverter> converterLookup;
	
	private WeakEventSource eventSource = new WeakEventSource(this);

	private ContentTypeRegistry() {
		// Object content type not supported?
		//addType0(new DefaultContentType(Object.class));
		
		addType0(new DefaultContentType(Integer.class));
		addType0(new DefaultContentType(Boolean.class));
		addType0(new DefaultContentType(Float.class));
		addType0(new DefaultContentType(Double.class));
		addType0(new DefaultContentType(Short.class));
		addType0(new DefaultContentType(Long.class));
		addType0(new DefaultContentType(Date.class));
		addType0(new DefaultContentType(String.class));
		addType0(new DefaultContentType(Location.class));
		addType0(new DefaultContentType(Identity.class));
		addType0(new DefaultContentType(Message.class));
	}

	public static ContentTypeRegistry getInstance() {
		if(instance==null) {
			synchronized (ContentTypeRegistry.class) {
				if(instance==null) {
					instance = new ContentTypeRegistry();
				}
			}
		}
		
		return instance;
	}
	
	public ContentType getType(String id) {
		ContentType type = contentTypes.get(id);
		if(type==null)
			throw new UnknownIdentifierException("No such content type: "+id); //$NON-NLS-1$
		
		return type;
	}
	
	public ContentType getTypeForClass(Object data) {
		if(data==null)
			throw new IllegalArgumentException("invalid data"); //$NON-NLS-1$
		
		Class<?> clazz = data instanceof Class ? (Class<?>)data : data.getClass();
		String className = clazz.getName();
		
		ContentType type = classMap.get(className);
		if(type==null)
			throw new IllegalArgumentException("No type defined for class: "+className); //$NON-NLS-1$
		
		return type;
	}
	
	private ContentType findEnclosingType(Class<?> clazz) {
		// Skip generalization to object
		if(clazz==Object.class) {
			return null;
		}
		
		ContentType type = classMap.get(clazz.getName());
		
		// Check cache
		if(type==null && classCache!=null) {
			type = classCache.get(clazz);
			if(type!=null) {
				return type;
			}
		}
		
		// Check super type
		if(type==null) {
			type = findEnclosingType(clazz.getSuperclass());
		}
		
		// Traverse interfaces
		if(type==null) {
			for(Class<?> interfaceClazz : clazz.getInterfaces()) {
				type = findEnclosingType(interfaceClazz);
				if(type!=null) {
					break;
				}
			}
		}
		
		// Cache info
		if(type!=null) {
			if(classCache==null) {
				classCache = new HashMap<>();
			}
			classCache.put(clazz, type);
		}
		
		return type;
	}
	
	public ContentType getEnclosingType(Object data) {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		
		Class<?> clazz = data instanceof Class ? (Class<?>)data : data.getClass();
		
		return findEnclosingType(clazz);
	}
	
	public ContentTypeCollection getEnclosingTypes(Object data) {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		
		ContentTypeCollection collection = new ContentTypeCollection();
		
		for(ContentType contentType : contentTypes.values()) {
			if(isCompatible(contentType, data)) {
				collection.addType(contentType);
			}
		}
		
		return collection;
	}
	
	/**
	 * Returns a collection of {@code ContentType} objects with declared
	 * content classes assignable to the content class of the {@code target}
	 * parameter. The returned collection does not contain the {@code target}
	 * parameter itself!
	 */
	public Collection<ContentType> getCompatibleTypes(ContentType target) {
		if(target==null)
			throw new IllegalArgumentException("Invalid target"); //$NON-NLS-1$
		
		if(contentTypes.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<ContentType> compatibleTypes = new ArrayList<>();
		
		for(ContentType type : contentTypes.values()) {
			if(type==target) {
				continue;
			}
			if(target.getContentClass().isAssignableFrom(type.getContentClass())) {
				compatibleTypes.add(type);
			}
		}
		
		return compatibleTypes;
	}
	
	public static boolean isCompatible(ContentType type, Object content) {
		if(type==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		if(content==null)
			throw new IllegalArgumentException("Invalid content"); //$NON-NLS-1$
		
		if(isStrictType(type)) {
			return type.getContentClass().equals(content.getClass()); 
		} else {
			return type.getContentClass().isAssignableFrom(content.getClass());
		}
	}
	
	/**
	 * Checks whether the content class of {@code target} can be assigned to
	 * the one of {@code type} or in the case of {@code type} being a <i>strict</i>
	 * {@code ContentType} whether it is equal.
	 */
	public static boolean isCompatible(ContentType type, ContentType target) {
		if(type==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		if(target==null)
			throw new IllegalArgumentException("Invalid target"); //$NON-NLS-1$
		
		if(isStrictType(type)) {
			return type.getContentClass().equals(target.getContentClass()); 
		} else {
			return type.getContentClass().isAssignableFrom(target.getContentClass());
		}
	}
	
	/**
	 * Fetches the {@code ContentType} identified by {@code typeId} and runs
	 * a call to {@link #isCompatible(ContentType, ContentType)} with the result.
	 */
	public static boolean isCompatible(String typeId, ContentType target) {
		if(typeId==null)
			throw new IllegalArgumentException("Invalid type id"); //$NON-NLS-1$
		if(target==null)
			throw new IllegalArgumentException("Invalid target"); //$NON-NLS-1$
		
		ContentType type = getInstance().getType(typeId);
		return isCompatible(type, target);
	}
	
	public static boolean isCompatible(String typeId, Object content) {
		if(typeId==null)
			throw new IllegalArgumentException("Invalid type id"); //$NON-NLS-1$
		if(content==null)
			throw new IllegalArgumentException("Invalid content"); //$NON-NLS-1$
		
		ContentType type = getInstance().getType(typeId);
		return isCompatible(type, content);
	}
	
	public static boolean isCompatible(ContentTypeCollection collection, String typeId) {
		if(collection==null)
			throw new IllegalArgumentException("Invalid collection"); //$NON-NLS-1$
		if(typeId==null)
			throw new IllegalArgumentException("Invalid type id"); //$NON-NLS-1$
		
		ContentType type = getInstance().getType(typeId);
		return collection.isCompatibleTo(type);
	}
	
	private static boolean isStrictType(ContentType type) {
		return CollectionUtils.isTrue(type.getProperties(), ContentType.STRICT_INHERITANCE);
	}
	
	public Object convert(Object data, Object targetType) throws DataConversionException {
		if(targetType==null)
			throw new IllegalArgumentException("Invalid target type"); //$NON-NLS-1$
		
		// Obtain result type
		ContentType resultType = null;
		if(targetType instanceof String) {
			resultType = getType((String)targetType);
		} else if(targetType instanceof ContentType) {
			resultType = (ContentType) targetType;
		} else
			throw new IllegalArgumentException("Unknown target type: "+targetType); //$NON-NLS-1$
		
		// Obtain input type
		ContentType inputType = getEnclosingType(data);
		if(inputType==null)
			throw new IllegalArgumentException("No content type found for class: "+data.getClass()); //$NON-NLS-1$
		
		DataConverter converter = getConverter(inputType, resultType);
		if(converter==null)
			throw new DataConversionException("Conversion not supported from "+data.getClass()+" to type "+resultType.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		
		return converter.convert(data);
	}

	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}

	public void removeListener(EventListener listener) {
		eventSource.removeListener(listener);
	}
	
	private void addType0(ContentType type) {
		contentTypes.put(type.getId(), type);
		classMap.put(type.getContentClass().getName(), type);
	}
	
	public void addType(ContentType type) {
		if(type==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		if(contentTypes.containsKey(type.getId()))
			throw new DuplicateIdentifierException("Content type id already in use: "+type.getId()); //$NON-NLS-1$
		
		String className = type.getContentClass().getName();
		if(classMap.containsKey(className))
			throw new IllegalArgumentException("Duplicate content type for class: "+className); //$NON-NLS-1$
		
		addType0(type);
		
		eventSource.fireEvent(new EventObject(Events.ADDED, "type", type)); //$NON-NLS-1$
	}
	
	public List<ContentType> availableTypes() {
		return new ArrayList<>(contentTypes.values());
	}
	
	public int availableTypesCount() {
		return contentTypes.size();
	}
	
	public void addConverter(DataConverter converter) {
		if(converter==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		
		rawConverters.add(converter);

		addConverter0(converter);
		
		eventSource.fireEvent(new EventObject(Events.ADDED, "converter", converter)); //$NON-NLS-1$
	}
	
	public List<DataConverter> availableConverters() {
		return new ArrayList<>(rawConverters);
	}
	
	public int availableConverterCount() {
		return rawConverters.size();
	}
	
	private void addConverter0(DataConverter newConverter) {
		
		// Try to directly replace an existing one
		DataConverter oldConverter = getConverter0(
				newConverter.getInputType(), newConverter.getResultType());
		if(oldConverter==null || oldConverter.getAccuracy()<newConverter.getAccuracy()) {
			putConverter(newConverter);
		}
		
		// Try to expand existing converters by "prefixing" with new one
		DataConverter[] converters = getConvertersForInputType(newConverter.getResultType());
		for(DataConverter converter : converters) {
			// Prevent loops
			if(converter.getResultType().equals(converter.getInputType())) {
				continue;
			}
			// Calc new accuracy
			double newAccuracy = newConverter.getAccuracy()*converter.getAccuracy();
			// Fetch existing converter
			oldConverter = getConverter(
					newConverter.getInputType(), converter.getResultType());
			// Skip expansion if accuracy doesn't improve
			if(oldConverter!=null && oldConverter.getAccuracy()>=newAccuracy) {
				continue;
			}
			// Register new chain
			putConverter(new ConverterChain(newConverter, converter));
		}
		
		// Try to expand existing converters by "suffixing" with new one
		converters = getConvertersForResultType(newConverter.getInputType());
		for(DataConverter converter : converters) {
			// Prevent loops
			if(converter.getInputType().equals(converter.getResultType())) {
				continue;
			}
			// Calc new accuracy
			double newAccuracy = newConverter.getAccuracy()*converter.getAccuracy();
			// Fetch existing converter
			oldConverter = getConverter(
					converter.getInputType(), newConverter.getResultType());
			// Skip expansion if accuracy doesn't improve
			if(oldConverter!=null && oldConverter.getAccuracy()>=newAccuracy) {
				continue;
			}
			// Register new chain
			putConverter(new ConverterChain(converter, newConverter));
		}
	}
	
	public DataConverter[] getConvertersForInputType(ContentType inputType) {
		if(converterLookup==null) {
			return new DataConverter[0];
		}
		
		List<DataConverter> converters = new ArrayList<>();
		for(DataConverter converter : converterLookup.values()) {
			if(converter.getInputType().equals(inputType)) {
				converters.add(converter);
			}
		}
		
		return converters.toArray(new DataConverter[converters.size()]);
	}
	
	public DataConverter[] getConvertersForResultType(ContentType resultType) {
		if(converterLookup==null) {
			return new DataConverter[0];
		}
		
		List<DataConverter> converters = new ArrayList<>();
		for(DataConverter converter : converterLookup.values()) {
			if(converter.getResultType().equals(resultType)) {
				converters.add(converter);
			}
		}
		
		return converters.toArray(new DataConverter[converters.size()]);
	}
	
	private void putConverter(DataConverter converter) {
		if (converterLookup==null) {
			converterLookup = new HashMap<>();
		}
		
		ContentType inputType = converter.getInputType();
		ContentType resultType = converter.getResultType();
		
		if(inputType.equals(resultType)) {
			return;
		}
		
		converterLookup.put(new ContentTypePair(inputType, resultType), converter);
	}
	
	private ContentTypePair lookupKey = new ContentTypePair(null, null);
	
	private DataConverter getConverter0(ContentType inputType, ContentType resultType) {
		if(converterLookup==null) {
			return null;
		}
		synchronized (lookupKey) {
			lookupKey.set(inputType, resultType);
			return converterLookup.get(lookupKey);
		}
	}
	
	public DataConverter getConverter(ContentType inputType, ContentType resultType) {
		if(inputType==null)
			throw new IllegalArgumentException("Invalid input type"); //$NON-NLS-1$
		if(resultType==null)
			throw new IllegalArgumentException("Invalid result type"); //$NON-NLS-1$
		if(inputType.getContentClass()==Object.class)
			throw new IllegalArgumentException("Need more specific input type than 'object'"); //$NON-NLS-1$
		if(resultType.getContentClass()==Object.class)
			throw new IllegalArgumentException("Need more specific result type than 'object'"); //$NON-NLS-1$
		
		return getConverter0(inputType, resultType);
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private class ContentTypePair {
		private ContentType inputType;
		private ContentType outputType;

		public ContentTypePair(ContentType inputType, ContentType outputType) {
			set(inputType, outputType);
		}
		
		void set(ContentType inputType, ContentType outputType) {
			this.inputType = inputType;
			this.outputType = outputType;
		}

		@Override
		public int hashCode() {
			return inputType.hashCode()*outputType.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ContentTypePair) {
				ContentTypePair other = (ContentTypePair)obj;
				return inputType.equals(other.inputType) 
						&& outputType.equals(other.outputType);
			}
			return false;
		}
	}
	
	private class DefaultContentType implements ContentType {
		
		private final Class<?> contentClass;
		
		public DefaultContentType(Class<?> contentClass) {
			this.contentClass = contentClass;
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return contentClass.getSimpleName()+"ContentType"; //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return contentClass.getSimpleName();
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return contentClass.getName();
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return ContentTypeRegistry.this;
		}

		/**
		 * @see net.ikarus_systems.icarus.util.data.ContentType#getContentClass()
		 */
		@Override
		public Class<?> getContentClass() {
			return contentClass;
		}

		/**
		 * @see net.ikarus_systems.icarus.util.data.ContentType#getProperties()
		 */
		@Override
		public Map<String, Object> getProperties() {
			return null;
		}
		
	}
}
