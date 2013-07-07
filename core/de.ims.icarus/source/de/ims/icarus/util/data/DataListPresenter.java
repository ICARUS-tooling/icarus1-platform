/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util.data;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import org.java.plugin.registry.Extension;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.NavigationControl;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionList.EntryType;
import de.ims.icarus.ui.helper.Editable;
import de.ims.icarus.ui.helper.FilteredListModel;
import de.ims.icarus.ui.helper.TextItem;
import de.ims.icarus.ui.helper.UIHelperRegistry;
import de.ims.icarus.ui.view.ListPresenter;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.Installable;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.PropertyChangeSource;
import de.ims.icarus.util.annotation.AnnotationContainer;
import de.ims.icarus.util.annotation.AnnotationControl;
import de.ims.icarus.util.annotation.AnnotationController;
import de.ims.icarus.util.annotation.AnnotationManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DataListPresenter<T extends Object> extends PropertyChangeSource 
		implements ListPresenter, AnnotationController {
	
	protected DataListModel<T> dataListModel;
	protected DataList<T> dataList;
	
	protected AnnotationManager annotationManager;
	protected AnnotationControl annotationControl;
	
	protected JList<?> list;
	protected ListSelectionModel listSelectionModel;
	
	protected Filter filter;
	protected FilteredListModel<T> filteredModel;
	
	protected String title;
	
	protected JPanel contentPanel;
	protected JComboBox<Object> filterSelect;
	protected JButton filterEditButton;
	protected JToggleButton outlineToggleButton;
	protected Map<Extension, Filter> filterInstances;
	protected NavigationControl navigationControl;
	
	protected JTextArea textArea;
	
	protected final String dummyEntry = "minus"; //$NON-NLS-1$
	
	protected Handler handler;

	public DataListPresenter() {
		// no-op
	}
	
	protected JPanel createContentPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		list = createList();
		list.addListSelectionListener(getHandler());
		annotationControl = createAnnotationControl();
		
		navigationControl = createNavigationControl();

		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(null);
		
		textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setVisible(false);
		textArea.setBorder(BorderFactory.createCompoundBorder(
				UIUtil.defaultBoxBorder, 
				BorderFactory.createEmptyBorder(0, 2, 0, 2)));
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(navigationControl.getToolBar(), BorderLayout.NORTH);
		topPanel.add(textArea, BorderLayout.CENTER);

		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		refresh();
		
		return panel;
	}
	
	protected JList<T> createList() {
		JList<T> list = new JList<>(getFilteredListModel());
		list.setSelectionModel(getSelectionModel());
		list.setBorder(UIUtil.defaultContentBorder);
		
		return list;
	}
	
	protected AnnotationControl createAnnotationControl() {
		return new AnnotationControl(false);
	}
	
	protected NavigationControl createNavigationControl() {	
		
		Options options = new Options();
		options.put(NavigationControl.RIGHT_CONTENT_OPTION, rightNavigationContent());
		options.put(NavigationControl.LEFT_CONTENT_OPTION, leftNavigationContent());
		
		return new NavigationControl(list, options);
	}
	
	protected Object leftNavigationContent() {
		List<Object> items = new ArrayList<>();
		items.add(getOutlineToggleButton());
		
		return items.toArray();
	}
	
	protected Object rightNavigationContent() {
		List<Object> items = new ArrayList<>();
		items.add(getFilterSelect());
		items.add(getFilterEditButton());
		
		if(annotationControl!=null) {
			items.add(EntryType.SEPARATOR);
			CollectionUtils.feedItems(items, (Object[])annotationControl.getComponents());
		}
		
		return items.toArray();
	}
	
	protected JButton getFilterEditButton() {
		if(filterEditButton==null) {
			filterEditButton = new JButton();
			filterEditButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("settings.gif")); //$NON-NLS-1$
			filterEditButton.setFocusable(false);
			filterEditButton.setFocusPainted(false);
			filterEditButton.addActionListener(getHandler());
			filterEditButton.setEnabled(getFilter() instanceof Editable);
		}
		
		return filterEditButton;
	}
	
	protected JToggleButton getOutlineToggleButton() {
		if(outlineToggleButton==null) {
			outlineToggleButton = new JToggleButton();
			ResourceManager.getInstance().getGlobalDomain().prepareComponent(outlineToggleButton, 
					"core.helpers.navigationControl.toggleOutlineAction.name",  //$NON-NLS-1$
					"core.helpers.navigationControl.toggleOutlineAction.description"); //$NON-NLS-1$
			ResourceManager.getInstance().getGlobalDomain().addComponent(outlineToggleButton);
			outlineToggleButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("externalize.gif")); //$NON-NLS-1$
			outlineToggleButton.setSelectedIcon(IconRegistry.getGlobalRegistry().getIcon("hide_externalized.gif")); //$NON-NLS-1$
			outlineToggleButton.setFocusable(false);
			outlineToggleButton.setFocusPainted(false);
			outlineToggleButton.addActionListener(getHandler());
			outlineToggleButton.setVisible(false);
		}
		
		return outlineToggleButton;
	}
	
	protected JComboBox<Object> getFilterSelect() {
		if(filterSelect==null) {
			filterSelect = new JComboBox<>(new DefaultComboBoxModel<>());
			filterSelect.setEditable(false);
			filterSelect.setFocusable(false);
			filterSelect.setRenderer(new ExtensionListCellRenderer());
			filterSelect.addActionListener(getHandler());
			UIUtil.fitToContent(filterSelect, 80, 150, 20);
		}
		
		return filterSelect;
	}
	
	protected Handler getHandler() {
		if(handler==null) {
			handler = new Handler();
		}
		
		return handler;
	}

	/**
	 * @see de.ims.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPanel==null) {
			contentPanel = createContentPanel();
		}
		
		return contentPanel;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible(
				"DataListContentType", type); //$NON-NLS-1$
	}
	
	protected void displayData(DataList<T> data, Options options) {
		if(dataList==null && dataList==data) {
			return;
		}
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		dataList = data;
		filter = (Filter) options.get(Options.FILTER);
		int index = options.get(Options.INDEX, -1);
		
		title = (String) options.get(Options.TITLE);
		
		if(contentPanel!=null) {
			refresh();
			
			if(index!=-1) {
				getSelectionModel().setSelectionInterval(index, index);
			} else {
				getSelectionModel().clearSelection();
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void refresh() {
		
		AnnotationManager annotationManager = null;
		if(dataList instanceof AnnotationContainer) {
			ContentType annotationType = ((AnnotationContainer)dataList).getAnnotationType();
			
			if(this.annotationManager!=null && this.annotationManager.supportsAnnotation(annotationType)) {
				annotationManager = this.annotationManager;
			}
			
			if(annotationManager==null && annotationType!=null) {
				annotationManager = UIHelperRegistry.globalRegistry().findHelper(AnnotationManager.class, annotationType);
			}
		}
		
		setAnnotationManager(annotationManager);
		
		ListCellRenderer renderer = null;
		if(dataList!=null) {
			renderer = UIHelperRegistry.globalRegistry().findHelper(
					ListCellRenderer.class, dataList.getContentType(), 
					true, true);
		}
		if(renderer==null) {
			renderer = new DefaultListCellRenderer();
		}
		if(renderer instanceof Installable) {
			((Installable)renderer).install(this);
		}
		
		ListCellRenderer oldRenderer = list.getCellRenderer();
		if(oldRenderer instanceof Installable) {
			((Installable)oldRenderer).uninstall(this);
		}
		
		list.setCellRenderer(renderer);
		
		getDataListModel().setDataList(dataList);
		getFilteredListModel().setFilter(filter);
		getSelectionModel().clearSelection();

		textArea.setText(null);
		ContentType entryType = getContentType();
		ContentType textType = ContentTypeRegistry.getInstance().getTypeForClass(TextItem.class);
		if(entryType==null || !ContentTypeRegistry.isCompatible(textType, entryType)) {
			getOutlineToggleButton().setVisible(false);
		} else {
			getOutlineToggleButton().setVisible(true);
		}
		
		navigationControl.setTitle(title);
		
		refreshFilterOptions();
		refreshTextOutline();
	}
	
	protected DataListModel<T> createListModel() {
		return new DataListModel<>();
	}
	
	public DataListModel<T> getDataListModel() {
		if(dataListModel==null) {
			dataListModel = createListModel();
		}
		return dataListModel;
	}
	
	public FilteredListModel<T> getFilteredListModel() {
		if(filteredModel==null) {
			filteredModel = new FilteredListModel<>(getDataListModel(), filter);
		}
		
		return filteredModel;
	}
	
	protected void refreshFilterOptions() {
		if(filterSelect==null || filterEditButton==null) {
			return;
		}
		
		Collection<Extension> filters = null;
		if(dataList!=null) {
			filters = ContentTypeRegistry.getInstance().getFilters(dataList.getContentType(), true);
		}
		
		DefaultComboBoxModel<Object> model = (DefaultComboBoxModel<Object>) filterSelect.getModel();
		model.removeAllElements();
		model.addElement(dummyEntry);
		if(filters!=null && !filters.isEmpty()) {
			for(Object item : filters) {
				model.addElement(item);
			}
		}
		model.setSelectedItem(dummyEntry);
		
		boolean enabled = model.getSize()>1;
		filterSelect.setEnabled(enabled);
		filterEditButton.setEnabled(enabled);
		
		boolean visible = model.getSize()>1;
		filterSelect.setVisible(visible);
		filterEditButton.setVisible(visible);
	}
	
	protected void refreshTextOutline() {
		if(outlineToggleButton==null || textArea==null) {
			return;
		}
		
		Object item = list.getSelectedValue();
		if(outlineToggleButton.isSelected() && item instanceof TextItem) {
			textArea.setText(((TextItem)item).getText());
			textArea.setVisible(true);
		} else {
			textArea.setText(null);
			textArea.setVisible(false);
		}
	}
	
	protected void setFilter(Filter filter) {
		if(this.filter==filter) {
			return;
		}
		
		Filter oldValue = this.filter;
		this.filter = filter;
		
		getSelectionModel().clearSelection();
		getFilteredListModel().setFilter(filter);
		
		if(filterEditButton!=null) {
			filterEditButton.setEnabled(filter instanceof Editable);
		}
		
		firePropertyChange("filter", oldValue, filter); //$NON-NLS-1$
	}
	
	public Filter getFilter() {
		return filter;
	}

	/**
	 * @see de.ims.icarus.util.annotation.AnnotationController#getAnnotationManager()
	 */
	@Override
	public AnnotationManager getAnnotationManager() {
		return annotationManager;
	}

	public void setAnnotationManager(AnnotationManager annotationManager) {
		if(this.annotationManager==annotationManager) {
			return;
		}
		
		if(this.annotationManager!=null) {
			this.annotationManager.removePropertyChangeListener(getHandler());
		}
		
		AnnotationManager oldValue = this.annotationManager;
		this.annotationManager = annotationManager;
		
		if(this.annotationManager!=null) {
			this.annotationManager.addPropertyChangeListener("displayMode", getHandler()); //$NON-NLS-1$
		}
		
		if(annotationControl!=null) {
			annotationControl.setAnnotationManager(annotationManager);
		}
		
		firePropertyChange("annotationManager", oldValue, annotationManager); //$NON-NLS-1$
		
		list.repaint();
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		
		if(!(data instanceof DataList))
			throw new UnsupportedPresentationDataException(
					"Data is not of required type '"+DataList.class+"' : "+data.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
	
		
		displayData((DataList<T>) data, options);
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		displayData(null, null);
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		clear();
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return dataList!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return dataList;
	}

	/**
	 * @see de.ims.icarus.ui.view.ListPresenter#getListModel()
	 */
	@Override
	public ListModel<?> getListModel() {
		return getFilteredListModel();
	}

	/**
	 * @see de.ims.icarus.ui.view.ListPresenter#getSelectionModel()
	 */
	@Override
	public ListSelectionModel getSelectionModel() {
		if(listSelectionModel==null) {
			listSelectionModel = new DefaultListSelectionModel();
			listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return listSelectionModel;
	}

	/**
	 * @see de.ims.icarus.ui.view.ListPresenter#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return dataList==null ?	null : dataList.getContentType();
	}
	
	protected Filter getFilter(Extension extension) {
		if(extension==null) {
			return null;
		}
		
		if(filterInstances==null) {
			filterInstances = new HashMap<>();
		}
		
		Filter filter = filterInstances.get(extension);
		
		if(filter==null) {
			try {
				filter = (Filter) PluginUtil.instantiate(extension);
				filterInstances.put(extension, filter);
			} catch (Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to instantiate filter: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}
		
		return filter;
	}
	
	protected class Handler implements ActionListener, ListSelectionListener,
			PropertyChangeListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(filterSelect==null || filterEditButton==null
					|| outlineToggleButton==null) {
				return;
			}
			
			if(e.getSource()==filterEditButton) {
				//TODO Show edit dialog
			} else if(e.getSource()==outlineToggleButton) {
				refreshTextOutline();
			} else if(e.getSource()==filterSelect) {
				Filter filter = null;
				Object selectedValue = filterSelect.getSelectedItem();
				if(selectedValue instanceof Extension) {
					filter = getFilter((Extension)selectedValue);
				}
				
				setFilter(filter);
			}
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			refreshTextOutline();
		}

		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			list.repaint(list.getVisibleRect());
		}
	}
}
