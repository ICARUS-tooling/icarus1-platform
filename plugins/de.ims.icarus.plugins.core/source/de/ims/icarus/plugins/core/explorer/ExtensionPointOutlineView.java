/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.core.explorer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;


import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginElement;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.ManagementConstants;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.LabelProxy;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ExtensionPointOutlineView extends View {
	
	public static final String VIEW_ID = ManagementConstants.EXTENSION_POINT_OUTLINE_VIEW_ID;
	
	private JTextArea infoLabel;
	private JLabel headerLabel;
	private JPanel contentPanel;
	private JTree extensionPointTree;
	private JScrollPane treeScrollPane;
	private ExtensionPoint currentExtensionPoint;
	
	private Handler handler;	
	private JPopupMenu popupMenu;
	private CallbackHandler callbackHandler;
	
	public ExtensionPointOutlineView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		URL actionLocation = ExtensionPointOutlineView.class.getResource(
				"extension-point-outline-view-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException(
					"Missing resources: extension-point-outline-view-actions.xml"); //$NON-NLS-1$
		
		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, "Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}
		
		handler = new Handler();
		
		container.setLayout(new BorderLayout());

		infoLabel = UIUtil.defaultCreateInfoLabel(container);
		ResourceManager.getInstance().getGlobalDomain().prepareComponent(
				infoLabel, "plugins.core.extensionPointOutlineView.notAvailable", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);
		
		container.add(infoLabel, BorderLayout.NORTH);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		extensionPointTree = new JTree(treeModel);
		UIUtil.enableRighClickTreeSelection(extensionPointTree);
		extensionPointTree.setCellRenderer(new PluginElementTreeCellRenderer());
		extensionPointTree.setEditable(false);
		extensionPointTree.setBorder(UIUtil.defaultContentBorder);
		extensionPointTree.addMouseListener(handler);
		extensionPointTree.addTreeSelectionListener(handler);
		
		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
		extensionPointTree.setSelectionModel(selectionModel);
		
		treeScrollPane = new JScrollPane(extensionPointTree);
		treeScrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(treeScrollPane);

		headerLabel = new JLabel("<empty>"); //$NON-NLS-1$
		headerLabel.setBorder(new EmptyBorder(1, 3, 1, 3));
		
		contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(headerLabel, BorderLayout.NORTH);
		contentPanel.add(extensionPointTree, BorderLayout.CENTER);
		contentPanel.setVisible(false);
		container.add(contentPanel, BorderLayout.CENTER);
		
		container.setPreferredSize(new Dimension(250, 200));
		container.setMinimumSize(new Dimension(200, 200));
		
		addBroadcastListener(ManagementConstants.EXPLORER_SELECTION_CHANGED, handler);

		registerActionCallbacks();
	}
	
	private void displayData(ExtensionPoint extensionPoint) {
		
		// just display our default message in case
		// there is no extension point to be displayed
		if(extensionPoint==null) {
			currentExtensionPoint = null;
			contentPanel.setVisible(false);
			infoLabel.setVisible(true);
			return;
		}
		
		if(currentExtensionPoint==extensionPoint)
			return;
		
		currentExtensionPoint = extensionPoint;
		
		String header = currentExtensionPoint.getId()+" - " //$NON-NLS-1$
				+currentExtensionPoint.getDeclaringPluginDescriptor().getId();
		headerLabel.setToolTipText(header);
		headerLabel.setText(header);
		
		rebuildTree();
		
		contentPanel.setVisible(true);
		infoLabel.setVisible(false);
	}
	
	private void rebuildTree() {		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) extensionPointTree.getModel().getRoot();
		root.removeAllChildren();
		root.setUserObject(null);
				
		ExtensionPoint extensionPoint = this.currentExtensionPoint;
		if(extensionPoint==null)
			return;
		
		LoggerFactory.log(this, Level.FINE, "Rebuilding tree for extension "+extensionPoint.getUniqueId()); //$NON-NLS-1$
		
		root.setUserObject(new PluginElementProxy(extensionPoint));
		
		DefaultMutableTreeNode category, node;
		int connectedExtensionsNodeRow = 1;

		// add parent info if extension point is derived
		if(extensionPoint.getParentPluginId()!=null) {
			root.add(new DefaultMutableTreeNode(new LabelProxy(
					"plugins.core.pluginExplorerView.labels.parentPluginId",  //$NON-NLS-1$
					null, extensionPoint.getParentPluginId())));
			connectedExtensionsNodeRow++;
		}
		
		if(extensionPoint.getParentExtensionPointId()!=null) {
			root.add(new DefaultMutableTreeNode(new LabelProxy(
					"plugins.core.pluginExplorerView.labels.parentPointId",  //$NON-NLS-1$
					null, extensionPoint.getParentExtensionPointId())));
			connectedExtensionsNodeRow++;
		}
		
		Set<PluginDescriptor> connectedPugins = new HashSet<>();
		int connectedPluginsNodeRow = connectedExtensionsNodeRow+1;
		
		// list connected extensions
		List<Extension> extensions = new ArrayList<>(extensionPoint.getConnectedExtensions());
		if(extensions.isEmpty()) {
			root.add(new DefaultMutableTreeNode(new LabelProxy(
					"plugins.core.extensionPointOutlineView.labels.emptyExtensionPoint", "extensions_obj.gif"))); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			category = new DefaultMutableTreeNode(new LabelProxy(
					"plugins.core.extensionPointOutlineView.labels.connectedExtensions",  //$NON-NLS-1$
					"extensions_obj.gif", extensions.size())); //$NON-NLS-1$
			
			DefaultMutableTreeNode paramsNode;
			Collections.sort(extensions, PluginUtil.IDENTITY_COMPARATOR);
			for(Extension extension : extensions) {
				connectedPugins.add(extension.getDeclaringPluginDescriptor());
				
				node = new DefaultMutableTreeNode(new PluginElementProxy(extension));
				// add extended plugin id info
				node.add(new DefaultMutableTreeNode(new LabelProxy(
						"plugins.core.pluginExplorerView.labels.extendedPlugin",  //$NON-NLS-1$
						null, extension.getExtendedPluginId())));
				// add extended point id info
				node.add(new DefaultMutableTreeNode(new LabelProxy(
						"plugins.core.pluginExplorerView.labels.extendedPoint",  //$NON-NLS-1$
						null, extension.getExtendedPointId())));				
				
				// parameter definitions
				List<Extension.Parameter> params = new ArrayList<>(
						extension.getParameters());
				if(!params.isEmpty()) {
					paramsNode = new DefaultMutableTreeNode(new LabelProxy(
							"plugins.core.pluginExplorerView.labels.parameters", null)); //$NON-NLS-1$
					Collections.sort(params, PluginUtil.IDENTITY_COMPARATOR);
					
					for(Extension.Parameter param : params) {
						paramsNode.add(new DefaultMutableTreeNode(new PluginElementProxy(param)));
					}
					node.add(paramsNode);
				}
				
				category.add(node);
				connectedPluginsNodeRow++;
			}
			
			root.add(category);
		}
		
		// display plug-ins for connected extensions
		if(!connectedPugins.isEmpty()) {
			category = new DefaultMutableTreeNode(new LabelProxy(
					"plugins.core.extensionPointOutlineView.labels.connectedPlugins",  //$NON-NLS-1$
					"plugin_depend.gif", connectedPugins.size())); //$NON-NLS-1$
			
			List<PluginDescriptor> descriptors = new ArrayList<>(connectedPugins);
			Collections.sort(descriptors, PluginUtil.IDENTITY_COMPARATOR);
			
			for(PluginDescriptor descriptor : descriptors) {
				category.add(new DefaultMutableTreeNode(new PluginElementProxy(descriptor)));
			}
			
			root.add(category); 
		}
		
		DefaultTreeModel treeModel = (DefaultTreeModel) extensionPointTree.getModel();
		treeModel.reload();
		
		extensionPointTree.expandRow(0);
		if(connectedExtensionsNodeRow<extensionPointTree.getRowCount())
			extensionPointTree.expandRow(connectedExtensionsNodeRow);
		if(connectedPluginsNodeRow<extensionPointTree.getRowCount())
			extensionPointTree.expandRow(connectedPluginsNodeRow);
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		if(contentPanel==null) {
			return;
		}
		
		displayData(null);
	}
	
	private void sendToExplorer(Object selectedObject) {
		if(selectedObject==null) {
			return;
		}
		if(selectedObject instanceof DefaultMutableTreeNode) {
			selectedObject = ((DefaultMutableTreeNode)selectedObject).getUserObject();
		}
		if(selectedObject instanceof PluginElementProxy) {
			selectedObject = ((PluginElementProxy)selectedObject).get();
		}
		
		if(selectedObject instanceof PluginElement) {
			// Fetch declaring descriptor
			selectedObject = ((PluginElement<?>)selectedObject).getDeclaringPluginDescriptor();
		} else if(!(selectedObject instanceof PluginDescriptor)) {
			return;
		}
		
		// selectedObject is now a PluginDescriptor
		Message message = new Message(this, Commands.SELECT, selectedObject, null);
		ResultMessage result = sendRequest(
				ManagementConstants.PLUGIN_EXPLORER_VIEW_ID, message);
		
		if(result.getThrowable()!=null) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to send display data to plugin explorer: "+selectedObject, result.getThrowable()); //$NON-NLS-1$
		}
	}
	
	private Object[] getSelectionPath() {
		if(extensionPointTree==null) {
			return null;
		}
		
		TreePath selectionPath = extensionPointTree.getSelectionPath();
		if(selectionPath==null) {
			return null;
		}
		
		return PluginExplorerUtil.getObjectPath(selectionPath);
	}
	
	private void showPopup(MouseEvent trigger) {
		if(popupMenu==null) {
			// Create new popup menu			
			Options options = new Options();
			popupMenu = getDefaultActionManager().createPopupMenu(
					"plugins.core.extensionPointOutlineView.popupMenuList", options); //$NON-NLS-1$
			
			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}
		
		if(popupMenu!=null) {			
			popupMenu.show(trigger.getComponent(), trigger.getX(), trigger.getY());
		}
	}
	
	private ExtensionPoint getPointFromData(Object data) {
		
		if(data instanceof Object[]) {
			Object[] path = (Object[]) data;
			
			if(path!=null) {
				// look for first extension point object in path
				for(int i=path.length-1; i>-1; i--) {
					if(path[i] instanceof ExtensionPoint) {
						data = (ExtensionPoint)path[i];
						break;
					}
				}
			}
		}
		
		return (data instanceof ExtensionPoint) ? (ExtensionPoint)data : null;
	}

	/**
	 * Accepted commands:
	 * <ul>
	 * <li>{@link Commands#DISPLAY}</li>
	 * <li>{@link Commands#CLEAR}</li>
	 * </ul>
	 * 
	 * @see de.ims.icarus.plugins.core.View#handleRequest(de.ims.icarus.util.mpi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		
		if(Commands.DISPLAY.equals(message.getCommand())) {
			
			ExtensionPoint extensionPoint = getPointFromData(message.getData());
			
			if(extensionPoint==null) {
				return message.unsupportedDataResult(this);
			}
			
			displayData((ExtensionPoint)extensionPoint);
			return message.successResult(this, null);
		} else if(Commands.CLEAR.equals(message.getCommand())) {
			reset();
			
			return message.successResult(this, null);
		} else {
			return message.unknownRequestResult(this);
		}
	}


	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.addHandler("plugins.core.extensionPointOutlineView.showPluginAction",  //$NON-NLS-1$
				callbackHandler, "showPlugin"); //$NON-NLS-1$
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public final class CallbackHandler {
		
		private CallbackHandler() {
			// no-op
		}
		
		public void showPlugin(ActionEvent e) {
			Object[] selectionPath = getSelectionPath();
			PluginDescriptor descriptor = PluginExplorerUtil.getDeclaringPluginDescriptor(selectionPath);
			
			if(descriptor!=null) {
				sendToExplorer(descriptor);
			}
		}
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	private final class Handler extends MouseAdapter implements 
			EventListener, TreeSelectionListener, ManagementConstants {

		@Override
		public void valueChanged(TreeSelectionEvent evt) {
			Object[] path = PluginExplorerUtil.getObjectPath(evt.getPath());
			
			fireBroadcastEvent(new EventObject(
					OUTLINE_SELECTION_CHANGED, "path", path)); //$NON-NLS-1$
		}
		
		private void maybeShowPopup(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showPopup(e);
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
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			ExtensionPoint extensionPoint = getPointFromData(
					event.getProperty("path")); //$NON-NLS-1$
			if(extensionPoint==null) {
				return;
			}
			displayData(extensionPoint);
		}
	}
}
