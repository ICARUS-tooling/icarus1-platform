/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/ui/Alignment.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.ui.tab;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;

import net.ikarus_systems.icarus.resources.ResourceManager;

/**
 * Component to be used as tabComponent; Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ButtonTabComponent extends JPanel implements ChangeListener,
		PropertyChangeListener, ContainerListener {

	private static final long serialVersionUID = -5642829173325085218L;
	private final JTabbedPane pane;
	private final TabButton tabButton;

	public ButtonTabComponent(final JTabbedPane pane) {
		// unset default FlowLayout' gaps
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		if(pane==null)
			throw new IllegalArgumentException("Invalid tabbed pane"); //$NON-NLS-1$

		this.pane = pane;
		setOpaque(false);
		setFocusable(false);

		pane.addPropertyChangeListener("model", this); //$NON-NLS-1$
		pane.addContainerListener(this);
		pane.getModel().addChangeListener(this);

		// make JLabel read titles from JTabbedPane
		JLabel label = new JLabel() {
			private static final long serialVersionUID = -101575414816694292L;

			@Override
			public String getText() {
				int i = pane.indexOfTabComponent(ButtonTabComponent.this);
				return i==-1 ? null : pane.getTitleAt(i);
			}

			@Override
			public Icon getIcon() {
				int i = pane.indexOfTabComponent(ButtonTabComponent.this);
				return i==-1 ? null : pane.getIconAt(i);
			}
		};

		add(label);
		// add more space between the label and the button
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		// tab button
		tabButton = new TabButton();
		add(tabButton);
		// add more space to the top of the component
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	}
	
	protected void checkTabButton() {
		int index = pane.indexOfTabComponent(this);
		tabButton.setVisible(pane.getSelectedIndex() == index);
	}

	@Override
	public void componentAdded(ContainerEvent e) {
		
	}

	@Override
	public void componentRemoved(ContainerEvent e) {
		checkTabButton();
	};

	@Override
	public void stateChanged(ChangeEvent e) {
		checkTabButton();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		((SingleSelectionModel) evt.getOldValue()).removeChangeListener(this);
		((SingleSelectionModel) evt.getNewValue()).addChangeListener(this);
	}

	private class TabButton extends JButton implements ActionListener, MouseListener {

		private static final long serialVersionUID = 7781685681499233316L;

		public TabButton() {
			int size = 17;
			setPreferredSize(new Dimension(size, size));

			ResourceManager.getInstance().getGlobalDomain().prepareComponent(this, null, "close"); //$NON-NLS-1$
			ResourceManager.getInstance().getGlobalDomain().addComponent(this);
			// Make the button looks the same for all Laf's
			setUI(new BasicButtonUI());
			// Make it transparent
			setContentAreaFilled(false);
			// No need to be focusable
			setFocusable(false);
			setBorder(BorderFactory.createEtchedBorder());
			setBorderPainted(false);
			setRolloverEnabled(true);
			
			addMouseListener(this);
			addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int i = pane.indexOfTabComponent(ButtonTabComponent.this);
			if (i != -1) {
				Component comp = pane.getComponentAt(i);
				
				TabController tabController = (TabController) SwingUtilities.getAncestorOfClass(
						TabController.class, comp);
				
				if(tabController!=null) {
					// Let tab controller handle it
					tabController.closeTab(comp);				
				} else {
					
					// Just remove
					pane.remove(i);
				}
			}
		}

		// We don't want to update UI for this button
		@Override
		public void updateUI() {
			// no-op
		}

		// Paint the cross
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			// shift the image for pressed buttons
			if (getModel().isPressed()) {
				g2.translate(1, 1);
			}
			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.BLACK);
			if (getModel().isRollover()) {
				g2.setColor(Color.MAGENTA);
			}
			int delta = 6;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight()
					- delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight()
					- delta - 1);
			g2.dispose();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// not needed
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			setBorderPainted(true);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			setBorderPainted(false);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// not needed
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// not needed
		}
	}
}
