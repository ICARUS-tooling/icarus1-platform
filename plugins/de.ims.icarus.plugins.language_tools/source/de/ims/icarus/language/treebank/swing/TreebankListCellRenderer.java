/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.treebank.swing;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankDescriptor;
import de.ims.icarus.language.treebank.TreebankInfo;
import de.ims.icarus.ui.UIUtil;



/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = -2941175073501767602L;

	/**
	 * 
	 */
	public TreebankListCellRenderer() {
		UIUtil.disableHtml(this);
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		setToolTipText(null);
		
		if(value instanceof Treebank) {
			value = ((Treebank)value).getName();
		} else if(value instanceof TreebankDescriptor) {
			value = ((TreebankDescriptor)value).getName();
		} else if(value instanceof TreebankInfo) {
			value = ((TreebankInfo)value).getTreebankName();
		}
		
		return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	}

}
