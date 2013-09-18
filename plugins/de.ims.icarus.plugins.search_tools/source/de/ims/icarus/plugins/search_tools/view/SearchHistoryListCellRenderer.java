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
package de.ims.icarus.plugins.search_tools.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchDescriptor;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.ui.GridBagUtil;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.ProgressBar;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchHistoryListCellRenderer extends JPanel implements ListCellRenderer<SearchDescriptor> {

	private static final long serialVersionUID = 1927522962722110567L;
	
	private JLabel label;
	private ProgressBar progressBar;
	
	private static final Icon dummyIcon = UIUtil.getBlankIcon(7, 8);
	private static final Icon pendingIcon = IconRegistry.getGlobalRegistry().getIcon("synch_co.gif"); //$NON-NLS-1$
	private static final Icon runningIcon = IconRegistry.getGlobalRegistry().getIcon("contention_ovr.gif"); //$NON-NLS-1$
	private static final Icon cancelledIcon = IconRegistry.getGlobalRegistry().getIcon("error_co.gif"); //$NON-NLS-1$
	private static final Icon doneIcon = IconRegistry.getGlobalRegistry().getIcon("installed_ovr.gif"); //$NON-NLS-1$
	
	private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public SearchHistoryListCellRenderer() {
		super(new GridBagLayout());
		
		label = new JLabel();
		label.setHorizontalTextPosition(SwingConstants.RIGHT);
		label.setIconTextGap(4);
		
		progressBar = new ProgressBar(new Dimension(100, 10));
		
		GridBagConstraints gbc = GridBagUtil.makeGbcH(0, 0, 1, 1);
		add(label, gbc);
		
		gbc = GridBagUtil.makeGbcH(0, 1, 1, 1);
		gbc.insets = new Insets(1, 11, 0, 1);
		add(progressBar, gbc);
	}


    private Border getNoFocusBorder() {
        Border border = UIManager.getBorder("List.cellNoFocusBorder"); //$NON-NLS-1$
        return border==null ? noFocusBorder : border;
    }
    
	@Override
	public Component getListCellRendererComponent(JList<? extends SearchDescriptor> list,
			SearchDescriptor descriptor, int index, boolean isSelected, boolean cellHasFocus) {

        setComponentOrientation(list.getComponentOrientation());

        setBackground(list.getBackground());
        setForeground(list.getForeground());

        label.setBackground(list.getBackground());
        label.setForeground(list.getForeground());

        setEnabled(list.isEnabled());
        label.setFont(list.getFont());

        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder"); //$NON-NLS-1$
            }
            if (border == null) {
                border = UIManager.getBorder("List.focusCellHighlightBorder"); //$NON-NLS-1$
            }
        } else {
            border = getNoFocusBorder();
        }
        setBorder(border);

		Search search = descriptor.getSearch();
		SearchResult result = descriptor.getSearchResult();
		Object target = descriptor.getTarget();
		Identity identity = PluginUtil.getIdentity(descriptor.getFactoryExtension());
		ResourceManager rm = ResourceManager.getInstance();
		
		// Generate label
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(index).append("] ").append(identity.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		if(result!=null) {
			sb.append(" (").append(StringUtil.formatDecimal(result.getTotalMatchCount())) //$NON-NLS-1$
			.append(" ").append(rm.get("plugins.searchTools.labels.matches")).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		label.setText(sb.toString());
		
		// Generate tool-tip
		sb.setLength(0);
		// Factory
		sb.append(rm.get("plugins.searchTools.searchManagerView.searchEditor.labels.factory")) //$NON-NLS-1$
			.append(" ").append(identity.getName()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		// Target
		String name = StringUtil.getName(target);
		if(name==null || name.isEmpty()) {
			name = rm.get("plugins.searchTools.undefinedStats"); //$NON-NLS-1$
		}
		sb.append(rm.get("plugins.searchTools.searchManagerView.searchEditor.labels.target")) //$NON-NLS-1$
		.append(" ").append(name).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		// Query
		String query = SearchUtils.getQueryStats(descriptor.getQuery());
		if(query==null || query.isEmpty()) {
			query = rm.get("plugins.searchTools.emptyStats"); //$NON-NLS-1$
		}
		sb.append(rm.get("plugins.searchTools.searchManagerView.searchEditor.labels.query")) //$NON-NLS-1$
		.append(" ").append(query).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		// Result
		String resultString = SearchUtils.getResultStats(result);
		if(resultString==null || resultString.isEmpty()) {
			resultString = rm.get("plugins.searchTools.emptyStats"); //$NON-NLS-1$
		}
		sb.append(rm.get("plugins.searchTools.searchManagerView.searchEditor.labels.result")) //$NON-NLS-1$
		.append(" ").append(resultString).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		// Parameters
		String parameterString = SearchUtils.getParameterStats(descriptor.getParameters());
		sb.append(parameterString);
		
		setToolTipText(UIUtil.toSwingTooltip(sb.toString()));
		
		Icon icon = dummyIcon;
		if(search!=null) {
			if(search.isRunning()) {
				icon = runningIcon;
			} else if(search.isCancelled()) {
				icon = cancelledIcon;
			} else if(search.isDone()) {
				icon = doneIcon;
			} else {
				icon = pendingIcon;
			}
		}
		label.setIcon(icon);
		
		if(search!=null && !search.isDone()) {
			//progressBar.setIndeterminate(SearchUtils.isLoading(target));
			progressBar.setValue(search.getProgress());
			progressBar.setVisible(true);
		} else {
			progressBar.setVisible(false);
		}
		
		return this;
	}

}
