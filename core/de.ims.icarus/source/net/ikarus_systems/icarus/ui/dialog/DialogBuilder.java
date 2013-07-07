/*
 * $Revision: 39 $
 * $Date: 2013-05-17 15:19:31 +0200 (Fr, 17 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/ui/dialog/DialogBuilder.java $
 *
 * $LastChangedDate: 2013-05-17 15:19:31 +0200 (Fr, 17 Mai 2013) $ 
 * $LastChangedRevision: 39 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.ui.dialog;

import java.awt.Component;

import net.ikarus_systems.icarus.resources.ResourceDomain;
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner 
 * @version $Id: DialogBuilder.java 39 2013-05-17 13:19:31Z mcgaerty $
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