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
package de.ims.icarus.model.api.layer;

import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.ManifestOwner;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.api.members.Annotation;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.Markable;
import de.ims.icarus.model.api.members.Structure;
import de.ims.icarus.util.Collector;

/**
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AnnotationLayer extends Layer, ManifestOwner<AnnotationLayerManifest> {

	/**
	 * Returns the shared {@code AnnotationLayerManifest} that holds
	 * information about keys and possible values in this annotation.
	 *
	 * @return The manifest that describes this annotation
	 */
	@Override
	AnnotationLayerManifest getManifest();

//	/**
//	 * Returns the annotation for a given markable or {@code null} if that markable
//	 * has not been assigned an annotation value in this layer. Note that the returned
//	 * object can be either an actual value or an {@link Annotation} instance that wraps
//	 * a value and provides further information.
//	 *
//	 * @param markable
//	 * @return
//	 * @throws NullPointerException if the {@code markable} is {@code null}
//	 */
//	Object getValue(Markable markable);

	/**
	 * Collects all the keys in this layer which are mapped to valid annotation values for
	 * the given markable. This method returns {@code true} iff at least one key was added
	 * to the supplied {@code buffer}. Note that this method does <b>not</b> take
	 * default annotations into consideration, since they are not accessed via a dedicated
	 * key!
	 *
	 * @param markable
	 * @param buffer
	 * @return
	 * @throws NullPointerException if any one of the two arguments is {@code null}
	 * @throws UnsupportedOperationException if this layer does not support additional keys
	 */
	boolean collectKeys(Markable markable, Collector<String> buffer);

	/**
	 * Returns the annotation for a given markable and key or {@code null} if that markable
	 * has not been assigned an annotation value for the specified key in this layer.
	 * Note that the returned object can be either an actual value or an {@link Annotation}
	 * instance that wraps a value and provides further information.
	 *
	 * @param markable
	 * @param key
	 * @return
	 * @throws NullPointerException if either the {@code markable} or {@code key}
	 * is {@code null}
	 * @throws UnsupportedOperationException if this layer does not support additional keys
	 */
	Object getValue(Markable markable, String key);

	int getIntValue(Markable markable, String key);
	float getFloatValue(Markable markable, String key);
	double getDoubleValue(Markable markable, String key);
	long getLongValue(Markable markable, String key);
	boolean getBooleanValue(Markable markable, String key);

	/**
	 * Deletes all annotations in this layer
	 * <p>
	 * Note that this does include all annotations for all keys,
	 * not only those declares for the default annotation.
	 *
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	void removeAllValues();

	/**
	 * Deletes in this layer all annotations for
	 * the given {@code key}.
	 *
	 * @param key The key for which annotations should be
	 * deleted
	 * @throws UnsupportedOperationException if this layer does not allow multiple keys
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	void removeAllValues(String key);

	/**
	 * Removes from this layer all annotations for the given
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
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	void removeAllValues(Markable markable, boolean recursive);

//	/**
//	 * Assigns the given {@code value} as new annotation for the specified
//	 * {@code Markable}, replacing any previously defined value. If the
//	 * {@code value} argument is {@code null} any stored annotation for the
//	 * {@code markable} will be deleted.
//	 *
//	 * @param markable The {@code Markable} to change the annotation value for
//	 * @param value the new annotation value or {@code null} if the annotation
//	 * for the given {@code markable} should be deleted
//	 * @throws NullPointerException if the {@code markable} argument is {@code null}
//	 * @throws IllegalArgumentException if the supplied {@code value} is not
//	 * contained in the {@link ValueSet} of this layer's manifest. This is only
//	 * checked if the manifest actually defines such restrictions.
//	 * @throws UnsupportedOperationException if the corpus
//	 * is not editable
//	 */
//	void setValue(Markable markable, Object value);


	/**
	 * Assigns the given {@code value} as new annotation for the specified
	 * {@code Markable} and {@code key}, replacing any previously defined value.
	 * If the {@code value} argument is {@code null} any stored annotation
	 * for the combination of {@code markable} and {@code key} will be deleted.
	 * <p>
	 * This is an optional method
	 *
	 * @param markable The {@code Markable} to change the annotation value for
	 * @param key the key for which the annotation should be changed
	 * @param value the new annotation value or {@code null} if the annotation
	 * for the given {@code markable} and {@code key} should be deleted
	 * @throws UnsupportedOperationException if this layer does not allow multiple keys
	 * @throws NullPointerException if the {@code markable} or {@code key}
	 * argument is {@code null}
	 * @throws IllegalArgumentException if the supplied {@code value} is not
	 * contained in the {@link ValueSet} of this layer's manifest for the given {@code key}.
	 * This is only checked if the manifest actually defines such restrictions.
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	void setValue(Markable markable, String key, Object value);

	/**
	 *
	 * @return {@code true} iff this layer holds at least one valid annotation object.
	 */
	boolean hasAnnotations();
}
