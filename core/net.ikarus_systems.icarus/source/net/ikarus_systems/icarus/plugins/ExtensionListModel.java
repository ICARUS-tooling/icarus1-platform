/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ExtensionListModel extends AbstractListModel<Extension> {

	private static final long serialVersionUID = -1741605769815304617L;
	
	private List<Extension> extensions = new ArrayList<>();
	
	public ExtensionListModel(Collection<Extension> items, boolean doSort) {
		if(items==null)
			throw new IllegalArgumentException("Invalid items collection"); //$NON-NLS-1$
		
		extensions.addAll(items);
		if(doSort) {
			Collections.sort(extensions, PluginUtil.EXTENSION_COMPARATOR);
		}
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return extensions.size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Extension getElementAt(int index) {
		return extensions.get(index);
	}

}
