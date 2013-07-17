/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.dialog;

import java.awt.Component;

import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public abstract class DialogBuilder {
	
	public static final String RESIZABLE_OPTION = "resizable"; //$NON-NLS-1$
	
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
	
	public abstract void showDialog(Component parent, Options options);
	
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