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
import java.util.List;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SubstitutionSupport {

	protected final TObjectIntMap<String> substitutions;

	protected final List<String> resubstitutions;

	public SubstitutionSupport() {
		this(100);
	}

	public SubstitutionSupport(int size) {
		substitutions = new TObjectIntHashMap<>(size, 0.5f, -1);
		resubstitutions = new ArrayList<>(size);
	}

	public int substitute(String value) {
		int id = substitutions.get(value);
		if (id == -1) {
			id = resubstitutions.size();
			resubstitutions.add(value);
			substitutions.put(value, id);
		}

		return id;
	}

	public int getSubstitution(String value) {
		return substitutions.get(value);
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

	public void set(String[] items) {
		substitutions.clear();
		resubstitutions.clear();

		for(int i=0; i<items.length; i++) {
			resubstitutions.add(items[i]);
			substitutions.put(items[i], i);
		}
	}
}
