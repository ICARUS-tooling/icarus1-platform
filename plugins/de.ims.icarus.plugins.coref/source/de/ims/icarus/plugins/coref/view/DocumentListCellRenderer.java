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

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.annotation.AnnotatedCoreferenceDocumentData;
import de.ims.icarus.search_tools.annotation.ResultAnnotation;
import de.ims.icarus.ui.list.TooltipListCellRenderer;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.annotation.Annotation;


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
	
	protected final StringBuilder sb = new StringBuilder(200);

	protected String getTextForValue(int index, CoreferenceDocumentData docData) {
		if(docData==null) {
			return null;
		}
		
		sb.setLength(0);		
		ConfigRegistry registry = ConfigRegistry.getGlobalRegistry();

		if(registry.getBoolean("plugins.coref.appearance.showRowIndex")) { //$NON-NLS-1$
			sb.append(StringUtil.formatDecimal(index)).append(": "); //$NON-NLS-1$
		}
		if(registry.getBoolean("plugins.coref.appearance.showSetIndex") //$NON-NLS-1$
				&& docData instanceof AnnotatedCoreferenceDocumentData) {
			Annotation annotation = ((AnnotatedCoreferenceDocumentData)docData).getAnnotation();
			if(annotation instanceof ResultAnnotation) {
				int setIndex = ((ResultAnnotation)annotation).getResultEntry().getIndex();
				sb.append(StringUtil.formatDecimal(setIndex));
			}
		}
		
		String id = docData.getId();		
		if(id==null) {
			id = (String) docData.getProperty(CoreferenceDocumentData.DOCUMENT_HEADER_PROPERTY);
		}
		if(id==null) {
			id = "document "+StringUtil.formatDecimal(index); //$NON-NLS-1$
		}
		
		sb.append(id);
		
		return id;
	}
}