/*
 * $Revision: 33 $
 * $Date: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/mpi/ResultMessage.java $
 *
 * $LastChangedDate: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $ 
 * $LastChangedRevision: 33 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.util.mpi;


/**
 * @author Markus Gärtner
 * @version $Id: ResultMessage.java 33 2013-05-13 12:33:31Z mcgaerty $
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
			throw new IllegalArgumentException("Invalid source"); //$NON-NLS-1$
		if(message==null)
			throw new IllegalArgumentException("Invalid message"); //$NON-NLS-1$
		if(type==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		
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
