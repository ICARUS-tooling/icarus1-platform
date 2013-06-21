/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.tab;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import net.ikarus_systems.icarus.util.Exceptions;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TabLabel extends JLabel {

	private static final long serialVersionUID = 8922903169312874278L;
	
	private final JTabbedPane tabbedPane;
	
	public TabLabel(JTabbedPane tabbedPane) {			
		Exceptions.testNullArgument(tabbedPane, "tabbedPane"); //$NON-NLS-1$
		
		this.tabbedPane = tabbedPane;
		
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	}
	
	@Override
	public String getText() {
		int index = tabbedPane == null ? -1 : tabbedPane.indexOfTabComponent(this);
		return index==-1 ? null : tabbedPane.getTitleAt(index);
	}
	
	@Override
	public Icon getIcon() {
		int index = tabbedPane==null ? -1 : tabbedPane.indexOfTabComponent(this);
		return index==-1 ? null : tabbedPane.getIconAt(index);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.height = 21;
		return dim;
	}
}