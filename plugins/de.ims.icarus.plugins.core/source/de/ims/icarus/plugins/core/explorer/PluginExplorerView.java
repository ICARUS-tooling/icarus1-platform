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
package de.ims.icarus.plugins.core.explorer;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.java.plugin.Plugin;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.Library;
import org.java.plugin.registry.PluginAttribute;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginElement;
import org.java.plugin.registry.PluginFragment;
import org.java.plugin.registry.PluginPrerequisite;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.IcarusCorePlugin;
import de.ims.icarus.plugins.core.InfoPanel;
import de.ims.icarus.plugins.core.ManagementConstants;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.LabelProxy;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PluginExplorerView extends View {

	public static final String VIEW_ID = ManagementConstants.PLUGIN_EXPLORER_VIEW_ID;

	private JTree pluginTree;

	// Maps data objects to tree nodes
	private Map<Object, Object> nodeMap;

	private CallbackHandler callbackHandler;
	private Handler handler;

	private JPopupMenu popupMenu;

	/**
	 *
	 */
	public PluginExplorerView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(final JComponent container) {

		// Load actions
		if(!defaultLoadActions(PluginExplorerView.class, "plugin-explorer-view-actions.xml")) { //$NON-NLS-1$
			return;
		}

		// Create and init tree
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();

		nodeMap = new HashMap<>();
		pluginTree = new JTree(root);
		UIUtil.enableRighClickTreeSelection(pluginTree);
		pluginTree.setCellRenderer(new PluginElementTreeCellRenderer());
		pluginTree.setEditable(false);
		pluginTree.setBorder(UIUtil.defaultContentBorder);

		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		pluginTree.setSelectionModel(selectionModel);

		// Collect all registered plug-ins
		PluginManager pluginManager = PluginUtil.getPluginManager();
		List<PluginDescriptor> plugins = new ArrayList<>(
				pluginManager.getRegistry().getPluginDescriptors());

		// Sort plug-ins lexically
		Collections.sort(plugins, PluginUtil.IDENTITY_COMPARATOR);

		// Populate our tree view
		for(int i=0; i<plugins.size(); i++) {
			PluginDescriptor descriptor = plugins.get(i);
			feedPluginNode(pluginManager, root, descriptor);
		}

		pluginTree.expandRow(0);
		pluginTree.setRootVisible(false);
		pluginTree.setShowsRootHandles(true);

		handler = new Handler();
		pluginTree.addTreeSelectionListener(handler);
		pluginTree.addMouseListener(handler);

		// Listen for future events on the plugin manager
		pluginManager.registerListener(handler);

		// Present the tree
		container.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(pluginTree);
		scrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		container.add(scrollPane, BorderLayout.CENTER);

		container.setPreferredSize(new Dimension(300, 500));
		container.setMinimumSize(new Dimension(250, 400));

		registerActionCallbacks();
	}

	private void feedPluginNode(PluginManager pluginManager, DefaultMutableTreeNode parent, PluginDescriptor descriptor) {
		DefaultMutableTreeNode pluginNode = new DefaultMutableTreeNode(
				new PluginElementProxy(descriptor));
		nodeMap.put(descriptor, pluginNode);

		DefaultMutableTreeNode category;
		DefaultMutableTreeNode node, newNode;

		// Add plugin version info
		pluginNode.add(new DefaultMutableTreeNode(new LabelProxy(
				"plugins.core.pluginExplorerView.labels.pluginVersion",  //$NON-NLS-1$
				null, descriptor.getVersion())));

		// Add plugin vendor info
		String vendor = descriptor.getVendor();
		if(vendor==null || vendor.isEmpty())
			vendor = "<undefined>"; //$NON-NLS-1$
		pluginNode.add(new DefaultMutableTreeNode(new LabelProxy(
				"plugins.core.pluginExplorerView.labels.pluginVendor",  //$NON-NLS-1$
				null, vendor)));

		// Add plugin class info
		pluginNode.add(new DefaultMutableTreeNode(new LabelProxy(
				"plugins.core.pluginExplorerView.labels.pluginClassName",  //$NON-NLS-1$
				"class_obj.gif", descriptor.getPluginClassName()))); //$NON-NLS-1$

		// Add plugin location info
		URL location = descriptor.getLocation();
		pluginNode.add(new DefaultMutableTreeNode(new LabelProxy(
				"plugins.core.pluginExplorerView.labels.pluginLocation",  //$NON-NLS-1$
				"plugin_mf_obj.gif", location.toExternalForm()))); //$NON-NLS-1$

		// Attributes
		List<PluginAttribute> attributes = new ArrayList<>(descriptor.getAttributes());
		if(!attributes.isEmpty()) {
			category = new DefaultMutableTreeNode(new LabelProxy(
					"plugins.core.pluginExplorerView.labels.attributes", "prop_ps.gif")); //$NON-NLS-1$ //$NON-NLS-2$

			Collections.sort(attributes, PluginUtil.IDENTITY_COMPARATOR);
			for(PluginAttribute attribute : attributes) {
				category.add(new DefaultMutableTreeNode(new LabelProxy(
						"plugins.core.pluginExplorerView.labels.pluginAttribute",  //$NON-NLS-1$
						null, attribute.getId(), attribute.getValue())));

			}
			pluginNode.add(category);
		}

		// Libraries
		List<Library> libraries = new ArrayList<>(descriptor.getLibraries());
		if(!libraries.isEmpty()) {
			category = new DefaultMutableTreeNode(new LabelProxy(
					"plugins.core.pluginExplorerView.labels.libraries", "classpath.gif")); //$NON-NLS-1$ //$NON-NLS-2$

			Collections.sort(libraries, PluginUtil.IDENTITY_COMPARATOR);
			for(Library library : libraries) {
				node = new DefaultMutableTreeNode(new PluginElementProxy(library));

				// List exported paths
				for(String export : library.getExports())
					node.add(new DefaultMutableTreeNode(export));

				category.add(node);
				nodeMap.put(library, node);
			}

			pluginNode.add(category);
		}

		// Fragments
		List<PluginFragment> fragments = new ArrayList<>(descriptor.getFragments());
		if(!fragments.isEmpty()) {
			category = new DefaultMutableTreeNode(new LabelProxy(
					"plugins.core.pluginExplorerView.labels.fragments", "frgmts_obj.gif")); //$NON-NLS-1$ //$NON-NLS-2$

			Collections.sort(fragments, PluginUtil.IDENTITY_COMPARATOR);
			for(PluginFragment fragment : fragments) {
				newNode = new DefaultMutableTreeNode(new PluginElementProxy(fragment));
				category.add(newNode);
				nodeMap.put(fragment, newNode);
			}

			pluginNode.add(category);
		}

		// Prerequisites
		List<PluginPrerequisite> prerequisites = new ArrayList<>(descriptor.getPrerequisites());
		if(!prerequisites.isEmpty()) {
			category = new DefaultMutableTreeNode(new LabelProxy(
					"plugins.core.pluginExplorerView.labels.prerequisites", "req_plugins_obj.gif")); //$NON-NLS-1$ //$NON-NLS-2$

			Collections.sort(prerequisites, PluginUtil.IDENTITY_COMPARATOR);
			for(PluginPrerequisite prerequisite : prerequisites) {
				category.add(new DefaultMutableTreeNode(new PluginElementProxy(prerequisite)));
			}

			pluginNode.add(category);
		}

		// Extension points
		List<ExtensionPoint> extensionPoints = new ArrayList<>(descriptor.getExtensionPoints());
		if(!extensionPoints.isEmpty()) {
			category = new DefaultMutableTreeNode(new LabelProxy(
					"plugins.core.pluginExplorerView.labels.extensionPoints", "ext_points_obj.gif")); //$NON-NLS-1$ //$NON-NLS-2$

			DefaultMutableTreeNode paramsNode;
			Collections.sort(extensionPoints, PluginUtil.IDENTITY_COMPARATOR);
			for(ExtensionPoint extensionPoint : extensionPoints) {
				node = new DefaultMutableTreeNode(new PluginElementProxy(extensionPoint));
				nodeMap.put(extensionPoint, node);

				if(extensionPoint.getParentPluginId()!=null)
					node.add(new DefaultMutableTreeNode(new LabelProxy(
							"plugins.core.pluginExplorerView.labels.parentPluginId",  //$NON-NLS-1$
							null, extensionPoint.getParentPluginId())));
				if(extensionPoint.getParentExtensionPointId()!=null)
					node.add(new DefaultMutableTreeNode(new LabelProxy(
							"plugins.core.pluginExplorerView.labels.parentPointId",  //$NON-NLS-1$
							null, extensionPoint.getParentExtensionPointId())));

				// Parameter definitions
				// TODO need recursive traversal of sub-parameters!!
				List<ExtensionPoint.ParameterDefinition> paramDefs = new ArrayList<>(
						extensionPoint.getParameterDefinitions());
				if(!paramDefs.isEmpty()) {
					paramsNode = new DefaultMutableTreeNode(new LabelProxy(
							"plugins.core.pluginExplorerView.labels.parameterDefinitions", "prop_ps.gif")); //$NON-NLS-1$ //$NON-NLS-2$
					Collections.sort(paramDefs, PluginUtil.IDENTITY_COMPARATOR);

					for(ExtensionPoint.ParameterDefinition paramDef : paramDefs) {
						newNode = new DefaultMutableTreeNode(new PluginElementProxy(paramDef, extensionPoint));
						paramsNode.add(newNode);
						//elementMap.put(paramDef, newNode);
					}
					node.add(paramsNode);
				}

				category.add(node);
			}

			pluginNode.add(category);
		}

		// Extensions
		List<Extension> extensions = new ArrayList<>(descriptor.getExtensions());
		if(!extensions.isEmpty()) {
			category = new DefaultMutableTreeNode(new LabelProxy(
					"plugins.core.pluginExplorerView.labels.extensions", "extensions_obj.gif")); //$NON-NLS-1$ //$NON-NLS-2$

			DefaultMutableTreeNode paramsNode;
			Collections.sort(extensions, PluginUtil.IDENTITY_COMPARATOR);
			for(Extension extension : extensions) {
				node = new DefaultMutableTreeNode(new PluginElementProxy(extension));
				nodeMap.put(extension, node);

				// Add extended plugin id info
				node.add(new DefaultMutableTreeNode(new LabelProxy(
						"plugins.core.pluginExplorerView.labels.extendedPlugin",  //$NON-NLS-1$
						null, extension.getExtendedPluginId())));
				// Add extended point id info
				node.add(new DefaultMutableTreeNode(new LabelProxy(
						"plugins.core.pluginExplorerView.labels.extendedPoint",  //$NON-NLS-1$
						null, extension.getExtendedPointId())));


				// Parameters
				// TODO need recursive traversal of sub-parameters!!
				List<Extension.Parameter> params = new ArrayList<>(
						extension.getParameters());
				if(!params.isEmpty()) {
					paramsNode = new DefaultMutableTreeNode(new LabelProxy(
							"plugins.core.pluginExplorerView.labels.parameters", "prop_ps.gif")); //$NON-NLS-1$ //$NON-NLS-2$
					Collections.sort(params, PluginUtil.IDENTITY_COMPARATOR);

					for(Extension.Parameter param : params) {
						newNode = new DefaultMutableTreeNode(new PluginElementProxy(param));
						paramsNode.add(newNode);
						//elementMap.put(param, newNode);
					}
					node.add(paramsNode);
				}

				category.add(node);
			}

			pluginNode.add(category);
		}

		parent.add(pluginNode);
	}

	private void showPopup(MouseEvent trigger) {
		if(popupMenu==null) {
			// Create new popup menu

			Options options = new Options();
			popupMenu = getDefaultActionManager().createPopupMenu(
					"plugins.core.pluginExplorerView.popupMenuList", options); //$NON-NLS-1$

			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}

		if(popupMenu!=null) {
			// TODO refresh enabled state of actions

			popupMenu.show(pluginTree, trigger.getX(), trigger.getY());
		}
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		if(pluginTree==null) {
			return;
		}

		UIUtil.expandAll(pluginTree, false);
		pluginTree.expandPath(new TreePath(pluginTree.getModel().getRoot()));
	}

	/**
	 * Accepted commands:
	 * <ul>
	 * <li>{@link Commands#SELECT}</li>
	 * </ul>
	 *
	 * @see de.ims.icarus.plugins.core.View#handleRequest(de.ims.icarus.util.mpi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {

		Object data = message.getData();

		if(data instanceof PluginElementProxy) {
			data = ((PluginElementProxy)data).get();
		}

		if(data==null) {
			return message.unsupportedDataResult(this);
		}

		/*
		 * Handle the 'select' command.
		 * This is done by first looking for a node that serves as
		 * container for the given data object and if such a node
		 * was found its TreePath will be selected.
		 */
		if(Commands.SELECT.equalsIgnoreCase(message.getCommand())) {
			Object mappedNode = nodeMap.get(data);

			if(mappedNode!=null && mappedNode instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) mappedNode;

				TreePath path = new TreePath(node.getPath());

				pluginTree.expandPath(path);
				pluginTree.setSelectionPath(path);
				pluginTree.scrollPathToVisible(path);
			}
		}

		return message.successResult(this, null);
	}

	/**
	 * Overridden to forward focus to the {@code tree}
	 * @see de.ims.icarus.plugins.core.View#focusView()
	 */
	@Override
	public void focusView() {
		if(pluginTree!=null) {
			pluginTree.requestFocusInWindow();
		}
	}

	@Override
	protected void refreshInfoPanel(InfoPanel infoPanel) {
		infoPanel.addLabel("selectedItem"); //$NON-NLS-1$
		infoPanel.addSeparator();
		infoPanel.addLabel("totalPlugins", 70); //$NON-NLS-1$
		infoPanel.addSeparator();
		infoPanel.addLabel("enabledPlugins", 70); //$NON-NLS-1$
		infoPanel.addSeparator();
		infoPanel.addLabel("activePlugins", 70); //$NON-NLS-1$
		infoPanel.addGap(100);

		Object item = getSelectedObject();
		if(item!=null) {
			infoPanel.displayText("selectedItem", item.toString()); //$NON-NLS-1$
		}

		showPluginInfo();
	}

	private void showPluginInfo() {
		InfoPanel infoPanel = getInfoPanel();
		if(infoPanel==null) {
			return;
		}

		// Total plug-ins
		String text = ResourceManager.getInstance().get(
				"plugins.core.pluginExplorerView.labels.totalPlugins",  //$NON-NLS-1$
				PluginUtil.pluginCount());
		infoPanel.displayText("totalPlugins", text); //$NON-NLS-1$

		// Enabled plug-ins
		text = ResourceManager.getInstance().get(
				"plugins.core.pluginExplorerView.labels.enabledPlugins",  //$NON-NLS-1$
				PluginUtil.countEnabled());
		infoPanel.displayText("enabledPlugins", text); //$NON-NLS-1$

		// Active plug-ins
		text = ResourceManager.getInstance().get(
				"plugins.core.pluginExplorerView.labels.activePlugins",  //$NON-NLS-1$
				PluginUtil.countActive());
		infoPanel.displayText("activePlugins", text); //$NON-NLS-1$
	}

	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}

		ActionManager actionManager = getDefaultActionManager();
		actionManager.addHandler("plugins.core.pluginExplorerView.activatePluginAction",  //$NON-NLS-1$
				callbackHandler, "activatePlugin"); //$NON-NLS-1$

		actionManager.addHandler("plugins.core.pluginExplorerView.deactivatePluginAction",  //$NON-NLS-1$
				callbackHandler, "deactivatePlugin"); //$NON-NLS-1$

		actionManager.addHandler("plugins.core.pluginExplorerView.checkIntegrityAction",  //$NON-NLS-1$
				callbackHandler, "checkIntegrity"); //$NON-NLS-1$

		actionManager.addHandler("plugins.core.pluginExplorerView.viewElementAction",  //$NON-NLS-1$
				callbackHandler, "viewElement"); //$NON-NLS-1$

		actionManager.addHandler("plugins.core.pluginExplorerView.openHierarchyViewAction",  //$NON-NLS-1$
				callbackHandler, "openHierarchyView"); //$NON-NLS-1$

		actionManager.addHandler("plugins.core.pluginExplorerView.openDirectoryAction",  //$NON-NLS-1$
				callbackHandler, "openDirectory"); //$NON-NLS-1$

		actionManager.addHandler("plugins.core.pluginExplorerView.viewManifestAction",  //$NON-NLS-1$
				callbackHandler, "viewManifest"); //$NON-NLS-1$

		actionManager.addHandler("plugins.core.pluginExplorerView.copyElementAction",  //$NON-NLS-1$
				callbackHandler, "copyElement"); //$NON-NLS-1$

		actionManager.addHandler("plugins.core.pluginExplorerView.copyElementNameAction",  //$NON-NLS-1$
				callbackHandler, "copyElementName"); //$NON-NLS-1$

		actionManager.addHandler("plugins.core.pluginExplorerView.showPluginAction",  //$NON-NLS-1$
				callbackHandler, "showPlugin"); //$NON-NLS-1$
	}

	private void refreshActions(Object[] path) {
		if(pluginTree==null) {
			return;
		}
		if(path==null) {
			path = PluginExplorerUtil.getObjectPath(pluginTree.getSelectionPath());
		}
		if(path==null || path.length==0) {
			return;
		}

		PluginDescriptor descriptor = PluginExplorerUtil.getPluginDescriptor(path);

		Object item = null;
		if(path!=null) {
			item = path[path.length-1];
			if(item instanceof PluginElementProxy)
				item = ((PluginElementProxy)item).get();
		}

		PluginManager pluginManager = PluginUtil.getPluginManager();
		ActionManager actionManager = getDefaultActionManager();
		// Open Hierarchy Action
		actionManager.setEnabled(item instanceof ExtensionPoint,
				"plugins.core.pluginExplorerView.openHierarchyViewAction");  //$NON-NLS-1$
		// Activate Plugin Action
		actionManager.setEnabled(descriptor!=null && !pluginManager.isPluginActivated(descriptor),
				"plugins.core.pluginExplorerView.activatePluginAction");  //$NON-NLS-1$
		// Deactivate Plugin Action
		actionManager.setEnabled(
				descriptor!=null && pluginManager.isPluginActivated(descriptor)
				&& !IcarusCorePlugin.PLUGIN_ID.equals(descriptor.getId()),
				"plugins.core.pluginExplorerView.deactivatePluginAction");  //$NON-NLS-1$
		// Check Integrity Action
		actionManager.setEnabled(descriptor!=null,
				"plugins.core.pluginExplorerView.checkIntegrityAction");  //$NON-NLS-1$
		// View Element Action
		actionManager.setEnabled(item instanceof PluginElement,
				"plugins.core.pluginExplorerView.viewElementAction");  //$NON-NLS-1$
		// View MemberManifest Action
		actionManager.setEnabled(descriptor!=null,
				"plugins.core.pluginExplorerView.viewManifestAction");  //$NON-NLS-1$
		// Show Plug-in Action
		actionManager.setEnabled(
				(item instanceof Extension) || (item instanceof Extension.Parameter)
					|| (item instanceof PluginPrerequisite),
				"plugins.core.pluginExplorerView.showPluginAction");  //$NON-NLS-1$
		// Open Directory Action
		actionManager.setEnabled(
				(item instanceof PluginDescriptor) || (item instanceof PluginFragment)
					|| (item instanceof Library),
				"plugins.core.pluginExplorerView.openDirectoryAction");  //$NON-NLS-1$

	}

	private Object getSelectedObject() {
		Object[] path = getSelectionPath();
		if(path==null || path.length==0) {
			return null;
		}

		return path[path.length-1];
	}

	private Object[] getSelectionPath() {
		if(pluginTree==null) {
			return null;
		}

		TreePath selectionPath = pluginTree.getSelectionPath();
		if(selectionPath==null) {
			return null;
		}

		return PluginExplorerUtil.getObjectPath(selectionPath);
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public final class CallbackHandler implements ClipboardOwner {

		private CallbackHandler() {
			// no-op
		}

		public void activatePlugin(ActionEvent e) {
			Object[] selectionPath = getSelectionPath();
			if(selectionPath==null) {
				return;
			}
			PluginDescriptor descriptor = PluginExplorerUtil.getPluginDescriptor(selectionPath);
			if(descriptor==null) {
				return;
			}

			PluginManager pluginManager = PluginUtil.getPluginManager();

			if(pluginManager.isPluginActivated(descriptor)) {
				return;
			}

			if(pluginManager.isBadPlugin(descriptor)) {
				DialogFactory.getGlobalFactory().showWarning(getContainer(),
						"plugins.core.pluginExplorerView.identity.name",  //$NON-NLS-1$
						"plugins.core.pluginExplorerView.dialogs.badPlugin",  //$NON-NLS-1$
						descriptor.getId());
				return;
			}

			try {
				pluginManager.activatePlugin(descriptor.getId());
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to activate plug-in: "+descriptor.getId(), ex); //$NON-NLS-1$

				DialogFactory.getGlobalFactory().showError(getContainer(),
						"plugins.core.pluginExplorerView.identity.name",  //$NON-NLS-1$
						"plugins.core.pluginExplorerView.dialogs.activationFailed",  //$NON-NLS-1$
						descriptor.getId(), ex.getMessage());
			}
		}

		public void deactivatePlugin(ActionEvent e) {
			Object[] selectionPath = getSelectionPath();
			if(selectionPath==null) {
				return;
			}
			PluginDescriptor descriptor = PluginExplorerUtil.getPluginDescriptor(selectionPath);
			if(descriptor==null) {
				return;
			}

			PluginManager pluginManager = PluginUtil.getPluginManager();

			if(!pluginManager.isPluginActivated(descriptor)) {
				return;
			}

			try {
				pluginManager.deactivatePlugin(descriptor.getId());
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to deactivate plug-in: "+descriptor.getId(), ex); //$NON-NLS-1$

				DialogFactory.getGlobalFactory().showError(getContainer(),
						"plugins.core.pluginExplorerView.identity.name",  //$NON-NLS-1$
						"plugins.core.pluginExplorerView.dialogs.deactivationFailed",  //$NON-NLS-1$
						descriptor.getId(), ex.getMessage());
			}
		}

		public void checkIntegrity(ActionEvent e) {
			// TODO
		}

		public void viewElement(ActionEvent e) {
			// TODO
		}

		public void openHierarchyView(ActionEvent e) {
			Object selectedObject = getSelectedObject();

			// Send extension-point to hierarchy view
			if(selectedObject instanceof ExtensionPoint) {
				Options options = new Options(
						Options.CONTENT_TYPE, ContentTypeRegistry.getInstance().getTypeForClass(ExtensionPoint.class));
				Message message = new Message(this, Commands.DISPLAY, selectedObject, options);
				ResultMessage result = sendRequest(
						ManagementConstants.EXTENSION_POINT_HIERARCHY_VIEW_ID, message);

				if(result.getThrowable()!=null) {
					LoggerFactory.log(this, Level.SEVERE,
							"Failed to send extension-point to hierarchy view", result.getThrowable()); //$NON-NLS-1$
				}
			}
		}

		/*
		 * Allowed elements:
		 * 	PluginDescriptor
		 * 	PluginFragment
		 * 	Library
		 */
		public void openDirectory(ActionEvent e) {
			if(!Desktop.isDesktopSupported()) {
				DialogFactory.getGlobalFactory().showWarning(getContainer(),
						"plugins.core.pluginExplorerView.identity.name",  //$NON-NLS-1$
						"plugins.core.pluginExplorerView.dialogs.desktopNotSupported");  //$NON-NLS-1$
			}

			Object selectedObject = getSelectedObject();
			if(selectedObject==null) {
				return;
			}

			if(selectedObject instanceof PluginElementProxy) {
				selectedObject = ((PluginElementProxy)selectedObject).get();
				// selectedObject should not be non-null!
			}

			// Get location information
			URL location = null;
			if(selectedObject instanceof PluginDescriptor) {
				location = ((PluginDescriptor)selectedObject).getLocation();
			} else if(selectedObject instanceof PluginFragment) {
				location = ((PluginFragment)selectedObject).getLocation();
			} else if(selectedObject instanceof Library) {
				PluginManager pluginManager = PluginUtil.getPluginManager();
				Library library = (Library)selectedObject;
				location = pluginManager.getPathResolver().resolvePath(library, library.getPath());
			}

			if(location==null) {
				LoggerFactory.log(this, Level.INFO, "Not a valid directory owner: "+selectedObject); //$NON-NLS-1$
				return;
			}

			// Resolve local file
			Path targetFile = null;
			try {
				targetFile = Paths.get(location.toURI());
			} catch (URISyntaxException|FileSystemNotFoundException ex) {
				LoggerFactory.error(this, "Unable to convert url to path: "+location, ex); //$NON-NLS-1$
			}
			// Switch to parent directory if necessary
			if(targetFile!=null && Files.isRegularFile(targetFile)) {
				targetFile = targetFile.getParent();
			}

			if(targetFile==null) {
				LoggerFactory.log(this, Level.FINE, "Unable to resolve file: "+location); //$NON-NLS-1$
				return;
			}

			// Abort with notification if file does not exist
			if(Files.notExists(targetFile)) {
				String path = targetFile.toString();
				if(path.length()>40) {
					path = "..."+path.substring(path.length()-37); //$NON-NLS-1$
				}
				DialogFactory.getGlobalFactory().showWarning(getContainer(),
						"plugins.core.pluginExplorerView.identity.name",  //$NON-NLS-1$
						"plugins.core.pluginExplorerView.dialogs.missingDirectory",  //$NON-NLS-1$
						path);
				return;
			}

			// Delegate opening to Desktop
			try {
				Desktop.getDesktop().open(targetFile.toFile());
			} catch (IOException ex) {
				LoggerFactory.log(this, Level.SEVERE, "Failed to open directory in desktop: "+targetFile, ex); //$NON-NLS-1$
				DialogFactory.getGlobalFactory().showError(getContainer(),
						"plugins.core.pluginExplorerView.identity.name",  //$NON-NLS-1$
						"plugins.core.pluginExplorerView.dialogs.desktopError",  //$NON-NLS-1$
						ex.getMessage());
			}
		}

		public void viewManifest(ActionEvent e) {
			// TODO
		}

		/**
		 * Exports the displayed label of the currently selected
		 * element to the clipboard.
		 */
		public void copyElement(ActionEvent e) {
			Object selectedObject = getSelectedObject();
			if(selectedObject==null) {
				return;
			}

			String text = null;
			// Use the id of PluginElement if possible
			if(selectedObject instanceof PluginElementProxy) {
				PluginElementProxy proxy = (PluginElementProxy)selectedObject;
				text = proxy.getElementLabel();
			}

			// As a fallback just convert the selected object into a string
			if(text==null) {
				text = selectedObject.toString();
			}

			// Send content to clipboard
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			if(clipboard!=null) {
				clipboard.setContents(new StringSelection(text), this);
			}
		}

		/**
		 * Exports the fully qualified unique id of the currently selected
		 * element to the clipboard if a valid element is selected.
		 */
		public void copyElementName(ActionEvent e) {
			Object[] selectionPath = getSelectionPath();
			if(selectionPath==null) {
				return;
			}

			// Build fully qualified name from labels and/or
			// element identifiers separated by '@'
			StringBuilder sb = new StringBuilder();
			for(Object pathElement : selectionPath) {
				if(sb.length()>0) {
					sb.append("@"); //$NON-NLS-1$
				}

				if(pathElement instanceof PluginElementProxy) {
					PluginElementProxy proxy = (PluginElementProxy)pathElement;
					sb.append(proxy.getElementId());
				} else if(pathElement!=null) {
					sb.append(pathElement.toString());
				} else {
					// TODO verify that this only happens at the root node?
					sb.append("pluginRoot"); //$NON-NLS-1$
				}
			}

			// Send content to clipboard
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			if(clipboard!=null) {
				clipboard.setContents(new StringSelection(sb.toString()), this);
			}
		}

		/**
		 * Expands and selects the node holding the declaring
		 * plug-in of the currently selected element
		 */
		public void showPlugin(ActionEvent e) {
			Object selectedObject = getSelectedObject();
			if(selectedObject==null) {
				return;
			}

			if(selectedObject instanceof PluginElementProxy) {
				selectedObject = ((PluginElementProxy)selectedObject).get();
			}

			if(!(selectedObject instanceof PluginElement)) {
				return;
			}

			// Get plug-in descriptor
			PluginDescriptor descriptor = ((PluginElement<?>)selectedObject).getDeclaringPluginDescriptor();

			// Get the node mapped to this item
			Object pluginNode = nodeMap.get(descriptor);
			if(pluginNode==null) {
				// Should never happen, but we better report this inconsistency
				LoggerFactory.log(this, Level.INFO, "Missing element mapping for plugin-descriptor: "+descriptor); //$NON-NLS-1$
				return;
			}

			// Expand the node and select it
			TreePath path = new TreePath(new Object[]{
					pluginTree.getModel().getRoot(), pluginNode});
			pluginTree.expandPath(path);
			pluginTree.setSelectionPath(path);
			pluginTree.scrollPathToVisible(path);
		}

		/**
		 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
		 */
		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			// no-op
		}
	}

	private final class Handler extends MouseAdapter implements TreeSelectionListener, PluginManager.EventListener {

		@Override
		public void valueChanged(TreeSelectionEvent evt) {
			Object[] path = PluginExplorerUtil.getObjectPath(evt.getPath());

			refreshActions(path);

			// Send message to present the selected item
			Object item = (path==null || path.length==0) ? null : path[path.length-1];
			String title = ResourceManager.getInstance().get(
					"plugins.core.pluginExplorerView.outlineTitle"); //$NON-NLS-1$
			Options options = new Options(
					"reuseTab", true, //$NON-NLS-1$
					"owner", PluginExplorerView.this, //$NON-NLS-1$
					"title", title); //$NON-NLS-1$

			InfoPanel infoPanel = getInfoPanel();
			if(infoPanel!=null) {
				infoPanel.displayText("selectedItem", item.toString()); //$NON-NLS-1$
			}

			fireBroadcastEvent(new EventObject(
					ManagementConstants.EXPLORER_SELECTION_CHANGED,
					"path", path, "item", item, "options", options)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		private void maybeShowPopup(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO verify that we really need to fire click event
			/*JTree tree = (JTree) e.getSource();
			TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
			if (treePath != null) {

				// Notify listeners about click
				Object[] path = getObjectPath(treePath);
				eventSource.fireEvent(new EventObject(PluginExplorerEvents.ITEM_CLICKED,
						"path", path, "clickCount", e.getClickCount())); //$NON-NLS-1$ //$NON-NLS-2$
			}*/

			maybeShowPopup(e);
		}

		private void refreshPluginIcon(PluginDescriptor descriptor) {
			Object pluginNode = nodeMap.get(descriptor);

			if(pluginNode==null) {
				LoggerFactory.log(this, Level.INFO, "Missing element mapping for plugin-descriptor: "+descriptor); //$NON-NLS-1$
				return;
			}

			Object[] path = null;
			if(pluginNode instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)pluginNode;
				path = node.getPath();
				pluginNode = node.getUserObject();
			}

			if(pluginNode instanceof PluginElementProxy) {
				((PluginElementProxy)pluginNode).refresh();
			}

			if(path!=null) {
				Rectangle bounds = pluginTree.getPathBounds(new TreePath(path));
				if(bounds!=null) {
					pluginTree.repaint(bounds);
				}
			}
			pluginTree.clearSelection();
		}

		/**
		 * @see org.java.plugin.PluginManager.EventListener#pluginActivated(org.java.plugin.Plugin)
		 */
		@Override
		public void pluginActivated(Plugin plugin) {
			refreshPluginIcon(plugin.getDescriptor());
			showPluginInfo();
		}

		/**
		 * @see org.java.plugin.PluginManager.EventListener#pluginDeactivated(org.java.plugin.Plugin)
		 */
		@Override
		public void pluginDeactivated(Plugin plugin) {
			refreshPluginIcon(plugin.getDescriptor());
			showPluginInfo();
		}

		/**
		 * @see org.java.plugin.PluginManager.EventListener#pluginDisabled(org.java.plugin.registry.PluginDescriptor)
		 */
		@Override
		public void pluginDisabled(PluginDescriptor descriptor) {
			showPluginInfo();
		}

		/**
		 * @see org.java.plugin.PluginManager.EventListener#pluginEnabled(org.java.plugin.registry.PluginDescriptor)
		 */
		@Override
		public void pluginEnabled(PluginDescriptor descriptor) {
			showPluginInfo();
		}
	}
}
