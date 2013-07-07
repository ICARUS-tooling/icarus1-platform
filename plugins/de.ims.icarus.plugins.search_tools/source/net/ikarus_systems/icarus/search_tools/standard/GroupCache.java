/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.standard;

import net.ikarus_systems.icarus.search_tools.result.ResultEntry;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface GroupCache {
	
	void cacheGroupInstance(int id, Object value);
	
	void lock();
	
	void reset();
	
	void commit(ResultEntry entry);
	
	/**
	 * 
	 */
	public static GroupCache dummyCache = new GroupCache() {
		
		@Override
		public void cacheGroupInstance(int id, Object value) {
			// do nothing
		}

		@Override
		public void lock() {
		}

		@Override
		public void reset() {
		}

		@Override
		public void commit(ResultEntry entry) {
			// no-op
		}
	};
}
