/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.dialog;

import java.awt.Component;

import net.ikarus_systems.icarus.resources.ResourceDomain;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public abstract class DialogBuilder {
	
	protected ResourceDomain resourceDomain;

	/**
	 * 
	 */
	public DialogBuilder(ResourceDomain resourceDomain) {
		this.resourceDomain = resourceDomain;
	}

	/**
	 * 
	 */
	public DialogBuilder() {
		this(null);
	}
	
	public abstract void showDialog(Component parent);
	
	/**
	 * @return the resourceDomain
	 */
	public ResourceDomain getResourceDomain() {
		return resourceDomain;
	}

	/**
	 * @param resourceDomain the resourceDomain to set
	 */
	public void setResourceDomain(ResourceDomain resourceDomain) {
		this.resourceDomain = resourceDomain;
	}
}