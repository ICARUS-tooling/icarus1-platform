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

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ListCellRenderer;

import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TaskListCellRenderer extends JPanel implements
		ListCellRenderer<Object>, EventListener {

	private static final long serialVersionUID = 3335847880598821420L;
	
	private JLabel header;
	private JProgressBar progressBar;
	private JLabel footer;
	private JButton cancelButton;
	
	private Object task;
	
	private final TaskManager manager;

	public TaskListCellRenderer(TaskManager manager) {
		if(manager==null)
			throw new IllegalArgumentException("Invalid manager"); //$NON-NLS-1$
		
		this.manager = manager;
		
		manager.addListener(null, this);
	}
	
	private void buildPanel() {
		GroupLayout layout = new GroupLayout(this);
		
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

	public void showTask(Object task) {
		if(task==null) {
			// TODO set all components to an "empty" state
			return;
		}
	}
	
	public void setTask(Object task) {
		if(this.task==task) {
			return;
		}
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
		Object task = event.getProperty("task");
		if(this.task!=task) {
			return;
		}
		
		// TODO refresh components based on property
	}
}
