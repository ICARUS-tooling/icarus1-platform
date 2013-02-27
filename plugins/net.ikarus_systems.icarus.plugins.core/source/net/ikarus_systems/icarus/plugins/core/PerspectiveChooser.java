/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.util.id.ExtensionIdentity;
import net.ikarus_systems.icarus.util.id.Identity;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PerspectiveChooser {
	
	private Filter filter;
	private Extension selectedPerspective;
	private ChangeListener changeListener;

	public PerspectiveChooser(ChangeListener changeListener, Filter filter) {
		if(changeListener==null)
			throw new IllegalArgumentException("Invalid change listener"); //$NON-NLS-1$
		
		this.changeListener = changeListener;
		this.filter = filter;
	}

	public PerspectiveChooser(ChangeListener changeListener) {
		this(changeListener, null);
	}
	
	public void init(JComponent container) {
		PluginDescriptor descriptor = PluginUtil.getPluginRegistry()
				.getPluginDescriptor(IcarusCorePlugin.PLUGIN_ID);
		
		ExtensionPoint extensionPoint = descriptor.getExtensionPoint("Perspective"); //$NON-NLS-1$
		List<Extension> connectedExtensions = new ArrayList<>(
				extensionPoint.getConnectedExtensions());
		
		if(connectedExtensions.isEmpty()) {
			JTextArea info = new JTextArea(ResourceManager.getInstance().get(
					"plugins.core.perspectiveChooser.noPerspectives")); //$NON-NLS-1$
			info.setBackground(container.getBackground());
			info.setEditable(false);
			info.setWrapStyleWord(true);
			info.setLineWrap(true);
			info.setBorder(new EmptyBorder(50, 50, 50, 50));
			container.setLayout(new BorderLayout());
			container.add(info, BorderLayout.CENTER);
			return;
		}
		
		Collections.sort(connectedExtensions, PluginUtil.IDENTITY_COMPARATOR);
		
		// Selection panels for perspectives
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
		int rows = Math.max(1, connectedExtensions.size()/2);
		infoPanel.setLayout(new GridLayout(rows, 2, 5, 10));
		for(Extension extension : connectedExtensions) {
			infoPanel.add(new PerspectivePanel(extension).getPanel());
		}
		if(connectedExtensions.size()<2) {
			infoPanel.add(new JLabel());
		}
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(infoPanel, BorderLayout.NORTH);
		contentPanel.add(Box.createGlue(), BorderLayout.CENTER);
		JScrollPane scrollPane = new JScrollPane(contentPanel);
		scrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		
		// Header label
		JLabel header = new JLabel(ResourceManager.getInstance().get(
				"plugins.core.perspectiveChooser.title")); //$NON-NLS-1$
		header.setHorizontalAlignment(SwingConstants.CENTER);
		header.setBorder(new EmptyBorder(20, 30, 30, 30));
		header.setFont(header.getFont().deriveFont(Font.BOLD, 19));
		
		// Arrange components
		container.removeAll();	
		container.setLayout(new BorderLayout());
		container.add(header, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		container.add(Box.createGlue(), BorderLayout.SOUTH);
	}
	
	private void setSelectedPerspective(Extension selectedPerspective) {
		if(selectedPerspective==null)
			throw new IllegalArgumentException("Invalid perspective extension"); //$NON-NLS-1$
		
		this.selectedPerspective = selectedPerspective;
		
		UIUtil.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				changeListener.stateChanged(new ChangeEvent(PerspectiveChooser.this));
			}
		});
	}
	
	/**
	 * @return the filter
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
	/**
	 * @return the selectedPerspective
	 */
	public Extension getSelectedPerspective() {
		return selectedPerspective;
	}

	private static Border perspectivePorder = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.blue, 1, true),
			BorderFactory.createEmptyBorder(2, 5, 2, 5));
	private static Border previewPorder = BorderFactory.createLineBorder(Color.black, 1);
	
	private class PerspectivePanel implements ActionListener {

		private final Extension extension;
		private final JPanel panel;

		/**
		 * @param extension
		 */
		private PerspectivePanel(Extension extension) {
			panel = new JPanel(new BorderLayout());
			
			if(extension==null)
				throw new IllegalArgumentException("Invalid extension"); //$NON-NLS-1$
			this.extension = extension;
			
			Identity identity = new ExtensionIdentity(extension);
			
			panel.setBorder(perspectivePorder);
			
			// Header
			JLabel header = new JLabel(identity.getName());
			header.setToolTipText(identity.getName()+" ("+identity.getId()+")");  //$NON-NLS-1$//$NON-NLS-2$
			header.setFont(header.getFont().deriveFont(Font.BOLD));
			header.setIcon(identity.getIcon());
			header.setBorder(new EmptyBorder(3, 10, 2, 5));
			
			// Preview
			JLabel preview = new JLabel();
			preview.setBorder(previewPorder);
			preview.setVerticalAlignment(SwingConstants.TOP);
			Extension.Parameter param = extension.getParameter("preview"); //$NON-NLS-1$
			if(param!=null) {
				try {
					ClassLoader loader = PluginUtil.getPluginManager().getPluginClassLoader(
							extension.getDeclaringPluginDescriptor());
					URL location = loader.getResource(param.valueAsString());
					Image image = ImageIO.read(location);
					// Wait for image to be fully loaded
					int height, width;
					while((height=image.getHeight(preview))==-1
							|| (width=image.getWidth(preview))==-1);
					
					image = image.getScaledInstance(
								width>300 ? 300 : -1, 
								height>200 ? 200 : -1, Image.SCALE_SMOOTH);
					
					/*while((height=image.getHeight(preview))==-1
							|| (width=image.getWidth(preview))==-1);*/
					
					Icon icon = new ImageIcon(image);
					preview.setIcon(icon);
					preview.setPreferredSize(new Dimension(
							icon.getIconWidth(), icon.getIconHeight()));
					preview.setMinimumSize(preview.getPreferredSize());
				} catch(Exception e) {
					LoggerFactory.getLogger(PerspectiveChooser.class).log(LoggerFactory.record(
							Level.FINE,	"Unable to load preview-icon: "+param.valueAsString(), e)); //$NON-NLS-1$
					param = null;
				}
			}
			if(param==null) {
				preview.setIcon(null);
				preview.setText(ResourceManager.getInstance().get(
						"plugins.core.perspectiveChooser.noPreview")); //$NON-NLS-1$
				preview.setHorizontalAlignment(SwingConstants.CENTER);
				preview.setVerticalAlignment(SwingConstants.CENTER);
				preview.setPreferredSize(new Dimension(200, 200));
			}
			
			// Description
			String description = identity.getDescription();
			if(description==null || description.isEmpty()) {
				description = ResourceManager.getInstance().get(
						"plugins.core.perspectiveChooser.noDescription"); //$NON-NLS-1$
			}
			JTextArea descriptionArea = new JTextArea(description){

				private static final long serialVersionUID = -3584521432069459903L;

				/**
				 * @see javax.swing.JTextArea#getScrollableTracksViewportWidth()
				 */
				@Override
				public boolean getScrollableTracksViewportWidth() {
					return true;
				}
				
			};
			descriptionArea.setEditable(false);
			descriptionArea.setEnabled(false);
			descriptionArea.setWrapStyleWord(true);
			descriptionArea.setLineWrap(true);
			descriptionArea.setDisabledTextColor(descriptionArea.getForeground());
			descriptionArea.setBorder(new EmptyBorder(3, 3, 3, 3));
			/*JScrollPane scrollPane = new JScrollPane(descriptionArea);
			scrollPane.setBorder(null);*/
			
			// Select-Button
			JButton button = new JButton(ResourceManager.getInstance().get(
						"plugins.core.perspectiveChooser.selectPerspective")); //$NON-NLS-1$
			button.addActionListener(this);
			JPanel footer = new JPanel();
			footer.add(button);
			footer.setBackground(descriptionArea.getBackground());
			
			panel.setBackground(descriptionArea.getBackground());
			
			// Arrange components
			panel.add(header, BorderLayout.NORTH);
			panel.add(preview, BorderLayout.WEST);
			panel.add(descriptionArea, BorderLayout.CENTER);
			//panel.add(scrollPane, BorderLayout.CENTER);
			panel.add(footer, BorderLayout.SOUTH);
		}
		
		JComponent getPanel() {
			return panel;
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			setSelectedPerspective(extension);
		}
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface Filter {
		boolean include(Extension perspectiveExtension);
	}
}
