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
package de.ims.icarus.language.model;

/**
 * Defines the possibles types a {@link CorpusMember} can declare to represent by
 * its {@link CorpusMember#getMemberType()} method. Not although that a class can implement
 * multiple interfaces of the corpus framework, it can only ever be assigned to exactly one
 * <i>member role</i> specified by its {@code MemberType}.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum MemberType {
	FRAGMENT,
	MARKABLE,
	EDGE,
	CONTAINER,
	STRUCTURE,
	LAYER;
}
