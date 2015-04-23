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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.grid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.ims.icarus.language.coref.CorefErrorType;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.plugins.coref.view.grid.EntityGridTableModel.EntityGridColumnModel;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.table.MultilineTableHeaderRenderer.MultilineListModel;
import de.ims.icarus.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EntityGridTableHeaderRenderer extends JComponent implements TableCellRenderer {

	private static final long serialVersionUID = -7801155678991387167L;

	public static final int DEFAULT_HEADER_LINES = 3;

	private boolean showErrorLabels = true;
	private boolean prototypeMode = false;

	private final TableCellRenderer renderer;
	private JList<String> list;
	private MultilineListModel listModel;

	private JLabel countLabel;
	private ClusterIcon clusterIcon;
	private JLabel[] errorLabels;
	private ErrorIcon[] errorIcons;

	public EntityGridTableHeaderRenderer(TableCellRenderer renderer) {
		if(renderer==null)
			throw new NullPointerException("Invalid renderer"); //$NON-NLS-1$

		setOpaque(false);

		this.renderer = renderer;

		JComponent comp = (JComponent) renderer;

		CorefErrorType[] errorTypes = CorefErrorType.values();
		errorLabels = new JLabel[errorTypes.length];
		errorIcons = new ErrorIcon[errorTypes.length];

		listModel = new MultilineListModel();
		//listModel.setMinLineCount(DEFAULT_HEADER_LINES);
		listModel.setMaxLineCount(DEFAULT_HEADER_LINES);
		list = new JList<>(listModel);
	    list.setOpaque(false);
	    list.setForeground(comp.getForeground());
	    list.setBorder(UIUtil.defaultContentBorder);

	    DefaultListCellRenderer listRenderer = new DefaultListCellRenderer();
	    listRenderer.setOpaque(false);
	    UIUtil.disableHtml(listRenderer);
	    listRenderer.setHorizontalAlignment(SwingConstants.LEFT);
	    list.setCellRenderer(listRenderer);

	    clusterIcon = new ClusterIcon();

	    countLabel = new JLabel();
	    countLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    countLabel.setVerticalAlignment(SwingConstants.TOP);
	    countLabel.setHorizontalTextPosition(SwingConstants.CENTER);
	    countLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
	    countLabel.setIconTextGap(2);
	    countLabel.setOpaque(false);
	    countLabel.setIcon(clusterIcon);
	    countLabel.setBorder(new EmptyBorder(0, 4, 0, 7));

		FormLayout layout = new FormLayout(
				"2dlu, l:m:grow, l:m, 2dlu, l:m, 2dlu, l:m, 2dlu, l:m, 2dlu, l:m, 2dlu, l:m, 2dlu, l:m, r:m:grow, 2dlu",  //$NON-NLS-1$
				"2dlu, t:p, 2dlu, t:p, 2dlu"); //$NON-NLS-1$

		setLayout(layout);
		add(list, CC.xyw(2, 2, 15));
		add(countLabel, CC.xy(3, 4, CellConstraints.CENTER, CellConstraints.FILL));

		for(int i=0; i<errorTypes.length; i++) {
			ErrorIcon icon = new ErrorIcon(errorTypes[i]);

			JLabel label = new JLabel(icon);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setVerticalAlignment(SwingConstants.TOP);
			label.setHorizontalTextPosition(SwingConstants.CENTER);
			label.setVerticalTextPosition(SwingConstants.BOTTOM);
			label.setIconTextGap(2);
			label.setOpaque(false);

			errorLabels[i] = label;
			errorIcons[i] = icon;

			add(label, CC.xy(i*2+5, 4));
		}

		setShowErrorLabels(false);
	}

	/**
	 * @return the prototypeMode
	 */
	public boolean isPrototypeMode() {
		return prototypeMode;
	}

	/**
	 * @param prototypeMode the prototypeMode to set
	 */
	public void setPrototypeMode(boolean prototypeMode) {
		this.prototypeMode = prototypeMode;
	}

	/**
	 * @return the showErrorLabels
	 */
	public boolean isShowErrorLabels() {
		return showErrorLabels;
	}

	/**
	 * @param showErrorLabels the showErrorLabels to set
	 */
	public void setShowErrorLabels(boolean showErrorLabels) {
		if(this.showErrorLabels==showErrorLabels) {
			return;
		}

		this.showErrorLabels = showErrorLabels;

		countLabel.setVisible(showErrorLabels);
		for(JLabel label : errorLabels) {
			label.setVisible(showErrorLabels);
		}
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

//		if(table!=null) {
			isSelected |= table.getColumnModel().getSelectionModel().isSelectedIndex(column);
//		}

		EntityGridTableModel model = (EntityGridTableModel) table.getModel();
		ErrorSummary summary = model.getErrorSummary(column);

		clusterIcon.setErrorType(summary.getClusterType());
		countLabel.setText(String.valueOf(summary.getTotalMentionCount()));

		for(int i=0; i<errorLabels.length; i++) {
			ErrorIcon icon = errorIcons[i];
			JLabel label = errorLabels[i];

			icon.setState(summary);
			int count = summary.getCount(icon.getErrorType());
			label.setText(count==0 ? null : String.valueOf(count));
		}

//		// Provide quick info about complete clusters being
//		// false positives or false negatives
//		Color bg = defaultBackground;
//
//		ErrorSummary summary = model.getErrorSummary(column);
//		CorefErrorType errorType = summary==null ? null : summary.getClusterType();
//
//		if(errorType!=null) {
//			bg = CoreferenceUtils.getErrorColor(errorType);
//		}
//
//		setBackground(bg);

		if(prototypeMode) {
			EntityGridColumnModel columnModel = model.getColumnModel();
			String prototypeLabel = columnModel.getPrototypeLabel();
			if(prototypeLabel!=null) {
				value = prototypeLabel;
			}
		}

		String str = (value == null) ? "" : value.toString(); //$NON-NLS-1$

		int width = table.getColumnModel().getColumn(column).getWidth()-5;
		width = Math.max(width, StringUtil.MIN_WRAP_WIDTH);
		JComponent listRenderer = (JComponent) list.getCellRenderer();
		String[] lines = StringUtil.split(str, listRenderer, width);

		listModel.setLines(lines);

//		System.out.printf("lines: %d text: %s\n", listModel.getSize(), Arrays.toString(lines));

		StringBuilder sb = new StringBuilder();
		sb.append('"').append(str).append('"');
		summary.append(sb);

		setToolTipText(UIUtil.toSwingTooltip(sb.toString()));

		renderer.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);

		return this;
	}

	public void setAutoAdjustEnabled(boolean autoAdjust) {
		if (autoAdjust) {
			listModel.setMinLineCount(0);
		} else {
			listModel.setMinLineCount(DEFAULT_HEADER_LINES);
		}
	}

	/**
	 * @see javax.swing.JComponent#print(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {

		// Required trick for our little hack to work:
		// Apparently some renderers require a valid parent to
		// work properly. Therefore we shift our current parent
		// component to the original renderer and let him paint
		// what we use as background.
		// Afterwards we reclaim our parent and draw the overlay
		// with all components set to be not opaque.

		// Assign parent to renderer
		Container parent = getParent();
		Component comp = (Component) renderer;
		parent.add(comp);
		// Make renderer mimic our size
		comp.setBounds(getBounds());
		// Let renderer paint "background"
		comp.paint(g);

		// Reclaim parent
		parent.add(this);

		// Perform default painting of child components
		super.paint(g);
	}

	public enum ErrorIconState {
		NONE,
		SOME,
		ALL;
	}

	public static class ClusterIcon implements Icon {

		private CorefErrorType errorType = null;

		/**
		 * @return the errorType
		 */
		public CorefErrorType getErrorType() {
			return errorType;
		}

		/**
		 * @param errorType the errorType to set
		 */
		public void setErrorType(CorefErrorType errorType) {
			this.errorType = errorType;
		}

		/**
		 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
		 */
		@Override
		public void paintIcon(Component c, Graphics graphics, int x, int y) {
			if(errorType==null) {
				return;
			}

			Graphics2D g = (Graphics2D) graphics;

			Color col = g.getColor();

			Color errorCol = CoreferenceUtils.getErrorColor(errorType);
			if(errorCol==null) {
				errorCol = Color.black;
			}
			g.setColor(errorCol);

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.fillOval(x, y, getIconWidth(), getIconHeight());

			g.setColor(col);
		}

		/**
		 * @see javax.swing.Icon#getIconWidth()
		 */
		@Override
		public int getIconWidth() {
			return 8;
		}

		/**
		 * @see javax.swing.Icon#getIconHeight()
		 */
		@Override
		public int getIconHeight() {
			return 8;
		}

	}

	public static class ErrorIcon implements Icon {

		private final CorefErrorType errorType;
		private ErrorIconState state = ErrorIconState.NONE;

		public ErrorIcon(CorefErrorType errorType) {
			if(errorType==null)
				throw new NullPointerException("Invalid error type"); //$NON-NLS-1$

			this.errorType = errorType;
		}

		public void setState(ErrorSummary summary) {
			if(summary.getClusterType()==errorType) {
				state = ErrorIconState.ALL;
			} else if(summary.getCount(errorType)>0) {
				state = ErrorIconState.SOME;
			} else {
				state = ErrorIconState.NONE;
			}
		}

		/**
		 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
		 */
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Color col = g.getColor();

			Color errorCol = CoreferenceUtils.getErrorColor(errorType);
			if(errorCol==null) {
				errorCol = Color.black;
			}
			g.setColor(errorCol);

			int w = getIconWidth()-1;
			int h = getIconHeight()-1;

			g.drawRect(x, y, w, h);

			switch (state) {
			case ALL:
				g.fillRect(x, y, w, h);
				break;

			case SOME:
				g.fillRect(x, y+h/2+1, w, h/2);
				break;

			default:
				break;
			}

			g.setColor(col);
		}

		/**
		 * @see javax.swing.Icon#getIconWidth()
		 */
		@Override
		public int getIconWidth() {
			return 8;
		}

		/**
		 * @see javax.swing.Icon#getIconHeight()
		 */
		@Override
		public int getIconHeight() {
			return 8;
		}

		/**
		 * @return the errorType
		 */
		public CorefErrorType getErrorType() {
			return errorType;
		}

	}
}
