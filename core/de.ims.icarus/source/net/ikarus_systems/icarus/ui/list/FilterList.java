/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.list;


import javax.swing.JList;
import javax.swing.ListModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FilterList extends JList<Boolean> {

	private static final long serialVersionUID = -6538597249341306029L;

	public FilterList(FilterListModel model) {
		super(model);

		setCellRenderer(new FilterListCellRenderer());
	}
	
	public FilterList() {
		this(new FilterListModel());
	}
	
	public void setSize(int newSize) {
		getModel().setSize(newSize);
	}
	
	public void setElementAt(int index, boolean value) {
		getModel().setElementAt(index, value);
	}
	
	public void flipElementAt(int index) {
		getModel().flipElementAt(index);
	}
	
	@Override
	public FilterListModel getModel() {
		return (FilterListModel) super.getModel();
	}
	
	@Override
	public void setModel(ListModel<Boolean> model) {
		if(!(model instanceof FilterListModel))
			throw new IllegalArgumentException("Unsupported model type: "+model.getClass()); //$NON-NLS-1$
		
		super.setModel(model);
	}
	
	public void clear() {
		getModel().clear();
	}
	
	public void fill() {
		getModel().fill();
	}
}
