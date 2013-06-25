/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view;

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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import net.ikarus_systems.hermes.ui.awt.ProgressBar;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchDescriptor;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.ui.GridBagUtil;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.util.StringUtil;
import net.ikarus_systems.icarus.util.id.Identity;
import sun.swing.DefaultLookup;

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
        Border border = DefaultLookup.getBorder(this, ui, "List.cellNoFocusBorder"); //$NON-NLS-1$
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
                border = DefaultLookup.getBorder(this, ui, "List.focusSelectedCellHighlightBorder"); //$NON-NLS-1$
            }
            if (border == null) {
                border = DefaultLookup.getBorder(this, ui, "List.focusCellHighlightBorder"); //$NON-NLS-1$
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
			sb.append(" (").append(result.getTotalMatchCount()) //$NON-NLS-1$
			.append(" ").append(rm.get("plugins.searchTools.labels.hits")).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
			progressBar.setValue(search.getProgress());
			progressBar.setVisible(true);
		} else {
			progressBar.setVisible(false);
		}
		
		return this;
	}

}