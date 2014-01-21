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
package de.ims.icarus.language.model.manifest;

import java.util.Set;

import de.ims.icarus.language.model.MarkableLayer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AnnotationLayerManifest extends LayerManifest {

	/**
	 * Returns the available keys that can be used for annotation. This method
	 * must not return {@code null}! If the annotation only supports one value
	 * and therefore does not require keys at all, it should return an empty
	 * {@code Set}. In addition the returned {@code Set} should be considered
	 * as 'owned' by the manifest and be immutable.
	 *
	 * @return An immutable {@code Set} containing all the available keys used
	 * for annotations.
	 */
	Set<String> getAvailableKeys();

	/**
	 * Returns the manifest responsible for describing the given annotation key.
	 *
	 * @param key
	 * @return
	 * @throws NullPointerException if the {@code key} argument is {@code null}
	 * @throws IllegalArgumentException if the given {@code key} is unknown to
	 * this manifest
	 * @throws UnsupportedOperationException if the manifest does not declare any
	 * keys available
	 */
	AnnotationManifest getAnnotationManifest(String key);

	/**
	 * Returns the manifest that describes annotations on this layer that do
	 * not use an additional key. If this layer returns a non-empty {@code Set}
	 * of available keys via its {@link #getAvailableKeys()} and only supports
	 * annotation specified by those keys than this method may return {@code null}.
	 *
	 * @return
	 */
	AnnotationManifest getDefaultAnnotationManifest();

	/**
	 * Defines whether an {@code AnnotationLayer} derived from this manifest should
	 * be able to handle keys that have not been declared within a nested
	 * {@link AnnotationManifest}.
	 * <p>
	 * Note that when a format allows arbitrary properties on the annotation level
	 * and therefore decides to allow those <i>unknown keys</i> it loses some of
	 * the robustness a finite declaration of supported keys and their values provides!
	 *
	 * @return
	 */
	boolean allowUnknownKeys();

	/**
	 * Returns whether this layer only provides annotations for the members
	 * of the respective {@link MarkableLayer}'s direct container. A return
	 * value of {@code true} indicates that this layer may be queried for
	 * annotations of nested containers or structures/edges.
	 *
	 * @return {@code true} if and only if this layer includes annotations
	 * for nested containers or structures/edges on the target {@code MarkableLayer}
	 */
	boolean isDeepAnnotation();
}
