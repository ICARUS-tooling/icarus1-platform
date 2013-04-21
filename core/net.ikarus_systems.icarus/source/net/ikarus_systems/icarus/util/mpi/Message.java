/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.mpi;

import net.ikarus_systems.icarus.util.Exceptions;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.mpi.ResultMessage.ResultType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Message {

	private final Object data;
	private final String command;
	private Options options;
	
	public Message(String command, Object data, Options options) {
		Exceptions.testNullArgument(command, "command"); //$NON-NLS-1$
		
		this.command = command;
		this.data = data;
		this.options = options;
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
	
	public ResultMessage errorResult(Throwable t) {
		return new ResultMessage(this, t);
	}
	
	public ResultMessage unsupportedDataResult() {
		return new ResultMessage(ResultType.UNSUPPORTED_DATA, this);
	}
	
	public ResultMessage unknownRequestResult() {
		return new ResultMessage(this);
	}
	
	public ResultMessage unknownReceiver() {
		return new ResultMessage(ResultType.UNKNOWN_RECEIVER, this);
	}
	
	public ResultMessage successResult(Object data) {
		return new ResultMessage(this, data);
	}
}
