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
package de.ims.icarus.plugins.coref.search;

import de.ims.icarus.io.Loadable;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.util.NamedObject;
import de.ims.icarus.util.data.AbstractDataList;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentSearchTarget extends AbstractDataList<CoreferenceDocumentData> implements Loadable, NamedObject {
	
	private final DocumentSetDescriptor documentSet;
	private final AllocationDescriptor  allocation;
	
	public CoreferenceDocumentSearchTarget(DocumentSetDescriptor documentSet,
			AllocationDescriptor allocation) {
		if(documentSet==null)
			throw new IllegalArgumentException("Invalid document set descriptor"); //$NON-NLS-1$
		
		this.documentSet = documentSet;
		this.allocation = allocation;
	}

	public DocumentSetDescriptor getDocumentSet() {
		return documentSet;
	}

	public AllocationDescriptor getAllocation() {
		return allocation;
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoaded()
	 */
	@Override
	public boolean isLoaded() {
		return documentSet.isLoaded() && (allocation==null || allocation.isLoaded());
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoading()
	 */
	@Override
	public boolean isLoading() {
		return documentSet.isLoading() || (allocation!=null && allocation.isLoading());
	}

	/**
	 * @see de.ims.icarus.io.Loadable#load()
	 */
	@Override
	public void load() throws Exception {
		// Wait for document set to finish loading
		while(documentSet.isLoading());
		if(!documentSet.isLoaded()) {
			documentSet.load();
		}
		
		if(allocation==null) {
			return;
		}

		// Wait for allocation to finish loading
		while(allocation.isLoading());
		if(!allocation.isLoaded()) {
			allocation.load();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CoreferenceDocumentSearchTarget) {
			CoreferenceDocumentSearchTarget other = (CoreferenceDocumentSearchTarget)obj;
			return documentSet==other.documentSet && allocation==other.allocation;
		}
		return false;
	}

	@Override
	public String toString() {
		return getName();
	}

	protected CoreferenceDocumentSet getSet() {
		return documentSet.get(); 
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		CoreferenceDocumentSet set = getSet();
		return set==null ? 0 : set.size();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#get(int)
	 */
	@Override
	public CoreferenceDocumentData get(int index) {
		CoreferenceDocumentSet set = getSet();
		return set==null ? null : set.get(index);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		CoreferenceDocumentSet set = getSet();
		return set==null ? null : set.getContentType();
	}

	/**
	 * @see de.ims.icarus.util.NamedObject#getName()
	 */
	@Override
	public String getName() {
		String name = documentSet.getName();
		if(allocation!=null) {
			name += " ["+allocation.getName()+"]"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return name;
	}
}
