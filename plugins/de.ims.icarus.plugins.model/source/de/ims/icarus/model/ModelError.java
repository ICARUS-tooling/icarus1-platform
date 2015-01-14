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
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest;
import de.ims.icarus.model.types.ValueType;
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
	 * Wraps an {@link OutOfMemoryError} object that was thrown when
	 * the Java VM ran out of memory to allocate objects.
	 */
	INSUFFICIENT_MEMORY(101),

	/**
	 * A general I/O error occurred.
	 */
	IO_ERROR(110),

	/**
	 * Reading from a resource is not possible.
	 */
	NO_READ_ACCESS(111),

	/**
	 * Writing to a resource is not possible.
	 */
	NO_WRITE_ACCESS(112),

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
	//       5xx  SUBCORPUS ERRORS
	//**************************************************

	/**
	 * Closing a sub corpus failed due to some owner not being able to release its lock when asked.
	 */
	SUBCORPUS_UNCLOASABLE(501),

	/**
	 * Creating a new sub corpus in an access mode that would grant write access failed due to some other
	 * sub corpus instance already working on the corpus in question.
	 * Note that there can only be one sub corpus instance for a particular corpus with write access, but
	 * an unlimited number of reading sub corpora!
	 */
	SUBCORPUS_ALREADY_OPENED(502),

	/**
	 * An attempt to fetch the model for a sub corpus failed because the data for the current
	 * page has not yet been loaded.
	 */
	SUBCORPUS_EMPTY(503),

	//**************************************************
	//       6xx  MANIFEST ERRORS
	//**************************************************

	/**
	 * Two or more elements in a manifest definition used the same id within a single namespace
	 */
	MANIFEST_DUPLICATE_ID(601),

	/**
	 * The reference via id to another resource is invalid due to the id being unknown.
	 */
	MANIFEST_UNKNOWN_ID(602),

	/**
	 * An attempt was made to declare a resource as template although in the given situation
	 * that particular resource was not allowed to be declared as template.
	 */
	MANIFEST_ILLEGAL_TEMPLATE(603),

	/**
	 * A set of templates form a cyclic relation.
	 */
	MANIFEST_CYCLIC_TEMPLATE(604),

	/**
	 * A manifest is referencing a template of a foreign manifest type.
	 */
	MANIFEST_INCOMPATTIBLE_TEMPLATE(605),

	/**
	 * Signals that a given operation is not possible since the manifest in question requires another
	 * manifest instance surrounding it. For example certain manifests (like {@link ContextManifest},
	 * {@link LayerManifest}, etc..) cannot be used without their respective environments ({@link CorpusManifest},
	 * {@link LayerGroupManifest}, respectively) in a live state.
	 */
	MANIFEST_MISSING_ENVIRONMENT(606),

	/**
	 * A manifest that requires value type information for its content (like annotations) is missing
	 * that declaration.
	 */
	MANIFEST_MISSING_TYPE(607),

	/**
	 * A manifest that relies on external resources is missing the location declaration for those resources.
	 */
	MANIFEST_MISSING_LOCATION(608),

	/**
	 * Some value (annotation, property, option, ...) declared in a manifest is incompatible
	 * with the respective value type specified in the context of that value.
	 */
	MANIFEST_TYPE_CAST(620),

	/**
	 * A value type definition in a manifest cannot be resolved to an actual {@link ValueType} implementation.
	 */
	MANIFEST_UNKNOWN_TYPE(621),
	//FIXME add errors for missing content etc...

	//**************************************************
	//       7xx  DATA ERRORS
	//**************************************************

	/**
	 * Mismatch between expected size of an array object and its actual length.
	 */
	DATA_ARRAY_SIZE(701),

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
