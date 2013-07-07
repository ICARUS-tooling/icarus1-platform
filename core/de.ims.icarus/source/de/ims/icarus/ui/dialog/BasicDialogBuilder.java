/*
 * $Revision: 60 $
 * $Date: 2013-07-06 18:59:47 +0200 (Sa, 06 Jul 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/dialog/BasicDialogBuilder.java $
 *
 * $LastChangedDate: 2013-07-06 18:59:47 +0200 (Sa, 06 Jul 2013) $ 
 * $LastChangedRevision: 60 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui.dialog;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import de.ims.icarus.Core;
import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id: BasicDialogBuilder.java 60 2013-07-06 16:59:47Z mcgaerty $
 *
 */
public class BasicDialogBuilder extends DialogBuilder {
	
	protected JOptionPane optionPane;
	
	protected String title = ""; //$NON-NLS-1$

	/**
	 * @param resourceDomain
	 */
	public BasicDialogBuilder(ResourceDomain resourceDomain) {
		super(resourceDomain);
		initOptionPane();
	}

	/**
	 * 
	 */
	public BasicDialogBuilder() {
		super();
		initOptionPane();
	}
	
	protected void initOptionPane() {
		optionPane = new JOptionPane(null);
		optionPane.setOptionType(JOptionPane.DEFAULT_OPTION);
		optionPane.setMessageType(JOptionPane.PLAIN_MESSAGE);
	}
	
	public JOptionPane getOptionPane() {
		return optionPane;
	}
	
	public void setTitle(String title) {
		Exceptions.testNullArgument(title, "title"); //$NON-NLS-1$
		if(resourceDomain!=null)
			title = resourceDomain.get(title);
		
		this.title = title;
	}
	
	public void setText(String text) {
		setText(text, (Object[])null);
	}
	
	public void setText(String text, Object...params) {
		Exceptions.testNullArgument(text, "text"); //$NON-NLS-1$
		if(resourceDomain!=null)
			text = resourceDomain.get(text, params);
		
		optionPane.setMessage(text);
	}
	
	public void setOptions(Object...options) {
		if(resourceDomain!=null) {
			for(int i=0; i<options.length; i++) {
				if(options[i] instanceof String)
					options[i] = resourceDomain.get((String)options[i]);
			}
		}
		
		optionPane.setOptions(options);
	}
	
	public void setMessage(Object message, Object...params) {
		if(resourceDomain!=null && message instanceof String) {
			message = resourceDomain.get((String) message, params);
		}
		
		optionPane.setMessage(message);
	}
	
	public void addMessage(Object message, Object...params) {
		if(resourceDomain!=null && message instanceof String) {
			message = resourceDomain.get((String) message, params);
		}
		
		Object currentMessage = optionPane.getMessage();
		Object[] newMessage = null;
		if(currentMessage==null) {
			// No message set so far
			newMessage = new Object[]{message};
		} else if(currentMessage instanceof Object[]) {
			// Already multiple messages present in the pane
			// -> append our new option object
			int length = ((Object[])currentMessage).length;
			newMessage = new Object[length+1];
			System.arraycopy(currentMessage, 0, newMessage, 0, length);
			newMessage[length] = message;
		} else {
			// Exactly one message defined so far
			newMessage = new Object[]{currentMessage, message};
		}
		
		optionPane.setMessage(newMessage);
	}
	
	public void setIcon(Icon icon) {
		optionPane.setIcon(icon);
	}
	
	public void setInitialValue(Object value) {
		optionPane.setInitialValue(value);
	}
	
	public void setInitialSelectionValue(Object value) {
		optionPane.setInitialSelectionValue(value);
	}
	
	public void setOptionType(int type) {
		optionPane.setOptionType(type);
	}
	
	public void setMessageType(int type) {
		optionPane.setMessageType(type);
	}
	
	public void setWarningType() {
		setMessageType(JOptionPane.WARNING_MESSAGE);
	}
	
	public void setErrorType() {
		setMessageType(JOptionPane.ERROR_MESSAGE);
	}
	
	public void setInfoType() {
		setMessageType(JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void setQuestionType() {
		setMessageType(JOptionPane.QUESTION_MESSAGE);
	}
	
	public void setPlainType() {
		setMessageType(JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * @see de.ims.icarus.ui.dialog.DialogBuilder#showDialog(java.awt.Window)
	 */
	@Override
	public void showDialog(Component parent, Options options) { 
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		JDialog dialog = optionPane.createDialog(parent, title);
		dialog.setIconImages(Core.getIconImages());
		
		// Apply options
		dialog.setResizable(options.get(RESIZABLE_OPTION, false));
		
		dialog.setVisible(true);
		dialog.dispose();
	}

	public void showDialog(Component parent) {
		showDialog(parent, null);
	}
	
	public Object getSelectedValue() {
		return optionPane.getValue();
	}
	
	public int getValue() {
		Object selectedValue = optionPane.getValue();		
		Object[] options = optionPane.getOptions();

        if(selectedValue == null)
            return JOptionPane.CLOSED_OPTION;
        if(options == null) {
            if(selectedValue instanceof Integer)
                return ((Integer)selectedValue).intValue();
            return JOptionPane.CLOSED_OPTION;
        }
        for(int counter = 0, maxCounter = options.length;
            counter < maxCounter; counter++) {
            if(options[counter].equals(selectedValue))
                return counter;
        }
        return JOptionPane.CLOSED_OPTION;
	}
	
	public Object[] getSelectionValues() {
		return optionPane.getSelectionValues();
	}
	
	public boolean isOkValue() {
		return JOptionPane.OK_OPTION==getValue();
	}
	
	public boolean isYesValue() {
		return JOptionPane.YES_OPTION==getValue();
	}
	
	public boolean isNoValue() {
		return JOptionPane.NO_OPTION==getValue();
	}
	
	public boolean isCancelValue() {
		return JOptionPane.CANCEL_OPTION==getValue();
	}
}