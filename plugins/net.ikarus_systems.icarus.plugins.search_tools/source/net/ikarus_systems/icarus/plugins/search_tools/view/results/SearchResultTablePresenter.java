/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view.results;

import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.SwingWorker;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.table.TableSortMode;
import net.ikarus_systems.icarus.ui.tasks.TaskManager;
import net.ikarus_systems.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class SearchResultTablePresenter extends SearchResultPresenter {
	
	protected SortTableJob sortTableJob;

	protected static final String[] SORT_ACTIONS = { 
		"plugins.searchTools.searchResultPresenter.sortColsAscAlphaAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortColsDescAlphaAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortColsAscNumAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortColsDescNumAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortRowsAscAlphaAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortRowsDescAlphaAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortRowsAscNumAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortRowsDescNumAction", //$NON-NLS-1$
	};
	
	protected SearchResultTablePresenter() {
		// no-op
	}
	
	@Override
	protected CallbackHandler createCallbackHandler() {
		return new TableCallbackHandler();
	}

	@Override
	protected void registerActionCallbacks() {
		super.registerActionCallbacks();
		
		ActionManager actionManager = getActionManager();
		
		for(String id : SORT_ACTIONS) {
			actionManager.addHandler(id, callbackHandler, "sortTable"); //$NON-NLS-1$
		}
		
		actionManager.addHandler("plugins.searchTools.searchResultPresenter.flipTableAction",  //$NON-NLS-1$
				callbackHandler, "flipTable"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchResultPresenter.resetTableAction",  //$NON-NLS-1$
				callbackHandler, "resetTable"); //$NON-NLS-1$
	}

	@Override
	protected void refreshActions() {
		super.refreshActions();
		
		ActionManager actionManager = getActionManager();
		
		boolean isSorting = sortTableJob!=null;
		
		actionManager.setEnabled(!isSorting, SORT_ACTIONS);
		
		actionManager.setEnabled(!isSorting, 
				"plugins.searchTools.searchResultPresenter.flipTableAction", //$NON-NLS-1$
				"plugins.searchTools.searchResultPresenter.resetTableAction"); //$NON-NLS-1$
	}

	protected void sortTable(TableSortMode sortMode) {
		// for subclasses
	}
	
	protected void flipTable() {
		// for subclasses
	}
	
	protected void resetTable() {
		// for subclasses
	}

	@Override
	public void clear() {
		super.clear();
		if(sortTableJob!=null) {
			sortTableJob.cancel(true);
		}
	}

	@Override
	public void close() {
		super.close();
		if(sortTableJob!=null) {
			sortTableJob.cancel(true);
		}
	}

	public class TableCallbackHandler extends CallbackHandler {
		
		public void sortTable(ActionEvent e) {
			try {
				TableSortMode sortMode = TableSortMode.parseMode(e.getActionCommand());
				SearchResultTablePresenter.this.sortTable(sortMode);
				
				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to sort", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void flipTable(ActionEvent e) {
			try {
				SearchResultTablePresenter.this.flipTable();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to flip table", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void resetTable(ActionEvent e) {
			try {
				SearchResultTablePresenter.this.resetTable();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to reset table", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
	}
	
	protected abstract class SortTableJob extends SwingWorker<Object, Object> implements Identity {

		private final TableSortMode sortMode;
		
		public SortTableJob(TableSortMode sortMode) {
			if(sortMode==null)
				throw new IllegalArgumentException("Invalid sort mode"); //$NON-NLS-1$
			
			this.sortMode = sortMode;
		}
		
		private Object owner() {
			return SearchResultTablePresenter.this;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof SortTableJob) {
				return owner()==((SortTableJob)obj).owner();
			}
			return false;
		}
		
		protected TableSortMode getSortMode() {
			return sortMode;
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return getClass().getSimpleName();
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"plugins.searchTools.searchResultPresenter.sortTableJob.name"); //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return ResourceManager.getInstance().get(
					"plugins.searchTools.searchResultPresenter.sortTableJob.description", sortMode.getName()); //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}

		@Override
		protected void done() {
			if(isCancelled()) {
				return;
			}
			
			TaskManager.getInstance().setIndeterminate(this, false);
			sortTableJob = null;
			refreshActions();
		}
	}
}
