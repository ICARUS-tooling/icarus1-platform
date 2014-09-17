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
package de.ims.icarus.plugins.coref.view.manager;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.SwingConstants;

import org.java.plugin.registry.Extension;

import de.ims.icarus.io.Loadable;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.DecoratedIcon;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.list.TooltipListCellRenderer;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.Locations;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceListCellRenderer extends TooltipListCellRenderer {

	private static final long serialVersionUID = 4329776900287250446L;

	private static final DecoratedIcon cellIcon = new DecoratedIcon(UIUtil.getBlankIcon(16, 16));

	private static final Icon loadingIcon = IconRegistry.getGlobalRegistry().getIcon("waiting_ovr.gif"); //$NON-NLS-1$
	private static final Icon loadedIcon = IconRegistry.getGlobalRegistry().getIcon("version_controlled.gif"); //$NON-NLS-1$
	private static final Icon invalidSettingsIcon = IconRegistry.getGlobalRegistry().getIcon("unconfigured_co.gif"); //$NON-NLS-1$
	private static final Icon invalidLocationIcon = IconRegistry.getGlobalRegistry().getIcon("warning_co.gif"); //$NON-NLS-1$

	private static final Icon documentSetIcon = IconRegistry.getGlobalRegistry().getIcon("file_obj.gif"); //$NON-NLS-1$

	public CoreferenceListCellRenderer() {
		UIUtil.disableHtml(this);
	}

	/**
	 * @see de.ims.icarus.ui.list.TooltipListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		Icon overlay = null;
		if(value instanceof Loadable) {
			Loadable loadable = (Loadable) value;

			if(loadable.isLoading()) {
				overlay = loadingIcon;
			} else if(loadable.isLoaded()) {
				overlay = loadedIcon;
			}
		}

		DocumentSetDescriptor descriptor = (DocumentSetDescriptor)value;
		value = descriptor.getName();
		Extension readerExtension = descriptor.getReaderExtension();
		Location location = descriptor.getLocation();

		StringBuilder sb = new StringBuilder(200);
		sb.append(ResourceManager.getInstance().get("plugins.languageTools.labels.name")) //$NON-NLS-1$
			.append(": ") //$NON-NLS-1$
			.append(descriptor.getName())
			.append("\n"); //$NON-NLS-1$
		String path = Locations.getPath(descriptor.getLocation());
		sb.append(ResourceManager.getInstance().get("plugins.languageTools.labels.location")) //$NON-NLS-1$
			.append(": ") //$NON-NLS-1$
			.append(path==null ? "?" : path); //$NON-NLS-1$

		String tooltip = UIUtil.toSwingTooltip(sb.toString());
		Icon icon = documentSetIcon;

		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		if(icon!=null) {
			cellIcon.setBaseIcon(icon);
		} else {
			cellIcon.setBaseIcon(UIUtil.getBlankIcon(16, 16));
		}

		cellIcon.removeDecorations();

		if(readerExtension==null || location==null) {
			overlay = invalidSettingsIcon;
		} else if(!Locations.isValid(location)) {
			overlay = invalidLocationIcon;
		}

		if(overlay!=null) {
			cellIcon.addDecoration(overlay, SwingConstants.SOUTH_WEST);
		}

		setIcon(cellIcon);
		setToolTipText(tooltip);

		return this;
	}

}
