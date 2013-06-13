/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.config.ConfigEvent;
import net.ikarus_systems.icarus.config.ConfigListener;
import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.logging.LoggerFactory;

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
}
