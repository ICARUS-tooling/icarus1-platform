/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools;

import javax.swing.Icon;

import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum SearchMode implements Identity {
	
	/**
	 * Every single hit encountered in a target graph
	 * should be cached and the graph reported as a whole.
	 * This effectively implies exhaustive searching!
	 */
	HITS("hits", true), //$NON-NLS-1$
	
	/**
	 * Every single hit encountered in a target graph
	 * should be reported independently. This effectively
	 * implies exhaustive searching!
	 */
	INDEPENDENT_HITS("independentHits", true), //$NON-NLS-1$
	
	/**
	 * Only the first hit in a target graph should be reported.
	 * Further processing of that graph is not necessary.
	 */
	MATCHES("matches", false); //$NON-NLS-1$
	
	private final String key;
	private final boolean exhaustive;
	
	private SearchMode(String key, boolean exhaustive) {
		this.key = key;
		this.exhaustive = exhaustive;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return getClass().getSimpleName();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.searchMode."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.searchMode."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
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
		return this;
	}

	public boolean isExhaustive() {
		return exhaustive;
	}
}
