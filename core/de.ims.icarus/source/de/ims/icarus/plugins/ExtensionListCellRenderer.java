/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
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

//	private static ExtensionListCellRenderer sharedInstance;
//
//	public static ExtensionListCellRenderer getSharedInstance() {
//		if(sharedInstance==null) {
//			synchronized (ExtensionListCellRenderer.class) {
//				if(sharedInstance==null) {
//					sharedInstance = new ExtensionListCellRenderer();
//				}
//			}
//		}
//		return sharedInstance;
//	}

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
		} else if(value instanceof String && !"NONE".equals(value)) { //$NON-NLS-1$
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
		} else {
			setIcon(null);
			setToolTipText(null);
		}

		return this;
	}

}
