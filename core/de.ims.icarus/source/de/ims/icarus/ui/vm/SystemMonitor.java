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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.vm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.util.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class SystemMonitor {

	private static volatile SystemMonitor instance;
	
	public static SystemMonitor getInstance() {
		SystemMonitor monitor = instance;
		
		if(monitor==null) {
			synchronized (SystemMonitor.class) {
				monitor = instance;
				if(monitor==null) {
					monitor = new SystemMonitor();
					
					monitor.init();
					
					instance = monitor;
				}
			}
		}
		
		return monitor;
	}
	
	public static final double DEFAULT_THRESHOLD = 0.8;

	private static final long KILO = 1024;
	private static final long MEGA = KILO * KILO;
	
	public static String formatMemory(long value) {
		if(value<=0) {
			return "<undefined>"; //$NON-NLS-1$
		}
		
		value /= MEGA;
		
		if(value < 10000) {
			return StringUtil.formatDecimal((int) value)+'M';
		} else {
			return String.format("%1.02dG", value / 1000d); //$NON-NLS-1$
		}
	}
	
	private Timer timer;
	
	private ActionListener timerTask;
	
	private EventListenerList listenerList = new EventListenerList();
	
	private AtomicBoolean gcActive = new AtomicBoolean(false);
	private volatile boolean mayWarnUser = false;
	private volatile boolean ignoreThreshold = false;
	
	// Cached state
	private MemoryUsage currentUsage;
	
	private SystemMonitor() {
		if(instance!=null)
			throw new IllegalStateException("Duplicate instance of system monitor"); //$NON-NLS-1$
	}
	
	private void init() {
		ConfigBuilder builder = new ConfigBuilder();
		
		builder.addGroup("general", true); //$NON-NLS-1$
		builder.addGroup("performance", true); //$NON-NLS-1$
		builder.addBooleanEntry("showMemoryMonitor", true); //$NON-NLS-1$s
		builder.addBooleanEntry("colorizeMemoryMonitor", true); //$NON-NLS-1$
		builder.addBooleanEntry("showMemoryWarning", true); //$NON-NLS-1$
		builder.addBooleanEntry("performDoubleCycle", true); //$NON-NLS-1$
		builder.addDoubleEntry("memoryThreshold", DEFAULT_THRESHOLD, 0.0, 0.95); //$NON-NLS-1$
	}
	
	private synchronized Timer getTimer() {
		if (timer==null) {
			timer = new Timer(1000, getTimerTask());
		}
		
		return timer;
	}
	
	private ActionListener getTimerTask() {
		if(timerTask==null) {
			timerTask = new ActionListener() {
				

				@Override
				public void actionPerformed(ActionEvent e) {
					runCheck();
				}
			};
		}
		
		return timerTask;
	}
	
	private synchronized void runCheck() {
		try {
			checkMemory();
		} catch(Exception ex) {
			LoggerFactory.error(this, 
					"Failed to check memory usage", ex); //$NON-NLS-1$
		}

		
		notifyListeners();
	}
	
	public boolean isGcRunning() {
		return gcActive.get();
	}
	
	public void runGc() {
		if(!gcActive.get()) {
			TaskManager.getInstance().execute(new GcTask());
			
			notifyListeners();
		}
	}
	
	private void checkMemory() {
		currentUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		
		// Don't bother with memory checking while last gc run is still active
		if(gcActive.get()) {
			return;
		}
		
		double used = currentUsage.getUsed();
		double max = currentUsage.getCommitted();
		
		double threshold = ConfigRegistry.getGlobalRegistry().getDouble(
				"general.performance.memoryThreshold"); //$NON-NLS-1$
		
		if(threshold<=0.1) {
			threshold = DEFAULT_THRESHOLD;
		}
		
		if(used/max >= threshold) {
			runGc();
			
			if(mayWarnUser && !ignoreThreshold && ConfigRegistry.getGlobalRegistry().getBoolean(
					"general.performance.showMemoryWarning")) { //$NON-NLS-1$
				
				ignoreThreshold = true;
				
				showMemoryWarning();
			}
		}
	}
	
	public static void showMemoryWarning() {
		

		DialogFactory.getGlobalFactory().showWarning(null, 
				"core.systemMonitor.dialogs.lowMemory.title",  //$NON-NLS-1$
				"core.systemMonitor.dialogs.lowMemory.message",  //$NON-NLS-1$
				formatMemory(getInstance().getUsed()), 
				formatMemory(getInstance().getCommitted()));
	}
	
	public long getThreshold() {

		double threshold = ConfigRegistry.getGlobalRegistry().getDouble(
				"general.performance.memoryThreshold"); //$NON-NLS-1$
		
		long max = getMax();
		
		if(threshold<0.1) {
			return -1;
		}
		
		return (long) (max * threshold);
	}
	
	public long getMin() {
		return currentUsage==null ? 0 : currentUsage.getInit();
	}
	
	public long getUsed() {
		return currentUsage==null ? 0 : currentUsage.getUsed();
	}
	
	public long getCommitted() {
		return currentUsage==null ? 0 : currentUsage.getCommitted();
	}
	
	public long getMax() {
		return currentUsage==null ? 0 : currentUsage.getMax();
	}
	
	private void notifyListeners() {
		Object[] pairs = listenerList.getListenerList();

		ChangeEvent event = null;

		for (int i = pairs.length - 2; i >= 0; i -= 2) {
			if (pairs[i] == ChangeListener.class) {
				if (event == null) {
					event = new ChangeEvent(this);
				}

				((ChangeListener) pairs[i + 1]).stateChanged(event);
			}
		}
	}
	
	public void addChangeListener(ChangeListener listener) {
		listenerList.remove(ChangeListener.class, listener);
		listenerList.add(ChangeListener.class, listener);
		
		if(listenerList.getListenerCount(ChangeListener.class)==1) {
			getTimer().start();
		}
	}
	
	public void removeChangeListener(ChangeListener listener) {
		listenerList.remove(ChangeListener.class, listener);
		
		if(listenerList.getListenerCount(ChangeListener.class)==0) {
			getTimer().stop();
		}
	}
	
	private class GcTask implements Runnable {

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			
			if(!gcActive.compareAndSet(false, true)) {
				return;
			}
			
			try {
				System.gc();
				
				if(ConfigRegistry.getGlobalRegistry().getBoolean(
						"general.performance.performDoubleCycle")) { //$NON-NLS-1$
					System.gc();
				}
			} finally {
				gcActive.set(false);
			}
			
			mayWarnUser = true;
		}
		
	}
}
