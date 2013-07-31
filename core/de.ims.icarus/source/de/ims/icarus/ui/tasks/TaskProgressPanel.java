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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TaskProgressPanel extends JPanel implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID = -7450710919190608148L;
	
	private JLabel label;
	private JProgressBar progressBar;
	private JButton cancelButton;
	
	private SwingWorker<?, ?> task;
	
	private boolean allowCancel = true;
	
	public TaskProgressPanel() {
		super(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		
		label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		add(label);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(false);
		UIUtil.resizeComponent(progressBar, 100, 14);
		add(progressBar);
		
		cancelButton = new JButton();
		cancelButton.setFocusable(false);
		cancelButton.setFocusPainted(false);
		cancelButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("nav_stop.gif")); //$NON-NLS-1$
		cancelButton.addActionListener(this);
		UIUtil.resizeComponent(cancelButton, 18, 18);
		add(cancelButton);
	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(task==null) {
			return;
		}
		
		switch (evt.getPropertyName()) {
		case TaskConstants.PROGRESS_PROPERTY:
			progressBar.setValue(task.getProgress());
			break;
			
		case TaskConstants.STATE_PROPERTY:
			if(task.isDone()) {
				task.removePropertyChangeListener(this);
				setVisible(false);
				setTitle(null);
				setInfo(null);
				setIndeterminate(false);
				setProgress(0);
				task = null;
			}
			break;
			
		case TaskConstants.TITLE_PROPERTY:
			label.setText((String)evt.getNewValue());
			break;
			
		case TaskConstants.INDETERMINATE_PROPERTY:
			progressBar.setIndeterminate((boolean)evt.getNewValue());
			break;
			
		case TaskConstants.INFO_PROPERTY:
			label.setToolTipText((String)evt.getNewValue());
			break;
		}
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		task.cancel(true);
	}

	public boolean isAllowCancel() {
		return allowCancel;
	}

	public void setAllowCancel(boolean allowCancel) {
		this.allowCancel = allowCancel;
	}

	public SwingWorker<?, ?> getTask() {
		return task;
	}
	
	public void setTitle(String title) {
		label.setText(title);
	}
	
	public void setInfo(String info) {
		label.setToolTipText(info);
	}

	public void setTask(SwingWorker<?, ?> task) {
		if(this.task==task) {
			return;
		}
		
		if(this.task!=null) {
			this.task.removePropertyChangeListener(this);
		}
		
		this.task = task;
		
		if(this.task!=null) {
			this.task.addPropertyChangeListener(this);
		}
		
		setVisible(this.task!=null);
		cancelButton.setVisible(allowCancel && this.task!=null);
		
		if(this.task!=null) {
			TaskManager taskManager = TaskManager.getInstance();
			setTitle(taskManager.getTitle(this.task));
			setInfo(taskManager.getInfo(this.task));
			setProgress(taskManager.getProgress(this.task));
			setIndeterminate(taskManager.isIndeterminate(this.task));
		}
	}
	
	public void setIndeterminate(boolean value) {
		progressBar.setIndeterminate(value);
		progressBar.repaint();
	}
	
	public void setProgress(int value) {
		progressBar.setValue(value);
		progressBar.repaint();
	}
}
