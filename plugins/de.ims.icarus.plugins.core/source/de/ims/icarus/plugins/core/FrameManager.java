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
package de.ims.icarus.plugins.core;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus.util.collections.BiDiMap;
import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class FrameManager {
	
	private static FrameManager instance;
	
	private BiDiMap<IcarusFrame, FrameHandle> frames;
	
	private Set<Window> windows;
	
	private AtomicInteger frameCount = new AtomicInteger();
	
	private boolean isShutdownActive = false;
	
	private Handler handler;
	
	public static FrameManager getInstance() {
		if(instance==null) {
			synchronized (FrameManager.class) {
				if(instance==null) {
					instance = new FrameManager();
				}
			}
		}
		
		return instance;
	}

	private FrameManager() {
		// no-op
	}
	
	private Handler getHandler() {
		if(handler==null) {
			handler = new Handler();
		}
		return handler;
	}
	
	public synchronized void closeFrame(IcarusFrame frame) {
		if(frames.size()==1) {
			boolean exitWoP = ConfigRegistry.getGlobalRegistry().getBoolean(
					"general.appearance.exitWithoutPrompt"); //$NON-NLS-1$
			if(!exitWoP) {
				MutableBoolean result = new MutableBoolean(exitWoP);
				if(!DialogFactory.getGlobalFactory().showCheckedConfirm(
						frame, result, 
						"plugins.core.icarusFrame.dialogs.confirmTitle",  //$NON-NLS-1$
						"plugins.core.icarusFrame.dialogs.confirmInfo",  //$NON-NLS-1$
						"plugins.core.icarusFrame.dialogs.confirmMessage")) { //$NON-NLS-1$
					return;
				}
				ConfigRegistry.getGlobalRegistry().setValue(
						"general.appearance.exitWithoutPrompt", result.getValue()); //$NON-NLS-1$
			}
		}
				
		try {
			frame.close();
			frame.setVisible(false);
			frame.dispose();
		} finally {
			frames.remove(frame);
		}
		
		if(frames.isEmpty() && !isShutdownActive) {
			ShutdownDialog.getDialog().shutdown();
		}
	}

	public synchronized FrameHandle newFrame() {
		return newFrame(null);
	}

	public synchronized FrameHandle newFrame(Options options) {
		IcarusFrame frame = new IcarusFrame(options);
		
		try {
			frame.init();
		} catch (Exception e) {
			LoggerFactory.log(FrameManager.class, Level.SEVERE, 
					"Failed to init frame: "+frame, e); //$NON-NLS-1$
			return null;
		}
		frame.addWindowListener(getHandler());
		
		String id = "IcarusFrame_"+frameCount.getAndIncrement(); //$NON-NLS-1$
		FrameHandle handle = new FrameHandle(id);
		
		if(frames==null) {
			frames = new BiDiMap<>();
		}
		frames.put(frame, handle);
		
		if(frames.size()==1) {
			ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
			config.addGroupListener(config.ROOT_HANDLE, getHandler());
		}
		
		frame.setVisible(true);
		return handle;
	}
	
	public synchronized IcarusFrame getFrame(FrameHandle handle) {
		return frames==null ? null : frames.getKey(handle);
	}
	
	public synchronized FrameHandle getHandle(IcarusFrame frame) {
		return frames==null ? null : frames.get(frame);
	}
	
	public synchronized void registerWindow(Window window) {
		if(window==null)
			throw new NullPointerException("Invalid window"); //$NON-NLS-1$
		
		if(windows==null) {
			windows = new HashSet<>();
		}
		
		if(windows.contains(window))
			throw new IllegalArgumentException("Window already registered: "+window.getName()); //$NON-NLS-1$
		
		window.addWindowListener(getHandler());
		windows.add(window);
	}
	
	public synchronized void unregisterWindow(Window window) {
		if(window==null)
			throw new NullPointerException("Invalid window"); //$NON-NLS-1$
		
		if(windows==null) {
			return;
		}
		
		if(!windows.contains(window))
			throw new IllegalArgumentException("Window not registered: "+window.getName()); //$NON-NLS-1$
		
		window.removeWindowListener(getHandler());
		windows.remove(window);
	}
	
	public synchronized void shutdown() {
		if(isShutdownActive) {
			return;
		}
		isShutdownActive = true;
		
		Queue<IcarusFrame> frames = new LinkedList<>(this.frames.keySet());
		IcarusFrame frame;
		
		while((frame = frames.poll())!=null) {
			try {
				frame.close();
				frame.setVisible(false);
				frame.dispose();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to close frame: "+frame.getTitle(), e); //$NON-NLS-1$
			}
		}
		
		if(windows!=null) {
			for(Window window : windows) {
				try {
					if(window instanceof Closeable) {
						((Closeable)window).close();
					}
					
					window.setVisible(false);
					window.dispose();
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to close external window: "+window.getName(), e); //$NON-NLS-1$
				}
			}
		}
	}
	
	// TODO add methods to open perspective and/or send messages to other frames
	
	private class Handler extends WindowAdapter implements ConfigListener {

		@Override
		public void windowClosing(WindowEvent e) {
			if(e.getWindow() instanceof IcarusFrame) {
				IcarusFrame frame = (IcarusFrame) e.getWindow();
				
				try {
					if(frame.isClosable()) {
						closeFrame(frame);
					}
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to handle close request for frame: "+frame.getTitle(), ex); //$NON-NLS-1$
				}
			} else {
				unregisterWindow(e.getWindow());
			}
		}

		/**
		 * @see de.ims.icarus.config.ConfigListener#invoke(de.ims.icarus.config.ConfigRegistry, de.ims.icarus.config.ConfigEvent)
		 */
		@Override
		public void invoke(ConfigRegistry sender, ConfigEvent event) {
			for(IcarusFrame frame : frames.keySet()) {
				frame.repaint();
			}
		}
		
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class FrameHandle {
		private final String id;
		
		private FrameHandle(String id) {
			if(id==null)
				throw new NullPointerException("Invalid id"); //$NON-NLS-1$
			
			this.id = id;
		}
		
		@Override
		public String toString() {
			return id;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof FrameHandle) {
				return ((FrameHandle)obj).id.equals(id);
			}
			return false;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}
}
