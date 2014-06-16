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
package de.ims.icarus.language.treebank;

import java.util.Map;

import de.ims.icarus.io.Loadable;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.util.location.Location;


/**
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Treebank extends SentenceDataList, Identity, Loadable {

	boolean isEditable();

	void setName(String name);

	/**
	 * Replaces
	 */
	void set(SentenceData item, int index, DataType type);

	/**
	 * Removes from this {@code Treebank} the data stored at the
	 * specified {@code index} that is associated with the given
	 * {@code type}. The exact behavior in case of the {@code type}
	 * argument being {@code null} is implementation specific. The general
	 * advice is to remove all data stored for that index, but implementations
	 * might decide to ignore calls without a valid {@code DataType} parameter.
	 * In addition an implementation can remove all data for that index if
	 * data of some important type was removed. I.e. a typical {@code Treebank}
	 * representing parse or other annotation results created by an automated
	 * process would drop an entire index when data of the type {@value DataType#SYSTEM}
	 * is removed.
	 */
	void remove(int index, DataType type);

	void destroy();

	boolean isDestroyed();

	/**
	 *
	 * @return
	 */
	@Override
	boolean isLoaded();

	/**
	 * Loads this {@code Treebank}. In a typical synchronous
	 * implementation this method will cover the entire
	 * loading process and return once all data is loaded.
	 * For asynchronous implementations this method returns
	 * immediately and actual loading is done in a background
	 * thread.
	 * @throws Exception if the loading failed
	 */
	@Override
	void load() throws Exception;

	/**
	 * Sets the new {@code Location} to be used for this
	 * {@code Treebank}. Subsequent calls to {@link #isLoaded()}
	 * will return {@code false} at least until the first call
	 * to {@link #load()} is performed
	 * @param location the new {@code Location} to be used
	 */
	void setLocation(Location location);

	/**
	 * Returns the {@code Location} this {@code Treebank} is
	 * loading data from
	 * @return the {@code source} of this {@code Treebank}
	 */
	Location getLocation();

	/**
	 * Releases all data within this {@code Treebank} so that
	 * later calls to {@link #isLoaded()} return {@code false}.
	 */
	@Override
	void free();

	void saveState(TreebankDescriptor descriptor);

	void loadState(TreebankDescriptor descriptor);

	TreebankMetaData getMetaData();

	Object getProperty(String key);

	void setProperty(String key, Object value);

	Map<String, Object> getProperties();

	/**
	 * @see de.ims.icarus.ui.events.EventSource#addListener(java.lang.String, de.ims.icarus.ui.events.EventListener)
	 */
	void addListener(String eventName, EventListener listener);

	/**
	 * @see de.ims.icarus.ui.events.EventSource#removeEventListener(de.ims.icarus.ui.events.EventListener)
	 */
	void removeListener(EventListener listener);

	/**
	 * @see de.ims.icarus.ui.events.EventSource#removeEventListener(de.ims.icarus.ui.events.EventListener, java.lang.String)
	 */
	void removeListener(EventListener listener, String eventName);
}
