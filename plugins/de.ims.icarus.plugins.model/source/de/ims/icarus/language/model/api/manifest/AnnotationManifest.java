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

import java.util.List;

import de.ims.icarus.language.model.util.ValueType;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AnnotationManifest extends MemberManifest {

	/**
	 * Returns the <i>base-name</i> of the key this manifest
	 * describes.
	 * @return
	 */
	String getKey();

	/**
	 * Returns a list of supported aliases that can be used for this
	 * manifest's key. If the key does not have any aliases this method
	 * should return an empty list.
	 * @return
	 */
	List<String> getAliases();

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
	 * <p>
	 * Note that in the case this method returns {@code true} <i>at least one</i>
	 * of the following methods {@code must} return a valid object that describes
	 * the bounds of supported values:
	 * <ul>
	 * <li>{@link #getSupportedRange()}</li>
	 * <li>{@link #getSupportedValues()}</li>
	 * </ul>
	 * Not doing so violates the general manifest contract and renders helper
	 * objects that, wish to present the values to a user, useless.
	 *
	 * @return {@code true} if and only if <b>all</b> the possible values of the
	 * corresponding {@code key} are known.
	 * @see #getSupportedRange()
	 * @see #getSupportedValues()
	 */
	boolean isBounded();

	/**
	 * Returns an object that describes the set of available values for this annotation
	 * by means of a lower and upper bound or {@code null} if this annotation is either
	 * unbounded or the values are wrapped into an iterator obtainable via the
	 * {@link #getSupportedValues()} method. Note that as a convention the {@code ValueRange}
	 * class should only wrap bound objects that implement the {@link Comparable} interface
	 * so that there is an easy way to actually use the bounds provided by the range object.
	 * Since the returned {@code ValueRange} only provides the boundary values, the
	 * {@link #getValueType()} method must be used to determine the type of those bounds.
	 *
	 * @return
	 * @see Comparable
	 */
	ValueRange getSupportedRange();

	/**
	 * Returns a new iterator to traverse possible values of this annotation or
	 * {@code null} if the set of possible annotations is unbounded. Note that
	 * for very large sets of values (especially numerical), it is far cheaper to
	 * use the {@link #getSupportedRange()} method and return a {@code ValueRange}
	 * object that describes the collection of supported values by means of an
	 * lower and upper bound, instead of generating an iterator that traverses all
	 * the values one by one.
	 * @return
	 */
	ValueSet getSupportedValues();

	/**
	 * Returns the type of this annotation
	 */
	ValueType getValueType();

	/**
	 * For annotations of type {@value ValueType#CUSTOM} this method returns the
	 * required {@code ContentType}. For all other value types, the returned value
	 * is {@code null}.
	 *
	 * @return
	 */
	ContentType getContentType();
}
