/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core.explorer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.core.ManagementConstants;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.cache.LRUCache;
import net.ikarus_systems.icarus.util.opi.Commands;
import net.ikarus_systems.icarus.util.opi.Message;
import net.ikarus_systems.icarus.util.opi.ResultMessage;

import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginElement;
import org.java.plugin.registry.PluginRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ExtensionPointHierarchyView extends View {
	
	// TODO implement the 'show in hierarchy' functionality so that
	// in lockOutline mode selection in the outlineList will display
	// the extensionPoint chain for the selected parameter
	
	public static final String VIEW_ID = ManagementConstants.EXTENSION_POINT_HIERARCHY_VIEW_ID;
	
	private JTree hierarchyTree;
	private ParamaterDefinitionListModel outlineModel;
	private JList<?> outlineList;
	private JLabel infoLabel;
	private JLabel outlineLabel;
	private JLabel headerLabel;
	private ExtensionPoint currentExtensionPoint;
	private JPanel contentPanel;
	
	private boolean lockOutline = false;
	private HierarchyType hierarchyType = HierarchyType.NORMAL;
	
	private LRUCache<Object, List<ExtensionPoint>> subPointCache;
	
	private CallbackHandler callbackHandler;
	private Handler handler;
	
	private JPopupMenu popupMenu;

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		URL actionLocation = ExtensionPointHierarchyView.class.getResource(
				"extension-point-hierarchy-view-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: extension-point-hierarchy-view-actions.xml"); //$NON-NLS-1$
		
		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.getLogger(ExtensionPointHierarchyView.class).log(LoggerFactory.record(
					Level.SEVERE, "Failed to load actions from file", e)); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}
		
		handler = new Handler();
		
		container.setLayout(new BorderLayout());
		
		// Info label
		infoLabel = new JLabel();
		infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		ResourceManager.getInstance().getGlobalDomain().prepareComponent(
				infoLabel, "plugins.core.extensionPointHierarchyView.notAvailable", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);
		container.add(infoLabel, BorderLayout.NORTH);
		
		contentPanel = new JPanel(new BorderLayout());
		
		// Header area
		Options options = new Options("glue", Box.createHorizontalGlue()); //$NON-NLS-1$
		contentPanel.add(getDefaultActionManager().createToolBar(
				"plugins.core.extensionPointHierarchyView.typeSelectionList", options), //$NON-NLS-1$
				BorderLayout.NORTH);
		
		// Upper panel holding header label and tree
		JPanel upperPanel = new JPanel(new BorderLayout());
		headerLabel = new JLabel("<empty>"); //$NON-NLS-1$
		headerLabel.setBorder(new EmptyBorder(1, 3, 1, 3));
		DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
		hierarchyTree = new JTree(treeModel);
		UIUtil.enableRighClickTreeSelection(hierarchyTree);
		hierarchyTree.setRootVisible(false);
		hierarchyTree.setEditable(false);
		hierarchyTree.setBorder(UIUtil.defaultContentBorder);
		hierarchyTree.addTreeSelectionListener(handler);
		hierarchyTree.addMouseListener(handler);
		hierarchyTree.setCellRenderer(new PluginElementTreeCellRenderer());
		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
		hierarchyTree.setSelectionModel(selectionModel);
		JScrollPane treeScrollPane = new JScrollPane(hierarchyTree);
		treeScrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(treeScrollPane);
		upperPanel.add(headerLabel, BorderLayout.NORTH);
		upperPanel.add(treeScrollPane, BorderLayout.CENTER);
		
		// Lower panel holding second tool-bar and outline list
		JPanel lowerPanel = new JPanel(new BorderLayout());
		outlineLabel = new JLabel();
		outlineLabel.setIcon(IconRegistry.getGlobalRegistry().getIcon("ext_point_obj.gif")); //$NON-NLS-1$
		options = new Options("label", outlineLabel, "glue", Box.createHorizontalGlue()); //$NON-NLS-1$ //$NON-NLS-2$
		lowerPanel.add(getDefaultActionManager().createToolBar(
				"plugins.core.extensionPointHierarchyView.parameterOutlineList", options), //$NON-NLS-1$
				BorderLayout.NORTH);
		outlineModel = new ParamaterDefinitionListModel();
		outlineList = new JList<>(outlineModel);
		UIUtil.enableRighClickListSelection(outlineList);
		outlineList.setBorder(UIUtil.defaultContentBorder);
		outlineList.setCellRenderer(new PluginElementListCellRenderer());
		outlineList.addMouseListener(handler);
		JScrollPane listScrollPane = new JScrollPane(outlineList);
		listScrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(listScrollPane);
		lowerPanel.add(listScrollPane, BorderLayout.CENTER);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPanel, lowerPanel);
		UIUtil.defaultHideSplitPaneDecoration(splitPane);
		splitPane.setResizeWeight(0.4);
		
		contentPanel.add(splitPane, BorderLayout.CENTER);
		container.add(contentPanel, BorderLayout.CENTER);
		contentPanel.setVisible(false);
		
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
		
		// Display as header both current extension point id
		// and declaring plug-in's id
		String header = currentExtensionPoint.getId()+" - " //$NON-NLS-1$
				+currentExtensionPoint.getDeclaringPluginDescriptor().getId();
		headerLabel.setToolTipText(header);
		headerLabel.setText(header);
		
		// Refresh view based on the type settings
		refreshHierarchy();		
		
		// Show content panel
		contentPanel.setVisible(true);
		infoLabel.setVisible(false);
	}
	
	private void refreshHierarchy() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		
		if(currentExtensionPoint==null) {
			return;
		}
		
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new PluginElementProxy(currentExtensionPoint));
		DefaultMutableTreeNode selectedNode = node;
		ExtensionPoint extensionPoint = currentExtensionPoint;
		
		switch (hierarchyType) {
		
		/*
		 * Show a linear tree beginning at the current extension points with
		 * its parent extension points as child nodes
		 */
		case SUPER_HIERARCHY:
			root.add(node);
			while((extensionPoint = getSuperPoint(extensionPoint))!=null) {
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new PluginElementProxy(extensionPoint));
				node.add(newNode);
				node = newNode;
			}
			break;
		
		/*
		 * Show a linear tree beginning at the topmost parent extension point
		 * down to the current extension point and from there on continuing
		 * with all descendants of the current point.
		 */
		case NORMAL:
			feedSubPoints(node, currentExtensionPoint);
			while((extensionPoint = getSuperPoint(extensionPoint))!=null) {
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new PluginElementProxy(extensionPoint));
				newNode.add(node);
				node = newNode;
			}
			root.add(node);
			break;
		
		/*
		 * Show a tree containing all extension points that are descendants
		 * of the current extension point.
		 */
		case SUB_HIERARCHY:
			root.add(node);
			feedSubPoints(node, currentExtensionPoint);
			break;
		}
		
		DefaultTreeModel model = (DefaultTreeModel) hierarchyTree.getModel();
		model.setRoot(root);
		
		// Select the node that is most relevant for the current hierarchy type
		hierarchyTree.setSelectionPath(new TreePath(selectedNode.getPath()));
		
		// Expand the entire tree so user does not have to click for every entry
		UIUtil.expandAll(hierarchyTree, true);
	}
	
	private ExtensionPoint getSuperPoint(ExtensionPoint extensionPoint) {
		if(extensionPoint.getParentExtensionPointId()!=null) {
			PluginRegistry registry = PluginUtil.getPluginRegistry();
			PluginDescriptor descriptor = registry.getPluginDescriptor(extensionPoint.getParentPluginId());
			return descriptor.getExtensionPoint(extensionPoint.getParentExtensionPointId());
		}
		
		return null;
	}
	
	private void feedSubPoints(DefaultMutableTreeNode node, ExtensionPoint extensionPoint) {
		List<ExtensionPoint> subPoints = getSubPoints(extensionPoint);
		for(ExtensionPoint subPoint : subPoints) {
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
					new PluginElementProxy(subPoint));
			node.add(newNode);
			feedSubPoints(newNode, subPoint);
		}
	}
	
	private void displayOutline(ExtensionPoint extensionPoint) {
		if(lockOutline) {
			return;
		}
		
		if(extensionPoint!=null) {
			String text = extensionPoint.getId()+" - " //$NON-NLS-1$
						+extensionPoint.getDeclaringPluginDescriptor().getId();
			outlineLabel.setToolTipText(text);
			
			if(text.length()>18) {
				text = extensionPoint.getId();
			}
			outlineLabel.setText(text);
		} else {
			outlineLabel.setText("-"); //$NON-NLS-1$
			outlineLabel.setToolTipText(null);
		}
		outlineModel.setExtensionPoint(extensionPoint);
	}
	
	private List<ExtensionPoint> getSubPoints(ExtensionPoint extensionPoint) {
		if(subPointCache==null) {
			subPointCache = new LRUCache<>(20);
		}
		
		String key = extensionPoint.getUniqueId();
		
		List<ExtensionPoint> subPoints = subPointCache.get(key);
		
		if(subPoints==null) {
			subPoints = new ArrayList<>();
			
			String id = extensionPoint.getId();
			String pluginId = extensionPoint.getDeclaringPluginDescriptor().getId();
			
			PluginRegistry registry = PluginUtil.getPluginRegistry();
			for(PluginDescriptor descriptor : registry.getPluginDescriptors()) {
				for(ExtensionPoint point : descriptor.getExtensionPoints()) {
					if(id.equals(point.getParentExtensionPointId())
							&& pluginId.equals(point.getParentPluginId())) {
						subPoints.add(point);
					}
				}
			}
		
			if(!subPoints.isEmpty()) {
				Collections.sort(subPoints, PluginUtil.IDENTITY_COMPARATOR);
			}
		}
		
		return subPoints;
	}
	
	/**
	 * When {@code doShow} is {@code true} sets the current hierarchy type
	 * to the given one and refreshes view (clears outline before rebuilding
	 * hierarchy).
	 */
	private void setHierarchyType(boolean doShow, HierarchyType type) {
		if(doShow) {
			hierarchyType = type;
			displayOutline(null);
			refreshHierarchy();
		}
	}
	
	private void sendToExplorer(Object selectedObject, boolean showPoint) {
		if(selectedObject==null) {
			return;
		}
		if(selectedObject instanceof DefaultMutableTreeNode) {
			selectedObject = ((DefaultMutableTreeNode)selectedObject).getUserObject();
		}
		if(selectedObject instanceof PluginElementProxy) {
			selectedObject = ((PluginElementProxy)selectedObject).get();
		}
		
		if(showPoint) { 
			if(!(selectedObject instanceof ExtensionPoint.ParameterDefinition)) {
				return;
			} 
			// Fetch declaring extension point 
			selectedObject = ((ExtensionPoint.ParameterDefinition)selectedObject).getDeclaringExtensionPoint();
		} else if(selectedObject instanceof PluginElement) {
			// Fetch declaring descriptor
			selectedObject = ((PluginElement<?>)selectedObject).getDeclaringPluginDescriptor();
		} else {
			return;
		}
		
		// selectedObject is now either a PluginDescriptor or ExtensionPoint
		Message message = new Message(Commands.SELECT, selectedObject, null);
		ResultMessage result = sendRequest(ManagementConstants.PLUGIN_EXPLORER_VIEW_ID, message);
		
		if(result.getThrowable()!=null) {
			LoggerFactory.getLogger(ExtensionPointHierarchyView.class).log(LoggerFactory.record(Level.SEVERE, 
					"Failed to send display data to plugin explorer: "+selectedObject, result.getThrowable())); //$NON-NLS-1$
		}
	}
	
	private void showPopup(MouseEvent trigger) {
		if(popupMenu==null) {
			// Create new popup menu
			
			Options options = new Options();
			popupMenu = getDefaultActionManager().createPopupMenu(
					"plugins.core.extensionPointHierarchyView.popupMenuList", options); //$NON-NLS-1$
			
			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.getLogger(ExtensionPointHierarchyView.class).log(LoggerFactory.record(
						Level.SEVERE, "Unable to create popup menu")); //$NON-NLS-1$
			}
		}
		
		if(popupMenu!=null) {			
			popupMenu.show(trigger.getComponent(), trigger.getX(), trigger.getY());
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#close()
	 */
	@Override
	public void close() {
		if(hierarchyTree==null) {
			return;
		}
		outlineModel.clear();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)hierarchyTree.getModel().getRoot();
		root.removeAllChildren();
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		if(contentPanel==null) {
			return;
		}
		
		displayData(null);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#receiveData(net.ikarus_systems.icarus.plugins.core.View, java.lang.Object, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {

		Object data = message.getData();
		
		if(!(data instanceof ExtensionPoint)) {
			return message.unsupportedDataResult();
		}
		
		if(Commands.DISPLAY.equals(message.getCommand())) {
			displayData((ExtensionPoint)data);
			requestFocusInPerspective();
			return message.successResult(null);
		} else {
			return message.unknownRequestResult();
		}
	}

	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();

		actionManager.setSelected(hierarchyType==HierarchyType.NORMAL,
				"plugins.core.extensionPointHierarchyView.showHierarchyAction");  //$NON-NLS-1$
		actionManager.addHandler("plugins.core.extensionPointHierarchyView.showHierarchyAction",  //$NON-NLS-1$
				callbackHandler, "showHierarchy"); //$NON-NLS-1$

		actionManager.setSelected(hierarchyType==HierarchyType.SUPER_HIERARCHY,
				"plugins.core.extensionPointHierarchyView.showSuperHierarchyAction");  //$NON-NLS-1$
		actionManager.addHandler("plugins.core.extensionPointHierarchyView.showSuperHierarchyAction",  //$NON-NLS-1$
				callbackHandler, "showSuperHierarchy"); //$NON-NLS-1$

		actionManager.setSelected(hierarchyType==HierarchyType.SUB_HIERARCHY,
				"plugins.core.extensionPointHierarchyView.showSubHierarchyAction");  //$NON-NLS-1$
		actionManager.addHandler("plugins.core.extensionPointHierarchyView.showSubHierarchyAction",  //$NON-NLS-1$
				callbackHandler, "showSubHierarchy"); //$NON-NLS-1$

		actionManager.setSelected(lockOutline, "plugins.core.extensionPointHierarchyView.lockOutlineAction"); //$NON-NLS-1$
		actionManager.addHandler("plugins.core.extensionPointHierarchyView.lockOutlineAction",  //$NON-NLS-1$
				callbackHandler, "lockOutline"); //$NON-NLS-1$

		actionManager.setSelected(outlineModel.isShowInheretedParamaters(),
				"plugins.core.extensionPointHierarchyView.showInheritedParametersAction");  //$NON-NLS-1$
		actionManager.addHandler("plugins.core.extensionPointHierarchyView.showInheritedParametersAction",  //$NON-NLS-1$
				callbackHandler, "showInheritedParameters"); //$NON-NLS-1$

		actionManager.setSelected(outlineModel.isSortParametersByExtensionPoint(),
				"plugins.core.extensionPointHierarchyView.sortParametersByExtensionPointAction");  //$NON-NLS-1$
		actionManager.addHandler("plugins.core.extensionPointHierarchyView.sortParametersByExtensionPointAction",  //$NON-NLS-1$
				callbackHandler, "sortParametersByExtensionPoint"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.core.extensionPointHierarchyView.showExtensionPointAction",  //$NON-NLS-1$
				callbackHandler, "showExtensionPoint"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.core.extensionPointHierarchyView.showPluginAction",  //$NON-NLS-1$
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
		
		public void showHierarchy(ActionEvent e) {
			// no-op
		}
		
		public void showHierarchy(boolean showHierarchy) {
			setHierarchyType(showHierarchy, HierarchyType.NORMAL);
		}
		
		public void showSuperHierarchy(ActionEvent e) {
			// no-op
		}
		
		public void showSuperHierarchy(boolean showSuperHierarchy) {
			setHierarchyType(showSuperHierarchy, HierarchyType.SUPER_HIERARCHY);
		}
		
		public void showSubHierarchy(ActionEvent e) {
			// no-op
		}
		
		public void showSubHierarchy(boolean showSubHierarchy) {
			setHierarchyType(showSubHierarchy, HierarchyType.SUB_HIERARCHY);
		}
		
		public void lockOutline(boolean b) {
			lockOutline = b;
			if(!lockOutline) {
				hierarchyTree.clearSelection();
			}
		}
		
		public void lockOutline(ActionEvent e) {
			// no-op
		}
		
		public void showInheritedParameters(boolean b) {
			outlineModel.setShowInheretedParamaters(b);
		}
		
		public void showInheritedParameters(ActionEvent e) {
			// no-op
		}
		
		public void sortParametersByExtensionPoint(boolean b) {
			outlineModel.setSortParametersByExtensionPoint(b);
		}
		
		public void sortParametersByExtensionPoint(ActionEvent e) {
			// no-op
		}
		
		public void showExtensionPoint(ActionEvent e) {
			Object selectedObject = null;
			if(popupMenu.getInvoker()==hierarchyTree) {
				selectedObject = hierarchyTree.getLastSelectedPathComponent();
			} else {
				selectedObject = outlineList.getSelectedValue();
			}
			
			if(selectedObject!=null) {
				sendToExplorer(selectedObject, true);
			}
		}
		
		public void showPlugin(ActionEvent e) {
			Object selectedObject = null;
			if(popupMenu.getInvoker()==hierarchyTree) {
				selectedObject = hierarchyTree.getLastSelectedPathComponent();
			} else {
				selectedObject = outlineList.getSelectedValue();
			}
			
			if(selectedObject!=null) {
				sendToExplorer(selectedObject, false);
			}
		}
	}
	
	private enum HierarchyType {
		NORMAL,
		SUB_HIERARCHY,
		SUPER_HIERARCHY;
	}
	
	private final class Handler extends MouseAdapter implements TreeSelectionListener {

		/**
		 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
		 */
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath treePath = e.getPath();
			if(treePath==null) {
				return;
			}
			
			Object selectedObject = treePath.getLastPathComponent();
			if(selectedObject instanceof DefaultMutableTreeNode) {
				selectedObject = ((DefaultMutableTreeNode)selectedObject).getUserObject();
			}
			if(selectedObject instanceof PluginElementProxy) {
				selectedObject = ((PluginElementProxy)selectedObject).get();
			}
			
			if(selectedObject instanceof ExtensionPoint) {
				displayOutline((ExtensionPoint) selectedObject);
			} else {
				displayOutline(null);
			}
		}
		

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			super.mouseClicked(e);
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
		
	}
}
