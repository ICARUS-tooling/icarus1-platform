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
package de.ims.icarus.plugins.search_tools.view.editor;

import de.ims.icarus.search_tools.SearchDescriptor;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class QueryEditor implements Editor<SearchDescriptor> {
	
	private QueryEditorView owner;
	protected SearchDescriptor searchDescriptor;

	public final void commit() throws Exception {
		if(owner==null)
			throw new IllegalStateException("No owner assigned to commit query"); //$NON-NLS-1$
		
		owner.commitQuery();
	}
	
	void setOwner(QueryEditorView owner) {
		if(this.owner!=null && this.owner!=owner)
			throw new IllegalStateException("Owner of editor already assigned: "+this.owner); //$NON-NLS-1$
		this.owner = owner;
	}
	
	public abstract boolean supports(ContentType contentType);
	
	@Override
	public SearchDescriptor getEditingItem() {
		return searchDescriptor;
	}

	@Override
	public void resetEdit() {
		// not-needed
	}

	@Override
	public boolean hasChanges() {
		// not-needed
		return false;
	}

	@Override
	public void close() {
		// no-op
	}
}
