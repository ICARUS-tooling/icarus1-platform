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
package de.ims.icarus.language.model;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import de.ims.icarus.language.model.events.CorpusListener;
import de.ims.icarus.language.model.manifest.CorpusManifest;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.id.DuplicateIdentifierException;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Corpus extends Iterable<Layer> {
	
	/**
	 * Returns the lock object that should be used when performing <i>write</i>
	 * operations on this corpus. Especially when attempting to add a new layer
	 * and generating a unique name using the {@link #getUniqueName(String)} method
	 * is it absolutely crucial to perform the entire process while holding the
	 * write lock. Not doing so could mean that another layer might be registered 
	 * with the exact same <i>unique</i> name and render the new layer invalid.
	 * 
	 * @return the <i>write-lock</i> of this corpus object
	 */
	Lock getLock();

	/**
	 * Resolves a given id and returns the member within this corpus
	 * that is registered with this id.
	 * 
	 * @param id the {@code id} of the member to be resolved
	 * @return The member that was registered for the given {@code ID}
	 * 
	 * @throws IllegalArgumentException if there is no member registered 
	 * for the given {@code id}
	 */
	CorpusMember getMember(long id);
	
	/**
	 * Resolves a given name to the corresponding member. 
	 * @param name The name of the member to be returned.
	 * @return The member registered for the given {@code name}
	 * @throws NullPointerException if the {@code name} is {@code null}
	 * @throws IllegalArgumentException if there is no member registered 
	 * for the given {@code name}
	 */
	NamedCorpusMember getNamedMember(String name);
	
	/**
	 * Utility method to help external source to get truly unique names
	 * for new members of the corpus.
	 * @param baseName
	 * @return
	 */
	String getUniqueName(String baseName);
	
	/**
	 * Returns the {@code Context} object all the default members of
	 * this corpus have been added to.
	 * 
	 * @return The {@code Context} hosting all the default members of the corpus
	 */
	Context getDefaultContext();
	
	/**
	 * Registers the given listener to the internal list of registered
	 * listeners. Does nothing if the provided listener is {@code null}.
	 * Note that implementations should make sure that no listener is
	 * registered more than once. Typically this means doubling the cost
	 * of registration. Since it is not to be expected that registrations
	 * occur extremely frequent, this can be ignored.
	 * 
	 * @param l The listener to be registered, may be {@code null}
	 */
	void addCorpusListener(CorpusListener l);
	
	/**
	 * Unregisters the given listener from the internal list of registered
	 * listeners. Does nothing if the provided listener is {@code null}.
	 * @param l The listener to be unregistered, may be {@code null}
	 */
	void removeCorpusListener(CorpusListener l);
	
	/**
	 * Returns the manifest that describes this corpus.
	 * 
	 * @return The {@code CorpusManifest} for this corpus.
	 */
	CorpusManifest getManifest();
	
	/**
	 * Returns the bottom-most layer responsible for representing the bare
	 * tokens of this corpus. 
	 * <p>
	 * This is a shorthand method. The returned {@code MarkableLayer} is the
	 * reference for all offset related indices used by {@code Markable}s in
	 * this corpus.
	 * 
	 * @return The {@code MarkableLayer} hosting the tokens of this corpus.
	 */
	MarkableLayer getBaseLayer();
	
	/**
	 * Returns all the {@code Context} object that are registered implicitly
	 * by adding layers. This list includes the <i>default context</i> as
	 * returned by {@link #getDefaultContext()} (therefore the returned {@code Set}
	 * is never empty).
	 * 
	 * @return All the contexts available for this corpus.
	 */
	Set<Context> getContexts();
	
	/**
	 * Returns all layers registered for this corpus in the order of their
	 * registration. If this corpus does not yet host any layers the returned
	 * list is empty. Either way the returned list should be immutable.
	 * 
	 * @return A list containing all the layers currently hosted within this corpus.
	 */
	List<Layer> getLayers();
	
	/**
	 * Returns all the layers in this corpus that are of the given type as defined
	 * by their {@link Layer#getTypeName()} method. If this corpus does not yet host any layers the returned
	 * list is empty. Either way the returned list should be immutable.
	 * 
	 * @param type The name of the desired type (e.g. "lemma")
	 * @return A list view of all the layers of the given type within this corpus
	 * @throws NullPointerException if the {@code type} argument is {@code null}.
	 */
	List<Layer> getLayers(String type);
	
	/**
	 * Adds the given layer to this corpus.
	 * 
	 * @param layer the layer to be added
	 * @throws NullPointerException if the {@code layer} argument is {@code null}
	 * @throws DuplicateIdentifierException if this corpus already contains a layer
	 * with the same {@code ID} as defined by {@link Layer#getId()}.
	 * @throws IllegalArgumentException if the layer is already part of another corpus
	 * or if the layer implementation violates the contract of the {@code Layer} interface
	 * and returns an invalid {@code Context} or {@code Manifest} object.
	 * @throws IllegalStateException if one or more of the layer's prerequisites cannot
	 * be fulfilled (i.e. one of the required underlying layers is missing).
	 */
	void addLayer(Layer layer);
	
	void removeLayer(Layer layer);
	
	void addMetaData(ContentType type, Layer layer, Object data);
	
	Set<?> getMetaData(ContentType type, Layer layer);
}
