/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.coref.text;

import javax.swing.Icon;

import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum HighlightType implements Identity {
	BACKGROUND("background"), //$NON-NLS-1$
	FOREGROUND("foreground"), //$NON-NLS-1$
	UNDERLINED("underlined"), //$NON-NLS-1$
	BOLD("bold"), //$NON-NLS-1$
	ITALIC("italic"); //$NON-NLS-1$
	
	private final String key;
	
	private HighlightType(String key) {
		this.key = key;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return toString();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return ResourceManager.getInstance().get("plugins.coref.highlightTypes."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get("plugins.coref.highlightTypes."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
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
}
