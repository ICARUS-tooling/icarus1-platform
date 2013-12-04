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
package de.ims.icarus.language.model.standard.manifest;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.model.manifest.LayerManifest;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AbstractLayerManifest extends AbstractManifest implements LayerManifest {
	
	private List<Prerequisite> prerequisites = new ArrayList<>();

	/**
	 * @see de.ims.icarus.language.model.manifest.LayerManifest#getPrerequisites()
	 */
	@Override
	public List<Prerequisite> getPrerequisites() {
		return CollectionUtils.getListProxy(prerequisites);
	}

	public void addPrerequiriste(Prerequisite prerequisite) {
		if(prerequisite==null)
			throw new NullPointerException("Invalid prerequisite"); //$NON-NLS-1$
		
		if(prerequisites.contains(prerequisite))
			throw new IllegalArgumentException("Duplicate prerequisite: "+CorpusUtils.getName(prerequisite)); //$NON-NLS-1$
		
		prerequisites.add(prerequisite);
	}
	
	public void removePrerequisite(Prerequisite prerequisite) {
		if(prerequisite==null)
			throw new NullPointerException("Invalid prerequisite"); //$NON-NLS-1$
		
		if(!prerequisites.remove(prerequisite))
			throw new IllegalArgumentException("Unknown prerequisite: "+CorpusUtils.getName(prerequisite)); //$NON-NLS-1$
	}
}
