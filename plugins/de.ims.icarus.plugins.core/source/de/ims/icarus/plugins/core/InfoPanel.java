/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.core;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class InfoPanel {
	
	private static final int DEFAULT_HEIGHT = 25;
	
	private JPanel contentPanel;
	
	private Map<String, JLabel> labels;

	public InfoPanel() {
		// no-op
	}
	
	private void buildContentPanel() {
		contentPanel = new JPanel(new GridBagLayout());
		resize(contentPanel, new Dimension(1000, DEFAULT_HEIGHT));
	}
	
	private void resize(Component comp, Dimension size) {
		comp.setSize(size);
		comp.setPreferredSize(size);
		comp.setMinimumSize(size);
		comp.setMaximumSize(size);
	}

	JPanel getContentPanel() {
		if(contentPanel==null) {
			buildContentPanel();
		}
		
		return contentPanel;
	}
	
	public void clear() {
		JPanel contentPanel = getContentPanel();
		
		contentPanel.removeAll();
		
		if(labels!=null) {
			labels.clear();
		}
		contentPanel.repaint();
	}
	
	public void addSeparator() {
		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
		resize(separator, new Dimension(5, DEFAULT_HEIGHT));
		add(separator, false);
	}
	
	private static Border labelBorder;
	
	public JLabel addLabel(String title, int width) {
		return addLabel(title, width, false);
	}
	
	public JLabel addLabel(String title) {
		return addLabel(title, -1, true);
	}
	
	public JLabel addLabel(String title, int width, boolean fill) {
		if(labels==null) {
			labels = new HashMap<>();
		}
		
		JLabel label = new JLabel(null, null, JLabel.LEFT);
		labels.put(title, label);
		
		if(width!=-1) {
			resize(label, new Dimension(width, DEFAULT_HEIGHT));
		}
		
		if(labelBorder==null) {
			labelBorder = new EmptyBorder(0, 4, 0, 4);
		}
		label.setBorder(labelBorder);
		
		add(label, fill);
		
		return label;
	}
	
	public void add(Component comp, boolean fill) {
		add(comp, fill, GridBagConstraints.WEST, 0);
	}
	
	public void add(Component comp,  int anchor) {
		add(comp, false, anchor, 100);
	}
	
	public void add(Component comp, boolean fill, int anchor, int weight) {
		JPanel contentPanel = getContentPanel();
		int index = contentPanel.getComponentCount();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = index;
		gbc.gridy = 0;
		gbc.anchor = anchor;
		if(fill) {
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 100;
		} else {
			gbc.weightx = weight;
		}
		
		contentPanel.add(comp, gbc);
	}
	
	public void addGap(int width) {
		add(Box.createRigidArea(new Dimension(width, DEFAULT_HEIGHT)), false);
	}
	
	public void addGlue() {
		add(Box.createGlue(), true);
	}

	public void displayText(String title, String text) {
		displayText(title, text, null);
	}
	
	public void displayText(String title, String text, Icon icon) {
		if(labels==null) {
			return;
		}
		
		JLabel label = labels.get(title);
		if(label==null) {
			return;
		}
		
		label.setText(text);
		label.setIcon(icon);
	}
	
	public void clearAllLabels() {
		if(labels==null || labels.isEmpty()) {
			return;
		}
		
		for(JLabel label  : labels.values()) {
			label.setText(null);
			label.setIcon(null);
		}
	}
}
