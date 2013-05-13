/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.dependency;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.plugins.core.Perspective;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.plugins.dependency.graph.DependencyGraphPresenter;
import net.ikarus_systems.icarus.plugins.language_tools.LanguageToolsConstants;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.view.UnsupportedPresentationDataException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyTestPerspective extends Perspective {

	public DependencyTestPerspective() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.Perspective#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		collectViewExtensions();
		defaultDoLayout(container);
	}

	@Override
	protected void collectViewExtensions() {
		// TODO Auto-generated method stub
		super.collectViewExtensions();
	}

}
