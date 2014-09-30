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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.ui.graph;

import de.ims.icarus.config.ConfigDelegate;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.plugins.dependency.graph.DependencyGraphPresenter;
import de.ims.icarus.plugins.jgraph.layout.GraphRenderer;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotationManager;
import de.ims.icarus.util.annotation.AnnotationControl;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodySentenceGraphPresenter extends DependencyGraphPresenter {

	private static final long serialVersionUID = 7817927685989029383L;


	@Override
	protected void loadPreferences() {
		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		setAutoZoomEnabled(config.getBoolean(
				"plugins.jgraph.appearance.prosody.autoZoom")); //$NON-NLS-1$
		setCompressEnabled(config.getBoolean(
				"plugins.jgraph.appearance.prosody.compressGraph")); //$NON-NLS-1$
	}

	@Override
	protected ConfigDelegate createConfigDelegate() {
		return new GraphConfigDelegate("plugins.jgraph.appearance.prosody", null); //$NON-NLS-1$
	}

	@Override
	protected GraphRenderer createDefaultGraphRenderer() {
		return new ProsodyGraphRenderer();
	}

	@Override
	protected AnnotationControl createAnnotationControl() {
		AnnotationControl annotationControl = super.createAnnotationControl();
		annotationControl.setAnnotationManager(new ProsodicAnnotationManager());

		return annotationControl;
	}

	@Override
	public ProsodicAnnotationManager getAnnotationManager() {
		return (ProsodicAnnotationManager) super.getAnnotationManager();
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.GraphPresenter#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return ProsodyUtils.getProsodySentenceContentType();
	}
}
