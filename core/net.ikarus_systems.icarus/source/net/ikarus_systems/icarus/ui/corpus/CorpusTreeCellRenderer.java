/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.corpus;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.ui.IconRegistry;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 466713405139267604L;

	public CorpusTreeCellRenderer() {
		setLeafIcon(null);
		setClosedIcon(null);
		setOpenIcon(null);
	}

	/**
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {	
		
		/*if(value instanceof DefaultMutableTreeNode) {
			value = ((DefaultMutableTreeNode)value).getUserObject();
		}*/
		
		Icon icon = null;
		
		if(value instanceof Corpus) {
			value = ((Corpus)value).getName();
		} else if(value instanceof Extension) {
			value = ((Extension)value).getId();
			icon = IconRegistry.getGlobalRegistry().getIcon("class_obj.gif"); //$NON-NLS-1$
		}
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		
		setIcon(icon);
		
		return this;
	}
}
