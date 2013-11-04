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
package de.ims.icarus.plugins.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.Core;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.ExtensionListModel;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.GridBagUtil;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.cache.LRUCache;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.xml.jaxb.MapAdapter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PerspectiveChooser {
	
	private JComponent container;
	
	private Filter filter;
	private Extension selectedPerspective;
	private ChangeListener changeListener;
	
	private Extension displayedPerspective;
	
	private PerspectiveUsageStatistic statistics;
	
	private JLabel previewLabel;
	private JLabel headerLabel;
	private JTextArea descriptionArea;
	private JList<Extension> perspectiveList;
	
	private Handler handler;

	public PerspectiveChooser(ChangeListener changeListener, Filter filter) {
		if(changeListener==null)
			throw new NullPointerException("Invalid change listener"); //$NON-NLS-1$
		
		this.changeListener = changeListener;
		this.filter = filter;
	}

	public PerspectiveChooser(ChangeListener changeListener) {
		this(changeListener, null);
	}
	
	private static final String STATISTICS_FILE = "perspectiveUsages.xml"; //$NON-NLS-1$
	
	public void init(JComponent container) {
		// Load statistics
		File file = new File(Core.getCore().getDataFolder(), STATISTICS_FILE);
		if(file.exists()) {
			try {
				JAXBContext context = JAXBContext.newInstance(PerspectiveUsageStatistic.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				statistics = (PerspectiveUsageStatistic)unmarshaller.unmarshal(file);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, "Failed to load usage statistics from file: "+file.getAbsolutePath(), e); //$NON-NLS-1$
			}
		}
		if(statistics==null) {
			statistics = new PerspectiveUsageStatistic();
		}
				
		// Collect available perspectives
		PluginDescriptor descriptor = PluginUtil.getPluginRegistry()
				.getPluginDescriptor(IcarusCorePlugin.PLUGIN_ID);		
		ExtensionPoint extensionPoint = descriptor.getExtensionPoint("Perspective"); //$NON-NLS-1$
		List<Extension> connectedExtensions = new ArrayList<>(
				PluginUtil.findExtensions(extensionPoint, getFilter()));
		
		// Handle (almost impossible) case of no available perspectives
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
		
		// Sort perspectives one way or the other
		if(ConfigRegistry.getGlobalRegistry().getBoolean(
				"general.appearance.sortPerspectivesByStatistics")) { //$NON-NLS-1$
			Collections.sort(connectedExtensions, statistics);
		} else {
			Collections.sort(connectedExtensions, PluginUtil.IDENTITY_COMPARATOR);
		}
		
		handler = new Handler();
		
		Color bg = Color.white; 
		
		perspectiveList = new JList<>(new ExtensionListModel(connectedExtensions, false));
		perspectiveList.setCellRenderer(ExtensionListCellRenderer.getSharedInstance());
		perspectiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		perspectiveList.addListSelectionListener(handler);
		perspectiveList.addMouseListener(handler);
		perspectiveList.setBackground(bg);
		
		JScrollPane spLeft = new JScrollPane(perspectiveList);
		spLeft.setBorder(UIUtil.defaultContentBorder);
		UIUtil.defaultSetUnitIncrement(spLeft);
		spLeft.setBackground(bg);
		
		// Header
		headerLabel = new JLabel();
		headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));
		headerLabel.setBorder(new EmptyBorder(3, 10, 2, 5));
		headerLabel.setBackground(bg);

		// Preview
		previewLabel = new JLabel();
		previewLabel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		previewLabel.setVerticalAlignment(SwingConstants.TOP);
		previewLabel.setBackground(bg);
		previewLabel.setMaximumSize(new Dimension(300, 300));
		
		// Description
		descriptionArea = new JTextArea(){

			private static final long serialVersionUID = -3584521432069459903L;

			/**
			 * @see javax.swing.JTextArea#getScrollableTracksViewportWidth()
			 */
			@Override
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}
			
		};
		UIUtil.disableCaretScroll(descriptionArea);
		descriptionArea.setEditable(false);
		descriptionArea.setEnabled(false);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setLineWrap(true);
		descriptionArea.setDisabledTextColor(descriptionArea.getForeground());
		descriptionArea.setBorder(new EmptyBorder(3, 3, 3, 3));
		descriptionArea.setBackground(bg);

		
		// Select-Button
		JButton button = new JButton(ResourceManager.getInstance().get(
					"plugins.core.perspectiveChooser.selectPerspective")); //$NON-NLS-1$
		button.addActionListener(handler);
		
		JScrollPane spRight = new JScrollPane(descriptionArea);
		spRight.setBorder(null);
		UIUtil.defaultSetUnitIncrement(spRight);
		spRight.setBackground(bg);	
		
		// Arrange stuff
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(headerLabel, GridBagUtil.makeGbcH(0, 0, 2, 1));
		GridBagConstraints gbc = GridBagUtil.gbcTop(GridBagUtil.makeGbc(0, 1), 100);
		gbc.insets = new Insets(10, 10, 10, 10);
		panel.add(previewLabel, gbc);
		panel.add(spRight, GridBagUtil.makeGbcR(1, 1, 1, 1));
		gbc = GridBagUtil.gbcCenter(GridBagUtil.makeGbc(0, 2, 2, 1));
		gbc.insets = new Insets(5, 5, 5, 5);
		panel.add(button, gbc);
		panel.setBackground(bg);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spLeft, panel);
		splitPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.blue, 1, true),
				BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		splitPane.setDividerLocation(220);
		splitPane.setDividerSize(5);
		splitPane.setBackground(bg);
		for(int i=0; i<splitPane.getComponentCount(); i++) {
			Component comp = splitPane.getComponent(i);
			if(comp instanceof BasicSplitPaneDivider) {
				comp.setBackground(Color.blue);
			}
		}
		
		JLabel title = new JLabel();
		ResourceManager.getInstance().getGlobalDomain().prepareComponent(
				title, "plugins.core.perspectiveChooser.selectPerspective", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(title);
		title.setBorder(new EmptyBorder(10, 50, 10, 50));
		title.setFont(Font.decode("Dialog-BOLD-20")); //$NON-NLS-1$
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setVerticalAlignment(SwingConstants.CENTER);
		title.setBackground(bg);
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		contentPanel.add(splitPane, BorderLayout.CENTER);
		contentPanel.add(title, BorderLayout.NORTH);
		contentPanel.setBackground(bg);

		container.setLayout(new BorderLayout());
		container.add(contentPanel, BorderLayout.CENTER);
		
		perspectiveList.setSelectedIndex(0);
		
		this.container = container;
	}
	
	private void setSelectedPerspective(Extension selectedPerspective) {
		if(selectedPerspective==null)
			throw new NullPointerException("Invalid perspective extension"); //$NON-NLS-1$
		
		this.selectedPerspective = selectedPerspective;
		
		statistics.increment(selectedPerspective);
		
		// Save statistics
		TaskManager.getInstance().execute(new Runnable() {
			
			@Override
			public void run() {
				File file = new File(Core.getCore().getDataFolder(), STATISTICS_FILE);
				try {
					JAXBContext context = JAXBContext.newInstance(PerspectiveUsageStatistic.class);
					Marshaller marshaller = context.createMarshaller();
					marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					marshaller.marshal(statistics, file);
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, "Failed to save usage statistics to file: "+file.getAbsolutePath(), e); //$NON-NLS-1$
				}
			}
		});
		
		JLabel label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setIcon(IconRegistry.getGlobalRegistry().getIcon("ajax-loader_32.gif")); //$NON-NLS-1$
		
		container.removeAll();
		container.add(label, BorderLayout.CENTER);
		container.revalidate();
		container.repaint();
		
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
	
	private static Map<Extension, Image> imageCache = new LRUCache<>(20);
	
	private void refreshDetails() {
		if(displayedPerspective==null) {
			return;
		}
		
		Identity identity = PluginUtil.getIdentity(displayedPerspective);

		// Header
		headerLabel.setText(identity.getName());
		headerLabel.setToolTipText(identity.getName()+" ("+identity.getId()+")");  //$NON-NLS-1$//$NON-NLS-2$
		headerLabel.setIcon(identity.getIcon());
		
		// Preview
		Extension.Parameter param = displayedPerspective.getParameter("preview"); //$NON-NLS-1$
		if(param!=null) {
			try {
				Image image = imageCache.get(displayedPerspective);
				if(image==null) {
					ClassLoader loader = PluginUtil.getClassLoader(displayedPerspective);
					URL location = loader.getResource(param.valueAsString());
					image = location==null ? null : ImageIO.read(location);
					
					if(image!=null) {
						imageCache.put(displayedPerspective, image);
					}
				}
				// Wait for image to be fully loaded
				int height, width;
				while((height=image.getHeight(previewLabel))==-1
						|| (width=image.getWidth(previewLabel))==-1);
				
				image = image.getScaledInstance(
							width>300 ? 300 : -1, 
							height>200 ? 200 : -1, Image.SCALE_SMOOTH);
				
				/*while((height=image.getHeight(preview))==-1
						|| (width=image.getWidth(preview))==-1);*/
				
				Icon icon = new ImageIcon(image);
				previewLabel.setIcon(icon);
				previewLabel.setText(null);
				previewLabel.setVerticalAlignment(SwingConstants.TOP);
				previewLabel.setPreferredSize(new Dimension(
						icon.getIconWidth()+1, icon.getIconHeight()+1));
				previewLabel.setMinimumSize(previewLabel.getPreferredSize());
			} catch(Exception e) {
				LoggerFactory.log(this, Level.FINE,	"Unable to load preview-icon: "+param.valueAsString(), e); //$NON-NLS-1$
				param = null;
			}
		}
		if(param==null) {
			previewLabel.setIcon(null);
			previewLabel.setText(ResourceManager.getInstance().get(
					"plugins.core.perspectiveChooser.noPreview")); //$NON-NLS-1$
			previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
			previewLabel.setVerticalAlignment(SwingConstants.CENTER);
			previewLabel.setPreferredSize(new Dimension(200, 200));
		}
		
		// Description
		String description = identity.getDescription();
		if(description==null || description.isEmpty()) {
			description = ResourceManager.getInstance().get(
					"plugins.core.perspectiveChooser.noDescription"); //$NON-NLS-1$
		}
		descriptionArea.setText(description);
	}
	
	private class Handler extends MouseAdapter implements ListSelectionListener, ActionListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
				int index = perspectiveList.locationToIndex(e.getPoint());
				if(index==-1) {
					return;
				}
				
				Extension perspective = perspectiveList.getModel().getElementAt(index);
				
				setSelectedPerspective(perspective);
			}
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(displayedPerspective==null) {
				return;
			}
			
			setSelectedPerspective(displayedPerspective);
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getValueIsAdjusting()) {
				return;
			}
			int index = perspectiveList.getSelectedIndex();
			displayedPerspective = index==-1 ? null : perspectiveList.getModel().getElementAt(index);
			
			refreshDetails();
		}
		
	}

	@XmlRootElement(name="PerspectiveUsageStatistic")
	public static class PerspectiveUsageStatistic implements Comparator<Extension> {
		
		@XmlElement
		@XmlJavaTypeAdapter(value=MapAdapter.class)
		private Map<String, Integer> usageCounts = new HashMap<>();
		
		void increment(Extension extension) {
			String perspectiveId = extension.getUniqueId();
			Integer count = usageCounts.get(perspectiveId);
			if(count==null) {
				count = 0;
			}
			count++;
			usageCounts.put(perspectiveId, count);
		}
		
		int getCount(Extension extension) {
			Integer count = usageCounts.get(extension.getUniqueId());
			return count==null ? 0 : count;
		}

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Extension e1, Extension e2) {
			int result = Integer.compare(getCount(e2), getCount(e1));
			if(result==0) {
				result = PluginUtil.IDENTITY_COMPARATOR.compare(e1, e2);
			}
			return result;
		}
	}
}
