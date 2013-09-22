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
package de.ims.icarus.plugins.coref.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import de.ims.icarus.language.coref.CorefMember;
import de.ims.icarus.language.coref.CorefProperties;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DetailOutline implements AWTPresenter {
	
	protected PropertyTableModel tableModel;
	protected JTable table;
	protected JTextArea textArea;
	
	protected JPanel contentPanel;
	
	protected CorefMember currentMember;
	
	protected CoreferenceDocumentData document;

	public DetailOutline() {
		tableModel = new PropertyTableModel();
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		return CorefProperties.class.isAssignableFrom(type.getContentClass());
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if(data==null)
			throw new IllegalArgumentException("invalid data"); //$NON-NLS-1$
		
		if(!(data instanceof Edge)
			&& !(data instanceof Span))
			throw new UnsupportedPresentationDataException("Not an edge or span: "+data); //$NON-NLS-1$
		
		// TODO allow for presentation of multiple spans?
		CorefMember member = (CorefMember) data;
		
		if(currentMember==member) {
			return;
		}
		
		currentMember = member;

		CorefProperties sentenceProperties = null;
		CorefProperties memberProperties = member.getProperties();
		
		if(document!=null && member instanceof Span) {
			Span span = (Span)member;
			CoreferenceData sentence = document.get(span.getSentenceIndex());
			sentenceProperties = CorefProperties.subset(
					sentence.getProperties(), 
					span.getHead());
		}
		
		tableModel.setProperties(memberProperties, sentenceProperties);
		textArea.setText(createLabel(member));
	}
	
	protected String createLabel(CorefMember data) {
		StringBuilder sb = new StringBuilder();
		
		ResourceManager rm = ResourceManager.getInstance();
		if(data instanceof Span) {
			Span span = (Span) data;
			sb.append(rm.get("plugins.coref.labels.span")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$
			span.appendTo(sb);
		} else if(data instanceof Edge) {
			Edge edge = (Edge) data;
			sb.append(rm.get("plugins.coref.labels.edge")).append('\n'); //$NON-NLS-1$
			// Source
			sb.append(rm.get("plugins.coref.labels.source")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$
			edge.getSource().appendTo(sb);
			sb.append('\n');
			// Target
			sb.append(rm.get("plugins.coref.labels.target")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$
			edge.getTarget().appendTo(sb);
		}
		
		CorefProperties properties = data.getProperties();
		if(properties!=null) {
			sb.append('\n');
			sb.append(rm.get("plugins.coref.labels.properties")).append(":"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return sb.toString();
	}
	

	public CoreferenceDocumentData getDocument() {
		return document;
	}

	public void setDocument(CoreferenceDocumentData document) {
		this.document = document;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		textArea.setText(ResourceManager.getInstance().get(
				"plugins.coref.propertyPresenter.notAvailable")); //$NON-NLS-1$
		tableModel.clear();
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		tableModel.clear();
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return tableModel.getRowCount()>0;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return tableModel.getProperties();
	}
	
	protected JPanel buildPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBackground(panel.getBackground());
		textArea.setBorder(UIUtil.defaultContentBorder);
		textArea.setMinimumSize(new Dimension(50, 50));
		
		tableModel = new PropertyTableModel();
		table = new JTable(tableModel);
		table.getTableHeader().setReorderingAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(false);
		table.setDefaultRenderer(Object.class, new PropertyTableCellRenderer());
		table.setBorder(UIUtil.emptyBorder);
		
		JScrollPane scrollPane = new JScrollPane(table);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		scrollPane.setBorder(UIUtil.emptyBorder);
		
		panel.add(textArea, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		return panel;
	}

	/**
	 * @see de.ims.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPanel==null) {
			contentPanel = buildPanel();
		}
		
		return contentPanel;
	}
	
	protected static final String[] columnKeys = {
		"plugins.coref.propertyInfoDialog.labels.index", //$NON-NLS-1$
		"plugins.coref.propertyInfoDialog.labels.key", //$NON-NLS-1$
		"plugins.coref.propertyInfoDialog.labels.value", //$NON-NLS-1$
	};
	
	protected static class PropertyTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -7180449877212820145L;

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			
			PropertyTableModel model = (PropertyTableModel) table.getModel();
			
			if(model.isMemperProperty(row)) {
				setBackground(Color.yellow.brighter());
			} else {
				setBackground(table.getBackground());
			}
			
			TableColumn col = table.getColumnModel().getColumn(column);

			String tooltip = getText();
			int columnWidth = col.getWidth();
			int textWidth = 0;
			
			if(tooltip!=null && !tooltip.isEmpty()) {
				FontMetrics fm = getFontMetrics(getFont());
				textWidth = fm.stringWidth(tooltip);
			}
			
			if(textWidth<=columnWidth) {
				tooltip = null;
			}

			setToolTipText(UIUtil.toSwingTooltip(tooltip));
			
			return this;
		}
	}

	protected static class PropertyTableModel extends AbstractTableModel {
		
		protected List<String> keys = new ArrayList<>();
		protected Map<String, Object> properties = new HashMap<>();
		protected int memberPropertiesCount = -1; 

		private static final long serialVersionUID = 7282841984347543682L;
		
		public Map<String, Object> getProperties() {
			return properties;
		}
		
		public void clear() {
			setProperties(null, null);
		}

		public void setProperties(CorefProperties memberProperties, 
				CorefProperties sentenceProperties) {
			
			properties.clear();
			keys.clear();
			memberPropertiesCount = -1;
			
			if(memberProperties!=null && memberProperties.size()>0) {
				List<String> mKeys = new ArrayList<>();
				Map<String, Object> map = memberProperties.asMap();
				if(map!=null) {
					mKeys.addAll(map.keySet());
				}
				
				Collections.sort(mKeys);
				memberPropertiesCount = map.size();
				
				keys.addAll(mKeys);
				properties.putAll(memberProperties.asMap());
			} else {
				memberPropertiesCount = 0;
			}
			
			if(sentenceProperties!=null && sentenceProperties.size()>0) {
				List<String> sKeys = new ArrayList<>();
				Map<String, Object> map = sentenceProperties.asMap();
				if(map!=null) {
					sKeys.addAll(map.keySet());
				}
				
				Collections.sort(sKeys);
				
				keys.addAll(sKeys);
				properties.putAll(sentenceProperties.asMap());
			}
			
			fireTableDataChanged();
		}
		
		public boolean isMemperProperty(int index) {
			return memberPropertiesCount==-1 || index<memberPropertiesCount;
		}

		@Override
		public String getColumnName(int column) {
			return ResourceManager.getInstance().get(columnKeys[column]);
		}

		/**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return keys.size();
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return 3;
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0: return StringUtil.formatDecimal(rowIndex+1);
			case 1: return keys.get(rowIndex);
			case 2: return properties==null ? null : properties.get(keys.get(rowIndex));

			default: return null;
			}
		}
		
	}
}
