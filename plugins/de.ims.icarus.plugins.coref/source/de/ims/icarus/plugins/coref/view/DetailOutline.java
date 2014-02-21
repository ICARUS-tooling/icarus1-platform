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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import de.ims.icarus.language.coref.CorefMember;
import de.ims.icarus.language.coref.CorefProperties;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.NavigationControl.ElementType;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DetailOutline implements AWTPresenter, ActionListener {
	
	protected PropertyTableModel tableModel;
	protected JTable table;
	protected JTextArea textArea;
	
	protected JPanel contentPanel;
	protected JLabel indexLabel;
	
	protected CoreferenceDocumentData document;
	
	protected ActionManager actionManager;
	
	private static ActionManager sharedActionManager;
	
	private CorefMember[] members;
	private int currentIndex = -1;

	protected static final String firstActionId = "plugins.coref.detailOutline.firstElementAction"; //$NON-NLS-1$
	protected static final String previousActionId = "plugins.coref.detailOutline.previousElementAction"; //$NON-NLS-1$
	protected static final String nextActionId = "plugins.coref.detailOutline.nextElementAction"; //$NON-NLS-1$
	protected static final String lastActionId = "plugins.coref.detailOutline.lastElementAction"; //$NON-NLS-1$

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
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
		
		Collection<?> items = (Collection<?>) data;
		
		if(items==null || items.isEmpty())
			throw new UnsupportedPresentationDataException("Cannot present empty or invalid collection. Use Presenter.clear() in that case!"); //$NON-NLS-1$
		
		CorefMember[] members = new CorefMember[items.size()];
		items.toArray(members);
		
		if(this.members!=null && Arrays.equals(members, this.members)) {
			return;
		}
		
		this.members = members;
		currentIndex = 0;
		
		displaySelectedMember();
	}
	
	protected CorefMember getSelectedMember() {
		return members==null || currentIndex==-1 ? null : members[currentIndex];
	}
	
	protected void displaySelectedMember() {

		CorefMember member = getSelectedMember();
		
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

		String indexString = String.valueOf(currentIndex+1)+'/'+String.valueOf(getMemberCount());
		
		indexLabel.setText(indexString);
		
		refreshActions();
	}
	
	protected String createLabel(CorefMember data) {
		StringBuilder sb = new StringBuilder();
		
		ResourceManager rm = ResourceManager.getInstance();
		if(data instanceof Span) {
			Span span = (Span) data;
			sb.append(rm.get("plugins.coref.labels.span")).append('\n'); //$NON-NLS-1$
			appendSpan(sb, span);
		} else if(data instanceof Edge) {
			Edge edge = (Edge) data;
			sb.append(rm.get("plugins.coref.labels.edge")).append('\n'); //$NON-NLS-1$
			// Source
			sb.append(rm.get("plugins.coref.labels.source")).append(":\n"); //$NON-NLS-1$ //$NON-NLS-2$
			appendSpan(sb, edge.getSource());
			sb.append('\n');
			// Target
			sb.append(rm.get("plugins.coref.labels.target")).append(":\n"); //$NON-NLS-1$ //$NON-NLS-2$
			appendSpan(sb, edge.getTarget());
		}
		
		CorefProperties properties = data.getProperties();
		if(properties!=null) {
			sb.append('\n');
			sb.append(rm.get("plugins.coref.labels.properties")).append(":"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return sb.toString();
	}
	
	private void appendSpan(StringBuilder sb, Span span) {
		ResourceManager rm = ResourceManager.getInstance();
		
		String text = CoreferenceUtils.getSpanText(span, document);
		text = StringUtil.fit(text, 40);
		
		sb.append(rm.get("plugins.coref.labels.text")) //$NON-NLS-1$
			.append(": ").append(text).append('\n'); //$NON-NLS-1$
		sb.append(rm.get("plugins.coref.labels.sentenceId")) //$NON-NLS-1$
			.append(": ").append(span.getSentenceIndex()+1).append('\n'); //$NON-NLS-1$
		sb.append(rm.get("plugins.coref.labels.beginIndex")) //$NON-NLS-1$
			.append(": ").append(span.getBeginIndex()+1).append('\n'); //$NON-NLS-1$
		sb.append(rm.get("plugins.coref.labels.endIndex")) //$NON-NLS-1$
			.append(": ").append(span.getEndIndex()+1).append('\n'); //$NON-NLS-1$
		sb.append(rm.get("plugins.coref.labels.clusterId")) //$NON-NLS-1$
			.append(": ").append(span.getClusterId()).append('\n'); //$NON-NLS-1$
	}
	
	private int getMemberCount() {
		return members==null ? 0 : members.length;
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
		
		currentIndex = -1;
		
		indexLabel.setText("-"); //$NON-NLS-1$
		refreshActions();
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
	
	protected static synchronized final ActionManager getSharedActionManager() {
		if(sharedActionManager==null) {
			sharedActionManager = ActionManager.globalManager().derive();

			URL actionLocation = DetailOutline.class.getResource("detail-outline-actions.xml"); //$NON-NLS-1$
			if(actionLocation==null)
				throw new CorruptedStateException("Missing resources: detail-outline-actions.xml"); //$NON-NLS-1$
			
			try {
				sharedActionManager.loadActions(actionLocation);
			} catch (IOException e) {
				LoggerFactory.log(DetailOutline.class, Level.SEVERE, 
						"Failed to load actions from file", e); //$NON-NLS-1$
			}
		}
		
		return sharedActionManager;
	}
	
	protected ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = getSharedActionManager().derive();
			
			registerActionCallbacks();
		}
		
		return actionManager;
	}
	
	protected ActionComponentBuilder createToolBar() {
		ActionComponentBuilder builder = new ActionComponentBuilder(getActionManager());
		builder.setActionListId("plugins.coref.detailOutline.toolBarList"); //$NON-NLS-1$

		indexLabel = createIndexLabel();
		builder.addOption("itemSelect", indexLabel); //$NON-NLS-1$
		
		return builder;
	}
	
	protected JLabel createIndexLabel() {
		
		JLabel indexLabel = new JLabel();
		indexLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		Dimension size = new Dimension(80, 22);
		indexLabel.setPreferredSize(size);
		indexLabel.setMinimumSize(size);
		indexLabel.setMaximumSize(size);
		
		return indexLabel;
	}
	
	protected void refreshActions() {
		int size = getMemberCount();
		int selectedIndex = currentIndex;
		
		boolean selected = selectedIndex!=-1;
		
		boolean firstEnabled = selected && selectedIndex>0;
		boolean previousEnabled = selected && selectedIndex>0;
		boolean nextEnabled = selected && selectedIndex<size-1;
		boolean lastEnabled = selected && selectedIndex<size-1;
		
		ActionManager actionManager = getActionManager();
		actionManager.setEnabled(firstEnabled, firstActionId);
		actionManager.setEnabled(previousEnabled, previousActionId);
		actionManager.setEnabled(nextEnabled, nextActionId);
		actionManager.setEnabled(lastEnabled, lastActionId);
		
		if(indexLabel!=null) {
			indexLabel.setEnabled(getMemberCount()>1);
		}
	}
	
	protected void registerActionCallbacks() {
		ActionManager actionManager = getActionManager();
		
		actionManager.addHandler(firstActionId, this, "actionPerformed"); //$NON-NLS-1$
		actionManager.addHandler(previousActionId, this, "actionPerformed"); //$NON-NLS-1$
		actionManager.addHandler(nextActionId, this, "actionPerformed"); //$NON-NLS-1$
		actionManager.addHandler(lastActionId, this, "actionPerformed"); //$NON-NLS-1$
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
		
		JPanel outlinePanel = new JPanel(new BorderLayout());
		outlinePanel.setBorder(UIUtil.topLineBorder);
		
		JScrollPane scrollPane = new JScrollPane(table);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		scrollPane.setBorder(UIUtil.emptyBorder);
		
		outlinePanel.add(textArea, BorderLayout.NORTH);
		outlinePanel.add(scrollPane, BorderLayout.CENTER);
		
		ActionComponentBuilder builder = createToolBar();
		if(builder!=null) {
			panel.add(builder.buildToolBar(), BorderLayout.NORTH);
		}
		
		panel.add(outlinePanel, BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(200, 200));
		
		return panel;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		ElementType type = ElementType.parse(command);
		
		int size = getMemberCount();
		int selectedIndex = currentIndex;
		
		switch (type) {
		case FIRST_ELEMENT:
			selectedIndex = 0;
			break;
			
		case PREVIOUS_ELEMENT:
			selectedIndex--;
			break;
			
		case NEXT_ELEMENT:
			selectedIndex++;
			break;
			
		case LAST_ELEMENT:
			selectedIndex = size-1;
			break;
		}
		
		if(selectedIndex<0 || selectedIndex>=size) {
			LoggerFactory.log(this, Level.WARNING, 
					"Invalid action state - selection index is out of bounds: "+selectedIndex, new Throwable()); //$NON-NLS-1$
			return;
		}
				
		currentIndex = selectedIndex;
		displaySelectedMember();
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
