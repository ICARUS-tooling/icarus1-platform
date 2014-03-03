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
package de.ims.icarus.language.model.api.manifest;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ContextWriterManifest extends MemberManifest {

	/**
	 * If the writer is not defined via a specific class but rather
	 * as an abstract format specification, this method returns the
	 * id of the format that was used for registration with the
	 * {@code CorpusRegistry}. Note that a {@code ContextReaderManifest}
	 * must either define a dedicated {@link Implementation} section or
	 * return a valid format id!
	 *
	 * @return
	 */
	String getFormatId();
}
