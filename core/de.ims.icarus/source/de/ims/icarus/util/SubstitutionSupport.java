/*
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
