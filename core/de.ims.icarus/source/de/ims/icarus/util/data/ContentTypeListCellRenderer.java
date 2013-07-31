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
package de.ims.icarus.util.data;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import de.ims.icarus.ui.UIUtil;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContentTypeListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 6080540503573355726L;

	public ContentTypeListCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		ContentType contentType = null;
		if(value instanceof ContentType) {
			contentType = (ContentType) value;
		} else if(value instanceof String) {
			try {
				contentType = ContentTypeRegistry.getInstance().getType((String)value);
			} catch(IllegalArgumentException e) {
				// ignore
			}
		}
		
		if(contentType!=null) {
			value = contentType.getName();
		}
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if(contentType!=null) {
			setIcon(contentType.getIcon());
			setToolTipText(UIUtil.toSwingTooltip(contentType.getDescription()));
		} else {
			setIcon(null);
			setToolTipText(null);
		}
		
		return this;
	}

}
