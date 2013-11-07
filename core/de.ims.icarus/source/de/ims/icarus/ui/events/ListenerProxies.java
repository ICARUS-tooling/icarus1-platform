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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.events;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.BiDiMap;
import de.ims.icarus.util.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ListenerProxies {
	
	private static Map<Class<?>, Map<Object, Object>> proxies = new HashMap<>();
	
	// Maps interface classes to proxy classes
	private static BiDiMap<Class<?>, Class<?>> proxyClasses = new BiDiMap<>();
	
	private static final Object lock = new Object();
	
	private ListenerProxies() {
		throw new AssertionError();
	}
	
	private static final Class<?>[] constructorParams = {
		InvocationHandler.class,
	};
	
	private static final Set<String> ignoredMethods = CollectionUtils.asSet(
			"toString", //$NON-NLS-1$
			"hashCode", //$NON-NLS-1$
			"equals" //$NON-NLS-1$
	);
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> T getProxy(Class<T> listenerClass, T owner) {
		if(listenerClass==null)
			throw new NullPointerException("Invalid listener class"); //$NON-NLS-1$
		if(owner==null)
			throw new NullPointerException("Invalid owner"); //$NON-NLS-1$
		
		synchronized (lock) {
			Map<Object, Object> proxyMap = proxies.get(listenerClass);
			if(proxyMap==null) {
				proxyMap = new WeakHashMap<>();
				proxies.put(listenerClass, proxyMap);
			}
						
			Object proxy = proxyMap.get(owner);
			
			if(proxy==null) {
				Class<?> proxyClass = proxyClasses.get(listenerClass);
				if(proxyClass==null) {
					proxyClass = Proxy.getProxyClass(listenerClass.getClassLoader(), listenerClass);
					proxyClasses.put(listenerClass, proxyClass);
				}
				
				try {
					ListenerProxy listenerProxy = new ListenerProxy(owner, listenerClass);
					Constructor<?> cs = proxyClass.getConstructor(constructorParams);
					proxy = cs.newInstance(listenerProxy);
					
					proxyMap.put(owner, proxy);
				} catch (Exception e) {
					LoggerFactory.error(ListenerProxies.class, 
							"Unable to instantiate proxy: "+proxyClass, e); //$NON-NLS-1$

					throw new IllegalStateException("Unable to produce proxy for listener class: "+listenerClass, e); //$NON-NLS-1$
				}
			}
			
			return (T) proxy;
		}
	}
//	
//	public static ChangeListener getChangeListenerProxy(ChangeListener owner) {
//		return getProxy(ChangeListener.class, owner);
//	}
//	
//	public static MouseListener getMouseListenerProxy(MouseListener owner) {
//		return getProxy(MouseListener.class, owner);
//	}
//	
//	public static MouseMotionListener getMouseMotionListenerProxy(MouseMotionListener owner) {
//		return getProxy(MouseMotionListener.class, owner);
//	}
//	
//	public static MouseWheelListener getMouseWheelListenerProxy(MouseWheelListener owner) {
//		return getProxy(MouseWheelListener.class, owner);
//	}
//	
//	public static PropertyChangeListener getPropertyChangeListenerProxy(PropertyChangeListener owner) {
//		return getProxy(PropertyChangeListener.class, owner);
//	}
//	
//	public static EventListener getEventListenerProxy(EventListener owner) {
//		return getProxy(EventListener.class, owner);
//	}
//	
//	public static AncestorListener getAncestorListenerProxy(AncestorListener owner) {
//		return getProxy(AncestorListener.class, owner);
//	}
//	
//	public static CaretListener getCaretListenerProxy(CaretListener owner) {
//		return getProxy(CaretListener.class, owner);
//	}
//	
//	public static CellEditorListener getCellEditorListenerProxy(CellEditorListener owner) {
//		return getProxy(CellEditorListener.class, owner);
//	}
//	
//	public static DocumentListener getDocumentListenerProxy(DocumentListener owner) {
//		return getProxy(DocumentListener.class, owner);
//	}
//	
//	public static HyperlinkListener getHyperlinkListenerProxy(HyperlinkListener owner) {
//		return getProxy(HyperlinkListener.class, owner);
//	}
//	
//	public static InternalFrameListener getInternalFrameListenerProxy(InternalFrameListener owner) {
//		return getProxy(InternalFrameListener.class, owner);
//	}
//	
//	public static ListDataListener getListDataListenerProxy(ListDataListener owner) {
//		return getProxy(ListDataListener.class, owner);
//	}
//	
//	public static ListSelectionListener getListSelectionListenerProxy(ListSelectionListener owner) {
//		return getProxy(ListSelectionListener.class, owner);
//	}
//	
//	public static MenuDragMouseListener getMenuDragMouseListenerProxy(MenuDragMouseListener owner) {
//		return getProxy(MenuDragMouseListener.class, owner);
//	}
//	
//	public static MenuKeyListener getMenuKeyListenerProxy(MenuKeyListener owner) {
//		return getProxy(MenuKeyListener.class, owner);
//	}
//	
//	public static MenuListener getMenuListenerProxy(MenuListener owner) {
//		return getProxy(MenuListener.class, owner);
//	}
//	
//	public static MouseInputListener getMouseInputListenerProxy(MouseInputListener owner) {
//		return getProxy(MouseInputListener.class, owner);
//	}
//	
//	public static PopupMenuListener getPopupMenuListenerProxy(PopupMenuListener owner) {
//		return getProxy(PopupMenuListener.class, owner);
//	}
//	
//	public static RowSorterListener getRowSorterListenerProxy(RowSorterListener owner) {
//		return getProxy(RowSorterListener.class, owner);
//	}
//	
//	public static TableColumnModelListener getTableColumnModelListenerProxy(TableColumnModelListener owner) {
//		return getProxy(TableColumnModelListener.class, owner);
//	}
//	
//	public static TableModelListener getTableModelListenerProxy(TableModelListener owner) {
//		return getProxy(TableModelListener.class, owner);
//	}
//	
//	public static TreeExpansionListener getTreeExpansionListenerProxy(TreeExpansionListener owner) {
//		return getProxy(TreeExpansionListener.class, owner);
//	}
//	
//	public static TreeModelListener getTreeModelListenerProxy(TreeModelListener owner) {
//		return getProxy(TreeModelListener.class, owner);
//	}
//	
//	public static TreeSelectionListener getTreeSelectionListenerProxy(TreeSelectionListener owner) {
//		return getProxy(TreeSelectionListener.class, owner);
//	}
//	
//	public static TreeWillExpandListener getTreeWillExpandListenerProxy(TreeWillExpandListener owner) {
//		return getProxy(TreeWillExpandListener.class, owner);
//	}
//	
//	public static UndoableEditListener getUndoableEditListenerProxy(UndoableEditListener owner) {
//		return getProxy(UndoableEditListener.class, owner);
//	}
//	
//	public static ActionListener getActionListenerProxy(ActionListener owner) {
//		return getProxy(ActionListener.class, owner);
//	}
//	
//	public static AdjustmentListener getAdjustmentListenerProxy(AdjustmentListener owner) {
//		return getProxy(AdjustmentListener.class, owner);
//	}
//	
//	public static AWTEventListener getAWTEventListenerProxy(AWTEventListener owner) {
//		return getProxy(AWTEventListener.class, owner);
//	}
//	
//	public static ComponentListener getComponentListenerProxy(ComponentListener owner) {
//		return getProxy(ComponentListener.class, owner);
//	}
//	
//	public static ContainerListener getContainerListenerProxy(ContainerListener owner) {
//		return getProxy(ContainerListener.class, owner);
//	}
//	
//	public static FocusListener getFocusListenerProxy(FocusListener owner) {
//		return getProxy(FocusListener.class, owner);
//	}
//	
//	public static HierarchyBoundsListener getHierarchyBoundsListenerProxy(HierarchyBoundsListener owner) {
//		return getProxy(HierarchyBoundsListener.class, owner);
//	}
//	
//	public static HierarchyListener getHierarchyListenerProxy(HierarchyListener owner) {
//		return getProxy(HierarchyListener.class, owner);
//	}
//	
//	public static InputMethodListener getInputMethodListenerProxy(InputMethodListener owner) {
//		return getProxy(InputMethodListener.class, owner);
//	}
//	
//	public static ItemListener getItemListenerProxy(ItemListener owner) {
//		return getProxy(ItemListener.class, owner);
//	}
//	
//	public static KeyListener getKeyListenerProxy(KeyListener owner) {
//		return getProxy(KeyListener.class, owner);
//	}
//	
//	public static TextListener getTextListenerProxy(TextListener owner) {
//		return getProxy(TextListener.class, owner);
//	}
//	
//	public static WindowFocusListener getWindowFocusListenerProxy(WindowFocusListener owner) {
//		return getProxy(WindowFocusListener.class, owner);
//	}
//	
//	public static WindowListener getWindowListenerProxy(WindowListener owner) {
//		return getProxy(WindowListener.class, owner);
//	}
//	
//	public static WindowStateListener getWindowStateListenerProxy(WindowStateListener owner) {
//		return getProxy(WindowStateListener.class, owner);
//	}
	
	private static Object getSource(Object[] args) {
		if(args==null || args.length==0) {
			return null;
		}
		
		for(Object arg : args) {
			if(arg instanceof EventSource || arg instanceof EventObject) {
				return arg;
			}
		}
		
		return null;
	}
	
	private static void tryRemoveListener(Object target, Object proxy, Class<?> listenerClass) {

		Class<?> proxyClass = proxy.getClass();
		
		if(listenerClass==null)
			throw new IllegalArgumentException("No listener class defined for proxy: "+proxyClass); //$NON-NLS-1$
		
		String methodName = "remove"+listenerClass.getSimpleName(); //$NON-NLS-1$

		try {			
			Method method = target.getClass().getMethod(methodName, listenerClass);
			
			method.invoke(target, proxy);
		} catch (NoSuchMethodException e) {
			LoggerFactory.debug(ListenerProxies.class, 
					"Unable to remove listener via method '"+methodName+"(" //$NON-NLS-1$ //$NON-NLS-2$
							+listenerClass.getSimpleName()+")' on target:"+target.getClass(), e); //$NON-NLS-1$
		} catch (SecurityException | IllegalAccessException
				| InvocationTargetException e) {
			LoggerFactory.error(ListenerProxies.class, 
					"Not allowed to remove listener on target: "+target.getClass(), e); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
			LoggerFactory.error(ListenerProxies.class, 
					"Listener argument rejected on target: "+target.getClass(), e); //$NON-NLS-1$
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 */
	static class ListenerProxy implements InvocationHandler {

		private final int hash;
		private final Reference<Object> ref;
		private final Class<?> listenerClass;
		
		private boolean dead = false;
		
		public ListenerProxy(Object owner, Class<?> listenerClass) {
			if(owner==null)
				throw new NullPointerException("Invalid owner"); //$NON-NLS-1$
			if(listenerClass==null)
				throw new NullPointerException("Invalid listener claass"); //$NON-NLS-1$
			
			ref = new WeakReference<>(owner);
			hash = 31 + owner.hashCode();
			this.listenerClass = listenerClass;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return getClass().hashCode() * hash;
		}
		
		private Object getOwner() {
			return ref.get();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj!=null && getClass().equals(obj.getClass())) {
				ListenerProxy other = (ListenerProxy) obj;
				return getOwner()==other.getOwner();
			}
			return false;
		}
		
		private void die() {
			dead = true;
		}
		
		private boolean isDead() {
			return dead;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("[ListenerProxy: class=%s owner=%s]",  //$NON-NLS-1$
					listenerClass.getName(), getOwner());
		}

		/**
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			// ENsure that several significant methods will be redirected
			// to this proxy object instead of its owner!
			if(ignoredMethods.contains(method.getName())) {
				return method.invoke(this, args);
			}
			
			// Early fail if owner is dead and we failed to unregister our
			// listener proxy
			if(isDead()) {
				return null;
			}
			
			Object owner = getOwner();
			
			if(owner==null) {
				die();
				
				Object source = getSource(args);
				if(source!=null) {
					System.out.println("Owner dead, unregistering");
					tryRemoveListener(source, proxy, listenerClass);
				}
				
				return null;
			}
			
			return method.invoke(owner, args);
		}
	}
}
