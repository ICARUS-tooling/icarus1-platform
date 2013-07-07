/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramHistoryTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 5225508135747626629L;

	public NGramHistoryTreeCellRenderer(){
		//noop
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		// TODO Auto-generated method stub
		return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
	}

}
