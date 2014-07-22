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
package de.ims.icarus.model;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import de.ims.icarus.model.api.driver.Driver;
import de.ims.icarus.model.api.driver.indexing.Index;
import de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest;
import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest;
import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum ModelError {

	//**************************************************
	//       1xx  GENERAL ERRORS
	//**************************************************

	/**
	 * Represents an error whose cause could not be identified or when a
	 * {@code ModelException} only contains an error message without the
	 * exact type of error being specified.
	 */
	UNKNOWN_ERROR(100),

	/**
	 * A general I/O error occurred.
	 */
	IO_ERROR(101),

	/**
	 * Wraps an {@link OutOfMemoryError} object that was thrown when
	 * the Java VM ran out of memory to allocate objects.
	 */
	INSUFFICIENT_MEMORY(102),

	//**************************************************
	//       2xx  IMPLEMENTATION ERRORS
	//**************************************************

	/**
	 * Instantiating a corpus member failed due to lack of a valid
	 * {@link ImplementationManifest} being present.
	 */
	IMPLEMENTATION_MISSING(200),

	/**
	 * The result of a member instantiation according to some foreign
	 * {@link ImplementationManifest} failed because the returned object
	 * was not assignment compatible with the required result class.
	 */
	IMPLEMENTATION_INCOMPATIBLE(201),

	/**
	 * A {@link ImplementationManifest} declared a class to be used for
	 * instantiation that could not be found. Remember that foreign
	 * implementations must be declared via extensions in a plugin manifest!
	 */
	IMPLEMENTATION_NOT_FOUND(202),

	/**
	 * A {@link ImplementationManifest} declared a class to be used for
	 * instantiation that was not accessible by the framework. Reasons
	 * might be the accidental restriction of the default constructor to
	 * have a {@code protected} modifier or not to provide a no-args
	 * constructor at all.
	 */
	IMPLEMENTATION_NOT_ACCESSIBLE(203),

	//**************************************************
	//       3xx  PREREQUISITE ERRORS
	//**************************************************

	/**
	 * Obtaining a layer (for example via {@link TargetLayerManifest#getResolvedLayerManifest()}
	 * failed, because the underlying {@link PrerequisiteManifest} has not yet been resolved to
	 * an actual target layer. This means that the prerequisite is lacking the required qualities
	 * (context and layer id) to be counted as resolved.
	 * <p>
	 * Note that for resolved prerequisites that contain invalid (i.e. non-existent) targets, the
	 * {@link #PREREQUISITE_INVALID} error should be used.
	 */
	PREREQUISITE_UNRESOLVED(300),

	/**
	 * A prerequisite presents the required qualities to count as resolved (this means it declared
	 * both a context and layer id) but one of those ids is invalid in the sense that it does not
	 * point to an existing target (e.g. the target context has been removed or a typo occurred when
	 * generating the context manifest manually).
	 */
	PREREQUISITE_INVALID(301),

	/**
	 * A prerequisite presents the required qualities to count as resolved (this means it declared
	 * both a context and layer id) but the target layer it got resolved to is of an incompatible type
	 * (e.g. it references an annotation layer but was meant to point to a markable layer).
	 * This error should be pretty rare, since it indicates a prior mistake in the framework when
	 * possible resolution targets have been collected.
	 */
	PREREQUISITE_INCOMPATIBLE(302),

	//**************************************************
	//       4xx  DRIVER ERRORS
	//**************************************************

	/**
	 * An unexpected I/O exception occurred during access to some indexing system associated with a
	 * {@link Driver}.
	 */
	DRIVER_INDEX_IO(401),

	/**
	 * Client code attempted to write to an index file in a manner other than using existing index
	 * values or appending to the greatest current index value. This restriction is imposed by the default
	 * implementations for the {@link Index} interface provided by file based {@link Driver}s. Note that
	 * the {@code Index} interface does not define write mechanics itself, since for example database
	 * backed implementations might directly link to the database's own indexing system and therefore
	 * not support client originated write operations on the index!
	 */
	DRIVER_INDEX_WRITE_VIOLATION(402),

	/**
	 * A driver implementation failed to create a proper checksum for an index or content file.
	 */
	DRIVER_CHECKSUM_FAIL(403),

	//**************************************************
	//       5xx  SEGMENT ERRORS
	//**************************************************

	/**
	 * Closing a segment failed due to some owner not being able to release its lock when asked.
	 */
	SEGMENT_OWNED(501),

	//**************************************************
	//       6xx  MANIFEST ERRORS
	//**************************************************

	/**
	 * Two or more elements in a manifest definition used the same id within a single namespace
	 */
	MANIFEST_DUPLICATE_ID(601),
	MANIFEST_UNKNOWN_ID(602),
	MANIFEST_ILLEGAL_TEMPLATE(603),
	MANIFEST_CYCLIC_TEMPLATE(604),
	MANIFEST_INCOMPATTIBLE_TEMPLATE(605),
	MANIFEST_MISSING_CONTEXT(606),
	MANIFEST_MISSING_TYPE(607),
	MANIFEST_MISSING_LOCATION(608),
	//FIXME add errors for missing content etc...

	;

	private final int errorCode;

	ModelError(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	@Override
	public String toString() {
		return name()+" ("+errorCode+")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static final TIntObjectMap<ModelError> codeLookup = new TIntObjectHashMap<>();

	public static ModelError forCode(int code) {
		if(codeLookup.isEmpty()) {
			synchronized (codeLookup) {
				if (codeLookup.isEmpty()) {
					for(ModelError error : values()) {
						//TODO Maybe add extra sanity check against duplicate error codes?
						if(codeLookup.containsKey(error.errorCode))
							throw new CorruptedStateException("Duplicate error code: "+error); //$NON-NLS-1$

						codeLookup.put(error.errorCode, error);
					}
				}

			}
		}

		ModelError error = codeLookup.get(code);

		if(error==null)
			throw new IllegalArgumentException("Unknown error code: "+code); //$NON-NLS-1$

		return error;
	}

}
