/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.data;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.ExtensionListCellRenderer;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.NavigationControl;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.helper.Editable;
import net.ikarus_systems.icarus.ui.helper.FilteredListModel;
import net.ikarus_systems.icarus.ui.view.ListPresenter;
import net.ikarus_systems.icarus.ui.view.UnsupportedPresentationDataException;
import net.ikarus_systems.icarus.util.Filter;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.PropertyChangeSource;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DataListPresenter<T extends Object> extends PropertyChangeSource implements ListPresenter {
	
	protected DataListModel<T> dataListModel;
	protected DataList<T> dataList;
	
	protected JList<T> list;
	protected ListSelectionModel listSelectionModel;
	
	protected Filter filter;
	protected FilteredListModel<T> filteredModel;
	
	protected JPanel contentPanel;
	protected JComboBox<Object> filterSelect;
	protected JButton filterEditButton;
	protected Map<Extension, Filter> filterInstances;
	
	protected final String dummyEntry = "minus"; //$NON-NLS-1$
	
	protected Handler handler;

	public DataListPresenter() {
		// no-op
	}
	
	protected JPanel createContentPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		list = createList();
		
		NavigationControl navigationControl = createNavigationControl();

		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(null);
		
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(navigationControl.getToolBar(), BorderLayout.NORTH);
		
		refresh();
		
		return panel;
	}
	
	protected JList<T> createList() {
		JList<T> list = new JList<>(getFilteredListModel());
		list.setSelectionModel(getSelectionModel());
		list.setBorder(UIUtil.defaultContentBorder);
		
		return list;
	}
	
	protected NavigationControl createNavigationControl() {	
		
		Options options = new Options();
		Object[] rightContent = {
				getFilterSelect(),
				getFilterEditButton(),
		};
		options.put(NavigationControl.RIGHT_CONTENT_OPTION, rightContent);
		
		return new NavigationControl(list, options);
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
	 * @see net.ikarus_systems.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPanel==null) {
			contentPanel = createContentPanel();
		}
		
		return contentPanel;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#supports(net.ikarus_systems.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible(
				"DataListContentType", type); //$NON-NLS-1$
	}
	
	protected void displayData(DataList<T> data, Options options) {
		if(dataList==data) {
			return;
		}
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		dataList = data;
		filter = (Filter) options.get(Options.FILTER);
		int index = options.get(Options.INDEX, -1);
		
		refresh();
		
		if(index!=-1) {
			getSelectionModel().setSelectionInterval(index, index);
		} else {
			getSelectionModel().clearSelection();
		}
	}
	
	protected void refresh() {
		getDataListModel().setDataList(dataList);
		getFilteredListModel().setFilter(filter);
		
		refreshFilterOptions();
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
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#present(java.lang.Object, net.ikarus_systems.icarus.util.Options)
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
		
		
		// It is perfectly legal to 're-display' the current list since
		// we cannot be aware of all changes within
		/*if(dataList==data) {
			return;
		}*/
		
		dataList = (DataList<T>)data;
				
		if(contentPanel!=null) {
			refresh();
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		displayData(null, null);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		clear();
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return dataList!=null;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return dataList;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.ListPresenter#getListModel()
	 */
	@Override
	public ListModel<?> getListModel() {
		return getFilteredListModel();
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.ListPresenter#getSelectionModel()
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
	 * @see net.ikarus_systems.icarus.ui.view.ListPresenter#getContentType()
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
	
	protected class Handler implements ActionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(filterSelect==null || filterEditButton==null) {
				return;
			}
			
			if(e.getSource()==filterEditButton) {
				//TODO Show edit dialog
			} else if(e.getSource()==filterSelect) {
				Filter filter = null;
				Object selectedValue = filterSelect.getSelectedItem();
				if(selectedValue instanceof Extension) {
					filter = getFilter((Extension)selectedValue);
				}
				
				setFilter(filter);
			}
		}
		
	}
}
