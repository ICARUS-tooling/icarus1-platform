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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;

import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.ExtensionPoint.ParameterDefinition;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ParamaterDefinitionListModel extends AbstractListModel<Object> {

	private static final long serialVersionUID = 8208701507000495338L;

	private ExtensionPoint extensionPoint;
	
	private List<PluginElementProxy> items;
	
	private boolean showInheretedParamaters = false;
	private boolean sortParametersByExtensionPoint = false;
	
	public void reload() {
		if(items==null) {
			items = new ArrayList<>();
		}		
		int size = items.size();
		items.clear();
		
		if(extensionPoint==null) {
			if(size>0) {
				fireIntervalRemoved(this, 0, size-1);
			}
			return;
		}
		
		Collection<ExtensionPoint.ParameterDefinition> parameters = extensionPoint.getParameterDefinitions();
		for(ExtensionPoint.ParameterDefinition param : parameters) {
			if(param.getDeclaringExtensionPoint()==extensionPoint 
					|| showInheretedParamaters) {
				items.add(new PluginElementProxy(param, extensionPoint));
			}
		}
		
		resort();
		
		// If we 'lost' a portion of lines we should notifiy about that
		int newSize = getSize();
		if(newSize>0 && newSize<size) {
			fireIntervalRemoved(this, newSize-1, size-1);
		}
	}
	
	private static Comparator<Object> groupComparator;
	private static Comparator<Object> defaultComparator;
	
	public void resort() {
		if(items==null) {
			return;
		}
		
		Comparator<Object> comparator = null;
		if(sortParametersByExtensionPoint) {
			if(groupComparator==null) {
				groupComparator = new Comparator<Object>() {

					@Override
					public int compare(Object o1, Object o2) {
						ExtensionPoint.ParameterDefinition param1 = 
								(ParameterDefinition) ((PluginElementProxy)o1).get();
						ExtensionPoint.ParameterDefinition param2 = 
								(ParameterDefinition) ((PluginElementProxy)o2).get();
						
						int result = param1.getDeclaringExtensionPoint().getUniqueId().compareTo(
								param2.getDeclaringExtensionPoint().getUniqueId());
						if(result==0) {
							result = param1.getId().compareTo(param2.getId());
						}
						return result;
					}
				};
			}
			comparator = groupComparator;
		}
		
		if(comparator==null) {
			if(defaultComparator==null) {
				defaultComparator = new Comparator<Object>() {

					@Override
					public int compare(Object o1, Object o2) {
						ExtensionPoint.ParameterDefinition param1 = 
								(ParameterDefinition) ((PluginElementProxy)o1).get();
						ExtensionPoint.ParameterDefinition param2 = 
								(ParameterDefinition) ((PluginElementProxy)o2).get();
						
						return param1.getId().compareTo(param2.getId());
					}
				};
			}
			comparator = defaultComparator;
		}
		
		Collections.sort(items, comparator);
		
		fireContentsChanged(this, 0, items.isEmpty() ? 0 : items.size()-1);
	}
	
	public void clear() {
		if(items==null) {
			return;
		}
		int size = getSize();
		items.clear();
		fireIntervalRemoved(this, 0, size-1);
	}

	/**
	 * @return the extensionPoint
	 */
	public ExtensionPoint getExtensionPoint() {
		return extensionPoint;
	}

	/**
	 * @return the showInheretedParamaters
	 */
	public boolean isShowInheretedParamaters() {
		return showInheretedParamaters;
	}

	/**
	 * @return the sortParametersByExtensionPoint
	 */
	public boolean isSortParametersByExtensionPoint() {
		return sortParametersByExtensionPoint;
	}

	/**
	 * @param extensionPoint the extensionPoint to set
	 */
	public void setExtensionPoint(ExtensionPoint extensionPoint) {
		if(this.extensionPoint!=extensionPoint) {
			this.extensionPoint = extensionPoint;
			reload();
		}
	}

	/**
	 * @param showInheretedParamaters the showInheretedParamaters to set
	 */
	public void setShowInheretedParamaters(boolean showInheretedParamaters) {
		if(this.showInheretedParamaters != showInheretedParamaters) {
			this.showInheretedParamaters = showInheretedParamaters;
			reload();
		}
	}

	/**
	 * @param sortParametersByExtensionPoint the sortParametersByExtensionPoint to set
	 */
	public void setSortParametersByExtensionPoint(
			boolean sortParametersByExtensionPoint) {
		if(this.sortParametersByExtensionPoint != sortParametersByExtensionPoint) {
			this.sortParametersByExtensionPoint = sortParametersByExtensionPoint;
			resort();
		}
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return items==null ? 0 : items.size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Object getElementAt(int index) {
		return items==null ? null : items.get(index);
	}
}
