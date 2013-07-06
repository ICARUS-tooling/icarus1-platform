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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class DataSourceFactory {
	
	private static DataSourceFactory instance;
	
	public static DataSourceFactory getInstance() {
		if(instance==null) {
			synchronized (DataSourceFactory.class) {
				if(instance==null) {
					instance = new DataSourceFactory();
				}
			}
		}
		return instance;
	}
	
	private Map<Object, Reference<DataSource>> cache;
	
	private DataSourceFactory() {
		// no-op
	}
	
	private DataSource getCachedSource(Object key) {
		if(cache==null) {
			return null;
		}
		
		Reference<DataSource> ref = cache.get(key);
		return ref==null ? null : ref.get();
	}
	
	private void cacheSource(Object key, DataSource dataSource) {
		if(cache==null) {
			cache = new HashMap<>();
			cache = Collections.synchronizedMap(cache);
		}
		
		Reference<DataSource> ref = new WeakReference<DataSource>(dataSource);
		cache.put(key, ref);
	}
	
	public DataSource getConfigDataSource(Handle handle, ChangeListener listener) {
		if(handle==null)
			throw new IllegalArgumentException("Invalid handle"); //$NON-NLS-1$
				
		DataSource dataSource = getCachedSource(handle);
		if(dataSource==null) {
			dataSource = new ConfigDataSource(handle);
			cacheSource(handle, dataSource);
		}
		
		if(listener!=null) {
			dataSource.addChangeListener(listener);
		}
		
		return dataSource;
	}
	
	public DataSource getConfigDataSource(ConfigRegistry registry, String path, ChangeListener listener) {
		if(path==null)
			throw new IllegalArgumentException("Invalid path"); //$NON-NLS-1$
		
		if(registry==null) {
			registry = ConfigRegistry.getGlobalRegistry();
		}
		
		Handle handle = registry.getHandle(path);
		if(handle==null)
			throw new IllegalArgumentException("Unknown config path: "+path); //$NON-NLS-1$
		
		return getConfigDataSource(handle, listener);
	}

	public DataSource getConfigDataSource(String path, ChangeListener listener) {
		return getConfigDataSource(null, path, listener);
	}
	
	private class DelegatingDataSource extends AbstractDataSource implements ChangeListener {
		
		private final DataSource source;

		DelegatingDataSource(DataSource source) {
			this.source = source;
			
			new OwnedChangeListener(this, source);
		}

		/**
		 * @see de.ims.icarus.util.data.DataSource#getData()
		 */
		@Override
		public Object getData() {
			return source.getData();
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			fireDataChanged();
		}
		
	}
	
	private static class OwnedChangeListener implements ChangeListener {
		private final Reference<DataSource> owner;
		private final DataSource target;

		OwnedChangeListener(DataSource owner,
				DataSource target) {
			this.owner = new WeakReference<>(owner);
			this.target = target;
			
			target.addChangeListener(this);
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			DataSource dataSource = owner.get();
			if(dataSource==null) {
				target.removeChangeListener(this);
				return;
			}
			
			if(dataSource instanceof ChangeListener) {
				((ChangeListener)dataSource).stateChanged(e);
			}
		}
	}

	private class ConfigDataSource extends AbstractDataSource {
		
		private final Handle handle;

		ConfigDataSource(Handle handle) {
			this.handle = handle;
			
			new OwnedConfigListener(this);
		}

		/**
		 * @see de.ims.icarus.util.data.DataSource#getData()
		 */
		@Override
		public Object getData() {
			ConfigRegistry registry = handle.getSource();
			return registry.getValue(handle);
		}
	}
	
	private class OwnedConfigListener implements ConfigListener {
		
		private final Handle handle;
		private final Reference<ConfigDataSource> owner;
		
		OwnedConfigListener(ConfigDataSource owner) {
			this.owner = new WeakReference<>(owner);
			handle = owner.handle;
		}

		/**
		 * @see de.ims.icarus.config.ConfigListener#invoke(de.ims.icarus.config.ConfigRegistry, de.ims.icarus.config.ConfigEvent)
		 */
		@Override
		public void invoke(ConfigRegistry sender, ConfigEvent event) {
			ConfigDataSource dataSource = owner.get();
			if(dataSource==null) {
				ConfigRegistry registry = handle.getSource();
				registry.removeListener(this);
			}
			
			if(event.getHandle()==handle) {
				dataSource.fireDataChanged();
			}
		}
	}
}
