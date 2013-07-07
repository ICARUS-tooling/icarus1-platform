/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.ims.icarus.config.EntryHandler;
import de.ims.icarus.resources.Localizable;
import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ColumnListHandler extends JPanel implements EntryHandler, 
		ActionListener, MouseListener, ListSelectionListener, Localizable {
	
	private static final long serialVersionUID = -1411123969553542551L;
	
	private List<ColumnInfo> columns;
	
	private JList<ColumnInfo> columnList;
	private JButton toggleButton;
	private JButton upButton;
	private JButton downButton;
	
	public ColumnListHandler() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.config.EntryHandler#setValue(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setValue(Object value) {
		columns = (List<ColumnInfo>) value;
	}

	/**
	 * @see de.ims.icarus.config.EntryHandler#getValue()
	 */
	@Override
	public Object getValue() {
		return columns;
	}
	
	private void buildPanel() {
		columnList = new JList<>(new ColumnListModel());
		JScrollPane scrollPane = new JScrollPane(columnList);
		
		toggleButton = new JButton();
		toggleButton.addActionListener(this);
		
		upButton = new JButton();
		upButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("navi_up.png")); //$NON-NLS-1$
		upButton.addActionListener(this);

		downButton = new JButton();
		downButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("navi_down.png")); //$NON-NLS-1$
		downButton.addActionListener(this);
		
		localize();
		
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(toggleButton, BorderLayout.NORTH);
		buttonPanel.add(upButton, BorderLayout.CENTER);
		buttonPanel.add(downButton, BorderLayout.SOUTH);
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = gbc.gridy = 0;
		gbc.weightx = gbc.weighty = 100;
		gbc.fill = GridBagConstraints.BOTH;
		add(scrollPane, gbc);
		
		gbc.gridx++;
		gbc.weightx = gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTH;
		add(buttonPanel, gbc);
		
		refreshButtons();
	}
	
	private void refreshButtons() {
		if(columnList==null) {
			return;
		}
		
		int index = columnList.getSelectedIndex();
		ColumnInfo info = index==-1 ? null : columns.get(index);
		
		toggleButton.setEnabled(index!=-1 && !info.isRequired());
		upButton.setEnabled(index<columnList.getModel().getSize()-1);
		downButton.setEnabled(index>0);
	}

	/**
	 * @see de.ims.icarus.resources.Localizable#localize()
	 */
	@Override
	public void localize() {
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();

		toggleButton.setText(resourceDomain.get("toggle")); //$NON-NLS-1$
		upButton.setText(resourceDomain.get("up")); //$NON-NLS-1$
		downButton.setText(resourceDomain.get("down")); //$NON-NLS-1$
		
		columnList.repaint();
	}

	/**
	 * @see de.ims.icarus.config.EntryHandler#getComponent()
	 */
	@Override
	public Component getComponent() {
		if(columnList==null) {
			buildPanel();
		}
		return this;
	}

	/**
	 * @see de.ims.icarus.config.EntryHandler#isValueEditable()
	 */
	@Override
	public boolean isValueEditable() {
		return true;
	}

	/**
	 * @see de.ims.icarus.config.EntryHandler#isValueValid()
	 */
	@Override
	public boolean isValueValid() {
		return columns!=null;
	}

	/**
	 * @see de.ims.icarus.config.EntryHandler#newEntry()
	 */
	@Override
	public Object newEntry() {
		return null;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==toggleButton) {
			toggleColumn();
		} else if(e.getSource()==upButton) {
			moveColumn(1);
		} else if(e.getSource()==downButton) {
			moveColumn(-1);
		}
	}
	
	private void moveColumn(int delta) {
		ColumnListModel model = (ColumnListModel) columnList.getModel();
		int index0 = columnList.getSelectedIndex();
		
		if(index0==-1) {
			return;
		}
		
		int index1 = index0+delta;
		
		if(index1<0 || index1>=model.getSize()) {
			return;
		}
		
		model.swap(index0, index1);
	}
	
	private void toggleColumn() {
		ColumnListModel model = (ColumnListModel) columnList.getModel();
		int index = columnList.getSelectedIndex();
		
		if(index==-1) {
			return;
		}
		
		ColumnInfo info = model.getElementAt(index);
		info.setActive(!info.isActive());
		
		model.fireContentsChanged(index, index);
	}

	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
			toggleColumn();
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// no-op
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// no-op
	}

	/**
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// no-op
	}

	/**
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// no-op
	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		refreshButtons();
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private class ColumnListModel extends AbstractListModel<ColumnInfo> {
		
		private static final long serialVersionUID = 486760712929721053L;

		/**
		 * @see javax.swing.ListModel#getSize()
		 */
		@Override
		public int getSize() {
			return columns==null ? 0 : columns.size();
		}

		/**
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		@Override
		public ColumnInfo getElementAt(int index) {
			return columns==null ? null : columns.get(index);
		}
		
		public void swap(int index0, int index1) {
			if(columns==null) {
				return;
			}
			
			Collections.swap(columns, index0, index1);
			
			fireContentsChanged(index0, index1);
		}

		/**
		 * @see javax.swing.AbstractListModel#fireContentsChanged(java.lang.Object, int, int)
		 */
		public void fireContentsChanged(int index0, int index1) {
			super.fireContentsChanged(this, index0, index1);
		}
		
	}
}
