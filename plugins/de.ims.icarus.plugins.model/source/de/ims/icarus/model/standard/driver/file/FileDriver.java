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
package de.ims.icarus.model.standard.driver.file;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.standard.driver.AbstractDriver;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FileDriver extends AbstractDriver {

	/**
	 * @param manifest
	 * @param corpus
	 * @throws ModelException
	 */
	protected FileDriver(ContextManifest manifest, Corpus corpus)
			throws ModelException {
		super(manifest, corpus);
		// TODO Auto-generated constructor stub
	}

}
