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
package de.ims.icarus.model.api.driver;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Candidates {

	private final IndexSet[] indices;

	private final boolean complete;

	private final String message;

	public static Candidates wrap(IndexSet[] indices) {
		return indices==null || indices.length==0 ? null : new Candidates(indices);
	}

	public Candidates(IndexSet[] indices) {
		this(indices, true, null);
	}

	public Candidates(IndexSet[] indices, boolean complete) {
		this(indices, complete, null);
	}

	public Candidates(IndexSet[] indices, String message) {
		this(indices, true, message);
	}

	public Candidates(IndexSet[] indices, boolean complete, String message) {
		if (indices == null)
			throw new NullPointerException("Invalid indices"); //$NON-NLS-1$

		this.indices = indices;
		this.complete = complete;
		this.message = message;
	}

	/**
	 * @return the indices
	 */
	public IndexSet[] getIndices() {
		return indices;
	}

	/**
	 * @return the complete
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
}
