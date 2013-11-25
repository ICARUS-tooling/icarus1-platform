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
package de.ims.icarus.plugins.language_tools.treebank;

import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.SwingWorker;

import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.IcarusFrame;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.tasks.TaskConstants;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankJob extends SwingWorker<Treebank, Object> implements Identity {
	
	private final boolean load;
	private final Treebank treebank;

	public TreebankJob(Treebank treebank, boolean load) {
		if(treebank==null)
			throw new NullPointerException("Invalid treebank"); //$NON-NLS-1$
		
		this.treebank = treebank;
		this.load = load;
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Treebank doInBackground() throws Exception {
		if(load) {
			load();
		} else {
			free();
		}
		return treebank;
	}
	
	private void load() throws Exception {
		try {
			firePropertyChange(TaskConstants.INDETERMINATE_PROPERTY, null, true);
			
			treebank.load();
			LoggerFactory.log(this, Level.INFO, "Loaded treebank: "+treebank.getName()); //$NON-NLS-1$
		} finally {
			firePropertyChange(TaskConstants.INDETERMINATE_PROPERTY, null, false);
		}
	}
	
	@Override
	protected void done() {
		try {
			get();

			if(load && !treebank.isLoaded()) {
				UIUtil.beep();
			}
		} catch(CancellationException | InterruptedException e) {
			// ignore
		} catch(Exception ex) {
			String operation = load ? "load" : "free"; //$NON-NLS-1$ //$NON-NLS-2$
			LoggerFactory.log(this, Level.SEVERE, 
					"Unable to "+operation+" treebank: "+treebank.getName(), ex); //$NON-NLS-1$ //$NON-NLS-2$
			UIUtil.beep();
			
			IcarusFrame.defaultShowError(ex);;
		}
	}

	private void free() throws Exception {
		try {
			firePropertyChange(TaskConstants.INDETERMINATE_PROPERTY, null, true);
			
			treebank.free();
			LoggerFactory.log(this, Level.INFO, "Freed treebank: "+treebank.getName()); //$NON-NLS-1$
		} finally {
			firePropertyChange(TaskConstants.INDETERMINATE_PROPERTY, null, false);
		}
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return getName();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return load ? 
				ResourceManager.getInstance().get("plugins.languageTools.treebankLoadTask.title") //$NON-NLS-1$
				: ResourceManager.getInstance().get("plugins.languageTools.treebankFreeTask.title"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return load ? 
				ResourceManager.getInstance().get("plugins.languageTools.treebankLoadTask.description") //$NON-NLS-1$
				: ResourceManager.getInstance().get("plugins.languageTools.treebankFreeTask.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return load ? 
				IconRegistry.getGlobalRegistry().getIcon("refresh_remote.gif")  //$NON-NLS-1$
				 : IconRegistry.getGlobalRegistry().getIcon("release_rls.gif"); //$NON-NLS-1$
		
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}
}
