/*
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
			id = "document "+index; //$NON-NLS-1$
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