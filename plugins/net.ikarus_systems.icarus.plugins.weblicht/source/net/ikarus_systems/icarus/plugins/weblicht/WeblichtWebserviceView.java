/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.weblicht;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webchain;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebchainRegistry;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webservice;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceRegistry;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WeblichtWebserviceView extends View {

	protected JList<Object> webserviceList;
	protected WebserviceViewListModel webserviceViewListModel;
	
	private JPopupMenu popupMenu;

	private Handler handler;
	private CallbackHandler callbackHandler;
	
	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@SuppressWarnings("static-access")
	@Override
	public void init(JComponent container) {
		// Load actions
		URL actionLocation = WeblichtWebserviceView.class
				.getResource("weblicht-webservice-view-actions.xml"); //$NON-NLS-1$
		if (actionLocation == null)
			throw new CorruptedStateException(
					"Missing resources: weblicht-webservice-view-actions.xml"); //$NON-NLS-1$

		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}

		handler = new Handler();
		

		 //ArrayList<Webservice> webservicesList = (ArrayList<Webservice>) WebserviceRegistry.getInstance().getWebserviceList();
		 //webserviceList = new JList(webservicesList.toArray());
		
		//Create and initialize JList
		webserviceViewListModel = new WebserviceViewListModel();
		webserviceList = new JList<Object>(webserviceViewListModel);
		webserviceList.setBorder(UIUtil.defaultContentBorder);
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		selectionModel.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		webserviceList.setSelectionModel(selectionModel);
		webserviceList.addListSelectionListener(handler);
		webserviceList.addMouseListener(handler);
		webserviceList.getModel().addListDataListener(handler);

		
		// Scroll pane
		JScrollPane scrollPane = new JScrollPane(webserviceList);
		scrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		
		// Header
		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.weblicht.weblichtWebserviceView.toolBarList", null); //$NON-NLS-1$
		toolBar.add(Box.createHorizontalGlue());
		
		// Add Stuff into container
		container.setLayout(new BorderLayout());
		container.add(toolBar, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		container.setPreferredSize(new Dimension(300, 500));
		container.setMinimumSize(new Dimension(250, 400));
		
		webserviceViewListModel.reload();
		
		registerActionCallbacks();
		refreshActions(null);
		
	}
	
	private void showPopup(MouseEvent trigger) {
		if (popupMenu == null) {
			// Create new popup menu

			Options options = new Options();
			popupMenu = getDefaultActionManager()
					.createPopupMenu(
							"plugins.weblicht.weblichtWebserviceView.popupMenuList", options); //$NON-NLS-1$

			if (popupMenu != null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE,
						"Unable to create popup menu"); //$NON-NLS-1$
			}
		}

		if (popupMenu != null) {
			popupMenu.show(webserviceList, trigger.getX(), trigger.getY());
		}
	}
	
	
	private Object getSelectedObject() {
		return webserviceList.getSelectedValue();
	}
		
	
	private void refreshActions(Object selectedObject) {
		ActionManager actionManager = getDefaultActionManager();

				
		actionManager.setEnabled(selectedObject instanceof Webservice,
				"plugins.weblicht.weblichtWebserviceView.deleteWebserviceAction", //$NON-NLS-1$
				"plugins.weblicht.weblichtWebserviceView.cloneWebserviceAction",  //$NON-NLS-1$
				"plugins.weblicht.weblichtWebserviceView.editWebserviceAction");//$NON-NLS-1$
	}
	
	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		actionManager.addHandler("plugins.weblicht.weblichtWebserviceView.saveWebserviceAction",  //$NON-NLS-1$
				callbackHandler, "saveWebservice"); //$NON-NLS-1$
		actionManager.addHandler("plugins.weblicht.weblichtWebserviceView.newWebserviceAction",  //$NON-NLS-1$
				callbackHandler, "newWebservice"); //$NON-NLS-1$
		actionManager.addHandler("plugins.weblicht.weblichtWebserviceView.deleteWebserviceAction",  //$NON-NLS-1$
				callbackHandler, "deleteWebservice"); //$NON-NLS-1$
		actionManager.addHandler("plugins.weblicht.weblichtWebserviceView.cloneWebserviceAction",  //$NON-NLS-1$
				callbackHandler, "cloneWebservice"); //$NON-NLS-1$
		actionManager.addHandler("plugins.weblicht.weblichtWebserviceView.editWebserviceAction",  //$NON-NLS-1$
				callbackHandler, "editWebservice"); //$NON-NLS-1$
	}
	
	/**
	 * 
	 * @author Gregor Thiele
	 * @version $Id$
	 *
	 */
	protected class Handler extends MouseAdapter implements 
	ListSelectionListener, ListDataListener {

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {

			Object selectedObject = webserviceList.getSelectedValue();
			refreshActions(selectedObject);
			fireBroadcastEvent(new EventObject(
					WeblichtConstants.WEBLICHT_WEBSERVICE_VIEW_CHANGED,
					"item", selectedObject)); //$NON-NLS-1$
		}
		
		
		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
				// Note: we rely on the fact, that our callback handler
				// does not use the supplied ActionEvent object, so we pass
				// null.
				// If this ever changes we could run into some trouble!
				callbackHandler.editWebservice(null);
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}
		

		/**
		 * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void contentsChanged(ListDataEvent e) {
			// TODO Auto-generated method stub
			System.out.println("contentsChanged: " + e.getIndex0() +
                   ", " + e.getIndex1());
			
		}

		/**
		 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void intervalAdded(ListDataEvent e) {
			// TODO Auto-generated method stub
			System.out.println("contentAdded: " + e.getIndex0() +
	                   ", " + e.getIndex1());
			
		}

		/**
		 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void intervalRemoved(ListDataEvent e) {
			// TODO Auto-generated method stub
			System.out.println("contentDeleted: " + e.getIndex0() +
	                   ", " + e.getIndex1());
			
		}
	}
	
	public final class CallbackHandler {
		
		private CallbackHandler() {
			// no-op
		}
		
		/**
		 * Check required Webservicefields, webservicename must be set!
		 * @param webservice
		 * @param uID 
		 */
		

		/**
		 * 
		 * @param e
		 */
		public void newWebservice(ActionEvent e) {	
			
			String uID = WebserviceRegistry.getInstance().createUniqueID(""); //$NON-NLS-1$
			
			//not needed? check already in webservice registry
			if (!WebserviceRegistry.getInstance().isValidUniqueID(uID)){
				//System.out.println("unique " +  WebserviceRegistry.getInstance().isValidUniqueID(uID));
				return;
			}
			
			//only uid needed rest will be filled out by user later!	
			Webservice webservice = WebserviceDialogs
					.getWebserviceDialogFactory()
					.showNewWebserviceReworkDialog(
							null,
							"plugins.weblicht.weblichtWebserviceView.dialogs.addWebservice.title", //$NON-NLS-1$
							"plugins.weblicht.weblichtWebserviceView.dialogs.addWebservice.message", //$NON-NLS-1$
							uID, null, null, null, null, null, null, "text/xml",null , null); //$NON-NLS-1$

			
			//user canceled or webservicename empty
			if (webservice==null){
				return;
			}

			try {				
				WebserviceRegistry.getInstance().addNewWebservice(webservice);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to create new webservice: "+webservice.getName(), ex); //$NON-NLS-1$
			}
			
			//System.out.println("WSCount " + WebserviceRegistry.getInstance().getWebserviceCount());
		}
		
		/**
		 * 
		 * @param e
		 */
		public void deleteWebservice(ActionEvent e) {
			Object selectedObject = webserviceList.getSelectedValue();
			if(selectedObject==null || !(selectedObject instanceof Webservice)) {
				return;
			}
			
			Webservice webservice = (Webservice)selectedObject;
			
			//Webservice used in any chain?
			boolean inUse = WebchainRegistry.getInstance().webserviceUsed(webservice);
			
			Webchain webchain = WebchainRegistry.getInstance().webserviceFirstOccurence(webservice);
					
			if(inUse && webchain!=null){
				DialogFactory.getGlobalFactory().showError(getFrame(),
						"plugins.weblicht.weblichtWebserviceView.dialogs.deleteWebserviceInUsed.title",  //$NON-NLS-1$
						"plugins.weblicht.weblichtWebserviceView.dialogs.deleteWebserviceInUsed.message",  //$NON-NLS-1$
						webchain.getName());
				return;
			}
			
			boolean doDelete = false;
			
			doDelete = DialogFactory.getGlobalFactory().showConfirm(getFrame(), 
					"plugins.weblicht.weblichtWebserviceView.dialogs.deleteWebservice.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtWebserviceView.dialogs.deleteWebservice.message",  //$NON-NLS-1$
					webservice.getName());
			
			if(doDelete) {
				try {
					//webserviceViewListModel.removeWebservice(webservice);
					WebserviceRegistry.getInstance().deleteWebservice(webservice);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Unable to delete webservice: "+webservice.getName(), ex); //$NON-NLS-1$
				}
			}
		}
		
		/**
		 * 
		 * @param e
		 */
		public void cloneWebservice(ActionEvent e) {
			Object selectedObject = webserviceList.getSelectedValue();
			if(selectedObject==null || !(selectedObject instanceof Webservice)) {
				return;
			}
			
			String uID = WebserviceRegistry.getInstance().createUniqueID(""); //$NON-NLS-1$
			
			//not needed? check already in webservice registry
			if (!WebserviceRegistry.getInstance().isValidUniqueID(uID)){
				//System.out.println("unique " +  WebserviceRegistry.getInstance().isValidUniqueID(uID));
				return;
			}
			
			Webservice webserviceOld = (Webservice)selectedObject;
			
			String webserviceName = WebserviceRegistry.getInstance()
					.getUniqueName(webserviceOld.getName());
			
			//only uid needed rest will be filled out by user later!	
			Webservice webservice = WebserviceDialogs
					.getWebserviceDialogFactory()
					.showNewWebserviceReworkDialog(
							null,
							"plugins.weblicht.weblichtWebserviceView.dialogs.cloneWebservice.title", //$NON-NLS-1$
							"plugins.weblicht.weblichtWebserviceView.dialogs.cloneWebservice.message", //$NON-NLS-1$
							uID,
							webserviceName,
							webserviceOld.getDescription(),							
							webserviceOld.getCreator(),
							webserviceOld.getContact(),
							webserviceOld.getURL(),
							webserviceOld.getServiceID(),
							webserviceOld.getWebresourceFormat(),
							null , null);

			
			//user canceled or webservicename empty
			if (webservice==null){
				return;
			}

			try {				
				WebserviceRegistry.getInstance().cloneWebservice(webservice,webserviceOld);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to create new webservice: "+webservice.getName(), ex); //$NON-NLS-1$
			}
		}

		/**
		 * 
		 * @param e
		 */
		public void editWebservice(ActionEvent e) {
			Object selectedObject = webserviceList.getSelectedValue();

			if(selectedObject==null || !(selectedObject instanceof Webservice)) {
				return;
			}	
			
			Webservice webservice = (Webservice)selectedObject;
			
			//System.out.println(webservice.getName() + " " + webservice.getUID());
			
			Message message = new Message(this, Commands.EDIT, webservice, null);
			sendRequest(WeblichtConstants.WEBSERVICE_EDIT_VIEW_ID, message);
		}
		
		/**
		 * 
		 * @param e
		 */
		public void saveWebservice(ActionEvent e) {
			try {
				WebserviceRegistry.getInstance().saveWebservices();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to save Webservices: ", ex); //$NON-NLS-1$
			}			
		}
		
	}

}
