/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.util.Collection;
import java.util.Comparator;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.dialog.BasicDialogBuilder;
import net.ikarus_systems.icarus.util.MutablePrimitives.MutableBoolean;
import net.ikarus_systems.icarus.util.id.Identity;

import org.java.plugin.ObjectFactory;
import org.java.plugin.PathResolver;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.standard.StandardObjectFactory;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class PluginUtil {
	
	public static final String CORE_PLUGIN_ID = "net.ikarus_systems.icarus.core"; //$NON-NLS-1$
	
	private static PluginRegistry pluginRegistry;
	private static PathResolver pathResolver;
	private static PluginManager pluginManager;

	private PluginUtil() {
		// no-op
	}

	public static int countActive() {
		int active = 0;
		
		for(PluginDescriptor descriptor : getPluginRegistry().getPluginDescriptors()) {
			if(getPluginManager().isPluginActivated(descriptor))
				active++;
		}
		
		return active;
	}
	
	public static int countEnabled() {
		int active = 0;
		
		for(PluginDescriptor descriptor : getPluginRegistry().getPluginDescriptors()) {
			if(getPluginManager().isPluginEnabled(descriptor))
				active++;
		}
		
		return active;
	}
	
	public static int pluginCount() {
		return getPluginRegistry().getPluginDescriptors().size();
	}
	
	// prevent multiple deserialization
	private Object readResolve() throws ObjectStreamException {
		throw new NotSerializableException(PluginUtil.class.getName());
	}
	
	// prevent cloning
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	public static void load(Logger logger) throws Exception {
		if(pluginRegistry!=null)
			throw new IllegalStateException("Plug-in registry object already loaded"); //$NON-NLS-1$
		if(pathResolver!=null)
			throw new IllegalStateException("Path resolver object already loaded"); //$NON-NLS-1$
		if(pluginManager!=null)
			throw new IllegalStateException("Plug-in manager object already loaded"); //$NON-NLS-1$
		
		// init plu-gin management objects
		ObjectFactory objectFactory = StandardObjectFactory.newInstance();
		logger.info("Using object factory: "+objectFactory); //$NON-NLS-1$
		
		pluginRegistry = objectFactory.createRegistry();
		logger.info("Using plugin registry: "+pluginRegistry); //$NON-NLS-1$
		
		pathResolver = objectFactory.createPathResolver();
		logger.info("Using path resolver: "+pathResolver); //$NON-NLS-1$
		
		pluginManager = objectFactory.createManager(pluginRegistry, pathResolver);
		logger.info("Using plugin manager: "+pluginManager); //$NON-NLS-1$
	}
	
	public static PluginManager getPluginManager() {
		if(pluginManager==null)
			throw new IllegalStateException("Plug-in manager not yet loaded!"); //$NON-NLS-1$
		
		return pluginManager;
	}

	public static PluginRegistry getPluginRegistry() {
		if(pluginRegistry==null)
			throw new IllegalStateException("Plug-in registry not yet loaded!"); //$NON-NLS-1$
		return pluginRegistry;
	}

	public static PathResolver getPathResolver() {
		if(pathResolver==null)
			throw new IllegalStateException("Path resolver not yet loaded!"); //$NON-NLS-1$
		return pathResolver;
	}
	
	public static PluginDescriptor getCorePlugin() {
		return getPluginRegistry().getPluginDescriptor(CORE_PLUGIN_ID);
	}

	public static final Comparator<org.java.plugin.registry.Identity> IDENTITY_COMPARATOR = new Comparator<org.java.plugin.registry.Identity>() {
	
		@Override
		public int compare(org.java.plugin.registry.Identity o1,
				org.java.plugin.registry.Identity o2) {
			return o1.getId().compareTo(o2.getId());
		}
	
	};
	
	public static final Comparator<Extension> EXTENSION_COMPARATOR = new Comparator<Extension>() {

		@Override
		public int compare(Extension e1, Extension e2) {
			if(e1==e2) {
				return 0;
			}
			
			Identity id1 = ExtensionIdentityCache.getInstance().getIdentity(e1);
			Identity id2 = ExtensionIdentityCache.getInstance().getIdentity(e2);
			
			if(id1!=null && id2!=null) {
				return Identity.COMPARATOR.compare(id1, id2);
			} else {
				return e1.getId().compareTo(e2.getId());
			}
		}
		
	};

	public static Extension findExtension(String pluginId, String extensionPointId, String uid) {
		PluginDescriptor descriptor = getPluginManager().getRegistry().getPluginDescriptor(pluginId);
		if(descriptor==null) {
			return null;
		}
		ExtensionPoint extensionPoint = descriptor.getExtensionPoint(extensionPointId);
		if(extensionPoint==null) {
			return null;
		}
		for(Extension extension : extensionPoint.getConnectedExtensions()) {
			if(extension.getUniqueId().equals(uid)) {
				return extension;
			}
		}
		return null;
	}
	
	public static Extension showExtensionDialog(Component parent, 
			String title, ExtensionPoint extensionPoint, boolean doSort) {
		if(extensionPoint==null)
			throw new IllegalArgumentException("Invalid extension-point"); //$NON-NLS-1$
		
		Collection<Extension> extensions = extensionPoint.getConnectedExtensions();
		// Nothing more to do in case there are no extensions connected
		if(extensions==null ||extensions.isEmpty()) {
			return null;
		}
		
		ExtensionListModel model = new ExtensionListModel(extensions, doSort);
		final JList<Extension> list = new JList<>(model);
		list.setCellRenderer(new ExtensionListCellRenderer());
		
		final MutableBoolean selected = new MutableBoolean(false);
		
		list.addMouseListener(new MouseAdapter() {

			/**
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()!=2) {
					return;
				}
				
				JDialog dialog = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, list);
				if(dialog==null) {
					return;
				}
				
				selected.setValue(true);
				dialog.setVisible(false);
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(250, 200));
		
		BasicDialogBuilder builder = new BasicDialogBuilder(
				ResourceManager.getInstance().getGlobalDomain());
		builder.setTitle(title);
		builder.addMessage(scrollPane);
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		if(!builder.isOkValue() && !selected.getValue()) {
			return null;
		}
		
		return list.getSelectedValue();
	}
}
