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
package de.ims.icarus.ui.list;


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
		this(model, new FilterListCellRenderer());
	}

	public FilterList(FilterListModel model, FilterListCellRenderer renderer) {
		super(model);

		setCellRenderer(renderer);
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
