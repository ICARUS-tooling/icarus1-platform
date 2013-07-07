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
import java.util.logging.Level;

import javax.swing.Icon;


import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.events.WeakEventSource;
import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.annotation.AnnotationContainer;
import de.ims.icarus.util.id.DuplicateIdentifierException;
import de.ims.icarus.util.id.UnknownIdentifierException;

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
	 * Maps content type ids to filters that are able to directly handle
	 * the mapped type.
	 */
	private Map<String, Collection<Extension>> filters;
	
	/**
	 * Collection of all registered {@code raw} converters, i.e.
	 * all converters that have not been created by this framework
	 * as an result of <i>conversion expansion</i>
	 */
	private Set<DataConverter> rawConverters;
	
	/**
	 * Mapping of all existing conversion chains for fast lookup.
	 */
	private Map<ContentTypePair, DataConverter> converterLookup;
	
	private WeakEventSource eventSource = new WeakEventSource(this);

	private ContentTypeRegistry() {
		// Object content type not supported?
		//addType0(new DefaultContentType(Object.class));
		
		// Java base types
		addType0(new DefaultContentType(Integer.class));
		addType0(new DefaultContentType(Boolean.class));
		addType0(new DefaultContentType(Float.class));
		addType0(new DefaultContentType(Double.class));
		addType0(new DefaultContentType(Short.class));
		addType0(new DefaultContentType(Long.class));
		addType0(new DefaultContentType(Date.class));
		addType0(new DefaultContentType(String.class));
		
		// Utility and Common types
		addType0(new DefaultContentType(Exception.class));
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
	
	public ContentType getType(Extension extension) {
		return getType(extension.getId());
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
		if(clazz==null || clazz==Object.class) {
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
		
		ContentType type = findEnclosingType(clazz);
		if(type==null)
			throw new IllegalArgumentException("No enclosing type defined for class: "+clazz.getName()); //$NON-NLS-1$
		
		return type;
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
	 * <p>
	 * Note that this method does not use the {@link ContentType#accepts(Object)}
	 * filter method to determine assignability but relies on {@link Class#isAssignableFrom(Class)}
	 * with the content classes of the two content types being checked.
	 */
	public Collection<ContentType> getAssignableTypes(ContentType target) {
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
	
	/**
	 * Returns a collection of {@code ContentType}s that are compatible as
	 * per the {@link #isCompatible(ContentType, ContentType)} method.
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
			if(isCompatible(target, type)) {
				compatibleTypes.add(type);
			}
		}
		
		return compatibleTypes;
	}
	
	private static Class<?> getClass(Object obj) {
		if(obj instanceof ContentType) {
			obj = ((ContentType)obj).getContentClass();
		}
		return obj instanceof Class ? (Class<?>)obj : obj.getClass();
	}
	
	/**
	 * Checks whether the given {@code ContentType} accepts the {@code content}
	 * argument. This is done by using the {@link ContentType#accepts(Object)}
	 * method. If {@code content} is of type {@link Class} it is passed <i>as-is</i>
	 * otherwise the result of its {@code Object#getClass()} method is used as
	 * argument.
	 */
	public static boolean isCompatible(ContentType type, Object content) {
		if(type==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		if(content==null)
			throw new IllegalArgumentException("Invalid content"); //$NON-NLS-1$
		
		return type.accepts(getClass(content));
	}
	
	/**
	 * Checks whether {@code ContentType} {@code target} is compatible
	 * towards the {@code type} argument. This check is delegates to {@code type}'s
	 * {@link ContentType#accepts(Object)} method with the result of {@code target}'s
	 * {@link ContentType#getContentClass()}.
	 * <p>
	 * Note that the default {@code ContentType} implementations for common
	 * java data types do a pure {@code Object#equals(Object)} check on the
	 * two {@code Class} objects in question. Implementations of type
	 * {@code ExtensionContentType} honor the {@link ContentType#STRICT_INHERITANCE}
	 * property and either check for class equality or assignability via
	 * {@link Class#isAssignableFrom(Class)}. Custom implementations are free
	 * to use whatever mechanics they seem fit. 
	 */
	public static boolean isCompatible(ContentType type, ContentType target) {
		if(type==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		if(target==null)
			throw new IllegalArgumentException("Invalid target"); //$NON-NLS-1$
		
		return type.accepts(target.getContentClass());
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
	
	public static boolean isStrictType(ContentType type) {
		return CollectionUtils.isTrue(type.getProperties(), ContentType.STRICT_INHERITANCE);
	}
	
	public static String getContentTypeId(Extension extension) {
		return extension.getId();
	}
	
	/**
	 * Tries to fetch the {@code ContentType} describing entries
	 * in the given container. If no type could be fetched a result
	 * value of {@code null} will be returned.
	 */
	public static ContentType getEntryType(Object container) {
		if(container==null)
			throw new IllegalArgumentException("Invalid container"); //$NON-NLS-1$
		
		if(container instanceof DataList) {
			return ((DataList<?>)container).getContentType();
		} else if(container instanceof DataContainer) {
			return ((DataContainer)container).getContentType();
		} else if(container instanceof AnnotationContainer) {
			return ((AnnotationContainer)container).getAnnotationType();
		}
		
		return null;
	}
	
	public boolean isConvertible(ContentType source, ContentType target) {
		return getConverter0(source, target)!=null;
	}
	
	public Collection<ContentType> getConversionTargets(ContentType type, boolean includeCompatible) {
		if(type==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		
		Set<ContentType> targets = new LinkedHashSet<>();
		
		// Collect raw converters
		for(DataConverter converter : rawConverters) {
			if(type.equals(converter.getInputType()) ||
					includeCompatible && isCompatible(type, converter.getInputType())) {
				targets.add(converter.getResultType());
			}
		}
		
		// Collect derived converters
		if(converterLookup!=null) {
			for(ContentTypePair key : converterLookup.keySet()) {
				if(type.equals(key.getInputType()) ||
						includeCompatible && isCompatible(type, key.getInputType())) {
					targets.add(key.getOutputType());
				}
			}
		}
		
		return targets;
	}
	
	public Object convert(Object data, Object targetType, Options options) throws DataConversionException {
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
		
		return converter.convert(data, options);
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

	/**
	 * Returns only those filters that are explicitly declared
	 * to handle the given {@code ContentType}.
	 */
	public Collection<Extension> getFilters(ContentType contentType) {
		return getFilters(contentType, false);
	}
	
	public Collection<Extension> getFilters(ContentType contentType, boolean includeCompatible) {
		if(contentType==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		
		if(filters==null) {
			return Collections.emptyList();
		}
		
		Collection<Extension> availableFilters = new ArrayList<>();
		
		ContentTypeCollection types = new ContentTypeCollection();
		if(includeCompatible) {
			types.addTypes(getCompatibleTypes(contentType));
		}
		types.addType(contentType);
		
		for(ContentType type : types.getContentTypes()) {
			Collection<Extension> list = filters.get(type.getId());
			if(list==null) {
				continue;
			}
			
			availableFilters.addAll(list);
		}
		
		return availableFilters;
	}
	
	/**
	 * Registers the filter represented by the given {@code Extension}
	 */
	public void addFilter(Extension extension) {
		if(extension==null)
			throw new IllegalArgumentException("Invalid extension"); //$NON-NLS-1$
		
		if(filters==null) {
			filters = new HashMap<>();
		}
		
		Extension contentTypeExtension = extension.getParameter("contentType").valueAsExtension(); //$NON-NLS-1$
		String contentTypeId = getContentTypeId(contentTypeExtension);
		
		Collection<Extension> list = filters.get(contentTypeId);
		if(list==null) {
			list = new LinkedHashSet<>();
			filters.put(contentTypeId, list);
		}
		list.add(extension);

		eventSource.fireEvent(new EventObject(Events.ADDED, "filter", extension)); //$NON-NLS-1$
	}
	
	private synchronized void checkConverters() {
		if(rawConverters==null) {
			rawConverters = new LinkedHashSet<>();
			
			PluginDescriptor descriptor = PluginUtil.getCorePlugin();
			for(Extension extension : descriptor.getExtensionPoint("DataConverter").getConnectedExtensions()) { //$NON-NLS-1$
				try {
					ContentTypeRegistry.getInstance().addConverter((DataConverter) PluginUtil.instantiate(extension));
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to register data converter: "+extension.getUniqueId(), e); //$NON-NLS-1$
				}
			}
		}
	}
	
	public void addConverter(DataConverter converter) {
		if(converter==null)
			throw new IllegalArgumentException("Invalid converter"); //$NON-NLS-1$
		
		checkConverters();
		
		rawConverters.add(converter);

		addConverter0(converter);
		
		eventSource.fireEvent(new EventObject(Events.ADDED, "converter", converter)); //$NON-NLS-1$
	}
	
	public List<DataConverter> availableConverters() {
		checkConverters();
		
		return new ArrayList<>(rawConverters);
	}
	
	public int availableConverterCount() {
		checkConverters();
		
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
		checkConverters();
		
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
		checkConverters();
		
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

		checkConverters();
		
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

		ContentType getInputType() {
			return inputType;
		}

		ContentType getOutputType() {
			return outputType;
		}
	}
	
	private class DefaultContentType implements ContentType {
		
		private final Class<?> contentClass;
		
		public DefaultContentType(Class<?> contentClass) {
			this.contentClass = contentClass;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return contentClass.getSimpleName()+"ContentType"; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return contentClass.getSimpleName();
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return contentClass.getName();
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return ContentTypeRegistry.this;
		}

		/**
		 * @see de.ims.icarus.util.data.ContentType#getContentClass()
		 */
		@Override
		public Class<?> getContentClass() {
			return contentClass;
		}

		/**
		 * @see de.ims.icarus.util.data.ContentType#getProperties()
		 */
		@Override
		public Map<String, Object> getProperties() {
			return null;
		}

		/**
		 * @see de.ims.icarus.util.Filter#accepts(java.lang.Object)
		 */
		@Override
		public boolean accepts(Object obj) {
			return getContentClass().equals(obj);
		}
		
	}
}
