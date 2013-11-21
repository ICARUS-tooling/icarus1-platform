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
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface MarkableLevels {

	public static final String TOKENS = "tokens"; //$NON-NLS-1$

	public static final String SPANS = "spans"; //$NON-NLS-1$

	public static final String SENTENCES = "sentences"; //$NON-NLS-1$

	public static final String CLUSTERS = "clusters"; //$NON-NLS-1$

	public static final String SECTIONS = "sections"; //$NON-NLS-1$

	public static final String CHAPTERS = "chapters"; //$NON-NLS-1$

	public static final String DOCUMENTS = "documents"; //$NON-NLS-1$
}
