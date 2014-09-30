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
package de.ims.icarus.ui.view;

import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;

/**
 * A very abstract specification of an object that is capable of
 * visually {@code presenting} other objects. The term {@code present}
 * is used in a rather general manner. This interface only describes
 * the absolute basic methods required. It is up to extending interfaces
 * or implementing classes to provide functionality to actually access
 * the rendering components like {@code Component} or {@code Graphics}
 * instances.
 *
 * @author Markus Gärtner
 * @version $Id$
 * @see AWTPresenter
 *
 */
public interface Presenter {

	/**
	 * Checks whether an implementation is capable of presenting
	 * a certain {@code ContentType} instance.
	 */
	boolean supports(ContentType type);

	/**
	 * {@code Presents} the given {@code data} object using the
	 * {@code options} parameter. As a general rule {@code data}
	 * should never be {@code null}. If a program wants to erase
	 * the internal state of a {@code Presenter} it should call
	 * {@link #clear()} instead!
	 * <p>
	 * If this method returns without errors all subsequent calls
	 * to {@link #isPresenting()} must return {@code true} until
	 * {@link #clear()} is performed.
	 * @throws UnsupportedPresentationDataException
	 */
	void present(Object data, Options options) throws UnsupportedPresentationDataException;

	/**
	 * Erases all previously set presentation data so that later calls
	 * to {@link #isPresenting()} return {@code false} until new data
	 * is being set.
	 */
	void clear();

	/**
	 * Releases all underlying resources.
	 */
	void close();

	/**
	 * Returns {@code true} if valid data has been set for presentation
	 * and no call to {@link #clear()} has been performed since then.
	 */
	boolean isPresenting();

	/**
	 * Returns the currently presented data or {@code null} if no
	 * data is being presented right now. In the later case a
	 * previous call to {@link #isPresenting()} should have returned
	 * {@code false}.
	 */
	Object getPresentedData();
}
