/*
 * $Revision: 29 $
 * $Date: 2013-05-03 20:03:21 +0200 (Fr, 03 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/ui/WeakHandler.java $
 *
 * $LastChangedDate: 2013-05-03 20:03:21 +0200 (Fr, 03 Mai 2013) $ 
 * $LastChangedRevision: 29 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.ui;

import java.beans.Statement;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.logging.Level;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.util.Exceptions;

/**
 * @author Markus Gärtner
 * @version $Id: WeakHandler.java 29 2013-05-03 18:03:21Z mcgaerty $
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
