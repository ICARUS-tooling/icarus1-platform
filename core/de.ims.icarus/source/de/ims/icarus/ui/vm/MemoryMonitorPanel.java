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
package de.ims.icarus.ui.vm;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.events.ListenerProxies;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MemoryMonitorPanel extends JPanel implements ChangeListener, ActionListener {

	private static final long serialVersionUID = 8764908795673331760L;
	
	private MemoryLabel label;
	private JButton gcButton;
	
	public MemoryMonitorPanel() {
		super(new BorderLayout());
		
		label = new MemoryLabel();
		label.setPreferredSize(new Dimension(120, 20));
		
		gcButton = new JButton();
		gcButton.setFocusable(false);
		gcButton.setFocusPainted(false);
		gcButton.setRolloverEnabled(true);
		gcButton.setBorder(null);
		gcButton.setPreferredSize(new Dimension(20, 20));
		gcButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("trash.gif")); //$NON-NLS-1$
		
		ResourceManager.getInstance().getGlobalDomain().prepareComponent(gcButton, 
				null, 
				"core.systemMonitorPanel.runGcAction.description"); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(gcButton);
		gcButton.addActionListener(this);
		
		add(label, BorderLayout.CENTER);
		add(gcButton, BorderLayout.EAST);
		
		SystemMonitor.getInstance().addChangeListener(
				ListenerProxies.getProxy(ChangeListener.class, this));
	}
	
	public void close() {
		SystemMonitor.getInstance().removeChangeListener(
				ListenerProxies.getProxy(ChangeListener.class, this));
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		
		setVisible(ConfigRegistry.getGlobalRegistry().getBoolean(
					"general.performance.showMemoryMonitor")); //$NON-NLS-1$
		
		if(isVisible()) {
			label.refresh();
			gcButton.setEnabled(!SystemMonitor.getInstance().isGcRunning());
		}
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		SystemMonitor.getInstance().runGc();
	}
	
	private static class MemoryLabel extends JLabel {
		
		private static final long serialVersionUID = -20960332800674878L;
		private final static BasicStroke dashed = new BasicStroke(1.0f,
		                        BasicStroke.CAP_BUTT,
		                        BasicStroke.JOIN_MITER,
		                        2.0f, new float[]{2f}, 0.0f);
		
		private double ratio = 0;

		private MemoryLabel() {
			UIUtil.disableHtml(this);
			setOpaque(false);
			setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			
			setHorizontalAlignment(SwingConstants.CENTER);
			setVerticalAlignment(SwingConstants.CENTER);
		}
		
		public void refresh() {
			long used = SystemMonitor.getInstance().getUsed();
			long committed = SystemMonitor.getInstance().getCommitted();
			long max = SystemMonitor.getInstance().getMax();
			long threshold = SystemMonitor.getInstance().getThreshold();
			
			ratio = (double)used / committed;
			
			String text = ResourceManager.getInstance().get(
					"core.systemMonitorPanel.label",  //$NON-NLS-1$
					SystemMonitor.formatMemory(used), SystemMonitor.formatMemory(committed));

			String tooltip = ResourceManager.getInstance().get(
					"core.systemMonitorPanel.tooltip", //$NON-NLS-1$
					SystemMonitor.formatMemory(used), SystemMonitor.formatMemory(committed),
					SystemMonitor.formatMemory(max), SystemMonitor.formatMemory(threshold));
			
			setText(text);
			setToolTipText(UIUtil.toUnwrappedSwingTooltip(tooltip));
		}
		
		

		/**
		 * @see javax.swing.JComponent#getBorder()
		 */
		@Override
		public EtchedBorder getBorder() {
			return (EtchedBorder) super.getBorder();
		}

		/**
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(Graphics graphics) {
			Graphics2D g = (Graphics2D) graphics;
			
//			Color barColor = UIManager.getColor("ToolTip.background"); //$NON-NLS-1$
			Color barColor = getBorder().getHighlightColor(this);
			
			boolean colorize = ConfigRegistry.getGlobalRegistry().getBoolean(
					"general.performance.colorizeMemoryMonitor"); //$NON-NLS-1$
			
			if(colorize) {
				barColor = UIUtil.getColor(1-ratio);
			}
			
			Color col = g.getColor();
			Stroke s = g.getStroke();
			Rectangle r = getBounds();
			
			int width = (int) (ratio * r.width);
			
			g.setColor(getBackground());
			g.fillRect(r.x, r.y, r.width, r.height);
			
			g.setColor(barColor);
			g.fillRect(r.x, r.y, r.x+width, r.height);
			
			if(width>0) {
				g.setColor(getBorder().getShadowColor(this));
				g.drawLine(r.x+width, r.y-1, r.x+width, r.y+r.height-4);
			}
			
			double threshold = ConfigRegistry.getGlobalRegistry().getDouble(
					"general.performance.memoryThreshold"); //$NON-NLS-1$
			if(threshold<0.1) {
				threshold = SystemMonitor.DEFAULT_THRESHOLD;
			}
			
			width = (int) (threshold * r.width);
			g.setStroke(dashed);
			if(colorize) {
				g.setColor(Color.darkGray);
			} else {
				g.setColor(Color.red);
			}
			g.drawLine(r.x+width, r.y-1, r.x+width, r.y+r.height-4);
			
			g.setColor(col);
			g.setStroke(s);
			
			super.paintComponent(g);
		}
	}
}
