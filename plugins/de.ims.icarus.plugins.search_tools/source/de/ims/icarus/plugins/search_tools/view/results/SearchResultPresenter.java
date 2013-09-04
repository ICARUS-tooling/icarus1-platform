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
package de.ims.icarus.plugins.search_tools.view.results;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.ui.NumberDisplayMode;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class SearchResultPresenter implements AWTPresenter {
	
	public static final int DEFAULT_CELL_HEIGHT = 25;
	public static final int DEFAULT_CELL_WIDTH = 75;
	
	protected JPanel contentPanel;
	protected SearchResult searchResult;
	
	protected Handler handler;
	protected CallbackHandler callbackHandler;
	
	protected SwingWorker<?, ?> currentTask;
	
	protected Options options;
	
	public static final int DEFAULT_REFRESH_DELAY = 1000;
	
	protected ActionManager actionManager;
	
	private static ActionManager sharedActionManager;
	
	protected synchronized static ActionManager getSharedActionManager() {
		if(sharedActionManager==null) {
			sharedActionManager = ActionManager.globalManager().derive();

			URL actionLocation = SearchResultPresenter.class.getResource("search-result-presenter-actions.xml"); //$NON-NLS-1$
			if(actionLocation==null)
				throw new CorruptedStateException("Missing resources: search-result-presenter-actions.xml"); //$NON-NLS-1$
			
			try {
				sharedActionManager.loadActions(actionLocation);
			} catch (IOException e) {
				LoggerFactory.log(SearchResultPresenter.class, Level.SEVERE, 
						"Failed to load actions from file", e); //$NON-NLS-1$
			}
		}
		return sharedActionManager;
	}

	protected SearchResultPresenter() {
		// no-op
	}
	
	protected ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = getSharedActionManager().derive();
			
			registerActionCallbacks();
		}
		
		return actionManager;
	}
	
	protected void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = createCallbackHandler();
		}
		
		ActionManager actionManager = getActionManager();
		
		actionManager.addHandler("plugins.searchTools.searchResultPresenter.toggleNumberDisplayModeAction",  //$NON-NLS-1$
				callbackHandler, "toggleNumberDisplayMode"); //$NON-NLS-1$
	}
	
	protected void refreshActions() {
		// no-op
	}
	
	protected void setCurrentTask(SwingWorker<? extends Object, ? extends Object> task) {
		currentTask = task;
	}
	
	protected boolean hasCurrentTask() {
		return currentTask!=null;
	}
	
	protected SwingWorker<?, ?> getCurrentTask() {
		return currentTask;
	}
	
	public void exportToolBarItems(JToolBar toolBar) {
		// no-op
	}
	
	public abstract int getSupportedDimensions();

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible("SearchResultContentType", type); //$NON-NLS-1$
	}
	
	public boolean supportsEntryType(ContentType type) {
		return true;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		
		if(!PresenterUtils.presenterSupports(this, data))
			throw new UnsupportedPresentationDataException("Unsupported data: "+data.getClass()); //$NON-NLS-1$
		
		SearchResult searchResult = (SearchResult)data;
		int supportedDimension = getSupportedDimensions();
		if(supportedDimension!=-1 && searchResult.getDimension()!=supportedDimension)
			throw new UnsupportedPresentationDataException("Result dimension not supported: "+searchResult.getDimension()); //$NON-NLS-1$
		
		setSearchResult(searchResult, options);
	}
	
	protected Handler getHandler() {
		if(handler==null) {
			handler = createHandler();
		}
		
		return handler;
	}
	
	protected Handler createHandler() {
		return new Handler();
	}
	
	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler();
	}
	
	protected void setSearchResult(SearchResult searchResult, Options options) {
		if(this.searchResult==searchResult) {
			return;
		}
		
		this.searchResult = searchResult;
		
		setOptions(options);
		displayResult();
		
		updateGroupPainters();
	}
	
	protected Options getOptions() {
		return options==null ? Options.emptyOptions : options;
	}

	protected void setOptions(Options options) {
		if(options!=null) {
			options = options.clone();
		}
		this.options = options;
	}

	protected abstract void displayResult();
	
	public abstract void refresh();
	
	public SearchResult getSearchResult() {
		return searchResult;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		setSearchResult(null, null);
		if(hasCurrentTask()) {
			getCurrentTask().cancel(true);
		}
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		if(hasCurrentTask()) {
			getCurrentTask().cancel(true);
		}
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return searchResult!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return getSearchResult();
	}
	
	protected abstract void buildContentPanel();
	
	protected void updateGroupPainters() {
		// for subclasses
	}

	/**
	 * @see de.ims.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPanel==null) {			
			buildContentPanel();
			
			refresh();
		}
		return contentPanel;
	}
	
	protected void setNumberDisplayMode(NumberDisplayMode mode) {
		// no-op
	}
	
	public void openPreferences() {
		UIUtil.openConfigDialog("plugins.searchTools"); //$NON-NLS-1$
	}
	
	public static String getHitCountString(SearchResult result) {
		if(result==null) {
			return "-"; //$NON-NLS-1$
		}
		ResourceManager rm = ResourceManager.getInstance();
		int total = result.getTotalMatchCount();
		int groups = result.getDimension();
		
		String format = "%d %s - %s %s"; //$NON-NLS-1$
		
		String matchString = total==1 ?
				rm.get("plugins.searchTools.labels.matchSg") //$NON-NLS-1$
				: rm.get("plugins.searchTools.labels.matchPl"); //$NON-NLS-1$
				
		String groupString = groups==1 ?
				rm.get("plugins.searchTools.labels.groupSg") //$NON-NLS-1$
				: rm.get("plugins.searchTools.labels.groupPl"); //$NON-NLS-1$
				
		return String.format(format, groups, groupString, StringUtil.formatDecimal(total), matchString);
	}

	protected class Handler extends MouseAdapter  {
	}
	
	public class CallbackHandler {
		
		protected CallbackHandler() {
			// no-op
		}
		
		public void toggleNumberDisplayMode(boolean b) {
			try {
				NumberDisplayMode mode = b ? NumberDisplayMode.PERCENTAGE : NumberDisplayMode.RAW;
				setNumberDisplayMode(mode);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle number display mode", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void toggleNumberDisplayMode(ActionEvent e) {
			// ignore
		}
	}
	
	protected abstract class AbstractResultJob extends SwingWorker<Object, Object> implements Identity {
		
		private final String key;
		
		protected AbstractResultJob(String key) {
			if(key==null)
				throw new IllegalArgumentException("Invalid key"); //$NON-NLS-1$
			
			this.key = key;
		}

		@Override
		public String getId() {
			return getClass().getSimpleName();
		}

		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"plugins.searchTools.searchResultPresenter."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		protected Object[] getDescriptionParams() {
			return null;
		}

		@Override
		public String getDescription() {
			return ResourceManager.getInstance().get(
					"plugins.searchTools.searchResultPresenter."+key+".description", getDescriptionParams()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		@Override
		public Icon getIcon() {
			return null;
		}

		@Override
		public Object getOwner() {
			return this;
		}
		
		protected final Object owner() {
			return SearchResultPresenter.this;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof AbstractResultJob) {
				return ((AbstractResultJob)obj).owner()==owner();
			}
			return false;
		}

		@Override
		public String toString() {
			return getName();
		}
	}
}
