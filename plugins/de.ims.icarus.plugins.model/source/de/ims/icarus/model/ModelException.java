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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import de.ims.icarus.model.api.Corpus;

/**
 * Defines an exception in the context of the model framework that is associated with
 * a {@link ModelError} and optionally has a live {@link Corpus} instance linked to it.
 * Note, however, that the corpus is only linked via a weak reference so that temporarily
 * kept instances of this exception type (e.g. for logging purposes) cannot prevent the
 * corpus in question from getting closed and {@code gc}ed.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ModelException extends RuntimeException {

	private static final long serialVersionUID = -3508678907020081630L;

	private final Reference<Corpus> source;
	private final ModelError error;
	private final boolean corpusSet;

	public ModelException(ModelError error, String message, Throwable cause) {

		if(error==null) {
			error = ModelError.UNKNOWN_ERROR;
		}

		this.source = null;
		this.error = error;
		this.corpusSet = false;
	}

	public ModelException(ModelError error, String message) {
		this(error, message, null);
	}

	public ModelException(String message) {
		this(null, message, null);
	}

	public ModelException(Corpus corpus, ModelError error, String message, Throwable cause) {
		super(message, cause);

		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		if(error==null) {
			error = ModelError.UNKNOWN_ERROR;
		}

		this.source = new WeakReference<>(corpus);
		this.error = error;
		this.corpusSet = true;
	}

	public ModelException(Corpus corpus, ModelError error, String message) {
		this(corpus, error, message, null);
	}

	public ModelException(Corpus corpus, String message) {
		this(corpus, null, message, null);
	}

	/**
	 * @return the corpus this exception is bound to
	 */
	public Corpus getCorpus() {
		return corpusSet ? source.get() : null;
	}

	public boolean wasCorpusSet() {
		return corpusSet;
	}

	/**
	 * @return the error
	 */
	public ModelError getError() {
		return error;
	}
}
