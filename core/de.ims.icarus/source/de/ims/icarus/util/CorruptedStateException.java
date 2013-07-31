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
package de.ims.icarus.util;

/**
 * Signals that some object reached a state in which it has no 
 * longer full control of its managed resources. This exception
 * is used for certain framework parts to forward encountered problems
 * in a way that enables the framework control to decide whether or
 * not a warning should be presented to the user suggesting him or her
 * to exit the program.
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class CorruptedStateException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 397341637226758734L;

	/**
	 * 
	 */
	public CorruptedStateException() {
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CorruptedStateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public CorruptedStateException(String message) {
		super(message);
	}

}
