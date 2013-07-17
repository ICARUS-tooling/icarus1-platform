/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.layout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.resources.Localizer;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.Alignment;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.id.Identifiable;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultAreaLayout implements AreaLayout {
	
	public static final String REQUIRES_TAB_PROPERTY = "requiresTab"; //$NON-NLS-1$

	protected JComponent root;
	
	protected JComponent mainContainer;
	
	protected Map<Alignment, List<JComponent>> areaMap;
	protected Map<Alignment, JComponent> areaContainers;
	protected Map<Alignment, JSplitPane> splitPanes;
	
	protected JComponent dummy;
	protected JComponent maximizedArea;
	
	protected ChangeListener containerWatcher;
	protected Localizer tabLocalizer;
	
	protected Comparator<JComponent> componentSorter;
	
	public DefaultAreaLayout() {
		// no-op
	}
	
	protected JComponent getDummy() {
		if(dummy==null) {
			dummy = new JLabel();
		}
		return dummy;
	}
	
	protected boolean isMaximizedState() {
		return maximizedArea!=null && getDummy().getParent()!=null;
	}

	/**
	 * @see de.ims.icarus.ui.layout.AreaLayout#doLayout()
	 */
	@Override
	public void doLayout() {
		
		// Clear maximized state first
		if(isMaximizedState()) {
			toggle(maximizedArea);
		}

		/*
		 * Layout:
		 * 
		 *   +------------------------+
		 *   |          TOP           |
		 *   +----+--------------+----+
		 *   |    |              | R  |
		 *   | L  |              | I  |
		 *   | E  |   CENTER     | G  |
		 *   | F  |              | H  |
		 *   | T  |              | T  |
		 *   |    +--------------+----+
		 *   |    |      BOTTOM       |
		 *   +----+-------------------+
		 *   
		 *   Areas that get merged when one of them is empty
		 *   (they contain a JSplitPane in between when both
		 *   are non-empty):
		 *   
		 *   CENTER + RIGHT = CENTER
		 *   CENTER + BOTTOM = CENTER
		 *   LEFT + CENTER = CENTER
		 *   TOP + CENTER = CENTER
		 *   
		 *   Final CENTER area will be assigned main container
		 *   and added to the given root component
		 */
		
		JComponent leftContainer = getAreaContainer(Alignment.LEFT);
		JComponent rightContainer = getAreaContainer(Alignment.RIGHT);
		JComponent centerContainer = getAreaContainer(Alignment.CENTER);
		JComponent topContainer = getAreaContainer(Alignment.TOP);
		JComponent bottomContainer = getAreaContainer(Alignment.BOTTOM);
		
		configureContainer(leftContainer);
		configureContainer(rightContainer);
		configureContainer(centerContainer);
		configureContainer(topContainer);
		configureContainer(bottomContainer);
		
		// Split center and right if necessary
		if(centerContainer!=null && rightContainer!=null) {
			JSplitPane splitPane = getSplitPane(Alignment.RIGHT);
			splitPane.setLeftComponent(centerContainer);
			splitPane.setRightComponent(rightContainer);
			centerContainer = splitPane;
		} else if(centerContainer==null)
			centerContainer = rightContainer;
		
		// Split center and bottom if necessary
		if(centerContainer!=null && bottomContainer!=null) {
			JSplitPane splitPane = getSplitPane(Alignment.BOTTOM);
			splitPane.setLeftComponent(centerContainer);
			splitPane.setRightComponent(bottomContainer);
			centerContainer = splitPane;
		} else if(centerContainer==null)
			centerContainer = bottomContainer;
		
		// Split left and center if necessary
		if(centerContainer!=null && leftContainer!=null) {
			JSplitPane splitPane = getSplitPane(Alignment.LEFT);
			splitPane.setLeftComponent(leftContainer);
			splitPane.setRightComponent(centerContainer);
			centerContainer = splitPane;
		} else if(centerContainer==null)
			centerContainer = leftContainer;
		
		// Split top and center if necessary
		if(centerContainer!=null && topContainer!=null) {
			JSplitPane splitPane = getSplitPane(Alignment.TOP);
			splitPane.setLeftComponent(topContainer);
			splitPane.setRightComponent(centerContainer);
			centerContainer = splitPane;
		} else if(centerContainer==null)
			centerContainer = topContainer;
		
		if(mainContainer==centerContainer) {
			return;
		}
		if(mainContainer!=null) {
			root.remove(mainContainer);
		}
		
		mainContainer = centerContainer;
		root.add(mainContainer, BorderLayout.CENTER);
	}
	
	protected JSplitPane getSplitPane(Alignment alignment) {
		if(splitPanes==null) {
			splitPanes = new HashMap<>();
		}
		
		JSplitPane splitPane = splitPanes.get(alignment);
		if(splitPane==null) {
			splitPane = new JSplitPane();
			splitPane.setContinuousLayout(true);
			
			splitPane.setOrientation(
					alignment==Alignment.LEFT || alignment==Alignment.RIGHT ? 
							JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT);
			splitPane.setResizeWeight(
					alignment==Alignment.RIGHT || alignment==Alignment.BOTTOM ?
					1d : 0d);
			
			configureSplitPane(splitPane);
			splitPanes.put(alignment, splitPane);
		}
		return splitPane;
	}
	
	protected void destroyContainer(JComponent container) {
		container.removeAll();
		if(container instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) container;
			if(containerWatcher!=null) {
				tabbedPane.removeChangeListener(containerWatcher);
			}
			ResourceManager.getInstance().getGlobalDomain().removeItem(tabbedPane);
		}
	}
	
	protected JComponent getAreaContainer(Alignment alignment) {
		if(areaMap==null) {
			areaMap = new HashMap<>();
		}
		List<JComponent> components = areaMap.get(alignment);
		
		if(areaContainers==null) {
			areaContainers = new HashMap<>();
		}

		JComponent container = areaContainers.get(alignment);
		
		if(components==null || components.isEmpty()) {
			if(container!=null) {
				destroyContainer(container);
			}
			areaContainers.remove(alignment);
			return null;
		}
		
				
		// Allow for hiding of tab if only one component is assigned
		// to a certain display area
		if(components.size()==1 && Boolean.FALSE.equals(
				components.get(0).getClientProperty(REQUIRES_TAB_PROPERTY))) {
			JComponent comp = components.get(0);
			if(container!=null && !(container instanceof JPanel)) {
				destroyContainer(container);
				container = null;
			}
			if(container==null) {
				container = new JPanel(new BorderLayout());
			}
			
			container.add(comp, BorderLayout.CENTER);
			container.setBorder(UIUtil.defaultAreaBorder);
			
			if(containerWatcher!=null) {
				containerWatcher.stateChanged(new ChangeEvent(comp));
			}
			
			areaContainers.put(alignment, container);
			return container;
		} 
		
		// Sort components if required
		if(components.size()>1 && componentSorter!=null) {
			Collections.sort(components, componentSorter);
		}
		
		JTabbedPane tabbedPane = null;
		if(container instanceof JTabbedPane) {
			tabbedPane = (JTabbedPane) container;
		} else if(container!=null) {
			destroyContainer(container);
		}
		if(tabbedPane==null) {
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			configureTabbedPane(tabbedPane);
		}
		
		
		for(JComponent comp : components) {
			//comp.setBorder(UIUtil.topLineBorder);
			
			int index = tabbedPane.getTabCount();
			
			String title = "tab_"+index; //$NON-NLS-1$
			Icon icon = null;
			
			if(comp instanceof Identifiable) {
				Identity identity = ((Identifiable)comp).getIdentity();
				title = identity.getName();
				icon = identity.getIcon();
			}
			
			tabbedPane.insertTab(title, icon, comp, null, index);
			tabbedPane.setTabComponentAt(index, new TabComponent(tabbedPane));
		}
		
		tabbedPane.setSelectedIndex(0);
		
		areaContainers.put(alignment, tabbedPane);
		
		return tabbedPane;
	}

	/**
	 * @see de.ims.icarus.ui.layout.AreaLayout#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		if(root!=null)
			throw new IllegalStateException("Layout already initialized"); //$NON-NLS-1$
		if(container==null)
			throw new IllegalArgumentException("Invalid container"); //$NON-NLS-1$
		
		root = container;
		
		LayoutManager layout = root.getLayout();
		if(!(layout instanceof BorderLayout)) {
			root.setLayout(new BorderLayout());
		}
	}
	
	protected void configureContainer(JComponent container) {
		if(container!=null) {
			container.setMinimumSize(new Dimension(100, 100));
			container.setPreferredSize(new Dimension(250, 150));
		}
	}

	protected void configureSplitPane(JSplitPane splitPane) {
		UIUtil.defaultHideSplitPaneDecoration(splitPane);
	}
	
	protected void configureTabbedPane(JTabbedPane tabbedPane) {
		UIUtil.defaultHideTabbedPaneDecoration(tabbedPane);
		if(containerWatcher!=null) {
			tabbedPane.addChangeListener(containerWatcher);
		}
		if(tabLocalizer!=null) {
			ResourceManager.getInstance().getGlobalDomain().addItem(tabbedPane, tabLocalizer);
		}
	}
	

	/**
	 * @see de.ims.icarus.ui.layout.AreaLayout#add(javax.swing.JComponent, de.ims.icarus.ui.Alignment)
	 */
	@Override
	public void add(JComponent comp, Alignment alignment) {
		if(areaMap==null) {
			areaMap = new HashMap<>();
		}
		
		List<JComponent> componentList = areaMap.get(alignment);
		if(componentList==null) {
			componentList = new ArrayList<>();
			areaMap.put(alignment, componentList);
		}
		componentList.add(comp);
	}

	/**
	 * @see de.ims.icarus.ui.layout.AreaLayout#remove(javax.swing.JComponent)
	 */
	@Override
	public void remove(JComponent comp) {
		if(areaMap==null) {
			return;
		}
		
		for(List<JComponent> componentList : areaMap.values()) {
			componentList.remove(comp);
		}
	}

	/**
	 * @see de.ims.icarus.ui.layout.AreaLayout#toggle(javax.swing.JComponent)
	 */
	@Override
	public void toggle(JComponent comp) {
		Set<JComponent> containers = new HashSet<>(areaContainers.values());
		
		// We only allow toggling of containers, so search for
		// the first container in the component's parent chain
		JComponent container = comp;
		while(!containers.contains(container)) {
			Component parent = container.getParent();
			if(parent==null || !(parent instanceof JComponent))
				throw new IllegalArgumentException("Unable to toggle component: "+comp); //$NON-NLS-1$
			
			container = (JComponent) parent;
		}
		
		try {
			if(maximizedArea==container) {
				minimize(container);
			} else {
				maximize(container);
			}
			focusContainer(container);
		} finally {
			root.revalidate();
			root.repaint();
		}
	}
	
	protected void minimize(JComponent comp) {
		
		// Add the component to its old parent
		JComponent dummy = getDummy();
		Component parent = dummy.getParent();
		if(parent instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) parent;
			int tabIndex = tabbedPane.indexOfComponent(dummy);
			tabbedPane.setComponentAt(tabIndex, comp);
		} else if(parent instanceof JSplitPane) {
			JSplitPane splitPane = (JSplitPane) parent;
			if(splitPane.getLeftComponent()==dummy) {
				splitPane.setLeftComponent(comp);
			} else {
				splitPane.setRightComponent(comp);
			}
		} else {
			JComponent component = (JComponent) parent;
			component.remove(dummy);
			component.add(comp, BorderLayout.CENTER);
		}
		
		try {
			// Now add the old main container to the root component
			root.remove(comp);
			root.add(mainContainer, BorderLayout.CENTER);
		} finally {
			maximizedArea = null;
		}
	}
	
	protected void maximize(JComponent comp) {
		
		// Remove the component from its parent
		Component parent = comp.getParent();
		if(parent instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) parent;
			int tabIndex = tabbedPane.indexOfComponent(comp);
			tabbedPane.setComponentAt(tabIndex, getDummy());
		} else if(parent instanceof JSplitPane) {
			JSplitPane splitPane = (JSplitPane) parent;
			if(splitPane.getLeftComponent()==comp) {
				splitPane.setLeftComponent(getDummy());
			} else {
				splitPane.setRightComponent(getDummy());
			}
		} else {
			JComponent component = (JComponent) parent;
			component.remove(comp);
			component.add(getDummy(), BorderLayout.CENTER);
		}
		
		try {
			// Now add the container to the root component
			root.remove(mainContainer);
			root.add(comp, BorderLayout.CENTER);
		} finally {
			maximizedArea = comp;
		}
	}
	
	protected void focusContainer(Component comp) {
		if(comp instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) comp;
			int tabIndex = tabbedPane.getSelectedIndex();
			if(tabIndex!=-1) {
				comp = tabbedPane.getComponentAt(tabIndex);
			}
		} else if(comp instanceof Container) {
			Container container = (Container) comp;
			synchronized (container.getTreeLock()) {
				if(container.getComponentCount()>0) {
					comp = container.getComponent(0);
				}
			}
		}
		
		comp.requestFocusInWindow();
	}

	/**
	 * @see de.ims.icarus.ui.layout.AreaLayout#setContainerWatcher(javax.swing.event.ChangeListener)
	 */
	@Override
	public void setContainerWatcher(ChangeListener listener) {
		containerWatcher = listener;
		// TODO refresh listener on all tab components?
	}

	/**
	 * @see de.ims.icarus.ui.layout.AreaLayout#setTabLocalizer(de.ims.icarus.resources.Localizer)
	 */
	@Override
	public void setTabLocalizer(Localizer localizer) {
		tabLocalizer = localizer;
		// TODO refresh listener on all tab components?
	}

	public void setComponentSorter(Comparator<JComponent> sorter) {
		this.componentSorter = sorter;
	}
	
	protected class TabComponent extends JLabel implements MouseListener {

		private static final long serialVersionUID = 4521161837142095078L;
		
		private final JTabbedPane tabbedPane;
		
		public TabComponent(JTabbedPane tabbedPane) {
			this.tabbedPane = tabbedPane;
			addMouseListener(this);
		}
		
		public JTabbedPane getTabbedPane() {
			return tabbedPane;
		}
		
		public void select() {
			if(tabbedPane==null) {
				return;
			}
			int tabIndex = tabbedPane.indexOfTabComponent(this);
			if(tabIndex==-1) {
				return;
			}
			tabbedPane.setSelectedIndex(tabIndex);
		}
		
		public Component getTab() {
			if(tabbedPane==null) {
				return null;
			}
			int tabIndex = tabbedPane.indexOfTabComponent(this);
			return tabIndex==-1 ? null : tabbedPane.getComponentAt(tabIndex);
		}
		
		/**
		 * 
		 * @see javax.swing.JLabel#getText()
		 */
		@Override
		public String getText() {
			if(tabbedPane==null) {
				return null;
			}
			int tabIndex = tabbedPane.indexOfTabComponent(this);
			return tabIndex==-1 ? null : tabbedPane.getTitleAt(tabIndex);
		}

		/**
		 * @see javax.swing.JLabel#getIcon()
		 */
		@Override
		public Icon getIcon() {
			if(tabbedPane==null) {
				return null;
			}
			int tabIndex = tabbedPane.indexOfTabComponent(this);
			return tabIndex==-1 ? null : tabbedPane.getIconAt(tabIndex);
		}

		/**
		 * @see javax.swing.JComponent#getToolTipText()
		 */
		@Override
		public String getToolTipText() {
			if(tabbedPane==null) {
				return null;
			}
			int tabIndex = tabbedPane.indexOfTabComponent(this);
			return tabIndex==-1 ? null : tabbedPane.getToolTipTextAt(tabIndex);
		}

		/**
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if(!SwingUtilities.isLeftMouseButton(e)) {
				return;
			}
			
			TabComponent tabComponent = (TabComponent) e.getComponent();
			
			if(e.getClickCount()==1) {
				tabComponent.select();
			} else if(e.getClickCount()==2) {
				toggle(tabComponent.getTabbedPane());
			}
		}

		/**
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			// no-op
		}

		/**
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			// no-op
		}

		/**
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
			// no-op
		}

		/**
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseExited(MouseEvent e) {
			// no-op
		}
	}
}
