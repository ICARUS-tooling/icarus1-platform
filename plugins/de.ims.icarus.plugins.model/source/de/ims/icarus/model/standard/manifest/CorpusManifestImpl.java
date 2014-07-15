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
package de.ims.icarus.model.standard.manifest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusManifestImpl extends AbstractMemberManifest<CorpusManifest> implements CorpusManifest {

	private ContextManifest defaultContextManifest;
	private List<ContextManifest> contextManifests = new ArrayList<>(3);
	private Map<String, ContextManifest> contextManifestLookup = new HashMap<>();
	private boolean editable;
	private List<Note> notes = new ArrayList<>();

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CORPUS_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#getRootContextManifest()
	 */
	@Override
	public ContextManifest getRootContextManifest() {
		return defaultContextManifest;
	}

	/**
	 * @param defaultContextManifest the defaultContextManifest to set
	 */
	public void setDefaultContextManifest(ContextManifest defaultContextManifest) {
		if (defaultContextManifest == null)
			throw new NullPointerException("Invalid defaultContextManifest"); //$NON-NLS-1$

		this.defaultContextManifest = defaultContextManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#getCustomContextManifests()
	 */
	@Override
	public List<ContextManifest> getCustomContextManifests() {
		return CollectionUtils.getListProxy(contextManifests);
	}

	/**
	 *
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#addCustomContextManifest(de.ims.icarus.model.api.manifest.ContextManifest)
	 */
	@Override
	public void addCustomContextManifest(ContextManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		contextManifests.add(manifest);
		contextManifestLookup.put(manifest.getId(), manifest);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#removeCustomContextManifest(de.ims.icarus.model.api.manifest.ContextManifest)
	 */
	@Override
	public void removeCustomContextManifest(ContextManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		if(!contextManifests.remove(manifest))
			throw new IllegalArgumentException("Unknown context manifest: "+manifest); //$NON-NLS-1$
		contextManifestLookup.remove(manifest.getId());
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#getContextManifest(java.lang.String)
	 */
	@Override
	public ContextManifest getContextManifest(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		ContextManifest contextManifest = contextManifestLookup.get(id);
		if(contextManifest==null)
			throw new IllegalArgumentException("No such context: "+id); //$NON-NLS-1$

		return contextManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#setEditable(boolean)
	 */
	@Override
	public void setEditable(boolean value) {
		this.editable = value;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#getNotes()
	 */
	@Override
	public List<Note> getNotes() {
		return CollectionUtils.getListProxy(notes);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#addNote(de.ims.icarus.model.api.manifest.CorpusManifest.Note)
	 */
	@Override
	public void addNote(Note note) {
		if(!notes.contains(note)) {
			notes.add(note);
		}
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.CorpusManifest#removeNote(de.ims.icarus.model.api.manifest.CorpusManifest.Note)
	 */
	@Override
	public void removeNote(Note note) {
		notes.remove(note);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#copyFrom(de.ims.icarus.model.api.manifest.MemberManifest)
	 */
	@Override
	protected void copyFrom(CorpusManifest template) {
		throw new UnsupportedOperationException();
	}

	public static class NoteImpl implements Note {

		private Date modificationDate;
		private String name;
		private String content;

		/**
		 * @see de.ims.icarus.model.api.manifest.CorpusManifest.Note#getModificationDate()
		 */
		@Override
		public Date getModificationDate() {
			return modificationDate;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.CorpusManifest.Note#getName()
		 */
		@Override
		public String getName() {
			return name;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.CorpusManifest.Note#getContent()
		 */
		@Override
		public String getContent() {
			return content;
		}

		/**
		 * @param modificationDate the modificationDate to set
		 */
		public void setModificationDate(Date modificationDate) {
			if (modificationDate == null)
				throw new NullPointerException("Invalid modificationDate"); //$NON-NLS-1$

			this.modificationDate = modificationDate;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			if (name == null)
				throw new NullPointerException("Invalid name");  //$NON-NLS-1$

			this.name = name;
		}

		/**
		 * @param content the content to set
		 */
		public void setContent(String content) {
			if (content == null)
				throw new NullPointerException("Invalid content"); //$NON-NLS-1$

			this.content = content;
		}

	}
}
