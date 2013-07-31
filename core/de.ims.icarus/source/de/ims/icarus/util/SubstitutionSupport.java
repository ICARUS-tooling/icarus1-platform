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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SubstitutionSupport {

	protected Map<String, Integer> substitutions;

	protected List<String> resubstitutions;

	public SubstitutionSupport() {
		this(100);
	}

	public SubstitutionSupport(int size) {
		substitutions = new HashMap<>(size);
		resubstitutions = new ArrayList<>(size);
	}

	public int substitute(String value) {
		Integer id = substitutions.get(value);
		if (id == null) {
			id = resubstitutions.size();
			resubstitutions.add(value);
			substitutions.put(value, id);
		}

		return id;
	}

	public int getSubstitution(String value) {
		Integer id = substitutions.get(value);
		return id == null ? -1 : id;
	}

	public String resubstitute(int value) {
		return resubstitutions.get(value);
	}

	public void reset() {
		substitutions.clear();
		resubstitutions.clear();
	}

	public int size() {
		return resubstitutions.size();
	}
}
