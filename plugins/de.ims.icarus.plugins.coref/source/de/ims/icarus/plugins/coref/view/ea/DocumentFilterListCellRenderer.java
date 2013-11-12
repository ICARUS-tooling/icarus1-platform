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
package de.ims.icarus.plugins.coref.view.ea;

import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.plugins.coref.view.DocumentListCellRenderer;
import de.ims.icarus.ui.list.FilterListCellRenderer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DocumentFilterListCellRenderer extends FilterListCellRenderer {

	private static final long serialVersionUID = -5231096330001611041L;
	
	private CoreferenceDocumentSet documentSet;

	/**
	 * @return the documentSet
	 */
	public CoreferenceDocumentSet getDocumentSet() {
		return documentSet;
	}

	/**
	 * @param documentSet the documentSet to set
	 */
	public void setDocumentSet(CoreferenceDocumentSet documentSet) {
		this.documentSet = documentSet;
	}

	/**
	 * @see de.ims.icarus.ui.list.FilterListCellRenderer#getTextForValue(int, java.lang.Boolean)
	 */
	@Override
	protected String getTextForValue(int index, Boolean value) {
		CoreferenceDocumentData docData = documentSet==null ? null :
			documentSet.get(index);
		
		return DocumentListCellRenderer.defaultGetTextForValue(index, docData);
	}

}
