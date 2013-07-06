/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.logging.LoggerFactory;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Grouping {
	
	private static Map<Integer, Grouping> groups;
	private static List<WeakReference<ChangeListener>> changeListeners;
	private static final ChangeEvent event = new ChangeEvent(new Object());
	
	private static final Color defaultColor = Color.black;
	private static int maxIndex = -1;
	
	private Color color = defaultColor;
	private final int index;
	
	private static final Object dummy = 0;
	private static WeakHashMap<GroupingPainter, Object> painters;

	private Grouping(int index) {
		this.index = index;
	}

	private Grouping(int index, Color c) {
		this.index = index;
		setColor(c);
	}
	
	public static void addListener(ChangeListener listener) {
		if(changeListeners==null) {
			changeListeners = new ArrayList<>();
		}
		
		changeListeners.remove(listener);
		changeListeners.add(new WeakReference<ChangeListener>(listener));
	}
	
	public static void removeListener(ChangeListener listener) {
		if(changeListeners!=null) {
			changeListeners.remove(listener);
		}
	}
	
	private static void notifyChangeListeners() {
		if(changeListeners==null || changeListeners.isEmpty()) {
			return;
		}
		
		Object[] listeners = changeListeners.toArray();
		for(Object ref : listeners) {
			Object listener = ((WeakReference<?>)ref).get();
			if(listener==null) {
				changeListeners.remove(ref);
			} else {
				((ChangeListener)listener).stateChanged(event);
			}
		}
	}
	
	private static void notifyPainters() {
		if(painters==null || painters.isEmpty()) {
			return;
		}
		
		Object[] items = painters.keySet().toArray();
		for(Object item : items) {
			((GroupingPainter)item).repaint();
		}
	}
	
	private static void reloadConfig(Handle handle) {
		ConfigRegistry config = handle.getSource();
		
		List<?> colors = config.getList(config.getChildHandle(handle, "groupColors")); //$NON-NLS-1$
		
		for(int i=0; i<colors.size(); i++) {
			getGrouping(i).setColor(new Color((Integer) colors.get(i)));
		}
		
		if(groups.size()!=colors.size()) {
			for(Entry<Integer, Grouping> entry : groups.entrySet()) {
				if(entry.getKey()>=colors.size()) {
					entry.getValue().setColor(defaultColor);
				}
			}
		}
		
		notifyChangeListeners();
		notifyPainters();
	}
	
	public static Grouping getGrouping(int index) {
		if(groups==null) {
			groups = new HashMap<>();
		}
		Grouping grouping = groups.get(index);
		if(grouping==null) {
			grouping = new Grouping(index);
			groups.put(index, grouping);
			maxIndex = Math.max(index, maxIndex);
		}
		
		return grouping;
	}
	
	public static int getMaxGroupIndex() {
		return maxIndex;
	}
	
	private void setColor(Color c) {
		if(c==null)
			throw new IllegalArgumentException("Invalid color"); //$NON-NLS-1$
		color = c;
	}

	public Color getColor() {
		return color;
	}
	
	public int getIndex() {
		return index;
	}
	
	static {
		try {
			ConfigRegistry.getGlobalRegistry().addGroupListener(
					"plugins.searchTools", new ConfigListener() { //$NON-NLS-1$
				
				@Override
				public void invoke(ConfigRegistry sender, ConfigEvent event) {
					reloadConfig((Handle) event.getProperty("handle")); //$NON-NLS-1$
				}
			});
			
			reloadConfig(ConfigRegistry.getGlobalRegistry().getHandle(
					"plugins.searchTools")); //$NON-NLS-1$
		} catch(Exception e) {
			LoggerFactory.log(Grouping.class, Level.SEVERE, 
					"Failed to register config listener for grpu colors", e); //$NON-NLS-1$
		}
	}
	
	public static GroupingPainter newGroupingPainter() {
		GroupingPainter painter = new GroupingPainter();
		
		if(painters==null) {
			painters = new WeakHashMap<>();
		}
		painters.put(painter, dummy);
		
		return painter;
	}
	
	public static void setGroupId(Component comp, int groupId) {
		if(comp==null)
			throw new IllegalArgumentException("Invalid component"); //$NON-NLS-1$
		
		JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(
				JScrollPane.class, comp);
		if(scrollPane==null)
			throw new IllegalArgumentException("No enclosing scroll pane available for component: "+comp.toString()); //$NON-NLS-1$
		
		setGroupIds0(scrollPane.getCorner(ScrollPaneConstants.UPPER_LEFT_CORNER), groupId, -1);
		setGroupIds0(scrollPane.getCorner(ScrollPaneConstants.LOWER_LEFT_CORNER), groupId, -1);
	}
	
	public static void setGroupIds(Component comp, int id1, int id2) {
		if(comp==null)
			throw new IllegalArgumentException("Invalid component"); //$NON-NLS-1$
		
		JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(
				JScrollPane.class, comp);
		if(scrollPane==null)
			throw new IllegalArgumentException("No enclosing scroll pane available for component: "+comp.toString()); //$NON-NLS-1$
		
		setGroupIds0(scrollPane.getCorner(ScrollPaneConstants.UPPER_LEFT_CORNER), id1, id2);
		setGroupIds0(scrollPane.getCorner(ScrollPaneConstants.LOWER_LEFT_CORNER), id1, -1);
		setGroupIds0(scrollPane.getCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER), id2, -1);
		setGroupIds0(scrollPane.getCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER), id1, id2);
	}
	
	private static void setGroupIds0(Component comp, int id1, int id2) {
		if(comp instanceof GroupingPainter) {
			((GroupingPainter)comp).setGroupIds(id1, id2);
		}
	}
	
	public static void decorate(JScrollPane scrollPane, boolean dualMode) {
		if(scrollPane==null)
			throw new IllegalArgumentException("Invalid scroll pane"); //$NON-NLS-1$
		
		scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, newGroupingPainter());
		scrollPane.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, newGroupingPainter());
		
		if(dualMode) {
			scrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, newGroupingPainter());
			scrollPane.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, newGroupingPainter());
		}
	}
	
	public static class GroupingPainter extends JPanel {

		private static final long serialVersionUID = 5075094251985115901L;
		
		private int groupId = -1;
		private int secondGroupId = -1;

		@Override
		protected void paintComponent(Graphics g) {
			if(groupId==-1) {
				super.paintComponent(g);
			} else if(secondGroupId==-1) {
				// Only one group to paint
				g.setColor(getGrouping(groupId).getColor());
				g.fillRect(1, 1, getWidth()-2, getHeight()-2);
			} else {
				// Split paintable area
				Graphics2D g2 = (Graphics2D) g;
				int width = getWidth();
				int height = getHeight();

				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				g2.setColor(getGrouping(groupId).getColor());
				g2.fillRect(1, 1, width - 2, height - 2);

				g2.setColor(getGrouping(secondGroupId).getColor());
				g2.fillPolygon(new int[] { 1, width - 1, width - 1 }, 
						new int[] { 1, 1, height - 1 }, 3);
			}
		}
		
		public void setGroupId(int id) {
			groupId = id;
			secondGroupId = -1;
			repaint();
		}
		
		public void setGroupIds(int id1,  int id2) {
			groupId = id1;
			secondGroupId = id2;
			repaint();
		}
		
		public void clearGroupIds() {
			groupId = secondGroupId = -1;
			repaint();
		}
	}
}
