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
package de.ims.icarus.config;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.Exceptions;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractConfigStorage implements ConfigStorage, ConfigConstants {
	
	protected AtomicInteger unsavedChangesCounter = new AtomicInteger();
	
	protected AtomicBoolean saveScheduled = new AtomicBoolean();
	
	protected int strategy = MANUAL_SAVING;
	protected int blockSize = 25;
	protected long interval = 5 * 60 * 1000; // 5 minutes
	
	protected ConfigRegistry registry;
	
	protected TimerTask saveJob = new TimerTask() {
		
		@Override
		public void run() {
			int changeCount = unsavedChangesCounter.get();
			if(changeCount>0) {
				try {
					write();
					unsavedChangesCounter.addAndGet(-changeCount);
				} catch (Exception e) {
					LoggerFactory.log(this, Level.SEVERE, "Failed to save config storage", e); //$NON-NLS-1$
				}
			}
			saveScheduled.set(false);
		}
	};
	
	protected TimerTask loadJob = new TimerTask() {
		
		@Override
		public void run() {
			try {
				read();
				unsavedChangesCounter.set(0);
				
				// notify registry of changes
				if(registry!=null) {
					registry.storageUpdated(AbstractConfigStorage.this);
				}
			} catch (Exception e) {
				LoggerFactory.log(this,	Level.SEVERE, "Failed to load config storage", e); //$NON-NLS-1$
			}
		}
	};
	
	protected abstract void read() throws Exception;

	protected abstract void write() throws Exception;

	@Override
	public void setRegistry(ConfigRegistry registry) {
		Exceptions.testNullArgument(registry, "registry"); //$NON-NLS-1$
		this.registry = registry;
	}

	@Override
	public boolean hasUnsavedChanges() {
		return unsavedChangesCounter.get()>0;
	}
	
	public void setStrategy(int strategy) {
		if(this.strategy!=strategy) {
			this.strategy = strategy;
			
			if((strategy&PERIODIC_SAVING)==PERIODIC_SAVING) 
				ConfigUtils.schedule(saveJob, interval, interval);
			else
				saveJob.cancel();	
			
			checkSaveRequirements();
		}
	}

	@Override
	public void commit() {
		if(unsavedChangesCounter.get()>0)
			ConfigUtils.execute(saveJob);
	}

	@Override
	public void update() {
		ConfigUtils.execute(loadJob);
	}

	@Override
	public void commitNow() {
		saveJob.run();
	}

	@Override
	public void updateNow() {
		loadJob.run();
	}
	
	private void checkSaveRequirements() {
		if((((strategy&IMMEDIATE_SAVING)==IMMEDIATE_SAVING 
				&& unsavedChangesCounter.get()>0)
				||((strategy&BLOCKWISE_SAVING)==BLOCKWISE_SAVING)
				&& unsavedChangesCounter.get()>=blockSize)
				&& saveScheduled.compareAndSet(false, true)) {
			
			ConfigUtils.execute(saveJob);
		}
	}

	@Override
	public void setValue(String path, Object value) {
		
		//System.out.printf("setting: path=%s value=%s\n", path, value);
		
		if(setValue0(path, value)) {
			unsavedChangesCounter.incrementAndGet();
			checkSaveRequirements();
		}
	}
	
	protected abstract boolean setValue0(String path, Object value);

	/**
	 * @return the blockSize
	 */
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * @param blockSize the blockSize to set
	 */
	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
		checkSaveRequirements();
	}

	/**
	 * @return the interval
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * @param interval the interval to set
	 */
	public void setInterval(long interval) {
		this.interval = interval;
		checkSaveRequirements();
	}

}
