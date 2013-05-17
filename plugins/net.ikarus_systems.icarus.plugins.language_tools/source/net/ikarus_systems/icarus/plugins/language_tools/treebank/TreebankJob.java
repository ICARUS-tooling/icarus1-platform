/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools.treebank;

import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.SwingWorker;

import net.ikarus_systems.icarus.language.treebank.Treebank;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.tasks.TaskConstants;
import net.ikarus_systems.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankJob extends SwingWorker<Object, Object> implements Identity {
	
	private final boolean load;
	private final Treebank treebank;

	public TreebankJob(Treebank treebank, boolean load) {
		if(treebank==null)
			throw new IllegalArgumentException("Invalid treebank"); //$NON-NLS-1$
		
		this.treebank = treebank;
		this.load = load;
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Object doInBackground() throws Exception {
		if(load) {
			load();
		} else {
			free();
		}
		return null;
	}
	
	private void load() throws Exception {
		try {
			firePropertyChange(TaskConstants.INDETERMINATE_PROPERTY, null, true);
			
			treebank.load();
			LoggerFactory.log(this, Level.INFO, "Loaded treebank: "+treebank.getName()); //$NON-NLS-1$
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to load treebank: "+treebank.getName(), e); //$NON-NLS-1$
			
			throw e;
		} finally {
			firePropertyChange(TaskConstants.INDETERMINATE_PROPERTY, null, false);
		}
	}
	
	private void free() throws Exception {
		try {
			firePropertyChange(TaskConstants.INDETERMINATE_PROPERTY, null, true);
			
			treebank.free();
			LoggerFactory.log(this, Level.INFO, "Freed treebank: "+treebank.getName()); //$NON-NLS-1$
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to free treebank: "+treebank.getName(), e); //$NON-NLS-1$
			
			throw e;
		} finally {
			firePropertyChange(TaskConstants.INDETERMINATE_PROPERTY, null, false);
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return getName();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return load ? 
				ResourceManager.getInstance().get("plugins.languageTools.treebankLoadTask.title") //$NON-NLS-1$
				: ResourceManager.getInstance().get("plugins.languageTools.treebankFreeTask.title"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return load ? 
				ResourceManager.getInstance().get("plugins.languageTools.treebankLoadTask.description") //$NON-NLS-1$
				: ResourceManager.getInstance().get("plugins.languageTools.treebankFreeTask.description"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return load ? 
				IconRegistry.getGlobalRegistry().getIcon("refresh_remote.gif")  //$NON-NLS-1$
				 : IconRegistry.getGlobalRegistry().getIcon("release_rls.gif"); //$NON-NLS-1$
		
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}
}
