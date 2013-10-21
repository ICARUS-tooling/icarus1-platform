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
package de.ims.icarus.xml.jaxb;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.ims.icarus.Core;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.tasks.TaskManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class JAXBGate<B extends Object> {

	private final File file;
	private final Object fileLock = new Object();
	
	private B pendingBuffer;
	private final Object bufferLock = new Object();
	
	private final Object gateLock = new Object();
	
	private AtomicBoolean updatePending = new AtomicBoolean(false);
	
	public JAXBGate(File file) {
		if(file==null)
			throw new IllegalArgumentException("Invalid file");
		
		this.file = file;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}
	
	protected abstract void readBuffer(B buffer) throws Exception;
	
	protected abstract B createBuffer() throws Exception;
	
	@SuppressWarnings("unchecked")
	public void loadBuffer() throws Exception {
		
		B buffer = null;
		synchronized (bufferLock) {
			buffer = pendingBuffer;
		}
		
		// Try to load new buffer
		if(buffer==null) {
			synchronized (fileLock) {
				File file = getFile();
				if(!file.exists() || file.length()==0) {
					return;
				}
	
				JAXBContext context = JAXBUtils.getSharedJAXBContext();
				Unmarshaller unmarshaller = context.createUnmarshaller();
				buffer = (B) unmarshaller.unmarshal(file);
			}
		}
		
		if(buffer==null) {
			return;
		}
		
		synchronized (gateLock) {
			readBuffer(buffer);
		}
	}
	
	public void saveBuffer() throws Exception {
		B buffer = null;
		
		synchronized (gateLock) {
			buffer = createBuffer();
		}
		
		if(buffer==null) {
			return;
		}
		
		synchronized (bufferLock) {
			pendingBuffer = buffer;
			
			scheduleUpdate();
		}
	}
	
	public void saveBufferNow() throws Exception {
		B buffer = null;
		
		synchronized (gateLock) {
			buffer = createBuffer();
		}
		
		if(buffer==null) {
			return;
		}
		
		synchronized (bufferLock) {
			pendingBuffer = buffer;
			
			scheduleUpdate();
		}
	}
	
	private void scheduleUpdate() {
		synchronized (bufferLock) {
			if(pendingBuffer==null) {
				return;
			}
			
			if(updatePending.compareAndSet(false, true)) {
				TaskManager.getInstance().execute(new SaveTask());
			}
		}
	}
	
	private void save(B buffer) throws Exception {
		synchronized (fileLock) {
			File file = getFile();
			if(!file.exists()) {
				file.createNewFile();
			}
			
			JAXBContext context = JAXBUtils.getSharedJAXBContext();
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(buffer, file);
		}
	}
	
	private class SaveTask implements Runnable {

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			updatePending.set(false);
			
			B buffer = null;
			synchronized (bufferLock) {
				buffer = pendingBuffer;
				pendingBuffer = null;
			}
			
			if(buffer==null) {
				return;
			}
			
			try {
				save(buffer);
			} catch (Exception e) {
				LoggerFactory.error(this, "Failed to save buffer", e);
			}
			
			scheduleUpdate();
		}
		
	}
}
