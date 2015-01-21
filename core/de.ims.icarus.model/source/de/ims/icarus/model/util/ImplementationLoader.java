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
package de.ims.icarus.model.util;

import org.java.plugin.registry.Extension;

import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.api.manifest.ImplementationManifest.SourceType;
import de.ims.icarus.model.api.members.CorpusMember;
import de.ims.icarus.plugins.PluginUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ImplementationLoader {


	public <T extends Object> T instantiate(CorpusMember owner, ImplementationManifest manifest, Class<T> resultClass) {
		return instantiate(owner, manifest, resultClass, ""); //$NON-NLS-1$
	}

	public <T extends Object> T instantiate(CorpusMember owner, ImplementationManifest manifest, Class<T> resultClass, String msg) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$
		if (resultClass == null)
			throw new NullPointerException("Invalid resultClass"); //$NON-NLS-1$

		final SourceType sourceType = manifest.getSourceType();
		final boolean isFactory = manifest.isUseFactory();
		final String source = manifest.getSource();

		ClassLoader classLoader = manifest.getManifestLocation().getClassLoader();
		String classname = manifest.getClassname();

		Class<?> clazz = null;

		switch (sourceType) {
		case EXTENSION: {
			try {
				Extension extension = PluginUtil.getExtension(source);
				classLoader = PluginUtil.getClassLoader(extension);
				classname = extension.getParameter("class").valueAsString(); //$NON-NLS-1$
			} catch(Exception e) {
				throw new ModelException(ModelError.IMPLEMENTATION_NOT_FOUND,
						msg+" Unknown extension or extension is missing a valid 'class' parameter: "+source, e); //$NON-NLS-1$
			}
		} break;

		case EXTERN: {
			if(source==null) {

			} else {
				clazz =
			}
		} break;

		default:
			break;
		}

		if(clazz==null) {
			try {
				clazz = classLoader.loadClass(classname);
			} catch (ClassNotFoundException e) {
				throw new ModelException(owner.getCorpus(), ModelError.IMPLEMENTATION_NOT_FOUND,
						"Implementing rasterizer class for fragment layer not found: "+CorpusUtils.getName(owner), e); //$NON-NLS-1$
			}
		}
	}
}
