/*
 * $Revision: 26 $
 * $Date: 2013-04-21 19:52:49 +0200 (So, 21 Apr 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.language_tools/source/net/ikarus_systems/icarus/language/treebank/swing/TreebankListCellRenderer.java $
 *
 * $LastChangedDate: 2013-04-21 19:52:49 +0200 (So, 21 Apr 2013) $ 
 * $LastChangedRevision: 26 $ 
 * $LastChangedBy: mcgaerty $
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
 * @version $Id: TreebankListCellRenderer.java 26 2013-04-21 17:52:49Z mcgaerty $
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
