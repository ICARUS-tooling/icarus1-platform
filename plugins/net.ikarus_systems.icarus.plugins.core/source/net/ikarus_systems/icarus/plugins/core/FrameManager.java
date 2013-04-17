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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private BiDiMap<IcarusFrame, FrameHandle> frames;
	
	private AtomicInteger frameCount = new AtomicInteger();
	
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
	
	public void closeFrame(IcarusFrame frame) {
		try {
			frame.close();
			frame.setVisible(false);
			frame.dispose();
		} finally {
			frames.remove(frame);
		}
		
		if(frames.isEmpty()) {
			IcarusCorePlugin.exit();
		}
	}

	public FrameHandle newFrame() {
		return newFrame(null);
	}

	public FrameHandle newFrame(Options options) {
		IcarusFrame frame = new IcarusFrame(options);
		try {
			frame.init();
		} catch (Exception e) {
			logger.log(LoggerFactory.record(Level.SEVERE, 
					"Failed to init frame: "+frame, e)); //$NON-NLS-1$
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
	
	public IcarusFrame getFrame(FrameHandle handle) {
		return frames==null ? null : frames.getKey(handle);
	}
	
	// TODO add methods to open perspective and/or send messages to other frames
	
	
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
	}
}
