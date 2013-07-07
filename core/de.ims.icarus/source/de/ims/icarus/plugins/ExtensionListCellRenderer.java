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

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;


import org.java.plugin.registry.Extension;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ExtensionListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 144607075843240899L;

	/**
	 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		Extension extension = null;
		Identity identity = null;
		if(value instanceof Extension) {
			extension = (Extension) value;
		} else if(value instanceof String) {
			try {
				extension = PluginUtil.getExtension((String)value);
			} catch(Exception e) {
				// ignore
			}
		}
		if(extension!=null) {
			identity = PluginUtil.getIdentity(extension);
		}
		if(identity!=null) {
			value = identity.getName();
		} else if(value instanceof String) {
			value = ResourceManager.getInstance().get((String)value);
		}
		
		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		
		if(identity!=null) {
			setIcon(identity.getIcon());
			setToolTipText(UIUtil.toSwingTooltip(identity.getDescription()));
		}
		
		return this;
	}
	
}
