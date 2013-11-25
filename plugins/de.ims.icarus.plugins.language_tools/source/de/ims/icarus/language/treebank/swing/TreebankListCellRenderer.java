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
package de.ims.icarus.language.treebank.swing;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.SwingConstants;

import org.java.plugin.registry.Extension;

import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankDescriptor;
import de.ims.icarus.language.treebank.TreebankInfo;
import de.ims.icarus.plugins.language_tools.treebank.DefaultSimpleTreebank;
import de.ims.icarus.ui.CompoundIcon;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.Locations;



/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = -2941175073501767602L;
	
	private static TreebankListCellRenderer sharedInstance;
	
	private static final CompoundIcon icon = new CompoundIcon(UIUtil.getBlankIcon(8, 16));

	/**
	 * @return the sharedInstance
	 */
	public static TreebankListCellRenderer getSharedInstance() {
		if(sharedInstance==null) {
			sharedInstance = new TreebankListCellRenderer();
		}
		return sharedInstance;
	}

	/**
	 * 
	 */
	public TreebankListCellRenderer() {
		UIUtil.disableHtml(this);
		
		setHorizontalTextPosition(SwingConstants.RIGHT);
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		setToolTipText(null);
		
		Treebank treebank = null;
		
		if(value instanceof Treebank) {
			treebank = (Treebank) value;
			value = treebank.getName();
		} else if(value instanceof TreebankDescriptor) {
			TreebankDescriptor descriptor = (TreebankDescriptor) value;
			treebank = descriptor.getTreebank();
			value = descriptor.getName();
		} else if(value instanceof TreebankInfo) {
			value = ((TreebankInfo)value).getTreebankName();
		}
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		Icon overlay = null;
		
		if(treebank!=null) {
			Extension readerExtension = null;
			if(treebank instanceof DefaultSimpleTreebank) {
				readerExtension = ((DefaultSimpleTreebank)treebank).getReader();
			}
			Location location = treebank.getLocation();
			
			if(readerExtension==null || location==null) {
				overlay = IconRegistry.getGlobalRegistry().getIcon("unconfigured_co.gif"); //$NON-NLS-1$
			} else if(!Locations.isValid(location)) {
				overlay = IconRegistry.getGlobalRegistry().getIcon("warning_co.gif"); //$NON-NLS-1$
			}
		}
		
		icon.setBottomLeftOverlay(overlay);
		
		setIcon(icon);
		
		return this;
	}
}
