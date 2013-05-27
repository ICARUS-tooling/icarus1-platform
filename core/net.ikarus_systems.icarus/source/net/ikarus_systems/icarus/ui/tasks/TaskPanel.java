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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.resources.ResourceDomain;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TaskPanel extends JToolBar {

	private static final long serialVersionUID = -8876327710810382573L;

	private JButton openDialogButton;
	
	private final TaskManager taskManager;
	private TaskListModel taskListModel;
	private TaskListCellRenderer activeTaskRenderer;
	private JList<?> taskList;
	private JPanel contentPanel;
	private JScrollPane scrollPane;
	
	private JLabel infoLabel;
	
	private Handler handler;

	public TaskPanel(TaskManager taskManager) {
		if(taskManager==null)
			throw new IllegalArgumentException("Invalid task-manager"); //$NON-NLS-1$
		
		this.taskManager = taskManager;
		
		taskManager.addListener(Events.ADDED, getHandler());
		taskManager.addListener(Events.REMOVED, getHandler());
		taskManager.addListener(TaskConstants.ACTIVE_TASK_CHANGED, getHandler());
		
		openDialogButton = new JButton();
		openDialogButton.setRolloverEnabled(true);
		openDialogButton.setFocusable(false);
		openDialogButton.setFocusPainted(false);
		openDialogButton.addActionListener(getHandler());
		
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		resourceDomain.prepareComponent(openDialogButton, 
				"taskManager.openTaskPanel.name",  //$NON-NLS-1$
				"taskManager.openTaskPanel.description"); //$NON-NLS-1$
		resourceDomain.addComponent(openDialogButton);
		
		openDialogButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("pview.gif")); //$NON-NLS-1$
		
		add(openDialogButton);
		
		setRollover(true);
		setFloatable(false);
		setBorder(null);
		
		refreshButtonEnabled();
	}
	
	public TaskManager getTaskManager() {
		return taskManager;
	}
	
	public void showDialog() {
		Component parent = null;
		if(openDialogButton!=null) {
			parent = SwingUtilities.getRoot(openDialogButton);
		}
		
		if(contentPanel==null) {
			activeTaskRenderer = new TaskListCellRenderer(getTaskManager());
			
			taskListModel = new TaskListModel(getTaskManager());
			taskList = new JList<Object>(taskListModel){

				private static final long serialVersionUID = 7924681566914684482L;

				@Override
				public boolean getScrollableTracksViewportWidth() {
					return true;
				}
			};
			taskList.setBorder(null);
			taskList.setFocusable(false);
			new TaskListCellRenderer(getTaskManager(), taskList);
			
			scrollPane = new JScrollPane(taskList);
			scrollPane.setPreferredSize(new Dimension(400, 270));
			scrollPane.setBorder(null);
			
			infoLabel = new JLabel();
			infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
			infoLabel.setVerticalAlignment(SwingConstants.TOP);
			ResourceManager.getInstance().getGlobalDomain().prepareComponent(infoLabel, 
					"taskManager.labels.noTasks", null); //$NON-NLS-1$
			ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);
			
			contentPanel = new JPanel(new BorderLayout());
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			contentPanel.add(activeTaskRenderer, BorderLayout.NORTH);
			contentPanel.add(infoLabel, BorderLayout.SOUTH);
		}
		
		activeTaskRenderer.setTask(getTaskManager().getActiveTask());
		scrollPane.setVisible(!getTaskManager().getQueue().isEmpty());
		
		infoLabel.setVisible(!activeTaskRenderer.isVisible() 
				&& !scrollPane.isVisible());
		
		DialogFactory.getGlobalFactory().showGenericDialog(parent, 
				"taskManager.dialogs.title",  //$NON-NLS-1$
				null, contentPanel, false, "close"); //$NON-NLS-1$
	}
	
	private Handler getHandler() {
		if(handler==null) {
			handler = new Handler();
		}
		return handler;
	}
	
	private void refreshButtonEnabled() {
		openDialogButton.setEnabled(!getTaskManager().isEmpty());
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private class Handler implements ActionListener, EventListener, Runnable {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			UIUtil.invokeLater(this);
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			refreshButtonEnabled();
			
			if(TaskConstants.ACTIVE_TASK_CHANGED.equals(event.getName())) {
				if(contentPanel==null) {
					return;
				}
				
				activeTaskRenderer.setTask(getTaskManager().getActiveTask());
				scrollPane.setVisible(!getTaskManager().getQueue().isEmpty());
				
				infoLabel.setVisible(!activeTaskRenderer.isVisible() 
						&& !scrollPane.isVisible());
			}
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				showDialog();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to show task list dialog", e); //$NON-NLS-1$
			}
		}		
	}
}
