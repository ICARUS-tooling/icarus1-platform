/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class NavigationControl implements ListSelectionListener, 
		ListDataListener, ActionListener, PropertyChangeListener {
	
	public static final String ARROW_STYLE_OPTION = "arrowStyle"; //$NON-NLS-1$
	public static final String LEFT_CONTENT_OPTION = "leftContent"; //$NON-NLS-1$
	public static final String CENTER_CONTENT_OPTION = "centerContent"; //$NON-NLS-1$
	public static final String RIGHT_CONTENT_OPTION = "rightContent"; //$NON-NLS-1$
	public static final String TITLE_LABEL_OPTION = "titleLabel"; //$NON-NLS-1$
	
	public static enum ElementType {
		
		FIRST_ELEMENT("firstElement"), //$NON-NLS-1$
		PREVIOUS_ELEMENT("previousElement"), //$NON-NLS-1$
		NEXT_ELEMENT("nextElement"), //$NON-NLS-1$
		LAST_ELEMENT("lastElement"); //$NON-NLS-1$
		
		private String name;
		
		private ElementType(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public static ElementType parse(String s) {
			for(ElementType t : values()) {
				if(t.getName().equals(s)) {
					return t;
				}
			}
			throw new IllegalArgumentException("Unknown element type string: "+String.valueOf(s)); //$NON-NLS-1$
		}
	}
	
	public static class ArrowStyle {
		
		protected String suffix;
		
		public ArrowStyle(String suffix) {
			if(suffix==null)
				throw new IllegalArgumentException("Invalid suffix"); //$NON-NLS-1$
			this.suffix = suffix;
		}
		
		public String getSuffix() {
			return suffix;
		}
		
		protected String getBaseName(ElementType elementType) {
			switch (elementType) {
			case FIRST_ELEMENT:
				return "navi_left_stop"; //$NON-NLS-1$
			case PREVIOUS_ELEMENT:
				return "navi_left"; //$NON-NLS-1$
			case NEXT_ELEMENT:
				return "navi_right"; //$NON-NLS-1$
			case LAST_ELEMENT:
				return "navi_right_stop"; //$NON-NLS-1$
			default:
				throw new IllegalArgumentException();
			}
		}
		
		public Icon getIcon(ElementType elementType) {
			String iconName = getBaseName(elementType)+getSuffix()+".png"; //$NON-NLS-1$
			return IconRegistry.getGlobalRegistry().getIcon(iconName);
		}
	}
	
	public static final ArrowStyle DEFAULT_ARROW_STYLE = new ArrowStyle(""); //$NON-NLS-1$
	public static final ArrowStyle RED_ARROW_STYLE = new ArrowStyle("_red"); //$NON-NLS-1$
	public static final ArrowStyle MINI_ARROW_STYLE = new ArrowStyle("_mini"); //$NON-NLS-1$ 
	
	protected static final String firstActionId = "core.helpers.navigationControl.firstElementAction"; //$NON-NLS-1$
	protected static final String previousActionId = "core.helpers.navigationControl.previousElementAction"; //$NON-NLS-1$
	protected static final String nextActionId = "core.helpers.navigationControl.nextElementAction"; //$NON-NLS-1$
	protected static final String lastActionId = "core.helpers.navigationControl.lastElementAction"; //$NON-NLS-1$
	
	protected final JList<?> list;
	protected JLabel titleLabel;
	protected JFormattedTextField indexField;
	
	protected ArrowStyle arrowStyle;
	
	protected ActionManager actionManager;
	
	private static ActionManager sharedActionManager;
	
	protected final JToolBar toolBar;

	public NavigationControl(JList<?> list, Options options) {
		if(list==null)
			throw new IllegalArgumentException("Invalid list"); //$NON-NLS-1$
		
		this.list = list;
		list.getSelectionModel().addListSelectionListener(this);
		list.getModel().addListDataListener(this);
		list.addPropertyChangeListener("model", this); //$NON-NLS-1$
		list.addPropertyChangeListener("selectionModel", this); //$NON-NLS-1$
		
		titleLabel = new JLabel();
		
		toolBar = createToolBar(options);
		
		setArrowStyle(DEFAULT_ARROW_STYLE);
		
		refreshActions();
	}
	
	public JToolBar getToolBar() {
		return toolBar;
	}

	public ArrowStyle getArrowStyle() {
		return arrowStyle;
	}

	public void setArrowStyle(ArrowStyle arrowStyle) {
		if(arrowStyle==null)
			throw new IllegalArgumentException("Invalid arrow style"); //$NON-NLS-1$
		
		if(this.arrowStyle==arrowStyle) {
			return;
		}
		
		this.arrowStyle = arrowStyle;

		ActionManager actionManager = getActionManager();
		
		// Set action icons according to arrow style value
		actionManager.getAction(firstActionId).putValue(
				Action.SMALL_ICON, arrowStyle.getIcon(ElementType.FIRST_ELEMENT));
		actionManager.getAction(previousActionId).putValue( 
				Action.SMALL_ICON, arrowStyle.getIcon(ElementType.PREVIOUS_ELEMENT));
		actionManager.getAction(nextActionId).putValue( 
				Action.SMALL_ICON, arrowStyle.getIcon(ElementType.NEXT_ELEMENT));
		actionManager.getAction(lastActionId).putValue( 
				Action.SMALL_ICON, arrowStyle.getIcon(ElementType.LAST_ELEMENT));
	}
	
	public void setTitle(String title) {
		titleLabel.setText(title);
	}
	
	protected static synchronized final ActionManager getSharedActionManager() {
		if(sharedActionManager==null) {
			sharedActionManager = ActionManager.globalManager().derive();

			URL actionLocation = NavigationControl.class.getResource("navigation-control-actions.xml"); //$NON-NLS-1$
			if(actionLocation==null)
				throw new CorruptedStateException("Missing resources: navigation-control-actions.xml"); //$NON-NLS-1$
			
			try {
				sharedActionManager.loadActions(actionLocation);
			} catch (IOException e) {
				LoggerFactory.log(NavigationControl.class, Level.SEVERE, 
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
	
	protected JToolBar createToolBar(Options options) {
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		indexField = createIndexField();
		options.put(CENTER_CONTENT_OPTION, indexField);
		options.putIfAbsent(TITLE_LABEL_OPTION, titleLabel);
		
		return getActionManager().createToolBar(
				"core.helpers.navigationControl.toolBarList", options); //$NON-NLS-1$
	}
	
	protected JFormattedTextField createIndexField() {
		JFormattedTextField.AbstractFormatter defaultFormat = new DefaultIndexFormatter();
		InternationalFormatter editFormat = new InternationalFormatter();
		editFormat.setMinimum(0);
		editFormat.setFormat(new DecimalFormat("###,###")); //$NON-NLS-1$
		
		JFormattedTextField.AbstractFormatterFactory formatterFactory = new DefaultFormatterFactory(
				defaultFormat, defaultFormat, editFormat, defaultFormat);
		
		JFormattedTextField indexField = new JFormattedTextField(formatterFactory);
		indexField.setHorizontalAlignment(JTextField.CENTER);
		indexField.setValue(list.getSelectedIndex()+1);
		indexField.addPropertyChangeListener("value", this); //$NON-NLS-1$
		
		Dimension size = new Dimension(80, 22);
		indexField.setPreferredSize(size);
		indexField.setMinimumSize(size);
		indexField.setMaximumSize(size);
		
		return indexField;
	}
	
	protected void refreshActions() {
		int size = list.getModel().getSize();
		int selectedIndex = list.getSelectedIndex();
		
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
	}
	
	protected void registerActionCallbacks() {
		ActionManager actionManager = getActionManager();
		
		actionManager.addHandler(firstActionId, this, "actionPerformed"); //$NON-NLS-1$
		actionManager.addHandler(previousActionId, this, "actionPerformed"); //$NON-NLS-1$
		actionManager.addHandler(nextActionId, this, "actionPerformed"); //$NON-NLS-1$
		actionManager.addHandler(lastActionId, this, "actionPerformed"); //$NON-NLS-1$
	}
	
	protected void refreshSelectedIndex() {
		int selectedIndex = list.getSelectedIndex();
		indexField.setValue(selectedIndex+1);
	}
	
	protected void refreshListSelection(int index) {
		list.setSelectedIndex(index);
		list.ensureIndexIsVisible(index);
	}
	
	protected void refreshDisplayedSize() {
		int size = list.getModel().getSize();
		
		// Adjust upper limit in formatter
		DefaultFormatterFactory factory = (DefaultFormatterFactory)indexField.getFormatterFactory();
		InternationalFormatter formatter = (InternationalFormatter) factory.getEditFormatter();
		formatter.setMaximum(size);
		
		if(size==0) {
			indexField.setValue(0);
		}
	}

	/**
	 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
	 */
	@Override
	public void intervalAdded(ListDataEvent e) {
		refreshDisplayedSize();
		refreshActions();
	}

	/**
	 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
	 */
	@Override
	public void intervalRemoved(ListDataEvent e) {
		refreshDisplayedSize();
		refreshActions();
	}

	/**
	 * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
	 */
	@Override
	public void contentsChanged(ListDataEvent e) {
		refreshDisplayedSize();
		refreshActions();
	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		refreshSelectedIndex();
		refreshActions();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		ElementType type = ElementType.parse(command);
		
		int size = list.getModel().getSize();
		int selectedIndex = list.getSelectedIndex();
		
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
		
		refreshListSelection(selectedIndex);
	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if("value".equals(evt.getPropertyName())) { //$NON-NLS-1$
			Integer value = (Integer) indexField.getValue();
			int selectedIndex = value==null ? -1 : value-1;
			if(selectedIndex==-1) {
				list.getSelectionModel().clearSelection();
			} else {
				refreshListSelection(selectedIndex);
			}
		} else if("selectionModel".equals(evt.getPropertyName())) { //$NON-NLS-1$
			((ListSelectionModel)evt.getOldValue()).removeListSelectionListener(this);
			((ListSelectionModel)evt.getNewValue()).addListSelectionListener(this);
		} else if("model".equals(evt.getPropertyName())) { //$NON-NLS-1$
			((ListModel<?>)evt.getOldValue()).removeListDataListener(this);
			((ListModel<?>)evt.getNewValue()).addListDataListener(this);
		}
	}

	public class DefaultIndexFormatter extends JFormattedTextField.AbstractFormatter {

		private static final long serialVersionUID = -1688584989456611374L;

		/**
		 * @see javax.swing.JFormattedTextField.AbstractFormatter#stringToValue(java.lang.String)
		 */
		@Override
		public Object stringToValue(String text) throws ParseException {
			return null;
		}

		/**
		 * @see javax.swing.JFormattedTextField.AbstractFormatter#valueToString(java.lang.Object)
		 */
		@Override
		public String valueToString(Object value) throws ParseException {
			if(!(value instanceof Integer) || value==null) {
				value = -1;
			}
			
			int size = list.getModel().getSize();
			
			if(size==0) {
				return "-"; //$NON-NLS-1$
			}
			
			int index = (int)value;
			String indexString = index<0 ? "-" : String.valueOf(index); //$NON-NLS-1$
			
			return String.format("%s / %d", indexString, size); //$NON-NLS-1$
		}
		
	}
}
