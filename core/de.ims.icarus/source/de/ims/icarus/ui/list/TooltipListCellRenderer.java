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
package de.ims.icarus.ui.list;

import java.awt.Component;
import java.awt.FontMetrics;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.helper.RendererCache;
import de.ims.icarus.util.NamedObject;
import de.ims.icarus.util.id.Identifiable;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TooltipListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 4283938142282983275L;

	private static TooltipListCellRenderer sharedInstance;

	public static TooltipListCellRenderer getSharedInstance() {
		if(sharedInstance==null) {
			sharedInstance = new TooltipListCellRenderer();
		}
		return sharedInstance;
	}

	public TooltipListCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {

		String tooltip = null;
		Icon icon = null;
		if(value instanceof Identifiable) {
			value = ((Identifiable)value).getIdentity();
		} else if(value instanceof NamedObject) {
			value = ((NamedObject)value).getName();
		}
		if(value instanceof Identity) {
			Identity identity = (Identity) value;
			value = identity.getName();
			tooltip = identity.getDescription();
			icon = identity.getIcon();
		}

		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		int columnWidth = list.getWidth();
		if(icon!=null) {
			columnWidth -= icon.getIconWidth()+getIconTextGap();
		}
		int textWidth = 0;

		if(tooltip==null) {
			tooltip = getText();
		}

		if(tooltip!=null && !tooltip.isEmpty()) {
			FontMetrics fm = getFontMetrics(getFont());
			textWidth = fm.stringWidth(tooltip);
		}

		if(textWidth<=columnWidth && tooltip.equals(getText())) {
			tooltip = null;
		}

		setIcon(icon);

		setToolTipText(UIUtil.toSwingTooltip(tooltip));

		return this;
	}


	/**
	 * @see javax.swing.JLabel#updateUI()
	 */
	@Override
	public void updateUI() {
		if(!RendererCache.getInstance().requiresNewUI(this)) {
			return;
		}

		super.updateUI();
	}
}
