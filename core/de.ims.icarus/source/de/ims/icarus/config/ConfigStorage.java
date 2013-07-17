/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.config;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ConfigStorage {
	
	/**
	 * Sets the {@link ConfigRegistry} for this
	 * storage.<p>
	 * The registry should only be used for callback
	 * reasons when data in the storage has been
	 * updated and entries in the registry have to
	 * be synchronized.
	 * 
	 * @param registry
	 */
	void setRegistry(ConfigRegistry registry);

	/**
	 * Returns the object currently being stored
	 * for the given path.<p>
	 * Note that a storage is not meant to hold
	 * every data within a <code>ConfigRegistry</code> and
	 * may therefore return <code>null</code> for a lot
	 * of paths.
	 * Often only values that differ from the default values
	 * are forwarded to a storage instance.
	 * 
	 * @param path
	 * @return
	 */
	Object getValue(String path);
	
	/**
	 * Stores the new value for the given path.<p>
	 * The actual further reaction to this call
	 * is implementation dependent. Some storages
	 * will mirror every change instantly to the
	 * underlying storage back-end, others will delay
	 * such saving or even omit it totally and rely
	 * on user side decisions.
	 * 
	 * @param path
	 * @param value
	 */
	void setValue(String path, Object value);
	
	/**
	 * Tells the storage to asynchronously reload its 
	 * data from whatever source it is linked with.
	 */
	void update();
	
	/**
	 * Loads data synchronously from the underlying data storage.
	 */
	void updateNow();
	
	/**
	 * Tells the storage to asynchronously save its
	 * data to the underlying resource location.
	 */
	void commit();
	
	void commitNow();
	
	boolean hasUnsavedChanges();
}
