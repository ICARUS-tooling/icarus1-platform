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
package de.ims.icarus.plugins.coref.view;

import java.awt.Component;

import javax.swing.JList;

import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.ui.helper.TooltipListCellRenderer;
import de.ims.icarus.util.StringUtil;


public class DocumentListCellRenderer extends TooltipListCellRenderer {

	private static final long serialVersionUID = -7249622078036629896L;
	
	private boolean showRowIndex = true;

	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		
		value = getTextForValue(index, (CoreferenceDocumentData) value);
		
		return super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
	}

	protected String getTextForValue(int index, CoreferenceDocumentData docData) {
		if(docData==null) {
			return null;
		}
		
		String id = docData.getId();
		
		if(id==null) {
			id = (String) docData.getProperty(CoreferenceDocumentData.DOCUMENT_HEADER_PROPERTY);
		}
		if(id==null) {
			id = "document "+StringUtil.formatDecimal(index); //$NON-NLS-1$
		}
		if(showRowIndex) {
			id = StringUtil.formatDecimal(index)+": "+id; //$NON-NLS-1$
		}
		
		return id;
	}

	public boolean isShowRowIndex() {
		return showRowIndex;
	}

	public void setShowRowIndex(boolean showRowIndex) {
		this.showRowIndex = showRowIndex;
	}
}