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
package de.ims.icarus.model.api;

/**
 * Specifies a member of a {@code Structure} object. In addition to being
 * a simple {@link Markable}, an {@code Edge} consists of a {@code source}
 * and {@code target} markable.
 * <p>
 * <b>Note</b> that while this interface specifies methods to change terminals
 * ({@link #setSource(Markable)} and {@link #setTarget(Markable)}) one should never
 * call them directly! They are necessary for the enclosing structure to modify
 * edge terminals. So to achieve the desired effect <b>always</b> use
 * {@link Structure#setTerminal(Edge, Markable, boolean)} on the structure object
 * obtained via {@link Edge#getStructure()}!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Edge extends Markable {

	Structure getStructure();

	Markable getSource();

	Markable getTarget();

	void setSource(Markable markable);

	void setTarget(Markable markable);

	boolean isDirected();
}
