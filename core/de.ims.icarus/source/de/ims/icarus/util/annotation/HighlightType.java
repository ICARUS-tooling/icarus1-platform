/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util.annotation;

import javax.swing.Icon;
import javax.xml.bind.annotation.XmlEnum;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.id.Identity;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlEnum
public enum HighlightType implements Identity {

	BACKGROUND("background"), //$NON-NLS-1$
	FOREGROUND("foreground"), //$NON-NLS-1$
	UNDERLINED("underlined"), //$NON-NLS-1$
	OUTLINED("outlined"), //$NON-NLS-1$
	ITALIC("italic"), //$NON-NLS-1$
	BOLD("bold"); //$NON-NLS-1$
	
	private final String key;
	
	private HighlightType(String key) {
		this.key = key;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return getClass().getSimpleName();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"core.helpers.highlightType."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"core.helpers.highlightType."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
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
		return this;
	}
	
}
