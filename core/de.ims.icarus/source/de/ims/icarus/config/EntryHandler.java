/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/config/EntryHandler.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.config;

import java.awt.Component;
import java.awt.Dialog;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.ims.icarus.config.ConfigRegistry.EntryType;



/**
 * Presents a way to handle the modification of entries
 * in a configuration GUI (typically used for entries
 * of type {@link EntryType#CUSTOM}.<p>
 * 
 * When the GUI encounters an entry of type {@link EntryType#CUSTOM} 
 * (or a {@link EntryType#LIST} whose entry type is {@code EntryType#CUSTOM})
 * it should check the {@link ConfigConstants#HANDLER} property to see
 * if there is a custom handler present. If so it is recommended for the
 * GUI to use this handler, since otherwise it will not be able to
 * properly handle the value(s) in this entry!<p>
 * 
 * Usage of the {@code EntryHandler} should be done as follows:
 * 
 * When the user attempts to modify a value, the {@link EntryHandler#setValue(Object)}
 * method should be called with the current value as its argument. 
 * {@link EntryHandler#isValueEditable()} determines if the value is allowed to be
 * modified by the user. A return value of {@code false} indicates that the GUI should
 * take no further actions and simply ignore the user modification attempts. After that
 * invoking {@link EntryHandler#getComponent()} will return the {@code Component}
 * used by the {@code EntryHandler} to lay out its own user interface. This 
 * {@code Component} would normally be wrapped into a {@link Dialog} (either by using
 * {@code Dialog} or {@link JDialog} directly or by relying on one of the many
 * helper methods in {@link JOptionPane}) and presented to the user. However if 
 * the returned {@code Component} is a {@link JFileChooser} or {@link JColorChooser}
 * it should be directly displayed using its own {@code showDialog()} method.
 * Another option is to place the returned {@code Component} next to the components
 * used by the GUI to display the entry itself.<br>
 * No matter which way of displaying was used, once the user has modified the value
 * the {@code EntryHandler}'s {@link EntryHandler#isValueValid()} method should 
 * be called to check if the user input is valid and the modifications can be committed.
 * When the value is declared valid invoking {@link EntryHandler#getValue()} will commit 
 * the modifications to the value object stored in the handler and the modified object
 * will then be returned. In most cases just invoking this method is enough, since
 * the handler itself holds the entrie's value and directly operates on it.<p>
 * 
 * In addition to simply modify existing entries this interface presents a way
 * to create new objects usable as values for the type this {@code EntryHandler} is
 * used for. By calling {@link EntryHandler#newEntry()} a blank new entry is created.
 * This comes in hand when the handler is used in the context of a {@code EntryType#LIST}
 * entry and the user wants to add a new object to the list. Instead of using 
 * the currently selected value in the list (which may be {@code null}) the GUI should
 * call {@code EntryHandler#newEntry()} and pass the result to the {@code EntryHandler#setValue(Object)}
 * method and continue the way described above. 
 * 
 * @author Markus Gärtner
 * @version $Id: EntryHandler.java 7 2013-02-27 13:18:56Z mcgaerty $
 *
 */
public interface EntryHandler {

	public void setValue(Object value);
	
	public Object getValue();
	
	public Component getComponent();
	
	boolean isValueEditable();
	
	boolean isValueValid();
	
	public Object newEntry();
}
