/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.tasks;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TaskTest {

	public static void main(String[] args) throws Exception {
		
		final TaskManager manager = new TaskManager();
		final TaskListModel listModel = new TaskListModel(manager);
		listModel.setIncludeActiveTask(true);
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				final JList<Object> taskList = new JList<>(listModel);
				taskList.setPreferredSize(new Dimension(500, 400));
				
				new TaskListCellRenderer(manager, taskList);
				
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(taskList);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
		
		for(int i=0; i<5; i++) {
			final int index = i;
			
			manager.schedule(new SwingWorker<Object, Object>() {				
	
				@Override
				protected Object doInBackground() throws Exception {
					
					for(int i=0; i<100; i++) {
						//System.out.printf("Test%d: progress=%d\n", index, i);
						setProgress(i);
						firePropertyChange("info", null, String.format("Processed %d%%\nblub", i));
						
						try {
							Thread.sleep(10);
						} catch(InterruptedException e) {
							// no-op
						}
						
						if(Thread.interrupted())
							break;
					}
					
					return null;
				}
			}, "Test"+index, null, null, TaskPriority.DEFAULT, false);
		}
	}
	
	
}
