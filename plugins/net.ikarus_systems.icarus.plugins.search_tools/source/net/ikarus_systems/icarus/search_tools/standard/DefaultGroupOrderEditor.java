/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.standard;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.ikarus_systems.icarus.search_tools.Grouping;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.ui.GridBagUtil;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.util.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultGroupOrderEditor {

	protected JPanel contentPanel;
	protected final Integer[] permutation;
	protected final SearchResult searchResult;
	protected GroupListRenderer renderer;
	
	public DefaultGroupOrderEditor(SearchResult searchResult) {
		if(searchResult==null)
			throw new IllegalArgumentException("Invalid search-result"); //$NON-NLS-1$
		if(searchResult.getDimension()<getMinimumDimension())
			throw new IllegalArgumentException("Illegal result dimension: "+searchResult.getDimension()); //$NON-NLS-1$
		
		this.searchResult = searchResult;
		
		permutation = new Integer[searchResult.getDimension()];
		CollectionUtils.fillAscending(permutation);
	}
	
	public JPanel getContentPanel() {
		if(contentPanel==null) {
			buildContentPanel();
			refresh();
		}
		
		return contentPanel;
	}
	
	protected void buildContentPanel() {
		contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayout(permutation.length, 1));
		
		for(int i=0; i<permutation.length; i++) {
			contentPanel.add(new GroupListRenderer(i));
		}
		
		contentPanel.setBorder(UIUtil.topLineBorder);
		contentPanel.setMinimumSize(contentPanel.getPreferredSize());
	}
	
	public void refresh() {
		if(contentPanel==null) {
			return;
		}
		
		for(int i=0; i<contentPanel.getComponentCount(); i++) {
			GroupListRenderer renderer = (GroupListRenderer) contentPanel.getComponent(i);
			renderer.refresh();
		}
	}
	
	protected int getMinimumDimension() {
		return 3;
	}
	
	public SearchResult getSearchResult() {
		return searchResult;
	}
	
	protected void move(int index, boolean up) {
		int newIndex = up ? index-1 : index+1;
		
		if(newIndex<0 || newIndex>=permutation.length) {
			return;
		}
		
		Integer tmp = permutation[newIndex];
		permutation[newIndex] = permutation[index];
		permutation[index] = tmp;
		
		refresh();
	}
	
	public int[] getPermutation() {
		int[] result = new int[permutation.length];
		
		for(int i=0; i<permutation.length; i++) {
			result[i] = permutation[i];
		}
		
		return result;
	}

	protected class GroupListRenderer extends JPanel implements ActionListener {
		
		private static final long serialVersionUID = 8073451338783830446L;
		
		protected JLabel label;
		protected JButton upButton;
		protected JButton downButton;
		
		private final int index;
		
		public GroupListRenderer(int index) {
			super(new GridBagLayout());
			
			this.index = index;
			
			label = new JLabel();
			label.setHorizontalAlignment(SwingConstants.LEFT);
			UIUtil.disableHtml(label);
			
			upButton = createButton("up"); //$NON-NLS-1$
			downButton = createButton("down"); //$NON-NLS-1$
			
			add(label, GridBagUtil.makeGbcH(0, 0, 1, 1));			
			add(downButton, GridBagUtil.makeGbc(1, 0));			
			add(upButton, GridBagUtil.makeGbc(2, 0));

			downButton.setEnabled(index<permutation.length-1);
			upButton.setEnabled(index>0);
			
			setPreferredSize(new Dimension(100, 30));
		}
		
		protected JButton createButton(String command) {
			JButton button = new JButton();
			button.setIcon(IconRegistry.getGlobalRegistry().getIcon(
					command+".gif")); //$NON-NLS-1$
			button.setActionCommand(command);
			button.setFocusable(false);
			button.setFocusPainted(false);
			button.setRolloverEnabled(true);
			button.addActionListener(this);
			UIUtil.resizeComponent(button, 24, 18);
			
			return button;
		}
		
		public void refresh() {
			int index = permutation[this.index];
			
			int groupId = SearchUtils.getGroupId(getSearchResult(), index);
			label.setForeground(Grouping.getGrouping(groupId).getColor());
			
			String text = String.format("[%d] %s", groupId, getSearchResult().getGroupLabel(index)); //$NON-NLS-1$
			label.setText(text);
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultGroupOrderEditor.this.move(index, "up".equals(e.getActionCommand())); //$NON-NLS-1$
		}
	}
}
