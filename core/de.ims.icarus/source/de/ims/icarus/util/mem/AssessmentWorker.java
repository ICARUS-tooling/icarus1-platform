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
package de.ims.icarus.util.mem;

import java.util.concurrent.CancellationException;

import javax.swing.SwingWorker;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AssessmentWorker extends SwingWorker<MemoryFootprint, MemoryFootprint> {

	private final Object root;

	private FootprintBuilder builder;

	public AssessmentWorker(Object root) {
		if (root == null)
			throw new NullPointerException("Invalid root"); //$NON-NLS-1$

		this.root = root;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AssessmentWorker) {
			AssessmentWorker other = (AssessmentWorker)obj;
			return root==other.root;
		}
		return false;
	}

	/**
	 * @return the builder
	 */
	public FootprintBuilder getBuilder() {
		return builder;
	}

	/**
	 * @param builder the builder to set
	 */
	public void setBuilder(FootprintBuilder builder) {
		this.builder = builder;
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected MemoryFootprint doInBackground() throws Exception {

		if(builder==null) {
			builder = new FootprintBuilder();
		}

		MemoryFootprint footprint = builder.calculateFootprint(root);

		return footprint;
	}

	/**
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		try {
			MemoryFootprint footprint = get();
			dumpFootprint(footprint);
		} catch(CancellationException | InterruptedException e) {
			// ignore
		} catch(Exception e) {
			LoggerFactory.error(this, "Failed to assess object: "+root, e); //$NON-NLS-1$
		}
	}

	protected void dumpFootprint(MemoryFootprint footprint) {
		if (footprint == null)
			throw new NullPointerException("Invalid footprint"); //$NON-NLS-1$


		System.out.println(footprint);
		for(Class<?> clazz : footprint.getClasses()) {
			if(clazz!=String.class && clazz.getName().startsWith("java")) { //$NON-NLS-1$
				continue;
			}

			System.out.println(clazz+": " //$NON-NLS-1$
					+StringUtil.formatDecimal(footprint.getInstanceCount(clazz))
					+" -> " //$NON-NLS-1$
					+StringUtil.formatDecimal(footprint.getInstanceFootprint(clazz)));
		}
	}

}
