/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Timer;

import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchDescriptor;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchHistory extends AbstractListModel<SearchDescriptor> {
	
	private static final long serialVersionUID = -9128957062735086417L;
	
	private List<SearchDescriptor> descriptors;
	
	private Handler handler;
	
	private Timer timer;
	
	private static SearchHistory sharedInstance;
	
	public static SearchHistory getSharedInstance() {
		if(sharedInstance==null) {
			synchronized (SearchHistory.class) {
				if(sharedInstance==null) {
					sharedInstance = new SearchHistory();
				}
			}
		}
		
		return sharedInstance;
	}

	public SearchHistory() {
		// no-op
	}
	
	private Handler getHandler() {
		if(handler==null) {
			handler = new Handler();
		}
		return handler;
	}
	
	private Timer getTimer() {
		if(timer==null) {
			timer = new Timer(500, getHandler());
		}
		return timer;
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return descriptors==null ? 0 : descriptors.size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public SearchDescriptor getElementAt(int index) {
		return descriptors==null ? null : descriptors.get(index);
	}

	public void addSearch(SearchDescriptor descriptor) {
		if(descriptor==null)
			throw new IllegalArgumentException("Invalid descriptor"); //$NON-NLS-1$
		if(descriptor.getSearch()==null)
			throw new IllegalArgumentException("Missing search object on descriptor: "+descriptor); //$NON-NLS-1$
		
		if(descriptors==null) {
			descriptors = new ArrayList<>();
		}
		
		int index = descriptors.size();
		
		descriptors.add(descriptor);
		
		descriptor.getSearch().addPropertyChangeListener("state", getHandler()); //$NON-NLS-1$
		
		fireIntervalAdded(this, index, index);
		
		Timer timer = getTimer();
		if(!timer.isRunning()) {
			timer.start();
		}
	}
	
	public void removeSearch(SearchDescriptor descriptor) {
		if(descriptor==null)
			throw new IllegalArgumentException("Invalid descriptor"); //$NON-NLS-1$
		
		if(descriptors==null) {
			return;
		}
		
		int index = descriptors.indexOf(descriptor);
		if(index==-1) {
			return;
		}
		
		descriptors.remove(index);
		
		descriptor.getSearch().removePropertyChangeListener(getHandler());
		
		fireIntervalRemoved(this, index, index);
	}
	
	public void clear() {
		if(descriptors==null || descriptors.isEmpty()) {
			return;
		}
		
		int index = descriptors.size()-1;
		
		for(SearchDescriptor descriptor : descriptors) {
			descriptor.getSearch().removePropertyChangeListener(getHandler());
		}
		
		descriptors.clear();
		
		fireIntervalRemoved(this, 0, index);
	}
	
	private int indexofSearch(Search search) {
		if(descriptors==null || descriptors.isEmpty()) {
			return -1;
		}
		
		for(int i=0; i<descriptors.size(); i++) {
			if(descriptors.get(i).getSearch()==search) {
				return i;
			}
		}
		
		return -1;
	}
	
	private class Handler implements PropertyChangeListener, ActionListener {

		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			
			Search search = (Search) evt.getSource();
			int index = indexofSearch(search);
			if(index==-1) {
				search.removePropertyChangeListener(this);
			} else {
				fireContentsChanged(SearchHistory.this, index, index);
			}
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(descriptors==null) {
				return;
			}
			
			boolean needsRefresh = false;
			int index0 = Integer.MAX_VALUE;
			int index1 = 0;
			
			for(int i=0; i<descriptors.size(); i++) {
				if(descriptors.get(i).getSearch().isRunning()) {
					needsRefresh = true;
					index0 = Math.min(index0, i);
					index1 = Math.max(index1, i);
				}
			}
			
			if(needsRefresh) {
				fireContentsChanged(SearchHistory.this, index0, index1);
			} else {
				Timer timer = getTimer();
				if(timer.isRunning()) {
					timer.stop();
				}
				if(getSize()>0) {
					fireContentsChanged(SearchHistory.this, 0, getSize()-1);
				}
			}
		}
	}
}
