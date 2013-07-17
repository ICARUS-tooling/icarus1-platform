/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui;

import java.beans.Statement;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.logging.Level;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.Exceptions;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class WeakHandler {

	private WeakReference<Object> target;
	private final String methodName;
	
	public WeakHandler(Object target, String methodName) {
		Exceptions.testNullArgument(target, "target"); //$NON-NLS-1$
		Exceptions.testNullArgument(methodName, "methodName"); //$NON-NLS-1$
		
		this.target = new WeakReference<Object>(target);
		this.methodName = methodName;
	}
	
	public Object getTarget() {
		if(target==null) {
			return null;
		}
		return target.get();
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public boolean isObsolete() {
		return getTarget()==null;
	}
	
	protected void dispatch(Object...args) {
		Statement statement = new Statement(getTarget(), methodName, args);
		try {
			statement.execute();
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, "Failed to execute handler statement: "+Arrays.toString(args), e); //$NON-NLS-1$
		}
	}
}
