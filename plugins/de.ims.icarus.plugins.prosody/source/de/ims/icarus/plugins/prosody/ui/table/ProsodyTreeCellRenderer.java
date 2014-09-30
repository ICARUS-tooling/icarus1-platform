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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.ui.table;

import java.awt.Component;
import java.awt.FontMetrics;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.ims.icarus.plugins.prosody.ui.view.SyllableInfo;
import de.ims.icarus.plugins.prosody.ui.view.WordInfo;
import de.ims.icarus.ui.UIUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 6623681132417803053L;


	/**
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		String tooltip = null;
		if(value instanceof WordInfo) {
			value = Column.rootColumn.getValue((WordInfo)value);
		} else if(value instanceof SyllableInfo) {
			value = Column.rootColumn.getValue((SyllableInfo)value);
		}

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);

		int columnWidth = tree.getWidth();
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

		setIcon(null);

		setToolTipText(UIUtil.toSwingTooltip(tooltip));

		return this;
	}
}
