/*
 * $Revision: 33 $
 * $Date: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/mpi/Message.java $
 *
 * $LastChangedDate: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $ 
 * $LastChangedRevision: 33 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.util.mpi;

import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.mpi.ResultMessage.ResultType;

/**
 * @author Markus Gärtner
 * @version $Id: Message.java 33 2013-05-13 12:33:31Z mcgaerty $
 *
 */
public class Message {

	private final Object data;
	private final String command;
	private final Object source;
	private Options options;
	
	public Message(Object source, String command, Object data, Options options) {
		Exceptions.testNullArgument(source, "source"); //$NON-NLS-1$
		Exceptions.testNullArgument(command, "command"); //$NON-NLS-1$
		
		this.source = source;
		this.command = command;
		this.data = data;
		this.options = options;
	}
	
	public Object getSource() {
		return source;
	}
	
	private Options getOptions0() {
		if(options==null)
			options = new Options();
		
		return options;
	}
	
	public void putOptions(Object... args) {
		getOptions0().putAll(args);
	}
	
	public void putOption(String key, Object value) {
		getOptions0().put(key, value);
	}
	
	public void addOptions(Options opts) {
		getOptions0().putAll(opts);
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @return the options
	 */
	public Options getOptions() {
		return options==null ? Options.emptyOptions : options;
	}
	
	public Object getOption(String key) {
		return options==null ? null : options.get(key);
	}
	
	public ResultMessage errorResult(Object source, Throwable t) {
		return new ResultMessage(source, this, t);
	}
	
	public ResultMessage unsupportedDataResult(Object source) {
		return new ResultMessage(source, ResultType.UNSUPPORTED_DATA, this);
	}
	
	public ResultMessage unknownRequestResult(Object source) {
		return new ResultMessage(source, this);
	}
	
	public ResultMessage unknownReceiver(Object source) {
		return new ResultMessage(source, ResultType.UNKNOWN_RECEIVER, this);
	}
	
	public ResultMessage successResult(Object source, Object data) {
		return new ResultMessage(source, this, data);
	}
}
