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
package de.ims.icarus.language.model.mutation;

import de.ims.icarus.language.model.AnnotationLayer;
import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.Structure;
import de.ims.icarus.language.model.mutation.batch.BatchMutator;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AnnotationLayerMutator extends Mutator<AnnotationLayer>, BatchMutator, LayerMutator {

	/**
	 * Deletes all annotations in the mutating layer
	 */
	void removeAllValues();

	/**
	 * Deletes in the mutating layer all annotations for
	 * the given {@code key}.
	 * 
	 * @param key The key for which annotations should be
	 * deleted
	 */
	void removeAllValues(String key);

	/**
	 * Removes from the mutating layer all annotations for the given
	 * markable.
	 * <p>
	 * If the {@code recursive} parameter is {@code true} and the supplied
	 * {@code markable} is a {@link Container} or {@link Structure} then all
	 * annotations defined for members of it should be removed as well.
	 * 
	 * @param markable the {@code Markable} for which annotations should be removed
	 * @param recursive if {@code true} removes all annotations defined for
	 * elements ({@code Markable}s and {@code Edge}s alike) in the supplied
	 * {@code Markable}
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 */
	void removeAllValues(Markable markable, boolean recursive);

	/**
	 * Assigns the given {@code value} as new annotation for the specified
	 * {@code Markable}, replacing any previously defined value. If the
	 * {@code value} argument is {@code null} any stored annotation for the
	 * {@code markable} will be deleted.
	 * 
	 * @param markable The {@code Markable} to change the annotation value for
	 * @param value the new annotation value or {@code null} if the annotation
	 * for the given {@code markable} should be deleted
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 */
	void setValue(Markable markable, Object value);


	/**
	 * Assigns the given {@code value} as new annotation for the specified
	 * {@code Markable} and {@code key}, replacing any previously defined value.
	 * If the {@code value} argument is {@code null} any stored annotation
	 * for the combination of {@code markable} and {@code key} will be deleted.
	 * 
	 * @param markable The {@code Markable} to change the annotation value for
	 * @param key the key for which the annotation should be changed
	 * @param value the new annotation value or {@code null} if the annotation
	 * for the given {@code markable} and {@code key} should be deleted
	 * @throws NullPointerException if the {@code markable} or {@code key}
	 * argument is {@code null}
	 */
	void setValue(Markable markable, String key, Object value);

	// BATCH OPERATIONS

	void batchRemoveAllValues();

	void batchRemoveAllValues(String key);

	void batchRemoveAllValues(Markable markable, boolean recursive);

	void batchSetValue(Markable markable, Object value);

	void batchSetValue(Markable markable, String key, Object value);
}
