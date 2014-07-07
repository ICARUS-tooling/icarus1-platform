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

import java.util.List;
import java.util.Map;

import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ContextManifest extends MemberManifest {

	CorpusManifest getCorpusManifest();

	DriverManifest getDriverManifest();

	/**
	 * Returns a list of prerequisites describing other layers a corpus
	 * has to host in order for this context to be operational. If this
	 * context does not depend on other layers the returned list is empty.
	 *
	 * @return
	 */
	List<PrerequisiteManifest> getPrerequisites();

	/**
	 * Generates a cloned version of this manifest that is identical to the current state
	 * except that all its prerequisites are resolved according to the {@code resolutionMap}
	 * argument.
	 *
	 * @param corpusManifest the new hosting corpus manifest
	 * @param resolutionMap
	 * @return
	 * @throws ModelException if the {@code resolutionMap} contains prerequisites that are either
	 * 			unresolved or invalid according to the prerequisite errors defined in {@link ModelError}.
	 * @throws NullPointerException if the {@code corpusManifest} argument is {@code null}
	 * @throws IllegalArgumentException if not every unresolved prerequisite in this manifest is
	 * 			covered as a key in the {@code resolutionMap} parameter.
	 * @throws UnsupportedOperationException if this manifest is not a template and therefore cannot
	 * 			be instantiated to a resolved form.
	 */
	ContextManifest getResolvedForm(CorpusManifest corpusManifest, Map<PrerequisiteManifest, PrerequisiteManifest> resolutionMap) throws ModelException;

	/**
	 * Allows for changes of the context's name at runtime. This is one of the
	 * few situations where a direct modification of a manifest by the user is
	 * possible.
	 * <p>
	 *
	 * @param newName The desired new name of the layer
	 * @throws UnsupportedOperationException if the manifest does not support renaming
	 * @throws NullPointerException if the {@code newName} parameter is {@code null}
	 */
	void setName(String newName);

	/**
	 * Returns the list of manifests that describe the layers in this context
	 *
	 * @return
	 */
	List<LayerManifest> getLayerManifests();

	List<LayerGroupManifest> getGroupManifests();

	MarkableLayerManifest getPrimaryLayerManifest();

	LayerManifest getLayerManifest(String id);

	/**
	 * Returns the manifest that describes where the data for this context's
	 * layers is loaded from and how to access distributed data sources.
	 *
	 * @return
	 */
	LocationManifest getLocationManifest();

	/**
	 * Changes the location from which this context's data is loaded.
	 *
	 * @param manifest
	 */
	void setLocationManifest(LocationManifest manifest);

	/**
	 * Tells whether or not a context depends on other resources besides the
	 * data contained in its own layers. Only a context that is independent of
	 * external data can be assigned as default context of a corpus!
	 * @return
	 */
	boolean isIndependentContext();

	boolean isRootContext();

	/**
	 * Abstract description of a layer object this context depends on.
	 * <p>
	 * Note that prerequisites are only used in templates. When a template
	 * is being instantiated, all the prerequisites will be resolved to actual
	 * layers and linked accordingly.
	 * <p>
	 * Depending on the return values of this interface's methods a prerequisite
	 * can be viewed as unresolved or resolved. In the latter case both the
	 * {@link #getContextId()} and {@link #getLayerId()} return a valid non-null
	 * id. Since dependencies within the same context are not expressed by
	 * dependency manifests, it is always necessary to provide a valid id of a
	 * foreign context to actually resolve the target layer. Once a prerequisite
	 * has been fully resolved, the target layer is accessible via the specified
	 * alias. It is possible to "hard bind" to a foreign layer by omitting the
	 * optional type id and specify context and layer id right from the start. This
	 * will bypass the regular binding process (possible involving the user to resolve
	 * ambiguities) but on the other hand sacrifices flexibility, for the target context
	 * cannot be changed (note that the first instance of a context template in a corpus
	 * will always carry the full and unchanged id of the template, making this type
	 * of "hard binding" possible in the first place).
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface PrerequisiteManifest {

		ContextManifest getContextManifest();

		/**
		 * Returns the id of the target layer or {@code null} if an exact id match
		 * is not required or the prerequisite has not yet been fully resolved.
		 *
		 * @return
		 */
		String getLayerId();

		/**
		 * Returns the id of the context which should be used to resolve the required
		 * layer (specified by the {@link #getLayerId() method}) or {@code null} if no
		 * exact match is required or the prerequisite has not yet been fully resolved.
		 * @return
		 */
		String getContextId();

		/**
		 * If this layer only requires <i>some</i> layer of a certain type to be present
		 * this method provides the mechanics to tell this. When the returned value is
		 * {@code non-null} it is considered to be the exact name of a previously
		 * defined layer type.
		 *
		 * @return
		 */
		String getTypeId();

		/**
		 * Returns the id the required layer should be assigned once resolved. This links
		 * the result of an abstract prerequisite declaration to a boundary or base definition
		 * in a template. In addition a prerequisite's alias serves as its identifier. Therefore
		 * an alias must be unique within the same context!
		 *
		 * @return
		 */
		String getAlias();

		/**
		 * If this prerequisite is in resolved state, it was created based on some unresolved
		 * prerequisite in a context template. In that case this method returns the original
		 * prerequisite manifest in unresolved form, or {@code null} otherwise.
		 * <p>
		 * Note that in case the prerequisite was declared using "hard binding" then this method
		 * will return also {@code null}!
		 *
		 * @return
		 */
		PrerequisiteManifest getUnresolvedForm();
	}
}
