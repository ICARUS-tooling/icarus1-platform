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
package de.ims.icarus.language.coref;

import de.ims.icarus.util.CompactProperties;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public abstract class CorefMember {

	@Link
	protected CompactProperties properties;

	protected CorefMember() {
		// no-op
	}

	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}

	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new CompactProperties();
		}

		properties.put(key, value);
	}

	public void setProperties(CompactProperties properties) {
		this.properties = properties;
	}

	public CompactProperties getProperties() {
		return properties;
	}

	protected CompactProperties cloneProperties() {
		return properties==null ? null : properties.clone();
	}
}
