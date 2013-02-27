/**
 * 
 */
package net.ikarus_systems.icarus.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.util.Exceptions;

/**
 * @author Markus Gärtner
 *
 */
public class CompoundMenuButton extends JButton {
	
	private static final long serialVersionUID = 523383068784557675L;
	
	protected JPopupMenu menu;
	
	protected OpenMenuButton openButton;
	
	protected int orientation = SwingUtilities.HORIZONTAL;
	
	protected Action currentAction;
	
	protected boolean openButtonActive = false;
	
	protected PropertyChangeListener actionPropertyChangeListener;
	
	protected ActionListener actionListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			AbstractButton button = (AbstractButton) e.getSource();
			
			if(button==CompoundMenuButton.this) {
				if(currentAction!=null && !openButtonActive)
					currentAction.actionPerformed(e);
			} else
				CompoundMenuButton.this.setSelectedAction(button.getAction());
		}
	};
	
	protected ChangeListener changeListener = new ChangeListener() {
		
		protected boolean isStateAdjusting = false;

		@Override
		public void stateChanged(ChangeEvent e) {
			if(isStateAdjusting)
				return;
			
			isStateAdjusting = true;
			try {
				AbstractButton source = (AbstractButton) e.getSource();
				AbstractButton target = source==CompoundMenuButton.this ? 
						openButton : CompoundMenuButton.this;
				
				target.getModel().setArmed(source.getModel().isArmed());
				target.getModel().setPressed(source.getModel().isPressed());
				target.getModel().setRollover(source.getModel().isRollover());
				
				if(source==openButton && openButtonActive && source.getModel().isPressed()) {
					if(orientation==SwingUtilities.HORIZONTAL)
						menu.show(CompoundMenuButton.this, 0, 
								CompoundMenuButton.this.getHeight());
					else
						menu.show(openButton, 
								CompoundMenuButton.this.getWidth(), 0);
				}
			} finally {
				isStateAdjusting = false;
			}
		}
	};
	
	protected MouseListener mouseListener = new MouseAdapter() {

		@Override
		public void mouseEntered(MouseEvent e) {
			openButtonActive = true;
		}

		@Override
		public void mouseExited(MouseEvent e) {
			openButtonActive = false;
		}
	};
	
	public CompoundMenuButton(Action...actions) {
		this(0, SwingUtilities.HORIZONTAL, actions);
	}
	
	public CompoundMenuButton(int selectedIndex, Action...actions) {
		this(selectedIndex, SwingUtilities.HORIZONTAL, actions);
	}
	
	public CompoundMenuButton(int selectedIndex, int orientationValue, Action...actions) {
		orientation = orientationValue;
		
		openButton = this.new OpenMenuButton();
		openButton.addChangeListener(changeListener);
		openButton.addMouseListener(mouseListener);

		menu = new JPopupMenu();
		
		setFocusable(false);
		setFocusPainted(false);
		setHideActionText(true);
		
		addActionListener(actionListener);
		addChangeListener(changeListener);
				
		setActions(actions);
		setSelectedAction(selectedIndex);
	}
	
	public void setActions(Action...actions) {
		Exceptions.testNullArgument(actions, "actions"); //$NON-NLS-1$
		menu.removeAll();
		
		JMenuItem item;
		for(Action action : actions) {
			 item = menu.add(action);
			 //item.setHideActionText(true);
			 item.addActionListener(actionListener);
		}
		
		openButton.setEnabled(isEnabled() && getActionCount()>0);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		openButton.setEnabled(enabled && getActionCount()>0);
		super.setEnabled(enabled);
	}
	
	public int getActionCount() {
		return menu.getComponentCount();
	}
	
	protected void setSelectedAction(Action action) {
		if(currentAction==action)
			return;
		
		if(currentAction!=null)
			currentAction.removePropertyChangeListener(actionPropertyChangeListener);
		
		currentAction = action;
		
		if(currentAction!=null) {
			configurePropertiesFromAction(currentAction);
			
            actionPropertyChangeListener = 
            	createActionPropertyChangeListener(action);
            currentAction.addPropertyChangeListener(actionPropertyChangeListener);
		}
	}
	
	public void setSelectedAction(int index) {
		AbstractButton button;
		synchronized (menu.getTreeLock()) {
			button = (AbstractButton) menu.getComponent(index);
		}
		setSelectedAction(button.getAction());
	}
	
	public Action getSelectedAction() {
		return getAction();
	}
	
	public AbstractButton getOpenButton() {
		return openButton;
	}
	
	public void setOrientation(int orientation) {
		if(orientation!=this.orientation) {
			this.orientation = orientation;
			
			openButton.repaint();
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 *
	 */
	protected class OpenMenuButton extends JButton {

		private static final long serialVersionUID = -7670943112973052450L;
		
		protected OpenMenuButton() {
			setFocusable(false);
			setFocusPainted(false);
		}
		
		protected Dimension prepareDimension(Dimension dim) {
			if(orientation==SwingUtilities.HORIZONTAL)
				dim.width = dim.width/2;
			else
				dim.height = dim.height/2;
			
			return dim;
		}
		
		@Override
		public Dimension getPreferredSize() {
			return prepareDimension(CompoundMenuButton.this.getPreferredSize());
		}
		
		@Override
		public Dimension getMinimumSize() {
			return prepareDimension(CompoundMenuButton.this.getMinimumSize());
		}
		
		@Override
		public Dimension getMaximumSize() {
			return prepareDimension(CompoundMenuButton.this.getMaximumSize());
		}
		
		@Override
		public Dimension getSize() {
			return prepareDimension(CompoundMenuButton.this.getSize());
		}
		
		@Override
		public int getHeight() {
			return orientation==SwingUtilities.HORIZONTAL ? 
					CompoundMenuButton.this.getHeight() :
						CompoundMenuButton.this.getHeight()/2;
		}
		
		@Override
		public int getWidth() {
			return orientation==SwingUtilities.HORIZONTAL ? 
					CompoundMenuButton.this.getWidth()/2 :
						CompoundMenuButton.this.getWidth();
		}
		
		@Override
		public String getText() {
			return null;
		}
		
		@Override
		public Icon getIcon() {
			return null;
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Graphics2D g2 = (Graphics2D)g;
			
			int x = getWidth()/2;
			int y = getHeight()/2;
			
			g.setColor(isEnabled() ? Color.black : Color.gray);
			if(orientation==SwingUtilities.HORIZONTAL) {
				y--;
				
				g2.fillPolygon(new int[]{x-3, x+3, x}, 
						new int[]{y, y, y+3}, 3);
			} else {
				x--;
				
				g2.fillPolygon(new int[]{x, x, x+3}, 
						new int[]{y-3, y+3, y}, 3);
			}
		}
	}
}
