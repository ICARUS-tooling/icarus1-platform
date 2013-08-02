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

import java.awt.Dimension;

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
