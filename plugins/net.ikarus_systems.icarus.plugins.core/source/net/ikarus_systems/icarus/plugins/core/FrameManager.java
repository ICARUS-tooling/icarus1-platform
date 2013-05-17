/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.util.BiDiMap;
import net.ikarus_systems.icarus.util.Options;

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
		
		String id = "IcarusFrame_"+frameCount.getAndIncrement(); //$NON-NLS-1$
		FrameHandle handle = new FrameHandle(id);
		
		if(frames==null) {
			frames = new BiDiMap<>();
		}
		frames.put(frame, handle);
		
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
		// TODO
	}
	
	// TODO add methods to open perspective and/or send messages to other frames
	
	private class FrameObserver extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent e) {
			if(!(e.getSource() instanceof IcarusFrame))
				throw new IllegalArgumentException("Not a valid IcarusFrame: "+e.getSource());
			
			// TODO if last window ask for exit
			// TODO check if closable and then call close
		}

		@Override
		public void windowClosed(WindowEvent e) {
			// TODO clear
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
