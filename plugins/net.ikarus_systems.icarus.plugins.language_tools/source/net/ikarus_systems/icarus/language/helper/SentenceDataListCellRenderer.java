/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.helper;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import net.ikarus_systems.icarus.language.SentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SentenceDataListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 6024979433069005270L;

	public SentenceDataListCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {

		if(value instanceof SentenceData) {
			String[] tokens = ((SentenceData)value).getForms();
			StringBuilder sb = new StringBuilder(tokens.length*20);
			
			for(int i=0; i<tokens.length; i++) {
				if(i>0) {
					sb.append(" "); //$NON-NLS-1$
				}
				sb.append(tokens[i]);
			}
			
			value = sb.toString();
		}
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		return this;
	}

}
