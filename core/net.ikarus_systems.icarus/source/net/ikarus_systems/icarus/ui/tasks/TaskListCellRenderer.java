/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.tasks;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import net.ikarus_systems.icarus.ui.GridBagUtil;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TaskListCellRenderer extends JPanel implements
		ListCellRenderer<Object>, EventListener, ActionListener {

	private static final long serialVersionUID = 3335847880598821420L;
	
	private JLabel taskIcon;
	private JLabel header;
	private JProgressBar progressBar;
	private JTextArea footer;
	private JButton cancelButton;
	
	private Object task;
	
	private final TaskManager manager;
	
	private static Icon defaultIcon;

	public TaskListCellRenderer(TaskManager manager) {
		if(manager==null)
			throw new IllegalArgumentException("Invalid manager"); //$NON-NLS-1$
		
		this.manager = manager;
		
		manager.addListener(null, this);
	}
	
	private void buildPanel() {
		
		taskIcon = new JLabel();
		taskIcon.setBorder(new EmptyBorder(3, 3, 3, 5));
		
		progressBar = new JProgressBar();
		
		header = new JLabel();
		header.setBorder(new EmptyBorder(3, 5, 2, 5));
		
		footer = new JTextArea(5, 40);
		footer.setWrapStyleWord(true);
		footer.setLineWrap(true);
		
		cancelButton = new JButton(IconRegistry.getGlobalRegistry().getIcon("nav_stop.gif")); //$NON-NLS-1$
		cancelButton.setFocusable(false);
		cancelButton.setBorderPainted(false);
		cancelButton.addActionListener(this);
		
		setLayout(GridBagUtil.getLayout());
		
		GridBagConstraints gbc = GridBagUtil.makeGbc(0, 0);
		add(taskIcon, gbc);
		
		gbc = GridBagUtil.makeGbcH(1, 0, 1, 1);
		add(progressBar, gbc);
		
		gbc = GridBagUtil.makeGbcH(1, 1, 1, 1);
		add(header, gbc);
		
		gbc = GridBagUtil.makeGbc(2, 0);
		add(cancelButton, gbc);
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
			c = list.getSelectionBackground();
		}
		setBackground(c);
		
		// Foreground
		c = list.getForeground();
		if(isSelected) {
			c = list.getSelectionForeground();
		}
		setForeground(c);
		
		showTask(value);
		
		return this;
	}
	
	private Icon getDefaultIcon() {
		if(defaultIcon==null) {
			defaultIcon = IconRegistry.getGlobalRegistry().getIcon("");
		}
		return defaultIcon;
	}

	public void showTask(Object task) {
		if(header==null) {
			buildPanel();
		}
		
		// Set "default" appearance
		if(task==null) {
			taskIcon.setIcon(null);
			header.setText(null);
			footer.setText(null);
			cancelButton.setVisible(false);
			progressBar.setVisible(false);
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
		} else {
			progressBar.setVisible(false);
			cancelButton.setVisible(false);
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
	 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
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
		
		// TODO refresh components based on property
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
			// TODO
		}
	}
}
