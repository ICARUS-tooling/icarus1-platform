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
package de.ims.icarus.plugins.coref.view.graph;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import de.ims.icarus.language.coref.CorefMember;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.Span;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorefConverter extends XmlAdapter<String, CorefMember> {

	/**
	 * @see jakarta.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public CorefMember unmarshal(String v) throws Exception {
		if(v.indexOf('>')==-1) {
			return Span.parse(v);
		} else {
			return Edge.parse(v);
		}
	}

	/**
	 * @see jakarta.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public String marshal(CorefMember v) throws Exception {
		return v.toString();
	}

}
