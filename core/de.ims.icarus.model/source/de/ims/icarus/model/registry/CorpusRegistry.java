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
package de.ims.icarus.model.registry;

import java.util.List;
import java.util.Set;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.layer.LayerType;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.Manifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.MemberManifest;
import de.ims.icarus.model.api.manifest.OptionsManifest;
import de.ims.icarus.ui.events.EventListener;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface CorpusRegistry {

	void saveCorpora();

	LayerType getLayerType(String name);

	void registerLayerType(LayerType layerType);

	void addCorpus(CorpusManifest manifest);

	void removeCorpus(CorpusManifest manifest);

	CorpusManifest getCorpus(String id);

	Set<String> getCorpusIds();

	List<CorpusManifest> getCorpora();

	Corpus getCorpusInstance(CorpusManifest manifest);

	boolean hasTemplate(String id);

	Manifest getTemplate(String id);


	void addContext(CorpusManifest corpus, ContextManifest context);

	void removeContext(CorpusManifest corpus, ContextManifest context);

	void corpusChanged(CorpusManifest corpus);

	void contextChanged(ContextManifest context);

	/**
	 * Returns all the {@code ContextManifest} templates added to this registry.
	 * @return
	 *
	 * @see #getRootContextTemplates()
	 */
	List<ContextManifest> getContextTemplates() ;

	/**
	 * Returns all previously registered templates that are of the given
	 * {@code ManifestType}. Note that this method only returns templates
	 * implementing the {@link MemberManifest} interface! So for example
	 * it is not possible to collect templates for the {@link OptionsManifest}
	 * interface this way. Use the {@link #getTemplatesOfClass(Class)} for
	 * such cases.
	 *
	 * @throws NullPointerException if the {@code type} argument is {@code null}
	 * @see #getTemplatesOfClass(Class)
	 */
	List<? extends MemberManifest> getTemplatesOfType(ManifestType type);

	/**
	 * Returns all previously templates that derive from the given {@code Class}.
	 *
	 * @throws NullPointerException if the {@code clazz} argument is {@code null}
	 */
	<E extends Manifest> List<E> getTemplatesOfClass(Class<E> clazz);

	List<Manifest> getManifestsForSource(ManifestLocation manifestLocation);

	/**
	 * Returns a list of {@code ContextManifest} objects that can be used to
	 * create a new corpus by serving as the default context of that corpus.
	 * Suitability is checked by means of the {@link ContextManifest#isIndependentContext()}
	 * method returning {@code true}.
	 *
	 * @return
	 */
	List<ContextManifest> getRootContextTemplates();

	void registerTemplate(Manifest template) throws ModelException;

	void addListener(String eventName, EventListener listener);

	void removeEventListener(EventListener listener);

	void removeEventListener(EventListener listener, String eventName);

	LayerType getOverlayLayerType();
}
