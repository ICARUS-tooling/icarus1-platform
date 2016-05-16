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
package de.ims.icarus.language.coref.registry;

import org.java.plugin.registry.Extension;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.DocumentSet;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.location.Location;

public class DefaultAllocationDescriptor extends AllocationDescriptor {

	private final DocumentSetDescriptor descriptor;
	
	public DefaultAllocationDescriptor(DocumentSetDescriptor descriptor) {
		if(descriptor==null)
			throw new NullPointerException("Invalid descriptor"); //$NON-NLS-1$
		
		this.descriptor = descriptor;
	}

	@Override
	public String getName() {
		String name = ResourceManager.getInstance().get(
				"plugins.coref.labels.defaultAllocation"); //$NON-NLS-1$
		CoreferenceAllocation allocation = getAllocation();
		if(allocation==null || allocation.size()==0) {
			name += " (empty)"; //$NON-NLS-1$
		}
		return name;
	}

	@Override
	public CoreferenceAllocation getAllocation() {
		if(descriptor==null) {
			return null;
		}
		DocumentSet documentSet = descriptor.getDocumentSet();
		if(documentSet==null) {
			return null;
		}
		return documentSet.getDefaultAllocation();
	}

	@Override
	public boolean isLoaded() {
		return true;
	}

	@Override
	public boolean isLoading() {
		return false;
	}

	@Override
	public void load() throws Exception {
		// no-op
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public Extension getReaderExtension() {
		return null;
	}

	@Override
	public void free() {
		// no-op
	}

	/**
	 * @return the descriptor
	 */
	public DocumentSetDescriptor getDescriptor() {
		return descriptor;
	}
}