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
package de.ims.icarus.search_tools.corpus;

import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankDescriptor;
import de.ims.icarus.language.treebank.TreebankListDelegate;
import de.ims.icarus.language.treebank.TreebankRegistry;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchFactory;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractCorpusSearchFactory implements SearchFactory {

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getSerializedTarget(de.ims.icarus.search_tools.Search)
	 */
	@Override
	public String getSerializedTarget(Search s) {
		AbstractCorpusSearch search = (AbstractCorpusSearch) s;
		TreebankListDelegate delegate = (TreebankListDelegate) search.getTarget();
		Treebank treebank = delegate.getTreebank();
		TreebankDescriptor descriptor = TreebankRegistry.getInstance().getDescriptor(treebank);

		return descriptor.getId();
	}

	public Object resolveTarget(String name) {
		TreebankDescriptor target = TreebankRegistry.getInstance().getDescriptor(name);

		if(target==null)
			throw new IllegalArgumentException("No such target treebank: "+name); //$NON-NLS-1$

		return TreebankRegistry.getInstance().getListDelegate(target.getTreebank());
	}
}
