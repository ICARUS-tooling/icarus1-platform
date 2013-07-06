/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.BiDiMap;
import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class FrameManager {
	
	private static FrameManager instance;
	
	private BiDiMap<IcarusFrame, FrameHandle> frames;
	
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
	
	public synchronized void shutdown() {
		isShutdownActive = true;
		
		Queue<IcarusFrame> frames = new LinkedList<>(this.frames.keySet());
		IcarusFrame frame;
		
		while((frame = frames.poll())!=null) {
			try {
				closeFrame(frame);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to close frame: "+frame.getTitle(), e); //$NON-NLS-1$
			}
		}
	}
	
	// TODO add methods to open perspective and/or send messages to other frames
	
	private class Handler extends WindowAdapter implements ConfigListener {

		@Override
		public void windowClosing(WindowEvent e) {
			if(!(e.getSource() instanceof IcarusFrame))
				throw new IllegalArgumentException("Not a valid IcarusFrame: "+e.getSource()); //$NON-NLS-1$
			
			IcarusFrame frame = (IcarusFrame) e.getSource();
			try {
				if(frame.isClosable()) {
					closeFrame(frame);
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to handle close request for frame: "+frame.getTitle(), ex); //$NON-NLS-1$
			}
		}

		@Override
		public void windowClosed(WindowEvent e) {
			// TODO clear
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
				throw new IllegalArgumentException("Invalid id"); //$NON-NLS-1$
			
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
	}
}
