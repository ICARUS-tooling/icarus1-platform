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

import java.util.Date;
import java.util.List;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface CorpusManifest extends MemberManifest {

	ContextManifest getDefaultContextManifest();

	List<ContextManifest> getCustomContextManifests();

	ContextManifest getContextManifest(String id);

	void addCustomContextManifest(ContextManifest manifest);

	void removeCustomContextManifest(ContextManifest manifest);

	/**
	 * Returns the notes added to this corpus. The order is not specified and
	 * may be random. However, most times it will be convenient to have the
	 * notes sorted in lexicographical order of their titles or in chronological
	 * order according to the dates of their last individual modifications.
	 *
	 * @return
	 */
	List<Note> getNotes();

	void addNote(Note note);
	void removeNote(Note note);

	/**
	 * Returns {@code true} if the corpus described by this manifest can
	 * be edited by the user.
	 *
	 * @return
	 */
	boolean isEditable();

	/**
	 *
	 * @param value
	 */
	void setEditable(boolean value);

	/**
	 * Notes are user made textual additions that are saved together with the corpus manifest.
	 * They allow the storage of information outside predefined options and/or properties and
	 * can hold arbitrary text, but are limited to 10.000 characters. Each note is given a name
	 * that serves as a title for the content text. Those names do not have to be unique, however,
	 * they have to be non-empty.
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	interface Note {

		public static final int MAX_CHARACTER_LIMIT = 10_000;

		/**
		 * Returns the date of the last modification. If there have not been any modifications since
		 * the note was created, this method will return the date of the note's creation.
		 * @return
		 */
		Date getModificationDate();

		/**
		 * Returns the title of this note. The returned {@code String} is always non-null and never empty.
		 * @return
		 */
		String getName();

		/**
		 * Returns the (potentially) empty content of this note.
		 * @return
		 */
		String getContent();
	}
}
