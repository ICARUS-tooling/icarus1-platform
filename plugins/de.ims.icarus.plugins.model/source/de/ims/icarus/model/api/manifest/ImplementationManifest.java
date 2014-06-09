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
package de.ims.icarus.model.api.manifest;

import de.ims.icarus.model.api.raster.Rasterizer;
import de.ims.icarus.model.xml.XmlElement;


/**
 * Models the ability to describe foreign implementations for an interface.
 * It is the responsibility of this manifest to instantiate the respective
 * implementation when asked to. While the most simple version of this manifest
 * might simply contain the fully qualified class name of the implementation, the
 * manifest itself is way more flexible. With the help of the optional {@code OptionsManifest}
 * within an implementation manifest the user can customize the implementation according
 * to his needs and the amount of modifiable properties.
 * <p>
 * Note that this manifest interface forces implementations to also implement {@link XmlElement}.
 * This is because implementations of this manifest interface can be originate from outside the
 * default manifest domain (e.g. declared via plugin extensions) and therefore the manifest
 * framework has no knowledge about their individual properties and how to represent them in xml form.
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ImplementationManifest extends ModifiableManifest {

	/**
	 * Creates an instance of the class this implementation manifest describes. It is up
	 * to the manifest whether or not it honors customization by the user. Manifest implementations
	 * are allowed to use the singleton pattern to share instances of the implementing class
	 * for multiple targets if this is possible (e.g. {@link Rasterizer} implementations for fragment
	 * layers).
	 *
	 * @param resultClass
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassCastException
	 */
	<T extends Object> T instantiate(Class<T> resultClass) throws
			ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException;

	/**
	 * Returns the class described by this manifest, loading and initializing it if necessary.
	 *
	 * @return
	 */
	Class<?> getImplementationClass() throws ClassNotFoundException;
}
