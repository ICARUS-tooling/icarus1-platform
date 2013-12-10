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
package de.ims.icarus.language.coref;

import javax.swing.Icon;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum CorefErrorType implements Identity {
	TRUE_POSITIVE_MENTION("truePositiveMention", false), //$NON-NLS-1$
	FALSE_POSITIVE_MENTION("falsePositiveMention", false), //$NON-NLS-1$
	FALSE_NEGATIVE_MENTION("falseNegativeMention", false), //$NON-NLS-1$
	FOREIGN_CLUSTER_HEAD("foreignClusterHead", true), //$NON-NLS-1$
	HALLUCINATED_HEAD("hallucinatedHead", true), //$NON-NLS-1$
	INVALID_CLUSTER_START("invalidClusterStart", true); //$NON-NLS-1$

	
	private final String key;
	private final boolean edgeRelated;
	
	private CorefErrorType(String key, boolean edgeRelated) {
		this.key = key;
		this.edgeRelated = edgeRelated;
	}
	
	public String getKey() {
		return key;
	}

	/**
	 * @return the edgeRelated
	 */
	public boolean isEdgeRelated() {
		return edgeRelated;
	}

	@Override
	public String getId() {
		return getName();
	}

	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.coref.errorType."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.coref.errorType."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public Object getOwner() {
		return CorefErrorType.class;
	}

}
