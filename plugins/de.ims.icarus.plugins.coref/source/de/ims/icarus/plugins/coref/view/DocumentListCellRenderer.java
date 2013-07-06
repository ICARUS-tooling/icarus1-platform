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


public class DocumentListCellRenderer extends TooltipListCellRenderer {

	private static final long serialVersionUID = -7249622078036629896L;

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
		
		String header = (String) docData.getProperty(CoreferenceDocumentData.DOCUMENT_HEADER_PROPERTY);
		if(header==null) {
			header = "document "+index; //$NON-NLS-1$
		}
		return header;
	}
}