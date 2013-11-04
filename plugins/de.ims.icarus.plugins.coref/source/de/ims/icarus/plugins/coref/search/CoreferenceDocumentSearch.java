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

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentResultAnnotator;
import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.annotation.ResultAnnotator;
import de.ims.icarus.search_tools.tree.AbstractTreeSearch;
import de.ims.icarus.search_tools.tree.TargetTree;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.DataList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentSearch extends AbstractTreeSearch {

	public CoreferenceDocumentSearch(SearchFactory factory,
			SearchQuery query, Options parameters, Object target) {
		super(factory, query, parameters, target);
		
		CoreferenceAllocation allocation = null;
		
		AllocationDescriptor alloc = getSearchTarget().getAllocation();
		if(alloc!=null) {
			allocation = alloc.get();
		}
		
		getParameters().put("goldAllocation", allocation); //$NON-NLS-1$
		getParameters().put("allocation", allocation); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.AbstractTreeSearch#createSource(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected DataList<CoreferenceDocumentData> createSource(Object target) {
		if(target==null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$
		
		if(!(target instanceof CoreferenceDocumentSearchTarget))
			throw new IllegalArgumentException("Unsupported target type: "+target.getClass()); //$NON-NLS-1$
		
		return (DataList<CoreferenceDocumentData>) target;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.AbstractTreeSearch#createAnnotator()
	 */
	@Override
	protected ResultAnnotator createAnnotator() {
		return new CoreferenceDocumentResultAnnotator(baseRootMatcher);
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.AbstractTreeSearch#createTargetTree()
	 */
	@Override
	protected TargetTree createTargetTree() {
		return new DocumentTargetTree();
	}
	
	public CoreferenceDocumentSearchTarget getSearchTarget() {
		return (CoreferenceDocumentSearchTarget) super.getTarget();
	}

	@Override
	protected Options createTreeOptions() {
		Options options = new Options();
		
		// Save the selected allocation (not its descriptor!)
		AllocationDescriptor alloc = getSearchTarget().getAllocation();
		if(alloc!=null) {
			options.put("allocation", alloc.get()); //$NON-NLS-1$
		}
		
		return options;
	}
}
