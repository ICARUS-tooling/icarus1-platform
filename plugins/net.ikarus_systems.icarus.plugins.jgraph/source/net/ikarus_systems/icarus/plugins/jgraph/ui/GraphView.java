/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.ui;

import javax.swing.JComponent;

import net.ikarus_systems.icarus.plugins.core.View;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class GraphView extends View {
	
	protected GraphPresenter presenter;

	protected GraphView() {
		// for subclasses
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		// TODO Auto-generated method stub

	}

}
