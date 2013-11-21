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
	 * Provides a localized identification of a certain key.
	 * 
	 * @param key The <i>base-name</i> of the key to be localized.
	 * @return The (optionally localized) name of the provided key.
	 * @throws NullPointerException if the {@code key} is {@code null}
	 * @throws IllegalArgumentException if the given {@code key} is 
	 * unknown to this manifest
	 */
	String getName(String key);
	
	/**
	 * 
	 * Provides a localized description of a certain key.
	 * <p>
	 * This is an optional method.
	 * 
	 * @param key The <i>base-name</i> of the key to be localized.
	 * @return The (optionally localized) description of the provided key or {@code null}.
	 * @throws NullPointerException if the {@code key} is {@code null}
	 * @throws IllegalArgumentException if the given {@code key} is 
	 * unknown to this manifest
	 */
	String getDescription(String key);

	/**
	 * Tells whether or not a certain {@code key} has a predefined
	 * set of possible values in this annotation. A return value of 
	 * {@code true} indicates that a call to {@link #getValueSet(String)}
	 * with the same {@code key} is guaranteed to return a non-empty {@code Set}
	 * that holds all the values allowed for this {@code key}.
	 * Note that a key should only be declared as bounded when <b>all</b> the
	 * possible values are known!
	 * <p>
	 * This is an optional method that will be used by visualizations and other
	 * user interfaces to improve usability by assisting the user. For example
	 * a dialog allowing for search constraints to be defined could present the 
	 * user a drop-down menu containing all the possible values. 
	 * 
	 * @param key The <i>base-name</i> of the key in question
	 * @return {@code true} if and only if <b>all</b> the possible values of the
	 * given {@code key} are known.
	 * @throws NullPointerException if the {@code key} is {@code null}
	 * @throws IllegalArgumentException if the given {@code key} is 
	 * unknown to this manifest.
	 */
	boolean isBounded(String key);
	
	/**
	 * Returns all the possible values used to annotate a given {@code key}.
	 * Note that before calling this method one should always check if a
	 * certain {@code key} is bounded via {@link #isBounded(String)} since
	 * this method is encouraged to throw an {@code IllegalArgumentException}
	 * if the key in question happens to be unbounded.
	 * 
	 * @param key The <i>base-name</i> of the key in question
	 * @return The non-empty collection of possible values for the
	 * given {@code key}.
	 * @throws NullPointerException if the {@code key} is {@code null}
	 * @throws IllegalArgumentException if the given {@code key} is 
	 * unknown to this manifest or if it is unbounded.
	 */
	Set<String> getValueSet(String key);
	
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
