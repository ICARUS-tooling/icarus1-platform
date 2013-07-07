/*
 * $Revision: 46 $
 * $Date: 2013-06-13 12:32:58 +0200 (Do, 13 Jun 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/mpi/Commands.java $
 *
 * $LastChangedDate: 2013-06-13 12:32:58 +0200 (Do, 13 Jun 2013) $ 
 * $LastChangedRevision: 46 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.util.mpi;

/**
 * A collection of commonly used commands.
 * 
 * @author Markus Gärtner
 * @version $Id: Commands.java 46 2013-06-13 10:32:58Z mcgaerty $
 *
 */
public interface Commands {

	/**
	 * Perform a selective action on the current data according to
	 * the transmitted hint. Typical parameters are an integer
	 * index pointing to the data item that has to be selected
	 * or the item itself so that the target handler has to find
	 * the matching index itself.
	 */
	public static final String SELECT = "select"; //$NON-NLS-1$
	
	/**
	 * Makes an arbitrary kind of visualization based on the
	 * data parameter. Unlike the {@link #PRESENT} command this
	 * does not imply the option of interaction from the user side.
	 * It is up to the target implementation how much the user will
	 * be allowed to interact with the data.
	 */
	public static final String DISPLAY = "display"; //$NON-NLS-1$
	
	/**
	 * Makes an arbitrary kind of visualization based on the
	 * data parameter. If the data being passed as parameter is mutable
	 * then the user should be presented with tools and options that allow
	 * him to modify the data.
	 */
	public static final String PRESENT = "present"; //$NON-NLS-1$
	
	/**
	 * Starts an 'edit' operation on the data parameter. Unlike the
	 * {@link #PRESENT} command that also allows user-side modifications
	 * of data the data is not necessarily visualized to the user, he only
	 * has to be presented a collection of low-level tools to access its state. 
	 */
	public static final String EDIT = "edit"; //$NON-NLS-1$

	/**
	 * All internal data should be reverted to some known 'default'
	 * state. This includes all performed visualizations.
	 */
	public static final String CLEAR = "clear"; //$NON-NLS-1$

	public static final String GET = "get"; //$NON-NLS-1$
	public static final String SET = "set"; //$NON-NLS-1$

	/**
	 * 
	 */
	public static final String GET_TEXT = "get-text"; //$NON-NLS-1$
	public static final String SET_TEXT = "set-text"; //$NON-NLS-1$
	public static final String APPEND = "append"; //$NON-NLS-1$
	
	/**
	 * Tells a target object that is assigned the task of modifying
	 * some data to instantly commit any pending changes to that data structure.
	 * This command is typically used to synchronize independent editor
	 * implementations that operate on a common data structure.
	 */
	public static final String COMMIT = "commit"; //$NON-NLS-1$
}
