/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;
import javax.swing.undo.UndoManager;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.resources.ResourceDomain;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.actions.Actions;
import net.ikarus_systems.icarus.util.Exceptions;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class UIUtil {
	
	static {
		// Disable event consumption by closing popups. This enables
		// selection of JTree nodes when clicking outside the popup
		// TODO: test on other platforms than Win7
        UIManager.put("PopupMenu.consumeEventOnClose", Boolean.FALSE); //$NON-NLS-1$
	}

	/**
	 * 
	 */
	private UIUtil() {
		// no-op
	}

	public static final Color defaultBorderColor = new Color(128, 128, 128);
	
	public static final Border defaultAreaBorder = BorderFactory.createLineBorder(defaultBorderColor, 1, true);
	
	public static final Border topLineBorder = new SeparatingBorder(true, false, false, false);
	
	public static final Border defaultContentBorder = new EmptyBorder(1, 3, 1, 3);
	
	private static Map<String, Icon> blankIcons;
	
	public static Icon getBlankIcon(int width, int height) {
		String key = width+"x"+height; //$NON-NLS-1$
		if(blankIcons==null) {
			blankIcons = new HashMap<>();
		}
		Icon icon = blankIcons.get(key);
		if(icon==null) {
			icon = createBlankIcon(width, height);
			blankIcons.put(key, icon);
		}
		return icon;
	}
	
	public static Icon createBlankIcon(final int width, final int height) {
		return new Icon() {
			
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				// no-op
			}
			
			@Override
			public int getIconWidth() {
				return width;
			}
			
			@Override
			public int getIconHeight() {
				return height;
			}
		};
	}
	
	private static MouseListener rightClickSelectionHandler = new MouseAdapter() {
		
		@Override
		public void mousePressed(MouseEvent e) {
			Object source = e.getSource();
			if(source instanceof JTree) {
				JTree tree = (JTree)source;
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				tree.setSelectionPath(path);
			} else if(source instanceof JList) {
				JList<?> list = (JList<?>) source;
				int index = list.locationToIndex(e.getPoint());
				if(index>-1) {
					list.setSelectedIndex(index);
				} else {
					list.clearSelection();
				}
			}
		}
	};
	
	public static void enableRighClickTreeSelection(JTree tree) {
		Exceptions.testNullArgument(tree, "tree"); //$NON-NLS-1$
		
		tree.addMouseListener(rightClickSelectionHandler);
	}
	
	public static void enableRighClickListSelection(JList<?> list) {
		Exceptions.testNullArgument(list, "list"); //$NON-NLS-1$
		
		list.addMouseListener(rightClickSelectionHandler);
	}
	
	public static int getDirection(String s) {
		if("north".equals(s) || "top".equals(s) || "up".equals(s)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return SwingConstants.NORTH;
		} else if("south".equals(s) || "bottom".equals(s) || "down".equals(s)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return SwingConstants.SOUTH;
		} else if("east".equals(s) || "right".equals(s)) { //$NON-NLS-1$ //$NON-NLS-2$
			return SwingConstants.EAST;
		} else if("west".equals(s) || "left".equals(s)) { //$NON-NLS-1$ //$NON-NLS-2$
			return SwingConstants.WEST;
		} else if("south_west".equals(s) || "bottom_left".equals(s)) { //$NON-NLS-1$ //$NON-NLS-2$
			return SwingConstants.SOUTH_WEST;
		} else if("south_east".equals(s) || "bottom_right".equals(s)) { //$NON-NLS-1$ //$NON-NLS-2$
			return SwingConstants.SOUTH_EAST;
		} else if("north_west".equals(s) || "top_left".equals(s)) { //$NON-NLS-1$ //$NON-NLS-2$
			return SwingConstants.NORTH_WEST;
		} else if("north_east".equals(s) || "top_right".equals(s)) { //$NON-NLS-1$ //$NON-NLS-2$
			return SwingConstants.NORTH_EAST;
		} else if("center".equals(s)) { //$NON-NLS-1$
			return SwingConstants.CENTER;
		} else
			throw new IllegalArgumentException("Invalid direction: "+s); //$NON-NLS-1$
	}
	
	public static void fitToContent(JComboBox<?> comboBox, int minWidth, int maxWidth) {
		ComboBoxUI ui = comboBox.getUI();
		Dimension size = ui.getPreferredSize(comboBox);
		int height = size.height;
		int width = Math.min(maxWidth, Math.max(minWidth, size.width));
		
		Dimension newSize = new Dimension(width, height);
		
		comboBox.setPreferredSize(newSize);
		comboBox.setMinimumSize(newSize);
		comboBox.setMaximumSize(newSize);
	}
	
	public static String toSwingTooltip(String tooltip) {
		if(tooltip==null || tooltip.isEmpty()) {
			return null;
		}
		
		String convertedTooltip = tooltip.replaceAll(
				"\\n\\r|\\r\\n|\\n|\\r", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
		if(convertedTooltip.length()!=tooltip.length()) {
			tooltip = "<html>"+convertedTooltip; //$NON-NLS-1$
		}
		return tooltip;
	}
	
	public static void invokeLater(final Runnable runnable) {
		Exceptions.testNullArgument(runnable, "runnable"); //$NON-NLS-1$
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					runnable.run();
				} catch(Throwable t) {
					LoggerFactory.log(UIUtil.class, Level.SEVERE, "Error while executing runnable on event dispatch thread", t); //$NON-NLS-1$
				}
			}
		});
	}
	
	public static void invokeAndWait(final Runnable runnable) throws InvocationTargetException, InterruptedException {
		Exceptions.testNullArgument(runnable, "runnable"); //$NON-NLS-1$
		
		SwingUtilities.invokeAndWait(new Runnable() {
			
			@Override
			public void run() {
				try {
					runnable.run();
				} catch(Throwable t) {
					LoggerFactory.log(UIUtil.class, Level.SEVERE, "Error while executing runnable on event dispatch thread", t); //$NON-NLS-1$
				}
			}
		});
	}

	// Resizing methods

	/** */
	public static <C extends Component> C resizeComponent(C comp,
			Dimension dimension) {
		comp.setMinimumSize(dimension);
		comp.setPreferredSize(dimension);
		comp.setMaximumSize(dimension);
		return comp;
	}

	public static <C extends Component> C resizeComponent(C comp,
			Integer width, Integer height) {
		Dimension dimension = new Dimension(width, height);
		return resizeComponent(comp, dimension);
	}

	public static <C extends Component> C resizeComponent(C comp,
			Dimension min, Dimension pref, Dimension max) {
		comp.setMinimumSize(min);
		comp.setPreferredSize(pref);
		comp.setMaximumSize(max);
		return comp;
	}

	public static <C extends Component> C resizeComponent(C comp,
			Integer minWidth, Integer prefWidth, Integer maxWidth,
			Integer minHeight, Integer prefHeight, Integer maxHeight) {
		Dimension min = new Dimension(minWidth, minHeight);
		Dimension pref = new Dimension(prefWidth, prefHeight);
		Dimension max = new Dimension(maxWidth, maxHeight);
		return resizeComponent(comp, min, pref, max);
	}

	public static <C extends Component> C resizeIfNeccessary(C comp,
			Dimension dim) {
		if (comp.getSize().width > dim.width)
			dim.width = comp.getSize().width;
		if (comp.getSize().height > dim.height)
			dim.height = comp.getSize().height;
		return resizeComponent(comp, dim);
	}
	
	public static void expandAll(JTree tree, boolean expand) {
		Exceptions.testNullArgument(tree, "tree"); //$NON-NLS-1$
		
		tree.cancelEditing();
	    Object root = tree.getModel().getRoot();
	    expandAll0(tree, new TreePath(root), expand);
	}
	 
	private static void expandAll0(JTree tree, TreePath parent, boolean expand) {
	    // Traverse children
	    Object node = parent.getLastPathComponent();
	    int childCount = tree.getModel().getChildCount(node); 
	    if (childCount>0) {
	        for (int i=0; i<childCount; i++) {
	            Object child = tree.getModel().getChild(node, i);
	            TreePath path = parent.pathByAddingChild(child);
	            expandAll0(tree, path, expand);
	        }
	    }
	    // Expansion or collapse must be done bottom-up
	    if (expand) {
	        tree.expandPath(parent);
	    } else {
	        tree.collapsePath(parent);
	    }
	}

	/** */
	public static <C extends Component> C unsizeComponent(C comp) {
		comp.setMinimumSize(null);
		comp.setPreferredSize(null);
		comp.setMaximumSize(null);
		return comp;
	}
	
	public static boolean equalsInsets(Insets a, Insets b) {
		if((a==null) != (b==null))
			return false;
		
		return a==null ? b==null : a.top==b.top && a.left==b.left 
				&&a.bottom==b.bottom && a.right==b.right;
	}

	public static void feedPopupMenu(JPopupMenu menu, Actions actions, String... items) {
		boolean allowSep = true;
		Action action;
		
		for (String item : items) {
			if (item == null && allowSep) {
				menu.addSeparator();
				allowSep = false;
			} else if (item != null && (action=actions.getAction(item)) != null) {
				menu.add(action);
				allowSep = true;
			}
		}
	}

	public static void feedToolBar(JToolBar toolBar, Actions actions, String... items) {
		boolean allowSep = true;
		Action action;
		
		for (String item : items) {
			if (item == null && allowSep) {
				toolBar.addSeparator();
				allowSep = false;
			} else if (item != null && (action=actions.getAction(item)) != null) {
				toolBar.add(action);
				allowSep = true;
			}
		}
	}

	public static void feedMenu(JMenu menu, Actions actions, String... items) {
		boolean allowSep = true;
		Action action;
		for (String item : items) {
			if (item == null && allowSep) {
				menu.addSeparator();
				allowSep = false;
			} else if (item != null && (action=actions.getAction(item)) != null) {
				menu.add(action);
				allowSep = true;
			}
		}
	}
	
	public static void feedButtonGroup(ButtonGroup group, JToolBar toolBar, Actions actions, String...items) {
		JToggleButton b;
		Action action;
		
		for (int i=0; i<items.length; i++) {
			String item = items[i];
			
			if(item==null) {
				// allow separator only "inside" the list
				if(i>0 && i<items.length-1)
					toolBar.addSeparator();
			} else if((action=actions.getAction(item)) != null) {
				b = new JToggleButton(action);
				b.setHideActionText(true);
				b.setFocusable(false);
				
				group.add(b);
				toolBar.add(b);
			}
		}
	}
	
	public static void feedButtonGroup(ButtonGroup group, JMenu menu, Actions actions, String...items) {
		JToggleButton b;
		Action action;
		
		for (int i=0; i<items.length; i++) {
			String item = items[i];
			
			if(item==null) {
				// allow separator only "inside" the list
				if(i>0 && i<items.length-1)
					menu.addSeparator();
			} else if((action=actions.getAction(item)) != null) {
				b = new JToggleButton(action);
				b.setHideActionText(true);
				b.setFocusable(false);
				
				group.add(b);
				menu.add(b);
			}
		}
	}

	public static void addHint(final JTextComponent component,
			final ResourceDomain resourceDomain, final String hint) {
		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				String hintText = resourceDomain==null ? null : resourceDomain.get(hint);  
				// remove input hint for the editpane and allow for user input
				if (component.getText().trim().equals(hintText)) {
					component.setText(""); //$NON-NLS-1$
				}
			}
		};
		
		component.addMouseListener(mouseListener);
		component.putClientProperty("hint:mouseListener", mouseListener); //$NON-NLS-1$

		FocusListener focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!e.isTemporary()) {
					// show input hint for the editpane if no user input was
					// made
					if (component.getText().trim().equals("")) { //$NON-NLS-1$
						String hintText = resourceDomain==null ? null : resourceDomain.get(hint);
						component.setText(hintText);
					}
				}
			}
		};
		
		component.addFocusListener(focusListener);
		component.putClientProperty("hint:focusListener", focusListener); //$NON-NLS-1$
	}

	/** */
	public static void changeFocus(final Component target, final Component source) {
		invokeLater(new Runnable() {
			public void run() {
				target.dispatchEvent(new FocusEvent(source,	FocusEvent.FOCUS_GAINED));
			}
		});
	}

	/** */
	public static void alignToOwner(Component owner, Component comp) {
		Point p = owner.getLocation();
		p.x += owner.getWidth() * 0.5 - comp.getWidth() * 0.5;
		p.y += owner.getHeight() * 0.5 - comp.getHeight() * 0.5;
		comp.setLocation(p);
	}

	/** */
	public static <C extends Component> C centerComponent(C comp) {
		Dimension d = comp.getSize();
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		// GraphicsDevice gd = ge.getDefaultScreenDevice();
		// DisplayMode dm = gd.getDisplayMode();
		Point p = ge.getCenterPoint();
		comp.setLocation(p.x - (int) (d.width * 0.5), p.y
				- (int) (d.height * 0.5));
		return comp;
	}

	/** */
	public static <C extends Component> C centerComponent(C comp,
			GraphicsConfiguration gc) {
		Dimension d = comp.getSize();
		Rectangle r = gc.getBounds();
		Point p = new Point(r.x + (int) (r.width * 0.5), r.y
				+ (int) (r.height * 0.5));
		comp.setLocation(p.x - (int) (d.width * 0.5), p.y
				- (int) (d.height * 0.5));
		return comp;
	}
	
	private static class UndecoratedTabbedPaneUI extends BasicTabbedPaneUI {
		@Override
		protected void installDefaults() {
			super.installDefaults();
			contentBorderInsets = new Insets(0, 0, 0, 0);
			tabAreaInsets = new Insets(2, 2, 2, 2);
		}
	}
	
	public static void defaultHideTabbedPaneDecoration(JTabbedPane tabbedPane) {
		tabbedPane.setUI(new UndecoratedTabbedPaneUI());
		tabbedPane.setBorder(defaultAreaBorder);
		// Prevent focus border on tabs being drawn
		tabbedPane.setFocusable(false);
	}
	
	private static class UndecoratedSplitPaneUI extends BasicSplitPaneUI {
		public BasicSplitPaneDivider createDefaultDivider() {
			return new BasicSplitPaneDivider(this) {

				private static final long serialVersionUID = -9149206851193508390L;

				@Override
				public void setBorder(Border b) {
				}
			};
		}
	}
	
	public static void defaultHideSplitPaneDecoration(JSplitPane splitPane) {
		splitPane.setUI(new UndecoratedSplitPaneUI());
		splitPane.setDividerSize(4);
		splitPane.setBorder(null);
	}
	
	public static void disableHtml(Object item) {
		if(item instanceof JComponent) {
			JComponent comp = (JComponent) item;
			comp.putClientProperty("html.disable", Boolean.TRUE); //$NON-NLS-1$
		}
	}
	
	public static final int DEFAULT_SCROLL_UNIT_INCREMENT = 16;
	
	public static final void defaultSetUnitIncrement(Object obj) {
		if(obj instanceof JScrollPane) {
			((JScrollPane)obj).getHorizontalScrollBar().setUnitIncrement(DEFAULT_SCROLL_UNIT_INCREMENT);
			((JScrollPane)obj).getVerticalScrollBar().setUnitIncrement(DEFAULT_SCROLL_UNIT_INCREMENT);
		} else if(obj instanceof JScrollBar) {
			((JScrollBar)obj).setUnitIncrement(DEFAULT_SCROLL_UNIT_INCREMENT);
		}
	}

	public static final Border DUMMY_BORDER = BorderFactory.createEmptyBorder(
			2, 2, 2, 2);

	public static final Border LOWERED_DUMMY_BORDER = BorderFactory
			.createBevelBorder(BevelBorder.LOWERED);

	public static final Border RAISED_DUMMY_BORDER = BorderFactory
			.createBevelBorder(BevelBorder.RAISED);

	public static final Border FLAT_BUTTON_BORDER = new FlatButtonBorder();
	
	public static class IconlessButton extends JButton {

		private static final long serialVersionUID = -780096198896775331L;

		public IconlessButton() {
			super();
		}
		
		public IconlessButton(Action a) {
			super(a);
		}
		
		@Override
		public final Icon getIcon() {
			return null;
		}
		
		@Override
		public final void setIcon(Icon i) {
			// do nothing
		}
	}
	
	public static class PlainToolBar extends JToolBar {

		private static final long serialVersionUID = 4560421380746793566L;
		
		public PlainToolBar() {
			setFloatable(false);
			setRollover(true);
		}

		protected JButton createActionComponent(Action a) {
			JButton b = super.createActionComponent(a);
			b.setFocusable(false);
			
			return b;
		}
	}
	
	public static class SeparatingBorder implements Border {
		
		private final Insets insets;
		
		public SeparatingBorder(boolean top, boolean right, boolean bottom, boolean left) {
			insets = new Insets(
					top ? 1 : 0, left ? 1 : 0, 
					bottom ? 1 : 0, right ? 1 :0);
		}
		
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width,
				int height) {
			g.setColor(defaultBorderColor);
			
			if(insets.top>0)
				g.drawLine(x+1, y, x+width-2, y);
			if(insets.right>0)
				g.drawLine(x+width-1, y+1, x+width-1, y+height-2);
			if(insets.left>0)
				g.drawLine(x, y+1, x, y+height-2);
			if(insets.bottom>0)
				g.drawLine(x+1, y+height-1, x+width-2, y+height-1);
		}
		
		@Override
		public boolean isBorderOpaque() {
			return false;
		}
		
		@Override
		public Insets getBorderInsets(Component c) {
			return insets;
		}
	};

	public static class FlatButtonBorder extends AbstractBorder {

		private static final long serialVersionUID = 8756755116840451541L;

		@Override
		public Insets getBorderInsets(Component c, Insets insets) {
			insets.set(2, 2, 2, 2);

			return insets;
		}

		@Override
		public boolean isBorderOpaque() {
			return true;
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			if (c instanceof AbstractButton) {
				ButtonModel bm = ((AbstractButton) c).getModel();
				if (!bm.isEnabled())
					DUMMY_BORDER.paintBorder(c, g, x, y, width, height);
				else if (bm.isPressed())
					LOWERED_DUMMY_BORDER.paintBorder(c, g, x, y, width, height);
				else if (bm.isArmed() || bm.isRollover())
					RAISED_DUMMY_BORDER.paintBorder(c, g, x, y, width, height);
				else
					DUMMY_BORDER.paintBorder(c, g, x, y, width, height);
			} else {
				DUMMY_BORDER.paintBorder(c, g, x, y, width, height);
			}
		}
	}

	public static UndoManager createUndoSupport(JTextComponent comp, int limit) {
		CompoundUndoManager undoManager = new CompoundUndoManager(comp);

		comp.getActionMap().put("undo", undoManager.getUndoAction()); //$NON-NLS-1$
		comp.getActionMap().put("redo", undoManager.getRedoAction()); //$NON-NLS-1$

		comp.getInputMap().put(KeyStroke.getKeyStroke("ctrl Z"), "undo"); //$NON-NLS-1$ //$NON-NLS-2$
		comp.getInputMap().put(KeyStroke.getKeyStroke("ctrl Y"), "redo"); //$NON-NLS-1$ //$NON-NLS-2$

		comp.putClientProperty("undoManager", undoManager); //$NON-NLS-1$

		return undoManager;
	}

	public static class HistoryAction extends AbstractAction {

		private static final long serialVersionUID = 806521843933852614L;

		protected final boolean undo;
		protected final UndoManager undoManager;
		protected HistoryAction complement;

		public HistoryAction(boolean undo, UndoManager undoManager) {
			this.undo = undo;
			this.undoManager = undoManager;

			if (undo) {
				putValue(Action.SMALL_ICON, IconRegistry.getGlobalRegistry().getIcon("undo_edit.gif")); //$NON-NLS-1$
				putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
						KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
			} else {
				putValue(Action.SMALL_ICON, IconRegistry.getGlobalRegistry().getIcon("redo_edit.gif")); //$NON-NLS-1$
				putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
						KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
			}

			refreshEnabled();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (undo) {
				undoManager.undo();
			} else {
				undoManager.redo();
			}

			refreshEnabled();
			if (complement != null)
				complement.refreshEnabled();
		}

		public void refreshEnabled() {
			setEnabled(undo ? undoManager.canUndo() : undoManager.canRedo());
		}

		/**
		 * @return the complement
		 */
		public HistoryAction getComplement() {
			return complement;
		}

		/**
		 * @param complement
		 *            the complement to set
		 */
		public void setComplement(HistoryAction complement) {
			this.complement = complement;
		}
	}

	public static JPopupMenu createDefaultTextMenu(JTextComponent comp,
			boolean allowUndo) {
		JPopupMenu menu = new JPopupMenu();

		ActionMap actionMap = comp.getActionMap();

		new TextAction(DefaultEditorKit.cutAction, comp);
		new TextAction(DefaultEditorKit.copyAction, comp);
		new TextAction(DefaultEditorKit.pasteAction, comp);
		new TextAction(DefaultEditorKit.selectAllAction, comp);
		new TextAction("clear", comp); //$NON-NLS-1$

		if (allowUndo && comp.getClientProperty("undoManager") != null) { //$NON-NLS-1$
			menu.add(actionMap.get("undo")); //$NON-NLS-1$
			menu.add(actionMap.get("redo")); //$NON-NLS-1$
			menu.addSeparator();
		}

		menu.add(actionMap.get(DefaultEditorKit.cutAction));
		menu.add(actionMap.get(DefaultEditorKit.copyAction));
		menu.add(actionMap.get(DefaultEditorKit.pasteAction));
		menu.addSeparator();
		menu.add(actionMap.get(DefaultEditorKit.selectAllAction));
		menu.add(actionMap.get("clear")); //$NON-NLS-1$

		return menu;
	}

	public static final void addPopupMenu(JComponent comp, JPopupMenu menu) {
		comp.putClientProperty("popupMenu", menu); //$NON-NLS-1$
		comp.addMouseListener(popupListener);
	}

	public static final MouseListener popupListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger() && e.getComponent() instanceof JComponent) {
				JPopupMenu menu = (JPopupMenu) ((JComponent) e.getComponent())
						.getClientProperty("popupMenu"); //$NON-NLS-1$
				if (menu != null) {
					menu.show(e.getComponent(), e.getX(), e.getY());
					e.consume();
				}
			}
		}
	};

	/**
	 * Action supporting the copy, paste, cut and selectAll operations with
	 * localization awareness and dynamic enabled-checks regarding the state of
	 * a designated JTextComponent
	 * 
	 * @author Markus Gärtner
	 * 
	 */
	public static class TextAction extends AbstractAction implements
			DocumentListener, PropertyChangeListener, CaretListener {

		private static final long serialVersionUID = 373159386962227762L;

		protected final String actionID;

		protected final JTextComponent component;

		public TextAction(String actionID, JTextComponent component) {
			this.actionID = actionID;
			this.component = component;

			if (DefaultEditorKit.cutAction.equals(actionID)) {
				// CUT
				putValue(Action.SMALL_ICON, IconRegistry.getGlobalRegistry().getIcon("cut_edit.gif")); //$NON-NLS-1$
				putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
				putValue(Action.ACCELERATOR_KEY, KeyStroke
						.getKeyStroke("ctrl X")); //$NON-NLS-1$
			} else if (DefaultEditorKit.copyAction.equals(actionID)) {
				// COPY
				putValue(Action.SMALL_ICON, IconRegistry.getGlobalRegistry().getIcon("copy_edit.gif")); //$NON-NLS-1$
				putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
				putValue(Action.ACCELERATOR_KEY, KeyStroke
						.getKeyStroke("ctrl C")); //$NON-NLS-1$
			} else if (DefaultEditorKit.pasteAction.equals(actionID)) {
				// PASTE
				putValue(Action.SMALL_ICON, IconRegistry.getGlobalRegistry().getIcon("paste_edit.gif")); //$NON-NLS-1$
				putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
				putValue(Action.ACCELERATOR_KEY, KeyStroke
						.getKeyStroke("ctrl V")); //$NON-NLS-1$
			} else if (DefaultEditorKit.selectAllAction.equals(actionID)) {
				// SELECT ALL
				putValue(Action.SMALL_ICON, IconRegistry.getGlobalRegistry().getIcon("task_obj.gif")); //$NON-NLS-1$
				putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
				putValue(Action.ACCELERATOR_KEY, KeyStroke
						.getKeyStroke("ctrl A")); //$NON-NLS-1$
			} else if("clear".equals(actionID)) { //$NON-NLS-1$
				// CLEAR ALL
				putValue(Action.SMALL_ICON, IconRegistry.getGlobalRegistry().getIcon("clear_co.gif")); //$NON-NLS-1$
				putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
				putValue(Action.ACCELERATOR_KEY, KeyStroke
						.getKeyStroke("ctrl N")); //$NON-NLS-1$
			}

			ResourceManager.getInstance().getGlobalDomain().prepareAction(this, actionID, null);
			ResourceManager.getInstance().getGlobalDomain().addAction(this);

			component.addPropertyChangeListener("document", this); //$NON-NLS-1$
			component.getDocument().addDocumentListener(this);
			component.addCaretListener(this);

			refreshEnabled();

			component.getActionMap().put(actionID, this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (DefaultEditorKit.selectAllAction.equals(actionID)) {
				Document doc = component.getDocument();
				component.setCaretPosition(0);
				component.moveCaretPosition(doc.getLength());
			} else if (DefaultEditorKit.cutAction.equals(actionID)) {
				component.cut();
			} else if (DefaultEditorKit.copyAction.equals(actionID)) {
				component.copy();
			} else if (DefaultEditorKit.pasteAction.equals(actionID)) {
				component.paste();
			} else if ("clear".equals(actionID)) { //$NON-NLS-1$
				Document doc = component.getDocument();
				try {
					doc.remove(0, doc.getLength());
				} catch (BadLocationException e1) {
					// no-op
				}
			}
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			refreshEnabled();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			changedUpdate(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			changedUpdate(e);
		}

		public void refreshEnabled() {
			boolean empty = component.getText().isEmpty();
			boolean selected = false;
			try {
				selected = component.getSelectedText() != null;
			} catch (IllegalArgumentException e) {
				// ignore
				// e.printStackTrace();
			}

			if (DefaultEditorKit.selectAllAction.equals(actionID)) {
				setEnabled(!empty);
			} else if (DefaultEditorKit.pasteAction.equals(actionID)) {
				setEnabled(true);
			} else {
				setEnabled(!empty && selected);
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			((Document) evt.getOldValue()).removeDocumentListener(this);
			((Document) evt.getNewValue()).addDocumentListener(this);
		}

		@Override
		public void caretUpdate(CaretEvent e) {
			refreshEnabled();
		}
	}
}
