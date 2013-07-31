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
package de.ims.icarus.ui.tasks;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import de.ims.icarus.ui.GridBagUtil;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TaskListCellRenderer extends JPanel implements
		ListCellRenderer<Object>, EventListener, ActionListener,
		MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 3335847880598821420L;
	
	private JLabel taskIcon;
	private JLabel header;
	private JProgressBar progressBar;
	private JTextArea footer;
	private JButton cancelButton;
	
	private Object task;
	
	private final TaskManager manager;
	
	private final JList<?> list;
	
	private static Icon defaultIcon;

	public TaskListCellRenderer(TaskManager manager) {
		this(manager, null);
	}

	public TaskListCellRenderer(TaskManager manager, JList<?> list) {
		if(manager==null)
			throw new IllegalArgumentException("Invalid manager"); //$NON-NLS-1$
		
		this.manager = manager;
		this.list = list;
		
		manager.addListener(null, this);
		
		if(list!=null) {
			list.addMouseListener(this);
			list.addMouseMotionListener(this);
			
			list.setCellRenderer(this);
		}
	}
	
	private void buildPanel() {
		
		taskIcon = new JLabel();
		taskIcon.setBorder(new EmptyBorder(3, 3, 3, 5));
		
		progressBar = new JProgressBar();
		
		header = new JLabel();
		header.setBorder(new EmptyBorder(3, 5, 2, 5));
		
		footer = new JTextArea();
		footer.setWrapStyleWord(true);
		footer.setLineWrap(true);
		footer.setOpaque(false);
		footer.setEditable(false);
		footer.setFont(UIManager.getFont("Label.font")); //$NON-NLS-1$
		
		cancelButton = new JButton(IconRegistry.getGlobalRegistry().getIcon("nav_stop.gif")); //$NON-NLS-1$
		cancelButton.setFocusable(false);
		cancelButton.setContentAreaFilled(false);
		cancelButton.setBorder(new UIUtil.FlatButtonBorder());
		cancelButton.addActionListener(this);
		cancelButton.setSize(new Dimension(24, 24));
		
		setLayout(GridBagUtil.getLayout());
		
		GridBagConstraints gbc = GridBagUtil.makeGbc(0, 0);
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.CENTER;
		add(taskIcon, gbc);
		
		gbc = GridBagUtil.makeGbcH(1, 0, 1, 1);
		add(header, gbc);
		
		gbc = GridBagUtil.makeGbcH(1, 1, 1, 1);
		gbc.anchor = GridBagConstraints.CENTER;
		add(progressBar, gbc);
		
		gbc = GridBagUtil.makeGbc(2, 1);
		gbc.anchor = GridBagConstraints.CENTER;
		add(cancelButton, gbc);
		
		gbc = GridBagUtil.makeGbcH(1, 2, 1, 1);
		add(footer, gbc);
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		if(task!=null) {
			throw new IllegalStateException("Cannot use renderer - permanent task attached"); //$NON-NLS-1$
		}
		
		if(header==null) {
			buildPanel();
		}
		
		// Background
		Color c = list.getBackground();
		if(isSelected) {
			//c = list.getSelectionBackground();
		}
		setBackground(c);
		
		// Foreground
		c = list.getForeground();
		if(isSelected) {
			//c = list.getSelectionForeground();
		}
		setForeground(c);
		
		showTask(value);
		
		validate();
		
		return this;
	}
	
	private Icon getDefaultIcon() {
		if(defaultIcon==null) {
			defaultIcon = IconRegistry.getGlobalRegistry().getIcon("task_set.gif"); //$NON-NLS-1$
		}
		return defaultIcon;
	}

	public void showTask(Object task) {
		if(header==null) {
			buildPanel();
		}

		setVisible(task!=null);
		
		// Set "default" appearance
		if(task==null) {
			return;
		}
		
		Icon icon = manager.getIcon(task);
		if(icon==null) {
			icon = getDefaultIcon();
		}
		
		taskIcon.setIcon(icon);
		header.setText(manager.getTitle(task));
		footer.setText(manager.getInfo(task));
		
		if(manager.isActiveTask(task)) {
			progressBar.setIndeterminate(manager.isIndeterminate(task));
			progressBar.setValue(manager.getProgress(task));
			
			progressBar.setVisible(true);
			cancelButton.setVisible(true);
			footer.setVisible(true);
		} else {
			progressBar.setVisible(false);
			cancelButton.setVisible(false);
			footer.setVisible(false);
		}
	}
	
	public void setTask(Object task) {
		if(this.task==task) {
			return;
		}
		
		this.task = task;
		showTask(task);
	}

	/**
	 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
	 */
	@Override
	public void invoke(Object sender, EventObject event) {
		if(TaskConstants.ACTIVE_TASK_CHANGED.equals(event.getName())
				|| this.task==null) {
			return;
		}
		Object task = event.getProperty("task"); //$NON-NLS-1$
		if(this.task!=task) {
			return;
		}
		
		String property = (String)event.getProperty("property"); //$NON-NLS-1$
		
		// General change
		if(property==null) {
			showTask(task);
			return;
		}
		
		// "Named" change
		switch (property) {
		case "title": //$NON-NLS-1$
			header.setText(manager.getTitle(task));
			break;

		case "info": //$NON-NLS-1$
			footer.setText(manager.getInfo(task));
			break;

		case "icon": //$NON-NLS-1$
			taskIcon.setIcon(manager.getIcon(task));
			break;

		case "indetermminate": //$NON-NLS-1$
			progressBar.setIndeterminate(manager.isIndeterminate(task));
			break;

		case "progress": //$NON-NLS-1$
			progressBar.setValue(manager.getProgress(task));
			break;
			
		default:
			showTask(task);
			break;
		}
		
		progressBar.repaint();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			Object task = manager.getActiveTask();
			manager.cancelTask(task);
		} catch(Exception ex) {
			// TODO show error dialog
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// no-op
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		dispatchMouseEvent(e);
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		dispatchMouseEvent(e);
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
	
	private void dispatchMouseEvent(MouseEvent e) {
		if(header==null || list==null) {
			return;
		}
		
		Point p = e.getPoint();
		int index = list.locationToIndex(p);
		
		if(index==-1) {
			return;
		}
		
		// Only care about active task
		Object task = list.getModel().getElementAt(index);
		if(!manager.isActiveTask(task)) {
			return;
		}
		
		// Translate location to current row
		Rectangle cellBounds = list.getCellBounds(index, index);
		p.translate(-cellBounds.x, -cellBounds.y);
		
		// Translate location to cancel button
		Rectangle buttonBounds = cancelButton.getBounds();
		p.translate(-buttonBounds.x, -buttonBounds.y);
		cancelButton.getModel().setArmed(cancelButton.contains(p));
		
		if(!cancelButton.getModel().isArmed()) {
			return;
		}
		
		// Forward event
		MouseEvent newEvent = new MouseEvent(cancelButton, e.getID(), 
				e.getWhen(), e.getModifiers(), p.x, p.y, 
				e.getClickCount(), e.isPopupTrigger());
		cancelButton.dispatchEvent(newEvent);
		list.repaint(cellBounds);
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		// no-op
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		dispatchMouseEvent(e);
	}
}
