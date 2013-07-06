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

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import de.ims.icarus.ui.UIUtil;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContentTypeListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 6080540503573355726L;

	public ContentTypeListCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		ContentType contentType = null;
		if(value instanceof ContentType) {
			contentType = (ContentType) value;
		} else if(value instanceof String) {
			try {
				contentType = ContentTypeRegistry.getInstance().getType((String)value);
			} catch(IllegalArgumentException e) {
				// ignore
			}
		}
		
		if(contentType!=null) {
			value = contentType.getName();
		}
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if(contentType!=null) {
			setIcon(contentType.getIcon());
			setToolTipText(UIUtil.toSwingTooltip(contentType.getDescription()));
		} else {
			setIcon(null);
			setToolTipText(null);
		}
		
		return this;
	}

}
