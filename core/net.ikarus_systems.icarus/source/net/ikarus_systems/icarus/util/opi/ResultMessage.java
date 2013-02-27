/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.opi;


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

	/**
	 * Generates a result message that indicates the request
	 * defined by {@code message} could not be performed because
	 * of the given {@code throwable}.
	 */
	public ResultMessage(Message message, Throwable throwable) {
		this(ResultType.REQUEST_FAILED, message, null, throwable);
	}

	/**
	 * Generates a result message that indicates successful
	 * handling of the request defined by the given {@code message}
	 */
	public ResultMessage(Message message, Object data) {
		this(ResultType.REQUEST_SUCCESSFUL, message, data, null);
	}

	/**
	 * Generates a result message that indicates an unknown request.
	 */
	public ResultMessage(Message message) {
		this(ResultType.UNKNOWN_REQUEST, message, null, null);
	}
	
	public ResultMessage(ResultType type, Message message) {
		this(type, message, null, null);
	}

	public ResultMessage(ResultType type, Message message, Object data, Throwable throwable) {
		if(message==null)
			throw new IllegalArgumentException("Invalid message"); //$NON-NLS-1$
		if(type==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		
		this.message = message;
		this.throwable = throwable;
		this.type = type;
		this.data = data;
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
