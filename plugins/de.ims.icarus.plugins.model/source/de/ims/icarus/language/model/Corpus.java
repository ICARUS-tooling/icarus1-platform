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

import de.ims.icarus.io.Loadable;
import de.ims.icarus.language.model.edit.CorpusEditModel;
import de.ims.icarus.language.model.edit.CorpusUndoListener;
import de.ims.icarus.language.model.edit.CorpusUndoManager;
import de.ims.icarus.language.model.events.CorpusAdapter;
import de.ims.icarus.language.model.events.CorpusListener;
import de.ims.icarus.language.model.events.EventManager;
import de.ims.icarus.language.model.manifest.CorpusManifest;
import de.ims.icarus.language.model.manifest.ManifestOwner;
import de.ims.icarus.language.model.meta.MetaData;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.id.DuplicateIdentifierException;

/**
 * A {@code Corpus} object is the top-most member of the corpus framework and the
 * root object of the tree that represents a complex corpus structure. It hosts
 * an arbitrary number of {@code Layer} objects of various types which are grouped
 * into several {@code Context} implementations. A {@code Context} is the abstract
 * representation of a <i>source</i> of layers. Commonly used formats to physically
 * represent corpus data usually contain multiple annotation and/or structure/grouping
 * layers. All these layers, originating from the same source, that belong to the same
 * <i>format</i> are grouped into one {@code Context}.
 *
 * A corpus provides two levels of changes that can be performed:
 * <ol>
 * <li>Content-based changes within layers, containers or structures. These changes
 * usually originate from user actions and will be wrapped into {@code UndoableCorpusEdit}
 * objects which in turn are then stored in the corpus' shared {@code CorpusUndoManager}.
 * Note that these changes are only reflected in the physical storage of the corpus once
 * it gets saved. If a corpus is not declared to be editable then all attempts to mutate
 * its content will result in exceptions being thrown!</li>
 * <li>Descriptor-based changes affecting the layout of a corpus and its identity (or that
 * of its members). These changes are managed on the manifest level and persistent storing
 * of those informations is performed by the {@code CorpusRegistry}. Unlike content-based
 * changes they do <b>not</b> trigger undoable edits. The can however affect the undo history
 * by simply discarding all changes (e.g. removing a context will invalidate the current
 * undo history). A reason for this policy is the fact, that keeping track of changes on that
 * scale requires to keep a backup copy of the entire affected data (in the aforementioned case
 * an entire context) in memory to allow the user to undo his action.</li>
 * </ol>
 * Both levels are handled by different managers which both allow for listeners to be registered
 * for various events:
 *
 * For <i>content-based</i> changes the {@link CorpusEditModel} returned by {@link #getEditModel()}
 * provides the methods to programmatically initialize an edit process, to add changes to an
 * active edit and to finally commit all pending changes to a single undoable edit. While an
 * edit is in progress the model will fire several events whenever the update-level is increased or
 * decreased (via the {@link CorpusEditModel#beginUpdate()} or {@link CorpusEditModel#endUpdate()}
 * methods) and when the final edit is committed. These events are forwarded to common {@link EventListener}
 * instances, while the final edit is also delegated to all the registered {@link CorpusUndoListener}s,
 * including the shared {@link CorpusUndoManager} of the corpus affected by the changes.
 *
 * All <i>descriptor-based</i> changes on the other hand are propagated by the {@link EventManager} of
 * the corpus as obtained via {@link #getEventManager()}. The listeners supported by this manager are
 * solely of type {@link CorpusListener} and a special callback method is defined for each type of change.
 * (Note that there exists the {@link CorpusAdapter} class that implements the {@code CorpusListener}
 * interface with empty methods so that a new listener implementation only needs to define the methods
 * it actually requires)
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Corpus extends Iterable<Layer>, Loadable, ManifestOwner<CorpusManifest> {

	/**
	 * Returns the lock object that should be used when performing <i>write</i>
	 * operations on this corpus. Especially when attempting to add a new layer
	 * and generating a unique name using the {@link #getUniqueName(String)} method
	 * is it absolutely crucial to perform the entire process while holding the
	 * write lock. Not doing so could mean that another layer might be registered
	 * with the exact same <i>unique</i> name and render the new layer invalid.
	 * <p>
	 * It is also advised that any implementation of the {@code Mutator} interface
	 * make use of this lock. Since a {@code Mutator} is intended to represent the
	 * process of user-originated modification it is indeed a valid option to perform
	 * the locking of resources on the top-most level (a user can only perform one
	 * modification at a given point of time).
	 *
	 * @return the <i>write-lock</i> of this corpus object
	 */
	Lock getLock();

	/**
	 * Returns the {@code CorpusEditModel} that is used to model changes the user
	 * made to this corpus.
	 * @return
	 */
	CorpusEditModel getEditModel();

	/**
	 * Returns the shared undo-manager that keeps track of changes of this corpus.
	 *
	 * @return
	 */
	CorpusUndoManager getUndoManager();

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
	 * Called by layers, containers and structures when new members get
	 * added to them. The purpose of this method is to centralize the
	 * registration and notification of new members.
	 * <p>
	 * This method should do nothing in the case that a member is already
	 * present in the corpus (although this might represent a state of
	 * inconsistency).
	 *
	 * @param member
	 */
	void addMember(CorpusMember member);

	/**
	 * Resolves a given name to the corresponding member.
	 * <p>
	 * This method
	 *
	 * @param name The name of the member to be returned.
	 * @return The member registered for the given {@code name}
	 * @throws NullPointerException if the {@code name} is {@code null}
	 * @throws IllegalArgumentException if there is no member registered
	 * for the given {@code name}
	 */
	NamedCorpusMember getNamedMember(String name);

	/**
	 * Utility method to help external sources to get truly unique names
	 * for new members of the corpus or for renaming.
	 *
	 * @param baseName
	 * @return
	 */
	String getUniqueName(String baseName);

	/**
	 * Utility method to help external sources to get truly unique ids
	 * for new context instances.
	 *
	 * @param baseName
	 * @return
	 */
	String getUniqueId(String baseId);

	/**
	 * Returns the {@code Context} object all the default members of
	 * this corpus have been added to.
	 *
	 * @return The {@code Context} hosting all the default members of the corpus
	 */
	Context getDefaultContext();

	/**
	 * Returns the event manager that is responsible for storing listeners
	 * and for publishing events.
	 *
	 * @return
	 */
	EventManager getEventManager();

	/**
	 * Returns the manifest that describes this corpus.
	 *
	 * @return The {@code CorpusManifest} for this corpus.
	 */
	@Override
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
	 * Returns the virtual overlay layer that gives a layer-style access to
	 * the containers defined in other {@code MarkableLayer} objects registered
	 * to this corpus.
	 *
	 * @return
	 */
	MarkableLayer getOverlayLayer();

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
	 * by their {@link Layer#getType()} method. If this corpus does not yet host any layers the returned
	 * list is empty. Either way the returned list should be immutable.
	 *
	 * @param type The desired type (e.g. "lemma")
	 * @return A list view of all the layers of the given type within this corpus
	 * @throws NullPointerException if the {@code type} argument is {@code null}.
	 */
	List<Layer> getLayers(LayerType type);

	/**
	 * Adds the given layer to this corpus and notifies listeners.
	 * <p>
	 * If the {@code Context} of the layer is unknown to the corpus it will be added
	 * first (including notification of listeners about the context).
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

	/**
	 * Removes the given layer from this corpus and notifies listeners.
	 *
	 * @param layer the layer to be added
	 * @throws NullPointerException if the {@code layer} argument is {@code null}
	 * @throws IllegalArgumentException if the layer is not part of this corpus
	 * @throws IllegalStateException if one or more other layers defined this
	 * layer as prerequisite
	 */
	void removeLayer(Layer layer);

	/**
	 * Removes the given context from this corpus and notifies listeners.
	 *
	 * @param context the context to be added
	 * @throws NullPointerException if the {@code context} argument is {@code null}
	 * @throws IllegalArgumentException if the context is not part of this corpus
	 * @throws IllegalStateException if the context is in a state that prevents it
	 * from being removed (e.g. if it is currently loading content)
	 */
	void removeContext(Context context);

	/**
	 * Adds the given meta-data to this corpus and links it with the optionally
	 * specified layer. The {@code contentType} argument is used to group meta-data
	 * objects besides their layer.
	 *
	 * @param type The {@link ContentType} describing the meta-data object
	 * @param layer The layer the meta-data should be linked with or {@code null} if
	 * the meta-data should be assigned to the entire corpus
	 * @param data The meta-data itself, must not be {@code null}
	 * @throws NullPointerException if either one of {@code type} or {@code data}
	 * is {@code null}
	 * @throws IllegalArgumentException if the specified layer is not a part of
	 * this corpus
	 */
	void addMetaData(ContentType type, Layer layer, Object data);

	void removeMetaData(ContentType type, Layer layer, Object data);

	/**
	 * Returns all the previously registered meta-data objects for the given
	 * combination of {@link ContentType} and {@link Layer}. If the {@code layer}
	 * argument is {@code null} then only meta-data objects assigned to the
	 * entire corpus will be returned. If the {@code type} argument is {@code null}
	 * then all meta-data registered for the specified layer (or the entire corpus)
	 * is returned.
	 *
	 * Note that the returned collection of meta-data items is unordered. The corpus
	 * does not keep track of the order of insertion! It is advised that decisions
	 * required to select one out of many meta-data objects of the same type be delegated
	 * to the user.
	 *
	 * @param type The {@link ContentType} describing the meta-data object or {@code null}
	 * if all meta-data registered for the {@code layer} argument should be returned
	 * @param layer The {@link Layer} for which to fetch meta-data or {@code null} if only
	 * meta-data assigned to the entire corpus should be returned.
	 * @return A {@link Set} holding meta-data objects for the given combination of
	 * {@link ContentType} and {@link Layer}
	 * @throws IllegalArgumentException if the {@code layer} argument is non-{@code null}
	 * and the layer is not part of this corpus
	 */
	Set<MetaData> getMetaData(ContentType type, Layer layer);

	/**
	 * Releases {@code all} the loaded data for this corpus. This is a shorthand method for
	 * fetching all {@code Context} objects for the corpus and calling {@code Context#free()}
	 * on each of them.
	 *
	 * @see de.ims.icarus.io.Loadable#free()
	 */
	@Override
	void free();
}
