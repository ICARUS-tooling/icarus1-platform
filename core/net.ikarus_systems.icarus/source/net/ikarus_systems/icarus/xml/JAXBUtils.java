/**
 * 
 */
package net.ikarus_systems.icarus.xml;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import net.ikarus_systems.icarus.util.Exceptions;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.strategy.Strategy;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class JAXBUtils {

	private static Registry registry; 
	private static Serializer serializer;
	
	private static Map<Object, Object> wrapperMap
		= new Hashtable<Object, Object>();
	
	static {
		registerCoreType(Integer.class);
		registerCoreType(Double.class);
		registerCoreType(Float.class);
		registerCoreType(Short.class);
		registerCoreType(Long.class);
		registerCoreType(Byte.class);
		registerCoreType(Character.class);
		registerCoreType(String.class);
		registerCoreType(Boolean.class);
		registerCoreType(URL.class);
		registerCoreType(Date.class);
		registerCoreType(Locale.class);
		registerCoreType(Currency.class);
		registerCoreType(TimeZone.class);
		registerCoreType(GregorianCalendar.class);
		registerCoreType(File.class);
		registerCoreType(Time.class);
		registerCoreType(java.sql.Date.class);
		registerCoreType(Timestamp.class);
		registerCoreType(BigDecimal.class);
		registerCoreType(BigInteger.class);
		
		registerWrapper(List.class, ListWrapper.class);
		registerWrapper(Map.class, MapWrapper.class);
	}

	private JAXBUtils(){
		// no-op
	};
	
	public static void bindConverter(Class<?> type, Converter<?> converter) 
			throws Exception {
		Exceptions.testNullArgument(type, "type");
		Exceptions.testNullArgument(converter, "converter");
		
		getRegistry().bind(type, converter);
	}
	
	public static void bindConverter(Class<?> type, Class<?> converter) 
			throws Exception {
		Exceptions.testNullArgument(type, "type");
		Exceptions.testNullArgument(converter, "converter");
		
		getRegistry().bind(type, converter);
	}
	
	private static Registry getRegistry() {
		if(registry==null) {
			registry = new Registry();
		}
		return registry;
	}
	
	public static Serializer getSharedSerializer() {
		if(serializer==null) {
			Strategy strategy = new RegistryStrategy(getRegistry());
			serializer = new Persister(strategy);
		}
		
		return serializer;
	}
	
	public static Serializer newSerializer(Strategy strategy) {
		return new Persister(new RegistryStrategy(getRegistry(), strategy));
	}
	
	public static Serializer newCycleSerializer() {
		return new Persister(new RegistryStrategy(getRegistry(),
				new CycleStrategy("ref_id", "ref")));
	}
	
	public static Serializer newSerializer() {
		return new Persister(new RegistryStrategy(getRegistry()));
	}
	
	public static Serializer newBasicSerializer() {
		return new Persister();
	}
	
	public static void registerCoreType(Class<?> clazz) {
		wrapperMap.put(clazz, true);
	}
	
	public static void registerWrapper(Class<?> clazz, 
			Class<? extends Wrapper> wrapperClazz) {
		wrapperMap.put(clazz, wrapperClazz);
	}
	
	private static Class<?> getProxy(Object o) {
		if(o instanceof List<?>)
			return List.class;
		else if(o instanceof Map<?, ?>)
			return Map.class;
		else return o.getClass();
	}
	
	public static Object wrap(Object o) {
		return wrap(o, null);
	}
	
	public static Object wrap(Object o, Class<?> proxy) {
		if(o==null)
			return null;
		
		if(o instanceof Wrapper)
			return o;
		
		Class<?> clazz = proxy==null ? getProxy(o) : proxy;
		if(clazz.isArray())
			return o;
		
		if(clazz.getAnnotation(Root.class)!=null)
			return o;
		
		Object hint = wrapperMap.get(clazz);
		if(hint instanceof Boolean)
			return o;
		
		if(hint==null) {
			// TODO what to do here?
			throw new IllegalArgumentException("No wrapper registered for "+o);
		}
		
		try {
			Wrapper wrapper = (Wrapper) ((Class<?>)hint).newInstance();
			
			wrapper.wrap(o);
			
			return wrapper;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return o;
	}
	
	public static Object unwrap(Object o) {		
		if(o instanceof Wrapper)
			return ((Wrapper)o).unwrap();
		else
			return o;
	}
	
	/**
	 * 
	 * @author Markus G�rtner
	 *
	 */
	public interface Wrapper {
		
		void wrap(Object o);
		
		Object unwrap();
	}
	
	@Root(name="list")
	public static class ListWrapper implements Wrapper {

		@ElementListUnion( {
				@ElementList(entry = "string", inline = true, type = String.class),
				@ElementList(entry = "double", inline = true, type = Double.class),
				@ElementList(entry = "float", inline = true, type = Float.class),
				@ElementList(entry = "short", inline = true, type = Short.class),
				@ElementList(entry = "byte", inline = true, type = Byte.class),
				@ElementList(entry = "integer", inline = true,type = Integer.class),
				@ElementList(entry = "long", inline = true, type = Long.class),
				@ElementList(entry = "char", inline = true, type = Character.class),
				@ElementList(entry = "boolean", inline = true, type = Boolean.class),
				@ElementList(entry = "item", inline = true, type = Object.class) })
		private List<?> items;
		
		public ListWrapper() {
			// only used by the serializer
		}
		
		@Override
		public List<?> unwrap() {
			return items;
		}
		
		@Override
		public void wrap(Object o) {
			items = (List<?>) o;
		}
	}
	
	/**
	 * 
	 * @author Markus G�rtner
	 *
	 */
	@Root(name="map")
	public static class MapWrapper implements Wrapper {
		
		@ElementMap
		private Map<?, ?> items;

		@Override
		public Object unwrap() {
			return items;
		}

		@Override
		public void wrap(Object o) {
			items = (Map<?, ?>) o;
		}
		
	}
}
