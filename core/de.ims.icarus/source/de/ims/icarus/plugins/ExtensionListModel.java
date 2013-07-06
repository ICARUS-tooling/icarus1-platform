/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ExtensionListModel extends AbstractListModel<Extension> implements ComboBoxModel<Extension> {

	private static final long serialVersionUID = -1741605769815304617L;
	
	private List<Extension> extensions = new ArrayList<>();
	
	private Extension selectedExtension = null;
	
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

	/**
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object anItem) {
		if(anItem!=null && !(anItem instanceof Extension))
			throw new IllegalArgumentException("Unsupported item: "+anItem); //$NON-NLS-1$
		
		if((selectedExtension!=null && !selectedExtension.equals(anItem))
				|| (selectedExtension==null && anItem!=null)) {
			selectedExtension = (Extension) anItem;
			
			fireContentsChanged(this, -1, -1);
		}
	}

	/**
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		return selectedExtension;
	}

}
