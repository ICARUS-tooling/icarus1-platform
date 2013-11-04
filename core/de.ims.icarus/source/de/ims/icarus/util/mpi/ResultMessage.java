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
package de.ims.icarus.util.mpi;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ResultMessage {
	
	private final Throwable throwable;
	private final Message message;
	private final ResultType type;
	private final Object data;
	private final Object source;

	/**
	 * Generates a result message that indicates the request
	 * defined by {@code message} could not be performed because
	 * of the given {@code throwable}.
	 */
	public ResultMessage(Object source, Message message, Throwable throwable) {
		this(source, ResultType.REQUEST_FAILED, message, null, throwable);
	}

	/**
	 * Generates a result message that indicates successful
	 * handling of the request defined by the given {@code message}
	 */
	public ResultMessage(Object source, Message message, Object data) {
		this(source, ResultType.REQUEST_SUCCESSFUL, message, data, null);
	}

	/**
	 * Generates a result message that indicates an unknown request.
	 */
	public ResultMessage(Object source, Message message) {
		this(source, ResultType.UNKNOWN_REQUEST, message, null, null);
	}
	
	public ResultMessage(Object source, ResultType type, Message message) {
		this(source, type, message, null, null);
	}

	public ResultMessage(Object source, ResultType type, Message message, Object data, Throwable throwable) {
		if(source==null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$
		if(message==null)
			throw new NullPointerException("Invalid message"); //$NON-NLS-1$
		if(type==null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$
		
		this.source = source;
		this.message = message;
		this.throwable = throwable;
		this.type = type;
		this.data = data;
	}
	
	public Object getSource() {
		return source;
	}

	/**
	 * @return the throwable
	 */
	public Throwable getThrowable() {
		return throwable;
	}

	/**
	 * @return the message
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * @return the type
	 */
	public ResultType getType() {
		return type;
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	public enum ResultType {
		REQUEST_SUCCESSFUL,
		UNKNOWN_RECEIVER,
		UNKNOWN_REQUEST,
		UNSUPPORTED_DATA,
		REQUEST_FAILED,
	}
}
