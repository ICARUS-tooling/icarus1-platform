/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.coref.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.BitSet;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.language.coref.CoreferenceDocumentData;
import net.ikarus_systems.icarus.language.coref.CoreferenceDocumentSet;
import net.ikarus_systems.icarus.language.coref.CoreferenceUtils;
import net.ikarus_systems.icarus.language.coref.text.CoreferenceDocument;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.list.FilterList;
import net.ikarus_systems.icarus.ui.list.FilterListCellRenderer;
import net.ikarus_systems.icarus.ui.list.FilterListModel;
import net.ikarus_systems.icarus.ui.tasks.TaskManager;
import net.ikarus_systems.icarus.ui.tasks.TaskPriority;
import net.ikarus_systems.icarus.util.Filter;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentSetPresenter extends
		AbstractCoreferenceTextPresenter {

	protected CoreferenceDocumentSet data;
	protected FilterList filterList;
	protected FilterListModel filterModel = new FilterListModel(0);
	
	private boolean showDocumentFilter = true;
	
	protected JSplitPane splitPane;
	
	protected JPopupMenu documentFilterPopupMenu;
		
	public CoreferenceDocumentSetPresenter() {
		toolBarListId = "plugins.coref.coreferenceDocumentPresenter.extendedToolBarList"; //$NON-NLS-1$
		
		visualizationLimit = ConfigRegistry.getGlobalRegistry().getInteger(
				"plugins.coref.appearance.visualizationLimit"); //$NON-NLS-1$
	}

	@Override
	protected void registerActionCallbacks() {
		super.registerActionCallbacks();
		
		ActionManager actionManager = getActionManager();
		
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.toggleDocumentFilterAction",  //$NON-NLS-1$
				callbackHandler, "toggleDocumentFilter"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.filterEmptyDocumentsAction",  //$NON-NLS-1$
				callbackHandler, "filterEmptyDocuments"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.selectAllDocumentsAction",  //$NON-NLS-1$
				callbackHandler, "selectAllDocuments"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.unselectAllDocumentsAction",  //$NON-NLS-1$
				callbackHandler, "unselectAllDocuments"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.invertDocumentSelectionAction",  //$NON-NLS-1$
				callbackHandler, "invertDocumentSelection"); //$NON-NLS-1$
	}

	@Override
	protected void refreshActions() {
		super.refreshActions();
		
		ActionManager actionManager = getActionManager();
		
		actionManager.setSelected(showDocumentFilter, 
				"plugins.coref.coreferenceDocumentPresenter.toggleDocumentFilterAction"); //$NON-NLS-1$
		
		int modelSize = filterModel.getSize();
		int filterSize = filterModel.filteredElementsCount();
		boolean hasFilter = modelSize>0;
		actionManager.setEnabled(hasFilter && modelSize>filterSize, 
				"plugins.coref.coreferenceDocumentPresenter.selectAllDocumentsAction"); //$NON-NLS-1$
		actionManager.setEnabled(hasFilter && filterSize<=modelSize, 
				"plugins.coref.coreferenceDocumentPresenter.unselectAllDocumentsAction"); //$NON-NLS-1$
		actionManager.setEnabled(hasFilter, 
				"plugins.coref.coreferenceDocumentPresenter.invertDocumentSelectionAction"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return data!=null;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public CoreferenceDocumentSet getPresentedData() {
		return data;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.coref.view.AbstractCoreferenceTextPresenter#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentSetContentType();
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.coref.view.AbstractCoreferenceTextPresenter#setData(java.lang.Object)
	 */
	@Override
	protected void setData(Object data) {
		this.data = (CoreferenceDocumentSet) data;
		
		filterModel.setSize(this.data==null ? 0 : this.data.size());
		
		filterModel.fill(visualizationLimit);
	}

	@Override
	protected Handler createHandler() {
		return new CDSHandler();
	}

	@Override
	protected CallbackHandler createCallbackHandler() {
		return new CDSCallbackHandler();
	}

	@Override
	protected JComponent createContentPanel() {
		JPanel panel = new JPanel(new BorderLayout());
				
		textPane = createTextPane();
		
		JScrollPane spRight = new JScrollPane(textPane);
		spRight.getViewport().addChangeListener(getHandler());
		UIUtil.defaultSetUnitIncrement(spRight);
		spRight.setBorder(null);

		filterList = new FilterList(filterModel);
		filterList.setCellRenderer(new DocumentFilterListCellRenderer());
		filterList.addMouseListener(getHandler());

		JScrollPane spLeft = new JScrollPane(filterList);
		UIUtil.defaultSetUnitIncrement(spLeft);
		spLeft.setBorder(null);
		spLeft.setVisible(showDocumentFilter);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, spLeft, spRight);
		splitPane.setResizeWeight(0);
		splitPane.setBorder(UIUtil.topLineBorder);
		splitPane.setDividerLocation(200);
		splitPane.setDividerSize(5);
		panel.add(splitPane, BorderLayout.CENTER);
		
		JToolBar toolBar = createToolBar();
		if(toolBar!=null) {
			panel.add(toolBar, BorderLayout.NORTH);
		}
		
		return panel;
	}

	@Override
	protected boolean buildDocument(CoreferenceDocument doc) throws Exception {	
		if(data==null) {
			return false;
		}
		
		int size = data.size();
		for(int i=0; i<size; i++) {
			if(Thread.currentThread().isInterrupted()) {
				return false;
			}
			if(filterList.getModel().getElementAt(i)) {
				doc.appendBatchCoreferenceDocumentData(data.get(i));
			}
		}
		
		doc.applyBatchUpdates(0);
		
		return true;
	}
	
	protected void showDocumentFilterPopup(MouseEvent trigger) {
		if(contentPanel==null) {
			return;
		}
		
		if(documentFilterPopupMenu==null) {
			// Create new popup menu
			
			Options options = new Options();
			documentFilterPopupMenu = getActionManager().createPopupMenu(
					"plugins.coref.coreferenceDocumentPresenter.documentFilterPopupMenuList", options); //$NON-NLS-1$
			
			if(documentFilterPopupMenu!=null) {
				documentFilterPopupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create document-filter popup menu"); //$NON-NLS-1$
			}
		}
		
		if(documentFilterPopupMenu!=null) {
			refreshActions();
			
			documentFilterPopupMenu.show(filterList, trigger.getX(), trigger.getY());
		}
	}
	
	public boolean isShowDocumentFilter() {
		return showDocumentFilter;
	}

	public void filterEmptyDocuments() {
		if(textPane==null || data==null) {
			return;
		}
		if(pendingFilter==null) {
			return;
		}
		
		RebuildFilterJob job = new RebuildFilterJob(pendingFilter);
		TaskManager.getInstance().schedule(job, TaskPriority.DEFAULT, true);
	}

	public void setShowDocumentFilter(boolean showDocumentFilter) {
		if(this.showDocumentFilter==showDocumentFilter) {
			return;
		}
		
		this.showDocumentFilter = showDocumentFilter;
		
		if(filterList==null) {
			return;
		}

		JScrollPane container = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, textPane);
		if(showDocumentFilter) {
			container.setBorder(null);
			splitPane.setRightComponent(container);
			splitPane.setDividerLocation(200);
			contentPanel.add(splitPane, BorderLayout.CENTER);
		} else {
			container.setBorder(UIUtil.topLineBorder);
			contentPanel.remove(splitPane);
			contentPanel.add(splitPane.getRightComponent(), BorderLayout.CENTER);
		}
		
		contentPanel.revalidate();
		contentPanel.repaint();
	}

	protected class DocumentFilterListCellRenderer extends FilterListCellRenderer {

		private static final long serialVersionUID = -7249622078036629896L;

		@Override
		protected String getTextForValue(int index, Boolean value) {
			if(data==null) {
				return null;
			}
			
			CoreferenceDocumentData docData = data.get(index);
			if(docData==null) {
				return null;
			}
			
			String header = (String) docData.getProperty(CoreferenceDocumentData.DOCUMENT_HEADER_PROPERTY);
			if(header==null) {
				header = "document "+index; //$NON-NLS-1$
			}
			return header;
		}
	}
	
	protected class CDSHandler extends Handler {

		@Override
		protected void maybeShowPopup(MouseEvent e) {
			if(e.isPopupTrigger() && e.getSource()==filterList) {
				showDocumentFilterPopup(e);
			} else {
				super.maybeShowPopup(e);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if(SwingUtilities.isLeftMouseButton(e) && e.getSource()==filterList) {
				int index = filterList.locationToIndex(e.getPoint());
				if(index==-1) {
					return;
				}
				
				filterModel.flipElementAt(index);
			}
		}
	}
	
	public class CDSCallbackHandler extends CallbackHandler {
		protected CDSCallbackHandler() {
			// no-op
		}

		public void toggleDocumentFilter(ActionEvent e) {
			// ignore
		}

		public void toggleDocumentFilter(boolean b) {
			try {
				setShowDocumentFilter(b);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'filterDocuments' flag", e); //$NON-NLS-1$
			}
		}

		public void filterEmptyDocuments(ActionEvent e) {
			try {
				CoreferenceDocumentSetPresenter.this.filterEmptyDocuments();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'filterEmptyDocuments' flag", ex); //$NON-NLS-1$
			}
		}

		public void selectAllDocuments(ActionEvent e) {
			// All documents already selected
			if(filterModel.filteredElementsCount()==filterModel.getSize()) {
				return;
			}
			
			try {
				filterModel.fill(true);
				//CoreferenceDocumentSetPresenter.this.refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to select all documents", ex); //$NON-NLS-1$
			}
		}

		public void unselectAllDocuments(ActionEvent e) {
			// All documents already unselected
			if(filterModel.filteredElementsCount()==0) {
				return;
			}
			
			try {
				filterModel.fill(false);
				//CoreferenceDocumentSetPresenter.this.refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to unselect all documents", ex); //$NON-NLS-1$
			}
		}

		public void invertDocumentSelection(ActionEvent e) {
			try {
				filterModel.flip();
				//CoreferenceDocumentSetPresenter.this.refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to invert document selection", ex); //$NON-NLS-1$
			}
		}
	}
	
	protected class RebuildFilterJob extends SwingWorker<BitSet, Integer> implements Identity {

		private final Filter filter;
		
		public RebuildFilterJob(Filter filter) {
			if(filter==null)
				throw new IllegalArgumentException("Invalid filter"); //$NON-NLS-1$
			
			this.filter = filter;
		}
		
		private CoreferenceDocumentSetPresenter owner() {
			return CoreferenceDocumentSetPresenter.this;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof RebuildFilterJob) {
				return owner()==((RebuildFilterJob)obj).owner();
			}
			return false;
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
					"plugins.coref.coreferenceDocumentPresenter.rebuildFilterJob.name"); //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return ResourceManager.getInstance().get(
					"plugins.coref.coreferenceDocumentPresenter.rebuildFilterJob.description"); //$NON-NLS-1$
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

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected BitSet doInBackground() throws Exception {
			CoreferenceDocumentSet set = data;
			if(set==null) {
				return null;
			}

			int documentCount = set.size();
			int updateInterval = Math.max(1, documentCount/10);
			int updateCount = 0;
			
			BitSet result = new BitSet(documentCount);
			
			for(int i=0; i<documentCount; i++) {
				if(Thread.currentThread().isInterrupted()) {
					return null;
				}
				
				CoreferenceDocumentData doc = set.get(i);
				if(CoreferenceUtils.containsSpan(doc, filter)) {
					result.set(i);
				}
				
				updateCount++;
				if(updateCount>=updateInterval || i==documentCount-1) {
					updateCount = 0;
					
					setProgress((int) ((i+1f)/documentCount*100));
				}
			}
			
			// If data has changed discard result
			if(set!=data) {
				result = null;
			}
			
			return result;
		}

		@Override
		protected void done() {
			try {
				BitSet filter = get();
				if(filter!=null) {
					filterModel.setFilter(filter);
				}
			} catch(InterruptedException | CancellationException e) {
				// ignore
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to create new filter", e); //$NON-NLS-1$
			}
		}
		
	}
}
