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
package de.ims.icarus.language.model.api;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusException extends RuntimeException {

	private static final long serialVersionUID = -3508678907020081630L;

	private final Reference<Corpus> source;
	private final CorpusError error;

	public CorpusException(Corpus corpus, CorpusError error, String message, Throwable cause) {
		super(message, cause);

		if(error==null) {
			error = CorpusError.UNKNOWN_ERROR;
		}

		this.source = corpus==null ? null : new WeakReference<>(corpus);
		this.error = error;
	}

	public CorpusException(Corpus corpus, CorpusError error, String message) {
		this(corpus, error, message, null);
	}

	public CorpusException(Corpus corpus, String message) {
		this(corpus, null, message, null);
	}

	/**
	 * @return the corpus this exception is bound to
	 */
	public Corpus getCorpus() {
		return source.get();
	}

	/**
	 * @return the error
	 */
	public CorpusError getError() {
		return error;
	}
}
