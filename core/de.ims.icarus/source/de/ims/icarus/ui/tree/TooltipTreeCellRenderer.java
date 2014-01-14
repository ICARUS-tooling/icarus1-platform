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
 * $Revision: 123 $
 * $Date: 2013-07-31 17:22:01 +0200 (Mi, 31 Jul 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/de/ims/icarus/ui/helper/TooltipListCellRenderer.java $
 *
 * $LastChangedDate: 2013-07-31 17:22:01 +0200 (Mi, 31 Jul 2013) $
 * $LastChangedRevision: 123 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui.tree;

import java.awt.Component;
import java.awt.FontMetrics;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.helper.RendererCache;
import de.ims.icarus.util.NamedObject;
import de.ims.icarus.util.id.Identifiable;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id: TooltipListCellRenderer.java 123 2013-07-31 15:22:01Z mcgaerty $
 *
 */
public class TooltipTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -28033820708371349L;

	private static TooltipTreeCellRenderer sharedInstance;

	public static TooltipTreeCellRenderer getSharedInstance() {
		if(sharedInstance==null) {
			sharedInstance = new TooltipTreeCellRenderer();
		}
		return sharedInstance;
	}

	public TooltipTreeCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

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

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);

		int columnWidth = tree.getWidth();
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

		if(textWidth<=columnWidth) {
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
